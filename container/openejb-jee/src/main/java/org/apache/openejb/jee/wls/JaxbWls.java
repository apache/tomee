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
package org.apache.openejb.jee.wls;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEvent;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.sax.SAXSource;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeSet;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
public class JaxbWls {
    public static final ThreadLocal<Set<String>> currentPublicId = new ThreadLocal<Set<String>>();

    private static Map<Class<?>, JAXBContext> jaxbContexts = new HashMap<Class<?>,JAXBContext>();

    public static <T>String marshal(Class<T> type, Object object) throws JAXBException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        JaxbWls.marshal(type, object, baos);

        return new String(baos.toByteArray());
    }

    public static <T>void marshal(Class<T> type, Object object, OutputStream out) throws JAXBException {
        JAXBContext ctx2 = JaxbWls.getContext(type);
        Marshaller marshaller = ctx2.createMarshaller();

        marshaller.setProperty("jaxb.formatted.output", true);

        marshaller.marshal(object, out);
    }

    private static <T>JAXBContext getContext(Class<T> type) throws JAXBException {
        JAXBContext jaxbContext = JaxbWls.jaxbContexts.get(type);
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(type);
            JaxbWls.jaxbContexts.put(type, jaxbContext);
        }
        return jaxbContext;
    }

    public static <T>Object unmarshal(Class<T> type, InputStream in) throws ParserConfigurationException, SAXException, JAXBException {
        InputSource inputSource = new InputSource(in);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        JAXBContext ctx = JaxbWls.getContext(type);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler(){
            public boolean handleEvent(ValidationEvent validationEvent) {
                System.out.println(validationEvent);
                return false;
            }
        });


        JaxbWls.NamespaceFilter xmlFilter = new JaxbWls.NamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());

        SAXSource source = new SAXSource(xmlFilter, inputSource);

        JaxbWls.currentPublicId.set(new TreeSet<String>());
        try {
            return unmarshaller.unmarshal(source);
        } finally {
            JaxbWls.currentPublicId.set(null);
        }
    }

    public static class NamespaceFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public NamespaceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            Set<String> publicIds = JaxbWls.currentPublicId.get();
            if (publicIds != null) {
                publicIds.add(publicId);
            }
            return JaxbWls.NamespaceFilter.EMPTY_INPUT_SOURCE;
        }

        public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
            super.startElement("http://www.bea.com/ns/weblogic/90", localName, qname, atts);
        }
    }
}
