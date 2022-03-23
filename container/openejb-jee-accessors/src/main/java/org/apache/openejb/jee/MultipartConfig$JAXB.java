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
public class MultipartConfig$JAXB
    extends JAXBObject<MultipartConfig> {


    public MultipartConfig$JAXB() {
        super(MultipartConfig.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "multipart-configType".intern()));
    }

    public static MultipartConfig readMultipartConfig(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeMultipartConfig(final XoXMLStreamWriter writer, final MultipartConfig multipartConfig, final RuntimeContext context)
        throws Exception {
        _write(writer, multipartConfig, context);
    }

    public void write(final XoXMLStreamWriter writer, final MultipartConfig multipartConfig, final RuntimeContext context)
        throws Exception {
        _write(writer, multipartConfig, context);
    }

    public final static MultipartConfig _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final MultipartConfig multipartConfig = new MultipartConfig();
        context.beforeUnmarshal(multipartConfig, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("multipart-configType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, MultipartConfig.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute);
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("location" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: location
                final String locationRaw = elementReader.getElementAsString();

                final String location;
                try {
                    location = Adapters.collapsedStringAdapterAdapter.unmarshal(locationRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                multipartConfig.location = location;
            } else if (("max-file-size" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: maxFileSize
                final Long maxFileSize = Long.valueOf(elementReader.getElementAsString());
                multipartConfig.maxFileSize = maxFileSize;
            } else if (("max-request-size" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: maxRequestSize
                final Long maxRequestSize = Long.valueOf(elementReader.getElementAsString());
                multipartConfig.maxRequestSize = maxRequestSize;
            } else if (("file-size-threshold" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: fileSizeThreshold
                final Integer fileSizeThreshold = Integer.valueOf(elementReader.getElementAsString());
                multipartConfig.fileSizeThreshold = fileSizeThreshold;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "location"), new QName("http://java.sun.com/xml/ns/javaee", "max-file-size"), new QName("http://java.sun.com/xml/ns/javaee", "max-request-size"), new QName("http://java.sun.com/xml/ns/javaee", "file-size-threshold"));
            }
        }

        context.afterUnmarshal(multipartConfig, LifecycleCallback.NONE);

        return multipartConfig;
    }

    public final MultipartConfig read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final MultipartConfig multipartConfig, RuntimeContext context)
        throws Exception {
        if (multipartConfig == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (MultipartConfig.class != multipartConfig.getClass()) {
            context.unexpectedSubclass(writer, multipartConfig, MultipartConfig.class);
            return;
        }

        context.beforeMarshal(multipartConfig, LifecycleCallback.NONE);


        // ELEMENT: location
        final String locationRaw = multipartConfig.location;
        String location = null;
        try {
            location = Adapters.collapsedStringAdapterAdapter.marshal(locationRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(multipartConfig, "location", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (location != null) {
            writer.writeStartElement(prefix, "location", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(location);
            writer.writeEndElement();
        }

        // ELEMENT: maxFileSize
        final Long maxFileSize = multipartConfig.maxFileSize;
        writer.writeStartElement(prefix, "max-file-size", "http://java.sun.com/xml/ns/javaee");
        writer.writeCharacters(Long.toString(maxFileSize));
        writer.writeEndElement();

        // ELEMENT: maxRequestSize
        final Long maxRequestSize = multipartConfig.maxRequestSize;
        writer.writeStartElement(prefix, "max-request-size", "http://java.sun.com/xml/ns/javaee");
        writer.writeCharacters(Long.toString(maxRequestSize));
        writer.writeEndElement();

        // ELEMENT: fileSizeThreshold
        final Integer fileSizeThreshold = multipartConfig.fileSizeThreshold;
        writer.writeStartElement(prefix, "file-size-threshold", "http://java.sun.com/xml/ns/javaee");
        writer.writeCharacters(Integer.toString(fileSizeThreshold));
        writer.writeEndElement();

        context.afterMarshal(multipartConfig, LifecycleCallback.NONE);
    }

}
