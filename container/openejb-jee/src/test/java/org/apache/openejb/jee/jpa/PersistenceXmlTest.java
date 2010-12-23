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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.Marshaller;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.net.URL;

import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.JAXBContextFactory;
import org.custommonkey.xmlunit.Diff;

/**
 * @version $Revision$ $Date$
 */
public class PersistenceXmlTest extends TestCase {

    /**
     * @throws Exception
     */
    public void testPersistenceVersion1() throws Exception {
        JAXBContext ctx = JAXBContextFactory.newInstance(Persistence.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();

        URL resource = this.getClass().getClassLoader().getResource("persistence-example.xml");
        InputStream in = resource.openStream();
        java.lang.String expected = readContent(in);

        Persistence element =  (Persistence) unmarshaller.unmarshal(new ByteArrayInputStream(expected.getBytes()));
        unmarshaller.setEventHandler(new TestValidationEventHandler());
        System.out.println("unmarshalled");

        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(element, baos);

        String actual = new String(baos.toByteArray());

        Diff myDiff = new Diff(expected, actual);
        assertTrue("Files are similar " + myDiff, myDiff.similar());
    }
    
    /**
     * @throws Exception
     */
    public void testPersistenceVersion2() throws Exception {
        JAXBContext ctx = JAXBContextFactory.newInstance(Persistence.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();

        URL resource = this.getClass().getClassLoader().getResource("persistence_2.0-example.xml");
        InputStream in = resource.openStream();
        java.lang.String expected = readContent(in);

        Persistence element =  (Persistence) unmarshaller.unmarshal(new ByteArrayInputStream(expected.getBytes()));
        unmarshaller.setEventHandler(new TestValidationEventHandler());
        System.out.println("unmarshalled");

        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(element, baos);

        String actual = new String(baos.toByteArray());

        Diff myDiff = new Diff(expected, actual);
        assertTrue("Files are similar " + myDiff, myDiff.similar());
    }

    private java.lang.String readContent(InputStream in) throws IOException {
        StringBuffer sb = new StringBuffer();
        in = new BufferedInputStream(in);
        int i = in.read();
        while (i != -1) {
            sb.append((char)i);
            i = in.read();
        }
        java.lang.String content = sb.toString();
        return content;
    }

    private static class TestValidationEventHandler implements ValidationEventHandler {
        public boolean handleEvent(ValidationEvent validationEvent) {
            System.out.println(validationEvent.getMessage());
            return true;
        }
    }
}
