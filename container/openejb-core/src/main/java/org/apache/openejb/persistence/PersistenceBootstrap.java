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

package org.apache.openejb.persistence;

import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.core.TempClassLoader;
import org.apache.openejb.javaagent.Agent;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.Saxs;
import org.apache.xbean.finder.ClassLoaders;
import org.apache.xbean.finder.UrlSet;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceProvider;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.openejb.loader.JarLocation.decode;

/**
 * The goal of this class is to support persistence providers that need to do
 * byte code enhancement in embedded environments like JUnit where all the
 * entity classes are right on the system classpath and likely to be loaded
 * before OpenEJB boots.  The org.apache.openejb.javaagent.Agent class calls
 * the bootstrap() method of this class the first time it finds a classloader
 * that has the openejb-core jar.  We then do a quick scan of the classpath
 * looking for persistence.xml files and attempt to get the related persistence
 * providers to setup the correct byte code enhancing for the classes listed
 * in the persistence.xml files.
 *
 * @version $Rev$ $Date$
 */
public class PersistenceBootstrap {

    public static final String DEFAULT_PROVIDER = getDefaultProvider();
    private static volatile boolean systemDone;

    private static String getDefaultProvider() { // TODO: we shouldn't use a logger here, too early!
        final Class<PersistenceBootstrap> clzz = PersistenceBootstrap.class;
        final String name = "/META-INF/" + clzz.getName() + ".provider";

        try {
            final URL provider = clzz.getResource(name);
            if (provider != null) {
                final String trim = IO.slurp(provider).trim();
                Logger.getLogger(PersistenceBootstrap.class.getName()).info("Default JPA Provider changed to " + trim + " specified by " + provider.toExternalForm());
                return trim;
            }
        } catch (final Exception e) {
            Logger.getLogger(PersistenceBootstrap.class.getName()).log(Level.WARNING, "Could not read " + name, e);
        }

        return "org.apache.openjpa.persistence.PersistenceProviderImpl";
    }

    private static boolean debug;

    public static void bootstrap(final ClassLoader classLoader) {
        if (classLoader == PersistenceBootstrap.class.getClassLoader()/*virtual system loader*/) {
            // no need to sync it otherwise far worse things should get sync
            if (systemDone) {
                return;
            }
            systemDone = true;
        }

        final Properties args = getAgentArgs(classLoader);

        debug = args.getProperty("debug", "false").equalsIgnoreCase("true");
        final boolean enabled = args.getProperty("enabled", "true").equalsIgnoreCase("true");

        if (!enabled) {
            debug("disabled");
            return;
        }

        try {
            debug("searching for persistence.xml files");

            // create persistence.xml names respecting altdd
            final Collection<String> pXmlNames = new ArrayList<>();

            // altdd logic duplicated to avoid classloading issue in tomee-webapp mode
            final String altDD = getAltDD();
            if (altDD != null) {
                for (final String p : altDD.split(",")) {
                    pXmlNames.add(p + ".persistence.xml");
                    pXmlNames.add(p + "-persistence.xml");
                }
            } else {
                pXmlNames.add("persistence.xml");
            }

            final List<URL> urls = new LinkedList<>();
            for (final String pXmlName : pXmlNames) { // find persistence.xml in the classloader and in WEB-INF
                urls.addAll(Collections.list(classLoader.getResources("META-INF/" + pXmlName)));
                if ("true".equals(args.getProperty("web-scan", "false"))) { // findUrls is slow for small tests and rarely needed
                    try {
                        final Collection<URL> loaderUrls = findUrls(classLoader, args);
                        for (final URL url : loaderUrls) {
                            final File file = toFile(url);
                            if ("classes".equals(file.getName()) && "WEB-INF".equals(file.getParentFile().getName())) {
                                final File pXml = new File(file.getParentFile(), pXmlName);
                                if (pXml.exists()) {
                                    urls.add(pXml.toURI().toURL());
                                }
                                break;
                            }
                            if (file.getName().endsWith(".jar") && file.getParentFile().getName().equals("lib") && "WEB-INF".equals(file.getParentFile().getParentFile().getName())) {
                                final File pXml = new File(file.getParentFile().getParentFile(), pXmlName);
                                if (pXml.exists()) {
                                    urls.add(pXml.toURI().toURL());
                                }
                                break;
                            }
                        }
                    } catch (final Throwable th) {
                        // no-op
                    }
                }
            }

            if (urls.size() == 0) {
                debug("no persistence.xml files found");
                return;
            }

            final Map<String, Unit> units = new HashMap<>();

            for (final URL url : urls) {
                final String urlPath = url.toExternalForm();
                debug("found " + urlPath);
                try {
                    final InputStream in = IO.read(url);
                    try {
                        collectUnits(in, units, args);
                    } catch (final Throwable e) {
                        debug("failed to read " + urlPath, e);
                        in.close();
                    }
                } catch (final Throwable e) {
                    debug("failed to read " + urlPath, e);
                }
            }

            for (final Unit unit : units.values()) {
                final String provider = unit.provider;
                final String extraClassesKey = provider + "@classes";
                final String unitNameKey = provider + "@unitName";
                final String unitName = args.getProperty(unitNameKey, "classpath-bootstrap");

                final String classes = args.getProperty(extraClassesKey);
                if (classes != null) {
                    debug("parsing value of " + extraClassesKey);

                    try {
                        final List<String> list = Arrays.asList(classes.split("[ \n\r\t,]"));
                        unit.classes.addAll(list);
                    } catch (final Exception e) {
                        debug("cannot parse: " + classes, e);
                    }
                }
                try {
                    // Hibernate doesn't use byte code modification
                    if (provider.startsWith("org.hibernate")) {
                        debug("skipping: " + provider);
                        continue;
                    } else {
                        debug("starting: " + provider);
                    }

                    final PersistenceUnitInfoImpl info = new PersistenceUnitInfoImpl(new Handler());
                    info.setManagedClassNames(new ArrayList(unit.classes));
                    info.setPersistenceProviderClassName(unit.provider);
                    info.setProperties(new Properties());
                    info.setId(unitName);
                    info.setPersistenceUnitName(unitName);
                    info.setRootUrlAndJarUrls("", Collections.EMPTY_LIST);
                    info.setJtaDataSource(new NullDataSource());
                    info.setNonJtaDataSource(new NullDataSource());
                    info.setExcludeUnlistedClasses(true);
                    info.setClassLoader(classLoader);

                    for (final String name : unit.classes) {
                        debug("class " + name);
                    }
                    final Class clazz = classLoader.loadClass(unit.provider);
                    final PersistenceProvider persistenceProvider = (PersistenceProvider) clazz.newInstance();

                    // Create entity manager factory
                    final EntityManagerFactory emf = persistenceProvider.createContainerEntityManagerFactory(info, new HashMap());
                    emf.close();
                    debug("success: " + provider);
                } catch (final Throwable e) {
                    debug("failed: " + provider, e);
                }
            }
        } catch (final Throwable t) {
            debug("error: ", t);
        }
    }

    private static Set<URL> findUrls(final ClassLoader classLoader, final Properties args) throws IOException {
        if ("true".equals(args.getProperty("fast-scan", "true"))) {
            try {
                return new HashSet<>(NewLoaderLogic.applyBuiltinExcludes(new UrlSet(ClassLoaders.findUrls(classLoader)).excludeJvm()).getUrls());
            } catch (final Throwable fallback) {
                // let it fallback
            }
        }
        return ClassLoaders.findUrls(classLoader);
    }

    // don't force eager init
    private static String getAltDD() {
        final String property = "openejb.altdd.prefix";
        if (SystemInstance.isInitialized()) {
            return SystemInstance.get().getOptions().get(property, (String) null);
        }
        return JavaSecurityManagers.getSystemProperty(property);
    }

    private static void debug(final String x) {
        if (debug) {
            System.out.println("[PersistenceBootstrap] " + x);
        }
    }

    private static void debug(final String x, final Throwable t) {
        if (debug) {
            System.out.println(x);
            t.printStackTrace();
        }
    }

    private static Properties getAgentArgs(final ClassLoader classLoader) {
        final Properties properties = new Properties();
        final String args = Agent.getAgentArgs();
        if (args != null && args.length() != 0) {
            for (final String string : args.split("[ ,:&]")) {
                final String[] strings = string.split("=");
                if (strings.length == 2) {
                    properties.put(strings[0], strings[1]);
                }
            }

            debug = properties.getProperty("debug", "false").equalsIgnoreCase("true");

        }

        try {
            final URL resource = classLoader.getResource("PersistenceBootstrap.properties");
            if (resource != null) {
                debug("found PersistenceBootstrap.properties file");
                IO.readProperties(resource, properties);
            }
        } catch (final Throwable e) {
            debug("can't read PersistenceBootstrap.properties file", e);
        }

        return properties;
    }

    private static void collectUnits(final InputStream in, final Map<String, Unit> units, final Properties args) throws ParserConfigurationException, SAXException, IOException {
        final InputSource inputSource = new InputSource(in);

        final SAXParser parser = Saxs.namespaceAwareFactory().newSAXParser();

        parser.parse(inputSource, new DefaultHandler() {
            private final StringBuilder characters = new StringBuilder(100);
            private Unit unit;

            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
                characters.setLength(0);

                if (localName.equals("persistence-unit")) {
                    startPersistenceUnit(uri, localName, qName, attributes);
                }
            }

            public void startPersistenceUnit(final String uri, final String localName, final String qName, final Attributes attributes) {
                final String unitName = attributes.getValue("name");
                unit = new Unit(unitName);
            }

            public void characters(final char[] ch, final int start, final int length) {
                final String text = new String(ch, start, length);
                characters.append(text.trim());
            }

            public void endElement(final String uri, final String localName, final String qName) {
                switch (localName) {
                    case "persistence-unit":
                        endPersistenceUnit(uri, localName, qName);
                        break;
                    case "provider":
                        endProvider(uri, localName, qName);
                        break;
                    case "class":
                        endClass(uri, localName, qName);
                        break;
                }
            }

            public void endPersistenceUnit(final String uri, final String localName, final String qName) {
                if (args.getProperty(unit.name + "@skip", "false").equalsIgnoreCase("true")) {
                    debug("skipping unit " + unit.name);
                } else {
                    debug("adding unit " + unit.name);

                    if (unit.provider == null) {
                        unit.provider = DEFAULT_PROVIDER;
                    }

                    final Unit u = units.get(unit.provider);
                    if (u == null) {
                        units.put(unit.provider, unit);
                    } else {
                        u.classes.addAll(unit.classes);
                    }
                }

                unit = null;
            }

            public void endProvider(final String uri, final String localName, final String qName) {
                unit.provider = characters.toString();
            }

            public void endClass(final String uri, final String localName, final String qName) {
                unit.classes.add(characters.toString());
            }
        });
    }

    public static File toFile(final URL url) {
        if ("jar".equals(url.getProtocol())) {
            try {
                final String spec = url.getFile();

                int separator = spec.indexOf('!');
                /*
                 * REMIND: we don't handle nested JAR URLs
                 */
                if (separator == -1) {
                    throw new MalformedURLException("no ! found in jar url spec:" + spec);
                }

                return toFile(new URL(spec.substring(0, separator++)));
            } catch (final MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        } else if ("file".equals(url.getProtocol())) {
            String path = decode(url.getFile());
            if (path.endsWith("!")) {
                path = path.substring(0, path.length() - 1);
            }
            return new File(path);
        } else {
            throw new IllegalArgumentException("Unsupported URL scheme: " + url.toExternalForm());
        }
    }

    private static class Unit {
        private String provider;
        private final Set<String> classes = new HashSet<>();
        private final String name;

        public Unit(final String name) {
            this.name = name;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(final String provider) {
            this.provider = provider;
        }
    }

    private static class Handler implements PersistenceClassLoaderHandler {
        public void addTransformer(final String unitId, final ClassLoader classLoader, final ClassFileTransformer classFileTransformer) {
            final Instrumentation instrumentation = Agent.getInstrumentation();
            if (instrumentation != null) {
                instrumentation.addTransformer(new Transformer(classFileTransformer));
            }
        }

        public void destroy(final String unitId) {
        }

        public ClassLoader getNewTempClassLoader(final ClassLoader classLoader) {
            return new TempClassLoader(classLoader);
        }

    }

    public static class Transformer implements ClassFileTransformer {
        private final ClassFileTransformer transformer;

        public Transformer(final ClassFileTransformer transformer) {
            this.transformer = transformer;
        }

        public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
            try {
                final byte[] bytes = transformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
                if (bytes != null) {
                    debug("enhanced " + className);
                }
                return bytes;
            } catch (final Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private static class NullDataSource implements DataSource {
        public Connection getConnection() throws SQLException {
            return null;
        }

        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return null;
        }

        public Connection getConnection(final String username, final String password) throws SQLException {
            return null;
        }

        public int getLoginTimeout() throws SQLException {
            return 0;
        }

        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }

        public void setLoginTimeout(final int seconds) throws SQLException {
        }

        public void setLogWriter(final PrintWriter out) throws SQLException {
        }

        public boolean isWrapperFor(final Class<?> iface) throws SQLException {
            return false;
        }

        public <T> T unwrap(final Class<T> iface) throws SQLException {
            throw new SQLException();
        }
    }
}
