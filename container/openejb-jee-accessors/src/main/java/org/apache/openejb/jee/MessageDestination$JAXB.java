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
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class MessageDestination$JAXB
    extends JAXBObject<MessageDestination>
{


    public MessageDestination$JAXB() {
        super(MessageDestination.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "message-destinationType".intern()), Text$JAXB.class, Icon$JAXB.class);
    }

    public static MessageDestination readMessageDestination(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeMessageDestination(XoXMLStreamWriter writer, MessageDestination messageDestination, RuntimeContext context)
        throws Exception
    {
        _write(writer, messageDestination, context);
    }

    public void write(XoXMLStreamWriter writer, MessageDestination messageDestination, RuntimeContext context)
        throws Exception
    {
        _write(writer, messageDestination, context);
    }

    public static final MessageDestination _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        MessageDestination messageDestination = new MessageDestination();
        context.beforeUnmarshal(messageDestination, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("message-destinationType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, MessageDestination.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, messageDestination);
                messageDestination.id = id;
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
                    icon = messageDestination.icon;
                    if (icon!= null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<>();
                    }
                }
                icon.add(iconItem);
            } else if (("message-destination-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: messageDestinationName
                String messageDestinationNameRaw = elementReader.getElementText();

                String messageDestinationName;
                try {
                    messageDestinationName = Adapters.collapsedStringAdapterAdapter.unmarshal(messageDestinationNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                messageDestination.messageDestinationName = messageDestinationName;
            } else if (("mapped-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: mappedName
                String mappedNameRaw = elementReader.getElementText();

                String mappedName;
                try {
                    mappedName = Adapters.collapsedStringAdapterAdapter.unmarshal(mappedNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                messageDestination.mappedName = mappedName;
            } else if (("lookup-name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: lookupName
                String lookupNameRaw = elementReader.getElementText();

                String lookupName;
                try {
                    lookupName = Adapters.collapsedStringAdapterAdapter.unmarshal(lookupNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                messageDestination.lookupName = lookupName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "message-destination-name"), new QName("http://java.sun.com/xml/ns/javaee", "mapped-name"), new QName("http://java.sun.com/xml/ns/javaee", "lookup-name"));
            }
        }
        if (descriptions!= null) {
            try {
                messageDestination.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, MessageDestination.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames!= null) {
            try {
                messageDestination.setDisplayNames(displayNames.toArray(new Text[displayNames.size()] ));
            } catch (Exception e) {
                context.setterError(reader, MessageDestination.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon!= null) {
            messageDestination.icon = icon;
        }

        context.afterUnmarshal(messageDestination, LifecycleCallback.NONE);

        return messageDestination;
    }

    public final MessageDestination read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, MessageDestination messageDestination, RuntimeContext context)
        throws Exception
    {
        if (messageDestination == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (MessageDestination.class!= messageDestination.getClass()) {
            context.unexpectedSubclass(writer, messageDestination, MessageDestination.class);
            return ;
        }

        context.beforeMarshal(messageDestination, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = messageDestination.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(messageDestination, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = messageDestination.getDescriptions();
        } catch (Exception e) {
            context.getterError(messageDestination, "descriptions", MessageDestination.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDestination, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = messageDestination.getDisplayNames();
        } catch (Exception e) {
            context.getterError(messageDestination, "displayNames", MessageDestination.class, "getDisplayNames", e);
        }
        if (displayNames!= null) {
            for (Text displayNamesItem: displayNames) {
                if (displayNamesItem!= null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDestination, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = messageDestination.icon;
        if (icon!= null) {
            for (Icon iconItem: icon) {
                if (iconItem!= null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(messageDestination, "icon");
                }
            }
        }

        // ELEMENT: messageDestinationName
        String messageDestinationNameRaw = messageDestination.messageDestinationName;
        String messageDestinationName = null;
        try {
            messageDestinationName = Adapters.collapsedStringAdapterAdapter.marshal(messageDestinationNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(messageDestination, "messageDestinationName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (messageDestinationName!= null) {
            writer.writeStartElement(prefix, "message-destination-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(messageDestinationName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(messageDestination, "messageDestinationName");
        }

        // ELEMENT: mappedName
        String mappedNameRaw = messageDestination.mappedName;
        String mappedName = null;
        try {
            mappedName = Adapters.collapsedStringAdapterAdapter.marshal(mappedNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(messageDestination, "mappedName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (mappedName!= null) {
            writer.writeStartElement(prefix, "mapped-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(mappedName);
            writer.writeEndElement();
        }

        // ELEMENT: lookupName
        String lookupNameRaw = messageDestination.lookupName;
        String lookupName = null;
        try {
            lookupName = Adapters.collapsedStringAdapterAdapter.marshal(lookupNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(messageDestination, "lookupName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (lookupName!= null) {
            writer.writeStartElement(prefix, "lookup-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(lookupName);
            writer.writeEndElement();
        }

        context.afterMarshal(messageDestination, LifecycleCallback.NONE);
    }

}
