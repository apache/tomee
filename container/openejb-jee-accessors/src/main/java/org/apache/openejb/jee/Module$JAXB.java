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
package org.apache.openejb.jee;

import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;

import static org.apache.openejb.jee.Web$JAXB.readWeb;
import static org.apache.openejb.jee.Web$JAXB.writeWeb;

@SuppressWarnings({
    "StringEquality"
})
public class Module$JAXB
    extends JAXBObject<Module> {


    public Module$JAXB() {
        super(Module.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "moduleType".intern()), Web$JAXB.class);
    }

    public static Module readModule(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeModule(final XoXMLStreamWriter writer, final Module module, final RuntimeContext context)
        throws Exception {
        _write(writer, module, context);
    }

    public void write(final XoXMLStreamWriter writer, final Module module, final RuntimeContext context)
        throws Exception {
        _write(writer, module, context);
    }

    public final static Module _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final Module module = new Module();
        context.beforeUnmarshal(module, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("moduleType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Module.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, module);
                module.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("connector" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: connector
                final String connectorRaw = elementReader.getElementAsString();

                final String connector;
                try {
                    connector = Adapters.collapsedStringAdapterAdapter.unmarshal(connectorRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                module.connector = connector;
            } else if (("ejb" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejb
                final String ejbRaw = elementReader.getElementAsString();

                final String ejb;
                try {
                    ejb = Adapters.collapsedStringAdapterAdapter.unmarshal(ejbRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                module.ejb = ejb;
            } else if (("java" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: java
                final String javaRaw = elementReader.getElementAsString();

                final String java;
                try {
                    java = Adapters.collapsedStringAdapterAdapter.unmarshal(javaRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                module.java = java;
            } else if (("web" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: web
                final Web web = readWeb(elementReader, context);
                module.web = web;
            } else if (("alt-dd" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: altDd
                final String altDdRaw = elementReader.getElementAsString();

                final String altDd;
                try {
                    altDd = Adapters.collapsedStringAdapterAdapter.unmarshal(altDdRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                module.altDd = altDd;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "connector"), new QName("http://java.sun.com/xml/ns/javaee", "ejb"), new QName("http://java.sun.com/xml/ns/javaee", "java"), new QName("http://java.sun.com/xml/ns/javaee", "web"), new QName("http://java.sun.com/xml/ns/javaee", "alt-dd"));
            }
        }

        context.afterUnmarshal(module, LifecycleCallback.NONE);

        return module;
    }

    public final Module read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final Module module, RuntimeContext context)
        throws Exception {
        if (module == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Module.class != module.getClass()) {
            context.unexpectedSubclass(writer, module, Module.class);
            return;
        }

        context.beforeMarshal(module, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = module.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(module, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: connector
        final String connectorRaw = module.connector;
        String connector = null;
        try {
            connector = Adapters.collapsedStringAdapterAdapter.marshal(connectorRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(module, "connector", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (connector != null) {
            writer.writeStartElement(prefix, "connector", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(connector);
            writer.writeEndElement();
        }

        // ELEMENT: ejb
        final String ejbRaw = module.ejb;
        String ejb = null;
        try {
            ejb = Adapters.collapsedStringAdapterAdapter.marshal(ejbRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(module, "ejb", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejb != null) {
            writer.writeStartElement(prefix, "ejb", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejb);
            writer.writeEndElement();
        }

        // ELEMENT: java
        final String javaRaw = module.java;
        String java = null;
        try {
            java = Adapters.collapsedStringAdapterAdapter.marshal(javaRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(module, "java", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (java != null) {
            writer.writeStartElement(prefix, "java", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(java);
            writer.writeEndElement();
        }

        // ELEMENT: web
        final Web web = module.web;
        if (web != null) {
            writer.writeStartElement(prefix, "web", "http://java.sun.com/xml/ns/javaee");
            writeWeb(writer, web, context);
            writer.writeEndElement();
        }

        // ELEMENT: altDd
        final String altDdRaw = module.altDd;
        String altDd = null;
        try {
            altDd = Adapters.collapsedStringAdapterAdapter.marshal(altDdRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(module, "altDd", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (altDd != null) {
            writer.writeStartElement(prefix, "alt-dd", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(altDd);
            writer.writeEndElement();
        }

        context.afterMarshal(module, LifecycleCallback.NONE);
    }

}
