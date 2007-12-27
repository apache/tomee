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
package org.apache.openejb.jee.wls;

import junit.framework.TestCase;

import javax.xml.bind.JAXBElement;

import org.custommonkey.xmlunit.Diff;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.BufferedInputStream;

/**
 * @version $Revision$ $Date$
 */
public class JaxbWlsTest extends TestCase {

    public void testVersion9() throws Exception {
        marshallAndUnmarshall("wls-ejb-jar.xml");
    }

    public void testVersion8() throws Exception {
        marshallAndUnmarshall("wls-v81-ejb-jar.xml");
    }

    public void marshallAndUnmarshall(String xmlFile) throws Exception {

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(xmlFile);
        String expected = readContent(in);

        Object object = JaxbWls.unmarshal(WeblogicEjbJar.class, new ByteArrayInputStream(expected.getBytes()));

        JAXBElement element = (JAXBElement) object;

        assertTrue(element.getValue() instanceof WeblogicEjbJar);

        String actual = JaxbWls.marshal(WeblogicEjbJar.class, element);

        Diff myDiff = new Diff(expected, actual);
        assertTrue("Files are similar " + myDiff, myDiff.similar());
    }

    private String readContent(InputStream in) throws IOException {
        StringBuffer sb = new StringBuffer();
        in = new BufferedInputStream(in);
        int i = in.read();
        while (i != -1) {
            sb.append((char)i);
            i = in.read();
        }
        String content = sb.toString();
        return content;
    }
}
