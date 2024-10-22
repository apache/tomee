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
public class FacesFacetExtension$JAXB
    extends JAXBObject<FacesFacetExtension>
{


    public FacesFacetExtension$JAXB() {
        super(FacesFacetExtension.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-facet-extensionType".intern()));
    }

    public static FacesFacetExtension readFacesFacetExtension(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesFacetExtension(XoXMLStreamWriter writer, FacesFacetExtension facesFacetExtension, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesFacetExtension, context);
    }

    public void write(XoXMLStreamWriter writer, FacesFacetExtension facesFacetExtension, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesFacetExtension, context);
    }

    public static final FacesFacetExtension _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesFacetExtension facesFacetExtension = new FacesFacetExtension();
        context.beforeUnmarshal(facesFacetExtension, LifecycleCallback.NONE);

        List<Object> any = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-facet-extensionType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesFacetExtension.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesFacetExtension);
                facesFacetExtension.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            // ELEMENT_REF: any
            if (any == null) {
                any = facesFacetExtension.any;
                if (any!= null) {
                    any.clear();
                } else {
                    any = new ArrayList<>();
                }
            }
            any.add(context.readXmlAny(elementReader, Object.class, true));
        }
        if (any!= null) {
            facesFacetExtension.any = any;
        }

        context.afterUnmarshal(facesFacetExtension, LifecycleCallback.NONE);

        return facesFacetExtension;
    }

    public final FacesFacetExtension read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesFacetExtension facesFacetExtension, RuntimeContext context)
        throws Exception
    {
        if (facesFacetExtension == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (FacesFacetExtension.class!= facesFacetExtension.getClass()) {
            context.unexpectedSubclass(writer, facesFacetExtension, FacesFacetExtension.class);
            return ;
        }

        context.beforeMarshal(facesFacetExtension, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesFacetExtension.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesFacetExtension, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT_REF: any
        List<Object> any = facesFacetExtension.any;
        if (any!= null) {
            for (Object anyItem: any) {
                context.writeXmlAny(writer, facesFacetExtension, "any", anyItem);
            }
        }

        context.afterMarshal(facesFacetExtension, LifecycleCallback.NONE);
    }

}
