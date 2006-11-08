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
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Module;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @version $Revision$ $Date$
 */
public class DeploymentLoader {

    public static final Logger logger = Logger.getInstance("OpenEJB.startup", DeploymentLoader.class.getPackage().getName());

    public DeploymentLoader() {

    }

    private static void loadFrom(Deployments dep, FileUtils path, List jarList) {

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

        ////////////////////////////////
        //
        //  Directory container Jar files
        //
        ////////////////////////////////
        String[] jarFiles = dir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar") || name.endsWith(".ear");
            }
        });

        if (jarFiles == null) {
            return;
        }

        for (int x = 0; x < jarFiles.length; x++) {
            String f = jarFiles[x];
            File jar = new File(dir, f);

            if (jarList.contains(jar.getAbsolutePath())) continue;
            jarList.add(jar.getAbsolutePath());
        }
    }

    public static enum Type {
        JAR, DIR, CLASSPATH
    }

    public List<EjbModule> load(Type type, Object source) throws OpenEJBException {
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

    public List<EjbModule> loadDeploymentsList(List<Deployments> deployments, DynamicDeployer deployer) throws OpenEJBException {
        if (deployer == null) {
            deployer = new DynamicDeployer() {
                public EjbModule deploy(EjbModule ejbModule) throws OpenEJBException {
                    return ejbModule;
                }
            };
        }

        deployer = new AnnotationDeployer(deployer);

        if (!SystemInstance.get().getProperty("openejb.validation.skip", "false").equalsIgnoreCase("true")) {
            deployer = new ValidateEjbModule(deployer);
        } else {
            logger.info("Validation is disabled.");
        }

        List<EjbModule> deployedJars = new ArrayList();

        // resolve jar locations //////////////////////////////////////  BEGIN  ///////

        FileUtils base = SystemInstance.get().getBase();


        List<String> jarList = new ArrayList(deployments.size());
        try {
            for (Deployments deployment : deployments) {
                if (deployment.getClasspath() != null) {
                    ClassLoader classLoader = deployment.getClasspath();
                    if (logger.isDebugEnabled()) {
                        Enumeration<URL> resources = null;
                        try {
                            resources = classLoader.getResources("META-INF");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        while (resources.hasMoreElements()) {
                            URL url = resources.nextElement();
                            logger.debug("Searching: " + url.toExternalForm());
                        }
                    }
                    loadFromClasspath(base, jarList, classLoader, "META-INF/ejb-jar.xml", "ejbs");
                    loadFromClasspath(base, jarList, deployment.getClasspath(), "META-INF/application.xml", "ear");
                } else {
                    loadFrom(deployment, base, jarList);
                }
            }
        } catch (SecurityException se) {

        }

        String[] jarsToLoad = (String[]) jarList.toArray(new String[]{});
        // resolve jar locations //////////////////////////////////////  END  ///////

        /*[1]  Put all EjbJar & OpenejbJar objects in a vector ***************/
        for (int i = 0; i < jarsToLoad.length; i++) {

            String pathname = jarsToLoad[i];

            logger.debug("Beginning load: "+pathname);

            File jarFile = new File(pathname);

            try {

                ClassLoader classLoader = getClassLoader(jarFile);

                URL baseUrl = getFileUrl(jarFile);

                URL appXml = getResource(baseUrl, classLoader, "META-INF/application.xml");

                if (appXml != null) {

                    jarFile = unpack(appXml, jarFile);

                    baseUrl = getFileUrl(jarFile);

                    classLoader = new URLClassLoader(new URL[]{baseUrl}, OpenEJB.class.getClassLoader());

                    try {

                        Application application = unmarshal(Application.class, "META-INF/application.xml", classLoader, baseUrl);
                        String[] files = jarFile.list(new FilenameFilter() {
                            public boolean accept(File dir, String name) {
                                return name.endsWith(".jar") || name.endsWith(".zip");
                            }
                        });

                        List<URL> appUrls = new ArrayList();
                        for (String fileName : files) {
                            File lib = new File(jarFile, fileName);
                            try {
                                appUrls.add(lib.toURL());
                            } catch (MalformedURLException e) {
                                logger.error("Bad resource in classpath.  Unable to search for entries. ", e);
                            }
                        }

                        ClassLoader appClassLoader = new URLClassLoader(appUrls.toArray(new URL[]{}), OpenEJB.class.getClassLoader());

                        for (Module module : application.getModule()) {
                            if (module.getEjb() != null) {
                                try {
                                    URL ejbUrl = new File(jarFile, module.getEjb()).toURL();
//                                    ClassLoader ejbClassLoader = new URLClassLoader(new URL[]{ejbUrl}, classLoader);

                                    EjbJar ejbJar = unmarshal(EjbJar.class, "META-INF/ejb-jar.xml", appClassLoader, ejbUrl);

                                    OpenejbJar openejbJar = null;

                                    URL openejbjarUrl = getResource(ejbUrl, appClassLoader, "META-INF/openejb-jar.xml");

                                    if (openejbjarUrl != null) {

                                        openejbJar = unmarshal(OpenejbJar.class, "META-INF/openejb-jar.xml", appClassLoader, ejbUrl);

                                    }

                                    String jarPath = new File(ejbUrl.getFile()).getAbsolutePath();

                                    EjbModule ejbModule = new EjbModule(classLoader, jarPath, ejbJar, openejbJar);

                                    deployedJars.add(ejbModule);
                                    
                                } catch (OpenEJBException e) {
                                    logger.error("Unable to load EJBs from EAR: " + jarFile.getAbsolutePath() + ", module: " + module.getEjb() + ". Exception: " + e.getMessage(), e);
                                } catch (MalformedURLException e) {
                                    logger.error("Bad resource in classpath.  Unable to search for entries. ", e);
                                }
                            }
                        }
                    } catch (OpenEJBException e) {
                        logger.error("Unable to load EAR: " + appXml.toExternalForm(), e);
                    }
                } else {
                    EjbJarUtils ejbJarUtils = new EjbJarUtils(jarFile.getAbsolutePath());
                    EjbModule undeployedModule = new EjbModule(classLoader, jarFile.getAbsolutePath(), ejbJarUtils.getEjbJar(), ejbJarUtils.getOpenejbJar());
                    EjbModule ejbModule = deployer.deploy(undeployedModule);

                    /* Add it to the Vector ***************/
                    deployedJars.add(ejbModule);
                }

            } catch (OpenEJBException e) {
                ConfigUtils.logger.i18n.warning("conf.0004", jarFile.getAbsolutePath(), e.getMessage());
            }
        }

        for (EjbModule ejbModule : deployedJars) {
            logger.info("Loaded EJBs: " + ejbModule.getJarLocation());
        }
        return deployedJars;
    }

    private final Map<Class,JaxbUnmarshaller> unmarshallers = new HashMap();

    private <T> T unmarshal(Class<T> type, String descriptor, ClassLoader classLoader, URL jarUrl) throws OpenEJBException {
        URL descriptorUrl = getResource(jarUrl, classLoader, descriptor);

        if (descriptorUrl == null) {
            throw new OpenEJBException(descriptor + " not found.");
        }

        return unmarshal(type, descriptor, descriptorUrl);
    }

    private <T>T unmarshal(Class<T> type, String descriptor, URL descriptorUrl) throws OpenEJBException {
        JaxbUnmarshaller unmarshaller = unmarshallers.get(type);
        if (unmarshaller == null) {
            unmarshaller = new JaxbUnmarshaller(type, descriptor);
            unmarshallers.put(type, unmarshaller);
        }
        return (T) unmarshaller.unmarshal(descriptorUrl);
    }

    private File unpack(URL appXml, File jarFile) throws OpenEJBException {
        if (appXml.getProtocol().equals("jar")) {

            String name = jarFile.getName();
            if (name.endsWith(".jar") || name.endsWith(".ear") || name.endsWith(".zip")) {
                name = name.replaceFirst("....$", "");
            } else {
                name += "_app";
            }

            try {
                jarFile = JarExtractor.extract(appXml, name, jarFile);
            } catch (IOException e) {
                throw new OpenEJBException("Unable to extract jar. " + e.getMessage(), e);
            }
        } else if (!jarFile.isDirectory()) {
            throw new OpenEJBException("Application file must be a directory or jar");
        }
        return jarFile;
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
        ClassLoader classLoader;
        if (jarFile.isDirectory()) {
            try {
                URL[] urls = new URL[]{jarFile.toURL()};
                classLoader = new URLClassLoader(urls, OpenEJB.class.getClassLoader());
                //                        classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());

            } catch (MalformedURLException e) {
                throw new OpenEJBException(ConfigurationFactory.messages.format("cl0001", jarFile.getAbsolutePath(), e.getMessage()));
            }
        } else {
            TempCodebase tempCodebase = new TempCodebase(jarFile.getAbsolutePath());
            classLoader = tempCodebase.getClassLoader();
        }
        return classLoader;
    }

    private URL getResource(URL baseUrl, ClassLoader classLoader, String name) {
        try {
            String fileUrl = baseUrl.toExternalForm();
            URL appXmlUrl = null;
            Enumeration<URL> urls = classLoader.getResources(name);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String location = url.toExternalForm();
                location = location.replaceFirst("^jar:", "");
                if (location.startsWith(fileUrl)) {
                    appXmlUrl = url;
                    break;
                }
            }
            return appXmlUrl;
        } catch (IOException e) {
            throw new IllegalStateException("Bad resource in classpath.  Unable to search for entries. ", e);
        }
    }

    private void loadFromClasspath(FileUtils base, List<String> jarList, ClassLoader classLoader, String descriptor, String type) {
        try {
            String exclude = SystemInstance.get().getProperty("openejb.deployments.classpath.exclude", "");
            Enumeration resources = classLoader.getResources(descriptor);
            while (resources.hasMoreElements()) {
                URL ejbJar1 = (URL) resources.nextElement();
                String urlString = ejbJar1.toExternalForm();
                if (urlString.matches(exclude)) {
                    ConfigurationFactory.logger.info("Excluding: " + urlString);
                    continue;
                }
                String path = null;
                Deployments deployment = new Deployments();
                if (ejbJar1.getProtocol().equals("jar")) {
                    ejbJar1 = new URL(ejbJar1.getFile().replaceFirst("!.*$", ""));
                    File file = new File(ejbJar1.getFile());
                    path = file.getAbsolutePath();
                    deployment.setJar(path);
                } else if (ejbJar1.getProtocol().equals("file")) {
                    File file = new File(ejbJar1.getFile());
                    File metainf = file.getParentFile();
                    File ejbPackage = metainf.getParentFile();
                    path = ejbPackage.getAbsolutePath();
                    deployment.setDir(path);
                } else {
                    ConfigurationFactory.logger.warning("Not loading " + type + ".  Unknown protocol " + ejbJar1.getProtocol());
                    continue;
                }

                ConfigurationFactory.logger.info("Found " + type + " in classpath: " + path);
                loadFrom(deployment, base, jarList);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            ConfigurationFactory.logger.warning("Unable to search classpath for " + type + ": Received Exception: " + e1.getClass().getName() + " " + e1.getMessage(), e1);
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
