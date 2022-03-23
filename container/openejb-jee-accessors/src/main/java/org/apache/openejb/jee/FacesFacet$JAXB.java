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
import java.util.List;

import static org.apache.openejb.jee.FacesFacetExtension$JAXB.readFacesFacetExtension;
import static org.apache.openejb.jee.FacesFacetExtension$JAXB.writeFacesFacetExtension;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class FacesFacet$JAXB
    extends JAXBObject<FacesFacet> {


    public FacesFacet$JAXB() {
        super(FacesFacet.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-facetType".intern()), Text$JAXB.class, Icon$JAXB.class, FacesFacetExtension$JAXB.class);
    }

    public static FacesFacet readFacesFacet(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesFacet(final XoXMLStreamWriter writer, final FacesFacet facesFacet, final RuntimeContext context)
        throws Exception {
        _write(writer, facesFacet, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesFacet facesFacet, final RuntimeContext context)
        throws Exception {
        _write(writer, facesFacet, context);
    }

    public final static FacesFacet _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesFacet facesFacet = new FacesFacet();
        context.beforeUnmarshal(facesFacet, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<FacesFacetExtension> facetExtension = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-facetType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesFacet.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesFacet);
                facesFacet.id = id;
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
                    icon = facesFacet.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("facet-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: facetName
                final String facetNameRaw = elementReader.getElementAsString();

                final String facetName;
                try {
                    facetName = Adapters.collapsedStringAdapterAdapter.unmarshal(facetNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesFacet.facetName = facetName;
            } else if (("facet-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: facetExtension
                final FacesFacetExtension facetExtensionItem = readFacesFacetExtension(elementReader, context);
                if (facetExtension == null) {
                    facetExtension = facesFacet.facetExtension;
                    if (facetExtension != null) {
                        facetExtension.clear();
                    } else {
                        facetExtension = new ArrayList<FacesFacetExtension>();
                    }
                }
                facetExtension.add(facetExtensionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "facet-name"), new QName("http://java.sun.com/xml/ns/javaee", "facet-extension"));
            }
        }
        if (descriptions != null) {
            try {
                facesFacet.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, FacesFacet.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                facesFacet.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, FacesFacet.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            facesFacet.icon = icon;
        }
        if (facetExtension != null) {
            facesFacet.facetExtension = facetExtension;
        }

        context.afterUnmarshal(facesFacet, LifecycleCallback.NONE);

        return facesFacet;
    }

    public final FacesFacet read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesFacet facesFacet, RuntimeContext context)
        throws Exception {
        if (facesFacet == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesFacet.class != facesFacet.getClass()) {
            context.unexpectedSubclass(writer, facesFacet, FacesFacet.class);
            return;
        }

        context.beforeMarshal(facesFacet, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = facesFacet.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(facesFacet, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = facesFacet.getDescriptions();
        } catch (final Exception e) {
            context.getterError(facesFacet, "descriptions", FacesFacet.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesFacet, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = facesFacet.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(facesFacet, "displayNames", FacesFacet.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesFacet, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = facesFacet.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesFacet, "icon");
                }
            }
        }

        // ELEMENT: facetName
        final String facetNameRaw = facesFacet.facetName;
        String facetName = null;
        try {
            facetName = Adapters.collapsedStringAdapterAdapter.marshal(facetNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesFacet, "facetName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (facetName != null) {
            writer.writeStartElement(prefix, "facet-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(facetName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesFacet, "facetName");
        }

        // ELEMENT: facetExtension
        final List<FacesFacetExtension> facetExtension = facesFacet.facetExtension;
        if (facetExtension != null) {
            for (final FacesFacetExtension facetExtensionItem : facetExtension) {
                if (facetExtensionItem != null) {
                    writer.writeStartElement(prefix, "facet-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesFacetExtension(writer, facetExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesFacet, LifecycleCallback.NONE);
    }

}
