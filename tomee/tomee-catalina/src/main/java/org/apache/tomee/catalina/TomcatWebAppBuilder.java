/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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
package org.apache.tomee.catalina;

import org.apache.catalina.Cluster;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Manager;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.Service;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.NamingContextListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.ContextTransaction;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.ResourceBase;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.ha.tcp.SimpleTcpCluster;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.HostConfig;
import org.apache.catalina.startup.RealmRuleSet;
import org.apache.catalina.startup.SetAllPropertiesRule;
import org.apache.catalina.startup.SetNextNamingRule;
import org.apache.catalina.users.MemoryUserDatabase;
import org.apache.naming.ContextAccessController;
import org.apache.naming.ContextBindings;
import org.apache.naming.ResourceEnvRef;
import org.apache.naming.ResourceRef;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBException;
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
import org.apache.openejb.assembler.classic.PersistenceUnitInfo;
import org.apache.openejb.assembler.classic.ReloadableEntityManagerFactory;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.ServletInfo;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.cdi.CdiBuilder;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.config.event.BeforeDeploymentEvent;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.core.ivm.naming.SystemComponentReference;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomee.catalina.cluster.ClusterObserver;
import org.apache.tomee.catalina.cluster.TomEEClusterListener;
import org.apache.tomee.catalina.event.AfterApplicationCreated;
import org.apache.tomee.catalina.routing.RouterValve;
import org.apache.tomee.common.LegacyAnnotationProcessor;
import org.apache.tomee.common.NamingUtil;
import org.apache.tomee.common.TomcatVersion;
import org.apache.tomee.common.UserTransactionFactory;
import org.apache.tomee.loader.TomcatHelper;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.adaptor.ELAdaptor;
import org.omg.CORBA.ORB;

import javax.ejb.spi.HandleDelegate;
import javax.el.ELResolver;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.servlet.ServletContext;
import javax.servlet.SessionTrackingMode;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.apache.tomee.catalina.BackportUtil.getNamingContextListener;

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
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("tomcat"), "org.apache.openejb.util.resources");

    private static final Digester CONTEXT_DIGESTER = createDigester();

    public static final String DEFAULT_J2EE_SERVER = "Apache TomEE";
    public static final String OPENEJB_WEBAPP_MODULE_ID = "openejb.webapp.moduleId";
    public static final String TOMEE_EAT_EXCEPTION_PROP = "tomee.eat-exception";
    public static final String TOMEE_INIT_J2EE_INFO = "tomee.init-J2EE-info";

    private static final boolean FORCE_RELOADABLE = SystemInstance.get().getOptions().get("tomee.force-reloadable", false);

    /**
     * Context information for web applications
     */
    private final Map<String, ContextInfo> infos = new HashMap<String, ContextInfo>();
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
    private final Map<String, HostConfig> deployers = new TreeMap<String, HostConfig>();
    private final Map<String, Host> hosts = new TreeMap<String, Host>();
    /**
     * Deployed web applications
     */
    // todo merge this map witth the infos map above
    private final Map<String, DeployedApplication> deployedApps = new TreeMap<String, DeployedApplication>();
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

    private Map<ClassLoader, Map<String, Set<String>>> jsfClasses = new HashMap<ClassLoader, Map<String, Set<String>>>();

    private Class<?> sessionManagerClass = null;

    private String defaultHost = "localhost";

    private Set<CatalinaCluster> clusters = new HashSet<CatalinaCluster>();

    private ClassLoader parentClassLoader;
    private boolean initJEEInfo = true;

    /**
     * Creates a new web application builder
     * instance.
     */
    public TomcatWebAppBuilder() {
        SystemInstance.get().setComponent(WebAppBuilder.class, this);
        initJEEInfo = "true".equalsIgnoreCase(SystemInstance.get().getProperty(TOMEE_INIT_J2EE_INFO, "true"));

        // TODO: re-write this bit, so this becomes part of the listener, and we register this with the mbean server.

        final StandardServer standardServer = TomcatHelper.getServer();
        globalListenerSupport = new GlobalListenerSupport(standardServer, this);

        // could search mbeans

        //Getting host config listeners
        for (final Service service : standardServer.findServices()) {
            if (service.getContainer() instanceof Engine) {
                final Engine engine = (Engine) service.getContainer();

                // add the global router if relevant
                final URL globalRouterConf = RouterValve.serverRouterConfigurationURL();
                if (globalRouterConf != null) {
                    final RouterValve routerValve = new RouterValve();
                    routerValve.setConfigurationPath(globalRouterConf);
                    engine.getPipeline().addValve(routerValve);
                }

                parentClassLoader = engine.getParentClassLoader();

                manageCluster(engine.getCluster());
                defaultHost = engine.getDefaultHost();
                addTomEERealm(engine);

                for (final Container engineChild : engine.findChildren()) {
                    if (engineChild instanceof StandardHost) {
                        final StandardHost host = (StandardHost) engineChild;
                        manageCluster(host.getCluster());
                        addTomEERealm(host);
                        hosts.put(host.getName(), host);
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

        configurationFactory = new ConfigurationFactory();
        deploymentLoader = new DeploymentLoader();
    }

    private void manageCluster(final Cluster cluster) {
        if (cluster == null) {
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
            haCluster.addClusterListener(TomEEClusterListener.INSTANCE); // better to be a singleton
            clusters.add(haCluster);
        }
    }

    private void addTomEERealm(final Engine engine) {
        final Realm realm = engine.getRealm();
        if (realm != null && !(realm instanceof TomEERealm)
            && (engine.getParent() == null
                    || (engine.getParent() != null && !realm.equals(engine.getParent().getRealm())))) {
            engine.setRealm(tomeeRealm(realm));
        }
    }

    private void addTomEERealm(final Host host) {
        final Realm realm = host.getRealm();
        if (realm != null && !(realm instanceof TomEERealm)
                && (host.getParent() == null
                || (host.getParent() != null && !realm.equals(host.getParent().getRealm())))) {
            host.setRealm(tomeeRealm(realm));
        }
    }

    protected Realm tomeeRealm(final Realm realm) {
        final TomEERealm trealm = new TomEERealm();
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

    public void start(final StandardServer server) {
        if (SystemInstance.get().isDefaultProfile()) { // add user tomee is no user are specified
            try {
                final NamingResources resources = server.getGlobalNamingResources();
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
            } catch (Throwable t) {
                // no-op
            }
        }
    }

    private static synchronized Digester createDigester() {
        if (CONTEXT_DIGESTER != null) {
            return CONTEXT_DIGESTER;
        }

        final Digester digester = new Digester();
        digester.setValidating(false);
        digester.addObjectCreate("Context", "org.apache.catalina.core.StandardContext", "className");
        digester.addSetProperties("Context");
        digester.addObjectCreate("Context/Loader", "org.apache.catalina.loader.WebappLoader", "className");
        digester.addSetProperties("Context/Loader");
        digester.addSetNext("Context/Loader", "setLoader", "org.apache.catalina.Loader");
        digester.addObjectCreate("Context/Manager", "org.apache.catalina.session.StandardManager", "className");
        digester.addSetProperties("Context/Manager");
        digester.addSetNext("Context/Manager", "setManager", "org.apache.catalina.Manager");
        digester.addObjectCreate("Context/Manager/Store", null, "className");
        digester.addSetProperties("Context/Manager/Store");
        digester.addSetNext("Context/Manager/Store", "setStore", "org.apache.catalina.Store");
        digester.addRuleSet(new RealmRuleSet("Context/"));
        digester.addCallMethod("Context/WatchedResource", "addWatchedResource", 0);
        digester.addObjectCreate("Context/Resource", "org.apache.catalina.deploy.ContextResource");
        digester.addRule("Context/Resource", new SetAllPropertiesRule());
        digester.addRule("Context/Resource", new SetNextNamingRule("addResource", "org.apache.catalina.deploy.ContextResource"));

        return digester;
    }

    //
    // OpenEJB WebAppBuilder
    //

    /**
     * {@inheritDoc}
     */
    @Override
    public void deployWebApps(final AppInfo appInfo, final ClassLoader classLoader) throws Exception {
        for (final WebAppInfo webApp : appInfo.webApps) {
            // look for context.xml
            final File war = new File(webApp.path);
            InputStream contextXml = null;
            URL contextXmlUrl = null;
            if (war.isDirectory()) {
                final File cXml = new File(war, Constants.ApplicationContextXml).getAbsoluteFile();
                if (cXml.exists()) {
                    contextXml = IO.read(cXml);
                    contextXmlUrl = cXml.toURI().toURL();
                    logger.info("using context file " + cXml.getAbsolutePath());
                }
            } else { // war
                final JarFile warAsJar = new JarFile(war);
                final JarEntry entry = warAsJar.getJarEntry(Constants.ApplicationContextXml);
                if (entry != null) {
                    contextXmlUrl = new URL("jar:" + war.getAbsoluteFile().toURI().toURL().toExternalForm() + "!/" + Constants.ApplicationContextXml);
                    contextXml = warAsJar.getInputStream(entry);
                }
            }

            StandardContext standardContext;
            if (contextXml != null) {
                synchronized (CONTEXT_DIGESTER) {
                    try {
                        standardContext = (StandardContext) CONTEXT_DIGESTER.parse(contextXml);
                        standardContext.setConfigFile(contextXmlUrl);
                    } catch (Exception e) {
                        logger.error("can't parse context xml for webapp " + webApp.path, e);
                        standardContext = new StandardContext();
                    } finally {
                        CONTEXT_DIGESTER.reset();
                    }
                }
            } else {
                standardContext = new StandardContext();
            }

            if (getContextInfo(webApp.host, webApp.contextRoot) != null) {
                continue;
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
            if (standardContext.getHostname() != null && !"_".equals(standardContext.getHostname())) {
                webApp.host = standardContext.getHostname();
            }

            final ApplicationParameter appParam = new ApplicationParameter();
            appParam.setName(OPENEJB_WEBAPP_MODULE_ID);
            appParam.setValue(webApp.moduleId);
            standardContext.addApplicationParameter(appParam);

            if (getContextInfo(webApp.host, webApp.contextRoot) == null) {
                if (standardContext.getPath() == null) {
                    if (webApp.contextRoot != null && webApp.contextRoot.startsWith("/")) {
                        standardContext.setPath(webApp.contextRoot);
                    } else {
                        standardContext.setPath("/" + webApp.contextRoot);
                    }
                }
                if (standardContext.getDocBase() == null) {
                    standardContext.setDocBase(webApp.path);
                }
                if (standardContext.getDocBase() != null && standardContext.getDocBase().endsWith(".war")) {
                    DeploymentLoader.unpack(new File(standardContext.getDocBase()));
                    if (standardContext.getPath().endsWith(".war")) {
                        standardContext.setPath(removeFirstSlashAndWar("/" + standardContext.getPath()));
                        standardContext.setName(standardContext.getPath());
                        webApp.contextRoot = standardContext.getPath();
                    }
                    standardContext.setDocBase(standardContext.getDocBase().substring(0, standardContext.getDocBase().length() - 4));
                }
                if (isRoot(standardContext.getName())) {
                    standardContext.setName("");
                    webApp.contextRoot = "";
                }

                if (getContextInfo(webApp.host, webApp.contextRoot) != null) { // possible because of the previous renaming
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
                    host = defaultHost;
                    logger.info("using default host: " + host);
                }

                if (classLoader != null) {
                    appInfo.autoDeploy = false;
                    deployWar(standardContext, host, appInfo);
                } else { // force a normal deployment with lazy building of AppInfo
                    deployWar(standardContext, host, null);
                }
            }
        }
    }

    private static boolean isRoot(final String name) {
        return "/ROOT".equals(name) || "ROOT".equals(name);
    }

    public void deployWar(final StandardContext standardContext, final String host, final AppInfo info) {
        // TODO: instead of storing deployers, we could just lookup the right hostconfig for the server.
        final HostConfig deployer = deployers.get(host);
        if (isReady(deployer)) { // if not ready using directly host to avoid a NPE
            if (info != null) {
                final ContextInfo contextInfo = addContextInfo(host, standardContext);
                contextInfo.appInfo = info;
                contextInfo.deployer = deployer;
            }

            deployer.manageApp(standardContext);
        } else if (hosts.containsKey(host)) {
            final Host theHost = hosts.get(host);
            if (info != null) {
                final ContextInfo contextInfo = addContextInfo(host, standardContext);
                contextInfo.appInfo = info;
                contextInfo.host = theHost;
            }

            theHost.addChild(standardContext);
        }
    }

    public synchronized ContextInfo standaAloneWebAppInfo(final File file) {
        for (ContextInfo info : infos.values()) {
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
        for (ContextInfo info : infos.values()) {
            if (info.appInfo != null && info.appInfo.webAppAlone
                    && (info.appInfo.path.endsWith(path) || (info.appInfo.path + ".war").endsWith(path))) {
                return info;
            }
        }

        return null;
    }

    public synchronized Collection<String> availableApps() {
        final Collection<String> apps = new ArrayList<String>();
        for (ContextInfo info : infos.values()) {
            if (info.appInfo != null) {
                apps.add(info.appInfo.path);
            } else if (info.standardContext != null) {
                apps.add("[not deployed] " + info.standardContext.getName());
            }
        }
        return apps;
    }

    // TODO: find something more sexy
    private static Field HOST_CONFIG_HOST = null;

    static {
        try { // do it only once
            HOST_CONFIG_HOST = HostConfig.class.getDeclaredField("host");
        } catch (NoSuchFieldException e) {
            // no-op
        }
    }

    private static boolean isReady(final HostConfig deployer) {
        if (deployer != null && HOST_CONFIG_HOST != null) {
            try {
                return HOST_CONFIG_HOST.get(deployer) != null;
            } catch (Exception e) {
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
                } catch (ClassNotFoundException cnfe) {
                    return;
                }
            }

            setInitParameter(context, "org.apache.myfaces.LOG_WEB_CONTEXT_PARAMS", "false");
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
        for (final WebAppInfo webApp : appInfo.webApps) {
            final ContextInfo contextInfo = getContextInfo(webApp.host, webApp.contextRoot);

            if (contextInfo != null) {
                final StandardContext standardContext = contextInfo.standardContext;

                undeploy(standardContext, contextInfo);
                final File extracted = warPath(standardContext);
                if (isExtracted(extracted)) {
                    deleteDir(extracted);
                }
                removeContextInfo(standardContext);
            }
        }
    }

    private boolean isExtracted(final File extracted) {
        // do we want to delete it?
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
        if (isIgnored(standardContext)) return;

        // just adding a carriage return to get logs more readable
        logger.info("------------------------- " + finalName(standardContext.getPath()));

        if (FORCE_RELOADABLE) {
            standardContext.setReloadable(true);
        }

        final String name = standardContext.getName();

        initJ2EEInfo(standardContext);

        File warFile = warPath(standardContext);
        if (!warFile.isDirectory()) {
            try {
                warFile = DeploymentLoader.unpack(warFile);
            } catch (OpenEJBException e) {
                logger.error("can't unpack '" + warFile.getAbsolutePath() + "'");
            }
        }

        if (warFile.exists()) {
            SystemInstance.get().fireEvent(new BeforeDeploymentEvent(DeploymentLoader.getWebappUrls(warFile)));
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
                    sessionManagerClass = TomcatHelper.getServer().getParentClassLoader().loadClass(sessionManager);
                } catch (ClassNotFoundException e) {
                    logger.error("can't find '" + sessionManager + "', StandardManager will be used", e);
                    sessionManagerClass = StandardManager.class;
                }
            }

            try {
                final Manager mgr = (Manager) sessionManagerClass.newInstance();
                standardContext.setManager(mgr);
            } catch (Exception e) {
                logger.error("can't instantiate '" + sessionManager + "', StandardManager will be used", e);
            }
        }

        if (standardContext.getConfigFile() == null) {
            final String s = File.pathSeparator;
            final File contextXmlFile = new File(standardContext.getDocBase() + s + "META-INF" + s + "context.xml");
            if (contextXmlFile.exists()) {
                BackportUtil.getAPI().setConfigFile(standardContext, contextXmlFile);
                standardContext.setOverride(true);
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
        ncl.setName(name);
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
            return "";
        }
        return path;
    }

    private static File warPath(final StandardContext standardContext) {
        final File file = realWarPath(standardContext);
        if (file == null) {
            return file;
        }

        final String name = file.getName();
        if (!file.isDirectory() && name.endsWith(".war")) {
            final File extracted = new File(file.getParentFile(), name.substring(0, name.length() - ".war".length()));
            if (extracted.exists()) {
                return extracted;
            }
        }
        return file;
    }

    private static File realWarPath(final StandardContext standardContext) {
        String doc = standardContext.getDocBase();
        // handle ROOT case
        if (doc == null || doc.length() == 0) {
            doc = "ROOT";
        }

        File war = new File(doc);
        if (war.exists()) {
            return war;
        }
        final StandardHost host = (StandardHost) standardContext.getParent();
        final String base = host.getAppBase();
        war = new File(base, doc);
        if (war.exists()) {
            return war;
        }
        war = new File(new File(System.getProperty("catalina.home"), base), doc);
        if (war.exists()) {
            return war;
        }
        return new File(new File(System.getProperty("catalina.base"), base), doc); // shouldn't occur
    }

    public ContextInfo getContextInfo(final String appName) {
        ContextInfo info = null;
        for (Map.Entry<String, ContextInfo> current : infos.entrySet()) {
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
                logger.warning("StandardContext should not be null", throwable);
            }
        }

        public AppInfo app() {
            final ContextInfo contextInfo = getContextInfo(standardContext);
            if (contextInfo == null) {
                logger.debug("No ContextInfo for StandardContext " + standardContext.getName());
                return null;
            }
            return contextInfo.appInfo;
        }

        public WebAppInfo get() {
            if (standardContext == null) return null;

            final ContextInfo contextInfo = getContextInfo(standardContext);
            if (contextInfo == null) {
                logger.debug("No ContextInfo for StandardContext " + standardContext.getName());
                return null;
            }

            logger.debug("contextInfo = " + contextInfo);
            logger.debug("standardContext = " + standardContext);

            if (contextInfo.appInfo == null) {
                logger.debug("ContextInfo has no AppInfo for StandardContext " + standardContext.getName());
                return null;
            }

            final String id = getId(standardContext);
            for (final WebAppInfo webApp : contextInfo.appInfo.webApps) {
                if (webApp == null) {
                    logger.debug("ContextInfo.appInfo.webApps entry is null StandardContext " + standardContext.getName());
                    continue;
                }

                final String wId = getId(webApp.host, webApp.contextRoot);
                if (id.equals(wId)) {
                    return webApp;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            if (standardContext == null) return super.toString();

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
        final ServletContext sc = standardContext.getServletContext();
        if (sc != null && !SystemInstance.get().getOptions().get(OPENEJB_JSESSION_ID_SUPPORT, true)) {
            final Set<SessionTrackingMode> defaultTrackingModes = sc.getEffectiveSessionTrackingModes();
            if (defaultTrackingModes.contains(SessionTrackingMode.URL)) {
                final Set<SessionTrackingMode> newModes = new HashSet<SessionTrackingMode>();
                newModes.remove(SessionTrackingMode.URL);
                sc.setSessionTrackingModes(newModes);
            }
        }

        // we just want to wrap it to lazy stop it (afterstop)
        // to avoid classnotfound in @PreDestoy or destroyApplication()
        final WebappLoader loader = new LazyStopWebappLoader(standardContext);
        loader.setDelegate(standardContext.getDelegate());
        loader.setLoaderClass(LazyStopWebappClassLoader.class.getName());
        final Loader lazyStopLoader = new LazyStopLoader(loader);
        standardContext.setLoader(lazyStopLoader);
    }

    @Override
    public void configureStart(final StandardContext standardContext) {
        if (TomcatHelper.isTomcat7()) {
            TomcatHelper.configureJarScanner(standardContext);

            final ContextTransaction contextTransaction = new ContextTransaction();
            contextTransaction.setProperty(org.apache.naming.factory.Constants.FACTORY, UserTransactionFactory.class.getName());
            standardContext.getNamingResources().setTransaction(contextTransaction);
            startInternal(standardContext);
        }

        // clear a bit log for default case
        addMyFacesDefaultParameters(standardContext.getLoader().getClassLoader(), standardContext.getServletContext());
    }

    /**
     * {@inheritDoc}
     */
    // context class loader is now defined, but no classes should have been loaded
    @SuppressWarnings("unchecked")
    @Override
    public void start(final StandardContext standardContext) {
        if (!TomcatHelper.isTomcat7()) {
            startInternal(standardContext);
        }
    }

    /**
     * {@inheritDoc}
     */
//    @Override
    private void startInternal(final StandardContext standardContext) {
        if (isIgnored(standardContext)) return;

        final CoreContainerSystem cs = getContainerSystem();

        final Assembler a = getAssembler();
        if (a == null) {
            logger.warning("OpenEJB has not been initialized so war will not be scanned for nested modules " + standardContext.getPath());
            return;
        }

        AppContext appContext = null;
        //Look for context info, maybe context is already scanned
        ContextInfo contextInfo = getContextInfo(standardContext);
        ClassLoader classLoader = standardContext.getLoader().getClassLoader();

        if (contextInfo == null) {
            final AppModule appModule = loadApplication(standardContext);

            if (standardContext.getNamingResources() instanceof OpenEJBNamingResource) {
                // add them to the app as resource
                final OpenEJBNamingResource nr = (OpenEJBNamingResource) standardContext.getNamingResources();
                for (ResourceBase resource : nr.getTomcatResources()) {
                    final String name = resource.getName();

                    boolean found = false;
                    for (ResourceInfo r : SystemInstance.get().getComponent(OpenEjbConfiguration.class).facilities.resources) {
                        if (r.id.equals(name)) {
                            nr.removeResource(name);
                            found = true;
                            logger.warning(name + " resource was defined in both tomcat and tomee so removing tomcat one");
                            break;
                        }
                    }

                    if (!found) {
                        final Resource newResource = new Resource(name, resource.getType(), "org.apache.tomee:ProvidedByTomcat");
                        newResource.getProperties().setProperty("jndiName", newResource.getId());
                        newResource.getProperties().setProperty("appName", getId(standardContext));
                        newResource.getProperties().setProperty("factory", (String) resource.getProperty("factory"));

                        final Reference reference = createReference(resource);
                        if (reference != null) {
                            newResource.getProperties().put("reference", reference);
                        }
                        appModule.getResources().add(newResource);
                    }
                }
            }

            if (appModule != null) {
                try {
                    contextInfo = addContextInfo(standardContext.getHostname(), standardContext);
                    contextInfo.standardContext = standardContext; // ensure to do it before an exception can be thrown

                    contextInfo.appInfo = configurationFactory.configureApplication(appModule);
                    final Boolean autoDeploy = DeployerEjb.AUTO_DEPLOY.get();
                    contextInfo.appInfo.autoDeploy = autoDeploy == null || autoDeploy;
                    DeployerEjb.AUTO_DEPLOY.remove();

                    if (!appModule.isWebapp()) {
                    	classLoader = appModule.getClassLoader();
                    } else {
                        final ClassLoader loader = standardContext.getLoader().getClassLoader();
                        if (loader instanceof LazyStopWebappClassLoader) {
                            final LazyStopWebappClassLoader lazyStopWebappClassLoader = (LazyStopWebappClassLoader) loader;
                            for (URL url : appModule.getWebModules().iterator().next().getAddedUrls()) {
                                lazyStopWebappClassLoader.addURL(url);
                            }
                        }
                    }

                    appContext = a.createApplication(contextInfo.appInfo, classLoader);
                    // todo add watched resources to context
                } catch (Exception e) {
                    logger.error("Unable to deploy collapsed ear in war " + standardContext, e);
                    undeploy(standardContext, contextInfo);
                    // just to force tomee to start without EE part
                    if (System.getProperty(TOMEE_EAT_EXCEPTION_PROP) == null) {
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
        }

        final String id = getId(standardContext);
        WebAppInfo webAppInfo = null;
        // appInfo is null when deployment fails
        if (contextInfo.appInfo != null) {
            for (final WebAppInfo w : contextInfo.appInfo.webApps) {
                final String wId = getId(w.host, w.contextRoot);
                if (id.equals(wId)) {
                    webAppInfo = w;

                    if (appContext == null) {
                        appContext = cs.getAppContext(contextInfo.appInfo.appId);
                    }

                    break;
                }
            }
        }

        if (webAppInfo != null) {

            if (appContext == null) {
                appContext = getContainerSystem().getAppContext(contextInfo.appInfo.appId);
            }

            // ensure matching (see getId() usage)
            webAppInfo.host = standardContext.getHostname();
            webAppInfo.contextRoot = standardContext.getName();

            // save jsf stuff
            final Map<String, Set<String>> scannedJsfClasses = new HashMap<String, Set<String>>();
            for (final ClassListInfo info : webAppInfo.jsfAnnotatedClasses) {
                scannedJsfClasses.put(info.name, info.list);
            }
            jsfClasses.put(classLoader, scannedJsfClasses);

            try {

                // determine the injections
                final Set<Injection> injections = new HashSet<Injection>();
                injections.addAll(appContext.getInjections());

                if (!contextInfo.appInfo.webAppAlone) {
                    updateInjections(injections, classLoader, false);
                    for (final BeanContext bean : appContext.getBeanContexts()) { // TODO: how if the same class in multiple webapps?
                        updateInjections(bean.getInjections(), classLoader, true);
                    }
                }
                injections.addAll(new InjectionBuilder(classLoader).buildInjections(webAppInfo.jndiEnc));

                // jndi bindings
                final Map<String, Object> bindings = new HashMap<String, Object>();
                bindings.putAll(appContext.getBindings());
                bindings.putAll(getJndiBuilder(classLoader, webAppInfo, injections).buildBindings(JndiEncBuilder.JndiScope.comp));

                // merge OpenEJB jndi into Tomcat jndi
                final TomcatJndiBuilder jndiBuilder = new TomcatJndiBuilder(standardContext, webAppInfo, injections);
                NamingUtil.setCurrentContext(standardContext);
                try {
                    jndiBuilder.mergeJndi();
                } finally {
                    NamingUtil.setCurrentContext(null);
                }

                // create EMF included in this webapp when nested in an ear
                for (PersistenceUnitInfo unitInfo : contextInfo.appInfo.persistenceUnits) {
                    if (unitInfo.webappName != null && unitInfo.webappName.equals(webAppInfo.moduleId)) {
                        try {
                            final ReloadableEntityManagerFactory remf =
                                (ReloadableEntityManagerFactory) SystemInstance.get().getComponent(ContainerSystem.class)
                                        .getJNDIContext().lookup(Assembler.PERSISTENCE_UNIT_NAMING_CONTEXT + unitInfo.id);
                            remf.overrideClassLoader(classLoader);
                            remf.createDelegate();
                        } catch (NameNotFoundException nnfe) {
                            logger.warning("Can't find " + unitInfo.id + " persistence unit");
                        }
                    }
                }

                // add WebDeploymentInfo to ContainerSystem
                final WebContext webContext = new WebContext(appContext);
                webContext.setJndiEnc(new InitialContext());
                webContext.setClassLoader(classLoader);
                webContext.setId(webAppInfo.moduleId);
                webContext.setContextRoot(webAppInfo.contextRoot);
                webContext.setBindings(bindings);
                webContext.getInjections().addAll(injections);
                appContext.getWebContexts().add(webContext);
                cs.addWebContext(webContext);

                if (!contextInfo.appInfo.webAppAlone) {
                    final List<BeanContext> beanContexts = assembler.initEjbs(classLoader, contextInfo.appInfo, appContext, injections, new ArrayList<BeanContext>(), webAppInfo.moduleId);
                    new CdiBuilder().build(contextInfo.appInfo, appContext, beanContexts, webContext);
                    assembler.startEjbs(true, beanContexts);
                }

                standardContext.setInstanceManager(new JavaeeInstanceManager(webContext, standardContext));
                standardContext.getServletContext().setAttribute(InstanceManager.class.getName(), standardContext.getInstanceManager());

            } catch (Exception e) {
                logger.error("Error merging Java EE JNDI entries in to war " + standardContext.getPath() + ": Exception: " + e.getMessage(), e);
            }

            final JspFactory factory = JspFactory.getDefaultFactory();
            if (factory != null) {
                final JspApplicationContext applicationCtx = factory.getJspApplicationContext(standardContext.getServletContext());
                final WebBeansContext context = appContext.getWebBeansContext();
                if (context != null && context.getBeanManagerImpl().isInUse()) {
                    // Registering ELResolver with JSP container
                    final ELAdaptor elAdaptor = context.getService(ELAdaptor.class);
                    final ELResolver resolver = elAdaptor.getOwbELResolver();
                    applicationCtx.addELResolver(resolver);
                }
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
            String paramName = params.next();
            String paramValue = (String) resource.getProperty(paramName);
            StringRefAddr refAddr = new StringRefAddr(paramName, paramValue);
            ref.add(refAddr);
        }

        return ref;
    }

    private static void updateInjections(final Collection<Injection> injections, final ClassLoader classLoader, final boolean keepInjection) {
        final Iterator<Injection> it = injections.iterator();
        final List<Injection> newOnes = new ArrayList<Injection>();
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
                } catch (ClassNotFoundException cnfe) {
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
        if (child instanceof org.apache.catalina.Context && TomcatContextUtil.isReloading((org.apache.catalina.Context) child)) {
            return true;
        }

        // skip undeployment if restarting
        final LazyStopWebappClassLoader lazyStopWebappClassLoader = lazyClassLoader(child);
        if (lazyStopWebappClassLoader != null && lazyStopWebappClassLoader.isRestarting()) {
            return true;
        }

        if (child != null) {
            host.removeChild(standardContext);
            return true;
        }
        return false;
    }

    private static LazyStopWebappClassLoader lazyClassLoader(final Container child) {
        if (child == null) {
            return null;
        }

        final Loader loader = child.getLoader();
        if (loader == null || !(loader instanceof LazyStopLoader)) {
            return null;
        }

        final ClassLoader old = ((LazyStopLoader) loader).getStopClassLoader();
        if (old == null || !(old instanceof LazyStopWebappClassLoader)) {
            return null;
        }

        return (LazyStopWebappClassLoader) old;
    }

    private JndiEncBuilder getJndiBuilder(final ClassLoader classLoader, final WebAppInfo webAppInfo, final Set<Injection> injections) throws OpenEJBException {
        return new JndiEncBuilder(webAppInfo.jndiEnc, injections, webAppInfo.moduleId, "Bean", null, webAppInfo.uniqueId, classLoader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterStart(final StandardContext standardContext) {
        if (isIgnored(standardContext)) return;

        final Realm realm = standardContext.getRealm();
        if (realm != null && !(realm instanceof TomEERealm)
                && (standardContext.getParent() == null
                || (standardContext.getParent() != null && !realm.equals(standardContext.getParent().getRealm())))) {
            final ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(standardContext.getLoader().getClassLoader());
            try {
                standardContext.setRealm(tomeeRealm(realm));
            } finally {
                Thread.currentThread().setContextClassLoader(originalLoader);
            }
        }

        // if appInfo is null this is a failed deployment... just ignore
        final ContextInfo contextInfo = getContextInfo(standardContext);
        if (contextInfo != null && contextInfo.appInfo == null) {
            return;
        } else if (contextInfo == null) { // openejb webapp loaded from the LoaderServlet
            return;
        }

        final String id = getId(standardContext);
        WebAppInfo currentWebAppInfo = null;
        for (final WebAppInfo webAppInfo : contextInfo.appInfo.webApps) {
            final String wId = getId(webAppInfo.host, webAppInfo.contextRoot);
            if (id.equals(wId)) {
                currentWebAppInfo = webAppInfo;
                break;
            }
        }

        // bind extra stuff at the java:comp level which can only be
        // bound after the context is created
        final NamingContextListener ncl = getNamingContextListener(standardContext);
        final String listenerName = ncl.getName();
        ContextAccessController.setWritable(listenerName, standardContext);
        try {

            Context openejbContext = getContainerSystem().getJNDIContext();
            openejbContext = (Context) openejbContext.lookup("openejb");

            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            // normal case = startup case use standardclassloader
            // so simply reproduce it even is a method is called from another context.
            // that said this code (try) shouldn't be useful anymore for tomcat 7
            if (tccl instanceof WebappClassLoader) {
                Thread.currentThread().setContextClassLoader(tccl.getParent());
            }

            final Context root;
            final Context comp;
            try {
                root = (Context) ContextBindings.getClassLoader().lookup("");
                comp = (Context) ContextBindings.getClassLoader().lookup("comp"); // usually fails
            } finally {
                Thread.currentThread().setContextClassLoader(tccl);
            }

            // Context root = ncl.getNamingContext();
            // Context comp = (Context) root.lookup("comp");
            safeBind(root, "openejb", openejbContext);

            // add context to WebDeploymentInfo
            if (currentWebAppInfo != null) {
                final WebContext webContext = getContainerSystem().getWebContext(currentWebAppInfo.moduleId);
                if (webContext != null) {
                    webContext.setJndiEnc(comp);
                }

                try {
                    // Bean Validation
                    standardContext.getServletContext().setAttribute("javax.faces.validator.beanValidator.ValidatorFactory", openejbContext.lookup(Assembler.VALIDATOR_FACTORY_NAMING_CONTEXT.replaceFirst("openejb", "") + currentWebAppInfo.uniqueId));
                } catch (NamingException ne) {
                    logger.warning("no validator factory found for webapp " + currentWebAppInfo.moduleId);
                }
            }

            // bind TransactionManager
            final TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
            safeBind(comp, "TransactionManager", transactionManager);

            // bind TransactionSynchronizationRegistry
            final TransactionSynchronizationRegistry synchronizationRegistry = SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class);
            safeBind(comp, "TransactionSynchronizationRegistry", synchronizationRegistry);

            safeBind(comp, "ORB", new SystemComponentReference(ORB.class));
            safeBind(comp, "HandleDelegate", new SystemComponentReference(HandleDelegate.class));
        } catch (NamingException e) {
            // no-op
        }
        ContextAccessController.setReadOnly(listenerName);

        // required for Pojo Web Services because when Assembler creates the application
        // the CoreContainerSystem does not contain the WebContext
        // see also the start method getContainerSystem().addWebDeployment(webContext);
        for (final WebAppInfo webApp : contextInfo.appInfo.webApps) {
            SystemInstance.get().fireEvent(new AfterApplicationCreated(contextInfo.appInfo, webApp));
        }

        if (!TomcatVersion.hasAnnotationProcessingSupport()) {
            try {
                final Context compEnv = (Context) ContextBindings.getClassLoader().lookup("comp/env");

                final LegacyAnnotationProcessor annotationProcessor = new LegacyAnnotationProcessor(compEnv);

                standardContext.addContainerListener(new ProcessAnnotatedListenersListener(annotationProcessor));

                for (final Container container : standardContext.findChildren()) {
                    if (container instanceof Wrapper) {
                        final Wrapper wrapper = (Wrapper) container;
                        wrapper.addInstanceListener(new ProcessAnnotatedServletsListener(annotationProcessor));
                    }
                }
            } catch (NamingException e) {
                // no-op
            }
        }


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
        }

        LinkageErrorProtection.preload(standardContext);

        final Pipeline pipeline = standardContext.getPipeline();
        pipeline.addValve(new OpenEJBValve());

        final String[] valves = SystemInstance.get().getOptions().get("tomee.valves", "").split(" *, *");
        for (final String className : valves) {
            if ("".equals(className)) continue;
            try {
                final Class<?> clazz = standardContext.getLoader().getClassLoader().loadClass(className);
                if (Valve.class.isAssignableFrom(clazz)) {
                    final Valve valve = (Valve) clazz.newInstance();
                    pipeline.addValve(valve);
                }
            } catch (Exception e) {
                logger.error("can't add the valve " + className, e);
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
    }

    private WebBeansContext getWebBeansContext(final ContextInfo contextInfo) {
        final AppContext appContext = getContainerSystem().getAppContext(contextInfo.appInfo.appId);

        if (appContext == null) return null;

        WebBeansContext webBeansContext = appContext.getWebBeansContext();

        if (webBeansContext == null) return null;

        for (final WebContext web : appContext.getWebContexts()) {
            final String stdName = removeFirstSlashAndWar(contextInfo.standardContext.getName());
            if (stdName == null) {
                continue;
            }

            final String name = removeFirstSlashAndWar(web.getContextRoot());
            if (stdName.equals(name)) {
                webBeansContext = web.getWebbeansContext();
                break;
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
        return "true".equalsIgnoreCase(SystemInstance.get().getProperty(name + ".tomcat-only", "false"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeStop(final StandardContext standardContext) {
        // if it is not our custom loader clean up now otherwise wait afterStop
        if (!(standardContext.getLoader() instanceof LazyStopLoader)) {
            jsfClasses.remove(standardContext.getLoader().getClassLoader());
        }
    }

    private boolean isUnDeployable(final ContextInfo contextInfo) {
        return contextInfo != null && contextInfo.appInfo != null && contextInfo.deployer == null
                && getAssembler().getDeployedApplications().contains(contextInfo.appInfo);
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

        final LazyStopWebappClassLoader old = lazyClassLoader(standardContext);
        if (old != null) { // should always be the case
            jsfClasses.remove(old);
        }

        final ContextInfo contextInfo = getContextInfo(standardContext);
        if (isUnDeployable(contextInfo)) {
            try {
                getAssembler().destroyApplication(contextInfo.appInfo.path);
            } catch (Exception e) {
                logger.error("Unable to stop web application " + standardContext.getPath() + ": Exception: " + e.getMessage(), e);
            }
        }

        NamingUtil.cleanUpContextResource(standardContext);

        if (old != null) {
            try {
                old.internalStop();
            } catch (LifecycleException e) {
                logger.error("error stopping classloader of webapp " + standardContext.getName(), e);
            }
            ClassLoaderUtil.cleanOpenJPACache(old);
        }
        removeContextInfo(standardContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy(final StandardContext standardContext) {
        //No operation
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
                final File realPath = warPath(standardContext);
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
        if (standardHost.getAutoDeploy()) {
            // Undeploy any modified application
            for (Iterator<Map.Entry<String, DeployedApplication>> iterator = deployedApps.entrySet().iterator(); iterator.hasNext(); ) {
                final Map.Entry<String, DeployedApplication> entry = iterator.next();
                final DeployedApplication deployedApplication = entry.getValue();
                if (deployedApplication.isModified()) { // TODO: for war use StandardContext.redeploy()
                    if (deployedApplication.appInfo != null) { // can happen with badly formed config
                        try {
                            getAssembler().destroyApplication(deployedApplication.appInfo.path);
                        } catch (Exception e) {
                            logger.error("Unable to application " + deployedApplication.appInfo.path, e);
                        }
                    } else {
                        logger.error("appinfo is null for " + deployedApplication);
                    }
                    iterator.remove();
                }
            }

            // Deploy new applications
            final File appBase = appBase(standardHost);
            final File[] files = appBase.listFiles();
            if (null != files) {
                for (File file : files) {
                    final String name = file.getName();
                    // ignore war files
                    if (name.toLowerCase().endsWith(".war") || isRoot(name) || name.equalsIgnoreCase("META-INF") || name.equalsIgnoreCase("WEB-INF")) {
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
                        final AppModule appModule = deploymentLoader.load(file);

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

                        // if this is an unpacked dir, tomcat will pick it up as a webapp so undeploy it first
                        if (file.isDirectory()) {
                            final ContainerBase context = (ContainerBase) standardHost.findChild("/" + name);
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

                        getAssembler().createApplication(appInfo);

                        deployedApps.put(file.getAbsolutePath(), new DeployedApplication(file, appInfo));
                    } catch (Throwable e) {
                        logger.warning("Error deploying application " + file.getAbsolutePath(), e);
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
        } catch (IOException e) {
            logger.debug(e.getMessage(),e);
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
            appModule = tomcatDeploymentLoader.load(warPath(standardContext));
        } catch (OpenEJBException e) {
            throw new TomEERuntimeException(e);
        }

        // create the web module
        loadWebModule(appModule, standardContext);

        return appModule;
    }

    /**
     * Creates a new {@link WebModule} instance from given
     * tomcat context instance.
     *
     * @param standardContext tomcat context instance
     */
    private void loadWebModule(final AppModule appModule, final StandardContext standardContext) {
        final WebModule webModule = appModule.getWebModules().get(0);
        final WebApp webApp = webModule.getWebApp();

        // create the web module
        final String path = standardContext.getPath();
        logger.debug("context path = " + path);
        webModule.setHost(standardContext.getHostname());
        // Add all Tomcat env entries to context so they can be overriden by the env.properties file
        final NamingResources naming = standardContext.getNamingResources();
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
            logger.info(name + " already bound, ignoring");
        } catch (Exception e) {
            try {
                comp.bind(name, value);
            } catch (NamingException ne) {
                logger.error("Error in safeBind method", e);
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
            containerSystem = (CoreContainerSystem) SystemInstance.get().getComponent(org.apache.openejb.spi.ContainerSystem.class);
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
        return getId(standardContext.getHostname(), standardContext.getName());
    }

    private String getId(final String host, final String context) {
        String contextRoot = context;
        if (isRoot(contextRoot)) {
            contextRoot = "";
        }
        if (!contextRoot.startsWith("/")) {
            contextRoot = "/" + contextRoot;
        }
        if (host != null) {
            return host + contextRoot;
        }
        return defaultHost + contextRoot;
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
    private synchronized ContextInfo getContextInfo(final String webAppHost, final String webAppContextRoot) {
        String host = webAppHost;
        if (host == null) {
            host = defaultHost;
        }

        final String id = getId(host, webAppContextRoot);

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
    private ContextInfo addContextInfo(final String host, final StandardContext standardContext) {
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
        final String id = getId(standardContext);
        synchronized (infos) {
            infos.remove(id);
        }
    }

    public static class ContextInfo {

        public AppInfo appInfo;
        public StandardContext standardContext;
        public HostConfig deployer;
        public Host host;
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

        private AppInfo appInfo;
        private final Map<File, Long> watchedResource = new HashMap<File, Long>();

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
}
