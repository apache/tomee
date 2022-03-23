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
public class CookieConfig$JAXB
    extends JAXBObject<CookieConfig> {


    public CookieConfig$JAXB() {
        super(CookieConfig.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "cookie-configType".intern()));
    }

    public static CookieConfig readCookieConfig(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeCookieConfig(final XoXMLStreamWriter writer, final CookieConfig cookieConfig, final RuntimeContext context)
        throws Exception {
        _write(writer, cookieConfig, context);
    }

    public void write(final XoXMLStreamWriter writer, final CookieConfig cookieConfig, final RuntimeContext context)
        throws Exception {
        _write(writer, cookieConfig, context);
    }

    public final static CookieConfig _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final CookieConfig cookieConfig = new CookieConfig();
        context.beforeUnmarshal(cookieConfig, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("cookie-configType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, CookieConfig.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, cookieConfig);
                cookieConfig.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: name
                final String nameRaw = elementReader.getElementAsString();

                final String name;
                try {
                    name = Adapters.collapsedStringAdapterAdapter.unmarshal(nameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                cookieConfig.name = name;
            } else if (("domain" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: domain
                final String domainRaw = elementReader.getElementAsString();

                final String domain;
                try {
                    domain = Adapters.collapsedStringAdapterAdapter.unmarshal(domainRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                cookieConfig.domain = domain;
            } else if (("path" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: path
                final String pathRaw = elementReader.getElementAsString();

                final String path;
                try {
                    path = Adapters.collapsedStringAdapterAdapter.unmarshal(pathRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                cookieConfig.path = path;
            } else if (("comment" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: comment
                final String commentRaw = elementReader.getElementAsString();

                final String comment;
                try {
                    comment = Adapters.collapsedStringAdapterAdapter.unmarshal(commentRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                cookieConfig.comment = comment;
            } else if (("http-only" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: httpOnly
                final Boolean httpOnly = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                cookieConfig.httpOnly = httpOnly;
            } else if (("secure" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: secure
                final Boolean secure = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                cookieConfig.secure = secure;
            } else if (("max-age" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: maxAge
                final Integer maxAge = Integer.valueOf(elementReader.getElementAsString());
                cookieConfig.maxAge = maxAge;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "name"), new QName("http://java.sun.com/xml/ns/javaee", "domain"), new QName("http://java.sun.com/xml/ns/javaee", "path"), new QName("http://java.sun.com/xml/ns/javaee", "comment"), new QName("http://java.sun.com/xml/ns/javaee", "http-only"), new QName("http://java.sun.com/xml/ns/javaee", "secure"), new QName("http://java.sun.com/xml/ns/javaee", "max-age"));
            }
        }

        context.afterUnmarshal(cookieConfig, LifecycleCallback.NONE);

        return cookieConfig;
    }

    public final CookieConfig read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final CookieConfig cookieConfig, RuntimeContext context)
        throws Exception {
        if (cookieConfig == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (CookieConfig.class != cookieConfig.getClass()) {
            context.unexpectedSubclass(writer, cookieConfig, CookieConfig.class);
            return;
        }

        context.beforeMarshal(cookieConfig, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = cookieConfig.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(cookieConfig, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: name
        final String nameRaw = cookieConfig.name;
        String name = null;
        try {
            name = Adapters.collapsedStringAdapterAdapter.marshal(nameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(cookieConfig, "name", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (name != null) {
            writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(name);
            writer.writeEndElement();
        }

        // ELEMENT: domain
        final String domainRaw = cookieConfig.domain;
        String domain = null;
        try {
            domain = Adapters.collapsedStringAdapterAdapter.marshal(domainRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(cookieConfig, "domain", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (domain != null) {
            writer.writeStartElement(prefix, "domain", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(domain);
            writer.writeEndElement();
        }

        // ELEMENT: path
        final String pathRaw = cookieConfig.path;
        String path = null;
        try {
            path = Adapters.collapsedStringAdapterAdapter.marshal(pathRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(cookieConfig, "path", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (path != null) {
            writer.writeStartElement(prefix, "path", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(path);
            writer.writeEndElement();
        }

        // ELEMENT: comment
        final String commentRaw = cookieConfig.comment;
        String comment = null;
        try {
            comment = Adapters.collapsedStringAdapterAdapter.marshal(commentRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(cookieConfig, "comment", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (comment != null) {
            writer.writeStartElement(prefix, "comment", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(comment);
            writer.writeEndElement();
        }

        // ELEMENT: httpOnly
        final Boolean httpOnly = cookieConfig.httpOnly;
        if (httpOnly != null) {
            writer.writeStartElement(prefix, "http-only", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(httpOnly));
            writer.writeEndElement();
        }

        // ELEMENT: secure
        final Boolean secure = cookieConfig.secure;
        if (secure != null) {
            writer.writeStartElement(prefix, "secure", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(secure));
            writer.writeEndElement();
        }

        // ELEMENT: maxAge
        final Integer maxAge = cookieConfig.maxAge;
        if (maxAge != null) {
            writer.writeStartElement(prefix, "max-age", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Integer.toString(maxAge));
            writer.writeEndElement();
        }

        context.afterMarshal(cookieConfig, LifecycleCallback.NONE);
    }

}
