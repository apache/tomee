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

import static org.apache.openejb.jee.InterceptorOrder$JAXB.readInterceptorOrder;
import static org.apache.openejb.jee.InterceptorOrder$JAXB.writeInterceptorOrder;
import static org.apache.openejb.jee.NamedMethod$JAXB.readNamedMethod;
import static org.apache.openejb.jee.NamedMethod$JAXB.writeNamedMethod;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class InterceptorBinding$JAXB
        extends JAXBObject<InterceptorBinding> {


    public InterceptorBinding$JAXB() {
        super(InterceptorBinding.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "interceptor-bindingType".intern()), Text$JAXB.class, InterceptorOrder$JAXB.class, NamedMethod$JAXB.class);
    }

    public static InterceptorBinding readInterceptorBinding(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeInterceptorBinding(XoXMLStreamWriter writer, InterceptorBinding interceptorBinding, RuntimeContext context)
            throws Exception {
        _write(writer, interceptorBinding, context);
    }

    public void write(XoXMLStreamWriter writer, InterceptorBinding interceptorBinding, RuntimeContext context)
            throws Exception {
        _write(writer, interceptorBinding, context);
    }

    public final static InterceptorBinding _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        InterceptorBinding interceptorBinding = new InterceptorBinding();
        context.beforeUnmarshal(interceptorBinding, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        List<String> interceptorClass = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("interceptor-bindingType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, InterceptorBinding.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, interceptorBinding);
                interceptorBinding.id = id;
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
            } else if (("ejb-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbName
                String ejbNameRaw = elementReader.getElementAsString();

                String ejbName;
                try {
                    ejbName = Adapters.collapsedStringAdapterAdapter.unmarshal(ejbNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                interceptorBinding.ejbName = ejbName;
            } else if (("interceptor-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: interceptorClass
                String interceptorClassItemRaw = elementReader.getElementAsString();

                String interceptorClassItem;
                try {
                    interceptorClassItem = Adapters.collapsedStringAdapterAdapter.unmarshal(interceptorClassItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (interceptorClass == null) {
                    interceptorClass = interceptorBinding.interceptorClass;
                    if (interceptorClass != null) {
                        interceptorClass.clear();
                    } else {
                        interceptorClass = new ArrayList<String>();
                    }
                }
                interceptorClass.add(interceptorClassItem);
            } else if (("interceptor-order" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: interceptorOrder
                InterceptorOrder interceptorOrder = readInterceptorOrder(elementReader, context);
                interceptorBinding.interceptorOrder = interceptorOrder;
            } else if (("exclude-default-interceptors" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: excludeDefaultInterceptors
                Boolean excludeDefaultInterceptors = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                interceptorBinding.excludeDefaultInterceptors = excludeDefaultInterceptors;
            } else if (("exclude-class-interceptors" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: excludeClassInterceptors
                Boolean excludeClassInterceptors = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                interceptorBinding.excludeClassInterceptors = excludeClassInterceptors;
            } else if (("method" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: method
                NamedMethod method = readNamedMethod(elementReader, context);
                interceptorBinding.method = method;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-name"), new QName("http://java.sun.com/xml/ns/javaee", "interceptor-class"), new QName("http://java.sun.com/xml/ns/javaee", "interceptor-order"), new QName("http://java.sun.com/xml/ns/javaee", "exclude-default-interceptors"), new QName("http://java.sun.com/xml/ns/javaee", "exclude-class-interceptors"), new QName("http://java.sun.com/xml/ns/javaee", "method"));
            }
        }
        if (descriptions != null) {
            try {
                interceptorBinding.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, InterceptorBinding.class, "setDescriptions", Text[].class, e);
            }
        }
        if (interceptorClass != null) {
            interceptorBinding.interceptorClass = interceptorClass;
        }

        context.afterUnmarshal(interceptorBinding, LifecycleCallback.NONE);

        return interceptorBinding;
    }

    public final InterceptorBinding read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, InterceptorBinding interceptorBinding, RuntimeContext context)
            throws Exception {
        if (interceptorBinding == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (InterceptorBinding.class != interceptorBinding.getClass()) {
            context.unexpectedSubclass(writer, interceptorBinding, InterceptorBinding.class);
            return;
        }

        context.beforeMarshal(interceptorBinding, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = interceptorBinding.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(interceptorBinding, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = interceptorBinding.getDescriptions();
        } catch (Exception e) {
            context.getterError(interceptorBinding, "descriptions", InterceptorBinding.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptorBinding, "descriptions");
                }
            }
        }

        // ELEMENT: ejbName
        String ejbNameRaw = interceptorBinding.ejbName;
        String ejbName = null;
        try {
            ejbName = Adapters.collapsedStringAdapterAdapter.marshal(ejbNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(interceptorBinding, "ejbName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbName != null) {
            writer.writeStartElement(prefix, "ejb-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(interceptorBinding, "ejbName");
        }

        // ELEMENT: interceptorClass
        List<String> interceptorClassRaw = interceptorBinding.interceptorClass;
        if (interceptorClassRaw != null) {
            for (String interceptorClassItem : interceptorClassRaw) {
                String interceptorClass = null;
                try {
                    interceptorClass = Adapters.collapsedStringAdapterAdapter.marshal(interceptorClassItem);
                } catch (Exception e) {
                    context.xmlAdapterError(interceptorBinding, "interceptorClass", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (interceptorClass != null) {
                    writer.writeStartElement(prefix, "interceptor-class", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(interceptorClass);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptorBinding, "interceptorClass");
                }
            }
        }

        // ELEMENT: interceptorOrder
        InterceptorOrder interceptorOrder = interceptorBinding.interceptorOrder;
        if (interceptorOrder != null) {
            writer.writeStartElement(prefix, "interceptor-order", "http://java.sun.com/xml/ns/javaee");
            writeInterceptorOrder(writer, interceptorOrder, context);
            writer.writeEndElement();
        }

        // ELEMENT: excludeDefaultInterceptors
        Boolean excludeDefaultInterceptors = interceptorBinding.excludeDefaultInterceptors;
        writer.writeStartElement(prefix, "exclude-default-interceptors", "http://java.sun.com/xml/ns/javaee");
        writer.writeCharacters(Boolean.toString(excludeDefaultInterceptors));
        writer.writeEndElement();

        // ELEMENT: excludeClassInterceptors
        Boolean excludeClassInterceptors = interceptorBinding.excludeClassInterceptors;
        writer.writeStartElement(prefix, "exclude-class-interceptors", "http://java.sun.com/xml/ns/javaee");
        writer.writeCharacters(Boolean.toString(excludeClassInterceptors));
        writer.writeEndElement();

        // ELEMENT: method
        NamedMethod method = interceptorBinding.method;
        if (method != null) {
            writer.writeStartElement(prefix, "method", "http://java.sun.com/xml/ns/javaee");
            writeNamedMethod(writer, method, context);
            writer.writeEndElement();
        }

        context.afterMarshal(interceptorBinding, LifecycleCallback.NONE);
    }

}
