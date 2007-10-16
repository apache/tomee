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
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.HostConfig;
import org.apache.naming.ContextAccessController;
import org.apache.naming.ContextBindings;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.LinkResolver;
import org.apache.openejb.assembler.classic.UniqueDefaultLinkResolver;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.AnnotationDeployer;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.ReadDescriptors;
import org.apache.openejb.config.UnknownModuleTypeException;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.core.ivm.naming.SystemComponentReference;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openjpa.lib.util.TemporaryClassLoader;
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

public class TomcatWebAppBuilder implements WebAppBuilder, ContextListener {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("tomcat"), "org.apache.openejb.util.resources");

    private final TreeMap<String, ContextInfo> infos = new TreeMap<String, ContextInfo>();
    private final GlobalListenerSupport globalListenerSupport;
    private final ConfigurationFactory configurationFactory;
    private final Map<String,HostConfig> deployers = new TreeMap<String,HostConfig>();
    private Assembler assembler;

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
        assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
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
                    contextInfo.emfLinkResolver = emfLinkResolver;
                } catch (Exception e) {
                    logger.error("Unable to deploy collapsed ear in war " + standardContext.getPath() + ": Exception: " + e.getMessage(), e);
                }
            }
        }

        contextInfo.standardContext = standardContext;

        WebAppInfo webAppInfo = null;
        for (WebAppInfo w : contextInfo.appInfo.webApps) {
            if (("/" + w.contextRoot).equals(standardContext.getPath())) {
                webAppInfo = w;
                break;
            }
        }

        if (webAppInfo != null) {
            try {
                TomcatJndiBuilder jndiBuilder = new TomcatJndiBuilder(standardContext, webAppInfo, contextInfo.emfLinkResolver);
                jndiBuilder.mergeJndi();
            } catch (Exception e) {
                logger.error("Error merging OpenEJB JNDI entries in to war " + standardContext.getPath() + ": Exception: " + e.getMessage(), e);
            }
        }
    }

    public void afterStart(StandardContext standardContext) {
        // bind extra stuff at the java:comp level which can only be
        // bound after the context is created
        ContextAccessController.setWritable(standardContext.getNamingContextListener().getName(), standardContext);
        try {
            Context comp = (Context) ContextBindings.getClassLoader().lookup("comp");
            
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
        ContextInfo contextInfo = getContextInfo(standardContext);
        if (contextInfo != null && contextInfo.deployer == null) {
            try {
                assembler.destroyApplication(contextInfo.appInfo.jarPath);
            } catch (Exception e) {
                logger.error("Unable to stop web application " + standardContext.getPath() + ": Exception: " + e.getMessage(), e);
            }
            removeContextInfo(standardContext);
        }
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
}
