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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.MethodParams$JAXB.readMethodParams;
import static org.apache.openejb.jee.MethodParams$JAXB.writeMethodParams;

@SuppressWarnings({
    "StringEquality"
})
public class NamedMethod$JAXB
    extends JAXBObject<NamedMethod>
{


    public NamedMethod$JAXB() {
        super(NamedMethod.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "named-methodType".intern()), MethodParams$JAXB.class);
    }

    public static NamedMethod readNamedMethod(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeNamedMethod(XoXMLStreamWriter writer, NamedMethod namedMethod, RuntimeContext context)
        throws Exception
    {
        _write(writer, namedMethod, context);
    }

    public void write(XoXMLStreamWriter writer, NamedMethod namedMethod, RuntimeContext context)
        throws Exception
    {
        _write(writer, namedMethod, context);
    }

    public static final NamedMethod _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        NamedMethod namedMethod = new NamedMethod();
        context.beforeUnmarshal(namedMethod, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("named-methodType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, NamedMethod.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, namedMethod);
                namedMethod.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("method-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodName
                String methodNameRaw = elementReader.getElementText();

                String methodName;
                try {
                    methodName = Adapters.collapsedStringAdapterAdapter.unmarshal(methodNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                namedMethod.methodName = methodName;
            } else if (("method-params" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodParams
                MethodParams methodParams = readMethodParams(elementReader, context);
                namedMethod.methodParams = methodParams;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "method-name"), new QName("http://java.sun.com/xml/ns/javaee", "method-params"));
            }
        }

        context.afterUnmarshal(namedMethod, LifecycleCallback.NONE);

        return namedMethod;
    }

    public final NamedMethod read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, NamedMethod namedMethod, RuntimeContext context)
        throws Exception
    {
        if (namedMethod == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (NamedMethod.class!= namedMethod.getClass()) {
            context.unexpectedSubclass(writer, namedMethod, NamedMethod.class);
            return ;
        }

        context.beforeMarshal(namedMethod, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = namedMethod.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(namedMethod, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: methodName
        String methodNameRaw = namedMethod.methodName;
        String methodName = null;
        try {
            methodName = Adapters.collapsedStringAdapterAdapter.marshal(methodNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(namedMethod, "methodName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (methodName!= null) {
            writer.writeStartElement(prefix, "method-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(methodName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(namedMethod, "methodName");
        }

        // ELEMENT: methodParams
        MethodParams methodParams = namedMethod.methodParams;
        if (methodParams!= null) {
            writer.writeStartElement(prefix, "method-params", "http://java.sun.com/xml/ns/javaee");
            writeMethodParams(writer, methodParams, context);
            writer.writeEndElement();
        }

        context.afterMarshal(namedMethod, LifecycleCallback.NONE);
    }

}
