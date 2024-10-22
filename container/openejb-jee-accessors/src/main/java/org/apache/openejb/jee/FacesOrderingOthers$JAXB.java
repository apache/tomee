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
public class FacesOrderingOthers$JAXB
    extends JAXBObject<FacesOrderingOthers>
{


    public FacesOrderingOthers$JAXB() {
        super(FacesOrderingOthers.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-ordering-othersType".intern()));
    }

    public static FacesOrderingOthers readFacesOrderingOthers(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesOrderingOthers(XoXMLStreamWriter writer, FacesOrderingOthers facesOrderingOthers, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesOrderingOthers, context);
    }

    public void write(XoXMLStreamWriter writer, FacesOrderingOthers facesOrderingOthers, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesOrderingOthers, context);
    }

    public static final FacesOrderingOthers _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesOrderingOthers facesOrderingOthers = new FacesOrderingOthers();
        context.beforeUnmarshal(facesOrderingOthers, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-ordering-othersType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesOrderingOthers.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesOrderingOthers);
                facesOrderingOthers.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            context.unexpectedElement(elementReader);
        }

        context.afterUnmarshal(facesOrderingOthers, LifecycleCallback.NONE);

        return facesOrderingOthers;
    }

    public final FacesOrderingOthers read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesOrderingOthers facesOrderingOthers, RuntimeContext context)
        throws Exception
    {
        if (facesOrderingOthers == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (FacesOrderingOthers.class!= facesOrderingOthers.getClass()) {
            context.unexpectedSubclass(writer, facesOrderingOthers, FacesOrderingOthers.class);
            return ;
        }

        context.beforeMarshal(facesOrderingOthers, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesOrderingOthers.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesOrderingOthers, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        context.afterMarshal(facesOrderingOthers, LifecycleCallback.NONE);
    }

}
