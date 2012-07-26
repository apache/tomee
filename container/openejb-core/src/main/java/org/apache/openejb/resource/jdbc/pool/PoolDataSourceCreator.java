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
import org.apache.openejb.resource.jdbc.managed.ManagedDataSource;
import org.apache.openejb.resource.jdbc.managed.ManagedXADataSource;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.util.Properties;

public abstract class PoolDataSourceCreator implements DataSourceCreator {
    @Override
    public DataSource managed(final String name, final DataSource ds) {
        if (ds instanceof XADataSource) {
            return new ManagedXADataSource(ds);
        }
        return new ManagedDataSource(ds);
    }

    @Override // TODO: manage recovery
    public DataSource poolManagedWithRecovery(final String name, final XAResourceWrapper xaResourceWrapper, final String driver, final Properties properties) {
        throw new UnsupportedOperationException("TODO: implement it");
    }

    @Override
    public DataSource poolManaged(final String name, final DataSource ds) {
        return managed(name, pool(name, ds));
    }

    @Override
    public DataSource poolManaged(final String name, final String driver, final Properties properties) {
        return managed(name, pool(name, driver, properties));
    }
}
