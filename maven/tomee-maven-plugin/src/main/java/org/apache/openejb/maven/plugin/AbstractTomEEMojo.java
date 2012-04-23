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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
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
import org.apache.openejb.config.RemoteServer;

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
     * @parameter expression="${tomee-plugin.version}" default-value="1.0.0-beta-3-SNAPSHOT"
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
    private int debugPort;

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
     * @parameter
     */
    protected List<String> libs;

    /**
     * @parameter
     */
    protected List<String> webapps;

    /**
     * @parameter default-value="${project.build.directory}/${project.build.finalName}.${project.packaging}"
     * @readonly
     */
    protected File warFile;

    /**
     * @parameter expression="${tomee-plugin.remove-default-webapps}" default-value="true"
     */
    private boolean removeDefaultWebapps;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        unzip(resolve(), catalinaBase);
        copyLibs(libs, new File(catalinaBase, libDir), "jar");
        copyLibs(webapps, new File(catalinaBase, webappDir), "war");
        overrideConf(config);
        overrideConf(bin);
        overrideConf(lib);
        overrideAddresses();
        if (removeDefaultWebapps) {
            removeDefaultWebapps();
        }
        copyWar();
        run();
    }

    private void removeDefaultWebapps() {
        final File webapps = new File(catalinaBase, "webapps");
        if (webapps.isDirectory()) {
            for (File webapp : webapps.listFiles()) {
                final String name = webapp.getName();
                if (webapp.isDirectory() && !name.equals("openejb") && !name.equals("tomee")) {
                    try {
                        deleteDirectory(webapp);
                    } catch (IOException ignored) {
                        // no-op
                    }
                }
            }
        }
    }

    private void copyLibs(final List<String> files, final File destParent, final String defaultType) {
        if (files == null || files.isEmpty()) {
            return;
        }

        for (String file : files) {
            copyLib(file, destParent, defaultType);
        }
    }

    private void copyLib(final String lib, final File destParent, final String defaultType) {
        InputStream is = null;
        OutputStream os = null;
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
            is = new BufferedInputStream(new FileInputStream(file));
            os = new BufferedOutputStream(new FileOutputStream(new File(destParent, file.getName())));
            copy(is, os);
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
            throw new TomEEException(e.getMessage(), e);
        } finally {
            close(is);
            close(os);
        }
    }

    private void copyWar() {
        final String name = warFile.getName();
        final File out = new File(catalinaBase, webappDir + "/" + name);
        delete(out);
        if (!out.isDirectory()) {
            final File unpacked = new File(catalinaBase, webappDir + "/" + name.substring(0, name.lastIndexOf('.')));
            delete(unpacked);
        }

        if (warFile.isDirectory()) {
            try {
                copyDirectory(warFile, out);
            } catch (IOException e) {
                throw new TomEEException(e.getMessage(), e);
            }
        } else {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = new FileInputStream(warFile);
                os = new FileOutputStream(out);
                copy(is, os);
            } catch (Exception e) {
                throw new TomEEException(e.getMessage(), e);
            } finally {
                close(is);
                close(os);
            }
        }
    }

    private void overrideAddresses() {
        final File serverXml = new File(catalinaBase, "conf/server.xml");
        final String value = read(serverXml);

        FileWriter writer = null;
        try {
            writer = new FileWriter(serverXml);
            writer.write(value
                    .replace("8080", Integer.toString(tomeeHttpPort))
                    .replace("8005", Integer.toString(tomeeShutdownPort))
                    .replace("localhost", tomeeHost)
                    .replace("webapps", webappDir));
            writer.close();
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
                if (f.isDirectory() || f.isHidden()) {
                    continue;
                }

                InputStream in = null;
                OutputStream out = null;
                try {
                    in = new FileInputStream(f);
                    out = new FileOutputStream(new File(catalinaBase, dir.getName() + "/" + f.getName()));
                    copy(in, out);
                } catch (Exception e) {
                    throw new TomEEException(e.getMessage(), e);
                } finally {
                    close(in);
                    close(out);
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

        System.setProperty("server.windows.fork", "true");

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

        final RemoteServer server = new RemoteServer(getConnectAttempts(), false);
        if (!getNoShutdownHook()) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override public void run() {
                    server.stop();
                }
            });
        }

        server.start(strings, getCmd(), false);

        if (!getNoShutdownHook()) {
            try {
                server.getServer().waitFor(); // connect attempts = 0
            } catch (InterruptedException e) {
                // ignored
            }
        }
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

        try {
            final Artifact artifact = factory.createDependencyArtifact(tomeeGroupId, tomeeArtifactId, createFromVersion(tomeeVersion), tomeeType, tomeeClassifier, SCOPE_COMPILE);
            resolver.resolve(artifact, remoteRepos, local);
            return artifact.getFile();
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
            throw new TomEEException(e.getMessage(), e);
        }
    }

    private static void unzip(File mvnTomEE, File catalinaBase) {
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
