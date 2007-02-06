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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.String;

/**
 * @version $Revision: 471447 $ $Date: 2006-11-05 07:42:50 -0800 (Sun, 05 Nov 2006) $
 */
public class OpenejbJarTest extends TestCase {

    public void testValidOpenejbJar() throws Exception {
        unmarshalAndMarshal(OpenejbJarType.class, "openejb-jar-2-full.xml");
    }

    public void testInvalidOpenejbJar() throws Exception {
        unmarshalAndMarshal(OpenejbJarType.class, "openejb-jar-2-invalid.xml", "openejb-jar-2-full.xml");
    }

    public void testGeronimoOpenejbXml() throws Exception {
        unmarshalAndMarshal(GeronimoEjbJarType.class, "geronimo-openejb-full.xml");
    }

    public void testGeronimoOpenejbInvalidXml() throws Exception {
        unmarshalAndMarshal(GeronimoEjbJarType.class, "geronimo-openejb-invalid.xml", "geronimo-openejb-corrected.xml");
    }

    public void testOpenejbJarMoreInvalid() throws Exception {
        unmarshalAndMarshal(OpenejbJarType.class, "daytrader-original.xml", "daytrader-corrected.xml");
    }

    private <T> void unmarshalAndMarshal(Class<T> type, java.lang.String xmlFileName) throws Exception {
        unmarshalAndMarshal(type, xmlFileName, xmlFileName);
    }

    private <T> void unmarshalAndMarshal(Class<T> type, java.lang.String xmlFileName, java.lang.String expectedFile) throws Exception {

        Object object = JaxbOpenejbJar2.unmarshal(type, getInputStream(xmlFileName));

        String actual = JaxbOpenejbJar2.marshal(type, object);

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
