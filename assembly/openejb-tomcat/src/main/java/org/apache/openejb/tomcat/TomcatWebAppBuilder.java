/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.tomcat;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.HostConfig;
import org.apache.naming.ContextAccessController;
import org.apache.naming.ContextBindings;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.Injection;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.server.webservices.WsServlet;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.LinkResolver;
import org.apache.openejb.assembler.classic.UniqueDefaultLinkResolver;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.InjectionBuilder;
import org.apache.openejb.config.AnnotationDeployer;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.ReadDescriptors;
import org.apache.openejb.config.UnknownModuleTypeException;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.core.ivm.naming.SystemComponentReference;
import org.apache.openejb.core.webservices.JaxWsUtils;
import org.apache.openejb.core.CoreWebDeploymentInfo;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.TemporaryClassLoader;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.finder.ResourceFinder;
import org.apache.xbean.finder.UrlSet;
import org.omg.CORBA.ORB;

import javax.ejb.spi.HandleDelegate;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContext;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Iterator;

public class TomcatWebAppBuilder implements WebAppBuilder, ContextListener {
    public static final String IGNORE_CONTEXT = TomcatWebAppBuilder.class.getName() + ".IGNORE";
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("tomcat"), "org.apache.openejb.util.resources");

    private final TreeMap<String, ContextInfo> infos = new TreeMap<String, ContextInfo>();
    private final GlobalListenerSupport globalListenerSupport;
    private final ConfigurationFactory configurationFactory;
    private final Map<String,HostConfig> deployers = new TreeMap<String,HostConfig>();
    // todo merge this map witth the infos map above
    private final Map<String,DeployedApplication> deployedApps = new TreeMap<String,DeployedApplication>();
    private final DeploymentLoader deploymentLoader;
    private Assembler assembler;
    private CoreContainerSystem containerSystem;

    public TomcatWebAppBuilder() {
        StandardServer standardServer = (StandardServer) ServerFactory.getServer();
        globalListenerSupport = new GlobalListenerSupport(standardServer, this);

        for (Service service : standardServer.findServices()) {
            if (service.getContainer() instanceof Engine) {
                Engine engine = (Engine) service.getContainer();
                for (Container engineChild : engine.findChildren()) {
                    if (engineChild instanceof StandardHost) {
                        StandardHost host = (StandardHost) engineChild;
                        for (LifecycleListener listener : host.findLifecycleListeners()) {
                            if (listener instanceof HostConfig) {
                                HostConfig hostConfig = (HostConfig) listener;
                                deployers.put(host.getName(), hostConfig);
                            }
                        }
                    }
                }
            }
        }

        // MBeanServer mbeanServer;
        // List mbeanServers = MBeanServerFactory.findMBeanServer(null);
        // if (mbeanServers.size() > 0) {
        //     mbeanServer = (MBeanServer) mbeanServers.get(0);
        // } else {
        //     mbeanServer = MBeanServerFactory.createMBeanServer();
        // }

        configurationFactory = new ConfigurationFactory();
        deploymentLoader = new DeploymentLoader();
        assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
        containerSystem = (CoreContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
    }

    public void start() {
        globalListenerSupport.start();

    }

    public void stop() {
        globalListenerSupport.stop();
    }

    //
    // OpenEJB WebAppBuilder
    //

    public void deployWebApps(AppInfo appInfo, LinkResolver<EntityManagerFactory> emfLinkResolver, ClassLoader classLoader) throws Exception {
        for (WebAppInfo webApp : appInfo.webApps) {
            if (getContextInfo(webApp) == null) {
                StandardContext standardContext = new StandardContext();
                standardContext.addLifecycleListener(new ContextConfig());
                standardContext.setPath("/" + webApp.contextRoot);
                standardContext.setDocBase(webApp.codebase);
                standardContext.setParentClassLoader(classLoader);
                standardContext.setDelegate(true);

                String host = webApp.host;
                if (host == null) host = "localhost";
                HostConfig deployer = deployers.get(host);
                if (deployer != null) {
                    // host isn't set until we call deployer.manageApp, so pass it
                    ContextInfo contextInfo = addContextInfo(host, standardContext);
                    contextInfo.appInfo = appInfo;
                    contextInfo.deployer = deployer;
                    contextInfo.standardContext = standardContext;
                    contextInfo.emfLinkResolver = emfLinkResolver;
                    deployer.manageApp(standardContext);
                }
            }
        }
    }

    public void undeployWebApps(AppInfo appInfo) throws Exception {
        for (WebAppInfo webApp : appInfo.webApps) {
            ContextInfo contextInfo = getContextInfo(webApp);
            if (contextInfo != null && contextInfo.deployer != null) {
                StandardContext standardContext = contextInfo.standardContext;
                HostConfig deployer = contextInfo.deployer;
                deployer.unmanageApp(standardContext.getPath());
                deleteDir(new File(standardContext.getServletContext().getRealPath("")));
                removeContextInfo(standardContext);
            }
        }
    }

    private void deleteDir(File dir) {
        if (dir == null) return;
        if (dir.isFile()) return;
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                deleteDir(file);
            } else {
                file.delete();
            }
        }
        dir.delete();
    }

    //
    // Tomcat Listener
    //

    public void init(StandardContext standardContext) {
    }

    public void beforeStart(StandardContext standardContext) {
    }

    // context class loader is now defined, but no classes should have been loaded
    public void start(StandardContext standardContext) {
        if (standardContext.getServletContext().getAttribute(IGNORE_CONTEXT) != null) return;

        Assembler assembler = getAssembler();
        if (assembler == null) {
            logger.warning("OpenEJB has not been initialized so war will not be scanned for nested modules " + standardContext.getPath());
            return;
        }

        ContextInfo contextInfo = getContextInfo(standardContext);
        if (contextInfo == null) {
            AppModule appModule = loadApplication(standardContext);
            if (appModule != null) {
                try {
                    contextInfo = addContextInfo(standardContext.getHostname(), standardContext);
                    AppInfo appInfo = configurationFactory.configureApplication(appModule);
                    contextInfo.appInfo = appInfo;

                    LinkResolver<EntityManagerFactory> emfLinkResolver = new UniqueDefaultLinkResolver<EntityManagerFactory>();
                    assembler.createApplication(contextInfo.appInfo, emfLinkResolver, standardContext.getLoader().getClassLoader());
                    // todo add watched resources to context
                    contextInfo.emfLinkResolver = emfLinkResolver;
                } catch (Exception e) {
                    logger.error("Unable to deploy collapsed ear in war " + standardContext.getPath() + ": Exception: " + e.getMessage(), e);
                }
            }
        }

        contextInfo.standardContext = standardContext;

        WebAppInfo webAppInfo = null;
        // appInfo is null when deployment fails
        if (contextInfo.appInfo != null) {
            for (WebAppInfo w : contextInfo.appInfo.webApps) {
                if (("/" + w.contextRoot).equals(standardContext.getPath())) {
                    webAppInfo = w;
                    break;
                }
            }
        }

        if (webAppInfo != null) {
            try {
                // determind the injections
                InjectionBuilder injectionBuilder = new InjectionBuilder(standardContext.getLoader().getClassLoader());
                List<Injection> injections = injectionBuilder.buildInjections(webAppInfo.jndiEnc);

                // merge OpenEJB jndi into Tomcat jndi
                TomcatJndiBuilder jndiBuilder = new TomcatJndiBuilder(standardContext, webAppInfo, injections, contextInfo.emfLinkResolver);
                jndiBuilder.mergeJndi();

                // add WebDeploymentInfo to ContainerSystem
                CoreWebDeploymentInfo webDeploymentInfo = new CoreWebDeploymentInfo();
                webDeploymentInfo.setId(webAppInfo.moduleId);
                webDeploymentInfo.setClassLoader(standardContext.getLoader().getClassLoader());
                webDeploymentInfo.getInjections().addAll(injections);
                getContainerSystem().addWebDeployment(webDeploymentInfo);
            } catch (Exception e) {
                logger.error("Error merging OpenEJB JNDI entries in to war " + standardContext.getPath() + ": Exception: " + e.getMessage(), e);
            }
        }
    }

    public void afterStart(StandardContext standardContext) {
        if (standardContext.getServletContext().getAttribute(IGNORE_CONTEXT) != null) return;

        // if appInfo is null this is a failed deployment... just ignore
        ContextInfo contextInfo = getContextInfo(standardContext);
        if (contextInfo.appInfo == null) return;

        // replace any webservices with the webservice servlet
        // HACK: use a temp class loader because the class may have been loaded before
        // the openejb classes were added to the system class path so the WebService anntation
        // will not be present on the class
        TemporaryClassLoader tempClassLoader = new TemporaryClassLoader(standardContext.getLoader().getClassLoader());
        for (Container container : standardContext.findChildren()) {
            if (container instanceof Wrapper) {
                Wrapper wrapper = (Wrapper) container;
                String servletClass = wrapper.getServletClass();
                try {
                    Class<?> clazz = tempClassLoader.loadClass(servletClass);
                    if (JaxWsUtils.isWebService(clazz)) {
                        wrapper.setServletClass(WsServlet.class.getName());
                        if (wrapper.getServlet() != null) {
                            wrapper.load();
                            wrapper.unload();
                        }
                    }
                } catch (Exception e) {
                    // will be reported by the tomcat
                }
            }
        }

        // bind extra stuff at the java:comp level which can only be
        // bound after the context is created
        ContextAccessController.setWritable(standardContext.getNamingContextListener().getName(), standardContext);
        try {
            Context comp = (Context) ContextBindings.getClassLoader().lookup("comp");

            // add context to WebDeploymentInfo
            for (WebAppInfo webAppInfo : contextInfo.appInfo.webApps) {
                if (("/" + webAppInfo.contextRoot).equals(standardContext.getPath())) {
                    CoreWebDeploymentInfo webDeploymentInfo = (CoreWebDeploymentInfo) getContainerSystem().getWebDeploymentInfo(webAppInfo.moduleId);
                    if (webDeploymentInfo != null) {
                        webDeploymentInfo.setJndiEnc(comp);
                    }
                    break;
                }
            }

            // bind TransactionManager
            TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
            safeBind(comp, "TransactionManager", transactionManager);

            // bind TransactionSynchronizationRegistry
            TransactionSynchronizationRegistry synchronizationRegistry = SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class);
            safeBind(comp, "TransactionSynchronizationRegistry", synchronizationRegistry);

            safeBind(comp, "ORB", new SystemComponentReference(ORB.class));
            safeBind(comp, "HandleDelegate", new SystemComponentReference(HandleDelegate.class));
        } catch (NamingException e) {
        }
        ContextAccessController.setReadOnly(standardContext.getNamingContextListener().getName());


        OpenEJBValve openejbValve = new OpenEJBValve();
        standardContext.getPipeline().addValve(openejbValve);
    }

    public void beforeStop(StandardContext standardContext) {
    }

    public void stop(StandardContext standardContext) {
    }

    public void afterStop(StandardContext standardContext) {
        if (standardContext.getServletContext().getAttribute(IGNORE_CONTEXT) != null) return;

        ContextInfo contextInfo = getContextInfo(standardContext);
        if (contextInfo != null && contextInfo.appInfo != null && contextInfo.deployer == null) {
            try {
                assembler.destroyApplication(contextInfo.appInfo.jarPath);
            } catch (Exception e) {
                logger.error("Unable to stop web application " + standardContext.getPath() + ": Exception: " + e.getMessage(), e);
            }
        }
        removeContextInfo(standardContext);
    }

    public void destroy(StandardContext standardContext) {
    }

    public void afterStop(StandardServer standardServer) {
        // clean ear based webapps after shutdown
        for (ContextInfo contextInfo : infos.values()) {
            if (contextInfo != null && contextInfo.deployer != null) {
                StandardContext standardContext = contextInfo.standardContext;
                HostConfig deployer = contextInfo.deployer;
                deployer.unmanageApp(standardContext.getPath());
                String realPath = standardContext.getServletContext().getRealPath("");
                if (realPath != null) {
                    deleteDir(new File(realPath));
                }
            }
        }
    }

    public void checkHost(StandardHost standardHost) {
        if (standardHost.getAutoDeploy()) {
            // Undeploy any modified application
            for (Iterator<Map.Entry<String, DeployedApplication>> iterator = deployedApps.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<String, DeployedApplication> entry = iterator.next();
                DeployedApplication deployedApplication = entry.getValue();
                if (deployedApplication.isModified()) {
                    try {
                        assembler.destroyApplication(deployedApplication.appInfo.jarPath);
                    } catch (Exception e) {
                        logger.error("Unable to application " + deployedApplication.appInfo.jarPath + ": Exception: " + e.getMessage(), e);
                    }
                    iterator.remove();
                }
            }

            // Deploy new applications
            File appBase = appBase(standardHost);
            File[] files = appBase.listFiles();
            for (File file : files) {
                String name = file.getName();
                if (name.toLowerCase().endsWith(".war") || name.equals("ROOT") || name.equalsIgnoreCase("META-INF") || name.equalsIgnoreCase("WEB-INF")) continue;
                if (file.isDirectory() && new File(file, "WEB-INF").exists()) continue;
                if (isDeployed(file, standardHost)) continue;

                AppInfo appInfo = null;
                try {
                    file = file.getCanonicalFile().getAbsoluteFile();

                    AppModule appModule = deploymentLoader.load(file);

                    // Ignore any standalone web modules - this happens when the app is unpaked and doesn't have a WEB-INF dir
                    if (appModule.getDeploymentModule().size() == 1 && appModule.getWebModules().size() == 1) {
                        WebModule webModule = appModule.getWebModules().iterator().next();
                        if (file.getAbsolutePath().equals(webModule.getJarLocation())) {
                            continue;
                        }
                    }

                    // if this is an unpacked dir, tomcat will pick it up as a webapp so undeploy it first
                    if (file.isDirectory()) {
                        ContainerBase context = (ContainerBase) standardHost.findChild("/" + name);
                        if (context != null) {
                            try {
                                standardHost.removeChild(context);
                            } catch (Throwable t) {
                                logger.warning("Error undeploying wep application from Tomcat  " + name, t);
                            }
                            try {
                                context.destroy();
                            } catch (Throwable t) {
                                logger.warning("Error destroying Tomcat web context " + name, t);
                            }
                        }
                    }

                    // tell web modules to deploy using this host
                    for (WebModule webModule : appModule.getWebModules()) {
                        webModule.setHost(standardHost.getName());
                    }

                    appInfo = configurationFactory.configureApplication(appModule);
                    assembler.createApplication(appInfo);
                } catch (Throwable e) {
                    logger.warning("Error deploying application " + file.getAbsolutePath(), e);
                }
                deployedApps.put(file.getAbsolutePath(), new DeployedApplication(file, appInfo));
            }
        }
    }

    private boolean isDeployed(File file, StandardHost standardHost) {
        if (deployedApps.containsKey(file.getAbsolutePath())) {
            return true;
        }

        // check if this is a deployed web application
        String name = "/" + file.getName();

        // ROOT context is a special case
        if (name.equals("/ROOT")) name = "";

        return file.isFile() && standardHost.findChild(name) != null;
    }

    protected File appBase(StandardHost standardHost) {
        File file = new File(standardHost.getAppBase());
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("catalina.base"), standardHost.getAppBase());
        }
        try {
            file= file.getCanonicalFile();
        } catch (IOException e) {
        }
        return file;
    }

    private AppModule loadApplication(StandardContext standardContext) {
        // create the web module
        WebModule webModule = createWebModule(standardContext);

        // create the app module
        AppModule appModule = new AppModule(webModule.getClassLoader(), webModule.getJarLocation());

        // add the web module itself
        appModule.getWebModules().add(webModule);

        // check each url to determine if it is an ejb jar
        for (URL url : getUrls(standardContext)) {
            try {
                Class moduleType = DeploymentLoader.discoverModuleType(url, standardContext.getLoader().getClassLoader(), true);
                if (EjbModule.class.isAssignableFrom(moduleType)) {
                    File file;
                    if (url.getProtocol().equals("jar")) {
                        url = new URL(url.getFile().replaceFirst("!.*$", ""));
                        file = new File(url.getFile());
                    } else if (url.getProtocol().equals("file")) {
                        file = new File(url.getFile());
                    } else {
                        logger.warning("Not loading " + moduleType.getSimpleName() + ".  Unknown protocol " + url.getProtocol());
                        continue;
                    }

                    logger.info("Found ejb module " + moduleType.getSimpleName() + " in war " + standardContext.getPath());

                    // creat the module
                    EjbModule ejbModule = new EjbModule(webModule.getClassLoader(), file.getAbsolutePath(), null, null);

                    // EJB deployment descriptors
                    try {
                        ResourceFinder ejbResourceFinder = new ResourceFinder("", standardContext.getLoader().getClassLoader(), file.toURL());
                        Map<String, URL> descriptors = ejbResourceFinder.getResourcesMap("META-INF/");
                        ejbModule.getAltDDs().putAll(descriptors);
                    } catch (IOException e) {
                        logger.error("Unable to determine descriptors in jar.", e);
                    }

                    // add module to app
                    appModule.getEjbModules().add(ejbModule);
                }
            } catch (IOException e) {
                logger.warning("Unable to determine the module type of " + url.toExternalForm() + ": Exception: " + e.getMessage(), e);
            } catch (UnknownModuleTypeException ignore) {
            }

        }

        // Persistence Units via META-INF/persistence.xml
        try {
            ResourceFinder finder = new ResourceFinder("", standardContext.getLoader().getClassLoader());
            List<URL> persistenceUrls = finder.findAll("META-INF/persistence.xml");
            appModule.getAltDDs().put("persistence.xml", persistenceUrls);
        } catch (IOException e) {
            logger.warning("Cannot load persistence-units from 'META-INF/persistence.xml' : " + e.getMessage(), e);
        }

        return appModule;
    }

    private WebModule createWebModule(StandardContext standardContext) {
        ServletContext servletContext = standardContext.getServletContext();

        // read the web.xml
        WebApp webApp = new WebApp();
        try {
            URL webXmlUrl = servletContext.getResource("/WEB-INF/web.xml");
            if (webXmlUrl != null) {
                webApp = ReadDescriptors.readWebApp(webXmlUrl);
            }
        } catch (Exception e) {
            logger.error("Unable to load web.xml in war " + servletContext.getContextPath() + ": Exception: " + e.getMessage(), e);
        }

        // create the web module
        String basePath = new File(servletContext.getRealPath(".")).getParentFile().getAbsolutePath();
        ClassLoader classLoader = new TemporaryClassLoader(standardContext.getLoader().getClassLoader());
        WebModule webModule = new WebModule(webApp, servletContext.getContextPath(), classLoader, basePath, getId(standardContext));
        webModule.setHost(standardContext.getHostname());

        // Add all Tomcat env entries to context so they can be overriden by the env.properties file
        NamingResources naming = standardContext.getNamingResources();
        for (ContextEnvironment environment : naming.findEnvironments()) {
            EnvEntry envEntry = webApp.getEnvEntryMap().get(environment.getName());
            if (envEntry == null) {
                envEntry = new EnvEntry();
                envEntry.setName(environment.getName());
                webApp.getEnvEntry().add(envEntry);
            }

            envEntry.setEnvEntryValue(environment.getValue());
            envEntry.setEnvEntryType(environment.getType());
        }

        // process the annotations
        try {
            AnnotationDeployer annotationDeployer = new AnnotationDeployer();
            annotationDeployer.deploy(webModule);
        } catch (OpenEJBException e) {
            logger.error("Unable to process annotation in " + standardContext.getPath() + ": Exception: " + e.getMessage(), e);
        }

        // remove all jndi entries where there is a configured Tomcat resource or resource-link
        webApp = webModule.getWebApp();
        for (ContextResource resource : naming.findResources()) {
            String name = resource.getName();
            removeRef(webApp, name);
        }
        for (ContextResourceLink resourceLink : naming.findResourceLinks()) {
            String name = resourceLink.getName();
            removeRef(webApp, name);
        }

        // remove all env entries from the web xml that are not overridable
        for (ContextEnvironment environment : naming.findEnvironments()) {
            if (!environment.getOverride()) {
                // overrides are not allowed
                webApp.getEnvEntryMap().remove(environment.getName());
            }
        }

        return webModule;
    }

    private void removeRef(WebApp webApp, String name) {
        webApp.getEnvEntryMap().remove(name);
        webApp.getEjbRefMap().remove(name);
        webApp.getEjbLocalRefMap().remove(name);
        webApp.getMessageDestinationRefMap().remove(name);
        webApp.getPersistenceContextRefMap().remove(name);
        webApp.getPersistenceUnitRefMap().remove(name);
        webApp.getResourceRefMap().remove(name);
        webApp.getResourceEnvRefMap().remove(name);
    }

    private List<URL> getUrls(StandardContext standardContext) {
        List<URL> urls = null;
        try {
            ClassLoader classLoader = standardContext.getLoader().getClassLoader();
            UrlSet urlSet = new UrlSet(classLoader);
            urlSet = urlSet.exclude(classLoader.getParent());
            urls = urlSet.getUrls();
        } catch (IOException e) {
            logger.warning("Unable to determine URLs in web application " + standardContext.getPath(), e);
        }
        return urls;
    }

    //
    // helper methods
    //

    private void safeBind(Context comp, String name, Object value) {
        try {
            comp.bind(name, value);
        } catch (NamingException e) {
        }
    }

    private Assembler getAssembler() {
        if (assembler == null) {
            assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
        }
        return assembler;
    }

    private CoreContainerSystem getContainerSystem() {
        if (containerSystem == null) {
            containerSystem = (CoreContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
        }
        return containerSystem;
    }

    private String getId(StandardContext standardContext) {
        String contextRoot = standardContext.getName();
        if (!contextRoot.startsWith("/")) contextRoot = "/" + contextRoot;
        return standardContext.getHostname() + contextRoot;
    }

    private ContextInfo getContextInfo(StandardContext standardContext) {
        String id = getId(standardContext);
        ContextInfo contextInfo = infos.get(id);
        return contextInfo;
    }

    private ContextInfo getContextInfo(WebAppInfo webAppInfo) {
        String host = webAppInfo.host;
        if (host == null) host = "localhost";
        String contextRoot = webAppInfo.contextRoot;
        String id = host + "/" + contextRoot;
        ContextInfo contextInfo = infos.get(id);
        return contextInfo;
    }

    private ContextInfo addContextInfo(String host, StandardContext standardContext) {
        String contextRoot = standardContext.getName();
        if (!contextRoot.startsWith("/")) contextRoot = "/" + contextRoot;
        String id = host + contextRoot;
        ContextInfo contextInfo = infos.get(id);
        if (contextInfo == null) {
            contextInfo = new ContextInfo();
            infos.put(id, contextInfo);
        }
        return contextInfo;
    }

    private void removeContextInfo(StandardContext standardContext) {
        String id = getId(standardContext);
        infos.remove(id);
    }

    private static class ContextInfo {
        public AppInfo appInfo;
        public StandardContext standardContext;
        public HostConfig deployer;
        public LinkResolver<EntityManagerFactory> emfLinkResolver;
    }

    private static class DeployedApplication {
        private AppInfo appInfo;
        private final Map<File,Long> watchedResource = new HashMap<File,Long>();

        public DeployedApplication(File base, AppInfo appInfo) {
            this.appInfo = appInfo;
            watchedResource.put(base, base.lastModified());
            if (appInfo != null) {
                for (String resource : appInfo.watchedResources) {
                    File file = new File(resource);
                    watchedResource.put(file, file.lastModified());
                }
                for (EjbJarInfo info : appInfo.ejbJars) {
                    for (String resource : info.watchedResources) {
                        File file = new File(resource);
                        watchedResource.put(file, file.lastModified());
                    }
                }
                for (WebAppInfo info : appInfo.webApps) {
                    for (String resource : info.watchedResources) {
                        File file = new File(resource);
                        watchedResource.put(file, file.lastModified());
                    }
                }
                for (ConnectorInfo info : appInfo.connectors) {
                    for (String resource : info.watchedResources) {
                        File file = new File(resource);
                        watchedResource.put(file, file.lastModified());
                    }
                }
            }
        }

        public boolean isModified() {
            for (Map.Entry<File, Long> entry : watchedResource.entrySet()) {
                File file = entry.getKey();
                long lastModified = entry.getValue();
                if ((!file.exists() && lastModified != 0L) ||
                        (file.lastModified() != lastModified)) {
                    return true;
                }
            }
            return false;
        }
    }
}
