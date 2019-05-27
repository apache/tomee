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
package org.superbiz.microprofile.config.source.database;

import org.apache.commons.dbutils.DbUtils;
import org.eclipse.microprofile.config.spi.ConfigSource;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseConfigSource implements ConfigSource {
    private DataSource dataSource;

    public DatabaseConfigSource() {
        try {
            dataSource = (DataSource) new InitialContext().lookup("openejb:Resource/config-source-database");
        } catch (final NamingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Map<String, String> getProperties() {
        final Map<String, String> properties = new HashMap<>();

        try {
            final Connection connection = dataSource.getConnection();
            final PreparedStatement query = connection.prepareStatement("SELECT NAME, VALUE FROM CONFIGURATIONS");
            final ResultSet names = query.executeQuery();

            while (names.next()) {
                properties.put(names.getString(0), names.getString(1));
            }

            DbUtils.closeQuietly(names);
            DbUtils.closeQuietly(query);
            DbUtils.closeQuietly(connection);
        } catch (final SQLException e) {
            e.printStackTrace();
        }

        return properties;
    }

    @Override
    public String getValue(final String propertyName) {
        try {
            final Connection connection = dataSource.getConnection();
            final PreparedStatement query =
                    connection.prepareStatement("SELECT VALUE FROM CONFIGURATIONS WHERE NAME = ?");
            query.setString(1, propertyName);
            final ResultSet value = query.executeQuery();

            if (value.next()) {
                return value.getString(1);
            }

            DbUtils.closeQuietly(value);
            DbUtils.closeQuietly(query);
            DbUtils.closeQuietly(connection);
        } catch (final SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getName() {
        return DatabaseConfigSource.class.getSimpleName();
    }
}
