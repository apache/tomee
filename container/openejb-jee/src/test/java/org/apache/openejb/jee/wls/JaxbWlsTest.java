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
import junit.framework.AssertionFailedError;

import jakarta.xml.bind.JAXBElement;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.DetailedDiff;

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

    public void marshallAndUnmarshall(final String xmlFile) throws Exception {

        final InputStream in = this.getClass().getClassLoader().getResourceAsStream(xmlFile);
        final String expected = readContent(in);

        final Object object = JaxbWls.unmarshal(WeblogicEjbJar.class, new ByteArrayInputStream(expected.getBytes()));

        final JAXBElement element = (JAXBElement) object;

        assertTrue(element.getValue() instanceof WeblogicEjbJar);

        final String actual = JaxbWls.marshal(WeblogicEjbJar.class, element);

        XMLUnit.setIgnoreWhitespace(true);
        try {
            final Diff myDiff = new DetailedDiff(new Diff(expected, actual));
            assertTrue("Files are not similar " + myDiff, myDiff.similar());
        } catch (final AssertionFailedError e) {
            assertEquals(expected, actual);
            throw e;
        }
    }

    private String readContent(InputStream in) throws IOException {
        final StringBuilder sb = new StringBuilder();
        in = new BufferedInputStream(in);
        int i = in.read();
        while (i != -1) {
            sb.append((char) i);
            i = in.read();
        }
        final String content = sb.toString();
        return content;
    }
}
