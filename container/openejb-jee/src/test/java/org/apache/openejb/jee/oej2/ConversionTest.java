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

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class ConversionTest extends TestCase {

    public void testConversion() throws Exception {
        JAXBElement element = (JAXBElement) Oej2JaxbUtil.unmarshal(OpenejbJarType.class, getInputStream("openejb-jar-2-full.xml"));
        OpenejbJarType o2 = (OpenejbJarType) element.getValue();

        GeronimoEjbJarType g2 = new GeronimoEjbJarType();

        g2.setEnvironment(o2.getEnvironment());
        g2.setSecurity(o2.getSecurity());
        g2.getService().addAll(o2.getService());
        g2.getMessageDestination().addAll(o2.getMessageDestination());

        List<JAXBElement<?>> jndi = g2.getJndiEnvironmentRefsGroup();
        for (EnterpriseBean bean : o2.getEnterpriseBeans()) {
            jndi.addAll(bean.getAbstractNamingEntry());
            //
        }

        JAXBElement root = new JAXBElement(new QName("http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0","ejb-jar"), GeronimoEjbJarType.class, g2);
        String result = Oej2JaxbUtil.marshal(GeronimoEjbJarType.class, root);
        assertEquals(readContent(getInputStream("geronimo-openejb-converted.xml")), result);

    }

    private <T> void unmarshalAndMarshal(Class<T> type, java.lang.String xmlFileName, java.lang.String expectedFile) throws Exception {

        Object object = Oej2JaxbUtil.unmarshal(type, getInputStream(xmlFileName));

        java.lang.String actual = Oej2JaxbUtil.marshal(type, object);

        if (xmlFileName.equals(expectedFile)) {
            String sourceXml = readContent(getInputStream(xmlFileName));
            assertEquals(sourceXml, actual);
        } else {
            String expected = readContent(getInputStream(expectedFile));
            assertEquals(expected, actual);
        }
    }

    private <T>InputStream getInputStream(String xmlFileName) {
        return getClass().getClassLoader().getResourceAsStream(xmlFileName);
    }

    private String readContent(InputStream in) throws IOException {
        StringBuffer sb = new StringBuffer();
        in = new BufferedInputStream(in);
        int i = in.read();
        while (i != -1) {
            sb.append((char) i);
            i = in.read();
        }
        return sb.toString();
    }

}
