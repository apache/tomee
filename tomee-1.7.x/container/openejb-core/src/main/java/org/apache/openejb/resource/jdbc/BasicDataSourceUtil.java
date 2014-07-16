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

package org.apache.openejb.resource.jdbc;

import org.apache.openejb.resource.jdbc.plugin.DataSourcePlugin;
import org.apache.xbean.finder.ResourceFinder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public final class BasicDataSourceUtil {
    private BasicDataSourceUtil() {
        // no-op
    }

    public static DataSourcePlugin getDataSourcePlugin(final String jdbcUrl) throws SQLException {
        // determine the vendor based on the jdbcUrl stirng "jdbc:${Vendor}:properties"
        final String vendor = getJdbcName(jdbcUrl);

        // no vendor so no plugin
        if (vendor == null) {
            return null;
        }

        // find the plugin class
        String pluginClassName = null;
        try {
            final ResourceFinder finder = new ResourceFinder("META-INF");
            final Map<String, String> plugins = finder.mapAvailableStrings(DataSourcePlugin.class.getName());
            pluginClassName = plugins.get(vendor);
        } catch (final IOException ignored) {
            // couldn't determine the plugins, which isn't fatal
        }

        // no plugin found
        if (pluginClassName == null || pluginClassName.length() <= 0) {
            return null;
        }

        // create the plugin
        try {
            final Class pluginClass = Class.forName(pluginClassName);
            return (DataSourcePlugin) pluginClass.newInstance();
        } catch (final ClassNotFoundException e) {
            throw new SQLException("Unable to load data source helper class '" + pluginClassName + "' for database '" + vendor + "'");
        } catch (final Exception e) {
            throw (SQLException) new SQLException("Unable to create data source helper class '" + pluginClassName + "' for database '" + vendor + "'").initCause(e);
        }
    }

    public static String getJdbcName(String jdbcUrl) {
        // nothing gets you nothing
        if (jdbcUrl == null) {
            return null;
        }

        // strip off "jdbc:"
        if (!jdbcUrl.startsWith("jdbc:")) {
            return null;
        }
        jdbcUrl = jdbcUrl.substring("jdbc:".length());

        // return text up to first ":" if present
        final int index = jdbcUrl.indexOf(':');

        // It is ok to have no trailing ':'.  This may be a url like jdbc:specialDB.
        if (index >= 0) {
            jdbcUrl = jdbcUrl.substring(0, index);
        }

        return jdbcUrl;
    }

}
