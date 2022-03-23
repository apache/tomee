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

@SuppressWarnings({
    "StringEquality"
})
public class FacesSystemEventListener$JAXB
    extends JAXBObject<FacesSystemEventListener> {


    public FacesSystemEventListener$JAXB() {
        super(FacesSystemEventListener.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-system-event-listenerType".intern()));
    }

    public static FacesSystemEventListener readFacesSystemEventListener(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesSystemEventListener(final XoXMLStreamWriter writer, final FacesSystemEventListener facesSystemEventListener, final RuntimeContext context)
        throws Exception {
        _write(writer, facesSystemEventListener, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesSystemEventListener facesSystemEventListener, final RuntimeContext context)
        throws Exception {
        _write(writer, facesSystemEventListener, context);
    }

    public final static FacesSystemEventListener _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesSystemEventListener facesSystemEventListener = new FacesSystemEventListener();
        context.beforeUnmarshal(facesSystemEventListener, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-system-event-listenerType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesSystemEventListener.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesSystemEventListener);
                facesSystemEventListener.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("system-event-listener-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: systemEventListenerClass
                final String systemEventListenerClassRaw = elementReader.getElementAsString();

                final String systemEventListenerClass;
                try {
                    systemEventListenerClass = Adapters.collapsedStringAdapterAdapter.unmarshal(systemEventListenerClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesSystemEventListener.systemEventListenerClass = systemEventListenerClass;
            } else if (("system-event-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: systemEventClass
                final String systemEventClassRaw = elementReader.getElementAsString();

                final String systemEventClass;
                try {
                    systemEventClass = Adapters.collapsedStringAdapterAdapter.unmarshal(systemEventClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesSystemEventListener.systemEventClass = systemEventClass;
            } else if (("source-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: sourceClass
                final String sourceClassRaw = elementReader.getElementAsString();

                final String sourceClass;
                try {
                    sourceClass = Adapters.collapsedStringAdapterAdapter.unmarshal(sourceClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesSystemEventListener.sourceClass = sourceClass;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "system-event-listener-class"), new QName("http://java.sun.com/xml/ns/javaee", "system-event-class"), new QName("http://java.sun.com/xml/ns/javaee", "source-class"));
            }
        }

        context.afterUnmarshal(facesSystemEventListener, LifecycleCallback.NONE);

        return facesSystemEventListener;
    }

    public final FacesSystemEventListener read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesSystemEventListener facesSystemEventListener, RuntimeContext context)
        throws Exception {
        if (facesSystemEventListener == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesSystemEventListener.class != facesSystemEventListener.getClass()) {
            context.unexpectedSubclass(writer, facesSystemEventListener, FacesSystemEventListener.class);
            return;
        }

        context.beforeMarshal(facesSystemEventListener, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = facesSystemEventListener.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(facesSystemEventListener, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: systemEventListenerClass
        final String systemEventListenerClassRaw = facesSystemEventListener.systemEventListenerClass;
        String systemEventListenerClass = null;
        try {
            systemEventListenerClass = Adapters.collapsedStringAdapterAdapter.marshal(systemEventListenerClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesSystemEventListener, "systemEventListenerClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (systemEventListenerClass != null) {
            writer.writeStartElement(prefix, "system-event-listener-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(systemEventListenerClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesSystemEventListener, "systemEventListenerClass");
        }

        // ELEMENT: systemEventClass
        final String systemEventClassRaw = facesSystemEventListener.systemEventClass;
        String systemEventClass = null;
        try {
            systemEventClass = Adapters.collapsedStringAdapterAdapter.marshal(systemEventClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesSystemEventListener, "systemEventClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (systemEventClass != null) {
            writer.writeStartElement(prefix, "system-event-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(systemEventClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesSystemEventListener, "systemEventClass");
        }

        // ELEMENT: sourceClass
        final String sourceClassRaw = facesSystemEventListener.sourceClass;
        String sourceClass = null;
        try {
            sourceClass = Adapters.collapsedStringAdapterAdapter.marshal(sourceClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesSystemEventListener, "sourceClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (sourceClass != null) {
            writer.writeStartElement(prefix, "source-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(sourceClass);
            writer.writeEndElement();
        }

        context.afterMarshal(facesSystemEventListener, LifecycleCallback.NONE);
    }

}
