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
package org.apache.openejb.tomcat.loader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Properties;

public class TomcatEmbedder {
	/**
	 * 
	 * @param properties this instance contains all System properties as well as all initialization parameters of the LoaderServlet
	 * @param catalinaCl The ClassLoader which loaded the ServletConfig class
	 */
    public static void embed(Properties properties, ClassLoader catalinaCl) {
        if (catalinaCl == null) throw new NullPointerException("catalinaCl is null");
        if (properties == null) throw new NullPointerException("properties is null");

        if (!properties.containsKey("openejb.war")) {
            throw new IllegalArgumentException("properties must contain the openejb.war property");
        }
        // openejbWar represents the absolute path of the openejb webapp i.e. the openejb directory
        File openejbWar = new File(properties.getProperty("openejb.war"));
        if (!openejbWar.isDirectory()) {
            throw new IllegalArgumentException("openejb.war is not a directory: " + openejbWar);
        }
        // retrieve the current ClassLoader
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        // set the ClassLoader to the one which loaded ServletConfig.class i.e. the parent ClassLoader
        Thread.currentThread().setContextClassLoader(catalinaCl);
        try {
            // WebappClassLoader childCl = new WebappClassLoader(catalinaCl)
            Class<?> webappClClass = catalinaCl.loadClass("org.apache.catalina.loader.WebappClassLoader");
            ClassLoader childCl = (ClassLoader) webappClClass.getConstructor(ClassLoader.class).newInstance(catalinaCl);

            // childCl.addRepository(openejb-tomcat-loader.jar)
            // Use reflection to add the openejb-tomcat-loader.jar file to the repository of WebappClassLoader. 
            // WebappClassLoader will now search for classes in this jar too
            File thisJar = getThisJar();
            String thisJarUrl = thisJar.toURI().toString();
            webappClClass.getMethod("addRepository", String.class).invoke(childCl, thisJarUrl);

            // childCl.addRepository(openejb-loader.jar)
            // Use reflection to add the openejb-loader.jar file to the repository of WebappClassLoader. 
            // WebappClassLoader will now search for classes in this jar too

            File jarFile = findOpenEJBJar(openejbWar, "openejb-loader");
            String openejbLoaderUrl = jarFile.toURI().toString();

            webappClClass.getMethod("addRepository", String.class).invoke(childCl, openejbLoaderUrl);

            // childCl.start()
            webappClClass.getMethod("start").invoke(childCl);

            // TomcatHook.hook()
            Class<?> tomcatUtilClass = childCl.loadClass("org.apache.openejb.tomcat.loader.TomcatHook");
            Method hookMethod = tomcatUtilClass.getDeclaredMethod("hook", Properties.class);
            hookMethod.setAccessible(true);
            hookMethod.invoke(null, properties);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    private static File getThisJar() {
        return jarLocation(TomcatEmbedder.class);
    }

    private static File jarLocation(Class clazz) {
        try {
            String classFileName = clazz.getName().replace(".", "/") + ".class";

            URL classURL = clazz.getClassLoader().getResource(classFileName);

            URI uri = null;
            String url = classURL.toExternalForm();
            if (url.contains("+")) {
                url = url.replaceAll("\\+", "%2B");
            }

            if (url.contains(" ")) {
                url = url.replaceAll(" ", "%20");
            }
            uri = new URI(url);

            if (uri.getPath() == null){
                uri = new URI(uri.getRawSchemeSpecificPart());
            }

            String path = uri.getPath();
            if (path.contains("!")){
                path = path.substring(0, path.indexOf('!'));
            } else {
                path = path.substring(0, path.length() - classFileName.length());
            }

            path = path.replaceAll("\\+", "%2B");
            return new File(URLDecoder.decode(path));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static File findOpenEJBJar(File openejbWar, String namePrefix) {
        File openEJBLibDir = new File(openejbWar, "lib");
        if (openEJBLibDir == null) return null;

        File openejbLoaderJar = null;
        for (File file : openEJBLibDir.listFiles()) {
            if (file.getName().startsWith(namePrefix + "-") && file.getName().endsWith(".jar")) {
                return file;
            }
        }

        return openejbLoaderJar;
    }
}
