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
public class LocaleEncodingMapping$JAXB
    extends JAXBObject<LocaleEncodingMapping> {


    public LocaleEncodingMapping$JAXB() {
        super(LocaleEncodingMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "locale-encoding-mappingType".intern()));
    }

    public static LocaleEncodingMapping readLocaleEncodingMapping(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeLocaleEncodingMapping(final XoXMLStreamWriter writer, final LocaleEncodingMapping localeEncodingMapping, final RuntimeContext context)
        throws Exception {
        _write(writer, localeEncodingMapping, context);
    }

    public void write(final XoXMLStreamWriter writer, final LocaleEncodingMapping localeEncodingMapping, final RuntimeContext context)
        throws Exception {
        _write(writer, localeEncodingMapping, context);
    }

    public final static LocaleEncodingMapping _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final LocaleEncodingMapping localeEncodingMapping = new LocaleEncodingMapping();
        context.beforeUnmarshal(localeEncodingMapping, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("locale-encoding-mappingType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, LocaleEncodingMapping.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, localeEncodingMapping);
                localeEncodingMapping.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("locale" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: locale
                final String localeRaw = elementReader.getElementAsString();

                final String locale;
                try {
                    locale = Adapters.collapsedStringAdapterAdapter.unmarshal(localeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                localeEncodingMapping.locale = locale;
            } else if (("encoding" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: encoding
                final String encodingRaw = elementReader.getElementAsString();

                final String encoding;
                try {
                    encoding = Adapters.collapsedStringAdapterAdapter.unmarshal(encodingRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                localeEncodingMapping.encoding = encoding;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "locale"), new QName("http://java.sun.com/xml/ns/javaee", "encoding"));
            }
        }

        context.afterUnmarshal(localeEncodingMapping, LifecycleCallback.NONE);

        return localeEncodingMapping;
    }

    public final LocaleEncodingMapping read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final LocaleEncodingMapping localeEncodingMapping, RuntimeContext context)
        throws Exception {
        if (localeEncodingMapping == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (LocaleEncodingMapping.class != localeEncodingMapping.getClass()) {
            context.unexpectedSubclass(writer, localeEncodingMapping, LocaleEncodingMapping.class);
            return;
        }

        context.beforeMarshal(localeEncodingMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = localeEncodingMapping.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(localeEncodingMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: locale
        final String localeRaw = localeEncodingMapping.locale;
        String locale = null;
        try {
            locale = Adapters.collapsedStringAdapterAdapter.marshal(localeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(localeEncodingMapping, "locale", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (locale != null) {
            writer.writeStartElement(prefix, "locale", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(locale);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(localeEncodingMapping, "locale");
        }

        // ELEMENT: encoding
        final String encodingRaw = localeEncodingMapping.encoding;
        String encoding = null;
        try {
            encoding = Adapters.collapsedStringAdapterAdapter.marshal(encodingRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(localeEncodingMapping, "encoding", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (encoding != null) {
            writer.writeStartElement(prefix, "encoding", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(encoding);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(localeEncodingMapping, "encoding");
        }

        context.afterMarshal(localeEncodingMapping, LifecycleCallback.NONE);
    }

}
