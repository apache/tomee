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

import static org.apache.openejb.jee.ActivationSpec$JAXB.readActivationSpec;
import static org.apache.openejb.jee.ActivationSpec$JAXB.writeActivationSpec;

@SuppressWarnings({
    "StringEquality"
})
public class MessageListener$JAXB
    extends JAXBObject<MessageListener> {


    public MessageListener$JAXB() {
        super(MessageListener.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "messagelistenerType".intern()), ActivationSpec$JAXB.class);
    }

    public static MessageListener readMessageListener(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeMessageListener(final XoXMLStreamWriter writer, final MessageListener messageListener, final RuntimeContext context)
        throws Exception {
        _write(writer, messageListener, context);
    }

    public void write(final XoXMLStreamWriter writer, final MessageListener messageListener, final RuntimeContext context)
        throws Exception {
        _write(writer, messageListener, context);
    }

    public final static MessageListener _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final MessageListener messageListener = new MessageListener();
        context.beforeUnmarshal(messageListener, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("messagelistenerType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, MessageListener.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, messageListener);
                messageListener.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("messagelistener-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageListenerType
                final String messageListenerTypeRaw = elementReader.getElementAsString();

                final String messageListenerType;
                try {
                    messageListenerType = Adapters.collapsedStringAdapterAdapter.unmarshal(messageListenerTypeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                messageListener.messageListenerType = messageListenerType;
            } else if (("activationspec" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: activationSpec
                final ActivationSpec activationSpec = readActivationSpec(elementReader, context);
                messageListener.activationSpec = activationSpec;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "messagelistener-type"), new QName("http://java.sun.com/xml/ns/javaee", "activationspec"));
            }
        }

        context.afterUnmarshal(messageListener, LifecycleCallback.NONE);

        return messageListener;
    }

    public final MessageListener read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final MessageListener messageListener, RuntimeContext context)
        throws Exception {
        if (messageListener == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (MessageListener.class != messageListener.getClass()) {
            context.unexpectedSubclass(writer, messageListener, MessageListener.class);
            return;
        }

        context.beforeMarshal(messageListener, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = messageListener.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(messageListener, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: messageListenerType
        final String messageListenerTypeRaw = messageListener.messageListenerType;
        String messageListenerType = null;
        try {
            messageListenerType = Adapters.collapsedStringAdapterAdapter.marshal(messageListenerTypeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(messageListener, "messageListenerType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (messageListenerType != null) {
            writer.writeStartElement(prefix, "messagelistener-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(messageListenerType);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(messageListener, "messageListenerType");
        }

        // ELEMENT: activationSpec
        final ActivationSpec activationSpec = messageListener.activationSpec;
        if (activationSpec != null) {
            writer.writeStartElement(prefix, "activationspec", "http://java.sun.com/xml/ns/javaee");
            writeActivationSpec(writer, activationSpec, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(messageListener, "activationSpec");
        }

        context.afterMarshal(messageListener, LifecycleCallback.NONE);
    }

}
