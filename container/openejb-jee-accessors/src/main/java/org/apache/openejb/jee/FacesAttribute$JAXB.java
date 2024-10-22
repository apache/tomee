/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee;

import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


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
    extends JAXBObject<FacesAttribute>
{


    public FacesAttribute$JAXB() {
        super(FacesAttribute.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-attributeType".intern()), Text$JAXB.class, Icon$JAXB.class, FacesAttributeExtension$JAXB.class);
    }

    public static FacesAttribute readFacesAttribute(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesAttribute(XoXMLStreamWriter writer, FacesAttribute facesAttribute, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesAttribute, context);
    }

    public void write(XoXMLStreamWriter writer, FacesAttribute facesAttribute, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesAttribute, context);
    }

    public static final FacesAttribute _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesAttribute facesAttribute = new FacesAttribute();
        context.beforeUnmarshal(facesAttribute, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<FacesAttributeExtension> attributeExtension = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-attributeType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesAttribute.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesAttribute);
                facesAttribute.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("description" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<>();
                }
                descriptions.add(descriptionsItem);
            } else if (("display-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayNames
                Text displayNamesItem = readText(elementReader, context);
                if (displayNames == null) {
                    displayNames = new ArrayList<>();
                }
                displayNames.add(displayNamesItem);
            } else if (("icon" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = facesAttribute.icon;
                    if (icon!= null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<>();
                    }
                }
                icon.add(iconItem);
            } else if (("attribute-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: attributeName
                String attributeNameRaw = elementReader.getElementText();

                String attributeName;
                try {
                    attributeName = Adapters.collapsedStringAdapterAdapter.unmarshal(attributeNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesAttribute.attributeName = attributeName;
            } else if (("attribute-class" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: attributeClass
                String attributeClassRaw = elementReader.getElementText();

                String attributeClass;
                try {
                    attributeClass = Adapters.collapsedStringAdapterAdapter.unmarshal(attributeClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesAttribute.attributeClass = attributeClass;
            } else if (("default-value" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: defaultValue
                String defaultValueRaw = elementReader.getElementText();

                String defaultValue;
                try {
                    defaultValue = Adapters.collapsedStringAdapterAdapter.unmarshal(defaultValueRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesAttribute.defaultValue = defaultValue;
            } else if (("suggested-value" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: suggestedValue
                String suggestedValueRaw = elementReader.getElementText();

                String suggestedValue;
                try {
                    suggestedValue = Adapters.collapsedStringAdapterAdapter.unmarshal(suggestedValueRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesAttribute.suggestedValue = suggestedValue;
            } else if (("attribute-extension" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: attributeExtension
                FacesAttributeExtension attributeExtensionItem = readFacesAttributeExtension(elementReader, context);
                if (attributeExtension == null) {
                    attributeExtension = facesAttribute.attributeExtension;
                    if (attributeExtension!= null) {
                        attributeExtension.clear();
                    } else {
                        attributeExtension = new ArrayList<>();
                    }
                }
                attributeExtension.add(attributeExtensionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "attribute-name"), new QName("http://java.sun.com/xml/ns/javaee", "attribute-class"), new QName("http://java.sun.com/xml/ns/javaee", "default-value"), new QName("http://java.sun.com/xml/ns/javaee", "suggested-value"), new QName("http://java.sun.com/xml/ns/javaee", "attribute-extension"));
            }
        }
        if (descriptions!= null) {
            try {
                facesAttribute.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, FacesAttribute.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames!= null) {
            try {
                facesAttribute.setDisplayNames(displayNames.toArray(new Text[displayNames.size()] ));
            } catch (Exception e) {
                context.setterError(reader, FacesAttribute.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon!= null) {
            facesAttribute.icon = icon;
        }
        if (attributeExtension!= null) {
            facesAttribute.attributeExtension = attributeExtension;
        }

        context.afterUnmarshal(facesAttribute, LifecycleCallback.NONE);

        return facesAttribute;
    }

    public final FacesAttribute read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesAttribute facesAttribute, RuntimeContext context)
        throws Exception
    {
        if (facesAttribute == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesAttribute.class!= facesAttribute.getClass()) {
            context.unexpectedSubclass(writer, facesAttribute, FacesAttribute.class);
            return ;
        }

        context.beforeMarshal(facesAttribute, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesAttribute.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesAttribute, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = facesAttribute.getDescriptions();
        } catch (Exception e) {
            context.getterError(facesAttribute, "descriptions", FacesAttribute.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
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
        } catch (Exception e) {
            context.getterError(facesAttribute, "displayNames", FacesAttribute.class, "getDisplayNames", e);
        }
        if (displayNames!= null) {
            for (Text displayNamesItem: displayNames) {
                if (displayNamesItem!= null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesAttribute, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = facesAttribute.icon;
        if (icon!= null) {
            for (Icon iconItem: icon) {
                if (iconItem!= null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesAttribute, "icon");
                }
            }
        }

        // ELEMENT: attributeName
        String attributeNameRaw = facesAttribute.attributeName;
        String attributeName = null;
        try {
            attributeName = Adapters.collapsedStringAdapterAdapter.marshal(attributeNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesAttribute, "attributeName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (attributeName!= null) {
            writer.writeStartElement(prefix, "attribute-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(attributeName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesAttribute, "attributeName");
        }

        // ELEMENT: attributeClass
        String attributeClassRaw = facesAttribute.attributeClass;
        String attributeClass = null;
        try {
            attributeClass = Adapters.collapsedStringAdapterAdapter.marshal(attributeClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesAttribute, "attributeClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (attributeClass!= null) {
            writer.writeStartElement(prefix, "attribute-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(attributeClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesAttribute, "attributeClass");
        }

        // ELEMENT: defaultValue
        String defaultValueRaw = facesAttribute.defaultValue;
        String defaultValue = null;
        try {
            defaultValue = Adapters.collapsedStringAdapterAdapter.marshal(defaultValueRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesAttribute, "defaultValue", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (defaultValue!= null) {
            writer.writeStartElement(prefix, "default-value", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(defaultValue);
            writer.writeEndElement();
        }

        // ELEMENT: suggestedValue
        String suggestedValueRaw = facesAttribute.suggestedValue;
        String suggestedValue = null;
        try {
            suggestedValue = Adapters.collapsedStringAdapterAdapter.marshal(suggestedValueRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesAttribute, "suggestedValue", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (suggestedValue!= null) {
            writer.writeStartElement(prefix, "suggested-value", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(suggestedValue);
            writer.writeEndElement();
        }

        // ELEMENT: attributeExtension
        List<FacesAttributeExtension> attributeExtension = facesAttribute.attributeExtension;
        if (attributeExtension!= null) {
            for (FacesAttributeExtension attributeExtensionItem: attributeExtension) {
                if (attributeExtensionItem!= null) {
                    writer.writeStartElement(prefix, "attribute-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesAttributeExtension(writer, attributeExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesAttribute, LifecycleCallback.NONE);
    }

}
