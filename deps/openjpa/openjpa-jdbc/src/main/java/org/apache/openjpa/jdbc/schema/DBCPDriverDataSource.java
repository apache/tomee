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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.util.Closeable;

/**
 * Commons DBCP basic pooling driver data source.
 * The commons-dbcp packages must be on the class path for this plugin to work,
 * as it WILL NOT fall back to non-DBCP mode if they are missing. For automatic
 * usage of Commons DBCP when available, use AutoDriverDataSource instead.
 */
public class DBCPDriverDataSource
extends SimpleDriverDataSource implements Configurable, Closeable {

    private static String DBCPPACKAGENAME = "org.apache.commons.dbcp";
    private static String DBCPBASICDATASOURCENAME = "org.apache.commons.dbcp.BasicDataSource";
    private static Class<?> _dbcpClass;
    private static Boolean _dbcpAvail;
    private static RuntimeException _dbcpEx;

    protected JDBCConfiguration conf;
    private DataSource _ds;
    
    @Override
    public Connection getConnection(Properties props) throws SQLException {
        return getDBCPConnection(props);
    }

    public void close() throws SQLException {
        try {
            if (_ds != null) {
                if (isDBCPLoaded(getClassLoader())) {
                    ((org.apache.commons.dbcp.BasicDataSource)_dbcpClass.cast(_ds)).close();
                }
            }
        } catch (Exception e) {
            // no-op
        } catch (Throwable t) {
            // no-op
        } finally {
            _ds = null;
        }
    }
    
    protected Connection getDBCPConnection(Properties props) throws SQLException {
        Connection con = getDBCPDataSource(props).getConnection();
        if (con == null) {
            throw new SQLException(_eloc.get("dbcp-ds-null",
                DBCPBASICDATASOURCENAME, getConnectionDriverName(), getConnectionURL()).getMessage());
        }
        return con;
    }

    protected DataSource getDBCPDataSource(Properties props) {
        if (isDBCPLoaded(getClassLoader())) {
            if (_ds == null) {
                try {
                    Properties dbcpProps = updateDBCPProperties(props);
                    _ds = (DataSource) Configurations.newInstance(DBCPBASICDATASOURCENAME, conf,
                        dbcpProps, getClassLoader());
                } catch (Exception e) {
                    _dbcpEx = new RuntimeException(_eloc.get("driver-null", DBCPBASICDATASOURCENAME).getMessage(), e);
                }
                return _ds;
            } else {
                return _ds;
            }
        } else {
            // user choose DBCP, so fail if it isn't on the classpath
            if (_dbcpEx == null)
                _dbcpEx = new RuntimeException(_eloc.get("driver-null", DBCPBASICDATASOURCENAME).getMessage());
            throw _dbcpEx;
        }
    }
    
    /**
     * This method should not throw an exception, as it is called by
     * AutoDriverDataSource to determine if user already specified
     * to use Commons DBCP.
     * @return true if ConnectionDriverName contains org.apache.commons.dbcp,
     *         otherwise false
     */
    protected boolean isDBCPDataSource() {
        return (getConnectionDriverName() != null &&
            getConnectionDriverName().toLowerCase().indexOf(DBCPPACKAGENAME) >= 0);
    }
    
    /**
     * This method should not throw an exception, as it is called by
     * AutoDriverDataSource to determine if it should use DBCP or not
     * based on if org.apache.commons.dbcp.BasicDataSource can be loaded.
     * @return true if Commons DBCP was found on the classpath, otherwise false
     */
    static protected boolean isDBCPLoaded(ClassLoader cl) {
        if (Boolean.TRUE.equals(_dbcpAvail) && (_dbcpClass != null)) {
            return true;
        } else if (Boolean.FALSE.equals(_dbcpAvail)) {
            return false;
        } else {
            // first time checking, so try to load it
            try {
                _dbcpClass = Class.forName(DBCPBASICDATASOURCENAME, true, cl);
                _dbcpAvail = Boolean.TRUE;
                return true;
            } catch (Exception e) {
                _dbcpAvail = Boolean.FALSE;
                // save exception details for later instead of throwing here
                _dbcpEx = new RuntimeException(_eloc.get("driver-null", DBCPBASICDATASOURCENAME).getMessage(), e);
            }
            return _dbcpAvail.booleanValue();
        }
    }        

    /**
     * Normalize properties for Commons DBCP.  This should be done for every call from DataSourceFactory,
     * as we do not have a pre-configured Driver to reuse.
     * @param props
     * @return updated properties
     */
    private Properties updateDBCPProperties(Properties props) {
        Properties dbcpProps = mergeConnectionProperties(props);
        
        // only perform the following check for the first connection attempt (_driverClassName == null),
        // as multiple connections could be requested (like from SchemaTool) and isDBCPDriver() will be true
        if (isDBCPDataSource()) {
            String propDriver = hasProperty(dbcpProps, "driverClassName");
            if (propDriver == null || propDriver.trim().isEmpty()) {
                // if user specified DBCP for the connectionDriverName, then make sure they supplied a DriverClassName
                throw new RuntimeException(_eloc.get("connection-property-invalid", "DriverClassName",
                    propDriver).getMessage());
            }
            propDriver = hasProperty(dbcpProps, "url");
            if (propDriver == null || propDriver.trim().isEmpty()) {
                // if user specified DBCP for the connectionDriverName, then make sure they supplied a Url
                throw new RuntimeException(_eloc.get("connection-property-invalid", "URL",
                    propDriver).getMessage());
            }
        } else {
            // set Commons DBCP expected DriverClassName to the original connection driver name
            dbcpProps.setProperty(hasKey(dbcpProps, "driverClassName", "driverClassName"), getConnectionDriverName());
            // set Commons DBCP expected URL property
            dbcpProps.setProperty(hasKey(dbcpProps, "url", "url"), getConnectionURL());
        }
        
        // Commons DBCP requires non-Null username/password values in the connection properties
        if (hasKey(dbcpProps, "username") == null) {
            if (getConnectionUserName() != null)
                dbcpProps.setProperty("username", getConnectionUserName());
            else
                dbcpProps.setProperty("username", "");
        }
        // Commons DBCP requires non-Null username/password values in the connection properties
        if (hasKey(dbcpProps, "password") == null) {
            if (getConnectionPassword() != null)
                dbcpProps.setProperty("password", getConnectionPassword());
            else
                dbcpProps.setProperty("password", "");
        }

        // set some default properties for DBCP
        if (hasKey(dbcpProps, "maxIdle") == null) {
            dbcpProps.setProperty("maxIdle", "1");
        }
        if (hasKey(dbcpProps, "minIdle") == null) {
            dbcpProps.setProperty("minIdle", "0");
        }
        if (hasKey(dbcpProps, "maxActive") == null) {
            dbcpProps.setProperty("maxActive", "10");
        }
        
        return dbcpProps;
    }
    
    /**
     * Merge the passed in properties with a copy of the existing _connectionProperties
     * @param props
     * @return Merged properties
     */
    private Properties mergeConnectionProperties(final Properties props) {
        Properties mergedProps = new Properties();
        mergedProps.putAll(getConnectionProperties());

        // need to map "user" to "username" for Commons DBCP
        String uid = removeProperty(mergedProps, "user");
        if (uid != null) {
            mergedProps.setProperty("username", uid);
        }
        
        // now, merge in any passed in properties
        if (props != null && !props.isEmpty()) {
            for (Iterator<Object> itr = props.keySet().iterator(); itr.hasNext();) {
                String key = (String)itr.next();
                String value = props.getProperty(key);
                // need to map "user" to "username" for Commons DBCP
                if ("user".equalsIgnoreCase(key)) {
                    key = "username";
                }
                // case-insensitive search for existing key
                String existingKey = hasKey(mergedProps, key);
                if (existingKey != null) {
                    // update existing entry
                    mergedProps.setProperty(existingKey, value);                        
                } else {
                    // add property to the merged set
                    mergedProps.setProperty(key, value);
                }
            }
        }
        return mergedProps;
    }
    
    /**
     * Case-insensitive search of the given properties for the given key.
     * @param props
     * @param key
     * @return Key name as found in properties or null if it was not found.
     */
    private String hasKey(Properties props, String key) {
        return hasKey(props, key, null);
    }
    
    /**
     * Case-insensitive search of the given properties for the given key.
     * @param props
     * @param key
     * @param defaultKey
     * @return Key name as found in properties or the given defaultKey if it was not found.
     */
    private String hasKey(Properties props, String key, String defaultKey)
    {
        if (props != null && key != null) {
            for (Iterator<Object> itr = props.keySet().iterator(); itr.hasNext();) {
                String entry = (String)itr.next();
                if (key.equalsIgnoreCase(entry))
                    return entry;
            }
        }
        return defaultKey;
    }
    
    private String hasProperty(Properties props, String key) {
        if (props != null && key != null) {
            String entry = hasKey(props, key);
            if (entry != null)
                return props.getProperty(entry);
        }
        return null;

    }
    
    private String removeProperty(Properties props, String key) {
        if (props != null && key != null) {
            String entry = hasKey(props, key);
            if (entry != null)
                return (String)props.remove(entry);
        }
        return null;
    }
    
    // Configurable interface methods
    public void setConfiguration(Configuration conf) {
        if (conf instanceof JDBCConfiguration)
            this.conf = (JDBCConfiguration)conf;
    }

    public void startConfiguration() {
        // no-op
    }

    public void endConfiguration() {
        // no-op
    }

}

