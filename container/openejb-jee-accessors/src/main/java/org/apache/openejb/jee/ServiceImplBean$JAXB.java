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

import com.envoisolutions.sxc.jaxb.JAXBObject;
import com.envoisolutions.sxc.jaxb.LifecycleCallback;
import com.envoisolutions.sxc.jaxb.RuntimeContext;
import com.envoisolutions.sxc.util.Attribute;
import com.envoisolutions.sxc.util.XoXMLStreamReader;
import com.envoisolutions.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;

@SuppressWarnings({
        "StringEquality"
})
public class ServiceImplBean$JAXB
        extends JAXBObject<ServiceImplBean> {


    public ServiceImplBean$JAXB() {
        super(ServiceImplBean.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "service-impl-beanType".intern()));
    }

    public static ServiceImplBean readServiceImplBean(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeServiceImplBean(XoXMLStreamWriter writer, ServiceImplBean serviceImplBean, RuntimeContext context)
            throws Exception {
        _write(writer, serviceImplBean, context);
    }

    public void write(XoXMLStreamWriter writer, ServiceImplBean serviceImplBean, RuntimeContext context)
            throws Exception {
        _write(writer, serviceImplBean, context);
    }

    public final static ServiceImplBean _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ServiceImplBean serviceImplBean = new ServiceImplBean();
        context.beforeUnmarshal(serviceImplBean, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("service-impl-beanType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ServiceImplBean.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, serviceImplBean);
                serviceImplBean.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("ejb-link" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbLink
                String ejbLinkRaw = elementReader.getElementAsString();

                String ejbLink;
                try {
                    ejbLink = Adapters.collapsedStringAdapterAdapter.unmarshal(ejbLinkRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                serviceImplBean.ejbLink = ejbLink;
            } else if (("servlet-link" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: servletLink
                String servletLinkRaw = elementReader.getElementAsString();

                String servletLink;
                try {
                    servletLink = Adapters.collapsedStringAdapterAdapter.unmarshal(servletLinkRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                serviceImplBean.servletLink = servletLink;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "ejb-link"), new QName("http://java.sun.com/xml/ns/javaee", "servlet-link"));
            }
        }

        context.afterUnmarshal(serviceImplBean, LifecycleCallback.NONE);

        return serviceImplBean;
    }

    public final ServiceImplBean read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, ServiceImplBean serviceImplBean, RuntimeContext context)
            throws Exception {
        if (serviceImplBean == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ServiceImplBean.class != serviceImplBean.getClass()) {
            context.unexpectedSubclass(writer, serviceImplBean, ServiceImplBean.class);
            return;
        }

        context.beforeMarshal(serviceImplBean, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = serviceImplBean.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(serviceImplBean, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: ejbLink
        String ejbLinkRaw = serviceImplBean.ejbLink;
        String ejbLink = null;
        try {
            ejbLink = Adapters.collapsedStringAdapterAdapter.marshal(ejbLinkRaw);
        } catch (Exception e) {
            context.xmlAdapterError(serviceImplBean, "ejbLink", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (ejbLink != null) {
            writer.writeStartElement(prefix, "ejb-link", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(ejbLink);
            writer.writeEndElement();
        }

        // ELEMENT: servletLink
        String servletLinkRaw = serviceImplBean.servletLink;
        String servletLink = null;
        try {
            servletLink = Adapters.collapsedStringAdapterAdapter.marshal(servletLinkRaw);
        } catch (Exception e) {
            context.xmlAdapterError(serviceImplBean, "servletLink", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (servletLink != null) {
            writer.writeStartElement(prefix, "servlet-link", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(servletLink);
            writer.writeEndElement();
        }

        context.afterMarshal(serviceImplBean, LifecycleCallback.NONE);
    }

}
