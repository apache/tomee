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
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.finder.ResourceFinder;
import org.xml.sax.SAXException;

import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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

                // EJB modules
                for (String moduleName : ejbModules.keySet()) {
                    try {
                        URL ejbUrl = ejbModules.get(moduleName);
                        File ejbFile = new File(ejbUrl.getPath());

                        Map<String, URL> descriptors = getDescriptors(ejbUrl);

                        EjbJar ejbJar = null;
                        if (descriptors.containsKey("ejb-jar.xml")){
                            ejbJar = ReadDescriptors.readEjbJar(descriptors.get("ejb-jar.xml"));
                        }

                        EjbModule ejbModule = new EjbModule(appClassLoader, moduleName, ejbFile.getAbsolutePath(), ejbJar, null);

                        ejbModule.getAltDDs().putAll(descriptors);

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
                        if (descriptors.containsKey("application-client.xml")){
                            applicationClient = ReadDescriptors.readApplicationClient(descriptors.get("application-client.xml"));
                        }

                        ClientModule clientModule = new ClientModule(applicationClient, appClassLoader, clientFile.getAbsolutePath(), mainClass, moduleName);

                        clientModule.getAltDDs().putAll(descriptors);

                        appModule.getClientModules().add(clientModule);
                    } catch (Exception e) {
                        logger.error("Unable to load App Client from EAR: " + appDir.getAbsolutePath() + ", module: " + moduleName + ". Exception: " + e.getMessage(), e);
                    }
                }

                // Resource modules
                for (String moduleName : resouceModules.keySet()) {
                    try {
                        URL rarUrl = resouceModules.get(moduleName);
                        File rarFile = new File(rarUrl.getPath());

                        Map<String, URL> descriptors = getDescriptors(rarUrl);

                        Connector connector = null;
                        if (descriptors.containsKey("ra.xml")){
                            connector = ReadDescriptors.readConnector(descriptors.get("ra.xml"));
                        }

                        ConnectorModule connectorModule = new ConnectorModule(connector, appClassLoader, rarFile.getAbsolutePath(),  moduleName);

                        connectorModule.getAltDDs().putAll(descriptors);

                        appModule.getResourceModules().add(connectorModule);
                    } catch (OpenEJBException e) {
                        logger.error("Unable to load RAR: " + appDir.getAbsolutePath() + ", module: " + moduleName + ". Exception: " + e.getMessage(), e);
                    }
                }

                // Web modules
                for (String moduleName : webModules.keySet()) {
                    try {
                        URL warUrl = webModules.get(moduleName);
                        File warFile = new File(warUrl.getPath());

                        // read web.xml file
                        Map<String, URL> descriptors = getDescriptors(warUrl);
                        WebApp webApp = null;
                        if (descriptors.containsKey("web.xml")){
                            webApp = ReadDescriptors.readWebApp(descriptors.get("web.xml"));
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
                        ClassLoader warClassLoader = new TemporaryClassLoader(webUrls, appClassLoader);

                        // create web module
                        WebModule webModule = new WebModule(webApp, webContextRoots.get(moduleName), warClassLoader, warFile.getAbsolutePath(),  moduleName);
                        webModule.getAltDDs().putAll(descriptors);

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
            if (descriptors.containsKey("ejb-jar.xml")){
                ejbJar = ReadDescriptors.readEjbJar(descriptors.get("ejb-jar.xml"));
            }

            // create the EJB Module
            EjbModule ejbModule = new EjbModule(classLoader, jarFile.getAbsolutePath(), ejbJar, null);
            ejbModule.getAltDDs().putAll(descriptors);

            // wrap the EJB Module with an Application Module
            AppModule appModule = new AppModule(classLoader, ejbModule.getJarLocation());
            appModule.getEjbModules().add(ejbModule);

            // Persistence Units
            addPersistenceUnits(appModule, classLoader, baseUrl);

            return appModule;
        } else if (ConnectorModule.class.equals(moduleClass)) {
            // unpack the rar file
            File rarFile = new File(baseUrl.getPath());
            rarFile = unpack(rarFile);
            baseUrl = getFileUrl(rarFile);

            // read the ra.xml file
            Map<String, URL> descriptors = getDescriptors(baseUrl);
            Connector connector = null;
            if (descriptors.containsKey("ra.xml")){
                connector = ReadDescriptors.readConnector(descriptors.get("ra.xml"));
            }

            // find the nested jar files
            HashMap<String, URL> rarLibs = new HashMap<String, URL>();
            scanDir(rarFile, rarLibs, "");
            for (Iterator<Map.Entry<String, URL>> iterator = rarLibs.entrySet().iterator(); iterator.hasNext();) {
                // remove all non jars from the rarLibs
                Map.Entry<String, URL> fileEntry = iterator.next();
                if (!fileEntry.getKey().endsWith(".jar")) continue;
                iterator.remove();
            }

            // create the class loader
            List<URL> classPath = new ArrayList<URL>();
            classPath.addAll(rarLibs.values());
            URL[] urls = classPath.toArray(new URL[]{});
            ClassLoader appClassLoader = new TemporaryClassLoader(urls, OpenEJB.class.getClassLoader());

            // create the Resource Module
            ConnectorModule connectorModule = new ConnectorModule(connector, appClassLoader, jarFile.getAbsolutePath(),  null);
            connectorModule.getAltDDs().putAll(descriptors);

            // Wrap the resource module with an Application Module
            AppModule appModule = new AppModule(classLoader, connectorModule.getJarLocation());
            appModule.getResourceModules().add(connectorModule);

            // Persistence Units
            addPersistenceUnits(appModule, classLoader, baseUrl);

            return appModule;
        } else if (WebModule.class.equals(moduleClass)) {
            // unpack the rar file
            File warFile = new File(baseUrl.getPath());
            warFile = unpack(warFile);
            baseUrl = getFileUrl(warFile);

            // read the web.xml file
            Map<String, URL> descriptors = getDescriptors(baseUrl);
            WebApp webApp = null;
            if (descriptors.containsKey("web.xml")){
                webApp = ReadDescriptors.readWebApp(descriptors.get("web.xml"));
            }

            // determine war class path
            List<URL> classPath = new ArrayList<URL>();
            File webInfDir = new File(warFile, "WEB-INF");
            try {
                classPath.add(new File(webInfDir, "classes").toURL());
            } catch (MalformedURLException e) {
                logger.warning("War path bad: " + new File(webInfDir, "classes"), e);
            }

            File libDir = new File(webInfDir, "lib");
            if (libDir.exists()) {
                for (File file : libDir.listFiles()) {
                    if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")) {
                        try {
                            classPath.add(file.toURL());
                        } catch (MalformedURLException e) {
                            logger.warning("War path bad: " + file, e);
                        }
                    }
                }
            }

            // create the class loader
            URL[] urls = classPath.toArray(new URL[]{});
            ClassLoader appClassLoader = new TemporaryClassLoader(urls, OpenEJB.class.getClassLoader());

            // create the Resource Module
            WebModule webModule = new WebModule(webApp, null, appClassLoader, jarFile.getAbsolutePath(),  null);
            webModule.getAltDDs().putAll(descriptors);

            // Wrap the resource module with an Application Module
            AppModule appModule = new AppModule(classLoader, webModule.getJarLocation());
            appModule.getWebModules().add(webModule);

            // Persistence Units
            addPersistenceUnits(appModule, classLoader, baseUrl);

            return appModule;
        } else {
            throw new UnsupportedModuleTypeException("Unsupported module type: "+moduleClass.getSimpleName());
        }
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

        if (descriptors.containsKey("application.xml") || baseUrl.getPath().endsWith(".ear")) {
            return AppModule.class;
        }

        if (descriptors.containsKey("ejb-jar.xml")) {
            return EjbModule.class;
        }

        if (descriptors.containsKey("application-client.xml")) {
            return ClientModule.class;
        }

        if (descriptors.containsKey("ra.xml")) {
            return ConnectorModule.class;
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
        if (name.endsWith(".jar") || name.endsWith(".ear") || name.endsWith(".zip")) {
            name = name.replaceFirst("....$", "");
        } else {
            name += "_app";
        }

        try {
            URL jarUrl = new URL("jar", "", jarFile.toURL().toExternalForm() + "!/");
            return JarExtractor.extract(jarUrl, name, jarFile);
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
