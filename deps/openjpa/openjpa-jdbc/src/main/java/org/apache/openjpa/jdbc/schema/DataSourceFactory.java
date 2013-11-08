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

import java.security.AccessController;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.jdbc.ConfiguringConnectionDecorator;
import org.apache.openjpa.lib.jdbc.ConnectionDecorator;
import org.apache.openjpa.lib.jdbc.DecoratingDataSource;
import org.apache.openjpa.lib.jdbc.DelegatingDataSource;
import org.apache.openjpa.lib.jdbc.JDBCEventConnectionDecorator;
import org.apache.openjpa.lib.jdbc.JDBCListener;
import org.apache.openjpa.lib.jdbc.LoggingConnectionDecorator;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.UserException;

/**
 * Factory for {@link DataSource} objects. The factory uses the supplied
 * configuration to obtain a 3rd-party datasource or to create one, and
 * to setup prepared statement caching.
 *
 * @author Abe White
 * @nojavadoc
 */
public class DataSourceFactory {

    private static final Localizer _loc = Localizer.forPackage(DataSourceFactory.class);
    protected static Localizer _eloc = Localizer.forPackage(DelegatingDataSource.class);

    /**
     * Create a datasource using the given configuration.
     */
    public static DataSource newDataSource(JDBCConfiguration conf,
        boolean factory2) {
        String driver = (factory2) ? conf.getConnection2DriverName()
            : conf.getConnectionDriverName();
        if (StringUtils.isEmpty(driver))
            throw new UserException(_loc.get("no-driver", conf)).
                setFatal(true);

        ClassLoader loader = conf.getClassResolverInstance().
            getClassLoader(DataSourceFactory.class, null);
        String props = (factory2) ? conf.getConnection2Properties()
            : conf.getConnectionProperties();
        try {
            Class<?> driverClass;
            try {
                driverClass = Class.forName(driver, true, loader);
            } catch (ClassNotFoundException cnfe) {
                // try with the core class loader
                driverClass = Class.forName(driver);
            }

            if (Driver.class.isAssignableFrom(driverClass)) {
                DriverDataSource ds = conf.newDriverDataSourceInstance();
                ds.setClassLoader(loader);
                ds.setConnectionDriverName(driver);
                ds.setConnectionProperties(Configurations.
                    parseProperties(props));

                if (!factory2) {
                    ds.setConnectionFactoryProperties(Configurations.
                        parseProperties(conf.getConnectionFactoryProperties()));
                    ds.setConnectionURL(conf.getConnectionURL());
                    ds.setConnectionUserName(conf.getConnectionUserName());
                    ds.setConnectionPassword(conf.getConnectionPassword());
                } else {
                    ds.setConnectionFactoryProperties
                        (Configurations.parseProperties(conf.
                        getConnectionFactory2Properties()));
                    ds.setConnectionURL(conf.getConnection2URL());
                    ds.setConnectionUserName(conf.getConnection2UserName());
                    ds.setConnectionPassword(conf.getConnection2Password());
                }
                return ds;
            }

            // see if their driver name is actually a data source
            if (DataSource.class.isAssignableFrom(driverClass)) {
                return (DataSource) Configurations.newInstance(driver,
                    conf, props, AccessController.doPrivileged(
                        J2DoPrivHelper.getClassLoaderAction(
                            DataSource.class))); 
            }
        }
        catch (OpenJPAException ke) {
            throw ke;
        } catch (Exception e) {
            throw newConnectException(conf, factory2, e);
        }

        // not a driver or a data source; die
        throw new UserException(_loc.get("bad-driver", driver)).setFatal(true);
    }

    /**
     * Install listeners and base decorators.
     */
    public static DecoratingDataSource decorateDataSource(DataSource ds,
        JDBCConfiguration conf, boolean factory2) {
        Options opts = Configurations.parseProperties((factory2)
            ? conf.getConnectionFactory2Properties()
            : conf.getConnectionFactoryProperties());
        Log jdbcLog = conf.getLog(JDBCConfiguration.LOG_JDBC);
        Log sqlLog = conf.getLog(JDBCConfiguration.LOG_SQL);

        DecoratingDataSource dds = new DecoratingDataSource(ds);
        try {
            // add user-defined decorators
            List<ConnectionDecorator> decorators = new ArrayList<ConnectionDecorator>();
            decorators.addAll(Arrays.asList(conf.
                getConnectionDecoratorInstances()));

            // add jdbc events decorator
            JDBCEventConnectionDecorator ecd =
                new JDBCEventConnectionDecorator();
            Configurations.configureInstance(ecd, conf, opts);
            JDBCListener[] listeners = conf.getJDBCListenerInstances();
            for (int i = 0; i < listeners.length; i++)
                ecd.addListener(listeners[i]);
            decorators.add(ecd);

            // ask the DriverDataSource to provide any additional decorators
            if (ds instanceof DriverDataSource) {
                List<ConnectionDecorator> decs = ((DriverDataSource) ds).
                    createConnectionDecorators();
                if (decs != null)
                    decorators.addAll(decs);
            }

            // logging decorator
            LoggingConnectionDecorator lcd =
                new LoggingConnectionDecorator();
            Configurations.configureInstance(lcd, conf, opts);
            lcd.getLogs().setJDBCLog(jdbcLog);
            lcd.getLogs().setSQLLog(sqlLog);
            decorators.add(lcd);

            dds.addDecorators(decorators);
            return dds;
        } catch (OpenJPAException ke) {
            throw ke;
        } catch (Exception e) {
            throw newConnectException(conf, factory2, e);
        }
    }

    /**
     * Install things deferred until the DBDictionary instance is available.
     *
     * @author Steve Kim
     */
    public static DecoratingDataSource installDBDictionary(DBDictionary dict,
        DecoratingDataSource ds, final JDBCConfiguration conf,
        boolean factory2) {
        DataSource inner = ds.getInnermostDelegate();
        if (inner instanceof DriverDataSource)
            ((DriverDataSource) inner).initDBDictionary(dict);
        Connection conn = null;

        try {
            // add the dictionary as a warning handler on the logging decorator
            ConnectionDecorator cd;
            for (Iterator<ConnectionDecorator> itr = ds.getDecorators().iterator(); itr.hasNext();) {
                cd = itr.next();
                if (cd instanceof LoggingConnectionDecorator)
                    ((LoggingConnectionDecorator) cd).setWarningHandler(dict);
            }

            // misc configuration connection decorator (statement timeouts,
            // transaction isolation, etc)
            ConfiguringConnectionDecorator ccd =
                new ConfiguringConnectionDecorator();
            ccd.setTransactionIsolation(conf.getTransactionIsolationConstant());
            ccd.setQueryTimeout(conf.getQueryTimeout() == -1 
                ? -1 : conf.getQueryTimeout());
            Log log = conf.getLog(JDBCConfiguration.LOG_JDBC);
            if (factory2 || !conf.isConnectionFactoryModeManaged()) {
                if (!dict.supportsMultipleNontransactionalResultSets)
                    ccd.setAutoCommit(Boolean.FALSE);
                else
                    ccd.setAutoCommit(Boolean.TRUE);
                // add trace info for autoCommit setting
                if (log.isTraceEnabled())
                    log.trace(_loc.get("set-auto-commit", new Object[] {
                    dict.supportsMultipleNontransactionalResultSets}));
            }
            Options opts = Configurations.parseProperties((factory2)
                ? conf.getConnectionFactory2Properties()
                : conf.getConnectionFactoryProperties());
            Configurations.configureInstance(ccd, conf, opts);
            ds.addDecorator(ccd);

            // allow the dbdictionary to decorate the connection further
            ds.addDecorator(dict);
            
            // ensure dbdictionary to process connectedConfiguration()
            if (!factory2)
                conn = ds.getConnection(conf.getConnectionUserName(), conf
                        .getConnectionPassword());
            else
                conn = ds.getConnection(conf.getConnection2UserName(), conf
                        .getConnection2Password());

            return ds;
        } catch (Exception e) {
        	throw newConnectException(conf, factory2, e);
        } finally {
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException se) {
                    // ignore any exception since the connection is not going
                    // to be used anyway
                }
        }
    }
    
    static OpenJPAException newConnectException(JDBCConfiguration conf, 
    		boolean factory2, Exception cause) {
    	return new UserException(_eloc.get("poolds-null", factory2 
          	  ? new Object[]{conf.getConnection2DriverName(), 
          			         conf.getConnection2URL()}
          	  : new Object[]{conf.getConnectionDriverName(),
          		             conf.getConnectionURL()}),
          		             cause).setFatal(true);
    }

    /**
     * Return a data source with the given user name and password
     * pre-configured as the defaults when {@link DataSource#getConnection}
     * is called.
     */
    public static DataSource defaultsDataSource(DataSource ds,
        String user, String pass) {
        if (user == null && pass == null)
            return ds;
        // also check if they are both blank strings
        if ("".equals(user) && "".equals(pass))
            return ds;
        return new DefaultsDataSource(ds, user, pass);
    }

    /**
     * Close the given data source.
     */
    public static void closeDataSource(DataSource ds) {
        if (ds instanceof DelegatingDataSource)
            ds = ((DelegatingDataSource) ds).getInnermostDelegate();
        ImplHelper.close(ds);
    }

    /**
     * A data source with pre-configured default user name and password.
     */
    private static class DefaultsDataSource
        extends DelegatingDataSource {

        private final String _user;
        private final String _pass;

        public DefaultsDataSource(DataSource ds, String user, String pass) {
            super(ds);
            _user = user;
            _pass = pass;
        }

        public Connection getConnection()
            throws SQLException {
            return super.getConnection(_user, _pass);
        }

        public Connection getConnection(String user, String pass)
            throws SQLException {
            return super.getConnection(user, pass);
        }
    }
}
