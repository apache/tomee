/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee;

import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


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
    extends JAXBObject<Filter>
{


    public Filter$JAXB() {
        super(Filter.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "filterType".intern()), Text$JAXB.class, Icon$JAXB.class, ParamValue$JAXB.class);
    }

    public static Filter readFilter(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFilter(XoXMLStreamWriter writer, Filter filter, RuntimeContext context)
        throws Exception
    {
        _write(writer, filter, context);
    }

    public void write(XoXMLStreamWriter writer, Filter filter, RuntimeContext context)
        throws Exception
    {
        _write(writer, filter, context);
    }

    public static final Filter _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        Filter filter = new Filter();
        context.beforeUnmarshal(filter, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<ParamValue> initParam = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("filterType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Filter.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, filter);
                filter.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("description" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<>();
                }
                descriptions.add(descriptionsItem);
            } else if (("display-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayNames
                Text displayNamesItem = readText(elementReader, context);
                if (displayNames == null) {
                    displayNames = new ArrayList<>();
                }
                displayNames.add(displayNamesItem);
            } else if (("icon" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = filter.icon;
                    if (icon!= null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<>();
                    }
                }
                icon.add(iconItem);
            } else if (("filter-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: filterName
                String filterNameRaw = elementReader.getElementText();

                String filterName;
                try {
                    filterName = Adapters.collapsedStringAdapterAdapter.unmarshal(filterNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                filter.filterName = filterName;
            } else if (("filter-class" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: filterClass
                String filterClassRaw = elementReader.getElementText();

                String filterClass;
                try {
                    filterClass = Adapters.collapsedStringAdapterAdapter.unmarshal(filterClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                filter.filterClass = filterClass;
            } else if (("async-supported" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: asyncSupported
                Boolean asyncSupported = ("1".equals(elementReader.getElementText())||"true".equals(elementReader.getElementText()));
                filter.asyncSupported = asyncSupported;
            } else if (("init-param" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: initParam
                ParamValue initParamItem = readParamValue(elementReader, context);
                if (initParam == null) {
                    initParam = filter.initParam;
                    if (initParam!= null) {
                        initParam.clear();
                    } else {
                        initParam = new ArrayList<>();
                    }
                }
                initParam.add(initParamItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "filter-name"), new QName("http://java.sun.com/xml/ns/javaee", "filter-class"), new QName("http://java.sun.com/xml/ns/javaee", "async-supported"), new QName("http://java.sun.com/xml/ns/javaee", "init-param"));
            }
        }
        if (descriptions!= null) {
            try {
                filter.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, Filter.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames!= null) {
            try {
                filter.setDisplayNames(displayNames.toArray(new Text[displayNames.size()] ));
            } catch (Exception e) {
                context.setterError(reader, Filter.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon!= null) {
            filter.icon = icon;
        }
        if (initParam!= null) {
            filter.initParam = initParam;
        }

        context.afterUnmarshal(filter, LifecycleCallback.NONE);

        return filter;
    }

    public final Filter read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, Filter filter, RuntimeContext context)
        throws Exception
    {
        if (filter == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Filter.class!= filter.getClass()) {
            context.unexpectedSubclass(writer, filter, Filter.class);
            return ;
        }

        context.beforeMarshal(filter, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = filter.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(filter, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = filter.getDescriptions();
        } catch (Exception e) {
            context.getterError(filter, "descriptions", Filter.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
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
        } catch (Exception e) {
            context.getterError(filter, "displayNames", Filter.class, "getDisplayNames", e);
        }
        if (displayNames!= null) {
            for (Text displayNamesItem: displayNames) {
                if (displayNamesItem!= null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(filter, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = filter.icon;
        if (icon!= null) {
            for (Icon iconItem: icon) {
                if (iconItem!= null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(filter, "icon");
                }
            }
        }

        // ELEMENT: filterName
        String filterNameRaw = filter.filterName;
        String filterName = null;
        try {
            filterName = Adapters.collapsedStringAdapterAdapter.marshal(filterNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(filter, "filterName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (filterName!= null) {
            writer.writeStartElement(prefix, "filter-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(filterName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(filter, "filterName");
        }

        // ELEMENT: filterClass
        String filterClassRaw = filter.filterClass;
        String filterClass = null;
        try {
            filterClass = Adapters.collapsedStringAdapterAdapter.marshal(filterClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(filter, "filterClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (filterClass!= null) {
            writer.writeStartElement(prefix, "filter-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(filterClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(filter, "filterClass");
        }

        // ELEMENT: asyncSupported
        Boolean asyncSupported = filter.asyncSupported;
        writer.writeStartElement(prefix, "async-supported", "http://java.sun.com/xml/ns/javaee");
        writer.writeCharacters(Boolean.toString(asyncSupported));
        writer.writeEndElement();

        // ELEMENT: initParam
        List<ParamValue> initParam = filter.initParam;
        if (initParam!= null) {
            for (ParamValue initParamItem: initParam) {
                if (initParamItem!= null) {
                    writer.writeStartElement(prefix, "init-param", "http://java.sun.com/xml/ns/javaee");
                    writeParamValue(writer, initParamItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(filter, LifecycleCallback.NONE);
    }

}
