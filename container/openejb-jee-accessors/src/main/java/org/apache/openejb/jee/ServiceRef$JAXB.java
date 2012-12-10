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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.apache.openejb.jee.Handler$JAXB.readHandler;
import static org.apache.openejb.jee.Handler$JAXB.writeHandler;
import static org.apache.openejb.jee.HandlerChains$JAXB.readHandlerChains;
import static org.apache.openejb.jee.HandlerChains$JAXB.writeHandlerChains;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.InjectionTarget$JAXB.readInjectionTarget;
import static org.apache.openejb.jee.InjectionTarget$JAXB.writeInjectionTarget;
import static org.apache.openejb.jee.PortComponentRef$JAXB.readPortComponentRef;
import static org.apache.openejb.jee.PortComponentRef$JAXB.writePortComponentRef;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class ServiceRef$JAXB
        extends JAXBObject<ServiceRef> {


    public ServiceRef$JAXB() {
        super(ServiceRef.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "service-refType".intern()), Text$JAXB.class, Icon$JAXB.class, PortComponentRef$JAXB.class, Handler$JAXB.class, HandlerChains$JAXB.class, InjectionTarget$JAXB.class);
    }

    public static ServiceRef readServiceRef(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeServiceRef(XoXMLStreamWriter writer, ServiceRef serviceRef, RuntimeContext context)
            throws Exception {
        _write(writer, serviceRef, context);
    }

    public void write(XoXMLStreamWriter writer, ServiceRef serviceRef, RuntimeContext context)
            throws Exception {
        _write(writer, serviceRef, context);
    }

    public final static ServiceRef _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ServiceRef serviceRef = new ServiceRef();
        context.beforeUnmarshal(serviceRef, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<PortComponentRef> portComponentRef = null;
        List<Handler> handler = null;
        Set<InjectionTarget> injectionTarget = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("service-refType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ServiceRef.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, serviceRef);
                serviceRef.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<Text>();
                }
                descriptions.add(descriptionsItem);
            } else if (("display-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayNames
                Text displayNamesItem = readText(elementReader, context);
                if (displayNames == null) {
                    displayNames = new ArrayList<Text>();
                }
                displayNames.add(displayNamesItem);
            } else if (("icon" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = serviceRef.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("service-ref-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceRefName
                String serviceRefNameRaw = elementReader.getElementAsString();

                String serviceRefName;
                try {
                    serviceRefName = Adapters.collapsedStringAdapterAdapter.unmarshal(serviceRefNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                serviceRef.serviceRefName = serviceRefName;
            } else if (("service-interface" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceInterface
                String serviceInterfaceRaw = elementReader.getElementAsString();

                String serviceInterface;
                try {
                    serviceInterface = Adapters.collapsedStringAdapterAdapter.unmarshal(serviceInterfaceRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                serviceRef.serviceInterface = serviceInterface;
            } else if (("service-ref-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceRefType
                String serviceRefTypeRaw = elementReader.getElementAsString();

                String serviceRefType;
                try {
                    serviceRefType = Adapters.collapsedStringAdapterAdapter.unmarshal(serviceRefTypeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                serviceRef.serviceRefType = serviceRefType;
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

                serviceRef.wsdlFile = wsdlFile;
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

                serviceRef.jaxrpcMappingFile = jaxrpcMappingFile;
            } else if (("service-qname" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: serviceQname
                QName serviceQname = elementReader.getElementAsQName();
                serviceRef.serviceQname = serviceQname;
            } else if (("port-component-ref" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: portComponentRef
                PortComponentRef portComponentRefItem = readPortComponentRef(elementReader, context);
                if (portComponentRef == null) {
                    portComponentRef = serviceRef.portComponentRef;
                    if (portComponentRef != null) {
                        portComponentRef.clear();
                    } else {
                        portComponentRef = new ArrayList<PortComponentRef>();
                    }
                }
                portComponentRef.add(portComponentRefItem);
            } else if (("handler" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: handler
                Handler handlerItem = readHandler(elementReader, context);
                if (handler == null) {
                    handler = serviceRef.handler;
                    if (handler != null) {
                        handler.clear();
                    } else {
                        handler = new ArrayList<Handler>();
                    }
                }
                handler.add(handlerItem);
            } else if (("handler-chains" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: handlerChains
                HandlerChains handlerChains = readHandlerChains(elementReader, context);
                serviceRef.handlerChains = handlerChains;
            } else if (("mapped-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: mappedName
                String mappedNameRaw = elementReader.getElementAsString();

                String mappedName;
                try {
                    mappedName = Adapters.collapsedStringAdapterAdapter.unmarshal(mappedNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                serviceRef.mappedName = mappedName;
            } else if (("injection-target" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: injectionTarget
                InjectionTarget injectionTargetItem = readInjectionTarget(elementReader, context);
                if (injectionTarget == null) {
                    injectionTarget = serviceRef.injectionTarget;
                    if (injectionTarget != null) {
                        injectionTarget.clear();
                    } else {
                        injectionTarget = new LinkedHashSet<InjectionTarget>();
                    }
                }
                injectionTarget.add(injectionTargetItem);
            } else if (("lookup-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: lookupName
                String lookupNameRaw = elementReader.getElementAsString();

                String lookupName;
                try {
                    lookupName = Adapters.collapsedStringAdapterAdapter.unmarshal(lookupNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                serviceRef.lookupName = lookupName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "service-ref-name"), new QName("http://java.sun.com/xml/ns/javaee", "service-interface"), new QName("http://java.sun.com/xml/ns/javaee", "service-ref-type"), new QName("http://java.sun.com/xml/ns/javaee", "wsdl-file"), new QName("http://java.sun.com/xml/ns/javaee", "jaxrpc-mapping-file"), new QName("http://java.sun.com/xml/ns/javaee", "service-qname"), new QName("http://java.sun.com/xml/ns/javaee", "port-component-ref"), new QName("http://java.sun.com/xml/ns/javaee", "handler"), new QName("http://java.sun.com/xml/ns/javaee", "handler-chains"), new QName("http://java.sun.com/xml/ns/javaee", "mapped-name"), new QName("http://java.sun.com/xml/ns/javaee", "injection-target"), new QName("http://java.sun.com/xml/ns/javaee", "lookup-name"));
            }
        }
        if (descriptions != null) {
            try {
                serviceRef.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, ServiceRef.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                serviceRef.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (Exception e) {
                context.setterError(reader, ServiceRef.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            serviceRef.icon = icon;
        }
        if (portComponentRef != null) {
            serviceRef.portComponentRef = portComponentRef;
        }
        if (handler != null) {
            serviceRef.handler = handler;
        }
        if (injectionTarget != null) {
            serviceRef.injectionTarget = injectionTarget;
        }

        context.afterUnmarshal(serviceRef, LifecycleCallback.NONE);

        return serviceRef;
    }

    public final ServiceRef read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, ServiceRef serviceRef, RuntimeContext context)
            throws Exception {
        if (serviceRef == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ServiceRef.class != serviceRef.getClass()) {
            context.unexpectedSubclass(writer, serviceRef, ServiceRef.class);
            return;
        }

        context.beforeMarshal(serviceRef, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = serviceRef.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(serviceRef, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = serviceRef.getDescriptions();
        } catch (Exception e) {
            context.getterError(serviceRef, "descriptions", ServiceRef.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(serviceRef, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = serviceRef.getDisplayNames();
        } catch (Exception e) {
            context.getterError(serviceRef, "displayNames", ServiceRef.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(serviceRef, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = serviceRef.icon;
        if (icon != null) {
            for (Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(serviceRef, "icon");
                }
            }
        }

        // ELEMENT: serviceRefName
        String serviceRefNameRaw = serviceRef.serviceRefName;
        String serviceRefName = null;
        try {
            serviceRefName = Adapters.collapsedStringAdapterAdapter.marshal(serviceRefNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(serviceRef, "serviceRefName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (serviceRefName != null) {
            writer.writeStartElement(prefix, "service-ref-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(serviceRefName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(serviceRef, "serviceRefName");
        }

        // ELEMENT: serviceInterface
        String serviceInterfaceRaw = serviceRef.serviceInterface;
        String serviceInterface = null;
        try {
            serviceInterface = Adapters.collapsedStringAdapterAdapter.marshal(serviceInterfaceRaw);
        } catch (Exception e) {
            context.xmlAdapterError(serviceRef, "serviceInterface", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (serviceInterface != null) {
            writer.writeStartElement(prefix, "service-interface", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(serviceInterface);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(serviceRef, "serviceInterface");
        }

        // ELEMENT: serviceRefType
        String serviceRefTypeRaw = serviceRef.serviceRefType;
        String serviceRefType = null;
        try {
            serviceRefType = Adapters.collapsedStringAdapterAdapter.marshal(serviceRefTypeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(serviceRef, "serviceRefType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (serviceRefType != null) {
            writer.writeStartElement(prefix, "service-ref-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(serviceRefType);
            writer.writeEndElement();
        }

        // ELEMENT: wsdlFile
        String wsdlFileRaw = serviceRef.wsdlFile;
        String wsdlFile = null;
        try {
            wsdlFile = Adapters.collapsedStringAdapterAdapter.marshal(wsdlFileRaw);
        } catch (Exception e) {
            context.xmlAdapterError(serviceRef, "wsdlFile", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (wsdlFile != null) {
            writer.writeStartElement(prefix, "wsdl-file", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(wsdlFile);
            writer.writeEndElement();
        }

        // ELEMENT: jaxrpcMappingFile
        String jaxrpcMappingFileRaw = serviceRef.jaxrpcMappingFile;
        String jaxrpcMappingFile = null;
        try {
            jaxrpcMappingFile = Adapters.collapsedStringAdapterAdapter.marshal(jaxrpcMappingFileRaw);
        } catch (Exception e) {
            context.xmlAdapterError(serviceRef, "jaxrpcMappingFile", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (jaxrpcMappingFile != null) {
            writer.writeStartElement(prefix, "jaxrpc-mapping-file", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(jaxrpcMappingFile);
            writer.writeEndElement();
        }

        // ELEMENT: serviceQname
        QName serviceQname = serviceRef.serviceQname;
        if (serviceQname != null) {
            writer.writeStartElement(prefix, "service-qname", "http://java.sun.com/xml/ns/javaee");
            writer.writeQName(serviceQname);
            writer.writeEndElement();
        }

        // ELEMENT: portComponentRef
        List<PortComponentRef> portComponentRef = serviceRef.portComponentRef;
        if (portComponentRef != null) {
            for (PortComponentRef portComponentRefItem : portComponentRef) {
                if (portComponentRefItem != null) {
                    writer.writeStartElement(prefix, "port-component-ref", "http://java.sun.com/xml/ns/javaee");
                    writePortComponentRef(writer, portComponentRefItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(serviceRef, "portComponentRef");
                }
            }
        }

        // ELEMENT: handler
        List<Handler> handler = serviceRef.handler;
        if (handler != null) {
            for (Handler handlerItem : handler) {
                if (handlerItem != null) {
                    writer.writeStartElement(prefix, "handler", "http://java.sun.com/xml/ns/javaee");
                    writeHandler(writer, handlerItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(serviceRef, "handler");
                }
            }
        }

        // ELEMENT: handlerChains
        HandlerChains handlerChains = serviceRef.handlerChains;
        if (handlerChains != null) {
            writer.writeStartElement(prefix, "handler-chains", "http://java.sun.com/xml/ns/javaee");
            writeHandlerChains(writer, handlerChains, context);
            writer.writeEndElement();
        }

        // ELEMENT: mappedName
        String mappedNameRaw = serviceRef.mappedName;
        String mappedName = null;
        try {
            mappedName = Adapters.collapsedStringAdapterAdapter.marshal(mappedNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(serviceRef, "mappedName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (mappedName != null) {
            writer.writeStartElement(prefix, "mapped-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(mappedName);
            writer.writeEndElement();
        }

        // ELEMENT: injectionTarget
        Set<InjectionTarget> injectionTarget = serviceRef.injectionTarget;
        if (injectionTarget != null) {
            for (InjectionTarget injectionTargetItem : injectionTarget) {
                if (injectionTargetItem != null) {
                    writer.writeStartElement(prefix, "injection-target", "http://java.sun.com/xml/ns/javaee");
                    writeInjectionTarget(writer, injectionTargetItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(serviceRef, "injectionTarget");
                }
            }
        }

        // ELEMENT: lookupName
        String lookupNameRaw = serviceRef.lookupName;
        String lookupName = null;
        try {
            lookupName = Adapters.collapsedStringAdapterAdapter.marshal(lookupNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(serviceRef, "lookupName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (lookupName != null) {
            writer.writeStartElement(prefix, "lookup-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(lookupName);
            writer.writeEndElement();
        }

        context.afterMarshal(serviceRef, LifecycleCallback.NONE);
    }

}
