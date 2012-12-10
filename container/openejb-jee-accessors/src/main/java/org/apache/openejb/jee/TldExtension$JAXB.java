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

import com.envoisolutions.sxc.jaxb.JAXBObject;
import com.envoisolutions.sxc.jaxb.LifecycleCallback;
import com.envoisolutions.sxc.jaxb.RuntimeContext;
import com.envoisolutions.sxc.util.Attribute;
import com.envoisolutions.sxc.util.XoXMLStreamReader;
import com.envoisolutions.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({
        "StringEquality"
})
public class TldExtension$JAXB
        extends JAXBObject<TldExtension> {


    public TldExtension$JAXB() {
        super(TldExtension.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "tld-extensionType".intern()));
    }

    public static TldExtension readTldExtension(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeTldExtension(XoXMLStreamWriter writer, TldExtension tldExtension, RuntimeContext context)
            throws Exception {
        _write(writer, tldExtension, context);
    }

    public void write(XoXMLStreamWriter writer, TldExtension tldExtension, RuntimeContext context)
            throws Exception {
        _write(writer, tldExtension, context);
    }

    public final static TldExtension _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        TldExtension tldExtension = new TldExtension();
        context.beforeUnmarshal(tldExtension, LifecycleCallback.NONE);

        List<String> extensionElement = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("tld-extensionType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, TldExtension.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, tldExtension);
                tldExtension.id = id;
            } else if (("namespace" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: namespace
                tldExtension.namespace = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"), new QName("", "namespace"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("extension-element" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: extensionElement
                String extensionElementItemRaw = elementReader.getElementAsString();

                String extensionElementItem;
                try {
                    extensionElementItem = Adapters.collapsedStringAdapterAdapter.unmarshal(extensionElementItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (extensionElement == null) {
                    extensionElement = tldExtension.extensionElement;
                    if (extensionElement != null) {
                        extensionElement.clear();
                    } else {
                        extensionElement = new ArrayList<String>();
                    }
                }
                extensionElement.add(extensionElementItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "extension-element"));
            }
        }
        if (extensionElement != null) {
            tldExtension.extensionElement = extensionElement;
        }

        context.afterUnmarshal(tldExtension, LifecycleCallback.NONE);

        return tldExtension;
    }

    public final TldExtension read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, TldExtension tldExtension, RuntimeContext context)
            throws Exception {
        if (tldExtension == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (TldExtension.class != tldExtension.getClass()) {
            context.unexpectedSubclass(writer, tldExtension, TldExtension.class);
            return;
        }

        context.beforeMarshal(tldExtension, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = tldExtension.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(tldExtension, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ATTRIBUTE: namespace
        String namespaceRaw = tldExtension.namespace;
        if (namespaceRaw != null) {
            String namespace = null;
            try {
                namespace = Adapters.collapsedStringAdapterAdapter.marshal(namespaceRaw);
            } catch (Exception e) {
                context.xmlAdapterError(tldExtension, "namespace", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "namespace", namespace);
        }

        // ELEMENT: extensionElement
        List<String> extensionElementRaw = tldExtension.extensionElement;
        if (extensionElementRaw != null) {
            for (String extensionElementItem : extensionElementRaw) {
                String extensionElement = null;
                try {
                    extensionElement = Adapters.collapsedStringAdapterAdapter.marshal(extensionElementItem);
                } catch (Exception e) {
                    context.xmlAdapterError(tldExtension, "extensionElement", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (extensionElement != null) {
                    writer.writeStartElementWithAutoPrefix("http://java.sun.com/xml/ns/javaee", "extension-element");
                    writer.writeCharacters(extensionElement);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(tldExtension, "extensionElement");
                }
            }
        }

        context.afterMarshal(tldExtension, LifecycleCallback.NONE);
    }

}
