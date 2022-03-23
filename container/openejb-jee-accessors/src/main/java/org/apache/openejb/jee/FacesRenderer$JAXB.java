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

import static org.apache.openejb.jee.FacesAttribute$JAXB.readFacesAttribute;
import static org.apache.openejb.jee.FacesAttribute$JAXB.writeFacesAttribute;
import static org.apache.openejb.jee.FacesFacet$JAXB.readFacesFacet;
import static org.apache.openejb.jee.FacesFacet$JAXB.writeFacesFacet;
import static org.apache.openejb.jee.FacesRendererExtension$JAXB.readFacesRendererExtension;
import static org.apache.openejb.jee.FacesRendererExtension$JAXB.writeFacesRendererExtension;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class FacesRenderer$JAXB
    extends JAXBObject<FacesRenderer> {


    public FacesRenderer$JAXB() {
        super(FacesRenderer.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-rendererType".intern()), Text$JAXB.class, Icon$JAXB.class, FacesFacet$JAXB.class, FacesAttribute$JAXB.class, FacesRendererExtension$JAXB.class);
    }

    public static FacesRenderer readFacesRenderer(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesRenderer(final XoXMLStreamWriter writer, final FacesRenderer facesRenderer, final RuntimeContext context)
        throws Exception {
        _write(writer, facesRenderer, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesRenderer facesRenderer, final RuntimeContext context)
        throws Exception {
        _write(writer, facesRenderer, context);
    }

    public final static FacesRenderer _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesRenderer facesRenderer = new FacesRenderer();
        context.beforeUnmarshal(facesRenderer, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<FacesFacet> facet = null;
        List<FacesAttribute> attribute1 = null;
        List<FacesRendererExtension> rendererExtension = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-rendererType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesRenderer.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesRenderer);
                facesRenderer.id = id;
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
                    icon = facesRenderer.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("component-family" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: componentFamily
                final String componentFamilyRaw = elementReader.getElementAsString();

                final String componentFamily;
                try {
                    componentFamily = Adapters.collapsedStringAdapterAdapter.unmarshal(componentFamilyRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesRenderer.componentFamily = componentFamily;
            } else if (("renderer-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: rendererType
                final String rendererTypeRaw = elementReader.getElementAsString();

                final String rendererType;
                try {
                    rendererType = Adapters.collapsedStringAdapterAdapter.unmarshal(rendererTypeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesRenderer.rendererType = rendererType;
            } else if (("renderer-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: rendererClass
                final String rendererClassRaw = elementReader.getElementAsString();

                final String rendererClass;
                try {
                    rendererClass = Adapters.collapsedStringAdapterAdapter.unmarshal(rendererClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesRenderer.rendererClass = rendererClass;
            } else if (("facet" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: facet
                final FacesFacet facetItem = readFacesFacet(elementReader, context);
                if (facet == null) {
                    facet = facesRenderer.facet;
                    if (facet != null) {
                        facet.clear();
                    } else {
                        facet = new ArrayList<FacesFacet>();
                    }
                }
                facet.add(facetItem);
            } else if (("attribute" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: attribute
                final FacesAttribute attributeItem = readFacesAttribute(elementReader, context);
                if (attribute1 == null) {
                    attribute1 = facesRenderer.attribute;
                    if (attribute1 != null) {
                        attribute1.clear();
                    } else {
                        attribute1 = new ArrayList<FacesAttribute>();
                    }
                }
                attribute1.add(attributeItem);
            } else if (("renderer-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: rendererExtension
                final FacesRendererExtension rendererExtensionItem = readFacesRendererExtension(elementReader, context);
                if (rendererExtension == null) {
                    rendererExtension = facesRenderer.rendererExtension;
                    if (rendererExtension != null) {
                        rendererExtension.clear();
                    } else {
                        rendererExtension = new ArrayList<FacesRendererExtension>();
                    }
                }
                rendererExtension.add(rendererExtensionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "component-family"), new QName("http://java.sun.com/xml/ns/javaee", "renderer-type"), new QName("http://java.sun.com/xml/ns/javaee", "renderer-class"), new QName("http://java.sun.com/xml/ns/javaee", "facet"), new QName("http://java.sun.com/xml/ns/javaee", "attribute"), new QName("http://java.sun.com/xml/ns/javaee", "renderer-extension"));
            }
        }
        if (descriptions != null) {
            try {
                facesRenderer.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, FacesRenderer.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                facesRenderer.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, FacesRenderer.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            facesRenderer.icon = icon;
        }
        if (facet != null) {
            facesRenderer.facet = facet;
        }
        if (attribute1 != null) {
            facesRenderer.attribute = attribute1;
        }
        if (rendererExtension != null) {
            facesRenderer.rendererExtension = rendererExtension;
        }

        context.afterUnmarshal(facesRenderer, LifecycleCallback.NONE);

        return facesRenderer;
    }

    public final FacesRenderer read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesRenderer facesRenderer, RuntimeContext context)
        throws Exception {
        if (facesRenderer == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesRenderer.class != facesRenderer.getClass()) {
            context.unexpectedSubclass(writer, facesRenderer, FacesRenderer.class);
            return;
        }

        context.beforeMarshal(facesRenderer, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = facesRenderer.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(facesRenderer, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = facesRenderer.getDescriptions();
        } catch (final Exception e) {
            context.getterError(facesRenderer, "descriptions", FacesRenderer.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesRenderer, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = facesRenderer.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(facesRenderer, "displayNames", FacesRenderer.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesRenderer, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = facesRenderer.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesRenderer, "icon");
                }
            }
        }

        // ELEMENT: componentFamily
        final String componentFamilyRaw = facesRenderer.componentFamily;
        String componentFamily = null;
        try {
            componentFamily = Adapters.collapsedStringAdapterAdapter.marshal(componentFamilyRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesRenderer, "componentFamily", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (componentFamily != null) {
            writer.writeStartElement(prefix, "component-family", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(componentFamily);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesRenderer, "componentFamily");
        }

        // ELEMENT: rendererType
        final String rendererTypeRaw = facesRenderer.rendererType;
        String rendererType = null;
        try {
            rendererType = Adapters.collapsedStringAdapterAdapter.marshal(rendererTypeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesRenderer, "rendererType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (rendererType != null) {
            writer.writeStartElement(prefix, "renderer-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(rendererType);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesRenderer, "rendererType");
        }

        // ELEMENT: rendererClass
        final String rendererClassRaw = facesRenderer.rendererClass;
        String rendererClass = null;
        try {
            rendererClass = Adapters.collapsedStringAdapterAdapter.marshal(rendererClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesRenderer, "rendererClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (rendererClass != null) {
            writer.writeStartElement(prefix, "renderer-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(rendererClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesRenderer, "rendererClass");
        }

        // ELEMENT: facet
        final List<FacesFacet> facet = facesRenderer.facet;
        if (facet != null) {
            for (final FacesFacet facetItem : facet) {
                writer.writeStartElement(prefix, "facet", "http://java.sun.com/xml/ns/javaee");
                if (facetItem != null) {
                    writeFacesFacet(writer, facetItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: attribute
        final List<FacesAttribute> attribute = facesRenderer.attribute;
        if (attribute != null) {
            for (final FacesAttribute attributeItem : attribute) {
                writer.writeStartElement(prefix, "attribute", "http://java.sun.com/xml/ns/javaee");
                if (attributeItem != null) {
                    writeFacesAttribute(writer, attributeItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: rendererExtension
        final List<FacesRendererExtension> rendererExtension = facesRenderer.rendererExtension;
        if (rendererExtension != null) {
            for (final FacesRendererExtension rendererExtensionItem : rendererExtension) {
                if (rendererExtensionItem != null) {
                    writer.writeStartElement(prefix, "renderer-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesRendererExtension(writer, rendererExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesRenderer, LifecycleCallback.NONE);
    }

}
