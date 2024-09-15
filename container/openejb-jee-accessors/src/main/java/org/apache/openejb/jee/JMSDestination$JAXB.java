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


import static org.apache.openejb.jee.Property$JAXB.readProperty;
import static org.apache.openejb.jee.Property$JAXB.writeProperty;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class JMSDestination$JAXB
    extends JAXBObject<org.apache.openejb.jee.JMSDestination>
{


    public JMSDestination$JAXB() {
        super(org.apache.openejb.jee.JMSDestination.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "jms-destinationType".intern()), Text$JAXB.class, Property$JAXB.class);
    }

    public static org.apache.openejb.jee.JMSDestination readJMSDestination(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeJMSDestination(XoXMLStreamWriter writer, org.apache.openejb.jee.JMSDestination JMSDestination, RuntimeContext context)
        throws Exception
    {
        _write(writer, JMSDestination, context);
    }

    public void write(XoXMLStreamWriter writer, org.apache.openejb.jee.JMSDestination JMSDestination, RuntimeContext context)
        throws Exception
    {
        _write(writer, JMSDestination, context);
    }

    public static final org.apache.openejb.jee.JMSDestination _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        org.apache.openejb.jee.JMSDestination JMSDestination = new org.apache.openejb.jee.JMSDestination();
        context.beforeUnmarshal(JMSDestination, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        List<Property> property = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("jms-destinationType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, org.apache.openejb.jee.JMSDestination.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, JMSDestination);
                JMSDestination.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: name
                String nameRaw = elementReader.getElementText();

                String name;
                try {
                    name = Adapters.collapsedStringAdapterAdapter.unmarshal(nameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                JMSDestination.name = name;
            } else if (("description" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<>();
                }
                descriptions.add(descriptionsItem);
            } else if (("class-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: className
                String classNameRaw = elementReader.getElementText();

                String className;
                try {
                    className = Adapters.collapsedStringAdapterAdapter.unmarshal(classNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                JMSDestination.className = className;
            } else if (("interface-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: interfaceName
                String interfaceNameRaw = elementReader.getElementText();

                String interfaceName;
                try {
                    interfaceName = Adapters.collapsedStringAdapterAdapter.unmarshal(interfaceNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                JMSDestination.interfaceName = interfaceName;
            } else if (("resource-adapter-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceAdapter
                String resourceAdapterRaw = elementReader.getElementText();

                String resourceAdapter;
                try {
                    resourceAdapter = Adapters.collapsedStringAdapterAdapter.unmarshal(resourceAdapterRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                JMSDestination.resourceAdapter = resourceAdapter;
            } else if (("destination-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: destinationName
                String destinationNameRaw = elementReader.getElementText();

                String destinationName;
                try {
                    destinationName = Adapters.collapsedStringAdapterAdapter.unmarshal(destinationNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                JMSDestination.destinationName = destinationName;
            } else if (("property" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: property
                Property propertyItem = readProperty(elementReader, context);
                if (property == null) {
                    property = JMSDestination.property;
                    if (property!= null) {
                        property.clear();
                    } else {
                        property = new ArrayList<>();
                    }
                }
                property.add(propertyItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "name"), new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "class-name"), new QName("http://java.sun.com/xml/ns/javaee", "interface-name"), new QName("http://java.sun.com/xml/ns/javaee", "resource-adapter-name"), new QName("http://java.sun.com/xml/ns/javaee", "destination-name"), new QName("http://java.sun.com/xml/ns/javaee", "property"));
            }
        }
        if (property!= null) {
            JMSDestination.property = property;
        }

        context.afterUnmarshal(JMSDestination, LifecycleCallback.NONE);

        return JMSDestination;
    }

    public final org.apache.openejb.jee.JMSDestination read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, org.apache.openejb.jee.JMSDestination JMSDestination, RuntimeContext context)
        throws Exception
    {
        if (JMSDestination == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (org.apache.openejb.jee.JMSDestination.class!= JMSDestination.getClass()) {
            context.unexpectedSubclass(writer, JMSDestination, org.apache.openejb.jee.JMSDestination.class);
            return ;
        }

        context.beforeMarshal(JMSDestination, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = JMSDestination.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(JMSDestination, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: name
        String nameRaw = JMSDestination.name;
        String name = null;
        try {
            name = Adapters.collapsedStringAdapterAdapter.marshal(nameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(JMSDestination, "name", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (name!= null) {
            writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(name);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(JMSDestination, "name");
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = JMSDestination.getDescriptions();
        } catch (Exception e) {
            context.getterError(JMSDestination, "descriptions", org.apache.openejb.jee.JMSDestination.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: className
        String classNameRaw = JMSDestination.className;
        String className = null;
        try {
            className = Adapters.collapsedStringAdapterAdapter.marshal(classNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(JMSDestination, "className", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (className!= null) {
            writer.writeStartElement(prefix, "class-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(className);
            writer.writeEndElement();
        }

        // ELEMENT: interfaceName
        String interfaceNameRaw = JMSDestination.interfaceName;
        String interfaceName = null;
        try {
            interfaceName = Adapters.collapsedStringAdapterAdapter.marshal(interfaceNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(JMSDestination, "interfaceName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (interfaceName!= null) {
            writer.writeStartElement(prefix, "interface-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(interfaceName);
            writer.writeEndElement();
        }

        // ELEMENT: resourceAdapter
        String resourceAdapterRaw = JMSDestination.resourceAdapter;
        String resourceAdapter = null;
        try {
            resourceAdapter = Adapters.collapsedStringAdapterAdapter.marshal(resourceAdapterRaw);
        } catch (Exception e) {
            context.xmlAdapterError(JMSDestination, "resourceAdapter", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (resourceAdapter!= null) {
            writer.writeStartElement(prefix, "resource-adapter-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(resourceAdapter);
            writer.writeEndElement();
        }

        // ELEMENT: destinationName
        String destinationNameRaw = JMSDestination.destinationName;
        String destinationName = null;
        try {
            destinationName = Adapters.collapsedStringAdapterAdapter.marshal(destinationNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(JMSDestination, "destinationName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (destinationName!= null) {
            writer.writeStartElement(prefix, "destination-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(destinationName);
            writer.writeEndElement();
        }

        // ELEMENT: property
        List<Property> property = JMSDestination.property;
        if (property!= null) {
            for (Property propertyItem: property) {
                writer.writeStartElement(prefix, "property", "http://java.sun.com/xml/ns/javaee");
                if (propertyItem!= null) {
                    writeProperty(writer, propertyItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        context.afterMarshal(JMSDestination, LifecycleCallback.NONE);
    }

}
