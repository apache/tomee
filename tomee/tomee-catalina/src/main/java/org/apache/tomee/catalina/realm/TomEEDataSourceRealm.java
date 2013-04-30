/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.catalina.realm;

import org.apache.catalina.realm.DataSourceRealm;
import org.apache.naming.ContextBindings;
import org.apache.openejb.config.AutoConfig;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.loader.SystemInstance;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Properties;

public class TomEEDataSourceRealm extends DataSourceRealm {
    private String fullName = null;

    @Override
    protected Connection open() {
        try { // parent behavior
            final Context context;
            if (fullName != null) {
                context = ContextBindings.getClassLoader();
            } else if (localDataSource) {
                context = Context.class.cast(ContextBindings.getClassLoader().lookup("comp/env"));
            } else {
                context = getServer().getGlobalNamingContext();
            }
            return DataSource.class.cast(context.lookup(dataSourceName)).getConnection();
        } catch (final SQLException e) { // user set full name
            try { // try globally
                return getConnection(dataSourceName);
            } catch (final SQLException e2) {
                try { // try globally but with custom subspace
                    return getConnection("java:openejb/Resource/" + dataSourceName);
                } catch (final Exception e3) {
                    final Collection<String> ids = SystemInstance.get().getComponent(ConfigurationFactory.class).getResourceIds(DataSource.class.getName(), new Properties());
                    final String id = AutoConfig.findResourceId(ids, dataSourceName);

                    if (id != null) {
                        try { // try globally but with custom subspace
                            return getConnection("java:openejb/Resource/" + id);
                        } catch (final Exception e4) {
                            containerLog.error(sm.getString("dataSourceRealm.exception"), e);
                        }
                    } else {
                        containerLog.error(sm.getString("dataSourceRealm.exception"), e);
                    }
                }
            } catch (final Exception e2) {
                containerLog.error(sm.getString("dataSourceRealm.exception"), e);
            }
        } catch (final Exception e) {
            containerLog.error(sm.getString("dataSourceRealm.exception"), e);
        }
        return null;
    }

    private Connection getConnection(final String name) throws SQLException, NamingException {
        final Connection c = DataSource.class.cast(Context.class.cast(ContextBindings.getClassLoader()).lookup(name)).getConnection();
        fullName = name; // if here, the default name needed to be changed to match requested resource so update it for next calls
        return c;
    }
}
