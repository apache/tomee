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

@SuppressWarnings({
        "StringEquality"
})
public class TldDeferredValue$JAXB
        extends JAXBObject<TldDeferredValue> {


    public TldDeferredValue$JAXB() {
        super(TldDeferredValue.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "tld-deferred-valueType".intern()));
    }

    public static TldDeferredValue readTldDeferredValue(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeTldDeferredValue(XoXMLStreamWriter writer, TldDeferredValue tldDeferredValue, RuntimeContext context)
            throws Exception {
        _write(writer, tldDeferredValue, context);
    }

    public void write(XoXMLStreamWriter writer, TldDeferredValue tldDeferredValue, RuntimeContext context)
            throws Exception {
        _write(writer, tldDeferredValue, context);
    }

    public final static TldDeferredValue _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        TldDeferredValue tldDeferredValue = new TldDeferredValue();
        context.beforeUnmarshal(tldDeferredValue, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("tld-deferred-valueType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, TldDeferredValue.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, tldDeferredValue);
                tldDeferredValue.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: type
                String typeRaw = elementReader.getElementAsString();

                String type;
                try {
                    type = Adapters.collapsedStringAdapterAdapter.unmarshal(typeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                tldDeferredValue.type = type;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "type"));
            }
        }

        context.afterUnmarshal(tldDeferredValue, LifecycleCallback.NONE);

        return tldDeferredValue;
    }

    public final TldDeferredValue read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, TldDeferredValue tldDeferredValue, RuntimeContext context)
            throws Exception {
        if (tldDeferredValue == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (TldDeferredValue.class != tldDeferredValue.getClass()) {
            context.unexpectedSubclass(writer, tldDeferredValue, TldDeferredValue.class);
            return;
        }

        context.beforeMarshal(tldDeferredValue, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = tldDeferredValue.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(tldDeferredValue, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: type
        String typeRaw = tldDeferredValue.type;
        String type = null;
        try {
            type = Adapters.collapsedStringAdapterAdapter.marshal(typeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(tldDeferredValue, "type", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (type != null) {
            writer.writeStartElementWithAutoPrefix("http://java.sun.com/xml/ns/javaee", "type");
            writer.writeCharacters(type);
            writer.writeEndElement();
        }

        context.afterMarshal(tldDeferredValue, LifecycleCallback.NONE);
    }

}
