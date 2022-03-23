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

import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;

import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class FacesApplicationResourceBundle$JAXB
    extends JAXBObject<FacesApplicationResourceBundle> {


    public FacesApplicationResourceBundle$JAXB() {
        super(FacesApplicationResourceBundle.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-application-resource-bundleType".intern()), Text$JAXB.class, Icon$JAXB.class);
    }

    public static FacesApplicationResourceBundle readFacesApplicationResourceBundle(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesApplicationResourceBundle(final XoXMLStreamWriter writer, final FacesApplicationResourceBundle facesApplicationResourceBundle, final RuntimeContext context)
        throws Exception {
        _write(writer, facesApplicationResourceBundle, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesApplicationResourceBundle facesApplicationResourceBundle, final RuntimeContext context)
        throws Exception {
        _write(writer, facesApplicationResourceBundle, context);
    }

    public final static FacesApplicationResourceBundle _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesApplicationResourceBundle facesApplicationResourceBundle = new FacesApplicationResourceBundle();
        context.beforeUnmarshal(facesApplicationResourceBundle, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-application-resource-bundleType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesApplicationResourceBundle.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesApplicationResourceBundle);
                facesApplicationResourceBundle.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                final Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<Text>();
                }
                descriptions.add(descriptionsItem);
            } else if (("display-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayNames
                final Text displayNamesItem = readText(elementReader, context);
                if (displayNames == null) {
                    displayNames = new ArrayList<Text>();
                }
                displayNames.add(displayNamesItem);
            } else if (("icon" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                final Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = facesApplicationResourceBundle.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("base-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: baseName
                final String baseNameRaw = elementReader.getElementAsString();

                final String baseName;
                try {
                    baseName = Adapters.collapsedStringAdapterAdapter.unmarshal(baseNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesApplicationResourceBundle.baseName = baseName;
            } else if (("var" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: var
                final String varRaw = elementReader.getElementAsString();

                final String var;
                try {
                    var = Adapters.collapsedStringAdapterAdapter.unmarshal(varRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesApplicationResourceBundle.var = var;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "base-name"), new QName("http://java.sun.com/xml/ns/javaee", "var"));
            }
        }
        if (descriptions != null) {
            try {
                facesApplicationResourceBundle.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, FacesApplicationResourceBundle.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                facesApplicationResourceBundle.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, FacesApplicationResourceBundle.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            facesApplicationResourceBundle.icon = icon;
        }

        context.afterUnmarshal(facesApplicationResourceBundle, LifecycleCallback.NONE);

        return facesApplicationResourceBundle;
    }

    public final FacesApplicationResourceBundle read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesApplicationResourceBundle facesApplicationResourceBundle, RuntimeContext context)
        throws Exception {
        if (facesApplicationResourceBundle == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesApplicationResourceBundle.class != facesApplicationResourceBundle.getClass()) {
            context.unexpectedSubclass(writer, facesApplicationResourceBundle, FacesApplicationResourceBundle.class);
            return;
        }

        context.beforeMarshal(facesApplicationResourceBundle, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = facesApplicationResourceBundle.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(facesApplicationResourceBundle, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = facesApplicationResourceBundle.getDescriptions();
        } catch (final Exception e) {
            context.getterError(facesApplicationResourceBundle, "descriptions", FacesApplicationResourceBundle.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesApplicationResourceBundle, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = facesApplicationResourceBundle.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(facesApplicationResourceBundle, "displayNames", FacesApplicationResourceBundle.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesApplicationResourceBundle, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = facesApplicationResourceBundle.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesApplicationResourceBundle, "icon");
                }
            }
        }

        // ELEMENT: baseName
        final String baseNameRaw = facesApplicationResourceBundle.baseName;
        String baseName = null;
        try {
            baseName = Adapters.collapsedStringAdapterAdapter.marshal(baseNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesApplicationResourceBundle, "baseName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (baseName != null) {
            writer.writeStartElement(prefix, "base-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(baseName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesApplicationResourceBundle, "baseName");
        }

        // ELEMENT: var
        final String varRaw = facesApplicationResourceBundle.var;
        String var = null;
        try {
            var = Adapters.collapsedStringAdapterAdapter.marshal(varRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesApplicationResourceBundle, "var", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (var != null) {
            writer.writeStartElement(prefix, "var", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(var);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesApplicationResourceBundle, "var");
        }

        context.afterMarshal(facesApplicationResourceBundle, LifecycleCallback.NONE);
    }

}
