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
import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.component.BuiltInOwbBean;
import org.apache.webbeans.component.SimpleProducerFactory;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.config.BeansDeployer;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.config.WebBeansFinder;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.el.ELContextStore;
import org.apache.webbeans.intercept.InterceptorResolutionService;
import org.apache.webbeans.portable.AbstractProducer;
import org.apache.webbeans.portable.InjectionTargetImpl;
import org.apache.webbeans.portable.ProviderBasedProducer;
import org.apache.webbeans.portable.events.discovery.BeforeShutdownImpl;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.spi.JNDIService;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.ScannerService;
import org.apache.webbeans.spi.adaptor.ELAdaptor;
import org.apache.webbeans.util.WebBeansConstants;
import org.apache.webbeans.util.WebBeansUtil;

import javax.el.ELResolver;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @version $Rev:$ $Date:$
 */
public class OpenEJBLifecycle implements ContainerLifecycle {
    public static final ThreadLocal<AppInfo> CURRENT_APP_INFO = new ThreadLocal<AppInfo>();

    //Logger instance
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_CDI, OpenEJBLifecycle.class);

    /**
     * Discover bean classes
     */
    protected ScannerService scannerService;

    protected final ContextsService contextsService;

    /**
     * Deploy discovered beans
     */
    private final BeansDeployer deployer;

    /**
     * Using for lookup operations
     */
    private final JNDIService jndiService;

    /**
     * Root container.
     */
    private final BeanManagerImpl beanManager;
    private final WebBeansContext webBeansContext;
    /**
     * Manages unused conversations
     */

    public OpenEJBLifecycle(final WebBeansContext webBeansContext) {
        this.webBeansContext = webBeansContext;

        this.beanManager = webBeansContext.getBeanManagerImpl();
        this.deployer = new BeansDeployer(webBeansContext);
        this.jndiService = webBeansContext.getService(JNDIService.class);
        this.scannerService = webBeansContext.getScannerService();
        this.contextsService = webBeansContext.getContextsService();
    }

    @Override
    public BeanManager getBeanManager() {
        return this.beanManager;
    }

    @Override
    public void startApplication(final Object startupObject) {
        if (ServletContextEvent.class.isInstance( startupObject)) {
            startServletContext(ServletContext.class.cast(getServletContext(startupObject))); // TODO: check it is relevant
            return;
        } else if (!StartupObject.class.isInstance(startupObject)) {
            logger.debug("startupObject is not of StartupObject type; ignored");
            return;
        }

        final StartupObject stuff = (StartupObject) startupObject;
        final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();

        // Initalize Application Context
        logger.info("OpenWebBeans Container is starting...");

        final long begin = System.currentTimeMillis();

        try {
            Thread.currentThread().setContextClassLoader(stuff.getClassLoader());

            final AppContext appContext = stuff.getAppContext();
            if (stuff.getWebContext() == null) { // do it before any other things to keep our singleton finder working
                appContext.setWebBeansContext(webBeansContext);
            }

            //Load all plugins
            webBeansContext.getPluginLoader().startUp();

            //Get Plugin
            final CdiPlugin cdiPlugin = (CdiPlugin) webBeansContext.getPluginLoader().getEjbPlugin();

            cdiPlugin.setClassLoader(stuff.getClassLoader());
            cdiPlugin.setWebBeansContext(webBeansContext);

            //Configure EJB Deployments
            cdiPlugin.configureDeployments(stuff.getBeanContexts());

            //Resournce Injection Service
            final CdiResourceInjectionService injectionService = (CdiResourceInjectionService) webBeansContext.getService(ResourceInjectionService.class);
            // todo use startupObject allDeployments to find Comp in priority (otherwise we can keep N times comps and loose time at injection time
            injectionService.setAppContext(stuff.getAppContext(), stuff.getBeanContexts() != null ? stuff.getBeanContexts() : Collections.<BeanContext>emptyList());

            //Deploy the beans
            CdiScanner cdiScanner = null;
            try {
                //Scanning process
                logger.debug("Scanning classpaths for beans artifacts.");

                if (CdiScanner.class.isInstance(scannerService)) {
                    cdiScanner = CdiScanner.class.cast(scannerService);
                    cdiScanner.setContext(webBeansContext);
                    cdiScanner.init(startupObject);
                } else {
                    cdiScanner = new CdiScanner();
                    cdiScanner.setContext(webBeansContext);
                    cdiScanner.init(startupObject);
                }

                //Scan
                this.scannerService.scan();

                // just to let us write custom CDI Extension using our internals easily
                CURRENT_APP_INFO.set(stuff.getAppInfo());

                addInternalBeans(); // before next event which can register custom beans (JAX-RS)
                SystemInstance.get().fireEvent(new WebBeansContextBeforeDeploy(webBeansContext));

                //Deploy bean from XML. Also configures deployments, interceptors, decorators.
                deployer.deploy(scannerService);
                contextsService.init(startupObject); // fire app event and also starts SingletonContext and ApplicationContext
            } catch (final Exception e1) {
                SystemInstance.get().getComponent(Assembler.class).logger.error("CDI Beans module deployment failed", e1);
                throw new OpenEJBRuntimeException(e1);
            } finally {
                CURRENT_APP_INFO.remove();
            }

            final Collection<Class<?>> ejbs = new ArrayList<>(stuff.getBeanContexts().size());
            for (final BeanContext bc : stuff.getBeanContexts()) {
                ejbs.add(bc.getManagedClass());

                final CdiEjbBean cdiEjbBean = bc.get(CdiEjbBean.class);
                if (cdiEjbBean == null) {
                    continue;
                }

                if (AbstractProducer.class.isInstance(cdiEjbBean)) {
                    AbstractProducer.class.cast(cdiEjbBean).defineInterceptorStack(cdiEjbBean, cdiEjbBean.getAnnotatedType(), cdiEjbBean.getWebBeansContext());
                }
                bc.mergeOWBAndOpenEJBInfo();
                bc.set(InterceptorResolutionService.BeanInterceptorInfo.class, InjectionTargetImpl.class.cast(cdiEjbBean.getInjectionTarget()).getInterceptorInfo());
                cdiEjbBean.initInternals();
            }

            //Start actual starting on sub-classes
            if (beanManager instanceof WebappBeanManager) {
                ((WebappBeanManager) beanManager).afterStart();
            }

            for (final Class<?> clazz : cdiScanner.getStartupClasses()) {
                if (ejbs.contains(clazz)) {
                    logger.debug("Skipping " + clazz.getName() + ", already registered as an EJB.");
                    continue;
                }
                starts(beanManager, clazz);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);

            // cleanup threadlocal used to enrich cdi context manually
            OptimizedLoaderService.ADDITIONAL_EXTENSIONS.remove();
        }

        logger.info("OpenWebBeans Container has started, it took {0} ms.", Long.toString(System.currentTimeMillis() - begin));
    }

    private void addInternalBeans() {
        beanManager.getInjectionResolver().clearCaches();

        if (!hasBean(beanManager, HttpServletRequest.class)) {
            beanManager.addInternalBean(new HttpServletRequestBean(webBeansContext));
        }
        if (!hasBean(beanManager, HttpSession.class)) {
            beanManager.addInternalBean(new InternalBean<>(webBeansContext, HttpSession.class, HttpSession.class));
        }
        if (!hasBean(beanManager, ServletContext.class)) {
            beanManager.addInternalBean(new InternalBean<>(webBeansContext, ServletContext.class, ServletContext.class));
        }

        beanManager.getInjectionResolver().clearCaches(); // hasBean() usage can have cached several things
    }

    private static boolean hasBean(final BeanManagerImpl beanManagerImpl, final Class<?> type) {
        return !beanManagerImpl.getInjectionResolver().implResolveByType(false, type).isEmpty();
    }

    private void starts(final BeanManager beanManager, final Class<?> clazz) {
        final Bean<?> bean = beanManager.resolve(beanManager.getBeans(clazz));

        logger.debug("Starting bean " + clazz.getName());
        if (!beanManager.isNormalScope(bean.getScope())) {
            throw new IllegalStateException("Unable to start bean " + clazz.getName() +
                    ", from " + CdiScanner.getLocation(clazz) +
                    ", with scope " + bean.getScope().getName() +
                    ". Only normal scoped beans can use @Startup (e.g. @ApplicationScoped)");
        }

        final CreationalContext<Object> creationalContext = beanManager.createCreationalContext(null);
        beanManager.getReference(bean, clazz, creationalContext).toString();
        // don't release now, will be done by the context - why we restrict it to normal scoped beans
    }

    @Override
    public void stopApplication(final Object endObject) {
        logger.debug("OpenWebBeans Container is stopping.");

        try {
            // Fire shut down
            if (WebappBeanManager.class.isInstance(beanManager)) {
                WebappBeanManager.class.cast(beanManager).beforeStop();
            }

            webBeansContext.getContextsService().endContext(RequestScoped.class, endObject);
            webBeansContext.getContextsService().endContext(ConversationScoped.class, endObject);
            webBeansContext.getContextsService().endContext(SessionScoped.class, endObject);
            webBeansContext.getContextsService().endContext(ApplicationScoped.class, endObject);
            webBeansContext.getContextsService().endContext(Singleton.class, endObject);

            // clean up the EL caches after each request
            ELContextStore elStore = ELContextStore.getInstance(false);
            if (elStore != null)
            {
                elStore.destroyELContextStore();
            }

            this.beanManager.fireEvent(new BeforeShutdownImpl(), true);

            // this will now even destroy the ExtensionBeans and other internal stuff
            this.contextsService.destroy(endObject);

            //Unbind BeanManager
            if (jndiService != null) {
                jndiService.unbind(WebBeansConstants.WEB_BEANS_MANAGER_JNDI_NAME);
            }

            //Free all plugin resources
            ((CdiPlugin) webBeansContext.getPluginLoader().getEjbPlugin()).clearProxies();
            webBeansContext.getPluginLoader().shutDown();

            //Clear extensions
            webBeansContext.getExtensionLoader().clear();

            //Delete Resolutions Cache
            beanManager.getInjectionResolver().clearCaches();

            //Delete AnnotateTypeCache
            webBeansContext.getAnnotatedElementFactory().clear();

            //After Stop
            //Clear the resource injection service
            final ResourceInjectionService injectionServices = webBeansContext.getService(ResourceInjectionService.class);
            if (injectionServices != null) {
                injectionServices.clear();
            }

            //Comment out for commit OWB-502
            //ContextFactory.cleanUpContextFactory();

            CdiAppContextsService.class.cast(contextsService).removeThreadLocals();

            WebBeansFinder.clearInstances(WebBeansUtil.getCurrentClassLoader());

            // Clear BeanManager
            this.beanManager.clear();

            // Clear singleton list
            WebBeansFinder.clearInstances(WebBeansUtil.getCurrentClassLoader());

        } catch (final Exception e) {
            logger.error("An error occured while stopping the container.", e);
        }

    }

    /**
     * @return the scannerService
     */
    protected ScannerService getScannerService() {
        return scannerService;
    }

    /**
     * @return the contextsService
     */
    public ContextsService getContextService() {
        return contextsService;
    }

    /**
     * @return the jndiService
     */
    protected JNDIService getJndiService() {
        return jndiService;
    }

    @Override
    public void initApplication(final Properties properties) {
        // no-op
    }

    public void startServletContext(final ServletContext servletContext) {
        initializeServletContext(servletContext, webBeansContext);
    }

    public static void initializeServletContext(final ServletContext servletContext, final WebBeansContext context) {
        if (context == null || !context.getBeanManagerImpl().isInUse()) {
            return;
        }

        final ELAdaptor elAdaptor = context.getService(ELAdaptor.class);
        final ELResolver resolver = elAdaptor.getOwbELResolver();
        //Application is configured as JSP
        if (context.getOpenWebBeansConfiguration().isJspApplication()) {
            logger.debug("Application is configured as JSP. Adding EL Resolver.");

            setJspELFactory(servletContext, resolver);
        }

        // Add BeanManager to the 'javax.enterprise.inject.spi.BeanManager' servlet context attribute
        servletContext.setAttribute(BeanManager.class.getName(), context.getBeanManagerImpl());
    }

    /**
     * On Tomcat we need to sometimes force a class load to get our hands on the JspFactory
     */
    private static void setJspELFactory(ServletContext startupObject, ELResolver resolver)
    {
        JspFactory factory = JspFactory.getDefaultFactory();
        if (factory == null)
        {
            try
            {
                try {
                    Class.forName("org.apache.jasper.servlet.JasperInitializer");
                } catch (final Throwable th) {
                    Class.forName("org.apache.jasper.compiler.JspRuntimeContext");
                }
                factory = JspFactory.getDefaultFactory();
            }
            catch (Exception e)
            {
                // ignore
            }

        }

        if (factory != null)
        {
            JspApplicationContext applicationCtx = factory.getJspApplicationContext(startupObject);
            applicationCtx.addELResolver(resolver);
        }
        else
        {
            logger.debug("Default JSPFactroy instance has not found. Skipping OWB JSP handling");
        }
    }


    /**
     * Returns servlet context otherwise throws exception.
     *
     * @param object object
     * @return servlet context
     */
    private Object getServletContext(Object object) {
        if (ServletContextEvent.class.isInstance(object)) {
            object = ServletContextEvent.class.cast(object).getServletContext();
            return object;
        }
        return object;
    }

    public static class InternalBean<T> extends BuiltInOwbBean<T> {
        private final String id;

        protected InternalBean(final WebBeansContext webBeansContext, final Class<T> api, final Class<?> type) {
            super(webBeansContext, WebBeansType.MANAGED, api,
                    new SimpleProducerFactory<T>(
                            new ProviderBasedProducer<>(webBeansContext, type, new OpenEJBComponentProvider(webBeansContext, type), false)));
            this.id = "openejb#container#" + api.getName();
        }

        @Override
        public boolean isPassivationCapable() {
            return true;
        }

        @Override
        protected String providedId() {
            return id;
        }

        @Override
        public Class<?> proxyableType() {
            return null;
        }
    }

    public static class HttpServletRequestBean extends InternalBean<HttpServletRequest> {
        private final Set<Type> types;

        protected HttpServletRequestBean(final WebBeansContext webBeansContext) {
            super(webBeansContext, HttpServletRequest.class, HttpServletRequest.class);
            this.types = new HashSet<>(); // here we need 2 types (+Object) otherwise decoratione etc fails
            this.types.add(HttpServletRequest.class);
            this.types.add(ServletRequest.class);
            this.types.add(Object.class);
        }

        @Override
        public Set<Type> getTypes() {
            return types;
        }
    }

    private static class OpenEJBComponentProvider<T> implements Provider<T>, Serializable {
        private Class<?> type;
        private transient WebBeansContext webBeansContext;

        public OpenEJBComponentProvider(final WebBeansContext webBeansContext, final Class<?> type) {
            this.webBeansContext = webBeansContext;
            this.type = type;
        }

        @Override
        public T get() {
            if (webBeansContext == null) {
                webBeansContext = WebBeansContext.currentInstance();
            }
            return (T) SystemInstance.get().getComponent(type);
        }

        Object readResolve() throws ObjectStreamException {
            return get();
        }
    }
}
