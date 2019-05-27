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

package org.apache.openejb.assembler.classic;

import org.apache.openejb.config.event.BeforeDeploymentEvent;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.util.JarCreator;
import org.apache.openejb.util.JarExtractor;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Saxs;
import org.apache.openejb.util.URLs;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class DeployTimeEnhancer {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_DEPLOY, DeployTimeEnhancer.class);

    private static final String OPENEJB_JAR_ENHANCEMENT_INCLUDE = "openejb.jar.enhancement.include";
    private static final String OPENEJB_JAR_ENHANCEMENT_EXCLUDE = "openejb.jar.enhancement.exclude";

    private static final String CLASS_EXT = ".class";
    private static final String PROPERTIES_FILE_PROP = "propertiesFile";
    private static final String META_INF_PERSISTENCE_XML = "META-INF/persistence.xml";
    private static final String TMP_ENHANCEMENT_SUFFIX = ".tmp-enhancement";

    private final Method enhancerMethod;
    private final Constructor<?> optionsConstructor;

    public DeployTimeEnhancer() {
        Method mtd;
        Constructor<?> cstr;
        final ClassLoader cl = DeployTimeEnhancer.class.getClassLoader();
        try {
            final Class<?> enhancerClass = cl.loadClass("org.apache.openjpa.enhance.PCEnhancer");
            final Class<?> arg2 = cl.loadClass("org.apache.openjpa.lib.util.Options");
            cstr = arg2.getConstructor(Properties.class);
            mtd = enhancerClass.getMethod("run", String[].class, arg2);
        } catch (final Exception e) {
            LOGGER.warning("openjpa enhancer can't be found in the container, will be skipped");
            mtd = null;
            cstr = null;
        }
        optionsConstructor = cstr;
        enhancerMethod = mtd;
    }

    public void enhance(@Observes final BeforeDeploymentEvent event) {
        if (enhancerMethod == null) {
            LOGGER.debug("OpenJPA is not available so no deploy-time enhancement will be done");
            return;
        }

        // find persistence.xml
        final Map<String, List<String>> classesByPXml = new HashMap<>();
        final List<URL> usedUrls = new ArrayList<>(); // for fake classloader
        for (final URL url : event.getUrls()) {
            final File file = URLs.toFile(url);
            if (file.isDirectory()) {
                final String pXmls = getWarPersistenceXml(url);
                if (pXmls != null) {
                    feed(classesByPXml, pXmls);
                }

                usedUrls.add(url);
            } else if (file.getName().endsWith(".jar")) {
                try (JarFile jar = new JarFile(file)) {
                    final ZipEntry entry = jar.getEntry(META_INF_PERSISTENCE_XML);
                    if (entry != null) {
                        final String path = file.getAbsolutePath();
                        final File unpacked = new File(path.substring(0, path.length() - 4) + TMP_ENHANCEMENT_SUFFIX);
                        JarExtractor.extract(file, unpacked);

                        // replace jar by folder url since otherwise enhancement doesn't work
                        usedUrls.add(unpacked.toURI().toURL());

                        feed(classesByPXml, new File(unpacked, META_INF_PERSISTENCE_XML).getAbsolutePath());
                    } else {
                        usedUrls.add(url);
                    }
                } catch (final IOException e) {
                    // ignored
                }
                // no-op
            } else {
                usedUrls.add(url);
            }
        }

        // enhancement

        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        final ClassLoader fakeClassLoader = new URLClassLoaderFirst(usedUrls.toArray(new URL[usedUrls.size()]), event.getParentClassLoader());

        LOGGER.info("Enhancing url(s): " + usedUrls);

        Thread.currentThread().setContextClassLoader(fakeClassLoader);
        try {
            for (final Map.Entry<String, List<String>> entry : classesByPXml.entrySet()) {
                final Properties opts = new Properties();
                opts.setProperty(PROPERTIES_FILE_PROP, entry.getKey());

                final Object optsArg;
                try {
                    optsArg = optionsConstructor.newInstance(opts);
                } catch (final Exception e) {
                    LOGGER.debug("can't create options for enhancing");
                    return;
                }

                final String[] args = toFilePaths(entry.getValue());
                LOGGER.info("Enhancing: " + Arrays.asList(args));

                try {
                    enhancerMethod.invoke(null, args, optsArg);
                } catch (final Exception e) {
                    LOGGER.warning("can't enhanced at deploy-time entities", e);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
            usedUrls.clear();
        }

        // clean up extracted jars and replace jar to keep consistent classloading
        for (final Map.Entry<String, List<String>> entry : classesByPXml.entrySet()) {
            final List<String> values = entry.getValue();
            for (final String rawPath : values) {
                if (rawPath.endsWith(TMP_ENHANCEMENT_SUFFIX + "/") || rawPath.endsWith(TMP_ENHANCEMENT_SUFFIX)) {
                    final File dir = new File(rawPath);
                    final File file = new File(rawPath.substring(0, rawPath.length() - TMP_ENHANCEMENT_SUFFIX.length() - 1) + ".jar");
                    if (file.exists()) {
                        String name = dir.getName();
                        name = name.substring(0, name.length() - TMP_ENHANCEMENT_SUFFIX.length()) + ".jar";

                        final File target = new File(dir.getParentFile(), name);
                        try { // override existing jar otherwise classloading is broken in tomee
                            Files.delete(file);
                            JarCreator.jarDir(dir, target);
                        } catch (final IOException e) {
                            LOGGER.error("can't repackage enhanced jar file " + file.getName());
                        }
                        Files.delete(dir);
                    }
                }
            }
            values.clear();
        }

        classesByPXml.clear();
    }

    private void feed(final Map<String, List<String>> classesByPXml, final String pXml) {
        final List<String> paths = new ArrayList<>();

        // first add the classes directory where is the persistence.xml
        if (pXml.endsWith(META_INF_PERSISTENCE_XML)) {
            paths.add(pXml.substring(0, pXml.length() - META_INF_PERSISTENCE_XML.length()));
        } else if (pXml.endsWith("/WEB-INF/persistence.xml")) {
            paths.add(pXml.substring(0, pXml.length() - 24));
        }

        // then jar-file
        try {
            final SAXParser parser = Saxs.factory().newSAXParser();
            final JarFileParser handler = new JarFileParser();
            parser.parse(new File(pXml), handler);
            for (final String path : handler.getPaths()) {
                paths.add(relative(paths.iterator().next(), path));
            }
        } catch (final Exception e) {
            LOGGER.error("can't parse '" + pXml + "'", e);
        }

        classesByPXml.put(pXml, paths);
    }

    // relativePath = relative path to the jar file containing the persistence.xml
    private String relative(final String relativePath, final String pXmlPath) {
        return new File(new File(pXmlPath).getParent(), relativePath).getAbsolutePath();
    }

    private String getWarPersistenceXml(final URL url) {
        final File dir = URLs.toFile(url);
        if (dir.isDirectory() && (dir.getAbsolutePath().endsWith("/WEB-INF/classes") || dir.getAbsolutePath().endsWith("/WEB-INF/classes/"))) {
            final File pXmlStd = new File(dir.getParentFile(), "persistence.xml");
            if (pXmlStd.exists()) {
                return pXmlStd.getAbsolutePath();
            }

            final File pXml = new File(dir, META_INF_PERSISTENCE_XML);
            if (pXml.exists()) {
                return pXml.getAbsolutePath();
            }
        }
        return null;
    }

    private String[] toFilePaths(final List<String> urls) {
        final List<String> files = new ArrayList<>();
        for (final String url : urls) {
            final File dir = new File(url);
            if (!dir.isDirectory()) {
                continue;
            }

            for (final File f : Files.collect(dir, new ClassFilter())) {
                files.add(f.getAbsolutePath());
            }
        }
        return files.toArray(new String[files.size()]);
    }

    private static class ClassFilter implements FileFilter {
        private static final String DEFAULT_INCLUDE = "\\*";
        private static final String DEFAULT_EXCLUDE = "";
        private static final Pattern INCLUDE_PATTERN = Pattern.compile(SystemInstance.get().getOptions().get(OPENEJB_JAR_ENHANCEMENT_INCLUDE, DEFAULT_INCLUDE));
        private static final Pattern EXCLUDE_PATTERN = Pattern.compile(SystemInstance.get().getOptions().get(OPENEJB_JAR_ENHANCEMENT_EXCLUDE, DEFAULT_EXCLUDE));

        @Override
        public boolean accept(final File file) {
            final boolean isClass = file.getName().endsWith(CLASS_EXT);
            if (DEFAULT_EXCLUDE.equals(EXCLUDE_PATTERN.pattern()) && DEFAULT_INCLUDE.equals(INCLUDE_PATTERN.pattern())) {
                return isClass;
            }

            final String path = file.getAbsolutePath();
            return isClass && INCLUDE_PATTERN.matcher(path).matches() && !EXCLUDE_PATTERN.matcher(path).matches();
        }
    }

    private static class JarFileParser extends DefaultHandler {
        private final List<String> paths = new ArrayList<>();
        private boolean getIt;

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes att) throws SAXException {
            if (!localName.endsWith("jar-file")) {
                return;
            }

            getIt = true;
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            if (getIt) {
                paths.add(String.valueOf(ch, start, length));
            }
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            getIt = false;
        }

        public List<String> getPaths() {
            return paths;
        }
    }
}
