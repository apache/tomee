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
import java.math.BigInteger;

@SuppressWarnings({
        "StringEquality"
})
public class ErrorPage$JAXB
        extends JAXBObject<ErrorPage> {


    public ErrorPage$JAXB() {
        super(ErrorPage.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "error-pageType".intern()));
    }

    public static ErrorPage readErrorPage(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeErrorPage(XoXMLStreamWriter writer, ErrorPage errorPage, RuntimeContext context)
            throws Exception {
        _write(writer, errorPage, context);
    }

    public void write(XoXMLStreamWriter writer, ErrorPage errorPage, RuntimeContext context)
            throws Exception {
        _write(writer, errorPage, context);
    }

    public final static ErrorPage _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ErrorPage errorPage = new ErrorPage();
        context.beforeUnmarshal(errorPage, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("error-pageType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, ErrorPage.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, errorPage);
                errorPage.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("error-code" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: errorCode
                BigInteger errorCode = new BigInteger(elementReader.getElementAsString());
                errorPage.errorCode = errorCode;
            } else if (("exception-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: exceptionType
                String exceptionTypeRaw = elementReader.getElementAsString();

                String exceptionType;
                try {
                    exceptionType = Adapters.collapsedStringAdapterAdapter.unmarshal(exceptionTypeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                errorPage.exceptionType = exceptionType;
            } else if (("location" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: location
                String locationRaw = elementReader.getElementAsString();

                String location;
                try {
                    location = Adapters.collapsedStringAdapterAdapter.unmarshal(locationRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                errorPage.location = location;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "error-code"), new QName("http://java.sun.com/xml/ns/javaee", "exception-type"), new QName("http://java.sun.com/xml/ns/javaee", "location"));
            }
        }

        context.afterUnmarshal(errorPage, LifecycleCallback.NONE);

        return errorPage;
    }

    public final ErrorPage read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, ErrorPage errorPage, RuntimeContext context)
            throws Exception {
        if (errorPage == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (ErrorPage.class != errorPage.getClass()) {
            context.unexpectedSubclass(writer, errorPage, ErrorPage.class);
            return;
        }

        context.beforeMarshal(errorPage, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = errorPage.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(errorPage, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: errorCode
        BigInteger errorCode = errorPage.errorCode;
        if (errorCode != null) {
            writer.writeStartElement(prefix, "error-code", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(errorCode.toString());
            writer.writeEndElement();
        }

        // ELEMENT: exceptionType
        String exceptionTypeRaw = errorPage.exceptionType;
        String exceptionType = null;
        try {
            exceptionType = Adapters.collapsedStringAdapterAdapter.marshal(exceptionTypeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(errorPage, "exceptionType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (exceptionType != null) {
            writer.writeStartElement(prefix, "exception-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(exceptionType);
            writer.writeEndElement();
        }

        // ELEMENT: location
        String locationRaw = errorPage.location;
        String location = null;
        try {
            location = Adapters.collapsedStringAdapterAdapter.marshal(locationRaw);
        } catch (Exception e) {
            context.xmlAdapterError(errorPage, "location", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (location != null) {
            writer.writeStartElement(prefix, "location", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(location);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(errorPage, "location");
        }

        context.afterMarshal(errorPage, LifecycleCallback.NONE);
    }

}
