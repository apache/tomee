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

import static org.apache.openejb.jee.ConcurrentLockType$JAXB.parseConcurrentLockType;
import static org.apache.openejb.jee.ConcurrentLockType$JAXB.toStringConcurrentLockType;
import static org.apache.openejb.jee.Method$JAXB.readMethod;
import static org.apache.openejb.jee.Method$JAXB.writeMethod;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class ContainerConcurrency$JAXB
        extends JAXBObject<ContainerConcurrency> {


    public ContainerConcurrency$JAXB() {
        super(ContainerConcurrency.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "container-concurrencyType".intern()), Text$JAXB.class, Method$JAXB.class, ConcurrentLockType$JAXB.class);
    }

    public static ContainerConcurrency readContainerConcurrency(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeContainerConcurrency(XoXMLStreamWriter writer, ContainerConcurrency containerConcurrency, RuntimeContext context)
            throws Exception {
        _write(writer, containerConcurrency, context);
    }

    public void write(XoXMLStreamWriter writer, ContainerConcurrency containerConcurrency, RuntimeContext context)
            throws Exception {
        _write(writer, containerConcurrency, context);
    }

    public final static ContainerConcurrency _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ContainerConcurrency containerConcurrency = new ContainerConcurrency();
        context.beforeUnmarshal(containerConcurrency, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        List<Method> method = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("container-concurrencyType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ContainerConcurrency.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, containerConcurrency);
                containerConcurrency.id = id;
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
            } else if (("method" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: method
                Method methodItem = readMethod(elementReader, context);
                if (method == null) {
                    method = containerConcurrency.method;
                    if (method != null) {
                        method.clear();
                    } else {
                        method = new ArrayList<Method>();
                    }
                }
                method.add(methodItem);
            } else if (("concurrency-attribute" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: lock
                ConcurrentLockType lock = parseConcurrentLockType(elementReader, context, elementReader.getElementAsString());
                if (lock != null) {
                    containerConcurrency.lock = lock;
                }
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "method"), new QName("http://java.sun.com/xml/ns/javaee", "concurrency-attribute"));
            }
        }
        if (descriptions != null) {
            try {
                containerConcurrency.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, ContainerConcurrency.class, "setDescriptions", Text[].class, e);
            }
        }
        if (method != null) {
            containerConcurrency.method = method;
        }

        context.afterUnmarshal(containerConcurrency, LifecycleCallback.NONE);

        return containerConcurrency;
    }

    public final ContainerConcurrency read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, ContainerConcurrency containerConcurrency, RuntimeContext context)
            throws Exception {
        if (containerConcurrency == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ContainerConcurrency.class != containerConcurrency.getClass()) {
            context.unexpectedSubclass(writer, containerConcurrency, ContainerConcurrency.class);
            return;
        }

        context.beforeMarshal(containerConcurrency, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = containerConcurrency.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(containerConcurrency, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = containerConcurrency.getDescriptions();
        } catch (Exception e) {
            context.getterError(containerConcurrency, "descriptions", ContainerConcurrency.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(containerConcurrency, "descriptions");
                }
            }
        }

        // ELEMENT: method
        List<Method> method = containerConcurrency.method;
        if (method != null) {
            for (Method methodItem : method) {
                if (methodItem != null) {
                    writer.writeStartElement(prefix, "method", "http://java.sun.com/xml/ns/javaee");
                    writeMethod(writer, methodItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(containerConcurrency, "method");
                }
            }
        }

        // ELEMENT: lock
        ConcurrentLockType lock = containerConcurrency.lock;
        if (lock != null) {
            writer.writeStartElement(prefix, "concurrency-attribute", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringConcurrentLockType(containerConcurrency, null, context, lock));
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(containerConcurrency, "lock");
        }

        context.afterMarshal(containerConcurrency, LifecycleCallback.NONE);
    }

}
