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
package org.apache.openejb.cli;

import org.apache.xbean.finder.ResourceFinder;
import org.apache.openejb.loader.SystemInstance;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.JarURLConnection;

/**
 * Entry point for ALL things OpenEJB.  This will use the new service
 * architecture explained here:
 *
 * @link http://docs.codehaus.org/display/OPENEJB/Executables
 * 
 * @version $Rev$ $Date$
 */
public class MainImpl implements Main {

    private static final String BASE_PATH = "META-INF/org.apache.openejb.cli/";
    private static final String MAIN_CLASS_PROPERTY_NAME = "main.class";

    private static ResourceFinder finder = null;
    private static String locale = "";
    private static String descriptionBase = "description";

    public void main(String[] args) {
        ArrayList<String> argsList = new ArrayList<String>();

        // We have to pre-screen for openejb.base as it has a direct affect
        // on where we look for the conf/system.properties file which we
        // need to read in and apply before we apply the command line -D
        // properties.  Once SystemInstance.init() is called in the next
        // section of code, the openejb.base value is cemented and cannot
        // be changed.
        for (String arg : args) {
            if (arg.indexOf("-Dopenejb.base") != -1) {
                String prop = arg.substring(arg.indexOf("-D") + 2, arg.indexOf("="));
                String val = arg.substring(arg.indexOf("=") + 1);

                System.setProperty(prop, val);
            }
        }

        // get SystemInstance (the only static class in the system)
        // so we'll set up all the props in it
        SystemInstance systemInstance = null;
        try {
            SystemInstance.init(System.getProperties());
            systemInstance = SystemInstance.get();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Read in and apply the conf/system.properties
        try {
            File conf = systemInstance.getBase().getDirectory("conf");
            File file = new File(conf, "system.properties");
            if (file.exists()){
                Properties systemProperties = new Properties();
                FileInputStream fin = new FileInputStream(file);
                InputStream in = new BufferedInputStream(fin);
                systemProperties.load(in);
                System.getProperties().putAll(systemProperties);
            }
        } catch (IOException e) {
            System.out.println("Processing conf/system.properties failed: "+e.getMessage());
        }

        // Now read in and apply the properties specified on the command line
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.indexOf("-D") != -1) {
                String prop = arg.substring(arg.indexOf("-D") + 2, arg.indexOf("="));
                String val = arg.substring(arg.indexOf("=") + 1);

                System.setProperty(prop, val);
            } else {
                argsList.add(arg);
            }
        }

        args = (String[]) argsList.toArray(new String[argsList.size()]);

        finder = new ResourceFinder(BASE_PATH);
        locale = Locale.getDefault().getLanguage();


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

        String mainClass = props.getProperty(MAIN_CLASS_PROPERTY_NAME);
        if (mainClass == null) {
            throw new NullPointerException("Command " + commandName + " did not specify a " + MAIN_CLASS_PROPERTY_NAME + " property");
        }

        Class<?> clazz = null;
        try {
            clazz = Thread.currentThread().getContextClassLoader().loadClass(mainClass);
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalStateException("Main class of command " + commandName + " does not exist: " + mainClass, cnfe);
        }

        Method mainMethod = null;
        try {
            mainMethod = clazz.getMethod("main", new Class[]{String[].class});
        } catch (Exception e) {
            throw new IllegalStateException("Main class of command " + commandName + " does not have a static main method: " + mainClass, e);
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

    //DMB: TODO: Delete me
    public static Enumeration<URL> doFindCommands() throws IOException {
        return Thread.currentThread().getContextClassLoader().getResources(BASE_PATH);
    }

    private static void printAvailableCommands() {
        System.out.println("COMMANDS:");

        try {
            Enumeration<URL> commandHomes = doFindCommands();

            if (commandHomes != null) {
                for (; commandHomes.hasMoreElements();) {
                    URL cHomeURL = commandHomes.nextElement();
                    JarURLConnection conn = (JarURLConnection) cHomeURL.openConnection();
                    JarFile jarfile = conn.getJarFile();
                    Enumeration<JarEntry> commands = jarfile.entries();
                    if (commands != null) {
                        while (commands.hasMoreElements()) {
                            JarEntry je = commands.nextElement();

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
                System.out.println("No commands available!");
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
