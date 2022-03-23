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

import static org.apache.openejb.jee.ConstructorParameterOrder$JAXB.readConstructorParameterOrder;
import static org.apache.openejb.jee.ConstructorParameterOrder$JAXB.writeConstructorParameterOrder;

@SuppressWarnings({
    "StringEquality"
})
public class ExceptionMapping$JAXB
    extends JAXBObject<ExceptionMapping> {


    public ExceptionMapping$JAXB() {
        super(ExceptionMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "exception-mappingType".intern()), ConstructorParameterOrder$JAXB.class);
    }

    public static ExceptionMapping readExceptionMapping(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeExceptionMapping(final XoXMLStreamWriter writer, final ExceptionMapping exceptionMapping, final RuntimeContext context)
        throws Exception {
        _write(writer, exceptionMapping, context);
    }

    public void write(final XoXMLStreamWriter writer, final ExceptionMapping exceptionMapping, final RuntimeContext context)
        throws Exception {
        _write(writer, exceptionMapping, context);
    }

    public final static ExceptionMapping _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final ExceptionMapping exceptionMapping = new ExceptionMapping();
        context.beforeUnmarshal(exceptionMapping, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("exception-mappingType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ExceptionMapping.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, exceptionMapping);
                exceptionMapping.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("exception-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: exceptionType
                final String exceptionTypeRaw = elementReader.getElementAsString();

                final String exceptionType;
                try {
                    exceptionType = Adapters.collapsedStringAdapterAdapter.unmarshal(exceptionTypeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                exceptionMapping.exceptionType = exceptionType;
            } else if (("wsdl-message" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: wsdlMessage
                final QName wsdlMessage = elementReader.getElementAsQName();
                exceptionMapping.wsdlMessage = wsdlMessage;
            } else if (("wsdl-message-part-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: wsdlMessagePartName
                final String wsdlMessagePartNameRaw = elementReader.getElementAsString();

                final String wsdlMessagePartName;
                try {
                    wsdlMessagePartName = Adapters.collapsedStringAdapterAdapter.unmarshal(wsdlMessagePartNameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                exceptionMapping.wsdlMessagePartName = wsdlMessagePartName;
            } else if (("constructor-parameter-order" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: constructorParameterOrder
                final ConstructorParameterOrder constructorParameterOrder = readConstructorParameterOrder(elementReader, context);
                exceptionMapping.constructorParameterOrder = constructorParameterOrder;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "exception-type"), new QName("http://java.sun.com/xml/ns/javaee", "wsdl-message"), new QName("http://java.sun.com/xml/ns/javaee", "wsdl-message-part-name"), new QName("http://java.sun.com/xml/ns/javaee", "constructor-parameter-order"));
            }
        }

        context.afterUnmarshal(exceptionMapping, LifecycleCallback.NONE);

        return exceptionMapping;
    }

    public final ExceptionMapping read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final ExceptionMapping exceptionMapping, RuntimeContext context)
        throws Exception {
        if (exceptionMapping == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ExceptionMapping.class != exceptionMapping.getClass()) {
            context.unexpectedSubclass(writer, exceptionMapping, ExceptionMapping.class);
            return;
        }

        context.beforeMarshal(exceptionMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = exceptionMapping.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(exceptionMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: exceptionType
        final String exceptionTypeRaw = exceptionMapping.exceptionType;
        String exceptionType = null;
        try {
            exceptionType = Adapters.collapsedStringAdapterAdapter.marshal(exceptionTypeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(exceptionMapping, "exceptionType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (exceptionType != null) {
            writer.writeStartElement(prefix, "exception-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(exceptionType);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(exceptionMapping, "exceptionType");
        }

        // ELEMENT: wsdlMessage
        final QName wsdlMessage = exceptionMapping.wsdlMessage;
        if (wsdlMessage != null) {
            writer.writeStartElement(prefix, "wsdl-message", "http://java.sun.com/xml/ns/javaee");
            writer.writeQName(wsdlMessage);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(exceptionMapping, "wsdlMessage");
        }

        // ELEMENT: wsdlMessagePartName
        final String wsdlMessagePartNameRaw = exceptionMapping.wsdlMessagePartName;
        String wsdlMessagePartName = null;
        try {
            wsdlMessagePartName = Adapters.collapsedStringAdapterAdapter.marshal(wsdlMessagePartNameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(exceptionMapping, "wsdlMessagePartName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (wsdlMessagePartName != null) {
            writer.writeStartElement(prefix, "wsdl-message-part-name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(wsdlMessagePartName);
            writer.writeEndElement();
        }

        // ELEMENT: constructorParameterOrder
        final ConstructorParameterOrder constructorParameterOrder = exceptionMapping.constructorParameterOrder;
        if (constructorParameterOrder != null) {
            writer.writeStartElement(prefix, "constructor-parameter-order", "http://java.sun.com/xml/ns/javaee");
            writeConstructorParameterOrder(writer, constructorParameterOrder, context);
            writer.writeEndElement();
        }

        context.afterMarshal(exceptionMapping, LifecycleCallback.NONE);
    }

}
