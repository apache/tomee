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
package org.apache.openejb.resource.jdbc.router;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class FailOverRouter extends AbstractRouter implements ConnectionProvider {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SERVER, FailOverRouter.class);

    private String delimiter = ",";
    private String datasourceNames = "";

    private Collection<DataSource> dataSources = new CopyOnWriteArrayList<DataSource>();

    @Override
    public DataSource getDataSource() {
        for (final DataSource ds : dataSources) {
            return ds;
        }
        throw new IllegalStateException("No datasource configured");
    }

    @Override
    public Connection getConnection() throws SQLException {
        for (final DataSource ds : dataSources) {
            try {
                return ds.getConnection();
            } catch (final SQLException e) {
                // no-op
            }
        }
        throw new SQLException("Can't connect to any datasources of " + dataSources);
    }

    @Override
    public Connection getConnection(String user, String pwd) throws SQLException {
        for (final DataSource ds : dataSources) {
            try {
                return ds.getConnection(user, pwd);
            } catch (final SQLException e) {
                // no-op
            }
        }
        throw new SQLException("Can't connect to any datasources of " + dataSources);
    }

    public void setDatasourceNames(final String datasourceNames) {
        this.datasourceNames = datasourceNames;
        initDataSources();
    }

    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
        initDataSources();
    }

    private void initDataSources() {
        for (final String ds : datasourceNames.split(Pattern.quote(delimiter))) {
            try {
                final Object o = getOpenEJBResource(ds.trim());
                if (DataSource.class.isInstance(o)) {
                    LOGGER.debug("Found datasource '" + ds + "'");
                    dataSources.add(DataSource.class.cast(o));
                }
            } catch (final NamingException error) {
                LOGGER.error("Can't find datasource '" + ds + "'", error);
            }
        }
    }

    public Collection<DataSource> getDataSources() {
        return dataSources;
    }

    public void updateDataSources(final Collection<DataSource> ds) {
        dataSources.clear();
        dataSources.addAll(ds);
    }
}
