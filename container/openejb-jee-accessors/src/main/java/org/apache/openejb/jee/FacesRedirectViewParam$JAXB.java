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
public class FacesRedirectViewParam$JAXB
    extends JAXBObject<FacesRedirectViewParam> {


    public FacesRedirectViewParam$JAXB() {
        super(FacesRedirectViewParam.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-redirect-viewParamType".intern()));
    }

    public static FacesRedirectViewParam readFacesRedirectViewParam(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesRedirectViewParam(final XoXMLStreamWriter writer, final FacesRedirectViewParam facesRedirectViewParam, final RuntimeContext context)
        throws Exception {
        _write(writer, facesRedirectViewParam, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesRedirectViewParam facesRedirectViewParam, final RuntimeContext context)
        throws Exception {
        _write(writer, facesRedirectViewParam, context);
    }

    public final static FacesRedirectViewParam _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesRedirectViewParam facesRedirectViewParam = new FacesRedirectViewParam();
        context.beforeUnmarshal(facesRedirectViewParam, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-redirect-viewParamType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesRedirectViewParam.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesRedirectViewParam);
                facesRedirectViewParam.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: name
                final String nameRaw = elementReader.getElementAsString();

                final String name;
                try {
                    name = Adapters.collapsedStringAdapterAdapter.unmarshal(nameRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesRedirectViewParam.name = name;
            } else if (("value" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: value
                final String valueRaw = elementReader.getElementAsString();

                final String value;
                try {
                    value = Adapters.collapsedStringAdapterAdapter.unmarshal(valueRaw);
                } catch (final Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesRedirectViewParam.value = value;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "name"), new QName("http://java.sun.com/xml/ns/javaee", "value"));
            }
        }

        context.afterUnmarshal(facesRedirectViewParam, LifecycleCallback.NONE);

        return facesRedirectViewParam;
    }

    public final FacesRedirectViewParam read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesRedirectViewParam facesRedirectViewParam, RuntimeContext context)
        throws Exception {
        if (facesRedirectViewParam == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesRedirectViewParam.class != facesRedirectViewParam.getClass()) {
            context.unexpectedSubclass(writer, facesRedirectViewParam, FacesRedirectViewParam.class);
            return;
        }

        context.beforeMarshal(facesRedirectViewParam, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = facesRedirectViewParam.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(facesRedirectViewParam, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: name
        final String nameRaw = facesRedirectViewParam.name;
        String name = null;
        try {
            name = Adapters.collapsedStringAdapterAdapter.marshal(nameRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesRedirectViewParam, "name", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (name != null) {
            writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(name);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesRedirectViewParam, "name");
        }

        // ELEMENT: value
        final String valueRaw = facesRedirectViewParam.value;
        String value = null;
        try {
            value = Adapters.collapsedStringAdapterAdapter.marshal(valueRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesRedirectViewParam, "value", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (value != null) {
            writer.writeStartElement(prefix, "value", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(value);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesRedirectViewParam, "value");
        }

        context.afterMarshal(facesRedirectViewParam, LifecycleCallback.NONE);
    }

}
