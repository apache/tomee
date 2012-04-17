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

import java.lang.management.ManagementFactory;
import java.util.Properties;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Configuration;
import org.apache.openejb.junit.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class JMXDataSourceTest {
    @Test
    public void checkDsIsRegistered() throws MalformedObjectNameException {
        final ObjectName on = new ObjectName("openejb.management:ObjectType=datasources,DataSource=JMXDataSourceTest");
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(on));
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
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

        public void setId(long i) {
            id = i;
        }
    }
}
