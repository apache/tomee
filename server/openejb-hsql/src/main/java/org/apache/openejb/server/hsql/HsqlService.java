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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.jdbc.HsqldbDataSourcePlugin;
import org.apache.openejb.server.SelfManaging;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.spi.ContainerSystem;
import org.hsqldb.Database;
import org.hsqldb.DatabaseManager;
import org.hsqldb.Server;
import org.hsqldb.ServerConfiguration;
import org.hsqldb.ServerConstants;
import org.hsqldb.jdbcDriver;
import org.hsqldb.persist.HsqlProperties;

import javax.naming.Binding;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * @version $Rev: 480809 $ $Date: 2006-11-29 18:27:38 -0800 (Wed, 29 Nov 2006) $
 */
public class HsqlService implements ServerService, SelfManaging {
    private int port = ServerConfiguration.getDefaultPort(ServerConstants.SC_PROTOCOL_HSQL, false);
    private String ip = ServerConstants.SC_DEFAULT_ADDRESS;
    private Server server;

    public String getName() {
        return "hsql";
    }

    public int getPort() {
        return port;
    }

    public String getIP() {
        return ip;
    }


    public void init(Properties p) throws Exception {
        Properties properties = new Properties();
        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            // Somtimes the properties object has non string values
            if (!(entry.getKey() instanceof String)) continue;
            if (!(entry.getValue() instanceof String)) continue;

            String property = (String) entry.getKey();
            String value = (String) entry.getValue();

            if (property.startsWith(ServerConstants.SC_KEY_DBNAME + ".") ||
                    property.startsWith(ServerConstants.SC_KEY_DATABASE + ".")) {

                throw new ServiceException("Databases cannot be declared in the hsql.properties.  " +
                        "Instead declare a database connection in the openejb.conf file");
            }

            if ("port".equals(property)) {
                properties.setProperty(ServerConstants.SC_KEY_PORT, value);
            } else if ("bind".equals(property)) {
                properties.setProperty(ServerConstants.SC_KEY_ADDRESS, value);
            } else {
                properties.setProperty(property, value);
            }
        }
        properties.setProperty(ServerConstants.SC_KEY_NO_SYSTEM_EXIT, "true");

        boolean disabled = Boolean.parseBoolean(properties.getProperty("disabled"));
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        if (!disabled && containerSystem != null) {
            NamingEnumeration<Binding> bindings = null;
            try {
                bindings = containerSystem.getJNDIContext().listBindings("java:openejb/Resource/");
                Set<String> dbnames = new TreeSet<String>();
                for (Binding binding : Collections.list(bindings)) {
                    Object value = binding.getObject();
                    if (value instanceof BasicDataSource) {
                        BasicDataSource jdbc = (BasicDataSource) value;
                        String path = getPath(jdbc);
                        if (path != null) {
                            if (dbnames.size() > 9) {
                                throw new ServiceException("Hsql Server can only host 10 database instances");
                            }
                            String dbname = path.substring(path.lastIndexOf(':') + 1);
                            dbname = dbname.substring(dbname.lastIndexOf('/') + 1);
                            if (!dbnames.contains(dbname)) {
                                properties.put(ServerConstants.SC_KEY_DBNAME + "." + dbnames.size(), dbname);
                                properties.put(ServerConstants.SC_KEY_DATABASE + "." + dbnames.size(), path);
                                dbnames.add(dbname);
                            }
                        }
                    }
                }
            } catch (NameNotFoundException e) {
            }

            // create the server
            server = new Server();
            // add the silent property
            properties.setProperty(ServerConstants.SC_KEY_SILENT, "true");
            // set the log and error writers
            server.setLogWriter(new HsqlPrintWriter(false));
            server.setErrWriter(new HsqlPrintWriter(true));
            server.setProperties(new HsqlProperties(properties));

            // get the port
            port = server.getPort();

            // get the Address
            String ipString = server.getAddress();
            if (ipString != null && ipString.length() > 0) {
                this.ip = ipString;
            }
        }
    }

    private String getPath(BasicDataSource jdbc) {
        // is this connectoion using the hsql driver?
        if (!jdbcDriver.class.getName().equals(jdbc.getDriverClassName())) {
            return null;
        }

        String url = jdbc.getUrl();

        // is this a hsql url?
        if (url == null || !url.startsWith("jdbc:hsqldb:")) {
            return null;
        }

        // resolve the relative path
        url = HsqldbDataSourcePlugin.toAbsolutePath(url);

        // hack off the jdbc:hsqldb stuff
        String path = url.substring("jdbc:hsqldb:".length());

        // is this a connection to a local file, mem, or res database?
        if (!path.startsWith("file:") && !path.startsWith("mem:") && path.startsWith("res:")) {
            return null;
        }

        return path;
    }

    public void service(InputStream inputStream, OutputStream outputStream) throws ServiceException, IOException {
        throw new UnsupportedOperationException("Method not implemented: service(InputStream in, OutputStream out)");
    }

    public void service(Socket socket) throws ServiceException, IOException {
        throw new UnsupportedOperationException("Method not implemented: service(Socket socket)");
    }

    public void start() throws ServiceException {
        if (server == null) return;
        server.start();
    }

    public void stop() throws ServiceException {
        if (server == null) return;
        try {
            server.stop();
        } finally {
            server = null;
            DatabaseManager.closeDatabases(Database.CLOSEMODE_COMPACT);
        }
    }
}
