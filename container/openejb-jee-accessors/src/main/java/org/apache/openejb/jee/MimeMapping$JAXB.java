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
public class MimeMapping$JAXB
    extends JAXBObject<MimeMapping> {


    public MimeMapping$JAXB() {
        super(MimeMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "mime-mappingType".intern()));
    }

    public static MimeMapping readMimeMapping(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeMimeMapping(final XoXMLStreamWriter writer, final MimeMapping mimeMapping, final RuntimeContext context)
        throws Exception {
        _write(writer, mimeMapping, context);
    }

    public void write(final XoXMLStreamWriter writer, final MimeMapping mimeMapping, final RuntimeContext context)
        throws Exception {
        _write(writer, mimeMapping, context);
    }

    public final static MimeMapping _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final MimeMapping mimeMapping = new MimeMapping();
        context.beforeUnmarshal(mimeMapping, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("mime-mappingType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, MimeMapping.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, mimeMapping);
                mimeMapping.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: extension
                final String extensionRaw = elementReader.getElementAsString();

                final String extension;
                try {
                    extension = Adapters.collapsedStringAdapterAdapter.unmarshal(extensionRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                mimeMapping.extension = extension;
            } else if (("mime-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: mimeType
                final String mimeTypeRaw = elementReader.getElementAsString();

                final String mimeType;
                try {
                    mimeType = Adapters.collapsedStringAdapterAdapter.unmarshal(mimeTypeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                mimeMapping.mimeType = mimeType;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "extension"), new QName("http://java.sun.com/xml/ns/javaee", "mime-type"));
            }
        }

        context.afterUnmarshal(mimeMapping, LifecycleCallback.NONE);

        return mimeMapping;
    }

    public final MimeMapping read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final MimeMapping mimeMapping, RuntimeContext context)
        throws Exception {
        if (mimeMapping == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (MimeMapping.class != mimeMapping.getClass()) {
            context.unexpectedSubclass(writer, mimeMapping, MimeMapping.class);
            return;
        }

        context.beforeMarshal(mimeMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = mimeMapping.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(mimeMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: extension
        final String extensionRaw = mimeMapping.extension;
        String extension = null;
        try {
            extension = Adapters.collapsedStringAdapterAdapter.marshal(extensionRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(mimeMapping, "extension", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (extension != null) {
            writer.writeStartElement(prefix, "extension", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(extension);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(mimeMapping, "extension");
        }

        // ELEMENT: mimeType
        final String mimeTypeRaw = mimeMapping.mimeType;
        String mimeType = null;
        try {
            mimeType = Adapters.collapsedStringAdapterAdapter.marshal(mimeTypeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(mimeMapping, "mimeType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (mimeType != null) {
            writer.writeStartElement(prefix, "mime-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(mimeType);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(mimeMapping, "mimeType");
        }

        context.afterMarshal(mimeMapping, LifecycleCallback.NONE);
    }

}
