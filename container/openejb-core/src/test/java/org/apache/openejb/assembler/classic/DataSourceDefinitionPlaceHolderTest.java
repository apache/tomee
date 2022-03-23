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
package org.apache.openejb.assembler.classic;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(ApplicationComposer.class)
public class DataSourceDefinitionPlaceHolderTest {

    @EJB
    private DSBean uniqueDataSource;

    @Module
    public Class<?>[] app() throws Exception {
        return new Class<?>[]{DSBean.class};
    }

    @Configuration
    public Properties properties() {
        final Properties properties = new Properties();
        properties.setProperty("jndi", "java:comp/env/superDS");
        properties.setProperty("driver", "org.hsqldb.jdbc.JDBCDataSource");
        properties.setProperty("user", "sa");
        properties.setProperty("pwd", "");
        properties.setProperty("url", "jdbc:hsqldb:mem:superDS");
        return properties;
    }

    @DataSourceDefinition(
        name = "${jndi}",
        className = "${driver}",
        user = "${user}",
        password = "${pwd}",
        url = "${url}"
    )

    @Singleton
    public static class DSBean {
        @SuppressWarnings("SpringJavaAutowiringInspection")
        @Resource(name = "java:comp/env/superDS")
        private DataSource ds;

        public DataSource ds() {
            return ds;
        }
    }

    private void check(final DataSource ds) throws NoSuchFieldException, IllegalAccessException {
        assertNotNull(ds);
        assertThat(ds, instanceOf(BasicDataSource.class));

        final BasicDataSource bds = (BasicDataSource) ds;
        assertEquals("sa", bds.getUsername());
        assertEquals("", bds.getPassword());

        final Field fieldDs = bds.getClass().getDeclaredField("ds");
        fieldDs.setAccessible(true);
        final JDBCDataSource realDs = (JDBCDataSource) fieldDs.get(bds);
        assertEquals("jdbc:hsqldb:mem:superDS", realDs.getUrl());
        assertEquals("sa", realDs.getUser());
    }

    @Test
    public void checkInjection() throws Exception {
        check(uniqueDataSource.ds());
    }

    @Test
    public void checkLookup() throws Exception {
        final DataSource ds = (DataSource) SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext().lookup("java:comp/env/superDS");
        check(ds);
    }
}
