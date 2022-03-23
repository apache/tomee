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
public class FacesLocale$JAXB
    extends JAXBObject<FacesLocale> {


    public FacesLocale$JAXB() {
        super(FacesLocale.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-localeType".intern()));
    }

    public static FacesLocale readFacesLocale(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesLocale(final XoXMLStreamWriter writer, final FacesLocale facesLocale, final RuntimeContext context)
        throws Exception {
        _write(writer, facesLocale, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesLocale facesLocale, final RuntimeContext context)
        throws Exception {
        _write(writer, facesLocale, context);
    }

    public final static FacesLocale _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesLocale facesLocale = new FacesLocale();
        context.beforeUnmarshal(facesLocale, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-localeType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesLocale.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute);
            }
        }

        // VALUE: value
        final String valueRaw = reader.getElementText();

        String value = null;
        boolean valueConverted;
        try {
            value = Adapters.collapsedStringAdapterAdapter.unmarshal(valueRaw);
            valueConverted = true;
        } catch (final Exception e) {
            context.xmlAdapterError(reader, CollapsedStringAdapter.class, String.class, String.class, e);
            valueConverted = false;
        }

        if (valueConverted) {
            facesLocale.value = value;
        }

        context.afterUnmarshal(facesLocale, LifecycleCallback.NONE);

        return facesLocale;
    }

    public final FacesLocale read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesLocale facesLocale, RuntimeContext context)
        throws Exception {
        if (facesLocale == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (FacesLocale.class != facesLocale.getClass()) {
            context.unexpectedSubclass(writer, facesLocale, FacesLocale.class);
            return;
        }

        context.beforeMarshal(facesLocale, LifecycleCallback.NONE);


        // VALUE: value
        final String valueRaw = facesLocale.value;
        String value = null;
        try {
            value = Adapters.collapsedStringAdapterAdapter.marshal(valueRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesLocale, "value", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        writer.writeCharacters(value);

        context.afterMarshal(facesLocale, LifecycleCallback.NONE);
    }

}
