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

import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.config.typed.util.Builders;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@XmlRootElement(name = "DataSource")
public class DataSourceBuilder extends Resource {

    private String serviceId = null;
    private String definition = null;
    private boolean jtaManaged = true;
    private String jdbcDriver = "org.hsqldb.jdbcDriver";
    private java.net.URI jdbcUrl = java.net.URI.create("jdbc:hsqldb:mem:hsqldb");
    private String userName = "sa";
    private String password = null;
    private String passwordCipher = "PlainText";
    private String connectionProperties = null;
    private boolean defaultAutoCommit = true;
    private String defaultReadOnly = null;
    private int initialSize = 0;
    private int maxActive = 20;
    private int maxIdle = 20;
    private int minIdle = 0;
    private int maxWait = -1;
    private String validationQuery = null;
    private boolean testOnBorrow = true;
    private boolean testOnReturn = false;
    private boolean testWhileIdle = false;
    private long timeBetweenEvictionRunsMillis = -1;
    private int numTestsPerEvictionRun = 3;
    private long minEvictableIdleTimeMillis = 1800000;
    private boolean poolPreparedStatements = false;
    private int maxOpenPreparedStatements = 0;
    private boolean accessToUnderlyingConnectionAllowed = false;
    private boolean ignoreDefaultValues = false;

    public DataSourceBuilder() {
        setClassName("org.apache.openejb.resource.jdbc.DataSourceFactory");
        setType("javax.sql.DataSource");
        setId("DataSource");

        setConstructor("serviceId, jtaManaged, jdbcDriver, definition");

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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
    public int getMinIdle() {
        return minIdle;
    }

    public DataSourceBuilder withMaxWait(int maxWait) {
        this.maxWait = maxWait;
        return this;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    @XmlAttribute
    public int getMaxWait() {
        return maxWait;
    }

    public DataSourceBuilder withValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
        return this;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
    public boolean getTestWhileIdle() {
        return testWhileIdle;
    }

    public DataSourceBuilder withTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        return this;
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    @XmlAttribute
    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public DataSourceBuilder withTimeBetweenEvictionRuns(long time, TimeUnit unit) {
        return withTimeBetweenEvictionRunsMillis(TimeUnit.MILLISECONDS.convert(time, unit));
    }

    public void setTimeBetweenEvictionRuns(long time, TimeUnit unit) {
        setTimeBetweenEvictionRunsMillis(TimeUnit.MILLISECONDS.convert(time, unit));
    }

    public DataSourceBuilder withNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
        return this;
    }

    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    @XmlAttribute
    public int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    public DataSourceBuilder withMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        return this;
    }

    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    @XmlAttribute
    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public DataSourceBuilder withMinEvictableIdleTime(long time, TimeUnit unit) {
        return withMinEvictableIdleTimeMillis(TimeUnit.MILLISECONDS.convert(time, unit));
    }

    public void setMinEvictableIdleTime(long time, TimeUnit unit) {
        setMinEvictableIdleTimeMillis(TimeUnit.MILLISECONDS.convert(time, unit));
    }

    public DataSourceBuilder withPoolPreparedStatements(boolean poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
        return this;
    }

    public void setPoolPreparedStatements(boolean poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
    }

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
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

    @XmlAttribute
    public boolean getIgnoreDefaultValues() {
        return ignoreDefaultValues;
    }

    public Properties getProperties() {
        return Builders.getProperties(this);
    }

}
