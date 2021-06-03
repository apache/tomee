/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.transport.util;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.ClassUnwrapper;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.endpoint.AbstractEndpointFactory;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.management.InstrumentationManager;
import org.apache.cxf.management.counters.CounterRepository;
import org.apache.cxf.management.jmx.InstrumentationManagerImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.CXFAuthenticator;
import org.apache.cxf.transport.http.HttpDestinationFactory;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.event.AssemblerBeforeApplicationDestroyed;
import org.apache.openejb.assembler.classic.event.AssemblerDestroyed;
import org.apache.openejb.assembler.classic.util.ServiceConfiguration;
import org.apache.openejb.assembler.classic.util.ServiceInfos;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.server.cxf.transport.OpenEJBHttpDestinationFactory;
import org.apache.openejb.server.cxf.transport.event.BusCreated;
import org.apache.openejb.util.PropertiesHelper;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.webbeans.intercept.DefaultInterceptorHandler;
import org.apache.webbeans.proxy.InterceptorHandler;
import org.apache.webbeans.proxy.OwbInterceptorProxy;
import org.apache.webbeans.util.ExceptionUtil;

import javax.management.MBeanServer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import static org.apache.webbeans.proxy.InterceptorDecoratorProxyFactory.FIELD_INTERCEPTOR_HANDLER;

public final class CxfUtil {
    public static final String ENDPOINT_PROPERTIES = "properties";
    public static final String FEATURES = "features";
    public static final String IN_INTERCEPTORS = "in-interceptors";
    public static final String IN_FAULT_INTERCEPTORS = "in-fault-interceptors";
    public static final String OUT_INTERCEPTORS = "out-interceptors";
    public static final String OUT_FAULT_INTERCEPTORS = "out-fault-interceptors";
    public static final String DATABINDING = "databinding";
    public static final String ADDRESS = "address";
    public static final String PUBLISHED_URL = "published-url";
    public static final String DEBUG = "debug";
    public static final String BUS_PREFIX = "org.apache.openejb.cxf.bus.";
    public static final String BUS_CONFIGURED_FLAG = "openejb.cxf.bus.configured";
    private static final AtomicReference<Bus> DEFAULT_BUS = new AtomicReference<>();
    private static final AtomicInteger USER_COUNT = new AtomicInteger();
    private static Map<String, BindingFactory> bindingFactoryMap;

    private CxfUtil() {
        // no-op
    }

    public static void release() { // symmetric of configureBus(), when last caller of configureBus() is calls this bus is destroyed
        if (USER_COUNT.decrementAndGet() == 0) {
            final Bus b = DEFAULT_BUS.get();
            if (b != null) {
                b.shutdown(true);
            }
        }
    }

    public static boolean hasService(final String name) {
        return bindingFactoryMap != null && bindingFactoryMap.containsKey(name);
    }

    private static Bus initDefaultBus() {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CxfUtil.class.getClassLoader());
        try { // create the bus reusing cxf logic but skipping factory lookup
            final Bus bus = BusFactory.newInstance(CXFBusFactory.class.getName()).createBus();
            bus.setId(SystemInstance.get().getProperty("openejb.cxf.bus.id", "openejb.cxf.bus"));

            final BindingFactoryManager bfm = bus.getExtension(BindingFactoryManager.class);
            bindingFactoryMap = (Map<String, BindingFactory>) Reflections.get(bfm, "bindingFactories");

            bus.setExtension(new OpenEJBHttpDestinationFactory(), HttpDestinationFactory.class);

            // ensure client proxies can use app classes
            CXFBusFactory.setDefaultBus(Bus.class.cast(Proxy.newProxyInstance(CxfUtil.class.getClassLoader(), new Class<?>[]{Bus.class}, new ClientAwareBusHandler())));

            bus.setProperty(ClassUnwrapper.class.getName(), new ClassUnwrapper() {
                @Override
                public Class<?> getRealClass(final Object o) {
                    final Class<?> aClass = o.getClass();
                    Class<?> c = aClass;
                    while (c.getName().contains("$$")) {
                        c = c.getSuperclass();
                    }
                    return c == Object.class ? aClass : c;
                }

                @Override
                public Class<?> getRealClassFromClass(Class<?> aClass) {
                    return aClass;
                }

                @Override
                public Object getRealObject(Object o) {
                    // special case for OWB proxies - ie, a webservice endpoint with a CDI interceptor
                    // we'll want to unwrap this and set the field on the proxied instance, rather than set the field
                    // straight on the proxy
                    if (o instanceof OwbInterceptorProxy) {
                        return getProxiedInstance(o);
                    }

                    return o;
                }

                private Object getProxiedInstance(Object o) {
                    try {
                        final Field field = o.getClass().getDeclaredField(FIELD_INTERCEPTOR_HANDLER);
                        field.setAccessible(true);

                        final Object fieldValue = field.get(o);

                        if (fieldValue instanceof DefaultInterceptorHandler) {
                            final DefaultInterceptorHandler handler = (DefaultInterceptorHandler) fieldValue;
                            return handler.getTarget();
                        } else {
                            throw new IllegalStateException("Expected OwbInterceptorProxy handler to be an instance of Default Interceptor Handler.");
                        }
                    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            SystemInstance.get().addObserver(new LifecycleManager());

            initAuthenticators();

            return bus; // we keep as internal the real bus and just expose to cxf the client aware bus to be able to cast it easily
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private static void initAuthenticators() { // TODO: drop when we get a fully supporting java 9 version of CXF
/*      Removing to bump to CXF 3.3.0 which supports Java 11  
        try {
            CXFAuthenticator.addAuthenticator();
        } catch (final RuntimeException re) {
            // we swallow it while cxf doesnt support java 9, this workaround is enough to make most of cases passing
        }*/
    }

    public static Bus getBus() {
        Bus bus = DEFAULT_BUS.get();
        if (bus == null) {
            synchronized (DEFAULT_BUS) { // synch could be better "in case off
                // " but with our lifecycle it is far enough since it is thread safe
                bus = DEFAULT_BUS.get();
                if (bus == null) {
                    bus = initDefaultBus();
                    DEFAULT_BUS.set(bus);
                }
            }
        }
        return bus;
    }

    public static ClassLoader initBusLoader() {
        final ClassLoader loader = CxfUtil.getBus().getExtension(ClassLoader.class);
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            if (CxfContainerClassLoader.class.isInstance(loader) && !CxfContainerClassLoader.class.isInstance(tccl)) {
                CxfContainerClassLoader.class.cast(loader).tccl(tccl);
            }
            return loader;
        }
        return tccl;
    }

    public static void clearBusLoader(final ClassLoader old) {
        final ClassLoader loader = CxfUtil.getBus().getExtension(ClassLoader.class);
        if (loader != null && CxfContainerClassLoader.class.isInstance(loader)
                && (old == null || !CxfContainerClassLoader.class.isInstance(old))) {
            CxfContainerClassLoader.class.cast(loader).clear();
        }
        Thread.currentThread().setContextClassLoader(old);
    }

    public static void configureEndpoint(final AbstractEndpointFactory svrFactory, final ServiceConfiguration configuration, final String prefix) {
        final Properties beanConfig = configuration.getProperties();
        if (beanConfig == null || beanConfig.isEmpty()) {
            return;
        }

        final Collection<ServiceInfo> availableServices = configuration.getAvailableServices();

        // endpoint properties
        final Properties properties = ServiceInfos.serviceProperties(availableServices, beanConfig.getProperty(prefix + ENDPOINT_PROPERTIES));
        if (properties != null) {
            svrFactory.setProperties(PropertiesHelper.map(properties));
        }

        final String debugKey = prefix + DEBUG;
        if ("true".equalsIgnoreCase(beanConfig.getProperty(debugKey, SystemInstance.get().getOptions().get(debugKey, "false")))) {
            svrFactory.getProperties(true).put("faultStackTraceEnabled", "true");
        }

        // endpoint features
        final String featuresIds = beanConfig.getProperty(prefix + FEATURES);
        if (featuresIds != null) {
            final List<? extends Feature> features = createFeatures(availableServices, featuresIds);
            svrFactory.setFeatures(features);
        }

        configureInterceptors(svrFactory, prefix, availableServices, beanConfig);

        // databinding
        final String databinding = beanConfig.getProperty(prefix + DATABINDING);
        if (databinding != null && !databinding.trim().isEmpty()) {
            Object instance = ServiceInfos.resolve(availableServices, databinding);
            if (instance == null) {  // maybe id == classname
                try {
                    instance = Thread.currentThread().getContextClassLoader().loadClass(databinding).newInstance();
                } catch (Exception e) {
                    // ignore
                }
            }

            if (!DataBinding.class.isInstance(instance)) {
                throw new OpenEJBRuntimeException(instance + " is not a " + DataBinding.class.getName()
                        + ", please check configuration of service [id=" + databinding + "]");
            }
            svrFactory.setDataBinding((DataBinding) instance);
        }

        // address: easier than using openejb-jar.xml
        final String changedAddress = beanConfig.getProperty(prefix + ADDRESS);
        if (changedAddress != null && !changedAddress.trim().isEmpty()) {
            svrFactory.setAddress(changedAddress);
        }

        // published url
        final String publishedUrl = beanConfig.getProperty(prefix + PUBLISHED_URL);
        if (publishedUrl != null && !publishedUrl.trim().isEmpty()) {
            svrFactory.setPublishedEndpointUrl(publishedUrl);
        }
    }

    public static void configureInterceptors(final InterceptorProvider abip, final String prefix, final Collection<ServiceInfo> availableServices, final Properties beanConfig) {
        // interceptors
        final String inInterceptorsIds = beanConfig.getProperty(prefix + IN_INTERCEPTORS);
        if (inInterceptorsIds != null && !inInterceptorsIds.trim().isEmpty()) {
            abip.getInInterceptors().addAll(createInterceptors(availableServices, inInterceptorsIds));
        }

        final String inFaultInterceptorsIds = beanConfig.getProperty(prefix + IN_FAULT_INTERCEPTORS);
        if (inFaultInterceptorsIds != null && !inFaultInterceptorsIds.trim().isEmpty()) {
            abip.getInFaultInterceptors().addAll(createInterceptors(availableServices, inFaultInterceptorsIds));
        }

        final String outInterceptorsIds = beanConfig.getProperty(prefix + OUT_INTERCEPTORS);
        if (outInterceptorsIds != null && !outInterceptorsIds.trim().isEmpty()) {
            abip.getOutInterceptors().addAll(createInterceptors(availableServices, outInterceptorsIds));
        }

        final String outFaultInterceptorsIds = beanConfig.getProperty(prefix + OUT_FAULT_INTERCEPTORS);
        if (outFaultInterceptorsIds != null && !outFaultInterceptorsIds.trim().isEmpty()) {
            abip.getOutFaultInterceptors().addAll(createInterceptors(availableServices, outFaultInterceptorsIds));
        }
    }

    public static List<Feature> createFeatures(final Collection<ServiceInfo> availableServices, final String featuresIds) {
        final List<?> features = ServiceInfos.resolve(availableServices, featuresIds.split(","));
        for (final Object instance : features) {
            if (!AbstractFeature.class.isInstance(instance)) {
                throw new OpenEJBRuntimeException("feature should inherit from " + AbstractFeature.class.getName());
            }
        }
        return (List<Feature>) features;
    }

    public static List<Interceptor<? extends Message>> createInterceptors(final Collection<ServiceInfo> availableServices, final String ids) {
        final List<?> instances = ServiceInfos.resolve(availableServices, ids.split(","));
        for (Object instance : instances) {
            if (!Interceptor.class.isInstance(instance)) {
                throw new OpenEJBRuntimeException("interceptors should implement " + Interceptor.class.getName());
            }
        }
        return (List<Interceptor<? extends Message>>) instances;
    }

    public static void configureBus() {
        if (USER_COUNT.incrementAndGet() > 1) {
            return;
        }

        final SystemInstance systemInstance = SystemInstance.get();

        final Bus bus = getBus();

        // ensure cxf classes are loaded from container to avoid conflicts with app
        if ("true".equalsIgnoreCase(systemInstance.getProperty("openejb.cxf.CxfContainerClassLoader", "true"))) {
            bus.setExtension(new CxfContainerClassLoader(), ClassLoader.class);
        }

        // activate jmx, by default isEnabled() == false in InstrumentationManagerImpl
        final boolean hasMonitoring = hasMonitoring(systemInstance);
        if (hasMonitoring || "true".equalsIgnoreCase(systemInstance.getProperty("openejb.cxf.jmx", "true"))) {
            final InstrumentationManager mgr = bus.getExtension(InstrumentationManager.class);
            if (InstrumentationManagerImpl.class.isInstance(mgr)) {
                bus.setExtension(LocalMBeanServer.get(), MBeanServer.class); // just to keep everything consistent

                final InstrumentationManagerImpl manager = InstrumentationManagerImpl.class.cast(mgr);
                manager.setEnabled(true);
                manager.setServer(LocalMBeanServer.get());

                try { // avoid to bother our nice logs
                    LogUtils.getL7dLogger(InstrumentationManagerImpl.class).setLevel(Level.WARNING);
                } catch (final Throwable th) {
                    // no-op
                }

                // failed when bus was constructed or even if passed we switch the MBeanServer
                manager.init();
            }
        }
        if (hasMonitoring) {
            new CounterRepository().setBus(bus);
        }

        final ServiceConfiguration configuration = new ServiceConfiguration(systemInstance.getProperties(),
                systemInstance.getComponent(OpenEjbConfiguration.class).facilities.services);

        final Collection<ServiceInfo> serviceInfos = configuration.getAvailableServices();
        Properties properties = configuration.getProperties();
        if (properties == null) {
            properties = new Properties();
        }

        final String featuresIds = properties.getProperty(BUS_PREFIX + FEATURES);
        if (featuresIds != null) {
            final List<Feature> features = createFeatures(serviceInfos, featuresIds);
            if (features != null) {
                features.addAll(bus.getFeatures());
                bus.setFeatures(features);
            }
        }

        final Properties busProperties = ServiceInfos.serviceProperties(serviceInfos, properties.getProperty(BUS_PREFIX + ENDPOINT_PROPERTIES));
        if (busProperties != null) {
            bus.getProperties().putAll(PropertiesHelper.map(busProperties));
        }

        configureInterceptors(bus, BUS_PREFIX, serviceInfos, configuration.getProperties());

        systemInstance.getProperties().setProperty(BUS_CONFIGURED_FLAG, "true");
        systemInstance.fireEvent(new BusCreated(bus));
    }

    private static boolean hasMonitoring(final SystemInstance systemInstance) {
        return "true".equalsIgnoreCase(systemInstance.getProperty("openejb.cxf.monitoring.jmx", "false"));
    }

    private static class ClientAwareBusHandler implements InvocationHandler {
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            final Bus bus = getBus();

            // when creating a client it is important to use the application loader to be able to load application classes
            // it is the default case but using our own CxfClassLoader we make it wrong so simply skip it when calling a client
            // and no app classloader is registered
            if ("getExtension".equals(method.getName()) && args != null && args.length == 1 && ClassLoader.class.equals(args[0])) {
                final ClassLoader extensionLoader = ClassLoader.class.cast(method.invoke(bus, args));
                if (CxfContainerClassLoader.class.isInstance(extensionLoader) && !CxfContainerClassLoader.class.cast(extensionLoader).hasTccl()) {
                    return null;
                }
                return extensionLoader;
            }

            return method.invoke(bus, args);
        }
    }

    public static class LifecycleManager {
        public void destroy(@Observes final AssemblerDestroyed ignored) {
            final SystemInstance systemInstance = SystemInstance.get();
            final Bus bus = getBus();
            if ("true".equalsIgnoreCase(systemInstance.getProperty("openejb.cxf.jmx", "true"))) {
                final InstrumentationManager mgr = bus.getExtension(InstrumentationManager.class);
                if (InstrumentationManagerImpl.class.isInstance(mgr)) {
                    mgr.shutdown();
                }
            }
            systemInstance.removeObserver(this);
        }

        public void destroy(@Observes final AssemblerBeforeApplicationDestroyed ignored) {
            final SystemInstance systemInstance = SystemInstance.get();
            final Bus bus = getBus();

            // avoid to leak, we can enhance it to remove endpoints by app but not sure it does worth the effort
            // alternative can be a bus per app but would enforce us to change some deeper part of our config/design
            if ("true".equalsIgnoreCase(systemInstance.getProperty("openejb.cxf.monitoring.jmx.clear-on-undeploy", "true"))) {
                final CounterRepository repo = bus.getExtension(CounterRepository.class);
                if (repo != null) {
                    repo.getCounters().clear();
                }
            }
        }
    }
}
