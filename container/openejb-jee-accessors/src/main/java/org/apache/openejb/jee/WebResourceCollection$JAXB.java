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

import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
    "StringEquality"
})
public class WebResourceCollection$JAXB
    extends JAXBObject<WebResourceCollection> {


    public WebResourceCollection$JAXB() {
        super(WebResourceCollection.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "web-resource-collectionType".intern()), Text$JAXB.class);
    }

    public static WebResourceCollection readWebResourceCollection(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeWebResourceCollection(final XoXMLStreamWriter writer, final WebResourceCollection webResourceCollection, final RuntimeContext context)
        throws Exception {
        _write(writer, webResourceCollection, context);
    }

    public void write(final XoXMLStreamWriter writer, final WebResourceCollection webResourceCollection, final RuntimeContext context)
        throws Exception {
        _write(writer, webResourceCollection, context);
    }

    public final static WebResourceCollection _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final WebResourceCollection webResourceCollection = new WebResourceCollection();
        context.beforeUnmarshal(webResourceCollection, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        List<String> urlPattern = null;
        List<String> httpMethod = null;
        List<String> httpMethodOmission = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("web-resource-collectionType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, WebResourceCollection.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, webResourceCollection);
                webResourceCollection.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("web-resource-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: webResourceName
                final String webResourceNameRaw = elementReader.getElementAsString();

                final String webResourceName;
                try {
                    webResourceName = Adapters.collapsedStringAdapterAdapter.unmarshal(webResourceNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                webResourceCollection.webResourceName = webResourceName;
            } else if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                final Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<Text>();
                }
                descriptions.add(descriptionsItem);
            } else if (("url-pattern" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: urlPattern
                final String urlPatternItemRaw = elementReader.getElementAsString();

                final String urlPatternItem;
                try {
                    urlPatternItem = Adapters.trimStringAdapterAdapter.unmarshal(urlPatternItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, TrimStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (urlPattern == null) {
                    urlPattern = webResourceCollection.urlPattern;
                    if (urlPattern != null) {
                        urlPattern.clear();
                    } else {
                        urlPattern = new ArrayList<String>();
                    }
                }
                urlPattern.add(urlPatternItem);
            } else if (("http-method" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: httpMethod
                final String httpMethodItemRaw = elementReader.getElementAsString();

                final String httpMethodItem;
                try {
                    httpMethodItem = Adapters.collapsedStringAdapterAdapter.unmarshal(httpMethodItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (httpMethod == null) {
                    httpMethod = webResourceCollection.httpMethod;
                    if (httpMethod != null) {
                        httpMethod.clear();
                    } else {
                        httpMethod = new ArrayList<String>();
                    }
                }
                httpMethod.add(httpMethodItem);
            } else if (("http-method-omission" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: httpMethodOmission
                final String httpMethodOmissionItemRaw = elementReader.getElementAsString();

                final String httpMethodOmissionItem;
                try {
                    httpMethodOmissionItem = Adapters.collapsedStringAdapterAdapter.unmarshal(httpMethodOmissionItemRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (httpMethodOmission == null) {
                    httpMethodOmission = webResourceCollection.httpMethodOmission;
                    if (httpMethodOmission != null) {
                        httpMethodOmission.clear();
                    } else {
                        httpMethodOmission = new ArrayList<String>();
                    }
                }
                httpMethodOmission.add(httpMethodOmissionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "web-resource-name"), new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "url-pattern"), new QName("http://java.sun.com/xml/ns/javaee", "http-method"), new QName("http://java.sun.com/xml/ns/javaee", "http-method-omission"));
            }
        }
        if (descriptions != null) {
            try {
                webResourceCollection.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (final Exception e) {
                context.setterError(reader, WebResourceCollection.class, "setDescriptions", Text[].class, e);
            }
        }
        if (urlPattern != null) {
            webResourceCollection.urlPattern = urlPattern;
        }
        if (httpMethod != null) {
            webResourceCollection.httpMethod = httpMethod;
        }
        if (httpMethodOmission != null) {
            webResourceCollection.httpMethodOmission = httpMethodOmission;
        }

        context.afterUnmarshal(webResourceCollection, LifecycleCallback.NONE);

        return webResourceCollection;
    }

    public final WebResourceCollection read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final WebResourceCollection webResourceCollection, RuntimeContext context)
        throws Exception {
        if (webResourceCollection == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (WebResourceCollection.class != webResourceCollection.getClass()) {
            context.unexpectedSubclass(writer, webResourceCollection, WebResourceCollection.class);
            return;
        }

        context.beforeMarshal(webResourceCollection, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = webResourceCollection.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(webResourceCollection, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: webResourceName
        final String webResourceNameRaw = webResourceCollection.webResourceName;
        String webResourceName = null;
        try {
            webResourceName = Adapters.collapsedStringAdapterAdapter.marshal(webResourceNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(webResourceCollection, "webResourceName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (webResourceName != null) {
            writer.writeStartElement(prefix, "web-resource-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(webResourceName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(webResourceCollection, "webResourceName");
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = webResourceCollection.getDescriptions();
        } catch (final Exception e) {
            context.getterError(webResourceCollection, "descriptions", WebResourceCollection.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (final Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webResourceCollection, "descriptions");
                }
            }
        }

        // ELEMENT: urlPattern
        final List<String> urlPatternRaw = webResourceCollection.urlPattern;
        if (urlPatternRaw != null) {
            for (final String urlPatternItem : urlPatternRaw) {
                String urlPattern = null;
                try {
                    urlPattern = Adapters.trimStringAdapterAdapter.marshal(urlPatternItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(webResourceCollection, "urlPattern", TrimStringAdapter.class, List.class, List.class, e);
                }
                if (urlPattern != null) {
                    writer.writeStartElement(prefix, "url-pattern", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(urlPattern);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(webResourceCollection, "urlPattern");
                }
            }
        }

        // ELEMENT: httpMethod
        final List<String> httpMethodRaw = webResourceCollection.httpMethod;
        if (httpMethodRaw != null) {
            for (final String httpMethodItem : httpMethodRaw) {
                String httpMethod = null;
                try {
                    httpMethod = Adapters.collapsedStringAdapterAdapter.marshal(httpMethodItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(webResourceCollection, "httpMethod", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (httpMethod != null) {
                    writer.writeStartElement(prefix, "http-method", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(httpMethod);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: httpMethodOmission
        final List<String> httpMethodOmissionRaw = webResourceCollection.httpMethodOmission;
        if (httpMethodOmissionRaw != null) {
            for (final String httpMethodOmissionItem : httpMethodOmissionRaw) {
                String httpMethodOmission = null;
                try {
                    httpMethodOmission = Adapters.collapsedStringAdapterAdapter.marshal(httpMethodOmissionItem);
                } catch (final Exception e) {
                    context.xmlAdapterError(webResourceCollection, "httpMethodOmission", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (httpMethodOmission != null) {
                    writer.writeStartElement(prefix, "http-method-omission", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(httpMethodOmission);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(webResourceCollection, LifecycleCallback.NONE);
    }

}
