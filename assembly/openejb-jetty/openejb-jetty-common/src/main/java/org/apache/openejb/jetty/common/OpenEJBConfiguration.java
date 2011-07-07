/**
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
package org.apache.openejb.jetty.common;

import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.InjectionBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.config.AnnotationDeployer;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ClientModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.ReadDescriptors;
import org.apache.openejb.config.UnknownModuleTypeException;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.ResourceFinder;
import org.apache.xbean.finder.UrlSet;
import org.eclipse.jetty.plus.annotation.InjectionCollection;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OpenEJBConfiguration extends AbstractConfiguration {
    private ConfigurationFactory configurationFactory;
    private Assembler assembler;
    private static final org.apache.openejb.util.Logger logger = org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB.createChild("jetty"), "org.apache.openejb.util.resources");

    public OpenEJBConfiguration() {
        configurationFactory = new ConfigurationFactory();
        assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
    }

    public void preConfigure(WebAppContext context) throws Exception {
    }

    public void configure(WebAppContext webAppContext) throws Exception {
        try {
            AppModule appModule = loadApplication(webAppContext);

            AppInfo appInfo = configurationFactory.configureApplication(appModule);
            assembler.createApplication(appInfo, webAppContext.getClassLoader());

            InitialContext context = new InitialContext();

            // add java:openejb to InitialContext
            Context openejbContext = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();
            openejbContext = (Context) openejbContext.lookup("openejb");

            context.bind("java:openejb", openejbContext);

            List<WebAppInfo> webApps = appInfo.webApps;
            for (WebAppInfo webApp : webApps) {
                InjectionBuilder injectionBuilder = new InjectionBuilder(webAppContext.getClassLoader());
                List<Injection> injections = injectionBuilder.buildInjections(webApp.jndiEnc);

                JettyJndiBuilder jndiBuilder = new JettyJndiBuilder(webAppContext, webApp);
                jndiBuilder.mergeJndi();

                for (Injection injection : injections) {

                    Class clazz = injection.getTarget();
                    String jndiName = injection.getJndiName();
                    String targetName = injection.getName();

                    InjectionCollection injectionCollection = (InjectionCollection) webAppContext.getAttribute(InjectionCollection.INJECTION_COLLECTION);
                    if (injectionCollection == null) {
                        injectionCollection = new InjectionCollection();
                        webAppContext.setAttribute(InjectionCollection.INJECTION_COLLECTION, injectionCollection);
                    }
                    Class valueClass = getValueClass(clazz, targetName);
                    if (valueClass != null) {
                        org.eclipse.jetty.plus.annotation.Injection inj = new org.eclipse.jetty.plus.annotation.Injection();
                        if (jndiName.startsWith("comp/env/")) {
                            inj.setJndiName(jndiName.substring(9));
                        } else {
                            inj.setJndiName(jndiName);
                        }

                        inj.setTarget(clazz, targetName, valueClass);
                        injectionCollection.add(inj);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Class getValueClass(Class clazz, String targetName) {
        try {
            Field field = clazz.getDeclaredField(targetName);
            if (field != null) {
                return field.getType();
            }
        } catch (Exception e) {
            // field not found, try and find a setter method instead
        }

        String setter = "set" + targetName.substring(0, 1).toUpperCase() + targetName.substring(1);
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (setter.equals(method.getName())) {
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes != null && paramTypes.length == 1) {
                    return paramTypes[0];
                }
            }
        }

        return null;
    }

    /**
     * Creates an openejb {@link AppModule} instance
     * from given tomcat context.
     *
     * @param application tomcat context instance
     * @return a openejb application module
     */
    private AppModule loadApplication(WebAppContext application) {
        // create the web module
        WebModule webModule = createWebModule(application);

        // create the app module
        AppModule appModule = new AppModule(webModule);

        // check each url to determine if it is an ejb jar
        for (URL url : getUrls(application)) {
            try {
                Class moduleType = new DeploymentLoader().discoverModuleType(url, application.getClassLoader(), true);
                if (EjbModule.class.isAssignableFrom(moduleType)) {
                    File file;
                    if (url.getProtocol().equals("jar")) {
                        url = new URL(url.getFile().replaceFirst("!.*$", ""));
                        file = URLs.toFile(url);
                    } else if (url.getProtocol().equals("file")) {
                        file = URLs.toFile(url);
                    } else {
                        logger.warning("Not loading " + moduleType.getSimpleName() + ".  Unknown protocol " + url.getProtocol());
                        continue;
                    }

                    logger.info("Found ejb module " + moduleType.getSimpleName() + " in war " + application.getWar());

                    // create the ejb module and set its moduleId to the webapp context root name
                    EjbModule ejbModule = new EjbModule(webModule.getClassLoader(), getEjbModuleId(application), file.getAbsolutePath(), null, null);
                    ejbModule.setClientModule(new ClientModule(null, ejbModule.getClassLoader(), ejbModule.getJarLocation(), null, ejbModule.getModuleId()));

                    // EJB deployment descriptors
                    try {
                        ResourceFinder ejbResourceFinder = new ResourceFinder("", application.getClassLoader(), file.toURI().toURL());
                        Map<String, URL> descriptors = ejbResourceFinder.getResourcesMap("META-INF/");
                        descriptors = DeploymentLoader.altDDSources(descriptors, true);
                        ejbModule.getAltDDs().putAll(descriptors);
                        ejbModule.getClientModule().getAltDDs().putAll(descriptors);
                    } catch (IOException e) {
                        logger.error("Unable to determine descriptors in jar.", e);
                    }

                    // add module to app
                    appModule.getEjbModules().add(ejbModule);
                }
            } catch (IOException e) {
                logger.warning("Unable to determine the module type of " + url.toExternalForm() + ": Exception: " + e.getMessage(), e);
            } catch (UnknownModuleTypeException ignore) {
                ignore.printStackTrace();
            }

        }

        // Persistence Units via META-INF/persistence.xml
        try {
            ResourceFinder finder = new ResourceFinder("", application.getClassLoader());
            List<URL> persistenceUrls = finder.findAll("META-INF/persistence.xml");
            appModule.getAltDDs().put("persistence.xml", persistenceUrls);
        } catch (IOException e) {
            logger.warning("Cannot load persistence-units from 'META-INF/persistence.xml' : " + e.getMessage(), e);
        }

        return appModule;
    }

    private String getEjbModuleId(WebAppContext application) {
        String ejbModuleId = application.getServletContext().getContextPath();
        if (ejbModuleId.startsWith("/")) ejbModuleId = ejbModuleId.substring(1);
        return ejbModuleId;
    }

    private WebModule createWebModule(WebAppContext application) {
        // todo replace this code with DeploymentLoader
        ServletContext servletContext = application.getServletContext();

        // read the web.xml
        WebApp webApp = new WebApp();
        try {
            URL webXmlUrl = servletContext.getResource("/WEB-INF/web.xml");
            if (webXmlUrl != null) {
                webApp = ReadDescriptors.readWebApp(webXmlUrl);
            }
        } catch (Exception e) {
            logger.error("Unable to load web.xml in war " + application.getWar() + ": Exception: " + e.getMessage(), e);
        }

        // create the web module
        File context = new File(servletContext.getRealPath("."));
        String basePath = context.getParentFile().getAbsolutePath();
        ClassLoader classLoader = application.getClassLoader();//ClassLoaderUtil.createTempClassLoader(application.getClassLoader());
        System.out.println("context path = " + application);

        WebModule webModule = new WebModule(webApp, application.getWar(), classLoader, basePath, getId(application));
        URL[] webappUrls = DeploymentLoader.getWebappUrls(context);
        webModule.setUrls(Arrays.asList(webappUrls));

        // process the annotations
        try {
            AnnotationDeployer annotationDeployer = new AnnotationDeployer();
            annotationDeployer.deploy(webModule);
        } catch (OpenEJBException e) {
            logger.error("Unable to process annotation in " + application.getWar() + ": Exception: " + e.getMessage(), e);
        }


        return webModule;
    }

    private String getId(WebAppContext application) {
        return application.getServletContext().getContextPath();
    }

    private List<URL> getUrls(WebAppContext standardContext) {
        List<URL> urls = null;
        try {
            ClassLoader classLoader = standardContext.getClassLoader();
            UrlSet urlSet = new UrlSet(classLoader);
            urlSet = urlSet.exclude(classLoader.getParent());
            urls = urlSet.getUrls();
        } catch (IOException e) {
            logger.warning("Unable to determine URLs in web application " + standardContext.getWar(), e);
        }
        return urls;
    }

}
