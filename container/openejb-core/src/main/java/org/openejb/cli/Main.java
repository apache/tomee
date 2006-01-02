package org.openejb.cli;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.openejb.loader.SystemClassPath;

public class Main {
    private static CommandFinder finder = null;
    private static String basePath = "META-INF/org.openejb.cli/";
    private static String locale = "";
    private static String descriptionBase = "description";

    public static void init() {
        finder = new CommandFinder(basePath);
        locale = Locale.getDefault().getLanguage();

        setupClasspath();
    }

    public static void setupClasspath() {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        URL classURL = Thread.currentThread().getContextClassLoader().getResource(basePath + "start");
        String propsString = classURL.getFile();
        URL jarURL = null;
        File jarFile = null;

        propsString = propsString.substring(0, propsString.indexOf("!"));

        try {
            jarURL = new URL(propsString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        jarFile = new File(jarURL.getFile());

        if (jarFile.getName().indexOf("openejb-core") > -1) {
            File lib = jarFile.getParentFile();
            File home = lib.getParentFile();

            System.setProperty("openejb.home", home.getAbsolutePath());
        }

        File lib = new File(System.getProperty("openejb.home") + File.separator + "lib");
        SystemClassPath systemCP = new SystemClassPath();

        try {
            systemCP.addJarsToPath(lib);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        init();

        if (args.length > 0) {
            Properties props = null;

            if (args[0].equals("--help")) {
                System.out.println("Usage: openejb help [command]");

                printAvailableCommands();
            } else {
                String mainClass = null;
                Class clazz = null;

                try {
                    props = finder.doFindCommandProperies(args[0]);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                if (props != null) {
                    mainClass = props.getProperty("main.class");
                }

                try {
                    clazz = Thread.currentThread().getContextClassLoader().loadClass(mainClass);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                Method mainMethod = null;

                try {
                    mainMethod = clazz.getMethod("main", new Class[]{String[].class});
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

                String[] trimmedArgs = null;

                if (args.length > 1) {
                    trimmedArgs = new String[args.length - 1];

                    System.arraycopy(args, 1, trimmedArgs, 0, args.length - 1);
                } else {
                    trimmedArgs = new String[0];
                }

                try {
                    mainMethod.invoke(clazz, new Object[]{trimmedArgs});
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Usage: openejb command [command-options-and-arguments]\n");

            printAvailableCommands();

            System.exit(1);
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

                            if (je.getName().indexOf(basePath) > -1 && !je.getName().equals(basePath) && !je.getName().endsWith(".help") && !je.getName().endsWith(".examples")) {
                                Properties props = finder.doFindCommandProperies(je.getName().substring(je.getName().lastIndexOf("/") + 1));

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