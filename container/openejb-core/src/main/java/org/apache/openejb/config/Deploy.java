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

package org.apache.openejb.config;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.InterceptorInfo;
import org.apache.openejb.assembler.classic.PersistenceUnitInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.cli.SystemExitException;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.JarExtractor;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.OpenEjbVersion;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.ServiceUnavailableException;

import static org.apache.openejb.util.JarExtractor.delete;

/**
 * Deploy EJB beans
 */
public class Deploy {

    private static final Messages messages = new Messages(Deploy.class);

    private static final String defaultServerUrl = "ejbd://localhost:4201";

    public static void main(final String... args) throws SystemExitException {

        final CommandLineParser parser = new PosixParser();

        // create the Options
        final Options options = new Options();
        options.addOption(option("v", "version", "cmd.deploy.opt.version"));
        options.addOption(option("h", "help", "cmd.deploy.opt.help"));
        options.addOption(option("o", "offline", "cmd.deploy.opt.offline"));
        options.addOption(option("s", "server-url", "url", "cmd.deploy.opt.server"));
        options.addOption(option("d", "debug", "cmd.deploy.opt.debug"));
        options.addOption(option("q", "quiet", "cmd.deploy.opt.quiet"));
        options.addOption(option("u", "undeploy", "cmd.deploy.opt.undeploy"));
        options.addOption(option(null, "dir", "cmd.deploy.opt.dir"));

        final CommandLine line;
        try {
            // parse the command line arguments
            line = parser.parse(options, args);
        } catch (final ParseException exp) {
            help(options);
            throw new SystemExitException(-1);
        }

        if (line.hasOption("help")) {
            help(options);
            return;
        } else if (line.hasOption("version")) {
            OpenEjbVersion.get().print(System.out);
            return;
        }

        if (line.getArgList().size() == 0) {
            System.out.println("Must specify an archive to deploy.");
            help(options);
            return;
        }

        // make sure that the modules given on the command line are accessible
        final List<?> modules = line.getArgList();
        for (final Object module : modules) {
            final String path = (String) module;
            final File file = new File(path);
            try {
                checkSource(file);
            } catch (final DeploymentTerminatedException e) {
                System.out.println(e.getMessage());
                // TODO: What is it for?
                throw new SystemExitException(-100);
            }
        }

        final boolean offline = line.hasOption("offline");

        final File apps;
        try {
            final String dir = line.getOptionValue("dir", "apps");
            apps = SystemInstance.get().getBase().getDirectory(dir);
        } catch (final IOException e) {
            throw new SystemExitException(-1);
        }

        if (!apps.exists()) {
            System.out.println("Directory does not exist: " + apps.getAbsolutePath());
        }

        Deployer deployer = null;
        if (!offline) {
            final Properties p = new Properties();
            p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");

            String serverUrl = line.getOptionValue("server-url", defaultServerUrl);
            if ("auto".equalsIgnoreCase(serverUrl.trim())) {
                try {
                    final File sXml = new File(JavaSecurityManagers.getSystemProperty("openejb.base", "conf/server.xml"));
                    if (sXml.exists()) {
                        final QuickServerXmlParser result = QuickServerXmlParser.parse(sXml);
                        serverUrl = "http://" + result.host() + ":" + result.http() + "/tomee/ejb";
                    }
                } catch (final Throwable e) {
                    // no-op
                }
            }
            p.put(Context.PROVIDER_URL, serverUrl);

            try {
                final InitialContext ctx = new InitialContext(p);
                deployer = (Deployer) ctx.lookup("openejb/DeployerBusinessRemote");
            } catch (final ServiceUnavailableException e) {
                System.out.println(e.getCause().getMessage());
                System.out.println(messages.format("cmd.deploy.serverOffline"));
                throw new SystemExitException(-1);
            } catch (final NamingException e) {
                System.out.println("openejb/DeployerBusinessRemote does not exist in server '" + serverUrl
                    + "', check the server logs to ensure it exists and has not been removed.");
                throw new SystemExitException(-2);
            }
        }

        final boolean undeploy = line.hasOption("undeploy");

        // We increment the exit code once for every failed deploy
        int exitCode = 0;
        for (final Object obj : line.getArgList()) {
            final String path = (String) obj;

            final File file = new File(path);

            File destFile = new File(apps, file.getName());

            try {
                if (shouldUnpack(file)) {
                    final File unpacked = unpackedLocation(file, apps);
                    if (undeploy) {
                        undeploy(offline, unpacked, path, deployer);
                    }
                    destFile = unpack(file, unpacked);
                } else {
                    if (undeploy) {
                        undeploy(offline, destFile, path, deployer);
                    }
                    checkDest(destFile, file);
                    copyFile(file, destFile);
                }

                if (offline) {
                    System.out.println(messages.format("cmd.deploy.offline", path, apps.getAbsolutePath()));
                    continue;
                }

                final String location;
                try {
                    location = destFile.getCanonicalPath();
                } catch (final IOException e) {
                    throw new OpenEJBException(messages.format("cmd.deploy.fileNotFound", path));
                }
                final AppInfo appInfo = deployer.deploy(location);

                System.out.println(messages.format("cmd.deploy.successful", path, appInfo.path));

                if (line.hasOption("quiet")) {
                    continue;
                }

                print(appInfo);

            } catch (final UndeployException e) {
                System.out.println(messages.format("cmd.undeploy.failed", path));
                e.printStackTrace(System.out);
                exitCode++;
            } catch (final DeploymentTerminatedException e) {
                System.out.println(e.getMessage());
                exitCode++;
            } catch (final ValidationFailedException e) {
                System.out.println(messages.format("cmd.deploy.validationFailed", path));
                int level = 2;
                if (line.hasOption("debug")) {
                    level = 3;
                }
                final AppValidator appValidator = new AppValidator(level, false, true, false);
                appValidator.printResults(e);
                exitCode++;
                if (!delete(destFile)) {
                    System.out.println(messages.format("cmd.deploy.cantDelete.deploy", destFile.getAbsolutePath()));
                }
            } catch (final Throwable e) {
                System.out.println(messages.format("cmd.deploy.failed", path));
                e.printStackTrace(System.out);
                exitCode++;
                if (!delete(destFile)) {
                    System.out.println(messages.format("cmd.deploy.cantDelete.deploy", destFile.getAbsolutePath()));
                }
            }
        }

        if (exitCode != 0) {
            throw new SystemExitException(exitCode);
        }
    }

    private static void undeploy(final boolean offline, final File dest, final String path, final Deployer deployer) throws UndeployException, DeploymentTerminatedException {
        if (offline) {
            if (dest.exists()) {
                if (!delete(dest)) {
                    throw new DeploymentTerminatedException(messages.format("cmd.deploy.cantDelete.undeploy", dest.getAbsolutePath()));
                }
            }
        } else {
            try {
                Undeploy.undeploy(path, dest, deployer);
            } catch (final NoSuchApplicationException nothingToUndeploy) {
                // no-op
            }
        }
    }

    private static void print(final AppInfo appInfo) {
        System.out.println("App(id=" + appInfo.path + ")");

        for (final EjbJarInfo info : appInfo.ejbJars) {
            System.out.println("    EjbJar(id=" + info.moduleName + ", path=" + info.path + ")");
            for (final EnterpriseBeanInfo beanInfo : info.enterpriseBeans) {
                System.out.println("        Ejb(ejb-name=" + beanInfo.ejbName + ", id=" + beanInfo.ejbDeploymentId + ")");
                for (final String name : beanInfo.jndiNames) {
                    System.out.println("            Jndi(name=" + name + ")");
                }
                System.out.println("");
            }
            for (final InterceptorInfo interceptorInfo : info.interceptors) {
                System.out.println("        Interceptor(class=" + interceptorInfo.clazz + ")");
            }
            System.out.println("");
        }
        for (final ClientInfo clientInfo : appInfo.clients) {
            System.out.println("    Client(main-class=" + clientInfo.mainClass + ", id=" + clientInfo.moduleId + ", path=" + clientInfo.path + ")");
            System.out.println("");
        }
        for (final ConnectorInfo connectorInfo : appInfo.connectors) {
            System.out.println("    Connector(id=" + connectorInfo.moduleId + ", path=" + connectorInfo.path + ")");
            System.out.println("");
        }
        for (final WebAppInfo webAppInfo : appInfo.webApps) {
            System.out.println("    WebApp(context-root=" + webAppInfo.contextRoot + ", id=" + webAppInfo.moduleId + ", path=" + webAppInfo.path + ")");
            System.out.println("");
        }
        for (final PersistenceUnitInfo persistenceUnitInfo : appInfo.persistenceUnits) {
            System.out.println("    PersistenceUnit(name=" + persistenceUnitInfo.name + ", provider=" + persistenceUnitInfo.provider + ")");
            System.out.println("");
        }
    }

    private static void checkSource(final File file) throws DeploymentTerminatedException {
        if (!file.exists()) {
            throw new DeploymentTerminatedException(messages.format("cmd.deploy.fileNotFound", file.getAbsolutePath()));
        }
    }

    private static void checkDest(final File destFile, final File file) throws DeploymentTerminatedException {
        if (destFile.exists()) {
            throw new DeploymentTerminatedException(messages.format("cmd.deploy.destExists", file.getAbsolutePath(), destFile.getAbsolutePath()));
        }
    }

    private static void copyFile(final File file, final File destFile) throws DeploymentTerminatedException {
        try {
            IO.copy(file, destFile);
        } catch (final Exception e) {
            throw new DeploymentTerminatedException(messages.format("cmd.deploy.cantCopy", file.getAbsolutePath(), destFile.getAbsolutePath()));
        }
    }

    private static boolean shouldUnpack(final File file) {
        final String name = file.getName();
        if (name.endsWith(".ear") || name.endsWith(".rar") || name.endsWith(".rar")) {
            return true;
        }

        try (JarFile jarFile = new JarFile(file)) {

            if (jarFile.getEntry("META-INF/application.xml") != null) {
                return true;
            }
            if (jarFile.getEntry("META-INF/ra.xml") != null) {
                return true;
            }
            if (jarFile.getEntry("WEB-INF/web.xml") != null) {
                return true;
            }
        } catch (final IOException e) {
            // no-op
        }
        // no-op

        return false;
    }

    private static File unpack(final File jarFile, final File destinationDir) throws OpenEJBException, DeploymentTerminatedException {

        try {
            checkDest(destinationDir, jarFile);
            JarExtractor.extract(jarFile, destinationDir);
            return destinationDir;
        } catch (final IOException e) {
            throw new OpenEJBException("Unable to extract jar. " + e.getMessage(), e);
        }
    }

    private static File unpackedLocation(final File jarFile, final File destDir) {
        if (jarFile.isDirectory()) {
            return jarFile;
        }

        String name = jarFile.getName();
        if (name.endsWith(".jar") || name.endsWith(".ear") || name.endsWith(".zip") || name.endsWith(".war") || name.endsWith(".rar")) {
            name = name.replaceFirst("....$", "");
        } else {
            name += ".unpacked";
        }

        final File destinationDir = new File(destDir, name);
        return destinationDir;
    }

    private static void help(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("deploy [options] <file> [<file>...]", "\n" + i18n("cmd.deploy.description"), options, "\n");
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

    public static class DeploymentTerminatedException extends Exception {
        public DeploymentTerminatedException(final String message) {
            super(message);
        }

        public DeploymentTerminatedException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
