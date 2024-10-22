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
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.MethodIntf$JAXB.parseMethodIntf;
import static org.apache.openejb.jee.MethodIntf$JAXB.toStringMethodIntf;
import static org.apache.openejb.jee.MethodParams$JAXB.readMethodParams;
import static org.apache.openejb.jee.MethodParams$JAXB.writeMethodParams;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class Method$JAXB
    extends JAXBObject<Method>
{


    public Method$JAXB() {
        super(Method.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "methodType".intern()), Text$JAXB.class, MethodIntf$JAXB.class, MethodParams$JAXB.class);
    }

    public static Method readMethod(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeMethod(XoXMLStreamWriter writer, Method method, RuntimeContext context)
        throws Exception
    {
        _write(writer, method, context);
    }

    public void write(XoXMLStreamWriter writer, Method method, RuntimeContext context)
        throws Exception
    {
        _write(writer, method, context);
    }

    public static final Method _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        Method method = new Method();
        context.beforeUnmarshal(method, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("methodType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Method.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, method);
                method.id = id;
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
            } else if (("ejb-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbName
                String ejbNameRaw = elementReader.getElementText();

                String ejbName;
                try {
                    ejbName = Adapters.collapsedStringAdapterAdapter.unmarshal(ejbNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                method.ejbName = ejbName;
            } else if (("method-intf" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodIntf
                MethodIntf methodIntf = parseMethodIntf(elementReader, context, elementReader.getElementText());
                if (methodIntf!= null) {
                    method.methodIntf = methodIntf;
                }
            } else if (("method-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodName
                String methodNameRaw = elementReader.getElementText();

                String methodName;
                try {
                    methodName = Adapters.collapsedStringAdapterAdapter.unmarshal(methodNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                method.methodName = methodName;
            } else if (("method-params" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodParams
                MethodParams methodParams = readMethodParams(elementReader, context);
                method.methodParams = methodParams;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "ejb-name"), new QName("http://java.sun.com/xml/ns/javaee", "method-intf"), new QName("http://java.sun.com/xml/ns/javaee", "method-name"), new QName("http://java.sun.com/xml/ns/javaee", "method-params"));
            }
        }
        if (descriptions!= null) {
            try {
                method.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, Method.class, "setDescriptions", Text[].class, e);
            }
        }

        context.afterUnmarshal(method, LifecycleCallback.NONE);

        return method;
    }

    public final Method read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, Method method, RuntimeContext context)
        throws Exception
    {
        if (method == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Method.class!= method.getClass()) {
            context.unexpectedSubclass(writer, method, Method.class);
            return ;
        }

        context.beforeMarshal(method, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = method.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(method, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = method.getDescriptions();
        } catch (Exception e) {
            context.getterError(method, "descriptions", Method.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(method, "descriptions");
                }
            }
        }

        // ELEMENT: ejbName
        String ejbNameRaw = method.ejbName;
        String ejbName = null;
        try {
            ejbName = Adapters.collapsedStringAdapterAdapter.marshal(ejbNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(method, "ejbName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbName!= null) {
            writer.writeStartElement(prefix, "ejb-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(method, "ejbName");
        }

        // ELEMENT: methodIntf
        MethodIntf methodIntf = method.methodIntf;
        if (methodIntf!= null) {
            writer.writeStartElement(prefix, "method-intf", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringMethodIntf(method, null, context, methodIntf));
            writer.writeEndElement();
        }

        // ELEMENT: methodName
        String methodNameRaw = method.methodName;
        String methodName = null;
        try {
            methodName = Adapters.collapsedStringAdapterAdapter.marshal(methodNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(method, "methodName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (methodName!= null) {
            writer.writeStartElement(prefix, "method-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(methodName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(method, "methodName");
        }

        // ELEMENT: methodParams
        MethodParams methodParams = method.methodParams;
        if (methodParams!= null) {
            writer.writeStartElement(prefix, "method-params", "http://java.sun.com/xml/ns/javaee");
            writeMethodParams(writer, methodParams, context);
            writer.writeEndElement();
        }

        context.afterMarshal(method, LifecycleCallback.NONE);
    }

}
