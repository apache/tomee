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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.OpenEjbVersion;
import org.apache.openejb.util.OptionsLog;
import org.apache.xbean.finder.ResourceFinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Entry point for ALL things OpenEJB.  This will use the new service
 * architecture explained here:
 *
 * @version $Rev$ $Date$
 * @link http://docs.codehaus.org/display/OPENEJB/Executables
 */
public class MainImpl implements Main {

    private static final String BASE_PATH = "META-INF/org.apache.openejb.cli/";
    private static final String MAIN_CLASS_PROPERTY_NAME = "main.class";

    private static ResourceFinder finder;
    private static String locale = "";
    private static final String descriptionBase = "description";
    private static String descriptionI18n;

    public void main(String[] args) {
        args = processSystemProperties(args);

        finder = new ResourceFinder(BASE_PATH);
        locale = Locale.getDefault().getLanguage();
        descriptionI18n = descriptionBase + "." + locale;


        final CommandLineParser parser = new PosixParser();

        // create the Options
        final Options options = new Options();
        options.addOption(null, "version", false, "Display version");
        options.addOption("h", "help", false, "Display help");
        options.addOption("e", "errors", false, "Produce execution error messages");

        CommandLine line = null;
        String commandName = null;
        try {
            // parse the arguments up until the first
            // command, then let the rest fall into
            // the arguments array.
            line = parser.parse(options, args, true);

            // Get and remove the commandName (first arg)
            final List<String> list = line.getArgList();
            if (list.size() > 0) {
                commandName = list.get(0);
                list.remove(0);
            }

            // The rest of the args will be passed to the command
            args = line.getArgs();
        } catch (final ParseException exp) {
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
        } catch (final IOException e1) {
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

        final String mainClass = props.getProperty(MAIN_CLASS_PROPERTY_NAME);
        if (mainClass == null) {
            throw new NullPointerException("Command " + commandName + " did not specify a " + MAIN_CLASS_PROPERTY_NAME + " property");
        }

        Class<?> clazz = null;
        try {
            clazz = Thread.currentThread().getContextClassLoader().loadClass(mainClass);
        } catch (final ClassNotFoundException cnfe) {
            throw new IllegalStateException("Main class of command " + commandName + " does not exist: " + mainClass, cnfe);
        }

        Method mainMethod = null;
        try {
            mainMethod = clazz.getMethod("main", String[].class);
        } catch (final Exception e) {
            throw new IllegalStateException("Main class of command " + commandName + " does not have a static main method: " + mainClass, e);
        }

        try {
            // WARNING, Definitely do *not* unwrap 'new Object[]{args}' to 'args'
            mainMethod.invoke(clazz, new Object[]{args});
        } catch (final Throwable e) {
            if (line.hasOption("errors")) {
                e.printStackTrace();
            }
            System.exit(-10);
        }
    }

    private String[] processSystemProperties(String[] args) {
        final ArrayList<String> argsList = new ArrayList<>();

        // We have to pre-screen for openejb.base as it has a direct affect
        // on where we look for the conf/system.properties file which we
        // need to read in and apply before we apply the command line -D
        // properties.  Once SystemInstance.init() is called in the next
        // section of code, the openejb.base value is cemented and cannot
        // be changed.
        for (final String arg : args) {
            if (arg.contains("-Dopenejb.base")) {
                final String prop = arg.substring(arg.indexOf("-D") + 2, arg.indexOf('='));
                final String val = arg.substring(arg.indexOf('=') + 1);

                JavaSecurityManagers.setSystemProperty(prop, val);
            }
        }

        // get SystemInstance (the only static class in the system)
        // so we'll set up all the props in it
        SystemInstance systemInstance = null;
        try {
            SystemInstance.init(new Properties());
            OptionsLog.install();
            systemInstance = SystemInstance.get();
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

        // Now read in and apply the properties specified on the command line
        for (final String arg : args) {
            final int idx = arg.indexOf("-D");
            final int eq = arg.indexOf('=');
            if (idx >= 0 && eq > idx) {
                final String prop = arg.substring(idx + 2, eq);
                final String val = arg.substring(eq + 1);

                JavaSecurityManagers.setSystemProperty(prop, val);
                systemInstance.setProperty(prop, val);
            } else {
                argsList.add(arg);
            }
        }

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

    private static void help(final boolean printHeader) {

        // Here we are using commons-cli to create the list of available commands
        // We actually use a different Options object to parse the 'openejb' command
        try {
            final Options options = new Options();

            final ResourceFinder commandFinder = new ResourceFinder("META-INF");
            final Map<String, Properties> commands = commandFinder.mapAvailableProperties("org.apache.openejb.cli");
            for (final Map.Entry<String, Properties> command : commands.entrySet()) {
                if (command.getKey().contains(".")) {
                    continue;
                }
                final Properties p = command.getValue();
                final String description = p.getProperty(descriptionI18n, p.getProperty(descriptionBase));
                options.addOption(command.getKey(), false, description);
            }

            final HelpFormatter formatter = new HelpFormatter();
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);

            final String syntax = "openejb <command> [options] [args]";

            final String header = "\nAvailable commands:";

            final String footer = "\n" +
                "Try 'openejb <command> --help' for help on a specific command.\n" +
                "For example 'openejb deploy --help'.\n" +
                "Important: to display exceptions while running commands, add -e option.\n" +
                "\n" +
                "Apache OpenEJB -- EJB Container System and Server.\n" +
                "For additional information, see http://tomee.apache.org\n" +
                "Bug Reports to <users@tomee.apache.org>";


            if (!printHeader) {
                pw.append(header).append("\n\n");
                formatter.printOptions(pw, 74, options, 1, 3);
            } else {
                formatter.printHelp(pw, 74, syntax, header, options, 1, 3, footer, false);
            }

            pw.flush();

            // Fix up the commons-cli output to our liking.
            String text = sw.toString().replaceAll("\n -", "\n  ");
            text = text.replace("\nApache OpenEJB", "\n\nApache OpenEJB");
            System.out.print(text);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
