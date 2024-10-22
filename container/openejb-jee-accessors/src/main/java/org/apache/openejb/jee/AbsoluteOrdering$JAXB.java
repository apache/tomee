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


import static org.apache.openejb.jee.OrderingOthers$JAXB.readOrderingOthers;
import static org.apache.openejb.jee.OrderingOthers$JAXB.writeOrderingOthers;

@SuppressWarnings({
    "StringEquality"
})
public class AbsoluteOrdering$JAXB
    extends JAXBObject<AbsoluteOrdering>
{


    public AbsoluteOrdering$JAXB() {
        super(AbsoluteOrdering.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "absoluteOrderingType".intern()), OrderingOthers$JAXB.class);
    }

    public static AbsoluteOrdering readAbsoluteOrdering(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeAbsoluteOrdering(XoXMLStreamWriter writer, AbsoluteOrdering absoluteOrdering, RuntimeContext context)
        throws Exception
    {
        _write(writer, absoluteOrdering, context);
    }

    public void write(XoXMLStreamWriter writer, AbsoluteOrdering absoluteOrdering, RuntimeContext context)
        throws Exception
    {
        _write(writer, absoluteOrdering, context);
    }

    public static final AbsoluteOrdering _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        AbsoluteOrdering absoluteOrdering = new AbsoluteOrdering();
        context.beforeUnmarshal(absoluteOrdering, LifecycleCallback.NONE);

        List<Object> nameOrOthers = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("absoluteOrderingType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, AbsoluteOrdering.class);
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
            if (("name" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: nameOrOthers
                java.lang.String nameOrOthersItem = elementReader.getElementText();
                if (nameOrOthers == null) {
                    nameOrOthers = absoluteOrdering.nameOrOthers;
                    if (nameOrOthers!= null) {
                        nameOrOthers.clear();
                    } else {
                        nameOrOthers = new ArrayList<>();
                    }
                }
                nameOrOthers.add(nameOrOthersItem);
            } else if (("others" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: nameOrOthers
                org.apache.openejb.jee.OrderingOthers nameOrOthersItem1 = readOrderingOthers(elementReader, context);
                if (nameOrOthers == null) {
                    nameOrOthers = absoluteOrdering.nameOrOthers;
                    if (nameOrOthers!= null) {
                        nameOrOthers.clear();
                    } else {
                        nameOrOthers = new ArrayList<>();
                    }
                }
                nameOrOthers.add(nameOrOthersItem1);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "name"), new QName("http://java.sun.com/xml/ns/javaee", "others"));
            }
        }
        if (nameOrOthers!= null) {
            absoluteOrdering.nameOrOthers = nameOrOthers;
        }

        context.afterUnmarshal(absoluteOrdering, LifecycleCallback.NONE);

        return absoluteOrdering;
    }

    public final AbsoluteOrdering read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, AbsoluteOrdering absoluteOrdering, RuntimeContext context)
        throws Exception
    {
        if (absoluteOrdering == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        java.lang.String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (AbsoluteOrdering.class!= absoluteOrdering.getClass()) {
            context.unexpectedSubclass(writer, absoluteOrdering, AbsoluteOrdering.class);
            return ;
        }

        context.beforeMarshal(absoluteOrdering, LifecycleCallback.NONE);


        // ELEMENT: nameOrOthers
        List<Object> nameOrOthers = absoluteOrdering.nameOrOthers;
        if (nameOrOthers!= null) {
            for (Object nameOrOthersItem: nameOrOthers) {
                if (nameOrOthersItem instanceof org.apache.openejb.jee.OrderingOthers) {
                    org.apache.openejb.jee.OrderingOthers OrderingOthers = ((org.apache.openejb.jee.OrderingOthers) nameOrOthersItem);
                    writer.writeStartElement(prefix, "others", "http://java.sun.com/xml/ns/javaee");
                    writeOrderingOthers(writer, OrderingOthers, context);
                    writer.writeEndElement();
                } else if (nameOrOthersItem instanceof java.lang.String) {
                    java.lang.String String = ((java.lang.String) nameOrOthersItem);
                    writer.writeStartElement(prefix, "name", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(String);
                    writer.writeEndElement();
                } else if (nameOrOthersItem == null) {
                    context.unexpectedNullValue(absoluteOrdering, "nameOrOthers");
                } else {
                    context.unexpectedElementType(writer, absoluteOrdering, "nameOrOthers", nameOrOthersItem, org.apache.openejb.jee.OrderingOthers.class, java.lang.String.class);
                }
            }
        }

        context.afterMarshal(absoluteOrdering, LifecycleCallback.NONE);
    }

}
