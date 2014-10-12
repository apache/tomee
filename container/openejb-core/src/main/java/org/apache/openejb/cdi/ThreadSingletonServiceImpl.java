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
import org.apache.openejb.cdi.transactional.TransactionContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.AppFinder;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.classloader.MultipleClassLoader;
import org.apache.webbeans.config.OpenWebBeansConfiguration;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.intercept.ApplicationScopedBeanInterceptorHandler;
import org.apache.webbeans.intercept.NormalScopedBeanInterceptorHandler;
import org.apache.webbeans.spi.BeanArchiveService;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.spi.ConversationService;
import org.apache.webbeans.spi.JNDIService;
import org.apache.webbeans.spi.LoaderService;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.ScannerService;
import org.apache.webbeans.spi.SecurityService;
import org.apache.webbeans.spi.TransactionService;
import org.apache.webbeans.spi.adaptor.ELAdaptor;
import org.apache.webbeans.web.intercept.RequestScopedBeanInterceptorHandler;

import javax.enterprise.inject.spi.DeploymentException;
import javax.transaction.Transactional;
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

    private String sessionContextClass;
    private boolean cachedApplicationScoped;

    //this needs to be static because OWB won't tell us what the existing SingletonService is and you can't set it twice.
    private static final ThreadLocal<WebBeansContext> contexts = new ThreadLocal<WebBeansContext>();
    private static final Map<ClassLoader, WebBeansContext> contextByClassLoader = new ConcurrentHashMap<ClassLoader, WebBeansContext>();
    private static final String WEBBEANS_FAILOVER_ISSUPPORTFAILOVER = "org.apache.webbeans.web.failover.issupportfailover";

    @Override
    public void initialize(final StartupObject startupObject) {
        if (sessionContextClass == null) { // done here cause Cdibuilder trigger this class loading and that's from Warmup so we can't init too early config
            synchronized (this) {
                if (sessionContextClass == null) {
                    sessionContextClass = SystemInstance.get().getProperty("openejb.session-context", "").trim();
                    cachedApplicationScoped = "true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.cdi.applicationScope.cached", "true").trim());
                }
            }
        }

        final AppContext appContext = startupObject.getAppContext();

        appContext.setCdiEnabled(hasBeans(startupObject.getAppInfo()));

        //initialize owb context, cf geronimo's OpenWebBeansGBean
        final Properties properties = new Properties();

        final Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();
        properties.setProperty(OpenWebBeansConfiguration.APPLICATION_IS_JSP, "true");
        properties.setProperty(OpenWebBeansConfiguration.USE_EJB_DISCOVERY, "true");
        //from CDI builder
        properties.setProperty(OpenWebBeansConfiguration.INTERCEPTOR_FORCE_NO_CHECKED_EXCEPTIONS, "false");
        properties.setProperty(SecurityService.class.getName(), ManagedSecurityService.class.getName());
        properties.setProperty(OpenWebBeansConfiguration.CONVERSATION_PERIODIC_DELAY, "1800000");
        properties.setProperty(OpenWebBeansConfiguration.APPLICATION_SUPPORTS_CONVERSATION, "true");
        properties.setProperty(OpenWebBeansConfiguration.IGNORED_INTERFACES, "org.apache.aries.proxy.weaving.WovenProxy");

        final String failoverService = startupObject.getAppInfo().properties.getProperty("org.apache.webbeans.spi.FailOverService",
            SystemInstance.get().getProperty("org.apache.webbeans.spi.FailOverService",
                null));
        if (failoverService != null) {
            properties.setProperty(OpenWebBeansConfiguration.IGNORED_INTERFACES, failoverService);
        }

        final boolean tomee = SystemInstance.get().getProperty("openejb.loader", "foo").startsWith("tomcat");

        final String defaultNormalScopeHandlerClass = NormalScopedBeanInterceptorHandler.class.getName();
        properties.setProperty("org.apache.webbeans.proxy.mapping.javax.enterprise.context.ApplicationScoped",
                cachedApplicationScoped ? ApplicationScopedBeanInterceptorHandler.class.getName() : defaultNormalScopeHandlerClass);

        if (tomee) {
            properties.setProperty("org.apache.webbeans.proxy.mapping.javax.enterprise.context.RequestScoped", RequestScopedBeanInterceptorHandler.class.getName());
        } else {
            properties.setProperty("org.apache.webbeans.proxy.mapping.javax.enterprise.context.RequestScoped", defaultNormalScopeHandlerClass);
        }

        if (sessionContextClass() != null && tomee) {
            properties.setProperty("org.apache.webbeans.proxy.mapping.javax.enterprise.context.SessionScoped", "org.apache.tomee.catalina.cdi.SessionNormalScopeBeanHandler");
        }

        if (SystemInstance.get().getOptions().get(WEBBEANS_FAILOVER_ISSUPPORTFAILOVER, false)) {
            properties.setProperty(WEBBEANS_FAILOVER_ISSUPPORTFAILOVER, "true");
        }

        properties.put(OpenWebBeansConfiguration.PRODUCER_INTERCEPTION_SUPPORT, SystemInstance.get().getProperty("openejb.cdi.producer.interception", "true"));

        properties.putAll(appContext.getProperties());

        services.put(BeanArchiveService.class, new OpenEJBBeanInfoService());
        services.put(AppContext.class, appContext);
        services.put(JNDIService.class, new OpenEJBJndiService());
        services.put(TransactionService.class, new OpenEJBTransactionService());
        services.put(ELAdaptor.class, new CustomELAdapter(appContext));
        services.put(ScannerService.class, new CdiScanner());
        final LoaderService loaderService = SystemInstance.get().getComponent(LoaderService.class);
        if (loaderService == null && !properties.containsKey(LoaderService.class.getName())) {
            services.put(LoaderService.class, new OptimizedLoaderService());
        } else if (loaderService != null) {
            services.put(LoaderService.class, loaderService);
        }

        optional(services, ConversationService.class, "org.apache.webbeans.jsf.DefaultConversationService");

        final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader cl;
        if (oldClassLoader != ThreadSingletonServiceImpl.class.getClassLoader() && ThreadSingletonServiceImpl.class.getClassLoader() != oldClassLoader.getParent()) {
            cl = new MultipleClassLoader(oldClassLoader, ThreadSingletonServiceImpl.class.getClassLoader());
        } else {
            cl = oldClassLoader;
        }
        Thread.currentThread().setContextClassLoader(cl);
        final WebBeansContext webBeansContext;
        Object old = null;
        try {
            if (startupObject.getWebContext() == null) {
                webBeansContext = new WebBeansContext(services, properties);
                appContext.set(WebBeansContext.class, webBeansContext);
            } else {
                webBeansContext = new WebappWebBeansContext(services, properties, appContext.getWebBeansContext());
                startupObject.getWebContext().setWebbeansContext(webBeansContext);
            }
            final BeanManagerImpl beanManagerImpl = webBeansContext.getBeanManagerImpl();
            beanManagerImpl.addContext(new TransactionContext());
            beanManagerImpl.addAdditionalInterceptorBindings(Transactional.class);

            SystemInstance.get().fireEvent(new WebBeansContextCreated(webBeansContext));
            OpenEJBTransactionService.class.cast(services.get(TransactionService.class)).setWebBeansContext(webBeansContext);

            // do it only here to get the webbeanscontext
            services.put(ContextsService.class, new CdiAppContextsService(webBeansContext, true));
            services.put(ResourceInjectionService.class, new CdiResourceInjectionService(webBeansContext));

            old = contextEntered(webBeansContext);
            setConfiguration(webBeansContext.getOpenWebBeansConfiguration());
            try {
                webBeansContext.getService(ContainerLifecycle.class).startApplication(startupObject);
            } catch (final Exception e) {
                throw new DeploymentException("couldn't start owb context", e);
            }
        } finally {
            contextExited(old);
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    private boolean hasBeans(final AppInfo appInfo) {
        for (final EjbJarInfo ejbJar : appInfo.ejbJars) {
            if (ejbJar.beans != null) {
                return true;
            }
        }
        return false;
    }

    private <T> void optional(final Map<Class<?>, Object> services, final Class<T> type, final String implementation) {
        try { // use TCCL since we can use webapp enrichment for services
            final Class clazz = Thread.currentThread().getContextClassLoader().loadClass(implementation);
            services.put(type, type.cast(clazz.newInstance()));
            logger.debug(String.format("CDI Service Installed: %s = %s", type.getName(), implementation));
        } catch (final ClassNotFoundException e) {
            logger.debug(String.format("CDI Service not installed: %s", type.getName()));
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    //not sure what openejb will need

    private void setConfiguration(final OpenWebBeansConfiguration configuration) {
        //from CDI builder
        configuration.setProperty(SecurityService.class.getName(), ManagedSecurityService.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.INTERCEPTOR_FORCE_NO_CHECKED_EXCEPTIONS, "false");
        // configuration.setProperty(OpenWebBeansConfiguration.APPLICATION_IS_JSP, "true");

        configuration.setProperty(OpenWebBeansConfiguration.CONTAINER_LIFECYCLE, OpenEJBLifecycle.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.TRANSACTION_SERVICE, OpenEJBTransactionService.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.SCANNER_SERVICE, CdiScanner.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.CONTEXTS_SERVICE, CdiAppContextsService.class.getName());
        configuration.setProperty(OpenWebBeansConfiguration.VALIDATOR_SERVICE, OpenEJBValidatorService.class.getName());
        configuration.setProperty(ResourceInjectionService.class.getName(), CdiResourceInjectionService.class.getName());
    }

    @Override
    public Object contextEntered(final WebBeansContext newOWBContext) {
        return enter(newOWBContext);
    }

    public static WebBeansContext enter(final WebBeansContext newOWBContext) {
        final WebBeansContext oldContext = contexts.get();
        if (newOWBContext != null) {
            contexts.set(newOWBContext);
        } else {
            contexts.remove();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Enter:'" + newOWBContext + "'");
        }

        return oldContext;
    }

    @Override
    public void contextExited(final Object oldContext) {
        exit(oldContext);
    }

    public static void exit(final Object oldContext) {
        if (oldContext != null && !(oldContext instanceof WebBeansContext)) {
            throw new IllegalArgumentException("ThreadSingletonServiceImpl can only be used with WebBeansContext, not " + oldContext.getClass().getName());
        }
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

        context = AppFinder.findAppContextOrWeb(cl, AppFinder.WebBeansContextTransformer.INSTANCE);
        if (context == null) {
            context = contexts.get();
            if (context == null) {
                // Fallback strategy is to just grab the first AppContext and assume it is the right one
                // This kind of algorithm could be greatly improved
                final List<AppContext> appContexts = SystemInstance.get().getComponent(ContainerSystem.class).getAppContexts();
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

    private static WebBeansContext getWebBeansContext(final List<AppContext> appContexts) {
        Collections.sort(appContexts, new Comparator<AppContext>() {
            @Override
            public int compare(final AppContext appContext, final AppContext appContext1) {
                return cdiSize(appContext1) - cdiSize(appContext);
            }
        });
        return appContexts.get(0).getWebBeansContext();
    }

    private static int cdiSize(final AppContext ctx) {
        final WebBeansContext wbc = ctx.getWebBeansContext();
        if (wbc == null) {
            return 0;
        }
        return wbc.getBeanManagerImpl().getBeans().size();
    }

    @Override
    public WebBeansContext get(final Object key) {
        return getContext((ClassLoader) key);
    }

    @Override
    public void clear(final Object key) {
        final WebBeansContext ctx = getContext((ClassLoader) key);
        if (logger.isDebugEnabled()) {
            logger.debug("Clearing:'" + ctx + "'");
        }
        contextByClassLoader.remove(key);
        ctx.clear();
    }

    @Override
    public String sessionContextClass() {
        if (!sessionContextClass.isEmpty()) {
            if ("http".equals(sessionContextClass)) { // easy way to manage this config
                return "org.apache.tomee.catalina.cdi.SessionContextBackedByHttpSession";
            }
            return sessionContextClass;
        }
        return null;
    }
}
