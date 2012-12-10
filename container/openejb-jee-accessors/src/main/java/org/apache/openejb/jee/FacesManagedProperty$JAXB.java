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

import static org.apache.openejb.jee.FacesListEntries$JAXB.readFacesListEntries;
import static org.apache.openejb.jee.FacesListEntries$JAXB.writeFacesListEntries;
import static org.apache.openejb.jee.FacesMapEntries$JAXB.readFacesMapEntries;
import static org.apache.openejb.jee.FacesMapEntries$JAXB.writeFacesMapEntries;
import static org.apache.openejb.jee.FacesNullValue$JAXB.readFacesNullValue;
import static org.apache.openejb.jee.FacesNullValue$JAXB.writeFacesNullValue;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class FacesManagedProperty$JAXB
        extends JAXBObject<FacesManagedProperty> {


    public FacesManagedProperty$JAXB() {
        super(FacesManagedProperty.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-managed-propertyType".intern()), Text$JAXB.class, Icon$JAXB.class, FacesMapEntries$JAXB.class, FacesNullValue$JAXB.class, FacesListEntries$JAXB.class);
    }

    public static FacesManagedProperty readFacesManagedProperty(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesManagedProperty(XoXMLStreamWriter writer, FacesManagedProperty facesManagedProperty, RuntimeContext context)
            throws Exception {
        _write(writer, facesManagedProperty, context);
    }

    public void write(XoXMLStreamWriter writer, FacesManagedProperty facesManagedProperty, RuntimeContext context)
            throws Exception {
        _write(writer, facesManagedProperty, context);
    }

    public final static FacesManagedProperty _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesManagedProperty facesManagedProperty = new FacesManagedProperty();
        context.beforeUnmarshal(facesManagedProperty, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-managed-propertyType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesManagedProperty.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesManagedProperty);
                facesManagedProperty.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<Text>();
                }
                descriptions.add(descriptionsItem);
            } else if (("display-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayNames
                Text displayNamesItem = readText(elementReader, context);
                if (displayNames == null) {
                    displayNames = new ArrayList<Text>();
                }
                displayNames.add(displayNamesItem);
            } else if (("icon" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = facesManagedProperty.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("property-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: propertyName
                String propertyNameRaw = elementReader.getElementAsString();

                String propertyName;
                try {
                    propertyName = Adapters.collapsedStringAdapterAdapter.unmarshal(propertyNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesManagedProperty.propertyName = propertyName;
            } else if (("property-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: propertyClass
                String propertyClassRaw = elementReader.getElementAsString();

                String propertyClass;
                try {
                    propertyClass = Adapters.collapsedStringAdapterAdapter.unmarshal(propertyClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesManagedProperty.propertyClass = propertyClass;
            } else if (("map-entries" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: mapEntries
                FacesMapEntries mapEntries = readFacesMapEntries(elementReader, context);
                facesManagedProperty.mapEntries = mapEntries;
            } else if (("null-value" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: nullValue
                FacesNullValue nullValue = readFacesNullValue(elementReader, context);
                facesManagedProperty.nullValue = nullValue;
            } else if (("value" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: value
                String valueRaw = elementReader.getElementAsString();

                String value;
                try {
                    value = Adapters.collapsedStringAdapterAdapter.unmarshal(valueRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesManagedProperty.value = value;
            } else if (("list-entries" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: listEntries
                FacesListEntries listEntries = readFacesListEntries(elementReader, context);
                facesManagedProperty.listEntries = listEntries;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "property-name"), new QName("http://java.sun.com/xml/ns/javaee", "property-class"), new QName("http://java.sun.com/xml/ns/javaee", "map-entries"), new QName("http://java.sun.com/xml/ns/javaee", "null-value"), new QName("http://java.sun.com/xml/ns/javaee", "value"), new QName("http://java.sun.com/xml/ns/javaee", "list-entries"));
            }
        }
        if (descriptions != null) {
            try {
                facesManagedProperty.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, FacesManagedProperty.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                facesManagedProperty.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (Exception e) {
                context.setterError(reader, FacesManagedProperty.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            facesManagedProperty.icon = icon;
        }

        context.afterUnmarshal(facesManagedProperty, LifecycleCallback.NONE);

        return facesManagedProperty;
    }

    public final FacesManagedProperty read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, FacesManagedProperty facesManagedProperty, RuntimeContext context)
            throws Exception {
        if (facesManagedProperty == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesManagedProperty.class != facesManagedProperty.getClass()) {
            context.unexpectedSubclass(writer, facesManagedProperty, FacesManagedProperty.class);
            return;
        }

        context.beforeMarshal(facesManagedProperty, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesManagedProperty.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesManagedProperty, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = facesManagedProperty.getDescriptions();
        } catch (Exception e) {
            context.getterError(facesManagedProperty, "descriptions", FacesManagedProperty.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesManagedProperty, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = facesManagedProperty.getDisplayNames();
        } catch (Exception e) {
            context.getterError(facesManagedProperty, "displayNames", FacesManagedProperty.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesManagedProperty, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = facesManagedProperty.icon;
        if (icon != null) {
            for (Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesManagedProperty, "icon");
                }
            }
        }

        // ELEMENT: propertyName
        String propertyNameRaw = facesManagedProperty.propertyName;
        String propertyName = null;
        try {
            propertyName = Adapters.collapsedStringAdapterAdapter.marshal(propertyNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesManagedProperty, "propertyName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (propertyName != null) {
            writer.writeStartElement(prefix, "property-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(propertyName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesManagedProperty, "propertyName");
        }

        // ELEMENT: propertyClass
        String propertyClassRaw = facesManagedProperty.propertyClass;
        String propertyClass = null;
        try {
            propertyClass = Adapters.collapsedStringAdapterAdapter.marshal(propertyClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesManagedProperty, "propertyClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (propertyClass != null) {
            writer.writeStartElement(prefix, "property-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(propertyClass);
            writer.writeEndElement();
        }

        // ELEMENT: mapEntries
        FacesMapEntries mapEntries = facesManagedProperty.mapEntries;
        if (mapEntries != null) {
            writer.writeStartElement(prefix, "map-entries", "http://java.sun.com/xml/ns/javaee");
            writeFacesMapEntries(writer, mapEntries, context);
            writer.writeEndElement();
        }

        // ELEMENT: nullValue
        FacesNullValue nullValue = facesManagedProperty.nullValue;
        if (nullValue != null) {
            writer.writeStartElement(prefix, "null-value", "http://java.sun.com/xml/ns/javaee");
            writeFacesNullValue(writer, nullValue, context);
            writer.writeEndElement();
        }

        // ELEMENT: value
        String valueRaw = facesManagedProperty.value;
        String value = null;
        try {
            value = Adapters.collapsedStringAdapterAdapter.marshal(valueRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesManagedProperty, "value", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (value != null) {
            writer.writeStartElement(prefix, "value", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(value);
            writer.writeEndElement();
        }

        // ELEMENT: listEntries
        FacesListEntries listEntries = facesManagedProperty.listEntries;
        if (listEntries != null) {
            writer.writeStartElement(prefix, "list-entries", "http://java.sun.com/xml/ns/javaee");
            writeFacesListEntries(writer, listEntries, context);
            writer.writeEndElement();
        }

        context.afterMarshal(facesManagedProperty, LifecycleCallback.NONE);
    }

}
