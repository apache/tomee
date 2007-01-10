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

import org.apache.openejb.core.EnvProps;
import org.apache.openejb.util.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.Set;

/**
 * @org.apache.xbean.XBean element="jdbcConnectionFactory"
 */
public class JdbcManagedConnectionFactory implements javax.resource.spi.ManagedConnectionFactory, java.io.Serializable {
    private static final long serialVersionUID = 8797357228901190014L;
    protected Logger logger = Logger.getInstance("OpenEJB.connector", "org.apache.openejb.alt.util.resources");
    private ManagedConnectionFactory factory;
    private String defaultUserName;
    private String defaultPassword;
    private String url;
    private String driver;
    private boolean unmanaged;


    public JdbcManagedConnectionFactory(String driver, String url, String defaultUserName, String defaultPassword, boolean unmanaged) throws ResourceAdapterInternalException {
        this.defaultPassword = defaultPassword;
        this.defaultUserName = defaultUserName;
        this.driver = driver;
        this.unmanaged = unmanaged;
        this.url = url;
        start();
    }

    public void init(java.util.Properties props) throws javax.resource.spi.ResourceAdapterInternalException {
        defaultUserName = props.getProperty(EnvProps.USER_NAME);
        defaultPassword = props.getProperty(EnvProps.PASSWORD);
        url = props.getProperty(EnvProps.JDBC_URL);
        driver = props.getProperty(EnvProps.JDBC_DRIVER);
        unmanaged = props.getProperty("Unmanaged", "false").equalsIgnoreCase("true");
        start();
    }


    public String getDefaultUserName() {
        return defaultUserName;
    }

    public void setDefaultUserName(String defaultUserName) {
        this.defaultUserName = defaultUserName;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public boolean isUnmanaged() {
        return unmanaged;
    }

    public void setUnmanaged(boolean unmanaged) {
        this.unmanaged = unmanaged;
    }

    /**
     * @org.apache.xbean.InitMethod
     */
    public void start() throws ResourceAdapterInternalException {
        loadDriver(driver);

        if (driver.equals("org.hsqldb.jdbcDriver")) {
            url = HsqldbPathHack.toAbsolutePath(url);
        }

        factory = new BasicManagedConnectionFactory(this, driver, url, defaultUserName, defaultPassword);

        if (driver.equals("org.enhydra.instantdb.jdbc.idbDriver")) {
            factory = new InstantdbPropertiesHack(factory, url);
            factory = new ManagedConnectionFactoryPathHack(factory);
        } else if (driver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
            factory = new DerbySystemHomeHack(factory);
            factory = new ManagedConnectionFactoryPathHack(factory);
        }

        JdbcConnectionRequestInfo info = new JdbcConnectionRequestInfo(defaultUserName, defaultPassword, driver, url);
        ManagedConnection connection = null;
        try {
            connection = factory.createManagedConnection(null, info);
        } catch (Throwable e) {
            logger.error("Testing driver failed.  " + "[" + url + "]  "
                    + "Could not obtain a physical JDBC connection from the DriverManager."
                    + "\nThe error message was:\n" + e.getMessage() + "\nPossible cause:"
                    + "\n\to JDBC driver classes are not available to OpenEJB"
                    + "\n\to Relative paths are not resolved properly");
        } finally {
            if (connection != null) {
                try {
                    connection.destroy();
                } catch (ResourceException dontCare) {
                }
            }
        }
    }

    private void loadDriver(String driver) throws ResourceAdapterInternalException {
        try {
            ClassLoader classLoader = (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
                public Object run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            });
            Class.forName(driver, true, classLoader);
        } catch (ClassNotFoundException cnf) {
            throw new ResourceAdapterInternalException("JDBC Driver class \"" + driver + "\" not found by class loader", ErrorCode.JDBC_0002);
        }
    }

    public Object createConnectionFactory(ConnectionManager connectionManager) throws ResourceException {
        return factory.createConnectionFactory(connectionManager);
    }

    public Object createConnectionFactory() throws ResourceException {
        return factory.createConnectionFactory();
    }

    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        return factory.createManagedConnection(subject, connectionRequestInfo);
    }

    public ManagedConnection matchManagedConnections(Set set, Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        return factory.matchManagedConnections(set, subject, connectionRequestInfo);
    }

    public void setLogWriter(PrintWriter printWriter) throws ResourceException {
        factory.setLogWriter(printWriter);
    }

    public PrintWriter getLogWriter() throws ResourceException {
        return factory.getLogWriter();
    }

    public int hashCode() {
        return factory.hashCode();
    }

    public boolean equals(Object o) {
        return factory.equals(o);
    }
}