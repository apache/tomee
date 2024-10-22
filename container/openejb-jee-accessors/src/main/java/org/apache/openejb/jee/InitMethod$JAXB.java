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


import static org.apache.openejb.jee.NamedMethod$JAXB.readNamedMethod;
import static org.apache.openejb.jee.NamedMethod$JAXB.writeNamedMethod;

@SuppressWarnings({
    "StringEquality"
})
public class InitMethod$JAXB
    extends JAXBObject<InitMethod>
{


    public InitMethod$JAXB() {
        super(InitMethod.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "init-methodType".intern()), NamedMethod$JAXB.class);
    }

    public static InitMethod readInitMethod(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeInitMethod(XoXMLStreamWriter writer, InitMethod initMethod, RuntimeContext context)
        throws Exception
    {
        _write(writer, initMethod, context);
    }

    public void write(XoXMLStreamWriter writer, InitMethod initMethod, RuntimeContext context)
        throws Exception
    {
        _write(writer, initMethod, context);
    }

    public static final InitMethod _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        InitMethod initMethod = new InitMethod();
        context.beforeUnmarshal(initMethod, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("init-methodType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, InitMethod.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, initMethod);
                initMethod.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("create-method" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: createMethod
                NamedMethod createMethod = readNamedMethod(elementReader, context);
                initMethod.createMethod = createMethod;
            } else if (("bean-method" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: beanMethod
                NamedMethod beanMethod = readNamedMethod(elementReader, context);
                initMethod.beanMethod = beanMethod;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "create-method"), new QName("http://java.sun.com/xml/ns/javaee", "bean-method"));
            }
        }

        context.afterUnmarshal(initMethod, LifecycleCallback.NONE);

        return initMethod;
    }

    public final InitMethod read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, InitMethod initMethod, RuntimeContext context)
        throws Exception
    {
        if (initMethod == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (InitMethod.class!= initMethod.getClass()) {
            context.unexpectedSubclass(writer, initMethod, InitMethod.class);
            return ;
        }

        context.beforeMarshal(initMethod, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = initMethod.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(initMethod, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: createMethod
        NamedMethod createMethod = initMethod.createMethod;
        if (createMethod!= null) {
            writer.writeStartElement(prefix, "create-method", "http://java.sun.com/xml/ns/javaee");
            writeNamedMethod(writer, createMethod, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(initMethod, "createMethod");
        }

        // ELEMENT: beanMethod
        NamedMethod beanMethod = initMethod.beanMethod;
        if (beanMethod!= null) {
            writer.writeStartElement(prefix, "bean-method", "http://java.sun.com/xml/ns/javaee");
            writeNamedMethod(writer, beanMethod, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(initMethod, "beanMethod");
        }

        context.afterMarshal(initMethod, LifecycleCallback.NONE);
    }

}
