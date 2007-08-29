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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.config.sys.Deployments;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Logger;
import org.apache.xbean.finder.UrlSet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class DeploymentsResolver {

    private static final String CLASSPATH_INCLUDE = "openejb.deployments.classpath.include";
    private static final String CLASSPATH_EXCLUDE = "openejb.deployments.classpath.exclude";
    private static final String CLASSPATH_REQUIRE_DESCRIPTOR = "openejb.deployments.classpath.require.descriptor";
    private static final String CLASSPATH_FILTER_DESCRIPTORS = "openejb.deployments.classpath.filter.descriptors";
    private static final Logger logger = DeploymentLoader.logger;

    private static void loadFrom(Deployments dep, FileUtils path, List<String> jarList) {

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
        DeploymentLoader.scanDir(dir, files, "");
        for (String fileName : files.keySet()) {
            if (fileName.endsWith(".class")) {
                if (!jarList.contains(dir.getAbsolutePath())) {
                    jarList.add(dir.getAbsolutePath());
                }
                return;
            }
        }

        ////////////////////////////////
        //
        //  Directory contains Jar files
        //
        ////////////////////////////////
        for (String fileName : files.keySet()) {
            if (fileName.endsWith(".jar") || fileName.endsWith(".ear")) {
                File jar = new File(dir, fileName);

                if (jarList.contains(jar.getAbsolutePath())) continue;
                jarList.add(jar.getAbsolutePath());
            }
        }
    }

    public static List<String> resolveAppLocations(List<Deployments> deployments) {
        // make a copy of the list because we update it
        deployments = new ArrayList<Deployments>(deployments);

        //// getOption /////////////////////////////////  BEGIN  ////////////////////
        String flag = SystemInstance.get().getProperty("openejb.deployments.classpath", "true").toLowerCase();
        boolean searchClassPath = flag.equals("true");
        //// getOption /////////////////////////////////  END  ////////////////////

        if (searchClassPath) {
            Deployments deployment = JaxbOpenejb.createDeployments();
            deployment.setClasspath(Thread.currentThread().getContextClassLoader());
            deployments.add(deployment);
        }
        // resolve jar locations //////////////////////////////////////  BEGIN  ///////

        FileUtils base = SystemInstance.get().getBase();

        List<String> jarList = new ArrayList<String>(deployments.size());
        try {
            for (Deployments deployment : deployments) {
                if (deployment.getClasspath() != null) {
                    loadFromClasspath(base, jarList, deployment.getClasspath());
                } else {
                    loadFrom(deployment, base, jarList);
                }
            }
        } catch (SecurityException ignored) {
        }

        return jarList;
    }

    /**
     * The algorithm of OpenEJB deployments class-path inclusion and exclusion is implemented as follows:
     * 1- If the string value of the resource URL matches the include class-path pattern
     * Then load this resource
     * 2- If the string value of the resource URL matches the exclude class-path pattern
     * Then ignore this resource
     * 3- If the include and exclude class-path patterns are not defined
     * Then load this resource
     * <p/>
     * The previous steps are based on the following points:
     * 1- Include class-path pattern has the highst priority
     * This helps in case both patterns are defined using the same values.
     * This appears in step 1 and 2 of the above algorithm.
     * 2- Loading the resource is the default behaviour in case of not defining a value for any class-path pattern
     * This appears in step 3 of the above algorithm.
     */
    private static void loadFromClasspath(FileUtils base, List<String> jarList, ClassLoader classLoader) {

        Deployments deployment = null;
        String include = null;
        String exclude = null;
        String path = null;

        include = SystemInstance.get().getProperty(CLASSPATH_INCLUDE, "");
        exclude = SystemInstance.get().getProperty(CLASSPATH_EXCLUDE, ".*");
        boolean requireDescriptors = SystemInstance.get().getProperty(CLASSPATH_REQUIRE_DESCRIPTOR, "false").equalsIgnoreCase("true");
        boolean filterDescriptors = SystemInstance.get().getProperty(CLASSPATH_FILTER_DESCRIPTORS, "false").equalsIgnoreCase("true");

        logger.debug("Using "+CLASSPATH_INCLUDE+" '"+include+"'");
        logger.debug("Using "+CLASSPATH_EXCLUDE+" '"+exclude+"'");
        logger.debug("Using "+CLASSPATH_FILTER_DESCRIPTORS+" '"+filterDescriptors+"'");
        logger.debug("Using "+CLASSPATH_REQUIRE_DESCRIPTOR+" '"+requireDescriptors+"'");

        try {
            UrlSet urlSet = new UrlSet(classLoader);
            UrlSet includes = urlSet.matching(include);
            urlSet = urlSet.exclude(ClassLoader.getSystemClassLoader().getParent());
            urlSet = urlSet.excludeJavaExtDirs();
            urlSet = urlSet.excludeJavaEndorsedDirs();
            urlSet = urlSet.excludeJavaHome();
            urlSet = urlSet.excludePaths(System.getProperty("sun.boot.class.path", ""));
            urlSet = urlSet.exclude(".*/JavaVM.framework/.*");
            UrlSet prefiltered = urlSet;
            urlSet = urlSet.exclude(exclude);
            urlSet = urlSet.include(includes);

            List<URL> urls = urlSet.getUrls();
            int size = urls.size();
            if (size == 0 && include.length() > 0) {
                logger.warning("No classpath URLs matched.  Current settings: " + CLASSPATH_EXCLUDE + "='" + exclude + "', " + CLASSPATH_INCLUDE + "='" + include + "'");
                return;
            } else if (size == 0 && (!filterDescriptors && prefiltered.getUrls().size() == 0)) {
                return;
            } else if (size < 10) {
                logger.debug("Inspecting classpath for applications: " + urls.size() + " urls.");
            } else if (size < 50 && !requireDescriptors) {
                logger.info("Inspecting classpath for applications: " + urls.size() + " urls. Consider adjusting your exclude/include.  Current settings: " + CLASSPATH_EXCLUDE + "='" + exclude + "', " + CLASSPATH_INCLUDE + "='" + include + "'");
            } else if (!requireDescriptors) {
                logger.warning("Inspecting classpath for applications: " + urls.size() + " urls.");
                logger.warning("ADJUST THE EXCLUDE/INCLUDE!!!.  Current settings: " + CLASSPATH_EXCLUDE + "='" + exclude + "', " + CLASSPATH_INCLUDE + "='" + include + "'");
            }

            long begin = System.currentTimeMillis();
            processUrls(urls, classLoader, !requireDescriptors, base, jarList);
            long end = System.currentTimeMillis();
            long time = end - begin;

            if (!filterDescriptors){
                UrlSet unchecked = prefiltered.exclude(urlSet);
                processUrls(unchecked.getUrls(), classLoader, false, base, jarList);
            }

            if (urls.size() == 0) return;
            
            if (time < 1000) {
                logger.debug("Searched " + urls.size() + " classpath urls in " + time + " milliseconds.  Average " + (time / urls.size()) + " milliseconds per url.");
            } else if (time < 4000 || urls.size() < 3) {
                logger.info("Searched " + urls.size() + " classpath urls in " + time + " milliseconds.  Average " + (time / urls.size()) + " milliseconds per url.");
            } else if (time < 10000) {
                logger.warning("Searched " + urls.size() + " classpath urls in " + time + " milliseconds.  Average " + (time / urls.size()) + " milliseconds per url.");
                logger.warning("Consider adjusting your " + CLASSPATH_EXCLUDE + " and " + CLASSPATH_INCLUDE + " settings.  Current settings: exclude='" + exclude + "', include='" + include + "'");
            } else {
                logger.fatal("Searched " + urls.size() + " classpath urls in " + time + " milliseconds.  Average " + (time / urls.size()) + " milliseconds per url.  TOO LONG!");
                logger.fatal("ADJUST THE EXCLUDE/INCLUDE!!!.  Current settings: " + CLASSPATH_EXCLUDE + "='" + exclude + "', " + CLASSPATH_INCLUDE + "='" + include + "'");
                List<String> list = new ArrayList<String>();
                for (URL url : urls) {
                    list.add(url.toExternalForm());
                }
                Collections.sort(list);
                for (String url : list) {
                    logger.info("Matched: " + url);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            logger.warning("Unable to search classpath for modules: Received Exception: " + e1.getClass().getName() + " " + e1.getMessage(), e1);
        }

    }

    private static void processUrls(List<URL> urls, ClassLoader classLoader, boolean desc, FileUtils base, List<String> jarList) {
        Deployments deployment;
        String path;
        for (URL url : urls) {
            try {
                Class moduleType = DeploymentLoader.discoverModuleType(url, classLoader, desc);
                if (AppModule.class.isAssignableFrom(moduleType) || EjbModule.class.isAssignableFrom(moduleType)) {
                    deployment = JaxbOpenejb.createDeployments();
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
                logger.warning("Unable to determine the module type of " + url.toExternalForm() + ": Exception: " + e.getMessage(), e);
            } catch (UnknownModuleTypeException ignore) {
            }
        }
    }
}
