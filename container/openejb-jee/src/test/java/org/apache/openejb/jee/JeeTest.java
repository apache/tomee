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

import junit.framework.TestCase;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.XMLConstants;
import jakarta.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.*;
import java.util.List;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class JeeTest extends TestCase {

    /**
     * TODO Doesn't seem there are any asserts here
     *
     * @throws Exception
     */
    public void testEjbJar() throws Exception {
        final String fileName = "ejb-jar-example1.xml";
        //        String fileName = "ejb-jar-empty.xml";

        //        marshalAndUnmarshal(EjbJar.class, fileName);

        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        final SAXParser parser = factory.newSAXParser();

        final long start = System.currentTimeMillis();

        //        Unmarshaller unmarshaller = new UnmarshallerImpl(Collections.<JAXBMarshaller>singleton(EjbJarJaxB.INSTANCE));
        //        Marshaller marshaller = new MarshallerImpl(Collections.<JAXBMarshaller>singleton(EjbJarJaxB.INSTANCE));
        final JAXBContext ctx = JAXBContextFactory.newInstance(EjbJar.class);
        final Unmarshaller unmarshaller = ctx.createUnmarshaller();
        final Marshaller marshaller = ctx.createMarshaller();

        final NamespaceFilter xmlFilter = new NamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());
        unmarshaller.setEventHandler(new TestValidationEventHandler());

        final InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);
        final String expected = readContent(in);

        final SAXSource source = new SAXSource(xmlFilter, new InputSource(new ByteArrayInputStream(expected.getBytes())));
        final Object object = unmarshaller.unmarshal(source);

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(object, baos);

        System.out.println("time: " + (System.currentTimeMillis() - start));
    }

    public void testEjbTimeout() throws Exception {
        final String fileName = "ejb-jar-timeout.xml";
        final InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);

        final Object o = JaxbJavaee.unmarshalJavaee(EjbJar.class, in);

        final EjbJar ejbJar = (EjbJar) o;
        final EnterpriseBean bean = ejbJar.getEnterpriseBean("A");

        assertTrue("The bean A is not a SessionBean", bean instanceof SessionBean);
        final SessionBean sbean = (SessionBean) bean;

        assertNotNull("Unable to get the StatefulTimeout value", sbean.getStatefulTimeout());
    }

    public void testSessionSynchronization() throws Exception {
        final String fileName = "ejb-session-synchronization.xml";
        final InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);

        final Object o = JaxbJavaee.unmarshalJavaee(EjbJar.class, in);

        final EjbJar ejbJar = (EjbJar) o;
        final EnterpriseBean bean = ejbJar.getEnterpriseBean("TestBean");

        assertTrue("The bean TestBean  is not a SessionBean", bean instanceof SessionBean);
        final SessionBean sbean = (SessionBean) bean;

        assertNotNull("Unable to get the afterBegin value", sbean.getAfterBeginMethod());
        assertNotNull("Unable to get the beforeCompletion value", sbean.getBeforeCompletionMethod());
        assertNotNull("Unable to get the afterCompletion value", sbean.getAfterCompletionMethod());

        assertNotNull("Unable to get the afterBegin value", sbean.getAfterBegin());
        assertNotNull("Unable to get the beforeCompletion value", sbean.getBeforeCompletion());
        assertNotNull("Unable to get the afterCompletion value", sbean.getAfterCompletion());
    }

    public void testAroundTimeout() throws Exception {
        final String fileName = "ejb-jar-aroundtimeout.xml";
        final InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);

        final Object o = JaxbJavaee.unmarshalJavaee(EjbJar.class, in);

        final EjbJar ejbJar = (EjbJar) o;
        final EnterpriseBean bean = ejbJar.getEnterpriseBean("TestBean");

        assertTrue("The bean TestBean  is not a SessionBean", bean instanceof SessionBean);
        final SessionBean sbean = (SessionBean) bean;

        final AroundTimeout beanAroundTimeout = sbean.getAroundTimeout().get(0);
        assertEquals("aroundTimeout", beanAroundTimeout.getMethodName());

        final AroundTimeout interceptorAroundTimeout = ejbJar.getInterceptors()[0].getAroundTimeout().get(0);
        assertEquals("aroundTimeout", interceptorAroundTimeout.getMethodName());
    }

    public void testTimerSchedule() throws Exception {
        final String fileName = "ejb-jar-timeschedule.xml";
        final InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);

        final Object o = JaxbJavaee.unmarshalJavaee(EjbJar.class, in);

        final EjbJar ejbJar = (EjbJar) o;
        final EnterpriseBean bean = ejbJar.getEnterpriseBean("TestBean");

        assertTrue("The bean TestBean  is not a SessionBean", bean instanceof SessionBean);
        final SessionBean sbean = (SessionBean) bean;

        final List<Timer> timers = sbean.getTimer();
        assertEquals(1, timers.size());

        final Timer timer = timers.get(0);
        final TimerSchedule timerSchedule = timer.getSchedule();
        assertEquals("10", timerSchedule.getSecond());
        assertEquals("10", timerSchedule.getMinute());
        assertEquals("*", timerSchedule.getDayOfMonth());
        assertEquals("Mon", timerSchedule.getDayOfWeek());
        assertEquals("*", timerSchedule.getHour());
        assertEquals("Nov", timerSchedule.getMonth());
        assertEquals("*", timerSchedule.getYear());

        assertEquals("2010-03-01T13:00:00Z", timer.getStart().toXMLFormat());
        assertEquals("2012-12-11T14:19:00Z", timer.getEnd().toXMLFormat());

        final NamedMethod timeoutMethod = timer.getTimeoutMethod();
        assertEquals("testScheduleMethod", timeoutMethod.getMethodName());
        assertEquals("jakarta.ejb.Timer", timeoutMethod.getMethodParams().getMethodParam().get(0));

        assertEquals(Boolean.FALSE, timer.getPersistent());
        assertEquals("America/New_York", timer.getTimezone());
        assertEquals("TestInfo", timer.getInfo());
    }

    public void testEjbJarMdb20() throws Exception {
        final String fileName = "ejb-jar-mdb-2.0.xml";
        final InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);

        final Object o = JaxbJavaee.unmarshalJavaee(EjbJar.class, in);

        final EjbJar ejbJar = (EjbJar) o;

        final MessageDrivenBean bean = (MessageDrivenBean) ejbJar.getEnterpriseBean("MyMdb");

        final Properties properties = bean.getActivationConfig().toProperties();

        assertEquals(4, properties.size());
        /*
        <message-selector>mySelector</message-selector>
        <acknowledge-mode>Auto-acknowledge</acknowledge-mode>
        <message-driven-destination>
        <destination-type>jakarta.jms.Queue</destination-type>
        <subscription-durability>Durable</subscription-durability>

         */
        assertEquals("mySelector", properties.get("messageSelector"));
        assertEquals("Auto-acknowledge", properties.get("acknowledgeMode"));
        assertEquals("jakarta.jms.Queue", properties.get("destinationType"));
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

    public void testWar5() throws Exception {
        marshalAndUnmarshal(WebApp.class, "web_5-example.xml", "web_5-example-expected.xml");
    }

    public void testTld() throws Exception {
        marshalAndUnmarshal(TldTaglib.class, "tld-example.xml", null);
    }

    public void testRar10() throws Exception {
        final Connector10 c10 = marshalAndUnmarshal(Connector10.class, "connector-1.0-example.xml", null);
        final Connector c = Connector.newConnector(c10);
        final JAXBContext ctx = JAXBContextFactory.newInstance(Connector.class);
        final Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(c, baos);

        final byte[] bytes = baos.toByteArray();
        final String actual = new String(bytes);

        final String expected;
        final InputStream in2 = this.getClass().getClassLoader().getResourceAsStream("connector-1.0-example-expected-1.6.xml");
        expected = readContent(in2);

        try {
            StaxCompare.compare(expected, actual);
        } catch (final Exception e) {
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

    public void testWebServiceHandlers() throws Exception {
        final QName[] expectedServiceNames = {new QName("http://www.helloworld.org", "HelloService", "ns1"), new QName("http://www.bar.org", "HelloService", "bar"),
            new QName("http://www.bar1.org", "HelloService", "bar"), new QName(XMLConstants.NULL_NS_URI, "HelloService", "foo"), new QName(XMLConstants.NULL_NS_URI, "*"), null};
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("handler.xml")) {
            final HandlerChains handlerChains = (HandlerChains) JaxbJavaee.unmarshalHandlerChains(HandlerChains.class, in);
            for (int index = 0; index < handlerChains.getHandlerChain().size(); index++) {
                final HandlerChain handlerChain = handlerChains.getHandlerChain().get(index);
                final QName serviceName = handlerChain.getServiceNamePattern();
                final QName expectedServiceName = expectedServiceNames[index];
                if (expectedServiceName == null) {
                    assertNull(serviceName);
                } else {
                    assertEquals("serviceNamePattern at index " + index + " mismatches", expectedServiceName, serviceName);
                }
            }
            System.out.println(JaxbJavaee.marshal(HandlerChains.class, handlerChains));
        }
    }

    public static <T> T marshalAndUnmarshal(final Class<T> type, final String sourceXmlFile, final String expectedXmlFile) throws Exception {
        final InputStream in = JeeTest.class.getClassLoader().getResourceAsStream(sourceXmlFile);
        final T object = (T) JaxbJavaee.unmarshalJavaee(type, in);
        in.close();
        assertTrue(object.getClass().isAssignableFrom(type));

        final JAXBContext ctx = JAXBContextFactory.newInstance(type);
        final Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(object, baos);

        final byte[] bytes = baos.toByteArray();
        final String actual = new String(bytes);

        final String expected;
        if (expectedXmlFile == null) {
            final InputStream in2 = JeeTest.class.getClassLoader().getResourceAsStream(sourceXmlFile);
            expected = readContent(in2);
        } else {
            final InputStream in2 = JeeTest.class.getClassLoader().getResourceAsStream(expectedXmlFile);
            expected = readContent(in2);
        }

        try {
            StaxCompare.compare(expected, actual);
        } catch (final Exception e) {
//            System.out.append(actual);
            writeToTmpFile(bytes, sourceXmlFile);
            throw e;
        }
        return object;
    }

    public static class NamespaceFilter extends XMLFilterImpl {

        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public NamespaceFilter(final XMLReader xmlReader) {
            super(xmlReader);
        }

        public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
            return EMPTY_INPUT_SOURCE;
        }

        public void startElement(final String uri, final String localName, final String qname, final Attributes atts) throws SAXException {
            super.startElement("http://java.sun.com/xml/ns/javaee", localName, qname, atts);
        }
    }

    private static void writeToTmpFile(final byte[] bytes, final String xmlFileName) {
        try {
            File tempFile = null;
            try {
                tempFile = File.createTempFile("jaxb-output", "xml");
            } catch (final Throwable e) {
                final File tmp = new File("tmp");
                if (!tmp.exists() && !tmp.mkdirs()) {
                    throw new IOException("Failed to create local tmp directory: " + tmp.getAbsolutePath());
                }

                tempFile = File.createTempFile("jaxb-output", "xml", tmp);
            }
            final FileOutputStream out = new FileOutputStream(tempFile);
            out.write(bytes);
            out.close();
            System.out.println("Jaxb output of " + xmlFileName + " written to " + tempFile.getAbsolutePath());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static String readContent(InputStream in) throws IOException {
        final StringBuilder sb = new StringBuilder();
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
        return sb.toString();
    }

    private static class TestValidationEventHandler implements ValidationEventHandler {

        public boolean handleEvent(final ValidationEvent validationEvent) {
            System.out.println(validationEvent.getMessage());
            System.out.println(validationEvent.getLocator());
            return false; // if an error occurs we must be aware of
        }
    }
}
