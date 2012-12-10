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

@SuppressWarnings({
        "StringEquality"
})
public class AroundTimeout$JAXB
        extends JAXBObject<AroundTimeout> {


    public AroundTimeout$JAXB() {
        super(AroundTimeout.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "around-timeoutType".intern()));
    }

    public static AroundTimeout readAroundTimeout(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeAroundTimeout(XoXMLStreamWriter writer, AroundTimeout aroundTimeout, RuntimeContext context)
            throws Exception {
        _write(writer, aroundTimeout, context);
    }

    public void write(XoXMLStreamWriter writer, AroundTimeout aroundTimeout, RuntimeContext context)
            throws Exception {
        _write(writer, aroundTimeout, context);
    }

    public final static AroundTimeout _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        AroundTimeout aroundTimeout = new AroundTimeout();
        context.beforeUnmarshal(aroundTimeout, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("around-timeoutType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, AroundTimeout.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute);
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: clazz
                String clazzRaw = elementReader.getElementAsString();

                String clazz;
                try {
                    clazz = Adapters.collapsedStringAdapterAdapter.unmarshal(clazzRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                aroundTimeout.clazz = clazz;
            } else if (("method-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodName
                String methodNameRaw = elementReader.getElementAsString();

                String methodName;
                try {
                    methodName = Adapters.collapsedStringAdapterAdapter.unmarshal(methodNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                aroundTimeout.methodName = methodName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "class"), new QName("http://java.sun.com/xml/ns/javaee", "method-name"));
            }
        }

        context.afterUnmarshal(aroundTimeout, LifecycleCallback.NONE);

        return aroundTimeout;
    }

    public final AroundTimeout read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, AroundTimeout aroundTimeout, RuntimeContext context)
            throws Exception {
        if (aroundTimeout == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (AroundTimeout.class != aroundTimeout.getClass()) {
            context.unexpectedSubclass(writer, aroundTimeout, AroundTimeout.class);
            return;
        }

        context.beforeMarshal(aroundTimeout, LifecycleCallback.NONE);


        // ELEMENT: clazz
        String clazzRaw = aroundTimeout.clazz;
        String clazz = null;
        try {
            clazz = Adapters.collapsedStringAdapterAdapter.marshal(clazzRaw);
        } catch (Exception e) {
            context.xmlAdapterError(aroundTimeout, "clazz", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (clazz != null) {
            writer.writeStartElement(prefix, "class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(clazz);
            writer.writeEndElement();
        }

        // ELEMENT: methodName
        String methodNameRaw = aroundTimeout.methodName;
        String methodName = null;
        try {
            methodName = Adapters.collapsedStringAdapterAdapter.marshal(methodNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(aroundTimeout, "methodName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (methodName != null) {
            writer.writeStartElement(prefix, "method-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(methodName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(aroundTimeout, "methodName");
        }

        context.afterMarshal(aroundTimeout, LifecycleCallback.NONE);
    }

}
