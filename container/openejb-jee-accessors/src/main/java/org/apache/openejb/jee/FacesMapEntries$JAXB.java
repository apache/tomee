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
import java.util.ArrayList;
import java.util.List;

import static org.apache.openejb.jee.FacesMapEntry$JAXB.readFacesMapEntry;
import static org.apache.openejb.jee.FacesMapEntry$JAXB.writeFacesMapEntry;

@SuppressWarnings({
    "StringEquality"
})
public class FacesMapEntries$JAXB
    extends JAXBObject<FacesMapEntries> {


    public FacesMapEntries$JAXB() {
        super(FacesMapEntries.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-map-entriesType".intern()), FacesMapEntry$JAXB.class);
    }

    public static FacesMapEntries readFacesMapEntries(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesMapEntries(final XoXMLStreamWriter writer, final FacesMapEntries facesMapEntries, final RuntimeContext context)
        throws Exception {
        _write(writer, facesMapEntries, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesMapEntries facesMapEntries, final RuntimeContext context)
        throws Exception {
        _write(writer, facesMapEntries, context);
    }

    public final static FacesMapEntries _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesMapEntries facesMapEntries = new FacesMapEntries();
        context.beforeUnmarshal(facesMapEntries, LifecycleCallback.NONE);

        List<FacesMapEntry> mapEntry = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-map-entriesType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesMapEntries.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesMapEntries);
                facesMapEntries.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("key-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: keyClass
                final String keyClassRaw = elementReader.getElementAsString();

                final String keyClass;
                try {
                    keyClass = Adapters.collapsedStringAdapterAdapter.unmarshal(keyClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesMapEntries.keyClass = keyClass;
            } else if (("value-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: valueClass
                final String valueClassRaw = elementReader.getElementAsString();

                final String valueClass;
                try {
                    valueClass = Adapters.collapsedStringAdapterAdapter.unmarshal(valueClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesMapEntries.valueClass = valueClass;
            } else if (("map-entry" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: mapEntry
                final FacesMapEntry mapEntryItem = readFacesMapEntry(elementReader, context);
                if (mapEntry == null) {
                    mapEntry = facesMapEntries.mapEntry;
                    if (mapEntry != null) {
                        mapEntry.clear();
                    } else {
                        mapEntry = new ArrayList<FacesMapEntry>();
                    }
                }
                mapEntry.add(mapEntryItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "key-class"), new QName("http://java.sun.com/xml/ns/javaee", "value-class"), new QName("http://java.sun.com/xml/ns/javaee", "map-entry"));
            }
        }
        if (mapEntry != null) {
            facesMapEntries.mapEntry = mapEntry;
        }

        context.afterUnmarshal(facesMapEntries, LifecycleCallback.NONE);

        return facesMapEntries;
    }

    public final FacesMapEntries read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesMapEntries facesMapEntries, RuntimeContext context)
        throws Exception {
        if (facesMapEntries == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesMapEntries.class != facesMapEntries.getClass()) {
            context.unexpectedSubclass(writer, facesMapEntries, FacesMapEntries.class);
            return;
        }

        context.beforeMarshal(facesMapEntries, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = facesMapEntries.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(facesMapEntries, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: keyClass
        final String keyClassRaw = facesMapEntries.keyClass;
        String keyClass = null;
        try {
            keyClass = Adapters.collapsedStringAdapterAdapter.marshal(keyClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesMapEntries, "keyClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (keyClass != null) {
            writer.writeStartElement(prefix, "key-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(keyClass);
            writer.writeEndElement();
        }

        // ELEMENT: valueClass
        final String valueClassRaw = facesMapEntries.valueClass;
        String valueClass = null;
        try {
            valueClass = Adapters.collapsedStringAdapterAdapter.marshal(valueClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesMapEntries, "valueClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (valueClass != null) {
            writer.writeStartElement(prefix, "value-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(valueClass);
            writer.writeEndElement();
        }

        // ELEMENT: mapEntry
        final List<FacesMapEntry> mapEntry = facesMapEntries.mapEntry;
        if (mapEntry != null) {
            for (final FacesMapEntry mapEntryItem : mapEntry) {
                if (mapEntryItem != null) {
                    writer.writeStartElement(prefix, "map-entry", "http://java.sun.com/xml/ns/javaee");
                    writeFacesMapEntry(writer, mapEntryItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesMapEntries, LifecycleCallback.NONE);
    }

}
