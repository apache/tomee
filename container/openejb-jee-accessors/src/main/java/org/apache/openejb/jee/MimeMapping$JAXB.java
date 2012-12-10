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

import com.envoisolutions.sxc.jaxb.JAXBObject;
import com.envoisolutions.sxc.jaxb.LifecycleCallback;
import com.envoisolutions.sxc.jaxb.RuntimeContext;
import com.envoisolutions.sxc.util.Attribute;
import com.envoisolutions.sxc.util.XoXMLStreamReader;
import com.envoisolutions.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;

@SuppressWarnings({
        "StringEquality"
})
public class MimeMapping$JAXB
        extends JAXBObject<MimeMapping> {


    public MimeMapping$JAXB() {
        super(MimeMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "mime-mappingType".intern()));
    }

    public static MimeMapping readMimeMapping(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeMimeMapping(XoXMLStreamWriter writer, MimeMapping mimeMapping, RuntimeContext context)
            throws Exception {
        _write(writer, mimeMapping, context);
    }

    public void write(XoXMLStreamWriter writer, MimeMapping mimeMapping, RuntimeContext context)
            throws Exception {
        _write(writer, mimeMapping, context);
    }

    public final static MimeMapping _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        MimeMapping mimeMapping = new MimeMapping();
        context.beforeUnmarshal(mimeMapping, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("mime-mappingType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, MimeMapping.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, mimeMapping);
                mimeMapping.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: extension
                String extensionRaw = elementReader.getElementAsString();

                String extension;
                try {
                    extension = Adapters.collapsedStringAdapterAdapter.unmarshal(extensionRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                mimeMapping.extension = extension;
            } else if (("mime-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: mimeType
                String mimeTypeRaw = elementReader.getElementAsString();

                String mimeType;
                try {
                    mimeType = Adapters.collapsedStringAdapterAdapter.unmarshal(mimeTypeRaw);
                } catch (Exception e) {
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

    public final MimeMapping read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, MimeMapping mimeMapping, RuntimeContext context)
            throws Exception {
        if (mimeMapping == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (MimeMapping.class != mimeMapping.getClass()) {
            context.unexpectedSubclass(writer, mimeMapping, MimeMapping.class);
            return;
        }

        context.beforeMarshal(mimeMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = mimeMapping.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(mimeMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: extension
        String extensionRaw = mimeMapping.extension;
        String extension = null;
        try {
            extension = Adapters.collapsedStringAdapterAdapter.marshal(extensionRaw);
        } catch (Exception e) {
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
        String mimeTypeRaw = mimeMapping.mimeType;
        String mimeType = null;
        try {
            mimeType = Adapters.collapsedStringAdapterAdapter.marshal(mimeTypeRaw);
        } catch (Exception e) {
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
