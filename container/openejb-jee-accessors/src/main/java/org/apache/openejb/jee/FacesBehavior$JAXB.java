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

import static org.apache.openejb.jee.FacesAttribute$JAXB.readFacesAttribute;
import static org.apache.openejb.jee.FacesAttribute$JAXB.writeFacesAttribute;
import static org.apache.openejb.jee.FacesBehaviorExtension$JAXB.readFacesBehaviorExtension;
import static org.apache.openejb.jee.FacesBehaviorExtension$JAXB.writeFacesBehaviorExtension;
import static org.apache.openejb.jee.FacesProperty$JAXB.readFacesProperty;
import static org.apache.openejb.jee.FacesProperty$JAXB.writeFacesProperty;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class FacesBehavior$JAXB
        extends JAXBObject<FacesBehavior> {


    public FacesBehavior$JAXB() {
        super(FacesBehavior.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-behaviorType".intern()), Text$JAXB.class, Icon$JAXB.class, FacesAttribute$JAXB.class, FacesProperty$JAXB.class, FacesBehaviorExtension$JAXB.class);
    }

    public static FacesBehavior readFacesBehavior(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesBehavior(XoXMLStreamWriter writer, FacesBehavior facesBehavior, RuntimeContext context)
            throws Exception {
        _write(writer, facesBehavior, context);
    }

    public void write(XoXMLStreamWriter writer, FacesBehavior facesBehavior, RuntimeContext context)
            throws Exception {
        _write(writer, facesBehavior, context);
    }

    public final static FacesBehavior _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesBehavior facesBehavior = new FacesBehavior();
        context.beforeUnmarshal(facesBehavior, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<FacesAttribute> attribute1 = null;
        List<FacesProperty> property = null;
        List<FacesBehaviorExtension> behaviorExtension = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-behaviorType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesBehavior.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute);
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
                    icon = facesBehavior.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("behavior-id" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: behaviorId
                String behaviorIdRaw = elementReader.getElementAsString();

                String behaviorId;
                try {
                    behaviorId = Adapters.collapsedStringAdapterAdapter.unmarshal(behaviorIdRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesBehavior.behaviorId = behaviorId;
            } else if (("behavior-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: behaviorClass
                String behaviorClassRaw = elementReader.getElementAsString();

                String behaviorClass;
                try {
                    behaviorClass = Adapters.collapsedStringAdapterAdapter.unmarshal(behaviorClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesBehavior.behaviorClass = behaviorClass;
            } else if (("attribute" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: attribute
                FacesAttribute attributeItem = readFacesAttribute(elementReader, context);
                if (attribute1 == null) {
                    attribute1 = facesBehavior.attribute;
                    if (attribute1 != null) {
                        attribute1.clear();
                    } else {
                        attribute1 = new ArrayList<FacesAttribute>();
                    }
                }
                attribute1.add(attributeItem);
            } else if (("property" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: property
                FacesProperty propertyItem = readFacesProperty(elementReader, context);
                if (property == null) {
                    property = facesBehavior.property;
                    if (property != null) {
                        property.clear();
                    } else {
                        property = new ArrayList<FacesProperty>();
                    }
                }
                property.add(propertyItem);
            } else if (("behavior-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: behaviorExtension
                FacesBehaviorExtension behaviorExtensionItem = readFacesBehaviorExtension(elementReader, context);
                if (behaviorExtension == null) {
                    behaviorExtension = facesBehavior.behaviorExtension;
                    if (behaviorExtension != null) {
                        behaviorExtension.clear();
                    } else {
                        behaviorExtension = new ArrayList<FacesBehaviorExtension>();
                    }
                }
                behaviorExtension.add(behaviorExtensionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "behavior-id"), new QName("http://java.sun.com/xml/ns/javaee", "behavior-class"), new QName("http://java.sun.com/xml/ns/javaee", "attribute"), new QName("http://java.sun.com/xml/ns/javaee", "property"), new QName("http://java.sun.com/xml/ns/javaee", "behavior-extension"));
            }
        }
        if (descriptions != null) {
            try {
                facesBehavior.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, FacesBehavior.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                facesBehavior.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (Exception e) {
                context.setterError(reader, FacesBehavior.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            facesBehavior.icon = icon;
        }
        if (attribute1 != null) {
            facesBehavior.attribute = attribute1;
        }
        if (property != null) {
            facesBehavior.property = property;
        }
        if (behaviorExtension != null) {
            facesBehavior.behaviorExtension = behaviorExtension;
        }

        context.afterUnmarshal(facesBehavior, LifecycleCallback.NONE);

        return facesBehavior;
    }

    public final FacesBehavior read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, FacesBehavior facesBehavior, RuntimeContext context)
            throws Exception {
        if (facesBehavior == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesBehavior.class != facesBehavior.getClass()) {
            context.unexpectedSubclass(writer, facesBehavior, FacesBehavior.class);
            return;
        }

        context.beforeMarshal(facesBehavior, LifecycleCallback.NONE);


        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = facesBehavior.getDescriptions();
        } catch (Exception e) {
            context.getterError(facesBehavior, "descriptions", FacesBehavior.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesBehavior, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = facesBehavior.getDisplayNames();
        } catch (Exception e) {
            context.getterError(facesBehavior, "displayNames", FacesBehavior.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesBehavior, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = facesBehavior.icon;
        if (icon != null) {
            for (Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesBehavior, "icon");
                }
            }
        }

        // ELEMENT: behaviorId
        String behaviorIdRaw = facesBehavior.behaviorId;
        String behaviorId = null;
        try {
            behaviorId = Adapters.collapsedStringAdapterAdapter.marshal(behaviorIdRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesBehavior, "behaviorId", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (behaviorId != null) {
            writer.writeStartElement(prefix, "behavior-id", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(behaviorId);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesBehavior, "behaviorId");
        }

        // ELEMENT: behaviorClass
        String behaviorClassRaw = facesBehavior.behaviorClass;
        String behaviorClass = null;
        try {
            behaviorClass = Adapters.collapsedStringAdapterAdapter.marshal(behaviorClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesBehavior, "behaviorClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (behaviorClass != null) {
            writer.writeStartElement(prefix, "behavior-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(behaviorClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesBehavior, "behaviorClass");
        }

        // ELEMENT: attribute
        List<FacesAttribute> attribute = facesBehavior.attribute;
        if (attribute != null) {
            for (FacesAttribute attributeItem : attribute) {
                writer.writeStartElement(prefix, "attribute", "http://java.sun.com/xml/ns/javaee");
                if (attributeItem != null) {
                    writeFacesAttribute(writer, attributeItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: property
        List<FacesProperty> property = facesBehavior.property;
        if (property != null) {
            for (FacesProperty propertyItem : property) {
                writer.writeStartElement(prefix, "property", "http://java.sun.com/xml/ns/javaee");
                if (propertyItem != null) {
                    writeFacesProperty(writer, propertyItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: behaviorExtension
        List<FacesBehaviorExtension> behaviorExtension = facesBehavior.behaviorExtension;
        if (behaviorExtension != null) {
            for (FacesBehaviorExtension behaviorExtensionItem : behaviorExtension) {
                if (behaviorExtensionItem != null) {
                    writer.writeStartElement(prefix, "behavior-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesBehaviorExtension(writer, behaviorExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesBehavior, LifecycleCallback.NONE);
    }

}
