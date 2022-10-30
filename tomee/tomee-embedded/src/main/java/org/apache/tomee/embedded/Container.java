/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.embedded;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.Catalina;
import org.apache.catalina.startup.CatalinaProperties;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.coyote.http2.Http2Protocol;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.Injector;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.WebAppDeployer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.BeansInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.ManagedBeanInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.AnnotationDeployer;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.DeploymentsResolver;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.FinderFactory;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.config.WebappAggregatedArchive;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.loader.provisining.ProvisioningResolver;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.ContainerClassesFilter;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomee.catalina.TomEERuntimeException;
import org.apache.tomee.catalina.TomcatLoader;
import org.apache.tomee.catalina.remote.TomEERemoteWebapp;
import org.apache.tomee.catalina.session.QuickSessionManager;
import org.apache.tomee.embedded.event.TomEEEmbeddedScannerCreated;
import org.apache.tomee.embedded.internal.StandardContextCustomizer;
import org.apache.tomee.util.QuickServerXmlParser;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.ResourceFinder;
import org.apache.xbean.finder.UrlSet;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.filter.Filter;
import org.apache.xbean.finder.filter.Filters;
import org.apache.xbean.recipe.ObjectRecipe;
import org.codehaus.swizzle.stream.ReplaceStringsInputStream;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * @version $Rev$ $Date$
 */
public class Container implements AutoCloseable {
    static {
        // org.apache.naming
        Assembler.installNaming("org.apache.naming", true);
        if (!Boolean.getBoolean("tomee.embedded.javaagent.auto.skip")) {
            try { // needs tools.jar to be in the cp to work but if so avoids the need of the jaavagent on the JVM
                org.apache.openejb.javaagent.Agent.getInstrumentation();
                org.apache.openejb.persistence.PersistenceBootstrap.bootstrap(Container.class.getClassLoader());
            } catch (final Throwable th) {
                // not important
            }
        }
    }

    private final Map<String, String> moduleIds = new HashMap<>(); // TODO: manage multimap
    private final Map<String, AppContext> appContexts = new HashMap<>(); // TODO: manage multimap
    private final Map<String, AppInfo> infos = new HashMap<>(); // TODO: manage multimap
    protected Configuration configuration;
    private File base;
    private ConfigurationFactory configurationFactory;
    private Assembler assembler;
    private InternalTomcat tomcat;

    // start the container directly
    public Container(final Configuration configuration) {
        setup(configuration);
        try {
            start();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Container() {
        this.configuration = new Configuration();
        this.configuration.setHttpPort(23880);
        this.configuration.setStopPort(23881);
    }

    public Container deployClasspathAsWebApp() {
        return deployClasspathAsWebApp("", null);
    }

    public Container deployClasspathAsWebApp(final String context, final File docBase, final String... dependencies) {
        return deployClasspathAsWebApp(context, docBase, Collections.<String>emptyList(), dependencies);
    }

    // used by maven and gradle (reflection)
    public Container deployClasspathAsWebApp(final String context, final File docBase, final boolean singleClassLoader) {
        return deployClasspathAsWebApp(context, docBase, Collections.<String>emptyList(), singleClassLoader);
    }

    public Container deployClasspathAsWebApp(final String context, final File docBase, final List<String> callers, final String... dependencies) {
        return deployClasspathAsWebApp(context, docBase, callers, false, dependencies);
    }

    public Container deployClasspathAsWebApp(final String context, final File docBase, final List<String> callers,
                                             final boolean singleLoader, final String... dependencies) {
        final List<URL> jarList = new DeploymentsResolver.ClasspathSearcher().loadUrls(Thread.currentThread().getContextClassLoader()).getUrls();
        if (dependencies != null) {
            for (final String dep : dependencies) {
                final Set<String> strings = SystemInstance.get().getComponent(ProvisioningResolver.class).realLocation(dep);
                for (final String path : strings) {
                    try {
                        jarList.add(new File(path).toURI().toURL());
                    } catch (final MalformedURLException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
        }

        try {
            return deployPathsAsWebapp(
                    context,
                    // ADDITIONAL_INCLUDE is not very well respected with NewLoaderLogic (but that's a different purpose) algo so force it a bit here
                    NewLoaderLogic.applyBuiltinExcludes(
                            new UrlSet(jarList), NewLoaderLogic.ADDITIONAL_INCLUDE == null ?
                                    null : Filters.prefixes(NewLoaderLogic.ADDITIONAL_INCLUDE.split("[ \t\n\n]*,[ \t\n\n]*"))).getUrls(),
                    docBase, singleLoader,
                    callers == null || callers.isEmpty() ? null : callers.toArray(new String[callers.size()]));
        } catch (final MalformedURLException e) {
            return deployPathsAsWebapp(context, jarList, docBase);
        }
    }

    public Container deployPathsAsWebapp(final File... jarList) {
        try {
            if (jarList == null || jarList.length < 1) {
                throw new IllegalArgumentException("The file does not have content");
            }

            final List<URL> urls = new ArrayList<>(jarList.length);
            for (final File jar : jarList) {
                urls.addAll(singletonList(jar.toURI().toURL()));
            }
            return deployPathsAsWebapp(null, urls, null);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Container deployPathsAsWebapp(final String context, final List<URL> jarList, final File docBase, final String... additionalCallers) {
        return deployPathsAsWebapp(context, jarList, docBase, false, additionalCallers);
    }

    public Container deployPathsAsWebapp(final String context, final List<URL> jarList, final File docBase,
                                         final boolean keepClassloader, final String... additionalCallers) {
        return deploy(new DeploymentRequest(context, jarList, docBase, keepClassloader, additionalCallers, null));
    }

    public Container deploy(final DeploymentRequest request) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final SystemInstance systemInstance = SystemInstance.get();

        String contextRoot = request.context == null ? "" : request.context;
        if (!contextRoot.isEmpty() && !contextRoot.startsWith("/")) {
            contextRoot = "/" + request.context;
        }

        File jarLocation = request.docBase == null || !request.docBase.isDirectory() ? fakeRootDir() : request.docBase;
        final WebModule webModule = new WebModule(new WebApp(), contextRoot, loader, jarLocation.getAbsolutePath(), contextRoot.replace("/", ""));
        if (request.docBase == null) {
            webModule.getProperties().put("fakeJarLocation", "true");
        }
        webModule.setUrls(request.jarList);
        webModule.setAddedUrls(Collections.<URL>emptyList());
        webModule.setRarUrls(Collections.<URL>emptyList());
        webModule.setScannableUrls(request.jarList);
        final AnnotationFinder finder;
        try {
            Filter filter = configuration.getClassesFilter();
            if (filter == null &&
                    (request.jarList.size() <= 4 || "true".equalsIgnoreCase(SystemInstance.get().getProperty("tomee.embedded.filter-container-classes")))) {
                filter = new ContainerClassesFilter(configuration.getProperties());
            }

            final Archive archive;
            if (request.archive == null) {
                archive = new WebappAggregatedArchive(webModule, request.jarList,
                        // see org.apache.openejb.config.DeploymentsResolver.ClasspathSearcher.cleanUpUrlSet()
                        filter);
            } else if (WebappAggregatedArchive.class.isInstance(request.archive)) {
                archive = request.archive;
            } else {
                archive = new WebappAggregatedArchive(request.archive, request.jarList);
            }

            finder = new FinderFactory.OpenEJBAnnotationFinder(archive).link();
            SystemInstance.get().fireEvent(new TomEEEmbeddedScannerCreated(finder));
            webModule.setFinder(finder);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }

        final File beansXml = new File(request.docBase, "WEB-INF/beans.xml");
        if (beansXml.exists()) { // add it since it is not in the scanned path by default
            try {
                webModule.getAltDDs().put("beans.xml", beansXml.toURI().toURL());
            } catch (final MalformedURLException e) {
                // no-op
            }
        } // else no classpath finding since we'll likely find it
        DeploymentLoader.addBeansXmls(webModule);

        final AppModule app = new AppModule(loader, null);
        app.setStandloneWebModule();
        app.setStandaloneModule(true);
        app.setModuleId(webModule.getModuleId());
        try {
            final Map<String, URL> webDescriptors = DeploymentLoader.getWebDescriptors(jarLocation);
            if (webDescriptors.isEmpty()) { // likely so let's try to find them in the classpath
                final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                final Collection<String> metaDir = asList("META-INF/tomee/", "META-INF/");
                for (final String dd : asList(
                        "app-ctx.xml", "module.properties", "application.properties",
                        "env-entries.properties", NewLoaderLogic.EXCLUSION_FILE,
                        "web.xml", "ejb-jar.xml", "openejb-jar.xml", "validation.xml")) {
                    if (Boolean.parseBoolean(SystemInstance.get().getProperty("tomee.embedded.descriptors.classpath." + dd + ".skip"))
                            || webDescriptors.containsKey(dd)) {
                        continue;
                    }
                    for (final String meta : metaDir) {
                        final URL url = classLoader.getResource(meta + dd);
                        if (url != null) {
                            webDescriptors.put(dd, url);
                            break;
                        }
                    }
                }
            }
            webDescriptors.remove("beans.xml");
            webModule.getAltDDs().putAll(webDescriptors);
            DeploymentLoader.addWebModule(webModule, app);
            DeploymentLoader.addWebModuleDescriptors(new File(webModule.getJarLocation()).toURI().toURL(), webModule, app);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

        if (!SystemInstance.isInitialized() || Boolean.parseBoolean(SystemInstance.get().getProperty("tomee.embedded.add-callers", "true"))) {
            addCallersAsEjbModule(loader, app, request.additionalCallers);
        }

        systemInstance.addObserver(new StandardContextCustomizer(configuration, webModule, request.keepClassloader));
        if (systemInstance.getComponent(AnnotationDeployer.FolderDDMapper.class) == null) {
            systemInstance.setComponent(AnnotationDeployer.FolderDDMapper.class, new AnnotationDeployer.FolderDDMapper() {
                @Override
                public File getDDFolder(final File dir) {
                    try {
                        return isMaven(dir) || isGradle(dir) ? new File(request.docBase, "WEB-INF") : null;
                    } catch (final RuntimeException re) { // folder doesn't exist -> test is stopped which is expected
                        return null;
                    }
                }

                private boolean isGradle(final File dir) {
                    return dir.getName().equals("classes") && dir.getParentFile().getName().equals("target");
                }

                private boolean isMaven(final File dir) {
                    return dir.getName().equals("main") && dir.getParentFile().getName().equals("classes")
                            && dir.getParentFile().getParentFile().getName().equals("build");
                }
            });
        }

        try {
            final AppInfo appInfo = configurationFactory.configureApplication(app);
            systemInstance.getComponent(Assembler.class).createApplication(appInfo, loader /* don't recreate a classloader */);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

        return this;
    }

    private static void addCallersAsEjbModule(final ClassLoader loader, final AppModule app, final String... additionalCallers) {
        final Set<String> callers = new HashSet<>(NewLoaderLogic.callers(Filters.classes(Container.class.getName(), "org.apache.openejb.maven.plugins.TomEEEmbeddedMojo")));
        // we don't care of these
        callers.remove("org.apache.tomee.embedded.Container");
        callers.remove("org.apache.tomee.gradle.embedded.TomEEEmbeddedTask");
        final Iterator<String> callerIt = callers.iterator();
        while (callerIt.hasNext()) { // TomEEEmbeddedMojo is also used with some anonymous classes (TomEEEmbeddedMojo$x)
            if (callerIt.next().startsWith("org.apache.openejb.maven.plugins.TomEEEmbeddedMojo")) {
                callerIt.remove();
                // no break since we remove anonymous class+the mojo itself
            }
        }
        if (additionalCallers != null && additionalCallers.length > 0) {
            callers.addAll(asList(additionalCallers));
        }
        if (callers.isEmpty()) {
            return;
        }
        final EjbJar ejbJar = new EjbJar();
        final OpenejbJar openejbJar = new OpenejbJar();

        for (final String caller : callers) {
            try {
                if (!AnnotationDeployer.isInstantiable(loader.loadClass(caller))) {
                    continue;
                }
            } catch (final ClassNotFoundException e) {
                continue;
            }

            final String name = caller.replace("$", "_");
            final ManagedBean bean = ejbJar.addEnterpriseBean(new ManagedBean(caller.replace("$", "_"), caller, true));
            bean.localBean();
            bean.setTransactionType(TransactionType.BEAN);
            final EjbDeployment ejbDeployment = openejbJar.addEjbDeployment(bean);
            ejbDeployment.setDeploymentId(name);
        }
        final EjbModule ejbModule = new EjbModule(ejbJar, openejbJar);
        ejbModule.setBeans(new Beans());
        app.getEjbModules().add(ejbModule);
    }

    private File fakeRootDir() {
        final File root = new File(configuration.getTempDir());
        Files.mkdirs(root);
        Files.deleteOnExit(root);
        return root;
    }

    private static boolean sameApplication(final File file, final WebAppInfo webApp) {
        String filename = file.getName();
        if (filename.endsWith(".war")) {
            filename = filename.substring(0, filename.length() - 4);
        }
        return filename.equals(webApp.moduleId);
    }

    private static String lastPart(final String name, final String defaultValue) {
        final int idx = name.lastIndexOf("/");
        final int space = name.lastIndexOf(" ");
        if (idx >= 0 && space < idx) {
            return name.substring(idx);
        } else if (idx < 0 && space < 0) {
            return name;
        }
        return defaultValue;
    }

    public void setup(final Configuration configuration) {
        this.configuration = configuration;

        if (configuration.isQuickSession()) {
            tomcat = new TomcatWithFastSessionIDs();
        } else {
            tomcat = new InternalTomcat();
        }

        // create basic installation in setup to be able to handle anything the caller does between setup() and start()
        base = new File(getBaseDir());
        if (base.exists() && configuration.isDeleteBaseOnStartup()) {
            Files.delete(base);
        } else if (!base.exists()) {
            Files.mkdirs(base);
            Files.deleteOnExit(base);
        }

        final File conf = createDirectory(base, "conf");
        createDirectory(base, "lib");
        createDirectory(base, "logs");
        createDirectory(base, "temp");
        createDirectory(base, "work");
        createDirectory(base, "webapps");

        synchronize(conf, configuration.getConf());
    }

    private void synchronize(final File base, final String resourceBase) {
        if (resourceBase == null) {
            return;
        }

        try {
            final Map<String, URL> urls = new ResourceFinder("").getResourcesMap(resourceBase);
            for (final Map.Entry<String, URL> u : urls.entrySet()) {
                try (final InputStream is = u.getValue().openStream()) {
                    final File to = new File(base, u.getKey());
                    IO.copy(is, to);
                    if ("server.xml".equals(u.getKey())) {
                        configuration.setServerXml(to.getAbsolutePath());
                    }
                }
            }
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public File getBase() {
        return base;
    }

    public void start() throws Exception {
        if (base == null || !base.exists()) {
            setup(configuration);
        }

        final Properties props = configuration.getProperties();

        if (props != null) {
            StrSubstitutor substitutor = null;
            for (final String s : props.stringPropertyNames()) {
                final String v = props.getProperty(s);
                if (v != null && v.contains("${")) {
                    if (substitutor == null) {
                        final Map<String, String> placeHolders = new HashMap<>();
                        placeHolders.put("tomee.embedded.http", Integer.toString(configuration.getHttpPort()));
                        placeHolders.put("tomee.embedded.https", Integer.toString(configuration.getHttpsPort()));
                        placeHolders.put("tomee.embedded.stop", Integer.toString(configuration.getStopPort()));
                        substitutor = new StrSubstitutor(placeHolders);
                    }
                    props.put(s, substitutor.replace(v));
                }
            }

            // inherit from system props
            final Properties properties = new Properties(System.getProperties());
            properties.putAll(configuration.getProperties());
            Logger.configure(properties);
        } else {
            Logger.configure();
        }

        final File conf = new File(base, "conf");
        final File webapps = new File(base, "webapps");

        final String catalinaBase = base.getAbsolutePath();

        // set the env before calling anoything on tomcat or Catalina!!
        // TODO: save previous value and restore in stop
        System.setProperty("catalina.base", catalinaBase);
        System.setProperty("openejb.deployments.classpath", "false");
        System.setProperty("catalina.home", catalinaBase);
        System.setProperty("catalina.base", catalinaBase);
        System.setProperty("openejb.home", catalinaBase);
        System.setProperty("openejb.base", catalinaBase);
        System.setProperty("openejb.servicemanager.enabled", "false");

        copyFileTo(conf, "catalina.policy");
        copyTemplateTo(conf, "catalina.properties");
        copyFileTo(conf, "context.xml");
        copyFileTo(conf, "openejb.xml");
        copyFileTo(conf, "tomcat-users.xml");
        copyFileTo(conf, "web.xml");

        final boolean initialized;
        if (configuration.hasServerXml()) {
            final File file = new File(conf, "server.xml");
            if (!file.equals(configuration.getServerXmlFile())) {
                final FileOutputStream fos = new FileOutputStream(file);
                try {
                    IO.copy(configuration.getServerXmlFile(), fos);
                } finally {
                    IO.close(fos);
                }
            }

            // respect config (host/port) of the Configuration
            final QuickServerXmlParser ports = QuickServerXmlParser.parse(file);
            if (configuration.isKeepServerXmlAsThis()) {
                // force ports to be able to stop the server and get @ArquillianResource
                configuration.setHttpPort(Integer.parseInt(ports.http()));
                configuration.setStopPort(Integer.parseInt(ports.stop()));
            } else {
                final Map<String, String> replacements = new HashMap<String, String>();
                replacements.put(ports.http(), String.valueOf(configuration.getHttpPort()));
                replacements.put(ports.https(), String.valueOf(configuration.getHttpsPort()));
                replacements.put(ports.stop(), String.valueOf(configuration.getStopPort()));
                IO.copy(IO.slurp(new ReplaceStringsInputStream(IO.read(file), replacements)).getBytes(), file);
            }

            tomcat.server(createServer(file.getAbsolutePath()));
            initialized = true;
        } else {
            copyFileTo(conf, "server.xml");
            initialized = false;
        }

        if (props != null && !props.isEmpty()) {
            final File file = new File(conf, "system.properties");
            if (file.isFile()) {
                final Properties existing = IO.readProperties(file);
                for (final String key : existing.stringPropertyNames()) {
                    if (!props.containsKey(key)) {
                        props.put(key, existing.getProperty(key));
                    }
                }
            }
            final FileWriter systemProperties = new FileWriter(file);
            try {
                props.store(systemProperties, "");
            } finally {
                IO.close(systemProperties);
            }
        }

        // Need to use JULI so log messages from the tests are visible
        // using openejb logging conf in embedded mode
        /* if we use our config (Logger.configure()) don't override it
        copyFileTo(conf, "logging.properties");
        System.setProperty("java.util.logging.manager", "org.apache.juli.ClassLoaderLogManager");
        final File logging = new File(conf, "logging.properties");
        if (logging.exists()) {
            System.setProperty("java.util.logging.config.file", logging.getAbsolutePath());
        }
        */

        // Trigger loading of catalina.properties
        CatalinaProperties.getProperty("foo");

        tomcat.setBaseDir(base.getAbsolutePath());
        tomcat.setHostname(configuration.getHost());
        if (!initialized) {
            tomcat.getHost().setAppBase(webapps.getAbsolutePath());
            tomcat.getEngine().setDefaultHost(configuration.getHost());
            tomcat.setHostname(configuration.getHost());
        }

        if (configuration.getRealm() != null) {
            tomcat.getEngine().setRealm(configuration.getRealm());
        }

        if (tomcat.getRawConnector() == null && !configuration.isSkipHttp()) {
            final Connector connector = createConnector();
            connector.setPort(configuration.getHttpPort());
            if (connector.getAttribute("connectionTimeout") == null) {
                connector.setAttribute("connectionTimeout", "3000");
            }
            if (configuration.isHttp2()) { // would likely need SSLHostConfig programmatically
                connector.addUpgradeProtocol(new Http2Protocol());
            }

            tomcat.getService().addConnector(connector);
            tomcat.setConnector(connector);
        }

        // create https connector
        if (configuration.isSsl()) {
            final Connector httpsConnector = createConnector();
            httpsConnector.setPort(configuration.getHttpsPort());
            httpsConnector.setSecure(true);
            httpsConnector.setProperty("SSLEnabled", "true");
            httpsConnector.setProperty("sslProtocol", configuration.getSslProtocol());

            if (configuration.getKeystoreFile() != null) {
                httpsConnector.setAttribute("keystoreFile", configuration.getKeystoreFile());
            }
            if (configuration.getKeystorePass() != null) {
                httpsConnector.setAttribute("keystorePass", configuration.getKeystorePass());
            }
            httpsConnector.setAttribute("keystoreType", configuration.getKeystoreType());
            if (configuration.getClientAuth() != null) {
                httpsConnector.setAttribute("clientAuth", configuration.getClientAuth());
            }
            if (configuration.getKeyAlias() != null) {
                httpsConnector.setAttribute("keyAlias", configuration.getKeyAlias());
            }

            if (configuration.isHttp2()) { // would likely need SSLHostConfig programmatically
                httpsConnector.addUpgradeProtocol(new Http2Protocol());
            }

            tomcat.getService().addConnector(httpsConnector);

            if (configuration.isSkipHttp()) {
                tomcat.setConnector(httpsConnector);
            }
        }

        for (final Connector c : configuration.getConnectors()) {
            tomcat.getService().addConnector(c);
        }
        if (!configuration.isSkipHttp() && !configuration.isSsl() && !configuration.getConnectors().isEmpty()) {
            tomcat.setConnector(configuration.getConnectors().iterator().next());
        }

        // Bootstrap Tomcat
        Logger.getInstance(LogCategory.OPENEJB_STARTUP, Container.class).info("Starting TomEE from: " + base.getAbsolutePath()); // create it after Logger is configured

        if (configuration.getUsers() != null) {
            for (final Map.Entry<String, String> user : configuration.getUsers().entrySet()) {
                tomcat.addUser(user.getKey(), user.getValue());
            }
        }
        if (configuration.getRoles() != null) {
            for (final Map.Entry<String, String> user : configuration.getRoles().entrySet()) {
                for (final String role : user.getValue().split(" *, *")) {
                    tomcat.addRole(user.getKey(), role);
                }
            }
        }
        if (!initialized) {
            tomcat.init();
        }
        tomcat.start();

        // Bootstrap OpenEJB
        final Properties properties = new Properties();
        properties.setProperty("openejb.deployments.classpath", "false");
        properties.setProperty("openejb.loader", "tomcat-system");
        properties.setProperty("openejb.home", catalinaBase);
        properties.setProperty("openejb.base", catalinaBase);
        properties.setProperty("openejb.servicemanager.enabled", "false");
        if (configuration.getProperties() != null) {
            properties.putAll(configuration.getProperties());
        }
        if (properties.getProperty("openejb.system.apps") == null) { // will make startup faster and it is rarely useful for embedded case
            properties.setProperty("openejb.system.apps", "false");
        }
        if (configuration.isQuickSession()) {
            properties.put("openejb.session.manager", QuickSessionManager.class.getName());
        }

        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            final Properties tomcatServerInfo = IO.readProperties(classLoader.getResourceAsStream("org/apache/catalina/util/ServerInfo.properties"), new Properties());

            String serverNumber = tomcatServerInfo.getProperty("server.number");
            if (serverNumber == null) {
                // Tomcat5 only has server.info
                final String serverInfo = tomcatServerInfo.getProperty("server.info");
                if (serverInfo != null) {
                    final int slash = serverInfo.indexOf('/');
                    serverNumber = serverInfo.substring(slash + 1);
                }
            }
            if (serverNumber != null) {
                System.setProperty("tomcat.version", serverNumber);
            }

            final String serverBuilt = tomcatServerInfo.getProperty("server.built");
            if (serverBuilt != null) {
                System.setProperty("tomcat.built", serverBuilt);
            }
        } catch (final Throwable e) {
            // no-op
        }

        final TomcatLoader loader = new TomcatLoader();
        loader.initDefaults(properties);

        // need to add properties after having initialized defaults
        // to properties passed to SystemInstance otherwise we loose some of them
        final Properties initProps = new Properties();
        initProps.putAll(System.getProperties());
        initProps.putAll(properties);
        if (SystemInstance.isInitialized()) {
            SystemInstance.get().getProperties().putAll(initProps);
        } else {
            SystemInstance.init(initProps);
        }
        SystemInstance.get().setComponent(StandardServer.class, (StandardServer) tomcat.getServer());
        SystemInstance.get().setComponent(Server.class, tomcat.getServer()); // needed again cause of init()

        loader.initialize(properties);

        assembler = SystemInstance.get().getComponent(Assembler.class);
        configurationFactory = new ConfigurationFactory();

        if (configuration.isWithEjbRemote()) {
            tomcat.getHost().addChild(new TomEERemoteWebapp());
        }
    }

    protected Connector createConnector() {
        final Connector connector;
        final Properties properties = configuration.getProperties();
        if (properties != null) {
            final Map<String, String> attributes = new HashMap<>();
            final ObjectRecipe recipe = new ObjectRecipe(Connector.class);
            for (final String key : properties.stringPropertyNames()) {
                if (!key.startsWith("connector.")) {
                    continue;
                }
                final String substring = key.substring("connector.".length());
                if (!substring.startsWith("attributes.")) {
                    recipe.setProperty(substring, properties.getProperty(key));
                } else {
                    attributes.put(substring.substring("attributes.".length()), properties.getProperty(key));
                }
            }
            connector = recipe.getProperties().isEmpty() ? new Connector() : Connector.class.cast(recipe.create());
            for (final Map.Entry<String, String> attr : attributes.entrySet()) {
                connector.setAttribute(attr.getKey(), attr.getValue());
            }
        } else {
            connector = new Connector();
        }
        return connector;
    }

    private static Server createServer(final String serverXml) {
        final Catalina catalina = new Catalina() {
            // skip few init we don't need *here*
            @Override
            protected void initDirs() {
                // no-op
            }

            @Override
            protected void initStreams() {
                // no-op
            }

            @Override
            protected void initNaming() {
                // no-op
            }
        };
        catalina.setConfigFile(serverXml);
        catalina.load();
        return catalina.getServer();
    }

    public ConfigurationFactory getConfigurationFactory() {
        return configurationFactory;
    }

    private String getBaseDir() {
        File file;
        try {

            final String dir = configuration.getDir();
            if (dir != null) {
                final File dirFile = new File(dir);
                if (dirFile.exists()) {
                    return dir;
                }
                return Files.mkdir(dirFile).getAbsolutePath();
            }

            try {
                final File target = new File("target");
                file = File.createTempFile("apache-tomee", "-home", target.exists() ? target : null);
            } catch (final Exception e) {

                final File tmp = new File(configuration.getTempDir());
                if (!tmp.exists() && !tmp.mkdirs()) {
                    throw new IOException("Failed to create local tmp directory: " + tmp.getAbsolutePath());
                }

                file = File.createTempFile("apache-tomee", "-home", tmp);
            }

            return file.getAbsolutePath();

        } catch (final IOException e) {
            throw new TomEERuntimeException("Failed to get or create base dir: " + configuration.getDir(), e);
        }
    }

    public void stop() throws Exception {

        final Connector connector = tomcat.getConnector();
        if (null != connector) {
            connector.stop();
        }

        try {
            tomcat.stop();
        } catch (final LifecycleException e) {
            e.printStackTrace();
        }
        try {
            tomcat.destroy();
        } catch (final LifecycleException e) {
            e.printStackTrace();
        }
        if (configuration.isDeleteBaseOnStartup()) {
            try {
                deleteTree(base);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        OpenEJB.destroy();
        // don't set base = null here to be able to use base after to clean up from outside of this class
    }

    @SuppressWarnings("UnusedDeclaration")
    public AppContext deploy(final String name, final File file) throws OpenEJBException, IOException, NamingException {
        return deploy(name, file, false);
    }

    public AppContext deploy(final String name, final File file, final boolean overrideName) throws OpenEJBException, IOException, NamingException {

        final AppContext context;
        final AppInfo appInfo;

        if (WebAppDeployer.Helper.isWebApp(file)) {
            String contextRoot = file.getName();
            if (overrideName) {
                contextRoot = name;
            }

            appInfo = SystemInstance.get().getComponent(WebAppDeployer.class).deploy(null, contextRoot, file);

            if (appInfo != null) {
                context = SystemInstance.get().getComponent(ContainerSystem.class).getAppContext(appInfo.appId);
            } else {
                context = null;
            }
        } else {
            appInfo = configurationFactory.configureApplication(file);
            // ensure to activate CDI for classpath deployment, we can desire to move it but it breaks less apps this way
            for (final EjbJarInfo jar : appInfo.ejbJars) {
                if (jar.enterpriseBeans.size() == 1) {
                    final EnterpriseBeanInfo next = jar.enterpriseBeans.iterator().next();
                    if (ManagedBeanInfo.class.isInstance(next) && ManagedBeanInfo.class.cast(next).hidden) {
                        continue;
                    }
                }
                if (jar.beans == null) {
                    if (!jar.enterpriseBeans.isEmpty()) {
                        jar.beans = new BeansInfo();
                        jar.beans.version = "1.1";
                        jar.beans.discoveryMode = "annotated";
                        final BeansInfo.BDAInfo info = new BeansInfo.BDAInfo();
                        info.discoveryMode = "annotated";
                        info.uri = jar.moduleUri;
                        jar.beans.noDescriptorBdas.add(info);
                        for (final EnterpriseBeanInfo bean : jar.enterpriseBeans) {
                            if (bean.ejbClass == null) {
                                continue;
                            }
                            info.managedClasses.add(bean.ejbClass);
                        }
                    }
                }
            }
            if (overrideName) {
                appInfo.appId = name;
                for (final EjbJarInfo ejbJar : appInfo.ejbJars) {
                    if (file.getName().equals(ejbJar.moduleName)) {
                        ejbJar.moduleName = name;
                        ejbJar.moduleId = name;
                    }
                    for (final EnterpriseBeanInfo ejb : ejbJar.enterpriseBeans) {
                        if (BeanContext.Comp.openejbCompName(file.getName()).equals(ejb.ejbName)) {
                            ejb.ejbName = BeanContext.Comp.openejbCompName(name);
                        }
                    }
                }
                for (final WebAppInfo webApp : appInfo.webApps) {
                    if (sameApplication(file, webApp)) {
                        webApp.moduleId = name;
                        webApp.contextRoot = lastPart(name, webApp.contextRoot);
                        if ("ROOT".equals(webApp.contextRoot)) {
                            webApp.contextRoot = "";
                        }
                    }
                }
            }

            context = assembler.createApplication(appInfo);
        }

        moduleIds.put(name, null != appInfo ? appInfo.path : null);
        infos.put(name, appInfo);
        appContexts.put(name, context);

        return context;
    }

    @SuppressWarnings("UnusedDeclaration")
    public AppInfo getInfo(final String name) {
        return infos.get(name);
    }

    public void undeploy(final String name) throws UndeployException, NoSuchApplicationException {
        final String moduleId = moduleIds.remove(name);
        infos.remove(name);
        appContexts.remove(name);
        if (moduleId != null) {
            assembler.destroyApplication(moduleId);
        }
    }

    public Context getJndiContext() {
        return assembler.getContainerSystem().getJNDIContext();
    }

    public AppContext getAppContexts(final String moduleId) {
        return appContexts.get(moduleId);
    }

    private void deleteTree(final File file) {
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            return;
        }

        if (file.isFile()) {
            if (!file.delete()) {
                file.deleteOnExit();
            }
            return;
        }

        if (file.isDirectory()) {
            if ("".equals(file.getName())) {
                return;
            }
            if ("src/main".equals(file.getName())) {
                return;
            }

            final File[] children = file.listFiles();

            if (children != null) {
                for (final File child : children) {
                    deleteTree(child);
                }
            }

            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
    }

    private void copyTemplateTo(final File targetDir, final String filename) throws Exception {
        final File file = new File(targetDir, filename);
        if (file.exists()) {
            return;
        }

        // don't break apps using Velocity facade
        final VelocityEngine engine = new VelocityEngine();
        engine.setProperty(Velocity.RESOURCE_LOADER, "class");
        engine.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
        engine.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
        engine.init();
        final Template template = engine.getTemplate("/org/apache/tomee/configs/" + filename);
        final VelocityContext context = new VelocityContext();
        context.put("tomcatHttpPort", Integer.toString(configuration.getHttpPort()));
        context.put("tomcatShutdownPort", Integer.toString(configuration.getStopPort()));
        final Writer writer = new FileWriter(file);
        template.merge(context, writer);
        writer.flush();
        writer.close();
    }

    private void copyFileTo(final File targetDir, final String filename) throws IOException {
        final File to = new File(targetDir, filename);
        if (to.exists()) { // user provided one
            return;
        }

        final InputStream is = getClass().getResourceAsStream("/org/apache/tomee/configs/" + filename);
        if (is != null) { // should be null since we are using default conf
            try {
                IO.copy(is, to);
            } finally {
                IO.close(is);
            }
        }
    }

    private File createDirectory(final File parent, final String directory) {
        final File dir = new File(parent, directory);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Unable to make dir " + dir.getAbsolutePath());
        }

        return dir;
    }

    public Tomcat getTomcat() {
        return tomcat;
    }

    public void await() {
        tomcat.getServer().await();
    }

    @Override
    public void close() {
        final CountDownLatch end = new CountDownLatch(1);
        final Container container = Container.this;
        new Thread() {
            {
                setName("tomee-embedded-await-" + hashCode());
            }

            @Override
            public void run() {
                try {
                    container.await();
                    end.countDown();
                } catch (final Exception e) {
                    end.countDown();
                    throw new IllegalStateException(e);
                }
            }
        }.start();

        try {
            stop();
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to stop container", e);
        }

        try {
            end.await();
        } catch (final InterruptedException e) {
            Thread.interrupted();
        }
    }

    public org.apache.catalina.Context addContext(final String context, final String path) {
        final File root = new File(path);
        if (!root.exists()) {
            Files.mkdirs(root);
        }
        return getTomcat().addContext(context, root.getAbsolutePath()); // we don't want to be relative
    }

    public Container inject(final Object instance) {
        Injector.inject(instance);
        return this;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    private static class InternalTomcat extends Tomcat {
        private Connector connector;

        private void server(final Server s) {
            server = s;
            connector = server != null && server.findServices().length > 0 && server.findServices()[0].findConnectors().length > 0 ?
                    server.findServices()[0].findConnectors()[0] : null;
        }

        public Connector getRawConnector() {
            return connector;
        }
    }

    private static class TomcatWithFastSessionIDs extends InternalTomcat {
        @Override
        public void start() throws LifecycleException {
            // Use fast, insecure session ID generation for all tests
            final Server server = getServer();
            for (final Service service : server.findServices()) {
                final org.apache.catalina.Container e = service.getContainer();
                for (final org.apache.catalina.Container h : e.findChildren()) {
                    for (final org.apache.catalina.Container c : h.findChildren()) {
                        Manager m = ((org.apache.catalina.Context) c).getManager();
                        if (m == null) {
                            m = new StandardManager();
                            ((org.apache.catalina.Context) c).setManager(m);
                        }
                        if (m instanceof ManagerBase) {
                            ((ManagerBase) m).setSecureRandomClass(
                                    "org.apache.catalina.startup.FastNonSecureRandom");
                        }
                    }
                }
            }
            super.start();
        }
    }

    // there to allow to add params without breaking signature each time
    public static class DeploymentRequest {
        private final String context;
        private final List<URL> jarList;
        private final File docBase;
        private final boolean keepClassloader;
        private final String[] additionalCallers;
        private final Archive archive;

        public DeploymentRequest(final String context, final List<URL> jarList, final File docBase,
                                 final boolean keepClassloader, final String[] additionalCallers,
                                 final Archive archive) {
            this.context = context;
            this.jarList = jarList == null ? Collections.<URL>emptyList() : jarList;
            this.docBase = docBase;
            this.keepClassloader = keepClassloader;
            this.additionalCallers = additionalCallers;
            this.archive = archive;
        }
    }
}
