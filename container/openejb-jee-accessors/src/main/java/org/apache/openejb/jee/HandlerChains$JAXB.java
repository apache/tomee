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

import static org.apache.openejb.jee.HandlerChain$JAXB.readHandlerChain;
import static org.apache.openejb.jee.HandlerChain$JAXB.writeHandlerChain;

@SuppressWarnings({
    "StringEquality"
})
public class HandlerChains$JAXB
    extends JAXBObject<HandlerChains> {

    public HandlerChains$JAXB() {
        super(HandlerChains.class, new QName("http://java.sun.com/xml/ns/javaee".intern(), "handler-chains".intern()), new QName("http://java.sun.com/xml/ns/javaee".intern(), "handler-chainsType".intern()), HandlerChain$JAXB.class);
    }

    public static HandlerChains readHandlerChains(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeHandlerChains(final XoXMLStreamWriter writer, final HandlerChains handlerChains, final RuntimeContext context)
        throws Exception {
        _write(writer, handlerChains, context);
    }

    public void write(final XoXMLStreamWriter writer, final HandlerChains handlerChains, final RuntimeContext context)
        throws Exception {
        _write(writer, handlerChains, context);
    }

    public final static HandlerChains _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final HandlerChains handlerChains = new HandlerChains();
        context.beforeUnmarshal(handlerChains, LifecycleCallback.NONE);

        List<HandlerChain> handlerChain = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("handler-chainsType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, HandlerChains.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, handlerChains);
                handlerChains.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("handler-chain" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: handlerChain
                final HandlerChain handlerChainItem = readHandlerChain(elementReader, context);
                if (handlerChain == null) {
                    handlerChain = handlerChains.handlerChain;
                    if (handlerChain != null) {
                        handlerChain.clear();
                    } else {
                        handlerChain = new ArrayList<HandlerChain>();
                    }
                }
                handlerChain.add(handlerChainItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "handler-chain"));
            }
        }
        if (handlerChain != null) {
            handlerChains.handlerChain = handlerChain;
        }

        context.afterUnmarshal(handlerChains, LifecycleCallback.NONE);

        return handlerChains;
    }

    public final HandlerChains read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final HandlerChains handlerChains, RuntimeContext context)
        throws Exception {
        if (handlerChains == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (HandlerChains.class != handlerChains.getClass()) {
            context.unexpectedSubclass(writer, handlerChains, HandlerChains.class);
            return;
        }

        context.beforeMarshal(handlerChains, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = handlerChains.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(handlerChains, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: handlerChain
        final List<HandlerChain> handlerChain = handlerChains.handlerChain;
        if (handlerChain != null) {
            for (final HandlerChain handlerChainItem : handlerChain) {
                if (handlerChainItem != null) {
                    writer.writeStartElementWithAutoPrefix("http://java.sun.com/xml/ns/javaee", "handler-chain");
                    writeHandlerChain(writer, handlerChainItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(handlerChains, LifecycleCallback.NONE);
    }
}
