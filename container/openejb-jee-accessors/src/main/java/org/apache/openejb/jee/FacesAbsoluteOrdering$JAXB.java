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


import static org.apache.openejb.jee.FacesOrderingOthers$JAXB.readFacesOrderingOthers;
import static org.apache.openejb.jee.FacesOrderingOthers$JAXB.writeFacesOrderingOthers;

@SuppressWarnings({
    "StringEquality"
})
public class FacesAbsoluteOrdering$JAXB
    extends JAXBObject<FacesAbsoluteOrdering>
{


    public FacesAbsoluteOrdering$JAXB() {
        super(FacesAbsoluteOrdering.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-absoluteOrderingType".intern()), FacesOrderingOthers$JAXB.class);
    }

    public static FacesAbsoluteOrdering readFacesAbsoluteOrdering(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesAbsoluteOrdering(XoXMLStreamWriter writer, FacesAbsoluteOrdering facesAbsoluteOrdering, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesAbsoluteOrdering, context);
    }

    public void write(XoXMLStreamWriter writer, FacesAbsoluteOrdering facesAbsoluteOrdering, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesAbsoluteOrdering, context);
    }

    public static final FacesAbsoluteOrdering _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesAbsoluteOrdering facesAbsoluteOrdering = new FacesAbsoluteOrdering();
        context.beforeUnmarshal(facesAbsoluteOrdering, LifecycleCallback.NONE);

        List<Object> nameOrOthers = null;
        List<Object> others = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-absoluteOrderingType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesAbsoluteOrdering.class);
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
            if (("others" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: nameOrOthers
                org.apache.openejb.jee.FacesOrderingOthers nameOrOthersItem = readFacesOrderingOthers(elementReader, context);
                if (nameOrOthers == null) {
                    nameOrOthers = facesAbsoluteOrdering.nameOrOthers;
                    if (nameOrOthers!= null) {
                        nameOrOthers.clear();
                    } else {
                        nameOrOthers = new ArrayList<>();
                    }
                }
                nameOrOthers.add(nameOrOthersItem);
            } else if (("name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: nameOrOthers
                java.lang.String nameOrOthersItem1 = elementReader.getElementText();
                if (nameOrOthers == null) {
                    nameOrOthers = facesAbsoluteOrdering.nameOrOthers;
                    if (nameOrOthers!= null) {
                        nameOrOthers.clear();
                    } else {
                        nameOrOthers = new ArrayList<>();
                    }
                }
                nameOrOthers.add(nameOrOthersItem1);
            } else {
                // ELEMENT_REF: others
                if (others == null) {
                    others = facesAbsoluteOrdering.others;
                    if (others!= null) {
                        others.clear();
                    } else {
                        others = new ArrayList<>();
                    }
                }
                others.add(context.readXmlAny(elementReader, Object.class, false));
            }
        }
        if (nameOrOthers!= null) {
            facesAbsoluteOrdering.nameOrOthers = nameOrOthers;
        }
        if (others!= null) {
            facesAbsoluteOrdering.others = others;
        }

        context.afterUnmarshal(facesAbsoluteOrdering, LifecycleCallback.NONE);

        return facesAbsoluteOrdering;
    }

    public final FacesAbsoluteOrdering read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesAbsoluteOrdering facesAbsoluteOrdering, RuntimeContext context)
        throws Exception
    {
        if (facesAbsoluteOrdering == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        java.lang.String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesAbsoluteOrdering.class!= facesAbsoluteOrdering.getClass()) {
            context.unexpectedSubclass(writer, facesAbsoluteOrdering, FacesAbsoluteOrdering.class);
            return ;
        }

        context.beforeMarshal(facesAbsoluteOrdering, LifecycleCallback.NONE);


        // ELEMENT: nameOrOthers
        List<Object> nameOrOthers = facesAbsoluteOrdering.nameOrOthers;
        if (nameOrOthers!= null) {
            for (Object nameOrOthersItem: nameOrOthers) {
                if (nameOrOthersItem instanceof java.lang.String) {
                    java.lang.String String = ((java.lang.String) nameOrOthersItem);
                    writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(String);
                    writer.writeEndElement();
                } else if (nameOrOthersItem instanceof org.apache.openejb.jee.FacesOrderingOthers) {
                    org.apache.openejb.jee.FacesOrderingOthers FacesOrderingOthers = ((org.apache.openejb.jee.FacesOrderingOthers) nameOrOthersItem);
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

        // ELEMENT_REF: others
        List<Object> others = facesAbsoluteOrdering.others;
        if (others!= null) {
            for (Object othersItem: others) {
                context.writeXmlAny(writer, facesAbsoluteOrdering, "others", othersItem);
            }
        }

        context.afterMarshal(facesAbsoluteOrdering, LifecycleCallback.NONE);
    }

}
