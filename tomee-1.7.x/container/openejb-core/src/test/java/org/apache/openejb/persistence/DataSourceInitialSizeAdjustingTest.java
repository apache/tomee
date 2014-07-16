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

package org.apache.openejb.persistence;

import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(ApplicationComposer.class)
public class DataSourceInitialSizeAdjustingTest {
    @Module
    public Persistence persistence() throws Exception {
        final PersistenceUnit unit = new PersistenceUnit("foo-unit");
        unit.setJtaDataSource("DataSourceInitialSizeAdjustingTest");
        return new Persistence(unit);
    }

    @Configuration
    public Properties config() {
        final String prefix = getClass().getSimpleName();
        final Properties p = new Properties();
        p.setProperty(prefix, "new://Resource?type=DataSource");
        p.setProperty(prefix + ".JdbcDriver", "org.hsqldb.jdbcDriver");
        p.setProperty(prefix + ".JdbcUrl", "jdbc:hsqldb:mem:bval");
        p.setProperty(prefix + ".InitialSize", "15");
        p.setProperty(prefix + ".JtaManaged", "true");
        return p;
    }

    @Test
    public void checkNonJtaPoolSizeWasCorrected() {
        final String prefix = getClass().getSimpleName();
        for (final ResourceInfo info : SystemInstance.get().getComponent(OpenEjbConfiguration.class).facilities.resources) {
            if ((prefix + "NonJta").equals(info.id)) {
                assertEquals("5", info.properties.getProperty("InitialSize"));
                return;
            }
        }
        fail("DataSource not found");
    }
}
