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
package org.apache.openejb.arquillian;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.apache.openejb.loader.JarLocation.jarLocation;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class EarWarResourcesXmlTest {
    @Deployment
    public static Archive<?> app() {
        return ShrinkWrap.create(EnterpriseArchive.class, "EarWarResourcesXmlTest.ear")
                .addAsModule(ShrinkWrap.create(WebArchive.class, "web.war")
                        .addClass(EarWarResourcesXmlTest.class)
                        .addAsLibraries(jarLocation(EmbeddedDriver.class))
                        .addAsWebInfResource(new StringAsset("<resources>\n" +
                                "  <Resource id=\"derby\" type=\"DataSource\">\n" +
                                "    JdbcDriver = org.apache.derby.jdbc.EmbeddedDriver\n" +
                                "    JdbcUrl = jdbc:derby:memory:EarWarResourcesXmlTest;create=true\n" +
                                "    UserName = SA\n" +
                                "    Lazy = true\n" +
                                "  </Resource>\n" +
                                "</resources>"), "resources.xml"));
    }

    @Resource(name = "derby")
    private DataSource ds;

    @Test
    public void checkServerInfo() throws SQLException {
        try (final Connection c = ds.getConnection()) {
            assertEquals("jdbc:derby:memory:EarWarResourcesXmlTest", c.getMetaData().getURL());
        }
    }
}
