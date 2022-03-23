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
import java.math.BigInteger;

import static org.apache.openejb.jee.WsdlMessageMapping$JAXB.readWsdlMessageMapping;
import static org.apache.openejb.jee.WsdlMessageMapping$JAXB.writeWsdlMessageMapping;

@SuppressWarnings({
    "StringEquality"
})
public class MethodParamPartsMapping$JAXB
    extends JAXBObject<MethodParamPartsMapping> {


    public MethodParamPartsMapping$JAXB() {
        super(MethodParamPartsMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "method-param-parts-mappingType".intern()), WsdlMessageMapping$JAXB.class);
    }

    public static MethodParamPartsMapping readMethodParamPartsMapping(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeMethodParamPartsMapping(final XoXMLStreamWriter writer, final MethodParamPartsMapping methodParamPartsMapping, final RuntimeContext context)
        throws Exception {
        _write(writer, methodParamPartsMapping, context);
    }

    public void write(final XoXMLStreamWriter writer, final MethodParamPartsMapping methodParamPartsMapping, final RuntimeContext context)
        throws Exception {
        _write(writer, methodParamPartsMapping, context);
    }

    public final static MethodParamPartsMapping _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final MethodParamPartsMapping methodParamPartsMapping = new MethodParamPartsMapping();
        context.beforeUnmarshal(methodParamPartsMapping, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("method-param-parts-mappingType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, MethodParamPartsMapping.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, methodParamPartsMapping);
                methodParamPartsMapping.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("param-position" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: paramPosition
                final BigInteger paramPosition = new BigInteger(elementReader.getElementAsString());
                methodParamPartsMapping.paramPosition = paramPosition;
            } else if (("param-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: paramType
                final String paramTypeRaw = elementReader.getElementAsString();

                final String paramType;
                try {
                    paramType = Adapters.collapsedStringAdapterAdapter.unmarshal(paramTypeRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                methodParamPartsMapping.paramType = paramType;
            } else if (("wsdl-message-mapping" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: wsdlMessageMapping
                final WsdlMessageMapping wsdlMessageMapping = readWsdlMessageMapping(elementReader, context);
                methodParamPartsMapping.wsdlMessageMapping = wsdlMessageMapping;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "param-position"), new QName("http://java.sun.com/xml/ns/javaee", "param-type"), new QName("http://java.sun.com/xml/ns/javaee", "wsdl-message-mapping"));
            }
        }

        context.afterUnmarshal(methodParamPartsMapping, LifecycleCallback.NONE);

        return methodParamPartsMapping;
    }

    public final MethodParamPartsMapping read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final MethodParamPartsMapping methodParamPartsMapping, RuntimeContext context)
        throws Exception {
        if (methodParamPartsMapping == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (MethodParamPartsMapping.class != methodParamPartsMapping.getClass()) {
            context.unexpectedSubclass(writer, methodParamPartsMapping, MethodParamPartsMapping.class);
            return;
        }

        context.beforeMarshal(methodParamPartsMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = methodParamPartsMapping.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(methodParamPartsMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: paramPosition
        final BigInteger paramPosition = methodParamPartsMapping.paramPosition;
        if (paramPosition != null) {
            writer.writeStartElement(prefix, "param-position", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(paramPosition.toString());
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(methodParamPartsMapping, "paramPosition");
        }

        // ELEMENT: paramType
        final String paramTypeRaw = methodParamPartsMapping.paramType;
        String paramType = null;
        try {
            paramType = Adapters.collapsedStringAdapterAdapter.marshal(paramTypeRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(methodParamPartsMapping, "paramType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (paramType != null) {
            writer.writeStartElement(prefix, "param-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(paramType);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(methodParamPartsMapping, "paramType");
        }

        // ELEMENT: wsdlMessageMapping
        final WsdlMessageMapping wsdlMessageMapping = methodParamPartsMapping.wsdlMessageMapping;
        if (wsdlMessageMapping != null) {
            writer.writeStartElement(prefix, "wsdl-message-mapping", "http://java.sun.com/xml/ns/javaee");
            writeWsdlMessageMapping(writer, wsdlMessageMapping, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(methodParamPartsMapping, "wsdlMessageMapping");
        }

        context.afterMarshal(methodParamPartsMapping, LifecycleCallback.NONE);
    }

}
