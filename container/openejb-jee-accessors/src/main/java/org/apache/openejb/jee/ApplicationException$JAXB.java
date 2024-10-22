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
public class ApplicationException$JAXB
    extends JAXBObject<ApplicationException>
{


    public ApplicationException$JAXB() {
        super(ApplicationException.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "application-exceptionType".intern()));
    }

    public static ApplicationException readApplicationException(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeApplicationException(XoXMLStreamWriter writer, ApplicationException applicationException, RuntimeContext context)
        throws Exception
    {
        _write(writer, applicationException, context);
    }

    public void write(XoXMLStreamWriter writer, ApplicationException applicationException, RuntimeContext context)
        throws Exception
    {
        _write(writer, applicationException, context);
    }

    public static final ApplicationException _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ApplicationException applicationException = new ApplicationException();
        context.beforeUnmarshal(applicationException, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("application-exceptionType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ApplicationException.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, applicationException);
                applicationException.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("exception-class" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: exceptionClass
                String exceptionClassRaw = elementReader.getElementText();

                String exceptionClass;
                try {
                    exceptionClass = Adapters.collapsedStringAdapterAdapter.unmarshal(exceptionClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                applicationException.exceptionClass = exceptionClass;
            } else if (("rollback" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: rollback
                Boolean rollback = ("1".equals(elementReader.getElementText())||"true".equals(elementReader.getElementText()));
                applicationException.rollback = rollback;
            } else if (("inherited" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: inherited
                Boolean inherited = ("1".equals(elementReader.getElementText())||"true".equals(elementReader.getElementText()));
                applicationException.inherited = inherited;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "exception-class"), new QName("http://java.sun.com/xml/ns/javaee", "rollback"), new QName("http://java.sun.com/xml/ns/javaee", "inherited"));
            }
        }

        context.afterUnmarshal(applicationException, LifecycleCallback.NONE);

        return applicationException;
    }

    public final ApplicationException read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, ApplicationException applicationException, RuntimeContext context)
        throws Exception
    {
        if (applicationException == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ApplicationException.class!= applicationException.getClass()) {
            context.unexpectedSubclass(writer, applicationException, ApplicationException.class);
            return ;
        }

        context.beforeMarshal(applicationException, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = applicationException.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(applicationException, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: exceptionClass
        String exceptionClassRaw = applicationException.exceptionClass;
        String exceptionClass = null;
        try {
            exceptionClass = Adapters.collapsedStringAdapterAdapter.marshal(exceptionClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(applicationException, "exceptionClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (exceptionClass!= null) {
            writer.writeStartElement(prefix, "exception-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(exceptionClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(applicationException, "exceptionClass");
        }

        // ELEMENT: rollback
        Boolean rollback = applicationException.rollback;
        if (rollback!= null) {
            writer.writeStartElement(prefix, "rollback", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(rollback));
            writer.writeEndElement();
        }

        // ELEMENT: inherited
        Boolean inherited = applicationException.inherited;
        if (inherited!= null) {
            writer.writeStartElement(prefix, "inherited", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(inherited));
            writer.writeEndElement();
        }

        context.afterMarshal(applicationException, LifecycleCallback.NONE);
    }

}
