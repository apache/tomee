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
package org.apache.tomee.catalina;

import org.apache.catalina.Cluster;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Loader;
import org.apache.catalina.Manager;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.Service;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.Valve;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.NamingContextListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.ha.tcp.SimpleTcpCluster;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.HostConfig;
import org.apache.catalina.startup.OpenEJBContextConfig;
import org.apache.catalina.users.MemoryUserDatabase;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.naming.ContextAccessController;
import org.apache.naming.ContextBindings;
import org.apache.naming.ResourceEnvRef;
import org.apache.naming.ResourceRef;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.DeployerEjb;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ClassListInfo;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.DeploymentExceptionManager;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.InjectionBuilder;
import org.apache.openejb.assembler.classic.JndiEncBuilder;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.OpenEjbConfigurationFactory;
import org.apache.openejb.assembler.classic.PersistenceUnitInfo;
import org.apache.openejb.assembler.classic.ReloadableEntityManagerFactory;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.ServletInfo;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.assembler.classic.event.NewEjbAvailableAfterApplicationCreated;
import org.apache.openejb.cdi.CdiAppContextsService;
import org.apache.openejb.cdi.CdiBuilder;
import org.apache.openejb.cdi.OpenEJBLifecycle;
import org.apache.openejb.cdi.Proxys;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.TldScanner;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.core.ivm.IntraVmProxy;
import org.apache.openejb.core.ivm.naming.SystemComponentReference;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.BeginWebBeansListener;
import org.apache.openejb.server.httpd.EndWebBeansListener;
import org.apache.openejb.server.httpd.HttpSession;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.URLs;
import org.apache.openejb.util.proxy.LocalBeanProxyFactory;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.util.descriptor.web.ApplicationParameter;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextResourceLink;
import org.apache.tomcat.util.descriptor.web.ContextTransaction;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.descriptor.web.ResourceBase;
import org.apache.tomcat.util.http.CookieProcessor;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.apache.tomee.catalina.cdi.ServletContextHandler;
import org.apache.tomee.catalina.cdi.WebBeansThreadBindingListener;
import org.apache.tomee.catalina.cluster.ClusterObserver;
import org.apache.tomee.catalina.cluster.TomEEClusterListener;
import org.apache.tomee.catalina.environment.Hosts;
import org.apache.tomee.catalina.event.AfterApplicationCreated;
import org.apache.tomee.catalina.routing.RouterValve;
import org.apache.tomee.common.NamingUtil;
import org.apache.tomee.common.UserTransactionFactory;
import org.apache.tomee.config.TomEESystemConfig;
import org.apache.tomee.loader.TomcatHelper;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContextsService;

import javax.ejb.spi.HandleDelegate;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.servlet.ServletContext;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.Arrays.asList;
import static org.apache.tomee.catalina.Contexts.warPath;

/**
 * Web application builder.
 *
 * @version $Rev$ $Date$
 */
public class TomcatWebAppBuilder implements WebAppBuilder, ContextListener, ParentClassLoaderFinder {

    public static final String OPENEJB_CROSSCONTEXT_PROPERTY = "openejb.crosscontext";
    public static final String OPENEJB_SESSION_MANAGER_PROPERTY = "openejb.session.manager";
    public static final String OPENEJB_JSESSION_ID_SUPPORT = "openejb.jsessionid-support";
    public static final String OPENEJB_MYFACES_DISABLE_DEFAULT_VALUES = "openejb.myfaces.disable-default-values";

    /**
     * Flag for ignore context
     */
    public static final String IGNORE_CONTEXT = TomcatWebAppBuilder.class.getName() + ".IGNORE";
    /**
     * Logger instance
     */
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB.createChild("tomcat"), "org.apache.openejb.util.resources");

    public static final String DEFAULT_J2EE_SERVER = "Apache TomEE";
    public static final String OPENEJB_WEBAPP_MODULE_ID = "openejb.webapp.moduleId";

    private static final boolean FORCE_RELOADABLE = SystemInstance.get().getOptions().get("tomee.force-reloadable", false);
    private static final boolean SKIP_TLD = SystemInstance.get().getOptions().get("tomee.skip-tld", false);

    private static final Method GET_NAMING_CONTEXT_NAME; // it just sucks but that's private

    static {
        try {
            GET_NAMING_CONTEXT_NAME = StandardContext.class.getDeclaredMethod("getNamingContextName");
            GET_NAMING_CONTEXT_NAME.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            throw new OpenEJBRuntimeException("can't find method getNamingContextName", e);
        }
    }

    private final Map<String, Realm> realms = new ConcurrentHashMap<>();

    private final Map<ClassLoader, InstanceManager> instanceManagers = new ConcurrentHashMap<>();

    /**
     * Context information for web applications
     */
    private final Map<String, ContextInfo> infos = new HashMap<>();
    /**
     * Global listener for Tomcat fired events.
     */
    private final GlobalListenerSupport globalListenerSupport;
    /**
     * OpenEJB configuration factory instance
     */
    private final ConfigurationFactory configurationFactory;
    /**
     * Tomcat host config elements
     */
    //Key is the host name
    private final Map<String, HostConfig> deployers = new TreeMap<>();
    private final Hosts hosts;
    /**
     * Deployed web applications
     */
    // todo merge this map witth the infos map above
    private final Map<String, DeployedApplication> deployedApps = new TreeMap<>();
    /**
     * OpenEJB deployment loader instance
     */
    private final DeploymentLoader deploymentLoader;
    /**
     * OpenEJB assembler instance
     * TODO can we use the SPI interface instead?
     */
    private Assembler assembler;
    /**
     * OpenEJB container system
     * TODO can we use the SPI interface instead?
     */
    private CoreContainerSystem containerSystem;

    private final Map<ClassLoader, Map<String, Set<String>>> jsfClasses = new HashMap<>();

    private Class<?> sessionManagerClass;

    private final Set<CatalinaCluster> clusters = new HashSet<>();

    private ClassLoader parentClassLoader;
    private boolean initJEEInfo = true;
    private final ServletContextHandler servletContextHandler;
    private final boolean noHostCheck;

    /**
     * Creates a new web application builder
     * instance.
     */
    public TomcatWebAppBuilder() {
        SystemInstance.get().setComponent(WebAppBuilder.class, this);
        SystemInstance.get().setComponent(TomcatWebAppBuilder.class, this);
        initJEEInfo = "true".equalsIgnoreCase(SystemInstance.get().getProperty(TomEESystemConfig.TOMEE_INIT_J2EE_INFO, "true"));

        // TODO: re-write this bit, so this becomes part of the listener, and we register this with the mbean server.

        final StandardServer standardServer = TomcatHelper.getServer();
        globalListenerSupport = new GlobalListenerSupport(standardServer, this);

        //Getting host config listeners
        hosts = new Hosts();
        SystemInstance.get().setComponent(Hosts.class, hosts);
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        for (final Service service : standardServer.findServices()) {
            if (service.getContainer() instanceof Engine) {
                final Engine engine = service.getContainer();

                // add the global router if relevant
                final URL globalRouterConf = RouterValve.serverRouterConfigurationURL();
                if (globalRouterConf != null) {
                    final RouterValve routerValve = new RouterValve();
                    routerValve.setConfigurationPath(globalRouterConf);
                    engine.getPipeline().addValve(routerValve);
                }

                parentClassLoader = engine.getParentClassLoader();
                if (parentClassLoader == ClassLoader.getSystemClassLoader() && parentClassLoader != tccl) {
                    parentClassLoader = tccl;
                    engine.setParentClassLoader(tccl);
                } // else assume tomcat was setup to force a classloader and then respect it

                manageCluster(engine.getCluster());
                hosts.setDefault(engine.getDefaultHost());
                addTomEERealm(engine);

                for (final Container engineChild : engine.findChildren()) {
                    if (engineChild instanceof StandardHost) {
                        final StandardHost host = (StandardHost) engineChild;
                        manageCluster(host.getCluster());
                        addTomEERealm(host);
                        host.getPipeline().addValve(new OpenEJBSecurityListener.RequestCapturer());
                        hosts.add(host);
                        for (final LifecycleListener listener : host.findLifecycleListeners()) {
                            if (listener instanceof HostConfig) {
                                final HostConfig hostConfig = (HostConfig) listener;
                                deployers.put(host.getName(), hostConfig);
                            }
                        }
                    }
                }
            }
        }

        SystemInstance.get().addObserver(new ClusterObserver(clusters));

        final OpenEjbConfigurationFactory component = SystemInstance.get().getComponent(OpenEjbConfigurationFactory.class);
        ConfigurationFactory configurationFactory = ConfigurationFactory.class.isInstance(component) ?
                ConfigurationFactory.class.cast(component) : SystemInstance.get().getComponent(ConfigurationFactory.class);
        if (configurationFactory == null) {
            configurationFactory = new ConfigurationFactory();
        }
        this.configurationFactory = configurationFactory;
        deploymentLoader = new DeploymentLoader();

        servletContextHandler = new ServletContextHandler();
        setComponentsUsedByCDI();

        try { // before tomcat was using ServiceLoader or manually instantiation, now it uses SL for itself so we can be in conflict
            WebSockets.setConfigurator();
        } catch (final Throwable th) {
            // no-op: can be another API impl, normally we are ok, this is really just a safe belt
        }

        noHostCheck = !Boolean.parseBoolean(SystemInstance.get().getProperty("tomee.host.check", "true"));
    }

    private void setComponentsUsedByCDI() {
        final SystemInstance systemInstance = SystemInstance.get();
        if (systemInstance.getComponent(HttpServletRequest.class) == null) {
            systemInstance.setComponent(HttpServletRequest.class, Proxys.threadLocalProxy(HttpServletRequest.class, OpenEJBSecurityListener.requests, null));
        }
        if (systemInstance.getComponent(HttpSession.class) == null) {
            systemInstance.setComponent(javax.servlet.http.HttpSession.class, Proxys.threadLocalRequestSessionProxy(OpenEJBSecurityListener.requests, null));
        }
        if (systemInstance.getComponent(ServletContext.class) == null) {
            systemInstance.setComponent(ServletContext.class, Proxys.handlerProxy(servletContextHandler, ServletContext.class, CdiAppContextsService.FiredManually.class));
        }
    }

    private void manageCluster(final Cluster cluster) {
        if (cluster == null || cluster instanceof SimpleTomEETcpCluster) {
            return;
        }

        Cluster current = cluster;
        if (cluster instanceof SimpleTcpCluster) {
            final Container container = cluster.getContainer();
            current = new SimpleTomEETcpCluster((SimpleTcpCluster) cluster);
            container.setCluster(current);
        }

        if (current instanceof CatalinaCluster) {
            final CatalinaCluster haCluster = (CatalinaCluster) current;
            TomEEClusterListener listener = SystemInstance.get().getComponent(TomEEClusterListener.class);
            if (listener == null) {
                listener = new TomEEClusterListener();
                SystemInstance.get().setComponent(TomEEClusterListener.class, listener);
            }
            haCluster.addClusterListener(listener); // better to be a singleton
            clusters.add(haCluster);
        }
    }

    private void addTomEERealm(final Engine engine) {
        final Realm realm = engine.getRealm();
        if (realm != null && !(realm instanceof TomEERealm) && (engine.getParent() == null || (!realm.equals(engine.getParent().getRealm())))) {
            final Realm tomeeRealm = tomeeRealm(realm);
            engine.setRealm(tomeeRealm);
            if (LifecycleState.STARTING_PREP.equals(engine.getState())) {
                try {
                    Lifecycle.class.cast(tomeeRealm).start();
                } catch (final LifecycleException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    private void addTomEERealm(final Host host) {
        final Realm realm = host.getRealm();
        if (realm != null && !(realm instanceof TomEERealm) && (host.getParent() == null || (!realm.equals(host.getParent().getRealm())))) {
            host.setRealm(tomeeRealm(realm));
        }
    }

    protected Realm tomeeRealm(final Realm realm) {
        final TomEERealm trealm = new TomEERealm();
        trealm.setRealmPath("/tomee");
        trealm.addRealm(realm);
        return trealm;
    }

    /**
     * Start operation.
     */
    public void start() {
        globalListenerSupport.start();
    }

    /**
     * Stop operation.
     */
    public void stop() {
        globalListenerSupport.stop();
    }

    @Override
    public void start(final StandardServer server) {
        if (SystemInstance.get().isDefaultProfile()) { // add user tomee is no user are specified
            try {
                final NamingResourcesImpl resources = server.getGlobalNamingResources();
                final ContextResource userDataBaseResource = resources.findResource("UserDatabase");
                final UserDatabase db = (UserDatabase) server.getGlobalNamingContext().lookup(userDataBaseResource.getName());
                if (!db.getUsers().hasNext() && db instanceof MemoryUserDatabase) {
                    final MemoryUserDatabase mudb = (MemoryUserDatabase) db;
                    final boolean oldRo = mudb.getReadonly();
                    try {
                        ((MemoryUserDatabase) db).setReadonly(false);

                        db.createRole("tomee-admin", "tomee admin role");
                        db.createUser("tomee", "tomee", "TomEE");
                        db.findUser("tomee").addRole(db.findRole("tomee-admin"));
                    } finally {
                        mudb.setReadonly(oldRo);
                    }
                }
            } catch (final Throwable t) {
                // no-op
            }
        }
    }

    //
    // OpenEJB WebAppBuilder
    //

    /**
     * {@inheritDoc}
     */
    @Override
    public void deployWebApps(final AppInfo appInfo, final ClassLoader classLoader) throws Exception {
        try {
            for (final WebAppInfo webApp : appInfo.webApps) {
                // look for context.xml
                final File war = new File(webApp.path);
                URL contextXmlUrl = null;
                if (war.isDirectory()) {
                    final File cXml = new File(war, Constants.ApplicationContextXml).getAbsoluteFile();
                    if (cXml.exists()) {
                        contextXmlUrl = cXml.toURI().toURL();
                        LOGGER.info("using context file " + cXml.getAbsolutePath());
                    }
                } else { // war
                    try (final JarFile warAsJar = new JarFile(war)) {
                        final JarEntry entry = warAsJar.getJarEntry(Constants.ApplicationContextXml);
                        if (entry != null) {
                            contextXmlUrl = new URL("jar:" + war.getAbsoluteFile().toURI().toURL().toExternalForm() + "!/" + Constants.ApplicationContextXml);
                        }
                    }
                }

                if (isAlreadyDeployed(appInfo, webApp)) {
                    continue;
                }

                StandardContext standardContext;
                {
                    final ClassLoader containerLoader = Helper.get();
                    final Host host = hosts.getDefault();
                    if (StandardHost.class.isInstance(host) && !StandardContext.class.getName().equals(StandardHost.class.cast(host).getContextClass())) {
                        try {
                            standardContext = StandardContext.class.cast(containerLoader.loadClass(StandardHost.class.cast(host).getContextClass()).newInstance());
                        } catch (final Throwable th) {
                            LOGGER.warning("Can't use context class specified, using default StandardContext", th);
                            standardContext = new StandardContext();
                        }
                    } else {
                        standardContext = new StandardContext();
                    }
                    // should be optional but in maven parent is app loader and not maven loader which is the real parent
                    final ClassLoader currentParent = standardContext.getParentClassLoader();
                    if (currentParent == null || isParent(currentParent, containerLoader)) {
                        standardContext.setParentClassLoader(containerLoader);
                    }
                }
                standardContext.setUnpackWAR(!"false".equalsIgnoreCase(appInfo.properties.getProperty("tomcat.unpackWar")));
                if (contextXmlUrl != null) {
                    standardContext.setConfigFile(contextXmlUrl);
                }

                if (standardContext.getPath() != null) {
                    webApp.contextRoot = standardContext.getPath();
                }
                if (webApp.contextRoot.startsWith("/") || webApp.contextRoot.startsWith(File.separator)) {
                    webApp.contextRoot = webApp.contextRoot.substring(1);
                }
                if (webApp.contextRoot.startsWith(File.separator)) {
                    webApp.contextRoot = webApp.contextRoot.replaceFirst(File.separator, "");
                }

                // /!\ take care, StandardContext default host = "_" and not null or localhost
                final String hostname = Contexts.getHostname(standardContext);
                if (hostname != null && !"_".equals(hostname)) {
                    webApp.host = hostname;
                }

                final ApplicationParameter appParam = new ApplicationParameter();
                appParam.setName(OPENEJB_WEBAPP_MODULE_ID);
                appParam.setValue(webApp.moduleId);
                standardContext.addApplicationParameter(appParam);

                if (!isAlreadyDeployed(appInfo, webApp)) {
                    if (standardContext.getPath() == null) {
                        if (webApp.contextRoot != null && webApp.contextRoot.startsWith("/")) {
                            standardContext.setPath(webApp.contextRoot);
                        } else if (isRoot(webApp.contextRoot)) {
                            standardContext.setPath("");
                        } else {
                            standardContext.setPath("/" + webApp.contextRoot);
                        }
                    }
                    if (standardContext.getDocBase() == null) {
                        standardContext.setDocBase(webApp.path);
                    }
                    String docBase = standardContext.getDocBase();
                    File docBaseFile = new File(docBase);
                    if (docBase != null && docBaseFile.isFile() && docBase.endsWith(".war")) {
                        DeploymentLoader.unpack(docBaseFile);
                        if (standardContext.getPath().endsWith(".war")) {
                            standardContext.setPath(removeFirstSlashAndWar("/" + standardContext.getPath()));
                            standardContext.setName(standardContext.getPath());
                            webApp.contextRoot = standardContext.getPath();
                        }
                        standardContext.setDocBase(docBase.substring(0, docBase.length() - 4));
                    }
                    if (isRoot(standardContext.getName())) {
                        standardContext.setName("");
                        webApp.contextRoot = "";
                    }

                    if (isAlreadyDeployed(appInfo, webApp)) { // possible because of the previous renaming
                        continue;
                    }

                    // add classloader which is an URLClassLoader created by openejb
                    // {@see Assembler}
                    //
                    // we add it as parent classloader since we scanned classes with this classloader
                    // that's why we force delegate to true.
                    //
                    // However since this classloader and the webappclassloader will have a lot
                    // of common classes/resources we have to avoid duplicated resources
                    // so we contribute a custom loader.
                    //
                    // Note: the line standardContext.getLoader().setDelegate(true);
                    // could be hardcoded in the custom loader
                    // but here we have all the classloading logic
                    if (classLoader != null) {
                        standardContext.setParentClassLoader(classLoader);
                        standardContext.setDelegate(true);
                    }

                    String host = webApp.host;
                    if (host == null) {
                        host = hosts.getDefaultHost();
                        LOGGER.info("using default host: " + host);
                    }

                    if (classLoader != null) {
                        appInfo.autoDeploy = false;
                        deployWar(standardContext, host, appInfo);
                    } else { // force a normal deployment with lazy building of AppInfo
                        deployWar(standardContext, host, null);
                    }
                }
            }
        } finally { // cleanup temp var passing
            for (final WebAppInfo webApp : appInfo.webApps) {
                appInfo.properties.remove(webApp);
            }
        }
    }

    private boolean isAlreadyDeployed(final AppInfo appInfo, final WebAppInfo webApp) {
        final String version = appVersion(appInfo);
        final ContextInfo contextInfo = getContextInfo(webApp.host, webApp.contextRoot, version);
        if (contextInfo != null && contextInfo.standardContext != null && contextInfo.standardContext.getState() == LifecycleState.FAILED) {
            synchronized (infos) {
                infos.remove(getId(webApp.host, webApp.contextRoot, version));
            }
            return false;
        }
        return contextInfo != null;
    }

    private static boolean isParent(final ClassLoader parent, final ClassLoader child) {
        ClassLoader current = child;
        while (current != null) {
            if (current == parent) {
                return true;
            }
            current = current.getParent();
        }
        return child.getParent() == null && child != ClassLoader.getSystemClassLoader(); // maven ClassRealm classloader...yeah that's not awesome
    }

    private static boolean isRoot(final String name) {
        return "/ROOT".equals(name) || "ROOT".equals(name) || name == null || name.isEmpty() || "ROOT.war".equals(name);
    }

    public void deployWar(final StandardContext standardContext, final String host, final AppInfo info) {
        // TODO: instead of storing deployers, we could just lookup the right hostconfig for the server.
        final HostConfig deployer = deployers.get(host);
        if (isReady(deployer)) { // if not ready using directly host to avoid a NPE
            if (info != null) {
                final ContextInfo contextInfo = addContextInfo(host, standardContext);
                contextInfo.appInfo = info;
                contextInfo.deployer = deployer;
                contextInfo.module = extractModule(standardContext, info);
            }

            deployer.manageApp(standardContext);
        } else {
            final Host theHost = hosts.get(host);
            if (theHost != null) {
                if (info != null) {
                    final ContextInfo contextInfo = addContextInfo(host, standardContext);
                    contextInfo.appInfo = info;
                    contextInfo.host = theHost;
                    contextInfo.module = extractModule(standardContext, info);
                }

                theHost.addChild(standardContext);
            }
        }
    }

    private EjbModule extractModule(final StandardContext standardContext, final AppInfo appInfo) {
        for (final WebAppInfo app : appInfo.webApps) {
            if (app.path != null && warPath(standardContext).equals(rootPath(new File(app.path)))) {
                // see org.apache.openejb.config.ConfigurationFactory.configureApplication(java.io.File)
                return EjbModule.class.cast(appInfo.properties.remove(app));
            }
        }
        return null;
    }

    public synchronized ContextInfo standaAloneWebAppInfo(final File file) {
        for (final ContextInfo info : infos.values()) {
            if (info.appInfo != null && info.appInfo.webAppAlone
                    && ((file.equals(new File(info.appInfo.path)) || file.equals(new File(info.appInfo.path + ".war"))))) {
                return info;
            }
            if (info.standardContext != null && (file.equals(new File(info.standardContext.getDocBase())) || file.equals(new File(info.standardContext.getDocBase() + ".war")))) {
                return info;
            }
        }

        // still not found - trying another algorithm - weird but it seems to happen
        final String path = file.getAbsolutePath();
        for (final ContextInfo info : infos.values()) {
            if (info.appInfo != null && info.appInfo.webAppAlone
                    && (info.appInfo.path.endsWith(path) || (info.appInfo.path + ".war").endsWith(path))) {
                return info;
            }
        }

        return null;
    }

    public synchronized Collection<String> availableApps() {
        final Collection<String> apps = new ArrayList<>();
        for (final ContextInfo info : infos.values()) {
            if (info.appInfo != null) {
                apps.add(info.appInfo.path);
            } else if (info.standardContext != null) {
                apps.add("[not deployed] " + info.standardContext.getName());
            }
        }
        return apps;
    }

    // TODO: find something more sexy
    private static final AtomicReference<Field> HOST_CONFIG_HOST = new AtomicReference<>(null);

    static {
        try { // do it only once
            HOST_CONFIG_HOST.set(HostConfig.class.getDeclaredField("host"));
        } catch (final NoSuchFieldException e) {
            // no-op
        }
    }

    private static boolean isReady(final HostConfig deployer) {
        if (deployer != null && HOST_CONFIG_HOST.get() != null) {
            try {
                return HOST_CONFIG_HOST.get().get(deployer) != null;
            } catch (final Exception e) {
                // no-op
            }
        }
        return false;
    }

    /**
     * just to avoid a lot of log lines which are often useless.
     *
     * @param context the servlet context to init.
     */
    private static void addMyFacesDefaultParameters(final ClassLoader classLoader, final ServletContext context) {
        if (!SystemInstance.get().getOptions().get(OPENEJB_MYFACES_DISABLE_DEFAULT_VALUES, false)) {
            if (classLoader != null) {
                try { // if myfaces is not here we doesn't need any trick
                    classLoader.loadClass("org.apache.myfaces.shared.config.MyfacesConfig");
                } catch (final ClassNotFoundException cnfe) {
                    return;
                }
            }

            setInitParameter(context, "org.apache.myfaces.LOG_WEB_CONTEXT_PARAMS", "false");
            setInitParameter(context, "org.apache.myfaces.EL_RESOLVER_COMPARATOR", "org.apache.myfaces.el.unified.OpenWebBeansELResolverComparator");
            setInitParameter(context, "org.apache.myfaces.EXPRESSION_FACTORY", "org.apache.el.ExpressionFactoryImpl");
        }
    }

    private static void setInitParameter(final ServletContext context, final String key, final String value) {
        if (context.getInitParameter(key) == null) {
            context.setInitParameter(key, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeployWebApps(final AppInfo appInfo) throws Exception {
        final String version = appVersion(appInfo);
        for (final WebAppInfo webApp : appInfo.webApps) {
            final ContextInfo contextInfo = getContextInfo(webApp.host, webApp.contextRoot, version);

            if (contextInfo != null) {
                final StandardContext standardContext = contextInfo.standardContext;

                if (!appInfo.webAppAlone || !appInfo.properties.containsKey("tomee.destroying")) {
                    undeploy(standardContext, contextInfo);
                    final File extracted = Contexts.warPath(standardContext);
                    if (isExtracted(extracted)) {
                        deleteDir(extracted);
                    }
                    removeContextInfo(standardContext);
                }
            }
        }
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private boolean isExtracted(final File extracted) {
        // TODO: do we want to delete it?
        return false;
    }

    /**
     * Deletes given directory.
     *
     * @param dir directory
     */
    private void deleteDir(final File dir) {
        if (dir == null) {
            return;
        }
        if (dir.isFile()) {
            return;
        }
        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    deleteDir(file);
                } else {
                    if (!file.delete()) {
                        file.deleteOnExit();
                    }
                }
            }
        }
        if (!dir.delete()) {
            dir.deleteOnExit();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final StandardContext standardContext) {
        if (isIgnored(standardContext)) {
            return;
        }

        // just adding a carriage return to get logs more readable
        LOGGER.info("------------------------- "
                + Contexts.getHostname(standardContext).replace("_", hosts.getDefaultHost()) + " -> "
                + finalName(standardContext.getPath()));

        if (FORCE_RELOADABLE) {
            final ContextInfo ctxInfo = getContextInfo(standardContext);
            if (ctxInfo == null || (ctxInfo.appInfo != null && ctxInfo.appInfo.webAppAlone)) { // don't do it for ears
                standardContext.setReloadable(true);
            }
        }
        if (SKIP_TLD) {
            if (standardContext.getJarScanner() != null && standardContext.getJarScanner().getJarScanFilter() != null) {
                final JarScanFilter jarScanFilter = standardContext.getJarScanner().getJarScanFilter();
                if (StandardJarScanFilter.class.isInstance(jarScanFilter)) {
                    StandardJarScanFilter.class.cast(jarScanFilter).setDefaultTldScan(false);
                }
            }
        }

        final String name = standardContext.getName();

        initJ2EEInfo(standardContext);

        File warFile = Contexts.warPath(standardContext);
        if (!warFile.exists()) {
            return;
        }

        if (!warFile.isDirectory()) {
            try {
                warFile = DeploymentLoader.unpack(warFile);
            } catch (final OpenEJBException e) {
                LOGGER.error("can't unpack '" + warFile.getAbsolutePath() + "'");
            }
        }

        standardContext.setCrossContext(SystemInstance.get().getOptions().get(OPENEJB_CROSSCONTEXT_PROPERTY, false));
        standardContext.setNamingResources(new OpenEJBNamingResource(standardContext.getNamingResources()));

        String sessionManager = SystemInstance.get().getOptions().get(OPENEJB_SESSION_MANAGER_PROPERTY + "." + name, (String) null);
        if (sessionManager == null) {
            sessionManager = SystemInstance.get().getOptions().get(OPENEJB_SESSION_MANAGER_PROPERTY, (String) null);
        }
        if (sessionManager != null) {
            if (sessionManagerClass == null) {
                try { // the manager should be in standardclassloader
                    sessionManagerClass = ParentClassLoaderFinder.Helper.get().loadClass(sessionManager);
                } catch (final ClassNotFoundException e) {
                    LOGGER.error("can't find '" + sessionManager + "', StandardManager will be used", e);
                    sessionManagerClass = StandardManager.class;
                }
            }

            try {
                final Manager mgr = (Manager) sessionManagerClass.newInstance();
                standardContext.setManager(mgr);
            } catch (final Exception e) {
                LOGGER.error("can't instantiate '" + sessionManager + "', StandardManager will be used", e);
            }
        }

        final LifecycleListener[] listeners = standardContext.findLifecycleListeners();
        for (final LifecycleListener l : listeners) {
            if (l instanceof ContextConfig) {
                standardContext.removeLifecycleListener(l);
            }
        }
        standardContext.addLifecycleListener(new OpenEJBContextConfig(new StandardContextInfo(standardContext)));

        // force manually the namingContextListener to merge jndi in an easier way
        final NamingContextListener ncl = new NamingContextListener();
        try {
            ncl.setName((String) GET_NAMING_CONTEXT_NAME.invoke(standardContext));
        } catch (final Exception e) {
            ncl.setName(getId(standardContext));
        }
        ncl.setExceptionOnFailedWrite(standardContext.getJndiExceptionOnFailedWrite());
        standardContext.setNamingContextListener(ncl);
        standardContext.addLifecycleListener(ncl);
        standardContext.addLifecycleListener(new TomcatJavaJndiBinder());

        // listen some events
        standardContext.addContainerListener(new TomEEContainerListener());
    }

    public void initJ2EEInfo(final StandardContext standardContext) {
        if (initJEEInfo) {
            standardContext.setJ2EEServer(DEFAULT_J2EE_SERVER);

            final ContextInfo contextInfo = getContextInfo(standardContext);
            if (contextInfo == null || contextInfo.appInfo == null || contextInfo.appInfo.path == null) {
                standardContext.setJ2EEApplication(jmxName(standardContext.getName()));
            } else {
                standardContext.setJ2EEApplication(jmxName(shortName(contextInfo.appInfo.path)));
            }
        }
    }

    private String jmxName(final String name) { // see javax.management.ObjectName.construct()
        return name.replace(':', '_');
    }

    private String shortName(final String path) {
        if (path.contains("/")) {
            return path.substring(path.lastIndexOf('/'), path.length());
        }
        return path;
    }

    private static String finalName(final String path) {
        if (isRoot(path)) {
            return "/";
        }
        return path;
    }

    public ContextInfo getContextInfo(final String appName) {
        ContextInfo info = null;
        for (final Map.Entry<String, ContextInfo> current : infos.entrySet()) {
            final String key = current.getKey();
            if (key.equals(appName)) {
                info = current.getValue();
                break;
            }
            if (key.endsWith(appName)) {
                info = current.getValue();
            }
        }
        return info;
    }

    public class StandardContextInfo {

        private final StandardContext standardContext;

        public StandardContextInfo(final StandardContext standardContext) {
            this.standardContext = standardContext;
            if (standardContext == null) {
                final Throwable throwable = new Exception("StandardContext is null").fillInStackTrace();
                LOGGER.warning("StandardContext should not be null", throwable);
            }
        }

        public AppInfo app() {
            final ContextInfo contextInfo = getContextInfo(standardContext);
            if (contextInfo == null) {
                LOGGER.debug("No ContextInfo for StandardContext " + standardContext.getName());
                return null;
            }
            return contextInfo.appInfo;
        }

        public WebAppInfo get() {
            if (standardContext == null) {
                return null;
            }

            final ContextInfo contextInfo = getContextInfo(standardContext);
            if (contextInfo == null) {
                LOGGER.debug("No ContextInfo for StandardContext " + standardContext.getName());
                return null;
            }

            LOGGER.debug("contextInfo = " + contextInfo);
            LOGGER.debug("standardContext = " + standardContext);

            if (contextInfo.appInfo == null) {
                LOGGER.debug("ContextInfo has no AppInfo for StandardContext " + standardContext.getName());
                return null;
            }

            final String id = getId(standardContext);
            for (final WebAppInfo webApp : contextInfo.appInfo.webApps) {
                if (webApp == null) {
                    LOGGER.debug("ContextInfo.appInfo.webApps entry is null StandardContext " + standardContext.getName());
                    continue;
                }

                final String wId = getId(webApp.host, webApp.contextRoot, contextInfo.version);
                if (id.equals(wId)) {
                    return webApp;
                }
            }
            return null;
        }

        public ClassLoader loader() {
            if (standardContext != null && standardContext.getLoader() != null) {
                return standardContext.getLoader().getClassLoader();
            }
            return null;
        }

        @Override
        public String toString() {
            if (standardContext == null) {
                return super.toString();
            }

            return "StandardContextInfo{" +
                    "standardContext=" + standardContext +
                    '}';
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeStart(final StandardContext standardContext) {
        if (standardContext.getResources() != null && LazyStopStandardRoot.class.isInstance(standardContext.getResources())) {
            // reset after reload
            Reflections.set(standardContext, "resources",  LazyStopStandardRoot.class.cast(standardContext.getResources()).getDelegate());
        }

        final ServletContext sc = standardContext.getServletContext();
        if (sc != null && !SystemInstance.get().getOptions().get(OPENEJB_JSESSION_ID_SUPPORT, true)) {
            final Set<SessionTrackingMode> defaultTrackingModes = sc.getEffectiveSessionTrackingModes();
            if (defaultTrackingModes.contains(SessionTrackingMode.URL)) {
                final Set<SessionTrackingMode> newModes = new HashSet<>();
                newModes.remove(SessionTrackingMode.URL);
                sc.setSessionTrackingModes(newModes);
            }
        }
        initContextLoader(standardContext);

        // used to add custom filters first - our arquillian integration uses it for instance
        // needs to be done now (= before start event) because of addFilterMapBefore() usage
        final String filters = SystemInstance.get().getProperty("org.apache.openejb.servlet.filters");
        if (filters != null) {
            final String[] names = filters.split(",");
            for (final String name : names) {
                final String[] clazzMapping = name.split("=");

                final FilterDef filterDef = new FilterDef();
                filterDef.setFilterClass(clazzMapping[0]);
                filterDef.setFilterName(clazzMapping[0]);
                standardContext.addFilterDef(filterDef);

                final FilterMap filterMap = new FilterMap();
                filterMap.setFilterName(clazzMapping[0]);
                filterMap.addURLPattern(clazzMapping[1]);
                standardContext.addFilterMapBefore(filterMap);
            }
        }

        // mainly to get back compatibility with tomcat <= 8.0
        final String cookieProcessor = SystemInstance.get().getProperty("tomee.tomcat.cookieProcessor");
        if (cookieProcessor != null) {
            // not that important for now if we use the container loader, we mainly want to be able to access
            // the legacy one
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                final Class<?> cookieProcessorClass = contextClassLoader.loadClass(cookieProcessor.trim());
                standardContext.setCookieProcessor(CookieProcessor.class.cast(cookieProcessorClass.newInstance()));
            } catch (final Exception e) {
                throw new IllegalArgumentException("Cannot set CookieProcessor: " + cookieProcessor);
            }
        }
    }

    private void initContextLoader(final StandardContext standardContext) {
        final Loader standardContextLoader = standardContext.getLoader();
        if (standardContextLoader != null
                && (
                (!TomEEWebappLoader.class.equals(standardContextLoader.getClass())
                    && !WebappLoader.class.equals(standardContextLoader.getClass()))
                        || (WebappLoader.class.equals(standardContextLoader.getClass())
                                && !WebappLoader.class.cast(standardContextLoader).getLoaderClass().startsWith("org.apache.tom")))
                ) {
            // custom loader, we don't know it
            // and since we don't have a full delegate pattern for our lazy stop loader
            // simply skip lazy stop loader - normally sides effect will be an early shutdown for ears and some particular features
            // only affecting the app if the classes were not laoded at all
            return;
        }

        if (standardContextLoader != null && TomEEWebappLoader.class.isInstance(standardContextLoader)) {
            standardContextLoader.setContext(standardContext);
            return; // no need to replace the loader
        }

        // we just want to wrap it to lazy stop it (afterstop)
        // to avoid classnotfound in @PreDestoy or destroyApplication()
        final TomEEWebappLoader loader = new TomEEWebappLoader();
        loader.setDelegate(standardContext.getDelegate());
        loader.setLoaderClass(TomEEWebappClassLoader.class.getName());

        final Loader lazyStopLoader = new LazyStopLoader(loader);
        standardContext.setLoader(lazyStopLoader);
    }

    @Override
    public void configureStart(final LifecycleEvent event, final StandardContext standardContext) {
        final ContextTransaction contextTransaction = new ContextTransaction();
        contextTransaction.setProperty(org.apache.naming.factory.Constants.FACTORY, UserTransactionFactory.class.getName());
        standardContext.getNamingResources().setTransaction(contextTransaction);

        if (event != null) {
            // ensure NamingContext is available for eager usage (@Observes @Initialized(ApplicationScoped) for instance)
            standardContext.getNamingContextListener().lifecycleEvent(event);
        }

        TomcatHelper.configureJarScanner(standardContext);

        startInternal(standardContext);

        // clear a bit log for default case
        addMyFacesDefaultParameters(standardContext.getLoader().getClassLoader(), standardContext.getServletContext());

        // breaks cdi
        standardContext.setTldValidation(Boolean.parseBoolean(SystemInstance.get().getProperty("tomee.tld.validation", "false")));
        // breaks jstl
        standardContext.setXmlValidation(Boolean.parseBoolean(SystemInstance.get().getProperty("tomee.xml.validation", "false")));
    }

    @Override
    public void start(final StandardContext standardContext) {
        // nothing to do for now
    }

    /**
     * {@inheritDoc}
     */
//    @Override
    private void startInternal(final StandardContext standardContext) {
        if (isIgnored(standardContext)) {
            return;
        }

        if (shouldNotDeploy(standardContext)) {
            return;
        }

        final CoreContainerSystem cs = getContainerSystem();

        final Assembler a = getAssembler();
        if (a == null) {
            LOGGER.warning("OpenEJB has not been initialized so war will not be scanned for nested modules " + standardContext.getPath());
            return;
        }

        AppContext appContext = null;
        //Look for context info, maybe context is already scanned
        ContextInfo contextInfo = getContextInfo(standardContext);
        ClassLoader classLoader = standardContext.getLoader().getClassLoader();

        // bind jta before the app starts to ensure we have it in CDI
        final Thread thread = Thread.currentThread();
        final ClassLoader originalLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(classLoader);

        final String listenerName = standardContext.getNamingContextListener().getName();
        ContextAccessController.setWritable(listenerName, standardContext.getNamingToken());
        try {
            final Context comp = Context.class.cast(ContextBindings.getClassLoader().lookup("comp"));

            // bind TransactionManager
            final TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
            safeBind(comp, "TransactionManager", transactionManager);

            // bind TransactionSynchronizationRegistry
            final TransactionSynchronizationRegistry synchronizationRegistry = SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class);
            safeBind(comp, "TransactionSynchronizationRegistry", synchronizationRegistry);
        } catch (final NamingException e) {
            // no-op
        } finally {
            thread.setContextClassLoader(originalLoader);
            ContextAccessController.setReadOnly(listenerName);
        }

        if (contextInfo == null) {
            final AppModule appModule = loadApplication(standardContext);
            appModule.getProperties().put("loader.from", "tomcat");

            final boolean skipTomeeResourceWrapping = !"true".equalsIgnoreCase(SystemInstance.get().getProperty("tomee.tomcat.resource.wrap", "true"));
            if (!skipTomeeResourceWrapping && OpenEJBNamingResource.class.isInstance(standardContext.getNamingResources())) {
                final Collection<String> importedNames = new ArrayList<>(); // we can get the same resource twice as in tomcat

                // add them to the app as resource
                final boolean forceDataSourceWrapping = "true".equalsIgnoreCase(SystemInstance.get().getProperty("tomee.tomcat.datasource.wrap", "false"));
                final OpenEJBNamingResource nr = (OpenEJBNamingResource) standardContext.getNamingResources();
                for (final ResourceBase resource : nr.getTomcatResources()) {
                    final String name = resource.getName();

                    // already init (org.apache.catalina.core.NamingContextListener.addResource())
                    // skip wrapping to ensure resource consistency
                    final boolean isDataSource = DataSource.class.getName().equals(resource.getType());
                    final boolean isAlreadyCreated = ContextResource.class.isInstance(resource) && ContextResource.class.cast(resource).getSingleton() && isDataSource;

                    if (!importedNames.contains(name)) {
                        importedNames.add(name);
                    } else {
                        continue;
                    }

                    boolean found = false;
                    for (final ResourceInfo r : SystemInstance.get().getComponent(OpenEjbConfiguration.class).facilities.resources) {
                        if (r.id.equals(name)) {
                            nr.removeResource(name);
                            found = true;
                            LOGGER.warning(name + " resource was defined in both tomcat and tomee so removing tomcat one");
                            break;
                        }
                    }

                    if (!found) {
                        final Resource newResource;

                        if (forceDataSourceWrapping || (!isAlreadyCreated && isDataSource)) { // we forward it to TomEE datasources
                            newResource = new Resource(name, resource.getType());

                            boolean jta = false;

                            final Properties properties = newResource.getProperties();
                            final Iterator<String> params = resource.listProperties();
                            while (params.hasNext()) {
                                final String paramName = params.next();
                                final String paramValue = (String) resource.getProperty(paramName);

                                // handling some param name conversion to OpenEJB style
                                if ("driverClassName".equals(paramName)) {
                                    properties.setProperty("JdbcDriver", paramValue);
                                } else if ("url".equals(paramName)) {
                                    properties.setProperty("JdbcUrl", paramValue);
                                } else {
                                    properties.setProperty(paramName, paramValue);
                                }

                                if ("JtaManaged".equalsIgnoreCase(paramName)) {
                                    jta = Boolean.parseBoolean(paramValue);
                                }
                            }

                            if (!jta) {
                                properties.setProperty("JtaManaged", "false");
                            }
                        } else { // custom type, let it be created
                            newResource = new Resource(name, resource.getType(), "org.apache.tomee:ProvidedByTomcat");

                            final Properties properties = newResource.getProperties();
                            properties.setProperty("jndiName", newResource.getId());
                            properties.setProperty("appName", getId(standardContext));
                            properties.setProperty("factory", (String) resource.getProperty("factory"));

                            final Reference reference = createReference(resource);
                            if (reference != null) {
                                properties.put("reference", reference);
                            }
                        }

                        appModule.getResources().add(newResource);
                    }
                }
            }

            if (appModule != null) {
                try {
                    contextInfo = addContextInfo(Contexts.getHostname(standardContext), standardContext);
                    contextInfo.standardContext = standardContext; // ensure to do it before an exception can be thrown

                    contextInfo.appInfo = configurationFactory.configureApplication(appModule);

                    final Boolean autoDeploy = DeployerEjb.AUTO_DEPLOY.get();
                    contextInfo.appInfo.autoDeploy = autoDeploy == null || autoDeploy;
                    DeployerEjb.AUTO_DEPLOY.remove();

                    if (!appModule.isWebapp()) {
                        classLoader = appModule.getClassLoader();
                    } else {
                        final ClassLoader loader = standardContext.getLoader().getClassLoader();
                        if (loader instanceof TomEEWebappClassLoader) {
                            final TomEEWebappClassLoader tomEEWebappClassLoader = (TomEEWebappClassLoader) loader;
                            for (final URL url : appModule.getWebModules().iterator().next().getAddedUrls()) {
                                tomEEWebappClassLoader.addURL(url);
                            }
                        }
                    }

                    setFinderOnContextConfig(standardContext, appModule);

                    servletContextHandler.getContexts().put(classLoader, standardContext.getServletContext());
                    try {
                        appContext = a.createApplication(contextInfo.appInfo, classLoader);
                    } finally {
                        servletContextHandler.getContexts().remove(classLoader);
                    }
                    // todo add watched resources to context

                    eagerInitOfLocalBeanProxies(appContext.getBeanContexts(), classLoader);
                } catch (final Exception e) {
                    LOGGER.error("Unable to deploy collapsed ear in war " + standardContext, e);
                    undeploy(standardContext, contextInfo);
                    // just to force tomee to start without EE part
                    if (System.getProperty(TomEESystemConfig.TOMEE_EAT_EXCEPTION_PROP) == null) {
                        final TomEERuntimeException tre = new TomEERuntimeException(e);
                        final DeploymentExceptionManager dem = SystemInstance.get().getComponent(DeploymentExceptionManager.class);
                        dem.saveDeploymentException(contextInfo.appInfo, tre);
                        throw tre;
                    }
                    return;
                }
            }
        } else {
            contextInfo.standardContext = standardContext;
            if (contextInfo.module != null && contextInfo.module.getFinder() != null) { // TODO: make it more explicit or less hacky not using properties
                final OpenEJBContextConfig openEJBContextConfig = findOpenEJBContextConfig(standardContext);
                if (openEJBContextConfig != null) {
                    openEJBContextConfig.finder(contextInfo.module.getFinder(), contextInfo.module.getClassLoader());
                }
            }
        }

        final String id = getId(standardContext);
        WebAppInfo webAppInfo = null;
        // appInfo is null when deployment fails
        if (contextInfo.appInfo != null) {
            for (final WebAppInfo w : contextInfo.appInfo.webApps) {
                if (id.equals(getId(w.host, w.contextRoot, contextInfo.version)) || id.equals(getId(w.host, w.moduleId, contextInfo.version))) {
                    if (webAppInfo == null) {
                        webAppInfo = w;
                    } else if (w.host != null && w.host.equals(Contexts.getHostname(standardContext))) {
                        webAppInfo = w;
                    }

                    break;
                }
            }

            if (appContext == null) {
                appContext = cs.getAppContext(contextInfo.appInfo.appId);
            }
        }

        if (webAppInfo != null) {

            if (appContext == null) {
                appContext = getContainerSystem().getAppContext(contextInfo.appInfo.appId);
            }

            // ensure matching (see getId() usage)
            webAppInfo.host = Contexts.getHostname(standardContext);
            webAppInfo.contextRoot = standardContext.getPath();

            // save jsf stuff
            final Map<String, Set<String>> scannedJsfClasses = new HashMap<>();
            for (final ClassListInfo info : webAppInfo.jsfAnnotatedClasses) {
                scannedJsfClasses.put(info.name, info.list);
            }
            jsfClasses.put(classLoader, scannedJsfClasses);

            try {

                // determine the injections
                final Set<Injection> injections = new HashSet<>();
                injections.addAll(appContext.getInjections());

                if (!contextInfo.appInfo.webAppAlone) {
                    updateInjections(injections, classLoader, false);
                    for (final BeanContext bean : appContext.getBeanContexts()) { // TODO: how if the same class in multiple webapps?
                        updateInjections(bean.getInjections(), classLoader, true);
                    }
                }
                injections.addAll(new InjectionBuilder(classLoader).buildInjections(webAppInfo.jndiEnc));

                // merge OpenEJB jndi into Tomcat jndi
                final TomcatJndiBuilder jndiBuilder = new TomcatJndiBuilder(standardContext, webAppInfo, injections);
                NamingUtil.setCurrentContext(standardContext);
                try {
                    jndiBuilder.mergeJndi();
                } finally {
                    NamingUtil.setCurrentContext(null);
                }

                // create EMF included in this webapp when nested in an ear
                for (final PersistenceUnitInfo unitInfo : contextInfo.appInfo.persistenceUnits) {
                    if (unitInfo.webappName != null && unitInfo.webappName.equals(webAppInfo.moduleId)) {
                        try {
                            final ReloadableEntityManagerFactory remf =
                                    (ReloadableEntityManagerFactory) SystemInstance.get().getComponent(ContainerSystem.class)
                                            .getJNDIContext().lookup(Assembler.PERSISTENCE_UNIT_NAMING_CONTEXT + unitInfo.id);
                            remf.overrideClassLoader(classLoader);
                            remf.createDelegate();
                        } catch (final NameNotFoundException nnfe) {
                            LOGGER.warning("Can't find " + unitInfo.id + " persistence unit");
                        }
                    }
                }

                // add WebDeploymentInfo to ContainerSystem
                final WebContext webContext = new WebContext(appContext);
                webContext.setServletContext(standardContext.getServletContext());
                webContext.setJndiEnc(new InitialContext());
                webContext.setClassLoader(classLoader);
                webContext.setId(webAppInfo.moduleId);
                webContext.setContextRoot(webAppInfo.contextRoot);
                webContext.setHost(webAppInfo.host);
                webContext.setBindings(new HashMap<String, Object>());
                webContext.getInjections().addAll(injections);
                appContext.getWebContexts().add(webContext);
                cs.addWebContext(webContext);
                standardContext.getServletContext().setAttribute("openejb.web.context", webContext);

                if (!contextInfo.appInfo.webAppAlone) {
                    final List<BeanContext> beanContexts = assembler.initEjbs(classLoader, contextInfo.appInfo, appContext, injections, new ArrayList<BeanContext>(), webAppInfo.moduleId);
                    OpenEJBLifecycle.CURRENT_APP_INFO.set(contextInfo.appInfo);
                    servletContextHandler.getContexts().put(classLoader, standardContext.getServletContext());
                    try {
                        new CdiBuilder().build(contextInfo.appInfo, appContext, beanContexts, webContext);
                    } catch (final Exception e) {
                        final DeploymentExceptionManager dem = SystemInstance.get().getComponent(DeploymentExceptionManager.class);
                        if (dem != null) {
                            dem.saveDeploymentException(contextInfo.appInfo, e);
                        }
                        throw e;
                    } finally {
                        servletContextHandler.getContexts().remove(classLoader);
                        OpenEJBLifecycle.CURRENT_APP_INFO.remove();
                    }
                    assembler.startEjbs(true, beanContexts);
                    assembler.bindGlobals(appContext.getBindings());
                    eagerInitOfLocalBeanProxies(beanContexts, standardContext.getLoader().getClassLoader());

                    deployWebServicesIfEjbCreatedHere(contextInfo.appInfo, beanContexts);
                }

                // jndi bindings
                webContext.getBindings().putAll(appContext.getBindings());
                webContext.getBindings().putAll(getJndiBuilder(classLoader, webAppInfo, injections, appContext.getProperties()).buildBindings(JndiEncBuilder.JndiScope.comp));

                final JavaeeInstanceManager instanceManager = new JavaeeInstanceManager(standardContext, webContext);
                standardContext.setInstanceManager(instanceManager);
                instanceManagers.put(classLoader, instanceManager);
                standardContext.getServletContext().setAttribute(InstanceManager.class.getName(), standardContext.getInstanceManager());
            } catch (final Exception e) {
                LOGGER.error("Error merging Java EE JNDI entries in to war " + standardContext.getPath() + ": Exception: " + e.getMessage(), e);
                if (System.getProperty(TomEESystemConfig.TOMEE_EAT_EXCEPTION_PROP) == null) {
                    final DeploymentExceptionManager dem = SystemInstance.get().getComponent(DeploymentExceptionManager.class);
                    if (dem != null && dem.getDeploymentException(contextInfo.appInfo) != null) {
                        if (RuntimeException.class.isInstance(e)) {
                            throw RuntimeException.class.cast(e);
                        }
                        throw new TomEERuntimeException(e);
                    }
                }
            }

            final WebBeansContext webBeansContext = appContext.getWebBeansContext();
            if (webBeansContext != null && webBeansContext.getBeanManagerImpl().isInUse()) {
                OpenEJBLifecycle.initializeServletContext(standardContext.getServletContext(), webBeansContext);
            }
        }

        // router
        final URL routerConfig = RouterValve.configurationURL(standardContext.getServletContext());
        if (routerConfig != null) {
            final RouterValve filter = new RouterValve();
            filter.setPrefix(standardContext.getName());
            filter.setConfigurationPath(routerConfig);
            standardContext.getPipeline().addValve(filter);
        }

        // register realm to have it in TomcatSecurityService
        final Realm realm = standardContext.getRealm();
        realms.put(standardContext.getName(), realm);
    }

    private static boolean shouldNotDeploy(final StandardContext standardContext) {
        if (StandardHost.class.isInstance(standardContext.getParent())) {
            final StandardHost host = StandardHost.class.cast(standardContext.getParent());
            if (host.getAutoDeploy() && standardContext.getDocBase() != null && 
                    standardContext.getDocBase() != null &&
                    new File(host.getAppBaseFile(), standardContext.getDocBase()).isDirectory() && (
                    new File(host.getAppBaseFile(), standardContext.getDocBase() + ".ear").exists() ||
                    new File(host.getAppBaseFile(), standardContext.getDocBase() + ".rar").exists())
            ) {

                LOGGER.info(String.format("Not deploying exploded directory %s as Java EE artifact exists which will be deployed.",
                        new File(host.getAppBaseFile(), standardContext.getPath()).getAbsolutePath()));

                return true;
            }
        }
        return false;
    }

    public void setFinderOnContextConfig(final StandardContext standardContext, final AppModule appModule) {
        final OpenEJBContextConfig openEJBContextConfig = findOpenEJBContextConfig(standardContext);
        if (openEJBContextConfig != null) {
            for (final EjbModule ejbModule : appModule.getEjbModules()) {
                if (ejbModule.getFile() != null && warPath(standardContext).equals(rootPath(ejbModule.getFile()))) {
                    openEJBContextConfig.finder(ejbModule.getFinder(), ejbModule.getClassLoader());
                    break;
                }
            }
        }
    }

    private OpenEJBContextConfig findOpenEJBContextConfig(StandardContext standardContext) {
        OpenEJBContextConfig openEJBContextConfig = null;
        for (final LifecycleListener listener : standardContext.findLifecycleListeners()) {
            if (OpenEJBContextConfig.class.isInstance(listener)) {
                openEJBContextConfig = OpenEJBContextConfig.class.cast(listener);
                break;
            }
        }
        return openEJBContextConfig;
    }

    private static File rootPath(final File file) {
        if (file.isDirectory() && file.getName().equals("classes") && file.getParentFile() != null && file.getParentFile().getName().equals("WEB-INF")) {
            final File parentFile = file.getParentFile().getParentFile();
            try {
                return parentFile.getCanonicalFile();
            } catch (final IOException e) {
                return parentFile;
            }
        }
        try {
            return file.getCanonicalFile();
        } catch (final IOException e) {
            return file;
        }
    }

    private static void deployWebServicesIfEjbCreatedHere(final AppInfo info, final Collection<BeanContext> beanContexts) {
        if (beanContexts == null || beanContexts.isEmpty()) {
            return;
        }
        SystemInstance.get().fireEvent(new NewEjbAvailableAfterApplicationCreated(info, beanContexts));
    }

    private static void eagerInitOfLocalBeanProxies(final Collection<BeanContext> beans, final ClassLoader classLoader) {
        for (final BeanContext deployment : beans) {
            if (deployment.isLocalbean() && !deployment.isDynamicallyImplemented()) { // init proxy eagerly otherwise deserialization of serialized object can't work
                final List<Class> interfaces = new ArrayList<>(2);
                interfaces.add(Serializable.class);
                interfaces.add(IntraVmProxy.class);
                final BeanType type = deployment.getComponentType();
                if (BeanType.STATEFUL.equals(type) || BeanType.MANAGED.equals(type)) {
                    interfaces.add(BeanContext.Removable.class);
                }
                try {
                    LocalBeanProxyFactory.createProxy(deployment.getBeanClass(), classLoader, interfaces.toArray(new Class<?>[interfaces.size()]));
                } catch (final Exception e) {
                    // no-op: as before
                }
            }
        }
    }

    private static Reference createReference(final ResourceBase resource) {
        final Reference ref;
        if (resource instanceof ContextResource) {
            final ContextResource cr = (ContextResource) resource;
            ref = new ResourceRef(resource.getType(), resource.getDescription(), cr.getScope(), cr.getAuth(), cr.getSingleton());
        } else {
            ref = new ResourceEnvRef(resource.getType());
        }

        final Iterator<String> params = resource.listProperties();
        while (params.hasNext()) {
            final String paramName = params.next();
            final String paramValue = (String) resource.getProperty(paramName);
            final StringRefAddr refAddr = new StringRefAddr(paramName, paramValue);
            ref.add(refAddr);
        }

        return ref;
    }

    private static void updateInjections(final Collection<Injection> injections, final ClassLoader classLoader, final boolean keepInjection) {
        final Iterator<Injection> it = injections.iterator();
        final List<Injection> newOnes = new ArrayList<>();
        while (it.hasNext()) {
            final Injection injection = it.next();
            if (injection.getTarget() == null) {
                try {
                    final Class<?> target = classLoader.loadClass(injection.getClassname());
                    if (keepInjection) {
                        final Injection added = new Injection(injection.getJndiName(), injection.getName(), target);
                        newOnes.add(added);
                    } else {
                        injection.setTarget(target);
                    }
                } catch (final ClassNotFoundException cnfe) {
                    // ignored
                }
            }
        }

        if (!newOnes.isEmpty()) {
            injections.addAll(newOnes);
        }
    }

    // return true if the dir can be deleted. TODO: revisit this heuristic
    private static boolean undeploy(final StandardContext standardContext, final ContextInfo contextInfo) {
        if (isReady(contextInfo.deployer)) {
            contextInfo.deployer.unmanageApp(standardContext.getName());
            return true;
        } else if (contextInfo.host != null) {
            return undeploy(standardContext, contextInfo.host);
        } else {
            Container container = contextInfo.standardContext;
            while (container != null) {
                if (container instanceof Host) {
                    break;
                }
                container = container.getParent();
            }
            return container != null && undeploy(standardContext, container);
        }
    }

    private static boolean undeploy(final StandardContext standardContext, final Container host) {
        final Container child = host.findChild(standardContext.getName());

        // skip undeployment if redeploying (StandardContext.redeploy())
        if (child instanceof org.apache.catalina.Context && org.apache.catalina.Context.class.cast(child).getPaused()) {
            return true;
        }

        // skip undeployment if restarting
        final TomEEWebappClassLoader tomEEWebappClassLoader = lazyClassLoader(
            org.apache.catalina.Context.class.isInstance(child) ? org.apache.catalina.Context.class.cast(child) : null);
        if (tomEEWebappClassLoader != null && tomEEWebappClassLoader.isRestarting()) {
            return true;
        }

        if (child != null) {
            host.removeChild(standardContext);
            return true;
        }
        return false;
    }

    private static TomEEWebappClassLoader lazyClassLoader(final org.apache.catalina.Context child) {
        if (child == null) {
            return null;
        }

        final Loader loader = child.getLoader();
        if (loader == null || !(loader instanceof LazyStopLoader)) {
            return null;
        }

        final ClassLoader old = ((LazyStopLoader) loader).getStopClassLoader();
        if (old == null || !(old instanceof TomEEWebappClassLoader)) {
            return null;
        }

        return (TomEEWebappClassLoader) old;
    }

    private JndiEncBuilder getJndiBuilder(final ClassLoader classLoader, final WebAppInfo webAppInfo, final Set<Injection> injections, final Properties props) throws OpenEJBException {
        return new JndiEncBuilder(webAppInfo.jndiEnc, injections, webAppInfo.moduleId, "Bean", null, webAppInfo.uniqueId, classLoader, props);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterStart(final StandardContext standardContext) {
        if (isIgnored(standardContext)) {
            return;
        }

        if (shouldNotDeploy(standardContext)) {
            return;
        }

        final Realm realm = standardContext.getRealm();
        final ClassLoader classLoader = standardContext.getLoader().getClassLoader();
        final Thread thread = Thread.currentThread();
        final ClassLoader originalLoader = thread.getContextClassLoader();
        if (realm != null && !(realm instanceof TomEERealm) && (standardContext.getParent() == null || (!realm.equals(standardContext.getParent().getRealm())))) {
            thread.setContextClassLoader(classLoader);
            try {
                standardContext.setRealm(tomeeRealm(realm));
            } finally {
                thread.setContextClassLoader(originalLoader);
            }
        }

        // if appInfo is null this is a failed deployment... just ignore
        final ContextInfo contextInfo = getContextInfo(standardContext);
        contextInfo.module = null; // shouldnt be there after startup (actually we shouldnt need it from info tree but our scanning does)
        if (contextInfo != null && contextInfo.appInfo == null) {
            return;
        } else if (contextInfo == null) { // openejb webapp loaded from the LoaderServlet
            return;
        }

        final String id = getId(standardContext);
        WebAppInfo currentWebAppInfo = null;
        for (final WebAppInfo webAppInfo : contextInfo.appInfo.webApps) {
            final String wId = getId(webAppInfo.host, webAppInfo.contextRoot, contextInfo.version);
            if (id.equals(wId)) {
                currentWebAppInfo = webAppInfo;
                break;
            }
        }

        // bind extra stuff at the java:comp level which can only be
        // bound after the context is created
        thread.setContextClassLoader(classLoader);

        final NamingContextListener ncl = standardContext.getNamingContextListener();
        final String listenerName = ncl.getName();
        ContextAccessController.setWritable(listenerName, standardContext.getNamingToken());
        try {
            final Context openejbContext = (Context) getContainerSystem().getJNDIContext().lookup("openejb");
            final Context root = (Context) ContextBindings.getClassLoader().lookup("");
            final Context comp = (Context) ContextBindings.getClassLoader().lookup("comp"); // usually fails

            // Context root = ncl.getNamingContext();
            // Context comp = (Context) root.lookup("comp");
            safeBind(root, "openejb", openejbContext);

            // add context to WebDeploymentInfo
            if (currentWebAppInfo != null) {
                final WebContext webContext = getContainerSystem().getWebContext(currentWebAppInfo.moduleId);
                if (webContext != null) {
                    webContext.setJndiEnc(root);
                }

                try {
                    // Bean Validation
                    standardContext.getServletContext().setAttribute("javax.faces.validator.beanValidator.ValidatorFactory", openejbContext.lookup(Assembler.VALIDATOR_FACTORY_NAMING_CONTEXT.replaceFirst("openejb", "") + currentWebAppInfo.uniqueId));
                } catch (final NamingException ne) {
                    LOGGER.warning("no validator factory found for webapp " + currentWebAppInfo.moduleId);
                }
            }

            try {
                final Class<?> orb = TomcatWebAppBuilder.class.getClassLoader().loadClass("org.omg.CORBA.ORB");
                if (SystemInstance.get().getComponent(orb) != null) {
                    safeBind(comp, "ORB", new SystemComponentReference(orb));
                }
            } catch (final NoClassDefFoundError | ClassNotFoundException cnfe) {
                // no-op
            }
            if (SystemInstance.get().getComponent(HandleDelegate.class) != null) {
                safeBind(comp, "HandleDelegate", new SystemComponentReference(HandleDelegate.class));
            }
        } catch (final NamingException e) {
            // no-op
        } finally {
            // required for Pojo Web Services because when Assembler creates the application
            // the CoreContainerSystem does not contain the WebContext
            // see also the start method getContainerSystem().addWebDeployment(webContext);
            try {
                servletContextHandler.getContexts().put(classLoader, standardContext.getServletContext());

                for (final WebAppInfo webAppInfo : contextInfo.appInfo.webApps) {
                    final String wId = getId(webAppInfo.host, webAppInfo.contextRoot, contextInfo.version);
                    if (id.equals(wId)) {
                        // Allow any post-deployment to happen without the RequestContext of a call to /tomee/ejb
                        final Request request = OpenEJBSecurityListener.requests.get();
                        OpenEJBSecurityListener.requests.remove();

                        SystemInstance.get().fireEvent(
                                new AfterApplicationCreated(contextInfo.appInfo,
                                                            webAppInfo,
                                                            standardContext.getServletContext()));

                        if (request != null) {
                            OpenEJBSecurityListener.requests.set(request);
                        }
                        break;
                    }
                }
            } finally {
                servletContextHandler.getContexts().remove(classLoader);
            }

            thread.setContextClassLoader(originalLoader);
            ContextAccessController.setReadOnly(listenerName);
        }

        thread.setContextClassLoader(classLoader);
        try {
            // owb integration filters
            final WebBeansContext webBeansContext = getWebBeansContext(contextInfo);
            if (webBeansContext != null) {
                // it is important to have a begin and a end listener
                // to be sure to create contexts before other listeners
                // and destroy contexts after other listeners

                final BeginWebBeansListener beginWebBeansListener = new BeginWebBeansListener(webBeansContext);
                final EndWebBeansListener endWebBeansListener = new EndWebBeansListener(webBeansContext);

                {
                    final Object[] appEventListeners = standardContext.getApplicationEventListeners();
                    final Object[] newEventListeners = new Object[appEventListeners.length + 2];

                    newEventListeners[0] = beginWebBeansListener;
                    System.arraycopy(appEventListeners, 0, newEventListeners, 1, appEventListeners.length);
                    newEventListeners[newEventListeners.length - 1] = endWebBeansListener;
                    standardContext.setApplicationEventListeners(newEventListeners);
                }

                {
                    final Object[] lifecycleListeners = standardContext.getApplicationLifecycleListeners();
                    final Object[] newLifecycleListeners = new Object[lifecycleListeners.length + 2];

                    newLifecycleListeners[0] = beginWebBeansListener;
                    System.arraycopy(lifecycleListeners, 0, newLifecycleListeners, 1, lifecycleListeners.length);
                    newLifecycleListeners[newLifecycleListeners.length - 1] = endWebBeansListener;
                    standardContext.setApplicationLifecycleListeners(newLifecycleListeners);
                }

                // also add the ThreadBindingListener to clean up async thread executions
                {
                    WebBeansThreadBindingListener webBeansThreadBindingListener = new WebBeansThreadBindingListener(webBeansContext, standardContext.getThreadBindingListener());
                    standardContext.setThreadBindingListener(webBeansThreadBindingListener);
                }

                final ContextsService contextsService = webBeansContext.getContextsService();
                if (CdiAppContextsService.class.isInstance(contextsService)) { // here ServletContext is usable
                    CdiAppContextsService.class.cast(contextsService).applicationStarted(standardContext.getServletContext());
                }
            } else {
                // just add the end listener to be able to stack tasks to execute at the request end
                final EndWebBeansListener endWebBeansListener = new EndWebBeansListener(webBeansContext);

                {
                    final Object[] appEventListeners = standardContext.getApplicationEventListeners();
                    final Object[] newEventListeners = new Object[appEventListeners.length + 1];

                    System.arraycopy(appEventListeners, 0, newEventListeners, 0, appEventListeners.length);
                    newEventListeners[newEventListeners.length - 1] = endWebBeansListener;
                    standardContext.setApplicationEventListeners(newEventListeners);
                }

                {
                    final Object[] lifecycleListeners = standardContext.getApplicationLifecycleListeners();
                    final Object[] newLifecycleListeners = new Object[lifecycleListeners.length + 1];

                    System.arraycopy(lifecycleListeners, 0, newLifecycleListeners, 0, lifecycleListeners.length);
                    newLifecycleListeners[newLifecycleListeners.length - 1] = endWebBeansListener;
                    standardContext.setApplicationLifecycleListeners(newLifecycleListeners);
                }
            }
        } finally {
            thread.setContextClassLoader(originalLoader);
        }

        LinkageErrorProtection.preload(standardContext);

        final Pipeline pipeline = standardContext.getPipeline();
        pipeline.addValve(new OpenEJBValve());

        final String[] valves = SystemInstance.get().getOptions().get("tomee.valves", "").split(" *, *");
        for (final String className : valves) {
            if ("".equals(className)) {
                continue;
            }
            try {
                final Class<?> clazz = classLoader.loadClass(className);
                if (Valve.class.isAssignableFrom(clazz)) {
                    final Valve valve = (Valve) clazz.newInstance();
                    pipeline.addValve(valve);
                }
            } catch (final Exception e) {
                LOGGER.error("can't add the valve " + className, e);
            }
        }

        // add servlets to webappinfo
        if (currentWebAppInfo != null) {
            for (final String mapping : standardContext.findServletMappings()) {
                final ServletInfo info = new ServletInfo();
                info.servletName = standardContext.findServletMapping(mapping);
                info.mappings.add(mapping);

                final Container container = standardContext.findChild(info.servletName);
                if (container instanceof StandardWrapper) {
                    info.servletClass = ((StandardWrapper) container).getServletClass();
                } else {
                    info.servletClass = mapping;
                }

                currentWebAppInfo.servlets.add(info);
            }
        }

        addConfiguredDocBases(standardContext, contextInfo);

        ensureMyFacesDontLooseFacesContext(standardContext);
    }

    private void ensureMyFacesDontLooseFacesContext(final StandardContext standardContext) {
        for (final Container w : standardContext.findChildren()) {
            if (!Wrapper.class.isInstance(w)) {
                continue;
            }
            final Wrapper wrapper = Wrapper.class.cast(w);
            if ("FacesServlet".equals(wrapper.getName()) && "javax.faces.webapp.FacesServlet".equals(wrapper.getServletClass())) {
                final ClassLoader loader = standardContext.getLoader().getClassLoader();
                try {
                    if (Files.toFile(loader.getResource("javax/faces/webapp/FacesServlet.class")).getName().startsWith("myfaces")) {
                        loader.loadClass("org.apache.tomee.myfaces.TomEEWorkaroundFacesServlet");
                        wrapper.setServletClass("org.apache.tomee.myfaces.TomEEWorkaroundFacesServlet");
                        break;
                    }
                } catch (final Throwable t) {
                    // not there, not a big deal in most of cases
                }
            }
        }
    }

    private static String appVersion(final AppInfo appInfo) {
        if (appInfo != null && appInfo.webAppAlone && appInfo.appId != null) {
            final int versionIndex = appInfo.appId.indexOf("##");
            return versionIndex >= 0 ? appInfo.appId.substring(versionIndex) : "";
        }
        return "";
    }

    private void addConfiguredDocBases(final StandardContext standardContext, final ContextInfo contextInfo) {
        if (contextInfo.appInfo.path != null) {   // add external web resources
            final String contextPath = standardContext.getServletContext().getContextPath();
            final String name = contextPath.isEmpty() ? "ROOT" : contextPath.substring(1);
            final String webResources = SystemInstance.get().getProperty("tomee." + name + ".docBases", contextInfo.appInfo.properties.getProperty("docBases"));
            if (webResources != null) {
                for (final String alt : webResources.trim().split(",")) {
                    final String trim = alt.trim();
                    if (trim.isEmpty()) {
                        continue;
                    }

                    if (!new File(trim).isDirectory()) {
                        LOGGER.warning("Can't add docBase which are not directory: " + trim);
                        continue;
                    }

                    final WebResourceRoot root = standardContext.getResources();
                    root.addPreResources(new DirResourceSet(root, "/", trim, "/"));
                }
            }
        }
    }

    private WebBeansContext getWebBeansContext(final ContextInfo contextInfo) {
        final AppContext appContext = getContainerSystem().getAppContext(contextInfo.appInfo.appId);

        if (appContext == null) {
            return null;
        }

        WebBeansContext webBeansContext = appContext.getWebBeansContext();

        if (webBeansContext == null) {
            return null;
        }

        for (final WebContext web : appContext.getWebContexts()) {
            final String stdName = removeFirstSlashAndWar(contextInfo.standardContext.getName());
            if (stdName == null) {
                continue;
            }

            final String name = removeFirstSlashAndWar(web.getContextRoot());
            if (stdName.equals(name)) {
                webBeansContext = web.getWebbeansContext();
                if (Contexts.getHostname(contextInfo.standardContext).equals(web.getHost())) {
                    break;
                } // else loop hoping to find a better matching
            }
        }

        if (webBeansContext == null) {
            webBeansContext = appContext.getWebBeansContext();
        }

        return webBeansContext;
    }

    private static String removeFirstSlashAndWar(final String name) {
        if (name == null || "/".equals(name) || name.isEmpty()) {
            return "";
        }

        String out = name;
        if (out.startsWith("/")) {
            out = out.substring(1);
        }
        if (out.endsWith(".war")) {
            return out.substring(0, Math.max(out.length() - 4, 0));
        }

        return out;
    }


    private static boolean isIgnored(final StandardContext standardContext) {
        // useful to disable web applications deployment
        // it can be placed in the context.xml file, server.xml, ...
        // see http://tomcat.apache.org/tomcat-5.5-doc/config/context.html#Context_Parameters
        return standardContext.getServletContext().getAttribute(IGNORE_CONTEXT) != null
                || standardContext.getServletContext().getInitParameter(IGNORE_CONTEXT) != null
                || standardContext instanceof IgnoredStandardContext
                || isExcludedBySystemProperty(standardContext);
    }

    private static boolean isExcludedBySystemProperty(final StandardContext standardContext) {
        String name = standardContext.getName();
        if (name == null) {
            name = standardContext.getPath();
            if (name == null) { // possible ?
                name = "";
            }
        }

        if (name.startsWith("/")) {
            name = name.substring(1);
        }

        final SystemInstance systemInstance = SystemInstance.get();
        return "true".equalsIgnoreCase(systemInstance.getProperty(name + ".tomcat-only", systemInstance.getProperty("tomcat-only", "false")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeStop(final StandardContext standardContext) {
        final ClassLoader classLoader = standardContext.getLoader().getClassLoader();

        // if it is not our custom loader clean up now otherwise wait afterStop
        if (!(standardContext.getLoader() instanceof LazyStopLoader)) {
            jsfClasses.remove(classLoader);
        }

        // ensure we can stop it lazily - before all was in the classloader, now we align webresourceroot on the classloader config
        // we wrap it only here to not modify at all runtime
        if (!LazyStopStandardRoot.class.isInstance(standardContext.getResources())
                && TomEEWebappClassLoader.class.isInstance(classLoader) && !TomEEWebappClassLoader.class.cast(classLoader).isForceStopPhase()) {
            final LazyStopStandardRoot standardRoot = new LazyStopStandardRoot(standardContext.getResources());
            Reflections.set(standardContext, "resources", standardRoot);
            TomEEWebappClassLoader.class.cast(classLoader).setWebResourceRoot(standardRoot); // cause Assembler relies on the classloader only
        }
    }

    private boolean isUnDeployable(final ContextInfo contextInfo) {
        return contextInfo.appInfo != null && contextInfo.deployer == null && contextInfo.appInfo.webAppAlone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(final StandardContext standardContext) {
        //No operation
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterStop(final StandardContext standardContext) {
        if (isIgnored(standardContext)) {
            return;
        }

        final TomEEWebappClassLoader old = lazyClassLoader(standardContext);
        if (old != null) { // should always be the case
            TldScanner.forceCompleteClean(old);
            jsfClasses.remove(old);
        }

        final ContextInfo contextInfo = getContextInfo(standardContext);
        boolean destroyFromTomcat = contextInfo != null && getAssembler().getDeployedApplications().contains(contextInfo.appInfo);
        if (destroyFromTomcat && isUnDeployable(contextInfo)) {
            contextInfo.appInfo.properties.setProperty("tomee.destroying", "true");
            try {
                getAssembler().destroyApplication(contextInfo.appInfo.path);
            } catch (final Exception e) {
                LOGGER.error("Unable to stop web application " + standardContext.getPath() + ": Exception: " + e.getMessage(), e);
            }
        } else {
            destroyFromTomcat = false;
        }

        NamingUtil.cleanUpContextResource(standardContext);

        if (old != null) {
            if (destroyFromTomcat) {
                try {
                    old.internalStop();
                } catch (final LifecycleException e) {
                    LOGGER.error("error stopping classloader of webapp " + standardContext.getName(), e);
                }
                ClassLoaderUtil.cleanOpenJPACache(old);
            }
            instanceManagers.remove(old);
        } else if (standardContext.getLoader() != null && standardContext.getLoader().getClassLoader() != null) {
            final ClassLoader classLoader = standardContext.getLoader().getClassLoader();
            instanceManagers.remove(classLoader);
        }
        realms.remove(standardContext.getName());

        if (contextInfo != null && (contextInfo.appInfo == null || contextInfo.appInfo.webAppAlone)) {
            removeContextInfo(standardContext);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy(final StandardContext standardContext) {
        final Loader standardContextLoader = standardContext.getLoader();
        if (LazyStopLoader.class.isInstance(standardContextLoader)) {
            final Loader delegate = LazyStopLoader.class.cast(standardContextLoader).getDelegateLoader();
            if (TomEEWebappLoader.class.isInstance(delegate)) {
                final TomEEWebappLoader webappLoader = TomEEWebappLoader.class.cast(delegate);
                final ClassLoader loader = webappLoader.internalLoader();
                webappLoader.clearLoader();
                if (TomEEWebappClassLoader.class.isInstance(loader)) {
                    TomEEWebappClassLoader.class.cast(loader).internalDestroy();
                }
            }
        }

        final WebResourceRoot root = standardContext.getResources();
        if (LazyStopStandardRoot.class.isInstance(root)) {
            try {
                LazyStopStandardRoot.class.cast(root).internalDestroy();
            } catch (final LifecycleException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void afterStop(final StandardServer standardServer) {
        // clean ear based webapps after shutdown
        for (final ContextInfo contextInfo : infos.values()) {
            if (contextInfo != null && contextInfo.deployer != null) {
                final StandardContext standardContext = contextInfo.standardContext;
                final HostConfig deployer = contextInfo.deployer;
                deployer.unmanageApp(standardContext.getPath());
                final File realPath = Contexts.warPath(standardContext);
                if (realPath != null) {
                    deleteDir(realPath);
                }
            }
        }

        TomcatLoader.destroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void checkHost(final StandardHost standardHost) {
        if (noHostCheck) {
            return;
        }
        if (standardHost.getAutoDeploy()) {
            // Undeploy any modified application
            for (final Iterator<Map.Entry<String, DeployedApplication>> iterator = deployedApps.entrySet().iterator(); iterator.hasNext(); ) {
                final Map.Entry<String, DeployedApplication> entry = iterator.next();
                final DeployedApplication deployedApplication = entry.getValue();
                if (deployedApplication.isModified()) { // TODO: for war use StandardContext.redeploy()
                    if (deployedApplication.appInfo != null) { // can happen with badly formed config
                        try {
                            getAssembler().destroyApplication(deployedApplication.appInfo.path);
                        } catch (final Exception e) {
                            LOGGER.error("Unable to application " + deployedApplication.appInfo.path, e);
                        }
                    } else {
                        LOGGER.error("appinfo is null for " + deployedApplication);
                    }
                    iterator.remove();
                }
            }

            // Deploy new applications
            final File appBase = appBase(standardHost);
            final File[] files = appBase.listFiles();
            if (null != files) {
                for (File file : files) {
                    if (file.getName().endsWith(".tmp")) { // tomcat is uploading, see org.apache.catalina.manager.ManagerServlet.deploy(java.io.PrintWriter, org.apache.catalina.util.ContextName, java.lang.String, boolean, javax.servlet.http.HttpServletRequest, org.apache.tomcat.util.res.StringManager)
                        continue;
                    }

                    final String name = file.getName();
                    // ignore war files
                    if (name.toLowerCase().endsWith(".war") || isRoot(name)
                            || name.equalsIgnoreCase("META-INF") || name.equalsIgnoreCase("WEB-INF")) {
                        continue;
                    }
                    // Simple fix for TOMEE-23
                    if (name.toLowerCase().equals(".ds_store")) {
                        continue;
                    }
                    // ignore unpacked web apps
                    if (file.isDirectory() && new File(file, "WEB-INF").exists()) {
                        continue;
                    }
                    // ignore unpacked apps where packed version is present (packed version is owner)
                    if (file.isDirectory() && (new File(file.getParent(), file.getName() + ".ear").exists()
                            || new File(file.getParent(), file.getName() + ".war").exists()
                            || new File(file.getParent(), file.getName() + ".rar").exists())) {
                        continue;
                    }
                    // ignore already deployed apps
                    if (isDeployed(file, standardHost)) {
                        continue;
                    }

                    final AppInfo appInfo;
                    try {
                        file = file.getCanonicalFile().getAbsoluteFile();
                        final AppModule appModule = deploymentLoader.load(file, null);

                        // Ignore any standalone web modules - this happens when the app is unpaked and doesn't have a WEB-INF dir
                        if (appModule.getDeploymentModule().size() == 1 && appModule.getWebModules().size() == 1) {
                            final WebModule webModule = appModule.getWebModules().iterator().next();
                            if (file.getAbsolutePath().equals(webModule.getJarLocation())) {
                                continue;
                            }
                        }

                        // tell web modules to deploy using this host
                        for (final WebModule webModule : appModule.getWebModules()) {
                            webModule.setHost(standardHost.getName());
                        }

                        appInfo = configurationFactory.configureApplication(appModule);
                        if (file.isFile() && file.getName().toLowerCase().endsWith(".ear")) {
                            // this is to prevent any WARs inside the EARs being unpacked in a directory where they'll then be deployed again
                            appInfo.properties.setProperty("tomcat.unpackWar", "false");
                        }

                        // if this is an unpacked dir, tomcat will pick it up as a webapp so undeploy it first
                        if (file.isDirectory()) {
                            final ContainerBase context = (ContainerBase) standardHost.findChild("/" + name);
                            if (context != null) {
                                try {
                                    standardHost.removeChild(context);
                                } catch (final Throwable t) {
                                    LOGGER.warning("Error undeploying wep application from Tomcat  " + name, t);
                                }
                                try {
                                    context.destroy();
                                } catch (final Throwable t) {
                                    LOGGER.warning("Error destroying Tomcat web context " + name, t);
                                }
                            }
                        }

                        getAssembler().createApplication(appInfo);

                        deployedApps.put(file.getAbsolutePath(), new DeployedApplication(file, appInfo));
                    } catch (final Throwable e) {
                        LOGGER.warning("Error deploying application " + file.getAbsolutePath(), e);
                    }
                }
            }
        }
    }

    /**
     * Returns true if given application is deployed
     * false otherwise.
     *
     * @param file         web application file
     * @param standardHost host
     * @return true if given application is deployed
     */
    private boolean isDeployed(final File file, final StandardHost standardHost) {
        if (deployedApps.containsKey(file.getAbsolutePath())) {
            return true;
        }

        // check if this is a deployed web application
        String name = "/" + file.getName();

        // ROOT context is a special case
        if (isRoot(name)) {
            name = "";
        }

        // can be a dir or a war so exists is fine
        return file.exists() && standardHost.findChild(name) != null;
    }

    /**
     * Returns application base of the given host.
     *
     * @param standardHost tomcat host
     * @return application base of the given host
     */
    protected File appBase(final StandardHost standardHost) {
        File file = new File(standardHost.getAppBase());
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("catalina.base"), standardHost.getAppBase());
        }
        try {
            file = file.getCanonicalFile();
        } catch (final IOException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return file;
    }

    /**
     * Creates an openejb {@link AppModule} instance
     * from given tomcat context.
     *
     * @param standardContext tomcat context instance
     * @return a openejb application module
     */
    private AppModule loadApplication(final StandardContext standardContext) {
        // don't use getId since the app id shouldnt get the host (jndi)
        // final TomcatDeploymentLoader tomcatDeploymentLoader = new TomcatDeploymentLoader(standardContext, getId(standardContext));

        String id = standardContext.getName();
        if (id.startsWith("/")) {
            id = id.substring(1);
        }

        final TomcatDeploymentLoader tomcatDeploymentLoader = new TomcatDeploymentLoader(standardContext, id);
        final AppModule appModule;
        try {
            appModule = tomcatDeploymentLoader.load(Contexts.warPath(standardContext), configuredClasspath(standardContext));
        } catch (final OpenEJBException e) {
            throw new TomEERuntimeException(e);
        }

        // create the web module
        loadWebModule(appModule, standardContext);

        return appModule;
    }

    private static DeploymentLoader.ExternalConfiguration configuredClasspath(final StandardContext standardContext) {
        Loader loader = standardContext.getLoader();
        if (loader != null && LazyStopLoader.class.isInstance(loader)) {
            loader = LazyStopLoader.class.cast(loader).getDelegateLoader();
        }
        if (loader != null) {
            final ClassLoader cl = standardContext.getLoader().getClassLoader();
            if (cl == null) {
                return null;
            }

            final Collection<String> cp = new LinkedList<>();

            final WebResourceRoot webResources = standardContext.getResources();
            if (webResources != null) { // to enhance
                for (final WebResourceSet[] sets : asList(webResources.getPreResources(), webResources.getPostResources(), webResources.getJarResources())) {
                    for (final WebResourceSet wr : sets) {
                        final URL base = wr.getBaseUrl();
                        if (base != null) {
                            final File baseFile = URLs.toFile(base);
                            if (baseFile.isDirectory()) {
                                final String[] libs = wr.list("/WEB-INF/lib/");
                                if (libs != null) {
                                    for (final String resource : libs) {
                                        cp.add(new File(baseFile, resource).getAbsolutePath());
                                    }
                                }

                                final WebResource classes = wr.getResource("/WEB-INF/classes/");
                                if (classes != null) {
                                    final String path = classes.getCanonicalPath();
                                    if (path != null) {
                                        cp.add(path);
                                    }
                                }
                            } else if (baseFile.exists() && baseFile.getName().endsWith(".jar") && wr.getResource("/WEB-INF/classes/").exists()) {
                                try {
                                    cp.add(baseFile.getCanonicalPath());
                                } catch (final IOException e) {
                                    throw new IllegalStateException(e);
                                }
                            }
                        }
                    }
                }
            }

            if (!cp.isEmpty()) {
                return new DeploymentLoader.ExternalConfiguration(cp.toArray(new String[cp.size()]), null /*for now doesnt make sense, todo: configure*/);
            }
        }
        return null;
    }

    /**
     * Creates a new {@link WebModule} instance from given
     * tomcat context instance.
     *
     * @param standardContext tomcat context instance
     */
    private void loadWebModule(final AppModule appModule, final StandardContext standardContext) {
        final List<WebModule> webModules = appModule.getWebModules();

        if (webModules.isEmpty()) {
            final File file = appModule.getFile();
            LOGGER.error("Failed to find a single module in: " + file);
            return;
        }

        final WebModule webModule = webModules.get(0);
        final WebApp webApp = webModule.getWebApp();

        // create the web module
        final String path = standardContext.getPath();
        LOGGER.debug("context path = " + path);
        webModule.setHost(Contexts.getHostname(standardContext));
        // Add all Tomcat env entries to context so they can be overriden by the env.properties file
        final NamingResourcesImpl naming = standardContext.getNamingResources();
        for (final ContextEnvironment environment : naming.findEnvironments()) {
            EnvEntry envEntry = webApp.getEnvEntryMap().get(environment.getName());
            if (envEntry == null) {
                envEntry = new EnvEntry();
                envEntry.setName(environment.getName());
                webApp.getEnvEntry().add(envEntry);
            }

            envEntry.setEnvEntryValue(environment.getValue());
            envEntry.setEnvEntryType(environment.getType());
        }

        // remove all jndi entries where there is a configured Tomcat resource or resource-link
        for (final ContextResource resource : naming.findResources()) {
            final String name = resource.getName();
            removeRef(webApp, name);
        }
        for (final ContextResourceLink resourceLink : naming.findResourceLinks()) {
            final String name = resourceLink.getName();
            removeRef(webApp, name);
        }

        // remove all env entries from the web xml that are not overridable
        for (final ContextEnvironment environment : naming.findEnvironments()) {
            if (!environment.getOverride()) {
                // overrides are not allowed
                webApp.getEnvEntryMap().remove(environment.getName());
            }
        }
    }

    /**
     * Remove jndi references from related info map.
     *
     * @param webApp web application instance
     * @param name   jndi reference name
     */
    private void removeRef(final WebApp webApp, final String name) {
        webApp.getEnvEntryMap().remove(name);
        webApp.getEjbRefMap().remove(name);
        webApp.getEjbLocalRefMap().remove(name);
        webApp.getMessageDestinationRefMap().remove(name);
        webApp.getPersistenceContextRefMap().remove(name);
        webApp.getPersistenceUnitRefMap().remove(name);
        webApp.getResourceRefMap().remove(name);
        webApp.getResourceEnvRefMap().remove(name);
    }

    /**
     * Binds given object into given component context.
     *
     * @param comp  context
     * @param name  name of the binding
     * @param value binded object
     */
    private void safeBind(final Context comp, final String name, final Object value) {
        try {
            comp.lookup(name);
            LOGGER.debug(name + " already bound, ignoring");
        } catch (final Exception e) {
            try {
                comp.bind(name, value);
            } catch (final NamingException ne) {
                LOGGER.error("Error in safeBind method", e);
            }
        }
    }

    /**
     * Gets openejb assembler instance.
     *
     * @return assembler
     */
    private Assembler getAssembler() {
        if (assembler == null) {
            assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
        }
        return assembler;
    }

    /**
     * Gets container system for openejb.
     *
     * @return openejb container system
     */
    private CoreContainerSystem getContainerSystem() {
        if (containerSystem == null) {
            containerSystem = (CoreContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
        }
        return containerSystem;
    }


    /**
     * Gets id of the context. Context id
     * is host name + context root name.
     *
     * @param standardContext context instance
     * @return id of the context
     */
    private String getId(final StandardContext standardContext) {
        return getId(Contexts.getHostname(standardContext), standardContext.getPath(), standardContext.getWebappVersion());
    }

    private String getId(final String host, final String context, final String version) {
        String contextRoot = context;
        if (isRoot(contextRoot)) {
            contextRoot = "";
        }
        if (!contextRoot.startsWith("/")) {
            contextRoot = "/" + contextRoot;
        }
        return (host == null ? hosts.getDefaultHost() : host) + contextRoot + (version == null || version.isEmpty() ? "" : (version.startsWith("##") ? version : "##" + version));
    }

    /**
     * Gets context info for given context.
     *
     * @param standardContext context
     * @return context info
     */
    public ContextInfo getContextInfo(final StandardContext standardContext) {
        final String id = getId(standardContext);
        final ContextInfo value;
        synchronized (infos) {
            value = infos.get(id);
        }
        return value;
    }

    /**
     * Gets context info for given web app info.
     *
     * @return context info
     */
    private synchronized ContextInfo getContextInfo(final String webAppHost, final String webAppContextRoot, final String version) {
        String host = webAppHost;
        if (host == null) {
            host = hosts.getDefaultHost();
        }

        final String id = getId(host, webAppContextRoot, version);

        final ContextInfo value;
        synchronized (infos) {
            value = infos.get(id);
        }
        return value;
    }

    /**
     * Add new context info.
     *
     * @param host            host name
     * @param standardContext context
     * @return context info
     */
    public ContextInfo addContextInfo(final String host, final StandardContext standardContext) {
        String contextRoot = standardContext.getName();
        if (!contextRoot.startsWith("/")) {
            contextRoot = "/" + contextRoot;
        }

        final String id = host + contextRoot;

        ContextInfo contextInfo;
        synchronized (infos) {
            contextInfo = infos.get(id);
            if (contextInfo == null) {
                contextInfo = new ContextInfo();
                final String webappVersion = standardContext.getWebappVersion();
                contextInfo.version = webappVersion != null && !webappVersion.isEmpty() ? "##" + webappVersion : webappVersion;
                contextInfo.standardContext = standardContext;
                infos.put(id, contextInfo);
            }
        }
        return contextInfo;
    }

    /**
     * Removes context info from map.
     *
     * @param standardContext context
     */
    private void removeContextInfo(final StandardContext standardContext) {
        boolean found = false;
        synchronized (infos) {
            final Iterator<Map.Entry<String, ContextInfo>> info = infos.entrySet().iterator();
            while (info.hasNext()) {
                if (info.next().getValue().standardContext == standardContext) {
                    info.remove();
                    found = true;
                    break;
                }
            }
        }
        if (!found) { // unlikely
            final String id = getId(standardContext);
            synchronized (infos) {
                infos.remove(id);
            }
        }
    }

    public static class ContextInfo {

        public AppInfo appInfo;
        public StandardContext standardContext;
        public HostConfig deployer;
        public Host host;
        public EjbModule module; // just during startup
        public String version;
        public Collection<String> resourceNames = Collections.emptyList();

        @Override
        public String toString() {
            return "ContextInfo{"
                    + "appInfo = " + appInfo + ", "
                    + "deployer = " + deployer + ", "
                    + "host = " + host
                    + "}";
        }
    }

    private static class DeployedApplication {

        private final AppInfo appInfo;
        private final Map<File, Long> watchedResource = new HashMap<>();

        public DeployedApplication(final File base, final AppInfo appInfo) {
            this.appInfo = appInfo;
            watchedResource.put(base, base.lastModified());
            if (appInfo != null) {
                for (final String resource : appInfo.watchedResources) {
                    final File file = new File(resource);
                    watchedResource.put(file, file.lastModified());
                }
                for (final EjbJarInfo info : appInfo.ejbJars) {
                    for (final String resource : info.watchedResources) {
                        final File file = new File(resource);
                        watchedResource.put(file, file.lastModified());
                    }
                }
                for (final WebAppInfo info : appInfo.webApps) {
                    for (final String resource : info.watchedResources) {
                        final File file = new File(resource);
                        watchedResource.put(file, file.lastModified());
                    }
                }
                for (final ConnectorInfo info : appInfo.connectors) {
                    for (final String resource : info.watchedResources) {
                        final File file = new File(resource);
                        watchedResource.put(file, file.lastModified());
                    }
                }
            }
        }

        public boolean isModified() {
            for (final Map.Entry<File, Long> entry : watchedResource.entrySet()) {
                final File file = entry.getKey();
                final long lastModified = entry.getValue();
                if ((!file.exists() && lastModified != 0L)
                        || (file.lastModified() != lastModified)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public Map<ClassLoader, Map<String, Set<String>>> getJsfClasses() {
        return jsfClasses;
    }

    @Override
    public ClassLoader getParentClassLoader(final ClassLoader fallback) {
        return (null != this.parentClassLoader ? this.parentClassLoader : fallback);
    }

    public Map<String, Realm> getRealms() {
        return realms;
    }

    public Map<ClassLoader, InstanceManager> getInstanceManagers() {
        return instanceManagers;
    }
}
