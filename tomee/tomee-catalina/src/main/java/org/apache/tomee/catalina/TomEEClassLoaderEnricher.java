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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.catalina;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.classloader.WebAppEnricher;
import org.apache.openejb.component.ClassLoaderEnricher;
import org.apache.openejb.loader.JarLocation;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomee.installer.Paths;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class TomEEClassLoaderEnricher implements WebAppEnricher {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, TomEEClassLoaderEnricher.class);

    /**
     * Enrichement part
     */

    public static final String TOMEE_WEBAPP_CLASSLOADER_ENRICHMENT_SKIP = "tomee.webapp.classloader.enrichment.skip";

    public static final String TOMEE_WEBAPP_CLASSLOADER_ENRICHMENT_CLASSES = "tomee.webapp.classloader.enrichment.classes";
    public static final String TOMEE_WEBAPP_CLASSLOADER_ENRICHMENT_PREFIXES = "tomee.webapp.classloader.enrichment.prefixes";

    private static final String[] DEFAULT_CLASSES_WHICH_CAN_BE_LOADED_FROM_APP_ONLY = new String[] {
            // openwebbeans-jsf to be able to embedded the jsf impl keeping CDI features
            "org.apache.webbeans.jsf.OwbApplicationFactory"
    };
    private static final String[] JAR_TO_ADD_CLASS_HELPERS;

    private static final String[] DEFAULT_PREFIXES_TO_ADD = new String[] { // always added since only used with loadClass
            "tomee-mojarra",
            "openejb-jpa-integration"
    };
    private static final String[] PREFIXES_TO_ADD;

    static {
        final Collection<String> classes = new ArrayList<String>();
        final Collection<String> prefixes = new ArrayList<String>();
        if (!SystemInstance.get().getOptions().get(TOMEE_WEBAPP_CLASSLOADER_ENRICHMENT_SKIP, false)) {
            final String additionalEnrichments = SystemInstance.get().getOptions().get(TOMEE_WEBAPP_CLASSLOADER_ENRICHMENT_CLASSES, "");
            classes.addAll(Arrays.asList(DEFAULT_CLASSES_WHICH_CAN_BE_LOADED_FROM_APP_ONLY));
            if (additionalEnrichments != null && !additionalEnrichments.isEmpty()) {
                for (String name : additionalEnrichments.split(",")) {
                    classes.add(name.trim());
                }
            }

            final String additionalPrefixes = SystemInstance.get().getOptions().get(TOMEE_WEBAPP_CLASSLOADER_ENRICHMENT_PREFIXES, "");
            prefixes.addAll(Arrays.asList(DEFAULT_PREFIXES_TO_ADD));
            if (additionalPrefixes != null && !additionalPrefixes.isEmpty()) {
                for (String name : additionalPrefixes.split(",")) {
                    prefixes.add(name.trim());
                }
            }
        }
        JAR_TO_ADD_CLASS_HELPERS = classes.toArray(new String[classes.size()]);
        PREFIXES_TO_ADD = prefixes.toArray(new String[prefixes.size()]);
    }

    @Override
    public URL[] enrichment(final ClassLoader appCl) {
        final Collection<URL> urls = new HashSet<URL>();

        // from class
        final ClassLoader cl = TomEEClassLoaderEnricher.class.getClassLoader(); // reference classloader = standardclassloader
        if (cl != appCl && appCl != null) {
            for (String name : JAR_TO_ADD_CLASS_HELPERS) {
                try {
                    final Class<?> clazz = cl.loadClass(name);
                    if (!clazz.getClassLoader().equals(OpenEJB.class.getClassLoader())) { // already provided?
                        continue;
                    }

                    // don't create a list here to loop only once to avoid to allocate memory for nothing

                    boolean add = false;
                    for (Class<?> itf : clazz.getInterfaces()) {
                        try {
                            final Class<?> tcclLoaded = appCl.loadClass(itf.getName());
                            if (!tcclLoaded.getClassLoader().equals(cl)) {
                                add = true;
                                break;
                            }
                        } catch (Exception e) {
                            // ignored
                        }
                    }

                    Class<?> current = clazz.getSuperclass();
                    while (current != null && !Object.class.equals(current)) {
                        try {
                            final Class<?> tcclLoaded = appCl.loadClass(current.getName());
                            if (!tcclLoaded.getClassLoader().equals(cl)) {
                                add = true;
                                break;
                            }
                        } catch (Exception cnfe) {
                            // ignored
                        }
                        current = current.getSuperclass();
                    }

                    if (!add) {
                        continue;
                    }

                    final URL url = JarLocation.jarLocation(clazz).toURI().toURL();
                    if (url == null) {
                        continue;
                    }

                    urls.add(url);
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        // from prefix
        final Paths paths = new Paths(new File(System.getProperty("openejb.home"))); // parameter is useless
        for (String prefix : PREFIXES_TO_ADD) {
            final File file = paths.findTomEELibJar(prefix);
            if (file != null) {
                try {
                    urls.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    // ignored
                }
            }
        }

        // from config
        urls.addAll(Arrays.asList(SystemInstance.get().getComponent(ClassLoaderEnricher.class).applicationEnrichment()));

        return urls.toArray(new URL[urls.size()]);
    }

    /**
     * Validation part
     */
    private static final String[] FORBIDDEN_CLASSES = new String[]{
            "javax.persistence.Entity", // JPA
            "javax.transaction.Transaction", // JTA
            "javax.jws.WebService", // JAXWS
            "javax.validation.Validation", // BVal
            "javax.jms.Queue", // JMS
            "javax.enterprise.context.ApplicationScoped", // CDI
            "javax.inject.Inject", // CDI
            // "javax.ws.rs.Path", // JAXRS - commented since we manage to find why jersey-core brings the api!
            "javax.ejb.EJB", // EJB
            "javax.annotation.PostConstruct" // javax.annotation
    };

    public static boolean validateJarFile(final File file) throws IOException {
        final ClassLoader parent = TomEEClassLoaderEnricher.class.getClassLoader();

        JarFile jarFile = null;

        try {
            jarFile = new JarFile(file);
            for (String name : FORBIDDEN_CLASSES) {
                // if we can't load if from our classLoader we'll not impose anything on this class
                boolean found = false;
                for (int i = 0; i < 2; i++) {
                    try {
                        try {
                            parent.loadClass(name);
                            found = true;
                            break;
                        } catch (Exception e) {
                            // found = false
                        }
                    } catch (LinkageError le) { // would be a pain to fail here
                        // retry
                    }
                }

                if (!found) {
                    continue;
                }

                // we found it so let's check it is or not in the file (potential conflict)
                final String entry = name.replace('.', '/') + ".class";
                final JarEntry jarEntry = jarFile.getJarEntry(entry);
                if (jarEntry != null) {
                    LOGGER.warning("jar '" + file.getAbsolutePath() + "' contains offending class: " + name
                                                + ". It will be ignored.");
                    return false;
                }
            }
            return true;
        } finally {
            if (jarFile != null) { // in java 6 JarFile is not Closeable so don't use IO.close()
                try {
                    jarFile.close();
                } catch (IOException ioe) {
                    // Ignored
                }
            }
        }
    }
}
