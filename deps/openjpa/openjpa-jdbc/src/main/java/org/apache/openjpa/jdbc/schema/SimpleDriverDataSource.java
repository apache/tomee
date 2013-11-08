/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.jdbc.schema;

import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.lib.jdbc.ConnectionDecorator;
import org.apache.openjpa.lib.jdbc.DelegatingDataSource;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.StoreException;

/**
 * Non-pooling driver data source.
 */
public class SimpleDriverDataSource
    implements DriverDataSource {

    private String _connectionDriverName;
    private String _connectionURL;
    private String _connectionUserName;
    private String _connectionPassword;
    private Properties _connectionProperties;
    private Properties _connectionFactoryProperties;
    private Driver _driver;
    private ClassLoader _classLoader;
    
    protected static Localizer _loc = Localizer.forPackage(SimpleDriverDataSource.class);
    protected static Localizer _eloc = Localizer.forPackage(DelegatingDataSource.class);

    public Connection getConnection()
        throws SQLException {
        return getConnection(null);
    }

    public Connection getConnection(String username, String password)
        throws SQLException {
        Properties props = new Properties();
        if (username == null)
            username = _connectionUserName;
        if (username != null)
            props.put("user", username);

        if (password == null)
            password = _connectionPassword;
        if (password != null)
            props.put("password", password);

        return getConnection(props);
    }

    public Connection getConnection(Properties props) throws SQLException {
        return getSimpleConnection(props);
    }
    
    protected Connection getSimpleConnection(Properties props) throws SQLException {
    	Connection con = getSimpleDriver().connect(_connectionURL, props == null? new Properties() : props);
    	if (con == null) {
            throw new SQLException(_eloc.get("poolds-null",
                    _connectionDriverName, _connectionURL).getMessage());
        }
        return con;
    }

    public int getLoginTimeout() {
        return 0;
    }

    public void setLoginTimeout(int seconds) {
    }

    public PrintWriter getLogWriter() {
        return DriverManager.getLogWriter();
    }

    public void setLogWriter(PrintWriter out) {
    }

    public void initDBDictionary(DBDictionary dict) {
    }

    public void setConnectionURL(String connectionURL) {
        _connectionURL = connectionURL;
    }

    public String getConnectionURL() {
        return _connectionURL;
    }

    public void setConnectionUserName(String connectionUserName) {
        _connectionUserName = connectionUserName;
    }

    public String getConnectionUserName() {
        return _connectionUserName;
    }

    public void setConnectionPassword(String connectionPassword) {
        _connectionPassword = connectionPassword;
    }

    // Only allow sub-classes to retrieve the password
    protected String getConnectionPassword() {
        return _connectionPassword;
    }

    public void setConnectionProperties(Properties props) {
        _connectionProperties = props;
    }

    public Properties getConnectionProperties() {
        return _connectionProperties;
    }

    public void setConnectionFactoryProperties(Properties props) {
        _connectionFactoryProperties = props;
    }

    public Properties getConnectionFactoryProperties() {
        return _connectionFactoryProperties;
    }

    public List<ConnectionDecorator> createConnectionDecorators() {
        return null;
    }

    public void setClassLoader(ClassLoader classLoader) {
        _classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return _classLoader;
    }

    public void setConnectionDriverName(String connectionDriverName) {
        _connectionDriverName = connectionDriverName;
    }

    public String getConnectionDriverName() {
        return _connectionDriverName;
    }

    protected Driver getSimpleDriver() {
        if (_driver != null)
            return _driver;

        try { 
            _driver = DriverManager.getDriver(_connectionURL);
            if (_driver != null)
                return _driver;
        } catch (Exception e) {
        }

        try {
            Class.forName(_connectionDriverName, true, _classLoader);
        } catch (Exception e) {
        }
        try {
            _driver = DriverManager.getDriver(_connectionURL);
            if (_driver != null)
                return _driver;
        } catch (Exception e) {
        }

        try {
            Class<?> c = Class.forName(_connectionDriverName,
                true, _classLoader);
            _driver = (Driver) AccessController.doPrivileged(
                J2DoPrivHelper.newInstanceAction(c));
            return _driver;
        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw(RuntimeException) e;
            if (e instanceof PrivilegedActionException)
                e = ((PrivilegedActionException) e).getException();
            throw new StoreException(e);
        }
    }
    

    // java.sql.Wrapper implementation (JDBC 4)
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isAssignableFrom(SimpleDriverDataSource.class);
    }

    @SuppressWarnings("unchecked")
    public Object unwrap(Class iface) {
        if (isWrapperFor(iface))
            return this;
        else
            return null;
    }

    // Java 7 methods follow

    public Logger getParentLogger() throws SQLFeatureNotSupportedException{
    	throw new SQLFeatureNotSupportedException();
    }
}

