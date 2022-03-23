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

import static org.apache.openejb.jee.FacesAttributeExtension$JAXB.readFacesAttributeExtension;
import static org.apache.openejb.jee.FacesAttributeExtension$JAXB.writeFacesAttributeExtension;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class FacesAttribute$JAXB
    extends JAXBObject<FacesAttribute> {


    public FacesAttribute$JAXB() {
        super(FacesAttribute.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-attributeType".intern()), Text$JAXB.class, Icon$JAXB.class, FacesAttributeExtension$JAXB.class);
    }

    public static FacesAttribute readFacesAttribute(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesAttribute(final XoXMLStreamWriter writer, final FacesAttribute facesAttribute, final RuntimeContext context)
        throws Exception {
        _write(writer, facesAttribute, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesAttribute facesAttribute, final RuntimeContext context)
        throws Exception {
        _write(writer, facesAttribute, context);
    }

    public final static FacesAttribute _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesAttribute facesAttribute = new FacesAttribute();
        context.beforeUnmarshal(facesAttribute, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<FacesAttributeExtension> attributeExtension = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-attributeType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesAttribute.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesAttribute);
                facesAttribute.id = id;
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
                    icon = facesAttribute.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("attribute-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: attributeName
                final String attributeNameRaw = elementReader.getElementAsString();

                final String attributeName;
                try {
                    attributeName = Adapters.collapsedStringAdapterAdapter.unmarshal(attributeNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesAttribute.attributeName = attributeName;
            } else if (("attribute-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: attributeClass
                final String attributeClassRaw = elementReader.getElementAsString();

                final String attributeClass;
                try {
                    attributeClass = Adapters.collapsedStringAdapterAdapter.unmarshal(attributeClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesAttribute.attributeClass = attributeClass;
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

                facesAttribute.defaultValue = defaultValue;
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

                facesAttribute.suggestedValue = suggestedValue;
            } else if (("attribute-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: attributeExtension
                final FacesAttributeExtension attributeExtensionItem = readFacesAttributeExtension(elementReader, context);
                if (attributeExtension == null) {
                    attributeExtension = facesAttribute.attributeExtension;
                    if (attributeExtension != null) {
                        attributeExtension.clear();
                    } else {
                        attributeExtension = new ArrayList<FacesAttributeExtension>();
                    }
                }
                attributeExtension.add(attributeExtensionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "attribute-name"), new QName("http://java.sun.com/xml/ns/javaee", "attribute-class"), new QName("http://java.sun.com/xml/ns/javaee", "default-value"), new QName("http://java.sun.com/xml/ns/javaee", "suggested-value"), new QName("http://java.sun.com/xml/ns/javaee", "attribute-extension"));
            }
        }
        if (descriptions != null) {
            try {
                facesAttribute.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, FacesAttribute.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                facesAttribute.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, FacesAttribute.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            facesAttribute.icon = icon;
        }
        if (attributeExtension != null) {
            facesAttribute.attributeExtension = attributeExtension;
        }

        context.afterUnmarshal(facesAttribute, LifecycleCallback.NONE);

        return facesAttribute;
    }

    public final FacesAttribute read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesAttribute facesAttribute, RuntimeContext context)
        throws Exception {
        if (facesAttribute == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesAttribute.class != facesAttribute.getClass()) {
            context.unexpectedSubclass(writer, facesAttribute, FacesAttribute.class);
            return;
        }

        context.beforeMarshal(facesAttribute, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = facesAttribute.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(facesAttribute, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = facesAttribute.getDescriptions();
        } catch (final Exception e) {
            context.getterError(facesAttribute, "descriptions", FacesAttribute.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesAttribute, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = facesAttribute.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(facesAttribute, "displayNames", FacesAttribute.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesAttribute, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = facesAttribute.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesAttribute, "icon");
                }
            }
        }

        // ELEMENT: attributeName
        final String attributeNameRaw = facesAttribute.attributeName;
        String attributeName = null;
        try {
            attributeName = Adapters.collapsedStringAdapterAdapter.marshal(attributeNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesAttribute, "attributeName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (attributeName != null) {
            writer.writeStartElement(prefix, "attribute-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(attributeName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesAttribute, "attributeName");
        }

        // ELEMENT: attributeClass
        final String attributeClassRaw = facesAttribute.attributeClass;
        String attributeClass = null;
        try {
            attributeClass = Adapters.collapsedStringAdapterAdapter.marshal(attributeClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesAttribute, "attributeClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (attributeClass != null) {
            writer.writeStartElement(prefix, "attribute-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(attributeClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesAttribute, "attributeClass");
        }

        // ELEMENT: defaultValue
        final String defaultValueRaw = facesAttribute.defaultValue;
        String defaultValue = null;
        try {
            defaultValue = Adapters.collapsedStringAdapterAdapter.marshal(defaultValueRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesAttribute, "defaultValue", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (defaultValue != null) {
            writer.writeStartElement(prefix, "default-value", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(defaultValue);
            writer.writeEndElement();
        }

        // ELEMENT: suggestedValue
        final String suggestedValueRaw = facesAttribute.suggestedValue;
        String suggestedValue = null;
        try {
            suggestedValue = Adapters.collapsedStringAdapterAdapter.marshal(suggestedValueRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesAttribute, "suggestedValue", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (suggestedValue != null) {
            writer.writeStartElement(prefix, "suggested-value", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(suggestedValue);
            writer.writeEndElement();
        }

        // ELEMENT: attributeExtension
        final List<FacesAttributeExtension> attributeExtension = facesAttribute.attributeExtension;
        if (attributeExtension != null) {
            for (final FacesAttributeExtension attributeExtensionItem : attributeExtension) {
                if (attributeExtensionItem != null) {
                    writer.writeStartElement(prefix, "attribute-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesAttributeExtension(writer, attributeExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesAttribute, LifecycleCallback.NONE);
    }

}
