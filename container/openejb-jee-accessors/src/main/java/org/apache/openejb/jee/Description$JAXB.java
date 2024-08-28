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
public class Description$JAXB
    extends JAXBObject<Description>
{


    public Description$JAXB() {
        super(Description.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "descriptionType".intern()));
    }

    public static Description readDescription(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeDescription(XoXMLStreamWriter writer, Description description, RuntimeContext context)
        throws Exception
    {
        _write(writer, description, context);
    }

    public void write(XoXMLStreamWriter writer, Description description, RuntimeContext context)
        throws Exception
    {
        _write(writer, description, context);
    }

    public static final Description _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        Description description = new Description();
        context.beforeUnmarshal(description, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("descriptionType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Description.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("lang" == attribute.getLocalName())&&("http://www.w3.org/XML/1998/namespace" == attribute.getNamespace())) {
                // ATTRIBUTE: lang
                description.lang = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, description);
                description.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("http://www.w3.org/XML/1998/namespace", "lang"), new QName("", "id"));
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
            description.value = value;
        }

        context.afterUnmarshal(description, LifecycleCallback.NONE);

        return description;
    }

    public final Description read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, Description description, RuntimeContext context)
        throws Exception
    {
        if (description == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (Description.class!= description.getClass()) {
            context.unexpectedSubclass(writer, description, Description.class);
            return ;
        }

        context.beforeMarshal(description, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = description.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(description, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ATTRIBUTE: lang
        String langRaw = description.lang;
        if (langRaw!= null) {
            String lang = null;
            try {
                lang = Adapters.collapsedStringAdapterAdapter.marshal(langRaw);
            } catch (Exception e) {
                context.xmlAdapterError(description, "lang", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("xml", "http://www.w3.org/XML/1998/namespace", "lang", lang);
        }

        // VALUE: value
        String valueRaw = description.value;
        String value = null;
        try {
            value = Adapters.collapsedStringAdapterAdapter.marshal(valueRaw);
        } catch (Exception e) {
            context.xmlAdapterError(description, "value", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        writer.writeCharacters(value);

        context.afterMarshal(description, LifecycleCallback.NONE);
    }

}
