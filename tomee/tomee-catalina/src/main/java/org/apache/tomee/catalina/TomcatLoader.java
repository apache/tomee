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

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Bootstrap;
import org.apache.catalina.startup.Catalina;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.WebAppDeployer;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.classloader.WebAppEnricher;
import org.apache.openejb.component.ClassLoaderEnricher;
import org.apache.openejb.config.ConfigUtils;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.config.sys.Tomee;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.Loader;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ServiceManager;
import org.apache.openejb.server.ejbd.EjbServer;
import org.apache.openejb.spi.Service;
import org.apache.openejb.util.OptionsLog;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.tomcat.util.file.Matcher;
import org.apache.tomee.catalina.deployment.TomcatWebappDeployer;
import org.apache.tomee.installer.Installer;
import org.apache.tomee.installer.Paths;
import org.apache.tomee.installer.Status;
import org.apache.tomee.loader.TomcatHelper;
import org.apache.xbean.finder.filter.Filter;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <h1>Prerequisites</h1>
 * System properties that must be set:
 * <ul>
 * <li>openejb.home -> catalina.home</li>
 * <li>openejb.base -> catalina.base</li>
 * <li>tomee.war -> $tomee.war</li>
 * <li>tomcat.version if not set</li>
 * <li>tomcat.built if not set</li>
 * </ul>
 * <h1>Integration Actions</h1>
 * <ul>
 * <li>Setup ServiceJar: set openejb.provider.default -> org.apache.tomee
 * We therefore will load this file: META-INF/org.apache.openejb.tomcat/service-jar.xml</li>
 * <li>Init SystemInstance and OptionsLog</li>
 * </ul>
 * See {@link org.apache.openejb.config.ServiceUtils#DEFAULT_PROVIDER_URL}
 *
 * @version $Revision: 617255 $ $Date: 2008-01-31 13:58:36 -0800 (Thu, 31 Jan 2008) $
 */
public class TomcatLoader implements Loader {
    private static final Logger LOGGER = Logger.getLogger(TomcatLoader.class.getName());
    public static final String TOMEE_NOSHUTDOWNHOOK_PROP = "tomee.noshutdownhook";

    /**
     * OpenEJB Server Daemon
     */
    private static EjbServer ejbServer;

    /**
     * OpenEJB Service Manager that manage services
     */
    private static ServiceManager manager;

    /**
     * other services
     */
    private static final List<ServerService> services = new ArrayList<>();

    private static final TomcatThreadContextListener threadContextListener = new TomcatThreadContextListener();

    /**
     * this method will be split in two to be able to use SystemInstance in between both invocations
     * ie use configuration before effective boot
     */
    @Deprecated
    public void init(final Properties properties) throws Exception {
        initSystemInstance(properties);
        initialize(properties);
    }

    public void initSystemInstance(final Properties properties) throws Exception {
        // Enable System EJBs like the MEJB and DeployerEJB
        initDefaults(properties);

        // Loader maybe the first thing executed in a new classloader
        // so we must attempt to initialize the system instance.
        SystemInstance.init(properties);
    }

    public void initDefaults(final Properties properties) {
        try {
            SecurityEnv.init();
        } catch (final Throwable t) {
            // ignore
        }

        setIfNull(properties, "authconfigprovider.factory", "false");
        setIfNull(properties, "openejb.deployments.classpath", "false");
        setIfNull(properties, "openejb.deployments.classpath.filter.systemapps", "false");

        //Sets default service provider
        setIfNull(properties, "openejb.provider.default", "org.apache.tomee");
    }

    public void initialize(final Properties properties) throws Exception {
        Warmup.warmup(); // better than static (we are sure we don't hit it too eagerly) and doesn't cost more since uses static block

        //Install Log
        OptionsLog.install();

        // install conf/openejb.xml and conf/logging.properties files
        final String openejbWarDir = properties.getProperty("tomee.war");
        if (openejbWarDir != null) {

            final Paths paths = new Paths(new File(openejbWarDir));
            if (paths.verify()) {
                final Installer installer = new Installer(paths);
                if (installer.getStatus() != Status.INSTALLED) {
                    installer.installConfigFiles(false);
                }
            }
        }

        // Not thread safe
        if (OpenEJB.isInitialized()) {
            ejbServer = SystemInstance.get().getComponent(EjbServer.class);
            return;
        }

        final File conf = new File(SystemInstance.get().getBase().getDirectory(), "conf");
        for (final String possibleTomeePaths : ConfigUtils.deducePaths("tomee.xml")) {
            final File tomeeXml = new File(conf, possibleTomeePaths);
            if (tomeeXml.exists()) { // use tomee.xml instead of openejb.xml
                SystemInstance.get().setProperty("openejb.configuration", tomeeXml.getAbsolutePath());
                SystemInstance.get().setProperty("openejb.configuration.class", Tomee.class.getName());
            }
        }

        // set tomcat pool
        try {// in embedded mode we can easily remove it so check we can use it before setting it
            final Class<?> creatorClass = TomcatLoader.class.getClassLoader().loadClass("org.apache.tomee.jdbc.TomEEDataSourceCreator");
            SystemInstance.get().setProperty(ConfigurationFactory.OPENEJB_JDBC_DATASOURCE_CREATOR, creatorClass.getName());
        } catch (final Throwable ignored) {
            // will use the defaul tone
        }

        // tomcat default behavior is webapp, simply keep it, it is overridable by system property too
        SystemInstance.get().setProperty("openejb.default.deployment-module", System.getProperty("openejb.default.deployment-module", "org.apache.openejb.config.WebModule"));

        //Those are set by TomcatHook, why re-set here???
        System.setProperty("openejb.home", SystemInstance.get().getHome().getDirectory().getAbsolutePath());
        System.setProperty("openejb.base", SystemInstance.get().getBase().getDirectory().getAbsolutePath());

        // Install tomcat thread context listener
        ThreadContext.addThreadContextListener(threadContextListener);

        // set ignorable libraries from a tomee property instead of using the standard openejb one
        // don't ignore standard openejb exclusions file
        final Class<?> scanner = Class.forName("org.apache.tomcat.util.scan.StandardJarScanFilter", true, TomcatLoader.class.getClassLoader());
        final Set<String> forcedScanJar = Set.class.cast(Reflections.get(scanner, null, "defaultScanSet"));
        final Set<String> forcedSkipJar = Set.class.cast(Reflections.get(scanner, null, "defaultSkipSet"));
        NewLoaderLogic.addAdditionalCustomFilter(
                forcedSkipJar.isEmpty() ? null : new TomcatToXbeanFilter(forcedSkipJar),
                forcedScanJar.isEmpty() ? null : new TomcatToXbeanFilter(forcedScanJar));
        // now we use the default tomcat filter so no need to do it
        // System.setProperty(Constants.SKIP_JARS_PROPERTY, Join.join(",", exclusions));

        // Install tomcat war builder
        TomcatWebAppBuilder tomcatWebAppBuilder = (TomcatWebAppBuilder) SystemInstance.get().getComponent(WebAppBuilder.class);
        if (tomcatWebAppBuilder == null) {
            tomcatWebAppBuilder = new TomcatWebAppBuilder();
            tomcatWebAppBuilder.start();
            SystemInstance.get().setComponent(WebAppBuilder.class, tomcatWebAppBuilder);
        }
        SystemInstance.get().setComponent(ParentClassLoaderFinder.class, tomcatWebAppBuilder);

        // set webapp deployer reusing tomcat deployer instead of our custom deployer for war
        SystemInstance.get().setComponent(WebAppDeployer.class, new TomcatWebappDeployer());

        // for compatibility purpose, no more used normally by our trunk
        SystemInstance.get().setComponent(WebDeploymentListeners.class, new WebDeploymentListeners());

        optionalService(properties, "org.apache.tomee.microprofile.TomEEMicroProfileService");

        // tomee webapp enricher
        final TomEEClassLoaderEnricher classLoaderEnricher = new TomEEClassLoaderEnricher();
        SystemInstance.get().setComponent(WebAppEnricher.class, classLoaderEnricher);

        // add common lib even in ear "lib" part (if the ear provides myfaces for instance)

        final ClassLoaderEnricher enricher = SystemInstance.get().getComponent(ClassLoaderEnricher.class);
        if (null != enricher) {
            for (final URL url : classLoaderEnricher.enrichment(null)) { // we rely on the fact we know what the impl does with null but that's fine
                enricher.addUrl(url);
            }
        }

        // optional services
        if (optionalService(properties, "org.apache.tomee.webservices.TomeeJaxRsService")) {
            // in embedded mode we use regex, in tomcat we use tomcat servlet mapping
            SystemInstance.get().setProperty("openejb.rest.wildcard", "*");
        }
        optionalService(properties, "org.apache.tomee.webservices.TomeeJaxWsService");

        // Start OpenEJB
        ejbServer = new EjbServer();
        SystemInstance.get().setComponent(EjbServer.class, ejbServer);
        OpenEJB.init(properties, new ServerFederation());
        TomcatJndiBuilder.importOpenEJBResourcesInTomcat(SystemInstance.get().getComponent(OpenEjbConfiguration.class).facilities.resources, TomcatHelper.getServer());

        final Properties ejbServerProps = new Properties();
        ejbServerProps.putAll(properties);
        for (final String prop : new String[]{"serializer", "gzip"}) { // ensure -Dejbd.xxx are read
            final String value = SystemInstance.get().getProperty("ejbd." + prop);
            if (value != null) {
                ejbServerProps.put(prop, value);
            }
        }
        ejbServerProps.setProperty("openejb.ejbd.uri", "http://127.0.0.1:8080/tomee/ejb");
        ejbServer.init(ejbServerProps);

        // Add our naming context listener to the server which registers all Tomcat resources with OpenEJB
        final StandardServer standardServer = TomcatHelper.getServer();
        final OpenEJBNamingContextListener namingContextListener = new OpenEJBNamingContextListener(standardServer);
        // Standard server has no state property, so we check global naming context to determine if server is started yet
        if (standardServer.getGlobalNamingContext() != null) {
            namingContextListener.start();
        }
        standardServer.addLifecycleListener(namingContextListener);

        // Process all applications already started.  This deploys EJBs, PersistenceUnits
        // and modifies JNDI ENC references to OpenEJB managed objects such as EJBs.
        processRunningApplications(tomcatWebAppBuilder, standardServer);

        final ClassLoader cl = TomcatLoader.class.getClassLoader();
        if (SystemInstance.get().getOptions().get("openejb.servicemanager.enabled", true)) {
            final String clazz = SystemInstance.get().getOptions().get("openejb.service.manager.class", (String) null);
            try {
                manager = clazz == null ? new TomEEServiceManager() : (ServiceManager) cl.loadClass(clazz).newInstance();
            } catch (final ClassNotFoundException cnfe) {
                LOGGER.severe("can't find the service manager " + clazz + ", the TomEE one will be used");
                manager = new TomEEServiceManager();
            }
            manager.init();
            manager.start(false);
        } else {
            // WS
            try {
                final ServerService cxfService = (ServerService) cl.loadClass("org.apache.openejb.server.cxf.CxfService").newInstance();
                cxfService.init(properties);
                cxfService.start();
                services.add(cxfService);
            } catch (final ClassNotFoundException ignored) {
                // no-op
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, "Webservices failed to start", e);
            }

            // REST
            try {
                final ServerService restService = (ServerService) cl.loadClass("org.apache.openejb.server.cxf.rs.CxfRSService").newInstance();
                restService.init(properties);
                restService.start();
                services.add(restService);
            } catch (final ClassNotFoundException ignored) {
                // no-op
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, "REST failed to start", e);
            }
        }

        if (SystemInstance.get().getOptions().get(TOMEE_NOSHUTDOWNHOOK_PROP, (String) null) != null) {
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

    private boolean optionalService(final Properties properties, final String className) {
        try {
            final Service service = (Service) getClass().getClassLoader().loadClass(className).newInstance();
            service.init(properties);
            return true;
        } catch (final ClassNotFoundException e) {
            // no-op: logger.info("Optional service not installed: " + className);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start: " + className, e);
        }
        return false;
    }

    private void setIfNull(final Properties properties, final String key, final String value) {
        if (!properties.containsKey(key) && !System.getProperties().containsKey(key)) {
            properties.setProperty(key, value);
        }
    }

    /**
     * Destroy system.
     */
    public static void destroy() {
        for (final ServerService s : services) {
            try {
                s.stop();
            } catch (final ServiceException ignored) {
                // no-op
            }
        }

        //Stop ServiceManager
        if (manager != null) {
            try {
                manager.stop();
            } catch (final ServiceException e) {
                // no-op
            }
            manager = null;
        }

        //Stop Ejb server
        if (ejbServer != null) {
            try {
                ejbServer.stop();
            } catch (final ServiceException e) {
                // no-op
            }
            ejbServer = null;
        }

        final TomcatWebAppBuilder tomcatWebAppBuilder = (TomcatWebAppBuilder) SystemInstance.get().getComponent(WebAppBuilder.class);
        if (tomcatWebAppBuilder != null) {
            try {
                tomcatWebAppBuilder.stop();
            } catch (final Exception ignored) {
                // no-op
            }
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
    private void processRunningApplications(final TomcatWebAppBuilder tomcatWebAppBuilder, final StandardServer standardServer) {
        for (final org.apache.catalina.Service service : standardServer.findServices()) {
            if (service.getContainer() instanceof Engine) {
                final Engine engine = (Engine) service.getContainer();
                for (final Container engineChild : engine.findChildren()) {
                    if (engineChild instanceof Host) {
                        final Host host = (Host) engineChild;
                        for (final Container hostChild : host.findChildren()) {
                            if (hostChild instanceof StandardContext) {
                                final StandardContext standardContext = (StandardContext) hostChild;
                                final int state = TomcatHelper.getContextState(standardContext);
                                if (state == 0) {
                                    // context only initialized
                                    tomcatWebAppBuilder.init(standardContext);
                                } else if (state == 1) {
                                    // context already started
                                    standardContext.addParameter("openejb.start.late", "true");
                                    final ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
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

    private static final class SecurityEnv {
        private SecurityEnv() {
            // no-op
        }

        public static void init() {
            if (Security.getProperty("authconfigprovider.factory") == null) { // the API we use doesn't have the right default
                Security.setProperty("authconfigprovider.factory", "org.apache.catalina.authenticator.jaspic.AuthConfigFactoryImpl");
            }
        }
    }

    private static final class TomcatToXbeanFilter implements Filter {
        private final Set<String> entries;

        private TomcatToXbeanFilter(final Set<String> entries) {
            this.entries = entries;
        }

        @Override
        public boolean accept(final String name) {
            return Matcher.matchName(entries, name);
        }
    }
}
