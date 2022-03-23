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

import static org.apache.openejb.jee.FacesNullValue$JAXB.readFacesNullValue;
import static org.apache.openejb.jee.FacesNullValue$JAXB.writeFacesNullValue;

@SuppressWarnings({
    "StringEquality"
})
public class FacesMapEntry$JAXB
    extends JAXBObject<FacesMapEntry> {


    public FacesMapEntry$JAXB() {
        super(FacesMapEntry.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-map-entryType".intern()), FacesNullValue$JAXB.class);
    }

    public static FacesMapEntry readFacesMapEntry(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesMapEntry(final XoXMLStreamWriter writer, final FacesMapEntry facesMapEntry, final RuntimeContext context)
        throws Exception {
        _write(writer, facesMapEntry, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesMapEntry facesMapEntry, final RuntimeContext context)
        throws Exception {
        _write(writer, facesMapEntry, context);
    }

    public final static FacesMapEntry _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesMapEntry facesMapEntry = new FacesMapEntry();
        context.beforeUnmarshal(facesMapEntry, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-map-entryType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesMapEntry.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesMapEntry);
                facesMapEntry.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("key" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: key
                final String keyRaw = elementReader.getElementAsString();

                final String key;
                try {
                    key = Adapters.collapsedStringAdapterAdapter.unmarshal(keyRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesMapEntry.key = key;
            } else if (("null-value" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: nullValue
                final FacesNullValue nullValue = readFacesNullValue(elementReader, context);
                facesMapEntry.nullValue = nullValue;
            } else if (("value" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: value
                final String valueRaw = elementReader.getElementAsString();

                final String value;
                try {
                    value = Adapters.collapsedStringAdapterAdapter.unmarshal(valueRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesMapEntry.value = value;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "key"), new QName("http://java.sun.com/xml/ns/javaee", "null-value"), new QName("http://java.sun.com/xml/ns/javaee", "value"));
            }
        }

        context.afterUnmarshal(facesMapEntry, LifecycleCallback.NONE);

        return facesMapEntry;
    }

    public final FacesMapEntry read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesMapEntry facesMapEntry, RuntimeContext context)
        throws Exception {
        if (facesMapEntry == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesMapEntry.class != facesMapEntry.getClass()) {
            context.unexpectedSubclass(writer, facesMapEntry, FacesMapEntry.class);
            return;
        }

        context.beforeMarshal(facesMapEntry, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = facesMapEntry.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(facesMapEntry, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: key
        final String keyRaw = facesMapEntry.key;
        String key = null;
        try {
            key = Adapters.collapsedStringAdapterAdapter.marshal(keyRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesMapEntry, "key", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (key != null) {
            writer.writeStartElement(prefix, "key", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(key);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesMapEntry, "key");
        }

        // ELEMENT: nullValue
        final FacesNullValue nullValue = facesMapEntry.nullValue;
        if (nullValue != null) {
            writer.writeStartElement(prefix, "null-value", "http://java.sun.com/xml/ns/javaee");
            writeFacesNullValue(writer, nullValue, context);
            writer.writeEndElement();
        }

        // ELEMENT: value
        final String valueRaw = facesMapEntry.value;
        String value = null;
        try {
            value = Adapters.collapsedStringAdapterAdapter.marshal(valueRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesMapEntry, "value", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (value != null) {
            writer.writeStartElement(prefix, "value", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(value);
            writer.writeEndElement();
        }

        context.afterMarshal(facesMapEntry, LifecycleCallback.NONE);
    }

}
