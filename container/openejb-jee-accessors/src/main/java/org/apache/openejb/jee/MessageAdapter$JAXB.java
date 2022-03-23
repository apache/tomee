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

import static org.apache.openejb.jee.MessageListener$JAXB.readMessageListener;
import static org.apache.openejb.jee.MessageListener$JAXB.writeMessageListener;

@SuppressWarnings({
    "StringEquality"
})
public class MessageAdapter$JAXB
    extends JAXBObject<MessageAdapter> {


    public MessageAdapter$JAXB() {
        super(MessageAdapter.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "messageadapterType".intern()), MessageListener$JAXB.class);
    }

    public static MessageAdapter readMessageAdapter(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeMessageAdapter(final XoXMLStreamWriter writer, final MessageAdapter messageAdapter, final RuntimeContext context)
        throws Exception {
        _write(writer, messageAdapter, context);
    }

    public void write(final XoXMLStreamWriter writer, final MessageAdapter messageAdapter, final RuntimeContext context)
        throws Exception {
        _write(writer, messageAdapter, context);
    }

    public final static MessageAdapter _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final MessageAdapter messageAdapter = new MessageAdapter();
        context.beforeUnmarshal(messageAdapter, LifecycleCallback.NONE);

        List<MessageListener> messageListener = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("messageadapterType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, MessageAdapter.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, messageAdapter);
                messageAdapter.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("messagelistener" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageListener
                final MessageListener messageListenerItem = readMessageListener(elementReader, context);
                if (messageListener == null) {
                    messageListener = messageAdapter.messageListener;
                    if (messageListener != null) {
                        messageListener.clear();
                    } else {
                        messageListener = new ArrayList<MessageListener>();
                    }
                }
                messageListener.add(messageListenerItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "messagelistener"));
            }
        }
        if (messageListener != null) {
            messageAdapter.messageListener = messageListener;
        }

        context.afterUnmarshal(messageAdapter, LifecycleCallback.NONE);

        return messageAdapter;
    }

    public final MessageAdapter read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final MessageAdapter messageAdapter, RuntimeContext context)
        throws Exception {
        if (messageAdapter == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (MessageAdapter.class != messageAdapter.getClass()) {
            context.unexpectedSubclass(writer, messageAdapter, MessageAdapter.class);
            return;
        }

        context.beforeMarshal(messageAdapter, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = messageAdapter.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(messageAdapter, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: messageListener
        final List<MessageListener> messageListener = messageAdapter.messageListener;
        if (messageListener != null) {
            for (final MessageListener messageListenerItem : messageListener) {
                if (messageListenerItem != null) {
                    writer.writeStartElementWithAutoPrefix("http://java.sun.com/xml/ns/javaee", "messagelistener");
                    writeMessageListener(writer, messageListenerItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageAdapter, "messageListener");
                }
            }
        }

        context.afterMarshal(messageAdapter, LifecycleCallback.NONE);
    }

}
