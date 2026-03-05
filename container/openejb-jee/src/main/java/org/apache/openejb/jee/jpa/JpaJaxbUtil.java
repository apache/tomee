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
package org.apache.openejb.jee.jpa;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.apache.openejb.jee.JAXBContextFactory;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEventHandler;
import jakarta.xml.bind.ValidationEvent;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @version $Rev$ $Date$
 */
public class JpaJaxbUtil {

    public static <T> String marshal(final Class<T> type, final Object object) throws JAXBException {
        final JAXBContext ctx2 = JAXBContextFactory.newInstance(type);
        final Marshaller marshaller = ctx2.createMarshaller();

        marshaller.setProperty("jaxb.formatted.output", true);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(object, baos);

        return new String(baos.toByteArray());
    }

    public static <T> void marshal(final Class<T> type, final Object object, final OutputStream out) throws JAXBException {
        final JAXBContext ctx2 = JAXBContextFactory.newInstance(type);
        final Marshaller marshaller = ctx2.createMarshaller();

        marshaller.setProperty("jaxb.formatted.output", true);

        marshaller.marshal(object, out);

    }

    public static <T> Object unmarshal(final Class<T> type, final InputStream in) throws ParserConfigurationException, SAXException, JAXBException {
        final InputSource inputSource = new InputSource(in);

        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);

        final JAXBContext ctx = JAXBContextFactory.newInstance(type);
        final Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() {
            public boolean handleEvent(final ValidationEvent validationEvent) {
                System.out.println(validationEvent);
                return false;
            }
        });

        return unmarshaller.unmarshal(inputSource);
    }
}
