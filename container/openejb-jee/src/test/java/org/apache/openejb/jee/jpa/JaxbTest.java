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
package org.apache.openejb.jee.jpa;

import junit.framework.TestCase;

import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;

import org.custommonkey.xmlunit.Diff;

/**
 * @version $Rev$ $Date$
 */
public class JaxbTest extends TestCase {

    public void testEntityMappings() throws Exception {
        unmarshalAndMarshal(EntityMappings.class, "jpa-mapping-full.xml");
        unmarshalAndMarshal(EntityMappings.class, "jpa-mapping-full_2.0.xml");
    }

    private <T> void unmarshalAndMarshal(final Class<T> type, final java.lang.String xmlFileName) throws Exception {
        unmarshalAndMarshal(type, xmlFileName, xmlFileName);
    }

    private <T> void unmarshalAndMarshal(final Class<T> type, final java.lang.String xmlFileName, final java.lang.String expectedFile) throws Exception {

        final Object object = JpaJaxbUtil.unmarshal(type, getInputStream(xmlFileName));

        final String actual = JpaJaxbUtil.marshal(type, object);

        final String expected;
        if (xmlFileName.equals(expectedFile)) {
            expected = readContent(getInputStream(xmlFileName));
        } else {
            expected = readContent(getInputStream(expectedFile));
        }
        final Diff myDiff = new Diff(expected, actual);
        assertTrue("Files are similar " + myDiff, myDiff.similar());
    }

    private <T> InputStream getInputStream(final String xmlFileName) {
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
