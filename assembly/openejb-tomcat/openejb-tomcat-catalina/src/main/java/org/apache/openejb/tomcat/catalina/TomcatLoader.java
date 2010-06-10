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
package org.apache.openejb.tomcat.catalina;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.Loader;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ServiceManager;
import org.apache.openejb.server.ejbd.EjbServer;
import org.apache.openejb.server.webservices.WsRegistry;
import org.apache.openejb.tomcat.installer.Installer;
import org.apache.openejb.tomcat.installer.Paths;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.OptionsLog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <h1>Prerequisites</h1>
 * <p/>
 * System properties that must be set:
 * <ul>
 * <li/>openejb.home -> catalina.home
 * <li/>openejb.base -> catalina.base
 * <li/>openejb.war -> $openejb.war
 * <li/>tomcat.version if not set
 * <li/>tomcat.built if not set
 * </ul>
 * <p/>
 * <h1>Integration Actions</h1>
 * <p/>
 * <ul>
 * <li/>Setup ServiceJar: set openejb.provider.default -> org.apache.openejb.tomcat
 * We therefore will load this file: META-INF/org.apache.openejb.tomcat/service-jar.xml
 * <li/>Init SystemInstance and OptionsLog
 * <li/>
 * <li/>
 * </ul>
 * <p/>
 * See {@link org.apache.openejb.config.ServiceUtils#defaultProviderURL}
 *
 * @version $Revision: 617255 $ $Date: 2008-01-31 13:58:36 -0800 (Thu, 31 Jan 2008) $
 */
public class TomcatLoader implements Loader {

    /**
     * OpenEJB Server Daemon
     */
    private EjbServer ejbServer;

    /**
     * OpenEJB Service Manager that manage services
     */
    private ServiceManager manager;

    /**
     * Platform OpenEJB works
     */
    private final String platform;

    /**
     * Creates a new instance.
     */
    public TomcatLoader() {
        platform = "tomcat";
    }

    /**
     *  {@inheritDoc}
     */
    public void init(Properties properties) throws Exception {

        // Enable System EJBs like the MEJB and DeployerEJB
        properties.setProperty("openejb.deployments.classpath", "true");
        properties.setProperty("openejb.deployments.classpath.filter.systemapps", "false");

        //Sets default service provider
        System.setProperty("openejb.provider.default", "org.apache.openejb." + platform);

        // Loader maybe the first thing executed in a new classloader
        // so we must attempt to initialize the system instance.
        SystemInstance.init(properties);

        //Install Log
        OptionsLog.install();

        // install conf/openejb.xml and conf/logging.properties files
        String openejbWarDir = properties.getProperty("openejb.war");
        if (openejbWarDir != null) {

            Paths paths = new Paths(new File(openejbWarDir));
            if (paths.verify()) {
                Installer installer = new Installer(paths);
                installer.installConfigFiles();
            }
        }

        // Not thread safe
        if (OpenEJB.isInitialized()) {
            ejbServer = SystemInstance.get().getComponent(EjbServer.class);
            return;
        }

        FileInputStream fin = null;
        // Read in and apply the conf/system.properties
        try {
            File conf = SystemInstance.get().getBase().getDirectory("conf");

            //Look for custom system properties
            File file = new File(conf, "system.properties");
            if (file.exists()) {
                Properties systemProperties = new Properties();
                fin = new FileInputStream(file);
                InputStream in = new BufferedInputStream(fin);
                systemProperties.load(in);
                System.getProperties().putAll(systemProperties);
                // store the system properties inside SystemInstance otherwise we will lose these properties.
                // i.e. any piece of code which is trying to look for properties inside SystemInstance will not be able to find it.
                SystemInstance.get().getProperties().putAll(systemProperties);
            }
        } catch (IOException e) {
            System.out.println("Processing conf/system.properties failed: " + e.getMessage());
        } finally {
            if (fin != null) {
                fin.close();
            }
        }

        //Those are set by TomcatHook, why re-set here???
        System.setProperty("openejb.home", SystemInstance.get().getHome().getDirectory().getAbsolutePath());
        System.setProperty("openejb.base", SystemInstance.get().getBase().getDirectory().getAbsolutePath());

        // Install tomcat thread context listener
        ThreadContext.addThreadContextListener(new TomcatThreadContextListener());

        // Install tomcat war builder
        TomcatWebAppBuilder tomcatWebAppBuilder = (TomcatWebAppBuilder) SystemInstance.get().getComponent(WebAppBuilder.class);
        if (tomcatWebAppBuilder == null) {
            tomcatWebAppBuilder = new TomcatWebAppBuilder();
            tomcatWebAppBuilder.start();
            SystemInstance.get().setComponent(WebAppBuilder.class, tomcatWebAppBuilder);
        }

        // Install the Tomcat webservice registry
        TomcatWsRegistry tomcatSoapHandler = (TomcatWsRegistry) SystemInstance.get().getComponent(WsRegistry.class);
        if (tomcatSoapHandler == null) {
            tomcatSoapHandler = new TomcatWsRegistry();
            SystemInstance.get().setComponent(WsRegistry.class, tomcatSoapHandler);
        }

        // Start OpenEJB
        ejbServer = new EjbServer();
        SystemInstance.get().setComponent(EjbServer.class, ejbServer);
        OpenEJB.init(properties, new ServerFederation());

        Properties ejbServerProps = new Properties();
        ejbServerProps.putAll(properties);
        ejbServerProps.setProperty("openejb.ejbd.uri", "http://127.0.0.1:8080/openejb/ejb");
        ejbServer.init(ejbServerProps);

        // Add our naming context listener to the server which registers all Tomcat resources with OpenEJB
        StandardServer standardServer = (StandardServer) ServerFactory.getServer();
        OpenEJBNamingContextListener namingContextListener = new OpenEJBNamingContextListener(standardServer);
        // Standard server has no state property, so we check global naming context to determine if server is started yet
        if (standardServer.getGlobalNamingContext() != null) {
            namingContextListener.start();
        }
        standardServer.addLifecycleListener(namingContextListener);

        // Process all applications already started.  This deploys EJBs, PersistenceUnits
        // and modifies JNDI ENC references to OpenEJB managed objects such as EJBs.
        processRunningApplications(tomcatWebAppBuilder, standardServer);

        if (Boolean.getBoolean("openejb.servicemanager.enabled")) {
            manager = ServiceManager.getManager();
            manager.init();
            manager.start(false);
        } else {
            try {
                ServerService serverService = (ServerService) Class.forName("org.apache.openejb.server.cxf.CxfService").newInstance();
                serverService.start();
            } catch (ClassNotFoundException ignored) {
            } catch (Exception e) {
                Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, getClass());
                logger.error("Webservices failed to start", e);
            }
        }

        standardServer.addLifecycleListener(new LifecycleListener() {
            public void lifecycleEvent(LifecycleEvent event) {
                String type = event.getType();
                if (Lifecycle.AFTER_STOP_EVENT.equals(type)) {
                    TomcatLoader.this.destroy();
                }
            }
        });
    }

    /**
     * Destroy system.
     */
    public void destroy() {
        //Stop ServiceManager
        if (manager != null) {
            try {
                manager.stop();
            } catch (ServiceException e) {
            }
            manager = null;
        }

        //Stop Ejb server
        if (ejbServer != null) {
            try {
                ejbServer.stop();
            } catch (ServiceException e) {
            }
            ejbServer = null;
        }

        //Destroy OpenEJB system
        OpenEJB.destroy();
    }

    /**
     * Process running web applications for ejb deployments.
     *
     * @param tomcatWebAppBuilder tomcat web app builder instance
     * @param standardServer      tomcat server instance
     */
    private void processRunningApplications(TomcatWebAppBuilder tomcatWebAppBuilder, StandardServer standardServer) {
        for (Service service : standardServer.findServices()) {
            if (service.getContainer() instanceof Engine) {
                Engine engine = (Engine) service.getContainer();
                for (Container engineChild : engine.findChildren()) {
                    if (engineChild instanceof Host) {
                        Host host = (Host) engineChild;
                        for (Container hostChild : host.findChildren()) {
                            if (hostChild instanceof StandardContext) {
                                StandardContext standardContext = (StandardContext) hostChild;
                                int state = standardContext.getState();
                                if (state == 0) {
                                    // context only initialized
                                    tomcatWebAppBuilder.init(standardContext);
                                } else if (state == 1) {
                                    // context already started
                                    standardContext.addParameter("openejb.start.late", "true");
                                    ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
                                    Thread.currentThread().setContextClassLoader(standardContext.getLoader().getClassLoader());
                                    try {
                                        tomcatWebAppBuilder.init(standardContext);
                                        tomcatWebAppBuilder.beforeStart(standardContext);
                                        tomcatWebAppBuilder.start(standardContext);
                                        tomcatWebAppBuilder.afterStart(standardContext);
                                    } finally {
                                        Thread.currentThread().setContextClassLoader(oldCL);
                                    }
                                    standardContext.removeParameter("openejb.start.late");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
