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

import com.envoisolutions.sxc.jaxb.ExtendedMarshaller;
import com.envoisolutions.sxc.jaxb.ExtendedUnmarshaller;
import com.envoisolutions.sxc.jaxb.JAXBObject;
import com.envoisolutions.sxc.jaxb.RuntimeContext;
import com.envoisolutions.sxc.util.PrettyPrintXMLStreamWriter;
import com.envoisolutions.sxc.util.RuntimeXMLStreamException;
import com.envoisolutions.sxc.util.XmlFactories;
import com.envoisolutions.sxc.util.XoXMLStreamReader;
import com.envoisolutions.sxc.util.XoXMLStreamReaderImpl;
import com.envoisolutions.sxc.util.XoXMLStreamWriter;
import com.envoisolutions.sxc.util.XoXMLStreamWriterImpl;

import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.namespace.QName;
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
    public static void marshall(final JAXBObject objectType, Object object, OutputStream outputStream) throws JAXBException {
        final Result result = new StreamResult(outputStream);

        marshal(objectType, object, result);
    }

    public static void marshal(JAXBObject objectType, Object object, Result result) throws JAXBException {
        if (result == null) throw new IllegalArgumentException("result is null");
        if (!(result instanceof StreamResult)) throw new IllegalArgumentException("result is null");
        if (object == null) throw new IllegalArgumentException("object is null");
        if (objectType == null) throw new IllegalArgumentException("jaxbObject is null");

        StreamResult streamResult = (StreamResult) result;

        XMLStreamWriter writer = null;
        try {
            final XMLOutputFactory xof = XmlFactories.getXof();
            writer = xof.createXMLStreamWriter(streamResult.getOutputStream(), "UTF-8");
            writer = new PrettyPrintXMLStreamWriter(writer);
            XoXMLStreamWriter w = new XoXMLStreamWriterImpl(writer);

            try {
                w.writeStartDocument("UTF-8", null);

                // write xsi:type if there is no default root element for this type

                final RuntimeContext context = new RuntimeContext((ExtendedMarshaller) null);

                try {

                    QName name = objectType.getXmlRootElement();

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
                        Throwable cause = e.getCause();
                        if (cause instanceof JAXBException) {
                            throw (JAXBException) e;
                        }
                        throw new MarshalException(cause == null ? e : cause);
                    }
                    throw new MarshalException(e);

                }

                w.writeEndDocument();
            } catch (Exception e) {
                throw new MarshalException(e);
            }


        } catch (XMLStreamException e) {
            throw new JAXBException("Could not close XMLStreamWriter.", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (XMLStreamException ignored) {
                }
            }
        }
    }

    public static <T> T unmarshalJavaee(URL resource, JAXBObject<T> jaxbType) throws Exception {
        final InputStream inputStream = resource.openStream();
        try {
            return unmarshalJavaee(jaxbType, inputStream);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e1) {
            }
        }
    }

    public static <T> T unmarshalJavaee(JAXBObject<T> jaxbType, InputStream inputStream) throws Exception {
        final Source source = new StreamSource(inputStream);

        final XMLStreamReader streamReader = XmlFactories.getXif().createXMLStreamReader(source);

        final XMLStreamReader filter = new JavaeeNamespaceFilter(streamReader);

        return unmarhsal(jaxbType, filter);
    }

    public static <T> T unmarhsal(JAXBObject<T> jaxbType, XMLStreamReader xmlStreamReader) throws Exception {

        final XoXMLStreamReader reader = new XoXMLStreamReaderImpl(xmlStreamReader);

        int event = reader.getEventType();
        while (event != XMLStreamConstants.START_ELEMENT && reader.hasNext()) {
            event = reader.next();
        }

        return jaxbType.read(reader, new RuntimeContext((ExtendedUnmarshaller) null));
    }
}
