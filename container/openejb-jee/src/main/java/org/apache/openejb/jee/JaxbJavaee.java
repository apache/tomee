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
package org.apache.openejb.jee;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEvent;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class JaxbJavaee {
    public static final ThreadLocal<Set<String>> currentPublicId = new ThreadLocal<Set<String>>();

    private static Map<Class<?>,JAXBContext> jaxbContexts = new HashMap<Class<?>,JAXBContext>();

    public static <T>String marshal(Class<T> type, Object object) throws JAXBException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        marshal(type, object, baos);

        return new String(baos.toByteArray());
    }

    public static <T>void marshal(Class<T> type, Object object, OutputStream out) throws JAXBException {
        JAXBContext ctx2 = JaxbJavaee.getContext(type);
        Marshaller marshaller = ctx2.createMarshaller();

        marshaller.setProperty("jaxb.formatted.output", true);

        marshaller.marshal(object, out);
    }

    private static <T>JAXBContext getContext(Class<T> type) throws JAXBException {
        JAXBContext jaxbContext = jaxbContexts.get(type);
        if (jaxbContext == null) {
            jaxbContext = JAXBContextFactory.newInstance(type);
            jaxbContexts.put(type, jaxbContext);
        }
        return jaxbContext;
    }

    /**
     * Convert the namespaceURI in the input to the javaee URI, do not validate the xml, and read in a T.
     *
     * @param type Class of object to be read in
     * @param in input stream to read
     * @param <T> class of object to be returned
     * @return a T read from the input stream
     * @throws ParserConfigurationException is the SAX parser can not be configured
     * @throws SAXException if there is an xml problem
     * @throws JAXBException if the xml cannot be marshalled into a T.
     */
    public static <T>Object unmarshalJavaee(Class<T> type, InputStream in) throws ParserConfigurationException, SAXException, JAXBException {
        InputSource inputSource = new InputSource(in);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        JAXBContext ctx = JaxbJavaee.getContext(type);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler(){
            public boolean handleEvent(ValidationEvent validationEvent) {
                System.out.println(validationEvent);
                return false;
            }
        });


        JavaeeNamespaceFilter xmlFilter = new JavaeeNamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());

        SAXSource source = new SAXSource(xmlFilter, inputSource);

        currentPublicId.set(new TreeSet<String>());
        try {
            JAXBElement<T> element = unmarshaller.unmarshal(source, type);
            return element.getValue();
        } finally {
            currentPublicId.set(null);
        }
    }

    /**
     * Read in a T from the input stream.
     *
     * @param type Class of object to be read in
     * @param in input stream to read
     * @param validate whether to validate the input.
     * @param <T> class of object to be returned
     * @return a T read from the input stream
     * @throws ParserConfigurationException is the SAX parser can not be configured
     * @throws SAXException if there is an xml problem
     * @throws JAXBException if the xml cannot be marshalled into a T.
     */
    public static <T>Object unmarshal(Class<T> type, InputStream in, boolean validate) throws ParserConfigurationException, SAXException, JAXBException {
        InputSource inputSource = new InputSource(in);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(validate);
        SAXParser parser = factory.newSAXParser();

        JAXBContext ctx = JaxbJavaee.getContext(type);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler(){
            public boolean handleEvent(ValidationEvent validationEvent) {
                System.out.println(validationEvent);
                return false;
            }
        });

        JaxbJavaee.NoSourceFilter xmlFilter = new JaxbJavaee.NoSourceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());

        SAXSource source = new SAXSource(xmlFilter, inputSource);

        currentPublicId.set(new TreeSet<String>());
        try {
            return unmarshaller.unmarshal(source);
        } finally {
            currentPublicId.set(null);
        }
    }

    /**
     * Convert the namespaceURI in the input to the taglib URI, do not validate the xml, and read in a T.
     *
     * @param type Class of object to be read in
     * @param in input stream to read
     * @param <T> class of object to be returned
     * @return a T read from the input stream
     * @throws ParserConfigurationException is the SAX parser can not be configured
     * @throws SAXException if there is an xml problem
     * @throws JAXBException if the xml cannot be marshalled into a T.
     */
    public static <T>Object unmarshalTaglib(Class<T> type, InputStream in) throws ParserConfigurationException, SAXException, JAXBException {
        InputSource inputSource = new InputSource(in);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        JAXBContext ctx = JaxbJavaee.getContext(type);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler(){
            public boolean handleEvent(ValidationEvent validationEvent) {
                System.out.println(validationEvent);
                return false;
            }
        });


        JaxbJavaee.TaglibNamespaceFilter xmlFilter = new JaxbJavaee.TaglibNamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());

        SAXSource source = new SAXSource(xmlFilter, inputSource);

        currentPublicId.set(new TreeSet<String>());
        try {
            return unmarshaller.unmarshal(source);
        } finally {
            currentPublicId.set(null);
        }
    }

    public static class JavaeeNamespaceFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public JavaeeNamespaceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            Set<String> publicIds = currentPublicId.get();
            if (publicIds != null) {
                publicIds.add(publicId);
            }
            return EMPTY_INPUT_SOURCE;
        }

        @Override
        public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
            super.startElement("http://java.sun.com/xml/ns/javaee", localName, qname, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement("http://java.sun.com/xml/ns/javaee", localName, qName);
        }
    }
    public static class NoSourceFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public NoSourceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            Set<String> publicIds = currentPublicId.get();
            if (publicIds != null) {
                publicIds.add(publicId);
            }
            return EMPTY_INPUT_SOURCE;
        }
    }

    public static class TaglibNamespaceFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public TaglibNamespaceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            Set<String> publicIds = currentPublicId.get();
            if (publicIds != null) {
                publicIds.add(publicId);
            }
            return EMPTY_INPUT_SOURCE;
        }

        @Override
        public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
            localName = fixLocalName(localName);
            super.startElement("http://java.sun.com/xml/ns/javaee", localName, qname, atts);
        }

        private String fixLocalName(String localName) {
            if (localName.equals("tlibversion")) {
                localName = "tlib-version";
            } else if (localName.equals("jspversion")) {
                localName = "jsp-version";
            } else if (localName.equals("shortname")) {
                localName = "short-name";
            } else if (localName.equals("tagclass")) {
                localName = "tag-class";
            } else if (localName.equals("teiclass")) {
                localName = "tei-class";
            } else if (localName.equals("bodycontent")) {
                localName = "body-content";
            } else if (localName.equals("jspversion")) {
                localName = "jsp-version";
            }
            return localName;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            localName = fixLocalName(localName);
            super.endElement("http://java.sun.com/xml/ns/javaee", localName, qName);
        }
    }

}
