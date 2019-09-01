/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.loader;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.io.File;

public class OpenEJBInstance {
    private final Method init;
    private final Method isInitialized;

    public OpenEJBInstance() throws Exception {
        final Class<?> openejb = loadOpenEJBClass();
        this.init = openejb.getMethod("init", Properties.class);
        this.isInitialized = openejb.getMethod("isInitialized");
    }
    
    public void init(final Properties props) throws Exception {
        try {
            init.invoke(null, props);
        } catch (final InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw (Error) e.getCause();
        } catch (final Exception e) {
            throw new LoaderRuntimeException("OpenEJB.init: ", e);
        }
    }

    public boolean isInitialized() {
        try {
            return (Boolean) isInitialized.invoke(null);
        } catch (final InvocationTargetException e) {
            throw new LoaderRuntimeException("OpenEJB.isInitialized: ", e.getCause());
        } catch (final Exception e) {
            throw new LoaderRuntimeException("OpenEJB.isInitialized: ", e);
        }
    }

    private Class<?> loadOpenEJBClass() throws Exception {
        final ClassPath classPath = SystemInstance.get().getClassPath();
        final ClassLoader classLoader = classPath.getClassLoader();
        try {
            return classLoader.loadClass("org.apache.openejb.OpenEJB");
        } catch (final Exception e) {
            try {
                checkOpenEjbHome(SystemInstance.get().getHome().getDirectory());
                final FileUtils home = SystemInstance.get().getHome();
                classPath.addJarsToPath(home.getDirectory("lib"));
            } catch (final Exception e2) {
                throw new Exception("Could not load OpenEJB libraries. Exception: " + e2.getClass().getName() + " " + e2.getMessage());
            }
            try {
                return classLoader.loadClass("org.apache.openejb.OpenEJB");
            } catch (final Exception e2) {
                throw new Exception("Could not load OpenEJB class after embedding libraries. Exception: " + e2.getClass().getName() + " " + e2.getMessage());
            }
        }
    }

    static final String NO_HOME = "The openejb.home is not set.";

    static final String BAD_HOME = "Invalid openejb.home: ";

    static final String NOT_THERE = "The path specified does not exist.";

    static final String NOT_DIRECTORY = "The path specified is not a directory.";

    static final String NO_DIST = "The path specified is not correct, it does not contain a 'dist' directory.";

    static final String NO_LIBS = "The path specified is not correct, it does not contain any OpenEJB libraries.";

    static final String INSTRUCTIONS = "Please edit the web.xml of the openejb_loader webapp and set the openejb.home init-param to the full path where OpenEJB is installed.";

    private void checkOpenEjbHome(final File openejbHome) throws Exception {
        try {

            final String homePath = openejbHome.getAbsolutePath();

            if (!openejbHome.exists()) {
                handleError(BAD_HOME + homePath, NOT_THERE, INSTRUCTIONS);
            }

            if (!openejbHome.isDirectory()) {
                handleError(BAD_HOME + homePath, NOT_DIRECTORY, INSTRUCTIONS);
            }

            final File openejbHomeLibs = new File(openejbHome, "lib");
            if (!openejbHomeLibs.exists()) {
                handleError(BAD_HOME + homePath, NO_DIST, INSTRUCTIONS);
            }

            final String[] libs = openejbHomeLibs.list();
            boolean found = false;
            for (int i = 0; i < libs.length && !found; i++) {
                found = (libs[i].startsWith("openejb-") && libs[i].endsWith(".jar"));
            }
            if (!found) {
                handleError(BAD_HOME + homePath, NO_LIBS, INSTRUCTIONS);
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void handleError(final String m1, final String m2, final String m3) throws Exception {
        System.err.println("--[PLEASE FIX]-------------------------------------");
        System.err.println(m1);
        System.err.println(m2);
        System.err.println(m3);
        System.err.println("---------------------------------------------------");
        throw new Exception(m1 + " " + m2 + " " + m3);
    }
}
