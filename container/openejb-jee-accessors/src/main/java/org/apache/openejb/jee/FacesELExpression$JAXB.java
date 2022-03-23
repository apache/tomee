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
public class FacesELExpression$JAXB
    extends JAXBObject<FacesELExpression> {


    public FacesELExpression$JAXB() {
        super(FacesELExpression.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-el-expressionType".intern()));
    }

    public static FacesELExpression readFacesELExpression(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesELExpression(final XoXMLStreamWriter writer, final FacesELExpression facesELExpression, final RuntimeContext context)
        throws Exception {
        _write(writer, facesELExpression, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesELExpression facesELExpression, final RuntimeContext context)
        throws Exception {
        _write(writer, facesELExpression, context);
    }

    public final static FacesELExpression _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesELExpression facesELExpression = new FacesELExpression();
        context.beforeUnmarshal(facesELExpression, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-el-expressionType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesELExpression.class);
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
            facesELExpression.value = value;
        }

        context.afterUnmarshal(facesELExpression, LifecycleCallback.NONE);

        return facesELExpression;
    }

    public final FacesELExpression read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesELExpression facesELExpression, RuntimeContext context)
        throws Exception {
        if (facesELExpression == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (FacesELExpression.class != facesELExpression.getClass()) {
            context.unexpectedSubclass(writer, facesELExpression, FacesELExpression.class);
            return;
        }

        context.beforeMarshal(facesELExpression, LifecycleCallback.NONE);


        // VALUE: value
        final String valueRaw = facesELExpression.value;
        String value = null;
        try {
            value = Adapters.collapsedStringAdapterAdapter.marshal(valueRaw);
        } catch (final Exception e) {
            context.xmlAdapterError(facesELExpression, "value", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        writer.writeCharacters(value);

        context.afterMarshal(facesELExpression, LifecycleCallback.NONE);
    }

}
