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
import org.w3c.dom.Element;


import static org.apache.openejb.jee.MethodParamPartsMapping$JAXB.readMethodParamPartsMapping;
import static org.apache.openejb.jee.MethodParamPartsMapping$JAXB.writeMethodParamPartsMapping;
import static org.apache.openejb.jee.WsdlReturnValueMapping$JAXB.readWsdlReturnValueMapping;
import static org.apache.openejb.jee.WsdlReturnValueMapping$JAXB.writeWsdlReturnValueMapping;

@SuppressWarnings({
    "StringEquality"
})
public class ServiceEndpointMethodMapping$JAXB
    extends JAXBObject<ServiceEndpointMethodMapping>
{


    public ServiceEndpointMethodMapping$JAXB() {
        super(ServiceEndpointMethodMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "service-endpoint-method-mappingType".intern()), MethodParamPartsMapping$JAXB.class, WsdlReturnValueMapping$JAXB.class);
    }

    public static ServiceEndpointMethodMapping readServiceEndpointMethodMapping(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeServiceEndpointMethodMapping(XoXMLStreamWriter writer, ServiceEndpointMethodMapping serviceEndpointMethodMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, serviceEndpointMethodMapping, context);
    }

    public void write(XoXMLStreamWriter writer, ServiceEndpointMethodMapping serviceEndpointMethodMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, serviceEndpointMethodMapping, context);
    }

    public static final ServiceEndpointMethodMapping _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ServiceEndpointMethodMapping serviceEndpointMethodMapping = new ServiceEndpointMethodMapping();
        context.beforeUnmarshal(serviceEndpointMethodMapping, LifecycleCallback.NONE);

        List<MethodParamPartsMapping> methodParamPartsMapping = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("service-endpoint-method-mappingType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ServiceEndpointMethodMapping.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, serviceEndpointMethodMapping);
                serviceEndpointMethodMapping.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("java-method-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: javaMethodName
                String javaMethodNameRaw = elementReader.getElementText();

                String javaMethodName;
                try {
                    javaMethodName = Adapters.collapsedStringAdapterAdapter.unmarshal(javaMethodNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                serviceEndpointMethodMapping.javaMethodName = javaMethodName;
            } else if (("wsdl-operation" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: wsdlOperation
                String wsdlOperationRaw = elementReader.getElementText();

                String wsdlOperation;
                try {
                    wsdlOperation = Adapters.collapsedStringAdapterAdapter.unmarshal(wsdlOperationRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                serviceEndpointMethodMapping.wsdlOperation = wsdlOperation;
            } else if (("wrapped-element" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: wrappedElement
                Element wrappedElement = elementReader.getElementAsDomElement();
                serviceEndpointMethodMapping.wrappedElement = wrappedElement;
            } else if (("method-param-parts-mapping" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodParamPartsMapping
                MethodParamPartsMapping methodParamPartsMappingItem = readMethodParamPartsMapping(elementReader, context);
                if (methodParamPartsMapping == null) {
                    methodParamPartsMapping = serviceEndpointMethodMapping.methodParamPartsMapping;
                    if (methodParamPartsMapping!= null) {
                        methodParamPartsMapping.clear();
                    } else {
                        methodParamPartsMapping = new ArrayList<>();
                    }
                }
                methodParamPartsMapping.add(methodParamPartsMappingItem);
            } else if (("wsdl-return-value-mapping" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: wsdlReturnValueMapping
                WsdlReturnValueMapping wsdlReturnValueMapping = readWsdlReturnValueMapping(elementReader, context);
                serviceEndpointMethodMapping.wsdlReturnValueMapping = wsdlReturnValueMapping;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "java-method-name"), new QName("http://java.sun.com/xml/ns/javaee", "wsdl-operation"), new QName("http://java.sun.com/xml/ns/javaee", "wrapped-element"), new QName("http://java.sun.com/xml/ns/javaee", "method-param-parts-mapping"), new QName("http://java.sun.com/xml/ns/javaee", "wsdl-return-value-mapping"));
            }
        }
        if (methodParamPartsMapping!= null) {
            serviceEndpointMethodMapping.methodParamPartsMapping = methodParamPartsMapping;
        }

        context.afterUnmarshal(serviceEndpointMethodMapping, LifecycleCallback.NONE);

        return serviceEndpointMethodMapping;
    }

    public final ServiceEndpointMethodMapping read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, ServiceEndpointMethodMapping serviceEndpointMethodMapping, RuntimeContext context)
        throws Exception
    {
        if (serviceEndpointMethodMapping == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ServiceEndpointMethodMapping.class!= serviceEndpointMethodMapping.getClass()) {
            context.unexpectedSubclass(writer, serviceEndpointMethodMapping, ServiceEndpointMethodMapping.class);
            return ;
        }

        context.beforeMarshal(serviceEndpointMethodMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = serviceEndpointMethodMapping.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(serviceEndpointMethodMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: javaMethodName
        String javaMethodNameRaw = serviceEndpointMethodMapping.javaMethodName;
        String javaMethodName = null;
        try {
            javaMethodName = Adapters.collapsedStringAdapterAdapter.marshal(javaMethodNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(serviceEndpointMethodMapping, "javaMethodName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (javaMethodName!= null) {
            writer.writeStartElement(prefix, "java-method-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(javaMethodName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(serviceEndpointMethodMapping, "javaMethodName");
        }

        // ELEMENT: wsdlOperation
        String wsdlOperationRaw = serviceEndpointMethodMapping.wsdlOperation;
        String wsdlOperation = null;
        try {
            wsdlOperation = Adapters.collapsedStringAdapterAdapter.marshal(wsdlOperationRaw);
        } catch (Exception e) {
            context.xmlAdapterError(serviceEndpointMethodMapping, "wsdlOperation", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (wsdlOperation!= null) {
            writer.writeStartElement(prefix, "wsdl-operation", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(wsdlOperation);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(serviceEndpointMethodMapping, "wsdlOperation");
        }

        // ELEMENT: wrappedElement
        Object wrappedElement = serviceEndpointMethodMapping.wrappedElement;
        if (wrappedElement!= null) {
            writer.writeStartElement(prefix, "wrapped-element", "http://java.sun.com/xml/ns/javaee");
            writer.writeDomElement(((Element) wrappedElement), false);
            writer.writeEndElement();
        }

        // ELEMENT: methodParamPartsMapping
        List<MethodParamPartsMapping> methodParamPartsMapping = serviceEndpointMethodMapping.methodParamPartsMapping;
        if (methodParamPartsMapping!= null) {
            for (MethodParamPartsMapping methodParamPartsMappingItem: methodParamPartsMapping) {
                if (methodParamPartsMappingItem!= null) {
                    writer.writeStartElement(prefix, "method-param-parts-mapping", "http://java.sun.com/xml/ns/javaee");
                    writeMethodParamPartsMapping(writer, methodParamPartsMappingItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: wsdlReturnValueMapping
        WsdlReturnValueMapping wsdlReturnValueMapping = serviceEndpointMethodMapping.wsdlReturnValueMapping;
        if (wsdlReturnValueMapping!= null) {
            writer.writeStartElement(prefix, "wsdl-return-value-mapping", "http://java.sun.com/xml/ns/javaee");
            writeWsdlReturnValueMapping(writer, wsdlReturnValueMapping, context);
            writer.writeEndElement();
        }

        context.afterMarshal(serviceEndpointMethodMapping, LifecycleCallback.NONE);
    }

}
