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
package org.apache.openejb.cli;

import org.apache.openejb.loader.SystemClassPath;

import java.io.File;
import java.net.URI;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public class Bootstrap {

    private final static String OPENEJB_VERSION_PROPERTIES_FILE_NAME = "openejb-version.properties";
    private final static String OPENEJB_HOME_PROPERTY_NAME = "openejb.home";
    private final static String OPENEJB_BASE_PROPERTY_NAME = "openejb.base";
    private final static String OPENEJB_CLI_MAIN_CLASS_NAME = "org.apache.openejb.cli.MainImpl";

    private static void setupHome(final String[] args) {
        for (final String arg : args) {
            if (arg.startsWith("-D" + OPENEJB_HOME_PROPERTY_NAME)) {
                addProperty(arg);
            } else if (arg.startsWith("-D" + OPENEJB_BASE_PROPERTY_NAME)) {
                addProperty(arg);
            }
        }

        final String homeProperty = System.getProperty(OPENEJB_HOME_PROPERTY_NAME);
        if (homeProperty != null) {
            if (new File(homeProperty).exists()) {
                return;
            }
        }

        try {
            final URL classURL = Thread.currentThread().getContextClassLoader().getResource(OPENEJB_VERSION_PROPERTIES_FILE_NAME);

            if (classURL != null) {
                String propsString = classURL.getFile();

                propsString = propsString.substring(0, propsString.indexOf("!"));

                final URI uri = new URI(propsString);

                final File jarFile = new File(uri.getSchemeSpecificPart());

                if (jarFile.getName().contains("openejb-core")) {
                    final File lib = jarFile.getParentFile();
                    final File home = lib.getParentFile().getCanonicalFile();

                    System.setProperty(OPENEJB_HOME_PROPERTY_NAME, home.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("Error setting " + OPENEJB_HOME_PROPERTY_NAME + " property: " + e.getClass() + ": " + e.getMessage());
        }
    }

    private static void addProperty(final String arg) {
        final String prop = arg.substring(arg.indexOf("-D") + 2, arg.indexOf("="));
        final String val = arg.substring(arg.indexOf("=") + 1);

        System.setProperty(prop, val);
    }

    private static void setupClasspath() {
        try {
            final File lib = new File(System.getProperty(OPENEJB_HOME_PROPERTY_NAME) + File.separator + "lib");
            final SystemClassPath systemCP = new SystemClassPath();
            systemCP.addJarsToPath(lib);
        } catch (Exception e) {
            System.err.println("Error setting up the classpath: " + e.getClass() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Read commands from BASE_PATH (using XBean's ResourceFinder) and execute the one specified on the command line
     */
    public static void main(final String[] args) throws Exception {
        setupHome(args);
        setupClasspath();

        final Class<?> clazz = Bootstrap.class.getClassLoader().loadClass(OPENEJB_CLI_MAIN_CLASS_NAME);
        final Main main = (Main) clazz.newInstance();
        try {
            main.main(args);
        } catch (SystemExitException e) {
            System.exit(e.getExitCode());
        }
    }

}
