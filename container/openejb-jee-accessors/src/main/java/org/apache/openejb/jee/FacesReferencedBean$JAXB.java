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

import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class FacesReferencedBean$JAXB
        extends JAXBObject<FacesReferencedBean> {


    public FacesReferencedBean$JAXB() {
        super(FacesReferencedBean.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-referenced-beanType".intern()), Text$JAXB.class, Icon$JAXB.class);
    }

    public static FacesReferencedBean readFacesReferencedBean(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesReferencedBean(XoXMLStreamWriter writer, FacesReferencedBean facesReferencedBean, RuntimeContext context)
            throws Exception {
        _write(writer, facesReferencedBean, context);
    }

    public void write(XoXMLStreamWriter writer, FacesReferencedBean facesReferencedBean, RuntimeContext context)
            throws Exception {
        _write(writer, facesReferencedBean, context);
    }

    public final static FacesReferencedBean _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesReferencedBean facesReferencedBean = new FacesReferencedBean();
        context.beforeUnmarshal(facesReferencedBean, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-referenced-beanType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesReferencedBean.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesReferencedBean);
                facesReferencedBean.id = id;
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
                    icon = facesReferencedBean.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("referenced-bean-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: referencedBeanName
                String referencedBeanNameRaw = elementReader.getElementAsString();

                String referencedBeanName;
                try {
                    referencedBeanName = Adapters.collapsedStringAdapterAdapter.unmarshal(referencedBeanNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesReferencedBean.referencedBeanName = referencedBeanName;
            } else if (("referenced-bean-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: referencedBeanClass
                String referencedBeanClassRaw = elementReader.getElementAsString();

                String referencedBeanClass;
                try {
                    referencedBeanClass = Adapters.collapsedStringAdapterAdapter.unmarshal(referencedBeanClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesReferencedBean.referencedBeanClass = referencedBeanClass;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "referenced-bean-name"), new QName("http://java.sun.com/xml/ns/javaee", "referenced-bean-class"));
            }
        }
        if (descriptions != null) {
            try {
                facesReferencedBean.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, FacesReferencedBean.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                facesReferencedBean.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (Exception e) {
                context.setterError(reader, FacesReferencedBean.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            facesReferencedBean.icon = icon;
        }

        context.afterUnmarshal(facesReferencedBean, LifecycleCallback.NONE);

        return facesReferencedBean;
    }

    public final FacesReferencedBean read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, FacesReferencedBean facesReferencedBean, RuntimeContext context)
            throws Exception {
        if (facesReferencedBean == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesReferencedBean.class != facesReferencedBean.getClass()) {
            context.unexpectedSubclass(writer, facesReferencedBean, FacesReferencedBean.class);
            return;
        }

        context.beforeMarshal(facesReferencedBean, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesReferencedBean.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesReferencedBean, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = facesReferencedBean.getDescriptions();
        } catch (Exception e) {
            context.getterError(facesReferencedBean, "descriptions", FacesReferencedBean.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesReferencedBean, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = facesReferencedBean.getDisplayNames();
        } catch (Exception e) {
            context.getterError(facesReferencedBean, "displayNames", FacesReferencedBean.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesReferencedBean, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = facesReferencedBean.icon;
        if (icon != null) {
            for (Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesReferencedBean, "icon");
                }
            }
        }

        // ELEMENT: referencedBeanName
        String referencedBeanNameRaw = facesReferencedBean.referencedBeanName;
        String referencedBeanName = null;
        try {
            referencedBeanName = Adapters.collapsedStringAdapterAdapter.marshal(referencedBeanNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesReferencedBean, "referencedBeanName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (referencedBeanName != null) {
            writer.writeStartElement(prefix, "referenced-bean-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(referencedBeanName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesReferencedBean, "referencedBeanName");
        }

        // ELEMENT: referencedBeanClass
        String referencedBeanClassRaw = facesReferencedBean.referencedBeanClass;
        String referencedBeanClass = null;
        try {
            referencedBeanClass = Adapters.collapsedStringAdapterAdapter.marshal(referencedBeanClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesReferencedBean, "referencedBeanClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (referencedBeanClass != null) {
            writer.writeStartElement(prefix, "referenced-bean-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(referencedBeanClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesReferencedBean, "referencedBeanClass");
        }

        context.afterMarshal(facesReferencedBean, LifecycleCallback.NONE);
    }

}
