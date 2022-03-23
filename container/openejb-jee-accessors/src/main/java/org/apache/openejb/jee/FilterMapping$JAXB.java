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

import static org.apache.openejb.jee.Dispatcher$JAXB.parseDispatcher;
import static org.apache.openejb.jee.Dispatcher$JAXB.toStringDispatcher;

@SuppressWarnings({
    "StringEquality"
})
public class FilterMapping$JAXB
    extends JAXBObject<FilterMapping> {


    public FilterMapping$JAXB() {
        super(FilterMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "filter-mappingType".intern()), Dispatcher$JAXB.class);
    }

    public static FilterMapping readFilterMapping(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFilterMapping(final XoXMLStreamWriter writer, final FilterMapping filterMapping, final RuntimeContext context)
        throws Exception {
        _write(writer, filterMapping, context);
    }

    public void write(final XoXMLStreamWriter writer, final FilterMapping filterMapping, final RuntimeContext context)
        throws Exception {
        _write(writer, filterMapping, context);
    }

    public final static FilterMapping _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FilterMapping filterMapping = new FilterMapping();
        context.beforeUnmarshal(filterMapping, LifecycleCallback.NONE);

        List<String> urlPattern = null;
        List<String> servletName = null;
        List<Dispatcher> dispatcher = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("filter-mappingType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FilterMapping.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, filterMapping);
                filterMapping.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("filter-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: filterName
                final String filterNameRaw = elementReader.getElementAsString();

                final String filterName;
                try {
                    filterName = Adapters.collapsedStringAdapterAdapter.unmarshal(filterNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                filterMapping.filterName = filterName;
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
                    urlPattern = filterMapping.urlPattern;
                    if (urlPattern != null) {
                        urlPattern.clear();
                    } else {
                        urlPattern = new ArrayList<String>();
                    }
                }
                urlPattern.add(urlPatternItem);
            } else if (("servlet-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: servletName
                final String servletNameItemRaw = elementReader.getElementAsString();

                final String servletNameItem;
                try {
                    servletNameItem = Adapters.collapsedStringAdapterAdapter.unmarshal(servletNameItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (servletName == null) {
                    servletName = filterMapping.servletName;
                    if (servletName != null) {
                        servletName.clear();
                    } else {
                        servletName = new ArrayList<String>();
                    }
                }
                servletName.add(servletNameItem);
            } else if (("dispatcher" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: dispatcher
                Dispatcher dispatcherItem = null;
                if (!elementReader.isXsiNil()) {
                    dispatcherItem = parseDispatcher(elementReader, context, elementReader.getElementAsString());
                }
                if (dispatcher == null) {
                    dispatcher = filterMapping.dispatcher;
                    if (dispatcher != null) {
                        dispatcher.clear();
                    } else {
                        dispatcher = new ArrayList<Dispatcher>();
                    }
                }
                dispatcher.add(dispatcherItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "filter-name"), new QName("http://java.sun.com/xml/ns/javaee", "url-pattern"), new QName("http://java.sun.com/xml/ns/javaee", "servlet-name"), new QName("http://java.sun.com/xml/ns/javaee", "dispatcher"));
            }
        }
        if (urlPattern != null) {
            filterMapping.urlPattern = urlPattern;
        }
        if (servletName != null) {
            filterMapping.servletName = servletName;
        }
        if (dispatcher != null) {
            filterMapping.dispatcher = dispatcher;
        }

        context.afterUnmarshal(filterMapping, LifecycleCallback.NONE);

        return filterMapping;
    }

    public final FilterMapping read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FilterMapping filterMapping, RuntimeContext context)
        throws Exception {
        if (filterMapping == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FilterMapping.class != filterMapping.getClass()) {
            context.unexpectedSubclass(writer, filterMapping, FilterMapping.class);
            return;
        }

        context.beforeMarshal(filterMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = filterMapping.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(filterMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: filterName
        final String filterNameRaw = filterMapping.filterName;
        String filterName = null;
        try {
            filterName = Adapters.collapsedStringAdapterAdapter.marshal(filterNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(filterMapping, "filterName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (filterName != null) {
            writer.writeStartElement(prefix, "filter-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(filterName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(filterMapping, "filterName");
        }

        // ELEMENT: urlPattern
        final List<String> urlPatternRaw = filterMapping.urlPattern;
        if (urlPatternRaw != null) {
            for (final String urlPatternItem : urlPatternRaw) {
                String urlPattern = null;
                try {
                    urlPattern = Adapters.trimStringAdapterAdapter.marshal(urlPatternItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(filterMapping, "urlPattern", TrimStringAdapter.class, List.class, List.class, e);
                }
                if (urlPattern != null) {
                    writer.writeStartElement(prefix, "url-pattern", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(urlPattern);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: servletName
        final List<String> servletNameRaw = filterMapping.servletName;
        if (servletNameRaw != null) {
            for (final String servletNameItem : servletNameRaw) {
                String servletName = null;
                try {
                    servletName = Adapters.collapsedStringAdapterAdapter.marshal(servletNameItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(filterMapping, "servletName", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (servletName != null) {
                    writer.writeStartElement(prefix, "servlet-name", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(servletName);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: dispatcher
        final List<Dispatcher> dispatcher = filterMapping.dispatcher;
        if (dispatcher != null) {
            for (final Dispatcher dispatcherItem : dispatcher) {
                writer.writeStartElement(prefix, "dispatcher", "http://java.sun.com/xml/ns/javaee");
                if (dispatcherItem != null) {
                    writer.writeCharacters(toStringDispatcher(filterMapping, null, context, dispatcherItem));
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        context.afterMarshal(filterMapping, LifecycleCallback.NONE);
    }

}
