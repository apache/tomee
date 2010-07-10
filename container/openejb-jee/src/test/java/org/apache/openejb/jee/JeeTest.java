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
package org.apache.openejb.jee;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import junit.framework.TestCase;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @version $Revision$ $Date$
 */
public class JeeTest extends TestCase {
    public void testEjbJar() throws Exception {
        String fileName = "ejb-jar-example1.xml";
//        String fileName = "ejb-jar-empty.xml";

//        marshalAndUnmarshal(EjbJar.class, fileName);


        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        long start = System.currentTimeMillis();

//        Unmarshaller unmarshaller = new UnmarshallerImpl(Collections.<JAXBMarshaller>singleton(EjbJarJaxB.INSTANCE));
//        Marshaller marshaller = new MarshallerImpl(Collections.<JAXBMarshaller>singleton(EjbJarJaxB.INSTANCE));
        JAXBContext ctx = JAXBContextFactory.newInstance(EjbJar.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        Marshaller marshaller = ctx.createMarshaller();

        NamespaceFilter xmlFilter = new NamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());
        unmarshaller.setEventHandler(new TestValidationEventHandler());

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);
        String expected = readContent(in);

        SAXSource source = new SAXSource(xmlFilter, new InputSource(new ByteArrayInputStream(expected.getBytes())));
        Object object = unmarshaller.unmarshal(source);

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(object, baos);

        System.out.println("time: " + (System.currentTimeMillis() - start));
    }

    public void testEjbTimeout() throws Exception {
        String fileName = "ejb-jar-timeout.xml";
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);

        Object o = JaxbJavaee.unmarshalJavaee(EjbJar.class, in);

        EjbJar ejbJar = (EjbJar) o;
        EnterpriseBean bean = ejbJar.getEnterpriseBean("A");

        assertTrue("The bean A is not a SessionBean", bean instanceof SessionBean);
        SessionBean sbean = (SessionBean) bean;

		assertNotNull("Unable to get the AccessTimeout value", sbean.getAccessTimeout());
		assertNotNull("Unable to get the StatefulTimeout value", sbean.getStatefulTimeout());
    }

    public void testSessionSynchronization() throws Exception {
        String fileName = "ejb-session-synchronization.xml";
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);

        Object o = JaxbJavaee.unmarshalJavaee(EjbJar.class, in);

        EjbJar ejbJar = (EjbJar) o;
        EnterpriseBean bean = ejbJar.getEnterpriseBean("TestBean");

        assertTrue("The bean TestBean  is not a SessionBean", bean instanceof SessionBean);
        SessionBean sbean = (SessionBean) bean;

        assertNotNull("Unable to get the afterBegin value", sbean.getAfterBeginMethod());
        assertNotNull("Unable to get the beforeCompletion value", sbean.getBeforeCompletionMethod());
        assertNotNull("Unable to get the afterCompletion value", sbean.getAfterCompletionMethod());

        assertNotNull("Unable to get the afterBegin value", sbean.getAfterBegin());
        assertNotNull("Unable to get the beforeCompletion value", sbean.getBeforeCompletion());
        assertNotNull("Unable to get the afterCompletion value", sbean.getAfterCompletion());
    }

    public void testAroundTimeout() throws Exception {
        String fileName = "ejb-jar-aroundtimeout.xml";
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);

        Object o = JaxbJavaee.unmarshalJavaee(EjbJar.class, in);

        EjbJar ejbJar = (EjbJar) o;
        EnterpriseBean bean = ejbJar.getEnterpriseBean("TestBean");

        assertTrue("The bean TestBean  is not a SessionBean", bean instanceof SessionBean);
        SessionBean sbean = (SessionBean) bean;

        AroundTimeout beanAroundTimeout = sbean.getAroundTimeout().get(0);
        assertEquals("aroundTimeout", beanAroundTimeout.getMethodName());

        AroundTimeout interceptorAroundTimeout =ejbJar.getInterceptors()[0].getAroundTimeout().get(0);
        assertEquals("aroundTimeout", interceptorAroundTimeout.getMethodName());
    }

    public void testEjbJarMdb20() throws Exception {
        String fileName = "ejb-jar-mdb-2.0.xml";
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);

        Object o = JaxbJavaee.unmarshalJavaee(EjbJar.class, in);

        EjbJar ejbJar = (EjbJar) o;

        MessageDrivenBean bean = (MessageDrivenBean) ejbJar.getEnterpriseBean("MyMdb");

        Properties properties = bean.getActivationConfig().toProperties();

        assertEquals(4, properties.size());
        /*
      <message-selector>mySelector</message-selector>
      <acknowledge-mode>Auto-acknowledge</acknowledge-mode>
      <message-driven-destination>
        <destination-type>javax.jms.Queue</destination-type>
        <subscription-durability>Durable</subscription-durability>

         */
        assertEquals("mySelector", properties.get("messageSelector"));
        assertEquals("Auto-acknowledge", properties.get("acknowledgeMode"));
        assertEquals("javax.jms.Queue", properties.get("destinationType"));
        assertEquals("Durable", properties.get("subscriptionDurability"));
    }

    public void testApplication() throws Exception {
        marshalAndUnmarshal(Application.class, "application-example.xml", null);
    }

    public void testApplicationClient() throws Exception {
        marshalAndUnmarshal(ApplicationClient.class, "application-client-example.xml", null);
    }

    public void testWar() throws Exception {
        marshalAndUnmarshal(WebApp.class, "web-example.xml", "web-example-expected.xml");
    }

    public void testWar2_3() throws Exception {
        marshalAndUnmarshal(WebApp.class, "web_2.3-example.xml", "web_2.3-example-expected.xml");
    }

    public void testTld() throws Exception {
        marshalAndUnmarshal(TldTaglib.class, "tld-example.xml", null);
    }

    public void testRar10() throws Exception {
        Connector10 c10 = marshalAndUnmarshal(Connector10.class, "connector-1.0-example.xml", null);
        Connector c = Connector.newConnector(c10);
        JAXBContext ctx = JAXBContextFactory.newInstance(Connector.class);
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(c, baos);

        byte[] bytes = baos.toByteArray();
        String actual = new String(bytes);

        String expected;
            InputStream in2 = this.getClass().getClassLoader().getResourceAsStream("connector-1.0-example-expected-1.6.xml");
            expected = readContent(in2);

        try {
            StaxCompare.compare(expected, actual);
        } catch (Exception e) {
//            System.out.append(actual);
            writeToTmpFile(bytes, "connector-1.0-example.xml");
            throw e;
        }
    }

    public void testRar15() throws Exception {
        marshalAndUnmarshal(Connector.class, "connector-1.5-example.xml", null);
    }

    public void testRar16() throws Exception {
        marshalAndUnmarshal(Connector.class, "connector-1.6-example.xml", null);
    }

    public static <T> T marshalAndUnmarshal(Class<T> type, String sourceXmlFile, String expectedXmlFile) throws Exception {
        InputStream in = JeeTest.class.getClassLoader().getResourceAsStream(sourceXmlFile);
        T object = (T)JaxbJavaee.unmarshalJavaee(type, in);
        in.close();
        assertTrue(object.getClass().isAssignableFrom(type));

        JAXBContext ctx = JAXBContextFactory.newInstance(type);
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(object, baos);

        byte[] bytes = baos.toByteArray();
        String actual = new String(bytes);

        String expected;
        if (expectedXmlFile == null) {
            InputStream in2 = JeeTest.class.getClassLoader().getResourceAsStream(sourceXmlFile);
            expected = readContent(in2);
        } else {
            InputStream in2 = JeeTest.class.getClassLoader().getResourceAsStream(expectedXmlFile);
            expected = readContent(in2);
        }

        try {
            StaxCompare.compare(expected, actual);
        } catch (Exception e) {
//            System.out.append(actual);
            writeToTmpFile(bytes, sourceXmlFile);
            throw e;
        }
        return object;
    }

    public static class NamespaceFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public NamespaceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return EMPTY_INPUT_SOURCE;
        }

        public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
            super.startElement("http://java.sun.com/xml/ns/javaee", localName, qname, atts);
        }
    }

    private static void writeToTmpFile(byte[] bytes, String xmlFileName) {
        try {
            File tempFile = File.createTempFile("jaxb-output", "xml");
            FileOutputStream out = new FileOutputStream(tempFile);
            out.write(bytes);
            out.close();
            System.out.println("Jaxb output of " + xmlFileName + " written to " + tempFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readContent(InputStream in) throws IOException {
        StringBuffer sb = new StringBuffer();
        in = new BufferedInputStream(in);
        try {
            int i = in.read();
            while (i != -1) {
                sb.append((char) i);
                i = in.read();
            }
        } finally {
            in.close();
        }
        String content = sb.toString();
        return content;
    }

    private static class TestValidationEventHandler implements ValidationEventHandler {
        public boolean handleEvent(ValidationEvent validationEvent) {
            System.out.println(validationEvent.getMessage());
            System.out.println(validationEvent.getLocator());
            return false; // if an error occurs we must be aware of
        }
    }
}
