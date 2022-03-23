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
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({
    "StringEquality"
})
public class ServletMapping$JAXB
    extends JAXBObject<ServletMapping> {


    public ServletMapping$JAXB() {
        super(ServletMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "servlet-mappingType".intern()));
    }

    public static ServletMapping readServletMapping(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeServletMapping(final XoXMLStreamWriter writer, final ServletMapping servletMapping, final RuntimeContext context)
        throws Exception {
        _write(writer, servletMapping, context);
    }

    public void write(final XoXMLStreamWriter writer, final ServletMapping servletMapping, final RuntimeContext context)
        throws Exception {
        _write(writer, servletMapping, context);
    }

    public final static ServletMapping _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final ServletMapping servletMapping = new ServletMapping();
        context.beforeUnmarshal(servletMapping, LifecycleCallback.NONE);

        List<String> urlPattern = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("servlet-mappingType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ServletMapping.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, servletMapping);
                servletMapping.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("servlet-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: servletName
                final String servletNameRaw = elementReader.getElementAsString();

                final String servletName;
                try {
                    servletName = Adapters.collapsedStringAdapterAdapter.unmarshal(servletNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                servletMapping.servletName = servletName;
            } else if (("url-pattern" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: urlPattern
                final String urlPatternItemRaw = elementReader.getElementAsString();

                final String urlPatternItem;
                try {
                    urlPatternItem = Adapters.trimStringAdapterAdapter.unmarshal(urlPatternItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, TrimStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (urlPattern == null) {
                    urlPattern = servletMapping.urlPattern;
                    if (urlPattern != null) {
                        urlPattern.clear();
                    } else {
                        urlPattern = new ArrayList<String>();
                    }
                }
                urlPattern.add(urlPatternItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "servlet-name"), new QName("http://java.sun.com/xml/ns/javaee", "url-pattern"));
            }
        }
        if (urlPattern != null) {
            servletMapping.urlPattern = urlPattern;
        }

        context.afterUnmarshal(servletMapping, LifecycleCallback.NONE);

        return servletMapping;
    }

    public final ServletMapping read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final ServletMapping servletMapping, RuntimeContext context)
        throws Exception {
        if (servletMapping == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ServletMapping.class != servletMapping.getClass()) {
            context.unexpectedSubclass(writer, servletMapping, ServletMapping.class);
            return;
        }

        context.beforeMarshal(servletMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = servletMapping.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(servletMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: servletName
        final String servletNameRaw = servletMapping.servletName;
        String servletName = null;
        try {
            servletName = Adapters.collapsedStringAdapterAdapter.marshal(servletNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(servletMapping, "servletName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (servletName != null) {
            writer.writeStartElement(prefix, "servlet-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(servletName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(servletMapping, "servletName");
        }

        // ELEMENT: urlPattern
        final List<String> urlPatternRaw = servletMapping.urlPattern;
        if (urlPatternRaw != null) {
            for (final String urlPatternItem : urlPatternRaw) {
                String urlPattern = null;
                try {
                    urlPattern = Adapters.trimStringAdapterAdapter.marshal(urlPatternItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(servletMapping, "urlPattern", TrimStringAdapter.class, List.class, List.class, e);
                }
                if (urlPattern != null) {
                    writer.writeStartElement(prefix, "url-pattern", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(urlPattern);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(servletMapping, "urlPattern");
                }
            }
        }

        context.afterMarshal(servletMapping, LifecycleCallback.NONE);
    }

}
