/**
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
package org.apache.openejb.assembler.classic;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NameAlreadyBoundException;
import javax.persistence.EntityManagerFactory;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.work.WorkManager;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.apache.geronimo.connector.GeronimoBootstrapContext;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.BeanType;
import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.DuplicateDeploymentIdException;
import org.apache.openejb.EnvProps;
import org.apache.openejb.Injection;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.core.ConnectorReference;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.SimpleTransactionSynchronizationRegistry;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.core.timer.EjbTimerServiceImpl;
import org.apache.openejb.core.timer.NullEjbTimerServiceImpl;
import org.apache.openejb.javaagent.Agent;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;
import org.apache.openejb.persistence.PersistenceClassLoaderHandler;
import org.apache.openejb.resource.GeronimoConnectionManagerFactory;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.OpenEJBErrorHandler;
import org.apache.openejb.util.SafeToolkit;
import org.apache.openejb.util.proxy.ProxyFactory;
import org.apache.openejb.util.proxy.ProxyManager;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.apache.xbean.recipe.StaticRecipe;
import org.apache.xbean.recipe.UnsetPropertiesRecipe;

public class Assembler extends AssemblerTool implements org.apache.openejb.spi.Assembler {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, Assembler.class);

    private final CoreContainerSystem containerSystem;
    private final PersistenceClassLoaderHandler persistenceClassLoaderHandler;
    private final JndiBuilder jndiBuilder;
    private TransactionManager transactionManager;
    private SecurityService securityService;
    private OpenEjbConfigurationFactory configFactory;
    private final Map<String, AppInfo> deployedApplications = new HashMap<String, AppInfo>();
    private final List<DeploymentListener> deploymentListeners = new ArrayList<DeploymentListener>();


    public org.apache.openejb.spi.ContainerSystem getContainerSystem() {
        return containerSystem;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public synchronized void addDeploymentListener(DeploymentListener deploymentListener) {
        deploymentListeners.add(deploymentListener);
    }

    public synchronized void removeDeploymentListener(DeploymentListener deploymentListener) {
        deploymentListeners.remove(deploymentListener);
    }

    private synchronized void fireAfterApplicationCreated(AppInfo appInfo) {
        ArrayList<DeploymentListener> listeners;
        synchronized (this) {
            listeners = new ArrayList<DeploymentListener>(deploymentListeners);
        }
        for (DeploymentListener listener : listeners) {
            String listenerName = listener.getClass().getSimpleName();
            try {
                logger.debug("appCreationEvent.start", listenerName, appInfo.jarPath);
                listener.afterApplicationCreated(appInfo);
            } catch (Throwable e) {
                logger.error("appCreationEvent.failed", e, listenerName, appInfo.jarPath);
            }
        }
    }

    private synchronized void fireBeforeApplicationDestroyed(AppInfo appInfo) {
        ArrayList<DeploymentListener> listeners;
        synchronized (this) {
            listeners = new ArrayList<DeploymentListener>(deploymentListeners);
        }
        for (DeploymentListener listener : listeners) {
            String listenerName = listener.getClass().getSimpleName();
            try {
                logger.debug("appDestroyedEvent.start", listenerName, appInfo.jarPath);
                listener.beforeApplicationDestroyed(appInfo);
            } catch (Throwable e) {
                logger.error("appDestroyedEvent.failed", e, listenerName, appInfo.jarPath);
            }
        }
    }

    protected SafeToolkit toolkit = SafeToolkit.getToolkit("Assembler");
    protected OpenEjbConfiguration config;

    public Assembler() {
        persistenceClassLoaderHandler = new PersistenceClassLoaderHandlerImpl();

        installNaming();

        SystemInstance system = SystemInstance.get();

        system.setComponent(Assembler.class, this);
        
        containerSystem = new CoreContainerSystem();
        system.setComponent(ContainerSystem.class, containerSystem);

        jndiBuilder = new JndiBuilder(containerSystem.getJNDIContext());

        setConfiguration(new OpenEjbConfiguration());

        ApplicationServer appServer = system.getComponent(ApplicationServer.class);
        if (appServer == null) {
            system.setComponent(ApplicationServer.class, new org.apache.openejb.core.ServerFederation());
        }

        system.setComponent(EjbResolver.class, new EjbResolver(null, EjbResolver.Scope.GLOBAL));
    }

    private void setConfiguration(OpenEjbConfiguration config) {
        this.config = config;
        if (config.containerSystem == null) {
            config.containerSystem = new ContainerSystemInfo();
        }

        if (config.facilities == null) {
            config.facilities = new FacilitiesInfo();
        }

        SystemInstance.get().setComponent(OpenEjbConfiguration.class, this.config);
    }

    public void init(Properties props) throws OpenEJBException {
        this.props = props;
        /* Get Configuration ////////////////////////////*/
        String className = props.getProperty(EnvProps.CONFIGURATION_FACTORY);
        if (className == null) {
            className = props.getProperty("openejb.configurator", "org.apache.openejb.config.ConfigurationFactory");
        }

        configFactory = (OpenEjbConfigurationFactory) toolkit.newInstance(className);
        configFactory.init(props);

        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
    }

    public static void installNaming() {
        if (System.getProperty("duct tape") != null) return;
        
        /* Add IntraVM JNDI service /////////////////////*/
        Properties systemProperties = System.getProperties();
        synchronized (systemProperties) {
            String str = systemProperties.getProperty(Context.URL_PKG_PREFIXES);
            String naming = "org.apache.openejb.core.ivm.naming";
            if (str == null) {
                str = naming;
            } else if (str.indexOf(naming) == -1) {
                str = str + ":" + naming;
            }
            systemProperties.setProperty(Context.URL_PKG_PREFIXES, str);
        }
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/}

    private static ThreadLocal<Map<String, Object>> context = new ThreadLocal<Map<String, Object>>();

    public static void setContext(Map<String, Object> map) {
        context.set(map);
    }

    public static Map<String, Object> getContext() {
        Map<String, Object> map = context.get();
        if (map == null) {
            map = new HashMap<String, Object>();
            context.set(map);
        }
        return map;
    }

    public void build() throws OpenEJBException {
        setContext(new HashMap<String, Object>());
        try {
            OpenEjbConfiguration config = configFactory.getOpenEjbConfiguration();
            buildContainerSystem(config);
        } catch (OpenEJBException ae) {
            /* OpenEJBExceptions contain useful information and are debbugable.
             * Let the exception pass through to the top and be logged.
             */
            throw ae;
        } catch (Exception e) {
            /* General Exceptions at this level are too generic and difficult to debug.
             * These exceptions are considered unknown bugs and are fatal.
             * If you get an error at this level, please trap and handle the error
             * where it is most relevant.
             */
            OpenEJBErrorHandler.handleUnknownError(e, "Assembler");
            throw new OpenEJBException(e);
        } finally {
            context.set(null);
        }
    }

    /////////////////////////////////////////////////////////////////////
    ////
    ////    Public Methods Used for Assembly
    ////
    /////////////////////////////////////////////////////////////////////

    /**
     * When given a complete OpenEjbConfiguration graph this method
     * will construct an entire container system and return a reference to that
     * container system, as ContainerSystem instance.
     * <p/>
     * This method leverage the other assemble and apply methods which
     * can be used independently.
     * <p/>
     * Assembles and returns the {@link org.apache.openejb.core.CoreContainerSystem} using the
     * information from the {@link OpenEjbConfiguration} object passed in.
     * <pre>
     * This method performs the following actions(in order):
     * <p/>
     * 1  Assembles ProxyFactory
     * 2  Assembles External JNDI Contexts
     * 3  Assembles TransactionService
     * 4  Assembles SecurityService
     * 5  Assembles ConnectionManagers
     * 6  Assembles Connectors
     * 7  Assembles Containers
     * 8  Assembles Applications
     * </pre>
     *
     * @param configInfo
     * @throws Exception if there was a problem constructing the ContainerSystem.
     * @throws Exception
     * @see OpenEjbConfiguration
     */
    public void buildContainerSystem(OpenEjbConfiguration configInfo) throws Exception {


        ContainerSystemInfo containerSystemInfo = configInfo.containerSystem;

        /*[1] Assemble ProxyFactory //////////////////////////////////////////

            This operation must take place first because of interdependencies.
            As DeploymentInfo objects are registered with the ContainerSystem using the
            ContainerSystem.addDeploymentInfo() method, they are also added to the JNDI
            Naming Service for OpenEJB.  This requires that a proxy for the deployed bean's
            EJBHome be created. The proxy requires that the default proxy factory is set.
        */
        createProxyFactory(configInfo.facilities.intraVmServer);

        for (JndiContextInfo contextInfo : configInfo.facilities.remoteJndiContexts) {
            createExternalContext(contextInfo);
        }

        createTransactionManager(configInfo.facilities.transactionService);

        createSecurityService(configInfo.facilities.securityService);

        for (ResourceInfo resourceInfo : configInfo.facilities.resources) {
            createResource(resourceInfo);
        }

        // Containers
        for (ContainerInfo serviceInfo : containerSystemInfo.containers) {
            createContainer(serviceInfo);
        }

        for (AppInfo appInfo : containerSystemInfo.applications) {

            try {
                createApplication(appInfo, createAppClassLoader(appInfo));
            } catch (DuplicateDeploymentIdException e) {
                // already logged.
            } catch (Throwable e) {
                logger.error("appNotDeployed", e, appInfo.jarPath);
            }
        }
    }

    public Collection<AppInfo> getDeployedApplications() {
        return new ArrayList<AppInfo>(deployedApplications.values());
    }

    public void createApplication(EjbJarInfo ejbJar) throws NamingException, IOException, OpenEJBException {
        createEjbJar(ejbJar);
    }

    public void createEjbJar(EjbJarInfo ejbJar) throws NamingException, IOException, OpenEJBException {
        AppInfo appInfo = new AppInfo();
        appInfo.jarPath = ejbJar.jarPath;
        appInfo.ejbJars.add(ejbJar);
        createApplication(appInfo);
    }

    public void createApplication(EjbJarInfo ejbJar, ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        createEjbJar(ejbJar, classLoader);
    }

    public void createEjbJar(EjbJarInfo ejbJar, ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        AppInfo appInfo = new AppInfo();
        appInfo.jarPath = ejbJar.jarPath;
        appInfo.ejbJars.add(ejbJar);
        createApplication(appInfo, classLoader);
    }

    public void createClient(ClientInfo clientInfo) throws NamingException, IOException, OpenEJBException {
        AppInfo appInfo = new AppInfo();
        appInfo.jarPath = clientInfo.moduleId;
        appInfo.clients.add(clientInfo);
        createApplication(appInfo);
    }

    public void createClient(ClientInfo clientInfo, ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        AppInfo appInfo = new AppInfo();
        appInfo.jarPath = clientInfo.moduleId;
        appInfo.clients.add(clientInfo);
        createApplication(appInfo, classLoader);
    }

    public void createConnector(ConnectorInfo connectorInfo) throws NamingException, IOException, OpenEJBException {
        AppInfo appInfo = new AppInfo();
        appInfo.jarPath = connectorInfo.moduleId;
        appInfo.connectors.add(connectorInfo);
        createApplication(appInfo);
    }

    public void createConnector(ConnectorInfo connectorInfo, ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        AppInfo appInfo = new AppInfo();
        appInfo.jarPath = connectorInfo.moduleId;
        appInfo.connectors.add(connectorInfo);
        createApplication(appInfo, classLoader);
    }

    public void createWebApp(WebAppInfo webAppInfo) throws NamingException, IOException, OpenEJBException {
        AppInfo appInfo = new AppInfo();
        appInfo.jarPath = webAppInfo.moduleId;
        appInfo.webApps.add(webAppInfo);
        createApplication(appInfo);
    }

    public void createWebApp(WebAppInfo webAppInfo, ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        AppInfo appInfo = new AppInfo();
        appInfo.jarPath = webAppInfo.moduleId;
        appInfo.webApps.add(webAppInfo);
        createApplication(appInfo, classLoader);
    }

    public void createApplication(AppInfo appInfo) throws OpenEJBException, IOException, NamingException {
        createApplication(appInfo, createAppClassLoader(appInfo));
    }

    public void createApplication(AppInfo appInfo, ClassLoader classLoader) throws OpenEJBException, IOException, NamingException {

        logger.info("createApplication.start", appInfo.jarPath);

        List<String> used = new ArrayList<String>();
        for (EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
            for (EnterpriseBeanInfo beanInfo : ejbJarInfo.enterpriseBeans) {
                if (containerSystem.getDeploymentInfo(beanInfo.ejbDeploymentId) != null) {
                    used.add(beanInfo.ejbDeploymentId);
                }
            }
        }

        if (used.size() > 0) {
            String message = logger.error("createApplication.appFailedDuplicateIds", appInfo.jarPath);
            for (String id : used) {
                logger.debug("createApplication.deploymentIdInUse", id);
                message += "\n    "+id;
            }
            throw new DuplicateDeploymentIdException(message);
        }

        try {
            // Generate the cmp2 concrete subclasses
            CmpJarBuilder cmpJarBuilder = new CmpJarBuilder(appInfo, classLoader);
            File generatedJar = cmpJarBuilder.getJarFile();
            if (generatedJar != null) {
                classLoader = ClassLoaderUtil.createClassLoader(appInfo.jarPath, new URL []{generatedJar.toURL()}, classLoader);
            }

            // JPA - Persistence Units MUST be processed first since they will add ClassFileTransformers
            // to the class loader which must be added before any classes are loaded
            PersistenceBuilder persistenceBuilder = new PersistenceBuilder(persistenceClassLoaderHandler);
            for (PersistenceUnitInfo info : appInfo.persistenceUnits) {
                try {
                    EntityManagerFactory factory = persistenceBuilder.createEntityManagerFactory(info, classLoader);
                    containerSystem.getJNDIContext().bind("java:openejb/PersistenceUnit/" + info.id, factory);
                } catch (NameAlreadyBoundException e) {
                    throw new OpenEJBException("PersistenceUnit already deployed: " + info.persistenceUnitRootUrl);
                } catch (Exception e) {
                    throw new OpenEJBException(e);
                }
            }

            // Connectors
            for (ConnectorInfo connector : appInfo.connectors) {
                ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(classLoader);
                try {
                    // todo add undeployment code for these
                    if (connector.resourceAdapter != null) {
                        createResource(connector.resourceAdapter);
                    }
                    for (ResourceInfo outbound : connector.outbound) {
                        createResource(outbound);
                    }
                    for (MdbContainerInfo inbound : connector.inbound) {
                        createContainer(inbound);
                    }
                    for (ResourceInfo adminObject : connector.adminObject) {
                        createResource(adminObject);
                    }
                } finally {
                    Thread.currentThread().setContextClassLoader(oldClassLoader);
                }
            }

            // EJB
            EjbJarBuilder ejbJarBuilder = new EjbJarBuilder(props, classLoader);
            for (EjbJarInfo ejbJar : appInfo.ejbJars) {
                HashMap<String, DeploymentInfo> deployments = ejbJarBuilder.build(ejbJar);

                JaccPermissionsBuilder jaccPermissionsBuilder = new JaccPermissionsBuilder();
                PolicyContext policyContext = jaccPermissionsBuilder.build(ejbJar, deployments);
                if (System.getProperty("duct tape") == null) {
                    jaccPermissionsBuilder.install(policyContext);
                }

                MethodTransactionBuilder methodTransactionBuilder = new MethodTransactionBuilder();
                methodTransactionBuilder.build(deployments, ejbJar.methodTransactions);

                for (DeploymentInfo deploymentInfo : deployments.values()) {
                    containerSystem.addDeployment(deploymentInfo);
                }

                jndiBuilder.build(ejbJar, deployments);

                // setup timers - must be after transaction attibutes are set
                for (DeploymentInfo deploymentInfo : deployments.values()) {
                    CoreDeploymentInfo coreDeploymentInfo = (CoreDeploymentInfo) deploymentInfo;
                    if (coreDeploymentInfo.getComponentType() != BeanType.STATEFUL) {
                        Method ejbTimeout = coreDeploymentInfo.getEjbTimeout();
                        if (ejbTimeout != null) {
                            // If user set the tx attribute to RequiresNew change it to Required so a new transaction is not started
                            if (coreDeploymentInfo.getTransactionAttribute(ejbTimeout) == CoreDeploymentInfo.TX_REQUIRES_NEW) {
                                coreDeploymentInfo.setMethodTransactionAttribute(ejbTimeout, "Required");
                            }

                            // Create the timer
                            EjbTimerServiceImpl timerService = new EjbTimerServiceImpl(coreDeploymentInfo);
                            coreDeploymentInfo.setEjbTimerService(timerService);
                        } else {
                            coreDeploymentInfo.setEjbTimerService(new NullEjbTimerServiceImpl());
                        }
                    }
                }
                // process application exceptions
                for (ApplicationExceptionInfo exceptionInfo : ejbJar.applicationException) {
                    try {
                        Class exceptionClass = classLoader.loadClass(exceptionInfo.exceptionClass);
                        for (DeploymentInfo deploymentInfo : deployments.values()) {
                            CoreDeploymentInfo coreDeploymentInfo = (CoreDeploymentInfo) deploymentInfo;
                            coreDeploymentInfo.addApplicationException(exceptionClass, exceptionInfo.rollback);
                        }
                    } catch (ClassNotFoundException e) {
                        logger.error("createApplication.invalidClass", e, exceptionInfo.exceptionClass, e.getMessage());
                    }
                }

                // now that everything is configured, deploy to the container
                ejbJarBuilder.deploy(deployments);

                for (EnterpriseBeanInfo beanInfo : ejbJar.enterpriseBeans) {
                    logger.info("createApplication.createdEjb", beanInfo.ejbDeploymentId, beanInfo.ejbName, beanInfo.containerId);
                }
            }

            // App Client
            for (ClientInfo clientInfo : appInfo.clients) {
                // determind the injections
                InjectionBuilder injectionBuilder = new InjectionBuilder(classLoader);
                List<Injection> injections = injectionBuilder.buildInjections(clientInfo.jndiEnc);

                // build the enc
                JndiEncBuilder jndiEncBuilder = new JndiEncBuilder(clientInfo.jndiEnc, injections, clientInfo.moduleId, classLoader);
                jndiEncBuilder.setClient(true);
                jndiEncBuilder.setUseCrossClassLoaderRef(false);
                Context context = (Context) jndiEncBuilder.build().lookup("env");
                containerSystem.getJNDIContext().bind("java:openejb/client/" + clientInfo.moduleId + "/comp/env", context);
                if (clientInfo.codebase != null) {
                    containerSystem.getJNDIContext().bind("java:openejb/client/" + clientInfo.moduleId + "/comp/path", clientInfo.codebase);
                }
                if (clientInfo.mainClass != null) {
                    containerSystem.getJNDIContext().bind("java:openejb/client/" + clientInfo.moduleId + "/comp/mainClass", clientInfo.mainClass);
                }
                if (clientInfo.callbackHandler != null) {
                    containerSystem.getJNDIContext().bind("java:openejb/client/" + clientInfo.moduleId + "/comp/callbackHandler", clientInfo.callbackHandler);
                }
                containerSystem.getJNDIContext().bind("java:openejb/client/" + clientInfo.moduleId + "/comp/injections", injections);
            }

            SystemInstance systemInstance = SystemInstance.get();

            // WebApp

            WebAppBuilder webAppBuilder = systemInstance.getComponent(WebAppBuilder.class);
            if (webAppBuilder != null) {
                webAppBuilder.deployWebApps(appInfo, classLoader);
            }

            EjbResolver globalEjbResolver = systemInstance.getComponent(EjbResolver.class);
            globalEjbResolver.addAll(appInfo.ejbJars);

            logger.info("createApplication.success", appInfo.jarPath);

            deployedApplications.put(appInfo.jarPath, appInfo);
            fireAfterApplicationCreated(appInfo);
        } catch (Throwable t) {
            try {
                destroyApplication(appInfo);
            } catch (Exception e1) {
                logger.debug("createApplication.undeployFailed", e1, appInfo.jarPath);
            }
            throw new OpenEJBException(messages.format("createApplication.failed", appInfo.jarPath), t);
        }
    }

    public void destroyApplication(String filePath) throws UndeployException, NoSuchApplicationException {
        AppInfo appInfo = deployedApplications.remove(filePath);
        if (appInfo == null) {
            throw new NoSuchApplicationException(filePath);
        }
        destroyApplication(appInfo);
    }

    private void destroyApplication(AppInfo appInfo) throws UndeployException {
        logger.info("destroyApplication.start", appInfo.jarPath);

        fireBeforeApplicationDestroyed(appInfo);


        EjbResolver globalResolver = new EjbResolver(null, EjbResolver.Scope.GLOBAL);
        for (AppInfo info : deployedApplications.values()) {
            globalResolver.addAll(info.ejbJars);
        }
        SystemInstance.get().setComponent(EjbResolver.class, globalResolver);


        Context globalContext = containerSystem.getJNDIContext();
        UndeployException undeployException = new UndeployException(messages.format("destroyApplication.failed", appInfo.jarPath));

        WebAppBuilder webAppBuilder = SystemInstance.get().getComponent(WebAppBuilder.class);
        if (webAppBuilder != null) {
            try {
                webAppBuilder.undeployWebApps(appInfo);
            } catch (Exception e) {
                undeployException.getCauses().add(new Exception("App: " + appInfo.jarPath + ": " + e.getMessage(), e));
            }
        }

        // get all of the ejb deployments
        List<CoreDeploymentInfo> deployments = new ArrayList<CoreDeploymentInfo>();
        for (EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
            for (EnterpriseBeanInfo beanInfo : ejbJarInfo.enterpriseBeans) {
                String deploymentId = beanInfo.ejbDeploymentId;
                CoreDeploymentInfo deployment = (CoreDeploymentInfo) containerSystem.getDeploymentInfo(deploymentId);
                if (deployment == null) {
                    undeployException.getCauses().add(new Exception("deployment not found: " + deploymentId));
                } else {
                    deployments.add(deployment);
                }
            }
        }

        // get the client ids
        List<String> clientIds = new ArrayList<String>();
        for (ClientInfo clientInfo : appInfo.clients) {
            clientIds.add(clientInfo.moduleId);
        }

        // Clear out naming for all components first
        for (CoreDeploymentInfo deployment : deployments) {
            String deploymentID = deployment.getDeploymentID() + "";
            try {
                containerSystem.removeDeploymentInfo(deployment);
            } catch (Throwable t) {
                undeployException.getCauses().add(new Exception(deploymentID, t));
            }

            JndiBuilder.Bindings bindings = deployment.get(JndiBuilder.Bindings.class);
            if (bindings != null) for (String name : bindings.getBindings()) {
                try {
                    globalContext.unbind(name);
                } catch (Throwable t) {
                    undeployException.getCauses().add(new Exception("bean: " + deploymentID + ": " + t.getMessage(), t));
                }
            }
        }

        for (PersistenceUnitInfo unitInfo : appInfo.persistenceUnits) {
            try {
                Object object = globalContext.lookup("openejb/PersistenceUnit/" + unitInfo.id);
                globalContext.unbind("openejb/PersistenceUnit/"+unitInfo.id);

                // close EMF so all resources are released
                ((EntityManagerFactory) object).close();
                persistenceClassLoaderHandler.destroy(unitInfo.id);
            } catch (Throwable t) {
                undeployException.getCauses().add(new Exception("persistence-unit: " + unitInfo.id + ": " + t.getMessage(), t));
            }
        }

        try {
            IvmContext ivmContext = (IvmContext) globalContext;
            ivmContext.prune("openejb/Deployment");
            ivmContext.prune("openejb/ejb");
        } catch (NamingException e) {
            undeployException.getCauses().add(new Exception("Unable to prune openejb/Deployments and openejb/ejb namespaces, this could cause future deployments to fail.", e));
        }

        for (CoreDeploymentInfo deployment : deployments) {
            String deploymentID = deployment.getDeploymentID() + "";
            try {
                Container container = deployment.getContainer();
                container.undeploy(deployment);
                deployment.setContainer(null);
            } catch (Throwable t) {
                undeployException.getCauses().add(new Exception("bean: " + deploymentID + ": " + t.getMessage(), t));
            } finally {
                deployment.setDestroyed(true);
            }
        }
        deployments.clear();

        for (String clientId : clientIds) {
            try {
                globalContext.unbind("/openejb/client/" + clientId);
            } catch (Throwable t) {
                undeployException.getCauses().add(new Exception("client: " + clientId + ": " + t.getMessage(), t));
            }
        }

        ClassLoaderUtil.destroyClassLoader(appInfo.jarPath);

        if (undeployException.getCauses().size() > 0) {
            throw undeployException;
        }

        logger.debug("destroyApplication.success", appInfo.jarPath);
    }

    public ClassLoader createAppClassLoader(AppInfo appInfo) throws OpenEJBException, IOException {
        List<URL> jars = new ArrayList<URL>();
        for (EjbJarInfo info : appInfo.ejbJars) {
            jars.add(toUrl(info.jarPath));
        }
        for (ClientInfo info : appInfo.clients) {
            jars.add(toUrl(info.codebase));
        }
        for (ConnectorInfo info : appInfo.connectors) {
            for (String jarPath : info.libs) {
                jars.add(toUrl(jarPath));
            }
        }
        for (String jarPath : appInfo.libs) {
            jars.add(toUrl(jarPath));
        }

        // Create the class loader
        ClassLoader classLoader = ClassLoaderUtil.createClassLoader(appInfo.jarPath, jars.toArray(new URL[jars.size()]), OpenEJB.class.getClassLoader());
        return classLoader;
    }

    public void createExternalContext(JndiContextInfo contextInfo) throws OpenEJBException {
        logger.getChildLogger("service").info("createService", contextInfo.service, contextInfo.id, contextInfo.className);

        InitialContext result;
        try {
            InitialContext ic = new InitialContext(contextInfo.properties);
            result = ic;
        } catch (NamingException ne) {

            throw new OpenEJBException("The remote JNDI EJB references for remote-jndi-contexts = " + contextInfo.id + "+ could not be resolved.", ne);
        }
        InitialContext cntx = result;

        try {
            containerSystem.getJNDIContext().bind("java:openejb/remote_jndi_contexts/" + contextInfo.id, cntx);
        } catch (NamingException e) {
            throw new OpenEJBException("Cannot bind " + contextInfo.service + " with id " + contextInfo.id, e);
        }

        // Update the config tree
        config.facilities.remoteJndiContexts.add(contextInfo);

        logger.getChildLogger("service").debug("createService.success", contextInfo.service, contextInfo.id, contextInfo.className);
    }

    public void createContainer(ContainerInfo serviceInfo) throws OpenEJBException {

        ObjectRecipe serviceRecipe = createRecipe(serviceInfo);

        serviceRecipe.setProperty("id", new StaticRecipe(serviceInfo.id));
        serviceRecipe.setProperty("transactionManager", new StaticRecipe(props.get(TransactionManager.class.getName())));
        serviceRecipe.setProperty("securityService", new StaticRecipe(props.get(SecurityService.class.getName())));

        // MDB container has a resource adapter string name that
        // must be replaced with the real resource adapter instance
        replaceResourceAdapterProperty(serviceRecipe);
        
        Object service = serviceRecipe.create();

        logUnusedProperties(serviceRecipe, serviceInfo);

        Class interfce = serviceInterfaces.get(serviceInfo.service);
        checkImplementation(interfce, service.getClass(), serviceInfo.service, serviceInfo.id);

        try {
            this.containerSystem.getJNDIContext().bind("java:openejb/" + serviceInfo.service + "/" + serviceInfo.id, service);
        } catch (NamingException e) {
            throw new OpenEJBException("Cannot bind " + serviceInfo.service + " with id " + serviceInfo.id, e);
        }

        setSystemInstanceComponent(interfce, service);

        props.put(interfce.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        containerSystem.addContainer(serviceInfo.id, (Container) service);

        // Update the config tree
        config.containerSystem.containers.add(serviceInfo);

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
    }

    public void removeContainer(String containerId) {
        containerSystem.removeContainer(containerId);

        // Update the config tree
        for (Iterator<ContainerInfo> iterator = config.containerSystem.containers.iterator(); iterator.hasNext();) {
            ContainerInfo containerInfo = iterator.next();
            if (containerInfo.id.equals(containerId)) {
                iterator.remove();
                try {
                    this.containerSystem.getJNDIContext().unbind("java:openejb/" + containerInfo.service + "/" + containerInfo.id);
                } catch (Exception e) {
                    logger.error("removeContainer.unbindFailed", containerId);
                }
            }
        }
    }

    public void createProxyFactory(ProxyFactoryInfo serviceInfo) throws OpenEJBException {

        ObjectRecipe serviceRecipe = createRecipe(serviceInfo);

        Object service = serviceRecipe.create();

        logUnusedProperties(serviceRecipe, serviceInfo);

        Class interfce = serviceInterfaces.get(serviceInfo.service);
        checkImplementation(interfce, service.getClass(), serviceInfo.service, serviceInfo.id);

        ProxyManager.registerFactory(serviceInfo.id, (ProxyFactory) service);
        ProxyManager.setDefaultFactory(serviceInfo.id);

        try {
            this.containerSystem.getJNDIContext().bind("java:openejb/" + serviceInfo.service + "/" + serviceInfo.id, service);
        } catch (NamingException e) {
            throw new OpenEJBException("Cannot bind " + serviceInfo.service + " with id " + serviceInfo.id, e);
        }

        setSystemInstanceComponent(interfce, service);

        getContext().put(interfce.getName(), service);

        props.put(interfce.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        // Update the config tree
        config.facilities.intraVmServer = serviceInfo;

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
    }

    private void replaceResourceAdapterProperty(ObjectRecipe serviceRecipe) throws OpenEJBException {
        Object resourceAdapterId = serviceRecipe.getProperty("ResourceAdapter");
        if (resourceAdapterId instanceof String)  {
            String id = (String) resourceAdapterId;
            id = id.trim();

            Object resourceAdapter = null;
            try {
                resourceAdapter = containerSystem.getJNDIContext().lookup("java:openejb/Resource/" + id);
            } catch (NamingException e) {
                // handled below
            }

            if (resourceAdapter == null) {
                throw new OpenEJBException("No existing resource adapter defined with id '" + id + "'.");
            }
            if (!(resourceAdapter instanceof ResourceAdapter)) {
                throw new OpenEJBException("Resource adapter defined with id '" + id + "' is not an instance of ResourceAdapter, " +
                        "but is an instance of " + resourceAdapter.getClass());
            }
            serviceRecipe.setProperty("ResourceAdapter", resourceAdapter);
        }
    }

    public void createResource(ResourceInfo serviceInfo) throws OpenEJBException {
        ObjectRecipe serviceRecipe = createRecipe(serviceInfo);
        serviceRecipe.setProperty("transactionManager", transactionManager);
        serviceRecipe.setProperty("properties", new UnsetPropertiesRecipe());

        replaceResourceAdapterProperty(serviceRecipe);

        Object service = serviceRecipe.create();


        // Java Connector spec ResourceAdapters and ManagedConnectionFactories need special activation
        if (service instanceof ResourceAdapter) {
            ResourceAdapter resourceAdapter = (ResourceAdapter) service;

            // resource adapters only work with a geronimo transaction manager
            if (!(transactionManager instanceof GeronimoTransactionManager)) {
                throw new OpenEJBException("The use of a resource adapter requires a Geronimo transaction manager");
            }
            GeronimoTransactionManager geronimoTransactionManager = (GeronimoTransactionManager) transactionManager;

            // create a thead pool
            int threadPoolSize = getIntProperty(serviceInfo.properties, "threadPoolSize", 30);
            if (threadPoolSize <= 0) throw new IllegalArgumentException("threadPoolSizes <= 0: " + threadPoolSize);
            Executor threadPool = Executors.newFixedThreadPool(threadPoolSize, new ResourceAdapterThreadFactory(serviceInfo.id));

            // create a work manager which the resource adapter can use to dispatch messages or perform tasks
            WorkManager workManager = new GeronimoWorkManager(threadPool, threadPool, threadPool, geronimoTransactionManager);

            // wrap the work mananger and transaction manager in a bootstrap context (connector spec thing)
            BootstrapContext bootstrapContext = new GeronimoBootstrapContext(workManager, geronimoTransactionManager);

            // start the resource adapter
            try {
                logger.debug("createResource.startingResourceAdapter", serviceInfo.id, service.getClass().getName());
                resourceAdapter.start(bootstrapContext);
            } catch (ResourceAdapterInternalException e) {
                throw new OpenEJBException(e);
            }

            Map<String, Object> unset = serviceRecipe.getUnsetProperties();
            unset.remove("threadPoolSize");
            logUnusedProperties(unset, serviceInfo)
                    ;
        } else if (service instanceof ManagedConnectionFactory) {
            ManagedConnectionFactory managedConnectionFactory = (ManagedConnectionFactory) service;

            // connection manager is constructed via a recipe so we automatically expose all cmf properties
            ObjectRecipe connectionManagerRecipe = new ObjectRecipe(GeronimoConnectionManagerFactory.class, "create");
            connectionManagerRecipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
            connectionManagerRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);
            connectionManagerRecipe.setAllProperties(serviceInfo.properties);
            connectionManagerRecipe.setProperty("name", serviceInfo.id);

            // standard properties
            connectionManagerRecipe.setProperty("transactionManager", transactionManager);
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) classLoader = getClass().getClassLoader();
            if (classLoader == null) classLoader = ClassLoader.getSystemClassLoader();
            connectionManagerRecipe.setProperty("classLoader", classLoader);

            logger.getChildLogger("service").info("createResource.createConnectionManager", serviceInfo.id, service.getClass().getName());

            // create the connection manager
            ConnectionManager connectionManager = (ConnectionManager) connectionManagerRecipe.create();
            if (connectionManager == null) {
                throw new RuntimeException("Invalid connection manager specified for connector identity = " + serviceInfo.id);
            }

            Map<String, Object> unsetA = serviceRecipe.getUnsetProperties();
            Map<String, Object> unsetB = connectionManagerRecipe.getUnsetProperties();
            Map<String, Object> unset = new HashMap<String, Object>();
            for (Map.Entry<String, Object> entry : unsetA.entrySet()) {
                if (unsetB.containsKey(entry.getKey())) unset.put(entry.getKey(),entry.getValue());
            }
            logUnusedProperties(unset, serviceInfo);

            // service becomes a ConnectorReference which merges connection manager and mcf
            service = new ConnectorReference(connectionManager, managedConnectionFactory);
        } else {
            logUnusedProperties(serviceRecipe, serviceInfo);
        }

        try {
            containerSystem.getJNDIContext().bind("java:openejb/Resource/" + serviceInfo.id, service);
        } catch (NamingException e) {
            throw new OpenEJBException("Cannot bind resource adapter with id " + serviceInfo.id, e);
        }

        // Update the config tree
        config.facilities.resources.add(serviceInfo);

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
    }

    private int getIntProperty(Properties properties, String propertyName, int defaultValue) {
        String propertyValue = properties.getProperty(propertyName);
        if (propertyValue == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(propertyValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(propertyName + " is not an integer " + propertyValue, e);
        }
    }

    public void createConnectionManager(ConnectionManagerInfo serviceInfo) throws OpenEJBException {

        ObjectRecipe serviceRecipe = createRecipe(serviceInfo);

        Object object = props.get("TransactionManager");
        serviceRecipe.setProperty("transactionManager", new StaticRecipe(object));

        Object service = serviceRecipe.create();

        logUnusedProperties(serviceRecipe, serviceInfo);

        Class interfce = serviceInterfaces.get(serviceInfo.service);
        checkImplementation(interfce, service.getClass(), serviceInfo.service, serviceInfo.id);

        try {
            this.containerSystem.getJNDIContext().bind("java:openejb/" + serviceInfo.service + "/" + serviceInfo.id, service);
        } catch (NamingException e) {
            throw new OpenEJBException("Cannot bind " + serviceInfo.service + " with id " + serviceInfo.id, e);
        }

        setSystemInstanceComponent(interfce, service);

        getContext().put(interfce.getName(), service);

        props.put(interfce.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        // Update the config tree
        config.facilities.connectionManagers.add(serviceInfo);

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
    }

    public void createSecurityService(SecurityServiceInfo serviceInfo) throws OpenEJBException {

        ObjectRecipe serviceRecipe = createRecipe(serviceInfo);

        Object service = serviceRecipe.create();

        logUnusedProperties(serviceRecipe, serviceInfo);

        Class interfce = serviceInterfaces.get(serviceInfo.service);
        checkImplementation(interfce, service.getClass(), serviceInfo.service, serviceInfo.id);

        try {
            this.containerSystem.getJNDIContext().bind("java:openejb/" + serviceInfo.service, service);
        } catch (NamingException e) {
            throw new OpenEJBException("Cannot bind " + serviceInfo.service + " with id " + serviceInfo.id, e);
        }

        setSystemInstanceComponent(interfce, service);

        getContext().put(interfce.getName(), service);

        props.put(interfce.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        this.securityService = (SecurityService) service;

        // Update the config tree
        config.facilities.securityService = serviceInfo;

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
    }

    public void createTransactionManager(TransactionServiceInfo serviceInfo) throws OpenEJBException {

        ObjectRecipe serviceRecipe = createRecipe(serviceInfo);

        Object service = serviceRecipe.create();

        logUnusedProperties(serviceRecipe, serviceInfo);

        Class interfce = serviceInterfaces.get(serviceInfo.service);
        checkImplementation(interfce, service.getClass(), serviceInfo.service, serviceInfo.id);

        try {
            this.containerSystem.getJNDIContext().bind("java:openejb/" + serviceInfo.service, service);
        } catch (NamingException e) {
            throw new OpenEJBException("Cannot bind " + serviceInfo.service + " with id " + serviceInfo.id, e);
        }

        setSystemInstanceComponent(interfce, service);

        getContext().put(interfce.getName(), service);

        props.put(interfce.getName(), service);
        props.put(serviceInfo.service, service);
        props.put(serviceInfo.id, service);

        this.transactionManager = (TransactionManager) service;

        // Update the config tree
        config.facilities.transactionService = serviceInfo;

        // todo find a better place for this

        // TransactionSynchronizationRegistry
        TransactionSynchronizationRegistry synchronizationRegistry;
        if (transactionManager instanceof TransactionSynchronizationRegistry) {
            synchronizationRegistry = (TransactionSynchronizationRegistry) transactionManager;
        } else {
            // todo this should be built
            synchronizationRegistry = new SimpleTransactionSynchronizationRegistry(transactionManager);
        }
        Assembler.getContext().put(TransactionSynchronizationRegistry.class.getName(), synchronizationRegistry);
        SystemInstance.get().setComponent(TransactionSynchronizationRegistry.class, synchronizationRegistry);

        // JtaEntityManagerRegistry
        // todo this should be built
        JtaEntityManagerRegistry jtaEntityManagerRegistry = new JtaEntityManagerRegistry(synchronizationRegistry);
        Assembler.getContext().put(JtaEntityManagerRegistry.class.getName(), jtaEntityManagerRegistry);
        SystemInstance.get().setComponent(JtaEntityManagerRegistry.class, jtaEntityManagerRegistry);

        logger.getChildLogger("service").debug("createService.success", serviceInfo.service, serviceInfo.id, serviceInfo.className);
    }

    private void logUnusedProperties(ObjectRecipe serviceRecipe, ServiceInfo info) {
        Map<String, Object> unsetProperties = serviceRecipe.getUnsetProperties();
        logUnusedProperties(unsetProperties, info);
    }

    private void logUnusedProperties(Map<String, Object> unsetProperties, ServiceInfo info) {
        for (String property : unsetProperties.keySet()) {
            //TODO: DMB: Make more robust later
            if (property.equalsIgnoreCase("properties")) return;
            if (property.equalsIgnoreCase("transactionManager")) return;
            //---

            logger.getChildLogger("service").warning("unusedProperty", property, info.id);
        }
    }

    private ObjectRecipe createRecipe(ServiceInfo info) {
        logger.getChildLogger("service").info("createService", info.service, info.id, info.className);
        ObjectRecipe serviceRecipe = new ObjectRecipe(info.className, info.factoryMethod, info.constructorArgs.toArray(new String[0]), null);
        serviceRecipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
        serviceRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        serviceRecipe.setAllProperties(info.properties);
        return serviceRecipe;
    }

    @SuppressWarnings({"unchecked"})
    private void setSystemInstanceComponent(Class interfce, Object service) {
        SystemInstance.get().setComponent(interfce, service);
    }

    private URL toUrl(String jarPath) throws OpenEJBException {
        try {
            return new File(jarPath).toURL();
        } catch (MalformedURLException e) {
            throw new OpenEJBException(messages.format("cl0001", jarPath, e.getMessage()), e);
        }
    }

    private static class PersistenceClassLoaderHandlerImpl implements PersistenceClassLoaderHandler {
        private final Map<String,List<ClassFileTransformer>> transformers = new TreeMap<String,List<ClassFileTransformer>>();

        public void addTransformer(String unitId, ClassLoader classLoader, ClassFileTransformer classFileTransformer) {
            Instrumentation instrumentation = Agent.getInstrumentation();
            if (instrumentation != null) {
                instrumentation.addTransformer(classFileTransformer);

                if (unitId != null) {
                    List<ClassFileTransformer> transformers = this.transformers.get(unitId);
                    if (transformers == null) {
                        transformers = new ArrayList<ClassFileTransformer>(1);
                        this.transformers.put(unitId, transformers);
                    }
                    transformers.add(classFileTransformer);
                }
            } else {
                logger.error("assembler.noAgent");
            }
        }

        public void destroy(String unitId) {
            List<ClassFileTransformer> transformers = this.transformers.remove(unitId);
            if (transformers != null) {
                Instrumentation instrumentation = Agent.getInstrumentation();
                if (instrumentation != null) {
                    for (ClassFileTransformer transformer : transformers) {
                        instrumentation.removeTransformer(transformer);
                    }
                } else {
                    logger.error("assembler.noAgent");
                }
            }
        }

        public ClassLoader getNewTempClassLoader(ClassLoader classLoader) {
            return ClassLoaderUtil.createTempClassLoader(classLoader);
        }
    }

    // Based on edu.emory.mathcs.backport.java.util.concurrent.Executors.DefaultThreadFactory
    // Which is freely licensed as follows.
    // "Use, modify, and redistribute this code in any way without acknowledgement"
    private static class ResourceAdapterThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final String namePrefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        ResourceAdapterThreadFactory(String resourceAdapterName) {
            SecurityManager securityManager = System.getSecurityManager();
            if (securityManager != null) {
                group = securityManager.getThreadGroup();
            } else {
                group = Thread.currentThread().getThreadGroup();
            }

            namePrefix = resourceAdapterName + "-worker-";
        }

        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement(), 0);
            if (!thread.isDaemon()) thread.setDaemon(true);
            if (thread.getPriority() != Thread.NORM_PRIORITY) thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }

}
