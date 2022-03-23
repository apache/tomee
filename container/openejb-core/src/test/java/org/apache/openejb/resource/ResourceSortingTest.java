/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.resource;

import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jakarta.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SimpleLog // close to org.apache.openejb.assembler.classic.ResourceInfoComparatorTest but with a real case!
@RunWith(ApplicationComposer.class)
public class ResourceSortingTest {

    public static final List<Character> LETTERS = asList('A', 'B', 'C', 'D', 'E');
    public static final String BASE_NAME = "foo:bar=Hello";

    @Module
    public Resources r() throws JAXBException, SAXException, ParserConfigurationException {
        final StringBuilder resourcesXml = new StringBuilder()
                .append("<Resources>\n");
        for (final Character c : LETTERS) {
            resourcesXml.append("<Resource id=\"Hello").append(c)
                    .append("\" class-name=\"org.apache.openejb.resource.ResourceSortingTest$Foo\">")
                    .append("name foo:bar=Hello").append(c).append("</Resource>");
        }
        resourcesXml.append("</Resources>");
        return JaxbOpenejb.unmarshal(Resources.class, new ByteArrayInputStream(resourcesXml.toString().getBytes()));
    }

    @Test
    public void checkOrder() {
        final Iterator<Character> letters = LETTERS.iterator();
        final Iterator<String> ids = Foo.IDS.iterator();
        while (letters.hasNext()) {
            assertTrue(ids.hasNext());
            assertEquals(BASE_NAME + letters.next(), ids.next());
        }
        assertFalse(letters.hasNext());
    }



    public static class Foo {
        private static final List<String> IDS = new ArrayList<>();
        private String name;

        public void setName(String name) {
            this.name = name;
            IDS.add(name);
        }
    }
}
