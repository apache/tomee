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
import org.apache.openejb.jee.jba.JndiName;
import org.apache.openejb.jee.jba.JndiName$JAXB;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.Description$JAXB.readDescription;
import static org.apache.openejb.jee.Description$JAXB.writeDescription;
import static org.apache.openejb.jee.Property$JAXB.readProperty;
import static org.apache.openejb.jee.Property$JAXB.writeProperty;
import static org.apache.openejb.jee.jba.JndiName$JAXB.readJndiName;
import static org.apache.openejb.jee.jba.JndiName$JAXB.writeJndiName;

@SuppressWarnings({
    "StringEquality"
})
public class ManagedScheduledExecutor$JAXB
    extends JAXBObject<ManagedScheduledExecutor>
{


    public ManagedScheduledExecutor$JAXB() {
        super(ManagedScheduledExecutor.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "managed-scheduled-executorType".intern()), Description$JAXB.class, JndiName$JAXB.class, Property$JAXB.class);
    }

    public static ManagedScheduledExecutor readManagedScheduledExecutor(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeManagedScheduledExecutor(XoXMLStreamWriter writer, ManagedScheduledExecutor managedScheduledExecutor, RuntimeContext context)
        throws Exception
    {
        _write(writer, managedScheduledExecutor, context);
    }

    public void write(XoXMLStreamWriter writer, ManagedScheduledExecutor managedScheduledExecutor, RuntimeContext context)
        throws Exception
    {
        _write(writer, managedScheduledExecutor, context);
    }

    public static final ManagedScheduledExecutor _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ManagedScheduledExecutor managedScheduledExecutor = new ManagedScheduledExecutor();
        context.beforeUnmarshal(managedScheduledExecutor, LifecycleCallback.NONE);

        List<Property> properties = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("managed-scheduled-executorType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ManagedScheduledExecutor.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute);
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("description" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: description
                Description description = readDescription(elementReader, context);
                managedScheduledExecutor.description = description;
            } else if (("name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: name
                JndiName name = readJndiName(elementReader, context);
                managedScheduledExecutor.name = name;
            } else if (("context-service-ref" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: contextService
                JndiName contextService = readJndiName(elementReader, context);
                managedScheduledExecutor.contextService = contextService;
            } else if (("hung-task-threshold" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: hungTaskThreshold
                Long hungTaskThreshold = Long.valueOf(elementReader.getElementText());
                managedScheduledExecutor.hungTaskThreshold = hungTaskThreshold;
            } else if (("max-async" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: maxAsync
                Integer maxAsync = Integer.valueOf(elementReader.getElementText());
                managedScheduledExecutor.maxAsync = maxAsync;
            } else if (("property" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: properties
                Property propertiesItem = readProperty(elementReader, context);
                if (properties == null) {
                    properties = managedScheduledExecutor.properties;
                    if (properties!= null) {
                        properties.clear();
                    } else {
                        properties = new ArrayList<>();
                    }
                }
                properties.add(propertiesItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "name"), new QName("http://java.sun.com/xml/ns/javaee", "context-service-ref"), new QName("http://java.sun.com/xml/ns/javaee", "hung-task-threshold"), new QName("http://java.sun.com/xml/ns/javaee", "max-async"), new QName("http://java.sun.com/xml/ns/javaee", "property"));
            }
        }
        if (properties!= null) {
            managedScheduledExecutor.properties = properties;
        }

        context.afterUnmarshal(managedScheduledExecutor, LifecycleCallback.NONE);

        return managedScheduledExecutor;
    }

    public final ManagedScheduledExecutor read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, ManagedScheduledExecutor managedScheduledExecutor, RuntimeContext context)
        throws Exception
    {
        if (managedScheduledExecutor == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ManagedScheduledExecutor.class!= managedScheduledExecutor.getClass()) {
            context.unexpectedSubclass(writer, managedScheduledExecutor, ManagedScheduledExecutor.class);
            return ;
        }

        context.beforeMarshal(managedScheduledExecutor, LifecycleCallback.NONE);


        // ELEMENT: description
        Description description = managedScheduledExecutor.description;
        if (description!= null) {
            writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
            writeDescription(writer, description, context);
            writer.writeEndElement();
        }

        // ELEMENT: name
        JndiName name = managedScheduledExecutor.name;
        if (name!= null) {
            writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
            writeJndiName(writer, name, context);
            writer.writeEndElement();
        }

        // ELEMENT: contextService
        JndiName contextService = managedScheduledExecutor.contextService;
        if (contextService!= null) {
            writer.writeStartElement(prefix, "context-service-ref", "http://java.sun.com/xml/ns/javaee");
            writeJndiName(writer, contextService, context);
            writer.writeEndElement();
        }

        // ELEMENT: hungTaskThreshold
        Long hungTaskThreshold = managedScheduledExecutor.hungTaskThreshold;
        if (hungTaskThreshold!= null) {
            writer.writeStartElement(prefix, "hung-task-threshold", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Long.toString(hungTaskThreshold));
            writer.writeEndElement();
        }

        // ELEMENT: maxAsync
        Integer maxAsync = managedScheduledExecutor.maxAsync;
        if (maxAsync!= null) {
            writer.writeStartElement(prefix, "max-async", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Integer.toString(maxAsync));
            writer.writeEndElement();
        }

        // ELEMENT: properties
        List<Property> properties = managedScheduledExecutor.properties;
        if (properties!= null) {
            for (Property propertiesItem: properties) {
                if (propertiesItem!= null) {
                    writer.writeStartElement(prefix, "property", "http://java.sun.com/xml/ns/javaee");
                    writeProperty(writer, propertiesItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(managedScheduledExecutor, LifecycleCallback.NONE);
    }

}
