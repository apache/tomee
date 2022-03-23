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

@SuppressWarnings({
    "StringEquality"
})
public class AdminObject$JAXB
    extends JAXBObject<AdminObject> {


    public AdminObject$JAXB() {
        super(AdminObject.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "adminobjectType".intern()), ConfigProperty$JAXB.class);
    }

    public static AdminObject readAdminObject(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeAdminObject(final XoXMLStreamWriter writer, final AdminObject adminObject, final RuntimeContext context)
        throws Exception {
        _write(writer, adminObject, context);
    }

    public void write(final XoXMLStreamWriter writer, final AdminObject adminObject, final RuntimeContext context)
        throws Exception {
        _write(writer, adminObject, context);
    }

    public final static AdminObject _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final AdminObject adminObject = new AdminObject();
        context.beforeUnmarshal(adminObject, LifecycleCallback.NONE);

        List<ConfigProperty> configProperty = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("adminobjectType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, AdminObject.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, adminObject);
                adminObject.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("adminobject-interface" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: adminObjectInterface
                final String adminObjectInterfaceRaw = elementReader.getElementAsString();

                final String adminObjectInterface;
                try {
                    adminObjectInterface = Adapters.collapsedStringAdapterAdapter.unmarshal(adminObjectInterfaceRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                adminObject.adminObjectInterface = adminObjectInterface;
            } else if (("adminobject-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: adminObjectClass
                final String adminObjectClassRaw = elementReader.getElementAsString();

                final String adminObjectClass;
                try {
                    adminObjectClass = Adapters.collapsedStringAdapterAdapter.unmarshal(adminObjectClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                adminObject.adminObjectClass = adminObjectClass;
            } else if (("config-property" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: configProperty
                final ConfigProperty configPropertyItem = readConfigProperty(elementReader, context);
                if (configProperty == null) {
                    configProperty = adminObject.configProperty;
                    if (configProperty != null) {
                        configProperty.clear();
                    } else {
                        configProperty = new ArrayList<ConfigProperty>();
                    }
                }
                configProperty.add(configPropertyItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "adminobject-interface"), new QName("http://java.sun.com/xml/ns/javaee", "adminobject-class"), new QName("http://java.sun.com/xml/ns/javaee", "config-property"));
            }
        }
        if (configProperty != null) {
            adminObject.configProperty = configProperty;
        }

        context.afterUnmarshal(adminObject, LifecycleCallback.NONE);

        return adminObject;
    }

    public final AdminObject read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final AdminObject adminObject, RuntimeContext context)
        throws Exception {
        if (adminObject == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (AdminObject.class != adminObject.getClass()) {
            context.unexpectedSubclass(writer, adminObject, AdminObject.class);
            return;
        }

        context.beforeMarshal(adminObject, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = adminObject.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(adminObject, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: adminObjectInterface
        final String adminObjectInterfaceRaw = adminObject.adminObjectInterface;
        String adminObjectInterface = null;
        try {
            adminObjectInterface = Adapters.collapsedStringAdapterAdapter.marshal(adminObjectInterfaceRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(adminObject, "adminObjectInterface", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (adminObjectInterface != null) {
            writer.writeStartElement(prefix, "adminobject-interface", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(adminObjectInterface);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(adminObject, "adminObjectInterface");
        }

        // ELEMENT: adminObjectClass
        final String adminObjectClassRaw = adminObject.adminObjectClass;
        String adminObjectClass = null;
        try {
            adminObjectClass = Adapters.collapsedStringAdapterAdapter.marshal(adminObjectClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(adminObject, "adminObjectClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (adminObjectClass != null) {
            writer.writeStartElement(prefix, "adminobject-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(adminObjectClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(adminObject, "adminObjectClass");
        }

        // ELEMENT: configProperty
        final List<ConfigProperty> configProperty = adminObject.configProperty;
        if (configProperty != null) {
            for (final ConfigProperty configPropertyItem : configProperty) {
                if (configPropertyItem != null) {
                    writer.writeStartElement(prefix, "config-property", "http://java.sun.com/xml/ns/javaee");
                    writeConfigProperty(writer, configPropertyItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(adminObject, LifecycleCallback.NONE);
    }

}
