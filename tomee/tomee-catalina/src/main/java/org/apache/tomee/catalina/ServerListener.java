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
package org.apache.tomee.catalina;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.openejb.classloader.ClassLoaderConfigurer;
import org.apache.openejb.config.QuickJarsTxtParser;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomee.loader.TomcatHelper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

// this listener is the real tomee one (the OpenEJBListener is more tomcat oriented)
public class ServerListener implements LifecycleListener {
    private static final Logger LOGGER = Logger.getLogger(ServerListener.class.getName());

    private static final AtomicBoolean listenerInstalled = new AtomicBoolean(false);

    @Override
    public void lifecycleEvent(final LifecycleEvent event) {
        synchronized (listenerInstalled) {

            // only install once
            if (listenerInstalled.get() || !Lifecycle.AFTER_INIT_EVENT.equals(event.getType())) {
                return;
            }
            if (!(event.getSource() instanceof StandardServer)) {
                return;
            }

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
                final String catalinaHome = System.getProperty("catalina.home");
                properties.setProperty("openejb.home", catalinaHome);

                //Sets system property for openejb.home
                System.setProperty("openejb.home", catalinaHome);

                //get the value of catalina.base and set it to openejb.base
                final String catalinaBase = System.getProperty("catalina.base");
                properties.setProperty("openejb.base", catalinaBase);

                //Sets system property for openejb.base
                System.setProperty("openejb.base", catalinaBase);


                // System.setProperty("tomcat.version", "x.y.z.w");
                // System.setProperty("tomcat.built", "mmm dd yyyy hh:mm:ss");
                // set the System properties, tomcat.version, tomcat.built
                final ClassLoader classLoader = ServerListener.class.getClassLoader();
                try {
                    final Properties tomcatServerInfo = IO.readProperties(classLoader.getResourceAsStream("org/apache/catalina/util/ServerInfo.properties"), new Properties());

                    String serverNumber = tomcatServerInfo.getProperty("server.number");
                    if (serverNumber == null) {
                        // Tomcat5 only has server.info
                        final String serverInfo = tomcatServerInfo.getProperty("server.info");
                        if (serverInfo != null) {
                            final int slash = serverInfo.indexOf('/');
                            serverNumber = serverInfo.substring(slash + 1);
                        }
                    }
                    if (serverNumber != null) {
                        System.setProperty("tomcat.version", serverNumber);
                    }

                    final String serverBuilt = tomcatServerInfo.getProperty("server.built");
                    if (serverBuilt != null) {
                        System.setProperty("tomcat.built", serverBuilt);
                    }
                } catch (final Throwable e) {
                    // no-op
                }

                final TomcatLoader loader = new TomcatLoader();
                loader.initSystemInstance(properties);

                // manage additional libraries
                try {
                    final Collection<URL> files = new ArrayList<>();
                    for (final File f : ProvisioningUtil.addAdditionalLibraries()) {
                        files.add(f.toURI().toURL());
                    }

                    final ClassLoaderConfigurer configurer = QuickJarsTxtParser.parse(SystemInstance.get().getConf(QuickJarsTxtParser.FILE_NAME));
                    if (configurer != null) {
                        files.addAll(asList(configurer.additionalURLs()));
                    }

                    if (!files.isEmpty() && URLClassLoader.class.isInstance(classLoader)) {
                        final URLClassLoader ucl = URLClassLoader.class.cast(classLoader);
                        try {
                            final Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                            final boolean acc = addUrl.isAccessible();
                            try {
                                for (final URL url : files) {
                                    addUrl(ucl, addUrl, url);
                                }
                            } finally {
                                addUrl.setAccessible(acc);
                            }
                        } catch (final Exception e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        }
                    }
                } catch (final IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                }

                loader.initialize(properties);

                listenerInstalled.set(true);
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, "TomEE Listener can't start OpenEJB", e);
                // e.printStackTrace(System.err);
            }
        }
    }

    private static void addUrl(final URLClassLoader ucl, final Method addUrl, final URL url) throws IllegalAccessException, InvocationTargetException, MalformedURLException {
        if (!addUrl.isAccessible()) { // set it lazily
            addUrl.setAccessible(true);
        }
        addUrl.invoke(ucl, url);
    }

}
