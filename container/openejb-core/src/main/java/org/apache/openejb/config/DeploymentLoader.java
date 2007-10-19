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
package org.apache.openejb.config;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.TemporaryClassLoader;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.Module;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.JspConfig;
import org.apache.openejb.jee.Taglib;
import org.apache.openejb.jee.TldTaglib;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.finder.ResourceFinder;
import org.apache.xbean.finder.UrlSet;
import org.xml.sax.SAXException;

import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/**
 * @version $Revision$ $Date$
 */
public class DeploymentLoader {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, "org.apache.openejb.util.resources");
    private static final Messages messages = new Messages("org.apache.openejb.util.resources");


    public AppModule load(File jarFile) throws OpenEJBException, UnsupportedModuleTypeException, UnknownModuleTypeException {
        ClassLoader classLoader = getClassLoader(jarFile);

        URL baseUrl = getFileUrl(jarFile);

        Class moduleClass = null;
        try {
            moduleClass = discoverModuleType(baseUrl, classLoader, true);
        } catch (Exception e) {
            throw new UnknownModuleTypeException("Unable to determine module type for jar: " + baseUrl.toExternalForm(), e);
        }

        if (AppModule.class.equals(moduleClass)) {

            File appDir = unpack(jarFile);

            URL appUrl = getFileUrl(appDir);

            ClassLoader tmpClassLoader = new TemporaryClassLoader(new URL[]{appUrl}, OpenEJB.class.getClassLoader());

            ResourceFinder finder = new ResourceFinder("", tmpClassLoader, appUrl);

            Map<String, URL> appDescriptors = null;
            try {
                appDescriptors = finder.getResourcesMap("META-INF");
            } catch (IOException e) {
                throw new OpenEJBException("Unable to determine descriptors in jar: " + appUrl.toExternalForm(), e);
            }

            try {

                //
                // Find all the modules using either the application xml or by searching for all .jar, .war and .rar files.
                //

                Map<String, URL> ejbModules = new HashMap<String, URL>();
                Map<String, URL> clientModules = new HashMap<String, URL>();
                Map<String, URL> resouceModules = new HashMap<String, URL>();
                Map<String, URL> webModules = new HashMap<String, URL>();
                Map<String, String> webContextRoots = new HashMap<String, String>();

                URL applicationXmlUrl = appDescriptors.get("application.xml");

                Application application;
                if (applicationXmlUrl != null) {
                    application = unmarshal(Application.class, "application.xml", applicationXmlUrl);
                    for (Module module : application.getModule()) {
                        try {
                            if (module.getEjb() != null) {
                                URL url = finder.find(module.getEjb().trim());
                                ejbModules.put(module.getEjb(), url);
                            } else if (module.getJava() != null) {
                                URL url = finder.find(module.getJava().trim());
                                clientModules.put(module.getConnector(), url);
                            } else if (module.getConnector() != null) {
                                URL url = finder.find(module.getConnector().trim());
                                resouceModules.put(module.getConnector(), url);
                            } else if (module.getWeb() != null) {
                                URL url = finder.find(module.getWeb().getWebUri().trim());
                                webModules.put(module.getWeb().getWebUri(), url);
                                webContextRoots.put(module.getWeb().getWebUri(), module.getWeb().getContextRoot());
                            }
                        } catch (IOException e) {
                            throw new OpenEJBException("Invalid path to module " + e.getMessage(), e);
                        }
                    }
                } else {
                    application = new Application();
                    HashMap<String, URL> files = new HashMap<String, URL>();
                    scanDir(appDir, files, "");
                    files.remove("META-INF/MANIFEST.MF");
                    for (Map.Entry<String, URL> entry : files.entrySet()) {
                        if (entry.getKey().startsWith("lib/")) continue;
                        if (!entry.getKey().matches(".*\\.(jar|war|rar|ear)")) continue;

                        try {
                            ClassLoader moduleClassLoader = new TemporaryClassLoader(new URL[]{entry.getValue()}, tmpClassLoader);

                            Class moduleType = discoverModuleType(entry.getValue(), moduleClassLoader, true);
                            if (EjbModule.class.equals(moduleType)) {
                                ejbModules.put(entry.getKey(), entry.getValue());
                            } else if (ClientModule.class.equals(moduleType)) {
                                clientModules.put(entry.getKey(), entry.getValue());
                            } else if (ConnectorModule.class.equals(moduleType)) {
                                resouceModules.put(entry.getKey(), entry.getValue());
                            } else if (WebModule.class.equals(moduleType)) {
                                webModules.put(entry.getKey(), entry.getValue());
                            }
                        } catch (UnsupportedOperationException e) {
                            // Ignore it as per the javaee spec EE.8.4.2 section 1.d.iiilogger.info("Ignoring unknown module type: "+entry.getKey());
                        } catch (Exception e) {
                            throw new OpenEJBException("Unable to determine the module type of " + entry.getKey() + ": Exception: " + e.getMessage(), e);
                        }
                    }
                }

                //
                // Create a class loader for the application
                //

                // lib/*
                if (application.getLibraryDirectory() == null) {
                    application.setLibraryDirectory("lib/");
                } else {
                    String dir = application.getLibraryDirectory();
                    if (!dir.endsWith("/")) application.setLibraryDirectory(dir + "/");
                }
                List<URL> extraLibs = new ArrayList<URL>();
                try {
                    Map<String, URL> libs = finder.getResourcesMap(application.getLibraryDirectory());
                    extraLibs.addAll(libs.values());
                } catch (IOException e) {
                    logger.warning("Cannot load libs from '" + application.getLibraryDirectory() + "' : " + e.getMessage(), e);
                }

                // APP-INF/lib/*
                try {
                    Map<String, URL> libs = finder.getResourcesMap("APP-INF/lib/");
                    extraLibs.addAll(libs.values());
                } catch (IOException e) {
                    logger.warning("Cannot load libs from 'APP-INF/lib/' : " + e.getMessage(), e);
                }

                // META-INF/lib/*
                try {
                    Map<String, URL> libs = finder.getResourcesMap("META-INF/lib/");
                    extraLibs.addAll(libs.values());
                } catch (IOException e) {
                    logger.warning("Cannot load libs from 'META-INF/lib/' : " + e.getMessage(), e);
                }

                // All jars nested in the Resource Adapter
                HashMap<String, URL> rarLibs = new HashMap<String, URL>();
                for (Map.Entry<String, URL> entry : resouceModules.entrySet()) {
                    try {
                        // unpack the resource adapter archive
                        File rarFile = new File(entry.getValue().getPath());
                        rarFile = unpack(rarFile);
                        entry.setValue(rarFile.toURL());

                        scanDir(appDir, rarLibs, "");
                    } catch (MalformedURLException e) {
                        throw new OpenEJBException("Malformed URL to app. " + e.getMessage(), e);
                    }
                }
                for (Iterator<Map.Entry<String, URL>> iterator = rarLibs.entrySet().iterator(); iterator.hasNext();) {
                    // remove all non jars from the rarLibs
                    Map.Entry<String, URL> fileEntry = iterator.next();
                    if (!fileEntry.getKey().endsWith(".jar")) continue;
                    iterator.remove();
                }

                List<URL> classPath = new ArrayList<URL>();
                classPath.addAll(ejbModules.values());
                classPath.addAll(clientModules.values());
                classPath.addAll(rarLibs.values());
                classPath.addAll(extraLibs);
                URL[] urls = classPath.toArray(new URL[]{});
                ClassLoader appClassLoader = new TemporaryClassLoader(urls, OpenEJB.class.getClassLoader());

                //
                // Create the AppModule and all nested module objects
                //

                AppModule appModule = new AppModule(appClassLoader, appDir.getAbsolutePath());
                appModule.getAdditionalLibraries().addAll(extraLibs);
                appModule.getAltDDs().putAll(appDescriptors);
                appModule.getWatchedResources().add(appDir.getAbsolutePath());
                if (applicationXmlUrl != null) {
                    appModule.getWatchedResources().add(applicationXmlUrl.getPath());
                }

                // EJB modules
                for (String moduleName : ejbModules.keySet()) {
                    try {
                        URL ejbUrl = ejbModules.get(moduleName);
                        File ejbFile = new File(ejbUrl.getPath());

                        Map<String, URL> descriptors = getDescriptors(ejbUrl);

                        EjbJar ejbJar = null;
                        URL ejbJarXmlUrl = descriptors.get("ejb-jar.xml");
                        if (ejbJarXmlUrl != null){
                            ejbJar = ReadDescriptors.readEjbJar(ejbJarXmlUrl);
                        }

                        EjbModule ejbModule = new EjbModule(appClassLoader, moduleName, ejbFile.getAbsolutePath(), ejbJar, null);

                        ejbModule.getAltDDs().putAll(descriptors);
                        ejbModule.getWatchedResources().add(ejbFile.getAbsolutePath());
                        if (ejbJarXmlUrl != null && "file".equals(ejbJarXmlUrl.getProtocol())) {
                            ejbModule.getWatchedResources().add(ejbJarXmlUrl.getPath());
                        }

                        appModule.getEjbModules().add(ejbModule);
                    } catch (OpenEJBException e) {
                        logger.error("Unable to load EJBs from EAR: " + appDir.getAbsolutePath() + ", module: " + moduleName + ". Exception: " + e.getMessage(), e);
                    }
                }

                // Application Client Modules
                for (String moduleName : clientModules.keySet()) {
                    try {
                        URL clientUrl = clientModules.get(moduleName);
                        File clientFile = new File(clientUrl.getPath());
                        ResourceFinder clientFinder = new ResourceFinder(clientUrl);

                        URL manifestUrl = clientFinder.find("META-INF/MANIFEST.MF");
                        InputStream is = manifestUrl.openStream();
                        Manifest manifest = new Manifest(is);
                        String mainClass = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);

                        Map<String, URL> descriptors = getDescriptors(clientUrl);

                        ApplicationClient applicationClient = null;
                        URL clientXmlUrl = descriptors.get("application-client.xml");
                        if (clientXmlUrl != null){
                            applicationClient = ReadDescriptors.readApplicationClient(clientXmlUrl);
                        }

                        ClientModule clientModule = new ClientModule(applicationClient, appClassLoader, clientFile.getAbsolutePath(), mainClass, moduleName);

                        clientModule.getAltDDs().putAll(descriptors);
                        clientModule.getWatchedResources().add(clientFile.getAbsolutePath());
                        if (clientXmlUrl != null && "file".equals(clientXmlUrl.getProtocol())) {
                            clientModule.getWatchedResources().add(clientXmlUrl.getPath());
                        }

                        appModule.getClientModules().add(clientModule);
                    } catch (Exception e) {
                        logger.error("Unable to load App Client from EAR: " + appDir.getAbsolutePath() + ", module: " + moduleName + ". Exception: " + e.getMessage(), e);
                    }
                }

                // Resource modules
                for (String moduleName : resouceModules.keySet()) {
                    try {
                        URL rarUrl = resouceModules.get(moduleName);
                        ConnectorModule connectorModule = createConnectorModule(rarUrl.getPath(), appClassLoader, moduleName);

                        appModule.getResourceModules().add(connectorModule);
                    } catch (OpenEJBException e) {
                        logger.error("Unable to load RAR: " + appDir.getAbsolutePath() + ", module: " + moduleName + ". Exception: " + e.getMessage(), e);
                    }
                }

                // Web modules
                for (String moduleName : webModules.keySet()) {
                    try {
                        URL warUrl = webModules.get(moduleName);
                        WebModule webModule = createWebModule(warUrl.getPath(), appClassLoader, webContextRoots.get(moduleName), moduleName);

                        appModule.getWebModules().add(webModule);
                    } catch (OpenEJBException e) {
                        logger.error("Unable to load WAR: " + appDir.getAbsolutePath() + ", module: " + moduleName + ". Exception: " + e.getMessage(), e);
                    }
                }

                // Persistence Units
                addPersistenceUnits(appModule, classLoader, urls);

                return appModule;

            } catch (OpenEJBException e) {
                logger.error("Unable to load EAR: " + jarFile.getAbsolutePath(), e);
                throw e;
            }

        } else if (EjbModule.class.equals(moduleClass)) {
            // read the ejb-jar.xml file
            Map<String, URL> descriptors = getDescriptors(baseUrl);
            EjbJar ejbJar = null;
            URL ejbJarXmlUrl = descriptors.get("ejb-jar.xml");
            if (ejbJarXmlUrl != null){
                ejbJar = ReadDescriptors.readEjbJar(ejbJarXmlUrl);
            }

            // create the EJB Module
            EjbModule ejbModule = new EjbModule(classLoader, jarFile.getAbsolutePath(), ejbJar, null);
            ejbModule.getAltDDs().putAll(descriptors);
            ejbModule.getWatchedResources().add(jarFile.getAbsolutePath());
            if (ejbJarXmlUrl != null && "file".equals(ejbJarXmlUrl.getProtocol())) {
                ejbModule.getWatchedResources().add(ejbJarXmlUrl.getPath());
            }

            // wrap the EJB Module with an Application Module
            AppModule appModule = new AppModule(classLoader, ejbModule.getJarLocation());
            appModule.getEjbModules().add(ejbModule);

            // Persistence Units
            addPersistenceUnits(appModule, classLoader, baseUrl);

            return appModule;
        } else if (ConnectorModule.class.equals(moduleClass)) {
            ConnectorModule connectorModule = createConnectorModule(baseUrl.getPath(), OpenEJB.class.getClassLoader(), null);

            // Wrap the resource module with an Application Module
            AppModule appModule = new AppModule(classLoader, connectorModule.getJarLocation());
            appModule.getResourceModules().add(connectorModule);

            return appModule;
        } else if (WebModule.class.equals(moduleClass)) {
            // unpack the rar file
            String moduleId = new File(baseUrl.getPath()).getName();
            WebModule webModule = createWebModule(baseUrl.getPath(), OpenEJB.class.getClassLoader(), null, moduleId);

            // Wrap the resource module with an Application Module
            AppModule appModule = new AppModule(classLoader, webModule.getJarLocation());
            appModule.getWebModules().add(webModule);

            // Persistence Units
            addPersistenceUnits(appModule, classLoader);

            return appModule;
        } else {
            throw new UnsupportedModuleTypeException("Unsupported module type: "+moduleClass.getSimpleName());
        }
    }

    private WebModule createWebModule(String warPath, ClassLoader parentClassLoader, String contextRoot, String moduleName) throws OpenEJBException {
        File warFile = new File(warPath);
        warFile = unpack(warFile);

        // read web.xml file
        Map<String, URL> descriptors = null;
        try {
            descriptors = getWebDescriptors(warFile);
        } catch (IOException e) {
            throw new OpenEJBException("Unable to determine descriptors in jar.", e);
        }

        WebApp webApp = null;
        URL webXmlUrl = descriptors.get("web.xml");
        if (webXmlUrl != null){
            webApp = ReadDescriptors.readWebApp(webXmlUrl);
        }

        // if this is a standalone module (no-context root), and webApp.getId is set then that is the module name
        if (contextRoot == null && webApp != null && webApp.getId() != null) {
            moduleName = webApp.getId();
        }

        // determine war class path
        List<URL> webClassPath = new ArrayList<URL>();
        File webInfDir = new File(warFile, "WEB-INF");
        try {
            webClassPath.add(new File(webInfDir, "classes").toURL());
        } catch (MalformedURLException e) {
            logger.warning("War path bad: " + new File(webInfDir, "classes"), e);
        }

        File libDir = new File(webInfDir, "lib");
        if (libDir.exists()) {
            for (File file : libDir.listFiles()) {
                if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")) {
                    try {
                        webClassPath.add(file.toURL());
                    } catch (MalformedURLException e) {
                        logger.warning("War path bad: " + file, e);
                    }
                }
            }
        }

        // create the class loader
        URL[] webUrls = webClassPath.toArray(new URL[]{});
        ClassLoader warClassLoader = new TemporaryClassLoader(webUrls, parentClassLoader);

        // create web module
        WebModule webModule = new WebModule(webApp, contextRoot, warClassLoader, warFile.getAbsolutePath(), moduleName);
        webModule.getAltDDs().putAll(descriptors);
        webModule.getWatchedResources().add(warPath);
        webModule.getWatchedResources().add(warFile.getAbsolutePath());
        if (webXmlUrl != null && "file".equals(webXmlUrl.getProtocol())) {
            webModule.getWatchedResources().add(webXmlUrl.getPath());
        }

        // find all tag libs
        addTagLibraries(webModule);

        return webModule;
    }

    private void addTagLibraries(WebModule webModule) throws OpenEJBException {
        Set<URL> tldLocations = new HashSet<URL>();

        // web.xml contains tag lib locations in nested jsp config elements
        File warFile = new File(webModule.getJarLocation());
        WebApp webApp = webModule.getWebApp();
        if (webApp != null) {
            for (JspConfig jspConfig : webApp.getJspConfig()) {
                for (Taglib taglib : jspConfig.getTaglib()) {
                    String location = taglib.getTaglibLocation();
                    if (!location.startsWith("/")) {
                        // this reproduces a tomcat bug
                        location = "/WEB-INF/" + location;
                    }
                    try {
                        File file = new File(warFile, location).getCanonicalFile().getAbsoluteFile();
                        if (location.endsWith(".jar")) {
                            URL url = file.toURL();
                            tldLocations.add(url);
                        } else {
                            Set<URL> urls = scanJarForTagLibs(file);
                            tldLocations.addAll(urls);
                        }
                    } catch (IOException e) {
                        logger.warning("JSP tag library location bad: " + location, e);
                    }
                }
            }
        }

        // WEB-INF/**/*.tld except in WEB-INF/classes and WEB-INF/lib
        Set<URL> urls = scanWarForTagLibs(warFile);
        tldLocations.addAll(urls);

        // Search all libs
        ClassLoader parentClassLoader = webModule.getClassLoader().getParent();
        urls = scanClassLoaderForTagLibs(parentClassLoader);
        tldLocations.addAll(urls);

        // load the tld files
        for (URL location : tldLocations) {
            TldTaglib taglib = ReadDescriptors.readTldTaglib(location);
            webModule.getTaglibs().add(taglib);
            if ("file".equals(location.getProtocol())) {
                webModule.getWatchedResources().add(location.getPath());
            }
        }
    }

    private Set<URL> scanClassLoaderForTagLibs(ClassLoader parentClassLoader) throws OpenEJBException {
        Set<URL> urls = new HashSet<URL>();
        if (parentClassLoader == null) return urls;

        UrlSet urlSet = null;
        try {
            urlSet = new UrlSet(parentClassLoader);
            urlSet = urlSet.excludeJavaEndorsedDirs();
            urlSet = urlSet.excludeJavaExtDirs();
            urlSet = urlSet.excludeJavaHome();
            urlSet = urlSet.exclude(ClassLoader.getSystemClassLoader());
        } catch (IOException e) {
            logger.warning("Error scanning class loader for JSP tag libraries", e);
        }

        for (URL url : urlSet.getUrls()) {
            if (url.getProtocol().equals("jar")) {
                try {
                    String path = url.getPath();
                    if (path.endsWith("!/")) {
                        path = path.substring(0, path.length() - 2);
                    }
                    url = new URL(path);
                } catch (MalformedURLException e) {
                    logger.warning("JSP tag library location bad: " + url.toExternalForm(), e);
                    continue;
                }
            }

            if (!url.getProtocol().equals("file")) {
                continue;
            }

            File file;
            try {
                file = new File(url.toURI());
            } catch (URISyntaxException e) {
                // Ignore, probably an unencoded char
                file = new File(url.getFile());
            }
            try {
                file = file.getCanonicalFile().getAbsoluteFile();
            } catch (IOException e) {
                logger.warning("JSP tag library location bad: " + file.getAbsolutePath(), e);
                continue;
            }

            urls.addAll(scanJarForTagLibs(file));
        }
        return urls;
    }

    private static Set<URL> scanWarForTagLibs(File war) {
        Set<URL> urls = new HashSet<URL>();

        File webInfDir = new File(war, "WEB-INF");
        if (!webInfDir.isDirectory()) return urls;


        // skip the lib and classes dir in WEB-INF
        LinkedList<File> files = new LinkedList<File>();
        for (File file : webInfDir.listFiles()) {
            if ("lib".equals(file.getName()) || "classes".equals(file.getName())) {
                continue;
            }
            files.add(file);
        }

        if (files.isEmpty()) return urls;

        // recursively scan the directories
        while(!files.isEmpty()) {
            File file = files.removeFirst();
            if (file.isDirectory()) {
                files.addAll(Arrays.asList(file.listFiles()));
            } else if (file.getName().endsWith(".tld")) {
                try {
                    file = file.getCanonicalFile().getAbsoluteFile();
                    urls.add(file.toURL());
                } catch (IOException e) {
                    logger.warning("JSP tag library location bad: " + file.getAbsolutePath(), e);
                }
            }
        }

        return urls;
    }

    private static Set<URL> scanJarForTagLibs(File file) {
        Set<URL> urls = new HashSet<URL>();

        if (!file.isFile()) return urls;

        JarFile jarFile = null;
        try {
            jarFile = new JarFile(file);

            URL jarFileUrl = new URL("jar", "", -1, file.toURL().toExternalForm() + "!/");
            for (JarEntry entry : Collections.list(jarFile.entries())) {
                String name = entry.getName();
                if (!name.startsWith("META-INF/") || !name.endsWith(".tld")) {
                    continue;
                }
                URL url = new URL(jarFileUrl, name);
                urls.add(url);
            }
        } catch (IOException e) {
            logger.warning("Error scanning jar for JSP tag libraries: " + file.getAbsolutePath(), e);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                }
            }
        }

        return urls;
    }

    private ConnectorModule createConnectorModule(String rarPath, ClassLoader parentClassLoader, String moduleId) throws OpenEJBException {
        URL baseUrl;// unpack the rar file
        File rarFile = new File(rarPath);
        rarFile = unpack(rarFile);
        baseUrl = getFileUrl(rarFile);

        // read the ra.xml file
        Map<String, URL> descriptors = getDescriptors(baseUrl);
        Connector connector = null;
        URL rarXmlUrl = descriptors.get("ra.xml");
        if (rarXmlUrl != null){
            connector = ReadDescriptors.readConnector(rarXmlUrl);
        }

        // find the nested jar files
        HashMap<String, URL> rarLibs = new HashMap<String, URL>();
        scanDir(rarFile, rarLibs, "");
        for (Iterator<Map.Entry<String, URL>> iterator = rarLibs.entrySet().iterator(); iterator.hasNext();) {
            // remove all non jars from the rarLibs
            Map.Entry<String, URL> fileEntry = iterator.next();
            if (!fileEntry.getKey().endsWith(".jar")) {
                iterator.remove();
            }
        }

        // create the class loader
        List<URL> classPath = new ArrayList<URL>();
        classPath.addAll(rarLibs.values());
        URL[] urls = classPath.toArray(new URL[]{});
        ClassLoader appClassLoader = new TemporaryClassLoader(urls, parentClassLoader);

        // create the Resource Module
        ConnectorModule connectorModule = new ConnectorModule(connector, appClassLoader, rarPath, moduleId);
        connectorModule.getAltDDs().putAll(descriptors);
        connectorModule.getLibraries().addAll(classPath);
        connectorModule.getWatchedResources().add(rarPath);
        connectorModule.getWatchedResources().add(rarFile.getAbsolutePath());
        if (rarXmlUrl != null && "file".equals(rarXmlUrl.getProtocol())) {
            connectorModule.getWatchedResources().add(rarXmlUrl.getPath());
        }

        return connectorModule;
    }

    private void addPersistenceUnits(AppModule appModule, ClassLoader classLoader, URL... urls) {
        try {
            ResourceFinder finder = new ResourceFinder("", classLoader, urls);
            List<URL> persistenceUrls = finder.findAll("META-INF/persistence.xml");
            appModule.getAltDDs().put("persistence.xml", persistenceUrls);
        } catch (IOException e) {
            logger.warning("Cannot load persistence-units from 'META-INF/persistence.xml' : " + e.getMessage(), e);
        }
    }

    private Map<String, URL> getDescriptors(URL moduleUrl) throws OpenEJBException {
        try {
            ResourceFinder finder = new ResourceFinder(moduleUrl);

            return finder.getResourcesMap("META-INF/");

        } catch (IOException e) {
            throw new OpenEJBException("Unable to determine descriptors in jar.", e);
        }
    }

    private static Map<String, URL> getWebDescriptors(File warFile) throws IOException {
        Map<String, URL> descriptors = new TreeMap<String,URL>();

        // xbean resource finder has a bug when you use any uri but "META-INF"
        // and the jar file does not contain a directory entry for the uri

        if (warFile.isFile()) {
            URL jarURL = new URL("jar", "", -1, warFile.toURL() + "!/");
            try {
                JarFile jarFile = new JarFile(warFile);
                for (JarEntry entry : Collections.list(jarFile.entries())) {
                    String entryName = entry.getName();
                    if (!entry.isDirectory() && entryName.startsWith("WEB-INF/") && entryName.indexOf('/', "WEB-INF/".length()) > 0) {
                        descriptors.put(entryName, new URL(jarURL, entry.getName()));
                    }
                }
            } catch (IOException e) {
                // most likely an invalid jar file
            }
        } else if (warFile.isDirectory()) {
            File webInfDir = new File(warFile, "WEB-INF");
            if (webInfDir.isDirectory()) {
                for (File file : webInfDir.listFiles()) {
                    if (!file.isDirectory()) {
                        descriptors.put(file.getName(), file.toURL());
                    }
                }
            }
        }

        return descriptors;
    }

    private static File getFile(URL warUrl) {
        if ("jar".equals(warUrl.getProtocol())) {
            String pathname = warUrl.getPath();

            // we only support file based jar urls
            if (!pathname .startsWith("file:")) {
                return null;
            }

            // strip off "file:"
            pathname = pathname.substring("file:".length());

            // file path has trailing !/ that must be stripped off
            pathname = pathname.substring(0, pathname.lastIndexOf('!'));
            return new File(pathname);
        } else if ("file".equals(warUrl.getProtocol())) {
            String pathname = warUrl.getPath();
            return new File(pathname);
        } else {
            return null;
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <T>T unmarshal(Class<T> type, String descriptor, URL url) throws OpenEJBException {
        try {
            return (T) JaxbJavaee.unmarshal(type, url.openStream());
        } catch (SAXException e) {
            throw new OpenEJBException("Cannot parse the " + descriptor + " file: "+ url.toExternalForm(), e);
        } catch (JAXBException e) {
            throw new OpenEJBException("Cannot unmarshall the " + descriptor + " file: "+ url.toExternalForm(), e);
        } catch (IOException e) {
            throw new OpenEJBException("Cannot read the " + descriptor + " file: "+ url.toExternalForm(), e);
        } catch (Exception e) {
            throw new OpenEJBException("Encountered unknown error parsing the " + descriptor + " file: "+ url.toExternalForm(), e);
        }
    }

    public static void scanDir(File dir, Map<String, URL> files, String path) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scanDir(file, files, path + file.getName() + "/");
            } else {
                String name = file.getName();
                try {
                    files.put(path + name, file.toURL());
                } catch (MalformedURLException e) {
                    logger.warning("EAR path bad: " + path + name, e);
                }
            }
        }
    }


    public static Class<? extends DeploymentModule> discoverModuleType(URL baseUrl, ClassLoader classLoader, boolean searchForDescriptorlessApplications) throws IOException, UnknownModuleTypeException {
        ResourceFinder finder = new ResourceFinder("", classLoader, baseUrl);

        Map<String, URL> descriptors = finder.getResourcesMap("META-INF");

        String path = baseUrl.getPath();
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        
        if (descriptors.containsKey("application.xml") || path.endsWith(".ear")) {
            return AppModule.class;
        }

        if (descriptors.containsKey("ejb-jar.xml")) {
            return EjbModule.class;
        }

        if (descriptors.containsKey("application-client.xml")) {
            return ClientModule.class;
        }

        if (descriptors.containsKey("ra.xml") || path.endsWith(".rar")) {
            return ConnectorModule.class;
        }

        Map<String, URL> webDescriptors = getWebDescriptors(getFile(baseUrl));
        if (webDescriptors.containsKey("web.xml") || path.endsWith(".war")) {
            return WebModule.class;
        }

        URL manifestUrl = descriptors.get("MANIFEST.MF");
        if (manifestUrl != null) {
            InputStream is = manifestUrl.openStream();
            Manifest manifest = new Manifest(is);
            String mainClass = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            if (mainClass != null) {
                return ClientModule.class;
            }
        }

        if (searchForDescriptorlessApplications) {
            ClassFinder classFinder = new ClassFinder(classLoader, baseUrl);

            if (classFinder.isAnnotationPresent(Stateless.class) ||
                    classFinder.isAnnotationPresent(Stateful.class) ||
                    classFinder.isAnnotationPresent(javax.ejb.MessageDriven.class)) {
                return EjbModule.class;
            }
        }

        throw new UnknownModuleTypeException("Unknown module type: url=" + baseUrl.toExternalForm());
    }

    private File unpack(File jarFile) throws OpenEJBException {
        if (jarFile.isDirectory()) {
            return jarFile;
        }

        String name = jarFile.getName();
        if (name.endsWith(".jar") || name.endsWith(".ear") || name.endsWith(".zip") || name.endsWith(".war") || name.endsWith(".rar")) {
            name = name.replaceFirst("....$", "");
        } else {
            name += ".unpacked";
        }

        try {
            return JarExtractor.extract(jarFile, name);
        } catch (IOException e) {
            throw new OpenEJBException("Unable to extract jar. " + e.getMessage(), e);
        }
    }

    private URL getFileUrl(File jarFile) throws OpenEJBException {
        URL baseUrl = null;
        try {
            baseUrl = jarFile.toURL();
        } catch (MalformedURLException e) {
            throw new OpenEJBException("Malformed URL to app. " + e.getMessage(), e);
        }
        return baseUrl;
    }

    private ClassLoader getClassLoader(File jarFile) throws OpenEJBException {
        try {
            URL[] urls = new URL[]{jarFile.toURL()};
            return new TemporaryClassLoader(urls, OpenEJB.class.getClassLoader());
        } catch (MalformedURLException e) {
            throw new OpenEJBException(messages.format("cl0001", jarFile.getAbsolutePath(), e.getMessage()), e);
        }
    }

}
