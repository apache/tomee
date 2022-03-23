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
package org.apache.openejb.config;

import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

@RunWith(ApplicationComposer.class)
public class JMXDataSourceTest {

    @Resource(name = "JMXDataSourceTest")
    private DataSource dataSource;

    @Test
    public void checkDsIsRegistered() throws MalformedObjectNameException {
        final ObjectName on = new ObjectName("openejb.management:ObjectType=datasources,DataSource=JMXDataSourceTest");
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(on));
    }

    @Test
    public void checkNumActiveAndNumIdle() throws MalformedObjectNameException, IntrospectionException,
        InstanceNotFoundException, ReflectionException,
        AttributeNotFoundException, MBeanException {

        final Map<String, Object> map = getDatasourceJmxMap();
        assertNotNull(map.get("numActive"));
        assertNotNull(map.get("numIdle"));


        assertAttributeValue("numActive", ((Integer) 0));
        assertAttributeValue("numIdle", ((Integer) 0));

        assertNotNull(dataSource);
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            assertAttributeValue("numActive", ((Integer) 1));

        } catch (final SQLException e) {
            fail();
        } finally {
            if (connection != null) try {
                connection.close();
            } catch (final SQLException e) {
                fail();
            }
        }
        assertAttributeValue("numActive", ((Integer) 0));
    }

    private <T> void assertAttributeValue(final String name, final T value) throws MalformedObjectNameException,
        IntrospectionException,
        InstanceNotFoundException,
        AttributeNotFoundException,
        MBeanException, ReflectionException {
        final Map<String, Object> map = getDatasourceJmxMap();
        assertEquals((T) value, (T) map.get(name));
    }

    private Map<String, Object> getDatasourceJmxMap() throws MalformedObjectNameException, InstanceNotFoundException,
        IntrospectionException, ReflectionException,
        MBeanException, AttributeNotFoundException {
        final ObjectName on = new ObjectName("openejb.management:ObjectType=datasources,DataSource=JMXDataSourceTest");
        final MBeanInfo mBeanInfo = ManagementFactory.getPlatformMBeanServer().getMBeanInfo(on);
        assertNotNull(mBeanInfo);
        final Map<String, Object> map = new HashMap<>();
        for (final MBeanAttributeInfo mBeanAttributeInfo : mBeanInfo.getAttributes()) {
            final String name = mBeanAttributeInfo.getName();
            final Object value = ManagementFactory.getPlatformMBeanServer().getAttribute(on, name);
            map.put(name, value);
        }
        return map;
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put(LocalMBeanServer.OPENEJB_JMX_ACTIVE, Boolean.TRUE.toString());

        p.put("JMXDataSourceTest", "new://Resource?type=DataSource");
        p.put("JMXDataSourceTest.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("JMXDataSourceTest.JdbcUrl", "jdbc:hsqldb:mem:bval");
        return p;
    }

    @Module
    public Persistence persistence() {
        final PersistenceUnit unit = new PersistenceUnit("JMXDataSourceTest-unit");
        unit.addClass(EntityToValidate.class);
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.getProperties().setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        unit.setExcludeUnlistedClasses(true);

        final Persistence persistence = new Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }

    @Entity
    public static class EntityToValidate {
        @Id
        @GeneratedValue
        private long id;

        public long getId() {
            return id;
        }

        public void setId(final long i) {
            id = i;
        }
    }
}
