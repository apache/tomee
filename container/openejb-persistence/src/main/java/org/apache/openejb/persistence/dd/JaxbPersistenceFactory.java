/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.persistence.dd;

import org.apache.openejb.persistence.PersistenceDeployer;
import org.xml.sax.Attributes;
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
import java.net.URL;

/**
 * @version $Revision$ $Date$
 */
public class JaxbPersistenceFactory {
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

        public PersistenceFilter(XMLReader arg0) {
            super(arg0);
        }

        @Override
        public void startElement(String arg0, String arg1, String arg2, Attributes arg3) throws SAXException {
            super.startElement(PersistenceDeployer.PERSISTENCE_SCHEMA, arg1, arg2, arg3);
        }
    }
}
