/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.jdbc;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.resource.jdbc.managed.local.ManagedDataSource;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.Assert.*;

@RunWith(ApplicationComposer.class)
public class TomcatDataSourceConfigurationTest {
    @Resource(name = "test")
    private DataSource ds;

    @Module
    public EjbJar mandatory() {
        return new EjbJar();
    }

    @Configuration
    public Properties props() {
        final String prefix = getClass().getSimpleName();
        return new PropertiesBuilder()
                .p("openejb.jdbc.datasource-creator", TomEEDataSourceCreator.class.getName())
                .p(prefix, "new://Resource?type=DataSource&name=test")
                .p(prefix + ".JdbcDriver", "org.hsqldb.jdbcDriver")
                .p(prefix + ".JdbcUrl", "jdbc:hsqldb:mem:tomeeDSConfigTest")
                .p(prefix + ".InitialSize", "15")
                .p(prefix + ".JtaManaged", "true")
                .p(prefix + ".MaxWait", "5000")
                .p(prefix + ".MinEvictableIdleTimeMillis", "7200000")
                .p(prefix + ".TimeBetweenEvictionRuns", "7300000")
                .p(prefix + ".password", "tiger...}")
                .build();
    }

    /*
     * TOMEE-2125 and TOMEE-2968
     */
    @Test
    public void testPoolConfiguration() {
        assertNotNull(ds);
        final TomEEDataSourceCreator.TomEEDataSource tds = TomEEDataSourceCreator.TomEEDataSource.class.cast(ManagedDataSource.class.cast(ds).getDelegate());

        assertNotNull(tds);

        PoolConfiguration poolConfig = tds.getPool().getPoolProperties();
        assertNotNull(poolConfig);
        assertEquals("test", poolConfig.getName());
        assertEquals("jdbc:hsqldb:mem:tomeeDSConfigTest", poolConfig.getUrl());
        assertEquals("org.hsqldb.jdbcDriver", poolConfig.getDriverClassName());
        assertEquals("test", poolConfig.getName());
        assertEquals(5000, poolConfig.getMaxWait());
        assertEquals(7200000, poolConfig.getMinEvictableIdleTimeMillis());
        assertEquals(7300000, poolConfig.getTimeBetweenEvictionRunsMillis());
        assertEquals("tiger...}", poolConfig.getPassword());
    }


}
