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

import static org.apache.openejb.jee.IsolationLevel$JAXB.parseIsolationLevel;
import static org.apache.openejb.jee.IsolationLevel$JAXB.toStringIsolationLevel;
import static org.apache.openejb.jee.Property$JAXB.readProperty;
import static org.apache.openejb.jee.Property$JAXB.writeProperty;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class DataSource$JAXB
        extends JAXBObject<DataSource> {


    public DataSource$JAXB() {
        super(DataSource.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "data-sourceType".intern()), Text$JAXB.class, Property$JAXB.class, IsolationLevel$JAXB.class);
    }

    public static DataSource readDataSource(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeDataSource(XoXMLStreamWriter writer, DataSource dataSource, RuntimeContext context)
            throws Exception {
        _write(writer, dataSource, context);
    }

    public void write(XoXMLStreamWriter writer, DataSource dataSource, RuntimeContext context)
            throws Exception {
        _write(writer, dataSource, context);
    }

    public final static DataSource _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        DataSource dataSource = new DataSource();
        context.beforeUnmarshal(dataSource, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        List<Property> property = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("data-sourceType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, DataSource.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, dataSource);
                dataSource.id = id;
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
            } else if (("name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: name
                String nameRaw = elementReader.getElementAsString();

                String name;
                try {
                    name = Adapters.collapsedStringAdapterAdapter.unmarshal(nameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                dataSource.name = name;
            } else if (("class-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: className
                String classNameRaw = elementReader.getElementAsString();

                String className;
                try {
                    className = Adapters.collapsedStringAdapterAdapter.unmarshal(classNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                dataSource.className = className;
            } else if (("server-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serverName
                String serverNameRaw = elementReader.getElementAsString();

                String serverName;
                try {
                    serverName = Adapters.collapsedStringAdapterAdapter.unmarshal(serverNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                dataSource.serverName = serverName;
            } else if (("port-number" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: portNumber
                Integer portNumber = Integer.valueOf(elementReader.getElementAsString());
                dataSource.portNumber = portNumber;
            } else if (("database-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: databaseName
                String databaseNameRaw = elementReader.getElementAsString();

                String databaseName;
                try {
                    databaseName = Adapters.collapsedStringAdapterAdapter.unmarshal(databaseNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                dataSource.databaseName = databaseName;
            } else if (("url" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: url
                String urlRaw = elementReader.getElementAsString();

                String url;
                try {
                    url = Adapters.collapsedStringAdapterAdapter.unmarshal(urlRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                dataSource.url = url;
            } else if (("user" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: user
                String userRaw = elementReader.getElementAsString();

                String user;
                try {
                    user = Adapters.collapsedStringAdapterAdapter.unmarshal(userRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                dataSource.user = user;
            } else if (("password" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: password
                String passwordRaw = elementReader.getElementAsString();

                String password;
                try {
                    password = Adapters.collapsedStringAdapterAdapter.unmarshal(passwordRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                dataSource.password = password;
            } else if (("property" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: property
                Property propertyItem = readProperty(elementReader, context);
                if (property == null) {
                    property = dataSource.property;
                    if (property != null) {
                        property.clear();
                    } else {
                        property = new ArrayList<Property>();
                    }
                }
                property.add(propertyItem);
            } else if (("login-timeout" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: loginTimeout
                Integer loginTimeout = Integer.valueOf(elementReader.getElementAsString());
                dataSource.loginTimeout = loginTimeout;
            } else if (("transactional" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: transactional
                Boolean transactional = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                dataSource.transactional = transactional;
            } else if (("isolation-level" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: isolationLevel
                IsolationLevel isolationLevel = parseIsolationLevel(elementReader, context, elementReader.getElementAsString());
                if (isolationLevel != null) {
                    dataSource.isolationLevel = isolationLevel;
                }
            } else if (("initial-pool-size" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: initialPoolSize
                Integer initialPoolSize = Integer.valueOf(elementReader.getElementAsString());
                dataSource.initialPoolSize = initialPoolSize;
            } else if (("max-pool-size" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: maxPoolSize
                Integer maxPoolSize = Integer.valueOf(elementReader.getElementAsString());
                dataSource.maxPoolSize = maxPoolSize;
            } else if (("min-pool-size" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: minPoolSize
                Integer minPoolSize = Integer.valueOf(elementReader.getElementAsString());
                dataSource.minPoolSize = minPoolSize;
            } else if (("max-idle-time" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: maxIdleTime
                Integer maxIdleTime = Integer.valueOf(elementReader.getElementAsString());
                dataSource.maxIdleTime = maxIdleTime;
            } else if (("max-statements" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: maxStatements
                Integer maxStatements = Integer.valueOf(elementReader.getElementAsString());
                dataSource.maxStatements = maxStatements;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "name"), new QName("http://java.sun.com/xml/ns/javaee", "class-name"), new QName("http://java.sun.com/xml/ns/javaee", "server-name"), new QName("http://java.sun.com/xml/ns/javaee", "port-number"), new QName("http://java.sun.com/xml/ns/javaee", "database-name"), new QName("http://java.sun.com/xml/ns/javaee", "url"), new QName("http://java.sun.com/xml/ns/javaee", "user"), new QName("http://java.sun.com/xml/ns/javaee", "password"), new QName("http://java.sun.com/xml/ns/javaee", "property"), new QName("http://java.sun.com/xml/ns/javaee", "login-timeout"), new QName("http://java.sun.com/xml/ns/javaee", "transactional"), new QName("http://java.sun.com/xml/ns/javaee", "isolation-level"), new QName("http://java.sun.com/xml/ns/javaee", "initial-pool-size"), new QName("http://java.sun.com/xml/ns/javaee", "max-pool-size"), new QName("http://java.sun.com/xml/ns/javaee", "min-pool-size"), new QName("http://java.sun.com/xml/ns/javaee", "max-idle-time"), new QName("http://java.sun.com/xml/ns/javaee", "max-statements"));
            }
        }
        if (descriptions != null) {
            try {
                dataSource.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, DataSource.class, "setDescriptions", Text[].class, e);
            }
        }
        if (property != null) {
            dataSource.property = property;
        }

        context.afterUnmarshal(dataSource, LifecycleCallback.NONE);

        return dataSource;
    }

    public final DataSource read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, DataSource dataSource, RuntimeContext context)
            throws Exception {
        if (dataSource == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (DataSource.class != dataSource.getClass()) {
            context.unexpectedSubclass(writer, dataSource, DataSource.class);
            return;
        }

        context.beforeMarshal(dataSource, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = dataSource.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(dataSource, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = dataSource.getDescriptions();
        } catch (Exception e) {
            context.getterError(dataSource, "descriptions", DataSource.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: name
        String nameRaw = dataSource.name;
        String name = null;
        try {
            name = Adapters.collapsedStringAdapterAdapter.marshal(nameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(dataSource, "name", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (name != null) {
            writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(name);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(dataSource, "name");
        }

        // ELEMENT: className
        String classNameRaw = dataSource.className;
        String className = null;
        try {
            className = Adapters.collapsedStringAdapterAdapter.marshal(classNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(dataSource, "className", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (className != null) {
            writer.writeStartElement(prefix, "class-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(className);
            writer.writeEndElement();
        }

        // ELEMENT: serverName
        String serverNameRaw = dataSource.serverName;
        String serverName = null;
        try {
            serverName = Adapters.collapsedStringAdapterAdapter.marshal(serverNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(dataSource, "serverName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (serverName != null) {
            writer.writeStartElement(prefix, "server-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(serverName);
            writer.writeEndElement();
        }

        // ELEMENT: portNumber
        Integer portNumber = dataSource.portNumber;
        if (portNumber != null) {
            writer.writeStartElement(prefix, "port-number", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Integer.toString(portNumber));
            writer.writeEndElement();
        }

        // ELEMENT: databaseName
        String databaseNameRaw = dataSource.databaseName;
        String databaseName = null;
        try {
            databaseName = Adapters.collapsedStringAdapterAdapter.marshal(databaseNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(dataSource, "databaseName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (databaseName != null) {
            writer.writeStartElement(prefix, "database-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(databaseName);
            writer.writeEndElement();
        }

        // ELEMENT: url
        String urlRaw = dataSource.url;
        String url = null;
        try {
            url = Adapters.collapsedStringAdapterAdapter.marshal(urlRaw);
        } catch (Exception e) {
            context.xmlAdapterError(dataSource, "url", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (url != null) {
            writer.writeStartElement(prefix, "url", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(url);
            writer.writeEndElement();
        }

        // ELEMENT: user
        String userRaw = dataSource.user;
        String user = null;
        try {
            user = Adapters.collapsedStringAdapterAdapter.marshal(userRaw);
        } catch (Exception e) {
            context.xmlAdapterError(dataSource, "user", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (user != null) {
            writer.writeStartElement(prefix, "user", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(user);
            writer.writeEndElement();
        }

        // ELEMENT: password
        String passwordRaw = dataSource.password;
        String password = null;
        try {
            password = Adapters.collapsedStringAdapterAdapter.marshal(passwordRaw);
        } catch (Exception e) {
            context.xmlAdapterError(dataSource, "password", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (password != null) {
            writer.writeStartElement(prefix, "password", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(password);
            writer.writeEndElement();
        }

        // ELEMENT: property
        List<Property> property = dataSource.property;
        if (property != null) {
            for (Property propertyItem : property) {
                writer.writeStartElement(prefix, "property", "http://java.sun.com/xml/ns/javaee");
                if (propertyItem != null) {
                    writeProperty(writer, propertyItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: loginTimeout
        Integer loginTimeout = dataSource.loginTimeout;
        if (loginTimeout != null) {
            writer.writeStartElement(prefix, "login-timeout", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Integer.toString(loginTimeout));
            writer.writeEndElement();
        }

        // ELEMENT: transactional
        Boolean transactional = dataSource.transactional;
        if (transactional != null) {
            writer.writeStartElement(prefix, "transactional", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(transactional));
            writer.writeEndElement();
        }

        // ELEMENT: isolationLevel
        IsolationLevel isolationLevel = dataSource.isolationLevel;
        if (isolationLevel != null) {
            writer.writeStartElement(prefix, "isolation-level", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringIsolationLevel(dataSource, null, context, isolationLevel));
            writer.writeEndElement();
        }

        // ELEMENT: initialPoolSize
        Integer initialPoolSize = dataSource.initialPoolSize;
        if (initialPoolSize != null) {
            writer.writeStartElement(prefix, "initial-pool-size", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Integer.toString(initialPoolSize));
            writer.writeEndElement();
        }

        // ELEMENT: maxPoolSize
        Integer maxPoolSize = dataSource.maxPoolSize;
        if (maxPoolSize != null) {
            writer.writeStartElement(prefix, "max-pool-size", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Integer.toString(maxPoolSize));
            writer.writeEndElement();
        }

        // ELEMENT: minPoolSize
        Integer minPoolSize = dataSource.minPoolSize;
        if (minPoolSize != null) {
            writer.writeStartElement(prefix, "min-pool-size", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Integer.toString(minPoolSize));
            writer.writeEndElement();
        }

        // ELEMENT: maxIdleTime
        Integer maxIdleTime = dataSource.maxIdleTime;
        if (maxIdleTime != null) {
            writer.writeStartElement(prefix, "max-idle-time", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Integer.toString(maxIdleTime));
            writer.writeEndElement();
        }

        // ELEMENT: maxStatements
        Integer maxStatements = dataSource.maxStatements;
        if (maxStatements != null) {
            writer.writeStartElement(prefix, "max-statements", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Integer.toString(maxStatements));
            writer.writeEndElement();
        }

        context.afterMarshal(dataSource, LifecycleCallback.NONE);
    }

}
