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

import static org.apache.openejb.jee.NamedMethod$JAXB.readNamedMethod;
import static org.apache.openejb.jee.NamedMethod$JAXB.writeNamedMethod;

@SuppressWarnings({
    "StringEquality"
})
public class RemoveMethod$JAXB
    extends JAXBObject<RemoveMethod> {


    public RemoveMethod$JAXB() {
        super(RemoveMethod.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "remove-methodType".intern()), NamedMethod$JAXB.class);
    }

    public static RemoveMethod readRemoveMethod(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeRemoveMethod(final XoXMLStreamWriter writer, final RemoveMethod removeMethod, final RuntimeContext context)
        throws Exception {
        _write(writer, removeMethod, context);
    }

    public void write(final XoXMLStreamWriter writer, final RemoveMethod removeMethod, final RuntimeContext context)
        throws Exception {
        _write(writer, removeMethod, context);
    }

    public final static RemoveMethod _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final RemoveMethod removeMethod = new RemoveMethod();
        context.beforeUnmarshal(removeMethod, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("remove-methodType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, RemoveMethod.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, removeMethod);
                removeMethod.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("bean-method" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: beanMethod
                final NamedMethod beanMethod = readNamedMethod(elementReader, context);
                removeMethod.beanMethod = beanMethod;
            } else if (("retain-if-exception" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: retainIfException
                final Boolean retainIfException = ("1".equals(elementReader.getElementAsString()) || "true".equals(elementReader.getElementAsString()));
                removeMethod.retainIfException = retainIfException;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "bean-method"), new QName("http://java.sun.com/xml/ns/javaee", "retain-if-exception"));
            }
        }

        context.afterUnmarshal(removeMethod, LifecycleCallback.NONE);

        return removeMethod;
    }

    public final RemoveMethod read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final RemoveMethod removeMethod, RuntimeContext context)
        throws Exception {
        if (removeMethod == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (RemoveMethod.class != removeMethod.getClass()) {
            context.unexpectedSubclass(writer, removeMethod, RemoveMethod.class);
            return;
        }

        context.beforeMarshal(removeMethod, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = removeMethod.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(removeMethod, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: beanMethod
        final NamedMethod beanMethod = removeMethod.beanMethod;
        if (beanMethod != null) {
            writer.writeStartElement(prefix, "bean-method", "http://java.sun.com/xml/ns/javaee");
            writeNamedMethod(writer, beanMethod, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(removeMethod, "beanMethod");
        }

        // ELEMENT: retainIfException
        final Boolean retainIfException = removeMethod.retainIfException;
        if (retainIfException != null) {
            writer.writeStartElement(prefix, "retain-if-exception", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(retainIfException));
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(removeMethod, "retainIfException");
        }

        context.afterMarshal(removeMethod, LifecycleCallback.NONE);
    }

}
