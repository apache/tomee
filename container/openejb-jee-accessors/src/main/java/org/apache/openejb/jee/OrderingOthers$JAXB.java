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

@SuppressWarnings({
    "StringEquality"
})
public class OrderingOthers$JAXB
    extends JAXBObject<OrderingOthers> {


    public OrderingOthers$JAXB() {
        super(OrderingOthers.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "ordering-othersType".intern()));
    }

    public static OrderingOthers readOrderingOthers(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeOrderingOthers(final XoXMLStreamWriter writer, final OrderingOthers orderingOthers, final RuntimeContext context)
        throws Exception {
        _write(writer, orderingOthers, context);
    }

    public void write(final XoXMLStreamWriter writer, final OrderingOthers orderingOthers, final RuntimeContext context)
        throws Exception {
        _write(writer, orderingOthers, context);
    }

    public final static OrderingOthers _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final OrderingOthers orderingOthers = new OrderingOthers();
        context.beforeUnmarshal(orderingOthers, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("ordering-othersType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, OrderingOthers.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, orderingOthers);
                orderingOthers.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            context.unexpectedElement(elementReader);
        }

        context.afterUnmarshal(orderingOthers, LifecycleCallback.NONE);

        return orderingOthers;
    }

    public final OrderingOthers read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final OrderingOthers orderingOthers, RuntimeContext context)
        throws Exception {
        if (orderingOthers == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (OrderingOthers.class != orderingOthers.getClass()) {
            context.unexpectedSubclass(writer, orderingOthers, OrderingOthers.class);
            return;
        }

        context.beforeMarshal(orderingOthers, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = orderingOthers.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(orderingOthers, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        context.afterMarshal(orderingOthers, LifecycleCallback.NONE);
    }

}
