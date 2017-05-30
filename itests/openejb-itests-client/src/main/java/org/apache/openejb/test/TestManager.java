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
package org.apache.openejb.test;

import java.io.File;
import java.io.FileInputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class TestManager {

    private static TestServer server;
    private static TestDatabase database;
    private static TestJms jms;
    private static boolean warn = true;

    // TODO: Move it to a central place where all system properties are managed in a unified way
    final static String TESTSUITE_PROPERTY_FILENAME = "openejb.testsuite.properties";
    final static String TEST_SERVER_CLASSNAME = "openejb.test.server";
    final static String TEST_DATABASE_CLASSNAME = "openejb.test.database";
    final static String TEST_JMS_CLASSNAME = "openejb.test.jms";

    public static void init(String propertiesFileName) throws Exception {
        Properties props = null;

        try {
            props = new Properties(System.getProperties());
            warn = props.getProperty("openejb.test.nowarn") == null;
        } catch (final SecurityException e) {
            throw new IllegalArgumentException("Cannot access the system properties: " + e.getClass().getName() + " " + e.getMessage());
        }

        if (propertiesFileName == null) {
            try {
                propertiesFileName = System.getProperty(TESTSUITE_PROPERTY_FILENAME);
                if (propertiesFileName != null) {
                    props.putAll(getProperties(propertiesFileName));
                }

            } catch (final SecurityException e) {
                throw new IllegalArgumentException("Cannot access the system property \"" + TESTSUITE_PROPERTY_FILENAME + "\": " + e.getClass().getName() + " " + e.getMessage());
            }
        } else {
            props.putAll(getProperties(propertiesFileName));
        }
        initServer(props);
        initDatabase(props);
        initJms(props);
    }

    public static void start() throws Exception {
        try {
            if (server != null) {
                server.start();
            }
        } catch (final Exception e) {
            if (warn)
                System.out.println("Cannot start the test server: " + e.getClass().getName() + " " + e.getMessage());
            throw e;
        }
        try {
            if (database != null) {
                database.start();
            }
        } catch (final Exception e) {
            if (warn)
                System.out.println("Cannot start the test database: " + e.getClass().getName() + " " + e.getMessage());
            throw e;
        }
    }

    public static void stop() throws Exception {
        try {
            if (database != null) {
                database.stop();
            }
        } catch (final Exception e) {
            if (warn)
                System.out.println("Cannot stop the test database: " + e.getClass().getName() + " " + e.getMessage());
            throw e;
        }
        try {
            if (server != null) {
                server.stop();
            }
        } catch (final Exception e) {
            if (warn)
                System.out.println("Cannot stop the test server 2: " + e.getClass().getName() + " " + e.getMessage());
            throw e;
        }
    }

    private static Properties getProperties(final String fileName) throws Exception {
        File file = new File(fileName);
        file = file.getAbsoluteFile();
        final Properties props = (Properties) System.getProperties().clone();
        props.load(new FileInputStream(file));
        return props;
    }

    private static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    private static void initServer(final Properties props) {
        try {

            final String className = props.getProperty(TEST_SERVER_CLASSNAME);
            if (className == null) {
                throw new IllegalArgumentException(
                    "Must specify a test server by setting its class name using the system property \"" + TEST_SERVER_CLASSNAME + "\"");
            }
            final ClassLoader cl = getContextClassLoader();
            final Class<?> testServerClass = Class.forName(className, true, cl);
            server = (TestServer) testServerClass.newInstance();
            server.init(props);
        } catch (final Exception e) {
            if (warn) e.printStackTrace();
            if (warn)
                System.out.println("Cannot instantiate or initialize the test server: " + e.getClass().getName() + " " + e.getMessage());
            throw new RuntimeException("Cannot instantiate or initialize the test server: " + e.getClass().getName() + " " + e.getMessage(), e);
        }
    }

    private static void initDatabase(final Properties props) {
        try {
            final String className = props.getProperty(TEST_DATABASE_CLASSNAME);
            if (className == null)
                throw new IllegalArgumentException("Must specify a test database by setting its class name  using the system property \"" + TEST_DATABASE_CLASSNAME + "\"");
            final ClassLoader cl = getContextClassLoader();
            final Class<?> testDatabaseClass = Class.forName(className, true, cl);
            database = (TestDatabase) testDatabaseClass.newInstance();
            database.init(props);
        } catch (final Exception e) {
            if (warn)
                System.out.println("Cannot instantiate or initialize the test database: " + e.getClass().getName() + " " + e.getMessage());
            throw new RuntimeException("Cannot instantiate or initialize the test database: " + e.getClass().getName() + " " + e.getMessage(), e);
        }
    }

    private static void initJms(final Properties props) {
        try {
            String className = props.getProperty(TEST_JMS_CLASSNAME);
            if (className == null) className = "org.apache.openejb.test.ActiveMqTestJms";
            final ClassLoader cl = getContextClassLoader();
            final Class<?> testJmsClass = Class.forName(className, true, cl);
            jms = (TestJms) testJmsClass.newInstance();
            jms.init(props);
        } catch (final Exception e) {
            if (warn)
                System.out.println("Cannot instantiate or initialize the test jms: " + e.getClass().getName() + " " + e.getMessage());
            throw new RuntimeException("Cannot instantiate or initialize the test jms: " + e.getClass().getName() + " " + e.getMessage(), e);
        }
    }


    public static TestServer getServer() {
        return server;
    }

    public static TestDatabase getDatabase() {
        return database;
    }

    public static TestJms getJms() {
        return jms;
    }

    public static Properties getContextEnvironment() {
        return server.getContextEnvironment();
    }
}
