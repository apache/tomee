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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.resource.jdbc;

import javax.resource.spi.ConnectionRequestInfo;

public class JdbcConnectionRequestInfo implements ConnectionRequestInfo {
    private String userName;
    private String password;
    private String jdbcDriver;
    private String jdbcUrl;

    public JdbcConnectionRequestInfo(String userName, String password, String jdbcDriver, String jdbcUrl) {
        this.userName = userName;
        this.password = password;
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
    }

    protected String getUserName() {
        return userName;
    }

    protected String getPassword() {
        return password;
    }

    protected String getJdbcDriver() {
        return jdbcDriver;
    }

    protected String getJdbcUrl() {
        return jdbcUrl;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JdbcConnectionRequestInfo that = (JdbcConnectionRequestInfo) o;

        return jdbcDriver.equals(that.jdbcDriver) &&
                jdbcUrl.equals(that.jdbcUrl) &&
                password.equals(that.password) &&
                userName.equals(that.userName);
    }

    public int hashCode() {
        int result;
        result = userName.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + jdbcDriver.hashCode();
        result = 31 * result + jdbcUrl.hashCode();
        return result;
    }
}