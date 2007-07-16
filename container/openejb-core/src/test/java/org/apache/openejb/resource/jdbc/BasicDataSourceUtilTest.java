/**
 *
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
package org.apache.openejb.resource.jdbc;

import junit.framework.TestCase;

import java.sql.SQLException;

public class BasicDataSourceUtilTest extends TestCase {
    public void testGetJdbcName() {
        assertEquals("hsqldb", BasicDataSourceUtil.getJdbcName("jdbc:hsqldb:file:foo"));
        assertEquals("idb", BasicDataSourceUtil.getJdbcName("jdbc:idb:foo"));

        // not a proper jdbc url
        assertNull(BasicDataSourceUtil.getJdbcName("notjdbc:hsqldb:file:foo"));

        // no trailing :
        assertEquals("specialDB", BasicDataSourceUtil.getJdbcName("jdbc:specialDB"));

        // null
        assertNull(BasicDataSourceUtil.getJdbcName(null));

        // empty string
        assertNull(BasicDataSourceUtil.getJdbcName(""));
    }

    public void testGetDataSourcePlugin() throws Exception {
        // all current known plugins
        assertPluginClass("jdbc:hsqldb:file:foo", HsqldbDataSourcePlugin.class);
        assertPluginClass("jdbc:idb:foo", InstantdbDataSourcePlugin.class);
        assertPluginClass("jdbc:derby:foo", DerbyDataSourcePlugin.class);

        // not a proper jdbc url
        assertNull(BasicDataSourceUtil.getDataSourcePlugin("notjdbc:hsqldb:file:foo"));

        // no trailing :
        assertPluginClass("jdbc:hsqldb", HsqldbDataSourcePlugin.class);

        // null
        assertNull(BasicDataSourceUtil.getDataSourcePlugin(null));

        // empty string
        assertNull(BasicDataSourceUtil.getDataSourcePlugin(""));
    }

    private void assertPluginClass(String jdbcUrl, Class<? extends DataSourcePlugin> pluginClass) throws SQLException {
        DataSourcePlugin plugin = BasicDataSourceUtil.getDataSourcePlugin(jdbcUrl);
        assertNotNull(plugin);
        assertSame(pluginClass, plugin.getClass());
    }

}
