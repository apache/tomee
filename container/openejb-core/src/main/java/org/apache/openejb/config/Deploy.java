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
package org.apache.openejb.config;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.cli.SystemExitException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.InterceptorInfo;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.PersistenceUnitInfo;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.OpenEjbVersion;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Closeable;
import java.util.Properties;

/**
 * Deploy EJB beans
 */
public class Deploy {

    private static Messages messages = new Messages(Deploy.class);

    private static final String defaultServerUrl = "ejbd://localhost:4201";
    private static final int BUF_SIZE = 8192;


    public static void main(String[] args) throws SystemExitException {

        CommandLineParser parser = new PosixParser();

        // create the Options
        Options options = new Options();
        options.addOption(option("v", "version", "cmd.deploy.opt.version"));
        options.addOption(option("h", "help", "cmd.deploy.opt.help"));
        options.addOption(option("o", "offline", "cmd.deploy.opt.offline"));
        options.addOption(option("s", "server-url", "url", "cmd.deploy.opt.server"));
        options.addOption(option("d", "debug", "cmd.deploy.opt.debug"));
        options.addOption(option("q", "quiet", "cmd.deploy.opt.quiet"));
        options.addOption(option(null, "dir", "cmd.deploy.opt.dir"));

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
        }

        if (line.getArgList().size() == 0) {
            System.out.println("Must specify an archive to deploy.");
            help(options);
        }


        boolean offline = line.hasOption("offline");

        File apps = null;
        try {
            String dir = line.getOptionValue("dir", "apps");
            apps = SystemInstance.get().getBase().getDirectory(dir);
        } catch (IOException e) {
            throw new SystemExitException(-1);
        }

        if (!apps.exists()) {
            System.out.println("Directory does not exist: " + apps.getAbsolutePath());
        }

        Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");

        String serverUrl = defaultServerUrl;
        if (line.hasOption(serverUrl)) {
            serverUrl = line.getOptionValue("serverUrl");
        }
        p.put(Context.PROVIDER_URL, serverUrl);

        Deployer deployer = null;
        try {
            InitialContext ctx = new InitialContext(p);
            deployer = (Deployer) ctx.lookup("openejb/DeployerBusinessRemote");
        } catch (javax.naming.ServiceUnavailableException e) {
            System.out.println(e.getCause().getMessage());
            System.out.println(messages.format("cmd.deploy.serverOffline"));
            throw new SystemExitException(1);
        } catch (javax.naming.NamingException e) {
            System.out.println("DeployerEjb does not exist in server '" + serverUrl + "', check the server logs to ensure it exists and has not been removed.");
            throw new SystemExitException(2);
        }

        int exitCode = 0;
        for (Object obj : line.getArgList()) {
            String path = (String) obj;

            try {
                File file = new File(path);

                checkSource(file);

                File destFile = new File(apps, file.getName());

                checkDest(destFile, file);

                copyFile(file, destFile);

                if (offline) {
                    System.out.println(messages.format("cmd.deploy.offline", path, apps.getAbsolutePath()));
                    continue;
                }

                AppInfo appInfo = deployer.deploy(file.getAbsolutePath());

                System.out.println(messages.format("cmd.deploy.successful", path, appInfo.jarPath));

                if (line.hasOption("quiet")) {
                    continue;
                }

                System.out.println("App(id=" + appInfo.jarPath + ")");

                for (EjbJarInfo info : appInfo.ejbJars) {
                    System.out.println("    EjbJar(id=" + info.moduleId + ", path=" + info.jarPath + ")");
                    for (EnterpriseBeanInfo beanInfo : info.enterpriseBeans) {
                        System.out.println("        Ejb(ejb-name=" + beanInfo.ejbName + ", id=" + beanInfo.ejbDeploymentId + ")");
                        for (String name : beanInfo.jndiNames) {
                            System.out.println("            Jndi(name=" + name + ")");
                        }
                        System.out.println("");
                    }
                    for (InterceptorInfo interceptorInfo : info.interceptors) {
                        System.out.println("        Interceptor(class=" + interceptorInfo.clazz + ")");
                    }
                    System.out.println("");
                }
                for (ClientInfo clientInfo : appInfo.clients) {
                    System.out.println("    Client(main-class=" + clientInfo.mainClass + ", id=" + clientInfo.moduleId + ", path=" + clientInfo.codebase + ")");
                    System.out.println("");
                }
                for (PersistenceUnitInfo persistenceUnitInfo : appInfo.persistenceUnits) {
                    System.out.println("    PersistenceUnit(name=" + persistenceUnitInfo.name + ", provider=" + persistenceUnitInfo.provider+ ")");
                    System.out.println("");
                }
            } catch (DeploymentTerminatedException e) {
                System.out.println(e.getMessage());
                exitCode += 100;
            } catch (ValidationFailedException e) {
                System.out.println(messages.format("cmd.deploy.validationFailed", path));
                int level = 2;
                if (line.hasOption("debug")){
                    level = 3;
                }
                EjbValidator ejbValidator = new EjbValidator(level, false, true, false);
                ejbValidator.printResults(e);
                exitCode += 100;
            } catch (OpenEJBException e) {
                System.out.println(messages.format("cmd.deploy.failed", path));
                e.printStackTrace(System.out);
                exitCode += 100;
            }
        }

        if (exitCode != 0){
            throw new SystemExitException(exitCode);
        }
    }

    private static void checkSource(File file) throws DeploymentTerminatedException {
        if (!file.exists()) {
            throw new DeploymentTerminatedException(messages.format("cmd.deploy.fileNotFound", file.getAbsolutePath()));
        }
    }

    private static void checkDest(File destFile, File file) throws DeploymentTerminatedException {
        if (destFile.exists()){
            throw new DeploymentTerminatedException(messages.format("cmd.deploy.destExists", file.getAbsolutePath(), destFile.getAbsolutePath()));
        }
    }

    private static void copyFile(File file, File destFile) throws DeploymentTerminatedException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(file);
            out = new FileOutputStream(destFile);

            byte[] buffer = new byte[BUF_SIZE];
            int count = 0;
            do {
                out.write(buffer, 0, count);
                count = in.read(buffer, 0, buffer.length);
            } while (count != -1);
        } catch (Exception e) {
            throw new DeploymentTerminatedException(messages.format("cmd.deploy.cantCopy", file.getAbsolutePath(), destFile.getAbsolutePath()));
        } finally {
            close(in);
            close(out);
        }
    }

    private static void close(Closeable in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    }

    private static void help(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("deploy [options] <file> [<file>...]", "\n"+i18n("cmd.deploy.description"), options, "\n");
    }

    private static Option option(String shortOpt, String longOpt, String description) {
        return OptionBuilder.withLongOpt(longOpt).withDescription(i18n(description)).create(shortOpt);
    }

    private static Option option(String shortOpt, String longOpt, String argName, String description) {
        return OptionBuilder.withLongOpt(longOpt).withArgName(argName).hasArg().withDescription(i18n(description)).create(shortOpt);
    }

    private static String i18n(String key) {
        return messages.format(key);
    }

    public static class DeploymentTerminatedException extends Exception {
        public DeploymentTerminatedException(String message) {
            super(message);
        }

        public DeploymentTerminatedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}