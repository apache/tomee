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

import org.apache.openejb.jee.JAXBContextFactory;
import org.apache.openejb.jee.JaxbJavaee;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.UnmarshallerHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

/**
 * @version $Revision$ $Date$
 */
public class JaxbPersistenceFactory {
    public static final String PERSISTENCE_SCHEMA = "http://java.sun.com/xml/ns/persistence";

    public static <T> T getPersistence(final Class<T> clazz, final InputStream persistenceDescriptor) throws Exception {
        final JAXBContext jc = clazz.getClassLoader() == JaxbPersistenceFactory.class.getClassLoader() ?
                JaxbJavaee.getContext(clazz) : JAXBContextFactory.newInstance(clazz);
        final Unmarshaller u = jc.createUnmarshaller();
        final UnmarshallerHandler uh = u.getUnmarshallerHandler();

        // create a new XML parser
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        final SAXParser parser = factory.newSAXParser();

        final XMLReader xmlReader = parser.getXMLReader();

        // Create a filter to intercept events
        final PersistenceFilter xmlFilter = new PersistenceFilter(xmlReader);

        // Be sure the filter has the JAXB content handler set (or it wont work)
        xmlFilter.setContentHandler(uh);
        final SAXSource source = new SAXSource(xmlFilter, new InputSource(persistenceDescriptor));

        return (T) u.unmarshal(source);
    }

    public static <T> T getPersistence(final Class<T> clazz, final URL url) throws Exception {

        try (InputStream persistenceDescriptor = url.openStream()) {

            return getPersistence(clazz, persistenceDescriptor);

        }
    }

    // Inject the proper namespace
    public static class PersistenceFilter extends JaxbJavaee.NoSourceFilter {
        public PersistenceFilter(final XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        protected String eeUri(final String uri) {
            // there should not be any other namespace, but let's see if we can match all of them
            // http://java.sun.com/xml/ns/persistence
            // http://xmlns.jcp.org/xml/ns/persistence
            // https://jakarta.ee/xml/ns/persistence
            return uri != null && uri.contains("/persistence") ? PERSISTENCE_SCHEMA: uri;
        }

        @Override
        public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
            return EMPTY_INPUT_SOURCE;
        }
    }
}
