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


import static org.apache.openejb.jee.FacesAttribute$JAXB.readFacesAttribute;
import static org.apache.openejb.jee.FacesAttribute$JAXB.writeFacesAttribute;
import static org.apache.openejb.jee.FacesConverterExtension$JAXB.readFacesConverterExtension;
import static org.apache.openejb.jee.FacesConverterExtension$JAXB.writeFacesConverterExtension;
import static org.apache.openejb.jee.FacesProperty$JAXB.readFacesProperty;
import static org.apache.openejb.jee.FacesProperty$JAXB.writeFacesProperty;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class FacesConverter$JAXB
    extends JAXBObject<FacesConverter>
{


    public FacesConverter$JAXB() {
        super(FacesConverter.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-converterType".intern()), Text$JAXB.class, Icon$JAXB.class, FacesAttribute$JAXB.class, FacesProperty$JAXB.class, FacesConverterExtension$JAXB.class);
    }

    public static FacesConverter readFacesConverter(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesConverter(XoXMLStreamWriter writer, FacesConverter facesConverter, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConverter, context);
    }

    public void write(XoXMLStreamWriter writer, FacesConverter facesConverter, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConverter, context);
    }

    public static final FacesConverter _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesConverter facesConverter = new FacesConverter();
        context.beforeUnmarshal(facesConverter, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<FacesAttribute> attribute1 = null;
        List<FacesProperty> property = null;
        List<FacesConverterExtension> converterExtension = null;
        List<Object> others = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-converterType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesConverter.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesConverter);
                facesConverter.id = id;
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
                    icon = facesConverter.icon;
                    if (icon!= null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<>();
                    }
                }
                icon.add(iconItem);
            } else if (("converter-id" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: converterId
                String converterIdRaw = elementReader.getElementText();

                String converterId;
                try {
                    converterId = Adapters.collapsedStringAdapterAdapter.unmarshal(converterIdRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesConverter.converterId = converterId;
            } else if (("converter-for-class" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: converterForClass
                String converterForClassRaw = elementReader.getElementText();

                String converterForClass;
                try {
                    converterForClass = Adapters.collapsedStringAdapterAdapter.unmarshal(converterForClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesConverter.converterForClass = converterForClass;
            } else if (("converter-class" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: converterClass
                String converterClassRaw = elementReader.getElementText();

                String converterClass;
                try {
                    converterClass = Adapters.collapsedStringAdapterAdapter.unmarshal(converterClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesConverter.converterClass = converterClass;
            } else if (("attribute" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: attribute
                FacesAttribute attributeItem = readFacesAttribute(elementReader, context);
                if (attribute1 == null) {
                    attribute1 = facesConverter.attribute;
                    if (attribute1 != null) {
                        attribute1 .clear();
                    } else {
                        attribute1 = new ArrayList<>();
                    }
                }
                attribute1 .add(attributeItem);
            } else if (("property" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: property
                FacesProperty propertyItem = readFacesProperty(elementReader, context);
                if (property == null) {
                    property = facesConverter.property;
                    if (property!= null) {
                        property.clear();
                    } else {
                        property = new ArrayList<>();
                    }
                }
                property.add(propertyItem);
            } else if (("converter-extension" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: converterExtension
                FacesConverterExtension converterExtensionItem = readFacesConverterExtension(elementReader, context);
                if (converterExtension == null) {
                    converterExtension = facesConverter.converterExtension;
                    if (converterExtension!= null) {
                        converterExtension.clear();
                    } else {
                        converterExtension = new ArrayList<>();
                    }
                }
                converterExtension.add(converterExtensionItem);
            } else {
                // ELEMENT_REF: others
                if (others == null) {
                    others = facesConverter.others;
                    if (others!= null) {
                        others.clear();
                    } else {
                        others = new ArrayList<>();
                    }
                }
                others.add(context.readXmlAny(elementReader, Object.class, false));
            }
        }
        if (descriptions!= null) {
            try {
                facesConverter.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, FacesConverter.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames!= null) {
            try {
                facesConverter.setDisplayNames(displayNames.toArray(new Text[displayNames.size()] ));
            } catch (Exception e) {
                context.setterError(reader, FacesConverter.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon!= null) {
            facesConverter.icon = icon;
        }
        if (attribute1 != null) {
            facesConverter.attribute = attribute1;
        }
        if (property!= null) {
            facesConverter.property = property;
        }
        if (converterExtension!= null) {
            facesConverter.converterExtension = converterExtension;
        }
        if (others!= null) {
            facesConverter.others = others;
        }

        context.afterUnmarshal(facesConverter, LifecycleCallback.NONE);

        return facesConverter;
    }

    public final FacesConverter read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesConverter facesConverter, RuntimeContext context)
        throws Exception
    {
        if (facesConverter == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesConverter.class!= facesConverter.getClass()) {
            context.unexpectedSubclass(writer, facesConverter, FacesConverter.class);
            return ;
        }

        context.beforeMarshal(facesConverter, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesConverter.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesConverter, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = facesConverter.getDescriptions();
        } catch (Exception e) {
            context.getterError(facesConverter, "descriptions", FacesConverter.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesConverter, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = facesConverter.getDisplayNames();
        } catch (Exception e) {
            context.getterError(facesConverter, "displayNames", FacesConverter.class, "getDisplayNames", e);
        }
        if (displayNames!= null) {
            for (Text displayNamesItem: displayNames) {
                if (displayNamesItem!= null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesConverter, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = facesConverter.icon;
        if (icon!= null) {
            for (Icon iconItem: icon) {
                if (iconItem!= null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesConverter, "icon");
                }
            }
        }

        // ELEMENT: converterId
        String converterIdRaw = facesConverter.converterId;
        String converterId = null;
        try {
            converterId = Adapters.collapsedStringAdapterAdapter.marshal(converterIdRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesConverter, "converterId", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (converterId!= null) {
            writer.writeStartElement(prefix, "converter-id", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(converterId);
            writer.writeEndElement();
        }

        // ELEMENT: converterForClass
        String converterForClassRaw = facesConverter.converterForClass;
        String converterForClass = null;
        try {
            converterForClass = Adapters.collapsedStringAdapterAdapter.marshal(converterForClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesConverter, "converterForClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (converterForClass!= null) {
            writer.writeStartElement(prefix, "converter-for-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(converterForClass);
            writer.writeEndElement();
        }

        // ELEMENT: converterClass
        String converterClassRaw = facesConverter.converterClass;
        String converterClass = null;
        try {
            converterClass = Adapters.collapsedStringAdapterAdapter.marshal(converterClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesConverter, "converterClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (converterClass!= null) {
            writer.writeStartElement(prefix, "converter-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(converterClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesConverter, "converterClass");
        }

        // ELEMENT: attribute
        List<FacesAttribute> attribute = facesConverter.attribute;
        if (attribute!= null) {
            for (FacesAttribute attributeItem: attribute) {
                writer.writeStartElement(prefix, "attribute", "http://java.sun.com/xml/ns/javaee");
                if (attributeItem!= null) {
                    writeFacesAttribute(writer, attributeItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: property
        List<FacesProperty> property = facesConverter.property;
        if (property!= null) {
            for (FacesProperty propertyItem: property) {
                writer.writeStartElement(prefix, "property", "http://java.sun.com/xml/ns/javaee");
                if (propertyItem!= null) {
                    writeFacesProperty(writer, propertyItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: converterExtension
        List<FacesConverterExtension> converterExtension = facesConverter.converterExtension;
        if (converterExtension!= null) {
            for (FacesConverterExtension converterExtensionItem: converterExtension) {
                if (converterExtensionItem!= null) {
                    writer.writeStartElement(prefix, "converter-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesConverterExtension(writer, converterExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT_REF: others
        List<Object> others = facesConverter.others;
        if (others!= null) {
            for (Object othersItem: others) {
                context.writeXmlAny(writer, facesConverter, "others", othersItem);
            }
        }

        context.afterMarshal(facesConverter, LifecycleCallback.NONE);
    }

}
