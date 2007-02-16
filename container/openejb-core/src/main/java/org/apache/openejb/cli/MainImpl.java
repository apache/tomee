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
 */
public class MainImpl implements Main {

    private static ResourceFinder finder = null;
    private static final String BASE_PATH = "META-INF/org.apache.openejb.cli/";
    private static String locale = "";
    private static String descriptionBase = "description";

    public void main(String[] args) {
        ArrayList<String> argsList = new ArrayList<String>();

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

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.indexOf("-D") == -1) {
                argsList.add(arg);
            } else {
                String prop = arg.substring(arg.indexOf("-D") + 2, arg.indexOf("="));
                String val = arg.substring(arg.indexOf("=") + 1);

                System.setProperty(prop, val);
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

    //DMB: TODO: Delete me
    public static Enumeration doFindCommands() throws IOException {
        return Thread.currentThread().getContextClassLoader().getResources(BASE_PATH);
    }

    private static void printAvailableCommands() {
        System.out.println("COMMANDS:");

        try {
            Enumeration commandHomes = doFindCommands();

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
