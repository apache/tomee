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
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.FacesOrderingOrdering$JAXB.readFacesOrderingOrdering;
import static org.apache.openejb.jee.FacesOrderingOrdering$JAXB.writeFacesOrderingOrdering;

@SuppressWarnings({
    "StringEquality"
})
public class FacesOrdering$JAXB
    extends JAXBObject<FacesOrdering>
{


    public FacesOrdering$JAXB() {
        super(FacesOrdering.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-orderingType".intern()), FacesOrderingOrdering$JAXB.class);
    }

    public static FacesOrdering readFacesOrdering(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesOrdering(XoXMLStreamWriter writer, FacesOrdering facesOrdering, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesOrdering, context);
    }

    public void write(XoXMLStreamWriter writer, FacesOrdering facesOrdering, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesOrdering, context);
    }

    public static final FacesOrdering _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesOrdering facesOrdering = new FacesOrdering();
        context.beforeUnmarshal(facesOrdering, LifecycleCallback.NONE);

        List<Object> others = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-orderingType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesOrdering.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute);
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("after" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: after
                FacesOrderingOrdering after = readFacesOrderingOrdering(elementReader, context);
                facesOrdering.after = after;
            } else if (("before" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: before
                FacesOrderingOrdering before = readFacesOrderingOrdering(elementReader, context);
                facesOrdering.before = before;
            } else {
                // ELEMENT_REF: others
                if (others == null) {
                    others = facesOrdering.others;
                    if (others!= null) {
                        others.clear();
                    } else {
                        others = new ArrayList<>();
                    }
                }
                others.add(context.readXmlAny(elementReader, Object.class, false));
            }
        }
        if (others!= null) {
            facesOrdering.others = others;
        }

        context.afterUnmarshal(facesOrdering, LifecycleCallback.NONE);

        return facesOrdering;
    }

    public final FacesOrdering read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesOrdering facesOrdering, RuntimeContext context)
        throws Exception
    {
        if (facesOrdering == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesOrdering.class!= facesOrdering.getClass()) {
            context.unexpectedSubclass(writer, facesOrdering, FacesOrdering.class);
            return ;
        }

        context.beforeMarshal(facesOrdering, LifecycleCallback.NONE);


        // ELEMENT: after
        FacesOrderingOrdering after = facesOrdering.after;
        if (after!= null) {
            writer.writeStartElement(prefix, "after", "http://java.sun.com/xml/ns/javaee");
            writeFacesOrderingOrdering(writer, after, context);
            writer.writeEndElement();
        }

        // ELEMENT: before
        FacesOrderingOrdering before = facesOrdering.before;
        if (before!= null) {
            writer.writeStartElement(prefix, "before", "http://java.sun.com/xml/ns/javaee");
            writeFacesOrderingOrdering(writer, before, context);
            writer.writeEndElement();
        }

        // ELEMENT_REF: others
        List<Object> others = facesOrdering.others;
        if (others!= null) {
            for (Object othersItem: others) {
                context.writeXmlAny(writer, facesOrdering, "others", othersItem);
            }
        }

        context.afterMarshal(facesOrdering, LifecycleCallback.NONE);
    }

}
