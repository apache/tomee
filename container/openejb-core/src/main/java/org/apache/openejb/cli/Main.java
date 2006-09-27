package org.apache.openejb.cli;

import org.apache.openejb.loader.SystemClassPath;
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
    private static String basePath = "META-INF/org.apache.openejb.cli/";
    private static String locale = "";
    private static String descriptionBase = "description";

    private static void setupHome() {
        try {
            URL classURL = Thread.currentThread().getContextClassLoader().getResource(basePath + "start");

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

    public static void main(String[] args) {
        ArrayList argsList = new ArrayList();

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

        finder = new ResourceFinder(basePath);
        locale = Locale.getDefault().getLanguage();

        setupHome();
        setupClasspath();

        if (args.length == 0 || args[0].equals("--help")) {
            System.out.println("Usage: openejb help [command]");

            printAvailableCommands();

            return;
        }

        Properties props = null;
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

        argsList = new ArrayList();
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

                            if (je.getName().indexOf(basePath) > -1 && !je.getName().equals(basePath) && !je.getName().endsWith(".help") && !je.getName().endsWith(".examples"))
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
        System.out.println("OpenEJB -- EJB Container System and EJB Server.");
        System.out.println("For updates and additional information, visit");
        System.out.println("http://www.openejb.org\n");
        System.out.println("Bug Reports to <user@openejb.org>");
    }
}