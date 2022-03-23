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

import static org.apache.openejb.jee.BodyContent$JAXB.parseBodyContent;
import static org.apache.openejb.jee.BodyContent$JAXB.toStringBodyContent;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;
import static org.apache.openejb.jee.TldAttribute$JAXB.readTldAttribute;
import static org.apache.openejb.jee.TldAttribute$JAXB.writeTldAttribute;
import static org.apache.openejb.jee.TldExtension$JAXB.readTldExtension;
import static org.apache.openejb.jee.TldExtension$JAXB.writeTldExtension;
import static org.apache.openejb.jee.Variable$JAXB.readVariable;
import static org.apache.openejb.jee.Variable$JAXB.writeVariable;

@SuppressWarnings({
    "StringEquality"
})
public class Tag$JAXB
    extends JAXBObject<Tag> {


    public Tag$JAXB() {
        super(Tag.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "tagType".intern()), Text$JAXB.class, Icon$JAXB.class, BodyContent$JAXB.class, Variable$JAXB.class, TldAttribute$JAXB.class, TldExtension$JAXB.class);
    }

    public static Tag readTag(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeTag(final XoXMLStreamWriter writer, final Tag tag, final RuntimeContext context)
        throws Exception {
        _write(writer, tag, context);
    }

    public void write(final XoXMLStreamWriter writer, final Tag tag, final RuntimeContext context)
        throws Exception {
        _write(writer, tag, context);
    }

    public final static Tag _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final Tag tag = new Tag();
        context.beforeUnmarshal(tag, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<Variable> variable = null;
        List<TldAttribute> attribute1 = null;
        List<TldExtension> tagExtension = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("tagType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Tag.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, tag);
                tag.id = id;
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
                    icon = tag.icon;
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

                tag.name = name;
            } else if (("tag-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: tagClass
                final String tagClassRaw = elementReader.getElementAsString();

                final String tagClass;
                try {
                    tagClass = Adapters.collapsedStringAdapterAdapter.unmarshal(tagClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                tag.tagClass = tagClass;
            } else if (("tei-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: teiClass
                final String teiClassRaw = elementReader.getElementAsString();

                final String teiClass;
                try {
                    teiClass = Adapters.collapsedStringAdapterAdapter.unmarshal(teiClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                tag.teiClass = teiClass;
            } else if (("body-content" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: bodyContent
                final BodyContent bodyContent = parseBodyContent(elementReader, context, elementReader.getElementAsString());
                if (bodyContent != null) {
                    tag.bodyContent = bodyContent;
                }
            } else if (("variable" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: variable
                final Variable variableItem = readVariable(elementReader, context);
                if (variable == null) {
                    variable = tag.variable;
                    if (variable != null) {
                        variable.clear();
                    } else {
                        variable = new ArrayList<Variable>();
                    }
                }
                variable.add(variableItem);
            } else if (("attribute" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: attribute
                final TldAttribute attributeItem = readTldAttribute(elementReader, context);
                if (attribute1 == null) {
                    attribute1 = tag.attribute;
                    if (attribute1 != null) {
                        attribute1.clear();
                    } else {
                        attribute1 = new ArrayList<TldAttribute>();
                    }
                }
                attribute1.add(attributeItem);
            } else if (("dynamic-attributes" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: dynamicAttributes
                final String dynamicAttributesRaw = elementReader.getElementAsString();

                final String dynamicAttributes;
                try {
                    dynamicAttributes = Adapters.collapsedStringAdapterAdapter.unmarshal(dynamicAttributesRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                tag.dynamicAttributes = dynamicAttributes;
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

                tag.example = example;
            } else if (("tag-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: tagExtension
                final TldExtension tagExtensionItem = readTldExtension(elementReader, context);
                if (tagExtension == null) {
                    tagExtension = tag.tagExtension;
                    if (tagExtension != null) {
                        tagExtension.clear();
                    } else {
                        tagExtension = new ArrayList<TldExtension>();
                    }
                }
                tagExtension.add(tagExtensionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "name"), new QName("http://java.sun.com/xml/ns/javaee", "tag-class"), new QName("http://java.sun.com/xml/ns/javaee", "tei-class"), new QName("http://java.sun.com/xml/ns/javaee", "body-content"), new QName("http://java.sun.com/xml/ns/javaee", "variable"), new QName("http://java.sun.com/xml/ns/javaee", "attribute"), new QName("http://java.sun.com/xml/ns/javaee", "dynamic-attributes"), new QName("http://java.sun.com/xml/ns/javaee", "example"), new QName("http://java.sun.com/xml/ns/javaee", "tag-extension"));
            }
        }
        if (descriptions != null) {
            try {
                tag.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, Tag.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                tag.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, Tag.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            tag.icon = icon;
        }
        if (variable != null) {
            tag.variable = variable;
        }
        if (attribute1 != null) {
            tag.attribute = attribute1;
        }
        if (tagExtension != null) {
            tag.tagExtension = tagExtension;
        }

        context.afterUnmarshal(tag, LifecycleCallback.NONE);

        return tag;
    }

    public final Tag read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final Tag tag, RuntimeContext context)
        throws Exception {
        if (tag == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Tag.class != tag.getClass()) {
            context.unexpectedSubclass(writer, tag, Tag.class);
            return;
        }

        context.beforeMarshal(tag, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = tag.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(tag, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = tag.getDescriptions();
        } catch (final Exception e) {
            context.getterError(tag, "descriptions", Tag.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(tag, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = tag.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(tag, "displayNames", Tag.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(tag, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = tag.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(tag, "icon");
                }
            }
        }

        // ELEMENT: name
        final String nameRaw = tag.name;
        String name = null;
        try {
            name = Adapters.collapsedStringAdapterAdapter.marshal(nameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(tag, "name", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (name != null) {
            writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(name);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(tag, "name");
        }

        // ELEMENT: tagClass
        final String tagClassRaw = tag.tagClass;
        String tagClass = null;
        try {
            tagClass = Adapters.collapsedStringAdapterAdapter.marshal(tagClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(tag, "tagClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (tagClass != null) {
            writer.writeStartElement(prefix, "tag-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(tagClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(tag, "tagClass");
        }

        // ELEMENT: teiClass
        final String teiClassRaw = tag.teiClass;
        String teiClass = null;
        try {
            teiClass = Adapters.collapsedStringAdapterAdapter.marshal(teiClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(tag, "teiClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (teiClass != null) {
            writer.writeStartElement(prefix, "tei-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(teiClass);
            writer.writeEndElement();
        }

        // ELEMENT: bodyContent
        final BodyContent bodyContent = tag.bodyContent;
        if (bodyContent != null) {
            writer.writeStartElement(prefix, "body-content", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringBodyContent(tag, null, context, bodyContent));
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(tag, "bodyContent");
        }

        // ELEMENT: variable
        final List<Variable> variable = tag.variable;
        if (variable != null) {
            for (final Variable variableItem : variable) {
                writer.writeStartElement(prefix, "variable", "http://java.sun.com/xml/ns/javaee");
                if (variableItem != null) {
                    writeVariable(writer, variableItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: attribute
        final List<TldAttribute> attribute = tag.attribute;
        if (attribute != null) {
            for (final TldAttribute attributeItem : attribute) {
                writer.writeStartElement(prefix, "attribute", "http://java.sun.com/xml/ns/javaee");
                if (attributeItem != null) {
                    writeTldAttribute(writer, attributeItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: dynamicAttributes
        final String dynamicAttributesRaw = tag.dynamicAttributes;
        String dynamicAttributes = null;
        try {
            dynamicAttributes = Adapters.collapsedStringAdapterAdapter.marshal(dynamicAttributesRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(tag, "dynamicAttributes", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (dynamicAttributes != null) {
            writer.writeStartElement(prefix, "dynamic-attributes", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(dynamicAttributes);
            writer.writeEndElement();
        }

        // ELEMENT: example
        final String exampleRaw = tag.example;
        String example = null;
        try {
            example = Adapters.collapsedStringAdapterAdapter.marshal(exampleRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(tag, "example", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (example != null) {
            writer.writeStartElement(prefix, "example", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(example);
            writer.writeEndElement();
        }

        // ELEMENT: tagExtension
        final List<TldExtension> tagExtension = tag.tagExtension;
        if (tagExtension != null) {
            for (final TldExtension tagExtensionItem : tagExtension) {
                if (tagExtensionItem != null) {
                    writer.writeStartElement(prefix, "tag-extension", "http://java.sun.com/xml/ns/javaee");
                    writeTldExtension(writer, tagExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(tag, LifecycleCallback.NONE);
    }

}
