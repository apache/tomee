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
package org.apache.openejb.tomcat.loader;

import java.io.File;
import java.util.Properties;

import org.apache.openejb.loader.Embedder;
import org.apache.openejb.loader.SystemInstance;

/**
 * This class should only be loadded and used via reflection from TomcatEmbedder. 
 */
class TomcatHook {
    @SuppressWarnings({"UnusedDeclaration"})
    private static void hook(Properties properties) {
        // verify properties
        if (properties == null) throw new NullPointerException("properties is null");
                if (!properties.containsKey("openejb.war")) {
            throw new IllegalArgumentException("properties must contain the openejb.war property");
        }
        File openejbWar = new File(properties.getProperty("openejb.war"));
        if (!openejbWar.isDirectory()) {
            throw new IllegalArgumentException("openejb.war is not a directory: " + openejbWar);
        }

        if (SystemInstance.isInitialized()) {
            return;
        }

        properties.setProperty("openejb.loader", "tomcat-system");

        String catalinaHome = System.getProperty("catalina.home");
        properties.setProperty("openejb.home", catalinaHome);
        System.setProperty("openejb.home", catalinaHome);

        String catalinaBase = System.getProperty("catalina.base");
        properties.setProperty("openejb.base", catalinaBase);
        System.setProperty("openejb.base", catalinaBase);

        System.setProperty("openejb.war", openejbWar.getAbsolutePath());
        File libDir = new File(openejbWar, "lib");
        String libPath = libDir.getAbsolutePath();
        properties.setProperty("openejb.libs", libPath);

        // System.setProperty("tomcat.version", "x.y.z.w");
        // System.setProperty("tomcat.built", "mmm dd yyyy hh:mm:ss");
        try {
            Properties tomcatServerInfo = new Properties();
            ClassLoader classLoader = TomcatHook.class.getClassLoader();
            tomcatServerInfo.load(classLoader.getResourceAsStream("org/apache/catalina/util/ServerInfo.properties"));

            String serverNumber = tomcatServerInfo.getProperty("server.number");
            if (serverNumber == null) {
                // Tomcat5 only has server.info
                String serverInfo = tomcatServerInfo.getProperty("server.info");
                if (serverInfo != null) {
                    int slash = serverInfo.indexOf('/');
                    serverNumber = serverInfo.substring(slash + 1);
                }
            }
            if (serverNumber != null) {
                System.setProperty("tomcat.version", serverNumber);
            }

            String serverBuilt = tomcatServerInfo.getProperty("server.built");
            if (serverBuilt != null) {
                System.setProperty("tomcat.built", serverBuilt);
            }
        } catch (Throwable e) {
        }

        try {
            // create the loader
            SystemInstance.init(properties);
            Embedder embedder = new Embedder("org.apache.openejb.tomcat.catalina.TomcatLoader");
            embedder.init(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}