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
import org.apache.webbeans.corespi.se.DefaultApplicationBoundaryService;
import org.apache.webbeans.intercept.ApplicationScopedBeanInterceptorHandler;
import org.apache.webbeans.intercept.NormalScopedBeanInterceptorHandler;
import org.apache.webbeans.intercept.SessionScopedBeanInterceptorHandler;
import org.apache.webbeans.spi.ApplicationBoundaryService;
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
import org.apache.webbeans.intercept.RequestScopedBeanInterceptorHandler;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.inject.spi.DeploymentException;
import javax.transaction.Transactional;

/**
 * @version $Rev:$ $Date:$
 */
public class ThreadSingletonServiceImpl implements ThreadSingletonService {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, ThreadSingletonServiceImpl.class);

    private Object lazyInit;
    private volatile boolean cachedApplicationScoped;
    private volatile boolean cachedRequestScoped;
    private volatile boolean cachedSessionScoped;

    //this needs to be static because OWB won't tell us what the existing SingletonService is and you can't set it twice.
    private static final ThreadLocal<WebBeansContext> contexts = new ThreadLocal<WebBeansContext>();
    private static final Map<ClassLoader, WebBeansContext> contextByClassLoader = new ConcurrentHashMap<ClassLoader, WebBeansContext>();

    @Override
    public void initialize(final StartupObject startupObject) {
        if (lazyInit == null) { // done here cause Cdibuilder trigger this class loading and that's from Warmup so we can't init too early config
            synchronized (this) {
                if (lazyInit == null) {
                    lazyInit = new Object();
                    cachedApplicationScoped = "true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.cdi.applicationScope.cached", "true").trim());
                    cachedRequestScoped = "true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.cdi.requestScope.cached", "true").trim());
                    cachedSessionScoped = "true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.cdi.sessionScope.cached", "true").trim());
                }
            }
        }

        final AppContext appContext = startupObject.getAppContext();

        appContext.setCdiEnabled(hasBeans(startupObject.getAppInfo()));

        //initialize owb context, cf geronimo's OpenWebBeansGBean
        final Properties properties = new Properties();

        properties.setProperty(OpenWebBeansConfiguration.APPLICATION_IS_JSP, "true");
        properties.setProperty(OpenWebBeansConfiguration.USE_EJB_DISCOVERY, "true");
        //from CDI builder
        properties.setProperty(OpenWebBeansConfiguration.INTERCEPTOR_FORCE_NO_CHECKED_EXCEPTIONS, "false");
        properties.setProperty(SecurityService.class.getName(), ManagedSecurityService.class.getName());
        properties.setProperty(OpenWebBeansConfiguration.CONVERSATION_PERIODIC_DELAY, "1800000");
        properties.setProperty(OpenWebBeansConfiguration.APPLICATION_SUPPORTS_CONVERSATION, "true");
        properties.setProperty(OpenWebBeansConfiguration.IGNORED_INTERFACES, "org.apache.aries.proxy.weaving.WovenProxy");

        final boolean tomee = SystemInstance.get().getProperty("openejb.loader", "foo").startsWith("tomcat");

        final String defaultNormalScopeHandlerClass = NormalScopedBeanInterceptorHandler.class.getName();
        properties.setProperty("org.apache.webbeans.proxy.mapping.javax.enterprise.context.ApplicationScoped",
                cachedApplicationScoped ? ApplicationScopedBeanInterceptorHandler.class.getName() : defaultNormalScopeHandlerClass);

        properties.setProperty("org.apache.webbeans.proxy.mapping.javax.enterprise.context.RequestScoped",
            tomee && cachedRequestScoped ? RequestScopedBeanInterceptorHandler.class.getName() : defaultNormalScopeHandlerClass);

        properties.setProperty("org.apache.webbeans.proxy.mapping.javax.enterprise.context.SessionScoped",
            tomee && cachedSessionScoped ? SessionScopedBeanInterceptorHandler.class.getName() : defaultNormalScopeHandlerClass);

        properties.put(OpenWebBeansConfiguration.PRODUCER_INTERCEPTION_SUPPORT, SystemInstance.get().getProperty("openejb.cdi.producer.interception", "true"));

        properties.putAll(appContext.getProperties());

        // services needing WBC as constructor param
        properties.put(ContextsService.class.getName(), CdiAppContextsService.class.getName());
        properties.put(ResourceInjectionService.class.getName(), CdiResourceInjectionService.class.getName());
        properties.put(TransactionService.class.getName(), OpenEJBTransactionService.class.getName());

        // NOTE: ensure user can extend/override all the services = set it only if not present in properties, see WebBeansContext#getService()
        final Map<Class<?>, Object> services = new HashMap<>();
        services.put(AppContext.class, appContext);
        if (!properties.containsKey(ApplicationBoundaryService.class.getName())) {
            services.put(ApplicationBoundaryService.class, new DefaultApplicationBoundaryService());
        }
        if (!properties.containsKey(ScannerService.class.getName())) {
            services.put(ScannerService.class, new CdiScanner());
        }
        if (!properties.containsKey(JNDIService.class.getName())) {
            services.put(JNDIService.class, new OpenEJBJndiService());
        }
        if (!properties.containsKey(BeanArchiveService.class.getName())) {
            services.put(BeanArchiveService.class, new OpenEJBBeanInfoService());
        }
        if (!properties.containsKey(ELAdaptor.class.getName())) {
            try {
                services.put(ELAdaptor.class, new CustomELAdapter(appContext));
            } catch (final NoClassDefFoundError noClassDefFoundError) {
                // no-op: no javax.el
            }
        }
        if (!properties.containsKey(LoaderService.class.getName())) {
            final LoaderService loaderService = SystemInstance.get().getComponent(LoaderService.class);
            if (loaderService == null && !properties.containsKey(LoaderService.class.getName())) {
                services.put(LoaderService.class, new OptimizedLoaderService(appContext.getProperties()));
            } else if (loaderService != null) {
                services.put(LoaderService.class, loaderService);
            }
        }

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

            // we want the same reference as the ContextsService if that's our impl
            if (webBeansContext.getOpenWebBeansConfiguration().supportsConversation()
                && "org.apache.webbeans.jsf.DefaultConversationService".equals(webBeansContext.getOpenWebBeansConfiguration().getProperty(ConversationService.class.getName()))) {
                webBeansContext.registerService(ConversationService.class, ConversationService.class.cast(webBeansContext.getService(ContextsService.class)));
            }

            final BeanManagerImpl beanManagerImpl = webBeansContext.getBeanManagerImpl();
            beanManagerImpl.addContext(new TransactionContext());
            webBeansContext.getInterceptorsManager().addInterceptorBindingType(Transactional.class);

            SystemInstance.get().fireEvent(new WebBeansContextCreated(webBeansContext));

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

    //not sure what openejb will need

    private void setConfiguration(final OpenWebBeansConfiguration configuration) {
        //from CDI builder
        setProperty(configuration, SecurityService.class.getName(), ManagedSecurityService.class.getName());
        setProperty(configuration, OpenWebBeansConfiguration.INTERCEPTOR_FORCE_NO_CHECKED_EXCEPTIONS, "false");
        // configuration.setProperty(OpenWebBeansConfiguration.APPLICATION_IS_JSP, "true");

        setProperty(configuration, OpenWebBeansConfiguration.CONTAINER_LIFECYCLE, OpenEJBLifecycle.class.getName());
        setProperty(configuration, OpenWebBeansConfiguration.TRANSACTION_SERVICE, OpenEJBTransactionService.class.getName());
        setProperty(configuration, OpenWebBeansConfiguration.SCANNER_SERVICE, CdiScanner.class.getName());
        setProperty(configuration, OpenWebBeansConfiguration.CONTEXTS_SERVICE, CdiAppContextsService.class.getName());
        setProperty(configuration, OpenWebBeansConfiguration.VALIDATOR_SERVICE, OpenEJBValidatorService.class.getName());
        setProperty(configuration, ResourceInjectionService.class.getName(), CdiResourceInjectionService.class.getName());
    }

    private void setProperty(final OpenWebBeansConfiguration configuration, final String name, final String value) {
        if (configuration.getProperty(name) == null) {
            configuration.setProperty(name, value);
        }
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
        if (ctx != null) {
            ctx.clear();
        }
    }
}
