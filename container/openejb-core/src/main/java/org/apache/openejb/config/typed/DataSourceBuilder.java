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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config.typed;

import org.apache.openejb.config.typed.util.*;
import org.apache.openejb.config.sys.*;
import javax.xml.bind.annotation.*;
import org.apache.openejb.util.Duration;
import java.util.*;
import java.util.concurrent.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "DataSource")
public class DataSourceBuilder extends Resource {

    @XmlAttribute
    private String serviceId = null;
    @XmlAttribute
    private String definition = null;
    @XmlAttribute
    private boolean jtaManaged = true;
    @XmlAttribute
    private String jdbcDriver = "org.hsqldb.jdbcDriver";
    @XmlAttribute
    private java.net.URI jdbcUrl = java.net.URI.create("jdbc:hsqldb:mem:hsqldb");
    @XmlAttribute
    private String userName = "sa";
    @XmlAttribute
    private String password = null;
    @XmlAttribute
    private String passwordCipher = "PlainText";
    @XmlAttribute
    private String connectionProperties = null;
    @XmlAttribute
    private boolean defaultAutoCommit = true;
    @XmlAttribute
    private String defaultReadOnly = null;
    @XmlAttribute
    private int initialSize = 0;
    @XmlAttribute
    private int maxActive = 20;
    @XmlAttribute
    private int maxIdle = 20;
    @XmlAttribute
    private int minIdle = 0;
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private org.apache.openejb.util.Duration maxWaitTime = org.apache.openejb.util.Duration.parse("-1 millisecond");
    @XmlAttribute
    private String validationQuery = null;
    @XmlAttribute
    private boolean testOnBorrow = true;
    @XmlAttribute
    private boolean testOnReturn = false;
    @XmlAttribute
    private boolean testWhileIdle = false;
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private org.apache.openejb.util.Duration timeBetweenEvictionRuns = org.apache.openejb.util.Duration.parse("-1 millisecond");
    @XmlAttribute
    private int numTestsPerEvictionRun = 3;
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @XmlAttribute
    private org.apache.openejb.util.Duration minEvictableIdleTime = org.apache.openejb.util.Duration.parse("30 minutes");
    @XmlAttribute
    private boolean poolPreparedStatements = false;
    @XmlAttribute
    private int maxOpenPreparedStatements = 0;
    @XmlAttribute
    private boolean accessToUnderlyingConnectionAllowed = false;
    @XmlAttribute
    private boolean ignoreDefaultValues = false;

    public DataSourceBuilder() {
        setClassName("org.apache.openejb.resource.jdbc.DataSourceFactory");
        setType("javax.sql.DataSource");
        setId("DataSource");

        setConstructor("serviceId, jtaManaged, jdbcDriver, definition, maxWaitTime, timeBetweenEvictionRuns, minEvictableIdleTime");

        setFactoryName("create");

    }

    public DataSourceBuilder id(String id) {
        setId(id);
        return this;
    }

    public DataSourceBuilder withServiceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public DataSourceBuilder withDefinition(String definition) {
        this.definition = definition;
        return this;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDefinition() {
        return definition;
    }

    public DataSourceBuilder withJtaManaged(boolean jtaManaged) {
        this.jtaManaged = jtaManaged;
        return this;
    }

    public void setJtaManaged(boolean jtaManaged) {
        this.jtaManaged = jtaManaged;
    }

    public boolean getJtaManaged() {
        return jtaManaged;
    }

    public DataSourceBuilder withJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
        return this;
    }

    public void setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public DataSourceBuilder withJdbcUrl(java.net.URI jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }

    public void setJdbcUrl(java.net.URI jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public java.net.URI getJdbcUrl() {
        return jdbcUrl;
    }

    public DataSourceBuilder withUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public DataSourceBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public DataSourceBuilder withPasswordCipher(String passwordCipher) {
        this.passwordCipher = passwordCipher;
        return this;
    }

    public void setPasswordCipher(String passwordCipher) {
        this.passwordCipher = passwordCipher;
    }

    public String getPasswordCipher() {
        return passwordCipher;
    }

    public DataSourceBuilder withConnectionProperties(String connectionProperties) {
        this.connectionProperties = connectionProperties;
        return this;
    }

    public void setConnectionProperties(String connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public String getConnectionProperties() {
        return connectionProperties;
    }

    public DataSourceBuilder withDefaultAutoCommit(boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
        return this;
    }

    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
    }

    public boolean getDefaultAutoCommit() {
        return defaultAutoCommit;
    }

    public DataSourceBuilder withDefaultReadOnly(String defaultReadOnly) {
        this.defaultReadOnly = defaultReadOnly;
        return this;
    }

    public void setDefaultReadOnly(String defaultReadOnly) {
        this.defaultReadOnly = defaultReadOnly;
    }

    public String getDefaultReadOnly() {
        return defaultReadOnly;
    }

    public DataSourceBuilder withInitialSize(int initialSize) {
        this.initialSize = initialSize;
        return this;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public DataSourceBuilder withMaxActive(int maxActive) {
        this.maxActive = maxActive;
        return this;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public DataSourceBuilder withMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
        return this;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public DataSourceBuilder withMinIdle(int minIdle) {
        this.minIdle = minIdle;
        return this;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public DataSourceBuilder withMaxWaitTime(org.apache.openejb.util.Duration maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
        return this;
    }

    public void setMaxWaitTime(org.apache.openejb.util.Duration maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public org.apache.openejb.util.Duration getMaxWaitTime() {
        return maxWaitTime;
    }

    public DataSourceBuilder withMaxWaitTime(long time, TimeUnit unit) {
        return withMaxWaitTime(new Duration(time, unit));
    }

    public void setMaxWaitTime(long time, TimeUnit unit) {
        setMaxWaitTime(new Duration(time, unit));
    }

    public DataSourceBuilder withValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
        return this;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public DataSourceBuilder withTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
        return this;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean getTestOnBorrow() {
        return testOnBorrow;
    }

    public DataSourceBuilder withTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
        return this;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public boolean getTestOnReturn() {
        return testOnReturn;
    }

    public DataSourceBuilder withTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
        return this;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public boolean getTestWhileIdle() {
        return testWhileIdle;
    }

    public DataSourceBuilder withTimeBetweenEvictionRuns(org.apache.openejb.util.Duration timeBetweenEvictionRuns) {
        this.timeBetweenEvictionRuns = timeBetweenEvictionRuns;
        return this;
    }

    public void setTimeBetweenEvictionRuns(org.apache.openejb.util.Duration timeBetweenEvictionRuns) {
        this.timeBetweenEvictionRuns = timeBetweenEvictionRuns;
    }

    public org.apache.openejb.util.Duration getTimeBetweenEvictionRuns() {
        return timeBetweenEvictionRuns;
    }

    public DataSourceBuilder withTimeBetweenEvictionRuns(long time, TimeUnit unit) {
        return withTimeBetweenEvictionRuns(new Duration(time, unit));
    }

    public void setTimeBetweenEvictionRuns(long time, TimeUnit unit) {
        setTimeBetweenEvictionRuns(new Duration(time, unit));
    }

    public DataSourceBuilder withNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
        return this;
    }

    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    public int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    public DataSourceBuilder withMinEvictableIdleTime(org.apache.openejb.util.Duration minEvictableIdleTime) {
        this.minEvictableIdleTime = minEvictableIdleTime;
        return this;
    }

    public void setMinEvictableIdleTime(org.apache.openejb.util.Duration minEvictableIdleTime) {
        this.minEvictableIdleTime = minEvictableIdleTime;
    }

    public org.apache.openejb.util.Duration getMinEvictableIdleTime() {
        return minEvictableIdleTime;
    }

    public DataSourceBuilder withMinEvictableIdleTime(long time, TimeUnit unit) {
        return withMinEvictableIdleTime(new Duration(time, unit));
    }

    public void setMinEvictableIdleTime(long time, TimeUnit unit) {
        setMinEvictableIdleTime(new Duration(time, unit));
    }

    public DataSourceBuilder withPoolPreparedStatements(boolean poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
        return this;
    }

    public void setPoolPreparedStatements(boolean poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
    }

    public boolean getPoolPreparedStatements() {
        return poolPreparedStatements;
    }

    public DataSourceBuilder withMaxOpenPreparedStatements(int maxOpenPreparedStatements) {
        this.maxOpenPreparedStatements = maxOpenPreparedStatements;
        return this;
    }

    public void setMaxOpenPreparedStatements(int maxOpenPreparedStatements) {
        this.maxOpenPreparedStatements = maxOpenPreparedStatements;
    }

    public int getMaxOpenPreparedStatements() {
        return maxOpenPreparedStatements;
    }

    public DataSourceBuilder withAccessToUnderlyingConnectionAllowed(boolean accessToUnderlyingConnectionAllowed) {
        this.accessToUnderlyingConnectionAllowed = accessToUnderlyingConnectionAllowed;
        return this;
    }

    public void setAccessToUnderlyingConnectionAllowed(boolean accessToUnderlyingConnectionAllowed) {
        this.accessToUnderlyingConnectionAllowed = accessToUnderlyingConnectionAllowed;
    }

    public boolean getAccessToUnderlyingConnectionAllowed() {
        return accessToUnderlyingConnectionAllowed;
    }

    public DataSourceBuilder withIgnoreDefaultValues(boolean ignoreDefaultValues) {
        this.ignoreDefaultValues = ignoreDefaultValues;
        return this;
    }

    public void setIgnoreDefaultValues(boolean ignoreDefaultValues) {
        this.ignoreDefaultValues = ignoreDefaultValues;
    }

    public boolean getIgnoreDefaultValues() {
        return ignoreDefaultValues;
    }

    public Properties getProperties() {
        return Builders.getProperties(this);
    }

}
