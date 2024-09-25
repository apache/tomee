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

package org.apache.openejb.jee.oejb3;

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

@SuppressWarnings({
    "StringEquality"
})
public class MethodParams$JAXB
    extends JAXBObject<MethodParams>
{


    public MethodParams$JAXB() {
        super(MethodParams.class, new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "method-params".intern()), new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "methodParams".intern()));
    }

    public static MethodParams readMethodParams(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeMethodParams(XoXMLStreamWriter writer, MethodParams methodParams, RuntimeContext context)
        throws Exception
    {
        _write(writer, methodParams, context);
    }

    public void write(XoXMLStreamWriter writer, MethodParams methodParams, RuntimeContext context)
        throws Exception
    {
        _write(writer, methodParams, context);
    }

    public static final MethodParams _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        MethodParams methodParams = new MethodParams();
        context.beforeUnmarshal(methodParams, LifecycleCallback.NONE);

        List<String> methodParam = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("methodParams"!= xsiType.getLocalPart())||("http://www.openejb.org/openejb-jar/1.1"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, MethodParams.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, methodParams);
                methodParams.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("method-param" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodParam
                String methodParamItemRaw = elementReader.getElementText();

                String methodParamItem;
                try {
                    methodParamItem = Adapters.collapsedStringAdapterAdapter.unmarshal(methodParamItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (methodParam == null) {
                    methodParam = methodParams.methodParam;
                    if (methodParam!= null) {
                        methodParam.clear();
                    } else {
                        methodParam = new ArrayList<>();
                    }
                }
                methodParam.add(methodParamItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://www.openejb.org/openejb-jar/1.1", "method-param"));
            }
        }
        if (methodParam!= null) {
            methodParams.methodParam = methodParam;
        }

        context.afterUnmarshal(methodParams, LifecycleCallback.NONE);

        return methodParams;
    }

    public final MethodParams read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, MethodParams methodParams, RuntimeContext context)
        throws Exception
    {
        if (methodParams == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (MethodParams.class!= methodParams.getClass()) {
            context.unexpectedSubclass(writer, methodParams, MethodParams.class);
            return ;
        }

        context.beforeMarshal(methodParams, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = methodParams.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(methodParams, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: methodParam
        List<String> methodParamRaw = methodParams.methodParam;
        if (methodParamRaw!= null) {
            for (String methodParamItem: methodParamRaw) {
                String methodParam = null;
                try {
                    methodParam = Adapters.collapsedStringAdapterAdapter.marshal(methodParamItem);
                } catch (Exception e) {
                    context.xmlAdapterError(methodParams, "methodParam", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (methodParam!= null) {
                    writer.writeStartElementWithAutoPrefix("http://www.openejb.org/openejb-jar/1.1", "method-param");
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