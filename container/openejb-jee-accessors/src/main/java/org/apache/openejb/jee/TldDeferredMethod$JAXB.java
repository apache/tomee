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
public class TldDeferredMethod$JAXB
    extends JAXBObject<TldDeferredMethod> {


    public TldDeferredMethod$JAXB() {
        super(TldDeferredMethod.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "tld-deferred-methodType".intern()));
    }

    public static TldDeferredMethod readTldDeferredMethod(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeTldDeferredMethod(final XoXMLStreamWriter writer, final TldDeferredMethod tldDeferredMethod, final RuntimeContext context)
        throws Exception {
        _write(writer, tldDeferredMethod, context);
    }

    public void write(final XoXMLStreamWriter writer, final TldDeferredMethod tldDeferredMethod, final RuntimeContext context)
        throws Exception {
        _write(writer, tldDeferredMethod, context);
    }

    public final static TldDeferredMethod _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final TldDeferredMethod tldDeferredMethod = new TldDeferredMethod();
        context.beforeUnmarshal(tldDeferredMethod, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("tld-deferred-methodType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, TldDeferredMethod.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, tldDeferredMethod);
                tldDeferredMethod.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("method-signature" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: methodSignature
                final String methodSignatureRaw = elementReader.getElementAsString();

                final String methodSignature;
                try {
                    methodSignature = Adapters.collapsedStringAdapterAdapter.unmarshal(methodSignatureRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                tldDeferredMethod.methodSignature = methodSignature;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "method-signature"));
            }
        }

        context.afterUnmarshal(tldDeferredMethod, LifecycleCallback.NONE);

        return tldDeferredMethod;
    }

    public final TldDeferredMethod read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final TldDeferredMethod tldDeferredMethod, RuntimeContext context)
        throws Exception {
        if (tldDeferredMethod == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (TldDeferredMethod.class != tldDeferredMethod.getClass()) {
            context.unexpectedSubclass(writer, tldDeferredMethod, TldDeferredMethod.class);
            return;
        }

        context.beforeMarshal(tldDeferredMethod, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = tldDeferredMethod.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(tldDeferredMethod, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: methodSignature
        final String methodSignatureRaw = tldDeferredMethod.methodSignature;
        String methodSignature = null;
        try {
            methodSignature = Adapters.collapsedStringAdapterAdapter.marshal(methodSignatureRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(tldDeferredMethod, "methodSignature", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (methodSignature != null) {
            writer.writeStartElementWithAutoPrefix("http://java.sun.com/xml/ns/javaee", "method-signature");
            writer.writeCharacters(methodSignature);
            writer.writeEndElement();
        }

        context.afterMarshal(tldDeferredMethod, LifecycleCallback.NONE);
    }

}
