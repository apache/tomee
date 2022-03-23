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
public class ActivationConfigProperty$JAXB
    extends JAXBObject<ActivationConfigProperty> {


    public ActivationConfigProperty$JAXB() {
        super(ActivationConfigProperty.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "activation-config-propertyType".intern()));
    }

    public static ActivationConfigProperty readActivationConfigProperty(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeActivationConfigProperty(final XoXMLStreamWriter writer, final ActivationConfigProperty activationConfigProperty, final RuntimeContext context)
        throws Exception {
        _write(writer, activationConfigProperty, context);
    }

    public void write(final XoXMLStreamWriter writer, final ActivationConfigProperty activationConfigProperty, final RuntimeContext context)
        throws Exception {
        _write(writer, activationConfigProperty, context);
    }

    public final static ActivationConfigProperty _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final ActivationConfigProperty activationConfigProperty = new ActivationConfigProperty();
        context.beforeUnmarshal(activationConfigProperty, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("activation-config-propertyType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ActivationConfigProperty.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, activationConfigProperty);
                activationConfigProperty.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("activation-config-property-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: activationConfigPropertyName
                final String activationConfigPropertyNameRaw = elementReader.getElementAsString();

                final String activationConfigPropertyName;
                try {
                    activationConfigPropertyName = Adapters.collapsedStringAdapterAdapter.unmarshal(activationConfigPropertyNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                activationConfigProperty.activationConfigPropertyName = activationConfigPropertyName;
            } else if (("activation-config-property-value" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: activationConfigPropertyValue
                final String activationConfigPropertyValueRaw = elementReader.getElementAsString();

                final String activationConfigPropertyValue;
                try {
                    activationConfigPropertyValue = Adapters.collapsedStringAdapterAdapter.unmarshal(activationConfigPropertyValueRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                activationConfigProperty.activationConfigPropertyValue = activationConfigPropertyValue;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "activation-config-property-name"), new QName("http://java.sun.com/xml/ns/javaee", "activation-config-property-value"));
            }
        }

        context.afterUnmarshal(activationConfigProperty, LifecycleCallback.NONE);

        return activationConfigProperty;
    }

    public final ActivationConfigProperty read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final ActivationConfigProperty activationConfigProperty, RuntimeContext context)
        throws Exception {
        if (activationConfigProperty == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ActivationConfigProperty.class != activationConfigProperty.getClass()) {
            context.unexpectedSubclass(writer, activationConfigProperty, ActivationConfigProperty.class);
            return;
        }

        context.beforeMarshal(activationConfigProperty, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = activationConfigProperty.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(activationConfigProperty, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: activationConfigPropertyName
        final String activationConfigPropertyNameRaw = activationConfigProperty.activationConfigPropertyName;
        String activationConfigPropertyName = null;
        try {
            activationConfigPropertyName = Adapters.collapsedStringAdapterAdapter.marshal(activationConfigPropertyNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(activationConfigProperty, "activationConfigPropertyName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (activationConfigPropertyName != null) {
            writer.writeStartElement(prefix, "activation-config-property-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(activationConfigPropertyName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(activationConfigProperty, "activationConfigPropertyName");
        }

        // ELEMENT: activationConfigPropertyValue
        final String activationConfigPropertyValueRaw = activationConfigProperty.activationConfigPropertyValue;
        String activationConfigPropertyValue = null;
        try {
            activationConfigPropertyValue = Adapters.collapsedStringAdapterAdapter.marshal(activationConfigPropertyValueRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(activationConfigProperty, "activationConfigPropertyValue", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (activationConfigPropertyValue != null) {
            writer.writeStartElement(prefix, "activation-config-property-value", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(activationConfigPropertyValue);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(activationConfigProperty, "activationConfigPropertyValue");
        }

        context.afterMarshal(activationConfigProperty, LifecycleCallback.NONE);
    }

}
