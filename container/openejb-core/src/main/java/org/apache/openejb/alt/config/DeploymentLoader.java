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
import org.apache.openejb.alt.config.sys.Deployments;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Module;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Logger;
import org.apache.xbean.finder.ResourceFinder;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.finder.UrlSet;

import javax.ejb.Stateless;
import javax.ejb.Stateful;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

/**
 * @version $Revision$ $Date$
 */
public class DeploymentLoader {

    private static final Map<Class<?>, JaxbUnmarshaller> unmarshallers = new HashMap();

	private static final String CLASSPATH_INCLUDE = "openejb.deployments.classpath.include";
	private static final String CLASSPATH_EXCLUDE = "openejb.deployments.classpath.exclude";

    public static Logger logger = Logger.getInstance("OpenEJB.startup", "org.apache.openejb.util.resources");

    public DeploymentLoader() {
        // For some reason intellij won't log to the one we configured statically
        logger = EjbJarUtils.logger;
    }

    private void loadFrom(Deployments dep, FileUtils path, List jarList) {

        ////////////////////////////////
        //
        //  Expand the path of a jar
        //
        ////////////////////////////////
        if (dep.getDir() == null && dep.getJar() != null) {
            try {
                File jar = path.getFile(dep.getJar(), false);
                if (!jarList.contains(jar.getAbsolutePath())) {
                    jarList.add(jar.getAbsolutePath());
                }
            } catch (Exception ignored) {
            }
            return;
        }

        File dir = null;
        try {
            dir = path.getFile(dep.getDir(), false);
        } catch (Exception ignored) {
        }

        if (dir == null || !dir.isDirectory()) return;

        ////////////////////////////////
        //
        //  Unpacked "Jar" directory
        //
        ////////////////////////////////
        File ejbJarXml = new File(dir, "META-INF" + File.separator + "ejb-jar.xml");
        if (ejbJarXml.exists()) {
            if (!jarList.contains(dir.getAbsolutePath())) {
                jarList.add(dir.getAbsolutePath());
            }
            return;
        }

        File appXml = new File(dir, "META-INF" + File.separator + "application.xml");
        if (appXml.exists()) {
            if (!jarList.contains(dir.getAbsolutePath())) {
                jarList.add(dir.getAbsolutePath());
            }
            return;
        }

        HashMap<String, URL> files = new HashMap<String, URL>();
        scanDir(dir, files,"");
        for (String fileName : files.keySet()) {
            if (fileName.endsWith(".class")){
                if (!jarList.contains(dir.getAbsolutePath())) {
                    jarList.add(dir.getAbsolutePath());
                }
                return;
            }
        }

        ////////////////////////////////
        //
        //  Directory container Jar files
        //
        ////////////////////////////////
        for (String fileName : files.keySet()) {
            if (fileName.endsWith(".jar") || fileName.endsWith(".ear")){
                File jar = new File(dir, fileName);

                if (jarList.contains(jar.getAbsolutePath())) continue;
                jarList.add(jar.getAbsolutePath());
            }
        }
    }

    public static enum Type {
        JAR, DIR, CLASSPATH
    }

    public List<DeploymentModule> load(Type type, Object source) throws OpenEJBException {
        Deployments deployments = new Deployments();
        switch (type) {
            case JAR:
                deployments.setJar((String) source);
                break;
            case DIR:
                deployments.setDir((String) source);
                break;
            case CLASSPATH:
                deployments.setClasspath((ClassLoader) source);
                break;
        }

        List<Deployments> list = new ArrayList();
        list.add(deployments);
        return loadDeploymentsList(list, null);
    }

    public List<DeploymentModule> loadDeploymentsList(List<Deployments> deployments, DynamicDeployer deployer) throws OpenEJBException {

        if (deployer == null) {
            deployer = new DynamicDeployer() {
                public EjbModule deploy(EjbModule ejbModule) throws OpenEJBException {
                    return ejbModule;
                }

                public ClientModule deploy(ClientModule clientModule) throws OpenEJBException {
                    return clientModule;
                }
            };
        }

        deployer = new AnnotationDeployer(deployer);

        if (!SystemInstance.get().getProperty("openejb.validation.skip", "false").equalsIgnoreCase("true")) {
            deployer = new ValidateEjbModule(deployer);
        } else {
            logger.info("Validation is disabled.");
        }

        List<DeploymentModule> deployedJars = new ArrayList();

        // resolve jar locations //////////////////////////////////////  BEGIN  ///////

        FileUtils base = SystemInstance.get().getBase();

        List<String> jarList = new ArrayList(deployments.size());
        try {
            for (Deployments deployment : deployments) {
                if (deployment.getClasspath() != null) {
                    loadFromClasspath(base, jarList, deployment.getClasspath());
                } else {
                    loadFrom(deployment, base, jarList);
                }
            }
        } catch (SecurityException se) {

        }

        String[] jarsToLoad = (String[]) jarList.toArray(new String[]{});

        /*[1]  Put all EjbJar & OpenejbJar objects in a vector ***************/
        for (int i = 0; i < jarsToLoad.length; i++) {

            String pathname = jarsToLoad[i];

            logger.debug("Beginning load: " + pathname);

            File jarFile = new File(pathname);

            try {

                ClassLoader classLoader = getClassLoader(jarFile);

                URL baseUrl = getFileUrl(jarFile);


                Class moduleClass = null;
                try {
                    moduleClass = discoverModuleType(baseUrl, classLoader);
                } catch (Exception e) {
                    throw new OpenEJBException("Unable to determine module type for jar: " + baseUrl.toExternalForm(), e);
                }

                if (AppModule.class.equals(moduleClass)) {

                    loadAppModule(jarFile, deployer, deployedJars);

                } else {
                    EjbJarUtils ejbJarUtils = new EjbJarUtils(jarFile.getAbsolutePath());
                    EjbModule undeployedModule = new EjbModule(classLoader, jarFile.getAbsolutePath(), ejbJarUtils.getEjbJar(), ejbJarUtils.getOpenejbJar());
                    EjbModule ejbModule = deployer.deploy(undeployedModule);

                    /* Add it to the Vector ***************/
                    deployedJars.add(ejbModule);
                }

            } catch (OpenEJBException e) {
                e.printStackTrace();
                ConfigUtils.logger.i18n.warning("conf.0004", jarFile.getAbsolutePath(), e.getMessage());
            }
        }

        for (DeploymentModule ejbModule : deployedJars) {
            logger.info("Loaded Module: " + ejbModule.getJarLocation());
        }
        return deployedJars;
    }

    private void scanDir(File dir, Map<String,URL> files, String path) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scanDir(file, files, path + file.getName() + "/");
            } else {
                String name = file.getName();
                try {
                    files.put(path + name, file.toURL());
                } catch (MalformedURLException e) {
                    logger.warning("EAR path bad: "+path + name, e);
                }
            }
        }
    }

    private void loadAppModule(File jarFile, DynamicDeployer deployer, List<DeploymentModule> deployedJars) throws OpenEJBException {
        File appDir = unpack(jarFile);

        URL appUrl = getFileUrl(appDir);

        ClassLoader tmpClassLoader = new URLClassLoader(new URL[]{appUrl}, OpenEJB.class.getClassLoader());

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

            Map<String,URL> ejbModules = new HashMap();
            Map<String,URL> clientModules = new HashMap();

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
                HashMap<String,URL> files = new HashMap();
                scanDir(appDir, files, "");
                files.remove("META-INF/MANIFEST.MF");
                for (Map.Entry<String, URL> entry : files.entrySet()) {
                    if (entry.getKey().startsWith("lib/")) continue;
                    if (!entry.getKey().matches(".*\\.(jar|war|rar|ear)")) continue;

                    try {
                        Class moduleType = discoverModuleType(entry.getValue(), tmpClassLoader);
                        if (EjbModule.class.equals(moduleType)){
                            ejbModules.put(entry.getKey(), entry.getValue());
                        } else if (ClientModule.class.equals(moduleType)){
                            clientModules.put(entry.getKey(), entry.getValue());
                        }
                    } catch (UnsupportedOperationException e) {
                        // Ignore it as per the javaee spec EE.8.4.2 section 1.d.iiilogger.info("Ignoring unknown module type: "+entry.getKey());
                    } catch (Exception e) {
                        throw new OpenEJBException("Unable to determine the module type of "+ entry.getKey()+": Exception: "+ e.getMessage(), e);
                    }
                }
            }

            List<URL> classPath = new ArrayList();
            classPath.addAll(ejbModules.values());
            classPath.addAll(clientModules.values());
            classPath.addAll(extraLibs);
            ClassLoader appClassLoader = new URLClassLoader(classPath.toArray(new URL[]{}), OpenEJB.class.getClassLoader());


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

                    ejbModule = deployer.deploy(ejbModule);

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

                    clientModule = deployer.deploy(clientModule);

                    appModule.getClientModules().add(clientModule);
                } catch (Exception e) {
                    logger.error("Unable to load App Client from EAR: " + appDir.getAbsolutePath() + ", module: " + moduleName + ". Exception: " + e.getMessage(), e);
                }
            }
            deployedJars.add(appModule);
        } catch (OpenEJBException e) {
            logger.error("Unable to load EAR: " + jarFile.getAbsolutePath(), e);
        }
    }


    private Class<? extends DeploymentModule> discoverModuleType(URL baseUrl, ClassLoader classLoader) throws IOException, UnsupportedOperationException {
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
        if (manifestUrl != null){
            InputStream is = manifestUrl.openStream();
            Manifest manifest = new Manifest(is);
            String mainClass = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            if (mainClass != null){
                return ClientModule.class;
            }
        }

        ClassFinder classFinder = new ClassFinder(new URLClassLoader(new URL[]{baseUrl},classLoader), baseUrl);
        List<Class> beans = classFinder.findAnnotatedClasses(Stateless.class);
        beans.addAll(classFinder.findAnnotatedClasses(Stateful.class));
        beans.addAll(classFinder.findAnnotatedClasses(javax.ejb.MessageDriven.class));

        if (beans.size() > 0){
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
        if (jarFile.isDirectory()){
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
            return new URLClassLoader(urls, OpenEJB.class.getClassLoader());
        } catch (MalformedURLException e) {
            throw new OpenEJBException(ConfigurationFactory.messages.format("cl0001", jarFile.getAbsolutePath(), e.getMessage()));
        }
    }

    /*
     * The algorithm of OpenEJB deployments class-path inclusion and exclusion is implemented as follows:
     * 	1- If the string value of the resource URL matches the include class-path pattern
     *     Then load this resource
     *  2- If the string value of the resource URL matches the exclude class-path pattern
     *     Then ignore this resource
     *  3- If the include and exclude class-path patterns are not defined
     *     Then load this resource
     *     
     * The previous steps are based on the following points:
     *  1- Include class-path pattern has the highst priority
     *     This helps in case both patterns are defined using the same values.
     *     This appears in step 1 and 2 of the above algorithm.
     *	2- Loading the resource is the default behaviour in case of not defining a value for any class-path pattern
     *	   This appears in step 3 of the above algorithm.
     */
    private void loadFromClasspath(FileUtils base, List<String> jarList, ClassLoader classLoader) {
    	
    	Deployments deployment = null;
    	String include = null;
    	String exclude = null;
    	String path = null;

    	include = SystemInstance.get().getProperty(CLASSPATH_INCLUDE, "");
    	exclude = SystemInstance.get().getProperty(CLASSPATH_EXCLUDE, ".*");
        try {
            UrlSet urlSet = new UrlSet(classLoader);
            UrlSet includes = urlSet.matching(include);
            urlSet = urlSet.exclude(ClassLoader.getSystemClassLoader().getParent());
            urlSet = urlSet.excludeJavaExtDirs();
            urlSet = urlSet.excludeJavaEndorsedDirs();
            urlSet = urlSet.excludeJavaHome();
            urlSet = urlSet.excludePaths(System.getProperty("sun.boot.class.path",""));
            urlSet = urlSet.exclude(".*/JavaVM.framework/.*");
            urlSet = urlSet.exclude(exclude);
            urlSet = urlSet.include(includes);

            List<URL> urls = urlSet.getUrls();
            int size = urls.size();
            if (size == 0 && include.length() > 0){
                logger.warning("No classpath URLs matched.  Current settings: "+CLASSPATH_EXCLUDE +"='"+exclude+"', "+CLASSPATH_INCLUDE+"='"+include+"'");
                return;
            } else if (size == 0) {
                return;
            } else if (size < 10) {
                logger.debug("Inspecting classpath for applications: "+urls.size()+" urls.");
            } else if (size < 50) {
                logger.info("Inspecting classpath for applications: "+urls.size()+" urls. Consider adjusting your exclude/include.  Current settings: "+CLASSPATH_EXCLUDE +"='"+exclude+"', "+CLASSPATH_INCLUDE+"='"+include+"'");
            } else {
                logger.warning("Inspecting classpath for applications: "+urls.size()+" urls.");
                logger.warning("ADJUST THE EXCLUDE/INCLUDE!!!.  Current settings: "+CLASSPATH_EXCLUDE +"='"+exclude+"', "+CLASSPATH_INCLUDE+"='"+include+"'");
            }

            long begin = System.currentTimeMillis();
            for (URL url : urls) {
                try {
                    Class moduleType = discoverModuleType(url, classLoader);
                    if (AppModule.class.isAssignableFrom(moduleType) || EjbModule.class.isAssignableFrom(moduleType)){
                        deployment = new Deployments();
                        if (url.getProtocol().equals("jar")) {
                            url = new URL(url.getFile().replaceFirst("!.*$", ""));
                            File file = new File(url.getFile());
                            path = file.getAbsolutePath();
                            deployment.setJar(path);
                        } else if (url.getProtocol().equals("file")) {
                            File file = new File(url.getFile());
                            path = file.getAbsolutePath();
                            deployment.setDir(path);
                        } else {
                            logger.warning("Not loading " + moduleType.getSimpleName() + ".  Unknown protocol " + url.getProtocol());
                            continue;
                        }
                        logger.info("Found " + moduleType.getSimpleName() + " in classpath: " + path);
                        loadFrom(deployment, base, jarList);
                    }
                } catch (IOException e) {
                    logger.warning("Unable to determine the module type of "+ url.toExternalForm()+": Exception: "+ e.getMessage(), e);
                } catch (UnsupportedOperationException ignore) {
                }
            }
            long end = System.currentTimeMillis();
            long time = end - begin;

            if (time < 1000) {
                logger.debug("Searched "+urls.size()+" classpath urls in "+time+" milliseconds.  Average "+(time/urls.size())+" milliseconds per url.");
            } else if (time < 4000 || urls.size() < 3) {
                logger.info("Searched "+urls.size()+" classpath urls in "+time+" milliseconds.  Average "+(time/urls.size())+" milliseconds per url.");
            } else if (time < 10000){
                logger.warning("Searched "+urls.size()+" classpath urls in "+time+" milliseconds.  Average "+(time/urls.size())+" milliseconds per url.");
                logger.warning("Consider adjusting your "+CLASSPATH_EXCLUDE +" and "+CLASSPATH_INCLUDE +" settings.  Current settings: exclude='"+exclude+"', include='"+include+"'");
            } else {
                logger.fatal("Searched "+urls.size()+" classpath urls in "+time+" milliseconds.  Average "+(time/urls.size())+" milliseconds per url.  TOO LONG!");
                logger.fatal("ADJUST THE EXCLUDE/INCLUDE!!!.  Current settings: "+CLASSPATH_EXCLUDE +"='"+exclude+"', "+CLASSPATH_INCLUDE+"='"+include+"'");
                List<String> list = new ArrayList<String>();
                for (URL url : urls) {
                    list.add(url.toExternalForm());
                }
                Collections.sort(list);
                for (String url : list) {
                    logger.info("Matched: "+url);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            logger.warning("Unable to search classpath for modules: Received Exception: " + e1.getClass().getName() + " " + e1.getMessage(), e1);
        }
        
    }

    public static class JarExtractor {

        /**
         * Extract the WAR file found at the specified URL into an unpacked
         * directory structure, and return the absolute pathname to the extracted
         * directory.
         *
         * @param jar      URL of the web application archive to be extracted
         *                 (must start with "jar:")
         * @param pathname Context path name for web application
         * @param file
         * @throws IllegalArgumentException if this is not a "jar:" URL
         * @throws IOException              if an input/output error was encountered
         *                                  during expansion
         */
        public static File extract(URL jar, String pathname, File file)
                throws IOException {

            // Make sure that there is no such directory already existing
            FileUtils base = SystemInstance.get().getBase();
            File appBase = base.getDirectory("apps", true);
            if (!appBase.exists() || !appBase.isDirectory()) {
                throw new IOException("" + appBase.getAbsolutePath());
            }

            File docBase = new File(appBase, pathname);
            if (docBase.exists()) {
                // Ear file is already installed
                return docBase;
            }

            logger.info("Extracting jar: " + file.getAbsolutePath());

            // Create the new document base directory
            docBase.mkdir();

            // Extract the JAR into the new directory
            JarURLConnection jarURLConnection = (JarURLConnection) jar.openConnection();
            jarURLConnection.setUseCaches(false);
            JarFile jarFile = null;
            InputStream input = null;
            try {
                jarFile = jarURLConnection.getJarFile();
                Enumeration jarEntries = jarFile.entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry jarEntry = (JarEntry) jarEntries.nextElement();
                    String name = jarEntry.getName();
                    int last = name.lastIndexOf('/');
                    if (last >= 0) {
                        File parent = new File(docBase,
                                name.substring(0, last));
                        parent.mkdirs();
                    }
                    if (name.endsWith("/")) {
                        continue;
                    }
                    input = jarFile.getInputStream(jarEntry);

                    File extractedFile = extract(input, docBase, name);
                    long lastModified = jarEntry.getTime();
                    if ((lastModified != -1) && (lastModified != 0) && (extractedFile != null)) {
                        extractedFile.setLastModified(lastModified);
                    }

                    input.close();
                    input = null;
                }
            } catch (IOException e) {
                // If something went wrong, delete extracted dir to keep things
                // clean
                deleteDir(docBase);
                throw e;
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (Throwable t) {
                        ;
                    }
                    input = null;
                }
                if (jarFile != null) {
                    try {
                        jarFile.close();
                    } catch (Throwable t) {
                        ;
                    }
                    jarFile = null;
                }
            }

            // Return the absolute path to our new document base directory
            logger.info("Extracted path: " + docBase.getAbsolutePath());

            return docBase;

        }


        /**
         * Copy the specified file or directory to the destination.
         *
         * @param src  File object representing the source
         * @param dest File object representing the destination
         */
        public static boolean copy(File src, File dest) {

            boolean result = true;

            String files[] = null;
            if (src.isDirectory()) {
                files = src.list();
                result = dest.mkdir();
            } else {
                files = new String[1];
                files[0] = "";
            }
            if (files == null) {
                files = new String[0];
            }
            for (int i = 0; (i < files.length) && result; i++) {
                File fileSrc = new File(src, files[i]);
                File fileDest = new File(dest, files[i]);
                if (fileSrc.isDirectory()) {
                    result = copy(fileSrc, fileDest);
                } else {
                    FileChannel ic = null;
                    FileChannel oc = null;
                    try {
                        ic = (new FileInputStream(fileSrc)).getChannel();
                        oc = (new FileOutputStream(fileDest)).getChannel();
                        ic.transferTo(0, ic.size(), oc);
                    } catch (IOException e) {
                        logger.error("Copy failed: src: " + fileSrc + ", dest: " + fileDest, e);
                        result = false;
                    } finally {
                        if (ic != null) {
                            try {
                                ic.close();
                            } catch (IOException e) {
                            }
                        }
                        if (oc != null) {
                            try {
                                oc.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            }
            return result;

        }


        /**
         * Delete the specified directory, including all of its contents and
         * subdirectories recursively.
         *
         * @param dir File object representing the directory to be deleted
         */
        public static boolean delete(File dir) {
            if (dir.isDirectory()) {
                return deleteDir(dir);
            } else {
                return dir.delete();
            }
        }


        /**
         * Delete the specified directory, including all of its contents and
         * subdirectories recursively.
         *
         * @param dir File object representing the directory to be deleted
         */
        public static boolean deleteDir(File dir) {

            String files[] = dir.list();
            if (files == null) {
                files = new String[0];
            }
            for (int i = 0; i < files.length; i++) {
                File file = new File(dir, files[i]);
                if (file.isDirectory()) {
                    deleteDir(file);
                } else {
                    file.delete();
                }
            }
            return dir.delete();

        }


        /**
         * Extract the specified input stream into the specified directory, creating
         * a file named from the specified relative path.
         *
         * @param input   InputStream to be copied
         * @param docBase Document base directory into which we are extracting
         * @param name    Relative pathname of the file to be created
         * @return A handle to the extracted File
         * @throws IOException if an input/output error occurs
         */
        protected static File extract(InputStream input, File docBase, String name)
                throws IOException {

            File file = new File(docBase, name);
            BufferedOutputStream output = null;
            try {
                output =
                        new BufferedOutputStream(new FileOutputStream(file));
                byte buffer[] = new byte[2048];
                while (true) {
                    int n = input.read(buffer);
                    if (n <= 0)
                        break;
                    output.write(buffer, 0, n);
                }
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            }

            return file;
        }


    }
}
