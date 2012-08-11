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
package org.apache.openejb.resource.jdbc.pool;

import org.apache.openejb.resource.XAResourceWrapper;
import org.apache.openejb.resource.jdbc.dbcp.BasicDataSource;
import org.apache.openejb.resource.jdbc.dbcp.BasicManagedDataSource;
import org.apache.openejb.resource.jdbc.dbcp.DbcpDataSource;
import org.apache.openejb.resource.jdbc.dbcp.DbcpDataSourceCreator;
import org.apache.openejb.resource.jdbc.dbcp.DbcpManagedDataSource;
import org.apache.openejb.resource.jdbc.dbcp.ManagedDataSourceWithRecovery;
import org.apache.xbean.recipe.ObjectRecipe;

import javax.sql.DataSource;
import java.util.Properties;

// TODO: remove it and replace it with org.apache.openejb.resource.jdbc.dbcp.DbcpDataSourceCreator
public class DefaultDataSourceCreator extends DbcpDataSourceCreator {
    @Override
    public DataSource managed(final String name, final DataSource ds) {
        return new DbcpManagedDataSource(name, ds);
    }

    @Override
    public DataSource poolManaged(final String name, final DataSource ds, Properties properties) {
        return new DbcpManagedDataSource(name, ds);
    }

    @Override
    public DataSource poolManaged(final String name, final String driver, final Properties properties) {
        final BasicManagedDataSource ds = new BasicManagedDataSource(name);
        ds.setDriverClassName(driver);
        return ds;
    }

    @Override
    public DataSource poolManagedWithRecovery(final String name, final XAResourceWrapper xaResourceWrapper, final String driver, final Properties properties) {
        final BasicManagedDataSource ds = new ManagedDataSourceWithRecovery(name, xaResourceWrapper);
        ds.setDriverClassName(driver);
        return ds;
    }

    @Override
    public DataSource pool(final String name, final DataSource ds, Properties properties) {
        return new DbcpDataSource(name, ds);
    }

    @Override
    public DataSource pool(final String name, final String driver, final Properties properties) {
        final BasicDataSource ds = new BasicDataSource(name);
        ds.setDriverClassName(driver);
        return ds;
    }

    @Override
    public void destroy(final Object object) throws Throwable {
        ((org.apache.commons.dbcp.BasicDataSource) object).close();
    }

    @Override
    protected void doDestroy(final DataSource dataSource) throws Throwable {
        ((org.apache.commons.dbcp.BasicDataSource) dataSource).close();
    }

    @Override
    public ObjectRecipe clearRecipe(final Object object) {
        return null; // no recipe here
    }
}
