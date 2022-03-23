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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.bonecp;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.jdbc.BasicDataSourceUtil;
import org.apache.openejb.resource.jdbc.managed.xa.ManagedXADataSource;
import org.apache.openejb.resource.jdbc.plugin.DataSourcePlugin;
import org.apache.openejb.resource.jdbc.pool.PoolDataSourceCreator;
import org.apache.openejb.resource.jdbc.pool.XADataSourceResource;
import org.apache.openejb.util.Strings;
import org.apache.xbean.recipe.ObjectRecipe;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public class BoneCPDataSourceCreator extends PoolDataSourceCreator {
    @Override
    protected void doDestroy(final CommonDataSource dataSource) throws Throwable {
        ((BoneCPDataSource) dataSource).close();
    }

    @Override
    public DataSource pool(final String name, final DataSource ds, final Properties properties) {
        final BoneCPDataSource dataSourceProvidedPool = createPool(properties);
        dataSourceProvidedPool.setDatasourceBean(ds);
        if (dataSourceProvidedPool.getPoolName() == null) {
            dataSourceProvidedPool.setPoolName(name);
        }
        return dataSourceProvidedPool;
    }

    @Override
    public CommonDataSource pool(final String name, final String driver, final Properties properties) {
        final BoneCPDataSource pool = createPool(properties);
        if (pool.getDriverClass() == null) {
            pool.setDriverClass(driver);
        }
        if (pool.getPoolName() == null) {
            pool.setPoolName(name);
        }
        final String xa = String.class.cast(properties.remove("XaDataSource"));
        if (xa != null) {
            final XADataSource xaDs = XADataSourceResource.proxy(Thread.currentThread().getContextClassLoader(), xa);
            pool.setDatasourceBean(new ManagedXADataSource(xaDs, OpenEJB.getTransactionManager(), SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class)));
        }
        return pool;
    }



    private BoneCPDataSource createPool(final Properties properties) {
        final BoneCPConfig config;
        try {
            config = new BoneCPConfig(prefixedProps(properties));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        final BoneCPDataSource dataSourceProvidedPool = new BoneCPDataSource(config);
        recipes.put(dataSourceProvidedPool, new ObjectRecipe(BoneCPDataSource.class.getName())); // no error
        return dataSourceProvidedPool;
    }

    private Properties prefixedProps(final Properties properties) {
        if (properties.containsKey("url")) {
            properties.setProperty("url", properties.getProperty("url"));
        }

        // updating relative url if mandatory (hsqldb for instance)
        final String currentUrl = properties.getProperty("jdbcUrl");
        if (currentUrl != null) {
            try {
                final DataSourcePlugin helper = BasicDataSourceUtil.getDataSourcePlugin(currentUrl);
                if (helper != null) {
                    final String newUrl = helper.updatedUrl(currentUrl);
                    if (!currentUrl.equals(newUrl)) {
                        properties.setProperty("jdbcUrl", newUrl);
                    }
                }
            } catch (SQLException ignored) {
                // no-op
            }
        }

        final String cipher = properties.getProperty("PasswordCipher");
        if (cipher == null || "PlainText".equals(cipher)) { // no need to warn
            properties.remove("PasswordCipher");
        }

        // bonecp expects bonecp prefix in properties
        final Properties prefixedProps = new Properties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String suffix = Strings.lcfirst((String) entry.getKey());
            if (!suffix.startsWith("bonecp.")) {
                prefixedProps.put("bonecp." + suffix, entry.getValue());
            } else {
                prefixedProps.put(suffix, entry.getValue());
            }
        }

        return prefixedProps;
    }
}
