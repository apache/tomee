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

import static org.apache.openejb.jee.FacesListEntries$JAXB.readFacesListEntries;
import static org.apache.openejb.jee.FacesListEntries$JAXB.writeFacesListEntries;
import static org.apache.openejb.jee.FacesManagedBeanExtension$JAXB.readFacesManagedBeanExtension;
import static org.apache.openejb.jee.FacesManagedBeanExtension$JAXB.writeFacesManagedBeanExtension;
import static org.apache.openejb.jee.FacesManagedProperty$JAXB.readFacesManagedProperty;
import static org.apache.openejb.jee.FacesManagedProperty$JAXB.writeFacesManagedProperty;
import static org.apache.openejb.jee.FacesMapEntries$JAXB.readFacesMapEntries;
import static org.apache.openejb.jee.FacesMapEntries$JAXB.writeFacesMapEntries;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class FacesManagedBean$JAXB
        extends JAXBObject<FacesManagedBean> {


    public FacesManagedBean$JAXB() {
        super(FacesManagedBean.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-managed-beanType".intern()), Text$JAXB.class, Icon$JAXB.class, FacesManagedProperty$JAXB.class, FacesMapEntries$JAXB.class, FacesListEntries$JAXB.class, FacesManagedBeanExtension$JAXB.class);
    }

    public static FacesManagedBean readFacesManagedBean(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesManagedBean(XoXMLStreamWriter writer, FacesManagedBean facesManagedBean, RuntimeContext context)
            throws Exception {
        _write(writer, facesManagedBean, context);
    }

    public void write(XoXMLStreamWriter writer, FacesManagedBean facesManagedBean, RuntimeContext context)
            throws Exception {
        _write(writer, facesManagedBean, context);
    }

    public final static FacesManagedBean _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesManagedBean facesManagedBean = new FacesManagedBean();
        context.beforeUnmarshal(facesManagedBean, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<FacesManagedProperty> managedProperty = null;
        List<FacesManagedBeanExtension> managedBeanExtension = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-managed-beanType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesManagedBean.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesManagedBean);
                facesManagedBean.id = id;
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
                    icon = facesManagedBean.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("managed-bean-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: managedBeanName
                String managedBeanNameRaw = elementReader.getElementAsString();

                String managedBeanName;
                try {
                    managedBeanName = Adapters.collapsedStringAdapterAdapter.unmarshal(managedBeanNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesManagedBean.managedBeanName = managedBeanName;
            } else if (("managed-bean-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: managedBeanClass
                String managedBeanClassRaw = elementReader.getElementAsString();

                String managedBeanClass;
                try {
                    managedBeanClass = Adapters.collapsedStringAdapterAdapter.unmarshal(managedBeanClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesManagedBean.managedBeanClass = managedBeanClass;
            } else if (("managed-bean-scope" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: managedBeanScope
                String managedBeanScopeRaw = elementReader.getElementAsString();

                String managedBeanScope;
                try {
                    managedBeanScope = Adapters.collapsedStringAdapterAdapter.unmarshal(managedBeanScopeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesManagedBean.managedBeanScope = managedBeanScope;
            } else if (("managed-property" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: managedProperty
                FacesManagedProperty managedPropertyItem = readFacesManagedProperty(elementReader, context);
                if (managedProperty == null) {
                    managedProperty = facesManagedBean.managedProperty;
                    if (managedProperty != null) {
                        managedProperty.clear();
                    } else {
                        managedProperty = new ArrayList<FacesManagedProperty>();
                    }
                }
                managedProperty.add(managedPropertyItem);
            } else if (("map-entries" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: mapEntries
                FacesMapEntries mapEntries = readFacesMapEntries(elementReader, context);
                facesManagedBean.mapEntries = mapEntries;
            } else if (("list-entries" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: listEntries
                FacesListEntries listEntries = readFacesListEntries(elementReader, context);
                facesManagedBean.listEntries = listEntries;
            } else if (("managed-bean-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: managedBeanExtension
                FacesManagedBeanExtension managedBeanExtensionItem = readFacesManagedBeanExtension(elementReader, context);
                if (managedBeanExtension == null) {
                    managedBeanExtension = facesManagedBean.managedBeanExtension;
                    if (managedBeanExtension != null) {
                        managedBeanExtension.clear();
                    } else {
                        managedBeanExtension = new ArrayList<FacesManagedBeanExtension>();
                    }
                }
                managedBeanExtension.add(managedBeanExtensionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "managed-bean-name"), new QName("http://java.sun.com/xml/ns/javaee", "managed-bean-class"), new QName("http://java.sun.com/xml/ns/javaee", "managed-bean-scope"), new QName("http://java.sun.com/xml/ns/javaee", "managed-property"), new QName("http://java.sun.com/xml/ns/javaee", "map-entries"), new QName("http://java.sun.com/xml/ns/javaee", "list-entries"), new QName("http://java.sun.com/xml/ns/javaee", "managed-bean-extension"));
            }
        }
        if (descriptions != null) {
            try {
                facesManagedBean.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, FacesManagedBean.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                facesManagedBean.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (Exception e) {
                context.setterError(reader, FacesManagedBean.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            facesManagedBean.icon = icon;
        }
        if (managedProperty != null) {
            facesManagedBean.managedProperty = managedProperty;
        }
        if (managedBeanExtension != null) {
            facesManagedBean.managedBeanExtension = managedBeanExtension;
        }

        context.afterUnmarshal(facesManagedBean, LifecycleCallback.NONE);

        return facesManagedBean;
    }

    public final FacesManagedBean read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, FacesManagedBean facesManagedBean, RuntimeContext context)
            throws Exception {
        if (facesManagedBean == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesManagedBean.class != facesManagedBean.getClass()) {
            context.unexpectedSubclass(writer, facesManagedBean, FacesManagedBean.class);
            return;
        }

        context.beforeMarshal(facesManagedBean, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesManagedBean.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesManagedBean, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = facesManagedBean.getDescriptions();
        } catch (Exception e) {
            context.getterError(facesManagedBean, "descriptions", FacesManagedBean.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesManagedBean, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = facesManagedBean.getDisplayNames();
        } catch (Exception e) {
            context.getterError(facesManagedBean, "displayNames", FacesManagedBean.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesManagedBean, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = facesManagedBean.icon;
        if (icon != null) {
            for (Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesManagedBean, "icon");
                }
            }
        }

        // ELEMENT: managedBeanName
        String managedBeanNameRaw = facesManagedBean.managedBeanName;
        String managedBeanName = null;
        try {
            managedBeanName = Adapters.collapsedStringAdapterAdapter.marshal(managedBeanNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesManagedBean, "managedBeanName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (managedBeanName != null) {
            writer.writeStartElement(prefix, "managed-bean-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(managedBeanName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesManagedBean, "managedBeanName");
        }

        // ELEMENT: managedBeanClass
        String managedBeanClassRaw = facesManagedBean.managedBeanClass;
        String managedBeanClass = null;
        try {
            managedBeanClass = Adapters.collapsedStringAdapterAdapter.marshal(managedBeanClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesManagedBean, "managedBeanClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (managedBeanClass != null) {
            writer.writeStartElement(prefix, "managed-bean-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(managedBeanClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesManagedBean, "managedBeanClass");
        }

        // ELEMENT: managedBeanScope
        String managedBeanScopeRaw = facesManagedBean.managedBeanScope;
        String managedBeanScope = null;
        try {
            managedBeanScope = Adapters.collapsedStringAdapterAdapter.marshal(managedBeanScopeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesManagedBean, "managedBeanScope", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (managedBeanScope != null) {
            writer.writeStartElement(prefix, "managed-bean-scope", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(managedBeanScope);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesManagedBean, "managedBeanScope");
        }

        // ELEMENT: managedProperty
        List<FacesManagedProperty> managedProperty = facesManagedBean.managedProperty;
        if (managedProperty != null) {
            for (FacesManagedProperty managedPropertyItem : managedProperty) {
                if (managedPropertyItem != null) {
                    writer.writeStartElement(prefix, "managed-property", "http://java.sun.com/xml/ns/javaee");
                    writeFacesManagedProperty(writer, managedPropertyItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: mapEntries
        FacesMapEntries mapEntries = facesManagedBean.mapEntries;
        if (mapEntries != null) {
            writer.writeStartElement(prefix, "map-entries", "http://java.sun.com/xml/ns/javaee");
            writeFacesMapEntries(writer, mapEntries, context);
            writer.writeEndElement();
        }

        // ELEMENT: listEntries
        FacesListEntries listEntries = facesManagedBean.listEntries;
        if (listEntries != null) {
            writer.writeStartElement(prefix, "list-entries", "http://java.sun.com/xml/ns/javaee");
            writeFacesListEntries(writer, listEntries, context);
            writer.writeEndElement();
        }

        // ELEMENT: managedBeanExtension
        List<FacesManagedBeanExtension> managedBeanExtension = facesManagedBean.managedBeanExtension;
        if (managedBeanExtension != null) {
            for (FacesManagedBeanExtension managedBeanExtensionItem : managedBeanExtension) {
                if (managedBeanExtensionItem != null) {
                    writer.writeStartElement(prefix, "managed-bean-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesManagedBeanExtension(writer, managedBeanExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesManagedBean, LifecycleCallback.NONE);
    }

}
