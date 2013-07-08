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
import org.apache.cxf.bus.CXFBusImpl;
import org.apache.cxf.bus.managers.BindingFactoryManagerImpl;
import org.apache.cxf.configuration.spring.MapProvider;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.endpoint.AbstractEndpointFactory;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.AbstractBasicInterceptorProvider;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.util.ServiceConfiguration;
import org.apache.openejb.assembler.classic.util.ServiceInfos;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.PropertiesHelper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public final class CxfUtil {
    public static final String ENDPOINT_PROPERTIES = "properties";
    public static final String FEATURES = "features";
    public static final String IN_INTERCEPTORS = "in-interceptors";
    public static final String IN_FAULT_INTERCEPTORS = "in-fault-interceptors";
    public static final String OUT_INTERCEPTORS = "out-interceptors";
    public static final String OUT_FAULT_INTERCEPTORS = "out-fault-interceptors";
    public static final String DATABINDING = "databinding";
    public static final String ADDRESS = "address";
    public static final String DEBUG = "debug";
    public static final String BUS_PREFIX = "org.apache.openejb.cxf.bus.";
    public static final String BUS_CONFIGURED_FLAG = "openejb.cxf.bus.configured";
    private static final Map<String, BindingFactory> bindingFactoryMap = new ConcurrentHashMap<String, BindingFactory>(8, 0.75f, 4);
    private static final Bus DEFAULT_BUS = initDefaultBus(); // has to be initializd after bindingFactoryMap
    private static volatile boolean usingBindingFactoryMap = false;

    private CxfUtil() {
        // no-op
    }

    public static boolean hasService(final String name) {
        return usingBindingFactoryMap && bindingFactoryMap.containsKey(name);
    }

    private static Bus initDefaultBus() {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CxfUtil.class.getClassLoader());
        try { // create the bus reusing cxf logic but skipping factory lookup
            final Bus bus = BusFactory.newInstance(CXFBusFactory.class.getName()).createBus();
            final BindingFactoryManager bfm = bus.getExtension(BindingFactoryManager.class);

            if (BindingFactoryManagerImpl.class.isInstance(bfm) && !usingBindingFactoryMap) {
                BindingFactoryManagerImpl.class.cast(bfm).setMapProvider(new MapProvider<String, BindingFactory>() {
                    @Override
                    public Map<String, BindingFactory> createMap() {
                        usingBindingFactoryMap = true;
                        return bindingFactoryMap;
                    }
                });
            }

            // ensure client proxies can use app classes
            CXFBusFactory.setDefaultBus(Bus.class.cast(Proxy.newProxyInstance(CxfUtil.class.getClassLoader(), new Class<?>[]{ Bus.class }, new ClientAwareBusHandler())));

            return bus; // we keep as internal the real bus and just expose to cxf the client aware bus to be able to cast it easily
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    public static Bus getBus() {
        return DEFAULT_BUS;
    }

    @Deprecated // no more useful since we create it once
    public static Bus getDefaultBus() {
        return getBus();
    }

    public static ClassLoader initBusLoader() {
        final ClassLoader loader = CxfUtil.getBus().getExtension(ClassLoader.class);
        if (loader != null) {
            if (CxfContainerClassLoader.class.isInstance(loader)) {
                CxfContainerClassLoader.class.cast(loader).tccl(Thread.currentThread().getContextClassLoader());
            }
            return loader;
        }
        return Thread.currentThread().getContextClassLoader();
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
            final List<?> features = createFeatures(availableServices, featuresIds);
            svrFactory.setFeatures((List<AbstractFeature>) features);
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
    }

    private static void configureInterceptors(final AbstractBasicInterceptorProvider abip, final String prefix, final Collection<ServiceInfo> availableServices, final Properties beanConfig) {
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

    private static List<AbstractFeature> createFeatures(final Collection<ServiceInfo> availableServices, final String featuresIds) {
        final List<?> features = ServiceInfos.resolve(availableServices, featuresIds.split(","));
        for (Object instance : features) {
            if (!AbstractFeature.class.isInstance(instance)) {
                throw new OpenEJBRuntimeException("feature should inherit from " + AbstractFeature.class.getName());
            }
        }
        return (List<AbstractFeature>) features;
    }

    private static List<Interceptor<? extends Message>> createInterceptors(final Collection<ServiceInfo> availableServices, final String ids) {
        final List<?> instances = ServiceInfos.resolve(availableServices, ids.split(","));
        for (Object instance : instances) {
            if (!Interceptor.class.isInstance(instance)) {
                throw new OpenEJBRuntimeException("interceptors should implement " + Interceptor.class.getName());
            }
        }
        return (List<Interceptor<? extends Message>>) instances;
    }

    public static void configureBus() {
        if (SystemInstance.get().getProperties().containsKey(BUS_CONFIGURED_FLAG)) { // jaxws and jaxrs for instance
            return;
        }

        final Bus bus = getBus();

        // ensure cxf classes are loaded from container to avoid conflicts with app
        bus.setExtension(new CxfContainerClassLoader(), ClassLoader.class);

        if (bus instanceof CXFBusImpl) {
            final ServiceConfiguration configuration = new ServiceConfiguration(SystemInstance.get().getProperties(),
                    SystemInstance.get().getComponent(OpenEjbConfiguration.class).facilities.services);

            final CXFBusImpl busImpl = (CXFBusImpl) bus;
            final Collection<ServiceInfo> serviceInfos = configuration.getAvailableServices();
            final Properties properties = configuration.getProperties();
            if (properties == null || properties.isEmpty()) {
                return;
            }

            final String featuresIds = properties.getProperty(BUS_PREFIX + FEATURES);
            if (featuresIds != null) {
                final List<AbstractFeature> features = createFeatures(serviceInfos, featuresIds);
                if (features != null) {
                    features.addAll(busImpl.getFeatures());
                    busImpl.setFeatures(features);
                }
            }

            final Properties busProperties = ServiceInfos.serviceProperties(serviceInfos, properties.getProperty(BUS_PREFIX + ENDPOINT_PROPERTIES));
            if (busProperties != null) {
                busImpl.getProperties().putAll(PropertiesHelper.map(busProperties));
            }

            configureInterceptors(busImpl, BUS_PREFIX, serviceInfos, configuration.getProperties());

            SystemInstance.get().getProperties().setProperty(BUS_CONFIGURED_FLAG, "true");
        }
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
}
