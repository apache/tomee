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
package org.apache.openejb.arquillian.openejb;

import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.cdi.CompositeBeans;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.FinderFactory;
import org.apache.openejb.config.ReadDescriptors;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.config.WebappAggregatedArchive;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebApp$JAXB;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.IO;
import org.apache.openejb.sxc.Sxc;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.apache.xbean.finder.archive.CompositeArchive;
import org.apache.xbean.finder.archive.FilteredArchive;
import org.apache.xbean.finder.archive.JarArchive;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.UrlAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.filter.IncludeRegExpPaths;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Arrays.asList;
import static org.apache.openejb.arquillian.openejb.reflection.Assets.EMPTY_LOADER;
import static org.apache.openejb.arquillian.openejb.reflection.Assets.get;

// doesn't implement ApplicationArchiveProcessor anymore since in some cases
// (with some observers set for instance)
// it is not called before the deployment itself
// so it is like if it was skipped
public class OpenEJBArchiveProcessor {
    private static final Logger LOGGER = Logger.getLogger(OpenEJBArchiveProcessor.class.getName());

    private static final String META_INF = "META-INF/";
    private static final String WEB_INF = "WEB-INF/";
    private static final String EJB_JAR_XML = "ejb-jar.xml";

    private static final String BEANS_XML = "beans.xml";
    private static final String VALIDATION_XML = "validation.xml";
    private static final String RESOURCES_XML = "resources.xml";
    private static final String PERSISTENCE_XML = "persistence.xml";
    private static final String OPENEJB_JAR_XML = "openejb-jar.xml";
    private static final String ENV_ENTRIES_PROPERTIES = "env-entries.properties";
    private static final String WEB_INF_CLASSES = "/WEB-INF/classes/";

    public static AppModule createModule(final Archive<?> archive, final TestClass testClass, final Closeables closeables) {
        final Class<?> javaClass;
        if (testClass != null) {
            javaClass = testClass.getJavaClass();
        } else {
            javaClass = null;
        }

        final ClassLoader parent;
        if (javaClass == null) {
            parent = Thread.currentThread().getContextClassLoader();
        } else {
            parent = javaClass.getClassLoader();
        }

        final List<URL> additionalPaths = new ArrayList<>();
        CompositeArchive scannedArchive = null;
        final Map<URL, List<String>> earMap = new HashMap<>();
        final Map<String, Object> altDD = new HashMap<>();
        final List<Archive> earLibsArchives = new ArrayList<>();
        final CompositeBeans earBeans = new CompositeBeans();

        final boolean isEar = EnterpriseArchive.class.isInstance(archive);
        final boolean isWebApp = WebArchive.class.isInstance(archive);
        final String prefix = isWebApp ? WEB_INF : META_INF;
        if (isEar || isWebApp) {
            final Map<ArchivePath, Node> jars = archive.getContent(new IncludeRegExpPaths(isEar ? "/.*\\.jar" : "/WEB-INF/lib/.*\\.jar"));
            scannedArchive = analyzeLibs(parent, additionalPaths, earMap, earLibsArchives, earBeans, jars, altDD);
        }

        final URL[] urls = additionalPaths.toArray(new URL[additionalPaths.size()]);

        final URLClassLoaderFirst swParent = new URLClassLoaderFirst(urls, parent);
        closeables.add(swParent);

        if (!isEar) {
            earLibsArchives.add(archive);
        }
        final SWClassLoader loader = new SWClassLoader(swParent, earLibsArchives.toArray(new Archive<?>[earLibsArchives.size()]));
        closeables.add(loader);
        final URLClassLoader tempClassLoader = ClassLoaderUtil.createTempClassLoader(loader);
        closeables.add(tempClassLoader);

        final AppModule appModule = new AppModule(loader, archive.getName());
        if (WEB_INF.equals(prefix)) {
            appModule.setDelegateFirst(false);
            appModule.setStandloneWebModule();

            final WebModule webModule = new WebModule(createWebApp(archive), contextRoot(archive.getName()), loader, "", appModule.getModuleId());
            webModule.setUrls(additionalPaths);
            appModule.getWebModules().add(webModule);
        } else if (isEar) { // mainly for CDI TCKs
            final FinderFactory.OpenEJBAnnotationFinder earLibFinder = new FinderFactory.OpenEJBAnnotationFinder(new SimpleWebappAggregatedArchive(tempClassLoader, scannedArchive, earMap));
            appModule.setEarLibFinder(earLibFinder);

            final EjbModule earCdiModule = new EjbModule(appModule.getClassLoader(), DeploymentLoader.EAR_SCOPED_CDI_BEANS + appModule.getModuleId(), new EjbJar(), new OpenejbJar());
            earCdiModule.setBeans(earBeans);
            earCdiModule.setFinder(earLibFinder);

            final EjbJar ejbJar;
            final AssetSource ejbJarXml = AssetSource.class.isInstance(altDD.get(EJB_JAR_XML)) ? AssetSource.class.cast(altDD.get(EJB_JAR_XML)) : null;
            if (ejbJarXml != null) {
                try {
                    ejbJar = ReadDescriptors.readEjbJar(ejbJarXml.get());
                } catch (final Exception e) {
                    throw new OpenEJBRuntimeException(e);
                }
            } else {
                ejbJar = new EjbJar();
            }

            earCdiModule.setEjbJar(ejbJar); // EmptyEjbJar would prevent to add scanned EJBs but this is *here* an aggregator so we need to be able to do so
            appModule.getEjbModules().add(earCdiModule);

            for (final Map.Entry<ArchivePath, Node> node : archive.getContent(new IncludeRegExpPaths("/.*\\.war")).entrySet()) {
                final Asset asset = node.getValue().getAsset();
                if (ArchiveAsset.class.isInstance(asset)) {
                    final Archive<?> webArchive = ArchiveAsset.class.cast(asset).getArchive();
                    if (WebArchive.class.isInstance(webArchive)) {
                        final Map<String, Object> webAltDD = new HashMap<>();
                        final Node beansXml = findBeansXml(webArchive, WEB_INF);

                        final List<URL> webappAdditionalPaths = new LinkedList<>();
                        final CompositeBeans webAppBeansXml = new CompositeBeans();
                        final List<Archive> webAppArchives = new LinkedList<Archive>();
                        final Map<URL, List<String>> webAppClassesByUrl = new HashMap<URL, List<String>>();
                        final CompositeArchive webAppArchive = analyzeLibs(parent,
                                webappAdditionalPaths, webAppClassesByUrl,
                                webAppArchives, webAppBeansXml,
                                webArchive.getContent(new IncludeRegExpPaths("/WEB-INF/lib/.*\\.jar")),
                                webAltDD);

                        webAppArchives.add(webArchive);
                        final SWClassLoader webLoader = new SWClassLoader(parent, webAppArchives.toArray(new Archive<?>[webAppArchives.size()]));
                        closeables.add(webLoader);

                        final FinderFactory.OpenEJBAnnotationFinder finder = new FinderFactory.OpenEJBAnnotationFinder(
                                finderArchive(beansXml, webArchive, webLoader, webAppArchive, webAppClassesByUrl, webAppBeansXml));

                        final String contextRoot = contextRoot(webArchive.getName());
                        final WebModule webModule = new WebModule(createWebApp(webArchive), contextRoot, webLoader, "", appModule.getModuleId() + "_" + contextRoot);
                        webModule.setUrls(Collections.<URL>emptyList());
                        webModule.setScannableUrls(Collections.<URL>emptyList());
                        webModule.setFinder(finder);

                        final EjbJar webEjbJar = createEjbJar(prefix, webArchive);

                        final EjbModule ejbModule = new EjbModule(webLoader, webModule.getModuleId(), null, webEjbJar, new OpenejbJar());
                        ejbModule.setBeans(webAppBeansXml);
                        ejbModule.getAltDDs().putAll(webAltDD);
                        ejbModule.getAltDDs().put("beans.xml", webAppBeansXml);
                        ejbModule.setFinder(finder);
                        ejbModule.setClassLoader(webLoader);
                        ejbModule.setWebapp(true);

                        appModule.getEjbModules().add(ejbModule);
                        appModule.getWebModules().add(webModule);

                        addPersistenceXml(archive, WEB_INF, appModule);
                        addOpenEJbJarXml(archive, WEB_INF, ejbModule);
                        addValidationXml(archive, WEB_INF, new HashMap<String, Object>(), ejbModule);
                        addResourcesXml(archive, WEB_INF, ejbModule);
                        addEnvEntries(archive, WEB_INF, appModule, ejbModule);
                    }
                }
            }
        }

        if (isEar) { // adding the test class as lib class can break test if tested against the web part of the ear
            addTestClassAsManagedBean(javaClass, tempClassLoader, appModule);
            return appModule;
        }

        // add the test as a managed bean to be able to inject into it easily
        final Map<String, Object> testDD;
        if (javaClass != null) {
            final EjbModule testEjbModule = addTestClassAsManagedBean(javaClass, tempClassLoader, appModule);
            testDD = testEjbModule.getAltDDs();
        } else {
            testDD = new HashMap<>(); // ignore
        }

        final EjbJar ejbJar = createEjbJar(prefix, archive);

        if (ejbJar.getModuleName() == null) {
            final String name = archive.getName();
            if (name.endsWith("ar") && name.length() > 4) {
                ejbJar.setModuleName(name.substring(0, name.length() - ".jar".length()));
            } else {
                ejbJar.setModuleName(name);
            }
        }

        final EjbModule ejbModule = new EjbModule(ejbJar);
        ejbModule.setClassLoader(tempClassLoader);

        final Node beansXml = findBeansXml(archive, prefix);

        final FinderFactory.OpenEJBAnnotationFinder finder = new FinderFactory.OpenEJBAnnotationFinder(
                finderArchive(beansXml, archive, loader, scannedArchive, earMap, earBeans));
        ejbModule.setFinder(finder);
        ejbModule.setBeans(earBeans);
        ejbModule.getAltDDs().put("beans.xml", earBeans);

        if (appModule.isWebapp()) { // war
            appModule.getWebModules().iterator().next().setFinder(ejbModule.getFinder());
        }
        appModule.getEjbModules().add(ejbModule);

        addPersistenceXml(archive, prefix, appModule);
        addOpenEJbJarXml(archive, prefix, ejbModule);
        addValidationXml(archive, prefix, testDD, ejbModule);
        addResourcesXml(archive, prefix, ejbModule);
        addEnvEntries(archive, prefix, appModule, ejbModule);

        if (!appModule.isWebapp()) {
            appModule.getAdditionalLibraries().addAll(additionalPaths);
        }

        if (archive.getName().endsWith(".jar")) { // otherwise global naming will be broken
            appModule.setStandloneWebModule();
        }
        return appModule;
    }

    private static EjbJar createEjbJar(final String prefix, final Archive<?> webArchive) {
        final EjbJar webEjbJar;
        final Node webEjbJarXml = webArchive.get(prefix.concat(EJB_JAR_XML));
        if (webEjbJarXml != null) {
            try {
                webEjbJar = ReadDescriptors.readEjbJar(webEjbJarXml.getAsset().openStream());
            } catch (final OpenEJBException e) {
                throw new OpenEJBRuntimeException(e);
            }
        } else {
            webEjbJar = new EjbJar();
        }
        return webEjbJar;
    }

    private static EjbModule addTestClassAsManagedBean(Class<?> javaClass, URLClassLoader tempClassLoader, AppModule appModule) {
        final EjbJar ejbJar = new EjbJar();
        final OpenejbJar openejbJar = new OpenejbJar();
        final String ejbName = appModule.getModuleId() + "_" + javaClass.getName();
        final ManagedBean bean = ejbJar.addEnterpriseBean(new ManagedBean(ejbName, javaClass.getName(), true));
        bean.localBean();
        bean.setTransactionType(TransactionType.BEAN);
        final EjbDeployment ejbDeployment = openejbJar.addEjbDeployment(bean);
        ejbDeployment.setDeploymentId(ejbName);
        final EjbModule e = new EjbModule(ejbJar, openejbJar);
        e.getProperties().setProperty("openejb.cdi.activated", "false");
        e.getProperties().setProperty("openejb.test.module", "true");
        e.setBeans(new Beans());
        e.setClassLoader(tempClassLoader);
        appModule.getEjbModules().add(e);
        return e;
    }

    private static WebApp createWebApp(final Archive<?> archive) {
        WebApp webApp;
        final Node webXml = archive.get(WEB_INF + "web.xml");
        if (webXml == null) {
            webApp = new WebApp();
        } else {
            InputStream inputStream = null;
            try {
                inputStream = webXml.getAsset().openStream();
                webApp = Sxc.unmarshalJavaee(new WebApp$JAXB(), inputStream);
            } catch (final Exception e) {
                webApp = new WebApp();
            } finally {
                IO.close(inputStream);
            }
        }
        return webApp;
    }

    private static CompositeArchive analyzeLibs(final ClassLoader parent,
                                                final List<URL> additionalPaths, final Map<URL, List<String>> earMap,
                                                final List<Archive> earLibsArchives,
                                                final CompositeBeans earBeans,
                                                final Map<ArchivePath, Node> jars,
                                                final Map<String, Object> altDD) {
        final List<org.apache.xbean.finder.archive.Archive> archives = new ArrayList<>(jars.size());
        for (final Map.Entry<ArchivePath, Node> node : jars.entrySet()) {
            final Asset asset = node.getValue().getAsset();
            if (ArchiveAsset.class.isInstance(asset)) {
                final Archive<?> libArchive = ArchiveAsset.class.cast(asset).getArchive();
                if (!isExcluded(libArchive.getName())) {
                    earLibsArchives.add(libArchive);
                    final List<Class<?>> earClasses = new ArrayList<>();
                    final List<String> earClassNames = new ArrayList<>();
                    final Map<ArchivePath, Node> content = libArchive.getContent(new IncludeRegExpPaths(".*.class"));
                    for (final Map.Entry<ArchivePath, Node> classNode : content.entrySet()) {
                        final String classname = name(classNode.getKey().get());
                        try {
                            earClasses.add(parent.loadClass(classname));
                            earClassNames.add(classname);
                        } catch (final ClassNotFoundException e) {
                            LOGGER.fine("Can't load class " + classname);
                        } catch (final NoClassDefFoundError ncdfe) {
                            // no-op
                        }
                    }
                    try { // ends with !/META-INF/beans.xml to force it to be used as a cdi module *with bda*
                        final Node beansNode = libArchive.get(META_INF + BEANS_XML);
                        final URL arUrl = new URL("jar:file://!/lib/" + libArchive.getName() + (beansNode != null ? "!/META-INF/beans.xml" : ""));
                        if (beansNode != null) {
                            try {
                                DeploymentLoader.doMerge(arUrl, earBeans, ReadDescriptors.readBeans(beansNode.getAsset().openStream()));
                            } catch (final OpenEJBException e) {
                                throw new IllegalArgumentException(e);
                            }
                        }
                        earMap.put(arUrl, earClassNames);
                    } catch (final MalformedURLException e) {
                        // no-op
                    }
                    archives.add(new ClassesArchive(earClasses));
                }

                final Node ejbJarXml = libArchive.get(META_INF + EJB_JAR_XML);
                if (ejbJarXml != null) { // not super, we should merge them surely but ok for use cases we met until today
                    altDD.put("ejb-jar.xml", new AssetSource(ejbJarXml.getAsset(), null));
                }
            } if (UrlAsset.class.isInstance(asset) || FileAsset.class.isInstance(asset)) {
                try {
                    final URL url = UrlAsset.class.isInstance(asset) ? get(URL.class, "url", asset) : get(File.class, "file", asset).toURI().toURL();
                    final List<String> classes = new ArrayList<String>();
                    archives.add(new FilteredArchive(
                            new JarArchive(parent, url),
                            new WebappAggregatedArchive.ScanXmlSaverFilter(false, null, classes, null)));
                    additionalPaths.add(url);

                    final URLClassLoader loader = new URLClassLoader(new URL[] { url }, EMPTY_LOADER);
                    for (final String beans : asList("META-INF/beans.xml", "/META-INF/beans.xml")) {
                        final URL u = loader.getResource(beans);
                        if (u != null) {
                            try {
                                DeploymentLoader.doMerge(u, earBeans, ReadDescriptors.readBeans(u.openStream()));
                            } catch (final OpenEJBException e) {
                                throw new IllegalArgumentException(e);
                            } catch (final IOException e) {
                                // no-op
                            }
                            earMap.put(u, classes);
                        }
                    }
                    try {
                        loader.close();
                    } catch (final IOException e) {
                        // no-op
                    }
                } catch (final MalformedURLException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        return new CompositeArchive(archives);
    }

    private static Node findBeansXml(final Archive<?> archive, final String prefix) {
        Node beansXml = archive.get(prefix.concat(BEANS_XML));
        if (beansXml == null && WEB_INF.equals(prefix)) {
            beansXml = archive.get(WEB_INF_CLASSES.concat(META_INF).concat(BEANS_XML));
        }
        return beansXml;
    }

    private static void addPersistenceXml(final Archive<?> archive, final String prefix, final AppModule appModule) {
        Node persistenceXml = archive.get(prefix.concat(PERSISTENCE_XML));
        if (persistenceXml == null && WEB_INF.equals(prefix)) {
            persistenceXml = archive.get(WEB_INF_CLASSES.concat(META_INF).concat(PERSISTENCE_XML));
        }
        if (persistenceXml != null) {
            final Asset asset = persistenceXml.getAsset();
            if (UrlAsset.class.isInstance(asset)) {
                appModule.getAltDDs().put(PERSISTENCE_XML, Arrays.asList(get(URL.class, "url", asset)));
            } else if (FileAsset.class.isInstance(asset)) {
                try {
                    appModule.getAltDDs().put(PERSISTENCE_XML, Arrays.asList(get(File.class, "file", asset).toURI().toURL()));
                } catch (final MalformedURLException e) {
                    appModule.getAltDDs().put(PERSISTENCE_XML, Arrays.asList(new AssetSource(persistenceXml.getAsset(), null)));
                }
            } else if (ClassLoaderAsset.class.isInstance(asset)) {
                final URL url = get(ClassLoader.class, "classLoader", asset).getResource(get(String.class, "resourceName", asset));
                if (url != null) {
                    appModule.getAltDDs().put(PERSISTENCE_XML, Arrays.asList(url));
                } else {
                    appModule.getAltDDs().put(PERSISTENCE_XML, Arrays.asList(new AssetSource(persistenceXml.getAsset(), null)));
                }
            } else {
                appModule.getAltDDs().put(PERSISTENCE_XML, Arrays.asList(new AssetSource(persistenceXml.getAsset(), null)));
            }
        }
    }

    private static void addOpenEJbJarXml(final Archive<?> archive, final String prefix, final EjbModule ejbModule) {
        final Node openejbJarXml = archive.get(prefix.concat(OPENEJB_JAR_XML));
        if (openejbJarXml != null) {
            ejbModule.getAltDDs().put(OPENEJB_JAR_XML, new AssetSource(openejbJarXml.getAsset(), null));
        }
    }

    private static void addValidationXml(final Archive<?> archive, final String prefix, final Map<String, Object> testDD, final EjbModule ejbModule) {
        Node validationXml = archive.get(prefix.concat(VALIDATION_XML));
        // bval tcks
        if (validationXml == null && WEB_INF == prefix) { // we can use == here
            validationXml = archive.get(WEB_INF_CLASSES.concat(META_INF).concat(VALIDATION_XML));
        }
        if (validationXml != null) {
            testDD.put(VALIDATION_XML, new AssetSource(validationXml.getAsset(), null)); // use same config otherwise behavior is weird
            ejbModule.getAltDDs().put(VALIDATION_XML, new AssetSource(validationXml.getAsset(), null));
        }
    }

    private static void addResourcesXml(final Archive<?> archive, final String prefix, final EjbModule ejbModule) {
        final Node resourcesXml = archive.get(prefix.concat(RESOURCES_XML));
        if (resourcesXml != null) {
            ejbModule.getAltDDs().put(RESOURCES_XML, new AssetSource(resourcesXml.getAsset(), null));
        }
    }

    private static void addEnvEntries(final Archive<?> archive, final String prefix, final AppModule appModule, final EjbModule ejbModule) {
        final Node envEntriesProperties = archive.get(prefix.concat(ENV_ENTRIES_PROPERTIES));
        if (envEntriesProperties != null) {
            InputStream is = null;
            final Properties properties = new Properties();
            try {
                is = envEntriesProperties.getAsset().openStream();
                properties.load(is);
                ejbModule.getAltDDs().put(ENV_ENTRIES_PROPERTIES, properties);

                // do it for test class too
                appModule.getEjbModules().iterator().next().getAltDDs().put(ENV_ENTRIES_PROPERTIES, properties);
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, "can't read env-entries.properties", e);
            } finally {
                IO.close(is);
            }
        }
    }

    private static String contextRoot(final String name) {
        if (name.endsWith(".war")) {
            return name.substring(0, name.length() - ".war".length());
        }
        return name;
    }

    private static org.apache.xbean.finder.archive.Archive finderArchive(
            final Node beansXml, final Archive<?> archive,
            final ClassLoader cl, final CompositeArchive libs,
            final Map<URL, List<String>> webAppClassesByUrl,
            final CompositeBeans compositeBeans) {
        final List<Class<?>> classes = new ArrayList<>();
        final Map<ArchivePath, Node> content = archive.getContent(new IncludeRegExpPaths(".*.class"));
        for (final Map.Entry<ArchivePath, Node> node : content.entrySet()) {
            final String classname = name(node.getKey().get());
            try {
                classes.add(cl.loadClass(classname));
            } catch (final ClassNotFoundException e) {
                LOGGER.fine("Can't load class " + classname);
                if (LOGGER.isLoggable(Level.FINEST)) {
                    e.printStackTrace(System.err);
                }
            } catch (final NoClassDefFoundError ncdfe) {
                // no-op
            }
        }

        final List<org.apache.xbean.finder.archive.Archive> archives = new ArrayList<>();

        archives.add(new ClassesArchive(classes));
        if (beansXml != null) {
            try {
                final URL key = new URL("jar:file://!/WEB-INF/beans.xml"); // no host avoid host resolution in hashcode()
                try {
                    DeploymentLoader.doMerge(key, compositeBeans, ReadDescriptors.readBeans(beansXml.getAsset().openStream()));
                } catch (final OpenEJBException e) {
                    throw new IllegalArgumentException(e);
                }

                final List<String> mainClasses = new ArrayList<>();
                for (final Class<?> clazz : classes) {
                    mainClasses.add(clazz.getName());
                }

                webAppClassesByUrl.put(key, mainClasses);
            } catch (final MalformedURLException mue) {
                // no-op
            }
        }

        if (libs != null) {
            archives.add(libs);
        }

        return new SimpleWebappAggregatedArchive(cl, new CompositeArchive(archives), webAppClassesByUrl);
    }

    private static boolean isExcluded(final String archiveName) {
        return "arquillian-junit.jar".equals(archiveName) || "arquillian-protocol.jar".equals(archiveName)
                || "arquillian-core.jar".equals(archiveName);
    }

    private static String name(final String raw) {
        String name = raw;
        if (name.startsWith(WEB_INF_CLASSES)) {
            name = name.substring(WEB_INF_CLASSES.length() - 1);
        }
        name = name.replace('/', '.');
        return name.substring(1, name.length() - 6);
    }

    private static final class AssetSource extends ReadDescriptors.UrlSource {
        private Asset asset;

        private AssetSource(final Asset asset, final URL url) {
            super(url);
            this.asset = asset;
        }

        @Override
        public InputStream get() throws IOException {
            return asset.openStream();
        }

        @Override
        public String toString() {
            return "AssetSource{asset=" + asset + '}';
        }
    }

    // mainly extended to be sure to reuse our tip about scanning for CDI
    private static class SimpleWebappAggregatedArchive extends WebappAggregatedArchive {
        private final CompositeArchive delegate;
        private final Map<URL, List<String>> classesMap;

        public SimpleWebappAggregatedArchive(final ClassLoader cl, final CompositeArchive archive, final Map<URL, List<String>> map) {
            super(cl, new HashMap<String, Object>(), new ArrayList<URL>());

            delegate = archive;
            classesMap = map;
        }

        @Override
        public Map<URL, List<String>> getClassesMap() {
            return classesMap;
        }

        @Override
        public InputStream getBytecode(final String s) throws IOException, ClassNotFoundException {
            return delegate.getBytecode(s);
        }

        @Override
        public Class<?> loadClass(final String s) throws ClassNotFoundException {
            return delegate.loadClass(s);
        }

        @Override
        public Iterator<Entry> iterator() {
            return delegate.iterator();
        }
    }
}
