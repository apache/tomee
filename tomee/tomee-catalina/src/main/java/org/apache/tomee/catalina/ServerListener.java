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
package org.apache.tomee.catalina;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomee.loader.TomcatHelper;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerListener implements LifecycleListener {
    private static final Logger LOGGER = Logger.getLogger(ServerListener.class.getName());

    static private boolean listenerInstalled;

    public void lifecycleEvent(LifecycleEvent event) {
        // only install once
        if (listenerInstalled || !Lifecycle.AFTER_INIT_EVENT.equals(event.getType())) return;
        if (!(event.getSource() instanceof StandardServer)) return;

        try {
            final StandardServer server = (StandardServer) event.getSource();

            TomcatHelper.setServer(server);

            final Properties properties = new Properties();
            System.getProperties().setProperty("openejb.embedder.source", getClass().getSimpleName());
            properties.setProperty("openejb.embedder.source", getClass().getSimpleName());


            // if SystemInstance is already initialized, then return
            if (SystemInstance.isInitialized()) {
                return;
            }

            // set the openejb.loader property to tomcat-system
            properties.setProperty("openejb.loader", "tomcat-system");

            // Get the value of catalina.home and set it to openejb.home
            String catalinaHome = System.getProperty("catalina.home");
            properties.setProperty("openejb.home", catalinaHome);

            //Sets system property for openejb.home
            System.setProperty("openejb.home", catalinaHome);

            //get the value of catalina.base and set it to openejb.base
            String catalinaBase = System.getProperty("catalina.base");
            properties.setProperty("openejb.base", catalinaBase);

            //Sets system property for openejb.base
            System.setProperty("openejb.base", catalinaBase);


            // System.setProperty("tomcat.version", "x.y.z.w");
            // System.setProperty("tomcat.built", "mmm dd yyyy hh:mm:ss");
            // set the System properties, tomcat.version, tomcat.built
            try {
                ClassLoader classLoader = ServerListener.class.getClassLoader();
                Properties tomcatServerInfo = IO.readProperties(classLoader.getResourceAsStream("org/apache/catalina/util/ServerInfo.properties"), new Properties());

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
                // no-op
            }

            // manage additional libraries
            try {
                ProvisioningUtil.addAdditionalLibraries();
            } catch (IOException e) {
                // ignored
            }

            TomcatLoader loader = new TomcatLoader();
            loader.init(properties);

            listenerInstalled = true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "TomEE Listener can't start OpenEJB", e);
            // e.printStackTrace(System.err);
        }
    }
}
