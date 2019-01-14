/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.loader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URI;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Properties;

/**
 * Ultimately this class does nothing lasting and just calls {@link TomcatHook#hook}
 *
 * This class needs to know the path to the tomee.war file.
 *
 * With that information this class finds the openejb-loader jar in the tomee.war
 * essentially creates a "mini-webapp" which is to say it creates a new WebappClassloader
 * that contains the openejb-loader jar and then uses that classloader to reflectively
 * call the {@link TomcatHook#hook} method which does all the work to load OpenEJB into Tomcat
 *
 * This messing around is required so that it doesn't matter if the {@link OpenEJBListener},
 * which does not execute in a webapp classloader, or the {@link LoaderServlet}, which does,
 * calls the TomcatEmbedder.  Either way the embedding process starts inside a WebappClassloader
 * and keeps that very complex code just a little simpler.
 */
public class TomcatEmbedder {

    /**Prefix for jar openejb-loader*/
    private static final String OPENEJB_LOADER_PREFIX = "openejb-loader";

    /**OpenEJB War name*/
    private static final String TOMEE_WAR_NAME = "tomee.war";

    /**
     * Starts to embed process.
     * @param properties this instance contains all System properties as well as all initialization parameters of the LoaderServlet
     * @param catalinaCl The ClassLoader which loaded the ServletConfig class
     */
    public static void embed(final Properties properties, final ClassLoader catalinaCl) {
        if (catalinaCl == null) {
            throw new NullPointerException("catalinaCl is null");
        }
        if (properties == null) {
            throw new NullPointerException("properties is null");
        }

        if (!properties.containsKey(TOMEE_WAR_NAME)) {
            throw new IllegalArgumentException("properties must contain the tomee.war property");
        }
        // openejbWar represents the absolute path of the openejb webapp i.e. the openejb directory
        final File openejbWar = new File(properties.getProperty(TOMEE_WAR_NAME));
        if (!openejbWar.isDirectory()) {
            throw new IllegalArgumentException("tomee.war is not a directory: " + openejbWar);
        }
        // retrieve the current ClassLoader
        final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        // set the ClassLoader to the one which loaded ServletConfig.class i.e. the parent ClassLoader
        Thread.currentThread().setContextClassLoader(catalinaCl);
        URLClassLoader childCl = null;
        try {
            childCl = new URLClassLoader(new URL[]{
                    getThisJar().toURI().toURL(),
                    findOpenEJBJar(openejbWar, OPENEJB_LOADER_PREFIX).toURI().toURL()
            });

            // TomcatHook.hook()
            //This is loaded by childCl and is defined in the tomee-loader
            final Class<?> tomcatUtilClass = childCl.loadClass("org.apache.tomee.loader.TomcatHook");
            final Method hookMethod = tomcatUtilClass.getDeclaredMethod("hook", Properties.class);
            hookMethod.setAccessible(true);
            hookMethod.invoke(null, properties);
        } catch (final Throwable e) {
            e.printStackTrace();
        } finally {
            if (childCl != null) {
                try {
                    childCl.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    /**
     * Return path to jar file that contains this class.
     * <p>
     * Normally, tomee.war/lib/tomee-loader.jar
     * </p>
     * @return path to jar file that contains this class
     */
    private static File getThisJar() {
        return jarLocation(TomcatEmbedder.class);
    }

    /**
     * Return location of the jar file that contains given class.
     * @param clazz class file
     * @return location of the jar file that contains given class
     */
    private static File jarLocation(final Class<?> clazz) {
        try {
            final String classFileName = clazz.getName().replace(".", "/") + ".class";

            final URL classURL = clazz.getClassLoader().getResource(classFileName);

            URI uri = null;
            String url = classURL.toExternalForm();
            if (url.contains("+")) {
                url = url.replaceAll("\\+", "%2B");
            }

            if (url.contains(" ")) {
                url = url.replaceAll(" ", "%20");
            }

            if (url.contains("#")) {
                url = url.replaceAll("#", "%23");
            }

            uri = new URI(url);

            if (uri.getPath() == null) {
                uri = new URI(uri.getRawSchemeSpecificPart());
            }

            String path = uri.getPath();
            if (path.contains("!")) {
                path = path.substring(0, path.indexOf('!'));
            } else {
                path = path.substring(0, path.length() - classFileName.length());
            }

            path = path.replaceAll("\\+", "%2B");
            return new File(URLDecoder.decode(path));
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Gets path to jar file that has namePrefix
     * and in the tomee.war/lib location.
     * @param tomeeWar path to tomee.war
     * @param namePrefix prefix of the jar file
     * @return path to file
     */
    private static File findOpenEJBJar(final File tomeeWar, final String namePrefix) {
        final File openEJBLibDir = new File(tomeeWar, "lib");
        if (openEJBLibDir == null) {
            return null;
        }

        final File openejbLoaderJar = null;
        final File[] files = openEJBLibDir.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.getName().startsWith(namePrefix + "-") && file.getName().endsWith(".jar")) {
                    return file;
                }
            }
        }

        return openejbLoaderJar;
    }
}
