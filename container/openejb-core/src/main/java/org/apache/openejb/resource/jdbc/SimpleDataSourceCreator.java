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

import org.apache.openejb.OpenEJB;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.XAResourceWrapper;
import org.apache.openejb.resource.jdbc.managed.local.ManagedDataSource;
import org.apache.openejb.resource.jdbc.managed.xa.ManagedXADataSource;
import org.apache.openejb.resource.jdbc.pool.DataSourceCreator;
import org.apache.xbean.recipe.ObjectRecipe;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.util.Properties;

public class SimpleDataSourceCreator implements DataSourceCreator {
    @Override
    public DataSource managed(final String name, final CommonDataSource ds) {
        final TransactionManager transactionManager = OpenEJB.getTransactionManager();
        if (XADataSource.class.isInstance(ds)) {
            return new ManagedXADataSource(XADataSource.class.cast(ds), transactionManager, SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class));
        }
        return new ManagedDataSource(DataSource.class.cast(ds), transactionManager, SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class));
    }

    @Override
    public DataSource poolManaged(final String name, final DataSource ds, final Properties properties) {
        throw new UnsupportedOperationException("pooling not supported");
    }

    @Override
    public DataSource pool(final String name, final DataSource ds, final Properties properties) {
        throw new UnsupportedOperationException("pooling not supported");
    }

    @Override
    public DataSource poolManagedWithRecovery(final String name, final XAResourceWrapper xaResourceWrapper,
                                              final String driver, final Properties properties) {
        throw new UnsupportedOperationException("pooling not supported");
    }

    @Override
    public DataSource poolManaged(final String name, final String driver, final Properties properties) {
        throw new UnsupportedOperationException("pooling not supported");
    }

    @Override
    public CommonDataSource pool(final String name, final String driver, final Properties properties) {
        throw new UnsupportedOperationException("pooling not supported");
    }

    @Override
    public void destroy(final Object object) throws Throwable {
        // no-op
    }

    @Override
    public ObjectRecipe clearRecipe(final Object object) {
        return null;
    }
}
