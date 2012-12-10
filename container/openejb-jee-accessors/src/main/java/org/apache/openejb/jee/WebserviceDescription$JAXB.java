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

import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.PortComponent$JAXB.readPortComponent;
import static org.apache.openejb.jee.PortComponent$JAXB.writePortComponent;

@SuppressWarnings({
        "StringEquality"
})
public class WebserviceDescription$JAXB
        extends JAXBObject<WebserviceDescription> {


    public WebserviceDescription$JAXB() {
        super(WebserviceDescription.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "webservice-descriptionType".intern()), Icon$JAXB.class, PortComponent$JAXB.class);
    }

    public static WebserviceDescription readWebserviceDescription(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeWebserviceDescription(XoXMLStreamWriter writer, WebserviceDescription webserviceDescription, RuntimeContext context)
            throws Exception {
        _write(writer, webserviceDescription, context);
    }

    public void write(XoXMLStreamWriter writer, WebserviceDescription webserviceDescription, RuntimeContext context)
            throws Exception {
        _write(writer, webserviceDescription, context);
    }

    public final static WebserviceDescription _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        WebserviceDescription webserviceDescription = new WebserviceDescription();
        context.beforeUnmarshal(webserviceDescription, LifecycleCallback.NONE);

        KeyedCollection<String, PortComponent> portComponent = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("webservice-descriptionType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, WebserviceDescription.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, webserviceDescription);
                webserviceDescription.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: description
                String descriptionRaw = elementReader.getElementAsString();

                String description;
                try {
                    description = Adapters.collapsedStringAdapterAdapter.unmarshal(descriptionRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                webserviceDescription.description = description;
            } else if (("display-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayName
                String displayNameRaw = elementReader.getElementAsString();

                String displayName;
                try {
                    displayName = Adapters.collapsedStringAdapterAdapter.unmarshal(displayNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                webserviceDescription.displayName = displayName;
            } else if (("icon" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                Icon icon = readIcon(elementReader, context);
                webserviceDescription.icon = icon;
            } else if (("webservice-description-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: webserviceDescriptionName
                String webserviceDescriptionNameRaw = elementReader.getElementAsString();

                String webserviceDescriptionName;
                try {
                    webserviceDescriptionName = Adapters.collapsedStringAdapterAdapter.unmarshal(webserviceDescriptionNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                webserviceDescription.webserviceDescriptionName = webserviceDescriptionName;
            } else if (("wsdl-file" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: wsdlFile
                String wsdlFileRaw = elementReader.getElementAsString();

                String wsdlFile;
                try {
                    wsdlFile = Adapters.collapsedStringAdapterAdapter.unmarshal(wsdlFileRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                webserviceDescription.wsdlFile = wsdlFile;
            } else if (("jaxrpc-mapping-file" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: jaxrpcMappingFile
                String jaxrpcMappingFileRaw = elementReader.getElementAsString();

                String jaxrpcMappingFile;
                try {
                    jaxrpcMappingFile = Adapters.collapsedStringAdapterAdapter.unmarshal(jaxrpcMappingFileRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                webserviceDescription.jaxrpcMappingFile = jaxrpcMappingFile;
            } else if (("port-component" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: portComponent
                PortComponent portComponentItem = readPortComponent(elementReader, context);
                if (portComponent == null) {
                    portComponent = webserviceDescription.portComponent;
                    if (portComponent != null) {
                        portComponent.clear();
                    } else {
                        portComponent = new KeyedCollection<String, PortComponent>();
                    }
                }
                portComponent.add(portComponentItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "webservice-description-name"), new QName("http://java.sun.com/xml/ns/javaee", "wsdl-file"), new QName("http://java.sun.com/xml/ns/javaee", "jaxrpc-mapping-file"), new QName("http://java.sun.com/xml/ns/javaee", "port-component"));
            }
        }
        if (portComponent != null) {
            webserviceDescription.portComponent = portComponent;
        }

        context.afterUnmarshal(webserviceDescription, LifecycleCallback.NONE);

        return webserviceDescription;
    }

    public final WebserviceDescription read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, WebserviceDescription webserviceDescription, RuntimeContext context)
            throws Exception {
        if (webserviceDescription == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (WebserviceDescription.class != webserviceDescription.getClass()) {
            context.unexpectedSubclass(writer, webserviceDescription, WebserviceDescription.class);
            return;
        }

        context.beforeMarshal(webserviceDescription, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = webserviceDescription.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(webserviceDescription, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: description
        String descriptionRaw = webserviceDescription.description;
        String description = null;
        try {
            description = Adapters.collapsedStringAdapterAdapter.marshal(descriptionRaw);
        } catch (Exception e) {
            context.xmlAdapterError(webserviceDescription, "description", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (description != null) {
            writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(description);
            writer.writeEndElement();
        }

        // ELEMENT: displayName
        String displayNameRaw = webserviceDescription.displayName;
        String displayName = null;
        try {
            displayName = Adapters.collapsedStringAdapterAdapter.marshal(displayNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(webserviceDescription, "displayName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (displayName != null) {
            writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(displayName);
            writer.writeEndElement();
        }

        // ELEMENT: icon
        Icon icon = webserviceDescription.icon;
        if (icon != null) {
            writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
            writeIcon(writer, icon, context);
            writer.writeEndElement();
        }

        // ELEMENT: webserviceDescriptionName
        String webserviceDescriptionNameRaw = webserviceDescription.webserviceDescriptionName;
        String webserviceDescriptionName = null;
        try {
            webserviceDescriptionName = Adapters.collapsedStringAdapterAdapter.marshal(webserviceDescriptionNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(webserviceDescription, "webserviceDescriptionName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (webserviceDescriptionName != null) {
            writer.writeStartElement(prefix, "webservice-description-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(webserviceDescriptionName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(webserviceDescription, "webserviceDescriptionName");
        }

        // ELEMENT: wsdlFile
        String wsdlFileRaw = webserviceDescription.wsdlFile;
        String wsdlFile = null;
        try {
            wsdlFile = Adapters.collapsedStringAdapterAdapter.marshal(wsdlFileRaw);
        } catch (Exception e) {
            context.xmlAdapterError(webserviceDescription, "wsdlFile", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (wsdlFile != null) {
            writer.writeStartElement(prefix, "wsdl-file", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(wsdlFile);
            writer.writeEndElement();
        }

        // ELEMENT: jaxrpcMappingFile
        String jaxrpcMappingFileRaw = webserviceDescription.jaxrpcMappingFile;
        String jaxrpcMappingFile = null;
        try {
            jaxrpcMappingFile = Adapters.collapsedStringAdapterAdapter.marshal(jaxrpcMappingFileRaw);
        } catch (Exception e) {
            context.xmlAdapterError(webserviceDescription, "jaxrpcMappingFile", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (jaxrpcMappingFile != null) {
            writer.writeStartElement(prefix, "jaxrpc-mapping-file", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(jaxrpcMappingFile);
            writer.writeEndElement();
        }

        // ELEMENT: portComponent
        KeyedCollection<String, PortComponent> portComponent = webserviceDescription.portComponent;
        if (portComponent != null) {
            for (PortComponent portComponentItem : portComponent) {
                if (portComponentItem != null) {
                    writer.writeStartElement(prefix, "port-component", "http://java.sun.com/xml/ns/javaee");
                    writePortComponent(writer, portComponentItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webserviceDescription, "portComponent");
                }
            }
        }

        context.afterMarshal(webserviceDescription, LifecycleCallback.NONE);
    }

}
