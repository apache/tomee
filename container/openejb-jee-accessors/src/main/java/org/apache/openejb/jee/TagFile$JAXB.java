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
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;
import static org.apache.openejb.jee.TldExtension$JAXB.readTldExtension;
import static org.apache.openejb.jee.TldExtension$JAXB.writeTldExtension;

@SuppressWarnings({
    "StringEquality"
})
public class TagFile$JAXB
    extends JAXBObject<TagFile> {


    public TagFile$JAXB() {
        super(TagFile.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "tagFileType".intern()), Text$JAXB.class, Icon$JAXB.class, TldExtension$JAXB.class);
    }

    public static TagFile readTagFile(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeTagFile(final XoXMLStreamWriter writer, final TagFile tagFile, final RuntimeContext context)
        throws Exception {
        _write(writer, tagFile, context);
    }

    public void write(final XoXMLStreamWriter writer, final TagFile tagFile, final RuntimeContext context)
        throws Exception {
        _write(writer, tagFile, context);
    }

    public final static TagFile _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final TagFile tagFile = new TagFile();
        context.beforeUnmarshal(tagFile, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<TldExtension> tagExtension = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("tagFileType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, TagFile.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, tagFile);
                tagFile.id = id;
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
                    icon = tagFile.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: name
                final String nameRaw = elementReader.getElementAsString();

                final String name;
                try {
                    name = Adapters.collapsedStringAdapterAdapter.unmarshal(nameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                tagFile.name = name;
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

                tagFile.path = path;
            } else if (("example" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: example
                final String exampleRaw = elementReader.getElementAsString();

                final String example;
                try {
                    example = Adapters.collapsedStringAdapterAdapter.unmarshal(exampleRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                tagFile.example = example;
            } else if (("tag-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: tagExtension
                final TldExtension tagExtensionItem = readTldExtension(elementReader, context);
                if (tagExtension == null) {
                    tagExtension = tagFile.tagExtension;
                    if (tagExtension != null) {
                        tagExtension.clear();
                    } else {
                        tagExtension = new ArrayList<TldExtension>();
                    }
                }
                tagExtension.add(tagExtensionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "name"), new QName("http://java.sun.com/xml/ns/javaee", "path"), new QName("http://java.sun.com/xml/ns/javaee", "example"), new QName("http://java.sun.com/xml/ns/javaee", "tag-extension"));
            }
        }
        if (descriptions != null) {
            try {
                tagFile.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, TagFile.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                tagFile.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, TagFile.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            tagFile.icon = icon;
        }
        if (tagExtension != null) {
            tagFile.tagExtension = tagExtension;
        }

        context.afterUnmarshal(tagFile, LifecycleCallback.NONE);

        return tagFile;
    }

    public final TagFile read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final TagFile tagFile, RuntimeContext context)
        throws Exception {
        if (tagFile == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (TagFile.class != tagFile.getClass()) {
            context.unexpectedSubclass(writer, tagFile, TagFile.class);
            return;
        }

        context.beforeMarshal(tagFile, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = tagFile.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(tagFile, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = tagFile.getDescriptions();
        } catch (final Exception e) {
            context.getterError(tagFile, "descriptions", TagFile.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(tagFile, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = tagFile.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(tagFile, "displayNames", TagFile.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(tagFile, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = tagFile.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(tagFile, "icon");
                }
            }
        }

        // ELEMENT: name
        final String nameRaw = tagFile.name;
        String name = null;
        try {
            name = Adapters.collapsedStringAdapterAdapter.marshal(nameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(tagFile, "name", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (name != null) {
            writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(name);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(tagFile, "name");
        }

        // ELEMENT: path
        final String pathRaw = tagFile.path;
        String path = null;
        try {
            path = Adapters.collapsedStringAdapterAdapter.marshal(pathRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(tagFile, "path", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (path != null) {
            writer.writeStartElement(prefix, "path", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(path);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(tagFile, "path");
        }

        // ELEMENT: example
        final String exampleRaw = tagFile.example;
        String example = null;
        try {
            example = Adapters.collapsedStringAdapterAdapter.marshal(exampleRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(tagFile, "example", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (example != null) {
            writer.writeStartElement(prefix, "example", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(example);
            writer.writeEndElement();
        }

        // ELEMENT: tagExtension
        final List<TldExtension> tagExtension = tagFile.tagExtension;
        if (tagExtension != null) {
            for (final TldExtension tagExtensionItem : tagExtension) {
                if (tagExtensionItem != null) {
                    writer.writeStartElement(prefix, "tag-extension", "http://java.sun.com/xml/ns/javaee");
                    writeTldExtension(writer, tagExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(tagFile, LifecycleCallback.NONE);
    }

}
