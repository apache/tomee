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
package org.apache.tomee.embedded;

import org.apache.openejb.testing.Application;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.tomee.embedded.junit.jupiter.RunWithTomEEEmbedded;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// needs its own surefire execution since the test jar contains several @Application,
// -Dtomee.application-composer.application=org.apache.tomee.embedded.ResourceInjectionExtensionTest$TheApp
@RunWithTomEEEmbedded
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ResourceInjectionExtensionTest {
    @Application
    private TheApp app;

    @Resource // matches the resource by the name of the field
    private DataSource db;

    @Resource(name = "other")
    private DataSource named;

    @Resource(name = "unknown") // no fallback on another DataSource for an explicit name
    private DataSource unknown;

    @Resource(lookup = "openejb:Resource/other")
    private DataSource lookedUp;

    @Resource(lookup = "openejb:Resource/missing")
    private DataSource missing;

    @Test
    public void descriptorInjection() throws SQLException {
        assertNotNull(app);
        assertNotNull(app.db);
        assertUrl(app.db, "jdbc:hsqldb:mem:resource-injection");
    }

    @Test
    public void testInstanceInjection() throws SQLException {
        assertNotNull(db);
        assertUrl(db, "jdbc:hsqldb:mem:resource-injection");
    }

    @Test
    public void explicitResource() throws SQLException {
        assertNotNull(named);
        assertUrl(named, "jdbc:hsqldb:mem:resource-injection-other");
        assertNotNull(lookedUp);
        assertUrl(lookedUp, "jdbc:hsqldb:mem:resource-injection-other");
        assertNull(missing);
        assertNull(unknown);
    }

    private static void assertUrl(final DataSource dataSource, final String expected) throws SQLException {
        try (final Connection connection = dataSource.getConnection()) {
            assertFalse(connection.isClosed());
            assertTrue(connection.getMetaData().getURL().startsWith(expected), connection.getMetaData().getURL());
        }
    }

    @Application
    @Classes(cdi = true, context = "app")
    public static class TheApp {
        @Resource
        private DataSource db;

        @org.apache.openejb.testing.Configuration
        public Properties config() {
            return new PropertiesBuilder()
                    .p("db", "new://Resource?type=DataSource")
                    .p("db.JdbcDriver", "org.hsqldb.jdbcDriver")
                    .p("db.JdbcUrl", "jdbc:hsqldb:mem:resource-injection")
                    .p("other", "new://Resource?type=DataSource")
                    .p("other.JdbcDriver", "org.hsqldb.jdbcDriver")
                    .p("other.JdbcUrl", "jdbc:hsqldb:mem:resource-injection-other")
                    .build();
        }
    }
}
