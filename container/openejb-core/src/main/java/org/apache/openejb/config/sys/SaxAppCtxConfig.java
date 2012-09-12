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
import org.apache.openejb.config.BeanProperties;
import org.apache.openejb.config.DeploymentModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.EnvEntriesPropertiesDeployer;
import org.apache.openejb.config.PojoConfiguration;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.loader.SystemInstance;
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

    public static void parse(final AppModule appModule, final InputSource source,
                             final EnvEntriesPropertiesDeployer envEntriesDeployer, final BeanProperties beanProperties)
            throws SAXException, ParserConfigurationException, IOException {
        Saxs.factory()
            .newSAXParser()
                .parse(source, new SaxAppContextConfig(appModule, envEntriesDeployer, beanProperties));
    }

    private static class SaxAppContextConfig extends StackHandler {
        private static final Collection<String> IMPORT_ALIASES = Arrays.asList("import", "include");
        private static final Collection<String> APPLICATION_ALIASES = Arrays.asList("appcontext", "app-context", "application");
        private static final Collection<String> POJOS_ALIASES = Arrays.asList("pojocontexts", "pojo-contexts", "pojos");
        private static final Collection<String> POJO_ALIASES = Arrays.asList("pojo");
        private static final Collection<String> BEAN_CONTEXTS_ALIASES = Arrays.asList("beancontexts", "bean-contexts", "ejbs");
        private static final Collection<String> WEBAPP_ALIASES = Arrays.asList("webapps", "webcontexts", "web-contexts", "wars");
        private static final Collection<String> MODULE_ALIASES = Arrays.asList("modulecontext", "module");
        private static final Collection<String> BEAN_CONTEXT_ALIASES = Arrays.asList("ejb", "beancontext", "bean-context");
        private static final Collection<String> CONFIGURATION_ALIASES = Arrays.asList("configuration", "properties", "settings");
        private static final Collection<String> RESOURCES_ALIASES = Arrays.asList("resources");
        private static final Collection<String> SERVICE_ALIASES = Arrays.asList("service");
        private static final Collection<String> RESOURCE_ALIASES = Arrays.asList("resource");
        private static final Collection<String> ENV_ENTRIES_ALIASES = Arrays.asList("enventries", "env-entries");
        private static final Collection<String> ENV_ENTRY_ALIASES = Arrays.asList("enventry", "env-entry");

        private final AppModule module;
        private final EnvEntriesPropertiesDeployer envEntriesDeployer;
        private final BeanProperties beanPropertiesDeployer;

        public SaxAppContextConfig(final AppModule appModule, final EnvEntriesPropertiesDeployer envEntriesDeployer, final BeanProperties beanProperties) {
            this.module = appModule;
            this.envEntriesDeployer = envEntriesDeployer;
            this.beanPropertiesDeployer = beanProperties;
        }

        @Override
        public void startDocument() throws SAXException {
            push(new Document());
        }

        private class Document extends DefaultHandler {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
                // by default don't care about root tag
                if (!APPLICATION_ALIASES.contains(localName.toLowerCase())
                        && SystemInstance.get().getOptions().get("openejb.configuration.strict-tags", false)) {
                    throw new IllegalStateException("Unsupported Element: " + localName);

                }
                push(new Root());
            }
        }

        private class Root extends DefaultHandler {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
                final String name = localName.toLowerCase();
                if (CONFIGURATION_ALIASES.contains(name)) {
                    push(new Configuration("", module.getProperties()));
                } else if (ENV_ENTRIES_ALIASES.contains(name)) {
                    push(new EnvEntries());
                } else if (BEAN_CONTEXTS_ALIASES.contains(name)) {
                    push(new BeanContexts(null));
                } else if (MODULE_ALIASES.contains(name)) {
                    push(new ModuleContext(attributes.getValue("id")));
                } else if (WEBAPP_ALIASES.contains(name)) {
                    push(new WebAppContext(attributes.getValue("id")));
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
                        parse(module, new InputSource(new FileInputStream(file)), envEntriesDeployer, beanPropertiesDeployer);
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
                                parse(module, new InputSource(is), envEntriesDeployer, beanPropertiesDeployer);
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
                if (properties == null) {
                    return;
                }

                try {
                    for (Map.Entry<Object, Object> entry : new PropertiesAdapter().unmarshal(text).entrySet()) {
                        properties.put(prefix + entry.getKey(), entry.getValue());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private class MultipleConfiguration extends Content {
            private final Collection<Properties> properties;

            private final String prefix;

            private MultipleConfiguration(final String prefix, final Collection<Properties> properties) {
                this.properties = properties;
                this.prefix =  prefix;
            }

            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
                push(new MultipleConfiguration(prefix + localName + ".", properties));
            }

            @Override
            public void setValue(final String text) {
                if (properties == null) {
                    return;
                }

                try {
                    for (Properties p : properties) {
                        for (Map.Entry<Object, Object> entry : new PropertiesAdapter().unmarshal(text).entrySet()) {
                            p.put(prefix + entry.getKey(), entry.getValue());
                        }
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

        private class ModuleContext extends DefaultHandler {
            protected final String id;

            private ModuleContext(final String id) {
                this.id = id;
            }

            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
                final String name = localName.toLowerCase();
                if (BEAN_CONTEXTS_ALIASES.contains(name)) {
                    push(new BeanContexts(id));
                } else if (POJOS_ALIASES.contains(name)) {
                    push(new Pojos());
                } else if (CONFIGURATION_ALIASES.contains(name)) {
                    push(new MultipleConfiguration("", propertiesForModule(id)));
                } else {
                    throw new IllegalStateException("Unsupported Element: " + localName);
                }
            }

            protected Collection<Properties> propertiesForModule(final String id) {
                final Collection<Properties> props = new ArrayList<Properties>();
                for (DeploymentModule m : module.getDeploymentModule()) {
                    if (acceptModule(id, m)) {
                        props.add(m.getProperties());
                    }
                }
                return props;
            }
        }

        private class WebAppContext extends ModuleContext {
            private WebAppContext(final String id) {
                super(id);
            }

            @Override
            protected Collection<Properties> propertiesForModule(final String id) {
                final Collection<Properties> props = new ArrayList<Properties>();
                for (WebModule m : module.getWebModules()) {
                    if (acceptModule(id, m)) {
                        props.add(m.getProperties());
                    }
                }
                return props;
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
                    if (generic.hasProperties()) {
                        beanPropertiesDeployer.addGlobalProperties(id, generic.getProperties());
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

                    if (pojoConfig.hasProperties()) {
                        beanPropertiesDeployer.addProperties(id, pojoConfig.getProperties());
                    }
                }
            }
        }

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

        private class EnvEntries extends DefaultHandler {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
                if (ENV_ENTRY_ALIASES.contains(localName.toLowerCase())) {
                    push(new EnvEntry());
                    get().startElement(uri, localName, qName, attributes);
                } else {
                    throw new IllegalStateException("Unsupported Element: " + localName);
                }
            }
        }

        private class EnvEntry extends Content {
            private String key;

            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
                if (ENV_ENTRY_ALIASES.contains(localName.toLowerCase())) {
                    key = attributes.getValue("key");
                } else {
                    throw new IllegalStateException("Unsupported Element: " + localName);
                }
            }

            @Override
            public void setValue(final String text) {
                envEntriesDeployer.addEnvEntries(key, text);
            }
        }
    }

    private static boolean acceptModule(final String id, final DeploymentModule ejbModule) {
        return id == null || id.equals(ejbModule.getModuleId());
    }
}
