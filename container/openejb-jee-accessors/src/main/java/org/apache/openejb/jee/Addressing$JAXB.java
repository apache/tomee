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
import javax.xml.namespace.QName;

import static org.apache.openejb.jee.AddressingResponses$JAXB.parseAddressingResponses;
import static org.apache.openejb.jee.AddressingResponses$JAXB.toStringAddressingResponses;


public class Addressing$JAXB
    extends JAXBObject<Addressing> {


    public Addressing$JAXB() {
        super(Addressing.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "addressingType".intern()), AddressingResponses$JAXB.class);
    }

    public static Addressing readAddressing(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeAddressing(final XoXMLStreamWriter writer, final Addressing addressing, final RuntimeContext context)
        throws Exception {
        _write(writer, addressing, context);
    }

    public void write(final XoXMLStreamWriter writer, final Addressing addressing, final RuntimeContext context)
        throws Exception {
        _write(writer, addressing, context);
    }

    public final static Addressing _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final Addressing addressing = new Addressing();
        context.beforeUnmarshal(addressing, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if ((!"addressingType".equals(xsiType.getLocalPart())) || (!"http://java.sun.com/xml/ns/javaee".equals(xsiType.getNamespaceURI()))) {
                return context.unexpectedXsiType(reader, Addressing.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (!XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(attribute.getNamespace())) {
                context.unexpectedAttribute(attribute);
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("enabled".equals(elementReader.getLocalName())) && ("http://java.sun.com/xml/ns/javaee".equals(elementReader.getNamespaceURI()))) {
                // ELEMENT: enabled
                addressing.enabled = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
            } else if (("required".equals(elementReader.getLocalName())) && ("http://java.sun.com/xml/ns/javaee".equals(elementReader.getNamespaceURI()))) {
                // ELEMENT: required
                addressing.required = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
            } else if (("responses".equals(elementReader.getLocalName())) && ("http://java.sun.com/xml/ns/javaee".equals(elementReader.getNamespaceURI()))) {
                // ELEMENT: responses
                final AddressingResponses responses = parseAddressingResponses(elementReader, context, elementReader.getElementAsString());
                if (responses != null) {
                    addressing.responses = responses;
                }
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "enabled"), new QName("http://java.sun.com/xml/ns/javaee", "required"), new QName("http://java.sun.com/xml/ns/javaee", "responses"));
            }
        }

        context.afterUnmarshal(addressing, LifecycleCallback.NONE);

        return addressing;
    }

    public final Addressing read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final Addressing addressing, RuntimeContext context)
        throws Exception {
        if (addressing == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Addressing.class != addressing.getClass()) {
            context.unexpectedSubclass(writer, addressing, Addressing.class);
            return;
        }

        context.beforeMarshal(addressing, LifecycleCallback.NONE);


        // ELEMENT: enabled
        final Boolean enabled = addressing.enabled;
        if (enabled != null) {
            writer.writeStartElement(prefix, "enabled", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(enabled));
            writer.writeEndElement();
        }

        // ELEMENT: required
        final Boolean required = addressing.required;
        if (required != null) {
            writer.writeStartElement(prefix, "required", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(required));
            writer.writeEndElement();
        }

        // ELEMENT: responses
        final AddressingResponses responses = addressing.responses;
        if (responses != null) {
            writer.writeStartElement(prefix, "responses", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringAddressingResponses(addressing, null, context, responses));
            writer.writeEndElement();
        }

        context.afterMarshal(addressing, LifecycleCallback.NONE);
    }

}
