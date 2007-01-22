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
package org.apache.openejb.jee.oej2;

import junit.framework.TestCase;
import org.xml.sax.InputSource;
import org.apache.openejb.jee.g2.GeronimoEjbJar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEvent;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.sax.SAXSource;
import java.lang.*;
import java.lang.String;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.StringReader;

/**
 * @version $Revision: 471447 $ $Date: 2006-11-05 07:42:50 -0800 (Sun, 05 Nov 2006) $
 */
public class OpenejbJarTest extends TestCase {

    public void testNothing(){}
    /**
     * @throws Exception
     */
    public void testValidOpenejbJar() throws Exception {
        unmarshalAndMarshal(OpenejbJarType.class, "openejb-jar-2-full.xml");
    }

    public void testInvalidOpenejbJar() throws Exception {
        unmarshalAndMarshal(OpenejbJarType.class, "openejb-jar-2-invalid.xml", "openejb-jar-2-full.xml");
    }

    public void _testGeronimoOpenejbXml() throws Exception {
        unmarshalAndMarshal(GeronimoEjbJar.class, "geronimo-openejb-full.xml");
    }

    private <T> void unmarshalAndMarshal(Class<T> type, java.lang.String xmlFileName) throws Exception {
        unmarshalAndMarshal(type, xmlFileName, xmlFileName);
    }

    private <T> void unmarshalAndMarshal(Class<T> type, java.lang.String xmlFileName, java.lang.String expectedFile) throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(type);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();

        String sourceXml = readContent(xmlFileName);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        // Create a filter to intercept events
        NamespaceFilter xmlFilter = new NamespaceFilter(parser.getXMLReader());

        // Be sure the filter has the JAXB content handler set (or it wont
        // work)
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());

        SAXSource source = new SAXSource(xmlFilter, new InputSource(new StringReader(sourceXml)));

        Object object = unmarshaller.unmarshal(source);
//        JAXBElement element =  (JAXBElement) object;
        unmarshaller.setEventHandler(new OpenejbJarTest.TestValidationEventHandler());
//        T app = (T) element.getValue();
//        System.out.println("unmarshalled");

        Marshaller marshaller = ctx.createMarshaller();

        marshaller.setProperty("jaxb.formatted.output", true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(object, baos);

        String actual = new String(baos.toByteArray());

        if (xmlFileName.equals(expectedFile)){
            assertEquals(sourceXml, actual);
        } else {
            String expected = readContent(expectedFile);
            assertEquals(expected, actual);
        }
    }

    private <T>String readContent(String xmlFileName) throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(xmlFileName);
        String sourceXml = readContent(in);
        return sourceXml;
    }

    private String readContent(InputStream in) throws IOException {
        StringBuffer sb = new StringBuffer();
        in = new BufferedInputStream(in);
        int i = in.read();
        while (i != -1) {
            sb.append((char) i);
            i = in.read();
        }
        String content = sb.toString();
        return content;
    }

    private static class TestValidationEventHandler implements ValidationEventHandler {
        public boolean handleEvent(ValidationEvent validationEvent) {
            System.out.println(validationEvent.getMessage());
            return true;
        }
    }
}
