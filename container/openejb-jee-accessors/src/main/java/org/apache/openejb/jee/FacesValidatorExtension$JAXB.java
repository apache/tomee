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
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({
    "StringEquality"
})
public class FacesValidatorExtension$JAXB
    extends JAXBObject<FacesValidatorExtension> {


    public FacesValidatorExtension$JAXB() {
        super(FacesValidatorExtension.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-validator-extensionType".intern()));
    }

    public static FacesValidatorExtension readFacesValidatorExtension(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesValidatorExtension(final XoXMLStreamWriter writer, final FacesValidatorExtension facesValidatorExtension, final RuntimeContext context)
        throws Exception {
        _write(writer, facesValidatorExtension, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesValidatorExtension facesValidatorExtension, final RuntimeContext context)
        throws Exception {
        _write(writer, facesValidatorExtension, context);
    }

    public final static FacesValidatorExtension _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesValidatorExtension facesValidatorExtension = new FacesValidatorExtension();
        context.beforeUnmarshal(facesValidatorExtension, LifecycleCallback.NONE);

        List<Object> any = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-validator-extensionType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesValidatorExtension.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesValidatorExtension);
                facesValidatorExtension.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            // ELEMENT_REF: any
            if (any == null) {
                any = facesValidatorExtension.any;
                if (any != null) {
                    any.clear();
                } else {
                    any = new ArrayList<Object>();
                }
            }
            any.add(context.readXmlAny(elementReader, Object.class, true));
        }
        if (any != null) {
            facesValidatorExtension.any = any;
        }

        context.afterUnmarshal(facesValidatorExtension, LifecycleCallback.NONE);

        return facesValidatorExtension;
    }

    public final FacesValidatorExtension read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesValidatorExtension facesValidatorExtension, RuntimeContext context)
        throws Exception {
        if (facesValidatorExtension == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (FacesValidatorExtension.class != facesValidatorExtension.getClass()) {
            context.unexpectedSubclass(writer, facesValidatorExtension, FacesValidatorExtension.class);
            return;
        }

        context.beforeMarshal(facesValidatorExtension, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = facesValidatorExtension.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(facesValidatorExtension, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT_REF: any
        final List<Object> any = facesValidatorExtension.any;
        if (any != null) {
            for (final Object anyItem : any) {
                context.writeXmlAny(writer, facesValidatorExtension, "any", anyItem);
            }
        }

        context.afterMarshal(facesValidatorExtension, LifecycleCallback.NONE);
    }

}
