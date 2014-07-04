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
import javax.xml.namespace.QName;

@SuppressWarnings({
    "StringEquality"
})
public class RespectBinding$JAXB
    extends JAXBObject<RespectBinding> {


    public RespectBinding$JAXB() {
        super(RespectBinding.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "respect-bindingType".intern()));
    }

    public static RespectBinding readRespectBinding(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeRespectBinding(final XoXMLStreamWriter writer, final RespectBinding respectBinding, final RuntimeContext context)
        throws Exception {
        _write(writer, respectBinding, context);
    }

    public void write(final XoXMLStreamWriter writer, final RespectBinding respectBinding, final RuntimeContext context)
        throws Exception {
        _write(writer, respectBinding, context);
    }

    public final static RespectBinding _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final RespectBinding respectBinding = new RespectBinding();
        context.beforeUnmarshal(respectBinding, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("respect-bindingType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, RespectBinding.class);
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
            if (("enabled" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: enabled
                final Boolean enabled = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                respectBinding.enabled = enabled;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "enabled"));
            }
        }

        context.afterUnmarshal(respectBinding, LifecycleCallback.NONE);

        return respectBinding;
    }

    public final RespectBinding read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final RespectBinding respectBinding, RuntimeContext context)
        throws Exception {
        if (respectBinding == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (RespectBinding.class != respectBinding.getClass()) {
            context.unexpectedSubclass(writer, respectBinding, RespectBinding.class);
            return;
        }

        context.beforeMarshal(respectBinding, LifecycleCallback.NONE);


        // ELEMENT: enabled
        final Boolean enabled = respectBinding.enabled;
        if (enabled != null) {
            writer.writeStartElementWithAutoPrefix("http://java.sun.com/xml/ns/javaee", "enabled");
            writer.writeCharacters(Boolean.toString(enabled));
            writer.writeEndElement();
        }

        context.afterMarshal(respectBinding, LifecycleCallback.NONE);
    }

}
