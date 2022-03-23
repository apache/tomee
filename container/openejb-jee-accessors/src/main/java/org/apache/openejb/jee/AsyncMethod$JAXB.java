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

import static org.apache.openejb.jee.MethodParams$JAXB.readMethodParams;
import static org.apache.openejb.jee.MethodParams$JAXB.writeMethodParams;

@SuppressWarnings({
    "StringEquality"
})
public class AsyncMethod$JAXB
    extends JAXBObject<AsyncMethod> {


    public AsyncMethod$JAXB() {
        super(AsyncMethod.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "async-methodType".intern()), MethodParams$JAXB.class);
    }

    public static AsyncMethod readAsyncMethod(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeAsyncMethod(final XoXMLStreamWriter writer, final AsyncMethod asyncMethod, final RuntimeContext context)
        throws Exception {
        _write(writer, asyncMethod, context);
    }

    public void write(final XoXMLStreamWriter writer, final AsyncMethod asyncMethod, final RuntimeContext context)
        throws Exception {
        _write(writer, asyncMethod, context);
    }

    public final static AsyncMethod _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final AsyncMethod asyncMethod = new AsyncMethod();
        context.beforeUnmarshal(asyncMethod, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("async-methodType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, AsyncMethod.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, asyncMethod);
                asyncMethod.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("method-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodName
                final String methodNameRaw = elementReader.getElementAsString();

                final String methodName;
                try {
                    methodName = Adapters.collapsedStringAdapterAdapter.unmarshal(methodNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                asyncMethod.methodName = methodName;
            } else if (("method-params" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodParams
                final MethodParams methodParams = readMethodParams(elementReader, context);
                asyncMethod.methodParams = methodParams;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "method-name"), new QName("http://java.sun.com/xml/ns/javaee", "method-params"));
            }
        }

        context.afterUnmarshal(asyncMethod, LifecycleCallback.NONE);

        return asyncMethod;
    }

    public final AsyncMethod read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final AsyncMethod asyncMethod, RuntimeContext context)
        throws Exception {
        if (asyncMethod == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (AsyncMethod.class != asyncMethod.getClass()) {
            context.unexpectedSubclass(writer, asyncMethod, AsyncMethod.class);
            return;
        }

        context.beforeMarshal(asyncMethod, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = asyncMethod.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(asyncMethod, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: methodName
        final String methodNameRaw = asyncMethod.methodName;
        String methodName = null;
        try {
            methodName = Adapters.collapsedStringAdapterAdapter.marshal(methodNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(asyncMethod, "methodName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (methodName != null) {
            writer.writeStartElement(prefix, "method-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(methodName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(asyncMethod, "methodName");
        }

        // ELEMENT: methodParams
        final MethodParams methodParams = asyncMethod.methodParams;
        if (methodParams != null) {
            writer.writeStartElement(prefix, "method-params", "http://java.sun.com/xml/ns/javaee");
            writeMethodParams(writer, methodParams, context);
            writer.writeEndElement();
        }

        context.afterMarshal(asyncMethod, LifecycleCallback.NONE);
    }

}
