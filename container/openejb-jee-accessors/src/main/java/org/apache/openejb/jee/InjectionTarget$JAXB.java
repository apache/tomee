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
public class InjectionTarget$JAXB
    extends JAXBObject<InjectionTarget> {


    public InjectionTarget$JAXB() {
        super(InjectionTarget.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "injection-targetType".intern()));
    }

    public static InjectionTarget readInjectionTarget(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeInjectionTarget(final XoXMLStreamWriter writer, final InjectionTarget injectionTarget, final RuntimeContext context)
        throws Exception {
        _write(writer, injectionTarget, context);
    }

    public void write(final XoXMLStreamWriter writer, final InjectionTarget injectionTarget, final RuntimeContext context)
        throws Exception {
        _write(writer, injectionTarget, context);
    }

    public final static InjectionTarget _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final InjectionTarget injectionTarget = new InjectionTarget();
        context.beforeUnmarshal(injectionTarget, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("injection-targetType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, InjectionTarget.class);
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
            if (("injection-target-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: injectionTargetClass
                final String injectionTargetClassRaw = elementReader.getElementAsString();

                final String injectionTargetClass;
                try {
                    injectionTargetClass = Adapters.collapsedStringAdapterAdapter.unmarshal(injectionTargetClassRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                injectionTarget.injectionTargetClass = injectionTargetClass;
            } else if (("injection-target-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: injectionTargetName
                final String injectionTargetNameRaw = elementReader.getElementAsString();

                final String injectionTargetName;
                try {
                    injectionTargetName = Adapters.collapsedStringAdapterAdapter.unmarshal(injectionTargetNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                injectionTarget.injectionTargetName = injectionTargetName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "injection-target-class"), new QName("http://java.sun.com/xml/ns/javaee", "injection-target-name"));
            }
        }

        context.afterUnmarshal(injectionTarget, LifecycleCallback.NONE);

        return injectionTarget;
    }

    public final InjectionTarget read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final InjectionTarget injectionTarget, RuntimeContext context)
        throws Exception {
        if (injectionTarget == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (InjectionTarget.class != injectionTarget.getClass()) {
            context.unexpectedSubclass(writer, injectionTarget, InjectionTarget.class);
            return;
        }

        context.beforeMarshal(injectionTarget, LifecycleCallback.NONE);


        // ELEMENT: injectionTargetClass
        final String injectionTargetClassRaw = injectionTarget.injectionTargetClass;
        String injectionTargetClass = null;
        try {
            injectionTargetClass = Adapters.collapsedStringAdapterAdapter.marshal(injectionTargetClassRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(injectionTarget, "injectionTargetClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (injectionTargetClass != null) {
            writer.writeStartElement(prefix, "injection-target-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(injectionTargetClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(injectionTarget, "injectionTargetClass");
        }

        // ELEMENT: injectionTargetName
        final String injectionTargetNameRaw = injectionTarget.injectionTargetName;
        String injectionTargetName = null;
        try {
            injectionTargetName = Adapters.collapsedStringAdapterAdapter.marshal(injectionTargetNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(injectionTarget, "injectionTargetName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (injectionTargetName != null) {
            writer.writeStartElement(prefix, "injection-target-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(injectionTargetName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(injectionTarget, "injectionTargetName");
        }

        context.afterMarshal(injectionTarget, LifecycleCallback.NONE);
    }

}
