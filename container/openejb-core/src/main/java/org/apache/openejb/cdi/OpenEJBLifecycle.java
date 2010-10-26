/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.openejb.cdi;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import org.apache.openejb.BeanContext;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansFinder;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.container.InjectionResolver;
import org.apache.webbeans.corespi.ServiceLoader;
import org.apache.webbeans.ejb.common.util.EjbUtility;
import org.apache.webbeans.intercept.InterceptorData;
import org.apache.webbeans.jms.JMSManager;
import org.apache.webbeans.logger.WebBeansLogger;
import org.apache.webbeans.plugins.PluginLoader;
import org.apache.webbeans.portable.AnnotatedElementFactory;
import org.apache.webbeans.portable.events.ExtensionLoader;
import org.apache.webbeans.portable.events.ProcessAnnotatedTypeImpl;
import org.apache.webbeans.portable.events.discovery.BeforeShutdownImpl;
import org.apache.webbeans.proxy.JavassistProxyFactory;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.spi.ContextsService;
import org.apache.webbeans.spi.JNDIService;
import org.apache.webbeans.spi.ScannerService;
import org.apache.webbeans.util.WebBeansConstants;
import org.apache.webbeans.util.WebBeansUtil;
import org.apache.webbeans.xml.WebBeansXMLConfigurator;

/**
 * @version $Rev:$ $Date:$
 */
public class OpenEJBLifecycle implements ContainerLifecycle {

    //Logger instance
    protected WebBeansLogger logger = WebBeansLogger.getLogger(OpenEJBLifecycle.class);

    /**Discover bean classes*/
    protected ScannerService scannerService;

    protected final ContextsService contextsService;

    /**Deploy discovered beans*/
    private final BeansDeployer deployer;

    /**XML discovery. */
    //XML discovery is removed from the specification. It is here for next revisions of spec.
    private final WebBeansXMLConfigurator xmlDeployer;

    /**Using for lookup operations*/
    private final JNDIService jndiService;

    /**Root container.*/
    private final BeanManagerImpl beanManager;

    public OpenEJBLifecycle()
    {
        this(null);
    }

    public OpenEJBLifecycle(Properties properties)
    {
        beforeInitApplication(properties);

        this.beanManager = (BeanManagerImpl) WebBeansFinder.getSingletonInstance(BeanManagerImpl.class.getName());
        this.xmlDeployer = new WebBeansXMLConfigurator();
        this.deployer = new BeansDeployer();
        this.jndiService = ServiceLoader.getService(JNDIService.class);
        this.beanManager.setXMLConfigurator(this.xmlDeployer);
        this.scannerService = ServiceLoader.getService(ScannerService.class);
        this.contextsService = ServiceLoader.getService(ContextsService.class);

        initApplication(properties);
    }

    @Override
    public BeanManager getBeanManager()
    {
        return this.beanManager;
    }

    @Override
    public void startApplication(Object startupObject) throws Exception
    {
        StartupObject stuff = (StartupObject) startupObject;
        // Initalize Application Context
        logger.info(OWBLogConst.INFO_0005);

        long begin = System.currentTimeMillis();

        //Before Start
        beforeStartApplication(startupObject);

        //Load all plugins
        PluginLoader.getInstance().startUp();

        //Get Plugin
        CdiPlugin cdiPlugin = (CdiPlugin) PluginLoader.getInstance().getEjbPlugin();

        cdiPlugin.setAppContext(stuff.getAppContext());

        cdiPlugin.startup();

        //Configure EJB Deployments
        cdiPlugin.configureDeployments(stuff.getBeanContexts());

        //Resournce Injection Service
        CdiResourceInjectionService injectionService = (CdiResourceInjectionService) WebBeansFinder.getSingletonInstance(CdiResourceInjectionService.class.getName(), stuff.getAppContext().getClassLoader());
        injectionService.setAppModule(stuff.getAppInfo());
        injectionService.setClassLoader(stuff.getAppContext().getClassLoader());

        //Deploy the beans
        try {
            //Load Extensions
            ExtensionLoader.getInstance().loadExtensionServices();

            //Initialize contexts
            this.contextsService.init(startupObject);

            //Fire Event
            BeansDeployer.fireBeforeBeanDiscoveryEvent(beanManager);

            //Scanning process
            logger.debug("Scanning classpaths for beans artifacts.");

            //Scan
            this.scannerService.scan();

            //Deploy bean from XML. Also configures deployments, interceptors, decorators.

//            final CdiScanner cdiScanner = buildScanner();

            //Build injections for managed beans
            // TODO Maybe we should build injections after the bean discovery
            injectionService.buildInjections(scannerService.getBeanClasses());

            //Checking stereotype conditions
            BeansDeployer.checkStereoTypes(scannerService);

            //Configure Default Beans
            BeansDeployer.configureDefaultBeans();

            //Discover classpath classes
            deployManagedBeans(scannerService.getBeanClasses());

            for (BeanContext beanContext : stuff.getBeanContexts()) {
                if (!beanContext.getComponentType().isSession()) continue;

                final Class implClass = beanContext.getBeanClass();

                //Define annotation type
                AnnotatedType<?> annotatedType = AnnotatedElementFactory.getInstance().newAnnotatedType(implClass);

                //Fires ProcessAnnotatedType
                ProcessAnnotatedTypeImpl<?> processAnnotatedEvent = WebBeansUtil.fireProcessAnnotatedTypeEvent(annotatedType);

                // TODO Can you really veto an EJB?
                //if veto() is called
                if (processAnnotatedEvent.isVeto()) {
                    continue;
                }

                CdiEjbBean<Object> bean = new CdiEjbBean<Object>(beanContext);

                beanContext.set(CdiEjbBean.class, bean);

                beanContext.addSystemInterceptor(new CdiInterceptor(bean, beanManager, cdiPlugin.getContexsServices()));

                EjbUtility.fireEvents((Class<Object>) implClass, bean, (ProcessAnnotatedTypeImpl<Object>) processAnnotatedEvent);

                WebBeansUtil.setInjectionTargetBeanEnableFlag(bean);
            }

            //Check Specialization
            BeansDeployer.checkSpecializations(scannerService);

            //Fire Event
            BeansDeployer.fireAfterBeanDiscoveryEvent(beanManager);

            //Validate injection Points
            BeansDeployer.validateInjectionPoints(beanManager);

            for (BeanContext beanContext : stuff.getBeanContexts()) {
                if (!beanContext.getComponentType().isSession()) continue;
                final CdiEjbBean bean = beanContext.get(CdiEjbBean.class);

                // The interceptor stack is empty until validateInjectionPoints is called as it does more than validate.
                final List<InterceptorData> datas = bean.getInterceptorStack();

                final List<org.apache.openejb.core.interceptor.InterceptorData> converted = new ArrayList<org.apache.openejb.core.interceptor.InterceptorData>();
                for (InterceptorData data : datas) {
                    // todo this needs to use the code in InterceptorBindingBuilder that respects override rules and private methods
                    converted.add(org.apache.openejb.core.interceptor.InterceptorData.scan(data.getInterceptorClass()));
                }

                beanContext.setCdiInterceptors(converted);
            }

            //Fire Event
            BeansDeployer.fireAfterDeploymentValidationEvent(beanManager);
        } catch (Exception e1) {
            Assembler.logger.error("CDI Beans module deployment failed", e1);
            throw new RuntimeException(e1);
        }
        //Start actual starting on sub-classes
        afterStartApplication(startupObject);

        logger.info(OWBLogConst.INFO_0001, Long.toString(System.currentTimeMillis() - begin));
    }

    private static void deployManagedBeans(Set<Class<?>> beanClasses) {

        // Start from the class
        for (Class<?> implClass : beanClasses) {
            //Define annotation type
            AnnotatedType<?> annotatedType = AnnotatedElementFactory.getInstance().newAnnotatedType(implClass);

            //Fires ProcessAnnotatedType
            ProcessAnnotatedTypeImpl<?> processAnnotatedEvent = WebBeansUtil.fireProcessAnnotatedTypeEvent(annotatedType);

            //if veto() is called
            if (processAnnotatedEvent.isVeto()) {
                continue;
            }

            BeansDeployer.defineManagedBean((Class<Object>) implClass, (ProcessAnnotatedTypeImpl<Object>) processAnnotatedEvent);
        }
    }

    @Override
    public void stopApplication(Object endObject)
    {
        logger.debug("OpenWebBeans Container is stopping.");

        try
        {
            //Sub-classes operations
            beforeStopApplication(endObject);

            //Fire shut down
            this.beanManager.fireEvent(new BeforeShutdownImpl(), new Annotation[0]);

            //Destroys context
            this.contextsService.destroy(endObject);

            //Unbind BeanManager
            jndiService.unbind(WebBeansConstants.WEB_BEANS_MANAGER_JNDI_NAME);

            //Free all plugin resources
            PluginLoader.getInstance().shutDown();

            //Clear extensions
            ExtensionLoader.getInstance().clear();

            //Delete Resolutions Cache
            InjectionResolver.getInstance().clearCaches();

            //Delte proxies
            JavassistProxyFactory.getInstance().clear();

            //Delete AnnotateTypeCache
            AnnotatedElementFactory.getInstance().clear();

            //Delete JMS Model Cache
            JMSManager.getInstance().clear();

            //After Stop
            afterStopApplication(endObject);

            // Clear BeanManager
            this.beanManager.clear();

            // Clear singleton list
            WebBeansFinder.clearInstances(WebBeansUtil.getCurrentClassLoader());

        }
        catch (Exception e)
        {
            logger.error(OWBLogConst.ERROR_0021, e);
        }

    }

    /**
     * @return the logger
     */
    protected WebBeansLogger getLogger()
    {
        return logger;
    }

    /**
     * @return the scannerService
     */
    protected ScannerService getScannerService()
    {
        return scannerService;
    }

    /**
     * @return the contextsService
     */
    public ContextsService getContextService()
    {
        return contextsService;
    }

    /**
     * @return the deployer
     */
    protected BeansDeployer getDeployer()
    {
        return deployer;
    }

    /**
     * @return the xmlDeployer
     */
    protected WebBeansXMLConfigurator getXmlDeployer()
    {
        return xmlDeployer;
    }

    /**
     * @return the jndiService
     */
    protected JNDIService getJndiService()
    {
        return jndiService;
    }

    @Override
    public void initApplication(Properties properties)
    {
        afterInitApplication(properties);
    }

    protected void beforeInitApplication(Properties properties)
    {
        //Do nothing as default
    }

    protected void afterInitApplication(Properties properties)
    {
        //Do nothing as default
    }

    protected void afterStartApplication(Object startupObject) throws Exception
    {
        //Do nothing as default
    }

    protected void afterStopApplication(Object stopObject) throws Exception
    {
        //Do nothing as default
    }

    protected void beforeStartApplication(Object startupObject) throws Exception
    {
        //Do nothing as default
    }

    protected void beforeStopApplication(Object stopObject) throws Exception
    {
        //Do nothing as default
    }
}
