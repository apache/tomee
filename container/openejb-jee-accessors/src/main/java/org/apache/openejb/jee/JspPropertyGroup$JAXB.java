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
import java.util.ArrayList;
import java.util.List;

import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class JspPropertyGroup$JAXB
        extends JAXBObject<JspPropertyGroup> {


    public JspPropertyGroup$JAXB() {
        super(JspPropertyGroup.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "jsp-property-groupType".intern()), Text$JAXB.class, Icon$JAXB.class);
    }

    public static JspPropertyGroup readJspPropertyGroup(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeJspPropertyGroup(XoXMLStreamWriter writer, JspPropertyGroup jspPropertyGroup, RuntimeContext context)
            throws Exception {
        _write(writer, jspPropertyGroup, context);
    }

    public void write(XoXMLStreamWriter writer, JspPropertyGroup jspPropertyGroup, RuntimeContext context)
            throws Exception {
        _write(writer, jspPropertyGroup, context);
    }

    public final static JspPropertyGroup _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        JspPropertyGroup jspPropertyGroup = new JspPropertyGroup();
        context.beforeUnmarshal(jspPropertyGroup, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<String> urlPattern = null;
        List<String> includePrelude = null;
        List<String> includeCoda = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("jsp-property-groupType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, JspPropertyGroup.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, jspPropertyGroup);
                jspPropertyGroup.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<Text>();
                }
                descriptions.add(descriptionsItem);
            } else if (("display-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayNames
                Text displayNamesItem = readText(elementReader, context);
                if (displayNames == null) {
                    displayNames = new ArrayList<Text>();
                }
                displayNames.add(displayNamesItem);
            } else if (("icon" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = jspPropertyGroup.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("url-pattern" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: urlPattern
                String urlPatternItemRaw = elementReader.getElementAsString();

                String urlPatternItem;
                try {
                    urlPatternItem = Adapters.trimStringAdapterAdapter.unmarshal(urlPatternItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, TrimStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (urlPattern == null) {
                    urlPattern = jspPropertyGroup.urlPattern;
                    if (urlPattern != null) {
                        urlPattern.clear();
                    } else {
                        urlPattern = new ArrayList<String>();
                    }
                }
                urlPattern.add(urlPatternItem);
            } else if (("el-ignored" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: elIgnored
                Boolean elIgnored = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                jspPropertyGroup.elIgnored = elIgnored;
            } else if (("page-encoding" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: pageEncoding
                String pageEncodingRaw = elementReader.getElementAsString();

                String pageEncoding;
                try {
                    pageEncoding = Adapters.collapsedStringAdapterAdapter.unmarshal(pageEncodingRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                jspPropertyGroup.pageEncoding = pageEncoding;
            } else if (("scripting-invalid" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: scriptingInvalid
                Boolean scriptingInvalid = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                jspPropertyGroup.scriptingInvalid = scriptingInvalid;
            } else if (("is-xml" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: isXml
                Boolean isXml = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                jspPropertyGroup.isXml = isXml;
            } else if (("include-prelude" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: includePrelude
                String includePreludeItemRaw = elementReader.getElementAsString();

                String includePreludeItem;
                try {
                    includePreludeItem = Adapters.collapsedStringAdapterAdapter.unmarshal(includePreludeItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (includePrelude == null) {
                    includePrelude = jspPropertyGroup.includePrelude;
                    if (includePrelude != null) {
                        includePrelude.clear();
                    } else {
                        includePrelude = new ArrayList<String>();
                    }
                }
                includePrelude.add(includePreludeItem);
            } else if (("include-coda" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: includeCoda
                String includeCodaItemRaw = elementReader.getElementAsString();

                String includeCodaItem;
                try {
                    includeCodaItem = Adapters.collapsedStringAdapterAdapter.unmarshal(includeCodaItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (includeCoda == null) {
                    includeCoda = jspPropertyGroup.includeCoda;
                    if (includeCoda != null) {
                        includeCoda.clear();
                    } else {
                        includeCoda = new ArrayList<String>();
                    }
                }
                includeCoda.add(includeCodaItem);
            } else if (("deferred-syntax-allowed-as-literal" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: deferredSyntaxAllowedAsLiteral
                Boolean deferredSyntaxAllowedAsLiteral = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                jspPropertyGroup.deferredSyntaxAllowedAsLiteral = deferredSyntaxAllowedAsLiteral;
            } else if (("trim-directive-whitespaces" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: trimDirectiveWhitespaces
                Boolean trimDirectiveWhitespaces = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                jspPropertyGroup.trimDirectiveWhitespaces = trimDirectiveWhitespaces;
            } else if (("default-content-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: defaultContentType
                String defaultContentTypeRaw = elementReader.getElementAsString();

                String defaultContentType;
                try {
                    defaultContentType = Adapters.collapsedStringAdapterAdapter.unmarshal(defaultContentTypeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                jspPropertyGroup.defaultContentType = defaultContentType;
            } else if (("buffer" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: buffer
                String bufferRaw = elementReader.getElementAsString();

                String buffer;
                try {
                    buffer = Adapters.collapsedStringAdapterAdapter.unmarshal(bufferRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                jspPropertyGroup.buffer = buffer;
            } else if (("error-on-undeclared-namespace" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: errorOnUndeclaredNamespace
                Boolean errorOnUndeclaredNamespace = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                jspPropertyGroup.errorOnUndeclaredNamespace = errorOnUndeclaredNamespace;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "url-pattern"), new QName("http://java.sun.com/xml/ns/javaee", "el-ignored"), new QName("http://java.sun.com/xml/ns/javaee", "page-encoding"), new QName("http://java.sun.com/xml/ns/javaee", "scripting-invalid"), new QName("http://java.sun.com/xml/ns/javaee", "is-xml"), new QName("http://java.sun.com/xml/ns/javaee", "include-prelude"), new QName("http://java.sun.com/xml/ns/javaee", "include-coda"), new QName("http://java.sun.com/xml/ns/javaee", "deferred-syntax-allowed-as-literal"), new QName("http://java.sun.com/xml/ns/javaee", "trim-directive-whitespaces"), new QName("http://java.sun.com/xml/ns/javaee", "default-content-type"), new QName("http://java.sun.com/xml/ns/javaee", "buffer"), new QName("http://java.sun.com/xml/ns/javaee", "error-on-undeclared-namespace"));
            }
        }
        if (descriptions != null) {
            try {
                jspPropertyGroup.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, JspPropertyGroup.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                jspPropertyGroup.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (Exception e) {
                context.setterError(reader, JspPropertyGroup.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            jspPropertyGroup.icon = icon;
        }
        if (urlPattern != null) {
            jspPropertyGroup.urlPattern = urlPattern;
        }
        if (includePrelude != null) {
            jspPropertyGroup.includePrelude = includePrelude;
        }
        if (includeCoda != null) {
            jspPropertyGroup.includeCoda = includeCoda;
        }

        context.afterUnmarshal(jspPropertyGroup, LifecycleCallback.NONE);

        return jspPropertyGroup;
    }

    public final JspPropertyGroup read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, JspPropertyGroup jspPropertyGroup, RuntimeContext context)
            throws Exception {
        if (jspPropertyGroup == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (JspPropertyGroup.class != jspPropertyGroup.getClass()) {
            context.unexpectedSubclass(writer, jspPropertyGroup, JspPropertyGroup.class);
            return;
        }

        context.beforeMarshal(jspPropertyGroup, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = jspPropertyGroup.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(jspPropertyGroup, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = jspPropertyGroup.getDescriptions();
        } catch (Exception e) {
            context.getterError(jspPropertyGroup, "descriptions", JspPropertyGroup.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(jspPropertyGroup, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = jspPropertyGroup.getDisplayNames();
        } catch (Exception e) {
            context.getterError(jspPropertyGroup, "displayNames", JspPropertyGroup.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(jspPropertyGroup, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = jspPropertyGroup.icon;
        if (icon != null) {
            for (Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(jspPropertyGroup, "icon");
                }
            }
        }

        // ELEMENT: urlPattern
        List<String> urlPatternRaw = jspPropertyGroup.urlPattern;
        if (urlPatternRaw != null) {
            for (String urlPatternItem : urlPatternRaw) {
                String urlPattern = null;
                try {
                    urlPattern = Adapters.trimStringAdapterAdapter.marshal(urlPatternItem);
                } catch (Exception e) {
                    context.xmlAdapterError(jspPropertyGroup, "urlPattern", TrimStringAdapter.class, List.class, List.class, e);
                }
                if (urlPattern != null) {
                    writer.writeStartElement(prefix, "url-pattern", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(urlPattern);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(jspPropertyGroup, "urlPattern");
                }
            }
        }

        // ELEMENT: elIgnored
        Boolean elIgnored = jspPropertyGroup.elIgnored;
        if (elIgnored != null) {
            writer.writeStartElement(prefix, "el-ignored", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(elIgnored));
            writer.writeEndElement();
        }

        // ELEMENT: pageEncoding
        String pageEncodingRaw = jspPropertyGroup.pageEncoding;
        String pageEncoding = null;
        try {
            pageEncoding = Adapters.collapsedStringAdapterAdapter.marshal(pageEncodingRaw);
        } catch (Exception e) {
            context.xmlAdapterError(jspPropertyGroup, "pageEncoding", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (pageEncoding != null) {
            writer.writeStartElement(prefix, "page-encoding", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(pageEncoding);
            writer.writeEndElement();
        }

        // ELEMENT: scriptingInvalid
        Boolean scriptingInvalid = jspPropertyGroup.scriptingInvalid;
        if (scriptingInvalid != null) {
            writer.writeStartElement(prefix, "scripting-invalid", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(scriptingInvalid));
            writer.writeEndElement();
        }

        // ELEMENT: isXml
        Boolean isXml = jspPropertyGroup.isXml;
        if (isXml != null) {
            writer.writeStartElement(prefix, "is-xml", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(isXml));
            writer.writeEndElement();
        }

        // ELEMENT: includePrelude
        List<String> includePreludeRaw = jspPropertyGroup.includePrelude;
        if (includePreludeRaw != null) {
            for (String includePreludeItem : includePreludeRaw) {
                String includePrelude = null;
                try {
                    includePrelude = Adapters.collapsedStringAdapterAdapter.marshal(includePreludeItem);
                } catch (Exception e) {
                    context.xmlAdapterError(jspPropertyGroup, "includePrelude", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (includePrelude != null) {
                    writer.writeStartElement(prefix, "include-prelude", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(includePrelude);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: includeCoda
        List<String> includeCodaRaw = jspPropertyGroup.includeCoda;
        if (includeCodaRaw != null) {
            for (String includeCodaItem : includeCodaRaw) {
                String includeCoda = null;
                try {
                    includeCoda = Adapters.collapsedStringAdapterAdapter.marshal(includeCodaItem);
                } catch (Exception e) {
                    context.xmlAdapterError(jspPropertyGroup, "includeCoda", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (includeCoda != null) {
                    writer.writeStartElement(prefix, "include-coda", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(includeCoda);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: deferredSyntaxAllowedAsLiteral
        Boolean deferredSyntaxAllowedAsLiteral = jspPropertyGroup.deferredSyntaxAllowedAsLiteral;
        if (deferredSyntaxAllowedAsLiteral != null) {
            writer.writeStartElement(prefix, "deferred-syntax-allowed-as-literal", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(deferredSyntaxAllowedAsLiteral));
            writer.writeEndElement();
        }

        // ELEMENT: trimDirectiveWhitespaces
        Boolean trimDirectiveWhitespaces = jspPropertyGroup.trimDirectiveWhitespaces;
        if (trimDirectiveWhitespaces != null) {
            writer.writeStartElement(prefix, "trim-directive-whitespaces", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(trimDirectiveWhitespaces));
            writer.writeEndElement();
        }

        // ELEMENT: defaultContentType
        String defaultContentTypeRaw = jspPropertyGroup.defaultContentType;
        String defaultContentType = null;
        try {
            defaultContentType = Adapters.collapsedStringAdapterAdapter.marshal(defaultContentTypeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(jspPropertyGroup, "defaultContentType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (defaultContentType != null) {
            writer.writeStartElement(prefix, "default-content-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(defaultContentType);
            writer.writeEndElement();
        }

        // ELEMENT: buffer
        String bufferRaw = jspPropertyGroup.buffer;
        String buffer = null;
        try {
            buffer = Adapters.collapsedStringAdapterAdapter.marshal(bufferRaw);
        } catch (Exception e) {
            context.xmlAdapterError(jspPropertyGroup, "buffer", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (buffer != null) {
            writer.writeStartElement(prefix, "buffer", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(buffer);
            writer.writeEndElement();
        }

        // ELEMENT: errorOnUndeclaredNamespace
        Boolean errorOnUndeclaredNamespace = jspPropertyGroup.errorOnUndeclaredNamespace;
        if (errorOnUndeclaredNamespace != null) {
            writer.writeStartElement(prefix, "error-on-undeclared-namespace", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(errorOnUndeclaredNamespace));
            writer.writeEndElement();
        }

        context.afterMarshal(jspPropertyGroup, LifecycleCallback.NONE);
    }

}
