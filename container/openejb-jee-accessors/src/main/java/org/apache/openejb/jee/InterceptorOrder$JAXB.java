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

@SuppressWarnings({
        "StringEquality"
})
public class InterceptorOrder$JAXB
        extends JAXBObject<InterceptorOrder> {


    public InterceptorOrder$JAXB() {
        super(InterceptorOrder.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "interceptor-orderType".intern()));
    }

    public static InterceptorOrder readInterceptorOrder(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeInterceptorOrder(XoXMLStreamWriter writer, InterceptorOrder interceptorOrder, RuntimeContext context)
            throws Exception {
        _write(writer, interceptorOrder, context);
    }

    public void write(XoXMLStreamWriter writer, InterceptorOrder interceptorOrder, RuntimeContext context)
            throws Exception {
        _write(writer, interceptorOrder, context);
    }

    public final static InterceptorOrder _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        InterceptorOrder interceptorOrder = new InterceptorOrder();
        context.beforeUnmarshal(interceptorOrder, LifecycleCallback.NONE);

        List<String> interceptorClass = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("interceptor-orderType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, InterceptorOrder.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, interceptorOrder);
                interceptorOrder.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("interceptor-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
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
                    interceptorClass = interceptorOrder.interceptorClass;
                    if (interceptorClass != null) {
                        interceptorClass.clear();
                    } else {
                        interceptorClass = new ArrayList<String>();
                    }
                }
                interceptorClass.add(interceptorClassItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "interceptor-class"));
            }
        }
        if (interceptorClass != null) {
            interceptorOrder.interceptorClass = interceptorClass;
        }

        context.afterUnmarshal(interceptorOrder, LifecycleCallback.NONE);

        return interceptorOrder;
    }

    public final InterceptorOrder read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, InterceptorOrder interceptorOrder, RuntimeContext context)
            throws Exception {
        if (interceptorOrder == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (InterceptorOrder.class != interceptorOrder.getClass()) {
            context.unexpectedSubclass(writer, interceptorOrder, InterceptorOrder.class);
            return;
        }

        context.beforeMarshal(interceptorOrder, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = interceptorOrder.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(interceptorOrder, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: interceptorClass
        List<String> interceptorClassRaw = interceptorOrder.interceptorClass;
        if (interceptorClassRaw != null) {
            for (String interceptorClassItem : interceptorClassRaw) {
                String interceptorClass = null;
                try {
                    interceptorClass = Adapters.collapsedStringAdapterAdapter.marshal(interceptorClassItem);
                } catch (Exception e) {
                    context.xmlAdapterError(interceptorOrder, "interceptorClass", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (interceptorClass != null) {
                    writer.writeStartElementWithAutoPrefix("http://java.sun.com/xml/ns/javaee", "interceptor-class");
                    writer.writeCharacters(interceptorClass);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptorOrder, "interceptorClass");
                }
            }
        }

        context.afterMarshal(interceptorOrder, LifecycleCallback.NONE);
    }

}
