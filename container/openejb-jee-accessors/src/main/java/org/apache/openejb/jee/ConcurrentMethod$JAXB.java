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


import static org.apache.openejb.jee.ConcurrentLockType$JAXB.parseConcurrentLockType;
import static org.apache.openejb.jee.ConcurrentLockType$JAXB.toStringConcurrentLockType;
import static org.apache.openejb.jee.NamedMethod$JAXB.readNamedMethod;
import static org.apache.openejb.jee.NamedMethod$JAXB.writeNamedMethod;
import static org.apache.openejb.jee.Timeout$JAXB.readTimeout;
import static org.apache.openejb.jee.Timeout$JAXB.writeTimeout;

@SuppressWarnings({
    "StringEquality"
})
public class ConcurrentMethod$JAXB
    extends JAXBObject<ConcurrentMethod>
{


    public ConcurrentMethod$JAXB() {
        super(ConcurrentMethod.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "concurrent-methodType".intern()), NamedMethod$JAXB.class, ConcurrentLockType$JAXB.class, Timeout$JAXB.class);
    }

    public static ConcurrentMethod readConcurrentMethod(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeConcurrentMethod(XoXMLStreamWriter writer, ConcurrentMethod concurrentMethod, RuntimeContext context)
        throws Exception
    {
        _write(writer, concurrentMethod, context);
    }

    public void write(XoXMLStreamWriter writer, ConcurrentMethod concurrentMethod, RuntimeContext context)
        throws Exception
    {
        _write(writer, concurrentMethod, context);
    }

    public static final ConcurrentMethod _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ConcurrentMethod concurrentMethod = new ConcurrentMethod();
        context.beforeUnmarshal(concurrentMethod, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("concurrent-methodType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ConcurrentMethod.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, concurrentMethod);
                concurrentMethod.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("method" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: method
                NamedMethod method = readNamedMethod(elementReader, context);
                concurrentMethod.method = method;
            } else if (("lock" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: lock
                ConcurrentLockType lock = parseConcurrentLockType(elementReader, context, elementReader.getElementText());
                if (lock!= null) {
                    concurrentMethod.lock = lock;
                }
            } else if (("access-timeout" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: accessTimeout
                Timeout accessTimeout = readTimeout(elementReader, context);
                concurrentMethod.accessTimeout = accessTimeout;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "method"), new QName("http://java.sun.com/xml/ns/javaee", "lock"), new QName("http://java.sun.com/xml/ns/javaee", "access-timeout"));
            }
        }

        context.afterUnmarshal(concurrentMethod, LifecycleCallback.NONE);

        return concurrentMethod;
    }

    public final ConcurrentMethod read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, ConcurrentMethod concurrentMethod, RuntimeContext context)
        throws Exception
    {
        if (concurrentMethod == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ConcurrentMethod.class!= concurrentMethod.getClass()) {
            context.unexpectedSubclass(writer, concurrentMethod, ConcurrentMethod.class);
            return ;
        }

        context.beforeMarshal(concurrentMethod, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = concurrentMethod.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(concurrentMethod, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: method
        NamedMethod method = concurrentMethod.method;
        if (method!= null) {
            writer.writeStartElement(prefix, "method", "http://java.sun.com/xml/ns/javaee");
            writeNamedMethod(writer, method, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(concurrentMethod, "method");
        }

        // ELEMENT: lock
        ConcurrentLockType lock = concurrentMethod.lock;
        if (lock!= null) {
            writer.writeStartElement(prefix, "lock", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(toStringConcurrentLockType(concurrentMethod, null, context, lock));
            writer.writeEndElement();
        }

        // ELEMENT: accessTimeout
        Timeout accessTimeout = concurrentMethod.accessTimeout;
        if (accessTimeout!= null) {
            writer.writeStartElement(prefix, "access-timeout", "http://java.sun.com/xml/ns/javaee");
            writeTimeout(writer, accessTimeout, context);
            writer.writeEndElement();
        }

        context.afterMarshal(concurrentMethod, LifecycleCallback.NONE);
    }

}
