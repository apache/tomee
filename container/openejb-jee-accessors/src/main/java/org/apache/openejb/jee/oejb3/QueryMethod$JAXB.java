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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.oejb3.MethodParams$JAXB.readMethodParams;
import static org.apache.openejb.jee.oejb3.MethodParams$JAXB.writeMethodParams;

@SuppressWarnings({
    "StringEquality"
})
public class QueryMethod$JAXB
    extends JAXBObject<QueryMethod>
{


    public QueryMethod$JAXB() {
        super(QueryMethod.class, new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "query-method".intern()), new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "queryMethod".intern()), MethodParams$JAXB.class);
    }

    public static QueryMethod readQueryMethod(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeQueryMethod(XoXMLStreamWriter writer, QueryMethod queryMethod, RuntimeContext context)
        throws Exception
    {
        _write(writer, queryMethod, context);
    }

    public void write(XoXMLStreamWriter writer, QueryMethod queryMethod, RuntimeContext context)
        throws Exception
    {
        _write(writer, queryMethod, context);
    }

    public static final QueryMethod _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        QueryMethod queryMethod = new QueryMethod();
        context.beforeUnmarshal(queryMethod, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("queryMethod"!= xsiType.getLocalPart())||("http://www.openejb.org/openejb-jar/1.1"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, QueryMethod.class);
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
            if (("method-name" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodName
                String methodNameRaw = elementReader.getElementText();

                String methodName;
                try {
                    methodName = Adapters.collapsedStringAdapterAdapter.unmarshal(methodNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                queryMethod.methodName = methodName;
            } else if (("method-params" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodParams
                MethodParams methodParams = readMethodParams(elementReader, context);
                queryMethod.methodParams = methodParams;
            } else {
                context.unexpectedElement(elementReader, new QName("http://www.openejb.org/openejb-jar/1.1", "method-name"), new QName("http://www.openejb.org/openejb-jar/1.1", "method-params"));
            }
        }

        context.afterUnmarshal(queryMethod, LifecycleCallback.NONE);

        return queryMethod;
    }

    public final QueryMethod read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, QueryMethod queryMethod, RuntimeContext context)
        throws Exception
    {
        if (queryMethod == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://www.openejb.org/openejb-jar/1.1");
        if (QueryMethod.class!= queryMethod.getClass()) {
            context.unexpectedSubclass(writer, queryMethod, QueryMethod.class);
            return ;
        }

        context.beforeMarshal(queryMethod, LifecycleCallback.NONE);


        // ELEMENT: methodName
        String methodNameRaw = queryMethod.methodName;
        String methodName = null;
        try {
            methodName = Adapters.collapsedStringAdapterAdapter.marshal(methodNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(queryMethod, "methodName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (methodName!= null) {
            writer.writeStartElement(prefix, "method-name", "http://www.openejb.org/openejb-jar/1.1");
            writer.writeCharacters(methodName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(queryMethod, "methodName");
        }

        // ELEMENT: methodParams
        MethodParams methodParams = queryMethod.methodParams;
        if (methodParams!= null) {
            writer.writeStartElement(prefix, "method-params", "http://www.openejb.org/openejb-jar/1.1");
            writeMethodParams(writer, methodParams, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(queryMethod, "methodParams");
        }

        context.afterMarshal(queryMethod, LifecycleCallback.NONE);
    }

}
