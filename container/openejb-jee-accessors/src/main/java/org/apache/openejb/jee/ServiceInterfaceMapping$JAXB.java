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


import static org.apache.openejb.jee.PortMapping$JAXB.readPortMapping;
import static org.apache.openejb.jee.PortMapping$JAXB.writePortMapping;

@SuppressWarnings({
    "StringEquality"
})
public class ServiceInterfaceMapping$JAXB
    extends JAXBObject<ServiceInterfaceMapping>
{


    public ServiceInterfaceMapping$JAXB() {
        super(ServiceInterfaceMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "service-interface-mappingType".intern()), PortMapping$JAXB.class);
    }

    public static ServiceInterfaceMapping readServiceInterfaceMapping(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeServiceInterfaceMapping(XoXMLStreamWriter writer, ServiceInterfaceMapping serviceInterfaceMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, serviceInterfaceMapping, context);
    }

    public void write(XoXMLStreamWriter writer, ServiceInterfaceMapping serviceInterfaceMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, serviceInterfaceMapping, context);
    }

    public static final ServiceInterfaceMapping _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ServiceInterfaceMapping serviceInterfaceMapping = new ServiceInterfaceMapping();
        context.beforeUnmarshal(serviceInterfaceMapping, LifecycleCallback.NONE);

        List<PortMapping> portMapping = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("service-interface-mappingType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ServiceInterfaceMapping.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, serviceInterfaceMapping);
                serviceInterfaceMapping.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("service-interface" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceInterface
                String serviceInterfaceRaw = elementReader.getElementText();

                String serviceInterface;
                try {
                    serviceInterface = Adapters.collapsedStringAdapterAdapter.unmarshal(serviceInterfaceRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                serviceInterfaceMapping.serviceInterface = serviceInterface;
            } else if (("wsdl-service-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: wsdlServiceName
                QName wsdlServiceName = elementReader.getElementAsQName();
                serviceInterfaceMapping.wsdlServiceName = wsdlServiceName;
            } else if (("port-mapping" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: portMapping
                PortMapping portMappingItem = readPortMapping(elementReader, context);
                if (portMapping == null) {
                    portMapping = serviceInterfaceMapping.portMapping;
                    if (portMapping!= null) {
                        portMapping.clear();
                    } else {
                        portMapping = new ArrayList<>();
                    }
                }
                portMapping.add(portMappingItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "service-interface"), new QName("http://java.sun.com/xml/ns/javaee", "wsdl-service-name"), new QName("http://java.sun.com/xml/ns/javaee", "port-mapping"));
            }
        }
        if (portMapping!= null) {
            serviceInterfaceMapping.portMapping = portMapping;
        }

        context.afterUnmarshal(serviceInterfaceMapping, LifecycleCallback.NONE);

        return serviceInterfaceMapping;
    }

    public final ServiceInterfaceMapping read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, ServiceInterfaceMapping serviceInterfaceMapping, RuntimeContext context)
        throws Exception
    {
        if (serviceInterfaceMapping == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ServiceInterfaceMapping.class!= serviceInterfaceMapping.getClass()) {
            context.unexpectedSubclass(writer, serviceInterfaceMapping, ServiceInterfaceMapping.class);
            return ;
        }

        context.beforeMarshal(serviceInterfaceMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = serviceInterfaceMapping.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(serviceInterfaceMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: serviceInterface
        String serviceInterfaceRaw = serviceInterfaceMapping.serviceInterface;
        String serviceInterface = null;
        try {
            serviceInterface = Adapters.collapsedStringAdapterAdapter.marshal(serviceInterfaceRaw);
        } catch (Exception e) {
            context.xmlAdapterError(serviceInterfaceMapping, "serviceInterface", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (serviceInterface!= null) {
            writer.writeStartElement(prefix, "service-interface", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(serviceInterface);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(serviceInterfaceMapping, "serviceInterface");
        }

        // ELEMENT: wsdlServiceName
        QName wsdlServiceName = serviceInterfaceMapping.wsdlServiceName;
        if (wsdlServiceName!= null) {
            writer.writeStartElement(prefix, "wsdl-service-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeQName(wsdlServiceName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(serviceInterfaceMapping, "wsdlServiceName");
        }

        // ELEMENT: portMapping
        List<PortMapping> portMapping = serviceInterfaceMapping.portMapping;
        if (portMapping!= null) {
            for (PortMapping portMappingItem: portMapping) {
                if (portMappingItem!= null) {
                    writer.writeStartElement(prefix, "port-mapping", "http://java.sun.com/xml/ns/javaee");
                    writePortMapping(writer, portMappingItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(serviceInterfaceMapping, LifecycleCallback.NONE);
    }

}
