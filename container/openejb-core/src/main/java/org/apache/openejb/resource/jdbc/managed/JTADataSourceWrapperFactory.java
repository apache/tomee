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
package org.apache.openejb.resource.jdbc.managed;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.jdbc.DataSourceFactory;
import org.apache.openejb.resource.jdbc.pool.DataSourceCreator;
import org.apache.openejb.spi.ContainerSystem;

import javax.naming.NamingException;
import javax.sql.DataSource;

public class JTADataSourceWrapperFactory {
    private String serviceId;
    private String delegate = "datasource";
    private String dataSourceCreator = "dbcp-alternative";
    private boolean logSql = false;

    public DataSource create() {
        final DataSourceCreator creator = DataSourceFactory.creator(dataSourceCreator, logSql);
        DataSource ds = creator.managed(serviceId, findDelegate());
        DataSourceFactory.setCreatedWith(creator, ds);
        if (logSql) {
            ds = DataSourceFactory.makeItLogging(ds);
        }
        return ds;
    }

    private DataSource findDelegate() {
        try {
            return DataSource.class.cast(SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext()
                    .lookup(Assembler.OPENEJB_RESOURCE_JNDI_PREFIX + delegate));
        } catch (final NamingException e) {
            throw new IllegalArgumentException("'" + delegate + "' not found", e);
        }
    }

    public void setServiceId(final String serviceId) {
        this.serviceId = serviceId;
    }

    public void setDelegate(final String delegate) {
        this.delegate = delegate;
    }

    public void setDataSourceCreator(final String dataSourceCreator) {
        this.dataSourceCreator = dataSourceCreator;
    }

    public void setLogSql(final boolean logSql) {
        this.logSql = logSql;
    }
}
