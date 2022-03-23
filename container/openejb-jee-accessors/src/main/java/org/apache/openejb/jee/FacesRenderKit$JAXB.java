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

import static org.apache.openejb.jee.FacesClientBehaviorRenderer$JAXB.readFacesClientBehaviorRenderer;
import static org.apache.openejb.jee.FacesClientBehaviorRenderer$JAXB.writeFacesClientBehaviorRenderer;
import static org.apache.openejb.jee.FacesRenderKitExtension$JAXB.readFacesRenderKitExtension;
import static org.apache.openejb.jee.FacesRenderKitExtension$JAXB.writeFacesRenderKitExtension;
import static org.apache.openejb.jee.FacesRenderer$JAXB.readFacesRenderer;
import static org.apache.openejb.jee.FacesRenderer$JAXB.writeFacesRenderer;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class FacesRenderKit$JAXB
    extends JAXBObject<FacesRenderKit> {


    public FacesRenderKit$JAXB() {
        super(FacesRenderKit.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-render-kitType".intern()), Text$JAXB.class, Icon$JAXB.class, FacesRenderer$JAXB.class, FacesClientBehaviorRenderer$JAXB.class, FacesRenderKitExtension$JAXB.class);
    }

    public static FacesRenderKit readFacesRenderKit(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesRenderKit(final XoXMLStreamWriter writer, final FacesRenderKit facesRenderKit, final RuntimeContext context)
        throws Exception {
        _write(writer, facesRenderKit, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesRenderKit facesRenderKit, final RuntimeContext context)
        throws Exception {
        _write(writer, facesRenderKit, context);
    }

    public final static FacesRenderKit _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesRenderKit facesRenderKit = new FacesRenderKit();
        context.beforeUnmarshal(facesRenderKit, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<FacesRenderer> renderer = null;
        List<FacesClientBehaviorRenderer> clientBehaviorRenderer = null;
        List<FacesRenderKitExtension> renderKitExtension = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-render-kitType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesRenderKit.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesRenderKit);
                facesRenderKit.id = id;
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
                    icon = facesRenderKit.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("render-kit-id" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: renderKitId
                final String renderKitIdRaw = elementReader.getElementAsString();

                final String renderKitId;
                try {
                    renderKitId = Adapters.collapsedStringAdapterAdapter.unmarshal(renderKitIdRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesRenderKit.renderKitId = renderKitId;
            } else if (("render-kit-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: renderKitClass
                final String renderKitClassRaw = elementReader.getElementAsString();

                final String renderKitClass;
                try {
                    renderKitClass = Adapters.collapsedStringAdapterAdapter.unmarshal(renderKitClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesRenderKit.renderKitClass = renderKitClass;
            } else if (("renderer" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: renderer
                final FacesRenderer rendererItem = readFacesRenderer(elementReader, context);
                if (renderer == null) {
                    renderer = facesRenderKit.renderer;
                    if (renderer != null) {
                        renderer.clear();
                    } else {
                        renderer = new ArrayList<FacesRenderer>();
                    }
                }
                renderer.add(rendererItem);
            } else if (("client-behavior-renderer" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: clientBehaviorRenderer
                final FacesClientBehaviorRenderer clientBehaviorRendererItem = readFacesClientBehaviorRenderer(elementReader, context);
                if (clientBehaviorRenderer == null) {
                    clientBehaviorRenderer = facesRenderKit.clientBehaviorRenderer;
                    if (clientBehaviorRenderer != null) {
                        clientBehaviorRenderer.clear();
                    } else {
                        clientBehaviorRenderer = new ArrayList<FacesClientBehaviorRenderer>();
                    }
                }
                clientBehaviorRenderer.add(clientBehaviorRendererItem);
            } else if (("render-kit-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: renderKitExtension
                final FacesRenderKitExtension renderKitExtensionItem = readFacesRenderKitExtension(elementReader, context);
                if (renderKitExtension == null) {
                    renderKitExtension = facesRenderKit.renderKitExtension;
                    if (renderKitExtension != null) {
                        renderKitExtension.clear();
                    } else {
                        renderKitExtension = new ArrayList<FacesRenderKitExtension>();
                    }
                }
                renderKitExtension.add(renderKitExtensionItem);
            } else {
                // just here ATM to not prevent users to get JSF 2.2 feature because we can't read it
                // TODO: handle it properly
                // context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "render-kit-id"), new QName("http://java.sun.com/xml/ns/javaee", "render-kit-class"), new QName("http://java.sun.com/xml/ns/javaee", "renderer"), new QName("http://java.sun.com/xml/ns/javaee", "client-behavior-renderer"), new QName("http://java.sun.com/xml/ns/javaee", "render-kit-extension"));
            }
        }
        if (descriptions != null) {
            try {
                facesRenderKit.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, FacesRenderKit.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                facesRenderKit.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (final Exception e) {
                context.setterError(reader, FacesRenderKit.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            facesRenderKit.icon = icon;
        }
        if (renderer != null) {
            facesRenderKit.renderer = renderer;
        }
        if (clientBehaviorRenderer != null) {
            facesRenderKit.clientBehaviorRenderer = clientBehaviorRenderer;
        }
        if (renderKitExtension != null) {
            facesRenderKit.renderKitExtension = renderKitExtension;
        }

        context.afterUnmarshal(facesRenderKit, LifecycleCallback.NONE);

        return facesRenderKit;
    }

    public final FacesRenderKit read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesRenderKit facesRenderKit, RuntimeContext context)
        throws Exception {
        if (facesRenderKit == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesRenderKit.class != facesRenderKit.getClass()) {
            context.unexpectedSubclass(writer, facesRenderKit, FacesRenderKit.class);
            return;
        }

        context.beforeMarshal(facesRenderKit, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = facesRenderKit.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(facesRenderKit, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = facesRenderKit.getDescriptions();
        } catch (final Exception e) {
            context.getterError(facesRenderKit, "descriptions", FacesRenderKit.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesRenderKit, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = facesRenderKit.getDisplayNames();
        } catch (final Exception e) {
            context.getterError(facesRenderKit, "displayNames", FacesRenderKit.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (final Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesRenderKit, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        final LocalCollection<Icon> icon = facesRenderKit.icon;
        if (icon != null) {
            for (final Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesRenderKit, "icon");
                }
            }
        }

        // ELEMENT: renderKitId
        final String renderKitIdRaw = facesRenderKit.renderKitId;
        String renderKitId = null;
        try {
            renderKitId = Adapters.collapsedStringAdapterAdapter.marshal(renderKitIdRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesRenderKit, "renderKitId", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (renderKitId != null) {
            writer.writeStartElement(prefix, "render-kit-id", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(renderKitId);
            writer.writeEndElement();
        }

        // ELEMENT: renderKitClass
        final String renderKitClassRaw = facesRenderKit.renderKitClass;
        String renderKitClass = null;
        try {
            renderKitClass = Adapters.collapsedStringAdapterAdapter.marshal(renderKitClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesRenderKit, "renderKitClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (renderKitClass != null) {
            writer.writeStartElement(prefix, "render-kit-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(renderKitClass);
            writer.writeEndElement();
        }

        // ELEMENT: renderer
        final List<FacesRenderer> renderer = facesRenderKit.renderer;
        if (renderer != null) {
            for (final FacesRenderer rendererItem : renderer) {
                writer.writeStartElement(prefix, "renderer", "http://java.sun.com/xml/ns/javaee");
                if (rendererItem != null) {
                    writeFacesRenderer(writer, rendererItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: clientBehaviorRenderer
        final List<FacesClientBehaviorRenderer> clientBehaviorRenderer = facesRenderKit.clientBehaviorRenderer;
        if (clientBehaviorRenderer != null) {
            for (final FacesClientBehaviorRenderer clientBehaviorRendererItem : clientBehaviorRenderer) {
                if (clientBehaviorRendererItem != null) {
                    writer.writeStartElement(prefix, "client-behavior-renderer", "http://java.sun.com/xml/ns/javaee");
                    writeFacesClientBehaviorRenderer(writer, clientBehaviorRendererItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: renderKitExtension
        final List<FacesRenderKitExtension> renderKitExtension = facesRenderKit.renderKitExtension;
        if (renderKitExtension != null) {
            for (final FacesRenderKitExtension renderKitExtensionItem : renderKitExtension) {
                if (renderKitExtensionItem != null) {
                    writer.writeStartElement(prefix, "render-kit-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesRenderKitExtension(writer, renderKitExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesRenderKit, LifecycleCallback.NONE);
    }

}
