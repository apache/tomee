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

@SuppressWarnings({
    "StringEquality"
})
public class MethodParams$JAXB
    extends JAXBObject<MethodParams> {


    public MethodParams$JAXB() {
        super(MethodParams.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "method-paramsType".intern()));
    }

    public static MethodParams readMethodParams(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeMethodParams(final XoXMLStreamWriter writer, final MethodParams methodParams, final RuntimeContext context)
        throws Exception {
        _write(writer, methodParams, context);
    }

    public void write(final XoXMLStreamWriter writer, final MethodParams methodParams, final RuntimeContext context)
        throws Exception {
        _write(writer, methodParams, context);
    }

    public final static MethodParams _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final MethodParams methodParams = new MethodParams();
        context.beforeUnmarshal(methodParams, LifecycleCallback.NONE);

        List<String> methodParam = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("method-paramsType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, MethodParams.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, methodParams);
                methodParams.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("method-param" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodParam
                final String methodParamItemRaw = elementReader.getElementAsString();

                final String methodParamItem;
                try {
                    methodParamItem = Adapters.collapsedStringAdapterAdapter.unmarshal(methodParamItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (methodParam == null) {
                    methodParam = methodParams.methodParam;
                    if (methodParam != null) {
                        methodParam.clear();
                    } else {
                        methodParam = new ArrayList<String>();
                    }
                }
                methodParam.add(methodParamItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "method-param"));
            }
        }
        if (methodParam != null) {
            methodParams.methodParam = methodParam;
        }

        context.afterUnmarshal(methodParams, LifecycleCallback.NONE);

        return methodParams;
    }

    public final MethodParams read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final MethodParams methodParams, RuntimeContext context)
        throws Exception {
        if (methodParams == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (MethodParams.class != methodParams.getClass()) {
            context.unexpectedSubclass(writer, methodParams, MethodParams.class);
            return;
        }

        context.beforeMarshal(methodParams, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = methodParams.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(methodParams, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: methodParam
        final List<String> methodParamRaw = methodParams.methodParam;
        if (methodParamRaw != null) {
            for (final String methodParamItem : methodParamRaw) {
                String methodParam = null;
                try {
                    methodParam = Adapters.collapsedStringAdapterAdapter.marshal(methodParamItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(methodParams, "methodParam", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (methodParam != null) {
                    writer.writeStartElementWithAutoPrefix("http://java.sun.com/xml/ns/javaee", "method-param");
                    writer.writeCharacters(methodParam);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(methodParams, "methodParam");
                }
            }
        }

        context.afterMarshal(methodParams, LifecycleCallback.NONE);
    }

}
