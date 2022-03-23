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
public class Function$JAXB
    extends JAXBObject<Function> {


    public Function$JAXB() {
        super(Function.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "functionType".intern()), Text$JAXB.class, Icon$JAXB.class, TldExtension$JAXB.class);
    }

    public static Function readFunction(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFunction(final XoXMLStreamWriter writer, final Function function, final RuntimeContext context)
        throws Exception {
        _write(writer, function, context);
    }

    public void write(final XoXMLStreamWriter writer, final Function function, final RuntimeContext context)
        throws Exception {
        _write(writer, function, context);
    }

    public final static Function _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final Function function = new Function();
        context.beforeUnmarshal(function, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<TldExtension> functionExtension = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("functionType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Function.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, function);
                function.id = id;
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
                    icon = function.icon;
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

                function.name = name;
            } else if (("function-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: functionClass
                final String functionClassRaw = elementReader.getElementAsString();

                final String functionClass;
                try {
                    functionClass = Adapters.collapsedStringAdapterAdapter.unmarshal(functionClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                function.functionClass = functionClass;
            } else if (("function-signature" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: functionSignature
                final String functionSignatureRaw = elementReader.getElementAsString();

                final String functionSignature;
                try {
                    functionSignature = Adapters.collapsedStringAdapterAdapter.unmarshal(functionSignatureRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                function.functionSignature = functionSignature;
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

                function.example = example;
            } else if (("function-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: functionExtension
                final TldExtension functionExtensionItem = readTldExtension(elementReader, context);
                if (functionExtension == null) {
                    functionExtension = function.functionExtension;
                    if (functionExtension != null) {
                        functionExtension.clear();
                    } else {
                        functionExtension = new ArrayList<TldExtension>();
                    }
                }
                functionExtension.add(functionExtensionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "name"), new QName("http://java.sun.com/xml/ns/javaee", "function-class"), new QName("http://java.sun.com/xml/ns/javaee", "function-signature"), new QName("http://java.sun.com/xml/ns/javaee", "example"), new QName("http://java.sun.com/xml/ns/javaee", "function-extension"));
            }
        }
        if (descriptions != null) {
            try {
                function.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, Function.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                function.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, Function.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            function.icon = icon;
        }
        if (functionExtension != null) {
            function.functionExtension = functionExtension;
        }

        context.afterUnmarshal(function, LifecycleCallback.NONE);

        return function;
    }

    public final Function read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final Function function, RuntimeContext context)
        throws Exception {
        if (function == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Function.class != function.getClass()) {
            context.unexpectedSubclass(writer, function, Function.class);
            return;
        }

        context.beforeMarshal(function, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = function.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(function, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = function.getDescriptions();
        } catch (final Exception e) {
            context.getterError(function, "descriptions", Function.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(function, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = function.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(function, "displayNames", Function.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(function, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = function.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(function, "icon");
                }
            }
        }

        // ELEMENT: name
        final String nameRaw = function.name;
        String name = null;
        try {
            name = Adapters.collapsedStringAdapterAdapter.marshal(nameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(function, "name", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (name != null) {
            writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(name);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(function, "name");
        }

        // ELEMENT: functionClass
        final String functionClassRaw = function.functionClass;
        String functionClass = null;
        try {
            functionClass = Adapters.collapsedStringAdapterAdapter.marshal(functionClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(function, "functionClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (functionClass != null) {
            writer.writeStartElement(prefix, "function-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(functionClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(function, "functionClass");
        }

        // ELEMENT: functionSignature
        final String functionSignatureRaw = function.functionSignature;
        String functionSignature = null;
        try {
            functionSignature = Adapters.collapsedStringAdapterAdapter.marshal(functionSignatureRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(function, "functionSignature", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (functionSignature != null) {
            writer.writeStartElement(prefix, "function-signature", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(functionSignature);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(function, "functionSignature");
        }

        // ELEMENT: example
        final String exampleRaw = function.example;
        String example = null;
        try {
            example = Adapters.collapsedStringAdapterAdapter.marshal(exampleRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(function, "example", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (example != null) {
            writer.writeStartElement(prefix, "example", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(example);
            writer.writeEndElement();
        }

        // ELEMENT: functionExtension
        final List<TldExtension> functionExtension = function.functionExtension;
        if (functionExtension != null) {
            for (final TldExtension functionExtensionItem : functionExtension) {
                if (functionExtensionItem != null) {
                    writer.writeStartElement(prefix, "function-extension", "http://java.sun.com/xml/ns/javaee");
                    writeTldExtension(writer, functionExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(function, LifecycleCallback.NONE);
    }

}
