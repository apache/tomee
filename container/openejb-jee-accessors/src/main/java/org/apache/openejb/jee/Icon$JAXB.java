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
public class Icon$JAXB
    extends JAXBObject<Icon> {


    public Icon$JAXB() {
        super(Icon.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "iconType".intern()));
    }

    public static Icon readIcon(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeIcon(final XoXMLStreamWriter writer, final Icon icon, final RuntimeContext context)
        throws Exception {
        _write(writer, icon, context);
    }

    public void write(final XoXMLStreamWriter writer, final Icon icon, final RuntimeContext context)
        throws Exception {
        _write(writer, icon, context);
    }

    public final static Icon _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final Icon icon = new Icon();
        context.beforeUnmarshal(icon, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("iconType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Icon.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, icon);
                icon.id = id;
            } else if (("lang" == attribute.getLocalName()) && ("http://www.w3.org/XML/1998/namespace" == attribute.getNamespace())) {
                // ATTRIBUTE: lang
                icon.lang = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"), new QName("http://www.w3.org/XML/1998/namespace", "lang"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("small-icon" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: smallIcon
                final String smallIconRaw = elementReader.getElementAsString();

                final String smallIcon;
                try {
                    smallIcon = Adapters.collapsedStringAdapterAdapter.unmarshal(smallIconRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                icon.smallIcon = smallIcon;
            } else if (("large-icon" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: largeIcon
                final String largeIconRaw = elementReader.getElementAsString();

                final String largeIcon;
                try {
                    largeIcon = Adapters.collapsedStringAdapterAdapter.unmarshal(largeIconRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                icon.largeIcon = largeIcon;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "small-icon"), new QName("http://java.sun.com/xml/ns/javaee", "large-icon"));
            }
        }

        context.afterUnmarshal(icon, LifecycleCallback.NONE);

        return icon;
    }

    public final Icon read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final Icon icon, RuntimeContext context)
        throws Exception {
        if (icon == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Icon.class != icon.getClass()) {
            context.unexpectedSubclass(writer, icon, Icon.class);
            return;
        }

        context.beforeMarshal(icon, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = icon.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(icon, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ATTRIBUTE: lang
        final String langRaw = icon.lang;
        if (langRaw != null) {
            String lang = null;
            try {
                lang = Adapters.collapsedStringAdapterAdapter.marshal(langRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(icon, "lang", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("xml", "http://www.w3.org/XML/1998/namespace", "lang", lang);
        }

        // ELEMENT: smallIcon
        final String smallIconRaw = icon.smallIcon;
        String smallIcon = null;
        try {
            smallIcon = Adapters.collapsedStringAdapterAdapter.marshal(smallIconRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(icon, "smallIcon", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (smallIcon != null) {
            writer.writeStartElement(prefix, "small-icon", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(smallIcon);
            writer.writeEndElement();
        }

        // ELEMENT: largeIcon
        final String largeIconRaw = icon.largeIcon;
        String largeIcon = null;
        try {
            largeIcon = Adapters.collapsedStringAdapterAdapter.marshal(largeIconRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(icon, "largeIcon", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (largeIcon != null) {
            writer.writeStartElement(prefix, "large-icon", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(largeIcon);
            writer.writeEndElement();
        }

        context.afterMarshal(icon, LifecycleCallback.NONE);
    }

}
