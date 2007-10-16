/**
 *
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
package org.apache.openejb.jee.jpa.unit;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * @version $Revision$ $Date$
 */
public class JaxbPersistenceFactory {
    public static final String PERSISTENCE_SCHEMA = "http://java.sun.com/xml/ns/persistence";
    public static Persistence getPersistence(URL url) throws Exception {
        InputStream persistenceDescriptor = null;

        try {

            persistenceDescriptor = url.openStream();

            JAXBContext jc = JAXBContext.newInstance(Persistence.class);
            Unmarshaller u = jc.createUnmarshaller();
            UnmarshallerHandler uh = u.getUnmarshallerHandler();

            // create a new XML parser
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            SAXParser parser = factory.newSAXParser();

            XMLReader xmlReader = parser.getXMLReader();

            // Create a filter to intercept events
            PersistenceFilter xmlFilter = new PersistenceFilter(xmlReader);

            // Be sure the filter has the JAXB content handler set (or it wont
            // work)
            xmlFilter.setContentHandler(uh);
            SAXSource source = new SAXSource(xmlFilter, new InputSource(persistenceDescriptor));

            return (Persistence) u.unmarshal(source);

        } finally {
            if (persistenceDescriptor != null) persistenceDescriptor.close();
        }
    }

    // Inject the proper namespace
    public static class PersistenceFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public PersistenceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return EMPTY_INPUT_SOURCE;
        }
    }
}
