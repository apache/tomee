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
import org.apache.openejb.util.Saxs;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SaxAppCtxConfig {
    public static void parse(final AppModule appModule, final InputSource source) throws SAXException, ParserConfigurationException, IOException {
        Saxs.factory()
            .newSAXParser()
                .parse(source, new SaxAppContextConfig(appModule));
    }

    private static class SaxAppContextConfig extends StackHandler {
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
                if (localName.equalsIgnoreCase("AppContext")) {
                    push(new Root());
                } else {
                    throw new IllegalStateException("Unsupported Element: " + localName);
                }
            }
        }

        private class Root extends DefaultHandler {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
                if (localName.equalsIgnoreCase("Configuration")) {
                    push(new Configuration(module.getProperties()));
                } else if (localName.equalsIgnoreCase("BeanContexts")) {
                    push(new BeanContexts(localName));
                } else if (localName.equalsIgnoreCase("Pojos")) {
                    push(new Pojos(localName));
                } else if (localName.equalsIgnoreCase("Resources")) {
                    push(new ResourcesConfig());
                } else {
                    throw new IllegalStateException("Unsupported Element: " + localName);
                }

                if (!(get() instanceof Pojos)) {
                    get().startElement(uri, localName, qName, attributes);
                }
            }
        }

        private class Configuration extends Content {
            private final Properties properties;

            private LinkedList<String> currentPrefix = new LinkedList<String>();

            private Configuration(final Properties properties) {
                this.properties = properties;
            }

            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
                if (currentPrefix.isEmpty()) { // <Configuration> used as marker
                    currentPrefix.push("");
                } else {
                    currentPrefix.push(currentPrefix.getLast() + localName + ".");
                    push(this);
                }
            }

            @Override
            public void setValue(final String text) { // endElement
                final String current = currentPrefix.pop();
                if (currentPrefix.size() == 0) { // <Configuration>, all is done
                    return;
                }

                try {
                    for (Map.Entry<Object, Object> entry : new PropertiesAdapter().unmarshal(text).entrySet()) {
                        properties.put(current + entry.getKey(), entry.getValue());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private class Pojos extends DefaultHandler {
            protected final List<PojoConfig> genericConfigs = new ArrayList<PojoConfig>();

            private final String tagName;

            public Pojos(final String tag) {
                tagName = tag.substring(0, tag.length() - 1); // remove 's'
            }

            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
                if (localName.equalsIgnoreCase(tagName)) {
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
            public BeanContexts(final String tag) {
                super(tag);
            }

            @Override
            protected DefaultHandler childParser(final String id, final PojoConfig beanConfig) {
                return new BeanContext(id, (BeanContextConfig) beanConfig);
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
                if (localName.equalsIgnoreCase("Configuration")) {
                    push(new Configuration(pojoConfig.getProperties()));
                } else {
                    throw new IllegalStateException("Unsupported Element: " + localName);
                }
                get().startElement(uri, localName, qName, attributes);
            }

            @Override
            public void endElement(final String uri, final String localName, final String qName) throws SAXException {
                module.getPojoConfigurations().put(id, new PojoConfiguration(pojoConfig.getProperties()));
            }
        }

        private class BeanContext extends Pojo {
            public BeanContext(final String id, final BeanContextConfig beanConfig) {
                super(id, beanConfig);
            }

            @Override
            public void endElement(final String uri, final String localName, final String qName) throws SAXException {
                for (EjbModule ejbModule : module.getEjbModules()) {
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
                if (localName.equalsIgnoreCase("Resources")) {
                    return;
                }

                if (localName.equalsIgnoreCase("Service")) {
                    push(new DeclaredServiceElement(module.getServices()));
                } else if (localName.equalsIgnoreCase("Resource")) {
                    push(new ResourceElement(module.getResources()));
                } else {
                    throw new IllegalStateException("Unsupported Element: " + localName);
                }
                get().startElement(uri, localName, qName, attributes);
            }
        }
    }
}
