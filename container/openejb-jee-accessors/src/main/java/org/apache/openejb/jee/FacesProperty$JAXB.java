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

import static org.apache.openejb.jee.FacesPropertyExtension$JAXB.readFacesPropertyExtension;
import static org.apache.openejb.jee.FacesPropertyExtension$JAXB.writeFacesPropertyExtension;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class FacesProperty$JAXB
    extends JAXBObject<FacesProperty> {


    public FacesProperty$JAXB() {
        super(FacesProperty.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-propertyType".intern()), Text$JAXB.class, Icon$JAXB.class, FacesPropertyExtension$JAXB.class);
    }

    public static FacesProperty readFacesProperty(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesProperty(final XoXMLStreamWriter writer, final FacesProperty facesProperty, final RuntimeContext context)
        throws Exception {
        _write(writer, facesProperty, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesProperty facesProperty, final RuntimeContext context)
        throws Exception {
        _write(writer, facesProperty, context);
    }

    public final static FacesProperty _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesProperty facesProperty = new FacesProperty();
        context.beforeUnmarshal(facesProperty, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<FacesPropertyExtension> propertyExtension = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-propertyType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesProperty.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesProperty);
                facesProperty.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                final Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<Text>();
                }
                descriptions.add(descriptionsItem);
            } else if (("display-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayNames
                final Text displayNamesItem = readText(elementReader, context);
                if (displayNames == null) {
                    displayNames = new ArrayList<Text>();
                }
                displayNames.add(displayNamesItem);
            } else if (("icon" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                final Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = facesProperty.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("property-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: propertyName
                final String propertyNameRaw = elementReader.getElementAsString();

                final String propertyName;
                try {
                    propertyName = Adapters.collapsedStringAdapterAdapter.unmarshal(propertyNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesProperty.propertyName = propertyName;
            } else if (("property-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: propertyClass
                final String propertyClassRaw = elementReader.getElementAsString();

                final String propertyClass;
                try {
                    propertyClass = Adapters.collapsedStringAdapterAdapter.unmarshal(propertyClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesProperty.propertyClass = propertyClass;
            } else if (("default-value" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: defaultValue
                final String defaultValueRaw = elementReader.getElementAsString();

                final String defaultValue;
                try {
                    defaultValue = Adapters.collapsedStringAdapterAdapter.unmarshal(defaultValueRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesProperty.defaultValue = defaultValue;
            } else if (("suggested-value" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: suggestedValue
                final String suggestedValueRaw = elementReader.getElementAsString();

                final String suggestedValue;
                try {
                    suggestedValue = Adapters.collapsedStringAdapterAdapter.unmarshal(suggestedValueRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesProperty.suggestedValue = suggestedValue;
            } else if (("property-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: propertyExtension
                final FacesPropertyExtension propertyExtensionItem = readFacesPropertyExtension(elementReader, context);
                if (propertyExtension == null) {
                    propertyExtension = facesProperty.propertyExtension;
                    if (propertyExtension != null) {
                        propertyExtension.clear();
                    } else {
                        propertyExtension = new ArrayList<FacesPropertyExtension>();
                    }
                }
                propertyExtension.add(propertyExtensionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "property-name"), new QName("http://java.sun.com/xml/ns/javaee", "property-class"), new QName("http://java.sun.com/xml/ns/javaee", "default-value"), new QName("http://java.sun.com/xml/ns/javaee", "suggested-value"), new QName("http://java.sun.com/xml/ns/javaee", "property-extension"));
            }
        }
        if (descriptions != null) {
            try {
                facesProperty.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, FacesProperty.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                facesProperty.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, FacesProperty.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            facesProperty.icon = icon;
        }
        if (propertyExtension != null) {
            facesProperty.propertyExtension = propertyExtension;
        }

        context.afterUnmarshal(facesProperty, LifecycleCallback.NONE);

        return facesProperty;
    }

    public final FacesProperty read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesProperty facesProperty, RuntimeContext context)
        throws Exception {
        if (facesProperty == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesProperty.class != facesProperty.getClass()) {
            context.unexpectedSubclass(writer, facesProperty, FacesProperty.class);
            return;
        }

        context.beforeMarshal(facesProperty, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = facesProperty.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(facesProperty, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = facesProperty.getDescriptions();
        } catch (final Exception e) {
            context.getterError(facesProperty, "descriptions", FacesProperty.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesProperty, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = facesProperty.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(facesProperty, "displayNames", FacesProperty.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesProperty, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = facesProperty.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesProperty, "icon");
                }
            }
        }

        // ELEMENT: propertyName
        final String propertyNameRaw = facesProperty.propertyName;
        String propertyName = null;
        try {
            propertyName = Adapters.collapsedStringAdapterAdapter.marshal(propertyNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesProperty, "propertyName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (propertyName != null) {
            writer.writeStartElement(prefix, "property-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(propertyName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesProperty, "propertyName");
        }

        // ELEMENT: propertyClass
        final String propertyClassRaw = facesProperty.propertyClass;
        String propertyClass = null;
        try {
            propertyClass = Adapters.collapsedStringAdapterAdapter.marshal(propertyClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesProperty, "propertyClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (propertyClass != null) {
            writer.writeStartElement(prefix, "property-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(propertyClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesProperty, "propertyClass");
        }

        // ELEMENT: defaultValue
        final String defaultValueRaw = facesProperty.defaultValue;
        String defaultValue = null;
        try {
            defaultValue = Adapters.collapsedStringAdapterAdapter.marshal(defaultValueRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesProperty, "defaultValue", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (defaultValue != null) {
            writer.writeStartElement(prefix, "default-value", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(defaultValue);
            writer.writeEndElement();
        }

        // ELEMENT: suggestedValue
        final String suggestedValueRaw = facesProperty.suggestedValue;
        String suggestedValue = null;
        try {
            suggestedValue = Adapters.collapsedStringAdapterAdapter.marshal(suggestedValueRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesProperty, "suggestedValue", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (suggestedValue != null) {
            writer.writeStartElement(prefix, "suggested-value", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(suggestedValue);
            writer.writeEndElement();
        }

        // ELEMENT: propertyExtension
        final List<FacesPropertyExtension> propertyExtension = facesProperty.propertyExtension;
        if (propertyExtension != null) {
            for (final FacesPropertyExtension propertyExtensionItem : propertyExtension) {
                if (propertyExtensionItem != null) {
                    writer.writeStartElement(prefix, "property-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesPropertyExtension(writer, propertyExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesProperty, LifecycleCallback.NONE);
    }

}
