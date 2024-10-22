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


import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.ParamValue$JAXB.readParamValue;
import static org.apache.openejb.jee.ParamValue$JAXB.writeParamValue;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class Handler$JAXB
    extends JAXBObject<Handler>
{


    public Handler$JAXB() {
        super(Handler.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "handlerType".intern()), Text$JAXB.class, Icon$JAXB.class, ParamValue$JAXB.class);
    }

    public static Handler readHandler(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeHandler(XoXMLStreamWriter writer, Handler handler, RuntimeContext context)
        throws Exception
    {
        _write(writer, handler, context);
    }

    public void write(XoXMLStreamWriter writer, Handler handler, RuntimeContext context)
        throws Exception
    {
        _write(writer, handler, context);
    }

    public static final Handler _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        Handler handler = new Handler();
        context.beforeUnmarshal(handler, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<ParamValue> initParam = null;
        List<QName> soapHeader = null;
        List<String> soapRole = null;
        List<String> portName = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("handlerType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Handler.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, handler);
                handler.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("description" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<>();
                }
                descriptions.add(descriptionsItem);
            } else if (("display-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayNames
                Text displayNamesItem = readText(elementReader, context);
                if (displayNames == null) {
                    displayNames = new ArrayList<>();
                }
                displayNames.add(displayNamesItem);
            } else if (("icon" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = handler.icon;
                    if (icon!= null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<>();
                    }
                }
                icon.add(iconItem);
            } else if (("handler-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: handlerName
                String handlerNameRaw = elementReader.getElementText();

                String handlerName;
                try {
                    handlerName = Adapters.collapsedStringAdapterAdapter.unmarshal(handlerNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                handler.handlerName = handlerName;
            } else if (("handler-class" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: handlerClass
                String handlerClassRaw = elementReader.getElementText();

                String handlerClass;
                try {
                    handlerClass = Adapters.collapsedStringAdapterAdapter.unmarshal(handlerClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                handler.handlerClass = handlerClass;
            } else if (("init-param" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: initParam
                ParamValue initParamItem = readParamValue(elementReader, context);
                if (initParam == null) {
                    initParam = handler.initParam;
                    if (initParam!= null) {
                        initParam.clear();
                    } else {
                        initParam = new ArrayList<>();
                    }
                }
                initParam.add(initParamItem);
            } else if (("soap-header" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: soapHeader
                QName soapHeaderItem = elementReader.getElementAsQName();
                if (soapHeader == null) {
                    soapHeader = handler.soapHeader;
                    if (soapHeader!= null) {
                        soapHeader.clear();
                    } else {
                        soapHeader = new ArrayList<>();
                    }
                }
                soapHeader.add(soapHeaderItem);
            } else if (("soap-role" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: soapRole
                String soapRoleItemRaw = elementReader.getElementText();

                String soapRoleItem;
                try {
                    soapRoleItem = Adapters.collapsedStringAdapterAdapter.unmarshal(soapRoleItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (soapRole == null) {
                    soapRole = handler.soapRole;
                    if (soapRole!= null) {
                        soapRole.clear();
                    } else {
                        soapRole = new ArrayList<>();
                    }
                }
                soapRole.add(soapRoleItem);
            } else if (("port-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: portName
                String portNameItemRaw = elementReader.getElementText();

                String portNameItem;
                try {
                    portNameItem = Adapters.collapsedStringAdapterAdapter.unmarshal(portNameItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (portName == null) {
                    portName = handler.portName;
                    if (portName!= null) {
                        portName.clear();
                    } else {
                        portName = new ArrayList<>();
                    }
                }
                portName.add(portNameItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "handler-name"), new QName("http://java.sun.com/xml/ns/javaee", "handler-class"), new QName("http://java.sun.com/xml/ns/javaee", "init-param"), new QName("http://java.sun.com/xml/ns/javaee", "soap-header"), new QName("http://java.sun.com/xml/ns/javaee", "soap-role"), new QName("http://java.sun.com/xml/ns/javaee", "port-name"));
            }
        }
        if (descriptions!= null) {
            try {
                handler.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, Handler.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames!= null) {
            try {
                handler.setDisplayNames(displayNames.toArray(new Text[displayNames.size()] ));
            } catch (Exception e) {
                context.setterError(reader, Handler.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon!= null) {
            handler.icon = icon;
        }
        if (initParam!= null) {
            handler.initParam = initParam;
        }
        if (soapHeader!= null) {
            handler.soapHeader = soapHeader;
        }
        if (soapRole!= null) {
            handler.soapRole = soapRole;
        }
        if (portName!= null) {
            handler.portName = portName;
        }

        context.afterUnmarshal(handler, LifecycleCallback.NONE);

        return handler;
    }

    public final Handler read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, Handler handler, RuntimeContext context)
        throws Exception
    {
        if (handler == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Handler.class!= handler.getClass()) {
            context.unexpectedSubclass(writer, handler, Handler.class);
            return ;
        }

        context.beforeMarshal(handler, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = handler.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(handler, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = handler.getDescriptions();
        } catch (Exception e) {
            context.getterError(handler, "descriptions", Handler.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(handler, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = handler.getDisplayNames();
        } catch (Exception e) {
            context.getterError(handler, "displayNames", Handler.class, "getDisplayNames", e);
        }
        if (displayNames!= null) {
            for (Text displayNamesItem: displayNames) {
                if (displayNamesItem!= null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(handler, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = handler.icon;
        if (icon!= null) {
            for (Icon iconItem: icon) {
                if (iconItem!= null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: handlerName
        String handlerNameRaw = handler.handlerName;
        String handlerName = null;
        try {
            handlerName = Adapters.collapsedStringAdapterAdapter.marshal(handlerNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(handler, "handlerName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (handlerName!= null) {
            writer.writeStartElement(prefix, "handler-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(handlerName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(handler, "handlerName");
        }

        // ELEMENT: handlerClass
        String handlerClassRaw = handler.handlerClass;
        String handlerClass = null;
        try {
            handlerClass = Adapters.collapsedStringAdapterAdapter.marshal(handlerClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(handler, "handlerClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (handlerClass!= null) {
            writer.writeStartElement(prefix, "handler-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(handlerClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(handler, "handlerClass");
        }

        // ELEMENT: initParam
        List<ParamValue> initParam = handler.initParam;
        if (initParam!= null) {
            for (ParamValue initParamItem: initParam) {
                if (initParamItem!= null) {
                    writer.writeStartElement(prefix, "init-param", "http://java.sun.com/xml/ns/javaee");
                    writeParamValue(writer, initParamItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: soapHeader
        List<QName> soapHeader = handler.soapHeader;
        if (soapHeader!= null) {
            for (QName soapHeaderItem: soapHeader) {
                if (soapHeaderItem!= null) {
                    writer.writeStartElement(prefix, "soap-header", "http://java.sun.com/xml/ns/javaee");
                    writer.writeQName(soapHeaderItem);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: soapRole
        List<String> soapRoleRaw = handler.soapRole;
        if (soapRoleRaw!= null) {
            for (String soapRoleItem: soapRoleRaw) {
                String soapRole = null;
                try {
                    soapRole = Adapters.collapsedStringAdapterAdapter.marshal(soapRoleItem);
                } catch (Exception e) {
                    context.xmlAdapterError(handler, "soapRole", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (soapRole!= null) {
                    writer.writeStartElement(prefix, "soap-role", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(soapRole);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: portName
        List<String> portNameRaw = handler.portName;
        if (portNameRaw!= null) {
            for (String portNameItem: portNameRaw) {
                String portName = null;
                try {
                    portName = Adapters.collapsedStringAdapterAdapter.marshal(portNameItem);
                } catch (Exception e) {
                    context.xmlAdapterError(handler, "portName", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (portName!= null) {
                    writer.writeStartElement(prefix, "port-name", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(portName);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(handler, "portName");
                }
            }
        }

        context.afterMarshal(handler, LifecycleCallback.NONE);
    }

}
