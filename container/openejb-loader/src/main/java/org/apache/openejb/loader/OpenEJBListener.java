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
package org.apache.openejb.loader;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;

import java.io.File;
import java.util.Properties;

public class OpenEJBListener implements LifecycleListener {
    static private boolean installed;


    public static boolean isInstalled() {
        return installed;
    }

    public OpenEJBListener() {
    }

    public void lifecycleEvent(LifecycleEvent event) {
        Object source = event.getSource();
        if (source instanceof StandardServer) {
            installed = true;
            StandardServer standardServer = (StandardServer) source;
            init(standardServer);
        }
    }

    public void init(StandardServer standardServer) {
        if (!SystemInstance.isInitialized()) {
            Properties properties = new Properties();
            properties.setProperty("openejb.loader", "tomcat-system");

            File webappDir = findOpenEjbWar(standardServer);
            File libDir = new File(webappDir, "lib");
            String catalinaHome = System.getProperty("catalina.home");
            properties.setProperty("openejb.home", catalinaHome);
            System.setProperty("openejb.home", catalinaHome);
            String catalinaBase = System.getProperty("catalina.base");
            properties.setProperty("openejb.base", catalinaBase);
            System.setProperty("openejb.base", catalinaBase);
            String libPath = libDir.getAbsolutePath();
            properties.setProperty("openejb.libs", libPath);

            try {
                // create the loader
                SystemInstance.init(properties);
                Embedder embedder = new Embedder("org.apache.openejb.tomcat.TomcatLoader");
                embedder.init(properties);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public File findOpenEjbWar(StandardServer standardServer) {
        // in Tomcat 5.5 the OpenEjb war is in the server/webapps director
        String catalinaBase = System.getProperty("catalina.base");
        File serverWebapps = new File(catalinaBase, "server/webapps");
        File openEjbWar = findOpenEjbWar(serverWebapps);
        if (openEjbWar != null) {
            return openEjbWar;
        }

        // in Tomcat 6 the OpenEjb war is normally in webapps, but we just scan all hosts directories
        for (Service service : standardServer.findServices()) {
            Container container = service.getContainer();
            if (container instanceof StandardEngine) {
                StandardEngine engine = (StandardEngine) container;
                for (Container child : engine.findChildren()) {
                    if (child instanceof StandardHost) {
                        StandardHost host = (StandardHost) child;
                        String appBase = host.getAppBase();

                        // determine the host dir (normally webapps)
                        File hostDir = new File(appBase);
                        if (!hostDir.isAbsolute()) {
                            hostDir = new File(catalinaBase, appBase);
                        }

                        openEjbWar = findOpenEjbWar(hostDir);
                        if (openEjbWar != null) {
                            return openEjbWar;
                        }
                    }
                }
            }
        }


        return null;
    }

    private File findOpenEjbWar(File hostDir) {
        if (!hostDir.isDirectory()) {
            return null;
        }

        // iterate over the contexts
        for (File contextDir : hostDir.listFiles()) {
            // does this war have a web-inf lib dir
            File webInfLib = new File(new File(contextDir, "WEB-INF"), "lib");
            if (!webInfLib.isDirectory()) {
                continue;
            }
            // iterate over the libs looking for the openejb-loader-*.jar
            for (File file : webInfLib.listFiles()) {
                if (file.getName().startsWith("openejb-loader-") && file.getName().endsWith(".jar")) {
                    // this should be the openejb war...
                    // make sure it has a lib directory
                    if (new File(contextDir, "lib").isDirectory()) {
                        return contextDir;
                    }
                }
            }
        }
        return null;
    }
}
