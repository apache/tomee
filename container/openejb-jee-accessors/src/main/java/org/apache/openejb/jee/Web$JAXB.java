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
public class Web$JAXB
    extends JAXBObject<Web> {


    public Web$JAXB() {
        super(Web.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "webType".intern()));
    }

    public static Web readWeb(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeWeb(final XoXMLStreamWriter writer, final Web web, final RuntimeContext context)
        throws Exception {
        _write(writer, web, context);
    }

    public void write(final XoXMLStreamWriter writer, final Web web, final RuntimeContext context)
        throws Exception {
        _write(writer, web, context);
    }

    public final static Web _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final Web web = new Web();
        context.beforeUnmarshal(web, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("webType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Web.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, web);
                web.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("web-uri" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: webUri
                final String webUriRaw = elementReader.getElementAsString();

                final String webUri;
                try {
                    webUri = Adapters.collapsedStringAdapterAdapter.unmarshal(webUriRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                web.webUri = webUri;
            } else if (("context-root" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: contextRoot
                final String contextRootRaw = elementReader.getElementAsString();

                final String contextRoot;
                try {
                    contextRoot = Adapters.collapsedStringAdapterAdapter.unmarshal(contextRootRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                web.contextRoot = contextRoot;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "web-uri"), new QName("http://java.sun.com/xml/ns/javaee", "context-root"));
            }
        }

        context.afterUnmarshal(web, LifecycleCallback.NONE);

        return web;
    }

    public final Web read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final Web web, RuntimeContext context)
        throws Exception {
        if (web == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Web.class != web.getClass()) {
            context.unexpectedSubclass(writer, web, Web.class);
            return;
        }

        context.beforeMarshal(web, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = web.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(web, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: webUri
        final String webUriRaw = web.webUri;
        String webUri = null;
        try {
            webUri = Adapters.collapsedStringAdapterAdapter.marshal(webUriRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(web, "webUri", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (webUri != null) {
            writer.writeStartElement(prefix, "web-uri", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(webUri);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(web, "webUri");
        }

        // ELEMENT: contextRoot
        final String contextRootRaw = web.contextRoot;
        String contextRoot = null;
        try {
            contextRoot = Adapters.collapsedStringAdapterAdapter.marshal(contextRootRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(web, "contextRoot", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (contextRoot != null) {
            writer.writeStartElement(prefix, "context-root", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(contextRoot);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(web, "contextRoot");
        }

        context.afterMarshal(web, LifecycleCallback.NONE);
    }

}
