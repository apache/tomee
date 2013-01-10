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
package org.apache.openejb.arquillian.openejb;

import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.FinderFactory;
import org.apache.openejb.config.ReadDescriptors;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.config.WebappAggregatedArchive;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;
import org.apache.xbean.finder.AnnotationFinder;
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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.filter.IncludeRegExpPaths;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final String PERSISTENCE_XML = "persistence.xml";
    private static final String OPENEJB_JAR_XML = "openejb-jar.xml";
    private static final String ENV_ENTRIES_PROPERTIES = "env-entries.properties";
    public static final String WEB_INF_CLASSES = "/WEB-INF/classes/";

    public static AppModule createModule(final Archive<?> archive, final TestClass testClass) {
        final Class<?> javaClass;
        if (testClass != null) {
            javaClass = testClass.getJavaClass();
        } else {
            javaClass = null;
        }

        final List<URL> additionalPaths = new ArrayList<URL>();

        final String prefix;
        if (WebArchive.class.isInstance(archive)) {
            prefix = WEB_INF;

            final Map<ArchivePath, Node> content = archive.getContent(new IncludeRegExpPaths("/WEB-INF/lib/.*"));
            for (final Map.Entry<ArchivePath, Node> node : content.entrySet()) {
                final Asset asset = node.getValue().getAsset();
                if (UrlAsset.class.isInstance(asset)) {
                    additionalPaths.add(get(URL.class, "url", asset));
                } else if (FileAsset.class.isInstance(asset)) {
                    try {
                        additionalPaths.add(get(File.class, "file", asset).toURI().toURL());
                    } catch (MalformedURLException e) {
                        LOGGER.log(Level.SEVERE, "can't add a library to the deployment", e);
                    }
                } else if (ArchiveAsset.class.isInstance(asset)) {
                    archive.merge(((ArchiveAsset) asset).getArchive());
                }
            }
        } else {
            prefix = META_INF;
        }

        final ClassLoader parent;
        if (javaClass == null) {
            parent = Thread.currentThread().getContextClassLoader();
        } else {
            parent = javaClass.getClassLoader();
        }

        final URL[] urls = additionalPaths.toArray(new URL[additionalPaths.size()]);

        final ClassLoader loader;
        if (!WEB_INF.equals(prefix)) {
            loader = new SWClassLoader("", new URLClassLoader(urls, parent), archive);
        } else {
            loader = new SWClassLoader("/WEB-INF/classes/", new URLClassLoaderFirst(urls, parent), archive);
        }
        final URLClassLoader tempClassLoader = ClassLoaderUtil.createTempClassLoader(loader);

        final AppModule appModule = new AppModule(loader, archive.getName());
        if (WEB_INF.equals(prefix)) {
            appModule.setDelegateFirst(false);
            appModule.setStandloneWebModule();

            final WebModule webModule = new WebModule(new WebApp(), contextRoot(archive.getName()), loader, "", appModule.getModuleId());
            webModule.setUrls(additionalPaths);
            appModule.getWebModules().add(webModule);
        }

        // add the test as a managed bean to be able to inject into it easily
        if (javaClass != null) {
            final EjbJar ejbJar = new EjbJar();
            final OpenejbJar openejbJar = new OpenejbJar();
            final ManagedBean bean = ejbJar.addEnterpriseBean(new ManagedBean(javaClass.getSimpleName(), javaClass.getName(), true));
            bean.setTransactionType(TransactionType.BEAN);
            final EjbDeployment ejbDeployment = openejbJar.addEjbDeployment(bean);
            ejbDeployment.setDeploymentId(javaClass.getName());
            final EjbModule e = new EjbModule(ejbJar, openejbJar);
            e.setClassLoader(tempClassLoader);
            appModule.getEjbModules().add(e);
        }

        final EjbJar ejbJar;
        final Node ejbJarXml = archive.get(prefix.concat(EJB_JAR_XML));
        if (ejbJarXml != null) {
            try {
                ejbJar = ReadDescriptors.readEjbJar(ejbJarXml.getAsset().openStream());
            } catch (OpenEJBException e) {
                throw new OpenEJBRuntimeException(e);
            }
        } else {
            ejbJar = new EjbJar();
        }

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

        Node beansXml = archive.get(prefix.concat(BEANS_XML));
        if (beansXml == null && WEB_INF.equals(prefix)) {
            beansXml = archive.get(WEB_INF_CLASSES.concat(META_INF).concat(BEANS_XML));
        }
        if (beansXml != null) {
            ejbModule.getAltDDs().put(BEANS_XML, new AssetSource(beansXml.getAsset()));
        }

        final org.apache.xbean.finder.archive.Archive finderArchive = finderArchive(beansXml, archive, tempClassLoader, additionalPaths);

        ejbModule.setFinder(new FinderFactory.ModuleLimitedFinder(new AnnotationFinder(finderArchive)));
        if (appModule.isWebapp()) { // war
            appModule.getWebModules().iterator().next().setFinder(ejbModule.getFinder());
        }
        appModule.getEjbModules().add(ejbModule);

        {
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
                    } catch (MalformedURLException e) {
                        appModule.getAltDDs().put(PERSISTENCE_XML, Arrays.asList(new AssetSource(persistenceXml.getAsset())));
                    }
                } else if (ClassLoaderAsset.class.isInstance(asset)) {
                    final URL url = get(ClassLoader.class, "classLoader", asset).getResource(get(String.class, "resourceName", asset));
                    if (url != null) {
                        appModule.getAltDDs().put(PERSISTENCE_XML, Arrays.asList(url));
                    } else {
                        appModule.getAltDDs().put(PERSISTENCE_XML, Arrays.asList(new AssetSource(persistenceXml.getAsset())));
                    }
                } else {
                    appModule.getAltDDs().put(PERSISTENCE_XML, Arrays.asList(new AssetSource(persistenceXml.getAsset())));
                }
            }
        }

        {
            final Node openejbJarXml = archive.get(prefix.concat(OPENEJB_JAR_XML));
            if (openejbJarXml != null) {
                ejbModule.getAltDDs().put(OPENEJB_JAR_XML, new AssetSource(openejbJarXml.getAsset()));
            }
        }

        {
            final Node validationXml = archive.get(prefix.concat(VALIDATION_XML));
            if (validationXml != null) {
                ejbModule.getAltDDs().put(VALIDATION_XML, new AssetSource(validationXml.getAsset()));
            }
        }

        {
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
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "can't read env-entries.properties", e);
                } finally {
                    IO.close(is);
                }
            }
        }

        if (!appModule.isWebapp()) {
            appModule.getAdditionalLibraries().addAll(additionalPaths);
        }

        return appModule;
    }

    private static String contextRoot(final String name) {
        if (name.endsWith(".war")) {
            return name.substring(0, name.length() - ".war".length());
        }
        return name;
    }

    private static <T> T get(final Class<T> fileClass, final String attr, final Asset asset) {
        try {
            final Field field = asset.getClass().getDeclaredField(attr);
            field.setAccessible(true);
            return fileClass.cast(field.get(asset));
        } catch (Exception e) {
            return null;
        }
    }

    private static org.apache.xbean.finder.archive.Archive finderArchive(final Node beansXml, final Archive<?> archive, final ClassLoader cl, final Collection<URL> additionalPaths) {
        final List<Class<?>> classes = new ArrayList<Class<?>>();
        final Map<ArchivePath, Node> content = archive.getContent(new IncludeRegExpPaths(".*.class"));
        for (final Map.Entry<ArchivePath, Node> node : content.entrySet()) {
            final String classname = name(node.getKey().get());
            try {
                classes.add(cl.loadClass(classname));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        final Map<URL, List<String>> classesByUrl = new HashMap<URL, List<String>>();

        final List<org.apache.xbean.finder.archive.Archive> archives = new ArrayList<org.apache.xbean.finder.archive.Archive>();
        for (final URL url : DeploymentLoader.filterWebappUrls(additionalPaths.toArray(new URL[additionalPaths.size()]), null)) {
            final List<String> currentClasses = new ArrayList<String>();
            final org.apache.xbean.finder.archive.Archive newArchive = new FilteredArchive(new JarArchive(cl, url), new WebappAggregatedArchive.ScanXmlSaverFilter(false, null, currentClasses));
            classesByUrl.put(url, currentClasses);
            archives.add(newArchive);
        }

        archives.add(new ClassesArchive(classes));
        if (beansXml != null) {
            final List<String> mainClasses = new ArrayList<String>();
            for (final Class<?> clazz : classes) {
                mainClasses.add(clazz.getName());
            }
            // look org.apache.openejb.config.AnnotationDeployer.DiscoverAnnotatedBeans.hasBeansXml()
            try {
                final URL key = new URL("jar:file://!/WEB-INF/classes/"); // no host avoid host resolution in hashcode()
                classesByUrl.put(key, mainClasses);
            } catch (MalformedURLException mue) {
                // no-op
            }
        }

        return new SimpleWebappAggregatedArchive(new CompositeArchive(archives), classesByUrl);
    }

    private static String name(final String raw) {
        String name = raw;
        if (name.startsWith(WEB_INF_CLASSES)) {
            name = name.substring(WEB_INF_CLASSES.length() - 1);
        }
        name = name.replace('/', '.');
        return name.substring(1, name.length() - 6);
    }

    private static class AssetSource implements ReadDescriptors.Source {
        private Asset asset;

        private AssetSource(final Asset asset) {
            this.asset = asset;
        }

        @Override
        public InputStream get() throws IOException {
            return asset.openStream();
        }
    }

    // mainly extended to be sure to reuse our tip about scanning for CDI
    private static class SimpleWebappAggregatedArchive extends WebappAggregatedArchive {
        private final CompositeArchive delegate;
        private final Map<URL, List<String>> classesMap;

        public SimpleWebappAggregatedArchive(final CompositeArchive archive, final Map<URL, List<String>> map) {
            super(Thread.currentThread().getContextClassLoader(), new HashMap<String, Object>(), new ArrayList<URL>());

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
