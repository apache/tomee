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
public class DisplayName$JAXB
    extends JAXBObject<DisplayName>
{


    public DisplayName$JAXB() {
        super(DisplayName.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "display-nameType".intern()));
    }

    public static DisplayName readDisplayName(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeDisplayName(XoXMLStreamWriter writer, DisplayName displayName, RuntimeContext context)
        throws Exception
    {
        _write(writer, displayName, context);
    }

    public void write(XoXMLStreamWriter writer, DisplayName displayName, RuntimeContext context)
        throws Exception
    {
        _write(writer, displayName, context);
    }

    public static final DisplayName _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        DisplayName displayName = new DisplayName();
        context.beforeUnmarshal(displayName, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("display-nameType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, DisplayName.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("lang" == attribute.getLocalName())&&("http://www.w3.org/XML/1998/namespace" == attribute.getNamespace())) {
                // ATTRIBUTE: lang
                displayName.lang = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, displayName);
                displayName.id = id;
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
            displayName.value = value;
        }

        context.afterUnmarshal(displayName, LifecycleCallback.NONE);

        return displayName;
    }

    public final DisplayName read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, DisplayName displayName, RuntimeContext context)
        throws Exception
    {
        if (displayName == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (DisplayName.class!= displayName.getClass()) {
            context.unexpectedSubclass(writer, displayName, DisplayName.class);
            return ;
        }

        context.beforeMarshal(displayName, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = displayName.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(displayName, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ATTRIBUTE: lang
        String langRaw = displayName.lang;
        if (langRaw!= null) {
            String lang = null;
            try {
                lang = Adapters.collapsedStringAdapterAdapter.marshal(langRaw);
            } catch (Exception e) {
                context.xmlAdapterError(displayName, "lang", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("xml", "http://www.w3.org/XML/1998/namespace", "lang", lang);
        }

        // VALUE: value
        String valueRaw = displayName.value;
        String value = null;
        try {
            value = Adapters.collapsedStringAdapterAdapter.marshal(valueRaw);
        } catch (Exception e) {
            context.xmlAdapterError(displayName, "value", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        writer.writeCharacters(value);

        context.afterMarshal(displayName, LifecycleCallback.NONE);
    }

}
