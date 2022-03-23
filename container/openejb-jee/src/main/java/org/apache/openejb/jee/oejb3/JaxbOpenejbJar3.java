/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee.oejb3;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLFilterImpl;
import org.apache.openejb.jee.JAXBContextFactory;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEventHandler;
import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
public class JaxbOpenejbJar3 {
    private static JAXBContext jaxbContext;

    public static <T> String marshal(final Class<T> type, final Object object) throws JAXBException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshal(type, object, baos);

        return new String(baos.toByteArray());
    }

    public static <T> void marshal(final Class<T> type, final Object object, final OutputStream out) throws JAXBException {
        final JAXBContext ctx2 = getContext(type);
        final Marshaller marshaller = ctx2.createMarshaller();

        marshaller.setProperty("jaxb.formatted.output", true);

        marshaller.marshal(object, out);
    }

    private static <T> JAXBContext getContext(final Class<T> type) throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContextFactory.newInstance(type);
        }
        return jaxbContext;
    }

    public static <T> T unmarshal(final Class<T> type, final InputStream in) throws ParserConfigurationException, SAXException, JAXBException {
        final InputSource inputSource = new InputSource(in);

        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        final SAXParser parser = factory.newSAXParser();

        final JAXBContext ctx = getContext(type);
        final Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() {
            public boolean handleEvent(final ValidationEvent validationEvent) {
//                System.out.println(validationEvent);
                return false;
            }
        });


        final NamespaceFilter xmlFilter = new NamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());

        final SAXSource source = new SAXSource(xmlFilter, inputSource);

        final Object o = unmarshaller.unmarshal(source);
        if (o instanceof JAXBElement) {
            final JAXBElement element = (JAXBElement) o;
            return (T) element.getValue();
        }
        return (T) o;
    }

    public static class NamespaceFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public NamespaceFilter(final XMLReader xmlReader) {
            super(xmlReader);
        }

        public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
            return EMPTY_INPUT_SOURCE;
        }

        public void startElement(final String uri, final String localName, final String qname, final Attributes atts) throws SAXException {
            super.startElement("http://www.openejb.org/openejb-jar/1.1", localName, qname, atts);
        }
    }
}
