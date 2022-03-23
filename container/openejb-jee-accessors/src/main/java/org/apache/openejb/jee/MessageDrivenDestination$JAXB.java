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

import static org.apache.openejb.jee.DestinationType$JAXB.readDestinationType;
import static org.apache.openejb.jee.DestinationType$JAXB.writeDestinationType;
import static org.apache.openejb.jee.SubscriptionDurability$JAXB.readSubscriptionDurability;
import static org.apache.openejb.jee.SubscriptionDurability$JAXB.writeSubscriptionDurability;

@SuppressWarnings({
    "StringEquality"
})
public class MessageDrivenDestination$JAXB
    extends JAXBObject<MessageDrivenDestination> {


    public MessageDrivenDestination$JAXB() {
        super(MessageDrivenDestination.class, new QName("http://java.sun.com/xml/ns/javaee".intern(), "message-driven-destination".intern()), null, DestinationType$JAXB.class, SubscriptionDurability$JAXB.class);
    }

    public static MessageDrivenDestination readMessageDrivenDestination(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeMessageDrivenDestination(final XoXMLStreamWriter writer, final MessageDrivenDestination messageDrivenDestination, final RuntimeContext context)
        throws Exception {
        _write(writer, messageDrivenDestination, context);
    }

    public void write(final XoXMLStreamWriter writer, final MessageDrivenDestination messageDrivenDestination, final RuntimeContext context)
        throws Exception {
        _write(writer, messageDrivenDestination, context);
    }

    public final static MessageDrivenDestination _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final MessageDrivenDestination messageDrivenDestination = new MessageDrivenDestination();
        context.beforeUnmarshal(messageDrivenDestination, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            return context.unexpectedXsiType(reader, MessageDrivenDestination.class);
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, messageDrivenDestination);
                messageDrivenDestination.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("destination-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: destinationType
                final DestinationType destinationType = readDestinationType(elementReader, context);
                messageDrivenDestination.destinationType = destinationType;
            } else if (("subscription-durability" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: subscriptionDurability
                final SubscriptionDurability subscriptionDurability = readSubscriptionDurability(elementReader, context);
                messageDrivenDestination.subscriptionDurability = subscriptionDurability;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "destination-type"), new QName("http://java.sun.com/xml/ns/javaee", "subscription-durability"));
            }
        }

        context.afterUnmarshal(messageDrivenDestination, LifecycleCallback.NONE);

        return messageDrivenDestination;
    }

    public final MessageDrivenDestination read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final MessageDrivenDestination messageDrivenDestination, RuntimeContext context)
        throws Exception {
        if (messageDrivenDestination == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (MessageDrivenDestination.class != messageDrivenDestination.getClass()) {
            context.unexpectedSubclass(writer, messageDrivenDestination, MessageDrivenDestination.class);
            return;
        }

        context.beforeMarshal(messageDrivenDestination, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = messageDrivenDestination.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(messageDrivenDestination, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: destinationType
        final DestinationType destinationType = messageDrivenDestination.destinationType;
        if (destinationType != null) {
            writer.writeStartElement(prefix, "destination-type", "http://java.sun.com/xml/ns/javaee");
            writeDestinationType(writer, destinationType, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(messageDrivenDestination, "destinationType");
        }

        // ELEMENT: subscriptionDurability
        final SubscriptionDurability subscriptionDurability = messageDrivenDestination.subscriptionDurability;
        if (subscriptionDurability != null) {
            writer.writeStartElement(prefix, "subscription-durability", "http://java.sun.com/xml/ns/javaee");
            writeSubscriptionDurability(writer, subscriptionDurability, context);
            writer.writeEndElement();
        }

        context.afterMarshal(messageDrivenDestination, LifecycleCallback.NONE);
    }

}
