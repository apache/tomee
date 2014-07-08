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
import java.util.ArrayList;
import java.util.List;

import static org.apache.openejb.jee.FacesOrderingOthers$JAXB.readFacesOrderingOthers;
import static org.apache.openejb.jee.FacesOrderingOthers$JAXB.writeFacesOrderingOthers;

@SuppressWarnings({
    "StringEquality"
})
public class FacesAbsoluteOrdering$JAXB
    extends JAXBObject<FacesAbsoluteOrdering> {


    public FacesAbsoluteOrdering$JAXB() {
        super(FacesAbsoluteOrdering.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-absoluteOrderingType".intern()), FacesOrderingOthers$JAXB.class);
    }

    public static FacesAbsoluteOrdering readFacesAbsoluteOrdering(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesAbsoluteOrdering(final XoXMLStreamWriter writer, final FacesAbsoluteOrdering facesAbsoluteOrdering, final RuntimeContext context)
        throws Exception {
        _write(writer, facesAbsoluteOrdering, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesAbsoluteOrdering facesAbsoluteOrdering, final RuntimeContext context)
        throws Exception {
        _write(writer, facesAbsoluteOrdering, context);
    }

    public final static FacesAbsoluteOrdering _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesAbsoluteOrdering facesAbsoluteOrdering = new FacesAbsoluteOrdering();
        context.beforeUnmarshal(facesAbsoluteOrdering, LifecycleCallback.NONE);

        List<Object> nameOrOthers = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-absoluteOrderingType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesAbsoluteOrdering.class);
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
            if (("others" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: nameOrOthers
                final org.apache.openejb.jee.FacesOrderingOthers nameOrOthersItem = readFacesOrderingOthers(elementReader, context);
                if (nameOrOthers == null) {
                    nameOrOthers = facesAbsoluteOrdering.nameOrOthers;
                    if (nameOrOthers != null) {
                        nameOrOthers.clear();
                    } else {
                        nameOrOthers = new ArrayList<Object>();
                    }
                }
                nameOrOthers.add(nameOrOthersItem);
            } else if (("name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: nameOrOthers
                final java.lang.String nameOrOthersItem1 = elementReader.getElementAsString();
                if (nameOrOthers == null) {
                    nameOrOthers = facesAbsoluteOrdering.nameOrOthers;
                    if (nameOrOthers != null) {
                        nameOrOthers.clear();
                    } else {
                        nameOrOthers = new ArrayList<Object>();
                    }
                }
                nameOrOthers.add(nameOrOthersItem1);
            } else {
                // just here ATM to not prevent users to get JSF 2.2 feature because we can't read it
                // TODO: handle it properly
                // context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "others"), new QName("http://java.sun.com/xml/ns/javaee", "name"));
            }
        }
        if (nameOrOthers != null) {
            facesAbsoluteOrdering.nameOrOthers = nameOrOthers;
        }

        context.afterUnmarshal(facesAbsoluteOrdering, LifecycleCallback.NONE);

        return facesAbsoluteOrdering;
    }

    public final FacesAbsoluteOrdering read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesAbsoluteOrdering facesAbsoluteOrdering, RuntimeContext context)
        throws Exception {
        if (facesAbsoluteOrdering == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final java.lang.String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesAbsoluteOrdering.class != facesAbsoluteOrdering.getClass()) {
            context.unexpectedSubclass(writer, facesAbsoluteOrdering, FacesAbsoluteOrdering.class);
            return;
        }

        context.beforeMarshal(facesAbsoluteOrdering, LifecycleCallback.NONE);


        // ELEMENT: nameOrOthers
        final List<Object> nameOrOthers = facesAbsoluteOrdering.nameOrOthers;
        if (nameOrOthers != null) {
            for (final Object nameOrOthersItem : nameOrOthers) {
                if (nameOrOthersItem instanceof java.lang.String) {
                    final java.lang.String String = ((java.lang.String) nameOrOthersItem);
                    writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(String);
                    writer.writeEndElement();
                } else if (nameOrOthersItem instanceof org.apache.openejb.jee.FacesOrderingOthers) {
                    final org.apache.openejb.jee.FacesOrderingOthers FacesOrderingOthers = ((org.apache.openejb.jee.FacesOrderingOthers) nameOrOthersItem);
                    writer.writeStartElement(prefix, "others", "http://java.sun.com/xml/ns/javaee");
                    writeFacesOrderingOthers(writer, FacesOrderingOthers, context);
                    writer.writeEndElement();
                } else if (nameOrOthersItem == null) {
                    context.unexpectedNullValue(facesAbsoluteOrdering, "nameOrOthers");
                } else {
                    context.unexpectedElementType(writer, facesAbsoluteOrdering, "nameOrOthers", nameOrOthersItem, java.lang.String.class, org.apache.openejb.jee.FacesOrderingOthers.class);
                }
            }
        }

        context.afterMarshal(facesAbsoluteOrdering, LifecycleCallback.NONE);
    }

}
