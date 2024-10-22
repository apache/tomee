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
public class JMSConnectionFactory$JAXB
    extends JAXBObject<org.apache.openejb.jee.JMSConnectionFactory>
{


    public JMSConnectionFactory$JAXB() {
        super(org.apache.openejb.jee.JMSConnectionFactory.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "jms-connection-factoryType".intern()), Text$JAXB.class, Property$JAXB.class);
    }

    public static org.apache.openejb.jee.JMSConnectionFactory readJMSConnectionFactory(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeJMSConnectionFactory(XoXMLStreamWriter writer, org.apache.openejb.jee.JMSConnectionFactory JMSConnectionFactory, RuntimeContext context)
        throws Exception
    {
        _write(writer, JMSConnectionFactory, context);
    }

    public void write(XoXMLStreamWriter writer, org.apache.openejb.jee.JMSConnectionFactory JMSConnectionFactory, RuntimeContext context)
        throws Exception
    {
        _write(writer, JMSConnectionFactory, context);
    }

    public static final org.apache.openejb.jee.JMSConnectionFactory _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        org.apache.openejb.jee.JMSConnectionFactory JMSConnectionFactory = new org.apache.openejb.jee.JMSConnectionFactory();
        context.beforeUnmarshal(JMSConnectionFactory, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        List<Property> property = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("jms-connection-factoryType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, org.apache.openejb.jee.JMSConnectionFactory.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, JMSConnectionFactory);
                JMSConnectionFactory.id = id;
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

                JMSConnectionFactory.name = name;
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

                JMSConnectionFactory.className = className;
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

                JMSConnectionFactory.interfaceName = interfaceName;
            } else if (("resource-adapter" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceAdapter
                String resourceAdapterRaw = elementReader.getElementText();

                String resourceAdapter;
                try {
                    resourceAdapter = Adapters.collapsedStringAdapterAdapter.unmarshal(resourceAdapterRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                JMSConnectionFactory.resourceAdapter = resourceAdapter;
            } else if (("user" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: user
                String userRaw = elementReader.getElementText();

                String user;
                try {
                    user = Adapters.collapsedStringAdapterAdapter.unmarshal(userRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                JMSConnectionFactory.user = user;
            } else if (("password" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: password
                String passwordRaw = elementReader.getElementText();

                String password;
                try {
                    password = Adapters.collapsedStringAdapterAdapter.unmarshal(passwordRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                JMSConnectionFactory.password = password;
            } else if (("clientId" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: clientId
                String clientIdRaw = elementReader.getElementText();

                String clientId;
                try {
                    clientId = Adapters.collapsedStringAdapterAdapter.unmarshal(clientIdRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                JMSConnectionFactory.clientId = clientId;
            } else if (("transactional" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: transactional
                Boolean transactional = ("1".equals(elementReader.getElementText())||"true".equals(elementReader.getElementText()));
                JMSConnectionFactory.transactional = transactional;
            } else if (("max-pool-size" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: maxPoolSize
                Integer maxPoolSize = Integer.valueOf(elementReader.getElementText());
                JMSConnectionFactory.maxPoolSize = maxPoolSize;
            } else if (("min-pool-size" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: minPoolSize
                Integer minPoolSize = Integer.valueOf(elementReader.getElementText());
                JMSConnectionFactory.minPoolSize = minPoolSize;
            } else if (("property" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: property
                Property propertyItem = readProperty(elementReader, context);
                if (property == null) {
                    property = JMSConnectionFactory.property;
                    if (property!= null) {
                        property.clear();
                    } else {
                        property = new ArrayList<>();
                    }
                }
                property.add(propertyItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "name"), new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "class-name"), new QName("http://java.sun.com/xml/ns/javaee", "interface-name"), new QName("http://java.sun.com/xml/ns/javaee", "resource-adapter"), new QName("http://java.sun.com/xml/ns/javaee", "user"), new QName("http://java.sun.com/xml/ns/javaee", "password"), new QName("http://java.sun.com/xml/ns/javaee", "clientId"), new QName("http://java.sun.com/xml/ns/javaee", "transactional"), new QName("http://java.sun.com/xml/ns/javaee", "max-pool-size"), new QName("http://java.sun.com/xml/ns/javaee", "min-pool-size"), new QName("http://java.sun.com/xml/ns/javaee", "property"));
            }
        }
        if (property!= null) {
            JMSConnectionFactory.property = property;
        }

        context.afterUnmarshal(JMSConnectionFactory, LifecycleCallback.NONE);

        return JMSConnectionFactory;
    }

    public final org.apache.openejb.jee.JMSConnectionFactory read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, org.apache.openejb.jee.JMSConnectionFactory JMSConnectionFactory, RuntimeContext context)
        throws Exception
    {
        if (JMSConnectionFactory == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (org.apache.openejb.jee.JMSConnectionFactory.class!= JMSConnectionFactory.getClass()) {
            context.unexpectedSubclass(writer, JMSConnectionFactory, org.apache.openejb.jee.JMSConnectionFactory.class);
            return ;
        }

        context.beforeMarshal(JMSConnectionFactory, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = JMSConnectionFactory.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(JMSConnectionFactory, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: name
        String nameRaw = JMSConnectionFactory.name;
        String name = null;
        try {
            name = Adapters.collapsedStringAdapterAdapter.marshal(nameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(JMSConnectionFactory, "name", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (name!= null) {
            writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(name);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(JMSConnectionFactory, "name");
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = JMSConnectionFactory.getDescriptions();
        } catch (Exception e) {
            context.getterError(JMSConnectionFactory, "descriptions", org.apache.openejb.jee.JMSConnectionFactory.class, "getDescriptions", e);
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
        String classNameRaw = JMSConnectionFactory.className;
        String className = null;
        try {
            className = Adapters.collapsedStringAdapterAdapter.marshal(classNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(JMSConnectionFactory, "className", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (className!= null) {
            writer.writeStartElement(prefix, "class-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(className);
            writer.writeEndElement();
        }

        // ELEMENT: interfaceName
        String interfaceNameRaw = JMSConnectionFactory.interfaceName;
        String interfaceName = null;
        try {
            interfaceName = Adapters.collapsedStringAdapterAdapter.marshal(interfaceNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(JMSConnectionFactory, "interfaceName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (interfaceName!= null) {
            writer.writeStartElement(prefix, "interface-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(interfaceName);
            writer.writeEndElement();
        }

        // ELEMENT: resourceAdapter
        String resourceAdapterRaw = JMSConnectionFactory.resourceAdapter;
        String resourceAdapter = null;
        try {
            resourceAdapter = Adapters.collapsedStringAdapterAdapter.marshal(resourceAdapterRaw);
        } catch (Exception e) {
            context.xmlAdapterError(JMSConnectionFactory, "resourceAdapter", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (resourceAdapter!= null) {
            writer.writeStartElement(prefix, "resource-adapter", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(resourceAdapter);
            writer.writeEndElement();
        }

        // ELEMENT: user
        String userRaw = JMSConnectionFactory.user;
        String user = null;
        try {
            user = Adapters.collapsedStringAdapterAdapter.marshal(userRaw);
        } catch (Exception e) {
            context.xmlAdapterError(JMSConnectionFactory, "user", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (user!= null) {
            writer.writeStartElement(prefix, "user", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(user);
            writer.writeEndElement();
        }

        // ELEMENT: password
        String passwordRaw = JMSConnectionFactory.password;
        String password = null;
        try {
            password = Adapters.collapsedStringAdapterAdapter.marshal(passwordRaw);
        } catch (Exception e) {
            context.xmlAdapterError(JMSConnectionFactory, "password", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (password!= null) {
            writer.writeStartElement(prefix, "password", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(password);
            writer.writeEndElement();
        }

        // ELEMENT: clientId
        String clientIdRaw = JMSConnectionFactory.clientId;
        String clientId = null;
        try {
            clientId = Adapters.collapsedStringAdapterAdapter.marshal(clientIdRaw);
        } catch (Exception e) {
            context.xmlAdapterError(JMSConnectionFactory, "clientId", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (clientId!= null) {
            writer.writeStartElement(prefix, "clientId", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(clientId);
            writer.writeEndElement();
        }

        // ELEMENT: transactional
        Boolean transactional = JMSConnectionFactory.transactional;
        writer.writeStartElement(prefix, "transactional", "http://java.sun.com/xml/ns/javaee");
        writer.writeCharacters(Boolean.toString(transactional));
        writer.writeEndElement();

        // ELEMENT: maxPoolSize
        Integer maxPoolSize = JMSConnectionFactory.maxPoolSize;
        if (maxPoolSize!= null) {
            writer.writeStartElement(prefix, "max-pool-size", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Integer.toString(maxPoolSize));
            writer.writeEndElement();
        }

        // ELEMENT: minPoolSize
        Integer minPoolSize = JMSConnectionFactory.minPoolSize;
        if (minPoolSize!= null) {
            writer.writeStartElement(prefix, "min-pool-size", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Integer.toString(minPoolSize));
            writer.writeEndElement();
        }

        // ELEMENT: property
        List<Property> property = JMSConnectionFactory.property;
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

        context.afterMarshal(JMSConnectionFactory, LifecycleCallback.NONE);
    }

}
