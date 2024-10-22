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

import java.util.concurrent.TimeUnit;
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
public class Timeout$JAXB
    extends JAXBObject<Timeout>
{


    public Timeout$JAXB() {
        super(Timeout.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "access-timeoutType".intern()));
    }

    public static Timeout readTimeout(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeTimeout(XoXMLStreamWriter writer, Timeout timeout, RuntimeContext context)
        throws Exception
    {
        _write(writer, timeout, context);
    }

    public void write(XoXMLStreamWriter writer, Timeout timeout, RuntimeContext context)
        throws Exception
    {
        _write(writer, timeout, context);
    }

    public static final Timeout _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        Timeout timeout = new Timeout();
        context.beforeUnmarshal(timeout, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("access-timeoutType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Timeout.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, timeout);
                timeout.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("timeout" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: timeout
                Long timeout1 = Long.valueOf(elementReader.getElementText());
                timeout.timeout = timeout1;
            } else if (("unit" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: unit
                String unitRaw = elementReader.getElementText();

                TimeUnit unit;
                try {
                    unit = Adapters.timeUnitAdapterAdapter.unmarshal(unitRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, TimeUnitAdapter.class, TimeUnit.class, TimeUnit.class, e);
                    continue;
                }

                if (unit!= null) {
                    timeout.unit = unit;
                }
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "timeout"), new QName("http://java.sun.com/xml/ns/javaee", "unit"));
            }
        }

        context.afterUnmarshal(timeout, LifecycleCallback.NONE);

        return timeout;
    }

    public final Timeout read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, Timeout timeout, RuntimeContext context)
        throws Exception
    {
        if (timeout == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Timeout.class!= timeout.getClass()) {
            context.unexpectedSubclass(writer, timeout, Timeout.class);
            return ;
        }

        context.beforeMarshal(timeout, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = timeout.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(timeout, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: timeout
        Long timeout1 = timeout.timeout;
        writer.writeStartElement(prefix, "timeout", "http://java.sun.com/xml/ns/javaee");
        writer.writeCharacters(Long.toString(timeout1));
        writer.writeEndElement();

        // ELEMENT: unit
        TimeUnit unitRaw = timeout.unit;
        String unit = null;
        try {
            unit = Adapters.timeUnitAdapterAdapter.marshal(unitRaw);
        } catch (Exception e) {
            context.xmlAdapterError(timeout, "unit", TimeUnitAdapter.class, TimeUnit.class, TimeUnit.class, e);
        }
        if (unit!= null) {
            writer.writeStartElement(prefix, "unit", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(unit);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(timeout, "unit");
        }

        context.afterMarshal(timeout, LifecycleCallback.NONE);
    }

}
