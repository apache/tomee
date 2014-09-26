/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.openejb.maven.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Settings;
import org.apache.openejb.config.RemoteServer;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.Zips;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.OpenEjbVersion;
import org.apache.tomee.util.QuickServerXmlParser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.logging.SimpleFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.maven.artifact.Artifact.SCOPE_COMPILE;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER;
import static org.apache.maven.artifact.versioning.VersionRange.createFromVersion;
import static org.apache.openejb.util.JarExtractor.delete;
import static org.codehaus.plexus.util.FileUtils.deleteDirectory;
import static org.codehaus.plexus.util.IOUtil.close;
import static org.codehaus.plexus.util.IOUtil.copy;

public abstract class AbstractTomEEMojo extends AbstractAddressMojo {
    // if we get let say > 5 patterns like it we should create a LocationAnalyzer
    // for < 5 patterns it should be fine
    private static final String NAME_STR = "?name=";
    private static final String UNZIP_PREFIX = "unzip:";
    private static final String REMOVE_PREFIX = "remove:";
    public static final String QUIT_CMD = "quit";
    public static final String EXIT_CMD = "exit";
    public static final String TOM_EE = "TomEE";

    @Component
    protected ArtifactFactory factory;

    @Component
    protected ArtifactResolver resolver;

    @Parameter(defaultValue = "${localRepository}", readonly = true)
    protected ArtifactRepository local;

    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true)
    protected List<ArtifactRepository> remoteRepos;

    @Parameter(property = "tomee-plugin.skipCurrentProject", defaultValue = "false")
    protected boolean skipCurrentProject;

    @Parameter(property = "tomee-plugin.version", defaultValue = "-1")
    protected String tomeeVersion;

    @Parameter(property = "tomee-plugin.groupId", defaultValue = "org.apache.openejb")
    protected String tomeeGroupId;

    @Parameter(property = "tomee-plugin.artifactId", defaultValue = "apache-tomee")
    protected String tomeeArtifactId;

    /**
     * while tar.gz is not managed it is readonly
     */
    @Parameter(property = "tomee-plugin.type", defaultValue = "zip", readonly = true)
    protected String tomeeType;

    @Parameter(property = "tomee-plugin.apache-repos", defaultValue = "snapshots")
    protected String apacheRepos;

    @Parameter(property = "tomee-plugin.classifier", defaultValue = "webprofile")
    protected String tomeeClassifier;

    @Parameter(property = "tomee-plugin.shutdown", defaultValue = "8005")
    protected int tomeeShutdownPort;

    @Parameter(property = "tomee-plugin.shutdown.attempts", defaultValue = "60")
    protected int tomeeShutdownAttempts;

    @Parameter(property = "tomee-plugin.shutdown-command", defaultValue = "SHUTDOWN")
    protected String tomeeShutdownCommand;

    @Parameter(property = "tomee-plugin.ajp", defaultValue = "8009")
    protected int tomeeAjpPort;

    @Parameter(property = "tomee-plugin.https")
    protected Integer tomeeHttpsPort;

    @Parameter(property = "tomee-plugin.args")
    protected String args;

    @Parameter(property = "tomee-plugin.debug", defaultValue = "false")
    protected boolean debug;

    @Parameter(property = "tomee-plugin.simple-log", defaultValue = "false")
    protected boolean simpleLog;

    @Parameter(property = "tomee-plugin.debugPort", defaultValue = "5005")
    protected int debugPort;

    @Parameter(defaultValue = "${project.basedir}/src/main/webapp", property = "tomee-plugin.webappResources")
    protected File webappResources;

    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "tomee-plugin.webappClasses")
    protected File webappClasses;

    @Parameter(defaultValue = "${project.build.directory}/apache-tomee", property = "tomee-plugin.catalina-base")
    protected File catalinaBase;

    /**
     * rename the current artifact
     */
    @Parameter
    protected String context;

    /**
     * relative to tomee.base.
     */
    @Parameter(defaultValue = "webapps")
    protected String webappDir;

    /**
     * relative to tomee.base.
     */
    @Parameter(defaultValue = "apps")
    protected String appDir;

    /**
     * relative to tomee.base.
     */
    @Parameter(defaultValue = "lib")
    protected String libDir;

    @Parameter(defaultValue = "${project.basedir}/src/main")
    protected File mainDir;

    @Parameter(defaultValue = "${project.build.directory}")
    protected File target;

    @Parameter(property = "tomee-plugin.conf", defaultValue = "${project.basedir}/src/main/tomee/conf")
    protected File config;

    @Parameter(property = "tomee-plugin.bin", defaultValue = "${project.basedir}/src/main/tomee/bin")
    protected File bin;

    @Parameter(property = "tomee-plugin.lib", defaultValue = "${project.basedir}/src/main/tomee/lib")
    protected File lib;

    @Parameter
    protected Map<String, String> systemVariables;

    @Parameter
    private List<String> classpaths;

    /**
     * forcing nice default for war development (WEB-INF/classes and web resources)
     */
    @Parameter(property = "tomee-plugin.webappDefaultConfig", defaultValue = "false")
    protected boolean webappDefaultConfig;

    /**
     * use a real random instead of secure random. saves few ms at startup.
     */
    @Parameter(property = "tomee-plugin.quick-session", defaultValue = "true")
    protected boolean quickSession;

    /**
     * force webapp to be reloadable
     */
    @Parameter(property = "tomee-plugin.force-reloadable", defaultValue = "false")
    protected boolean forceReloadable;

    /**
     * supported formats:
     * --> groupId:artifactId:version...
     * --> unzip:groupId:artifactId:version...
     * --> remove:prefix (often prefix = artifactId)
     */
    @Parameter
    protected List<String> libs;

    @Parameter
    protected List<String> javaagents;

    @Parameter
    protected List<String> webapps;

    @Parameter
    protected List<String> apps;

    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.${project.packaging}")
    protected File warFile;

    @Parameter(property = "tomee-plugin.remove-default-webapps", defaultValue = "true")
    protected boolean removeDefaultWebapps;

    @Parameter(property = "tomee-plugin.deploy-openejb-internal-application", defaultValue = "false")
    protected boolean deployOpenEjbApplication;

    @Parameter(property = "tomee-plugin.remove-tomee-webapps", defaultValue = "true")
    protected boolean removeTomeeWebapp;

    @Parameter(property = "tomee-plugin.ejb-remote", defaultValue = "true")
    protected boolean ejbRemote;

    @Parameter(defaultValue = "${project.packaging}", readonly = true)
    protected String packaging;

    @Parameter(property = "tomee-plugin.keep-server-xml", defaultValue = "false")
    protected boolean keepServerXmlAsthis;

    @Parameter(property = "tomee-plugin.check-started", defaultValue = "false")
    protected boolean checkStarted;

    @Parameter(property = "tomee-plugin.use-console", defaultValue = "true")
    protected boolean useConsole;

    @Parameter(property = "tomee-plugin.exiting", defaultValue = "false")
    protected boolean tomeeAlreadyInstalled;

    /**
     * The current user system settings for use in Maven.
     */
    @Parameter(defaultValue = "${settings}", readonly = true)
    protected Settings settings;

    /**
     * use openejb-standalone automatically instead of TomEE
     */
    @Parameter(property = "tomee-plugin.openejb", defaultValue = "false")
    protected boolean useOpenEJB;

    /**
     * for TomEE and wars only, which docBase to use for this war.
     */
    @Parameter
    protected List<File> docBases;

    /**
     * for TomEE and wars only, add some external repositories to classloader.
     */
    @Parameter
    protected List<File> externalRepositories;

    /**
     * when you set docBases to src/main/webapp setting it to true will allow hot refresh.
     */
    @Parameter(property = "tomee-plugin.skipWarResources", defaultValue = "false")
    protected boolean skipWarResources = false;

    protected File deployedFile = null;
    protected RemoteServer server = null;
    protected String container = TOM_EE;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        fixConfig();

        if ("-1".equals(tomeeVersion)) {
            final String version = OpenEjbVersion.get().getVersion();
            tomeeVersion = "1" + version.substring(1, version.length());
        }

        if (!tomeeAlreadyInstalled) {
            final Collection<String> existingWebapps; // added before using the plugin with maven dependency plugin or sthg like that
            if (removeDefaultWebapps) {
                existingWebapps = webappsAlreadyAdded();
            } else {
                existingWebapps = Collections.emptyList();
            }

            unzip(resolve());
            if (removeDefaultWebapps) { // do it first to let add other war
                removeDefaultWebapps(removeTomeeWebapp, existingWebapps);
            }
            copyLibs(libs, new File(catalinaBase, libDir), "jar");
            copyLibs(webapps, new File(catalinaBase, webappDir), "war");
            copyLibs(apps, new File(catalinaBase, appDir), "jar");
            overrideConf(config, "conf");
            overrideConf(lib, "lib");
            final Collection<File> copied = overrideConf(bin, "bin");

            for (final File copy : copied) {
                if (copy.getName().endsWith(".bat") || copy.getName().endsWith(".sh")) {
                    if (!copy.setExecutable(true)) {
                        getLog().warn("can't make " + copy.getPath() + " executable");
                    }
                }
            }

            if (classpaths == null) { // NPE protection for activateSimpleLog() and run()
                classpaths = new ArrayList<String>();
            }
            if (simpleLog) {
                activateSimpleLog();
            }

            if (!keepServerXmlAsthis) {
                overrideAddresses();
            }
            if (!skipCurrentProject) {
                copyWar();
            }
        }

        run();
    }

    protected void fixConfig() {
        if (useOpenEJB) {
            tomeeGroupId = "org.apache.openejb";
            tomeeArtifactId = "openejb-standalone";
            tomeeClassifier = null;
            tomeeShutdownCommand = "Q";
            if (8005 == tomeeShutdownPort) { // default admin port
                tomeeShutdownPort = 4200;
            }
            if (tomeeVersion.startsWith("1.")) {
                tomeeVersion = OpenEjbVersion.get().getVersion();
            }

            if (catalinaBase.getName().equals("apache-tomee") && catalinaBase.getParentFile().equals(target)) {
                catalinaBase = new File(target, "apache-openejb");
            }
            if (config.getParentFile().getName().equals("tomee") && config.getParentFile().getParentFile().equals(mainDir)) {
                config = new File(mainDir, "openejb/conf");
            }
            if (lib.getParentFile().getName().equals("tomee") && lib.getParentFile().getParentFile().equals(mainDir)) {
                lib = new File(mainDir, "openejb/lib");
            }
            if (bin.getParentFile().getName().equals("tomee") && bin.getParentFile().getParentFile().equals(mainDir)) {
                bin = new File(mainDir, "openejb/bin");
            }
        }
    }

    protected String getAdditionalClasspath() {
        if (!classpaths.isEmpty()) {
            final StringBuilder cpBuilder = new StringBuilder();
            for (final String cp : classpaths) {
                cpBuilder.append(cp);
                cpBuilder.append(File.pathSeparatorChar);
            }
            return cpBuilder.substring(0, cpBuilder.length() - 1); // Dump the final path separator
        }
        return null;
    }

    private List<String> webappsAlreadyAdded() {
        final List<String> list = new ArrayList<String>();
        final File webapps = new File(catalinaBase, webappDir);
        if (webapps.exists() && webapps.isDirectory()) {
            final File[] files = webapps.listFiles();
            if (files != null) {
                for (final File f : files) {
                    list.add(f.getName());
                }
            }
        }
        return list;
    }

    private void activateSimpleLog() {
        // replacing java.util.logging.SimpleFormatter by SimpleTomEEFormatter
        final File loggingProperties = new File(catalinaBase, "conf/logging.properties");
        if (loggingProperties.exists() && !new File(config, "conf/logging.properties").exists()) {
            try {
                String content = IO.slurp(loggingProperties);
                if (!content.contains("java.util.logging.ConsoleHandler.formatter")) {
                    content += System.getProperty("line.separator") + "java.util.logging.ConsoleHandler.formatter = org.apache.tomee.jul.formatter.SimpleTomEEFormatter";
                } else {
                    content = content.replace(SimpleFormatter.class.getName(), "org.apache.tomee.jul.formatter.SimpleTomEEFormatter");
                }

                final FileWriter writer = new FileWriter(loggingProperties);
                try {
                    writer.write(content);
                } finally {
                    IO.close(writer);
                }
            } catch (final Exception e) {
                getLog().error("Can't set SimpleTomEEFormatter", e);
            }
        }
    }

    private void removeDefaultWebapps(final boolean removeTomee, final Collection<String> providedWebapps) {
        final File webapps = new File(catalinaBase, webappDir);
        if (webapps.isDirectory()) {
            final File[] files = webapps.listFiles();
            if (null != files) {
                for (final File webapp : files) {
                    final String name = webapp.getName();
                    if (webapp.isDirectory() && !providedWebapps.contains(name) && (removeTomee || !name.equals("tomee"))) {
                        try {
                            deleteDirectory(webapp);
                        } catch (final IOException ignored) {
                            // no-op
                        }
                    }
                }
            }
        }

        getLog().info("Removed not mandatory default webapps");
    }

    private void copyLibs(final List<String> files, final File destParent, final String defaultType) {
        if (files == null || files.isEmpty()) {
            return;
        }

        if (!destParent.exists() && !destParent.mkdirs()) {
            getLog().warn("can't create '" + destParent.getPath() + "'");
        }

        for (final String file : files) {
            updateLib(file, destParent, defaultType);
        }
    }

    private void updateLib(final String rawLib, final File destParent, final String defaultType) {
        InputStream is = null;
        OutputStream os = null;

        // special hook to get more info
        String lib = rawLib;
        String extractedName = null;
        if (lib.contains(NAME_STR)) {
            lib = lib.substring(0, rawLib.indexOf(NAME_STR));
            extractedName = rawLib.substring(rawLib.indexOf(NAME_STR) + NAME_STR.length(), rawLib.length());
            if (!extractedName.endsWith(".jar") && !extractedName.endsWith(".war") && !extractedName.endsWith(".ear")) {
                extractedName = extractedName + "." + defaultType;
            }
        }

        boolean unzip = false;
        if (lib.startsWith(UNZIP_PREFIX)) {
            lib = lib.substring(UNZIP_PREFIX.length());
            unzip = true;
        }

        if (lib.startsWith(REMOVE_PREFIX)) {
            final String prefix = lib.substring(REMOVE_PREFIX.length());
            final File[] files = destParent.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return name.startsWith(prefix);
                }
            });
            if (files != null) {
                for (final File file : files) {
                    if (!IO.delete(file)) {
                        file.deleteOnExit();
                    }
                    getLog().info("Deleted " + file.getPath());
                }
            }
        } else {
            try {
                final File file = mvnToFile(lib, defaultType);
                if (!unzip) {
                    final File dest;
                    if (extractedName == null) {
                        dest = new File(destParent, file.getName());
                    } else {
                        dest = new File(destParent, extractedName);
                    }

                    is = new BufferedInputStream(new FileInputStream(file));
                    os = new BufferedOutputStream(new FileOutputStream(dest));
                    copy(is, os);

                    getLog().info("Copied '" + lib + "' in '" + dest.getAbsolutePath());
                } else {
                    Zips.unzip(file, destParent, true);

                    getLog().info("Unzipped '" + lib + "' in '" + destParent.getAbsolutePath());
                }
            } catch (final Exception e) {
                getLog().error(e.getMessage(), e);
                throw new TomEEException(e.getMessage(), e);
            } finally {
                close(is);
                close(os);
            }
        }
    }

    private File mvnToFile(final String lib, final String defaultType) throws ArtifactResolutionException, ArtifactNotFoundException {
        final String[] infos = lib.split(":");
        final String classifier;
        final String type;
        if (infos.length < 3) {
            throw new TomEEException("format for librairies should be <groupId>:<artifactId>:<version>[:<type>[:<classifier>]]");
        }
        if (infos.length >= 4) {
            type = infos[3];
        } else {
            type = defaultType;
        }
        if (infos.length == 5) {
            classifier = infos[4];
        } else {
            classifier = null;
        }

        final Artifact artifact = factory.createDependencyArtifact(infos[0], infos[1], createFromVersion(infos[2]), type, classifier, SCOPE_COMPILE);
        resolver.resolve(artifact, remoteRepos, local);
        return artifact.getFile();
    }

    private void copyWar() {
        if ("pom".equals(packaging)) {
            return;
        }

        final boolean war = "war".equals(packaging);
        final String name = destinationName();
        final File out;
        if (war) {
            out = new File(catalinaBase, webappDir + "/" + name);
        } else {
            final File parent = new File(catalinaBase, appDir);
            if (!parent.exists() && !parent.mkdirs()) {
                getLog().warn("can't create '" + parent.getPath() + "'");
            }
            out = new File(parent, name);
        }
        delete(out);
        if (!warFile.isDirectory() && name.endsWith("." + packaging)) {
            final String dir = name.substring(0, name.lastIndexOf('.'));
            final File unpacked;
            if (war) {
                unpacked = new File(catalinaBase, webappDir + "/" + dir);
            } else {
                unpacked = new File(catalinaBase, appDir + "/" + dir);
            }
            delete(unpacked);
        }

        if (warFile.exists() && warFile.isDirectory()) {
            try {
                IO.copyDirectory(warFile, out);
            } catch (final IOException e) {
                throw new TomEEException(e.getMessage(), e);
            }
        } else if (warFile.exists()) {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = new FileInputStream(warFile);
                os = new FileOutputStream(out);
                copy(is, os);

                getLog().info("Installed '" + warFile.getAbsolutePath() + "' in " + out.getAbsolutePath());
            } catch (final Exception e) {
                throw new TomEEException(e.getMessage(), e);
            } finally {
                close(is);
                close(os);
            }
        } else {
            getLog().warn("'" + warFile + "' doesn't exist, ignoring (maybe run mvn package before this plugin)");
        }

        deployedFile = out;
    }

    protected String destinationName() {
        if (context != null) {
            if (!context.contains(".") && !warFile.isDirectory()) {
                return context + "." + packaging;
            }
            return context;
        }
        return warFile.getName();
    }

    private void overrideAddresses() {
        final File serverXml = new File(catalinaBase, "conf/server.xml");
        if (!serverXml.exists()) { // openejb
            return;
        }

        final QuickServerXmlParser parser = QuickServerXmlParser.parse(serverXml);

        String value = read(serverXml);

        File keystoreFile = new File(parser.keystore());

        if (!keystoreFile.exists()) {
            keystoreFile = new File(System.getProperty("user.home"), ".keystore");
        }

        if (!keystoreFile.exists()) {
            keystoreFile = new File("target", ".keystore");
        }

        final String keystoreFilePath = (keystoreFile.exists() ? keystoreFile.getAbsolutePath() : "");


        if (tomeeHttpsPort != null && tomeeHttpsPort > 0 && parser.value("HTTPS", null) == null) {
            // ensure connector is not commented
            value = value.replace("<Service name=\"Catalina\">", "<Service name=\"Catalina\">\n"
                + "    <Connector port=\"" + tomeeHttpsPort + "\" protocol=\"HTTP/1.1\" SSLEnabled=\"true\"\n" +
                "                scheme=\"https\" secure=\"true\"\n" +
                "                clientAuth=\"false\" sslProtocol=\"TLS\" keystoreFile=\"" + keystoreFilePath + "\" />\n");
        }

        if (tomeeHttpsPort == null) {
            // avoid NPE
            tomeeHttpsPort = 8443;
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(serverXml);
            writer.write(value
                .replace(parser.http(), Integer.toString(tomeeHttpPort))
                .replace(parser.https(), Integer.toString(tomeeHttpsPort))
                .replace(parser.ajp(), Integer.toString(tomeeAjpPort))
                .replace(parser.stop(), Integer.toString(tomeeShutdownPort))
                .replace(parser.host(), tomeeHost)
                .replace(parser.appBase(), webappDir));
        } catch (final IOException e) {
            throw new TomEEException(e.getMessage(), e);
        } finally {
            close(writer);
        }
    }

    private static String read(final File file) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            final StringBuilder sb = new StringBuilder();
            int i = in.read();
            while (i != -1) {
                sb.append((char) i);
                i = in.read();
            }
            return sb.toString();
        } catch (final Exception e) {
            throw new TomEEException(e.getMessage(), e);
        } finally {
            close(in);
        }
    }

    private Collection<File> overrideConf(final File dir, final String baseDir) {
        if (!dir.exists()) {
            return Collections.emptyList();
        }

        final File[] files = dir.listFiles();
        if (files != null) {
            final Collection<File> copied = new ArrayList<File>();
            for (final File f : files) {
                if (f.isHidden()) {
                    continue;
                }

                final String file = baseDir + "/" + f.getName();
                final File destination = new File(catalinaBase, file);
                if (f.isDirectory()) {
                    Files.mkdirs(destination);
                    try {
                        IO.copyDirectory(f, destination);
                    } catch (final IOException e) {
                        throw new TomEEException(e.getMessage(), e);
                    }
                } else {
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = new FileInputStream(f);
                        out = new FileOutputStream(destination);
                        copy(in, out);

                        copied.add(f);
                        getLog().info("Override '" + file + "'");
                    } catch (final Exception e) {
                        throw new TomEEException(e.getMessage(), e);
                    } finally {
                        close(in);
                        close(out);
                    }
                }
            }

            return copied;
        }

        return Collections.emptyList();
    }

    protected void run() {
        if (classpaths == null) { // NPE protection when execute is skipped and mojo delegates to run directly
            classpaths = new ArrayList<String>();
        }

        final List<String> strings = generateJVMArgs();

        // init env for RemoteServer
        System.setProperty("openejb.home", catalinaBase.getAbsolutePath());
        if (debug) {
            System.setProperty("openejb.server.debug", "true");
            System.setProperty("server.debug.port", Integer.toString(debugPort));
        }
        System.setProperty("server.shutdown.port", Integer.toString(tomeeShutdownPort));
        System.setProperty("server.shutdown.command", tomeeShutdownCommand);

        server = new RemoteServer(getConnectAttempts(), debug);
        server.setAdditionalClasspath(getAdditionalClasspath());

        addShutdownHooks(server); // some shutdown hooks are always added (see UpdatableTomEEMojo)

        if (TOM_EE.equals(container)) {

            server.setPortStartup(tomeeHttpPort);

            getLog().info("Running '" + getClass().getName().replace("TomEEMojo", "").toLowerCase(Locale.ENGLISH)
                + "'. Configured TomEE in plugin is " + tomeeHost + ":" + tomeeHttpPort
                + " (plugin shutdown port is " + tomeeShutdownPort + " and https port is " + tomeeHttpsPort + ")");
        } else {
            getLog().info("Running '" + getClass().getSimpleName().replace("TomEEMojo", "").toLowerCase(Locale.ENGLISH));
        }

        final InputStream originalIn = System.in; // piped when starting resmote server so saving it

        serverCmd(server, strings);

        if (getWaitTomEE()) {
            final CountDownLatch stopCondition = new CountDownLatch(1);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    stopServer(stopCondition);
                }
            });

            if (useConsole) {
                final Scanner reader = new Scanner(originalIn);

                System.out.flush();
                getLog().info("Waiting for command: " + availableCommands());

                String line;
                while ((line = reader.nextLine()) != null) {

                    if (isQuit(line)) {
                        break;
                    }

                    if (!handleLine(line.trim())) {
                        System.out.flush();
                        getLog().warn("Command '" + line + "' not understood. Use one of " + availableCommands());
                    }
                }

                reader.close();
                stopServer(stopCondition); // better than using shutdown hook since it doesn't rely on the hook which are not sent by eclipse for instance
            }

            try {
                stopCondition.await();
            } catch (final InterruptedException e) {
                // no-op
            }
        }
    }

    protected List<String> generateJVMArgs() {
        final String deployOpenEjbAppKey = "openejb.system.apps";
        final String servletCompliance = "org.apache.catalina.STRICT_SERVLET_COMPLIANCE";

        boolean deactivateStrictServletCompliance = args == null || !args.contains(servletCompliance);

        final List<String> strings = new ArrayList<String>();
        if (systemVariables != null) {
            for (final Map.Entry<String, String> entry : systemVariables.entrySet()) {
                final String key = entry.getKey();
                if (servletCompliance.equals(key)) {
                    deactivateStrictServletCompliance = false;
                }

                final String value = entry.getValue();
                if (value == null) {
                    strings.add("-D" + key);
                } else if (value.contains(" ")) {
                    strings.add(String.format("'-D%s=%s'", key, value));
                } else {
                    strings.add(String.format("-D%s=%s", key, value));
                }

                if (deployOpenEjbAppKey.equals(key)) {
                    deployOpenEjbApplication = true;
                }
            }
        }

        if (webappDefaultConfig) {
            forceDefaultForNiceWebAppDevelopment();
        }

        if (deactivateStrictServletCompliance) {
            strings.add("-D" + servletCompliance + "=false");
        }
        if (quickSession) {
            strings.add("-Dopenejb.session.manager=org.apache.tomee.catalina.session.QuickSessionManager");
        }
        if (removeTomeeWebapp && ejbRemote) { // if we have tomee webapp no need to activate ejb remote support this way
            strings.add("-Dtomee.remote.support=true");
        }
        if (!deployOpenEjbApplication) { // true is the default so don't need to set the property
            if (args == null || !args.contains("-D" + deployOpenEjbAppKey)) {
                strings.add("-D" + deployOpenEjbAppKey + "=false");
            }
        }
        if (args != null) {
            strings.addAll(Arrays.asList(args.split(" ")));
        }
        if (javaagents != null) {
            for (final String rawJavaagent : javaagents) {
                final String javaagent;
                final String args;
                int argsIdx = rawJavaagent.indexOf('=');
                if (argsIdx < 0) {
                    argsIdx = rawJavaagent.indexOf('?');
                }
                if (argsIdx > 0) {
                    javaagent = rawJavaagent.substring(0, argsIdx);
                    args = rawJavaagent.substring(argsIdx);
                } else {
                    javaagent = rawJavaagent;
                    args = "";
                }

                if (!new File(javaagent).isFile()) {
                    try {
                        strings.add("-javaagent:" + mvnToFile(javaagent, "jar") + args);
                    } catch (final Exception e) {
                        getLog().warn("Can't find " + javaagent);
                        strings.add("-javaagent:" + javaagent);
                    }
                } else {
                    strings.add("-javaagent:" + javaagent);
                }
            }
        }

        if (forceReloadable) {
            strings.add("-Dtomee.force-reloadable=true");
        }

        if (!getWaitTomEE()) {
            strings.add("-Dtomee.noshutdownhook=true");
        }

        String appName = null; // computed lazily
        if (docBases != null && !docBases.isEmpty()) {
            if ("war".equals(packaging)) {
                appName = destinationName().replace(".war", "");
                if (appName.startsWith("/")) {
                    appName = appName.substring(1);
                }
                strings.add("-Dtomee." + appName + ".docBases=" + filesToString(docBases));
                strings.add("-Dtomee." + appName + ".docBases.cache=false"); // doesn't work for dev if activated
            } else {
                getLog().warn("docBases parameter only valid for a war");
            }
        }

        if (externalRepositories != null && !externalRepositories.isEmpty()) {
            if ("war".equals(packaging)) {
                appName = appName == null ? destinationName().replace(".war", "") : appName;
                if (appName.startsWith("/")) {
                    appName = appName.substring(1);
                }
                strings.add("-Dtomee." + appName + ".externalRepositories=" + filesToString(externalRepositories));
            } else {
                getLog().warn("externalRepositories parameter only valid for a war");
            }
        }

        if (skipWarResources) {
            strings.add("-Dtomee.skip-war-resources=" + skipWarResources);
        }

        return strings;
    }

    private void forceDefaultForNiceWebAppDevelopment() {
        if (!deployOpenEjbApplication) {
            getLog().info("Forcing deployOpenEjbApplication=true to be able to type 'reload[ENTER]' when classes are updated");
            deployOpenEjbApplication = true;
        }
        if (!forceReloadable) {
            getLog().info("Forcing forceReloadable=true to be able to type 'reload[ENTER]' when classes are updated");
            forceReloadable = true;
        }
        if (!skipWarResources) {
            getLog().info("Forcing skipWarResources=true to be able to refresh resources with F5");
            skipWarResources = true;
        }
        if (docBases == null) {
            docBases = new ArrayList<File>();
        }
        if (docBases.isEmpty() && webappResources.exists()) {
            getLog().info("adding " + webappResources.toString() + " docBase");
            docBases.add(webappResources);
        }
        if (externalRepositories == null) {
            externalRepositories = new ArrayList<File>();
        }
        if (externalRepositories.isEmpty() && webappClasses.exists()) {
            getLog().info("adding " + webappClasses.toString() + " externalRepository");
            externalRepositories.add(webappClasses);
        }
    }

    private static String filesToString(final Collection<File> files) {
        final Collection<String> paths = new ArrayList<String>(files.size());
        for (final File path : files) { // don't use relative paths (toString())
            paths.add(path.getAbsolutePath());
        }
        return Join.join(",", paths);
    }

    protected Collection<String> availableCommands() {
        return Arrays.asList(QUIT_CMD, EXIT_CMD);
    }

    protected synchronized void stopServer(final CountDownLatch stopCondition) {
        if (server == null) {
            return;
        }

        try {
            server.stop();
        } catch (final Exception e) {
            // no-op
        }
        try {
            server.getServer().waitFor();
            getLog().info(container + " stopped");
        } catch (final Exception e) {
            getLog().error("Can't stop " + container, e);
        }

        server = null;
        stopCondition.countDown();
    }

    private static boolean isQuit(String line) {
        if (QUIT_CMD.equalsIgnoreCase(line) || EXIT_CMD.equalsIgnoreCase(line)) {
            return true;
        }

        //http://youtrack.jetbrains.com/issue/IDEA-94826
        line = new StringBuilder(line).reverse().toString();

        return QUIT_CMD.equalsIgnoreCase(line) || EXIT_CMD.equalsIgnoreCase(line);
    }

    protected boolean handleLine(final String line) {
        return false;
    }

    protected void serverCmd(final RemoteServer server, final List<String> strings) {
        server.start(strings, getCmd(), checkStarted);
    }

    protected void addShutdownHooks(final RemoteServer server) {
        // no-op
    }

    protected int getConnectAttempts() {
        return (tomeeShutdownAttempts == 0 ? 60 : tomeeShutdownAttempts);
    }

    protected boolean getWaitTomEE() {
        return true;
    }

    private File resolve() {
        if (!settings.isOffline()) {
            try {
                if ("snapshots".equals(apacheRepos) || "true".equals(apacheRepos)) {
                    remoteRepos.add(new DefaultArtifactRepository("apache", "https://repository.apache.org/content/repositories/snapshots/",
                        new DefaultRepositoryLayout(),
                        new ArtifactRepositoryPolicy(true, UPDATE_POLICY_DAILY, CHECKSUM_POLICY_WARN),
                        new ArtifactRepositoryPolicy(false, UPDATE_POLICY_NEVER, CHECKSUM_POLICY_WARN)));
                } else {
                    try {
                        new URI(apacheRepos); // to check it is a uri
                        remoteRepos.add(new DefaultArtifactRepository("additional-repo-tomee-mvn-plugin", apacheRepos,
                            new DefaultRepositoryLayout(),
                            new ArtifactRepositoryPolicy(true, UPDATE_POLICY_DAILY, CHECKSUM_POLICY_WARN),
                            new ArtifactRepositoryPolicy(true, UPDATE_POLICY_NEVER, CHECKSUM_POLICY_WARN)));
                    } catch (final URISyntaxException e) {
                        // ignored, use classical repos
                    }
                }
            } catch (final UnsupportedOperationException uoe) {
                // can happen if remoterepos is unmodifiable (possible in complex builds)
                // no-op
            }
        } else if (remoteRepos != null && remoteRepos.isEmpty()) {
            remoteRepos = new ArrayList<ArtifactRepository>();
        }

        if ((tomeeClassifier != null && (tomeeClassifier.isEmpty() || tomeeClassifier.equals("ignore")))
            || ("org.apache.openejb".equals(tomeeGroupId) && "openejb-standalone".equals(tomeeArtifactId))) {
            tomeeClassifier = null;
        }

        try {
            final Artifact artifact = factory.createDependencyArtifact(tomeeGroupId, tomeeArtifactId, createFromVersion(tomeeVersion), tomeeType, tomeeClassifier, SCOPE_COMPILE);
            resolver.resolve(artifact, remoteRepos, local);
            return artifact.getFile();
        } catch (final Exception e) {
            getLog().error(e.getMessage(), e);
            throw new TomEEException(e.getMessage(), e);
        }
    }

    private void unzip(final File mvnTomEE) {
        ZipFile in = null;
        try {
            in = new ZipFile(mvnTomEE);

            final Enumeration<? extends ZipEntry> entries = in.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                int idx = name.indexOf("/");
                if (idx < 0) {
                    idx = name.indexOf(File.separator);
                }
                if (idx < 0) {
                    continue;
                }
                name = name.substring(idx + 1);
                final File dest = new File(catalinaBase.getAbsolutePath(), name);
                if (!dest.exists()) {
                    final File parent = dest.getParentFile();
                    if ((!parent.exists() && !parent.mkdirs())
                        || (!parent.canWrite() && !parent.setWritable(true))
                        || (!parent.canRead() && !parent.setReadable(true))) {
                        throw new RuntimeException("Failed to create or set permissions on: " + parent);
                    }
                }
                if (entry.isDirectory()) {
                    if (!dest.exists() && !dest.mkdir()) {
                        throw new RuntimeException("Failed to create: " + dest);
                    }
                } else {
                    final FileOutputStream fos = new FileOutputStream(dest);
                    try {
                        copy(in.getInputStream(entry), fos);
                    } catch (final IOException e) {
                        // ignored
                    }
                    close(fos);

                    if (!dest.canRead() && !dest.setReadable(true)) {
                        throw new RuntimeException("Failed to set readable on: " + dest);
                    }
                    if (dest.getName().endsWith(".sh")) {
                        if (!dest.canExecute() && !dest.setExecutable(true)) {
                            throw new RuntimeException("Failed to set executable on: " + dest);
                        }
                    }
                }
            }

            File file = new File(catalinaBase, "conf/tomee.xml");
            if (file.exists()) {
                container = TOM_EE;
            } else {
                container = "OpenEJB";
                file = new File(catalinaBase, "conf/openejb.xml");
                if (file.exists()) {
                    webappDir = "apps";
                }
            }

            ensureAppsFolderExistAndIsConfiguredByDefault(file);

            getLog().info(container + " was unzipped in '" + catalinaBase.getAbsolutePath() + "'");
        } catch (final Exception e) {
            throw new TomEEException(e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                    // no-op
                }
            }
        }
    }

    private void ensureAppsFolderExistAndIsConfiguredByDefault(final File file) throws IOException {
        if ("openejb".equals(container.toLowerCase(Locale.ENGLISH))
            || (file.exists()
            && (
            (apps != null && !apps.isEmpty())
                || (!"pom".equals(packaging) && !"war".equals(packaging))))) { // webapps doesn't need apps folder in tomee
            final FileWriter writer = new FileWriter(file);
            final String rootTag = container.toLowerCase(Locale.ENGLISH);
            writer.write("<?xml version=\"1.0\"?>\n" +
                "<" + rootTag + ">\n" +
                "  <Deployments dir=\"apps\" />\n" +
                "</" + rootTag + ">\n");
            writer.close();

            final File appsFolder = new File(catalinaBase, "apps");
            if (!appsFolder.exists() && !appsFolder.mkdirs()) {
                throw new RuntimeException("Failed to create: " + appsFolder);
            }
        }
    }

    public abstract String getCmd();
}
