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

import org.apache.openejb.config.sys.Container;
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

public class PropertiesProviderFromXmlTest {
    @Test
    public void ensureAliasesAreParsed() throws IOException, SAXException, ParserConfigurationException {
        final String xml = "<?xml version=\"1.0\"?>" +
            "<tomee>" +
            "   <Resource id=\"foo\" type=\"DataSource\" properties-provider=\"org.acme.Foo\"/>" +
            "   <Container id=\"bar\" ctype=\"STATELESS\" properties-provider=\"org.acme.Foo\"/>" +
            "</tomee>";

        final Openejb openejb = JaxbOpenejb.readConfig(new InputSource(new ByteArrayInputStream(xml.getBytes())));
        assertEquals(1, openejb.getResource().size());

        final Resource resource = openejb.getResource().iterator().next();
        assertEquals("foo", resource.getId());
        assertEquals("org.acme.Foo", resource.getPropertiesProvider());

        final Container container = openejb.getContainer().iterator().next();
        assertEquals("bar", container.getId());
        assertEquals("org.acme.Foo", container.getPropertiesProvider());
    }
}
