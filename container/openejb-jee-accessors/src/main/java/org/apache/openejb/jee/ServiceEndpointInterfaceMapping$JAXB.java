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


import static org.apache.openejb.jee.ServiceEndpointMethodMapping$JAXB.readServiceEndpointMethodMapping;
import static org.apache.openejb.jee.ServiceEndpointMethodMapping$JAXB.writeServiceEndpointMethodMapping;

@SuppressWarnings({
    "StringEquality"
})
public class ServiceEndpointInterfaceMapping$JAXB
    extends JAXBObject<ServiceEndpointInterfaceMapping>
{


    public ServiceEndpointInterfaceMapping$JAXB() {
        super(ServiceEndpointInterfaceMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "service-endpoint-interface-mappingType".intern()), ServiceEndpointMethodMapping$JAXB.class);
    }

    public static ServiceEndpointInterfaceMapping readServiceEndpointInterfaceMapping(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeServiceEndpointInterfaceMapping(XoXMLStreamWriter writer, ServiceEndpointInterfaceMapping serviceEndpointInterfaceMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, serviceEndpointInterfaceMapping, context);
    }

    public void write(XoXMLStreamWriter writer, ServiceEndpointInterfaceMapping serviceEndpointInterfaceMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, serviceEndpointInterfaceMapping, context);
    }

    public static final ServiceEndpointInterfaceMapping _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ServiceEndpointInterfaceMapping serviceEndpointInterfaceMapping = new ServiceEndpointInterfaceMapping();
        context.beforeUnmarshal(serviceEndpointInterfaceMapping, LifecycleCallback.NONE);

        List<ServiceEndpointMethodMapping> serviceEndpointMethodMapping = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("service-endpoint-interface-mappingType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ServiceEndpointInterfaceMapping.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, serviceEndpointInterfaceMapping);
                serviceEndpointInterfaceMapping.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("service-endpoint-interface" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceEndpointInterface
                String serviceEndpointInterfaceRaw = elementReader.getElementText();

                String serviceEndpointInterface;
                try {
                    serviceEndpointInterface = Adapters.collapsedStringAdapterAdapter.unmarshal(serviceEndpointInterfaceRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                serviceEndpointInterfaceMapping.serviceEndpointInterface = serviceEndpointInterface;
            } else if (("wsdl-port-type" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: wsdlPortType
                QName wsdlPortType = elementReader.getElementAsQName();
                serviceEndpointInterfaceMapping.wsdlPortType = wsdlPortType;
            } else if (("wsdl-binding" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: wsdlBinding
                QName wsdlBinding = elementReader.getElementAsQName();
                serviceEndpointInterfaceMapping.wsdlBinding = wsdlBinding;
            } else if (("service-endpoint-method-mapping" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceEndpointMethodMapping
                ServiceEndpointMethodMapping serviceEndpointMethodMappingItem = readServiceEndpointMethodMapping(elementReader, context);
                if (serviceEndpointMethodMapping == null) {
                    serviceEndpointMethodMapping = serviceEndpointInterfaceMapping.serviceEndpointMethodMapping;
                    if (serviceEndpointMethodMapping!= null) {
                        serviceEndpointMethodMapping.clear();
                    } else {
                        serviceEndpointMethodMapping = new ArrayList<>();
                    }
                }
                serviceEndpointMethodMapping.add(serviceEndpointMethodMappingItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "service-endpoint-interface"), new QName("http://java.sun.com/xml/ns/javaee", "wsdl-port-type"), new QName("http://java.sun.com/xml/ns/javaee", "wsdl-binding"), new QName("http://java.sun.com/xml/ns/javaee", "service-endpoint-method-mapping"));
            }
        }
        if (serviceEndpointMethodMapping!= null) {
            serviceEndpointInterfaceMapping.serviceEndpointMethodMapping = serviceEndpointMethodMapping;
        }

        context.afterUnmarshal(serviceEndpointInterfaceMapping, LifecycleCallback.NONE);

        return serviceEndpointInterfaceMapping;
    }

    public final ServiceEndpointInterfaceMapping read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, ServiceEndpointInterfaceMapping serviceEndpointInterfaceMapping, RuntimeContext context)
        throws Exception
    {
        if (serviceEndpointInterfaceMapping == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ServiceEndpointInterfaceMapping.class!= serviceEndpointInterfaceMapping.getClass()) {
            context.unexpectedSubclass(writer, serviceEndpointInterfaceMapping, ServiceEndpointInterfaceMapping.class);
            return ;
        }

        context.beforeMarshal(serviceEndpointInterfaceMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = serviceEndpointInterfaceMapping.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(serviceEndpointInterfaceMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: serviceEndpointInterface
        String serviceEndpointInterfaceRaw = serviceEndpointInterfaceMapping.serviceEndpointInterface;
        String serviceEndpointInterface = null;
        try {
            serviceEndpointInterface = Adapters.collapsedStringAdapterAdapter.marshal(serviceEndpointInterfaceRaw);
        } catch (Exception e) {
            context.xmlAdapterError(serviceEndpointInterfaceMapping, "serviceEndpointInterface", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (serviceEndpointInterface!= null) {
            writer.writeStartElement(prefix, "service-endpoint-interface", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(serviceEndpointInterface);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(serviceEndpointInterfaceMapping, "serviceEndpointInterface");
        }

        // ELEMENT: wsdlPortType
        QName wsdlPortType = serviceEndpointInterfaceMapping.wsdlPortType;
        if (wsdlPortType!= null) {
            writer.writeStartElement(prefix, "wsdl-port-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeQName(wsdlPortType);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(serviceEndpointInterfaceMapping, "wsdlPortType");
        }

        // ELEMENT: wsdlBinding
        QName wsdlBinding = serviceEndpointInterfaceMapping.wsdlBinding;
        if (wsdlBinding!= null) {
            writer.writeStartElement(prefix, "wsdl-binding", "http://java.sun.com/xml/ns/javaee");
            writer.writeQName(wsdlBinding);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(serviceEndpointInterfaceMapping, "wsdlBinding");
        }

        // ELEMENT: serviceEndpointMethodMapping
        List<ServiceEndpointMethodMapping> serviceEndpointMethodMapping = serviceEndpointInterfaceMapping.serviceEndpointMethodMapping;
        if (serviceEndpointMethodMapping!= null) {
            for (ServiceEndpointMethodMapping serviceEndpointMethodMappingItem: serviceEndpointMethodMapping) {
                if (serviceEndpointMethodMappingItem!= null) {
                    writer.writeStartElement(prefix, "service-endpoint-method-mapping", "http://java.sun.com/xml/ns/javaee");
                    writeServiceEndpointMethodMapping(writer, serviceEndpointMethodMappingItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(serviceEndpointInterfaceMapping, LifecycleCallback.NONE);
    }

}
