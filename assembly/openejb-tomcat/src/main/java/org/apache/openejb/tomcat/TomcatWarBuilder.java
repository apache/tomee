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
package org.apache.openejb.tomcat;

import org.apache.catalina.ServerFactory;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.UnknownModuleTypeException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Logger;
import org.apache.openjpa.lib.util.TemporaryClassLoader;
import org.apache.xbean.finder.ResourceFinder;
import org.apache.xbean.finder.UrlSet;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class TomcatWarBuilder implements ContextListener {
    private static final Logger logger = Logger.getInstance("OpenEJB.tomcat", "org.apache.openejb.util.resources");

    private final StandardServer standardServer;
    private final MBeanServer mbeanServer;
    private final GlobalListenerSupport globalListenerSupport;

    private final ConfigurationFactory configurationFactory;
    private Assembler assembler;

    public TomcatWarBuilder() {
        this.standardServer = (StandardServer) ServerFactory.getServer();

        globalListenerSupport = new GlobalListenerSupport(standardServer, this);

        List mbeanServers = MBeanServerFactory.findMBeanServer(null);
        if (mbeanServers.size() > 0) {
            mbeanServer = (MBeanServer) mbeanServers.get(0);
        } else {
            mbeanServer = MBeanServerFactory.createMBeanServer();
        }

        configurationFactory = new ConfigurationFactory();
        assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
    }

    public StandardServer getStandardServer() {
        return standardServer;
    }

    public MBeanServer getMBeanServer() {
        return mbeanServer;
    }

    private Assembler getAssembler() {
        if (assembler == null) {
            assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
        }
        return assembler;
    }

    public void start() {
        globalListenerSupport.start();
    }

    public void init(StandardContext context) {
    }

    public void beforeStart(StandardContext context) {
    }

    // context class loader is now defined, but no classes should have been loaded
    public void start(StandardContext context) {

        Assembler assembler = getAssembler();
        if (assembler == null) {
            logger.warning("OpenEJB has not been initialized so war will not be scanned for nested modules " + context.getPath());
        }

        ClassLoader classLoader = context.getLoader().getClassLoader();
        AppModule appModule = load(classLoader, context.getDocBase(), context.getPath());
        if (appModule != null) {
            try {
                AppInfo appInfo = configurationFactory.configureApplication(appModule);
                assembler.createApplication(appInfo, classLoader);
            } catch (Exception e) {
                logger.error("Unable to deploy collapsed ear in war " + context.getPath() + ": Exception: " + e.getMessage(), e);
            }
        }
    }

    public void afterStart(StandardContext context) {
    }

    public void beforeStop(StandardContext context) {
    }

    public void stop(StandardContext context) {
    }

    public void afterStop(StandardContext context) {
    }

    public void destroy(StandardContext context) {
    }

    private AppModule load(ClassLoader classLoader, String docBase, String path) {
        List<URL> urls = null;
        try {
            UrlSet urlSet = new UrlSet(classLoader);
            urlSet = urlSet.exclude(classLoader.getParent());
            urls = urlSet.getUrls();
        } catch (IOException e) {
            logger.warning("Unable to determine URLs in web application " + docBase, e);
        }

        // does the war contain any libraries
        if (urls.isEmpty()) return null;

        // create the app module
        ClassLoader appClassLoader = new TemporaryClassLoader(classLoader);
        AppModule appModule = new AppModule(appClassLoader, path);

        // check each url to determine if it is an ejb jar
        for (URL url : urls) {
            try {
                Class moduleType = DeploymentLoader.discoverModuleType(url, appClassLoader, true);
                if (EjbModule.class.isAssignableFrom(moduleType)) {
                    File file;
                    if (url.getProtocol().equals("jar")) {
                        url = new URL(url.getFile().replaceFirst("!.*$", ""));
                        file = new File(url.getFile());
                    } else if (url.getProtocol().equals("file")) {
                        file = new File(url.getFile());
                    } else {
                        logger.warning("Not loading " + moduleType.getSimpleName() + ".  Unknown protocol " + url.getProtocol());
                        continue;
                    }

                    logger.info("Found ejb module " + moduleType.getSimpleName() + " in war " + docBase);

                    // creat the module
                    EjbModule ejbModule = new EjbModule(appClassLoader, file.getAbsolutePath(), null, null);

                    // EJB deployment descriptors
                    try {
                        ResourceFinder ejbResourceFinder = new ResourceFinder("", appClassLoader, file.toURL());
                        Map<String, URL> descriptors = ejbResourceFinder.getResourcesMap("META-INF/");
                        ejbModule.getAltDDs().putAll(descriptors);
                    } catch (IOException e) {
                        logger.error("Unable to determine descriptors in jar.", e);
                    }

                    // add module to app
                    appModule.getEjbModules().add(ejbModule);
                }
            } catch (IOException e) {
                logger.warning("Unable to determine the module type of " + url.toExternalForm() + ": Exception: " + e.getMessage(), e);
            } catch (UnknownModuleTypeException ignore) {
            }

        }

        // Persistence Units via META-INF/persistence.xml
        try {
            ResourceFinder finder = new ResourceFinder("", appClassLoader);
            List<URL> persistenceUrls = finder.findAll("META-INF/persistence.xml");
            appModule.getAltDDs().put("persistence.xml", persistenceUrls);
        } catch (IOException e) {
            logger.warning("Cannot load persistence-units from 'META-INF/persistence.xml' : " + e.getMessage(), e);
        }

        return appModule;
    }
}
