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

import static org.apache.openejb.jee.ConfigProperty$JAXB.readConfigProperty;
import static org.apache.openejb.jee.ConfigProperty$JAXB.writeConfigProperty;

@SuppressWarnings({
    "StringEquality"
})
public class ConnectionDefinition$JAXB
    extends JAXBObject<ConnectionDefinition> {


    public ConnectionDefinition$JAXB() {
        super(ConnectionDefinition.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "connection-definitionType".intern()), ConfigProperty$JAXB.class);
    }

    public static ConnectionDefinition readConnectionDefinition(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeConnectionDefinition(final XoXMLStreamWriter writer, final ConnectionDefinition connectionDefinition, final RuntimeContext context)
        throws Exception {
        _write(writer, connectionDefinition, context);
    }

    public void write(final XoXMLStreamWriter writer, final ConnectionDefinition connectionDefinition, final RuntimeContext context)
        throws Exception {
        _write(writer, connectionDefinition, context);
    }

    public final static ConnectionDefinition _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final ConnectionDefinition connectionDefinition = new ConnectionDefinition();
        context.beforeUnmarshal(connectionDefinition, LifecycleCallback.NONE);

        List<ConfigProperty> configProperty = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("connection-definitionType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ConnectionDefinition.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, connectionDefinition);
                connectionDefinition.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("managedconnectionfactory-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: managedConnectionFactoryClass
                final String managedConnectionFactoryClassRaw = elementReader.getElementAsString();

                final String managedConnectionFactoryClass;
                try {
                    managedConnectionFactoryClass = Adapters.collapsedStringAdapterAdapter.unmarshal(managedConnectionFactoryClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                connectionDefinition.managedConnectionFactoryClass = managedConnectionFactoryClass;
            } else if (("config-property" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: configProperty
                final ConfigProperty configPropertyItem = readConfigProperty(elementReader, context);
                if (configProperty == null) {
                    configProperty = connectionDefinition.configProperty;
                    if (configProperty != null) {
                        configProperty.clear();
                    } else {
                        configProperty = new ArrayList<ConfigProperty>();
                    }
                }
                configProperty.add(configPropertyItem);
            } else if (("connectionfactory-interface" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: connectionFactoryInterface
                final String connectionFactoryInterfaceRaw = elementReader.getElementAsString();

                final String connectionFactoryInterface;
                try {
                    connectionFactoryInterface = Adapters.collapsedStringAdapterAdapter.unmarshal(connectionFactoryInterfaceRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                connectionDefinition.connectionFactoryInterface = connectionFactoryInterface;
            } else if (("connectionfactory-impl-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: connectionFactoryImplClass
                final String connectionFactoryImplClassRaw = elementReader.getElementAsString();

                final String connectionFactoryImplClass;
                try {
                    connectionFactoryImplClass = Adapters.collapsedStringAdapterAdapter.unmarshal(connectionFactoryImplClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                connectionDefinition.connectionFactoryImplClass = connectionFactoryImplClass;
            } else if (("connection-interface" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: connectionInterface
                final String connectionInterfaceRaw = elementReader.getElementAsString();

                final String connectionInterface;
                try {
                    connectionInterface = Adapters.collapsedStringAdapterAdapter.unmarshal(connectionInterfaceRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                connectionDefinition.connectionInterface = connectionInterface;
            } else if (("connection-impl-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: connectionImplClass
                final String connectionImplClassRaw = elementReader.getElementAsString();

                final String connectionImplClass;
                try {
                    connectionImplClass = Adapters.collapsedStringAdapterAdapter.unmarshal(connectionImplClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                connectionDefinition.connectionImplClass = connectionImplClass;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "managedconnectionfactory-class"), new QName("http://java.sun.com/xml/ns/javaee", "config-property"), new QName("http://java.sun.com/xml/ns/javaee", "connectionfactory-interface"), new QName("http://java.sun.com/xml/ns/javaee", "connectionfactory-impl-class"), new QName("http://java.sun.com/xml/ns/javaee", "connection-interface"), new QName("http://java.sun.com/xml/ns/javaee", "connection-impl-class"));
            }
        }
        if (configProperty != null) {
            connectionDefinition.configProperty = configProperty;
        }

        context.afterUnmarshal(connectionDefinition, LifecycleCallback.NONE);

        return connectionDefinition;
    }

    public final ConnectionDefinition read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final ConnectionDefinition connectionDefinition, RuntimeContext context)
        throws Exception {
        if (connectionDefinition == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ConnectionDefinition.class != connectionDefinition.getClass()) {
            context.unexpectedSubclass(writer, connectionDefinition, ConnectionDefinition.class);
            return;
        }

        context.beforeMarshal(connectionDefinition, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = connectionDefinition.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(connectionDefinition, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: managedConnectionFactoryClass
        final String managedConnectionFactoryClassRaw = connectionDefinition.managedConnectionFactoryClass;
        String managedConnectionFactoryClass = null;
        try {
            managedConnectionFactoryClass = Adapters.collapsedStringAdapterAdapter.marshal(managedConnectionFactoryClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(connectionDefinition, "managedConnectionFactoryClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (managedConnectionFactoryClass != null) {
            writer.writeStartElement(prefix, "managedconnectionfactory-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(managedConnectionFactoryClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(connectionDefinition, "managedConnectionFactoryClass");
        }

        // ELEMENT: configProperty
        final List<ConfigProperty> configProperty = connectionDefinition.configProperty;
        if (configProperty != null) {
            for (final ConfigProperty configPropertyItem : configProperty) {
                if (configPropertyItem != null) {
                    writer.writeStartElement(prefix, "config-property", "http://java.sun.com/xml/ns/javaee");
                    writeConfigProperty(writer, configPropertyItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: connectionFactoryInterface
        final String connectionFactoryInterfaceRaw = connectionDefinition.connectionFactoryInterface;
        String connectionFactoryInterface = null;
        try {
            connectionFactoryInterface = Adapters.collapsedStringAdapterAdapter.marshal(connectionFactoryInterfaceRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(connectionDefinition, "connectionFactoryInterface", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (connectionFactoryInterface != null) {
            writer.writeStartElement(prefix, "connectionfactory-interface", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(connectionFactoryInterface);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(connectionDefinition, "connectionFactoryInterface");
        }

        // ELEMENT: connectionFactoryImplClass
        final String connectionFactoryImplClassRaw = connectionDefinition.connectionFactoryImplClass;
        String connectionFactoryImplClass = null;
        try {
            connectionFactoryImplClass = Adapters.collapsedStringAdapterAdapter.marshal(connectionFactoryImplClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(connectionDefinition, "connectionFactoryImplClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (connectionFactoryImplClass != null) {
            writer.writeStartElement(prefix, "connectionfactory-impl-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(connectionFactoryImplClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(connectionDefinition, "connectionFactoryImplClass");
        }

        // ELEMENT: connectionInterface
        final String connectionInterfaceRaw = connectionDefinition.connectionInterface;
        String connectionInterface = null;
        try {
            connectionInterface = Adapters.collapsedStringAdapterAdapter.marshal(connectionInterfaceRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(connectionDefinition, "connectionInterface", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (connectionInterface != null) {
            writer.writeStartElement(prefix, "connection-interface", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(connectionInterface);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(connectionDefinition, "connectionInterface");
        }

        // ELEMENT: connectionImplClass
        final String connectionImplClassRaw = connectionDefinition.connectionImplClass;
        String connectionImplClass = null;
        try {
            connectionImplClass = Adapters.collapsedStringAdapterAdapter.marshal(connectionImplClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(connectionDefinition, "connectionImplClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (connectionImplClass != null) {
            writer.writeStartElement(prefix, "connection-impl-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(connectionImplClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(connectionDefinition, "connectionImplClass");
        }

        context.afterMarshal(connectionDefinition, LifecycleCallback.NONE);
    }

}
