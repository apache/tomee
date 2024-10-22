/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee;

import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.AdminObject$JAXB.readAdminObject;
import static org.apache.openejb.jee.AdminObject$JAXB.writeAdminObject;
import static org.apache.openejb.jee.ConfigProperty$JAXB.readConfigProperty;
import static org.apache.openejb.jee.ConfigProperty$JAXB.writeConfigProperty;
import static org.apache.openejb.jee.InboundResourceadapter$JAXB.readInboundResourceadapter;
import static org.apache.openejb.jee.InboundResourceadapter$JAXB.writeInboundResourceadapter;
import static org.apache.openejb.jee.OutboundResourceAdapter$JAXB.readOutboundResourceAdapter;
import static org.apache.openejb.jee.OutboundResourceAdapter$JAXB.writeOutboundResourceAdapter;
import static org.apache.openejb.jee.SecurityPermission$JAXB.readSecurityPermission;
import static org.apache.openejb.jee.SecurityPermission$JAXB.writeSecurityPermission;

@SuppressWarnings({
    "StringEquality"
})
public class ResourceAdapter$JAXB
    extends JAXBObject<ResourceAdapter>
{


    public ResourceAdapter$JAXB() {
        super(ResourceAdapter.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "resourceadapterType".intern()), ConfigProperty$JAXB.class, OutboundResourceAdapter$JAXB.class, InboundResourceadapter$JAXB.class, AdminObject$JAXB.class, SecurityPermission$JAXB.class);
    }

    public static ResourceAdapter readResourceAdapter(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeResourceAdapter(XoXMLStreamWriter writer, ResourceAdapter resourceAdapter, RuntimeContext context)
        throws Exception
    {
        _write(writer, resourceAdapter, context);
    }

    public void write(XoXMLStreamWriter writer, ResourceAdapter resourceAdapter, RuntimeContext context)
        throws Exception
    {
        _write(writer, resourceAdapter, context);
    }

    public static final ResourceAdapter _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ResourceAdapter resourceAdapter = new ResourceAdapter();
        context.beforeUnmarshal(resourceAdapter, LifecycleCallback.NONE);

        List<ConfigProperty> configProperty = null;
        List<AdminObject> adminObject = null;
        List<SecurityPermission> securityPermission = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("resourceadapterType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ResourceAdapter.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, resourceAdapter);
                resourceAdapter.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("resourceadapter-class" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceAdapterClass
                String resourceAdapterClassRaw = elementReader.getElementText();

                String resourceAdapterClass;
                try {
                    resourceAdapterClass = Adapters.collapsedStringAdapterAdapter.unmarshal(resourceAdapterClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                resourceAdapter.resourceAdapterClass = resourceAdapterClass;
            } else if (("config-property" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: configProperty
                ConfigProperty configPropertyItem = readConfigProperty(elementReader, context);
                if (configProperty == null) {
                    configProperty = resourceAdapter.configProperty;
                    if (configProperty!= null) {
                        configProperty.clear();
                    } else {
                        configProperty = new ArrayList<>();
                    }
                }
                configProperty.add(configPropertyItem);
            } else if (("outbound-resourceadapter" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: outboundResourceAdapter
                OutboundResourceAdapter outboundResourceAdapter = readOutboundResourceAdapter(elementReader, context);
                resourceAdapter.outboundResourceAdapter = outboundResourceAdapter;
            } else if (("inbound-resourceadapter" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: inboundResourceAdapter
                InboundResourceadapter inboundResourceAdapter = readInboundResourceadapter(elementReader, context);
                resourceAdapter.inboundResourceAdapter = inboundResourceAdapter;
            } else if (("adminobject" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: adminObject
                AdminObject adminObjectItem = readAdminObject(elementReader, context);
                if (adminObject == null) {
                    adminObject = resourceAdapter.adminObject;
                    if (adminObject!= null) {
                        adminObject.clear();
                    } else {
                        adminObject = new ArrayList<>();
                    }
                }
                adminObject.add(adminObjectItem);
            } else if (("security-permission" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: securityPermission
                SecurityPermission securityPermissionItem = readSecurityPermission(elementReader, context);
                if (securityPermission == null) {
                    securityPermission = resourceAdapter.securityPermission;
                    if (securityPermission!= null) {
                        securityPermission.clear();
                    } else {
                        securityPermission = new ArrayList<>();
                    }
                }
                securityPermission.add(securityPermissionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "resourceadapter-class"), new QName("http://java.sun.com/xml/ns/javaee", "config-property"), new QName("http://java.sun.com/xml/ns/javaee", "outbound-resourceadapter"), new QName("http://java.sun.com/xml/ns/javaee", "inbound-resourceadapter"), new QName("http://java.sun.com/xml/ns/javaee", "adminobject"), new QName("http://java.sun.com/xml/ns/javaee", "security-permission"));
            }
        }
        if (configProperty!= null) {
            resourceAdapter.configProperty = configProperty;
        }
        if (adminObject!= null) {
            resourceAdapter.adminObject = adminObject;
        }
        if (securityPermission!= null) {
            resourceAdapter.securityPermission = securityPermission;
        }

        context.afterUnmarshal(resourceAdapter, LifecycleCallback.NONE);

        return resourceAdapter;
    }

    public final ResourceAdapter read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, ResourceAdapter resourceAdapter, RuntimeContext context)
        throws Exception
    {
        if (resourceAdapter == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ResourceAdapter.class!= resourceAdapter.getClass()) {
            context.unexpectedSubclass(writer, resourceAdapter, ResourceAdapter.class);
            return ;
        }

        context.beforeMarshal(resourceAdapter, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = resourceAdapter.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(resourceAdapter, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: resourceAdapterClass
        String resourceAdapterClassRaw = resourceAdapter.resourceAdapterClass;
        String resourceAdapterClass = null;
        try {
            resourceAdapterClass = Adapters.collapsedStringAdapterAdapter.marshal(resourceAdapterClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(resourceAdapter, "resourceAdapterClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (resourceAdapterClass!= null) {
            writer.writeStartElement(prefix, "resourceadapter-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(resourceAdapterClass);
            writer.writeEndElement();
        }

        // ELEMENT: configProperty
        List<ConfigProperty> configProperty = resourceAdapter.configProperty;
        if (configProperty!= null) {
            for (ConfigProperty configPropertyItem: configProperty) {
                if (configPropertyItem!= null) {
                    writer.writeStartElement(prefix, "config-property", "http://java.sun.com/xml/ns/javaee");
                    writeConfigProperty(writer, configPropertyItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: outboundResourceAdapter
        OutboundResourceAdapter outboundResourceAdapter = resourceAdapter.outboundResourceAdapter;
        if (outboundResourceAdapter!= null) {
            writer.writeStartElement(prefix, "outbound-resourceadapter", "http://java.sun.com/xml/ns/javaee");
            writeOutboundResourceAdapter(writer, outboundResourceAdapter, context);
            writer.writeEndElement();
        }

        // ELEMENT: inboundResourceAdapter
        InboundResourceadapter inboundResourceAdapter = resourceAdapter.inboundResourceAdapter;
        if (inboundResourceAdapter!= null) {
            writer.writeStartElement(prefix, "inbound-resourceadapter", "http://java.sun.com/xml/ns/javaee");
            writeInboundResourceadapter(writer, inboundResourceAdapter, context);
            writer.writeEndElement();
        }

        // ELEMENT: adminObject
        List<AdminObject> adminObject = resourceAdapter.adminObject;
        if (adminObject!= null) {
            for (AdminObject adminObjectItem: adminObject) {
                if (adminObjectItem!= null) {
                    writer.writeStartElement(prefix, "adminobject", "http://java.sun.com/xml/ns/javaee");
                    writeAdminObject(writer, adminObjectItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: securityPermission
        List<SecurityPermission> securityPermission = resourceAdapter.securityPermission;
        if (securityPermission!= null) {
            for (SecurityPermission securityPermissionItem: securityPermission) {
                if (securityPermissionItem!= null) {
                    writer.writeStartElement(prefix, "security-permission", "http://java.sun.com/xml/ns/javaee");
                    writeSecurityPermission(writer, securityPermissionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(resourceAdapter, LifecycleCallback.NONE);
    }

}
