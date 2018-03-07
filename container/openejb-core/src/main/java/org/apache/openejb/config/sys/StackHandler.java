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

package org.apache.openejb.config.sys;

import org.apache.openejb.config.SystemProperty;
import org.apache.openejb.util.Join;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;

public class StackHandler extends DefaultHandler {
    private static final boolean DEBUG = Boolean.getBoolean("openejb.sax.debug");

    private final List<DefaultHandler> handlers = new LinkedList<>();

    protected DefaultHandler get() {
        return handlers.get(0);
    }

    protected DefaultHandler pop() {
        return handlers.remove(0);
    }

    protected void checkAttributes(final Attributes attributes, final String... allowed) throws SAXException {
        checkAttributes(attributes, Arrays.asList(allowed));
    }

    protected void checkAttributes(final Attributes attributes, final List<String> allowed) throws SAXException {

        final List<String> invalid = new ArrayList<>();

        for (int i = 0; i < attributes.getLength(); i++) {
            if (!allowed.contains(attributes.getLocalName(i))) {
                invalid.add(attributes.getLocalName(i));
            }
        }

        if (invalid.size() > 0) {
            throw new SAXException("Unsupported Attribute(s): " + Join.join(", ", invalid) + ".  Supported Attributes are: " + Join.join(", ", allowed) + ".  If the setting is a configuration property it must be placed inside the element body.");
        }
    }


    protected void push(final DefaultHandler handler) {
        if (DEBUG) {
            for (final DefaultHandler ignored : handlers) {
                System.out.print("  ");
            }
            System.out.println("+ " + handler);
        }
        handlers.add(0, handler);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if (DEBUG) {
            for (final DefaultHandler ignored : handlers) {
                System.out.print("  ");
            }
            System.out.println("> " + get());
        }
        get().startElement(uri, localName, qName, attributes);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        get().endElement(uri, localName, qName);
        if (!DEBUG) {
            pop();
        } else {
            for (final DefaultHandler ignored : handlers) {
                System.out.print("  ");
            }
            System.out.println(" - " + pop());
        }
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        get().characters(ch, start, length);
    }

    public class Content extends DefaultHandler {

        private StringBuilder characters = new StringBuilder();

        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
            characters = new StringBuilder();
        }

        public void characters(final char[] ch, final int start, final int length) {
            characters.append(new String(ch, start, length));
        }

        public void endElement(final String uri, final String localName, final String qName) {
            setValue(characters.toString());
        }

        public void setValue(final String text) {
        }
    }

    public class SystemPropertyElement extends Content {
        private final List<String> allowed = asList("name", "value");

        private final SystemProperty built;
        private final List<SystemProperty> list;

        public SystemPropertyElement(final List<SystemProperty> systemProperties) {
            this.list = systemProperties;
            this.built = new SystemProperty();
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            built.setName(attributes.getValue("name"));
            built.setValue(attributes.getValue("value"));
            checkAttributes(attributes, allowed);
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) {
            list.add(built);
            super.endElement(uri, localName, qName);
        }
    }

    public abstract class ServiceElement<S extends AbstractService> extends Content {

        final S service;

        protected ServiceElement(final S service) {
            this.service = service;
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
            if (attributes.getValue("type") != null) {
                service.setType(attributes.getValue("type"));
            }
            if (attributes.getValue("jar") != null) {
                service.setJar(attributes.getValue("jar"));
            }
            if (attributes.getValue("provider") != null) {
                service.setProvider(attributes.getValue("provider"));
            }
            if (attributes.getValue("id") != null) {
                service.setId(attributes.getValue("id"));
            }
            if (attributes.getValue("class-name") != null) {
                service.setClassName(attributes.getValue("class-name"));
            }
            if (attributes.getValue("constructor") != null) {
                service.setConstructor(attributes.getValue("constructor"));
            }
            if (attributes.getValue("factory-name") != null) {
                service.setFactoryName(attributes.getValue("factory-name"));
            }
            if (attributes.getValue("classpath") != null) {
                service.setClasspath(attributes.getValue("classpath"));
            }
            if (attributes.getValue("classpath-api") != null) {
                service.setClasspathAPI(attributes.getValue("classpath-api"));
            }

            if (attributes.getValue("properties-provider") != null) {
                service.setPropertiesProvider(attributes.getValue("properties-provider"));
            }

            checkAttributes(attributes, getAttributes());
        }

        protected List<String> getAttributes() {
            final List<String> attributes = new ArrayList<>();
            attributes.add("type");
            attributes.add("jar");
            attributes.add("provider");
            attributes.add("id");
            attributes.add("class-name");
            attributes.add("constructor");
            attributes.add("factory-name");
            attributes.add("classpath");
            attributes.add("properties-provider");
            return attributes;
        }

        @Override
        public void setValue(final String text) {
            try {
                service.getProperties().putAll(new PropertiesAdapter().unmarshal(text));
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public class ResourceElement extends ServiceElement<Resource> {
        private final Collection<Resource> resources;

        public ResourceElement(final Collection<Resource> resources) {
            super(new Resource());
            this.resources = resources;
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            service.setJndi(attributes.getValue("jndi"));
            service.setPostConstruct(attributes.getValue("post-construct"));
            service.setPreDestroy(attributes.getValue("pre-destroy"));
            service.setTemplate(attributes.getValue("template"));
            service.setPropertiesProvider(attributes.getValue("property-provider"));
            if (service.getPropertiesProvider() == null) {
                service.setPropertiesProvider(attributes.getValue("properties-provider"));
            }

            final String aliases = attributes.getValue("aliases");
            if (aliases != null) {
                service.getAliases().addAll(Arrays.asList(aliases.split(",")));
            }
            final String dependsOn = attributes.getValue("depends-on");
            if (dependsOn != null) {
                service.getDependsOn().addAll(Arrays.asList(dependsOn.split(",")));
            }
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) {
            resources.add(service);
            super.endElement(uri, localName, qName);
        }

        @Override
        protected List<String> getAttributes() {
            final List<String> attributes = super.getAttributes();
            attributes.add("jndi");
            attributes.add("aliases");
            attributes.add("properties-provider");
            attributes.add("property-provider");
            attributes.add("depends-on");
            return attributes;
        }
    }

    public class DeclaredServiceElement extends ServiceElement<Service> {
        private final Collection<Service> services;

        public DeclaredServiceElement(final Collection<Service> services) {
            super(new Service());
            this.services = services;
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            service.setClazz(attributes.getValue("class"));
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) {
            services.add(service); // TODO: add it only once
            super.endElement(uri, localName, qName);
        }

        @Override
        protected List<String> getAttributes() {
            final List<String> attributes = super.getAttributes();
            attributes.add("class");
            return attributes;
        }
    }
}
