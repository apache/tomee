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

    private static final String[] JAR_TO_ADD_CLASS_HELPERS;

    private static final String[] DEFAULT_PREFIXES_TO_ADD = new String[]{
            "openwebbeans-jsf", // to be able to provide jsf impl
            "tomee-mojarra",
            "tomee-myfaces", // to be able to embedded myfaces in the webapp
            "openejb-jpa-integration" // to be able to embedded hibernate, eclipselinks....
    };
    private static final String[] PREFIXES_TO_ADD;

    static {
        final Collection<String> classes = new ArrayList<String>();
        final Collection<String> prefixes = new ArrayList<String>();
        if (!SystemInstance.get().getOptions().get(TOMEE_WEBAPP_CLASSLOADER_ENRICHMENT_SKIP, false)) {
            final String additionalEnrichments = SystemInstance.get().getOptions().get(TOMEE_WEBAPP_CLASSLOADER_ENRICHMENT_CLASSES, "");
            if (additionalEnrichments != null && !additionalEnrichments.isEmpty()) {
                for (final String name : additionalEnrichments.split(",")) {
                    classes.add(name.trim());
                }
            }

            final String additionalPrefixes = SystemInstance.get().getOptions().get(TOMEE_WEBAPP_CLASSLOADER_ENRICHMENT_PREFIXES, "");
            prefixes.addAll(Arrays.asList(DEFAULT_PREFIXES_TO_ADD));
            if (additionalPrefixes != null && !additionalPrefixes.isEmpty()) {
                for (final String name : additionalPrefixes.split(",")) {
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
            for (final String name : JAR_TO_ADD_CLASS_HELPERS) {
                try { // don't do anything with appCl otherwise in tomcat it will be broken since WebAppClassLoader caches missed resources
                    final String classFileName = name.replace(".", "/") + ".class";
                    final URL parentUrl = cl.getResource(classFileName);
                    final File file = jarLocation(parentUrl, classFileName);
                    if (file == null) {
                        continue;
                    }
                    urls.add(file.toURI().toURL());
                } catch (final Exception | NoClassDefFoundError e) {
                    // ignore
                }
            }
        }

        // from prefix
        final Paths paths = new Paths(new File(System.getProperty("openejb.home"))); // parameter is useless
        for (final String prefix : PREFIXES_TO_ADD) {
            final File file = paths.findTomEELibJar(prefix);
            if (file != null) {
                try {
                    urls.add(file.toURI().toURL());
                } catch (final MalformedURLException e) {
                    // ignored
                }
            }
        }

        // from config
        urls.addAll(Arrays.asList(SystemInstance.get().getComponent(ClassLoaderEnricher.class).applicationEnrichment()));

        return urls.toArray(new URL[urls.size()]);
    }

    private static File jarLocation(final URL url, final String classFileName) throws MalformedURLException {
        if ("jar".equals(url.getProtocol())) {
            final String spec = url.getFile();
            return new File(JarLocation.decode(new URL(spec.substring(0, spec.indexOf('!'))).getFile()));
        } else if ("file".equals(url.getProtocol())) {
            return JarLocation.toFile(classFileName, url);
        }
        return null;
    }

    /**
     * Validation part
     */
    private static final String[][] FORBIDDEN_CLASSES = new String[][]{ // name, spec +1 name, message. Note: [1] != null => [2] != null
            {"javax.persistence.Entity", null, null}, // JPA
            {"javax.transaction.Transaction", null, null}, // JTA
            {"javax.jws.WebService", null, null}, // JAXWS
            {"javax.validation.Validation", null, null}, // BVal
            {"javax.jms.Queue", null, null}, // JMS
            {"javax.enterprise.context.ApplicationScoped", null, null}, // CDI
            {"javax.inject.Inject", null, null}, // CDI
            {"javax.ws.rs.Path", null, null},
            {"javax.ejb.EJB", null, null}, // EJB
            {"javax.annotation.PostConstruct", "javax.annotation.Priority", "You provide javax.annotation API 1.2 so we'll tolerate new classes but it should surely be upgraded in the container"} // javax.annotation
    };

    public static boolean validateJarFile(final File file) throws IOException {
        final ClassLoader parent = TomEEClassLoaderEnricher.class.getClassLoader();

        JarFile jarFile = null;

        try {
            jarFile = new JarFile(file);
            for (final String[] name : FORBIDDEN_CLASSES) {
                // if we can't load if from our classLoader we'll not impose anything on this class
                boolean found = false;
                for (int i = 0; i < 2; i++) {
                    try {
                        try {
                            parent.loadClass(name[0]);
                            found = true;
                            break;
                        } catch (final Exception e) {
                            // found = false
                        }
                    } catch (final LinkageError le) { // would be a pain to fail here
                        // retry
                    }
                }

                if (!found) {
                    continue;
                }

                // we found it so let's check it is or not in the file (potential conflict)
                final String entry = name[0].replace('.', '/') + ".class";
                final JarEntry jarEntry = jarFile.getJarEntry(entry);
                if (jarEntry != null) {
                    if (name[1] != null) {
                        if (jarFile.getJarEntry(name[1].replace('.', '/') + ".class") != null) {
                            LOGGER.warning("jar '" + file.getAbsolutePath() + "' contains offending class: " + name[0]
                                    + "but: " + name[2]);
                            return true;
                        }
                    }
                    LOGGER.warning("jar '" + file.getAbsolutePath() + "' contains offending class: " + name[0]
                            + ". It will be ignored.");
                    return !"true".equals(SystemInstance.get().getProperty("openejb.api." + name[0] + ".validation", "true"));
                }
            }
            return true;
        } finally {
            if (jarFile != null) { // in java 6 JarFile is not Closeable so don't use IO.close()
                try {
                    jarFile.close();
                } catch (final IOException ioe) {
                    // Ignored
                }
            }
        }
    }
}
