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

package org.apache.openejb.assembler.classic.cmd;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.OpenEjbVersion;
import org.apache.openejb.util.SuperProperties;
import org.apache.openejb.util.URISupport;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.ServiceUnavailableException;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class Info2Properties {
    private static final Messages messages = new Messages(Info2Properties.class);

    private static final String defaultServerUrl = "ejbd://localhost:4201";

    public static void main(final String[] args) {

        final CommandLineParser parser = new PosixParser();

        // create the Options
        final Options options = new Options();
        options.addOption(option("v", "version", "cmd.properties.opt.version"));
        options.addOption(option("h", "help", "cmd.properties.opt.help"));
        options.addOption(option("s", "server-url", "url", "cmd.properties.opt.server"));

        CommandLine line = null;
        try {
            // parse the command line arguments
            line = parser.parse(options, args);
        } catch (final ParseException exp) {
            help(options);
            System.exit(-1);
        }

        if (line.hasOption("help")) {
            help(options);
            System.exit(0);
        } else if (line.hasOption("version")) {
            OpenEjbVersion.get().print(System.out);
            System.exit(0);
        }

        final Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");

        final String serverUrl = line.getOptionValue("server-url", defaultServerUrl);
        p.put(Context.PROVIDER_URL, serverUrl);

        ConfigurationInfo configInfo = null;
        try {
            final InitialContext ctx = new InitialContext(p);
            configInfo = (ConfigurationInfo) ctx.lookup("openejb/ConfigurationInfoBusinessRemote");
        } catch (final ServiceUnavailableException e) {
            System.out.println(e.getCause().getMessage());
            System.out.println(messages.format("cmd.deploy.serverOffline"));
            System.exit(1);
        } catch (final NamingException e) {
            System.out.println("ConfigurationInfo does not exist in server '" + serverUrl + "', check the server logs to ensure it exists and has not been removed.");
            System.exit(2);
        }

        File tempFile = null;
        try {
            try {
                tempFile = File.createTempFile("configrequest", "txt");
            } catch (final Throwable e) {
                final File tmp = new File("tmp");
                if (!tmp.exists() && !tmp.mkdirs()) {
                    throw new IOException("Failed to create local tmp directory: " + tmp.getAbsolutePath());
                }

                tempFile = File.createTempFile("configrequest", "txt", tmp);

            }
            if (!tempFile.exists()) {
                throw new IllegalStateException("Failed to create tmp file: " + tempFile.getAbsolutePath());
            }
        } catch (final Exception e) {
            System.err.println("Temp file creation failed.");
            e.printStackTrace();
            System.exit(1);
        }

        OpenEjbConfiguration configuration = null;
        try {
            configuration = configInfo.getOpenEjbConfiguration(tempFile);
        } catch (final ConfigurationInfo.UnauthorizedException e) {
            System.err.println("This tool is currently crippled to only work with server's on the same physical machine.  See this JIRA issue for details: http://issues.apache.org/jira/browse/OPENEJB-621");
            System.exit(10);
        }

        printConfig(configuration);
    }

    public static void printLocalConfig() {
        final OpenEjbConfiguration configuration = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
        if (configuration != null) {
            printConfig(configuration);
        }
    }

    public static void printConfig(final OpenEjbConfiguration configuration) {
        printConfig(configuration, System.out, JavaSecurityManagers.getSystemProperty("line.separator"));
    }

    public static void printConfig(final OpenEjbConfiguration configuration, final PrintStream out, final String cr) {
        comment(out, cr, i18n("cmd.properties.header"));
        comment(out, cr, "");
        comment(out, cr, "");
        comment(out, cr, "Generated by OpenEJB " + OpenEjbVersion.get().getVersion());
        comment(out, cr, "On " + new Date().toString());
        comment(out, cr, "");
        comment(out, cr, "");
        println(out, cr, "");
        println(out, cr, "");

        comment(out, cr, "-------------------------------------------------");
        comment(out, cr, " Components configurable via openejb.xml");
        comment(out, cr, "-------------------------------------------------");
        println(out, cr, "");
        println(out, cr, "");

        for (final ServiceInfo info : configuration.containerSystem.containers) {
            print(out, cr, info);
        }

        for (final ServiceInfo info : configuration.facilities.connectionManagers) {
            print(out, cr, info);
        }

        for (final ServiceInfo info : configuration.facilities.resources) {
            print(out, cr, info);
        }

        print(out, cr, configuration.facilities.securityService);

        print(out, cr, configuration.facilities.transactionService);

        println(out, cr, "");
        comment(out, cr, "-------------------------------------------------");
        comment(out, cr, " Services configured via conf/<id>.properties");
        comment(out, cr, "-------------------------------------------------");
        println(out, cr, "");
        println(out, cr, "");

        for (final ServiceInfo info : configuration.facilities.services) {
            print(out, cr, info);
        }

        println(out, cr, "");
        comment(out, cr, "-------------------------------------------------");
        comment(out, cr, " Misc OpenEJB flags and properties");
        comment(out, cr, "-------------------------------------------------");
        println(out, cr, "");
        printSystemProperties(out, cr);
    }

    private static void printSystemProperties(final PrintStream out, final String cr) {

        try {
            final SuperProperties p = new SuperProperties();
            p.setSpaceBetweenProperties(false);
            p.setKeyValueSeparator(" = ");
            p.setLineSeparator(cr);
            copyOpenEjbProperties(JavaSecurityManagers.getSystemProperties(), p);
            copyOpenEjbProperties(SystemInstance.get().getProperties(), p);
            p.store(out, null);


            final Properties p2 = JavaSecurityManagers.getSystemProperties();
            final String[] misc = {"os.version", "os.name", "os.arch", "java.version", "java.vendor"};
            for (final String prop : misc) {
                comment(out, cr, prop + "=" + p2.get(prop));
            }
        } catch (final IOException e) {
            e.printStackTrace(new PrintWriter(new CommentsFilter(out)));
        }
    }

    private static void copyOpenEjbProperties(final Properties source, final Properties dest) {
        for (final Map.Entry<Object, Object> entry : source.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                continue;
            }
            if (!(entry.getValue() instanceof String)) {
                continue;
            }

            final String key = (String) entry.getKey();
            if (key.startsWith("openejb.")) {
                dest.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private static void comment(final PrintStream out, final String cr, String text) {
        text = text.replaceAll("\n", "\n# ").replace("\n", cr);
        print(out, "# ");
        println(out, cr, text);
    }

    private static void print(final PrintStream out, final String text) {
        out.print(text);
    }

    private static void println(final PrintStream out, final String cr, final String text) {
        out.print(text.replace("\n", cr) + cr);
        out.flush();
    }

    private static void print(final PrintStream out, final String cr, final ServiceInfo info) {
        try {

            println(out, cr, "");

            comment(out, cr, info.service + "(id=" + info.id + ")");
            comment(out, cr, "className: " + info.className);
            // TODO: the codebase value usually isn't filled in, we should do that.
            // comment("codebase: " + info.codebase);
            comment(out, cr, "");
            final SuperProperties p = new SuperProperties();
            p.setSpaceBetweenProperties(false);
            p.setKeyValueSeparator(" = ");
            p.setLineSeparator(cr);

            String uri = "new://" + info.service;
            if (info.service.matches("Container|Resource|Connector")) {
                try {
                    final Map query = new HashMap();
                    query.put("type", info.types.get(0));
                    uri += "?" + URISupport.createQueryString(query);
                } catch (final Exception e) {
                    // no-op
                }
            }

            p.put(info.id, uri);

            for (final Map.Entry<Object, Object> entry : info.properties.entrySet()) {
                if (!(entry.getKey() instanceof String)) {
                    continue;
                }
                if (!(entry.getValue() instanceof String)) {
                    continue;
                }

                // If property name is 'password' replace value with 'xxxx' to protect it
                if ("password".equalsIgnoreCase((String) entry.getKey())) {
                    p.put(info.id + "." + entry.getKey(), "xxxx");
                } else {
                    p.put(info.id + "." + entry.getKey(), entry.getValue());
                }
            }
            p.store(out, null);

        } catch (final IOException e) {
            out.println("# Printing service(id=" + info.id + ") failed.");
            e.printStackTrace(new PrintWriter(new CommentsFilter(out)));
        }

    }

    // Filter out the stupid date comment the Properties.store() method
    // adds seemingly no matter what.
    static class Filter extends FilterOutputStream {
        private boolean pastFirstLine;

        public Filter(final OutputStream out) {
            super(out);
        }

        public void write(final int b) throws IOException {
            if (pastFirstLine) {
                super.write(b);
            } else {
                pastFirstLine = b == '\n';
            }
        }

    }

    static class CommentsFilter extends FilterOutputStream {

        public CommentsFilter(final OutputStream out) {
            super(out);
        }

        public void write(final int b) throws IOException {
            super.write(b);

            if (b == '\n') {
                super.write('#');
            }
        }

    }

    private static void help(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("properties [options]", "\n" + i18n("cmd.properties.description"), options, "\n");
    }

    private static Option option(final String shortOpt, final String longOpt, final String description) {
        return OptionBuilder.withLongOpt(longOpt).withDescription(i18n(description)).create(shortOpt);
    }

    private static Option option(final String shortOpt, final String longOpt, final String argName, final String description) {
        return OptionBuilder.withLongOpt(longOpt).withArgName(argName).hasArg().withDescription(i18n(description)).create(shortOpt);
    }

    private static String i18n(final String key) {
        return messages.format(key);
    }

}
