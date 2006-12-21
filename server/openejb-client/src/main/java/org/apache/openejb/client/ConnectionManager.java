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
package org.apache.openejb.client;

import java.io.IOException;
import java.util.Properties;
import java.net.URI;

public class ConnectionManager {

    private static ConnectionFactory factory;
    private static Class defaultFactoryClass = SocketConnectionFactory.class;
    private static String factoryName;

    static {
        try {
            installFactory(defaultFactoryClass.getName());
        } catch (Exception e) {

        }
    }

    public static Connection getConnection(URI uri) throws IOException {
        if (uri.getScheme().equals("http")){
            return new HttpConnectionFactory().getConnection(uri);
        } else {
            return factory.getConnection(uri);
        }
    }

    public static void setFactory(String factoryName) throws IOException {
        installFactory(factoryName);
    }

    public static ConnectionFactory getFactory() {
        return factory;
    }

    public static String getFactoryName() {
        return factoryName;
    }

    private static void installFactory(String factoryName) throws IOException {

        Class factoryClass = null;
        ConnectionFactory factory = null;

        try {
            ClassLoader cl = getContextClassLoader();
            factoryClass = Class.forName(factoryName, true, cl);
        } catch (Exception e) {
            throw new IOException("No ConnectionFactory Can be installed. Unable to load the class " + factoryName);
        }

        try {
            factory = (ConnectionFactory) factoryClass.newInstance();
        } catch (Exception e) {
            throw new IOException("No ConnectionFactory Can be installed. Unable to instantiate the class " + factoryName);
        }

        try {

            factory.init(new Properties());
        } catch (Exception e) {
            throw new IOException("No ConnectionFactory Can be installed. Unable to initialize the class " + factoryName);
        }

        ConnectionManager.factory = factory;
        ConnectionManager.factoryName = factoryName;
    }

    public static ClassLoader getContextClassLoader() {
        return (ClassLoader) java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                }
        );
    }

}
