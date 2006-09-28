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
package org.apache.openejb.alt.config.ejb;

import junit.framework.TestCase;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEvent;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;

/**
 * @version $Revision$ $Date$
 */
public class OpenejbJarTest extends TestCase {

    public void testAll() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(OpenejbJar.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("openejb-jar.xml");
        java.lang.String expected = readContent(in);

        unmarshaller.setEventHandler(new TestValidationEventHandler());
        Object object = unmarshaller.unmarshal(new ByteArrayInputStream(expected.getBytes()));

        OpenejbJar ejbJar = (OpenejbJar) object;
        System.out.println("unmarshalled");

        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(object, baos);

        java.lang.String actual = new java.lang.String(baos.toByteArray());

        assertEquals(expected, actual);
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
