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

import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class ConfigProperty$JAXB
    extends JAXBObject<ConfigProperty> {


    public ConfigProperty$JAXB() {
        super(ConfigProperty.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "config-propertyType".intern()), Text$JAXB.class);
    }

    public static ConfigProperty readConfigProperty(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeConfigProperty(final XoXMLStreamWriter writer, final ConfigProperty configProperty, final RuntimeContext context)
        throws Exception {
        _write(writer, configProperty, context);
    }

    public void write(final XoXMLStreamWriter writer, final ConfigProperty configProperty, final RuntimeContext context)
        throws Exception {
        _write(writer, configProperty, context);
    }

    public final static ConfigProperty _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final ConfigProperty configProperty = new ConfigProperty();
        context.beforeUnmarshal(configProperty, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("config-propertyType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ConfigProperty.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, configProperty);
                configProperty.id = id;
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
            } else if (("config-property-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: configPropertyName
                final String configPropertyNameRaw = elementReader.getElementAsString();

                final String configPropertyName;
                try {
                    configPropertyName = Adapters.collapsedStringAdapterAdapter.unmarshal(configPropertyNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                configProperty.configPropertyName = configPropertyName;
            } else if (("config-property-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: configPropertyType
                final String configPropertyTypeRaw = elementReader.getElementAsString();

                final String configPropertyType;
                try {
                    configPropertyType = Adapters.collapsedStringAdapterAdapter.unmarshal(configPropertyTypeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                configProperty.configPropertyType = configPropertyType;
            } else if (("config-property-value" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: configPropertyValue
                final String configPropertyValueRaw = elementReader.getElementAsString();

                final String configPropertyValue;
                try {
                    configPropertyValue = Adapters.collapsedStringAdapterAdapter.unmarshal(configPropertyValueRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                configProperty.configPropertyValue = configPropertyValue;
            } else if (("config-property-ignore" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: configPropertyIgnore
                final Boolean configPropertyIgnore = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                configProperty.configPropertyIgnore = configPropertyIgnore;
            } else if (("config-property-supports-dynamic-updates" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: configPropertySupportsDynamicUpdates
                final Boolean configPropertySupportsDynamicUpdates = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                configProperty.configPropertySupportsDynamicUpdates = configPropertySupportsDynamicUpdates;
            } else if (("config-property-confidential" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: configPropertyConfidential
                final Boolean configPropertyConfidential = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                configProperty.configPropertyConfidential = configPropertyConfidential;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "config-property-name"), new QName("http://java.sun.com/xml/ns/javaee", "config-property-type"), new QName("http://java.sun.com/xml/ns/javaee", "config-property-value"), new QName("http://java.sun.com/xml/ns/javaee", "config-property-ignore"), new QName("http://java.sun.com/xml/ns/javaee", "config-property-supports-dynamic-updates"), new QName("http://java.sun.com/xml/ns/javaee", "config-property-confidential"));
            }
        }
        if (descriptions != null) {
            try {
                configProperty.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, ConfigProperty.class, "setDescriptions", Text[].class, e);
            }
        }

        context.afterUnmarshal(configProperty, LifecycleCallback.NONE);

        return configProperty;
    }

    public final ConfigProperty read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final ConfigProperty configProperty, RuntimeContext context)
        throws Exception {
        if (configProperty == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ConfigProperty.class != configProperty.getClass()) {
            context.unexpectedSubclass(writer, configProperty, ConfigProperty.class);
            return;
        }

        context.beforeMarshal(configProperty, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = configProperty.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(configProperty, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = configProperty.getDescriptions();
        } catch (final Exception e) {
            context.getterError(configProperty, "descriptions", ConfigProperty.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(configProperty, "descriptions");
                }
            }
        }

        // ELEMENT: configPropertyName
        final String configPropertyNameRaw = configProperty.configPropertyName;
        String configPropertyName = null;
        try {
            configPropertyName = Adapters.collapsedStringAdapterAdapter.marshal(configPropertyNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(configProperty, "configPropertyName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (configPropertyName != null) {
            writer.writeStartElement(prefix, "config-property-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(configPropertyName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(configProperty, "configPropertyName");
        }

        // ELEMENT: configPropertyType
        final String configPropertyTypeRaw = configProperty.configPropertyType;
        String configPropertyType = null;
        try {
            configPropertyType = Adapters.collapsedStringAdapterAdapter.marshal(configPropertyTypeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(configProperty, "configPropertyType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (configPropertyType != null) {
            writer.writeStartElement(prefix, "config-property-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(configPropertyType);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(configProperty, "configPropertyType");
        }

        // ELEMENT: configPropertyValue
        final String configPropertyValueRaw = configProperty.configPropertyValue;
        String configPropertyValue = null;
        try {
            configPropertyValue = Adapters.collapsedStringAdapterAdapter.marshal(configPropertyValueRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(configProperty, "configPropertyValue", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (configPropertyValue != null) {
            writer.writeStartElement(prefix, "config-property-value", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(configPropertyValue);
            writer.writeEndElement();
        }

        // ELEMENT: configPropertyIgnore
        final Boolean configPropertyIgnore = configProperty.configPropertyIgnore;
        if (configPropertyIgnore != null) {
            writer.writeStartElement(prefix, "config-property-ignore", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(configPropertyIgnore));
            writer.writeEndElement();
        }

        // ELEMENT: configPropertySupportsDynamicUpdates
        final Boolean configPropertySupportsDynamicUpdates = configProperty.configPropertySupportsDynamicUpdates;
        if (configPropertySupportsDynamicUpdates != null) {
            writer.writeStartElement(prefix, "config-property-supports-dynamic-updates", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(configPropertySupportsDynamicUpdates));
            writer.writeEndElement();
        }

        // ELEMENT: configPropertyConfidential
        final Boolean configPropertyConfidential = configProperty.configPropertyConfidential;
        if (configPropertyConfidential != null) {
            writer.writeStartElement(prefix, "config-property-confidential", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(configPropertyConfidential));
            writer.writeEndElement();
        }

        context.afterMarshal(configProperty, LifecycleCallback.NONE);
    }

}
