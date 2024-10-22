/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee;

import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;

@SuppressWarnings({
    "StringEquality"
})
public class FacesConverterExtension$JAXB
    extends JAXBObject<FacesConverterExtension>
{


    public FacesConverterExtension$JAXB() {
        super(FacesConverterExtension.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-converter-extensionType".intern()));
    }

    public static FacesConverterExtension readFacesConverterExtension(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesConverterExtension(XoXMLStreamWriter writer, FacesConverterExtension facesConverterExtension, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConverterExtension, context);
    }

    public void write(XoXMLStreamWriter writer, FacesConverterExtension facesConverterExtension, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConverterExtension, context);
    }

    public static final FacesConverterExtension _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesConverterExtension facesConverterExtension = new FacesConverterExtension();
        context.beforeUnmarshal(facesConverterExtension, LifecycleCallback.NONE);

        List<Object> any = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-converter-extensionType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesConverterExtension.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesConverterExtension);
                facesConverterExtension.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            // ELEMENT_REF: any
            if (any == null) {
                any = facesConverterExtension.any;
                if (any!= null) {
                    any.clear();
                } else {
                    any = new ArrayList<>();
                }
            }
            any.add(context.readXmlAny(elementReader, Object.class, true));
        }
        if (any!= null) {
            facesConverterExtension.any = any;
        }

        context.afterUnmarshal(facesConverterExtension, LifecycleCallback.NONE);

        return facesConverterExtension;
    }

    public final FacesConverterExtension read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesConverterExtension facesConverterExtension, RuntimeContext context)
        throws Exception
    {
        if (facesConverterExtension == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (FacesConverterExtension.class!= facesConverterExtension.getClass()) {
            context.unexpectedSubclass(writer, facesConverterExtension, FacesConverterExtension.class);
            return ;
        }

        context.beforeMarshal(facesConverterExtension, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesConverterExtension.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesConverterExtension, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT_REF: any
        List<Object> any = facesConverterExtension.any;
        if (any!= null) {
            for (Object anyItem: any) {
                context.writeXmlAny(writer, facesConverterExtension, "any", anyItem);
            }
        }

        context.afterMarshal(facesConverterExtension, LifecycleCallback.NONE);
    }

}
