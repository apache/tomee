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
public class LifecycleCallback$JAXB
    extends JAXBObject<org.apache.openejb.jee.LifecycleCallback> {


    public LifecycleCallback$JAXB() {
        super(org.apache.openejb.jee.LifecycleCallback.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "lifecycle-callbackType".intern()));
    }

    public static org.apache.openejb.jee.LifecycleCallback readLifecycleCallback(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeLifecycleCallback(final XoXMLStreamWriter writer, final org.apache.openejb.jee.LifecycleCallback lifecycleCallback, final RuntimeContext context)
        throws Exception {
        _write(writer, lifecycleCallback, context);
    }

    public void write(final XoXMLStreamWriter writer, final org.apache.openejb.jee.LifecycleCallback lifecycleCallback, final RuntimeContext context)
        throws Exception {
        _write(writer, lifecycleCallback, context);
    }

    public final static org.apache.openejb.jee.LifecycleCallback _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final org.apache.openejb.jee.LifecycleCallback lifecycleCallback = new org.apache.openejb.jee.LifecycleCallback();
        context.beforeUnmarshal(lifecycleCallback, org.metatype.sxc.jaxb.LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("lifecycle-callbackType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, org.apache.openejb.jee.LifecycleCallback.class);
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
            if (("lifecycle-callback-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: lifecycleCallbackClass
                final String lifecycleCallbackClassRaw = elementReader.getElementAsString();

                final String lifecycleCallbackClass;
                try {
                    lifecycleCallbackClass = Adapters.collapsedStringAdapterAdapter.unmarshal(lifecycleCallbackClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                lifecycleCallback.lifecycleCallbackClass = lifecycleCallbackClass;
            } else if (("lifecycle-callback-method" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: lifecycleCallbackMethod
                final String lifecycleCallbackMethodRaw = elementReader.getElementAsString();

                final String lifecycleCallbackMethod;
                try {
                    lifecycleCallbackMethod = Adapters.collapsedStringAdapterAdapter.unmarshal(lifecycleCallbackMethodRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                lifecycleCallback.lifecycleCallbackMethod = lifecycleCallbackMethod;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "lifecycle-callback-class"), new QName("http://java.sun.com/xml/ns/javaee", "lifecycle-callback-method"));
            }
        }

        context.afterUnmarshal(lifecycleCallback, org.metatype.sxc.jaxb.LifecycleCallback.NONE);

        return lifecycleCallback;
    }

    public final org.apache.openejb.jee.LifecycleCallback read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final org.apache.openejb.jee.LifecycleCallback lifecycleCallback, RuntimeContext context)
        throws Exception {
        if (lifecycleCallback == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (org.apache.openejb.jee.LifecycleCallback.class != lifecycleCallback.getClass()) {
            context.unexpectedSubclass(writer, lifecycleCallback, org.apache.openejb.jee.LifecycleCallback.class);
            return;
        }

        context.beforeMarshal(lifecycleCallback, org.metatype.sxc.jaxb.LifecycleCallback.NONE);


        // ELEMENT: lifecycleCallbackClass
        final String lifecycleCallbackClassRaw = lifecycleCallback.lifecycleCallbackClass;
        String lifecycleCallbackClass = null;
        try {
            lifecycleCallbackClass = Adapters.collapsedStringAdapterAdapter.marshal(lifecycleCallbackClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(lifecycleCallback, "lifecycleCallbackClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (lifecycleCallbackClass != null) {
            writer.writeStartElement(prefix, "lifecycle-callback-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(lifecycleCallbackClass);
            writer.writeEndElement();
        }

        // ELEMENT: lifecycleCallbackMethod
        final String lifecycleCallbackMethodRaw = lifecycleCallback.lifecycleCallbackMethod;
        String lifecycleCallbackMethod = null;
        try {
            lifecycleCallbackMethod = Adapters.collapsedStringAdapterAdapter.marshal(lifecycleCallbackMethodRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(lifecycleCallback, "lifecycleCallbackMethod", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (lifecycleCallbackMethod != null) {
            writer.writeStartElement(prefix, "lifecycle-callback-method", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(lifecycleCallbackMethod);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(lifecycleCallback, "lifecycleCallbackMethod");
        }

        context.afterMarshal(lifecycleCallback, org.metatype.sxc.jaxb.LifecycleCallback.NONE);
    }

}
