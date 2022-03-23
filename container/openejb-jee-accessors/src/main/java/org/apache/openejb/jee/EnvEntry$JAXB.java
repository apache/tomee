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
import java.util.LinkedHashSet;
import java.util.Set;

import static org.apache.openejb.jee.InjectionTarget$JAXB.readInjectionTarget;
import static org.apache.openejb.jee.InjectionTarget$JAXB.writeInjectionTarget;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class EnvEntry$JAXB
    extends JAXBObject<EnvEntry> {


    public EnvEntry$JAXB() {
        super(EnvEntry.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "env-entryType".intern()), Text$JAXB.class, InjectionTarget$JAXB.class);
    }

    public static EnvEntry readEnvEntry(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeEnvEntry(final XoXMLStreamWriter writer, final EnvEntry envEntry, final RuntimeContext context)
        throws Exception {
        _write(writer, envEntry, context);
    }

    public void write(final XoXMLStreamWriter writer, final EnvEntry envEntry, final RuntimeContext context)
        throws Exception {
        _write(writer, envEntry, context);
    }

    public final static EnvEntry _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final EnvEntry envEntry = new EnvEntry();
        context.beforeUnmarshal(envEntry, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        Set<InjectionTarget> injectionTarget = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("env-entryType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, EnvEntry.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, envEntry);
                envEntry.id = id;
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
            } else if (("env-entry-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: envEntryName
                final String envEntryNameRaw = elementReader.getElementAsString();

                final String envEntryName;
                try {
                    envEntryName = Adapters.collapsedStringAdapterAdapter.unmarshal(envEntryNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                envEntry.envEntryName = envEntryName;
            } else if (("env-entry-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: envEntryType
                final String envEntryTypeRaw = elementReader.getElementAsString();

                final String envEntryType;
                try {
                    envEntryType = Adapters.collapsedStringAdapterAdapter.unmarshal(envEntryTypeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                envEntry.envEntryType = envEntryType;
            } else if (("env-entry-value" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: envEntryValue
                final String envEntryValueRaw = elementReader.getElementText(); // don't trim

                final String envEntryValue;
                try {
                    envEntryValue = Adapters.stringAdapterAdapter.unmarshal(envEntryValueRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, StringAdapter.class, String.class, String.class, e);
                    continue;
                }

                envEntry.envEntryValue = envEntryValue;
            } else if (("mapped-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: mappedName
                final String mappedNameRaw = elementReader.getElementAsString();

                final String mappedName;
                try {
                    mappedName = Adapters.collapsedStringAdapterAdapter.unmarshal(mappedNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                envEntry.mappedName = mappedName;
            } else if (("injection-target" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: injectionTarget
                final InjectionTarget injectionTargetItem = readInjectionTarget(elementReader, context);
                if (injectionTarget == null) {
                    injectionTarget = envEntry.injectionTarget;
                    if (injectionTarget != null) {
                        injectionTarget.clear();
                    } else {
                        injectionTarget = new LinkedHashSet<InjectionTarget>();
                    }
                }
                injectionTarget.add(injectionTargetItem);
            } else if (("lookup-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: lookupName
                final String lookupNameRaw = elementReader.getElementAsString();

                final String lookupName;
                try {
                    lookupName = Adapters.collapsedStringAdapterAdapter.unmarshal(lookupNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                envEntry.lookupName = lookupName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "env-entry-name"), new QName("http://java.sun.com/xml/ns/javaee", "env-entry-type"), new QName("http://java.sun.com/xml/ns/javaee", "env-entry-value"), new QName("http://java.sun.com/xml/ns/javaee", "mapped-name"), new QName("http://java.sun.com/xml/ns/javaee", "injection-target"), new QName("http://java.sun.com/xml/ns/javaee", "lookup-name"));
            }
        }
        if (descriptions != null) {
            try {
                envEntry.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, EnvEntry.class, "setDescriptions", Text[].class, e);
            }
        }
        if (injectionTarget != null) {
            envEntry.injectionTarget = injectionTarget;
        }

        context.afterUnmarshal(envEntry, LifecycleCallback.NONE);

        return envEntry;
    }

    public final EnvEntry read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final EnvEntry envEntry, RuntimeContext context)
        throws Exception {
        if (envEntry == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (EnvEntry.class != envEntry.getClass()) {
            context.unexpectedSubclass(writer, envEntry, EnvEntry.class);
            return;
        }

        context.beforeMarshal(envEntry, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = envEntry.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(envEntry, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = envEntry.getDescriptions();
        } catch (final Exception e) {
            context.getterError(envEntry, "descriptions", EnvEntry.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(envEntry, "descriptions");
                }
            }
        }

        // ELEMENT: envEntryName
        final String envEntryNameRaw = envEntry.envEntryName;
        String envEntryName = null;
        try {
            envEntryName = Adapters.collapsedStringAdapterAdapter.marshal(envEntryNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(envEntry, "envEntryName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (envEntryName != null) {
            writer.writeStartElement(prefix, "env-entry-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(envEntryName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(envEntry, "envEntryName");
        }

        // ELEMENT: envEntryType
        final String envEntryTypeRaw = envEntry.envEntryType;
        String envEntryType = null;
        try {
            envEntryType = Adapters.collapsedStringAdapterAdapter.marshal(envEntryTypeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(envEntry, "envEntryType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (envEntryType != null) {
            writer.writeStartElement(prefix, "env-entry-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(envEntryType);
            writer.writeEndElement();
        }

        // ELEMENT: envEntryValue
        final String envEntryValueRaw = envEntry.envEntryValue;
        String envEntryValue = null;
        try {
            envEntryValue = Adapters.stringAdapterAdapter.marshal(envEntryValueRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(envEntry, "envEntryValue", StringAdapter.class, String.class, String.class, e);
        }
        if (envEntryValue != null) {
            writer.writeStartElement(prefix, "env-entry-value", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(envEntryValue);
            writer.writeEndElement();
        }

        // ELEMENT: mappedName
        final String mappedNameRaw = envEntry.mappedName;
        String mappedName = null;
        try {
            mappedName = Adapters.collapsedStringAdapterAdapter.marshal(mappedNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(envEntry, "mappedName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (mappedName != null) {
            writer.writeStartElement(prefix, "mapped-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(mappedName);
            writer.writeEndElement();
        }

        // ELEMENT: injectionTarget
        final Set<InjectionTarget> injectionTarget = envEntry.injectionTarget;
        if (injectionTarget != null) {
            for (final InjectionTarget injectionTargetItem : injectionTarget) {
                if (injectionTargetItem != null) {
                    writer.writeStartElement(prefix, "injection-target", "http://java.sun.com/xml/ns/javaee");
                    writeInjectionTarget(writer, injectionTargetItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(envEntry, "injectionTarget");
                }
            }
        }

        // ELEMENT: lookupName
        final String lookupNameRaw = envEntry.lookupName;
        String lookupName = null;
        try {
            lookupName = Adapters.collapsedStringAdapterAdapter.marshal(lookupNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(envEntry, "lookupName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (lookupName != null) {
            writer.writeStartElement(prefix, "lookup-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(lookupName);
            writer.writeEndElement();
        }

        context.afterMarshal(envEntry, LifecycleCallback.NONE);
    }

}
