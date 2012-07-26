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

import org.apache.openejb.resource.jdbc.dbcp.BasicDataSource;
import org.apache.openejb.resource.jdbc.dbcp.DbcpDataSource;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.sql.DataSource;
import java.util.Properties;

public class DbcpDataSourceCreator extends PoolDataSourceCreator {
    @Override
    public DataSource pool(final String name, final DataSource ds) {
        return new DbcpDataSource(name, ds);
    }

    @Override
    public DataSource pool(final String name, final String driver, final Properties properties) {
        final ObjectRecipe serviceRecipe = new ObjectRecipe(BasicDataSource.class.getName());
        serviceRecipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
        serviceRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        serviceRecipe.setProperty("name", name);
        if (!properties.containsKey("JdbcDriver")) {
            properties.setProperty("driverClassName", driver);
        }
        serviceRecipe.setAllProperties(properties);

        final BasicDataSource ds = (BasicDataSource) serviceRecipe.create(); // new BasicDataSource(name);
        ds.setDriverClassName(driver);
        return ds;
    }

    @Override
    public boolean hasCreated(final Object object) {
        return object instanceof org.apache.commons.dbcp.BasicDataSource;
    }

    @Override
    public void destroy(final Object object) throws Throwable {
        ((org.apache.commons.dbcp.BasicDataSource) object).close();
    }
}
