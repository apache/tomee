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

import static org.apache.openejb.jee.Function$JAXB.readFunction;
import static org.apache.openejb.jee.Function$JAXB.writeFunction;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Listener$JAXB.readListener;
import static org.apache.openejb.jee.Listener$JAXB.writeListener;
import static org.apache.openejb.jee.Tag$JAXB.readTag;
import static org.apache.openejb.jee.Tag$JAXB.writeTag;
import static org.apache.openejb.jee.TagFile$JAXB.readTagFile;
import static org.apache.openejb.jee.TagFile$JAXB.writeTagFile;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;
import static org.apache.openejb.jee.TldExtension$JAXB.readTldExtension;
import static org.apache.openejb.jee.TldExtension$JAXB.writeTldExtension;
import static org.apache.openejb.jee.Validator$JAXB.readValidator;
import static org.apache.openejb.jee.Validator$JAXB.writeValidator;

@SuppressWarnings({
    "StringEquality"
})
public class TldTaglib$JAXB
    extends JAXBObject<TldTaglib> {


    public TldTaglib$JAXB() {
        super(TldTaglib.class, new QName("http://java.sun.com/xml/ns/javaee".intern(), "taglib".intern()), new QName("http://java.sun.com/xml/ns/javaee".intern(), "tldTaglibType".intern()), Text$JAXB.class, Icon$JAXB.class, Validator$JAXB.class, Listener$JAXB.class, Tag$JAXB.class, TagFile$JAXB.class, Function$JAXB.class, TldExtension$JAXB.class);
    }

    public static TldTaglib readTldTaglib(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeTldTaglib(final XoXMLStreamWriter writer, final TldTaglib tldTaglib, final RuntimeContext context)
        throws Exception {
        _write(writer, tldTaglib, context);
    }

    public void write(final XoXMLStreamWriter writer, final TldTaglib tldTaglib, final RuntimeContext context)
        throws Exception {
        _write(writer, tldTaglib, context);
    }

    public final static TldTaglib _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final TldTaglib tldTaglib = new TldTaglib();
        context.beforeUnmarshal(tldTaglib, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<Listener> listener = null;
        List<Tag> tag = null;
        List<TagFile> tagFile = null;
        List<Function> function = null;
        List<TldExtension> taglibExtension = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("tldTaglibType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, TldTaglib.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, tldTaglib);
                tldTaglib.id = id;
            } else if (("version" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: version
                tldTaglib.version = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"), new QName("", "version"));
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
                    icon = tldTaglib.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("tlib-version" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: tlibVersion
                final String tlibVersionRaw = elementReader.getElementAsString();

                final String tlibVersion;
                try {
                    tlibVersion = Adapters.collapsedStringAdapterAdapter.unmarshal(tlibVersionRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                tldTaglib.tlibVersion = tlibVersion;
            } else if (("jsp-version" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: jspVersion
                final String jspVersionRaw = elementReader.getElementAsString();

                final String jspVersion;
                try {
                    jspVersion = Adapters.collapsedStringAdapterAdapter.unmarshal(jspVersionRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                tldTaglib.jspVersion = jspVersion;
            } else if (("short-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: shortName
                final String shortNameRaw = elementReader.getElementAsString();

                final String shortName;
                try {
                    shortName = Adapters.collapsedStringAdapterAdapter.unmarshal(shortNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                tldTaglib.shortName = shortName;
            } else if (("uri" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: uri
                final String uriRaw = elementReader.getElementAsString();

                final String uri;
                try {
                    uri = Adapters.collapsedStringAdapterAdapter.unmarshal(uriRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                tldTaglib.uri = uri;
            } else if (("validator" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: validator
                final Validator validator = readValidator(elementReader, context);
                tldTaglib.validator = validator;
            } else if (("listener" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: listener
                final Listener listenerItem = readListener(elementReader, context);
                if (listener == null) {
                    listener = tldTaglib.listener;
                    if (listener != null) {
                        listener.clear();
                    } else {
                        listener = new ArrayList<Listener>();
                    }
                }
                listener.add(listenerItem);
            } else if (("tag" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: tag
                final Tag tagItem = readTag(elementReader, context);
                if (tag == null) {
                    tag = tldTaglib.tag;
                    if (tag != null) {
                        tag.clear();
                    } else {
                        tag = new ArrayList<Tag>();
                    }
                }
                tag.add(tagItem);
            } else if (("tag-file" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: tagFile
                final TagFile tagFileItem = readTagFile(elementReader, context);
                if (tagFile == null) {
                    tagFile = tldTaglib.tagFile;
                    if (tagFile != null) {
                        tagFile.clear();
                    } else {
                        tagFile = new ArrayList<TagFile>();
                    }
                }
                tagFile.add(tagFileItem);
            } else if (("function" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: function
                final Function functionItem = readFunction(elementReader, context);
                if (function == null) {
                    function = tldTaglib.function;
                    if (function != null) {
                        function.clear();
                    } else {
                        function = new ArrayList<Function>();
                    }
                }
                function.add(functionItem);
            } else if (("taglib-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: taglibExtension
                final TldExtension taglibExtensionItem = readTldExtension(elementReader, context);
                if (taglibExtension == null) {
                    taglibExtension = tldTaglib.taglibExtension;
                    if (taglibExtension != null) {
                        taglibExtension.clear();
                    } else {
                        taglibExtension = new ArrayList<TldExtension>();
                    }
                }
                taglibExtension.add(taglibExtensionItem);
            }/* else { // we are not the one responsible of the failure so don't fail
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "tlib-version"), new QName("http://java.sun.com/xml/ns/javaee", "jsp-version"), new QName("http://java.sun.com/xml/ns/javaee", "short-name"), new QName("http://java.sun.com/xml/ns/javaee", "uri"), new QName("http://java.sun.com/xml/ns/javaee", "validator"), new QName("http://java.sun.com/xml/ns/javaee", "listener"), new QName("http://java.sun.com/xml/ns/javaee", "tag"), new QName("http://java.sun.com/xml/ns/javaee", "tag-file"), new QName("http://java.sun.com/xml/ns/javaee", "function"), new QName("http://java.sun.com/xml/ns/javaee", "taglib-extension"));
            }*/
        }
        if (descriptions != null) {
            try {
                tldTaglib.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, TldTaglib.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                tldTaglib.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, TldTaglib.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            tldTaglib.icon = icon;
        }
        if (listener != null) {
            tldTaglib.listener = listener;
        }
        if (tag != null) {
            tldTaglib.tag = tag;
        }
        if (tagFile != null) {
            tldTaglib.tagFile = tagFile;
        }
        if (function != null) {
            tldTaglib.function = function;
        }
        if (taglibExtension != null) {
            tldTaglib.taglibExtension = taglibExtension;
        }

        context.afterUnmarshal(tldTaglib, LifecycleCallback.NONE);

        return tldTaglib;
    }

    public final TldTaglib read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final TldTaglib tldTaglib, RuntimeContext context)
        throws Exception {
        if (tldTaglib == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (TldTaglib.class != tldTaglib.getClass()) {
            context.unexpectedSubclass(writer, tldTaglib, TldTaglib.class);
            return;
        }

        context.beforeMarshal(tldTaglib, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = tldTaglib.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(tldTaglib, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ATTRIBUTE: version
        final String versionRaw = tldTaglib.version;
        if (versionRaw != null) {
            String version = null;
            try {
                version = Adapters.collapsedStringAdapterAdapter.marshal(versionRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(tldTaglib, "version", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "version", version);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = tldTaglib.getDescriptions();
        } catch (final Exception e) {
            context.getterError(tldTaglib, "descriptions", TldTaglib.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(tldTaglib, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = tldTaglib.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(tldTaglib, "displayNames", TldTaglib.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(tldTaglib, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = tldTaglib.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(tldTaglib, "icon");
                }
            }
        }

        // ELEMENT: tlibVersion
        final String tlibVersionRaw = tldTaglib.tlibVersion;
        String tlibVersion = null;
        try {
            tlibVersion = Adapters.collapsedStringAdapterAdapter.marshal(tlibVersionRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(tldTaglib, "tlibVersion", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (tlibVersion != null) {
            writer.writeStartElement(prefix, "tlib-version", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(tlibVersion);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(tldTaglib, "tlibVersion");
        }

        // ELEMENT: jspVersion
        final String jspVersionRaw = tldTaglib.jspVersion;
        String jspVersion = null;
        try {
            jspVersion = Adapters.collapsedStringAdapterAdapter.marshal(jspVersionRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(tldTaglib, "jspVersion", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (jspVersion != null) {
            writer.writeStartElement(prefix, "jsp-version", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(jspVersion);
            writer.writeEndElement();
        }

        // ELEMENT: shortName
        final String shortNameRaw = tldTaglib.shortName;
        String shortName = null;
        try {
            shortName = Adapters.collapsedStringAdapterAdapter.marshal(shortNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(tldTaglib, "shortName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (shortName != null) {
            writer.writeStartElement(prefix, "short-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(shortName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(tldTaglib, "shortName");
        }

        // ELEMENT: uri
        final String uriRaw = tldTaglib.uri;
        String uri = null;
        try {
            uri = Adapters.collapsedStringAdapterAdapter.marshal(uriRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(tldTaglib, "uri", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (uri != null) {
            writer.writeStartElement(prefix, "uri", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(uri);
            writer.writeEndElement();
        }

        // ELEMENT: validator
        final Validator validator = tldTaglib.validator;
        if (validator != null) {
            writer.writeStartElement(prefix, "validator", "http://java.sun.com/xml/ns/javaee");
            writeValidator(writer, validator, context);
            writer.writeEndElement();
        }

        // ELEMENT: listener
        final List<Listener> listener = tldTaglib.listener;
        if (listener != null) {
            for (final Listener listenerItem : listener) {
                writer.writeStartElement(prefix, "listener", "http://java.sun.com/xml/ns/javaee");
                if (listenerItem != null) {
                    writeListener(writer, listenerItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: tag
        final List<Tag> tag = tldTaglib.tag;
        if (tag != null) {
            for (final Tag tagItem : tag) {
                writer.writeStartElement(prefix, "tag", "http://java.sun.com/xml/ns/javaee");
                if (tagItem != null) {
                    writeTag(writer, tagItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: tagFile
        final List<TagFile> tagFile = tldTaglib.tagFile;
        if (tagFile != null) {
            for (final TagFile tagFileItem : tagFile) {
                if (tagFileItem != null) {
                    writer.writeStartElement(prefix, "tag-file", "http://java.sun.com/xml/ns/javaee");
                    writeTagFile(writer, tagFileItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: function
        final List<Function> function = tldTaglib.function;
        if (function != null) {
            for (final Function functionItem : function) {
                writer.writeStartElement(prefix, "function", "http://java.sun.com/xml/ns/javaee");
                if (functionItem != null) {
                    writeFunction(writer, functionItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: taglibExtension
        final List<TldExtension> taglibExtension = tldTaglib.taglibExtension;
        if (taglibExtension != null) {
            for (final TldExtension taglibExtensionItem : taglibExtension) {
                if (taglibExtensionItem != null) {
                    writer.writeStartElement(prefix, "taglib-extension", "http://java.sun.com/xml/ns/javaee");
                    writeTldExtension(writer, taglibExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(tldTaglib, LifecycleCallback.NONE);
    }

}
