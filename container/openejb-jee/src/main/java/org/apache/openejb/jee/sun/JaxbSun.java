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
package org.apache.openejb.jee.sun;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEvent;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @version $Rev$ $Date$
 */
public class JaxbSun {

    public static <T>String marshal(Class<T> type, Object object) throws JAXBException {
        JAXBContext ctx2 = JAXBContext.newInstance(type);
        Marshaller marshaller = ctx2.createMarshaller();

        marshaller.setProperty("jaxb.formatted.output", true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(object, baos);

        return new String(baos.toByteArray());
    }

    public static <T>Object unmarshal(Class<T> type, InputStream in) throws ParserConfigurationException, SAXException, JAXBException {
        return JaxbSun.unmarshal(type, in, true);
    }

    public static <T>Object unmarshal(Class<T> type, InputStream in, final boolean logErrors) throws ParserConfigurationException, SAXException, JAXBException {
        // create a parser with validation disabled
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        // Get the JAXB context -- this should be cached
        JAXBContext ctx = JAXBContext.newInstance(type);

        // get the unmarshaller
        Unmarshaller unmarshaller = ctx.createUnmarshaller();

        // log errors?
        unmarshaller.setEventHandler(new ValidationEventHandler(){
            public boolean handleEvent(ValidationEvent validationEvent) {
                if (logErrors) {
                    System.out.println(validationEvent);
                }
                return false;
            }
        });

        // add our XMLFilter which disables dtd downloading
        NamespaceFilter xmlFilter = new NamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());

        // Wrap the input stream with our filter
        SAXSource source = new SAXSource(xmlFilter, new InputSource(in));

        // unmarshal the document
        return unmarshaller.unmarshal(source);
    }

    // todo Inject the proper namespace
    public static class NamespaceFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public NamespaceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return EMPTY_INPUT_SOURCE;
        }
    }
}
