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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config.sys;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.PojoConfiguration;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Saxs;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SaxAppCtxConfig {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, SaxAppContextConfig.class);

    public static void parse(final AppModule appModule, final InputSource source) throws SAXException, ParserConfigurationException, IOException {
        Saxs.factory()
            .newSAXParser()
                .parse(source, new SaxAppContextConfig(appModule));
    }

    private static class SaxAppContextConfig extends StackHandler {
        private static final Collection<String> IMPORT_ALIASES = Arrays.asList("import", "include");
        private static final Collection<String> APPLICATION_ALIASES = Arrays.asList("appcontext", "application");
        private static final Collection<String> POJOS_ALIASES = Arrays.asList("pojocontexts", "pojos");
        private static final Collection<String> POJO_ALIASES = Arrays.asList("pojo");
        private static final Collection<String> MODULE_ALIASES = Arrays.asList("modulecontext", "module", "beancontexts", "ejbs");
        private static final Collection<String> BEAN_CONTEXT_ALIASES = Arrays.asList("ejb", "beancontext");
        private static final Collection<String> CONFIGURATION_ALIASES = Arrays.asList("configuration", "properties", "settings");
        private static final Collection<String> RESOURCES_ALIASES = Arrays.asList("resources");
        private static final Collection<String> SERVICE_ALIASES = Arrays.asList("service");
        private static final Collection<String> RESOURCE_ALIASES = Arrays.asList("resource");

        private final AppModule module;

        public SaxAppContextConfig(final AppModule appModule) {
            module = appModule;
        }

        @Override
        public void startDocument() throws SAXException {
            push(new Document());
        }

        private class Document extends DefaultHandler {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
                if (APPLICATION_ALIASES.contains(localName.toLowerCase())) {
                    push(new Root());
                } else {
                    throw new IllegalStateException("Unsupported Element: " + localName);
                }
            }
        }

        private class Root extends DefaultHandler {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
                final String name = localName.toLowerCase();
                if (CONFIGURATION_ALIASES.contains(name)) {
                    push(new Configuration("", module.getProperties()));
                } else if (MODULE_ALIASES.contains(name)) {
                    push(new BeanContexts(attributes.getValue("id")));
                } else if (POJOS_ALIASES.contains(name)) {
                    push(new Pojos());
                } else if (RESOURCES_ALIASES.contains(name)) {
                    push(new ResourcesConfig());
                } else if (IMPORT_ALIASES.contains(name)) {
                    importFile(attributes.getValue("path"));
                    push(new DefaultHandler()); // just to keep the stack consistent
                } else {
                    throw new IllegalStateException("Unsupported Element: " + localName);
                }
            }

            private void importFile(final String path) throws SAXException {
                final File file = new File(path);
                if (file.exists()) {
                    try {
                        parse(module, new InputSource(new FileInputStream(file)));
                    } catch (ParserConfigurationException e) {
                        throw new SAXException(e);
                    } catch (IOException e) {
                        throw new SAXException(e);
                    }
                } else { // try in the classpath
                    final ClassLoader cl = module.getClassLoader();
                    if (cl != null) {
                        final InputStream is = cl.getResourceAsStream(path);
                        if (is != null) {
                            try {
                                parse(module, new InputSource(is));
                            } catch (ParserConfigurationException e) {
                                throw new SAXException(e);
                            } catch (IOException e) {
                                throw new SAXException(e);
                            }
                        } else {
                            LOGGER.warning("Can't find " + path);
                        }
                    } else {
                        LOGGER.warning("Can't find " + path + ", no classloader for the module " + module);
                    }
                }
            }
        }

        private class Configuration extends Content {
            private final Properties properties;

            private final String prefix;

            private Configuration(final String prefix, final Properties properties) {
                this.properties = properties;
                this.prefix =  prefix;
            }

            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
                push(new Configuration(prefix + localName + ".", properties));
            }

            @Override
            public void setValue(final String text) {
                try {
                    for (Map.Entry<Object, Object> entry : new PropertiesAdapter().unmarshal(text).entrySet()) {
                        properties.put(prefix + entry.getKey(), entry.getValue());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private class Pojos extends DefaultHandler {
            protected final List<PojoConfig> genericConfigs = new ArrayList<PojoConfig>();

            protected final Collection<String> aliases;

            private Pojos() {
                this(POJO_ALIASES);
            }

            protected Pojos(final Collection<String> aliases) {
                this.aliases = aliases;
            }

            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
                if (aliases.contains(localName.toLowerCase())) {
                    final PojoConfig beanConfig = newItem();
                    final String id = attributes.getValue("id");
                    if (id == null || "*".equals(id)) {
                        genericConfigs.add(beanConfig);
                    }
                    push(childParser(id, beanConfig));
                } else {
                    throw new IllegalStateException("Unsupported Element: " + localName);
                }
            }

            protected DefaultHandler childParser(final String id, final PojoConfig beanConfig) {
                return new Pojo(id, beanConfig);
            }

            protected PojoConfig newItem() {
                return new PojoConfig();
            }

            @Override
            public void endElement(final String uri, final String localName, final String qName) throws SAXException {
                for (PojoConfig generic : genericConfigs) {
                    for (PojoConfiguration config : module.getPojoConfigurations().values()) {
                        for (String key : generic.getProperties().stringPropertyNames()) {
                            if (!config.getProperties().containsKey(key)) {
                                config.getProperties().put(key, generic.getProperties().get(key));
                            }
                        }
                    }
                }
            }
        }

        private class BeanContexts extends Pojos {
            private final String id;

            public BeanContexts(final String id) {
                super(BEAN_CONTEXT_ALIASES);
                this.id = id;
            }

            @Override
            protected DefaultHandler childParser(final String beanId, final PojoConfig beanConfig) {
                return new BeanContext(id, beanId, (BeanContextConfig) beanConfig);
            }

            @Override
            protected PojoConfig newItem() {
                return new BeanContextConfig();
            }

            @Override
            public void endElement(final String uri, final String localName, final String qName) throws SAXException {
                for (PojoConfig generic : genericConfigs) { // BeanContextConfig
                    if (!generic.hasProperties()) {
                        continue;
                    }

                    for (EjbModule ejbModule : module.getEjbModules()) {
                        if (acceptModule(id, ejbModule)) {
                            continue;
                        }

                        for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                            final String name = bean.getEjbName();

                            OpenejbJar openEjbJar = ejbModule.getOpenejbJar();
                            if (openEjbJar == null) {
                                openEjbJar = new OpenejbJar();
                                ejbModule.setOpenejbJar(openEjbJar);
                            }

                            final Map<String, EjbDeployment> openejbJarDeployment = openEjbJar.getDeploymentsByEjbName();
                            EjbDeployment deployment = openejbJarDeployment.get(name);
                            if (deployment == null) {
                                deployment = openEjbJar.addEjbDeployment(bean);
                            }

                            for (String key : generic.getProperties().stringPropertyNames()) {
                                if (!deployment.getProperties().containsKey(key)) {
                                    deployment.getProperties().put(key, generic.getProperties().get(key));
                                }
                            }
                        }
                    }
                }
            }
        }

        private class Pojo extends DefaultHandler {
            protected final PojoConfig pojoConfig;
            protected final String id;

            public Pojo(final String id, final PojoConfig beanConfig) {
                this.id = id;
                pojoConfig = beanConfig;
            }

            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
                if (CONFIGURATION_ALIASES.contains(localName.toLowerCase())) {
                    push(new Configuration("", pojoConfig.getProperties()));
                } else {
                    throw new IllegalStateException("Unsupported Element: " + localName);
                }
            }

            @Override
            public void endElement(final String uri, final String localName, final String qName) throws SAXException {
                module.getPojoConfigurations().put(id, new PojoConfiguration(pojoConfig.getProperties()));
            }
        }

        private class BeanContext extends Pojo {
            private final String moduleId;

            public BeanContext(final String moduleId, final String id, final BeanContextConfig beanConfig) {
                super(id, beanConfig);
                this.moduleId = moduleId;
            }

            @Override
            public void endElement(final String uri, final String localName, final String qName) throws SAXException {
                for (EjbModule ejbModule : module.getEjbModules()) {
                    if (!acceptModule(moduleId, ejbModule)) {
                        continue;
                    }

                    final EnterpriseBean bean = ejbModule.getEjbJar().getEnterpriseBeansByEjbName().get(id);
                    if (bean == null) {
                        continue;
                    }

                    final String name = bean.getEjbName();

                    if (pojoConfig.hasProperties()) {
                        OpenejbJar openEjbJar = ejbModule.getOpenejbJar();
                        if (openEjbJar == null) {
                            openEjbJar = new OpenejbJar();
                            ejbModule.setOpenejbJar(openEjbJar);
                        }

                        final Map<String, EjbDeployment> openejbJarDeployment = openEjbJar.getDeploymentsByEjbName();
                        EjbDeployment deployment = openejbJarDeployment.get(name);
                        if (deployment == null) {
                            deployment = openEjbJar.addEjbDeployment(bean);
                        }
                        deployment.getProperties().putAll(pojoConfig.getProperties());
                    }
                }
            }
        }

        // TODO: use it to parse resources.xml if we keep this file
        private class ResourcesConfig extends DefaultHandler {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
                final String name = localName.toLowerCase();
                if (SERVICE_ALIASES.contains(name)) {
                    push(new DeclaredServiceElement(module.getServices()));
                } else if (RESOURCE_ALIASES.contains(name)) {
                    push(new ResourceElement(module.getResources()));
                } else {
                    throw new IllegalStateException("Unsupported Element: " + localName);
                }
                get().startElement(uri, localName, qName, attributes);
            }
        }
    }

    private static boolean acceptModule(final String id, final EjbModule ejbModule) {
        return id == null || id.equals(ejbModule.getModuleId());
    }
}
