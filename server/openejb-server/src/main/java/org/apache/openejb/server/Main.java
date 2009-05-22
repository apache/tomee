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
package org.apache.openejb.server;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.OpenEjbVersion;
import org.apache.openejb.util.Messages;
import org.apache.openejb.cli.SystemExitException;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.xbean.finder.ResourceFinder;

/**
 * Assemble OpenEJB instance and boot it up
 * 
 * @version $Rev$ $Date$
 */
public class Main {

    private static Messages messages = new Messages(Main.class);

    public static void main(String args[]) throws SystemExitException {
        CommandLineParser parser = new PosixParser();

        // create the Options
        Options options = new Options();
        options.addOption(option("v", "version", "cmd.start.opt.version"));
        options.addOption(option("h", "help", "cmd.start.opt.help"));
        options.addOption(option(null, "conf", "file", "cmd.start.opt.conf"));
        options.addOption(option(null, "local-copy", "boolean", "cmd.start.opt.localCopy"));
        options.addOption(option(null, "examples", "cmd.start.opt.examples"));

        ResourceFinder finder = new ResourceFinder("META-INF/");

        Set<String> services = null;
        try {
            Map<String, Properties> serviceEntries = finder.mapAvailableProperties(ServerService.class.getName());
            services = serviceEntries.keySet();
            for (String service : services) {
                options.addOption(option(null, service+"-port", "int", "cmd.start.opt.port", service));
                options.addOption(option(null, service+"-bind", "host", "cmd.start.opt.bind", service));
            }
        } catch (IOException e) {
            services = Collections.EMPTY_SET;
        }

        CommandLine line = null;
        try {
            // parse the command line arguments
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            help(options);
            throw new SystemExitException(-1);
        }

        if (line.hasOption("help")) {
            help(options);
            return;
        } else if (line.hasOption("version")) {
            OpenEjbVersion.get().print(System.out);
            return;
        } else if (line.hasOption("examples")) {
            try {
                String text = finder.findString("org.apache.openejb.cli/start.examples");
                System.out.println(text);
                return;
            } catch (IOException e) {
                System.err.println("Unable to print examples:");
                e.printStackTrace();
                throw new SystemExitException(-2);
            }
        }

        Properties props = SystemInstance.get().getProperties();

        if (line.hasOption("conf")) {
            props.setProperty("openejb.configuration", line.getOptionValue("conf"));
        } else if (line.hasOption("local-copy")) {
            String value = line.getOptionValue("local-copy");
            if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no") || value.equalsIgnoreCase("off")) {
                props.setProperty("openejb.localcopy", "false");
            } else {
                props.setProperty("openejb.localcopy", "true");
            }
        }

        for (String service : services) {
            String[] opts = {"port", "bind"};
            for (String opt : opts) {
                String option = service + "-" + opt;
                if (line.hasOption(option)){
                    props.setProperty(service+"."+opt, line.getOptionValue(option));
                }
            }
        }

        try {
            SystemInstance system = SystemInstance.get();
            File libs = system.getHome().getDirectory("lib");
            system.getClassPath().addJarsToPath(libs);
            initServer();
        } catch (DontStartServerException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void help(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("start [options]", "\n"+i18n("cmd.start.description"), options, "\n");
    }

    private static Option option(String shortOpt, String longOpt, String description) {
        return OptionBuilder.withLongOpt(longOpt).withDescription(i18n(description)).create(shortOpt);
    }

    private static Option option(String shortOpt, String longOpt, String argName, String description, Object... details) {
        return OptionBuilder.withLongOpt(longOpt).withArgName(argName).hasArg().withDescription(i18n(description,details)).create(shortOpt);
    }

    private static String i18n(String key, Object... details) {
        return messages.format(key, details);
    }


    private static void initServer() throws Exception {
        Server server = new Server();
        server.init(SystemInstance.get().getProperties());
        server.start();
    }
}

class DontStartServerException extends Exception {
    private static final long serialVersionUID = 1L;
}
