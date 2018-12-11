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

import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The sole purpose of this class is to call the {@link TomcatEmbedder#embed} method
 * <p>
 * This is an alternate way to load the Tomcat integration
 * This approach is mutually exclussive to the {@link LoaderServlet}
 * </p>
 * <p>
 * This class does nothing more than scrape around in
 * Tomcat and look for the tomee.war so it can call the embedder
 * </p>
 * <p>
 * This class can be installed in the Tomcat server.xml as an alternate
 * way to bootstrap OpenEJB into Tomcat.  The benefit of this is that
 * OpenEJB is guaranteed to start before all webapps.
 * </p>
 */
public class OpenEJBListener implements LifecycleListener {
    private static final Logger LOGGER = Logger.getLogger(OpenEJBListener.class.getName());

    private static boolean listenerInstalled;
    private static boolean logWebappNotFound = true;

    public static boolean isListenerInstalled() {
        return listenerInstalled;
    }

    @Override
    public void lifecycleEvent(final LifecycleEvent event) {
        // only install once
        if (listenerInstalled || !Lifecycle.AFTER_INIT_EVENT.equals(event.getType())) {
            return;
        }

        try {
            File webappDir = findOpenEjbWar();
            if (webappDir == null && event.getSource() instanceof StandardServer) {
                final StandardServer server = (StandardServer) event.getSource();
                webappDir = tryToFindAndExtractWar(server);
                if (webappDir != null) { // we are using webapp startup
                    final File exploded = extractDirectory(webappDir);
                    if (exploded != null) {
                        extract(webappDir, exploded);
                    }
                    webappDir = exploded;
                    TomcatHelper.setServer(server);
                }
            }
            if (webappDir != null) {
                LOGGER.log(Level.INFO, "found the tomee webapp on {0}", webappDir.getPath());
                final Properties properties = new Properties();
                properties.setProperty("tomee.war", webappDir.getAbsolutePath());
                properties.setProperty("openejb.embedder.source", OpenEJBListener.class.getSimpleName());
                TomcatEmbedder.embed(properties, StandardServer.class.getClassLoader());
                listenerInstalled = true;
            } else if (logWebappNotFound) {
                LOGGER.info("tomee webapp not found from the listener, will try from the webapp if exists");
                logWebappNotFound = false;
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "TomEE Listener can't start OpenEJB", e);
            // e.printStackTrace(System.err);
        }
    }

    private static File extractDirectory(final File webappDir) {
        File exploded = new File(webappDir.getAbsolutePath().replace(".war", ""));
        int i = 0;
        while (exploded.exists()) {
            exploded = new File(exploded.getAbsolutePath() + "_" + i++);
        }
        return exploded;
    }

    private static File tryToFindAndExtractWar(final StandardServer source) {
        if (System.getProperties().containsKey("openejb.war")) {
            return new File(System.getProperty("openejb.war"));
        }

        for (final Service service : source.findServices()) {
            final Container container = service.getContainer();
            if (container instanceof StandardEngine) {
                final StandardEngine engine = (StandardEngine) container;
                for (final Container child : engine.findChildren()) {
                    if (child instanceof StandardHost) {
                        final StandardHost host = (StandardHost) child;
                        final File base = hostDir(System.getProperty("catalina.base"), host.getAppBase());

                        final File[] files = base.listFiles();
                        if (files != null) {
                            for (final File file : files) {
                                if (isTomEEWar(file)) {
                                    return file;
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private static boolean isTomEEWar(final File file) {
        final String name = file.getName();
        try (final JarFile jarFile = new JarFile(file)) {
            return jarFile.getEntry("lib") != null
                    && (name.startsWith("tomee") || name.startsWith("openejb")
                    && name.endsWith(".war"));
        } catch (final IOException e) {
            return false;
        }
    }

    private static File findOpenEjbWar() {
        // in Tomcat 5.5 the OpenEjb war is in the server/webapps director
        final String catalinaBase = System.getProperty("catalina.base");
        final File serverWebapps = new File(catalinaBase, "server/webapps");
        File openEjbWar = findOpenEjbWar(serverWebapps);
        if (openEjbWar != null) {
            return openEjbWar;
        }

        try {
            // in Tomcat 6 the OpenEjb war is normally in webapps, but we just
            // scan all hosts directories
            for (final Service service : TomcatHelper.getServer().findServices()) {
                final Container container = service.getContainer();
                if (container instanceof StandardEngine) {
                    final StandardEngine engine = (StandardEngine) container;
                    for (final Container child : engine.findChildren()) {
                        if (child instanceof StandardHost) {
                            final StandardHost host = (StandardHost) child;
                            final File hostDir = hostDir(catalinaBase, host.getAppBase());

                            openEjbWar = findOpenEjbWar(hostDir);
                            if (openEjbWar != null) {
                                return openEjbWar;
                            } else {
                                return findOpenEjbWar(host);
                            }
                        }
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "OpenEJBListener.findOpenEjbWar: {0}", e.getMessage());
        }

        return null;
    }

    private static File hostDir(final String catalinaBase, final String appBase) {
        File hostDir = new File(appBase);
        if (!hostDir.isAbsolute()) {
            hostDir = new File(catalinaBase, appBase);
        }
        return hostDir;
    }

    private static File findOpenEjbWar(final StandardHost standardHost) {
        //look for openejb war in a Tomcat context
        for (final Container container : standardHost.findChildren()) {
            if (container instanceof StandardContext) {
                final StandardContext standardContext = (StandardContext) container;
                File contextDocBase = new File(standardContext.getDocBase());
                if (!contextDocBase.isDirectory() && standardContext.getOriginalDocBase() != null) {
                    contextDocBase = new File(standardContext.getOriginalDocBase());
                }
                if (contextDocBase.isDirectory()) {
                    final File openEjbWar = findOpenEjbWarInContext(contextDocBase);
                    if (openEjbWar != null) {
                        return openEjbWar;
                    }
                }
            }
        }
        return null;
    }

    private static File findOpenEjbWar(final File hostDir) {
        if (!hostDir.isDirectory()) {
            return null;
        }

        // iterate over the contexts
        final File[] files = hostDir.listFiles();
        if (null != files) {
            for (final File contextDir : files) {
                final File foundContextDir = findOpenEjbWarInContext(contextDir);
                if (foundContextDir != null) {
                    return foundContextDir;
                }
            }
        }
        return null;
    }

    private static File findOpenEjbWarInContext(final File contextDir) {
        // this should be a webapp
        if (!new File(contextDir, "WEB-INF").exists()) {
            return null;
        }

        // this should be the openejb war...
        // make sure it has a lib directory
        final File webInfLib = new File(contextDir, "lib");
        if (!webInfLib.isDirectory()) {
            return null;
        }
        // iterate over the libs looking for the openejb-loader-*.jar
        final File[] files = webInfLib.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.getName().startsWith("tomee-catalina-") && file.getName().endsWith(".jar")) {
                    return contextDir;
                }
            }
        }
        return null;
    }

    // copied for classloading reason
    public static void extract(final File src, final File dest) throws IOException {
        if (dest.exists()) {
            return;
        }

        LOGGER.log(Level.INFO, "Extracting openejb webapp from {0} to {1}", new Object[]{src.getAbsolutePath(), dest.getAbsolutePath()});

        if (!dest.mkdirs()) {
            throw new IOException("Failed to create: " + dest);
        }

        JarFile jarFile = null;
        InputStream input = null;
        try {
            jarFile = new JarFile(src);
            final Enumeration jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                final JarEntry jarEntry = (JarEntry) jarEntries.nextElement();
                final String name = jarEntry.getName();
                final int last = name.lastIndexOf('/');
                if (last >= 0) {
                    final File parent = new File(dest, name.substring(0, last));
                    if (!parent.mkdirs()) {
                        throw new IOException("Failed to create: " + parent);
                    }
                }
                if (name.endsWith("/")) {
                    continue;
                }
                input = jarFile.getInputStream(jarEntry);

                final File file = new File(dest, name);
                BufferedOutputStream output = null;
                try {
                    output = new BufferedOutputStream(new FileOutputStream(file));
                    final byte[] buffer = new byte[2048];
                    while (true) {
                        final int n = input.read(buffer);
                        if (n <= 0) {
                            break;
                        }
                        output.write(buffer, 0, n);
                    }
                } finally {
                    if (output != null) {
                        try {
                            output.close();
                        } catch (final IOException e) {
                            // Ignore
                        }
                    }
                }

                final long lastModified = jarEntry.getTime();
                if (lastModified != -1 && lastModified != 0 && file != null) {
                    if (!file.setLastModified(lastModified)) {
                        LOGGER.log(Level.WARNING, "Failed to set last modified time on: {0}", file.getAbsolutePath());
                    }
                }

                input.close();
                input = null;
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (final Throwable t) {
                    // no-op
                }
            }
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (final Throwable t) {
                    // no-op
                }
            }
        }

        LOGGER.info("Extracted openejb webapp");
    }
}
