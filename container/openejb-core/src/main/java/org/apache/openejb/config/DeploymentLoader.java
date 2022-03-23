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

package org.apache.openejb.config;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.api.LocalClient;
import org.apache.openejb.api.RemoteClient;
import org.apache.openejb.cdi.CompositeBeans;
import org.apache.openejb.classloader.ClassLoaderConfigurer;
import org.apache.openejb.classloader.WebAppEnricher;
import org.apache.openejb.config.event.BeforeDeploymentEvent;
import org.apache.openejb.config.event.EnhanceScannableUrlsEvent;
import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.core.EmptyResourcesClassLoader;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.FacesConfig;
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.openejb.jee.JspConfig;
import org.apache.openejb.jee.Module;
import org.apache.openejb.jee.Taglib;
import org.apache.openejb.jee.TldTaglib;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebserviceDescription;
import org.apache.openejb.jee.Webservices;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.sxc.ApplicationXml;
import org.apache.openejb.util.AnnotationFinder;
import org.apache.openejb.util.JarExtractor;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.ResourceFinder;
import org.apache.xbean.finder.UrlSet;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.apache.xbean.finder.filter.Filter;
import org.apache.xbean.finder.filter.Filters;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * @version $Revision$ $Date$
 */
public class DeploymentLoader implements DeploymentFilterable {
    public static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, "org.apache.openejb.util.resources");
    public static final String OPENEJB_ALTDD_PREFIX = "openejb.altdd.prefix";

    static final String META_INF = "META-INF/";

    public static final String EAR_WEBAPP_PERSISTENCE_XML_JARS = "ear-webapp-persistence-xml-jars";
    public static final String EAR_SCOPED_CDI_BEANS = "ear-scoped-cdi-beans_";
    public static final String RAR_URLS_KEY = "rar-urls";
    public static final String URLS_KEY = "urls";

    private static final String RESOURCES_XML = "resources.xml";
    private static final String WEB_FRAGMENT_XML = "web-fragment.xml";

    private final boolean scanManagedBeans = true;
    private static final Collection<String> KNOWN_DESCRIPTORS = Arrays.asList("app-ctx.xml", "module.properties", "application.properties", "web.xml", "ejb-jar.xml", "openejb-jar.xml", "env-entries.properties", "beans.xml", "ra.xml", "application.xml", "application-client.xml", "persistence-fragment.xml", "persistence.xml", "validation.xml", NewLoaderLogic.EXCLUSION_FILE);
    private static String ALTDD = SystemInstance.get().getOptions().get(OPENEJB_ALTDD_PREFIX, (String) null);
    private volatile List<URL> containerUrls = null;

    @Deprecated // use load(File, ExternalConfiguration)
    public AppModule load(final File jarFile) throws OpenEJBException {
        return load(jarFile, null);
    }

    /**
     * @param jarFile the app file (war, jar, ear)
     * @param config potentially some more config, mainly used when linking to another system like tomcat to enrich the conf we can guess
     * @return the loaded module
     */
    public AppModule load(final File jarFile, final ExternalConfiguration config) throws OpenEJBException {
        // verify we have a valid file
        final String jarPath;
        try {
            jarPath = jarFile.getCanonicalPath();
        } catch (final IOException e) {
            throw new OpenEJBException("Invalid application file path " + jarFile, e);
        }

        final URL baseUrl = getFileUrl(jarFile);

        // create a class loader to use for detection of module type
        // do not use this class loader for any other purposes... it is
        // non-temp class loader and usage will mess up JPA
        ClassLoader doNotUseClassLoader = null;// = ClassLoaderUtil.createClassLoader(jarPath, new URL[]{baseUrl}, OpenEJB.class.getClassLoader());

        try {
            // determine the module type
            final Class<? extends DeploymentModule> moduleClass;

            try {
                doNotUseClassLoader = ClassLoaderUtil.createClassLoader(jarPath, new URL[]{baseUrl}, getOpenEJBClassLoader());
                moduleClass = discoverModuleType(baseUrl, ClassLoaderUtil.createTempClassLoader(doNotUseClassLoader), true);
            } catch (final Exception e) {
                throw new UnknownModuleTypeException("Unable to determine module type for jar: " + baseUrl.toExternalForm(), e);
            }

            if (ResourcesModule.class.equals(moduleClass)) {
                final AppModule appModule = new AppModule(null, jarPath);
                final ResourcesModule module = new ResourcesModule();
                module.getAltDDs().put(RESOURCES_XML, baseUrl);
                ReadDescriptors.readResourcesXml(module);
                module.initAppModule(appModule);
                // here module is no more useful since everything is in the appmodule
                return appModule;
            }

            //We always load AppModule, as it somewhat likes a wrapper module
            if (AppModule.class.equals(moduleClass)) {
                return createAppModule(jarFile, jarPath);
            }

            if (EjbModule.class.equals(moduleClass)) {
                final URL[] urls = new URL[]{baseUrl};

                SystemInstance.get().fireEvent(new BeforeDeploymentEvent(urls));

                final ClassLoader classLoader = ClassLoaderUtil.createTempClassLoader(jarPath, urls, getOpenEJBClassLoader());

                final AppModule appModule;
                //final Class<? extends DeploymentModule> o = EjbModule.class;
                final EjbModule ejbModule = createEjbModule(baseUrl, jarPath, classLoader);

                // wrap the EJB Module with an Application Module
                appModule = new AppModule(ejbModule);

                addPersistenceUnits(appModule, baseUrl);

                return appModule;
            }

            if (ClientModule.class.equals(moduleClass)) {
                final String jarLocation = URLs.toFilePath(baseUrl);
                final ClientModule clientModule = createClientModule(baseUrl, jarLocation, getOpenEJBClassLoader(), null);

                // Wrap the resource module with an Application Module
                return new AppModule(clientModule);
            }

            if (ConnectorModule.class.equals(moduleClass)) {
                final String jarLocation = URLs.toFilePath(baseUrl);
                final ConnectorModule connectorModule = createConnectorModule(jarLocation, jarLocation, getOpenEJBClassLoader(), null);
                if (connectorModule != null) {
                    final List<ConnectorModule> connectorModules = new ArrayList<>();

                    // let it be able to deploy the same connector several times
                    final String id = connectorModule.getModuleId();
                    if (!"true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.connector." + id + ".skip-default", "false"))) {
                        connectorModules.add(connectorModule);
                    }

                    final String aliases = SystemInstance.get().getProperty("openejb.connector." + id + ".aliases");
                    if (aliases != null) {
                        for (final String alias : aliases.split(",")) {
                            final ConnectorModule aliasModule = createConnectorModule(jarLocation, jarLocation, getOpenEJBClassLoader(), alias);
                            connectorModules.add(aliasModule);
                        }
                    }


                    // Wrap the resource module with an Application Module
                    final AppModule appModule = new AppModule(connectorModules.toArray(new ConnectorModule[connectorModules.size()]));

                    return appModule;
                }
            }

            if (WebModule.class.equals(moduleClass)) {
                final File file = URLs.toFile(baseUrl);

                // Standalone Web Module
                final WebModule webModule = createWebModule(file.getAbsolutePath(), baseUrl, getOpenEJBClassLoader(), getContextRoot(), getModuleName(), config);
                // important to use the webapp classloader here otherwise each time we'll check something using loadclass it will fail (=== empty classloader)
                final AppModule appModule = new AppModule(webModule.getClassLoader(), file.getAbsolutePath(), new Application(), true);
                addWebModule(webModule, appModule);

                addWebModuleDescriptors(baseUrl, webModule, appModule);

                appModule.setStandloneWebModule();
                appModule.setDelegateFirst(true); // force it for webapps
                return appModule;
            }

            if (PersistenceModule.class.equals(moduleClass)) {
                final String jarLocation = URLs.toFilePath(baseUrl);
                final ClassLoader classLoader = ClassLoaderUtil.createTempClassLoader(jarPath, new URL[]{baseUrl}, getOpenEJBClassLoader());

                // wrap the EJB Module with an Application Module
                final AppModule appModule = new AppModule(classLoader, jarLocation);

                // Persistence Units
                addPersistenceUnits(appModule, baseUrl);

                return appModule;
            }

            throw new UnsupportedModuleTypeException("Unsupported module type: " + moduleClass.getSimpleName());

        } finally {
            // if the application was unpacked appId used to create this class loader will be wrong
            // We can safely destroy this class loader in either case, as it was not use by any modules
            if (null != doNotUseClassLoader) {
                ClassLoaderUtil.destroyClassLoader(doNotUseClassLoader);
            }
        }
    }

    public static void addWebModuleDescriptors(final URL baseUrl, final WebModule webModule, final AppModule appModule) throws OpenEJBException {
        final List<URL> urls = webModule.getScannableUrls();
        final ResourceFinder finder = new ResourceFinder("", urls.toArray(new URL[urls.size()]));
        final Map<String, Object> otherDD = new HashMap<>(getDescriptors(finder, false));

        // "persistence.xml" is done separately since we manage a list of url and not s single url
        try {
            final List<URL> persistenceXmls = finder.findAll(META_INF + "persistence.xml");
            if (persistenceXmls.size() >= 1) {
                final URL old = (URL) otherDD.get("persistence.xml");
                if (old != null && !persistenceXmls.contains(old)) {
                    persistenceXmls.add(old);
                }
                otherDD.put("persistence.xml", persistenceXmls);
            }
        } catch (final IOException e) {
            // ignored
        }

        addConnectorModules(appModule, webModule);

        addWebPersistenceDD("persistence.xml", otherDD, appModule);
        addWebPersistenceDD("persistence-fragment.xml", otherDD, appModule);
        addPersistenceUnits(appModule, baseUrl);
        addWebFragments(webModule, urls);
    }

    private static void addConnectorModules(final AppModule appModule, final WebModule webModule) throws OpenEJBException {
        // WEB-INF
        if (webModule.getAltDDs().containsKey("ra.xml")) {
            final String jarLocation = new File(webModule.getJarLocation(), "/WEB-INF/classes").getAbsolutePath();
            final ConnectorModule connectorModule = createConnectorModule(jarLocation, jarLocation, webModule.getClassLoader(), webModule.getModuleId() + "RA", (URL) webModule.getAltDDs().get("ra.xml"));
            if (connectorModule != null) {
                appModule.getConnectorModules().add(connectorModule);
            }
        }

        // .rar
        for (final URL url : webModule.getRarUrls()) {
            try {
                final File file = URLs.toFile(url);
                if (file.getName().endsWith(".rar")) {
                    final String jarLocation = file.getAbsolutePath();
                    final ConnectorModule connectorModule = createConnectorModule(jarLocation, jarLocation, webModule.getClassLoader(), null);
                    if (connectorModule != null) {
                        appModule.getConnectorModules().add(connectorModule);
                    }
                }
            } catch (final Exception e) {
                LOGGER.error("error processing url " + url.toExternalForm(), e);
            }
        }

        for (final URL url : webModule.getScannableUrls()) {
            try {
                final File file = URLs.toFile(url);
                if (file.getName().endsWith(".jar")) {
                    try (JarFile jarFile = new JarFile(file)) {

                        // TODO: better management of altdd
                        String name = (ALTDD != null ? ALTDD + "." : "") + "ra.xml";

                        JarEntry entry = jarFile.getJarEntry(name);
                        if (entry == null) {
                            name = "META-INF/" + name;
                            entry = jarFile.getJarEntry(name);
                        }
                        if (entry == null) {
                            continue;
                        }

                        final String jarLocation = file.getAbsolutePath();
                        final ConnectorModule connectorModule = createConnectorModule(jarLocation, jarLocation, webModule.getClassLoader(), null);
                        if (connectorModule != null) {
                            appModule.getConnectorModules().add(connectorModule);
                        }
                    }
                }
            } catch (final Exception e) {
                LOGGER.error("error processing url " + url.toExternalForm(), e);
            }
        }
    }

    protected ClassLoader getOpenEJBClassLoader() {
        return ParentClassLoaderFinder.Helper.get();
    }

    @SuppressWarnings("unchecked")
    private static void addWebPersistenceDD(final String name, final Map<String, Object> otherDD, final AppModule appModule) {
        if (otherDD.containsKey(name)) {
            List<URL> persistenceUrls = (List<URL>) appModule.getAltDDs().get(name);
            if (persistenceUrls == null) {
                persistenceUrls = new ArrayList<>();
                appModule.getAltDDs().put(name, persistenceUrls);
            }

            if (otherDD.containsKey(name)) {
                final Object otherUrl = otherDD.get(name);
                if (otherUrl instanceof URL && !persistenceUrls.contains(otherUrl)) {
                    persistenceUrls.add((URL) otherUrl);
                } else if (otherUrl instanceof List) {
                    final List<URL> otherList = (List<URL>) otherDD.get(name);
                    for (final URL url : otherList) {
                        if (!persistenceUrls.contains(url)) {
                            persistenceUrls.add(url);
                        }
                    }
                }
            }
        }
    }

    protected AppModule createAppModule(final File jarFile, final String jarPath) throws OpenEJBException {
        File appDir = unpack(jarFile);
        try {
            appDir = appDir.getCanonicalFile();
        } catch (final IOException e) {
            throw new OpenEJBException("Invalid application directory " + appDir.getAbsolutePath());
        }

        final URL appUrl = getFileUrl(appDir);

        final String appId = appDir.getAbsolutePath();
        final ClassLoader tmpClassLoader = ClassLoaderUtil.createTempClassLoader(appId, new URL[]{appUrl}, getOpenEJBClassLoader());

        final ResourceFinder finder = new ResourceFinder("", tmpClassLoader, appUrl);
        final Map<String, URL> appDescriptors = getDescriptors(finder);

        try {

            //
            // Find all the modules using either the application xml or by searching for all .jar, .war and .rar files.
            //

            final Map<String, URL> ejbModules = new LinkedHashMap<>();
            final Map<String, URL> clientModules = new LinkedHashMap<>();
            final Map<String, URL> resouceModules = new LinkedHashMap<>();
            final Map<String, URL> webModules = new LinkedHashMap<>();
            final Map<String, String> webContextRoots = new LinkedHashMap<>();

            final URL applicationXmlUrl = appDescriptors.get("application.xml");
            final List<URL> extraLibs = new ArrayList<>();

            final Application application;
            if (applicationXmlUrl != null) {

                application = unmarshal(applicationXmlUrl);
                for (final Module module : application.getModule()) {
                    try {
                        if (module.getEjb() != null) {
                            final URL url = finder.find(module.getEjb().trim());
                            ejbModules.put(module.getEjb(), url);
                        } else if (module.getJava() != null) {
                            final URL url = finder.find(module.getJava().trim());
                            clientModules.put(module.getJava(), url);
                            extraLibs.add(url);
                        } else if (module.getConnector() != null) {
                            final URL url = finder.find(module.getConnector().trim());
                            resouceModules.put(module.getConnector(), url);
                        } else if (module.getWeb() != null) {
                            final URL url = finder.find(module.getWeb().getWebUri().trim());
                            webModules.put(module.getWeb().getWebUri(), url);
                            webContextRoots.put(module.getWeb().getWebUri(), module.getWeb().getContextRoot());
                        }
                    } catch (final IOException e) {
                        throw new OpenEJBException("Invalid path to module " + e.getMessage(), e);
                    }
                }
            } else {
                application = new Application();
                final HashMap<String, URL> files = new HashMap<>();
                scanDir(appDir, files, "", false);
                files.remove("META-INF/MANIFEST.MF");

                // todo we should also filter URLs here using DeploymentsResolver.loadFromClasspath

                createApplicationFromFiles(appId, tmpClassLoader, ejbModules, clientModules, resouceModules, webModules, files);
            }

            final ClassLoaderConfigurer configurer = QuickJarsTxtParser.parse(new File(appDir, "META-INF/" + QuickJarsTxtParser.FILE_NAME));
            final Collection<URL> jarsXmlLib = new ArrayList<>();
            if (configurer != null) {
                for (final URL url : configurer.additionalURLs()) {
                    try {
                        detectAndAddModuleToApplication(appId, tmpClassLoader,
                            ejbModules, clientModules, resouceModules, webModules,
                            new ImmutablePair<>(URLs.toFile(url).getAbsolutePath(), url));
                    } catch (final Exception e) {
                        jarsXmlLib.add(url);
                    }
                }
            }

            //
            // Create a class loader for the application
            //

            // lib/*
            if (application.getLibraryDirectory() == null) {
                application.setLibraryDirectory("lib/");
            } else {
                final String dir = application.getLibraryDirectory();
                if (!dir.endsWith("/")) {
                    application.setLibraryDirectory(dir + "/");
                }
            }

            try {
                final Map<String, URL> libs = finder.getResourcesMap(application.getLibraryDirectory());
                extraLibs.addAll(libs.values());
            } catch (final IOException e) {
                LOGGER.warning("Cannot load libs from '" + application.getLibraryDirectory() + "' : " + e.getMessage(), e);
            }

            // APP-INF/lib/*
            try {
                final Map<String, URL> libs = finder.getResourcesMap("APP-INF/lib/");
                extraLibs.addAll(libs.values());
            } catch (final IOException e) {
                LOGGER.warning("Cannot load libs from 'APP-INF/lib/' : " + e.getMessage(), e);
            }

            // META-INF/lib/*
            try {
                final Map<String, URL> libs = finder.getResourcesMap("META-INF/lib/");
                extraLibs.addAll(libs.values());
            } catch (final IOException e) {
                LOGGER.warning("Cannot load libs from 'META-INF/lib/' : " + e.getMessage(), e);
            }

            // All jars nested in the Resource Adapter
            final HashMap<String, URL> rarLibs = new HashMap<>();
            for (final Map.Entry<String, URL> entry : resouceModules.entrySet()) {
                try {
                    // unpack the resource adapter archive
                    File rarFile = URLs.toFile(entry.getValue());
                    rarFile = unpack(rarFile);
                    entry.setValue(rarFile.toURI().toURL());

                    scanDir(appDir, rarLibs, "");
                } catch (final MalformedURLException e) {
                    throw new OpenEJBException("Malformed URL to app. " + e.getMessage(), e);
                }
            }
            for (final Iterator<Map.Entry<String, URL>> iterator = rarLibs.entrySet().iterator(); iterator.hasNext(); ) {
                // remove all non jars from the rarLibs
                final Map.Entry<String, URL> fileEntry = iterator.next();
                if (!fileEntry.getKey().endsWith(".jar")) {
                    continue;
                }
                iterator.remove();
            }

            final List<URL> classPath = new ArrayList<>();
            classPath.addAll(ejbModules.values());
            classPath.addAll(clientModules.values());
            classPath.addAll(rarLibs.values());
            classPath.addAll(extraLibs);
            classPath.addAll(jarsXmlLib);
            final URL[] urls = classPath.toArray(new URL[classPath.size()]);

            SystemInstance.get().fireEvent(new BeforeDeploymentEvent(urls));

            final ClassLoader appClassLoader = ClassLoaderUtil.createTempClassLoader(appId, urls, getOpenEJBClassLoader());

            //
            // Create the AppModule and all nested module objects
            //

            final AppModule appModule = new AppModule(appClassLoader, appId, application, false);
            appModule.getAdditionalLibraries().addAll(extraLibs);
            appModule.getAltDDs().putAll(appDescriptors);
            appModule.getWatchedResources().add(appId);
            if (applicationXmlUrl != null) {
                appModule.getWatchedResources().add(URLs.toFilePath(applicationXmlUrl));
            }
            if (appDescriptors.containsKey(RESOURCES_XML)) {
                final Map<String, Object> altDd = new HashMap<>(appDescriptors);
                ReadDescriptors.readResourcesXml(new org.apache.openejb.config.Module(false) {
                    @Override
                    public Map<String, Object> getAltDDs() {
                        return altDd;
                    }

                    @Override
                    public void initResources(final Resources resources) {
                        appModule.getContainers().addAll(resources.getContainer());
                        appModule.getResources().addAll(resources.getResource());
                        appModule.getServices().addAll(resources.getService());
                    }
                });
            }

            // EJB modules
            for (final Map.Entry<String, URL> stringURLEntry : ejbModules.entrySet()) {
                try {
                    URL ejbUrl = stringURLEntry.getValue();
                    // we should try to use a reference to the temp classloader
                    if (ClassLoaderUtil.isUrlCached(appModule.getJarLocation(), ejbUrl)) {
                        try {
                            ejbUrl = ClassLoaderUtil.getUrlCachedName(appModule.getJarLocation(), ejbUrl).toURI().toURL();

                        } catch (final MalformedURLException ignore) {
                            // no-op
                        }
                    }
                    final File ejbFile = URLs.toFile(ejbUrl);
                    final String absolutePath = ejbFile.getAbsolutePath();

                    final EjbModule ejbModule = createEjbModule(ejbUrl, absolutePath, appClassLoader);
                    appModule.getEjbModules().add(ejbModule);
                } catch (final OpenEJBException e) {
                    LOGGER.error("Unable to load EJBs from EAR: " + appId + ", module: " + stringURLEntry.getKey() + ". Exception: " + e.getMessage(), e);
                }
            }

            // Application Client Modules
            for (final Map.Entry<String, URL> stringURLEntry : clientModules.entrySet()) {
                try {
                    URL clientUrl = stringURLEntry.getValue();
                    // we should try to use a reference to the temp classloader
                    if (ClassLoaderUtil.isUrlCached(appModule.getJarLocation(), clientUrl)) {
                        try {
                            clientUrl = ClassLoaderUtil.getUrlCachedName(appModule.getJarLocation(), clientUrl).toURI().toURL();

                        } catch (final MalformedURLException ignore) {
                            // no-op
                        }
                    }
                    final File clientFile = URLs.toFile(clientUrl);
                    final String absolutePath = clientFile.getAbsolutePath();

                    final ClientModule clientModule = createClientModule(clientUrl, absolutePath, appClassLoader, null);

                    appModule.getClientModules().add(clientModule);
                } catch (final Exception e) {
                    LOGGER.error("Unable to load App Client from EAR: " + appId + ", module: " + stringURLEntry.getKey() + ". Exception: " + e.getMessage(), e);
                }
            }

            // Resource modules
            for (final Map.Entry<String, URL> stringURLEntry : resouceModules.entrySet()) {
                try {
                    URL rarUrl = stringURLEntry.getValue();
                    // we should try to use a reference to the temp classloader
                    if (ClassLoaderUtil.isUrlCached(appModule.getJarLocation(), rarUrl)) {
                        try {
                            rarUrl = ClassLoaderUtil.getUrlCachedName(appModule.getJarLocation(), rarUrl).toURI().toURL();

                        } catch (final MalformedURLException ignore) {
                            // no-op
                        }
                    }
                    final ConnectorModule connectorModule = createConnectorModule(appId, URLs.toFilePath(rarUrl), appClassLoader, stringURLEntry.getKey());
                    if (connectorModule != null) {
                        appModule.getConnectorModules().add(connectorModule);
                    }
                } catch (final OpenEJBException e) {
                    LOGGER.error("Unable to load RAR: " + appId + ", module: " + stringURLEntry.getKey() + ". Exception: " + e.getMessage(), e);
                }
            }

            // Web modules
            for (final Map.Entry<String, URL> stringURLEntry : webModules.entrySet()) {
                try {
                    final URL warUrl = stringURLEntry.getValue();
                    addWebModule(appModule, warUrl, appClassLoader, webContextRoots.get(stringURLEntry.getKey()), null);
                } catch (final OpenEJBException e) {
                    LOGGER.error("Unable to load WAR: " + appId + ", module: " + stringURLEntry.getKey() + ". Exception: " + e.getMessage(), e);
                }
            }


            addBeansXmls(appModule);

            // Persistence Units
            final Properties p = new Properties();
            p.put(appModule.getModuleId(), appModule.getJarLocation());
            final FileUtils base = new FileUtils(appModule.getModuleId(), appModule.getModuleId(), p);
            final List<URL> filteredUrls = new ArrayList<>();
            DeploymentsResolver.loadFromClasspath(base, filteredUrls, appModule.getClassLoader());
            addPersistenceUnits(appModule, filteredUrls.toArray(new URL[filteredUrls.size()]));

            final Object pXmls = appModule.getAltDDs().get("persistence.xml");

            for (final WebModule webModule : appModule.getWebModules()) {
                final List<URL> foundRootUrls = new ArrayList<>();
                final List<URL> scannableUrls = webModule.getScannableUrls();
                for (final URL url : scannableUrls) {
                    if (!addPersistenceUnits(appModule, url).isEmpty()) {
                        foundRootUrls.add(url);
                    }
                }

                if (pXmls != null && Collection.class.isInstance(pXmls)) {
                    final File webapp = webModule.getFile();
                    if (webapp == null) {
                        continue;
                    }
                    final String webappAbsolutePath = webapp.getAbsolutePath();

                    final Collection<URL> list = Collection.class.cast(pXmls);
                    for (final URL url : list) {
                        try {
                            final File file = URLs.toFile(url);
                            if (file.getAbsolutePath().startsWith(webappAbsolutePath)) {
                                foundRootUrls.add(url);
                            }
                        } catch (final IllegalArgumentException iae) {
                            // no-op
                        }
                    }
                }

                webModule.getAltDDs().put(EAR_WEBAPP_PERSISTENCE_XML_JARS, foundRootUrls);
            }

            for (final DeploymentModule module : appModule.getDeploymentModule()) {
                module.setStandaloneModule(false);
            }

            return appModule;

        } catch (final OpenEJBException e) {
            LOGGER.error("Unable to load EAR: " + jarPath, e);
            throw e;
        }
    }

    private void createApplicationFromFiles(final String appId, final ClassLoader tmpClassLoader, final Map<String, URL> ejbModules, final Map<String, URL> clientModules, final Map<String, URL> resouceModules, final Map<String, URL> webModules, final HashMap<String, URL> files) throws OpenEJBException {
        for (final Map.Entry<String, URL> entry : files.entrySet()) {
            // if (entry.getKey().startsWith("lib/")) continue;// will not be scanned since we don't get folder anymore
            if (!entry.getKey().matches(".*\\.(jar|war|rar|ear)")) {
                continue;
            }

            try {
                detectAndAddModuleToApplication(appId, tmpClassLoader, ejbModules, clientModules, resouceModules, webModules, entry);
            } catch (final UnsupportedOperationException | UnknownModuleTypeException e) {
                // Ignore it as per the javaee spec EE.8.4.2 section 1.d.iii
                LOGGER.info("Ignoring unknown module type: " + entry.getKey());
            } catch (final Exception e) {
                throw new OpenEJBException("Unable to determine the module type of " + entry.getKey() + ": Exception: " + e.getMessage(), e);
            }
        }
    }

    private void detectAndAddModuleToApplication(final String appId, final ClassLoader tmpClassLoader, final Map<String, URL> ejbModules, final Map<String, URL> clientModules, final Map<String, URL> resouceModules, final Map<String, URL> webModules, final Map.Entry<String, URL> entry) throws IOException, UnknownModuleTypeException {
        final ClassLoader moduleClassLoader = ClassLoaderUtil.createTempClassLoader(appId, new URL[]{entry.getValue()}, tmpClassLoader);

        final Class<? extends DeploymentModule> moduleType = discoverModuleType(entry.getValue(), moduleClassLoader, true);

        if (EjbModule.class.equals(moduleType)) {
            ejbModules.put(entry.getKey(), entry.getValue());
        } else if (ClientModule.class.equals(moduleType)) {
            clientModules.put(entry.getKey(), entry.getValue());
        } else if (ConnectorModule.class.equals(moduleType)) {
            resouceModules.put(entry.getKey(), entry.getValue());
        } else if (WebModule.class.equals(moduleType)) {
            webModules.put(entry.getKey(), entry.getValue());
        }
    }

    protected ClientModule createClientModule(final URL clientUrl, final String absolutePath, final ClassLoader appClassLoader, final String moduleName) throws OpenEJBException {
        return createClientModule(clientUrl, absolutePath, appClassLoader, moduleName, true);
    }

    protected ClientModule createClientModule(final URL clientUrl, final String absolutePath, final ClassLoader appClassLoader, final String moduleName, final boolean log) throws OpenEJBException {
        final ResourceFinder clientFinder = new ResourceFinder(clientUrl);

        URL manifestUrl = null;
        try {
            manifestUrl = clientFinder.find("META-INF/MANIFEST.MF");
        } catch (final IOException e) {
            //
        }

        String mainClass = null;
        if (manifestUrl != null) {
            try {
                final InputStream is = IO.read(manifestUrl);
                final Manifest manifest = new Manifest(is);
                mainClass = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            } catch (final IOException e) {
                throw new OpenEJBException("Unable to determine Main-Class defined in META-INF/MANIFEST.MF file", e);
            }
        }

//        if (mainClass == null) throw new IllegalStateException("No Main-Class defined in META-INF/MANIFEST.MF file");

        final Map<String, URL> descriptors = getDescriptors(clientFinder, log);

        ApplicationClient applicationClient = null;
        final URL clientXmlUrl = descriptors.get("application-client.xml");
        if (clientXmlUrl != null) {
            applicationClient = ReadDescriptors.readApplicationClient(clientXmlUrl);
        }

        final ClientModule clientModule = new ClientModule(applicationClient, appClassLoader, absolutePath, mainClass, moduleName);

        clientModule.getAltDDs().putAll(descriptors);
        if (absolutePath != null) {
            clientModule.getWatchedResources().add(absolutePath);
        }
        if (clientXmlUrl != null && "file".equals(clientXmlUrl.getProtocol())) {
            clientModule.getWatchedResources().add(URLs.toFilePath(clientXmlUrl));
        }
        return clientModule;
    }

    protected EjbModule createEjbModule(final URL baseUrl, final String jarPath, final ClassLoader classLoader) throws OpenEJBException {
        // read the ejb-jar.xml file
        Map<String, URL> descriptors;
        if (baseUrl != null) {
            descriptors = getDescriptors(baseUrl);
        } else {
            try {
                descriptors = getDescriptors(classLoader, null);
            } catch (final IOException e) {
                descriptors = new HashMap<>();
            }
        }

        EjbJar ejbJar = null;
        final URL ejbJarXmlUrl = descriptors.get("ejb-jar.xml");
        if (ejbJarXmlUrl != null) {
            try {
                ejbJar = ReadDescriptors.readEjbJar(ejbJarXmlUrl.openStream());
            } catch (final IOException e) {
                throw new OpenEJBException(e);
            }
        }

        // create the EJB Module
        final EjbModule ejbModule = new EjbModule(classLoader, null, jarPath, ejbJar, null);
        ejbModule.getAltDDs().putAll(descriptors);
        if (jarPath != null) {
            ejbModule.getWatchedResources().add(jarPath);
        }
        if (ejbJarXmlUrl != null && "file".equals(ejbJarXmlUrl.getProtocol())) {
            ejbModule.getWatchedResources().add(URLs.toFilePath(ejbJarXmlUrl));
        }

        ejbModule.setClientModule(createClientModule(baseUrl, jarPath, classLoader, null, false));

        // load webservices descriptor
        addWebservices(ejbModule);
        return ejbModule;
    }

    private WebModule createWebModule(final String jar, final URL warUrl, final ClassLoader parentClassLoader, final String contextRoot, final String moduleName, final ExternalConfiguration config) throws OpenEJBException {
        return createWebModule(jar, URLs.toFilePath(warUrl), parentClassLoader, contextRoot, moduleName, config);
    }

    public void addWebModule(final AppModule appModule, final URL warUrl, final ClassLoader parentClassLoader, final String contextRoot, final String moduleName) throws OpenEJBException {
        final WebModule webModule = createWebModule(appModule.getJarLocation(), URLs.toFilePath(warUrl), parentClassLoader, contextRoot, moduleName, null);
        addWebModule(webModule, appModule);
    }

    public static EjbModule addWebModule(final WebModule webModule, final AppModule appModule) throws OpenEJBException {
        // create and add the WebModule
        appModule.getWebModules().add(webModule);
        if (appModule.isStandaloneModule()) {
            appModule.getAdditionalLibraries().addAll(webModule.getUrls());
        }

        {
            final Object pXml = appModule.getAltDDs().get("persistence.xml");

            List<URL> persistenceXmls = pXml == null ? null : (List.class.isInstance(pXml) ? (List<URL>) pXml :
                    new ArrayList<>(Collections.singletonList(URL.class.cast(pXml))));
            if (persistenceXmls == null) {
                persistenceXmls = new ArrayList<>();
                appModule.getAltDDs().put("persistence.xml", persistenceXmls);
            }

            final Object o = webModule.getAltDDs().get("persistence.xml");

            if (o instanceof URL) {
                final URL url = (URL) o;
                persistenceXmls.add(url);
            }

            if (o instanceof List) {
                final List<URL> urls = (List<URL>) o;
                persistenceXmls.addAll(urls);
            }
        }


        // Per the Spec version of the Collapsed EAR there
        // aren't individual EjbModules inside a war.
        // The war itself is one big EjbModule if certain
        // conditions are met.  These conditions are different
        // than an ear file, so the ear-style code we were previously
        // using doesn't exactly work anymore.
        final EjbModule webEjbModule = new EjbModule(webModule.getClassLoader(), webModule.getModuleId(), webModule.getJarLocation(), null, null);
        webEjbModule.setWebapp(true);
        webEjbModule.getAltDDs().putAll(webModule.getAltDDs());
        appModule.getEjbModules().add(webEjbModule);

        try {
            // TODO:  Put our scanning ehnancements back, here
            fillEjbJar(webModule, webEjbModule);

            if (webModule.getFinder() == null) {
                if (isMetadataComplete(webModule, webEjbModule)) {
                    final IAnnotationFinder finder = new org.apache.xbean.finder.AnnotationFinder(new ClassesArchive());
                    webModule.setFinder(finder);
                    webEjbModule.setFinder(finder);
                } else {
                    final IAnnotationFinder finder = FinderFactory.createFinder(webModule);
                    webModule.setFinder(finder);
                    webEjbModule.setFinder(finder);
                }
            } else if (webEjbModule.getFinder() == null) {
                webEjbModule.setFinder(webModule.getFinder());
            }
        } catch (final Exception e) {
            throw new OpenEJBException("Unable to create annotation scanner for web module " + webModule.getModuleId(), e);
        }

        addWebservices(webEjbModule);

        return webEjbModule;
    }

    /**
     * If the web.xml is metadata-complete and there is no ejb-jar.xml
     * then per specification we use the web.xml metadata-complete setting
     * to imply the same for EJBs.
     *
     * @param webModule WebModule
     * @param ejbModule EjbModule
     */
    private static void fillEjbJar(final WebModule webModule, final EjbModule ejbModule) {
        final Object o = webModule.getAltDDs().get("ejb-jar.xml");
        if (o != null) {
            return;
        }
        if (ejbModule.getEjbJar() != null) {
            return;
        }

        final EjbJar ejbJar = new EjbJar();
        final WebApp webApp = webModule.getWebApp();

        ejbJar.setMetadataComplete(webApp.isMetadataComplete());

        ejbModule.setEjbJar(ejbJar);
    }

    private static boolean isMetadataComplete(final WebModule webModule, final EjbModule ejbModule) {
        if (webModule.getWebApp() == null) {
            return false;
        }
        if (!webModule.getWebApp().isMetadataComplete()) {
            return false;
        }

        // At this point we know the web.xml is metadata-complete
        // We need to determine if there are cdi or ejb xml files
        if (webModule.getAltDDs().get("beans.xml") == null) {
            return true;
        }
        if (ejbModule.getEjbJar() == null) {
            return true;
        }
        return ejbModule.getEjbJar().isMetadataComplete();

    }

    public WebModule createWebModule(final String appId, final String warPath, final ClassLoader parentClassLoader, final String contextRoot, final String moduleName, final ExternalConfiguration config) throws OpenEJBException {
        File warFile = new File(warPath);
        if (!warFile.isDirectory()) {
            warFile = unpack(warFile);
        }

        // read web.xml file
        final Map<String, URL> descriptors;
        try {
            descriptors = getWebDescriptors(warFile);
        } catch (final IOException e) {
            throw new OpenEJBException("Unable to collect descriptors in web module: " + contextRoot, e);
        }

        final WebApp webApp;
        final URL webXmlUrl = descriptors.get("web.xml");
        if (webXmlUrl != null) {
            webApp = ReadDescriptors.readWebApp(webXmlUrl);
        } else {
            // no web.xml webapp - possible since Servlet 3.0
            webApp = new WebApp();
        }

        // determine war class path

        ensureContainerUrls();
        final List<URL> webUrls = new ArrayList<>(containerUrls);

        final SystemInstance systemInstance = SystemInstance.get();

        // add these urls first to ensure we load classes from here first
        final String externalRepos = systemInstance.getProperty("tomee." + warFile.getName().replace(".war", "") + ".externalRepositories");
        List<URL> externalUrls = null;
        if (externalRepos != null) {
            externalUrls = new ArrayList<>();
            for (final String additional : externalRepos.split(",")) {
                final String trim = additional.trim();
                if (!trim.isEmpty()) {
                    try {
                        externalUrls.add(new File(trim).toURI().toURL());
                    } catch (final MalformedURLException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
            webUrls.addAll(externalUrls);
        }

        final Map<String, URL[]> urls = getWebappUrlsAndRars(warFile);
        webUrls.addAll(Arrays.asList(urls.get(URLS_KEY)));

        final List<URL> addedUrls = new ArrayList<>();
        for (final URL url : urls.get(RAR_URLS_KEY)) { // eager unpack to be able to use it in classloader
            final File[] files = unpack(URLs.toFile(url)).listFiles();
            if (files != null) {
                for (final File f : files) {
                    if (f.getName().endsWith(".jar")) {
                        try {
                            addedUrls.add(f.toURI().toURL());
                        } catch (final MalformedURLException e) {
                            LOGGER.warning("War path bad: " + f.getAbsolutePath(), e);
                        }
                    }
                }
            }
        }
        webUrls.addAll(addedUrls);

        // context.xml can define some additional libraries
        if (config != null) { // we don't test all !=null inline to show that config will get extra params in the future and that it is hierarchic
            if (config.getClasspath() != null && config.getClasspath().length > 0) {
                final Set<URL> contextXmlUrls = new LinkedHashSet<>();
                for (final String location : config.getClasspath()) {
                    try {
                        webUrls.add(new File(location).toURI().toURL());
                    } catch (final MalformedURLException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
                webUrls.addAll(contextXmlUrls);
            }
        }

        final ClassLoaderConfigurer configurer = QuickJarsTxtParser.parse(new File(warFile, "WEB-INF/" + QuickJarsTxtParser.FILE_NAME));
        if (configurer != null) {
            ClassLoaderConfigurer.Helper.configure(webUrls, configurer);
        }

        final URL[] webUrlsArray = webUrls.toArray(new URL[webUrls.size()]);

        // in TomEE this is done in init hook since we don't manage tomee webapp classloader
        // so here is not the best idea for tomee
        // if we want to manage it in a generic way
        // simply add a boolean shared between tomcat and openejb world
        // to know if we should fire it or not
        systemInstance.fireEvent(new BeforeDeploymentEvent(webUrlsArray, parentClassLoader));

        final ClassLoader warClassLoader = ClassLoaderUtil.createTempClassLoader(appId, webUrlsArray, parentClassLoader);

        // create web module
        final List<URL> scannableUrls = filterWebappUrls(webUrlsArray, config == null ? null : config.customerFilter, descriptors.get(NewLoaderLogic.EXCLUSION_FILE));
        // executable war will add war in scannable urls, we don't want it since it will surely contain tomee, cxf, ...
        if (Boolean.parseBoolean(systemInstance.getProperty("openejb.core.skip-war-in-loader", "true"))) {
            File archive = warFile;
            if (!archive.getName().endsWith(".war")) {
                archive = new File(warFile.getParentFile(), warFile.getName() + ".war");
                final String unpackDir = systemInstance.getProperty("tomee.unpack.dir");
                if (unpackDir != null && !archive.isFile()) {
                    try {
                        archive = new File(systemInstance.getBase().getDirectory(unpackDir, false), warFile.getName());
                    } catch (final IOException e) {
                        // no-op
                    }
                }
            }
            if (archive.isFile()) {
                try {
                    scannableUrls.remove(archive.toURI().toURL());
                } catch (final MalformedURLException e) {
                    // no-op
                }
            }
        }
        if (externalUrls != null) {
            for (final URL url : externalUrls) {
                if (scannableUrls.contains(url)) {
                    scannableUrls.remove(url);
                    scannableUrls.add(0, url);
                }
            }
        }

        SystemInstance.get().fireEvent(new EnhanceScannableUrlsEvent(scannableUrls));

        final WebModule webModule = new WebModule(webApp, contextRoot, warClassLoader, warFile.getAbsolutePath(), moduleName);
        webModule.setUrls(webUrls);
        webModule.setAddedUrls(addedUrls);
        webModule.setRarUrls(Arrays.asList(urls.get(RAR_URLS_KEY)));
        webModule.setScannableUrls(scannableUrls);
        webModule.setDefaultContextPath(webApp.getDefaultContextPath());
        webModule.getAltDDs().putAll(descriptors);
        webModule.getWatchedResources().add(warPath);
        webModule.getWatchedResources().add(warFile.getAbsolutePath());
        if (webXmlUrl != null && "file".equals(webXmlUrl.getProtocol())) {
            webModule.getWatchedResources().add(URLs.toFilePath(webXmlUrl));
        }

        //If webModule object is loaded by ejbModule or persitenceModule, no need to load tag libraries, web service and JSF related staffs.
        addTagLibraries(webModule);

        // load webservices descriptor
        addWebservices(webModule);

        // load faces configuration files
        addFacesConfigs(webModule);

        addBeansXmls(webModule);

        return webModule;
    }

    private void ensureContainerUrls() {
        if (containerUrls == null) {
            if ("true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.scan.webapp.container", "false"))) {
                synchronized (this) {
                    if (containerUrls == null) {
                        try {
                            UrlSet urlSet = new UrlSet(ParentClassLoaderFinder.Helper.get());
                            urlSet = URLs.cullSystemJars(urlSet);
                            urlSet = NewLoaderLogic.applyBuiltinExcludes(urlSet);
                            containerUrls = urlSet.getUrls();

                            final boolean skipContainerFolders = "true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.scan.webapp.container.skip-folder", "true"));
                            final Iterator<URL> it = containerUrls.iterator();
                            while (it.hasNext()) { // remove lib/
                                final File file = URLs.toFile(it.next());
                                // TODO: see if websocket should be added in default.exclusions
                                final String name = file.getName();
                                if ((skipContainerFolders && file.isDirectory())
                                        // few hardcoded exclusions, TODO: see if we should filter them in previous call of applyBuiltinExcludes()
                                        || name.endsWith("tomcat-websocket.jar")
                                        || name.startsWith("commons-jcs-")
                                        || name.startsWith("xx-arquillian-tomee")
                                        || ("lib".equals(name) && file.isDirectory() &&
                                            new File(JavaSecurityManagers.getSystemProperty("openejb.base", "-")).equals(file.getParentFile()))) {
                                    it.remove();
                                }
                            }
                        } catch (final Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }
            } else {
                containerUrls = new ArrayList<>();
            }
        }
    }

    public static List<URL> filterWebappUrls(final URL[] webUrls, final Filter filter, final URL exclusions) {
        Filter excludeFilter = null;
        if (exclusions != null) {
            try {
                final String[] prefixes = NewLoaderLogic.readInputStreamList(exclusions.openStream());
                excludeFilter = Filters.prefixes(prefixes);
            } catch (final IOException e) {
                LOGGER.warning("can't read " + exclusions.toExternalForm());
            }
        }

        UrlSet urls = new UrlSet(webUrls);
        try {
            urls = NewLoaderLogic.applyBuiltinExcludes(urls, filter, excludeFilter);
        } catch (final MalformedURLException e) {
            return Arrays.asList(webUrls);
        }
        return urls.getUrls();
    }

    public static void addBeansXmls(final WebModule webModule) {
        final List<URL> urls = webModule.getScannableUrls();
        // parent returns nothing when calling getresources because we don't want here to be fooled by maven classloader
        final URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[urls.size()]), new EmptyResourcesClassLoader());

        final List<URL> xmls = new LinkedList<>();
        try {
            final URL e = (URL) webModule.getAltDDs().get("beans.xml");
            if (e != null) { // first!
                xmls.add(e);
            }
            xmls.addAll(Collections.list(loader.getResources("META-INF/beans.xml")));
        } catch (final IOException e) {
            return;
        }

        final CompositeBeans complete = new CompositeBeans();
        for (final URL url : xmls) {
            if (url == null) {
                continue;
            }
            mergeBeansXml(complete, url);
        }
        if (!complete.getDiscoveryByUrl().isEmpty()) {
            complete.removeDuplicates();
        }
        webModule.getAltDDs().put("beans.xml", complete);
    }

    private static Beans mergeBeansXml(final CompositeBeans current, final URL url) {
        try {
            final Beans beans;
            try {
                beans = ReadDescriptors.readBeans(url.openStream());
            } catch (final IOException e) {
                return current;
            }

            doMerge(url, current, beans);
        } catch (final OpenEJBException e) {
            LOGGER.error("Unable to read beans.xml from: " + url.toExternalForm(), e);
        }
        return current;
    }

    public static void doMerge(final URL url, final CompositeBeans current, final Beans beans) {
        current.mergeClasses(url, beans);
        current.getScan().getExclude().addAll(beans.getScan().getExclude());

        // check is done here since later we lost the data of the origin
        ReadDescriptors.checkDuplicatedByBeansXml(beans, current);

        String beanDiscoveryMode = beans.getBeanDiscoveryMode();
        if (beanDiscoveryMode == null) {
            beanDiscoveryMode = "ALL";
        }
        else if ("ALL".equalsIgnoreCase(beanDiscoveryMode) && beans.isTrim()) {
            beanDiscoveryMode = "TRIM";
        }

        current.getDiscoveryByUrl().put(url, beanDiscoveryMode);
    }

    private void addBeansXmls(final AppModule appModule) {
        final List<URL> urls = appModule.getAdditionalLibraries();
        final URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[urls.size()]));

        final ArrayList<URL> xmls;
        try {
            xmls = Collections.list(loader.getResources("META-INF/beans.xml"));
        } catch (final IOException e) {

            return;
        }

        final CompositeBeans complete = new CompositeBeans();
        for (final URL url : xmls) {
            if (url == null) {
                continue;
            }
            mergeBeansXml(complete, url);
        }

        if (complete.getDiscoveryByUrl().isEmpty()) {
            return;
        }
        complete.removeDuplicates();

        ensureContainerUrls();
        final List<URL> scannableUrls = new ArrayList<>(this.containerUrls);
        SystemInstance.get().fireEvent(new EnhanceScannableUrlsEvent(scannableUrls));
        appModule.getScannableContainerUrls().addAll(scannableUrls);

        IAnnotationFinder finder;
        try {
            finder = FinderFactory.createFinder(appModule);
        } catch (final Exception e) {
            finder = new FinderFactory.ModuleLimitedFinder(new FinderFactory.OpenEJBAnnotationFinder(new WebappAggregatedArchive(appModule.getClassLoader(), appModule.getAltDDs(), xmls)));
        }
        appModule.setEarLibFinder(finder);

        final EjbModule ejbModule = new EjbModule(appModule.getClassLoader(), EAR_SCOPED_CDI_BEANS + appModule.getModuleId(), new EjbJar(), new OpenejbJar());
        ejbModule.setBeans(complete);
        ejbModule.setFinder(finder);
        ejbModule.setEjbJar(new EmptyEjbJar());

        appModule.getEjbModules().add(ejbModule);
    }

    protected String getContextRoot() {
        return null;
    }

    protected String getModuleName() {
        return null;
    }

    public static Map<String, URL[]> getWebappUrlsAndRars(final File warFile) {
        final Set<URL> webClassPath = new HashSet<>();
        final Set<URL> webRars = new HashSet<>();
        final File webInfDir = new File(warFile, "WEB-INF");
        try {
            webClassPath.add(new File(webInfDir, "classes").toURI().toURL());
        } catch (final MalformedURLException e) {
            LOGGER.warning("War path bad: " + new File(webInfDir, "classes"), e);
        }

        final File libDir = new File(webInfDir, "lib");
        if (libDir.exists()) {
            final File[] list = libDir.listFiles();
            if (list != null) {
                for (final File file : list) {
                    final String name = file.getName();
                    if (name.endsWith(".jar") || name.endsWith(".zip")) {
                        try {
                            webClassPath.add(file.toURI().toURL());
                        } catch (final MalformedURLException e) {
                            LOGGER.warning("War path bad: " + file, e);
                        }
                    } else if (name.endsWith(".rar")) {
                        try {
                            webRars.add(file.toURI().toURL());
                        } catch (final MalformedURLException e) {
                            LOGGER.warning("War path bad: " + file, e);
                        }
                    }
                }
            }
        }

        final WebAppEnricher enricher = SystemInstance.get().getComponent(WebAppEnricher.class);
        if (enricher != null) {
            webClassPath.addAll(Arrays.asList(enricher.enrichment(null)));
        }

        // create the class loader
        final Map<String, URL[]> urls = new HashMap<>();
        urls.put(URLS_KEY, webClassPath.toArray(new URL[webClassPath.size()]));
        urls.put(RAR_URLS_KEY, webRars.toArray(new URL[webRars.size()]));
        return urls;
    }

    public static URL[] getWebappUrls(final File warFile) {
        return getWebappUrlsAndRars(warFile).get("urls");
    }

    private static void addWebservices(final WsModule wsModule) throws OpenEJBException {
        final boolean webservicesEnabled = SystemInstance.get().getOptions().get(ConfigurationFactory.WEBSERVICES_ENABLED, true);
        if (!webservicesEnabled) {
            wsModule.getAltDDs().remove("webservices.xml");
            wsModule.setWebservices(null); // should be null already, but just for good measure
            return;
        }

        // get location of webservices.xml file
        final Object webservicesObject = wsModule.getAltDDs().get("webservices.xml");
        if (webservicesObject == null || !(webservicesObject instanceof URL)) {
            return;
        }
        final URL webservicesUrl = (URL) webservicesObject;

        // determine the base url for this module (either file: or jar:)
        URL moduleUrl;
        try {
            final File jarFile = new File(wsModule.getJarLocation());
            moduleUrl = jarFile.toURI().toURL();
            if (jarFile.isFile()) {
                moduleUrl = new URL("jar", "", -1, moduleUrl + "!/");
            }
        } catch (final MalformedURLException e) {
            LOGGER.warning("Invalid module location " + wsModule.getJarLocation());
            return;
        }

        // parse the webservices.xml file
        final Map<URL, JavaWsdlMapping> jaxrpcMappingCache = new HashMap<>();
        final Webservices webservices = ReadDescriptors.readWebservices(webservicesUrl);
        wsModule.setWebservices(webservices);
        if ("file".equals(webservicesUrl.getProtocol())) {
            wsModule.getWatchedResources().add(URLs.toFilePath(webservicesUrl));
        }

        // parse any jaxrpc-mapping-files mentioned in the webservices.xml file
        for (final WebserviceDescription webserviceDescription : webservices.getWebserviceDescription()) {
            final String jaxrpcMappingFile = webserviceDescription.getJaxrpcMappingFile();
            if (jaxrpcMappingFile != null) {
                final URL jaxrpcMappingUrl;
                try {
                    jaxrpcMappingUrl = new URL(moduleUrl, jaxrpcMappingFile);
                    JavaWsdlMapping jaxrpcMapping = jaxrpcMappingCache.get(jaxrpcMappingUrl);
                    if (jaxrpcMapping == null) {
                        jaxrpcMapping = ReadDescriptors.readJaxrpcMapping(jaxrpcMappingUrl);
                        jaxrpcMappingCache.put(jaxrpcMappingUrl, jaxrpcMapping);
                    }
                    webserviceDescription.setJaxrpcMapping(jaxrpcMapping);
                    if ("file".equals(jaxrpcMappingUrl.getProtocol())) {
                        wsModule.getWatchedResources().add(URLs.toFilePath(jaxrpcMappingUrl));
                    }
                } catch (final MalformedURLException e) {
                    LOGGER.warning("Invalid jaxrpc-mapping-file location " + jaxrpcMappingFile);
                }
            }
        }

    }

    private void addTagLibraries(final WebModule webModule) throws OpenEJBException {
        final Set<URL> tldLocations = new HashSet<>();

        // web.xml contains tag lib locations in nested jsp config elements
        final File warFile = new File(webModule.getJarLocation());
        final WebApp webApp = webModule.getWebApp();
        if (webApp != null) {
            for (final JspConfig jspConfig : webApp.getJspConfig()) {
                for (final Taglib taglib : jspConfig.getTaglib()) {
                    String location = taglib.getTaglibLocation();
                    if (!location.startsWith("/")) {
                        // this reproduces a tomcat bug
                        location = "/WEB-INF/" + location;
                    }
                    try {
                        final File file = new File(warFile, location).getCanonicalFile().getAbsoluteFile();
                        tldLocations.addAll(TldScanner.scanForTagLibs(file));
                    } catch (final IOException e) {
                        LOGGER.warning("JSP tag library location bad: " + location, e);
                    }
                }
            }
        }

        // WEB-INF/**/*.tld except in WEB-INF/classes and WEB-INF/lib
        Set<URL> urls = TldScanner.scanWarForTagLibs(warFile);
        tldLocations.addAll(urls);

        // Search all libs
        final ClassLoader parentClassLoader = webModule.getClassLoader().getParent();
        urls = TldScanner.scan(parentClassLoader);
        tldLocations.addAll(urls);

        // load the tld files
        for (final URL location : tldLocations) {
            final TldTaglib taglib = ReadDescriptors.readTldTaglib(location);
            if (taglib != null && taglib != ReadDescriptors.SKIP_TAGLIB) {
                webModule.getTaglibs().add(taglib);
                if ("file".equals(location.getProtocol())) {
                    webModule.getWatchedResources().add(URLs.toFilePath(location));
                }
            }
        }

        // no more need + this classloader is a temp one in Servlet container so avoid mem leaks
        TldScanner.quickClean(parentClassLoader);
    }

    /**
     * Finds all faces configuration files and stores them in the WebModule
     *
     * @param webModule WebModule
     * @throws OpenEJBException
     */
    private void addFacesConfigs(final WebModule webModule) throws OpenEJBException {
        //*************************IMPORTANT*******************************************
        // TODO : kmalhi :: Add support to scrape META-INF/faces-config.xml in jar files
        // look at section 10.4.2 of the JSF v1.2 spec, bullet 1 for details
        final Set<URL> facesConfigLocations = new HashSet<>();

        // web.xml contains faces config locations in the context parameter jakarta.faces.CONFIG_FILES
        final File warFile = new File(webModule.getJarLocation());
        final WebApp webApp = webModule.getWebApp();
        if (webApp != null) {
            final String foundContextParam = webApp.contextParamsAsMap().get("jakarta.faces.CONFIG_FILES");
            if (foundContextParam != null) {
                // the value is a comma separated list of config files
                final String commaDelimitedListOfFiles = foundContextParam.trim();
                final String[] configFiles = commaDelimitedListOfFiles.split(",");
                // trim any extra spaces in each file
                final String[] trimmedConfigFiles = new String[configFiles.length];
                for (int i = 0; i < configFiles.length; i++) {
                    trimmedConfigFiles[i] = configFiles[i].trim();
                }
                // convert each file to a URL and add it to facesConfigLocations
                for (final String location : trimmedConfigFiles) {
                    if (!location.startsWith("/")) {
                        LOGGER.error("A faces configuration file should be context relative when specified in web.xml. Please fix the value of context parameter jakarta.faces.CONFIG_FILES for the file " + location);
                    }
                    try {
                        final File file = new File(warFile, location).getCanonicalFile().getAbsoluteFile();
                        final URL url = file.toURI().toURL();
                        facesConfigLocations.add(url);

                    } catch (final IOException e) {
                        LOGGER.error("Faces configuration file location bad: " + location, e);
                    }
                }
            } else {
                LOGGER.debug("faces config file is null");
            }
        }

        // Search for WEB-INF/faces-config.xml
        final File webInf = new File(warFile, "WEB-INF");
        if (webInf.isDirectory()) {
            File facesConfigFile = new File(webInf, "faces-config.xml");
            if (facesConfigFile.exists()) {
                try {
                    facesConfigFile = facesConfigFile.getCanonicalFile().getAbsoluteFile();
                    final URL url = facesConfigFile.toURI().toURL();
                    facesConfigLocations.add(url);
                } catch (final IOException e) {
                    // TODO: kmalhi:: Remove the printStackTrace after testing
                    e.printStackTrace();
                }
            }
        }
        // load the faces configuration files
        // TODO:kmalhi:: Its good to have separate FacesConfig objects for multiple configuration files, but what if there is a conflict where the same
        // managebean is declared in two different files, which one wins? -- check the jsf spec, Hopefully JSF should be able to check for this and
        // flag an error and not allow the application to be deployed.
        for (final URL location : facesConfigLocations) {
            final FacesConfig facesConfig = ReadDescriptors.readFacesConfig(location);
            webModule.getFacesConfigs().add(facesConfig);
            if ("file".equals(location.getProtocol())) {
                webModule.getWatchedResources().add(URLs.toFilePath(location));
            }
        }
    }

    protected static ConnectorModule createConnectorModule(final String appId, final String rarPath, final ClassLoader parentClassLoader, final String moduleId) throws OpenEJBException {
        return createConnectorModule(appId, rarPath, parentClassLoader, moduleId, null);
    }

    protected static ConnectorModule createConnectorModule(final String appId, final String rarPath, final ClassLoader parentClassLoader, final String moduleId, final URL raXmlUrl) throws OpenEJBException {
        final URL baseUrl;// unpack the rar file
        File rarFile = new File(rarPath);
        if (!rarFile.exists()) {
            LOGGER.warning(rarPath + " doesn't exist, skipping connector");
            return null;
        }
        rarFile = unpack(rarFile);
        baseUrl = getFileUrl(rarFile);

        // read the ra.xml file
        final Map<String, URL> descriptors = getDescriptors(baseUrl);
        Connector connector = null;
        URL rarXmlUrl = descriptors.get("ra.xml");
        if (rarXmlUrl == null && raXmlUrl != null) {
            descriptors.put("ra.xml", raXmlUrl);
            rarXmlUrl = raXmlUrl;
        }
        if (rarXmlUrl != null) {
            connector = ReadDescriptors.readConnector(rarXmlUrl);
        }

        // find the nested jar files
        final HashMap<String, URL> rarLibs = new HashMap<>();
        scanDir(rarFile, rarLibs, "");
        // remove all non jars from the rarLibs
        rarLibs.entrySet().removeIf(fileEntry -> !fileEntry.getKey().endsWith(".jar"));

        // create the class loader
        final List<URL> classPath = new ArrayList<>(rarLibs.values());

        final ClassLoaderConfigurer configurer = QuickJarsTxtParser.parse(new File(rarFile, "META-INF/" + QuickJarsTxtParser.FILE_NAME));
        if (configurer != null) {
            ClassLoaderConfigurer.Helper.configure(classPath, configurer);
        }

        final URL[] urls = classPath.toArray(new URL[classPath.size()]);
        final ClassLoader appClassLoader = ClassLoaderUtil.createTempClassLoader(appId, urls, parentClassLoader);

        // create the Resource Module
        final ConnectorModule connectorModule = new ConnectorModule(connector, appClassLoader, rarPath, moduleId);
        connectorModule.getAltDDs().putAll(descriptors);
        connectorModule.getLibraries().addAll(classPath);
        connectorModule.getWatchedResources().add(rarPath);
        connectorModule.getWatchedResources().add(rarFile.getAbsolutePath());
        if (rarXmlUrl != null && "file".equals(rarXmlUrl.getProtocol())) {
            connectorModule.getWatchedResources().add(URLs.toFilePath(rarXmlUrl));
        }

        return connectorModule;
    }

    protected static void addWebFragments(final WebModule webModule, final Collection<URL> urls) throws OpenEJBException {
        if (urls == null) {
            return;
        }

        List<URL> webFragmentUrls;
        try {
            webFragmentUrls = (List<URL>) webModule.getAltDDs().get(WEB_FRAGMENT_XML);
        } catch (final ClassCastException e) {
            final Object value = webModule.getAltDDs().get(WEB_FRAGMENT_XML);
            webFragmentUrls = new ArrayList<>();
            webFragmentUrls.add(URL.class.cast(value));
            webModule.getAltDDs().put(WEB_FRAGMENT_XML, webFragmentUrls);
        }
        if (webFragmentUrls == null) {
            webFragmentUrls = new ArrayList<>();
            webModule.getAltDDs().put(WEB_FRAGMENT_XML, webFragmentUrls);
        }


        for (final URL url : urls) {
            final ResourceFinder finder = new ResourceFinder("", webModule.getClassLoader(), url);
            final Map<String, URL> descriptors = getDescriptors(finder, false);

            if (descriptors.containsKey(WEB_FRAGMENT_XML)) {
                final URL descriptor = descriptors.get(WEB_FRAGMENT_XML);
                if (webFragmentUrls.contains(descriptor)) {
                    continue;
                }

                final String urlString = descriptor.toExternalForm();
                if (!urlString.contains("META-INF/" + WEB_FRAGMENT_XML)) {
                    LOGGER.info("AltDD persistence.xml -> " + urlString);
                }

                webFragmentUrls.add(descriptor);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    protected static Collection<URL> addPersistenceUnits(final AppModule appModule, final URL... urls) throws OpenEJBException {
        final Collection<URL> added = new ArrayList<>();

        // OPENEJB-1059: Anything in the appModule.getAltDDs() map has already been
        // processed by the altdd code, so anything in here should not cause OPENEJB-1059
        List<URL> persistenceUrls;
        try {
            persistenceUrls = (List<URL>) appModule.getAltDDs().get("persistence.xml");
        } catch (final ClassCastException e) {
            //That happens when we are trying to deploy an ear file.
            //lets try to get a single object instead
            final Object value = appModule.getAltDDs().get("persistence.xml");

            persistenceUrls = new ArrayList<>();
            persistenceUrls.add(URL.class.cast(value));
            added.add(persistenceUrls.iterator().next());

            appModule.getAltDDs().put("persistence.xml", persistenceUrls);
        }
        if (persistenceUrls == null) {
            persistenceUrls = new ArrayList<>();
            appModule.getAltDDs().put("persistence.xml", persistenceUrls);
        }

        List<URL> persistenceFragmentsUrls = (List<URL>) appModule.getAltDDs().get("persistence-fragment.xml");
        if (persistenceFragmentsUrls == null) {
            persistenceFragmentsUrls = new ArrayList<>();
            appModule.getAltDDs().put("persistence-fragment.xml", persistenceFragmentsUrls);
        }


        for (final URL url : urls) {
            // OPENEJB-1059: looking for an altdd persistence.xml file in all urls
            // delegates to xbean finder for going throughout the list
            final ResourceFinder finder = new ResourceFinder("", appModule.getClassLoader(), url);
            final Map<String, URL> descriptors = getDescriptors(finder, false);

            // if a persistence.xml has been found, just pull it to the list
            if (descriptors.containsKey("persistence.xml")) {
                final URL descriptor = descriptors.get("persistence.xml");

                // don't add it if already present
                if (persistenceUrls.contains(descriptor)) {
                    continue;
                }

                // log if it is an altdd
                final String urlString = descriptor.toExternalForm();
                if (!urlString.contains("META-INF/persistence.xml")) {
                    LOGGER.info("AltDD persistence.xml -> " + urlString);
                }

                persistenceUrls.add(descriptor);
                added.add(descriptor);
            }
        }

        // look for persistence-fragment.xml
        for (final URL url : urls) {
            // OPENEJB-1059: looking for an altdd persistence.xml file in all urls
            // delegates to xbean finder for going throughout the list
            final ResourceFinder finder = new ResourceFinder("", appModule.getClassLoader(), url);
            final Map<String, URL> descriptors = getDescriptors(finder, false);

            // if a persistence.xml has been found, just pull it to the list
            if (descriptors.containsKey("persistence-fragment.xml")) {
                final URL descriptor = descriptors.get("persistence-fragment.xml");

                if (persistenceFragmentsUrls.contains(descriptor)) {
                    continue;
                }

                // log if it is an altdd
                final String urlString = descriptor.toExternalForm();
                if (!urlString.contains("META-INF/persistence-fragment.xml")) {
                    LOGGER.info("AltDD persistence-fragment.xml -> " + urlString);
                }

                persistenceFragmentsUrls.add(descriptor);
                added.add(descriptor);
            }
        }

        return added;
    }

    public static Map<String, URL> getDescriptors(final URL moduleUrl) throws OpenEJBException {

        final ResourceFinder finder = new ResourceFinder(moduleUrl);
        return getDescriptors(finder);
    }

    private static Map<String, URL> getDescriptors(final ResourceFinder finder) throws OpenEJBException {
        return getDescriptors(finder, true);
    }

    private static Map<String, URL> getDescriptors(final ResourceFinder finder, final boolean log) throws OpenEJBException {
        try {

            return altDDSources(mapDescriptors(finder), log);

        } catch (final IOException e) {
            throw new OpenEJBException("Unable to determine descriptors in jar.", e);
        }
    }

    public static Map<String, URL> mapDescriptors(final ResourceFinder finder)
        throws IOException {
        final Map<String, URL> map = finder.getResourcesMap(META_INF);

        if (map.size() == 0) {

            for (final String descriptor : KNOWN_DESCRIPTORS) {

                final URL url = finder.getResource(META_INF + descriptor);
                if (url != null) {
                    map.put(descriptor, url);
                }
            }

        }
        return map;
    }

    /**
     * Modifies the map passed in with all the alt dd URLs found
     *
     * @param map Map
     * @param log boolean
     * @return the same map instance updated with alt dds
     */
    public static Map<String, URL> altDDSources(final Map<String, URL> map, final boolean log) {
        if (ALTDD == null || ALTDD.length() <= 0) {
            return map;
        }

        final List<String> list = new ArrayList<>(Arrays.asList(ALTDD.split(",")));
        Collections.reverse(list);

        final Map<String, URL> alts = new HashMap<>();

        for (String prefix : list) {
            prefix = prefix.trim();
            if (!prefix.matches(".*[.-]$")) {
                prefix += ".";
            }

            for (final Map.Entry<String, URL> entry : new HashMap<>(map).entrySet()) {
                String key = entry.getKey();
                final URL value = entry.getValue();
                if (key.startsWith(prefix)) {
                    key = key.substring(prefix.length());

                    alts.put(key, value);
                }
            }
        }

        for (final Map.Entry<String, URL> alt : alts.entrySet()) {
            final String key = alt.getKey();
            final URL value = alt.getValue();

            // don't add and log if the same key/value is already in the map
            if (value.equals(map.get(key))) {
                continue;
            }

            if (log) {
                LOGGER.info("AltDD " + key + " -> " + value.toExternalForm());
            }
            map.put(key, value);
        }

        return map;
    }

    public static Map<String, URL> getWebDescriptors(final File warFile) throws IOException {
        final Map<String, URL> descriptors = new TreeMap<>();

        // xbean resource finder has a bug when you use any uri but "META-INF"
        // and the jar file does not contain a directory entry for the uri

        if (warFile.isFile()) { // only to discover module type so xml file filtering is enough
            final URL jarURL = new URL("jar", "", -1, warFile.toURI().toURL() + "!/");
            try (JarFile jarFile = new JarFile(warFile)) {
                for (final JarEntry entry : Collections.list(jarFile.entries())) {
                    final String entryName = entry.getName();
                    if (!entry.isDirectory() && entryName.startsWith("WEB-INF/")
                        && (KNOWN_DESCRIPTORS.contains(entryName.substring("WEB-INF/".length())) || entryName.endsWith(".xml"))) { // + web.xml, web-fragment.xml...
                        descriptors.put(entryName, new URL(jarURL, entry.getName()));
                    }
                }
            } catch (final IOException e) {
                // most likely an invalid jar file
            }
        } else if (warFile.isDirectory()) {
            final File webInfDir = new File(warFile, "WEB-INF");
            if (webInfDir.isDirectory()) {
                final File[] files = webInfDir.listFiles();
                if (files != null) {
                    for (final File file : files) {
                        if (!file.isDirectory()) {
                            descriptors.put(file.getName(), file.toURI().toURL());
                        }
                    }
                }
            }

            // handle some few file(s) which can be in META-INF too
            final File webAppDdDir = new File(webInfDir, "classes/" + META_INF);
            if (webAppDdDir.isDirectory()) {
                final File[] files = webAppDdDir.listFiles();
                if (files != null) {
                    for (final File file : files) {
                        final String name = file.getName();
                        if (!descriptors.containsKey(name)) {
                            descriptors.put(name, file.toURI().toURL());
                        } else {
                            LOGGER.warning("Can't have a " + name + " in WEB-INF and WEB-INF/classes/META-INF, second will be ignored");
                        }
                    }
                }
            }
        }

        return descriptors;
    }

    protected File getFile(final URL warUrl) {
        if ("jar".equals(warUrl.getProtocol())) {
            String pathname = warUrl.getPath();

            // we only support file based jar urls
            if (!pathname.startsWith("file:")) {
                return null;
            }

            // strip off "file:"
            pathname = pathname.substring("file:".length());

            // file path has trailing !/ that must be stripped off
            pathname = pathname.substring(0, pathname.lastIndexOf('!'));

            try {
                pathname = URLDecoder.decode(pathname, "UTF-8");
            } catch (final Exception e) {
                //noinspection deprecation
                pathname = URLDecoder.decode(pathname);
            }
            return new File(pathname);
        } else if ("file".equals(warUrl.getProtocol())) {
            final String pathname = warUrl.getPath();
            try {
                return new File(URLDecoder.decode(pathname, "UTF-8"));
            } catch (final UnsupportedEncodingException e) {
                //noinspection deprecation
                return new File(URLDecoder.decode(pathname));
            }
        } else {
            return null;
        }
    }

    @SuppressWarnings({"unchecked"})
    public static Application unmarshal(final URL url) throws OpenEJBException {
        try {
            return ApplicationXml.unmarshal(url);
        } catch (final Exception e) {
            throw new OpenEJBException("Encountered error parsing the application.xml file: " + url.toExternalForm(), e);
        }
    }

    public static void scanDir(final File dir, final Map<String, URL> files, final String path) {
        scanDir(dir, files, path, true);
    }

    public static void scanDir(final File dir, final Map<String, URL> files, final String path, final boolean recursive) {
        final File[] dirFiles = dir.listFiles();
        if (dirFiles != null) {
            for (final File file : dirFiles) {
                if (file.isDirectory()) {
                    if (DeploymentsResolver.isExtractedDir(file)) {
                        continue;
                    }

                    if (recursive) {
                        scanDir(file, files, path + file.getName() + "/");
                    }
                } else {
                    final String name = file.getName();
                    try {
                        files.put(path + name, file.toURI().toURL());
                    } catch (final MalformedURLException e) {
                        LOGGER.warning("EAR path bad: " + path + name, e);
                    }
                }
            }
        }
    }

    public Class<? extends DeploymentModule> discoverModuleType(final URL baseUrl, final ClassLoader classLoader, final boolean searchForDescriptorlessApplications) throws IOException, UnknownModuleTypeException {
        final Set<RequireDescriptors> search = EnumSet.noneOf(RequireDescriptors.class);

        if (!searchForDescriptorlessApplications) {
            search.addAll(Arrays.asList(RequireDescriptors.values()));
        }

        return discoverModuleType(baseUrl, classLoader, search);
    }

    @SuppressWarnings("unchecked")
    public Class<? extends DeploymentModule> discoverModuleType(final URL baseUrl, final ClassLoader classLoader, final Set<RequireDescriptors> requireDescriptor) throws IOException, UnknownModuleTypeException {
        final boolean scanPotentialEjbModules = !requireDescriptor.contains(RequireDescriptors.EJB);
        final boolean scanPotentialClientModules = !requireDescriptor.contains(RequireDescriptors.CLIENT);


        URL pathToScanDescriptors = baseUrl;
        String path;
        if (baseUrl != null) {
            path = URLs.toFile(baseUrl).getAbsolutePath();
            if (baseUrl.getProtocol().equals("file") && path.endsWith("WEB-INF/classes/")) {
                //EJB found in WAR/WEB-INF/classes, scan WAR for ejb-jar.xml
                pathToScanDescriptors = new URL(path.substring(0, path.lastIndexOf("WEB-INF/classes/")));
            }
        } else {
            path = "";
        }

        final Map<String, URL> descriptors = getDescriptors(classLoader, pathToScanDescriptors);

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (path.endsWith(".xml") || path.endsWith(".json")) { // let say it is a resource module
            return ResourcesModule.class;
        }

        if (descriptors.containsKey("application.xml") || path.endsWith(".ear")) {
            return AppModule.class;
        }

        if (descriptors.containsKey("ra.xml") || path.endsWith(".rar")) {
            return ConnectorModule.class;
        }

        if (baseUrl != null) {
            final Map<String, URL> webDescriptors = getWebDescriptors(getFile(baseUrl));
            if (webDescriptors.containsKey("web.xml") || webDescriptors.containsKey(WEB_FRAGMENT_XML) // descriptor
                || path.endsWith(".war") || new File(path, "WEB-INF").exists()) { // webapp specific files
                return WebModule.class;
            }
        }

        if (descriptors.containsKey("ejb-jar.xml") || descriptors.containsKey("beans.xml")) {
            return EjbModule.class;
        }

        if (descriptors.containsKey("application-client.xml")) {
            return ClientModule.class;
        }

        final URL manifestUrl = descriptors.get("MANIFEST.MF");
        if (scanPotentialClientModules && manifestUrl != null) {
            // In this case scanPotentialClientModules really means "require application-client.xml"
            final InputStream is = new BufferedInputStream(manifestUrl.openStream());
            final Manifest manifest = new Manifest(is);
            final String mainClass = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            if (mainClass != null) {
                return ClientModule.class;
            }
        }

        final Class<? extends DeploymentModule> cls = checkAnnotations(baseUrl, classLoader, scanPotentialEjbModules, scanPotentialClientModules);
        if (cls != null) {
            return cls;
        }

        if (descriptors.containsKey("persistence.xml") || descriptors.containsKey("persistence-fragment.xml")) {
            return PersistenceModule.class;
        }

        //#TOMEE-613
        final File file = URLs.toFile(baseUrl);
        if (DeploymentsResolver.isValidDirectory(file)) {

            final File[] files = file.listFiles();
            if (containsEarAssets(files)) {
                return AppModule.class;
            }
            if (containsWebAssets(files)) {
                return WebModule.class;
            }
        }

        final Class<? extends DeploymentModule> defaultType = (Class<? extends DeploymentModule>) SystemInstance.get().getOptions().get("openejb.default.deployment-module", (Class<?>) null);
        if (defaultType != null) {
            // should we do a better filtering? it seems enough for common cases.
            if (WebModule.class.equals(defaultType) && (path.endsWith(".jar!") || path.endsWith(".jar"))) {
                throw new UnknownModuleTypeException("Unknown module type: url=" + path + " which can't be a war.");
            }

            LOGGER.debug("type for '" + path + "' was not found, defaulting to " + defaultType.getSimpleName());
            return defaultType;
        }
        throw new UnknownModuleTypeException("Unknown module type: url=" + path); // baseUrl can be null
    }

    private static boolean containsWebAssets(final File[] files) {
        if (files != null) {
            for (final File file : files) {
                final String fn = file.getName().toLowerCase(Locale.ENGLISH);
                if (fn.endsWith(".jsp")) {
                    return true;
                }
                if (fn.endsWith(".html")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean containsEarAssets(final File[] files) {
        if (files != null) {
            for (final File file : files) {
                final String fn = file.getName().toLowerCase(Locale.ENGLISH);
                if (fn.endsWith(".jar")) {
                    return true;
                }
                if (fn.endsWith(".war")) {
                    return true;
                }
                if (fn.endsWith(".rar")) {
                    return true;
                }
            }
        }
        return false;
    }


    private Map<String, URL> getDescriptors(final ClassLoader classLoader, final URL pathToScanDescriptors)
        throws IOException {
        final ResourceFinder finder = new ResourceFinder("", classLoader, pathToScanDescriptors);

        return altDDSources(mapDescriptors(finder), false);
    }

    private Class<? extends DeploymentModule> checkAnnotations(final URL urls, final ClassLoader classLoader, final boolean scanPotentialEjbModules, final boolean scanPotentialClientModules) {
        Class<? extends DeploymentModule> cls = null;
        if (scanPotentialEjbModules || scanPotentialClientModules) {
            final AnnotationFinder classFinder = new AnnotationFinder(classLoader, urls);

            final Set<Class<? extends DeploymentModule>> otherTypes = new LinkedHashSet<>();

            final AnnotationFinder.Filter filter = new AnnotationFinder.Filter() {
                final String packageName = LocalClient.class.getName().replace("LocalClient", "");

                @Override
                public boolean accept(final String annotationName) {
                    if (scanPotentialClientModules && annotationName.startsWith(packageName)) {
                        if (LocalClient.class.getName().equals(annotationName)) {
                            otherTypes.add(ClientModule.class);
                        }
                        if (RemoteClient.class.getName().equals(annotationName)) {
                            otherTypes.add(ClientModule.class);
                        }
                    } else if (scanPotentialEjbModules) {
                        if (annotationName.startsWith("jakarta.ejb.")) {
                            if ("jakarta.ejb.Stateful".equals(annotationName)) {
                                return true;
                            }
                            if ("jakarta.ejb.Stateless".equals(annotationName)) {
                                return true;
                            }
                            if ("jakarta.ejb.Singleton".equals(annotationName)) {
                                return true;
                            }
                            if ("jakarta.ejb.MessageDriven".equals(annotationName)) {
                                return true;
                            }
                        } else if (scanManagedBeans && "jakarta.annotation.ManagedBean".equals(annotationName)) {
                            return true;
                        }
                    }
                    return false;
                }
            };

            if (classFinder.find(filter)) {
                cls = EjbModule.class;
                // if it is a war just throw an error
                try {
                    if(LOGGER.isWarningEnabled()) {
                        final File ar = URLs.toFile(urls);
                        if (!ar.isDirectory() && !ar.getName().endsWith("ar")) { // guess no archive extension, check it is not a hidden war
                            try (JarFile war = new JarFile(ar)) {
                                final ZipEntry entry = war.getEntry("WEB-INF/");
                                if (entry != null) {
                                    LOGGER.warning("you deployed " + urls.toExternalForm() + ", it seems it is a war with no extension, please rename it");
                                }
                            }
                        }
                    }
                } catch (final Exception ignored) {
                    // no-op
                }
            }

            if (otherTypes.size() > 0) {
                // We may want some ordering/sorting if we add more type scanning
                cls = otherTypes.iterator().next();
            }
        }
        return cls;
    }

    public static File unpack(final File jarFile) throws OpenEJBException {
        if (jarFile.isDirectory() || jarFile.getName().endsWith(".jar")) {
            return jarFile;
        }

        String name = jarFile.getName();
        if (name.endsWith(".ear") || name.endsWith(".zip") || name.endsWith(".war") || name.endsWith(".rar")) {
            name = name.replaceFirst("....$", "");
        } else {
            name += ".unpacked";
        }

        try {
            return JarExtractor.extract(jarFile, name);
        } catch (final Throwable e) {
            throw new OpenEJBException("Unable to extract jar. " + e.getMessage(), e);
        }
    }

    protected static URL getFileUrl(final File jarFile) throws OpenEJBException {
        final URL baseUrl;
        try {
            baseUrl = jarFile.toURI().toURL();
        } catch (final MalformedURLException e) {
            throw new OpenEJBException("Malformed URL to app. " + e.getMessage(), e);
        }
        return baseUrl;
    }

    public static void reloadAltDD() {
        ALTDD = SystemInstance.get().getOptions().get(OPENEJB_ALTDD_PREFIX, (String) null);
    }

    public static class ExternalConfiguration {
        private final String[] classpath;
        private final Filter customerFilter;

        public ExternalConfiguration(final String[] classpath, final Filter customerFilter) {
            this.classpath = classpath;
            this.customerFilter = customerFilter;
        }

        public Filter getCustomerFilter() {
            return customerFilter;
        }

        public String[] getClasspath() {
            return classpath;
        }
    }
}
