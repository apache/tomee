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
import static org.apache.openejb.jee.FacesComponentExtension$JAXB.readFacesComponentExtension;
import static org.apache.openejb.jee.FacesComponentExtension$JAXB.writeFacesComponentExtension;
import static org.apache.openejb.jee.FacesFacet$JAXB.readFacesFacet;
import static org.apache.openejb.jee.FacesFacet$JAXB.writeFacesFacet;
import static org.apache.openejb.jee.FacesProperty$JAXB.readFacesProperty;
import static org.apache.openejb.jee.FacesProperty$JAXB.writeFacesProperty;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class FacesComponent$JAXB
    extends JAXBObject<FacesComponent>
{


    public FacesComponent$JAXB() {
        super(FacesComponent.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-componentType".intern()), Text$JAXB.class, Icon$JAXB.class, FacesFacet$JAXB.class, FacesAttribute$JAXB.class, FacesProperty$JAXB.class, FacesComponentExtension$JAXB.class);
    }

    public static FacesComponent readFacesComponent(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesComponent(XoXMLStreamWriter writer, FacesComponent facesComponent, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesComponent, context);
    }

    public void write(XoXMLStreamWriter writer, FacesComponent facesComponent, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesComponent, context);
    }

    public static final FacesComponent _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesComponent facesComponent = new FacesComponent();
        context.beforeUnmarshal(facesComponent, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<FacesFacet> facet = null;
        List<FacesAttribute> attribute1 = null;
        List<FacesProperty> property = null;
        List<FacesComponentExtension> componentExtension = null;
        List<Object> others = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-componentType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesComponent.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesComponent);
                facesComponent.id = id;
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
                    icon = facesComponent.icon;
                    if (icon!= null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<>();
                    }
                }
                icon.add(iconItem);
            } else if (("component-type" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: componentType
                String componentTypeRaw = elementReader.getElementText();

                String componentType;
                try {
                    componentType = Adapters.collapsedStringAdapterAdapter.unmarshal(componentTypeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesComponent.componentType = componentType;
            } else if (("component-class" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: componentClass
                String componentClassRaw = elementReader.getElementText();

                String componentClass;
                try {
                    componentClass = Adapters.collapsedStringAdapterAdapter.unmarshal(componentClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesComponent.componentClass = componentClass;
            } else if (("facet" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: facet
                FacesFacet facetItem = readFacesFacet(elementReader, context);
                if (facet == null) {
                    facet = facesComponent.facet;
                    if (facet!= null) {
                        facet.clear();
                    } else {
                        facet = new ArrayList<>();
                    }
                }
                facet.add(facetItem);
            } else if (("attribute" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: attribute
                FacesAttribute attributeItem = readFacesAttribute(elementReader, context);
                if (attribute1 == null) {
                    attribute1 = facesComponent.attribute;
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
                    property = facesComponent.property;
                    if (property!= null) {
                        property.clear();
                    } else {
                        property = new ArrayList<>();
                    }
                }
                property.add(propertyItem);
            } else if (("component-extension" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: componentExtension
                FacesComponentExtension componentExtensionItem = readFacesComponentExtension(elementReader, context);
                if (componentExtension == null) {
                    componentExtension = facesComponent.componentExtension;
                    if (componentExtension!= null) {
                        componentExtension.clear();
                    } else {
                        componentExtension = new ArrayList<>();
                    }
                }
                componentExtension.add(componentExtensionItem);
            } else {
                // ELEMENT_REF: others
                if (others == null) {
                    others = facesComponent.others;
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
                facesComponent.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, FacesComponent.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames!= null) {
            try {
                facesComponent.setDisplayNames(displayNames.toArray(new Text[displayNames.size()] ));
            } catch (Exception e) {
                context.setterError(reader, FacesComponent.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon!= null) {
            facesComponent.icon = icon;
        }
        if (facet!= null) {
            facesComponent.facet = facet;
        }
        if (attribute1 != null) {
            facesComponent.attribute = attribute1;
        }
        if (property!= null) {
            facesComponent.property = property;
        }
        if (componentExtension!= null) {
            facesComponent.componentExtension = componentExtension;
        }
        if (others!= null) {
            facesComponent.others = others;
        }

        context.afterUnmarshal(facesComponent, LifecycleCallback.NONE);

        return facesComponent;
    }

    public final FacesComponent read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesComponent facesComponent, RuntimeContext context)
        throws Exception
    {
        if (facesComponent == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesComponent.class!= facesComponent.getClass()) {
            context.unexpectedSubclass(writer, facesComponent, FacesComponent.class);
            return ;
        }

        context.beforeMarshal(facesComponent, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesComponent.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesComponent, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = facesComponent.getDescriptions();
        } catch (Exception e) {
            context.getterError(facesComponent, "descriptions", FacesComponent.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesComponent, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = facesComponent.getDisplayNames();
        } catch (Exception e) {
            context.getterError(facesComponent, "displayNames", FacesComponent.class, "getDisplayNames", e);
        }
        if (displayNames!= null) {
            for (Text displayNamesItem: displayNames) {
                if (displayNamesItem!= null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesComponent, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = facesComponent.icon;
        if (icon!= null) {
            for (Icon iconItem: icon) {
                if (iconItem!= null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesComponent, "icon");
                }
            }
        }

        // ELEMENT: componentType
        String componentTypeRaw = facesComponent.componentType;
        String componentType = null;
        try {
            componentType = Adapters.collapsedStringAdapterAdapter.marshal(componentTypeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesComponent, "componentType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (componentType!= null) {
            writer.writeStartElement(prefix, "component-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(componentType);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesComponent, "componentType");
        }

        // ELEMENT: componentClass
        String componentClassRaw = facesComponent.componentClass;
        String componentClass = null;
        try {
            componentClass = Adapters.collapsedStringAdapterAdapter.marshal(componentClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesComponent, "componentClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (componentClass!= null) {
            writer.writeStartElement(prefix, "component-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(componentClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesComponent, "componentClass");
        }

        // ELEMENT: facet
        List<FacesFacet> facet = facesComponent.facet;
        if (facet!= null) {
            for (FacesFacet facetItem: facet) {
                writer.writeStartElement(prefix, "facet", "http://java.sun.com/xml/ns/javaee");
                if (facetItem!= null) {
                    writeFacesFacet(writer, facetItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: attribute
        List<FacesAttribute> attribute = facesComponent.attribute;
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
        List<FacesProperty> property = facesComponent.property;
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

        // ELEMENT: componentExtension
        List<FacesComponentExtension> componentExtension = facesComponent.componentExtension;
        if (componentExtension!= null) {
            for (FacesComponentExtension componentExtensionItem: componentExtension) {
                if (componentExtensionItem!= null) {
                    writer.writeStartElement(prefix, "component-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesComponentExtension(writer, componentExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT_REF: others
        List<Object> others = facesComponent.others;
        if (others!= null) {
            for (Object othersItem: others) {
                context.writeXmlAny(writer, facesComponent, "others", othersItem);
            }
        }

        context.afterMarshal(facesComponent, LifecycleCallback.NONE);
    }

}
