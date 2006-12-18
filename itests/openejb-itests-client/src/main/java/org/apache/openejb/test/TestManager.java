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
import java.util.Properties;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * 
 * @version $Rev$ $Date$
 */
public class TestManager {
    
    private static TestServer server;
    private static TestDatabase database;
    private static boolean warn = true;

    public static void init(String propertiesFileName) throws Exception{
        Properties props = null;

        try{
            props = new Properties(System.getProperties());
            warn = props.getProperty("openejb.test.nowarn") != null;
        } catch (SecurityException e){
            throw new IllegalArgumentException("Cannot access the system properties: "+e.getClass().getName()+" "+e.getMessage());
        }
        
        if (propertiesFileName == null) {
            try{
                propertiesFileName = System.getProperty("openejb.testsuite.properties");
                
                if (propertiesFileName == null) {
                    if (warn) System.out.println("Warning: No test suite configuration file specified, assuming system properties contain all needed information.  To specify a test suite configuration file by setting its location using the system property \"openejb.testsuite.properties\"");
                } else {
                    props.putAll( getProperties( propertiesFileName ) );
                }

            } catch (SecurityException e){
                throw new IllegalArgumentException("Cannot access the system property \"openejb.testsuite.properties\": "+e.getClass().getName()+" "+e.getMessage());
            }
        } else {
            props.putAll( getProperties( propertiesFileName ) );
        }
        initServer(props);
        initDatabase(props);
    }
    
    public static void start() throws Exception{
        try{
            if (server != null) {
                server.start();
            }
        } catch (Exception e){
            if (warn) System.out.println("Cannot start the test server: "+e.getClass().getName()+" "+e.getMessage());
            throw e;
        }
        try{
            if (database != null) {
                database.start();
            }
        } catch (Exception e){
            if (warn) System.out.println("Cannot start the test database: "+e.getClass().getName()+" "+e.getMessage());
            throw e;
        }
    }

    public static void stop() throws Exception{
        try{
            if (database != null) {
                database.stop();
            }
        } catch (Exception e){
            if (warn) System.out.println("Cannot stop the test database: "+e.getClass().getName()+" "+e.getMessage());
            throw e;
        }
        try{
            if (server != null) {
                server.stop();
            }
        } catch (Exception e){
            if (warn) System.out.println("Cannot stop the test server 2: "+e.getClass().getName()+" "+e.getMessage());
            throw e;
        }
    }
            
    private static Properties getProperties(String fileName) throws Exception{
        File file = new File(fileName);
        file = file.getAbsoluteFile();
        Properties props = (Properties)System.getProperties().clone();
        props.load(new FileInputStream(file));
        return props;
    }

    private static ClassLoader getContextClassLoader() {
        return (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    private static void initServer(Properties props){
        try{

            String className = props.getProperty("openejb.test.server");
            if (className == null) throw new IllegalArgumentException("Must specify a test server by setting its class name using the system property \"openejb.test.server\"");
            ClassLoader cl = getContextClassLoader();
            Class<?> testServerClass = Class.forName( className, true, cl );
            server = (TestServer)testServerClass.newInstance();
            server.init( props );
        } catch (Exception e){
            if (warn) e.printStackTrace();
            if (warn) System.out.println("Cannot instantiate or initialize the test server: "+e.getClass().getName()+" "+e.getMessage());
            throw new RuntimeException("Cannot instantiate or initialize the test server: "+e.getClass().getName()+" "+e.getMessage(),e);
        }
    }

    private static void initDatabase(Properties props){
        try{
            String className = props.getProperty("openejb.test.database");
            if (className == null) throw new IllegalArgumentException("Must specify a test database by setting its class name  using the system property \"openejb.test.database\"");
            ClassLoader cl = getContextClassLoader();
            Class<?> testDatabaseClass = Class.forName( className , true, cl);
            database = (TestDatabase)testDatabaseClass.newInstance();
            database.init( props );
        } catch (Exception e){
            if (warn) System.out.println("Cannot instantiate or initialize the test database: "+e.getClass().getName()+" "+e.getMessage());
            throw new RuntimeException("Cannot instantiate or initialize the test database: "+e.getClass().getName()+" "+e.getMessage(),e);
        }
    }


    public static TestServer getServer(){
        return server;
    }

    public static TestDatabase getDatabase(){
        return database;
    }
    
    public static Properties getContextEnvironment(){
        return server.getContextEnvironment();
    }
}
