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

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.resource.jdbc.BasicDataSourceUtil;
import org.apache.openejb.resource.jdbc.plugin.DataSourcePlugin;
import org.apache.openejb.resource.jdbc.pool.PoolDataSourceCreator;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Strings;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class BoneCPDataSourceCreator extends PoolDataSourceCreator {
    @Override
    protected void doDestroy(final DataSource dataSource) throws Throwable {
        ((BoneCPDataSource) dataSource).close();
    }

    @Override
    public DataSource pool(final String name, final DataSource ds, final Properties properties) {
        final BoneCPConfig config;
        final BoneCP pool;
        try {
            config = new BoneCPConfig(prefixedProps(properties));
            pool = new BoneCP(config);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return build(BoneCPDataSourceProvidedPool.class, new BoneCPDataSourceProvidedPool(pool), new Properties());
    }

    private Properties prefixedProps(final Properties properties) {
        if (properties.containsKey("url")) {
            properties.setProperty("", properties.getProperty("url"));
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

        // TODO: convert some more properties:
        // InitialSize, TestOnReturn, ConnectionProperties, MaxOpenPreparedStatements
        // AccessToUnderlyingConnectionAllowed, PoolPreparedStatements, MinIdle, TestWhileIdle
        // NumTestsPerEvictionRun, MaxIdle, MaxWait, MinEvictableIdleTimeMillis, TestOnBorrow, ValidationQuery

        final String cipher = properties.getProperty("PasswordCipher");
        if (cipher == null || "PlainText".equals(cipher)) { // no need to warn
            properties.remove("PasswordCipher");
        }
        if (properties.containsKey("TimeBetweenEvictionRuns")) {
            properties.setProperty("idleConnectionTestPeriodInSeconds", Long.toString(new Duration((String) properties.remove("TimeBetweenEvictionRuns")).getTime(TimeUnit.SECONDS)));
        }
        if (properties.containsKey("UserName")) {
            properties.put("username", properties.remove("UserName"));
        }
        if (properties.containsKey("MaxActive")) {
            properties.put("maxConnectionsPerPartition", properties.remove("MaxActive"));
        }

        // bonecp expects bonecp prefix in properties
        final Properties prefixedProps = new Properties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String suffix = Strings.lcfirst((String) entry.getKey());
            prefixedProps.put("bonecp." + suffix, entry.getValue());
        }

        return prefixedProps;
    }

    @Override
    public DataSource pool(final String name, final String driver, final Properties properties) {
        // bonecp already have a kind of ObjectRecipe so simply giving it the values
        final Properties props = new Properties();
        props.put("properties", prefixedProps(properties));

        final BoneCPDataSource ds = build(BoneCPDataSource.class, props);
        if (ds.getDriverClass() == null || ds.getDriverClass().isEmpty()) {
            ds.setDriverClass(driver);
        }
        if (ds.getPoolName() == null || ds.getPoolName().isEmpty()) {
            ds.setPoolName(name);
        }
        return ds;
    }

    private static final class BoneCPDataSourceProvidedPool extends BoneCPDataSource {
        private static final Field POOL_FIELD;
        static {
            try {
                POOL_FIELD = BoneCPDataSource.class.getDeclaredField("pool");
                POOL_FIELD.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new OpenEJBRuntimeException(e);
            }
        }

        public BoneCPDataSourceProvidedPool(final BoneCP pool) {
            try {
                POOL_FIELD.set(this, pool);
            } catch (IllegalAccessException e) {
                throw new OpenEJBRuntimeException(e);
            }
        }

        // @Override // java 7
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }
    }
}
