/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.openejb.cdi;

import org.apache.openejb.AppContext;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.classloader.MultipleClassLoader;
import org.apache.webbeans.config.OpenWebBeansConfiguration;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.intercept.ApplicationScopedBeanInterceptorHandler;
import org.apache.webbeans.intercept.NormalScopedBeanInterceptorHandler;
import org.apache.webbeans.proxy.Factory;
import org.apache.webbeans.proxy.ProxyFactory;
import org.apache.webbeans.proxy.javassist.JavassistFactory;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.spi.ConversationService;
import org.apache.webbeans.spi.LoaderService;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.ScannerService;
import org.apache.webbeans.spi.SecurityService;
import org.apache.webbeans.spi.TransactionService;
import org.apache.webbeans.spi.adaptor.ELAdaptor;
import org.apache.webbeans.web.intercept.RequestScopedBeanInterceptorHandler;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version $Rev:$ $Date:$
 */
public class ThreadSingletonServiceImpl implements ThreadSingletonService {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, ThreadSingletonServiceImpl.class);

    public static final String OPENEJB_OWB_PROXY_FACTORY = "openejb.owb.proxy-factory";

    //this needs to be static because OWB won't tell us what the existing SingletonService is and you can't set it twice.
    private static final ThreadLocal<WebBeansContext> contexts = new ThreadLocal<WebBeansContext>();
    private static final Map<ClassLoader, WebBeansContext> contextByClassLoader = new ConcurrentHashMap<ClassLoader, WebBeansContext>();
    private static final String WEBBEANS_FAILOVER_ISSUPPORTFAILOVER = "org.apache.webbeans.web.failover.issupportfailover";

    public ThreadSingletonServiceImpl() {
        // no-op
    }

    public static Factory owbProxyFactory() {
        if ("asm".equals(SystemInstance.get().getProperty(OPENEJB_OWB_PROXY_FACTORY))) {
            return new AsmFactory();
        }
        return new JavassistFactory();
    }

    @Override
    public void initialize(StartupObject startupObject) {
        AppContext appContext = startupObject.getAppContext();

        appContext.setCdiEnabled(hasBeans(startupObject.getAppInfo()));

        //initialize owb context, cf geronimo's OpenWebBeansGBean
        Properties properties = new Properties();
        Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();
        properties.setProperty(OpenWebBeansConfiguration.APPLICATION_IS_JSP, "true");
        properties.setProperty(OpenWebBeansConfiguration.USE_EJB_DISCOVERY, "true");
        //from CDI builder
        properties.setProperty(OpenWebBeansConfiguration.INTERCEPTOR_FORCE_NO_CHECKED_EXCEPTIONS, "false");
        properties.setProperty(SecurityService.class.getName(), ManagedSecurityService.class.getName());
        properties.setProperty(OpenWebBeansConfiguration.CONVERSATION_PERIODIC_DELAY, "1800000");
        properties.setProperty(OpenWebBeansConfiguration.APPLICATION_SUPPORTS_CONVERSATION, "true");
        properties.setProperty(OpenWebBeansConfiguration.IGNORED_INTERFACES, "org.apache.aries.proxy.weaving.WovenProxy");

        final String failoverService = startupObject.getAppInfo().properties.getProperty("org.apache.webbeans.spi.FailOverService",
                                            SystemInstance.get().getProperty("org.apache.webbeans.spi.FailOverService", (String) null));
        if (failoverService != null) {
            properties.setProperty(OpenWebBeansConfiguration.IGNORED_INTERFACES, failoverService);
        }

        properties.setProperty("org.apache.webbeans.proxy.mapping.javax.enterprise.context.ApplicationScoped", ApplicationScopedBeanInterceptorHandler.class.getName());
        if (SystemInstance.get().getProperty("openejb.loader", "foo").startsWith("tomcat")) {
            properties.setProperty("org.apache.webbeans.proxy.mapping.javax.enterprise.context.RequestScoped", RequestScopedBeanInterceptorHandler.class.getName());
        } else {
            properties.setProperty("org.apache.webbeans.proxy.mapping.javax.enterprise.context.RequestScoped", NormalScopedBeanInterceptorHandler.class.getName());
        }

        if (SystemInstance.get().getOptions().get(WEBBEANS_FAILOVER_ISSUPPORTFAILOVER, false)) {
            properties.setProperty(WEBBEANS_FAILOVER_ISSUPPORTFAILOVER, "true");
        }

        services.put(AppContext.class, appContext);
        services.put(TransactionService.class, new OpenEJBTransactionService());
        if (startupObject.getWebContext() == null) {
            services.put(ELAdaptor.class,new CustomELAdapter(appContext));
        } else {
            services.put(ELAdaptor.class,new CustomELAdapter(appContext, startupObject.getWebContext()));
        }
        services.put(ResourceInjectionService.class, new CdiResourceInjectionService());
        services.put(ScannerService.class, new CdiScanner());
        services.put(LoaderService.class, new OptimizedLoaderService());
        services.put(org.apache.webbeans.proxy.ProxyFactory.class, new ProxyFactory(owbProxyFactory()));

        optional(services, ConversationService.class, "org.apache.webbeans.jsf.DefaultConversationService");

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader cl;
        if (oldClassLoader != ThreadSingletonServiceImpl.class.getClassLoader() && ThreadSingletonServiceImpl.class.getClassLoader() != oldClassLoader.getParent()) {
            cl = new MultipleClassLoader(oldClassLoader, ThreadSingletonServiceImpl.class.getClassLoader());
        } else {
            cl = oldClassLoader;
        }
        Thread.currentThread().setContextClassLoader(cl);
        WebBeansContext webBeansContext;
        Object old = null;
        try {
            if (startupObject.getWebContext() == null) {
                webBeansContext = new WebBeansContext(services, properties);
                appContext.set(WebBeansContext.class, webBeansContext);
            } else {
                webBeansContext = new WebappWebBeansContext(services, properties, appContext.getWebBeansContext());
                startupObject.getWebContext().setWebbeansContext(webBeansContext);
            }

            // do it only here to get the webbeanscontext
            services.put(ContextsService.class, new CdiAppContextsService(webBeansContext, true));

            old = contextEntered(webBeansContext);
            setConfiguration(webBeansContext.getOpenWebBeansConfiguration());
            try {
                webBeansContext.getService(ContainerLifecycle.class).startApplication(startupObject);
            } catch (Exception e) {
                throw new OpenEJBRuntimeException("couldn't start owb context", e);
            }
        } finally {
            contextExited(old);
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    private boolean hasBeans(AppInfo appInfo) {
        for (EjbJarInfo ejbJar : appInfo.ejbJars) {
            if (ejbJar.beans != null) return true;
        }
        return false;
    }

    private <T> void optional(Map<Class<?>, Object> services, Class<T> type, String implementation) {
        try {
            Class clazz = this.getClass().getClassLoader().loadClass(implementation);
            services.put(type, type.cast(clazz.newInstance()));

            logger.debug(String.format("CDI Service Installed: %s = %s", type.getName(), implementation));
        } catch (ClassNotFoundException e) {
            logger.debug(String.format("CDI Service not installed: %s", type.getName()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    //not sure what openejb will need

    private void setConfiguration(OpenWebBeansConfiguration configuration) {
        //from CDI builder
        configuration.setProperty(SecurityService.class.getName(), ManagedSecurityService.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.INTERCEPTOR_FORCE_NO_CHECKED_EXCEPTIONS, "false");
//        configuration.setProperty(OpenWebBeansConfiguration.APPLICATION_IS_JSP, "true");

        configuration.setProperty(OpenWebBeansConfiguration.CONTAINER_LIFECYCLE, OpenEJBLifecycle.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.TRANSACTION_SERVICE, OpenEJBTransactionService.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.SCANNER_SERVICE, CdiScanner.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.CONTEXTS_SERVICE, CdiAppContextsService.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.VALIDATOR_SERVICE, OpenEJBValidatorService.class.getName());
        configuration.setProperty(ResourceInjectionService.class.getName(), CdiResourceInjectionService.class.getName());
    }

    @Override
    public Object contextEntered(WebBeansContext newOWBContext) {
        return enter(newOWBContext);
    }

    public static WebBeansContext enter(WebBeansContext newOWBContext) {
        WebBeansContext oldContext = contexts.get();
        contexts.set(newOWBContext);
        contextMessage(newOWBContext, "Enter:");
        return oldContext;
    }

    private static void contextMessage(WebBeansContext newOWBContext, String prefix) {
        if (logger.isDebugEnabled()) {
            logger.debug(prefix + "'" + newOWBContext + "'");
        }
    }

    @Override
    public void contextExited(Object oldContext) {
        exit(oldContext);
    }

    public static void exit(Object oldContext) {
        if (oldContext != null && !(oldContext instanceof WebBeansContext)) throw new IllegalArgumentException("ThreadSingletonServiceImpl can only be used with WebBeansContext, not " + oldContext.getClass().getName());
        contexts.set((WebBeansContext) oldContext);
    }

    private WebBeansContext getContext(final ClassLoader cl) {
        return get(cl);
    }

    /**
     * Generally contexts.get() is enough since we set the current context from a request (see webbeanslistener)
     * but sometimes matching the classloader is better (manager webapps of tomcat deploys for instance)
     * so here the algorithm:
     * 1) try to match with the classloader
     * 2) if not matched try to use the threadlocal
     * 3) (shouldn't happen) simply return the biggest webbeancontext
     *
     * @param cl the key (generally TCCL)
     * @return the webbeancontext matching the current context
     */
    public static WebBeansContext get(final ClassLoader cl) {
        WebBeansContext context = contextByClassLoader.get(cl);
        if (context != null) {
            return context;
        }

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        for (AppContext appContext : containerSystem.getAppContexts()) {
            if (appContext.getClassLoader().equals(cl)) {
                context = appContext.getWebBeansContext();
                break;
            }
            for (WebContext web : appContext.getWebContexts()) {
                if (web.getClassLoader().equals(cl)) {
                    if (web.getWebbeansContext() != null) { // ear
                        context = web.getWebbeansContext();
                        break;
                    } else { // war
                        context = appContext.getWebBeansContext();
                        break;
                    }
                }
            }
            if (context != null) {
                break;
            }
        }

        if (context == null) {
            context = contexts.get();
            if (context == null) {
                // Fallback strategy is to just grab the first AppContext and assume it is the right one
                // This kind of algorithm could be greatly improved
                final List<AppContext> appContexts = containerSystem.getAppContexts();
                if (appContexts.size() > 0) {
                    return getWebBeansContext(appContexts);
                }

                throw new IllegalStateException("On a thread without an initialized context nor a classloader mapping a deployed app");
            }
        } else { // some cache to avoid to browse each app each time
            contextByClassLoader.put(cl, context);
        }

        return context;
    }

    private static WebBeansContext getWebBeansContext(List<AppContext> appContexts) {
        Collections.sort(appContexts, new Comparator<AppContext>() {
            @Override
            public int compare(AppContext appContext, AppContext appContext1) {
                return appContext1.getWebBeansContext().getBeanManagerImpl().getBeans().size() - appContext.getWebBeansContext().getBeanManagerImpl().getBeans().size();
            }
        });
        return appContexts.get(0).getWebBeansContext();
    }

    @Override
    public WebBeansContext get(Object key) {
        return getContext((ClassLoader) key);
    }

    @Override
    public void clear(Object key) {
        final WebBeansContext ctx = getContext((ClassLoader) key);
        contextMessage(ctx, "clearing ");
        contextByClassLoader.remove(key);
        ctx.clear();
    }

}
