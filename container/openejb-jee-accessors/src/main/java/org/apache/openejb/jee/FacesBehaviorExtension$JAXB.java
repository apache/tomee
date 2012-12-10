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

import com.envoisolutions.sxc.jaxb.JAXBObject;
import com.envoisolutions.sxc.jaxb.LifecycleCallback;
import com.envoisolutions.sxc.jaxb.RuntimeContext;
import com.envoisolutions.sxc.util.Attribute;
import com.envoisolutions.sxc.util.XoXMLStreamReader;
import com.envoisolutions.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({
        "StringEquality"
})
public class FacesBehaviorExtension$JAXB
        extends JAXBObject<FacesBehaviorExtension> {


    public FacesBehaviorExtension$JAXB() {
        super(FacesBehaviorExtension.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-behavior-extensionType".intern()));
    }

    public static FacesBehaviorExtension readFacesBehaviorExtension(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesBehaviorExtension(XoXMLStreamWriter writer, FacesBehaviorExtension facesBehaviorExtension, RuntimeContext context)
            throws Exception {
        _write(writer, facesBehaviorExtension, context);
    }

    public void write(XoXMLStreamWriter writer, FacesBehaviorExtension facesBehaviorExtension, RuntimeContext context)
            throws Exception {
        _write(writer, facesBehaviorExtension, context);
    }

    public final static FacesBehaviorExtension _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesBehaviorExtension facesBehaviorExtension = new FacesBehaviorExtension();
        context.beforeUnmarshal(facesBehaviorExtension, LifecycleCallback.NONE);

        List<Object> any = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-behavior-extensionType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesBehaviorExtension.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesBehaviorExtension);
                facesBehaviorExtension.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            // ELEMENT_REF: any
            if (any == null) {
                any = facesBehaviorExtension.any;
                if (any != null) {
                    any.clear();
                } else {
                    any = new ArrayList<Object>();
                }
            }
            any.add(context.readXmlAny(elementReader, Object.class, true));
        }
        if (any != null) {
            facesBehaviorExtension.any = any;
        }

        context.afterUnmarshal(facesBehaviorExtension, LifecycleCallback.NONE);

        return facesBehaviorExtension;
    }

    public final FacesBehaviorExtension read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, FacesBehaviorExtension facesBehaviorExtension, RuntimeContext context)
            throws Exception {
        if (facesBehaviorExtension == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (FacesBehaviorExtension.class != facesBehaviorExtension.getClass()) {
            context.unexpectedSubclass(writer, facesBehaviorExtension, FacesBehaviorExtension.class);
            return;
        }

        context.beforeMarshal(facesBehaviorExtension, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesBehaviorExtension.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesBehaviorExtension, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT_REF: any
        List<Object> any = facesBehaviorExtension.any;
        if (any != null) {
            for (Object anyItem : any) {
                context.writeXmlAny(writer, facesBehaviorExtension, "any", anyItem);
            }
        }

        context.afterMarshal(facesBehaviorExtension, LifecycleCallback.NONE);
    }

}
