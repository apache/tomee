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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.core.security.jaas;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Base64;
import org.apache.openejb.util.HexConverter;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.StringUtilities;

import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.sql.DataSource;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * A login module that loads security information from a SQL database.  Expects
 * to be run by a GenericSecurityRealm (doesn't work on its own).
 * <p/>
 * This requires database connectivity information (either 1: a dataSourceName and
 * optional dataSourceApplication or 2: a JDBC driver, URL, username, and password)
 * and 2 SQL queries.
 * <p/>
 * The userSelect query should return 2 values, the username and the password in
 * that order.  It should include one PreparedStatement parameter (a ?) which
 * will be filled in with the username.  In other words, the query should look
 * like: <tt>SELECT user, password FROM credentials WHERE username=?</tt>
 * <p/>
 * The groupSelect query should return 2 values, the username and the group name in
 * that order (but it may return multiple rows, one per group).  It should include
 * one PreparedStatement parameter (a ?) which will be filled in with the username.
 * In other words, the query should look like:
 * <tt>SELECT user, role FROM user_roles WHERE username=?</tt>
 * <p/>
 * This login module checks security credentials so the lifecycle methods must return true to indicate success
 * or throw LoginException to indicate failure.
 *
 * @version $Rev$ $Date$
 */
public class SQLLoginModule implements LoginModule {
    private static Logger log = Logger.getInstance(
            LogCategory.OPENEJB_SECURITY, "org.apache.openejb.util.resources");

    private EnumMap<Option, String> optionsMap = new EnumMap<Option, String>(Option.class);
    private String connectionURL;
    private Properties properties;
    private Driver driver;
    private DataSource dataSource;
    private String userSelect;
    private String groupSelect;
    private String digest;
    private String encoding;

    private boolean loginSucceeded;
    private Subject subject;
    private CallbackHandler handler;
    private String cbUsername;
    private String cbPassword;
    private final Set<String> groups = new HashSet<String>();
    private final Set<Principal> allPrincipals = new HashSet<Principal>();

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.handler = callbackHandler;

        for (Object key : options.keySet()) {
            Option option = Option.findByName((String) key);
            if (option != null) {
                String value = (String) options.get(key);
                optionsMap.put(option, value.trim());
            } else {
                log.warning("Ignoring option: {0}. Not supported.", key);
            }
        }

        userSelect = optionsMap.get(Option.USER_SELECT);
        groupSelect = optionsMap.get(Option.GROUP_SELECT);

        digest = optionsMap.get(Option.DIGEST);
        encoding = optionsMap.get(Option.ENCODING);

        if (!StringUtilities.checkNullBlankString(digest)) {
            // Check if the digest algorithm is available
            try {
                MessageDigest.getInstance(digest);
            } catch (NoSuchAlgorithmException e) {
                initError(e, "Digest algorithm %s is not available.", digest);
            }

            if (encoding != null && !"hex".equalsIgnoreCase(encoding) && !"base64".equalsIgnoreCase(encoding)) {
                initError(null, "Digest Encoding %s is not supported.", encoding);
            }
        }

        if (optionsMap.containsKey(Option.DATABASE_POOL_NAME)) {
            String dataSourceName = optionsMap.get(Option.DATABASE_POOL_NAME);
            ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            try {
                dataSource = (DataSource) containerSystem.getJNDIContext().lookup("openejb/Resource/" + dataSourceName);
            } catch (NamingException e) {
                initError(e, "Data source %s not found.", dataSourceName);
            }
        } else if (optionsMap.containsKey(Option.CONNECTION_URL)) {
            connectionURL = optionsMap.get(Option.CONNECTION_URL);
            String user = optionsMap.get(Option.USER);
            String password = optionsMap.get(Option.PASSWORD);
            String driverName = optionsMap.get(Option.DRIVER);
            properties = new Properties();

            if (user != null) {
                properties.put("user", user);
            }

            if (password != null) {
                properties.put("password", password);
            }

            if (driverName != null) {
                ClassLoader cl = getClass().getClassLoader();
                try {
                    driver = (Driver) cl.loadClass(driverName).newInstance();
                } catch (ClassNotFoundException e) {
                    initError(e, "Driver class %s is not available. Perhaps you need to add it as a dependency in your deployment plan?", driverName);
                } catch (Exception e) {
                    initError(e, "Unable to load, instantiate, register driver %s: %s", driverName, e.getMessage());
                }
            }
        } else {
            initError(null, "Neither %s nor %s was specified", Option.DATABASE_POOL_NAME.name, Option.CONNECTION_URL.name);
        }
    }

    private void initError(Exception e, String format, Object... args) {
        String message = String.format(format, args);
        log.error("Initialization failed. {0}", message);
        throw new IllegalArgumentException(message, e);
    }

    /**
     * This LoginModule is not to be ignored. So, this method should never
     * return false.
     *
     * @return true if authentication succeeds, or throw a LoginException such
     *         as FailedLoginException if authentication fails
     */
    public boolean login() throws LoginException {
        loginSucceeded = false;
        Callback[] callbacks = new Callback[2];

        callbacks[0] = new NameCallback("User name");
        callbacks[1] = new PasswordCallback("Password", false);

        try {
            handler.handle(callbacks);
        } catch (IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            throw (LoginException) new LoginException().initCause(uce);
        }

        assert callbacks.length == 2;

        cbUsername = ((NameCallback) callbacks[0]).getName();

        if (StringUtilities.checkNullBlankString(cbUsername)) {
            throw new FailedLoginException();
        }

        char[] provided = ((PasswordCallback) callbacks[1]).getPassword();
        cbPassword = provided == null ? null : new String(provided);

        try {
            Connection conn;
            if (dataSource != null) {
                conn = dataSource.getConnection();
            } else if (driver != null) {
                conn = driver.connect(connectionURL, properties);
            } else {
                conn = DriverManager.getConnection(connectionURL, properties);
            }

            try {
                PreparedStatement statement = conn.prepareStatement(userSelect);
                try {
                    int count = statement.getParameterMetaData().getParameterCount();
                    for (int i = 0; i < count; i++) {
                        statement.setObject(i + 1, cbUsername);
                    }
                    ResultSet result = statement.executeQuery();

                    try {
                        boolean found = false;
                        while (result.next()) {
                            String userName = result.getString(1);
                            String userPassword = result.getString(2);

                            if (cbUsername.equals(userName)) {
                                found = true;
                                if (!checkPassword(userPassword, cbPassword)) {
                                    throw new FailedLoginException();
                                }
                                break;
                            }
                        }
                        if (!found) {
                            // User does not exist
                            throw new FailedLoginException();
                        }
                    } finally {
                        result.close();
                    }
                } finally {
                    statement.close();
                }

                statement = conn.prepareStatement(groupSelect);
                try {
                    int count = statement.getParameterMetaData().getParameterCount();
                    for (int i = 0; i < count; i++) {
                        statement.setObject(i + 1, cbUsername);
                    }
                    ResultSet result = statement.executeQuery();

                    try {
                        while (result.next()) {
                            String userName = result.getString(1);
                            String groupName = result.getString(2);

                            if (cbUsername.equals(userName)) {
                                groups.add(groupName);
                            }
                        }
                    } finally {
                        result.close();
                    }
                } finally {
                    statement.close();
                }
            } finally {
                conn.close();
            }
        } catch (LoginException e) {
            // Clear out the private state
            cbUsername = null;
            cbPassword = null;
            groups.clear();
            throw e;
        } catch (SQLException sqle) {
            // Clear out the private state
            cbUsername = null;
            cbPassword = null;
            groups.clear();
            throw (LoginException) new LoginException("SQL error").initCause(sqle);
        } catch (Exception e) {
            // Clear out the private state
            cbUsername = null;
            cbPassword = null;
            groups.clear();
            throw (LoginException) new LoginException("Could not access datasource").initCause(e);
        }

        loginSucceeded = true;
        return true;
    }

    /**
     * @return true if login succeeded and commit succeeded, or false if login
     *         failed but commit succeeded.
     * @throws LoginException if login succeeded but commit failed.
     */
    public boolean commit() throws LoginException {
        if (loginSucceeded) {
            if (cbUsername != null) {
                allPrincipals.add(new UserPrincipal(cbUsername));
            }
            for (String group : groups) {
                allPrincipals.add(new GroupPrincipal(group));
            }
            subject.getPrincipals().addAll(allPrincipals);
        }

        // Clear out the private state
        cbUsername = null;
        cbPassword = null;
        groups.clear();

        return loginSucceeded;
    }

    public boolean abort() throws LoginException {
        if (loginSucceeded) {
            // Clear out the private state
            cbUsername = null;
            cbPassword = null;
            groups.clear();
            allPrincipals.clear();
        }
        return loginSucceeded;
    }

    public boolean logout() throws LoginException {
        // Clear out the private state
        loginSucceeded = false;
        cbUsername = null;
        cbPassword = null;
        groups.clear();
        if (!subject.isReadOnly()) {
            // Remove principals added by this LoginModule
            subject.getPrincipals().removeAll(allPrincipals);
        }
        allPrincipals.clear();
        return true;
    }

    /**
     * This method checks if the provided password is correct. The original
     * password may have been digested.
     *
     * @param real     Original password in digested form if applicable
     * @param provided User provided password in clear text
     * @return true If the password is correct
     */
    private boolean checkPassword(String real, String provided) {
        if (real == null && provided == null) {
            return true;
        }

        if (real == null || provided == null) {
            return false;
        }

        // Both are non-null
        if (StringUtilities.checkNullBlankString(digest)) {
            // No digest algorithm is used
            return real.equals(provided);
        }

        try {
            // Digest the user provided password
            MessageDigest md = MessageDigest.getInstance(digest);
            byte[] data = md.digest(provided.getBytes());

            if (encoding == null || "hex".equalsIgnoreCase(encoding)) {
                return real.equalsIgnoreCase(HexConverter.bytesToHex(data));
            } else if ("base64".equalsIgnoreCase(encoding)) {
                return real.equals(new String(Base64.encodeBase64(data)));
            }
        } catch (NoSuchAlgorithmException e) {
            // Should not occur.  Availability of algorithm has been checked at initialization
            log.error("Should not occur.  Availability of algorithm has been checked at initialization.", e);
        }
        return false;
    }

    private enum Option {
        USER_SELECT("userSelect"),
        GROUP_SELECT("groupSelect"),
        CONNECTION_URL("jdbcURL"),
        USER("jdbcUser"),
        PASSWORD("jdbcPassword"),
        DRIVER("jdbcDriver"),
        DATABASE_POOL_NAME("dataSourceName"),
        DIGEST("digest"),
        ENCODING("encoding");

        public final String name;

        private Option(String name) {
            this.name = name;
        }

        public static Option findByName(String name) {
            for (Option opt : values()) {
                if (opt.name.equals(name))
                    return opt;
            }
            return null;
        }

    }
}