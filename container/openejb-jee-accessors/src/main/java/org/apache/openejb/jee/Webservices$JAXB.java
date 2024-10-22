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
import static org.apache.openejb.jee.WebserviceDescription$JAXB.readWebserviceDescription;
import static org.apache.openejb.jee.WebserviceDescription$JAXB.writeWebserviceDescription;

@SuppressWarnings({
    "StringEquality"
})
public class Webservices$JAXB
    extends JAXBObject<Webservices>
{


    public Webservices$JAXB() {
        super(Webservices.class, new QName("http://java.sun.com/xml/ns/javaee".intern(), "webservices".intern()), new QName("http://java.sun.com/xml/ns/javaee".intern(), "webservicesType".intern()), Text$JAXB.class, Icon$JAXB.class, WebserviceDescription$JAXB.class);
    }

    public static Webservices readWebservices(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeWebservices(XoXMLStreamWriter writer, Webservices webservices, RuntimeContext context)
        throws Exception
    {
        _write(writer, webservices, context);
    }

    public void write(XoXMLStreamWriter writer, Webservices webservices, RuntimeContext context)
        throws Exception
    {
        _write(writer, webservices, context);
    }

    public static final Webservices _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        Webservices webservices = new Webservices();
        context.beforeUnmarshal(webservices, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        KeyedCollection<String, WebserviceDescription> webserviceDescription = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("webservicesType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Webservices.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, webservices);
                webservices.id = id;
            } else if (("version" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: version
                webservices.version = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"), new QName("", "version"));
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
                    icon = webservices.icon;
                    if (icon!= null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<>();
                    }
                }
                icon.add(iconItem);
            } else if (("webservice-description" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: webserviceDescription
                WebserviceDescription webserviceDescriptionItem = readWebserviceDescription(elementReader, context);
                if (webserviceDescription == null) {
                    webserviceDescription = webservices.webserviceDescription;
                    if (webserviceDescription!= null) {
                        webserviceDescription.clear();
                    } else {
                        webserviceDescription = new KeyedCollection<>();
                    }
                }
                webserviceDescription.add(webserviceDescriptionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "webservice-description"));
            }
        }
        if (descriptions!= null) {
            try {
                webservices.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, Webservices.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames!= null) {
            try {
                webservices.setDisplayNames(displayNames.toArray(new Text[displayNames.size()] ));
            } catch (Exception e) {
                context.setterError(reader, Webservices.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon!= null) {
            webservices.icon = icon;
        }
        if (webserviceDescription!= null) {
            webservices.webserviceDescription = webserviceDescription;
        }

        context.afterUnmarshal(webservices, LifecycleCallback.NONE);

        return webservices;
    }

    public final Webservices read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, Webservices webservices, RuntimeContext context)
        throws Exception
    {
        if (webservices == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Webservices.class!= webservices.getClass()) {
            context.unexpectedSubclass(writer, webservices, Webservices.class);
            return ;
        }

        context.beforeMarshal(webservices, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = webservices.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(webservices, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ATTRIBUTE: version
        String versionRaw = webservices.version;
        if (versionRaw!= null) {
            String version = null;
            try {
                version = Adapters.collapsedStringAdapterAdapter.marshal(versionRaw);
            } catch (Exception e) {
                context.xmlAdapterError(webservices, "version", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "version", version);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = webservices.getDescriptions();
        } catch (Exception e) {
            context.getterError(webservices, "descriptions", Webservices.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webservices, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = webservices.getDisplayNames();
        } catch (Exception e) {
            context.getterError(webservices, "displayNames", Webservices.class, "getDisplayNames", e);
        }
        if (displayNames!= null) {
            for (Text displayNamesItem: displayNames) {
                if (displayNamesItem!= null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webservices, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = webservices.icon;
        if (icon!= null) {
            for (Icon iconItem: icon) {
                if (iconItem!= null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webservices, "icon");
                }
            }
        }

        // ELEMENT: webserviceDescription
        KeyedCollection<String, WebserviceDescription> webserviceDescription = webservices.webserviceDescription;
        if (webserviceDescription!= null) {
            for (WebserviceDescription webserviceDescriptionItem: webserviceDescription) {
                if (webserviceDescriptionItem!= null) {
                    writer.writeStartElement(prefix, "webservice-description", "http://java.sun.com/xml/ns/javaee");
                    writeWebserviceDescription(writer, webserviceDescriptionItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webservices, "webserviceDescription");
                }
            }
        }

        context.afterMarshal(webservices, LifecycleCallback.NONE);
    }

}
