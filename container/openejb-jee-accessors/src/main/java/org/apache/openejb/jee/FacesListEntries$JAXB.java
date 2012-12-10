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

import com.envoisolutions.sxc.jaxb.JAXBObject;
import com.envoisolutions.sxc.jaxb.LifecycleCallback;
import com.envoisolutions.sxc.jaxb.RuntimeContext;
import com.envoisolutions.sxc.util.Attribute;
import com.envoisolutions.sxc.util.XoXMLStreamReader;
import com.envoisolutions.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

import static org.apache.openejb.jee.FacesNullValue$JAXB.readFacesNullValue;
import static org.apache.openejb.jee.FacesNullValue$JAXB.writeFacesNullValue;

@SuppressWarnings({
        "StringEquality"
})
public class FacesListEntries$JAXB
        extends JAXBObject<FacesListEntries> {


    public FacesListEntries$JAXB() {
        super(FacesListEntries.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-list-entriesType".intern()), FacesNullValue$JAXB.class);
    }

    public static FacesListEntries readFacesListEntries(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesListEntries(XoXMLStreamWriter writer, FacesListEntries facesListEntries, RuntimeContext context)
            throws Exception {
        _write(writer, facesListEntries, context);
    }

    public void write(XoXMLStreamWriter writer, FacesListEntries facesListEntries, RuntimeContext context)
            throws Exception {
        _write(writer, facesListEntries, context);
    }

    public final static FacesListEntries _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesListEntries facesListEntries = new FacesListEntries();
        context.beforeUnmarshal(facesListEntries, LifecycleCallback.NONE);

        List<Object> nullValueOrValue = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-list-entriesType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesListEntries.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                java.lang.String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesListEntries);
                facesListEntries.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("value-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: valueClass
                java.lang.String valueClassRaw = elementReader.getElementAsString();

                java.lang.String valueClass;
                try {
                    valueClass = Adapters.collapsedStringAdapterAdapter.unmarshal(valueClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, java.lang.String.class, java.lang.String.class, e);
                    continue;
                }

                facesListEntries.valueClass = valueClass;
            } else if (("value" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: nullValueOrValue
                java.lang.String nullValueOrValueItem = elementReader.getElementAsString();
                if (nullValueOrValue == null) {
                    nullValueOrValue = facesListEntries.nullValueOrValue;
                    if (nullValueOrValue != null) {
                        nullValueOrValue.clear();
                    } else {
                        nullValueOrValue = new ArrayList<Object>();
                    }
                }
                nullValueOrValue.add(nullValueOrValueItem);
            } else if (("null-value" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: nullValueOrValue
                org.apache.openejb.jee.FacesNullValue nullValueOrValueItem1 = readFacesNullValue(elementReader, context);
                if (nullValueOrValue == null) {
                    nullValueOrValue = facesListEntries.nullValueOrValue;
                    if (nullValueOrValue != null) {
                        nullValueOrValue.clear();
                    } else {
                        nullValueOrValue = new ArrayList<Object>();
                    }
                }
                nullValueOrValue.add(nullValueOrValueItem1);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "value-class"), new QName("http://java.sun.com/xml/ns/javaee", "value"), new QName("http://java.sun.com/xml/ns/javaee", "null-value"));
            }
        }
        if (nullValueOrValue != null) {
            facesListEntries.nullValueOrValue = nullValueOrValue;
        }

        context.afterUnmarshal(facesListEntries, LifecycleCallback.NONE);

        return facesListEntries;
    }

    public final FacesListEntries read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, FacesListEntries facesListEntries, RuntimeContext context)
            throws Exception {
        if (facesListEntries == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        java.lang.String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesListEntries.class != facesListEntries.getClass()) {
            context.unexpectedSubclass(writer, facesListEntries, FacesListEntries.class);
            return;
        }

        context.beforeMarshal(facesListEntries, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        java.lang.String idRaw = facesListEntries.id;
        if (idRaw != null) {
            java.lang.String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesListEntries, "id", CollapsedStringAdapter.class, java.lang.String.class, java.lang.String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: valueClass
        java.lang.String valueClassRaw = facesListEntries.valueClass;
        java.lang.String valueClass = null;
        try {
            valueClass = Adapters.collapsedStringAdapterAdapter.marshal(valueClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesListEntries, "valueClass", CollapsedStringAdapter.class, java.lang.String.class, java.lang.String.class, e);
        }
        if (valueClass != null) {
            writer.writeStartElement(prefix, "value-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(valueClass);
            writer.writeEndElement();
        }

        // ELEMENT: nullValueOrValue
        List<Object> nullValueOrValue = facesListEntries.nullValueOrValue;
        if (nullValueOrValue != null) {
            for (Object nullValueOrValueItem : nullValueOrValue) {
                if (nullValueOrValueItem instanceof org.apache.openejb.jee.FacesNullValue) {
                    org.apache.openejb.jee.FacesNullValue FacesNullValue = ((org.apache.openejb.jee.FacesNullValue) nullValueOrValueItem);
                    writer.writeStartElement(prefix, "null-value", "http://java.sun.com/xml/ns/javaee");
                    writeFacesNullValue(writer, FacesNullValue, context);
                    writer.writeEndElement();
                } else if (nullValueOrValueItem instanceof java.lang.String) {
                    java.lang.String String = ((java.lang.String) nullValueOrValueItem);
                    writer.writeStartElement(prefix, "value", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(String);
                    writer.writeEndElement();
                } else if (nullValueOrValueItem == null) {
                    context.unexpectedNullValue(facesListEntries, "nullValueOrValue");
                } else {
                    context.unexpectedElementType(writer, facesListEntries, "nullValueOrValue", nullValueOrValueItem, org.apache.openejb.jee.FacesNullValue.class, java.lang.String.class);
                }
            }
        }

        context.afterMarshal(facesListEntries, LifecycleCallback.NONE);
    }

}
