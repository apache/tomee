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
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @version $Revision$ $Date$
 */
public class DeploymentLoader {

    private static final Map<Class<?>, JaxbUnmarshaller> unmarshallers = new HashMap<Class<?>, JaxbUnmarshaller>();

    public static Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, "org.apache.openejb.util.resources");
    private static final Messages messages = new Messages("org.apache.openejb.util.resources");

    public DeploymentLoader() {
        // For some reason intellij won't log to the one we configured statically
        logger = EjbJarUtils.logger;
    }

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

                Map<String, URL> ejbModules = new HashMap<String, URL>();
                Map<String, URL> clientModules = new HashMap<String, URL>();

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
                            }
                        } catch (UnsupportedOperationException e) {
                            // Ignore it as per the javaee spec EE.8.4.2 section 1.d.iiilogger.info("Ignoring unknown module type: "+entry.getKey());
                        } catch (Exception e) {
                            throw new OpenEJBException("Unable to determine the module type of " + entry.getKey() + ": Exception: " + e.getMessage(), e);
                        }
                    }
                }

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

                try {
                    Map<String, URL> libs = finder.getResourcesMap("APP-INF/lib/");
                    extraLibs.addAll(libs.values());
                } catch (IOException e) {
                    logger.warning("Cannot load libs from 'APP-INF/lib/' : " + e.getMessage(), e);
                }

                try {
                    Map<String, URL> libs = finder.getResourcesMap("META-INF/lib/");
                    extraLibs.addAll(libs.values());
                } catch (IOException e) {
                    logger.warning("Cannot load libs from 'META-INF/lib/' : " + e.getMessage(), e);
                }

                List<URL> classPath = new ArrayList<URL>();
                classPath.addAll(ejbModules.values());
                classPath.addAll(clientModules.values());
                classPath.addAll(extraLibs);
                URL[] urls = classPath.toArray(new URL[]{});
                ClassLoader appClassLoader = new TemporaryClassLoader(urls, OpenEJB.class.getClassLoader());

                AppModule appModule = new AppModule(appClassLoader, appDir.getAbsolutePath());
                appModule.getAdditionalLibraries().addAll(extraLibs);
                appModule.getAltDDs().putAll(appDescriptors);

                for (String moduleName : ejbModules.keySet()) {
                    try {
                        URL ejbUrl = ejbModules.get(moduleName);
                        File ejbFile = new File(ejbUrl.getPath());
                        EjbModule ejbModule = new EjbModule(appClassLoader, moduleName, ejbFile.getAbsolutePath(), null, null);

                        fillDescriptors(ejbUrl, ejbModule);

                        appModule.getEjbModules().add(ejbModule);
                    } catch (OpenEJBException e) {
                        logger.error("Unable to load EJBs from EAR: " + appDir.getAbsolutePath() + ", module: " + moduleName + ". Exception: " + e.getMessage(), e);
                    }
                }
                for (String moduleName : clientModules.keySet()) {
                    try {
                        URL clientUrl = clientModules.get(moduleName);
                        File clientFile = new File(clientUrl.getPath());
                        ResourceFinder clientFinder = new ResourceFinder(clientUrl);

                        URL manifestUrl = clientFinder.find("META-INF/MANIFEST.MF");
                        InputStream is = manifestUrl.openStream();
                        Manifest manifest = new Manifest(is);
                        String mainClass = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);

                        ClientModule clientModule = new ClientModule(null, appClassLoader, clientFile.getAbsolutePath(), mainClass, moduleName);

                        fillDescriptors(clientUrl, clientModule);

                        appModule.getClientModules().add(clientModule);
                    } catch (Exception e) {
                        logger.error("Unable to load App Client from EAR: " + appDir.getAbsolutePath() + ", module: " + moduleName + ". Exception: " + e.getMessage(), e);
                    }
                }

                //
                // Persistence Units
                try {
                    ResourceFinder finder1 = new ResourceFinder("", appClassLoader, urls);
                    List<URL> persistenceUrls = finder1.findAll("META-INF/persistence.xml");
                    appModule.getAltDDs().put("persistence.xml", persistenceUrls);
                } catch (IOException e1) {
                    logger.warning("Cannot load persistence-units from 'META-INF/persistence.xml' : " + e1.getMessage(), e1);
                }

                return appModule;

            } catch (OpenEJBException e) {
                logger.error("Unable to load EAR: " + jarFile.getAbsolutePath(), e);
                throw e;
            }

        } else if (EjbModule.class.equals(moduleClass)) {

            EjbModule ejbModule = new EjbModule(classLoader, jarFile.getAbsolutePath(), null, null);

            fillDescriptors(baseUrl, ejbModule);

            AppModule appModule = new AppModule(classLoader, ejbModule.getJarLocation());
            appModule.getEjbModules().add(ejbModule);

            //
            // Persistence Units
            try {
                ResourceFinder finder = new ResourceFinder("", classLoader, baseUrl);
                List<URL> persistenceUrls = finder.findAll("META-INF/persistence.xml");
                appModule.getAltDDs().put("persistence.xml", persistenceUrls);
            } catch (IOException e) {
                logger.warning("Cannot load persistence-units from 'META-INF/persistence.xml' : " + e.getMessage(), e);
            }

            return appModule;
        } else {
            throw new UnsupportedModuleTypeException("Unsupported module type: "+moduleClass.getSimpleName());
        }
    }

    private void fillDescriptors(URL moduleUrl, DeploymentModule module) throws OpenEJBException {
        try {
            ResourceFinder finder = new ResourceFinder(moduleUrl);

            Map<String, URL> descriptors = finder.getResourcesMap("META-INF/");

            module.getAltDDs().putAll(descriptors);
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
            return ResourceModule.class;
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
