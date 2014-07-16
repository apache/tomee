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
import javax.xml.namespace.QName;

import static org.apache.openejb.jee.FacesOrderingOrdering$JAXB.readFacesOrderingOrdering;
import static org.apache.openejb.jee.FacesOrderingOrdering$JAXB.writeFacesOrderingOrdering;

@SuppressWarnings({
    "StringEquality"
})
public class FacesOrdering$JAXB
    extends JAXBObject<FacesOrdering> {


    public FacesOrdering$JAXB() {
        super(FacesOrdering.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-orderingType".intern()), FacesOrderingOrdering$JAXB.class);
    }

    public static FacesOrdering readFacesOrdering(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesOrdering(final XoXMLStreamWriter writer, final FacesOrdering facesOrdering, final RuntimeContext context)
        throws Exception {
        _write(writer, facesOrdering, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesOrdering facesOrdering, final RuntimeContext context)
        throws Exception {
        _write(writer, facesOrdering, context);
    }

    public final static FacesOrdering _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesOrdering facesOrdering = new FacesOrdering();
        context.beforeUnmarshal(facesOrdering, LifecycleCallback.NONE);


        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-orderingType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesOrdering.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute);
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("after" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: after
                final FacesOrderingOrdering after = readFacesOrderingOrdering(elementReader, context);
                facesOrdering.after = after;
            } else if (("before" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: before
                final FacesOrderingOrdering before = readFacesOrderingOrdering(elementReader, context);
                facesOrdering.before = before;
            } else {
                // just here ATM to not prevent users to get JSF 2.2 feature because we can't read it
                // TODO: handle it properly
                // context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "after"), new QName("http://java.sun.com/xml/ns/javaee", "before"));
            }
        }

        context.afterUnmarshal(facesOrdering, LifecycleCallback.NONE);

        return facesOrdering;
    }

    public final FacesOrdering read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesOrdering facesOrdering, RuntimeContext context)
        throws Exception {
        if (facesOrdering == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesOrdering.class != facesOrdering.getClass()) {
            context.unexpectedSubclass(writer, facesOrdering, FacesOrdering.class);
            return;
        }

        context.beforeMarshal(facesOrdering, LifecycleCallback.NONE);


        // ELEMENT: after
        final FacesOrderingOrdering after = facesOrdering.after;
        if (after != null) {
            writer.writeStartElement(prefix, "after", "http://java.sun.com/xml/ns/javaee");
            writeFacesOrderingOrdering(writer, after, context);
            writer.writeEndElement();
        }

        // ELEMENT: before
        final FacesOrderingOrdering before = facesOrdering.before;
        if (before != null) {
            writer.writeStartElement(prefix, "before", "http://java.sun.com/xml/ns/javaee");
            writeFacesOrderingOrdering(writer, before, context);
            writer.writeEndElement();
        }

        context.afterMarshal(facesOrdering, LifecycleCallback.NONE);
    }

}
