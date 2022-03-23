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

import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.ParamValue$JAXB.readParamValue;
import static org.apache.openejb.jee.ParamValue$JAXB.writeParamValue;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class Filter$JAXB
    extends JAXBObject<Filter> {


    public Filter$JAXB() {
        super(Filter.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "filterType".intern()), Text$JAXB.class, Icon$JAXB.class, ParamValue$JAXB.class);
    }

    public static Filter readFilter(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFilter(final XoXMLStreamWriter writer, final Filter filter, final RuntimeContext context)
        throws Exception {
        _write(writer, filter, context);
    }

    public void write(final XoXMLStreamWriter writer, final Filter filter, final RuntimeContext context)
        throws Exception {
        _write(writer, filter, context);
    }

    public final static Filter _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final Filter filter = new Filter();
        context.beforeUnmarshal(filter, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<ParamValue> initParam = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("filterType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Filter.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, filter);
                filter.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                final Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<Text>();
                }
                descriptions.add(descriptionsItem);
            } else if (("display-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayNames
                final Text displayNamesItem = readText(elementReader, context);
                if (displayNames == null) {
                    displayNames = new ArrayList<Text>();
                }
                displayNames.add(displayNamesItem);
            } else if (("icon" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                final Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = filter.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("filter-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: filterName
                final String filterNameRaw = elementReader.getElementAsString();

                final String filterName;
                try {
                    filterName = Adapters.collapsedStringAdapterAdapter.unmarshal(filterNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                filter.filterName = filterName;
            } else if (("filter-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: filterClass
                final String filterClassRaw = elementReader.getElementAsString();

                final String filterClass;
                try {
                    filterClass = Adapters.collapsedStringAdapterAdapter.unmarshal(filterClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                filter.filterClass = filterClass;
            } else if (("async-supported" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: asyncSupported
                final Boolean asyncSupported = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                filter.asyncSupported = asyncSupported;
            } else if (("init-param" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: initParam
                final ParamValue initParamItem = readParamValue(elementReader, context);
                if (initParam == null) {
                    initParam = filter.initParam;
                    if (initParam != null) {
                        initParam.clear();
                    } else {
                        initParam = new ArrayList<ParamValue>();
                    }
                }
                initParam.add(initParamItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "filter-name"), new QName("http://java.sun.com/xml/ns/javaee", "filter-class"), new QName("http://java.sun.com/xml/ns/javaee", "async-supported"), new QName("http://java.sun.com/xml/ns/javaee", "init-param"));
            }
        }
        if (descriptions != null) {
            try {
                filter.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, Filter.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                filter.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, Filter.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            filter.icon = icon;
        }
        if (initParam != null) {
            filter.initParam = initParam;
        }

        context.afterUnmarshal(filter, LifecycleCallback.NONE);

        return filter;
    }

    public final Filter read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final Filter filter, RuntimeContext context)
        throws Exception {
        if (filter == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Filter.class != filter.getClass()) {
            context.unexpectedSubclass(writer, filter, Filter.class);
            return;
        }

        context.beforeMarshal(filter, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = filter.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(filter, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = filter.getDescriptions();
        } catch (final Exception e) {
            context.getterError(filter, "descriptions", Filter.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(filter, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = filter.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(filter, "displayNames", Filter.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(filter, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = filter.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(filter, "icon");
                }
            }
        }

        // ELEMENT: filterName
        final String filterNameRaw = filter.filterName;
        String filterName = null;
        try {
            filterName = Adapters.collapsedStringAdapterAdapter.marshal(filterNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(filter, "filterName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (filterName != null) {
            writer.writeStartElement(prefix, "filter-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(filterName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(filter, "filterName");
        }

        // ELEMENT: filterClass
        final String filterClassRaw = filter.filterClass;
        String filterClass = null;
        try {
            filterClass = Adapters.collapsedStringAdapterAdapter.marshal(filterClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(filter, "filterClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (filterClass != null) {
            writer.writeStartElement(prefix, "filter-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(filterClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(filter, "filterClass");
        }

        // ELEMENT: asyncSupported
        final Boolean asyncSupported = filter.asyncSupported;
        writer.writeStartElement(prefix, "async-supported", "http://java.sun.com/xml/ns/javaee");
        writer.writeCharacters(Boolean.toString(asyncSupported));
        writer.writeEndElement();

        // ELEMENT: initParam
        final List<ParamValue> initParam = filter.initParam;
        if (initParam != null) {
            for (final ParamValue initParamItem : initParam) {
                if (initParamItem != null) {
                    writer.writeStartElement(prefix, "init-param", "http://java.sun.com/xml/ns/javaee");
                    writeParamValue(writer, initParamItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(filter, LifecycleCallback.NONE);
    }

}
