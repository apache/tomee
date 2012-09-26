package org.apache.openejb.maven.plugin;
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
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Settings;
import org.apache.openejb.config.RemoteServer;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.Zips;
import org.apache.tomee.util.QuickServerXmlParser;

import static org.apache.maven.artifact.Artifact.SCOPE_COMPILE;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER;
import static org.apache.maven.artifact.versioning.VersionRange.createFromVersion;
import static org.apache.openejb.util.JarExtractor.delete;
import static org.codehaus.plexus.util.FileUtils.copyDirectory;
import static org.codehaus.plexus.util.FileUtils.deleteDirectory;
import static org.codehaus.plexus.util.IOUtil.close;
import static org.codehaus.plexus.util.IOUtil.copy;

public abstract class AbstractTomEEMojo extends AbstractAddressMojo {
    // if we get let say > 5 patterns like it we should create a LocationAnalyzer
    // for < 5 patterns it should be fine
    private static final String NAME_STR = "?name=";
    private static final String UNZIP_PREFIX = "unzip:";
    private static final String REMOVE_PREFIX = "remove:";


    /**
     * @component
     */
    protected ArtifactFactory factory;

    /**
     * @component
     */
    protected ArtifactResolver resolver;

    /**
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected ArtifactRepository local;

    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    protected List<ArtifactRepository> remoteRepos;

    /**
     * @parameter expression="${tomee-plugin.skipCurrentProject}" default-value="false"
     */
    protected boolean skipCurrentProject;

    /**
     * @parameter expression="${tomee-plugin.version}" default-value="1.5.1-SNAPSHOT"
     */
    protected String tomeeVersion;

    /**
     * @parameter expression="${tomee-plugin.groupId}" default-value="org.apache.openejb"
     */
    protected String tomeeGroupId;

    /**
     * @parameter expression="${tomee-plugin.artifactId}" default-value="apache-tomee"
     */
    protected String tomeeArtifactId;

    /**
     * @parameter expression="${tomee-plugin.type}" default-value="zip"
     * @readonly // while tar.gz is not managed
     */
    protected String tomeeType;

    /**
     * @parameter expression="${tomee-plugin.apache-repos}" default-value="snapshots"
     */
    protected String apacheRepos;

    /**
     * @parameter expression="${tomee-plugin.classifier}" default-value="webprofile"
     */
    protected String tomeeClassifier;

    /**
     * @parameter expression="${tomee-plugin.shutdown}" default-value="8005"
     */
    protected int tomeeShutdownPort = 8005;

    /**
     * @parameter expression="${tomee-plugin.ajp}" default-value="8009"
     */
    protected int tomeeAjpPort = 8009;

    /**
     * @parameter expression="${tomee-plugin.https}" default-value="8443"
     */
    protected int tomeeHttpsPort = 8080;

    /**
     * @parameter expression="${tomee-plugin.args}"
     */
    protected String args;

    /**
     * @parameter expression="${tomee-plugin.debug}" default-value="false"
     */
    protected boolean debug;

    /**
     * @parameter expression="${tomee-plugin.debugPort}" default-value="5005"
     */
    protected int debugPort;

    /**
     * @parameter default-value="${project.build.directory}/apache-tomee"
     * @readonly
     */
    protected File catalinaBase;

    /**
     * relative to tomee.base.
     *
     * @parameter default-value="webapps"
     */
    protected String webappDir;

    /**
     * relative to tomee.base.
     *
     * @parameter default-value="apps"
     */
    protected String appDir;

    /**
     * relative to tomee.base.
     *
     * @parameter default-value="lib"
     */
    protected String libDir;

    /**
     * @parameter expression="${tomee-plugin.conf}" default-value="${project.basedir}/src/main/tomee/conf"
     * @optional
     */
    protected File config;

    /**
     * @parameter expression="${tomee-plugin.bin}" default-value="${project.basedir}/src/main/tomee/bin"
     * @optional
     */
    protected File bin;

    /**
     * @parameter expression="${tomee-plugin.lib}" default-value="${project.basedir}/src/main/tomee/lib"
     * @optional
     */
    protected File lib;

    /**
     * @parameter
     */
    protected Map<String, String> systemVariables;

    /**
     * @parameter expression="${tomee-plugin.quick-session}" default-value="true"
     */
    private boolean quickSession;

    /**
     * supported formats:
     * --> groupId:artifactId:version...
     * --> unzip:groupId:artifactId:version...
     * --> remove:prefix (often prefix = artifactId)
     *
     * @parameter
     */
    protected List<String> libs;

    /**
     * @parameter
     */
    protected List<String> webapps;

    /**
     * @parameter
     */
    protected List<String> apps;

    /**
     * @parameter default-value="${project.build.directory}/${project.build.finalName}.${project.packaging}"
     * @readonly
     */
    protected File warFile;

    /**
     * @parameter expression="${tomee-plugin.remove-default-webapps}" default-value="true"
     */
    protected boolean removeDefaultWebapps;

    /**
     * @parameter expression="${tomee-plugin.remove-tomee-webapps}" default-value="false"
     */
    protected boolean removeTomeeWebapp;

    /**
     * @parameter expression="${project.packaging}"
     */
    protected String packaging;

    /**
     * @parameter expression="${tomee-plugin.keep-server-xml}" default-value="false"
     */
    protected boolean keepServerXmlAsthis;

    /**
     * The current user system settings for use in Maven.
     *
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    protected Settings settings;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        unzip(resolve(), catalinaBase);
        copyLibs(libs, new File(catalinaBase, libDir), "jar");
        copyLibs(webapps, new File(catalinaBase, webappDir), "war"); // TODO: manage custom context ?context=foo
        copyLibs(apps, new File(catalinaBase, appDir), "jar");
        overrideConf(config);
        overrideConf(bin);
        overrideConf(lib);
        if (!keepServerXmlAsthis) {
            overrideAddresses();
        }
        if (removeDefaultWebapps) {
            removeDefaultWebapps(removeTomeeWebapp);
        }
        if (!skipCurrentProject) {
            copyWar();
        }
        run();
    }

    private void removeDefaultWebapps(final boolean removeTomee) {
        final File webapps = new File(catalinaBase, webappDir);
        if (webapps.isDirectory()) {
            for (File webapp : webapps.listFiles()) {
                final String name = webapp.getName();
                if (webapp.isDirectory() && (removeTomee || !name.equals("tomee"))) {
                    try {
                        deleteDirectory(webapp);
                    } catch (IOException ignored) {
                        // no-op
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

        for (String file : files) {
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
            if (!extractedName.endsWith(defaultType)) {
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
                for (File file : files) {
                    if (!IO.delete(file)) {
                        file.deleteOnExit();
                    }
                    getLog().info("Deleted " + file.getPath());
                }
            }
        } else {
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

            try {
                final Artifact artifact = factory.createDependencyArtifact(infos[0], infos[1], createFromVersion(infos[2]), type, classifier, SCOPE_COMPILE);
                resolver.resolve(artifact, remoteRepos, local);
                final File file = artifact.getFile();

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
            } catch (Exception e) {
                getLog().error(e.getMessage(), e);
                throw new TomEEException(e.getMessage(), e);
            } finally {
                close(is);
                close(os);
            }
        }
    }

    private void copyWar() {
        if ("pom".equals(packaging)) {
            return;
        }

        final boolean war = "war".equals(packaging);
        final String name = warFile.getName();
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
        if (!out.isDirectory()) {
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
                copyDirectory(warFile, out);
            } catch (IOException e) {
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
            } catch (Exception e) {
                throw new TomEEException(e.getMessage(), e);
            } finally {
                close(is);
                close(os);
            }
        } else {
            getLog().warn("'" + warFile + "' doesn't exist, ignoring (maybe run mvn package before this plugin)");
        }
    }

    private void overrideAddresses() {
        final File serverXml = new File(catalinaBase, "conf/server.xml");
        final String value = read(serverXml);
        final QuickServerXmlParser parser = QuickServerXmlParser.parse(serverXml);

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
        } catch (IOException e) {
            throw new TomEEException(e.getMessage(), e);
        } finally {
            close(writer);
        }
    }

    private static String read(File file) {
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
        } catch (Exception e) {
            throw new TomEEException(e.getMessage(), e);
        } finally {
            close(in);
        }
    }

    private void overrideConf(final File dir) {
        if (!dir.exists()) {
            return;
        }

        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File f : files) {
                if (f.isHidden()) {
                    continue;
                }

                final String file = dir.getName() + "/" + f.getName();
                final File destination = new File(catalinaBase, file);
                if (f.isDirectory()) {
                    Files.mkdirs(destination);
                    try {
                        IO.copyDirectory(f, destination);
                    } catch (IOException e) {
                        throw new TomEEException(e.getMessage(), e);
                    }
                } else {
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = new FileInputStream(f);
                        out = new FileOutputStream(destination);
                        copy(in, out);

                        getLog().info("Override '" + file + "'");
                    } catch (Exception e) {
                        throw new TomEEException(e.getMessage(), e);
                    } finally {
                        close(in);
                        close(out);
                    }
                }
            }
        }
    }

    protected void run() {
        System.setProperty("openejb.home", catalinaBase.getAbsolutePath());
        if (debug) {
            System.setProperty("openejb.server.debug", "true");
            System.setProperty("server.debug.port", Integer.toString(debugPort));
        }

        final List<String> strings = new ArrayList<String>();
        if (systemVariables != null) {
            for (Map.Entry<String, String> entry : systemVariables.entrySet()) {
                if (entry.getValue().contains(" ")) {
                    strings.add(String.format("'-D%s=%s'", entry.getKey(), entry.getValue()));
                } else {
                    strings.add(String.format("-D%s=%s", entry.getKey(), entry.getValue()));
                }
            }
        }
        if (args != null) {
            strings.addAll(Arrays.asList(args.split(" ")));
        }
        if (getNoShutdownHook()) {
            strings.add("-Dtomee.noshutdownhook=true");
        }
        if (quickSession) {
            strings.add("-Dopenejb.session.manager=org.apache.tomee.catalina.session.QuickSessionManager");
        }

        System.setProperty("server.shutdown.port", Integer.toString(tomeeShutdownPort));
        final RemoteServer server = new RemoteServer(getConnectAttempts(), false);
        if (!getNoShutdownHook()) {
            addShutdownHooks(server);
        }

        getLog().info("Running '" + getClass().getSimpleName().replace("TomEEMojo", "").toLowerCase(Locale.ENGLISH)
                + "'. Configured TomEE in plugin is " + tomeeHost + ":" + tomeeHttpPort
                + " (plugin shutdown port is " + tomeeShutdownPort + ")");

        server.start(strings, getCmd(), false);

        if (!getNoShutdownHook()) {
            try {
                server.getServer().waitFor(); // connect attempts = 0
            } catch (InterruptedException e) {
                // ignored
            }
        }
    }

    protected void addShutdownHooks(final RemoteServer server) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override public void run() {
                server.stop();
            }
        });
    }

    protected  int getConnectAttempts() {
        return Integer.MAX_VALUE;
    }

    protected static String java() {
        return new File(System.getProperty("java.home"), "/bin/java").getAbsolutePath();
    }

    protected boolean getNoShutdownHook() {
        return true;
    }

    protected String cp() {
        final boolean unix = !System.getProperty("os.name").toLowerCase().startsWith("win");
        final char cpSep;
        if (unix) {
            cpSep = ':';
        } else {
            cpSep = ';';
        }

        return "bin/bootstrap.jar" + cpSep + "bin/tomcat-juli.jar";
    }

    private File resolve() {
        if (!settings.isOffline()) {
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
                } catch (URISyntaxException e) {
                    // ignored, use classical repos
                }
            }
        } else if (remoteRepos != null && remoteRepos.isEmpty()) {
            remoteRepos = new ArrayList<ArtifactRepository>();
        }

        try {
            final Artifact artifact = factory.createDependencyArtifact(tomeeGroupId, tomeeArtifactId, createFromVersion(tomeeVersion), tomeeType, tomeeClassifier, SCOPE_COMPILE);
            resolver.resolve(artifact, remoteRepos, local);
            return artifact.getFile();
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
            throw new TomEEException(e.getMessage(), e);
        }
    }

    private void unzip(File mvnTomEE, File catalinaBase) {
        ZipFile in = null;
        try {
            in = new ZipFile(mvnTomEE);

            final Enumeration<? extends ZipEntry> entries = in.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith("apache-tomee-")) {
                    int idx = name.indexOf("/");
                    if (idx < 0) {
                        idx = name.indexOf(File.separator);
                    }
                    if (idx < 0) {
                        continue;
                    }
                    name = name.substring(idx + 1);
                }
                final File dest = new File(catalinaBase.getAbsolutePath(), name);
                if (!dest.exists()) {
                    final File parent = dest.getParentFile();
                    parent.mkdirs();
                    parent.setWritable(true);
                    parent.setReadable(true);
                }
                if (entry.isDirectory()) {
                    dest.mkdir();
                } else {
                    final FileOutputStream fos = new FileOutputStream(dest);
                    try {
                        copy(in.getInputStream(entry), fos);
                    } catch (IOException e) {
                        // ignored
                    }
                    close(fos);

                    dest.setReadable(true);
                    if (dest.getName().endsWith(".sh")) {
                        dest.setExecutable(true);
                    }
                }
            }

            getLog().info("TomEE was unzipped in '" + catalinaBase.getAbsolutePath() + "'");
        } catch (Exception e) {
            throw new TomEEException(e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // no-op
                }
            }
        }
    }

    public abstract String getCmd();
}
