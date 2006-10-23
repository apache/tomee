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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.cli;

import org.apache.openejb.loader.SystemClassPath;
import org.apache.openejb.loader.SystemInstance;
import org.apache.xbean.finder.ResourceFinder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Entry point for ALL things OpenEJB.  This will use the new service
 * architecture explained here:
 *
 * @link http://docs.codehaus.org/display/OPENEJB/Executables
 */
public class Main {
    private static ResourceFinder finder = null;
    private static final String BASE_PATH = "META-INF/org.apache.openejb.cli/";
    private static String locale = "";
    private static String descriptionBase = "description";

    private static void setupHome() {
        try {
            URL classURL = Thread.currentThread().getContextClassLoader().getResource(BASE_PATH + "start");

            if (classURL != null) {
                String propsString = classURL.getFile();

                propsString = propsString.substring(0, propsString.indexOf("!"));

                URI uri = new URI(propsString);

                File jarFile = new File(uri);

                if (jarFile.getName().indexOf("openejb-core") > -1) {
                    File lib = jarFile.getParentFile();
                    File home = lib.getParentFile();

                    System.setProperty("openejb.home", home.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("Error setting openejb.home :" + e.getClass() + ": " + e.getMessage());
        }
    }

    private static void setupClasspath() {
        try {
            File lib = new File(System.getProperty("openejb.home") + File.separator + "lib");
            SystemClassPath systemCP = new SystemClassPath();
            systemCP.addJarsToPath(lib);
        } catch (Exception e) {
            System.err.println("Error setting up the classpath: " + e.getClass() + ": " + e.getMessage());
        }
    }

    /**
     * Read commands from BASE_PATH (using XBean's ResourceFinder) and execute the one specified on the command line
     * 
     * TODO: There must be a better way to read command line args and spawn a command
     */
    public static void main(String[] args) {
        ArrayList<String> argsList = new ArrayList<String>();

        // get SystemInstance (the only static class in the system)
        // so we'll set up all the props in it
        // FIXME: The first candidate for XBean'ization - external dependency
        SystemInstance system = null;
        try {
            SystemInstance.init(System.getProperties());
            system = SystemInstance.get();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // FIXME: Why do we bother to process env vars? Remove it and leave it to JVM
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.indexOf("-D") == -1) {
                argsList.add(arg);
            } else {
                String prop = arg.substring(arg.indexOf("-D") + 2, arg.indexOf("="));
                String val = arg.substring(arg.indexOf("=") + 1);

                // It might confuse readers - watch out lower-case system is ours...
                system.setProperty(prop, val);
                // ...whereas System is Java's and should be removed as the lower-case version has been verified to work fine
                System.setProperty(prop, val);
            }
        }

        args = (String[]) argsList.toArray(new String[argsList.size()]);

        finder = new ResourceFinder(BASE_PATH);
        locale = Locale.getDefault().getLanguage();

        setupHome();
        setupClasspath();

        if (args.length == 0 || args[0].equals("--help")) {
            System.out.println("Usage: openejb help [command]");

            printAvailableCommands();

            return;
        }

        boolean help = false;
        int argIndex = 0;

        if (args[0].equals("help")) {
            if (args.length < 2) {
                printAvailableCommands();

                return;
            }

            help = true;

            argIndex = 1;
        }

        String commandName = args[argIndex];

        Properties props = null;
        try {
            props = finder.findProperties(commandName);
        } catch (IOException e1) {
            System.out.println("Unavailable command: " + commandName);

            printAvailableCommands();

            return;
        }

        if (props == null) {
            System.out.println("Unavailable command: " + commandName);
            printAvailableCommands();
            return;
        }

        String mainClass = props.getProperty("main.class");
        if (mainClass == null) {
            throw new NullPointerException("Command " + commandName + " did not specify a main.class property");
        }

        Class clazz = null;
        try {
            clazz = Thread.currentThread().getContextClassLoader().loadClass(mainClass);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Command " + commandName + " main.class does not exist: " + mainClass);
        }

        Method mainMethod = null;
        try {
            mainMethod = clazz.getMethod("main", new Class[]{String[].class});
        } catch (Exception e) {
            throw new IllegalStateException("Main class of command " + commandName + " does not have a static main method: " + mainClass);
        }

        argsList.clear();
        
        int startPoint = 1;

        if (help) {
            startPoint = 2;

            argsList.add("--help");
        }

        for (int i = startPoint; i < args.length; i++) {
            argsList.add(args[i]);
        }

        args = (String[]) argsList.toArray(new String[argsList.size()]);

        try {
            mainMethod.invoke(clazz, new Object[]{args});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void printAvailableCommands() {
        System.out.println("COMMANDS:");

        try {
            Enumeration commandHomes = finder.doFindCommands();

            if (commandHomes != null) {
                for (; commandHomes.hasMoreElements();) {
                    URL cHomeURL = (URL) commandHomes.nextElement();
                    JarURLConnection conn = (JarURLConnection) cHomeURL.openConnection();
                    JarFile jarfile = conn.getJarFile();
                    Enumeration commands = jarfile.entries();

                    if (commands != null) {
                        while (commands.hasMoreElements()) {
                            JarEntry je = (JarEntry) commands.nextElement();

                            if (je.getName().indexOf(BASE_PATH) > -1 && !je.getName().equals(BASE_PATH) && !je.getName().endsWith(".help") && !je.getName().endsWith(".examples"))
                            {
                                Properties props = finder.findProperties(je.getName().substring(je.getName().lastIndexOf("/") + 1));

                                String key = locale.equals("en") ? descriptionBase : descriptionBase + "." + locale;

                                System.out.println("\n  " + props.getProperty("name") + " - " + props.getProperty(key));
                            }
                        }
                    }
                }
            } else {
                System.out.println("No available commands!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\nTry 'openejb help <command>' for more information about the command.\n");
        System.out.println("Apache OpenEJB -- EJB Container System and EJB Server.");
        System.out.println("For updates and additional information, visit\n");
        System.out.println("   http://incubator.apache.org/openejb\n");
        System.out.println("Bug Reports to <openejb-user@incubator.apache.org>");
    }
}