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
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.OrderingOrdering$JAXB.readOrderingOrdering;
import static org.apache.openejb.jee.OrderingOrdering$JAXB.writeOrderingOrdering;

@SuppressWarnings({
    "StringEquality"
})
public class Ordering$JAXB
    extends JAXBObject<Ordering>
{


    public Ordering$JAXB() {
        super(Ordering.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "orderingType".intern()), OrderingOrdering$JAXB.class);
    }

    public static Ordering readOrdering(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeOrdering(XoXMLStreamWriter writer, Ordering ordering, RuntimeContext context)
        throws Exception
    {
        _write(writer, ordering, context);
    }

    public void write(XoXMLStreamWriter writer, Ordering ordering, RuntimeContext context)
        throws Exception
    {
        _write(writer, ordering, context);
    }

    public static final Ordering _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        Ordering ordering = new Ordering();
        context.beforeUnmarshal(ordering, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("orderingType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Ordering.class);
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
                OrderingOrdering after = readOrderingOrdering(elementReader, context);
                ordering.after = after;
            } else if (("before" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: before
                OrderingOrdering before = readOrderingOrdering(elementReader, context);
                ordering.before = before;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "after"), new QName("http://java.sun.com/xml/ns/javaee", "before"));
            }
        }

        context.afterUnmarshal(ordering, LifecycleCallback.NONE);

        return ordering;
    }

    public final Ordering read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, Ordering ordering, RuntimeContext context)
        throws Exception
    {
        if (ordering == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Ordering.class!= ordering.getClass()) {
            context.unexpectedSubclass(writer, ordering, Ordering.class);
            return ;
        }

        context.beforeMarshal(ordering, LifecycleCallback.NONE);


        // ELEMENT: after
        OrderingOrdering after = ordering.after;
        if (after!= null) {
            writer.writeStartElement(prefix, "after", "http://java.sun.com/xml/ns/javaee");
            writeOrderingOrdering(writer, after, context);
            writer.writeEndElement();
        }

        // ELEMENT: before
        OrderingOrdering before = ordering.before;
        if (before!= null) {
            writer.writeStartElement(prefix, "before", "http://java.sun.com/xml/ns/javaee");
            writeOrderingOrdering(writer, before, context);
            writer.writeEndElement();
        }

        context.afterMarshal(ordering, LifecycleCallback.NONE);
    }

}
