/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.catalina.util;


import java.io.InputStream;
import java.util.Properties;

import org.apache.tomcat.util.ExceptionUtils;


/**
 * Simple utility module to make it easy to plug in the server identifier
 * when integrating Tomcat.
 *
 * @author Craig R. McClanahan
 */
public class ServerInfo {


    // ------------------------------------------------------- Static Variables


    /**
     * The server information String with which we identify ourselves.
     */
    private static final String serverInfo;

    /**
     * The server built String.
     */
    private static final String serverBuilt;

    /**
     * The server's version number String.
     */
    private static final String serverNumber;

    static {

        String info = null;
        String built = null;
        String number = null;

        Properties props = new Properties();
        try (InputStream is = ServerInfo.class.getResourceAsStream
                ("/org/apache/catalina/util/ServerInfo.properties")) {
            props.load(is);
            info = props.getProperty("server.info");
            built = props.getProperty("server.built");
            number = props.getProperty("server.number");
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
        }
        if (info == null || info.equals("Apache Tomcat/@VERSION@")) {
            info = "Apache Tomcat/10.0.x-dev";
        }
        if (built == null || built.equals("@VERSION_BUILT@")) {
            built = "unknown";
        }
        if (number == null || number.equals("@VERSION_NUMBER@")) {
            number = "10.0.x";
        }

        // TOMEE-4014 - PATCH START
        try {
            final Class<?> systemInstanceClass = Class.forName("org.apache.openejb.loader.SystemInstance");
            final Class<?> optionsClass = Class.forName("org.apache.openejb.loader.Options");
            final Object systemInstance = systemInstanceClass.getDeclaredMethod("get").invoke(null);
            final Object optionsInstance = systemInstanceClass.getDeclaredMethod("getOptions").invoke(systemInstance);

            final boolean keepServerInfo = (boolean) optionsClass.getDeclaredMethod("get", String.class, boolean.class).invoke(optionsInstance, "tomee.keep-server-info", false);

            if (!keepServerInfo) {
                final int slash = info.indexOf('/');
                final Class<?> versionClass = Class.forName("org.apache.openejb.util.OpenEjbVersion");
                final Object instance = versionClass.getDeclaredMethod("get").invoke(null);
                final String tomeeVersion = (String) versionClass.getDeclaredMethod("getVersion").invoke(instance);

                info = info.substring(0, slash) + " (TomEE)" + info.substring(slash) + " (" + tomeeVersion + ")";
            }
        } catch (Throwable t) {
            // ignore
            t.printStackTrace();
        }
        // TOMEE-4014 - PATCH END

        serverInfo = info;
        serverBuilt = built;
        serverNumber = number;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * @return the server identification for this version of Tomcat.
     */
    public static String getServerInfo() {
        return serverInfo;
    }

    /**
     * @return the server built time for this version of Tomcat.
     */
    public static String getServerBuilt() {
        return serverBuilt;
    }

    /**
     * @return the server's version number.
     */
    public static String getServerNumber() {
        return serverNumber;
    }

    public static void main(String args[]) {
        System.out.println("Server version: " + getServerInfo());
        System.out.println("Server built:   " + getServerBuilt());
        System.out.println("Server number:  " + getServerNumber());
        System.out.println("OS Name:        " +
                System.getProperty("os.name"));
        System.out.println("OS Version:     " +
                System.getProperty("os.version"));
        System.out.println("Architecture:   " +
                System.getProperty("os.arch"));
        System.out.println("JVM Version:    " +
                System.getProperty("java.runtime.version"));
        System.out.println("JVM Vendor:     " +
                System.getProperty("java.vm.vendor"));
    }

}