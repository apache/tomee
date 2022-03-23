/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.resource.heroku;

import org.apache.openejb.junit.ApplicationComposerRule;
import org.apache.openejb.loader.Files;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.SimpleLog;
import org.apache.openejb.util.NetworkUtil;
import org.hsqldb.Server;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import jakarta.annotation.Resource;
import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Classes
@ContainerProperties({
        @ContainerProperties.Property(name = "DATABASE_URL", value = "hsql://SA@localhost:${hsqldb}/adb"),
        @ContainerProperties.Property(name = "db", value = "new://Resource?type=DataSource&properties-provider=heroku")
})
@SimpleLog
public class HerokuDatabasePropertiesProviderResourceTest {
    @Rule
    public final TestRule rule = RuleChain
            .outerRule(new TestRule() {
                @Override
                public Statement apply(final Statement base, final Description description) {
                    return new Statement() {
                        @Override
                        public void evaluate() throws Throwable {
                            final int port = NetworkUtil.getNextAvailablePort();
                            final Server server = new Server();
                            server.setAddress("localhost");
                            server.setPort(port);
                            server.setDatabaseName(0, "adb");
                            server.setDatabasePath(0, Files.mkdirs(new File("target/HerokuDatabasePropertiesProviderResourceTest")).getAbsolutePath());
                            server.start();
                            System.setProperty("hsqldb", Integer.toString(port));
                            try {
                                base.evaluate();
                            } finally {
                                server.stop();
                            }
                        }
                    };
                }
            })
            .around(new ApplicationComposerRule(this));

    @Resource(name = "db")
    private DataSource db;

    @Test
    public void herokuToJava() throws Exception {
        assertNotNull(db);

        final Connection connection = db.getConnection();
        final DatabaseMetaData metaData = connection.getMetaData();
        final String url = metaData.getURL();
        assertTrue(url.startsWith("jdbc:hsqldb:hsql://localhost:"));
        assertTrue(url.endsWith("/adb"));
        assertEquals("SA", metaData.getUserName());
        connection.close();
    }
}
