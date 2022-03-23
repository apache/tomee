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
package org.apache.openejb.sxc;

import org.metatype.sxc.jaxb.ExtendedMarshaller;
import org.metatype.sxc.jaxb.ExtendedUnmarshaller;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.PrettyPrintXMLStreamWriter;
import org.metatype.sxc.util.RuntimeXMLStreamException;
import org.metatype.sxc.util.XmlFactories;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamReaderImpl;
import org.metatype.sxc.util.XoXMLStreamWriter;
import org.metatype.sxc.util.XoXMLStreamWriterImpl;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.MarshalException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public class Sxc {
    public static void marshall(final JAXBObject objectType, final Object object, final OutputStream outputStream) throws JAXBException {
        final Result result = new StreamResult(outputStream);

        marshal(objectType, object, result);
    }

    public static void marshal(final JAXBObject objectType, final Object object, final Result result) throws JAXBException {
        if (result == null) throw new IllegalArgumentException("result is null");
        if (!(result instanceof StreamResult)) throw new IllegalArgumentException("result is null");
        if (object == null) throw new IllegalArgumentException("object is null");
        if (objectType == null) throw new IllegalArgumentException("jaxbObject is null");

        final StreamResult streamResult = (StreamResult) result;

        XMLStreamWriter writer = null;
        try {
            final XMLOutputFactory xof = getXmOutputFactory();
            writer = xof.createXMLStreamWriter(streamResult.getOutputStream(), "UTF-8");
            writer = new PrettyPrintXMLStreamWriter(writer);
            final XoXMLStreamWriter w = new XoXMLStreamWriterImpl(writer);

            try {
                w.writeStartDocument("UTF-8", null);

                // write xsi:type if there is no default root element for this type

                final RuntimeContext context = new RuntimeContext((ExtendedMarshaller) null);

                try {

                    final QName name = objectType.getXmlRootElement();

                    // open element
                    w.writeStartElementWithAutoPrefix(name.getNamespaceURI(), name.getLocalPart());

                    objectType.write(w, object, context);

                    w.writeEndElement();
                } catch (Exception e) {
                    if (e instanceof JAXBException) {
                        // assume event handler has already been notified
                        throw (JAXBException) e;
                    }
                    if (e instanceof RuntimeXMLStreamException) {
                        // simply unwrap and handle below
                        e = ((RuntimeXMLStreamException) e).getCause();
                    }

                    if (e instanceof XMLStreamException) {
                        final Throwable cause = e.getCause();
                        if (cause instanceof JAXBException) {
                            throw (JAXBException) e;
                        }
                        throw new MarshalException(cause == null ? e : cause);
                    }
                    throw new MarshalException(e);

                }

                w.writeEndDocument();
            } catch (final Exception e) {
                throw new MarshalException(e);
            }


        } catch (final XMLStreamException e) {
            throw new JAXBException("Could not close XMLStreamWriter.", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (final XMLStreamException ignored) {
                }
            }
        }
    }

    public static <T> T unmarshalJavaee(final URL resource, final JAXBObject<T> jaxbType) throws Exception {
        try (InputStream inputStream = resource.openStream()) {
            return unmarshalJavaee(jaxbType, inputStream);
        }
    }

    public static <T> T unmarshalJavaee(final JAXBObject<T> jaxbType, final InputStream inputStream) throws Exception {
        final XMLStreamReader filter = prepareReader(inputStream);
        return unmarhsal(jaxbType, filter);
    }

    public static XMLStreamReader prepareReader(final InputStream inputStream) throws XMLStreamException {
        final Source source = new StreamSource(inputStream);

        final XMLStreamReader streamReader = getXmlInputFactory().createXMLStreamReader(source);

        return new JavaeeNamespaceFilter(streamReader);
    }

    public static <T> T unmarhsal(final JAXBObject<T> jaxbType, final XMLStreamReader xmlStreamReader) throws Exception {

        final XoXMLStreamReader reader = new XoXMLStreamReaderImpl(xmlStreamReader);

        return unmarshall(jaxbType, reader);
    }

    public static <T> T unmarshall(final JAXBObject<T> jaxbType, final XoXMLStreamReader reader) throws Exception {
        int event = reader.getEventType();
        while (event != XMLStreamConstants.START_ELEMENT && reader.hasNext()) {
            event = reader.next();
        }

        return jaxbType.read(reader, new RuntimeContext((ExtendedUnmarshaller) null));
    }

    private static XMLInputFactory getXmlInputFactory() {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // We don't want to use whatever they have put in the their app as a STAX impl
            Thread.currentThread().setContextClassLoader(Sxc.class.getClassLoader());
            XMLInputFactory factory = null;
            try { // 1) trying to force jvm one, 2) skipping classloading/SPI mecanism, 3) setting specific property
                factory = (XMLInputFactory) Sxc.class.getClassLoader()
                    .loadClass("com.sun.xml.internal.stream.XMLInputFactoryImpl").newInstance();
                factory.setProperty("http://java.sun.com/xml/stream/properties/ignore-external-dtd", Boolean.TRUE);
            } catch (final Exception e) { // not a big deal, using the default one
                factory = XMLInputFactory.newInstance();
            }
            factory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
            factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            return factory;
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private static XMLOutputFactory getXmOutputFactory() {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // We don't want to use whatever they have put in the their app as a STAX impl
            Thread.currentThread().setContextClassLoader(Sxc.class.getClassLoader());
            return XMLOutputFactory.newInstance();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

}
