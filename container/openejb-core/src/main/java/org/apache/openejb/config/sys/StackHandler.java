/**
 *
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

import org.apache.openejb.util.Join;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class StackHandler extends DefaultHandler {
    private static final boolean DEBUG = Boolean.getBoolean("openejb.sax.debug");

    private final List<DefaultHandler> handlers = new LinkedList<DefaultHandler>();

    protected DefaultHandler get() {
        return handlers.get(0);
    }

    protected DefaultHandler pop() {
        return handlers.remove(0);
    }

    protected void checkAttributes(Attributes attributes, String... allowed) throws SAXException {
        checkAttributes(attributes, Arrays.asList(allowed));
    }

    protected void checkAttributes(Attributes attributes, List<String> allowed) throws SAXException {

        final List<String> invalid = new ArrayList<String>();

        for (int i = 0; i < attributes.getLength(); i++) {
            if (!allowed.contains(attributes.getLocalName(i))) {
                invalid.add(attributes.getLocalName(i));
            }
        }

        if (invalid.size() > 0) {
            throw new SAXException("Unsupported Attribute(s): "+ Join.join(", ", invalid) +".  Supported Attributes are: "+Join.join(", ", allowed) + ".  If the setting is a configuration property it must be placed inside the element body.");
        }
    }


    protected void push(DefaultHandler handler) {
        if (DEBUG) {
            for (int i = 0; i < handlers.size(); i++) {
                System.out.print("  ");
            }
            System.out.println("+ " + handler);
        }
        handlers.add(0, handler);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (DEBUG) {
            for (int i = 0; i < handlers.size(); i++) {
                System.out.print("  ");
            }
            System.out.println("> " + get());
        }
        get().startElement(uri, localName, qName, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        get().endElement(uri, localName, qName);
        if (!DEBUG) {
            pop();
        } else {
            for (int i = 0; i < handlers.size(); i++) {
                System.out.print("  ");
            }
            System.out.println(" - " + pop());
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        get().characters(ch, start, length);
    }

    public class Content extends DefaultHandler {

        private StringBuilder characters = new StringBuilder();

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            characters = new StringBuilder();
        }

        public void characters(char ch[], int start, int length) {
            characters.append(new String(ch, start, length));
        }

        public void endElement(String uri, String localName, String qName) {
            setValue(characters.toString());
        }

        public void setValue(String text) {
        }
    }

    public abstract class ServiceElement<S extends AbstractService> extends Content {

        final S service;

        protected ServiceElement(S service) {
            this.service = service;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
            if (attributes.getValue("type") != null) service.setType(attributes.getValue("type"));
            if (attributes.getValue("jar") != null) service.setJar(attributes.getValue("jar"));
            if (attributes.getValue("provider") != null) service.setProvider(attributes.getValue("provider"));
            if (attributes.getValue("id") != null) service.setId(attributes.getValue("id"));
            if (attributes.getValue("class-name") != null) service.setClassName(attributes.getValue("class-name"));
            if (attributes.getValue("constructor") != null) service.setConstructor(attributes.getValue("constructor"));
            if (attributes.getValue("factory-name") != null) service.setFactoryName(attributes.getValue("factory-name"));
            checkAttributes(attributes, getAttributes());
        }

        protected List<String> getAttributes() {
            List<String> attributes = new ArrayList<String>();
            attributes.add("type");
            attributes.add("jar");
            attributes.add("provider");
            attributes.add("id");
            attributes.add("class-name");
            attributes.add("constructor");
            attributes.add("factory-name");
            return attributes;
        }

        @Override
        public void setValue(String text) {
            try {
                service.getProperties().putAll(new PropertiesAdapter().unmarshal(text));
            } catch (Exception e) {
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
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            service.setJndi(attributes.getValue("jndi"));

            final String aliases = attributes.getValue("aliases");
            if (aliases != null) {
                service.getAliases().addAll(Arrays.asList(aliases.split(",")));
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            resources.add(service);
            super.endElement(uri, localName, qName);
        }

        @Override
        protected List<String> getAttributes() {
            final List<String> attributes = super.getAttributes();
            attributes.add("jndi");
            attributes.add("aliases");
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
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            service.setClazz(attributes.getValue("class"));
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
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
