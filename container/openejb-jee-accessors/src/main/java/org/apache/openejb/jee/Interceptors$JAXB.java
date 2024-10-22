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


import static org.apache.openejb.jee.Interceptor$JAXB.readInterceptor;
import static org.apache.openejb.jee.Interceptor$JAXB.writeInterceptor;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class Interceptors$JAXB
    extends JAXBObject<Interceptors>
{


    public Interceptors$JAXB() {
        super(Interceptors.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "interceptorsType".intern()), Text$JAXB.class, Interceptor$JAXB.class);
    }

    public static Interceptors readInterceptors(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeInterceptors(XoXMLStreamWriter writer, Interceptors interceptors, RuntimeContext context)
        throws Exception
    {
        _write(writer, interceptors, context);
    }

    public void write(XoXMLStreamWriter writer, Interceptors interceptors, RuntimeContext context)
        throws Exception
    {
        _write(writer, interceptors, context);
    }

    public static final Interceptors _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        Interceptors interceptors = new Interceptors();
        context.beforeUnmarshal(interceptors, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Interceptor> interceptor = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("interceptorsType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Interceptors.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, interceptors);
                interceptors.id = id;
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
            } else if (("interceptor" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: interceptor
                Interceptor interceptorItem = readInterceptor(elementReader, context);
                if (interceptor == null) {
                    interceptor = new ArrayList<>();
                }
                interceptor.add(interceptorItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "interceptor"));
            }
        }
        if (descriptions!= null) {
            try {
                interceptors.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, Interceptors.class, "setDescriptions", Text[].class, e);
            }
        }
        if (interceptor!= null) {
            try {
                interceptors.setInterceptor(interceptor.toArray(new Interceptor[interceptor.size()] ));
            } catch (Exception e) {
                context.setterError(reader, Interceptors.class, "setInterceptor", Interceptor[].class, e);
            }
        }

        context.afterUnmarshal(interceptors, LifecycleCallback.NONE);

        return interceptors;
    }

    public final Interceptors read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, Interceptors interceptors, RuntimeContext context)
        throws Exception
    {
        if (interceptors == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Interceptors.class!= interceptors.getClass()) {
            context.unexpectedSubclass(writer, interceptors, Interceptors.class);
            return ;
        }

        context.beforeMarshal(interceptors, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = interceptors.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(interceptors, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = interceptors.getDescriptions();
        } catch (Exception e) {
            context.getterError(interceptors, "descriptions", Interceptors.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptors, "descriptions");
                }
            }
        }

        // ELEMENT: interceptor
        Interceptor[] interceptor = null;
        try {
            interceptor = interceptors.getInterceptor();
        } catch (Exception e) {
            context.getterError(interceptors, "interceptor", Interceptors.class, "getInterceptor", e);
        }
        if (interceptor!= null) {
            for (Interceptor interceptorItem: interceptor) {
                if (interceptorItem!= null) {
                    writer.writeStartElement(prefix, "interceptor", "http://java.sun.com/xml/ns/javaee");
                    writeInterceptor(writer, interceptorItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(interceptors, "interceptor");
                }
            }
        }

        context.afterMarshal(interceptors, LifecycleCallback.NONE);
    }

}
