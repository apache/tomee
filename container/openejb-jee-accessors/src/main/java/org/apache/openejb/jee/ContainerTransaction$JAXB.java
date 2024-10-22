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


import static org.apache.openejb.jee.Method$JAXB.readMethod;
import static org.apache.openejb.jee.Method$JAXB.writeMethod;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;
import static org.apache.openejb.jee.TransAttribute$JAXB.parseTransAttribute;
import static org.apache.openejb.jee.TransAttribute$JAXB.toStringTransAttribute;

@SuppressWarnings({
    "StringEquality"
})
public class ContainerTransaction$JAXB
    extends JAXBObject<ContainerTransaction>
{


    public ContainerTransaction$JAXB() {
        super(ContainerTransaction.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "container-transactionType".intern()), Text$JAXB.class, Method$JAXB.class, TransAttribute$JAXB.class);
    }

    public static ContainerTransaction readContainerTransaction(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeContainerTransaction(XoXMLStreamWriter writer, ContainerTransaction containerTransaction, RuntimeContext context)
        throws Exception
    {
        _write(writer, containerTransaction, context);
    }

    public void write(XoXMLStreamWriter writer, ContainerTransaction containerTransaction, RuntimeContext context)
        throws Exception
    {
        _write(writer, containerTransaction, context);
    }

    public static final ContainerTransaction _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ContainerTransaction containerTransaction = new ContainerTransaction();
        context.beforeUnmarshal(containerTransaction, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        List<Method> method = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("container-transactionType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ContainerTransaction.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, containerTransaction);
                containerTransaction.id = id;
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
            } else if (("method" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: method
                Method methodItem = readMethod(elementReader, context);
                if (method == null) {
                    method = containerTransaction.method;
                    if (method!= null) {
                        method.clear();
                    } else {
                        method = new ArrayList<>();
                    }
                }
                method.add(methodItem);
            } else if (("trans-attribute" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: transAttribute
                TransAttribute transAttribute = parseTransAttribute(elementReader, context, elementReader.getElementText());
                if (transAttribute!= null) {
                    containerTransaction.transAttribute = transAttribute;
                }
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "method"), new QName("http://java.sun.com/xml/ns/javaee", "trans-attribute"));
            }
        }
        if (descriptions!= null) {
            try {
                containerTransaction.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, ContainerTransaction.class, "setDescriptions", Text[].class, e);
            }
        }
        if (method!= null) {
            containerTransaction.method = method;
        }

        context.afterUnmarshal(containerTransaction, LifecycleCallback.NONE);

        return containerTransaction;
    }

    public final ContainerTransaction read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, ContainerTransaction containerTransaction, RuntimeContext context)
        throws Exception
    {
        if (containerTransaction == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ContainerTransaction.class!= containerTransaction.getClass()) {
            context.unexpectedSubclass(writer, containerTransaction, ContainerTransaction.class);
            return ;
        }

        context.beforeMarshal(containerTransaction, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = containerTransaction.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(containerTransaction, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = containerTransaction.getDescriptions();
        } catch (Exception e) {
            context.getterError(containerTransaction, "descriptions", ContainerTransaction.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(containerTransaction, "descriptions");
                }
            }
        }

        // ELEMENT: method
        List<Method> method = containerTransaction.method;
        if (method!= null) {
            for (Method methodItem: method) {
                if (methodItem!= null) {
                    writer.writeStartElement(prefix, "method", "http://java.sun.com/xml/ns/javaee");
                    writeMethod(writer, methodItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(containerTransaction, "method");
                }
            }
        }

        // ELEMENT: transAttribute
        TransAttribute transAttribute = containerTransaction.transAttribute;
        if (transAttribute!= null) {
            writer.writeStartElement(prefix, "trans-attribute", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringTransAttribute(containerTransaction, null, context, transAttribute));
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(containerTransaction, "transAttribute");
        }

        context.afterMarshal(containerTransaction, LifecycleCallback.NONE);
    }

}
