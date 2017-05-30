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
package org.apache.tomee.loader;

import org.apache.catalina.Context;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.util.scan.StandardJarScanner;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Properties;

public class TomcatHelper {
    private static StandardServer server;
    private static boolean stopping;

    public static boolean isStopping() {
        return stopping;
    }

    public static void setServer(final StandardServer server) {
        TomcatHelper.server = server;
        SystemInstance.get().setComponent(Server.class, server);
    }

    public static void setStopping(final boolean stopping) {
        TomcatHelper.stopping = stopping;
    }

    public static StandardServer getServer() {
        StandardServer server = null;
        try {
            final Class<?> systemInstanceClass = Thread.currentThread().getContextClassLoader().loadClass("org.apache.openejb.loader.SystemInstance");
            final Object instance = systemInstanceClass.getDeclaredMethod("get").invoke(null);
            server = (StandardServer) systemInstanceClass.getDeclaredMethod("getComponent", Class.class).invoke(instance, StandardServer.class);
        } catch (final Exception classNotFoundException) {
            // ignored
        }
        if (server != null) {
            TomcatHelper.server = server;
            return server;
        }

        // first try to use Tomcat's ServerFactory class to give us a reference to the server

        try {
            final Class<?> tomcatServerFactory = Class.forName("org.apache.catalina.ServerFactory");
            final Method getServerMethod = tomcatServerFactory.getMethod("getServer");
            server = (StandardServer) getServerMethod.invoke(null);
        } catch (final Exception e) {
            // ignored
        }
        if (server != null) {
            TomcatHelper.server = server;
            return server;
        }

        if (TomcatHelper.server != null) { // try it before next one otherwise we depend on "Catalina" name which can change
            return TomcatHelper.server;
        }

        // if this fails, we'll try and get a reference from the platform mbean server
        try {
            final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
            server = (StandardServer) mbeanServer.getAttribute(new ObjectName("Catalina:type=Server"), "managedResource");
        } catch (final Exception e) {
            // ignored
        }

        if (server != null) {
            TomcatHelper.server = server;
            return server;
        }

        // if this still fails, that's too bad.
        return TomcatHelper.server;
    }

    public static int getContextState(final StandardContext standardContext) {
        final int state;

        try {
            final Method getStateMethod = StandardContext.class.getMethod("getState");
            final Object result = getStateMethod.invoke(standardContext);


            if (Integer.TYPE.equals(result.getClass())) {
                state = (Integer) result;
                return state;
            }

            if (result.getClass().isEnum()) {
                final Enum<?> e = (Enum<?>) result;

                if ("FAILED".equals(e.toString())) {
                    return 4;
                } else if ("STOPPING".equals(e.toString()) || "STOPPING_PREP".equals(e.toString()) || "MUST_STOP".equals(e.toString()) || "MUST_DESTROY".equals(e.toString())) {
                    return 2;
                } else if ("RUNNING".equals(e.toString()) || "STARTED".equals(e.toString())) {
                    return 1;
                } else if ("INITIALIZED".equals(e.toString())) {
                    return 0;
                }
            }
        } catch (final Exception e) {
            // no-op
        }

        // return STOPPED by default
        return 3;
    }

    /**
     * Helper method to call the correct org.apache.catalina.Realm.hasRole method based on the Tomcat version
     * @param realm
     * @param tomcatPrincipal
     * @param logicalRole
     * @return true the the principle has the specified role
     */
    public static boolean hasRole(final Realm realm, final Principal tomcatPrincipal, final String logicalRole) {
        try {
            final Method method = realm.getClass().getMethod("hasRole", Wrapper.class, Principal.class, String.class);
            return (Boolean) method.invoke(realm, null, tomcatPrincipal, logicalRole);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isTomcat7() {
        return System.getProperty("tomcat.version", "7.").startsWith("7.");
    }

    public static void configureJarScanner(final Context standardContext) {
        try { // override only if default
            final JarScanner originalJarScanner = standardContext.getJarScanner();
            if ("true".equalsIgnoreCase(SystemInstance.get().getProperty("tomee.tomcat.override.jar-scanner", "true"))
                    && !TomEEJarScanner.class.isInstance(originalJarScanner)
                    && StandardJarScanner.class.isInstance(originalJarScanner)) {
                final TomEEJarScanner jarScanner = new TomEEJarScanner();

                final Properties properties = SystemInstance.get().getProperties();
                final String scanClasspath = properties.getProperty(TomEEJarScanner.class.getName() + ".scanClassPath");
                if (scanClasspath != null) {
                    jarScanner.setScanClassPath(Boolean.parseBoolean(scanClasspath));
                }
                final String scanBootstrap = properties.getProperty(TomEEJarScanner.class.getName() + ".scanBootstrapClassPath");
                if (scanBootstrap != null) {
                    jarScanner.setScanBootstrapClassPath(Boolean.parseBoolean(scanBootstrap));
                }
                final JarScanFilter jarScanFilter = originalJarScanner.getJarScanFilter();
                if (jarScanFilter != null && Boolean.parseBoolean(properties.getProperty(TomEEJarScanner.class.getName() + ".useOriginalJarScannerFilter", "true"))) {
                    jarScanner.setJarScanFilter(jarScanFilter);
                }
                standardContext.setJarScanner(jarScanner);
            }
        } catch (final Exception e) {
            // ignore
        }
    }

}
