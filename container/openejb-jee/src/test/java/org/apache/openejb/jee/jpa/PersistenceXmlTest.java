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
package org.apache.openejb.jee.jpa;


import junit.framework.TestCase;
import org.apache.openejb.jee.JAXBContextFactory;
import org.apache.openejb.jee.jpa.unit.JaxbPersistenceFactory;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @version $Revision$ $Date$
 */
public class PersistenceXmlTest extends TestCase {

    /**
     * @throws Exception
     */
    public void testPersistenceVersion1() throws Exception {
        final JAXBContext ctx = JAXBContextFactory.newInstance(Persistence.class);
        final Unmarshaller unmarshaller = ctx.createUnmarshaller();

        final URL resource = this.getClass().getClassLoader().getResource("persistence-example.xml");
        final InputStream in = resource.openStream();
        final java.lang.String expected = readContent(in);

        final Persistence element = (Persistence) unmarshaller.unmarshal(new ByteArrayInputStream(expected.getBytes()));
        unmarshaller.setEventHandler(new TestValidationEventHandler());
        System.out.println("unmarshalled");

        final Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(element, baos);

        final String actual = new String(baos.toByteArray());

        final Diff myDiff = new Diff(expected, actual);
        myDiff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        assertTrue("Files are similar " + myDiff, myDiff.similar());
    }

    /**
     * @throws Exception
     */
    public void testPersistenceVersion2() throws Exception {
        final JAXBContext ctx = JAXBContextFactory.newInstance(Persistence.class);
        final Unmarshaller unmarshaller = ctx.createUnmarshaller();

        final URL resource = this.getClass().getClassLoader().getResource("persistence_2.0-example.xml");
        final InputStream in = resource.openStream();
        final java.lang.String expected = readContent(in);

        final Persistence element = (Persistence) unmarshaller.unmarshal(new ByteArrayInputStream(expected.getBytes()));
        unmarshaller.setEventHandler(new TestValidationEventHandler());
        System.out.println("unmarshalled");

        final Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(element, baos);

        final String actual = new String(baos.toByteArray());

        final Diff myDiff = new Diff(expected, actual);
        myDiff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        assertTrue("Files are similar " + myDiff, myDiff.similar());
    }

    public void testPersistenceJakarta() throws Exception {

        // make sure we can still parse previous versions
        {
            final URL resource = this.getClass().getClassLoader().getResource("persistence_2.0-example.xml");
            final InputStream in = resource.openStream();

            final Persistence element = JaxbPersistenceFactory.getPersistence(Persistence.class, in);

            assertNotNull(element);
            System.out.println("unmarshalled " + element);
        }
        {
            final URL resource = this.getClass().getClassLoader().getResource("persistence-example.xml");
            final InputStream in = resource.openStream();

            final Persistence element = JaxbPersistenceFactory.getPersistence(Persistence.class, in);

            assertNotNull(element);
            System.out.println("unmarshalled " + element);
        }

        // try new jakarta namespace
        {
            final URL resource = this.getClass().getClassLoader().getResource("persistence_2.0-jakarta.xml");
            final InputStream in = resource.openStream();

            final Persistence element = JaxbPersistenceFactory.getPersistence(Persistence.class, in);

            assertNotNull(element);
            System.out.println("unmarshalled " + element);
        }

    }

    private java.lang.String readContent(InputStream in) throws IOException {
        final StringBuilder sb = new StringBuilder();
        in = new BufferedInputStream(in);
        int i = in.read();
        while (i != -1) {
            sb.append((char) i);
            i = in.read();
        }
        final java.lang.String content = sb.toString();
        return content;
    }

    private static class TestValidationEventHandler implements ValidationEventHandler {
        public boolean handleEvent(final ValidationEvent validationEvent) {
            System.out.println(validationEvent.getMessage());
            return true;
        }
    }
}
