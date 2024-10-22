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
public class Listener$JAXB
    extends JAXBObject<Listener>
{


    public Listener$JAXB() {
        super(Listener.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "listenerType".intern()), Text$JAXB.class, Icon$JAXB.class);
    }

    public static Listener readListener(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeListener(XoXMLStreamWriter writer, Listener listener, RuntimeContext context)
        throws Exception
    {
        _write(writer, listener, context);
    }

    public void write(XoXMLStreamWriter writer, Listener listener, RuntimeContext context)
        throws Exception
    {
        _write(writer, listener, context);
    }

    public static final Listener _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        Listener listener = new Listener();
        context.beforeUnmarshal(listener, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("listenerType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Listener.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, listener);
                listener.id = id;
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
                    icon = listener.icon;
                    if (icon!= null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<>();
                    }
                }
                icon.add(iconItem);
            } else if (("listener-class" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: listenerClass
                String listenerClassRaw = elementReader.getElementText();

                String listenerClass;
                try {
                    listenerClass = Adapters.collapsedStringAdapterAdapter.unmarshal(listenerClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                listener.listenerClass = listenerClass;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "listener-class"));
            }
        }
        if (descriptions!= null) {
            try {
                listener.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, Listener.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames!= null) {
            try {
                listener.setDisplayNames(displayNames.toArray(new Text[displayNames.size()] ));
            } catch (Exception e) {
                context.setterError(reader, Listener.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon!= null) {
            listener.icon = icon;
        }

        context.afterUnmarshal(listener, LifecycleCallback.NONE);

        return listener;
    }

    public final Listener read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, Listener listener, RuntimeContext context)
        throws Exception
    {
        if (listener == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Listener.class!= listener.getClass()) {
            context.unexpectedSubclass(writer, listener, Listener.class);
            return ;
        }

        context.beforeMarshal(listener, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = listener.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(listener, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = listener.getDescriptions();
        } catch (Exception e) {
            context.getterError(listener, "descriptions", Listener.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(listener, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = listener.getDisplayNames();
        } catch (Exception e) {
            context.getterError(listener, "displayNames", Listener.class, "getDisplayNames", e);
        }
        if (displayNames!= null) {
            for (Text displayNamesItem: displayNames) {
                if (displayNamesItem!= null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(listener, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = listener.icon;
        if (icon!= null) {
            for (Icon iconItem: icon) {
                if (iconItem!= null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(listener, "icon");
                }
            }
        }

        // ELEMENT: listenerClass
        String listenerClassRaw = listener.listenerClass;
        String listenerClass = null;
        try {
            listenerClass = Adapters.collapsedStringAdapterAdapter.marshal(listenerClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(listener, "listenerClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (listenerClass!= null) {
            writer.writeStartElement(prefix, "listener-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(listenerClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(listener, "listenerClass");
        }

        context.afterMarshal(listener, LifecycleCallback.NONE);
    }

}
