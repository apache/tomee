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
package org.apache.openejb.alt.config;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.alt.config.ejb.OpenejbJar;
import org.apache.openejb.core.TemporaryClassLoader;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Module;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.finder.ResourceFinder;

import javax.ejb.Stateful;
import javax.ejb.Stateless;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @version $Revision$ $Date$
 */
public class DeploymentLoader {

    private static final Map<Class<?>, JaxbUnmarshaller> unmarshallers = new HashMap();

    public static Logger logger = Logger.getInstance("OpenEJB.startup", "org.apache.openejb.util.resources");
    private static final Messages messages = new Messages("org.apache.openejb.util.resources");

    public DeploymentLoader() {
        // For some reason intellij won't log to the one we configured statically
        logger = EjbJarUtils.logger;
    }


    public AppModule load(File jarFile) throws OpenEJBException {
        ClassLoader classLoader = getClassLoader(jarFile);

        URL baseUrl = getFileUrl(jarFile);

        Class moduleClass = null;
        try {
            moduleClass = discoverModuleType(baseUrl, classLoader);
        } catch (Exception e) {
            throw new OpenEJBException("Unable to determine module type for jar: " + baseUrl.toExternalForm(), e);
        }

        if (AppModule.class.equals(moduleClass)) {

            File appDir = unpack(jarFile);

            URL appUrl = getFileUrl(appDir);

            ClassLoader tmpClassLoader = new TemporaryClassLoader(new URL[]{appUrl}, OpenEJB.class.getClassLoader());

            ResourceFinder finder = new ResourceFinder("", tmpClassLoader, appUrl);

            Map<String, URL> descriptors = null;
            try {
                descriptors = finder.getResourcesMap("META-INF");
            } catch (IOException e) {
                throw new OpenEJBException("Unable to determine descriptors in jar: " + appUrl.toExternalForm(), e);
            }

            try {
                List<URL> extraLibs = new ArrayList();

                try {
                    Map<String, URL> libs = finder.getResourcesMap("lib/");
                    extraLibs.addAll(libs.values());
                } catch (IOException e) {
                    logger.warning("Cannot load libs from 'lib/' : " + e.getMessage(), e);
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

                Map<String, URL> ejbModules = new HashMap();
                Map<String, URL> clientModules = new HashMap();

                URL applicationXmlUrl = descriptors.get("application.xml");

                Application application;
                if (applicationXmlUrl != null) {
                    application = unmarshal(Application.class, "META-INF/application.xml", applicationXmlUrl);
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
                    HashMap<String, URL> files = new HashMap();
                    scanDir(appDir, files, "");
                    files.remove("META-INF/MANIFEST.MF");
                    for (Map.Entry<String, URL> entry : files.entrySet()) {
                        if (entry.getKey().startsWith("lib/")) continue;
                        if (!entry.getKey().matches(".*\\.(jar|war|rar|ear)")) continue;

                        try {
                            Class moduleType = discoverModuleType(entry.getValue(), tmpClassLoader);
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

                List<URL> classPath = new ArrayList();
                classPath.addAll(ejbModules.values());
                classPath.addAll(clientModules.values());
                classPath.addAll(extraLibs);
                ClassLoader appClassLoader = new TemporaryClassLoader(classPath.toArray(new URL[]{}), OpenEJB.class.getClassLoader());


                AppModule appModule = new AppModule(appClassLoader, appDir.getAbsolutePath());
                appModule.getAdditionalLibraries().addAll(extraLibs);

                for (String moduleName : ejbModules.keySet()) {
                    URL ejbUrl = ejbModules.get(moduleName);
                    File ejbFile = new File(ejbUrl.getPath());
                    try {
                        ResourceFinder ejbFinder = new ResourceFinder(ejbUrl);

                        Map<String, URL> ejbDescriptors = null;
                        try {
                            ejbDescriptors = ejbFinder.getResourcesMap("META-INF/");
                        } catch (IOException e) {
                            throw new OpenEJBException("Unable to determine descriptors in jar.", e);
                        }

                        EjbJar ejbJar;
                        if (ejbDescriptors.containsKey("ejb-jar.xml")) {
                            ejbJar = unmarshal(EjbJar.class, "META-INF/ejb-jar.xml", ejbDescriptors.get("ejb-jar.xml"));
                        } else {
                            logger.warning("No ejb-jar.xml found assuming annotated beans present: " + appDir.getAbsolutePath() + ", module: " + moduleName);
                            ejbJar = new EjbJar();
                        }


                        OpenejbJar openejbJar = null;

                        if (ejbDescriptors.containsKey("openejb-jar.xml")) {
                            openejbJar = unmarshal(OpenejbJar.class, "META-INF/openejb-jar.xml", ejbDescriptors.get("openejb-jar.xml"));
                        }

                        EjbModule ejbModule = new EjbModule(appClassLoader, ejbFile.getAbsolutePath(), ejbJar, openejbJar);

                        appModule.getEjbModules().add(ejbModule);
                    } catch (OpenEJBException e) {
                        logger.error("Unable to load EJBs from EAR: " + appDir.getAbsolutePath() + ", module: " + moduleName + ". Exception: " + e.getMessage(), e);
                    }
                }
                for (String moduleName : clientModules.keySet()) {
                    URL clientUrl = clientModules.get(moduleName);
                    File clientFile = new File(clientUrl.getPath());
                    try {
                        ResourceFinder clientFinder = new ResourceFinder(clientUrl);

                        ApplicationClient applicationClient;
                        try {
                            URL appClientXmlUrl = clientFinder.find("META-INF/application-client.xml");
                            applicationClient = unmarshal(ApplicationClient.class, "META-INF/application-client.xml", appClientXmlUrl);
                        } catch (IOException e) {
                            logger.warning("No application-client.xml found assuming annotations present: " + appDir.getAbsolutePath() + ", module: " + moduleName);
                            applicationClient = new ApplicationClient();
                        }

                        URL manifestUrl = clientFinder.find("META-INF/MANIFEST.MF");
                        InputStream is = manifestUrl.openStream();
                        Manifest manifest = new Manifest(is);
                        String mainClass = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);

                        ClientModule clientModule = new ClientModule(applicationClient, appClassLoader, clientFile.getAbsolutePath(), mainClass);

                        appModule.getClientModules().add(clientModule);
                    } catch (Exception e) {
                        logger.error("Unable to load App Client from EAR: " + appDir.getAbsolutePath() + ", module: " + moduleName + ". Exception: " + e.getMessage(), e);
                    }
                }

                return appModule;

            } catch (OpenEJBException e) {
                logger.error("Unable to load EAR: " + jarFile.getAbsolutePath(), e);
                throw e;
            }

        } else {

            EjbJarUtils ejbJarUtils = new EjbJarUtils(jarFile.getAbsolutePath());
            EjbModule ejbModule = new EjbModule(classLoader, jarFile.getAbsolutePath(), ejbJarUtils.getEjbJar(), ejbJarUtils.getOpenejbJar());


//            EjbModule ejbModule = deployer.deploy(undeployedModule);

            AppModule appModule = new AppModule(classLoader, null);
            appModule.getEjbModules().add(ejbModule);

            return appModule;
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


    public static Class<? extends DeploymentModule> discoverModuleType(URL baseUrl, ClassLoader classLoader) throws IOException, UnsupportedOperationException {
        ResourceFinder finder = new ResourceFinder("", classLoader, baseUrl);

        Map<String, URL> descriptors = finder.getResourcesMap("META-INF");

        if (descriptors.containsKey("application.xml") || baseUrl.getPath().endsWith(".ear")) {
            return AppModule.class;
        }

        if (descriptors.containsKey("application-client.xml")) {
            return ClientModule.class;
        }

        if (descriptors.containsKey("ejb-jar.xml")) {
            return EjbModule.class;
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

        ClassFinder classFinder = new ClassFinder(new TemporaryClassLoader(new URL[]{baseUrl}, classLoader), baseUrl);
        List<Class> beans = classFinder.findAnnotatedClasses(Stateless.class);
        beans.addAll(classFinder.findAnnotatedClasses(Stateful.class));
        beans.addAll(classFinder.findAnnotatedClasses(javax.ejb.MessageDriven.class));

        if (beans.size() > 0) {
            return EjbModule.class;
        }

        throw new UnsupportedOperationException("Unknown module type");
    }

    private <T>T unmarshal(Class<T> type, String descriptor, URL descriptorUrl) throws OpenEJBException {
        JaxbUnmarshaller unmarshaller = unmarshallers.get(type);
        if (unmarshaller == null) {
            unmarshaller = new JaxbUnmarshaller(type, descriptor);
            unmarshallers.put(type, unmarshaller);
        }
        return (T) unmarshaller.unmarshal(descriptorUrl);
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
            throw new OpenEJBException(messages.format("cl0001", jarFile.getAbsolutePath(), e.getMessage()));
        }
    }

}
