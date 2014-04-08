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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.resource.jdbc;

import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.sys.Resource;
import org.hsqldb.jdbcDriver;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class IgnoreDefaultTest {
    private static Assembler assembler = new Assembler();
    private static ConfigurationFactory configurationFactory = new ConfigurationFactory();

    @Test
    public void createNormalDs() throws OpenEJBException, NamingException {
        final Resource resource = new Resource(IgnoreDefaultTest.class.getName() + "#normal");
        resource.setType(DataSource.class.getName());
        final ResourceInfo info = configurationFactory.configureService(resource, ResourceInfo.class);
        assembler.createResource(info);

        check(resource.getId(), "sa", "");
    }

    @Test
    public void createWithoutDefaultDs() throws OpenEJBException, NamingException {
        final Resource resource = new Resource(IgnoreDefaultTest.class.getName() + "#without-default");
        resource.setType(DataSource.class.getName());
        resource.getProperties().setProperty("IgnoreDefaultValues", "true");
        resource.getProperties().setProperty("JdbcDriver", jdbcDriver.class.getName());
        final ResourceInfo info = configurationFactory.configureService(resource, ResourceInfo.class);
        assembler.createResource(info);

        check(resource.getId(), null, null);
    }

    private void check(String id, String user, String password) throws NamingException {
        final DataSource ds = (DataSource) assembler.getContainerSystem().getJNDIContext().lookup("openejb/Resource/" + id);
        assertThat(ds, instanceOf(org.apache.commons.dbcp.BasicDataSource.class));
        assertEquals(user, ((org.apache.commons.dbcp.BasicDataSource) ds).getUsername());
        assertEquals(password, ((org.apache.commons.dbcp.BasicDataSource) ds).getPassword());
    }
}
