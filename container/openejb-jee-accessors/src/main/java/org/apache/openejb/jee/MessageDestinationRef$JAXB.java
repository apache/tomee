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
import static org.apache.openejb.jee.MessageDestinationUsage$JAXB.parseMessageDestinationUsage;
import static org.apache.openejb.jee.MessageDestinationUsage$JAXB.toStringMessageDestinationUsage;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class MessageDestinationRef$JAXB
    extends JAXBObject<MessageDestinationRef> {


    public MessageDestinationRef$JAXB() {
        super(MessageDestinationRef.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "message-destination-refType".intern()), Text$JAXB.class, MessageDestinationUsage$JAXB.class, InjectionTarget$JAXB.class);
    }

    public static MessageDestinationRef readMessageDestinationRef(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeMessageDestinationRef(final XoXMLStreamWriter writer, final MessageDestinationRef messageDestinationRef, final RuntimeContext context)
        throws Exception {
        _write(writer, messageDestinationRef, context);
    }

    public void write(final XoXMLStreamWriter writer, final MessageDestinationRef messageDestinationRef, final RuntimeContext context)
        throws Exception {
        _write(writer, messageDestinationRef, context);
    }

    public final static MessageDestinationRef _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final MessageDestinationRef messageDestinationRef = new MessageDestinationRef();
        context.beforeUnmarshal(messageDestinationRef, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        Set<InjectionTarget> injectionTarget = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("message-destination-refType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, MessageDestinationRef.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, messageDestinationRef);
                messageDestinationRef.id = id;
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
            } else if (("message-destination-ref-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageDestinationRefName
                final String messageDestinationRefNameRaw = elementReader.getElementAsString();

                final String messageDestinationRefName;
                try {
                    messageDestinationRefName = Adapters.collapsedStringAdapterAdapter.unmarshal(messageDestinationRefNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                messageDestinationRef.messageDestinationRefName = messageDestinationRefName;
            } else if (("message-destination-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageDestinationType
                final String messageDestinationTypeRaw = elementReader.getElementAsString();

                final String messageDestinationType;
                try {
                    messageDestinationType = Adapters.collapsedStringAdapterAdapter.unmarshal(messageDestinationTypeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                messageDestinationRef.messageDestinationType = messageDestinationType;
            } else if (("message-destination-usage" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageDestinationUsage
                final MessageDestinationUsage messageDestinationUsage = parseMessageDestinationUsage(elementReader, context, elementReader.getElementAsString());
                if (messageDestinationUsage != null) {
                    messageDestinationRef.messageDestinationUsage = messageDestinationUsage;
                }
            } else if (("message-destination-link" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageDestinationLink
                final String messageDestinationLinkRaw = elementReader.getElementAsString();

                final String messageDestinationLink;
                try {
                    messageDestinationLink = Adapters.collapsedStringAdapterAdapter.unmarshal(messageDestinationLinkRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                messageDestinationRef.messageDestinationLink = messageDestinationLink;
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

                messageDestinationRef.mappedName = mappedName;
            } else if (("injection-target" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: injectionTarget
                final InjectionTarget injectionTargetItem = readInjectionTarget(elementReader, context);
                if (injectionTarget == null) {
                    injectionTarget = messageDestinationRef.injectionTarget;
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

                messageDestinationRef.lookupName = lookupName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination-ref-name"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination-type"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination-usage"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination-link"), new QName("http://java.sun.com/xml/ns/javaee", "mapped-name"), new QName("http://java.sun.com/xml/ns/javaee", "injection-target"), new QName("http://java.sun.com/xml/ns/javaee", "lookup-name"));
            }
        }
        if (descriptions != null) {
            try {
                messageDestinationRef.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, MessageDestinationRef.class, "setDescriptions", Text[].class, e);
            }
        }
        if (injectionTarget != null) {
            messageDestinationRef.injectionTarget = injectionTarget;
        }

        context.afterUnmarshal(messageDestinationRef, LifecycleCallback.NONE);

        return messageDestinationRef;
    }

    public final MessageDestinationRef read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final MessageDestinationRef messageDestinationRef, RuntimeContext context)
        throws Exception {
        if (messageDestinationRef == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (MessageDestinationRef.class != messageDestinationRef.getClass()) {
            context.unexpectedSubclass(writer, messageDestinationRef, MessageDestinationRef.class);
            return;
        }

        context.beforeMarshal(messageDestinationRef, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = messageDestinationRef.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(messageDestinationRef, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = messageDestinationRef.getDescriptions();
        } catch (final Exception e) {
            context.getterError(messageDestinationRef, "descriptions", MessageDestinationRef.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDestinationRef, "descriptions");
                }
            }
        }

        // ELEMENT: messageDestinationRefName
        final String messageDestinationRefNameRaw = messageDestinationRef.messageDestinationRefName;
        String messageDestinationRefName = null;
        try {
            messageDestinationRefName = Adapters.collapsedStringAdapterAdapter.marshal(messageDestinationRefNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(messageDestinationRef, "messageDestinationRefName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (messageDestinationRefName != null) {
            writer.writeStartElement(prefix, "message-destination-ref-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(messageDestinationRefName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(messageDestinationRef, "messageDestinationRefName");
        }

        // ELEMENT: messageDestinationType
        final String messageDestinationTypeRaw = messageDestinationRef.messageDestinationType;
        String messageDestinationType = null;
        try {
            messageDestinationType = Adapters.collapsedStringAdapterAdapter.marshal(messageDestinationTypeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(messageDestinationRef, "messageDestinationType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (messageDestinationType != null) {
            writer.writeStartElement(prefix, "message-destination-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(messageDestinationType);
            writer.writeEndElement();
        }

        // ELEMENT: messageDestinationUsage
        final MessageDestinationUsage messageDestinationUsage = messageDestinationRef.messageDestinationUsage;
        if (messageDestinationUsage != null) {
            writer.writeStartElement(prefix, "message-destination-usage", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringMessageDestinationUsage(messageDestinationRef, null, context, messageDestinationUsage));
            writer.writeEndElement();
        }

        // ELEMENT: messageDestinationLink
        final String messageDestinationLinkRaw = messageDestinationRef.messageDestinationLink;
        String messageDestinationLink = null;
        try {
            messageDestinationLink = Adapters.collapsedStringAdapterAdapter.marshal(messageDestinationLinkRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(messageDestinationRef, "messageDestinationLink", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (messageDestinationLink != null) {
            writer.writeStartElement(prefix, "message-destination-link", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(messageDestinationLink);
            writer.writeEndElement();
        }

        // ELEMENT: mappedName
        final String mappedNameRaw = messageDestinationRef.mappedName;
        String mappedName = null;
        try {
            mappedName = Adapters.collapsedStringAdapterAdapter.marshal(mappedNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(messageDestinationRef, "mappedName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (mappedName != null) {
            writer.writeStartElement(prefix, "mapped-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(mappedName);
            writer.writeEndElement();
        }

        // ELEMENT: injectionTarget
        final Set<InjectionTarget> injectionTarget = messageDestinationRef.injectionTarget;
        if (injectionTarget != null) {
            for (final InjectionTarget injectionTargetItem : injectionTarget) {
                if (injectionTargetItem != null) {
                    writer.writeStartElement(prefix, "injection-target", "http://java.sun.com/xml/ns/javaee");
                    writeInjectionTarget(writer, injectionTargetItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDestinationRef, "injectionTarget");
                }
            }
        }

        // ELEMENT: lookupName
        final String lookupNameRaw = messageDestinationRef.lookupName;
        String lookupName = null;
        try {
            lookupName = Adapters.collapsedStringAdapterAdapter.marshal(lookupNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(messageDestinationRef, "lookupName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (lookupName != null) {
            writer.writeStartElement(prefix, "lookup-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(lookupName);
            writer.writeEndElement();
        }

        context.afterMarshal(messageDestinationRef, LifecycleCallback.NONE);
    }

}
