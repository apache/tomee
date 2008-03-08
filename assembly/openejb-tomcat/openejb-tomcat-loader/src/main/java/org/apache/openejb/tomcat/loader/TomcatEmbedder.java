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
import java.util.Properties;

public class TomcatEmbedder {
    public static void embed(Properties properties, ClassLoader catalinaCl) {
        if (catalinaCl == null) throw new NullPointerException("catalinaCl is null");
        if (properties == null) throw new NullPointerException("properties is null");

        if (!properties.containsKey("openejb.war")) {
            throw new IllegalArgumentException("properties must contain the openejb.war property");
        }
        File openejbWar = new File(properties.getProperty("openejb.war"));
        if (!openejbWar.isDirectory()) {
            throw new IllegalArgumentException("openejb.war is not a directory: " + openejbWar);
        }

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(catalinaCl);
        try {
            // WebappClassLoader childCl = new WebappClassLoader(catalinaCl)
            Class<?> webappClClass = catalinaCl.loadClass("org.apache.catalina.loader.WebappClassLoader");
            ClassLoader childCl = (ClassLoader) webappClClass.getConstructor(ClassLoader.class).newInstance(catalinaCl);

            // childCl.addRepository(openejb-tomcat-loader.jar)
            File thisJar = getThisJar();
            webappClClass.getMethod("addRepository", String.class).invoke(childCl, thisJar.toURI().toString());

            // childCl.addRepository(openejb-loader.jar)
            File jarFile = findOpenEJBJar(openejbWar, "openejb-loader");
            webappClClass.getMethod("addRepository", String.class).invoke(childCl, jarFile.toURI().toString());

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

            URI uri = classURL.toURI();
            if (uri.getPath() == null){
                uri = new URI(uri.getRawSchemeSpecificPart());
            }

            String path = uri.getPath();
            if (path.contains("!")){
                path = path.substring(0, path.indexOf('!'));
            } else {
                path = path.substring(0, path.length() - classFileName.length());
            }

            return new File(path);
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
