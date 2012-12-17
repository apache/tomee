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
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.config.sys.Resource;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class AliasesFromXmlTest {
    @Test
    public void ensureAliasesAreParsed() throws IOException, SAXException, ParserConfigurationException {
        final String xml = "<?xml version=\"1.0\"?>" +
                "<openejb>" +
                "   <Resource id=\"foo\" aliases=\"bar\" type=\"DataSource\" />" +
                "</openejb>";

        final Openejb openejb = JaxbOpenejb.readConfig(new InputSource(new ByteArrayInputStream(xml.getBytes())));
        assertEquals(1, openejb.getResource().size());

        final Resource resource = openejb.getResource().iterator().next();
        assertEquals(1, resource.getAliases().size());
        assertEquals("foo", resource.getId());
        assertEquals("bar", resource.getAliases().iterator().next());
    }
}
