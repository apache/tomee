/**
 *
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
package org.apache.openejb.resource.jdbc;

import org.apache.openejb.loader.SystemInstance;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

public class BasicManagedDataSource extends org.apache.commons.dbcp.managed.BasicManagedDataSource {
    public synchronized String getUserName() {
        return super.getUsername();
    }

    public synchronized void setUserName(String string) {
        super.setUsername(string);
    }

    public synchronized String getJdbcDriver() {
        return super.getDriverClassName();
    }

    public synchronized void setJdbcDriver(String string) {
        super.setDriverClassName(string);
    }

    public synchronized String getJdbcUrl() {
        return super.getUrl();
    }

    public synchronized void setJdbcUrl(String string) {
        super.setUrl(string);
    }

    public synchronized void setDefaultTransactionIsolation(String s) {
        if (s == null || s.equals("")) return;
        int level = IsolationLevels.getIsolationLevel(s);
        super.setDefaultTransactionIsolation(level);
    }
    
    protected synchronized DataSource createDataSource() throws SQLException {
        if (dataSource != null) {
            return dataSource;
        }

        // get the plugin
        DataSourcePlugin helper = BasicDataSourceUtil.getDataSourcePlugin(getUrl());

        // configure this
        if (helper != null) {
            helper.configure(this);
        }

        // creat the data source
        if (helper == null || !helper.enableUserDirHack()) {
            return super.createDataSource();
        } else {
            // wrap super call with code that sets user.dir to openejb.base and then resets it
            Properties systemProperties = System.getProperties();
            synchronized (systemProperties) {
                String userDir = systemProperties.getProperty("user.dir");
                try {
                    File base = SystemInstance.get().getBase().getDirectory();
                    systemProperties.setProperty("user.dir", base.getAbsolutePath());
                    return super.createDataSource();
                } finally {
                    systemProperties.setProperty("user.dir", userDir);
                }
            }
        }
    }
}
