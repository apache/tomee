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

@SuppressWarnings({
    "StringEquality"
})
public class FacesClientBehaviorRenderer$JAXB
    extends JAXBObject<FacesClientBehaviorRenderer> {


    public FacesClientBehaviorRenderer$JAXB() {
        super(FacesClientBehaviorRenderer.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-client-behavior-rendererType".intern()));
    }

    public static FacesClientBehaviorRenderer readFacesClientBehaviorRenderer(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesClientBehaviorRenderer(final XoXMLStreamWriter writer, final FacesClientBehaviorRenderer facesClientBehaviorRenderer, final RuntimeContext context)
        throws Exception {
        _write(writer, facesClientBehaviorRenderer, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesClientBehaviorRenderer facesClientBehaviorRenderer, final RuntimeContext context)
        throws Exception {
        _write(writer, facesClientBehaviorRenderer, context);
    }

    public final static FacesClientBehaviorRenderer _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesClientBehaviorRenderer facesClientBehaviorRenderer = new FacesClientBehaviorRenderer();
        context.beforeUnmarshal(facesClientBehaviorRenderer, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-client-behavior-rendererType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesClientBehaviorRenderer.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute);
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("client-behavior-renderer-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: clientBehaviorRendererType
                final String clientBehaviorRendererTypeRaw = elementReader.getElementAsString();

                final String clientBehaviorRendererType;
                try {
                    clientBehaviorRendererType = Adapters.collapsedStringAdapterAdapter.unmarshal(clientBehaviorRendererTypeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesClientBehaviorRenderer.clientBehaviorRendererType = clientBehaviorRendererType;
            } else if (("client-behavior-renderer-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: clientBehaviorRendererClass
                final String clientBehaviorRendererClassRaw = elementReader.getElementAsString();

                final String clientBehaviorRendererClass;
                try {
                    clientBehaviorRendererClass = Adapters.collapsedStringAdapterAdapter.unmarshal(clientBehaviorRendererClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesClientBehaviorRenderer.clientBehaviorRendererClass = clientBehaviorRendererClass;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "client-behavior-renderer-type"), new QName("http://java.sun.com/xml/ns/javaee", "client-behavior-renderer-class"));
            }
        }

        context.afterUnmarshal(facesClientBehaviorRenderer, LifecycleCallback.NONE);

        return facesClientBehaviorRenderer;
    }

    public final FacesClientBehaviorRenderer read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesClientBehaviorRenderer facesClientBehaviorRenderer, RuntimeContext context)
        throws Exception {
        if (facesClientBehaviorRenderer == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesClientBehaviorRenderer.class != facesClientBehaviorRenderer.getClass()) {
            context.unexpectedSubclass(writer, facesClientBehaviorRenderer, FacesClientBehaviorRenderer.class);
            return;
        }

        context.beforeMarshal(facesClientBehaviorRenderer, LifecycleCallback.NONE);


        // ELEMENT: clientBehaviorRendererType
        final String clientBehaviorRendererTypeRaw = facesClientBehaviorRenderer.clientBehaviorRendererType;
        String clientBehaviorRendererType = null;
        try {
            clientBehaviorRendererType = Adapters.collapsedStringAdapterAdapter.marshal(clientBehaviorRendererTypeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesClientBehaviorRenderer, "clientBehaviorRendererType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (clientBehaviorRendererType != null) {
            writer.writeStartElement(prefix, "client-behavior-renderer-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(clientBehaviorRendererType);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesClientBehaviorRenderer, "clientBehaviorRendererType");
        }

        // ELEMENT: clientBehaviorRendererClass
        final String clientBehaviorRendererClassRaw = facesClientBehaviorRenderer.clientBehaviorRendererClass;
        String clientBehaviorRendererClass = null;
        try {
            clientBehaviorRendererClass = Adapters.collapsedStringAdapterAdapter.marshal(clientBehaviorRendererClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesClientBehaviorRenderer, "clientBehaviorRendererClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (clientBehaviorRendererClass != null) {
            writer.writeStartElement(prefix, "client-behavior-renderer-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(clientBehaviorRendererClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesClientBehaviorRenderer, "clientBehaviorRendererClass");
        }

        context.afterMarshal(facesClientBehaviorRenderer, LifecycleCallback.NONE);
    }

}
