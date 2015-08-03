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
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.sys.JSonConfigReader;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.resource.jdbc.dbcp.BasicDataSource;
import org.apache.openejb.testing.AppResource;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class JSonConfigTest {
    @Configuration
    public String config() {
        System.setProperty("JSonConfigTest.foo", "bar");
        return "openejb.json";
    }

    @Module
    public EjbJar jar() {
        return new EjbJar();
    }

    @AppResource
    private Context ctx;

    @Test
    public void checkDsIsHere() throws NamingException {
        final BasicDataSource ds = BasicDataSource.class.cast(ctx.lookup("openejb:Resource/json-datasource"));
        assertNotNull(ds);
        assertEquals(123, ds.getMaxTotal());
        assertEquals("jdbc:hsqldb:mem:json", ds.getJdbcUrl());
    }

    @Test
    public void checkSystProps() {
        assertEquals("b", System.getProperty("a"));
        assertEquals("d", System.getProperty("c"));
        assertEquals("g", System.getProperty("e.f"));
        assertEquals("j", System.getProperty("e.h.i"));
        assertEquals("properties-value", System.getProperty("properties-key"));
        assertEquals("2", System.getProperty("object.attribute.#1"));
    }

    @Test
    public void checkPlaceHolder() {
        assertEquals("bar", System.getProperty("JSonConfigTest.foo")); // the real one
        assertEquals("bar", System.getProperty("placeholder")); // the configured one
    }

    @Test
    public void simpleRead() throws IOException, OpenEJBException {
        final Openejb openejb = JSonConfigReader.read(Openejb.class, Thread.currentThread().getContextClassLoader().getResource(config()).openStream());

        assertEquals(1, openejb.getResource().size());
        final Resource resource = openejb.getResource().iterator().next();
        assertEquals("json-datasource", resource.getId());
        assertTrue("123".equals(resource.getProperties().getProperty("MaxTotal")));
        assertTrue("jdbc:hsqldb:mem:json".equals(resource.getProperties().getProperty("JdbcUrl")));

        assertEquals(1, openejb.getDeployments().size());
        assertEquals("apps", openejb.getDeployments().iterator().next().getDir());

        assertEquals(1, openejb.getContainer().size());
        final Container container = openejb.getContainer().iterator().next();
        assertEquals("STATELESS", container.getType());
        assertEquals("10 seconds", container.getProperties().getProperty("AccessTimeout"));
    }
}
