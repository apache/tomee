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

@SuppressWarnings({
    "StringEquality"
})
public class DestinationType$JAXB
    extends JAXBObject<DestinationType>
{


    public DestinationType$JAXB() {
        super(DestinationType.class, new QName("http://java.sun.com/xml/ns/javaee".intern(), "destination-type".intern()), null);
    }

    public static DestinationType readDestinationType(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeDestinationType(XoXMLStreamWriter writer, DestinationType destinationType, RuntimeContext context)
        throws Exception
    {
        _write(writer, destinationType, context);
    }

    public void write(XoXMLStreamWriter writer, DestinationType destinationType, RuntimeContext context)
        throws Exception
    {
        _write(writer, destinationType, context);
    }

    public static final DestinationType _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        DestinationType destinationType = new DestinationType();
        context.beforeUnmarshal(destinationType, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            return context.unexpectedXsiType(reader, DestinationType.class);
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, destinationType);
                destinationType.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // VALUE: value
        String valueRaw = reader.getElementText();

        String value = null;
        boolean valueConverted;
        try {
            value = Adapters.collapsedStringAdapterAdapter.unmarshal(valueRaw);
            valueConverted = true;
        } catch (Exception e) {
            context.xmlAdapterError(reader, CollapsedStringAdapter.class, String.class, String.class, e);
            valueConverted = false;
        }

        if (valueConverted) {
            destinationType.value = value;
        }

        context.afterUnmarshal(destinationType, LifecycleCallback.NONE);

        return destinationType;
    }

    public final DestinationType read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, DestinationType destinationType, RuntimeContext context)
        throws Exception
    {
        if (destinationType == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (DestinationType.class!= destinationType.getClass()) {
            context.unexpectedSubclass(writer, destinationType, DestinationType.class);
            return ;
        }

        context.beforeMarshal(destinationType, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = destinationType.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(destinationType, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // VALUE: value
        String valueRaw = destinationType.value;
        String value = null;
        try {
            value = Adapters.collapsedStringAdapterAdapter.marshal(valueRaw);
        } catch (Exception e) {
            context.xmlAdapterError(destinationType, "value", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        writer.writeCharacters(value);

        context.afterMarshal(destinationType, LifecycleCallback.NONE);
    }

}
