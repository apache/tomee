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
public class FacesNullValue$JAXB
    extends JAXBObject<FacesNullValue> {


    public FacesNullValue$JAXB() {
        super(FacesNullValue.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-null-valueType".intern()));
    }

    public static FacesNullValue readFacesNullValue(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesNullValue(final XoXMLStreamWriter writer, final FacesNullValue facesNullValue, final RuntimeContext context)
        throws Exception {
        _write(writer, facesNullValue, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesNullValue facesNullValue, final RuntimeContext context)
        throws Exception {
        _write(writer, facesNullValue, context);
    }

    public final static FacesNullValue _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesNullValue facesNullValue = new FacesNullValue();
        context.beforeUnmarshal(facesNullValue, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-null-valueType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesNullValue.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesNullValue);
                facesNullValue.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            context.unexpectedElement(elementReader);
        }

        context.afterUnmarshal(facesNullValue, LifecycleCallback.NONE);

        return facesNullValue;
    }

    public final FacesNullValue read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesNullValue facesNullValue, RuntimeContext context)
        throws Exception {
        if (facesNullValue == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (FacesNullValue.class != facesNullValue.getClass()) {
            context.unexpectedSubclass(writer, facesNullValue, FacesNullValue.class);
            return;
        }

        context.beforeMarshal(facesNullValue, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = facesNullValue.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(facesNullValue, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        context.afterMarshal(facesNullValue, LifecycleCallback.NONE);
    }

}
