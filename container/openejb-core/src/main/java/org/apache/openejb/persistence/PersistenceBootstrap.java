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

import org.apache.openejb.core.TempClassLoader;
import org.apache.openejb.javaagent.Agent;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.ProtectionDomain;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Arrays;
import java.util.List;

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

    private static final String defaultProvider = "org.apache.openjpa.persistence.PersistenceProviderImpl";
    private static boolean debug;

    public static void bootstrap(ClassLoader classLoader) {
        Properties args = getAgentArgs(classLoader);

        debug = (args.getProperty("debug", "false").equalsIgnoreCase("true"));
        boolean enabled = (args.getProperty("enabled", "true").equalsIgnoreCase("true"));

        if (!enabled) {
            debug("disabled");
            return;
        }

        try {
            debug("searching for persistence.xml files");

            ArrayList<URL> urls = Collections.list(classLoader.getResources("META-INF/persistence.xml"));

            if (urls.size() == 0) {
                debug("no persistence.xml files found");
                return;
            }

            Map<String, Unit> units = new HashMap<String, Unit>();

            for (URL url : urls) {
                String urlPath = url.toExternalForm();
                debug("found " + urlPath);
                try {
                    InputStream in = url.openStream();
                    try {
                        collectUnits(in, units, args);
                    } catch (Throwable e) {
                        debug("failed to read " + urlPath, e);
                        in.close();
                    }
                } catch (Throwable e) {
                    debug("failed to read " + urlPath, e);
                }
            }

            for (Unit unit : units.values()) {
                String provider = unit.provider;
                String extraClassesKey = provider + "@classes";
                String unitNameKey = provider + "@unitName";
                String unitName = args.getProperty(unitNameKey, "classpath-bootstrap");

                String classes = args.getProperty(extraClassesKey);
                if (classes != null) {
                    debug("parsing value of " + extraClassesKey);

                    try {
                        List<String> list = Arrays.asList(classes.split("[ \n\r\t,]"));
                        unit.classes.addAll(list);
                    } catch (Exception e) {
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

                    PersistenceUnitInfoImpl info = new PersistenceUnitInfoImpl(new Handler());
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

                    for (String name : unit.classes) {
                        debug("class " + name);
                    }
                    Class clazz = classLoader.loadClass(unit.provider);
                    PersistenceProvider persistenceProvider = (PersistenceProvider) clazz.newInstance();

                    // Create entity manager factory
                    EntityManagerFactory emf = persistenceProvider.createContainerEntityManagerFactory(info, new HashMap());
                    debug("success: " + provider);
                } catch (Throwable e) {
                    debug("failed: " + provider, e);
                }
            }
        } catch (Throwable t) {
            debug("error: ", t);
        }
    }

    private static void debug(String x) {
        if (debug) System.out.println("[PersistenceBootstrap] " + x);
    }

    private static void debug(String x, Throwable t) {
        if (debug) {
            System.out.println(x);
            t.printStackTrace();
        }
    }

    private static Properties getAgentArgs(ClassLoader classLoader) {
        Properties properties = new Properties();
        String args = Agent.getAgentArgs();
        if (args != null && args.length() != 0) {
            for (String string : args.split("[ ,:&]")) {
                String[] strings = string.split("=");
                if (strings != null && strings.length == 2) {
                    properties.put(strings[0], strings[1]);
                }
            }

            debug = (properties.getProperty("debug", "false").equalsIgnoreCase("true"));

        }

        try {
            URL resource = classLoader.getResource("PersistenceBootstrap.properties");
            if (resource != null) {
                debug("found PersistenceBootstrap.properties file");
                InputStream in = resource.openStream();
                try {
                    properties.load(in);
                } finally {
                    if (in != null) in.close();
                }
            }
        } catch (Throwable e) {
            debug("can't read PersistenceBootstrap.properties file", e);
        }

        return properties;
    }

    private static void collectUnits(InputStream in, final Map<String, Unit> units, final Properties args) throws ParserConfigurationException, SAXException, IOException {
        InputSource inputSource = new InputSource(in);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();


        parser.parse(inputSource, new DefaultHandler() {
            private StringBuilder characters = new StringBuilder();
            private Unit unit;

            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                characters = new StringBuilder(100);

                if (localName.equals("persistence-unit")) startPersistenceUnit(uri, localName, qName, attributes);
            }

            public void startPersistenceUnit(String uri, String localName, String qName, Attributes attributes) {
                String unitName = attributes.getValue(null, "name");
                unit = new Unit(unitName);
            }

            public void characters(char ch[], int start, int length) {
                String text = new String(ch, start, length);
                characters.append(text.trim());
            }

            public void endElement(String uri, String localName, String qName) {
                if (localName.equals("persistence-unit")) endPersistenceUnit(uri, localName, qName);
                else if (localName.equals("provider")) endProvider(uri, localName, qName);
                else if (localName.equals("class")) endClass(uri, localName, qName);
            }

            public void endPersistenceUnit(String uri, String localName, String qName) {
                if (args.getProperty(unit.name + "@skip", "false").equalsIgnoreCase("true")) {
                    debug("skipping unit " + unit.name);
                } else {
                    debug("adding unit " + unit.name);

                    if (unit.provider == null) {
                        unit.provider = defaultProvider;
                    }

                    Unit u = units.get(unit.provider);
                    if (u == null) {
                        units.put(unit.provider, unit);
                    } else {
                        u.classes.addAll(unit.classes);
                    }
                }

                unit = null;
            }

            public void endProvider(String uri, String localName, String qName) {
                unit.provider = characters.toString();
            }

            public void endClass(String uri, String localName, String qName) {
                unit.classes.add(characters.toString());
            }
        });
    }

    private static class Unit {
        private String provider;
        private final Set<String> classes = new HashSet<String>();
        private final String name;

        public Unit(String name) {
            this.name = name;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }
    }

    private static class Handler implements PersistenceClassLoaderHandler {
        public void addTransformer(String unitId, ClassLoader classLoader, ClassFileTransformer classFileTransformer) {
            Instrumentation instrumentation = Agent.getInstrumentation();
            if (instrumentation != null) {
                instrumentation.addTransformer(new Transformer(classFileTransformer));
            }
        }

        public void destroy(String unitId) {
        }

        public ClassLoader getNewTempClassLoader(ClassLoader classLoader) {
            return new TempClassLoader(classLoader);
        }

    }

    public static class Transformer implements ClassFileTransformer {
        private final ClassFileTransformer transformer;

        public Transformer(ClassFileTransformer transformer) {
            this.transformer = transformer;
        }

        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            try {
                byte[] bytes = transformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
                if (bytes != null) {
                    debug("enhanced " + className);
                }
                return bytes;
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private static class NullDataSource implements DataSource {
        public Connection getConnection() throws SQLException {
            return null;
        }

        public Connection getConnection(String username, String password) throws SQLException {
            return null;
        }

        public int getLoginTimeout() throws SQLException {
            return 0;
        }

        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }

        public void setLoginTimeout(int seconds) throws SQLException {
        }

        public void setLogWriter(PrintWriter out) throws SQLException {
        }

        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }

        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new SQLException();
        }
    }
}
