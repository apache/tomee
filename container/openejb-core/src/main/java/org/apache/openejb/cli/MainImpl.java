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
import org.apache.openejb.util.OpenEjbVersion;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Map;
import java.util.List;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;

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
    private static String descriptionI18n;

    public void main(String[] args) {
        args = processSystemProperties(args);

        finder = new ResourceFinder(BASE_PATH);
        locale = Locale.getDefault().getLanguage();
        descriptionI18n = descriptionBase + "." + locale;


        CommandLineParser parser = new PosixParser();

        // create the Options
        Options options = new Options();
        options.addOption(null, "version", false, "");
        options.addOption("h", "help", false, "");
        options.addOption("e", "errors", false, "Produce execution error messages");

        CommandLine line = null;
        String commandName = null;
        try {
            // parse the arguments up until the first
            // command, then let the rest fall into
            // the arguments array.
            line = parser.parse(options, args, true);

            // Get and remove the commandName (first arg)
            List<String> list = line.getArgList();
            if (list.size() > 0){
                commandName = list.get(0);
                list.remove(0);
            }

            // The rest of the args will be passed to the command
            args = line.getArgs();
        } catch (ParseException exp) {
            exp.printStackTrace();
            System.exit(-1);
        }

        if (line.hasOption("version")) {
            OpenEjbVersion.get().print(System.out);
            System.exit(0);
        } else if (line.hasOption("help") || commandName == null || commandName.equals("help")) {
            help();
            System.exit(0);
        }


        Properties props = null;
        try {
            props = finder.findProperties(commandName);
        } catch (IOException e1) {
            System.out.println("Unavailable command: " + commandName);

            help(false);

            System.exit(1);
        }

        if (props == null) {
            System.out.println("Unavailable command: " + commandName);
            help(false);

            System.exit(1);
        }

        // Shift the command name itself off the args list

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
            mainMethod = clazz.getMethod("main", String[].class);
        } catch (Exception e) {
            throw new IllegalStateException("Main class of command " + commandName + " does not have a static main method: " + mainClass, e);
        }

        try {
            // WARNING, Definitely do *not* unwrap 'new Object[]{args}' to 'args'
            mainMethod.invoke(clazz, new Object[]{args});
        } catch (Throwable e) {
            if (line.hasOption("errors")) {
                e.printStackTrace();
            }   
            System.exit(-10);
        }
    }

    private String[] processSystemProperties(String[] args) {
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
            System.exit(2);
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
        SystemInstance.get().getProperties().putAll(System.getProperties());
        args = (String[]) argsList.toArray(new String[argsList.size()]);
        return args;
    }

    //DMB: TODO: Delete me
    public static Enumeration<URL> doFindCommands() throws IOException {
        return Thread.currentThread().getContextClassLoader().getResources(BASE_PATH);
    }

    private static void help() {
        help(true);
    }
    private static void help(boolean printHeader) {

        // Here we are using commons-cli to create the list of available commands
        // We actually use a different Options object to parse the 'openejb' command
        try {
            Options options = new Options();

            ResourceFinder commandFinder = new ResourceFinder("META-INF");
            Map<String, Properties> commands = commandFinder.mapAvailableProperties("org.apache.openejb.cli");
            for (Map.Entry<String, Properties> command : commands.entrySet()) {
                if (command.getKey().contains(".")) continue;
                Properties p = command.getValue();
                String description = p.getProperty(descriptionI18n, p.getProperty(descriptionBase));
                options.addOption(command.getKey(), false, description);
            }

            HelpFormatter formatter = new HelpFormatter();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            String syntax = "openejb <command> [options] [args]";

            String header = "\nAvailable commands:";

            String footer = "\n" +
                    "Try 'openejb <command> --help' for help on a specific command.\n" +
                    "For example 'openejb deploy --help'.\n" +
                    "\n" +
                    "Apache OpenEJB -- EJB Container System and Server.\n" +
                    "For additional information, see http://openejb.apache.org\n" +
                    "Bug Reports to <users@openejb.apache.org>";


            if (!printHeader){
                pw.append(header).append("\n\n");
                formatter.printOptions(pw, 74, options, 1, 3);
            } else {
                formatter.printHelp(pw, 74, syntax, header, options, 1, 3, footer, false);
            }

            pw.flush();

            // Fix up the commons-cli output to our liking.
            String text = sw.toString().replaceAll("\n -", "\n  ");
            text = text.replace("\nApache OpenEJB","\n\nApache OpenEJB");
            System.out.print(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
