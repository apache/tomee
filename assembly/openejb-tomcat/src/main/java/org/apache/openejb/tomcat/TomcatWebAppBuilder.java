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
import org.apache.catalina.Container;
import org.apache.catalina.util.DefaultAnnotationProcessor;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.naming.ContextAccessController;
import org.apache.naming.ContextBindings;
import org.apache.naming.NamingContext;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.JndiEncBuilder;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.ReadDescriptors;
import org.apache.openejb.config.UnknownModuleTypeException;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openjpa.lib.util.TemporaryClassLoader;
import org.apache.xbean.finder.ResourceFinder;
import org.apache.xbean.finder.UrlSet;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;
import java.util.Stack;

public class TomcatWebAppBuilder implements WebAppBuilder, ContextListener {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("tomcat"), "org.apache.openejb.util.resources");

    private final Map<String, StandardContext> contexts = new LinkedHashMap<String, StandardContext>();
    private final Map<String, Context> encs = new LinkedHashMap<String, Context>();
    private final GlobalListenerSupport globalListenerSupport;
    private final ConfigurationFactory configurationFactory;
    private Assembler assembler;

    public TomcatWebAppBuilder() {
        StandardServer standardServer = (StandardServer) ServerFactory.getServer();
        standardServer.addLifecycleListener(new OpenEJBNamingContextListener(standardServer));

        globalListenerSupport = new GlobalListenerSupport(standardServer, this);

        // MBeanServer mbeanServer;
        // List mbeanServers = MBeanServerFactory.findMBeanServer(null);
        // if (mbeanServers.size() > 0) {
        //     mbeanServer = (MBeanServer) mbeanServers.get(0);
        // } else {
        //     mbeanServer = MBeanServerFactory.createMBeanServer();
        // }

        configurationFactory = new ConfigurationFactory();
        assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
    }

    public void start() {
        globalListenerSupport.start();
    }

    public void stop() {
        globalListenerSupport.stop();
    }

    //
    // OpenEJB WebAppBuilder
    //

    public void deploy(WebAppInfo webAppInfo) throws Exception {
        StandardContext standardContext = contexts.get(webAppInfo.contextRoot);

        if (standardContext != null) {
            JndiEncBuilder jndiEncBuilder = new JndiEncBuilder(webAppInfo.jndiEnc, webAppInfo.moduleId);
            jndiEncBuilder.setUseCrossClassLoaderRef(false);
            Context enc = (Context) jndiEncBuilder.build().lookup("env");
            bindEnc(standardContext, enc);
        }
    }

    public void undeploy(WebAppInfo webAppInfo) throws Exception {
        StandardContext standardContext = contexts.get(webAppInfo.contextRoot);
        if (standardContext != null) {
            unbindEnc(standardContext);
        }
    }

    //
    // Tomcat Listener
    //

    public void init(StandardContext standardContext) {
        // turn off Tomcat's naming system
        standardContext.setUseNaming(false);
    }

    public void beforeStart(StandardContext standardContext) {
    }

    // context class loader is now defined, but no classes should have been loaded
    public void start(StandardContext standardContext) {

        Assembler assembler = getAssembler();
        if (assembler == null) {
            logger.warning("OpenEJB has not been initialized so war will not be scanned for nested modules " + standardContext.getPath());
        }

        ClassLoader classLoader = standardContext.getLoader().getClassLoader();
        ServletContext servletContext = standardContext.getServletContext();
        AppModule appModule = loadApplication(servletContext, classLoader);

        if (appModule != null) {
            try {
                contexts.put(servletContext.getContextPath(), standardContext);
                AppInfo appInfo = configurationFactory.configureApplication(appModule);
                assembler.createApplication(appInfo, classLoader);
            } catch (Exception e) {
                logger.error("Unable to deploy collapsed ear in war " + standardContext.getPath() + ": Exception: " + e.getMessage(), e);
            }
        }
    }

    public void afterStart(StandardContext standardContext) {
        Context enc = encs.get(standardContext.getPath());
        standardContext.setAnnotationProcessor(new DefaultAnnotationProcessor(enc));
    }

    public void beforeStop(StandardContext standardContext) {
    }

    public void stop(StandardContext standardContext) {
    }

    public void afterStop(StandardContext standardContext) {
        contexts.remove(standardContext.getPath());
    }

    public void destroy(StandardContext standardContext) {
    }

    private AppModule loadApplication(ServletContext servletContext, ClassLoader classLoader) {
        // read the web.xml
        WebApp webApp = new WebApp();
        try {
            URL webXmlUrl = servletContext.getResource("/WEB-INF/web.xml");
            if (webXmlUrl != null) {
                webApp = ReadDescriptors.readWebApp(webXmlUrl);
            }
        } catch (Exception e) {
            logger.error("Unable to load web.xml in war " + servletContext.getContextPath() + ": Exception: " + e.getMessage(), e);
        }

        String basePath = servletContext.getRealPath(".");

        List<URL> urls = null;
        try {
            UrlSet urlSet = new UrlSet(classLoader);
            urlSet = urlSet.exclude(classLoader.getParent());
            urls = urlSet.getUrls();
        } catch (IOException e) {
            logger.warning("Unable to determine URLs in web application " + basePath, e);
        }

        // create the app module
        ClassLoader appClassLoader = new TemporaryClassLoader(classLoader);
        AppModule appModule = new AppModule(appClassLoader, basePath);

        // add the web module itself
        WebModule webModule = new WebModule(webApp, servletContext.getContextPath(), classLoader, basePath, null);
        appModule.getWebModules().add(webModule);

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

                    logger.info("Found ejb module " + moduleType.getSimpleName() + " in war " + basePath);

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

    //
    // helper methods
    //

    private void bindEnc(StandardContext standardContext, Context enc) {
        Context rootContext = null;
        try {
            rootContext = new NamingContext(new Hashtable(), getNamingContextName(standardContext));
            Context compCtx = rootContext.createSubcontext("comp");
            compCtx.bind("env", enc);
        } catch (NamingException e) {
            // Never happens
        }

        // Add enc to global map of named contexts
        ContextAccessController.setSecurityToken(standardContext.getName(), standardContext);
        ContextBindings.bindContext(standardContext, rootContext, standardContext);
        if( logger.isDebugEnabled() ) {
            logger.debug("Bound enc for " + standardContext);
        }

        // Binding the naming context to the class loader
        try {
            ContextBindings.bindClassLoader(standardContext, standardContext, standardContext.getLoader().getClassLoader());
        } catch (NamingException e) {
            logger.error("Unable to bind enc for " + standardContext.getPath(), e);
        }

        encs.put(standardContext.getPath(), enc);
    }

    private void unbindEnc(StandardContext standardContext) {
        encs.remove(standardContext.getPath());

        ContextBindings.unbindContext(standardContext, standardContext);

        ContextBindings.unbindClassLoader(standardContext, standardContext, standardContext.getLoader().getClassLoader());

        ContextAccessController.unsetSecurityToken(standardContext.getName(), standardContext);
    }

    private Assembler getAssembler() {
        if (assembler == null) {
            assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
        }
        return assembler;
    }

    private String getNamingContextName(StandardContext standardContext) {
        Container parent = standardContext.getParent();
        if (parent == null) {
            return standardContext.getName();
        } else {
            Stack<String> stk = new Stack<String>();
            StringBuffer buff = new StringBuffer();
            while (parent != null) {
                stk.push(parent.getName());
                parent = parent.getParent();
            }
            while (!stk.empty()) {
                buff.append("/").append(stk.pop());
            }
            buff.append(standardContext.getName());
            return buff.toString();
        }
    }
}
