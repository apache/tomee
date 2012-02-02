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

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Bootstrap;
import org.apache.catalina.startup.Catalina;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.config.sys.Tomee;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.Loader;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ServiceManager;
import org.apache.openejb.server.ejbd.EjbServer;
import org.apache.openejb.spi.Service;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.OptionsLog;
import org.apache.tomcat.util.scan.Constants;
import org.apache.tomee.installer.Installer;
import org.apache.tomee.installer.Paths;
import org.apache.tomee.loader.TomcatHelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * <h1>Prerequisites</h1>
 * <p/>
 * System properties that must be set:
 * <ul>
 * <li/>openejb.home -> catalina.home
 * <li/>openejb.base -> catalina.base
 * <li/>tomee.war -> $tomee.war
 * <li/>tomcat.version if not set
 * <li/>tomcat.built if not set
 * </ul>
 * <p/>
 * <h1>Integration Actions</h1>
 * <p/>
 * <ul>
 * <li/>Setup ServiceJar: set openejb.provider.default -> org.apache.tomee
 * We therefore will load this file: META-INF/org.apache.openejb.tomcat/service-jar.xml
 * <li/>Init SystemInstance and OptionsLog
 * <li/>
 * <li/>
 * </ul>
 * <p/>
 * See {@link org.apache.openejb.config.ServiceUtils#DEFAULT_PROVIDER_URL}
 *
 * @version $Revision: 617255 $ $Date: 2008-01-31 13:58:36 -0800 (Thu, 31 Jan 2008) $
 */
public class TomcatLoader implements Loader {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, TomcatLoader.class);
    public static final String TOMEE_NOSHUTDOWNHOOK_PROP = "tomee.noshutdownhook";

    /**
     * OpenEJB Server Daemon
     */
    private EjbServer ejbServer;

    /**
     * OpenEJB Service Manager that manage services
     */
    private ServiceManager manager;

    /** other services */
    private List<ServerService> services = new ArrayList<ServerService> ();

    /**
     * Creates a new instance.
     */
    public TomcatLoader() {
    }

    /**
     *  {@inheritDoc}
     */
    public void init(Properties properties) throws Exception {

        // Enable System EJBs like the MEJB and DeployerEJB
        initDefaults(properties);

        // Loader maybe the first thing executed in a new classloader
        // so we must attempt to initialize the system instance.
        SystemInstance.init(properties);
        initialize(properties);
    }

    public void initDefaults(Properties properties) {
        setIfNull(properties, "openejb.deployments.classpath", "true");
        setIfNull(properties, "openejb.deployments.classpath.filter.systemapps", "false");

        //Sets default service provider
        setIfNull(properties, "openejb.provider.default", "org.apache.tomee");
    }

    public void initialize(Properties properties) throws Exception {
        //Install Log
        OptionsLog.install();

        // install conf/openejb.xml and conf/logging.properties files
        String openejbWarDir = properties.getProperty("tomee.war");
        if (openejbWarDir != null) {

            Paths paths = new Paths(new File(openejbWarDir));
            if (paths.verify()) {
                Installer installer = new Installer(paths);
                if (installer.getStatus() != Installer.Status.INSTALLED) {
                    installer.installConfigFiles();
                }
            }
        }

        // Not thread safe
        if (OpenEJB.isInitialized()) {
            ejbServer = SystemInstance.get().getComponent(EjbServer.class);
            return;
        }

        FileInputStream fin = null;
        File conf = null;
        // Read in and apply the conf/system.properties
        try {
            conf = SystemInstance.get().getBase().getDirectory("conf");

            final File tomeeXml = new File(conf, "tomee.xml");
            if (tomeeXml.exists()) { // use tomee.xml instead of openejb.xml
                SystemInstance.get().setProperty("openejb.configuration", tomeeXml.getAbsolutePath());
                SystemInstance.get().setProperty("openejb.configuration.class", Tomee.class.getName());
            }

            //Look for custom system properties
            File file = new File(conf, "system.properties");
            if (file.exists()) {
                System.out.println("Processing conf/system.properties: " + file.toString());
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

        // set ignorable libraries from a tomee property instead of using the standard openejb one
        // don't ignore standard openejb exclusions file
        final Set<String> exclusions = new HashSet<String>(Arrays.asList(NewLoaderLogic.getExclusions()));
        final File catalinaProperties = new File(conf, "catalina.properties");
        if (catalinaProperties.exists()) {
            final Properties catalinaProps = new Properties();
            catalinaProps.load(new FileInputStream(catalinaProperties));
            final String jarToSkipProp = catalinaProps.getProperty("tomcat.util.scan.DefaultJarScanner.jarsToSkip");
            if (jarToSkipProp != null) {
                for (String s : jarToSkipProp.split(",")) {
                    exclusions.add(s.trim());
                }
            }
        }
        NewLoaderLogic.setExclusions(exclusions.toArray(new String[exclusions.size()]));
        System.setProperty(Constants.SKIP_JARS_PROPERTY, Join.join(",", exclusions));

        // Install tomcat war builder
        TomcatWebAppBuilder tomcatWebAppBuilder = (TomcatWebAppBuilder) SystemInstance.get().getComponent(WebAppBuilder.class);
        if (tomcatWebAppBuilder == null) {
            tomcatWebAppBuilder = new TomcatWebAppBuilder();
            tomcatWebAppBuilder.start();
            SystemInstance.get().setComponent(WebAppBuilder.class, tomcatWebAppBuilder);
        }

        // Web Services will be installed into the WebDeploymentListeners list
        SystemInstance.get().setComponent(WebDeploymentListeners.class, new WebDeploymentListeners());

        optionalService(properties, "org.apache.tomee.webservices.TomeeJaxRsService");
        optionalService(properties, "org.apache.tomee.webservices.TomeeJaxWsService");

        // Start OpenEJB
        ejbServer = new EjbServer();
        SystemInstance.get().setComponent(EjbServer.class, ejbServer);
        OpenEJB.init(properties, new ServerFederation());

        Properties ejbServerProps = new Properties();
        ejbServerProps.putAll(properties);
        ejbServerProps.setProperty("openejb.ejbd.uri", "http://127.0.0.1:8080/tomee/ejb");
        ejbServer.init(ejbServerProps);

        // Add our naming context listener to the server which registers all Tomcat resources with OpenEJB
        StandardServer standardServer = TomcatHelper.getServer();
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
            // WS
            try {
                ServerService cxfService = (ServerService) Class.forName("org.apache.openejb.server.cxf.CxfService").newInstance();
                cxfService.start();
                services.add(cxfService);
            } catch (ClassNotFoundException ignored) {
            } catch (Exception e) {
                Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, getClass());
                logger.error("Webservices failed to start", e);
            }

            // REST
            try {
                ServerService restService = (ServerService) Class.forName("org.apache.openejb.server.cxf.rs.CxfRSService").newInstance();
                restService.start();
                services.add(restService);
            } catch (ClassNotFoundException ignored) {
            } catch (Exception e) {
                logger.error("REST failed to start", e);
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

        if (System.getProperty(TOMEE_NOSHUTDOWNHOOK_PROP) != null) {
            final Field daemonField = Bootstrap.class.getDeclaredField("daemon");
            final boolean acc = daemonField.isAccessible();
            try {
                daemonField.setAccessible(true);
                final Bootstrap daemon = (Bootstrap) daemonField.get(null);
                if (daemon != null) {
                    final Field catalinaField = Bootstrap.class.getDeclaredField("catalinaDaemon");
                    final boolean catalinaAcc = catalinaField.isAccessible();
                    catalinaField.setAccessible(true);
                    try {
                        Catalina.class.getMethod("setUseShutdownHook", boolean.class).invoke(catalinaField.get(daemon), false);
                    } finally {
                        catalinaField.setAccessible(catalinaAcc);
                    }
                }
            } finally {
                daemonField.setAccessible(acc);
            }
        }
    }

    private void optionalService(Properties properties, String className) {
        try {
            Service service = (Service) getClass().getClassLoader().loadClass(className).newInstance();
            service.init(properties);
        } catch (ClassNotFoundException e) {
            logger.info("Optional service not installed: " + className);
        } catch (Exception e) {
            logger.error("Failed to start: " + className, e);
        }
    }

    private void setIfNull(Properties properties, String key, String value) {
        if (!properties.containsKey(key)) properties.setProperty(key, value);
    }

    /**
     * Destroy system.
     */
    public void destroy() {
        for (ServerService s : services) {
            try {
                s.stop();
            } catch (ServiceException ignored) {
                // no-op
            }
        }

        //Stop ServiceManager
        if (manager != null) {
            try {
                manager.stop();
            } catch (ServiceException e) {
                // no-op
            }
            manager = null;
        }

        //Stop Ejb server
        if (ejbServer != null) {
            try {
                ejbServer.stop();
            } catch (ServiceException e) {
                // no-op
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
        for (org.apache.catalina.Service service : standardServer.findServices()) {
            if (service.getContainer() instanceof Engine) {
                Engine engine = (Engine) service.getContainer();
                for (Container engineChild : engine.findChildren()) {
                    if (engineChild instanceof Host) {
                        Host host = (Host) engineChild;
                        for (Container hostChild : host.findChildren()) {
                            if (hostChild instanceof StandardContext) {
                                StandardContext standardContext = (StandardContext) hostChild;
                                int state = TomcatHelper.getContextState(standardContext);
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
