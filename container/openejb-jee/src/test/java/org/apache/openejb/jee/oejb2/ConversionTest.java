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
package org.apache.openejb.jee.oejb2;

import junit.framework.TestCase;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

import jakarta.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version $Rev$ $Date$
 */
public class ConversionTest extends TestCase {

    public void testConversion() throws Exception {
        final JAXBElement element = (JAXBElement) JaxbOpenejbJar2.unmarshal(OpenejbJarType.class, getInputStream("openejb-jar-2-full.xml"));
        final OpenejbJarType o2 = (OpenejbJarType) element.getValue();

        final GeronimoEjbJarType g2 = new GeronimoEjbJarType();

        g2.setEnvironment(o2.getEnvironment());
        g2.setSecurity(o2.getSecurity());
        g2.getService().addAll(o2.getService());
        g2.getMessageDestination().addAll(o2.getMessageDestination());

        for (final EnterpriseBean bean : o2.getEnterpriseBeans()) {
            g2.getAbstractNamingEntry().addAll(bean.getAbstractNamingEntry());
            g2.getPersistenceContextRef().addAll(bean.getPersistenceContextRef());
            g2.getEjbLocalRef().addAll(bean.getEjbLocalRef());
            g2.getEjbRef().addAll(bean.getEjbRef());
            g2.getResourceEnvRef().addAll(bean.getResourceEnvRef());
            g2.getResourceRef().addAll(bean.getResourceRef());
            g2.getServiceRef().addAll(bean.getServiceRef());

            if (bean instanceof RpcBean) {
                final RpcBean rpcBean = (RpcBean) bean;
                if (rpcBean.getTssLink() != null) {
                    g2.getTssLink().add(new TssLinkType(rpcBean.getEjbName(), rpcBean.getTssLink(), rpcBean.getJndiName()));
                }
            }
        }

        final JAXBElement root = new JAXBElement(new QName("http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0", "ejb-jar"), GeronimoEjbJarType.class, g2);
        final String result = JaxbOpenejbJar2.marshal(GeronimoEjbJarType.class, root);
        final String expected = readContent(getInputStream("geronimo-openejb-converted.xml"));

        final Diff myDiff = new DetailedDiff(new Diff(expected, result));
        final AtomicInteger differenceNumber = new AtomicInteger(0); // just to get an int wrapper for the test
        myDiff.overrideDifferenceListener(new DifferenceListener() {
            @Override
            public int differenceFound(final Difference difference) {
                if (!difference.isRecoverable()) {
                    differenceNumber.incrementAndGet();
                    System.err.println(">>> " + difference.toString());
                }
                return 0;
            }

            @Override
            public void skippedComparison(final Node node, final Node node1) {
                // no-op
            }
        });
        assertTrue("Files are not similar", myDiff.similar());
    }

    private <T> void unmarshalAndMarshal(final Class<T> type, final java.lang.String xmlFileName, final java.lang.String expectedFile) throws Exception {

        final Object object = JaxbOpenejbJar2.unmarshal(type, getInputStream(xmlFileName));

        final java.lang.String actual = JaxbOpenejbJar2.marshal(type, object);

        if (xmlFileName.equals(expectedFile)) {
            final String sourceXml = readContent(getInputStream(xmlFileName));
            assertEquals(sourceXml, actual);
        } else {
            final String expected = readContent(getInputStream(expectedFile));
            assertEquals(expected, actual);
        }
    }

    private InputStream getInputStream(final String xmlFileName) {
        return getClass().getClassLoader().getResourceAsStream(xmlFileName);
    }

    private String readContent(InputStream in) throws IOException {
        final StringBuilder sb = new StringBuilder();
        in = new BufferedInputStream(in);
        int i = in.read();
        while (i != -1) {
            sb.append((char) i);
            i = in.read();
        }
        return sb.toString();
    }

}
