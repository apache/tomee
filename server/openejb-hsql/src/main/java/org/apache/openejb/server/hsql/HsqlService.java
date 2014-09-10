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
package org.apache.openejb.server.hsql;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.jdbc.plugin.HsqldbDataSourcePlugin;
import org.apache.openejb.server.SelfManaging;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.spi.ContainerSystem;
import org.hsqldb.Database;
import org.hsqldb.DatabaseManager;
import org.hsqldb.Server;
import org.hsqldb.persist.HsqlDatabaseProperties;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.ServerConfiguration;
import org.hsqldb.server.ServerConstants;

import javax.naming.Binding;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.sql.DataSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class HsqlService implements ServerService, SelfManaging {

    // copied from org.hsqldb.server.ServerProperties since it uses package visibility
    private static final java.lang.String sc_key_port = "server.port";
    private static final java.lang.String sc_key_silent = "server.silent";
    private static final java.lang.String sc_key_dbname = "server.dbname";
    private static final java.lang.String sc_key_address = "server.address";
    private static final java.lang.String sc_key_database = "server.database";
    private static final java.lang.String sc_key_no_system_exit = "server.no_system_exit";
	private static final String DRIVER_NAME = HsqlDatabaseProperties.PRODUCT_NAME + " Driver";

    private int port = ServerConfiguration.getDefaultPort(ServerConstants.SC_PROTOCOL_HSQL, false);
    private String ip = ServerConstants.SC_DEFAULT_ADDRESS;
    private Server server;

    @Override
    public String getName() {
        return "hsql";
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getIP() {
        return ip;
    }

    @Override
    public void init(final Properties p) throws Exception {
        final Properties properties = new Properties();
        for (final Map.Entry<Object, Object> entry : p.entrySet()) {
            // Sometimes the properties object has non string values
            if (!(entry.getKey() instanceof String))
                continue;
            if (!(entry.getValue() instanceof String))
                continue;

            final String property = (String) entry.getKey();
            final String value = (String) entry.getValue();

            if (property.startsWith(sc_key_dbname + ".") ||
                property.startsWith(sc_key_database + ".")) {

                throw new ServiceException("Databases cannot be declared in the hsql.properties.  " +
                    "Instead declare a database connection in the openejb.conf file");
            }

            if ("port".equals(property)) {
                properties.setProperty(sc_key_port, value);
            } else if ("bind".equals(property)) {
                properties.setProperty(sc_key_address, value);
            } else {
                properties.setProperty(property, value);
            }
        }
        properties.setProperty(sc_key_no_system_exit, "true");

        final boolean disabled = Boolean.parseBoolean(properties.getProperty("disabled"));
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        if (!disabled && containerSystem != null) {
            final NamingEnumeration<Binding> bindings;
            try {
                bindings = containerSystem.getJNDIContext().listBindings("openejb/Resource/");
                final Set<String> dbnames = new TreeSet<String>();
                for (final Binding binding : Collections.list(bindings)) {
                    final Object value = binding.getObject();
                    if (value instanceof DataSource) {
                        final DataSource jdbc = (DataSource) value;
                        Connection connection = null;
                        String path = null;
                        try {
                            connection = jdbc.getConnection();
                            final DatabaseMetaData meta = connection.getMetaData();
                            path = getPath(meta.getDriverName(), meta.getURL());
                        } catch (Throwable t) {
                            continue;
                        } finally {
                            if (connection != null) {
                                try {
                                    connection.close();
                                } catch (SQLException sqlEx) {
                                    // no-op
                                }
                            }
                        }

                        if (path != null) {
                            if (dbnames.size() > 9) {
                                throw new ServiceException("Hsql Server can only host 10 database instances");
                            }
                            String dbname = path.substring(path.lastIndexOf(':') + 1);
                            dbname = dbname.substring(dbname.lastIndexOf('/') + 1);
                            if (!dbnames.contains(dbname)) {
                                properties.put(sc_key_dbname + "." + dbnames.size(), dbname);
                                properties.put(sc_key_database + "." + dbnames.size(), path);
                                dbnames.add(dbname);
                            }
                        }
                    }
                }
            } catch (NameNotFoundException e) {
                //Ignore
            }

            // create the server
            server = new Server();
            // add the silent property
            properties.setProperty(sc_key_silent, "true");
            // set the log and error writers
            server.setLogWriter(new HsqlPrintWriter(false));
            server.setErrWriter(new HsqlPrintWriter(true));
            server.setProperties(new HsqlProperties(properties));

            // get the port
            port = server.getPort();

            // get the Address
            final String ipString = server.getAddress();
            if (ipString != null && ipString.length() > 0) {
                this.ip = ipString;
            }
        }
    }

    private String getPath(final String driver, final String url) {
        // is this connectoion using the hsql driver?
        if ((!HsqlDatabaseProperties.PRODUCT_NAME.equals(driver)) && (!DRIVER_NAME.equals(driver)))  {
            return null;
        }

        // is this a hsql url?
        if (url == null || !url.startsWith("jdbc:hsqldb:")) {
            return null;
        }

        // resolve the relative path and
        // hack off the jdbc:hsqldb stuff
        final String path = HsqldbDataSourcePlugin.toAbsolutePath(url).substring("jdbc:hsqldb:".length());

        // is this a connection to a local file, mem, or res database?
        if (!path.startsWith("file:") && !path.startsWith("mem:") && path.startsWith("res:")) {
            return null;
        }

        return path;
    }

    @Override
    public void service(final InputStream inputStream, final OutputStream outputStream) throws ServiceException, IOException {
        throw new UnsupportedOperationException("Method not implemented: service(InputStream in, OutputStream out)");
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {
        throw new UnsupportedOperationException("Method not implemented: service(Socket socket)");
    }

    @Override
    public void start() throws ServiceException {
        if (server == null)
            return;
        server.start();
    }

    @Override
    public void stop() throws ServiceException {
        if (server == null)
            return;
        try {
            server.stop();
        } finally {
            server = null;
            DatabaseManager.closeDatabases(Database.CLOSEMODE_COMPACT);
        }
    }
}
