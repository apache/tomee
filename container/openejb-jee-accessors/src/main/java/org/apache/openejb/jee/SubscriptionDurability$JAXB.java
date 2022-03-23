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
public class SubscriptionDurability$JAXB
    extends JAXBObject<SubscriptionDurability> {


    public SubscriptionDurability$JAXB() {
        super(SubscriptionDurability.class, new QName("http://java.sun.com/xml/ns/javaee".intern(), "subscription-durability".intern()), null);
    }

    public static SubscriptionDurability readSubscriptionDurability(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeSubscriptionDurability(final XoXMLStreamWriter writer, final SubscriptionDurability subscriptionDurability, final RuntimeContext context)
        throws Exception {
        _write(writer, subscriptionDurability, context);
    }

    public void write(final XoXMLStreamWriter writer, final SubscriptionDurability subscriptionDurability, final RuntimeContext context)
        throws Exception {
        _write(writer, subscriptionDurability, context);
    }

    public final static SubscriptionDurability _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final SubscriptionDurability subscriptionDurability = new SubscriptionDurability();
        context.beforeUnmarshal(subscriptionDurability, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            return context.unexpectedXsiType(reader, SubscriptionDurability.class);
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, subscriptionDurability);
                subscriptionDurability.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // VALUE: value
        final String valueRaw = reader.getElementText();

        String value = null;
        boolean valueConverted;
        try {
            value = Adapters.collapsedStringAdapterAdapter.unmarshal(valueRaw);
            valueConverted = true;
        } catch (final Exception e) {
            context.xmlAdapterError(reader, CollapsedStringAdapter.class, String.class, String.class, e);
            valueConverted = false;
        }

        if (valueConverted) {
            subscriptionDurability.value = value;
        }

        context.afterUnmarshal(subscriptionDurability, LifecycleCallback.NONE);

        return subscriptionDurability;
    }

    public final SubscriptionDurability read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final SubscriptionDurability subscriptionDurability, RuntimeContext context)
        throws Exception {
        if (subscriptionDurability == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (SubscriptionDurability.class != subscriptionDurability.getClass()) {
            context.unexpectedSubclass(writer, subscriptionDurability, SubscriptionDurability.class);
            return;
        }

        context.beforeMarshal(subscriptionDurability, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = subscriptionDurability.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(subscriptionDurability, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // VALUE: value
        final String valueRaw = subscriptionDurability.value;
        String value = null;
        try {
            value = Adapters.collapsedStringAdapterAdapter.marshal(valueRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(subscriptionDurability, "value", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        writer.writeCharacters(value);

        context.afterMarshal(subscriptionDurability, LifecycleCallback.NONE);
    }

}
