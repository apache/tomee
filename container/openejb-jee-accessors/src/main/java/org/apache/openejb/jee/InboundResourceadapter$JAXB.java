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

import com.envoisolutions.sxc.jaxb.JAXBObject;
import com.envoisolutions.sxc.jaxb.LifecycleCallback;
import com.envoisolutions.sxc.jaxb.RuntimeContext;
import com.envoisolutions.sxc.util.Attribute;
import com.envoisolutions.sxc.util.XoXMLStreamReader;
import com.envoisolutions.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;

import static org.apache.openejb.jee.MessageAdapter$JAXB.readMessageAdapter;
import static org.apache.openejb.jee.MessageAdapter$JAXB.writeMessageAdapter;

@SuppressWarnings({
        "StringEquality"
})
public class InboundResourceadapter$JAXB
        extends JAXBObject<InboundResourceadapter> {


    public InboundResourceadapter$JAXB() {
        super(InboundResourceadapter.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "inbound-resourceadapterType".intern()), MessageAdapter$JAXB.class);
    }

    public static InboundResourceadapter readInboundResourceadapter(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeInboundResourceadapter(XoXMLStreamWriter writer, InboundResourceadapter inboundResourceadapter, RuntimeContext context)
            throws Exception {
        _write(writer, inboundResourceadapter, context);
    }

    public void write(XoXMLStreamWriter writer, InboundResourceadapter inboundResourceadapter, RuntimeContext context)
            throws Exception {
        _write(writer, inboundResourceadapter, context);
    }

    public final static InboundResourceadapter _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        InboundResourceadapter inboundResourceadapter = new InboundResourceadapter();
        context.beforeUnmarshal(inboundResourceadapter, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("inbound-resourceadapterType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, InboundResourceadapter.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, inboundResourceadapter);
                inboundResourceadapter.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("messageadapter" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageAdapter
                MessageAdapter messageAdapter = readMessageAdapter(elementReader, context);
                inboundResourceadapter.messageAdapter = messageAdapter;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "messageadapter"));
            }
        }

        context.afterUnmarshal(inboundResourceadapter, LifecycleCallback.NONE);

        return inboundResourceadapter;
    }

    public final InboundResourceadapter read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, InboundResourceadapter inboundResourceadapter, RuntimeContext context)
            throws Exception {
        if (inboundResourceadapter == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (InboundResourceadapter.class != inboundResourceadapter.getClass()) {
            context.unexpectedSubclass(writer, inboundResourceadapter, InboundResourceadapter.class);
            return;
        }

        context.beforeMarshal(inboundResourceadapter, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = inboundResourceadapter.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(inboundResourceadapter, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: messageAdapter
        MessageAdapter messageAdapter = inboundResourceadapter.messageAdapter;
        if (messageAdapter != null) {
            writer.writeStartElementWithAutoPrefix("http://java.sun.com/xml/ns/javaee", "messageadapter");
            writeMessageAdapter(writer, messageAdapter, context);
            writer.writeEndElement();
        }

        context.afterMarshal(inboundResourceadapter, LifecycleCallback.NONE);
    }

}
