/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
    * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.jee;

import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;

@SuppressWarnings({
    "StringEquality"
})
public class Text$JAXB
    extends JAXBObject<Text> {


    public Text$JAXB() {
        super(Text.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "string".intern()));
    }

    public static Text readText(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeText(final XoXMLStreamWriter writer, final Text text, final RuntimeContext context)
        throws Exception {
        _write(writer, text, context);
    }

    public void write(final XoXMLStreamWriter writer, final Text text, final RuntimeContext context)
        throws Exception {
        _write(writer, text, context);
    }

    public final static Text _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final Text text = new Text();
        context.beforeUnmarshal(text, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("string" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Text.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, text);
                text.id = id;
            } else if (("lang" == attribute.getLocalName()) && ("http://www.w3.org/XML/1998/namespace" == attribute.getNamespace())) {
                // ATTRIBUTE: lang
                text.lang = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"), new QName("http://www.w3.org/XML/1998/namespace", "lang"));
            }
        }

        // VALUE: value
        final String valueRaw = reader.getElementText();

        String value = null;
        boolean valueConverted;
        try {
            value = Adapters.collapsedStringAdapterAdapter.unmarshal(valueRaw);
            valueConverted = true;
        } catch (final Exception e) {
            context.xmlAdapterError(reader, CollapsedStringAdapter.class, String.class, String.class, e);
            valueConverted = false;
        }

        if (valueConverted) {
            text.value = value;
        }

        context.afterUnmarshal(text, LifecycleCallback.NONE);

        return text;
    }

    public final Text read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final Text text, RuntimeContext context)
        throws Exception {
        if (text == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (Text.class != text.getClass()) {
            context.unexpectedSubclass(writer, text, Text.class);
            return;
        }

        context.beforeMarshal(text, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = text.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(text, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ATTRIBUTE: lang
        final String langRaw = text.lang;
        if (langRaw != null) {
            String lang = null;
            try {
                lang = Adapters.collapsedStringAdapterAdapter.marshal(langRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(text, "lang", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("xml", "http://www.w3.org/XML/1998/namespace", "lang", lang);
        }

        // VALUE: value
        final String valueRaw = text.value;
        String value = null;
        try {
            value = Adapters.collapsedStringAdapterAdapter.marshal(valueRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(text, "value", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        writer.writeCharacters(value);

        context.afterMarshal(text, LifecycleCallback.NONE);
    }

}
