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

import static org.apache.openejb.jee.ConfigProperty$JAXB.readConfigProperty;
import static org.apache.openejb.jee.ConfigProperty$JAXB.writeConfigProperty;
import static org.apache.openejb.jee.RequiredConfigProperty$JAXB.readRequiredConfigProperty;
import static org.apache.openejb.jee.RequiredConfigProperty$JAXB.writeRequiredConfigProperty;

@SuppressWarnings({
    "StringEquality"
})
public class ActivationSpec$JAXB
    extends JAXBObject<ActivationSpec> {


    public ActivationSpec$JAXB() {
        super(ActivationSpec.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "activationspecType".intern()), RequiredConfigProperty$JAXB.class, ConfigProperty$JAXB.class);
    }

    public static ActivationSpec readActivationSpec(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeActivationSpec(final XoXMLStreamWriter writer, final ActivationSpec activationSpec, final RuntimeContext context)
        throws Exception {
        _write(writer, activationSpec, context);
    }

    public void write(final XoXMLStreamWriter writer, final ActivationSpec activationSpec, final RuntimeContext context)
        throws Exception {
        _write(writer, activationSpec, context);
    }

    public final static ActivationSpec _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final ActivationSpec activationSpec = new ActivationSpec();
        context.beforeUnmarshal(activationSpec, LifecycleCallback.NONE);

        List<RequiredConfigProperty> requiredConfigProperty = null;
        List<ConfigProperty> configProperty = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("activationspecType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ActivationSpec.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, activationSpec);
                activationSpec.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("activationspec-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: activationSpecClass
                final String activationSpecClassRaw = elementReader.getElementAsString();

                final String activationSpecClass;
                try {
                    activationSpecClass = Adapters.collapsedStringAdapterAdapter.unmarshal(activationSpecClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                activationSpec.activationSpecClass = activationSpecClass;
            } else if (("required-config-property" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: requiredConfigProperty
                final RequiredConfigProperty requiredConfigPropertyItem = readRequiredConfigProperty(elementReader, context);
                if (requiredConfigProperty == null) {
                    requiredConfigProperty = activationSpec.requiredConfigProperty;
                    if (requiredConfigProperty != null) {
                        requiredConfigProperty.clear();
                    } else {
                        requiredConfigProperty = new ArrayList<RequiredConfigProperty>();
                    }
                }
                requiredConfigProperty.add(requiredConfigPropertyItem);
            } else if (("config-property" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: configProperty
                final ConfigProperty configPropertyItem = readConfigProperty(elementReader, context);
                if (configProperty == null) {
                    configProperty = activationSpec.configProperty;
                    if (configProperty != null) {
                        configProperty.clear();
                    } else {
                        configProperty = new ArrayList<ConfigProperty>();
                    }
                }
                configProperty.add(configPropertyItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "activationspec-class"), new QName("http://java.sun.com/xml/ns/javaee", "required-config-property"), new QName("http://java.sun.com/xml/ns/javaee", "config-property"));
            }
        }
        if (requiredConfigProperty != null) {
            activationSpec.requiredConfigProperty = requiredConfigProperty;
        }
        if (configProperty != null) {
            activationSpec.configProperty = configProperty;
        }

        context.afterUnmarshal(activationSpec, LifecycleCallback.NONE);

        return activationSpec;
    }

    public final ActivationSpec read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final ActivationSpec activationSpec, RuntimeContext context)
        throws Exception {
        if (activationSpec == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ActivationSpec.class != activationSpec.getClass()) {
            context.unexpectedSubclass(writer, activationSpec, ActivationSpec.class);
            return;
        }

        context.beforeMarshal(activationSpec, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = activationSpec.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(activationSpec, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: activationSpecClass
        final String activationSpecClassRaw = activationSpec.activationSpecClass;
        String activationSpecClass = null;
        try {
            activationSpecClass = Adapters.collapsedStringAdapterAdapter.marshal(activationSpecClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(activationSpec, "activationSpecClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (activationSpecClass != null) {
            writer.writeStartElement(prefix, "activationspec-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(activationSpecClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(activationSpec, "activationSpecClass");
        }

        // ELEMENT: requiredConfigProperty
        final List<RequiredConfigProperty> requiredConfigProperty = activationSpec.requiredConfigProperty;
        if (requiredConfigProperty != null) {
            for (final RequiredConfigProperty requiredConfigPropertyItem : requiredConfigProperty) {
                if (requiredConfigPropertyItem != null) {
                    writer.writeStartElement(prefix, "required-config-property", "http://java.sun.com/xml/ns/javaee");
                    writeRequiredConfigProperty(writer, requiredConfigPropertyItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: configProperty
        final List<ConfigProperty> configProperty = activationSpec.configProperty;
        if (configProperty != null) {
            for (final ConfigProperty configPropertyItem : configProperty) {
                if (configPropertyItem != null) {
                    writer.writeStartElement(prefix, "config-property", "http://java.sun.com/xml/ns/javaee");
                    writeConfigProperty(writer, configPropertyItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(activationSpec, LifecycleCallback.NONE);
    }

}
