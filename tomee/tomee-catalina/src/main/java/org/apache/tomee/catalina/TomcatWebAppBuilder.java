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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Manager;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Service;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.NamingContextListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.ContextTransaction;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.loader.StandardClassLoader;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.HostConfig;
import org.apache.catalina.startup.RealmRuleSet;
import org.apache.naming.ContextAccessController;
import org.apache.naming.ContextBindings;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.*;
import org.apache.openejb.cdi.CdiBuilder;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.core.ivm.naming.SystemComponentReference;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LinkResolver;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomee.common.LegacyAnnotationProcessor;
import org.apache.tomee.common.TomcatVersion;
import org.apache.tomee.common.UserTransactionFactory;
import org.apache.tomee.loader.TomcatHelper;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.adaptor.ELAdaptor;
import org.omg.CORBA.ORB;

import javax.ejb.spi.HandleDelegate;
import javax.el.ELResolver;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContext;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
public class TomcatWebAppBuilder implements WebAppBuilder, ContextListener {
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
    public static final String OPENEJB_WEBAPP_MODULE_ID = "openejb.webapp.moduleId";
    public static final String TOMEE_EAT_EXCEPTION_PROP = "tomee.eat-exception";
    public static final String MYFACES_TOMEE_ANNOTATION_FINDER = "org.apache.tomee.myfaces.TomEEAnnotationProvider";

    /**
     * Context information for web applications
     */
    private final TreeMap<String, ContextInfo> infos = new TreeMap<String, ContextInfo>();
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

    /**
     * Creates a new web application builder
     * instance.
     */
    public TomcatWebAppBuilder() {

        // TODO: re-write this bit, so this becomes part of the listener, and we register this with the mbean server.

        final StandardServer standardServer = TomcatHelper.getServer();
        globalListenerSupport = new GlobalListenerSupport(standardServer, this);

        // could search mbeans

        //Getting host config listeners
        for (final Service service : standardServer.findServices()) {
            if (service.getContainer() instanceof Engine) {
                final Engine engine = (Engine) service.getContainer();
                for (final Container engineChild : engine.findChildren()) {
                    if (engineChild instanceof StandardHost) {
                        final StandardHost host = (StandardHost) engineChild;
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

        configurationFactory = new ConfigurationFactory();
        deploymentLoader = new DeploymentLoader();
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
            if (war.isDirectory()) {
                final File cXml = new File(war, Constants.ApplicationContextXml);
                if (cXml.exists()) {
                    contextXml = IO.read(cXml);
                    System.out.println("using context file " + cXml.getAbsolutePath());
                }
            } else { // war
                final JarFile warAsJar = new JarFile(war);
                final JarEntry entry = warAsJar.getJarEntry(Constants.ApplicationContextXml);
                if (entry != null) {
                    contextXml = warAsJar.getInputStream(entry);
                }
            }

            StandardContext standardContext;
            if (contextXml != null) {
                synchronized (CONTEXT_DIGESTER) {
                    try {
                        standardContext = (StandardContext) CONTEXT_DIGESTER.parse(contextXml);
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
                    standardContext.setDocBase(standardContext.getDocBase().substring(0, standardContext.getDocBase().length() - 4));
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
                standardContext.setParentClassLoader(classLoader);
                standardContext.setDelegate(true);
                standardContext.setLoader(new TomEEWebappLoader(appInfo.path, classLoader));
                standardContext.getLoader().setDelegate(true);

                String host = webApp.host;
                if (host == null) {
                    host = "localhost";
                    logger.warning("using default host: " + host);
                }

                // TODO: instead of storing deployers, we could just lookup the right hostconfig for the server.
                final HostConfig deployer = deployers.get(host);
                if (isReady(deployer)) { // if not ready using directly host to avoid a NPE
                    // host isn't set until we call deployer.manageApp, so pass it
                    // ?? host is set through an event and it can be null here :(
                    final ContextInfo contextInfo = addContextInfo(host, standardContext);
                    contextInfo.appInfo = appInfo;
                    contextInfo.deployer = deployer;
                    deployer.manageApp(standardContext);
                } else if (hosts.containsKey(host)) {
                    final Host theHost = hosts.get(host);

                    final ContextInfo contextInfo = addContextInfo(host, standardContext);
                    contextInfo.appInfo = appInfo;
                    contextInfo.host = theHost;

                    theHost.addChild(standardContext);
                }
            }
        }
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

            if (contextInfo != null && contextInfo.deployer != null) {
                final StandardContext standardContext = contextInfo.standardContext;
                final HostConfig deployer = contextInfo.deployer;

                if (deployer != null) {
                    deployer.unmanageApp(standardContext.getPath());
                } else if (contextInfo.host != null) {
                    contextInfo.host.removeChild(standardContext);
                }
                deleteDir(new File(standardContext.getServletContext().getRealPath("")));
                removeContextInfo(standardContext);
            }
        }
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
        standardContext.setCrossContext(SystemInstance.get().getOptions().get(OPENEJB_CROSSCONTEXT_PROPERTY, false));
        standardContext.setNamingResources(new OpenEJBNamingResource());

        String sessionManager = SystemInstance.get().getOptions().get(OPENEJB_SESSION_MANAGER_PROPERTY + "." + standardContext.getName(), (String) null);
        if (sessionManager == null) {
            sessionManager = SystemInstance.get().getOptions().get(OPENEJB_SESSION_MANAGER_PROPERTY, (String) null);
        }
        if (sessionManager != null) {
            if (sessionManagerClass == null) {
                try { // the manager should be in standardclassloader
                    sessionManagerClass = TomcatHelper.getServer().getParentClassLoader().loadClass(sessionManager);
                } catch (ClassNotFoundException e) {
                    logger.error("can't find '" + sessionManager + "', StandardManager will be used", e);
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
        ncl.setName(standardContext.getName());
        standardContext.setNamingContextListener(ncl);
        standardContext.addLifecycleListener(ncl);
        standardContext.addLifecycleListener(new TomcatJavaJndiBinder());
    }

    public class StandardContextInfo {

        private final StandardContext standardContext;

        public StandardContextInfo(final StandardContext standardContext) {
            this.standardContext = standardContext;
        }

        public WebAppInfo get() {
            final ContextInfo contextInfo = getContextInfo(standardContext);
            System.out.println("contextInfo = " + contextInfo);
            System.out.println("standardContext = " + standardContext);
            for (final WebAppInfo webApp : contextInfo.appInfo.webApps) {
                if (standardContext.getName().equals("/" + webApp.contextRoot)) {
                    return webApp;
                }
            }
            return null;
        }

        @Override
        public String toString() {
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
        System.out.println("TomcatWebAppBuilder.start " + standardContext.getPath());
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
        final ClassLoader classLoader = standardContext.getLoader().getClassLoader();
        if (contextInfo == null) {
            final AppModule appModule = loadApplication(standardContext);
            if (appModule != null) {
                try {
                    contextInfo = addContextInfo(standardContext.getHostname(), standardContext);
                    final AppInfo appInfo = configurationFactory.configureApplication(appModule);
                    contextInfo.appInfo = appInfo;

                    appContext = a.createApplication(contextInfo.appInfo, classLoader);
                    // todo add watched resources to context
                } catch (Exception e) {
                    undeploy(standardContext, contextInfo);
                    logger.error("Unable to deploy collapsed ear in war " + standardContext.getPath() + ": Exception: " + e.getMessage(), e);
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
        }

        if (appContext == null) {
            String contextRoot = standardContext.getName();
            if (contextRoot.startsWith("/")) {
                contextRoot = contextRoot.replaceAll("^/+", "");
            }
        }

        contextInfo.standardContext = standardContext;

        WebAppInfo webAppInfo = null;
        // appInfo is null when deployment fails
        if (contextInfo.appInfo != null) {
            for (final WebAppInfo w : contextInfo.appInfo.webApps) {
                if (("/" + w.contextRoot).equals(standardContext.getPath()) || isRootApplication(standardContext)) {
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

            // save jsf stuff
            final Map<String, Set<String>> scannedJsfClasses = new HashMap<String, Set<String>>();
            for (final ClassListInfo info : webAppInfo.jsfAnnotatedClasses) {
                scannedJsfClasses.put(info.name, info.list);
            }
            jsfClasses.put(standardContext.getLoader().getClassLoader(), scannedJsfClasses);

            try {

                // determine the injections
                final Set<Injection> injections = new HashSet<Injection>();
                injections.addAll(appContext.getInjections());

                if (!contextInfo.appInfo.webAppAlone) {
                    updateInjections(injections, classLoader);
                    for (BeanContext bean : appContext.getBeanContexts()) { // TODO: how if the same class in multiple webapps?
                        updateInjections(bean.getInjections(), classLoader);
                    }
                }
                injections.addAll(new InjectionBuilder(classLoader).buildInjections(webAppInfo.jndiEnc));

                // jndi bindings
                final Map<String, Object> bindings = new HashMap<String, Object>();
                bindings.putAll(appContext.getBindings());
                bindings.putAll(getJndiBuilder(classLoader, webAppInfo, injections).buildBindings(JndiEncBuilder.JndiScope.comp));

                // merge OpenEJB jndi into Tomcat jndi
                final TomcatJndiBuilder jndiBuilder = new TomcatJndiBuilder(standardContext, webAppInfo, injections);
                jndiBuilder.mergeJndi();

                // add WebDeploymentInfo to ContainerSystem
                final WebContext webContext = new WebContext(appContext);
                webContext.setClassLoader(classLoader);
                webContext.setId(webAppInfo.moduleId);
                webContext.setBindings(bindings);
                webContext.getInjections().addAll(injections);
                appContext.getWebContexts().add(webContext);
                cs.addWebContext(webContext);

                if (!contextInfo.appInfo.webAppAlone) {
                    new CdiBuilder().build(contextInfo.appInfo, appContext, appContext.getBeanContexts(), webContext);
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
    }

    private static void updateInjections(Collection<Injection> injections, ClassLoader classLoader) {
        final Iterator<Injection> it = injections.iterator();
        while (it.hasNext()) { // update not loaded injections for classloader issues or remove them
            final Injection injection = it.next();
            if (injection.getTarget() == null) {
                try {
                    final Class<?> target = classLoader.loadClass(injection.getClassname());
                    injection.setTarget(target);
                } catch (ClassNotFoundException cnfe) {
                    it.remove();
                }
            }
        }
    }

    private static void undeploy(final StandardContext standardContext, final ContextInfo contextInfo) {
        if (isReady(contextInfo.deployer)) {
            contextInfo.deployer.unmanageApp(standardContext.getName());
        } else if (contextInfo.host != null) {
            contextInfo.host.removeChild(standardContext);
        }
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

        // if appInfo is null this is a failed deployment... just ignore
        final ContextInfo contextInfo = getContextInfo(standardContext);
        if (contextInfo != null && contextInfo.appInfo == null) {
            return;
        } else if (contextInfo == null) { // openejb webapp loaded from the LoaderServlet
            return;
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
            for (final WebAppInfo webAppInfo : contextInfo.appInfo.webApps) {
                final boolean isRoot = isRootApplication(standardContext);
                if (("/" + webAppInfo.contextRoot).equals(standardContext.getPath()) || isRoot) {
                    final WebContext webContext = getContainerSystem().getWebContext(webAppInfo.moduleId);
                    if (webContext != null) {
                        webContext.setJndiEnc(comp);
                    }

                    try {
                        // Bean Validation
                        standardContext.getServletContext().setAttribute("javax.faces.validator.beanValidator.ValidatorFactory", openejbContext.lookup(Assembler.VALIDATOR_FACTORY_NAMING_CONTEXT.replaceFirst("openejb", "") + webAppInfo.uniqueId));
                    } catch (NamingException ne) {
                        logger.warning("no validator factory found for webapp " + webAppInfo.moduleId);
                    }

                    break;
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
        }
        ContextAccessController.setReadOnly(listenerName);

        // required for Pojo Web Services because when Assembler creates the application
        // the CoreContainerSystem does not contain the WebContext
        // see also the start method getContainerSystem().addWebDeployment(webContext);
        final WebDeploymentListeners listeners = SystemInstance.get().getComponent(WebDeploymentListeners.class);
        if (listeners != null) {
            for (final WebAppInfo webApp : contextInfo.appInfo.webApps) {
                listeners.afterApplicationCreated(contextInfo.appInfo, webApp);
            }
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
            }
        }


        final WebBeansListener webBeansListener = getWebBeansContext(contextInfo);

        if (webBeansListener != null) {
            standardContext.addApplicationEventListener(webBeansListener);
            standardContext.addApplicationLifecycleListener(webBeansListener);
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
                e.printStackTrace();
            }
        }
    }

    private WebBeansListener getWebBeansContext(final ContextInfo contextInfo) {
        final AppContext appContext = getContainerSystem().getAppContext(contextInfo.appInfo.appId);

        if (appContext == null) return null;

        final WebBeansContext webBeansContext = appContext.getWebBeansContext();

        if (webBeansContext == null) return null;

        return new WebBeansListener(webBeansContext);
    }


    private static boolean isIgnored(final StandardContext standardContext) {
        // useful to disable web applications deployment
        // it can be placed in the context.xml file, server.xml, ...
        // see http://tomcat.apache.org/tomcat-5.5-doc/config/context.html#Context_Parameters
        if (standardContext.getServletContext().getAttribute(IGNORE_CONTEXT) != null) return true;
        if (standardContext.getServletContext().getInitParameter(IGNORE_CONTEXT) != null) return true;

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeStop(final StandardContext standardContext) {
        //No operation
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
        if (isIgnored(standardContext)) return;

        final ContextInfo contextInfo = getContextInfo(standardContext);
        if (contextInfo != null && contextInfo.appInfo != null && contextInfo.deployer == null
                && getAssembler().getDeployedApplications().contains(contextInfo.appInfo)) {
            try {
                getAssembler().destroyApplication(contextInfo.appInfo.path);
            } catch (Exception e) {
                logger.error("Unable to stop web application " + standardContext.getPath() + ": Exception: " + e.getMessage(), e);
            }
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
    public void afterStop(final StandardServer standardServer) {
        // clean ear based webapps after shutdown
        for (final ContextInfo contextInfo : infos.values()) {
            if (contextInfo != null && contextInfo.deployer != null) {
                final StandardContext standardContext = contextInfo.standardContext;
                final HostConfig deployer = contextInfo.deployer;
                deployer.unmanageApp(standardContext.getPath());
                final String realPath = standardContext.getServletContext().getRealPath("");
                if (realPath != null) {
                    deleteDir(new File(realPath));
                }
            }
        }
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
                if (deployedApplication.isModified()) {
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
                    if (name.toLowerCase().endsWith(".war") || name.equals("ROOT") || name.equalsIgnoreCase("META-INF") || name.equalsIgnoreCase("WEB-INF")) {
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
        if (name.equals("/ROOT")) {
            name = "";
        }

        return file.isFile() && standardHost.findChild(name) != null;
    }

    /**
     * Returns true if given context is root web appliction
     * false otherwise.
     *
     * @param standardContext tomcat context
     * @return true if given context is root web appliction
     */
    private boolean isRootApplication(final StandardContext standardContext) {
        return "".equals(standardContext.getPath());
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
        final ServletContext servletContext = standardContext.getServletContext();

        final TomcatDeploymentLoader tomcatDeploymentLoader = new TomcatDeploymentLoader(standardContext, getId(standardContext));
        final AppModule appModule;
        try {
            appModule = tomcatDeploymentLoader.load(new File(servletContext.getRealPath(".")).getParentFile());
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
     * @return a openejb web module
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
        String contextRoot = standardContext.getName();
        if (!contextRoot.startsWith("/")) {
            contextRoot = "/" + contextRoot;
        }
        return standardContext.getHostname() + contextRoot;
    }

    /**
     * Gets context info for given context.
     *
     * @param standardContext context
     * @return context info
     */
    private ContextInfo getContextInfo(final StandardContext standardContext) {
        final String id = getId(standardContext);
        final ContextInfo contextInfo = infos.get(id);
        return contextInfo;
    }

    /**
     * Gets context info for given web app info.
     *
     * @return context info
     */
    private ContextInfo getContextInfo(final String webAppHost, final String webAppContextRoot) {
        String host = webAppHost;
        if (host == null) {
            host = "localhost";
        }
        final String contextRoot = webAppContextRoot;
        final String id = host + "/" + contextRoot;
        final ContextInfo contextInfo = infos.get(id);
        return contextInfo;
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
        ContextInfo contextInfo = infos.get(id);
        if (contextInfo == null) {
            contextInfo = new ContextInfo();
            contextInfo.standardContext = standardContext;
            infos.put(id, contextInfo);
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
        infos.remove(id);
    }

    private static class ContextInfo {

        public AppInfo appInfo;
        public StandardContext standardContext;
        public HostConfig deployer;
        public Host host;
        public LinkResolver<EntityManagerFactory> emfLinkResolver;
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

    public Map<ClassLoader, Map<String, Set<String>>> getJsfClasses() {
        return jsfClasses;
    }
}
