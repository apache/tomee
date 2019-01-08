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
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.config.RemoteServer;
import org.apache.openejb.config.sys.Deployments;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.Zips;
import org.apache.openejb.maven.plugin.cli.Args;
import org.apache.openejb.maven.util.XmlFormatter;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.NetworkUtil;
import org.apache.openejb.util.OpenEjbVersion;
import org.apache.tomee.util.QuickServerXmlParser;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.FileUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
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
import static org.apache.openejb.loader.Files.mkdirs;
import static org.apache.openejb.util.JarExtractor.delete;
import static org.codehaus.plexus.util.FileUtils.deleteDirectory;
import static org.codehaus.plexus.util.IOUtil.close;
import static org.codehaus.plexus.util.IOUtil.copy;

/**
 * The type AbstractTomEEMojo is the base class to all the maven actions privided by the plugin.
 */
public abstract class AbstractTomEEMojo extends AbstractAddressMojo {
    // if we get let say > 5 patterns like it we should create a LocationAnalyzer
    // for < 5 patterns it should be fine
    private static final String NAME_STR = "?name=";
    private static final String UNZIP_PREFIX = "unzip:";
    private static final String REMOVE_PREFIX = "remove:";
    /**
     * The constant QUIT_CMD.
     */
    public static final String QUIT_CMD = "quit";
    /**
     * The constant EXIT_CMD.
     */
    public static final String EXIT_CMD = "exit";
    /**
     * The constant TOM_EE.
     */
    public static final String TOM_EE = "TomEE";

    /**
     * The Factory.
     */
    @Component
    protected ArtifactFactory factory;

    /**
     * The Resolver.
     */
    @Component
    protected ArtifactResolver resolver;

    /**
     * The Local.
     */
    @Parameter(defaultValue = "${localRepository}", readonly = true)
    protected ArtifactRepository local;

    /**
     * The Remote repos.
     */
    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true)
    protected List<ArtifactRepository> remoteRepos;

    /**
     * The Skip current project.
     */
    @Parameter(property = "tomee-plugin.skipCurrentProject", defaultValue = "false")
    protected boolean skipCurrentProject;

    /**
     * The TomEE version.
     */
    @Parameter(property = "tomee-plugin.version", defaultValue = "-1")
    protected String tomeeVersion;

    /**
     * The TomEE group id.
     */
    @Parameter(property = "tomee-plugin.groupId", defaultValue = "org.apache.tomee")
    protected String tomeeGroupId;

    /**
     * The TomEE artifact id.
     */
    @Parameter(property = "tomee-plugin.artifactId", defaultValue = "apache-tomee")
    protected String tomeeArtifactId;

    /**
     * while tar.gz is not managed it is readonly
     */
    @Parameter(property = "tomee-plugin.type", defaultValue = "zip", readonly = true)
    protected String tomeeType;

    /**
     * The Apache repos.
     */
    @Parameter(property = "tomee-plugin.apache-repos", defaultValue = "snapshots")
    protected String apacheRepos;

    /**
     * tomee classifier to use (webprofile or plus)
     */
    @Parameter(property = "tomee-plugin.classifier", defaultValue = "webprofile")
    protected String tomeeClassifier;

    /**
     * The TomEE shutdown port.
     */
    @Parameter(property = "tomee-plugin.shutdown")
    protected String tomeeShutdownPort;

    /**
     * The TomEE shutdown attempts.
     */
    @Parameter(property = "tomee-plugin.shutdown.attempts", defaultValue = "60")
    protected int tomeeShutdownAttempts;

    /**
     * The TomEE shutdown command.
     */
    @Parameter(property = "tomee-plugin.shutdown-command", defaultValue = "SHUTDOWN")
    protected String tomeeShutdownCommand;

    /**
     * The TomEE ajp port.
     */
    @Parameter(property = "tomee-plugin.ajp")
    protected String tomeeAjpPort;

    /**
     * The Args.
     */
    @Parameter(property = "tomee-plugin.args")
    protected String args;

    /**
     * The Debug.
     */
    @Parameter(property = "tomee-plugin.debug", defaultValue = "false")
    protected boolean debug;

    /**
     * The Simple log.
     */
    @Parameter(property = "tomee-plugin.simple-log", defaultValue = "false")
    protected boolean simpleLog;

    /**
     * The Extract wars.
     */
    @Parameter(property = "tomee-plugin.extractWars", defaultValue = "false")
    protected boolean extractWars;

    /**
     * The Strip war version.
     */
    @Parameter(property = "tomee-plugin.stripWarVersion", defaultValue = "true")
    protected boolean stripWarVersion;

    /**
     * The Strip version.
     */
    @Parameter(property = "tomee-plugin.stripVersion", defaultValue = "false")
    protected boolean stripVersion;

    /**
     * The Debug port.
     */
    @Parameter(property = "tomee-plugin.debugPort", defaultValue = "5005")
    protected int debugPort;

    /**
     * The Webapp resources.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/webapp", property = "tomee-plugin.webappResources")
    protected File webappResources;

    /**
     * The Webapp classes.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "tomee-plugin.webappClasses")
    protected File webappClasses;

    /**
     * The Catalina base.
     */
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
    @Parameter
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

    /**
     * The Main dir.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main")
    protected File mainDir;

    /**
     * The Target.
     */
    @Parameter(defaultValue = "${project.build.directory}")
    protected File target;

    /**
     * The Config.
     */
    @Parameter(property = "tomee-plugin.conf", defaultValue = "${project.basedir}/src/main/tomee/conf")
    protected File config;

    /**
     * The Bin.
     */
    @Parameter(property = "tomee-plugin.bin", defaultValue = "${project.basedir}/src/main/tomee/bin")
    protected File bin;

    /**
     * The Lib.
     */
    @Parameter(property = "tomee-plugin.lib", defaultValue = "${project.basedir}/src/main/tomee/lib")
    protected File lib;

    /**
     * The System variables.
     */
    @Parameter
    protected Map<String, String> systemVariables;

    /**
     * The Classpaths.
     */
    @Parameter
    protected List<String> classpaths;

    /**
     * The Classpath separator.
     */
    @Parameter(property = "tomee-plugin.classpathSeparator")
    protected String classpathSeparator;

    /**
     * The Customizers.
     */
    @Parameter
    protected List<String> customizers;

    /**
     * The Js customizers.
     */
    @Parameter
    protected List<String> jsCustomizers;

    /**
     * The Groovy customizers.
     */
    @Parameter
    protected List<String> groovyCustomizers;

    /**
     * The Project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

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
     * force webapp to be reloadable
     */
    @Parameter(property = "tomee-plugin.jsp-development", defaultValue = "true")
    protected boolean forceJspDevelopment;

    /**
     * supported formats:
     * --> groupId:artifactId:version...
     * --> unzip:groupId:artifactId:version...
     * --> remove:prefix (often prefix = artifactId)
     */
    @Parameter
    protected List<String> libs;

    /**
     * The Endorsed libs.
     */
    @Parameter
    protected List<String> endorsedLibs;

    /**
     * The Javaagents.
     */
    @Parameter
    protected List<String> javaagents;

    /**
     * The Persist javaagents.
     */
    @Parameter(property = "tomee-plugin.persist-javaagents", defaultValue = "false")
    protected boolean persistJavaagents;

    /**
     * The Webapps.
     */
    @Parameter
    protected List<String> webapps;

    /**
     * The Apps.
     */
    @Parameter
    protected List<String> apps;

    /**
     * The Classes.
     */
    @Parameter(property = "tomee-plugin.classes", defaultValue = "${project.build.outputDirectory}", readonly = true)
    protected File classes;

    /**
     * The War file.
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.${project.packaging}")
    protected File warFile;

    /**
     * The Work war file.
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}", readonly = true)
    protected File workWarFile;

    /**
     * The Final name.
     */
    @Parameter(defaultValue = "${project.build.finalName}", readonly = true)
    protected String finalName;

    /**
     * The Artifact id.
     */
    @Parameter(defaultValue = "${project.artifactId}", readonly = true)
    protected String artifactId;

    /**
     * The Remove default webapps.
     */
    @Parameter(property = "tomee-plugin.remove-default-webapps", defaultValue = "true")
    protected boolean removeDefaultWebapps;

    /**
     * The Deploy open ejb application.
     */
    @Parameter(property = "tomee-plugin.deploy-openejb-internal-application", defaultValue = "false")
    protected boolean deployOpenEjbApplication;

    /**
     * The Remove tomee webapp.
     */
    @Parameter(property = "tomee-plugin.remove-tomee-webapps", defaultValue = "true")
    protected boolean removeTomeeWebapp;

    /**
     * The Ejb remote.
     */
    @Parameter(property = "tomee-plugin.ejb-remote", defaultValue = "true")
    protected boolean ejbRemote;

    /**
     * The Packaging.
     */
    @Parameter(defaultValue = "${project.packaging}", readonly = true)
    protected String packaging;

    /**
     * The Check started.
     */
    @Parameter(property = "tomee-plugin.check-started", defaultValue = "false")
    protected boolean checkStarted;

    /**
     * The Use console.
     */
    @Parameter(property = "tomee-plugin.use-console", defaultValue = "true")
    protected boolean useConsole;

    /**
     * The TomEE already installed.
     */
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
     * server.xml configured inlined (is Server tag is the first child of inlinedServerXml)
     */
    @Parameter
    protected PlexusConfiguration inlinedServerXml;

    /**
     * tomee.xml configured inlined (is tomee tag is the first child of inlinedTomEEXml)
     */
    @Parameter
    protected PlexusConfiguration inlinedTomEEXml;

    /**
     * if a file is already there when unpacking tomee zip should it be overriden?
     */
    @Parameter(property = "tomee-plugin.override-on-unzip", defaultValue = "true")
    protected boolean overrideOnUnzip;
    /**
     * if a file is already there when unpacking tomee zip should it be overriden?
     */
    @Parameter(property = "tomee-plugin.skip-root-folder-on-unzip", defaultValue = "true")
    protected boolean skipRootFolderOnUnzip;

    /**
     * the actual path used in server.xml for the https keystore if relevant.
     * Common usage will be to put in src/main/tomee/conf a keystore foo.jks
     * and set this value to ${catalina.base}/foo.jks.
     * <p/>
     * Note: if not set we'll check for any *.jks in conf/. You can set it to "ignore" to skip this.
     */
    @Parameter(property = "tomee-plugin.keystore")
    protected String keystore;

    /**
     * The Deployed file.
     */
    protected File deployedFile = null;
    /**
     * The Server.
     */
    protected RemoteServer server = null;
    /**
     * The Container.
     */
    protected String container = TOM_EE;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        fixConfig();

        if ("-1".equals(tomeeVersion)) {
            tomeeVersion = OpenEjbVersion.get().getVersion();
        }

        if (!tomeeAlreadyInstalled) {
            final Collection<String> existingWebapps; // added before using the plugin with maven dependency plugin or sthg like that
            if (removeDefaultWebapps) {
                existingWebapps = webappsAlreadyAdded();
            } else {
                existingWebapps = Collections.emptyList();
            }

            unzip(resolve());

            if (inlinedServerXml != null && inlinedServerXml.getChildCount() > 0) {
                final File serverXml = new File(catalinaBase, "conf/server.xml");
                try {
                    FileUtils.forceMkdir(serverXml.getParentFile());
                    FileUtils.fileWrite(serverXml, XmlFormatter.format(inlinedServerXml.getChild(0).toString()));
                } catch (final Exception e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
            if (inlinedTomEEXml != null && inlinedTomEEXml.getChildCount() > 0) {
                final File tomeeXml = new File(catalinaBase, "conf/tomee.xml");
                try {
                    FileUtils.forceMkdir(tomeeXml.getParentFile());
                    FileUtils.fileWrite(tomeeXml, XmlFormatter.format(inlinedTomEEXml.getChild(0).toString()));
                } catch (final Exception e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }

            overrideConf(config, "conf");
            overrideServerXml();
            alignConfigOnServerXmlCurrentConfig();

            if (removeDefaultWebapps) { // do it first to let add other war
                removeDefaultWebapps(removeTomeeWebapp, existingWebapps);
            }

            if (classpaths == null) { // NPE protection for activateSimpleLog() and run()
                classpaths = new ArrayList<>();
            }
            if (simpleLog) {
                activateSimpleLog();
            }

            copyLibs(libs, new File(catalinaBase, libDir), "jar");
            copyLibs(endorsedLibs, new File(catalinaBase, "endorsed"), "jar");
            copyLibs(webapps, new File(catalinaBase, webappDir), "war");
            copyLibs(apps, new File(catalinaBase, appDir), "jar");
            overrideConf(lib, "lib");
            final Collection<File> copied = overrideConf(bin, "bin");

            for (final File copy : copied) {
                if (copy.getName().endsWith(".sh")) {
                    if (!copy.setExecutable(true)) {
                        getLog().warn("can't make " + copy.getPath() + " executable");
                    }
                }
            }

            if (!skipCurrentProject) {
                copyWar();
            }

            if (customizers != null) {
                final Thread thread = Thread.currentThread();
                final ClassLoader currentLoader = thread.getContextClassLoader();
                final ClassLoader tccl = createClassLoader(currentLoader);
                thread.setContextClassLoader(tccl);
                try {
                    // a customizer is a Runnable with or without a constructor taking a File as parameter (catalina base)
                    // one goal is to avoid coupling as much as possible with this plugin
                    //
                    // if really needed we could introduce a Customizer interface but then it has more impacts on your packaging/config
                    for (final String customizer : customizers) {
                        try {
                            final Class<?> clazz = tccl.loadClass(customizer);
                            try {
                                clazz.getMethod("main", String[].class)
                                        .invoke(null, new String[]{catalinaBase.getAbsolutePath()});
                            } catch (final NoSuchMethodException noMainEx) {
                                try {
                                    final Constructor<?> cons = clazz.getConstructor(File.class);
                                    Runnable.class.cast(cons.newInstance(catalinaBase)).run();
                                } catch (final NoSuchMethodException e) {
                                    try {
                                        Runnable.class.cast(clazz.newInstance()).run();
                                    } catch (final Exception e1) {
                                        throw new MojoExecutionException("can't create customizer: " + currentLoader, e);
                                    }
                                } catch (final InvocationTargetException | InstantiationException | IllegalAccessException e) {
                                    throw new MojoExecutionException("can't create customizer: " + currentLoader, e);
                                }
                            } catch (final InvocationTargetException | IllegalAccessException e) {
                                throw new MojoExecutionException("can't find customizer: " + currentLoader, e);
                            }
                        } catch (final ClassNotFoundException e) {
                            throw new MojoExecutionException("can't find customizer: " + currentLoader, e);
                        }
                    }
                } finally {
                    try {
                        if (tccl != null && Closeable.class.isInstance(tccl)) {
                            Closeable.class.cast(tccl).close();
                        }
                    } catch (final IOException e) {
                        // no-op
                    }
                    thread.setContextClassLoader(currentLoader);
                }
            }

            scriptCustomization(jsCustomizers, "js");
            scriptCustomization(groovyCustomizers, "groovy");
        } else {
            alignConfigOnServerXmlCurrentConfig();
        }

        run();
    }

    private void scriptCustomization(final List<String> customizers, final String ext) throws MojoExecutionException {
        if (customizers != null) {
            final ScriptEngine engine = new ScriptEngineManager().getEngineByExtension(ext);
            if (engine == null) {
                throw new IllegalStateException("No engine for " + ext + ". Maybe add the JSR223 implementation as plugin dependency.");
            }
            for (final String js : customizers) {
                try {
                    final SimpleBindings bindings = new SimpleBindings();
                    bindings.put("catalinaBase", catalinaBase.getAbsolutePath());
                    bindings.put("resolver", new Resolver() {
                        @Override
                        public File resolve(final String group, final String artifact, final String version,
                                            final String classifier, final String type) {
                            try {
                                return AbstractTomEEMojo.this.resolve(group, artifact, version, classifier, type).resolved;
                            } catch (final ArtifactResolutionException | ArtifactNotFoundException e) {
                                throw new IllegalArgumentException(e);
                            }
                        }
                        @Override
                        public File resolve(final String group, final String artifact, final String version) {
                            try {
                                return AbstractTomEEMojo.this.resolve(group, artifact, version, null, "jar").resolved;
                            } catch (final ArtifactResolutionException | ArtifactNotFoundException e) {
                                throw new IllegalArgumentException(e);
                            }
                        }
                        @Override
                        public File resolve(final String group, final String artifact, final String version, final String type) {
                            try {
                                return AbstractTomEEMojo.this.resolve(group, artifact, version, null, type).resolved;
                            } catch (final ArtifactResolutionException | ArtifactNotFoundException e) {
                                throw new IllegalArgumentException(e);
                            }
                        }
                    });
                    engine.eval(new StringReader(js), bindings);
                } catch (final ScriptException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
        }
    }

    private void alignConfigOnServerXmlCurrentConfig() {
        final File sXml = new File(catalinaBase, "conf/server.xml");
        if (sXml.isFile()) {
            final QuickServerXmlParser quickServerXmlParser = QuickServerXmlParser.parse(sXml, false);
            tomeeHttpPort = quickServerXmlParser.value("HTTP", null);
            tomeeHttpsPort = quickServerXmlParser.value("HTTPS", null);
            tomeeAjpPort = quickServerXmlParser.value("AJP", null);
            tomeeShutdownPort = quickServerXmlParser.value("STOP", null);
            final String host = quickServerXmlParser.value("host", null);
            if (host != null) {
                tomeeHost = host;
            }
            final String appBase = quickServerXmlParser.value("app-base", null);
            if (appBase != null) {
                webappDir = appBase;
            }
        }
        if (webappDir == null) {
            webappDir = "webapps";
        }
    }

    @SuppressWarnings("unchecked")
    private ClassLoader createClassLoader(final ClassLoader parent) {
        final List<URL> urls = new ArrayList<>();
        for (final Artifact artifact : (Collection<Artifact>) project.getArtifacts()) {
            try {
                urls.add(artifact.getFile().toURI().toURL());
            } catch (final MalformedURLException e) {
                getLog().warn("can't use artifact " + artifact.toString());
            }
        }
        if (classes != null && classes.exists()) {
            try {
                urls.add(classes.toURI().toURL());
            } catch (final MalformedURLException e) {
                getLog().warn("can't use path " + classes.getAbsolutePath());
            }
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
    }

    /**
     * Fix config.
     */
    protected void fixConfig() {
        if (useOpenEJB) {
            tomeeGroupId = "org.apache.tomee";
            tomeeArtifactId = "openejb-standalone";
            tomeeClassifier = null;
            tomeeShutdownCommand = "Q";
            if ("8005".equals(tomeeShutdownPort)) { // default admin port
                tomeeShutdownPort = "4200";
            }
            if (tomeeVersion.startsWith("2.")) {
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

    /**
     * Gets additional classpath.
     *
     * @return the additional classpath
     */
    protected String getAdditionalClasspath() {
        if (!classpaths.isEmpty()) {
            final StringBuilder cpBuilder = new StringBuilder();
            for (final String cp : classpaths) {
                final String[] split = cp.split(":");
                if (split.length >= 3 /*GAV*/) {
                    final FileWithMavenMeta jar;
                    try {
                        jar = resolve(split[0], split[1], split[2],
                                split.length > 4 ? split[4] : null, split.length > 3 ? split[3] : "jar");
                    } catch (final ArtifactResolutionException | ArtifactNotFoundException e) {
                        throw new IllegalArgumentException(e);
                    }

                    final File classpathRoot = new File(catalinaBase, "boot");
                    if (!classpathRoot.isDirectory()) {
                        mkdirs(classpathRoot);
                    }

                    final File target = new File(classpathRoot, stripVersion ? jar.stripVersion(true) : jar.resolved.getName());
                    try {
                        IO.copy(jar.resolved, target);
                    } catch (final IOException e) {
                        throw new IllegalArgumentException(e);
                    }

                    cpBuilder.append("${openejb.base}/boot/").append(target.getName());
                } else { // else plain path
                    cpBuilder.append(cp);
                }
                if (classpathSeparator == null) {
                    classpathSeparator = File.pathSeparator;
                }
                cpBuilder.append(classpathSeparator);
            }
            return cpBuilder.substring(0, cpBuilder.length() - 1); // Dump the final path separator
        }
        return null;
    }

    private List<String> webappsAlreadyAdded() {
        final List<String> list = new ArrayList<String>();
        final File webapps = new File(catalinaBase, webappDirOrImplicitDefault());
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

    private String webappDirOrImplicitDefault() {
        return webappDir == null ? "webapps" : webappDir;
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

                doWrite(loggingProperties, content);
            } catch (final Exception e) {
                getLog().error("Can't set SimpleTomEEFormatter", e);
            }
        }
    }

    private void removeDefaultWebapps(final boolean removeTomee, final Collection<String> providedWebapps) {
        final File webapps = new File(catalinaBase, webappDirOrImplicitDefault());
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

    private void updateLib(final String rawLib, final File rawDestParent, final String defaultType) {
        InputStream is = null;
        OutputStream os = null;

        // special hook to get more info
        String lib = rawLib;
        String extractedName = null;
        if (lib.contains(NAME_STR)) {
            lib = lib.substring(0, rawLib.indexOf(NAME_STR));
            extractedName = rawLib.substring(rawLib.indexOf(NAME_STR) + NAME_STR.length(), rawLib.length());
            if (!extractedName.endsWith(".jar") && !extractedName.endsWith(".war")
                    && !extractedName.endsWith(".ear") && !extractedName.endsWith(".rar")) {
                extractedName = extractedName + "." + defaultType;
            }
        }

        final boolean isWar = "war".equals(defaultType);
        final boolean isExplodedWar = extractWars && isWar;

        boolean unzip = isExplodedWar;
        if (lib.startsWith(UNZIP_PREFIX)) {
            lib = lib.substring(UNZIP_PREFIX.length());
            unzip = true;
        }

        File destParent = rawDestParent;
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
                final FileWithMavenMeta file = mvnToFile(lib, defaultType);
                if (extractedName == null && (stripVersion || isWar && stripWarVersion)) {
                    extractedName = isCurrentArtifact(file) && file.version != null ? finalName.replace("-" + file.version, "") :
                            file.stripVersion(!isExplodedWar);
                }

                if (!unzip) {
                    final File dest;
                    if (extractedName == null) {
                        dest = new File(destParent, file.resolved.getName());
                    } else {
                        dest = new File(destParent, extractedName);
                    }

                    is = new BufferedInputStream(new FileInputStream(file.resolved));
                    os = new BufferedOutputStream(new FileOutputStream(dest));
                    copy(is, os);

                    getLog().info("Copied '" + lib + "' in '" + dest.getAbsolutePath());
                } else {
                    if (isExplodedWar) {
                        destParent = Files.mkdirs(new File(rawDestParent, extractedName != null ?
                                extractedName : file.resolved.getName().replace(".war", "")));
                    }
                    Zips.unzip(file.resolved, destParent, !isExplodedWar);

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

    private boolean isCurrentArtifact(final FileWithMavenMeta file) {
        return file.artifact.equals(artifactId);
    }

    private FileWithMavenMeta mvnToFile(final String lib, final String defaultType) throws ArtifactResolutionException, ArtifactNotFoundException {
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

        return resolve(infos[0], infos[1], infos[2], classifier, type);
    }

    private FileWithMavenMeta resolve(final String group, final String artifact, final String version, final String classifier, final String type) throws ArtifactResolutionException, ArtifactNotFoundException {
        final Artifact dependencyArtifact = factory.createDependencyArtifact(group, artifact, createFromVersion(version), type, classifier, SCOPE_COMPILE);
        resolver.resolve(dependencyArtifact, remoteRepos, local);
        return new FileWithMavenMeta(group, artifact, version, classifier, type, dependencyArtifact.getFile());
    }

    private void copyWar() {
        if ("pom".equals(packaging)) {
            return;
        }

        final boolean war = "war".equals(packaging);
        final String name = destinationName();
        File out;
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

        if (extractWars) {
            warFile = workWarFile;
            if (context == null && out.getName().endsWith(".war") && !warFile.getName().endsWith(".war")) {
                out = new File(out.getParentFile(), warFile.getName());
            }
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

    /**
     * Destination name string.
     *
     * @return the string
     */
    protected String destinationName() {
        if (context != null) {
            if (!context.contains(".") && !warFile.isDirectory()) {
                return context + "." + packaging;
            }
            return context;
        }
        return warFile.getName();
    }

    private void overrideServerXml() {
        final File serverXml = new File(catalinaBase, "conf/server.xml");
        if (!serverXml.exists()) { // openejb
            return;
        }

        final QuickServerXmlParser parser = QuickServerXmlParser.parse(serverXml);

        final String original = read(serverXml);
        String value = original;

        if (tomeeHttpsPort != null && tomeeHttpsPort.length() > 0 && parser.value("HTTPS", null) == null) {
            String keystorePath = keystore != null ? keystore : parser.keystore();
            if (keystorePath == null) {
                final File conf = new File(catalinaBase, "conf");
                if (conf.isDirectory()) {
                    final File[] jks = conf.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(final File dir, final String name) {
                            return name.endsWith(".jks");
                        }
                    });
                    if (jks != null && jks.length == 1) {
                        keystorePath = "${catalina.base}/conf/" + jks[0].getName();
                    } else {
                        throw new IllegalArgumentException("Ambiguous jks in conf/,please use <keystore /> to force it.");
                    }
                }
            }

            if (keystorePath == null) {
                throw new IllegalArgumentException("No keystore specified, please use <keystore></keystore>");
            }

            // ensure connector is not commented
            value = value.replace("<Service name=\"Catalina\">", "<Service name=\"Catalina\">\n"
                    + "    <Connector port=\"" + tomeeHttpsPort + "\" protocol=\"HTTP/1.1\" SSLEnabled=\"true\"\n" +
                    "                scheme=\"https\" secure=\"true\"\n" +
                    "                clientAuth=\"false\" sslProtocol=\"TLS\" keystoreFile=\"" + keystorePath + "\" />\n");
        }

        if (tomeeHttpPort != null) {
            value = value.replace("\"" + parser.http() + "\"", "\"" + tomeeHttpPort + "\"");
        }
        if (tomeeHttpsPort != null) {
            value = value.replace("\"" + parser.https() + "\"", "\"" + tomeeHttpsPort + "\"");
        }
        if (tomeeAjpPort != null) {
            value = value.replace("\"" + parser.ajp() + "\"", "\"" + tomeeAjpPort + "\"");
        }
        if (tomeeShutdownPort != null) {
            value = value.replace("\"" + parser.stop() + "\"", "\"" + tomeeShutdownPort + "\"");
        }
        if (webappDir != null) {
            value = value.replace("\"" + parser.value("app-base", "webapps") + "\"", "\"" + webappDir + "\"");
        }
        if (tomeeHost != null) {
            value = value.replace("\"" + parser.host() + "\"", "\"" + tomeeHost + "\"");
        }

        if (!original.equals(value)) {
            FileWriter writer = null;
            try {
                writer = new FileWriter(serverXml);
                writer.write(value);
            } catch (final IOException e) {
                throw new TomEEException(e.getMessage(), e);
            } finally {
                close(writer);
            }
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
            final Collection<File> copied = new ArrayList<>();
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

    /**
     * Run.
     */
    protected void run() {
        if (classpaths == null) { // NPE protection when execute is skipped and mojo delegates to run directly
            classpaths = new ArrayList<>();
        }

        // init ports if needed
        tomeeHttpPort = getOrInitPort(tomeeHttpPort);
        tomeeHttpsPort = getOrInitPort(tomeeHttpsPort);
        tomeeAjpPort = getOrInitPort(tomeeAjpPort);
        tomeeShutdownPort = getOrInitPort(tomeeShutdownPort);

        final List<String> strings = generateJVMArgs();

        // init env for RemoteServer
        System.setProperty("openejb.home", catalinaBase.getAbsolutePath());
        if (debug) {
            System.setProperty("openejb.server.debug", "true");
            System.setProperty("server.debug.port", Integer.toString(debugPort));
        }
        System.setProperty("server.shutdown.port", String.valueOf(tomeeShutdownPort));
        System.setProperty("server.shutdown.command", tomeeShutdownCommand);

        server = new RemoteServer(getConnectAttempts(), debug);
        server.setAdditionalClasspath(getAdditionalClasspath());

        addShutdownHooks(server); // some shutdown hooks are always added (see UpdatableTomEEMojo)

        if (TOM_EE.equals(container)) {
            try {
                server.setPortStartup(Integer.parseInt(tomeeHttpPort == null ? tomeeHttpsPort : tomeeHttpPort));
            } catch (final NumberFormatException nfe) {
                // no-op
            }

            getLog().info("Running '" + getClass().getName().replace("TomEEMojo", "").toLowerCase(Locale.ENGLISH)
                    + "'. Configured TomEE in plugin is " + tomeeHost + ":" + server.getPortStartup()
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
                while ((line = getNextLine(reader)) != null) {

                    if (isQuit(line)) {
                        break;
                    }

                    if ("ignore".equals(line)) {
                        continue;
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

    private static String getOrInitPort(final String raw) {
        try {
            if (Integer.parseInt(raw) <= 0) {
                return Integer.toString(NetworkUtil.getNextAvailablePort());
            }
        } catch (final NumberFormatException nfe) {
            // no-op, surely a placeholder
        }
        return raw;
    }

    private String getNextLine(final Scanner reader) {
        try {
            return reader.nextLine();
        } catch (final NoSuchElementException e) {
            return "ignore";
        }
    }

    /**
     * Generate jvm args list.
     *
     * @return the list
     */
    protected List<String> generateJVMArgs() {
        final String deployOpenEjbAppKey = "openejb.system.apps";
        final String servletCompliance = "org.apache.catalina.STRICT_SERVLET_COMPLIANCE";

        boolean deactivateStrictServletCompliance = args == null || !args.contains(servletCompliance);

        if (webappDefaultConfig) {
            forceDefaultForNiceWebAppDevelopment();
        }

        final List<String> strings = new ArrayList<>();
        if (systemVariables != null) {
            for (final Map.Entry<String, String> entry : systemVariables.entrySet()) {
                final String key = entry.getKey();
                if (servletCompliance.equals(key)) {
                    deactivateStrictServletCompliance = false;
                }

                final String value = entry.getValue();
                if (value == null) {
                    strings.add("-D" + key);
                } else {
                    strings.add(String.format("-D%s=%s", key, value));
                }

                if (deployOpenEjbAppKey.equals(key)) {
                    deployOpenEjbApplication = true;
                }
            }
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
            strings.addAll(Args.parse(args));
        }
        if (javaagents != null) {
            addJavaagents(strings);
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

        if (forceJspDevelopment) {
            getLog().info("TomEE will run in development mode");
            strings.add("-Dtomee.jsp-development=true");
        }

        return strings;
    }

    private void addJavaagents(final List<String> strings) {
        final String existingJavaagent = "\\\"-javaagent:$CATALINA_HOME/lib/openejb-javaagent.jar\\\"";
        final StringBuilder javaagentString = new StringBuilder(existingJavaagent);

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

            String path = javaagent;
            if (!new File(javaagent).isFile()) {
                try {
                    final FileWithMavenMeta jar = mvnToFile(javaagent, "jar");
                    if (persistJavaagents) {
                        final File javaagentFolder = new File(catalinaBase, "javaagent");
                        Files.mkdirs(javaagentFolder);
                        String name = jar.resolved.getName();
                        if (stripVersion) {
                            name = jar.stripVersion(true);
                        }
                        path = "$CATALINA_HOME/javaagent/" + name;
                        IO.copy(jar.resolved, new File(javaagentFolder, name));
                    }
                    strings.add("-javaagent:" + jar.resolved.getAbsolutePath() + args);
                } catch (final Exception e) {
                    getLog().warn("Can't find " + javaagent);
                    strings.add("-javaagent:" + javaagent + args);
                }
            } else {
                strings.add("-javaagent:" + javaagent + args);
            }

            if (persistJavaagents) {
                javaagentString.append(" -javaagent:").append(path).append(args);
            }
        }

        if (persistJavaagents) {
            try {
                {
                    final File catalinaSh = new File(catalinaBase, "bin/catalina.sh");
                    final String content = IO.slurp(catalinaSh).replace(existingJavaagent, javaagentString.toString());
                    doWrite(catalinaSh, content);
                }
                {
                    final File catalinaBat = new File(catalinaBase, "bin/catalina.bat");
                    final String content = IO.slurp(catalinaBat)
                            .replace(
                                    "\"-javaagent:%CATALINA_HOME%\\lib\\openejb-javaagent.jar\"",
                                    javaagentString.toString()
                                            .replace('\'', '"')
                                            .replace('/', '\\')
                                            .replace("$CATALINA_HOME", "%CATALINA_HOME%"));

                    doWrite(catalinaBat, content);
                }
            } catch (final IOException ioe) {
                throw new OpenEJBRuntimeException(ioe);
            }
        }
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
        if (docBases == null) {
            docBases = new ArrayList<>();
        }
        if (docBases.isEmpty() && webappResources.exists()) {
            getLog().info("adding " + webappResources.toString() + " docBase");
            docBases.add(webappResources);
        }
        if (externalRepositories == null) {
            externalRepositories = new ArrayList<>();
        }
        if (externalRepositories.isEmpty() && webappClasses.exists()) {
            getLog().info("adding " + webappClasses.toString() + " externalRepository");
            externalRepositories.add(webappClasses);
        }
        if (systemVariables == null) {
            systemVariables = new HashMap<>();
        }
        if (!systemVariables.containsKey("openejb.classloader.resources.deeper-first")) {
            systemVariables.put("openejb.classloader.force-maven", "true");
        }
    }

    private static String filesToString(final Collection<File> files) {
        final Collection<String> paths = new ArrayList<>(files.size());
        for (final File path : files) { // don't use relative paths (toString())
            paths.add(path.getAbsolutePath());
        }
        return Join.join(",", paths);
    }

    /**
     * Available commands collection.
     *
     * @return the collection
     */
    protected Collection<String> availableCommands() {
        return Arrays.asList(QUIT_CMD, EXIT_CMD);
    }

    /**
     * Stop server.
     *
     * @param stopCondition the stop condition
     */
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

    /**
     * Handle line boolean.
     *
     * @param line the line
     * @return the boolean
     */
    protected boolean handleLine(final String line) {
        return false;
    }

    /**
     * Server cmd.
     *
     * @param server  the server
     * @param strings the strings
     */
    protected void serverCmd(final RemoteServer server, final List<String> strings) {
        try {
            server.start(strings, getCmd(), checkStarted);
        } catch (final Exception e) {
            //TODO - Optional server.destroy()
            getLog().warn("Failed to check or track server startup on port: " + this.tomeeHttpPort);
        }
    }

    /**
     * Add shutdown hooks.
     *
     * @param server the server
     */
    protected void addShutdownHooks(final RemoteServer server) {
        // no-op
    }

    /**
     * Gets connect attempts.
     *
     * @return the connect attempts
     */
    protected int getConnectAttempts() {
        return (tomeeShutdownAttempts == 0 ? 60 : tomeeShutdownAttempts);
    }

    /**
     * Gets wait TomEE.
     *
     * @return the wait TomEE
     */
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
            remoteRepos = new ArrayList<>();
        }

        if ((tomeeClassifier != null && (tomeeClassifier.isEmpty() || tomeeClassifier.equals("ignore")))
                || ("org.apache.tomee".equals(tomeeGroupId) && "openejb-standalone".equals(tomeeArtifactId))) {
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
                if (skipRootFolderOnUnzip) {
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
                    if ((!parent.exists() && !parent.mkdirs())
                            || (!parent.canWrite() && !parent.setWritable(true))
                            || (!parent.canRead() && !parent.setReadable(true))) {
                        throw new RuntimeException("Failed to create or set permissions on: " + parent);
                    }
                } else if (!overrideOnUnzip) {
                    continue;
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
            final String rootTag = container.toLowerCase(Locale.ENGLISH);
            if (file.isFile()) { // can be not existing since we dont always deploy tomee but shouldn't since then apps/ is not guaranteed to work
                try {
                    final Openejb jaxb = JaxbOpenejb.readConfig(file.getAbsolutePath());
                    boolean needAdd = true;
                    for (final Deployments d : jaxb.getDeployments()) {
                        if ("apps".equals(d.getDir())) {
                            needAdd = false;
                            break;
                        }
                    }
                    if (needAdd) {
                        final String content = IO.slurp(file);
                        final FileWriter writer = new FileWriter(file);
                        final String end = "</" + rootTag + ">";
                        writer.write(content.replace(end, "  <Deployments dir=\"apps\" />\n" + end));
                        writer.close();
                    }
                } catch (final OpenEJBException e) {
                    throw new IllegalStateException("illegal tomee.xml:\n" + IO.slurp(file), e);
                }
            } else {
                final FileWriter writer = new FileWriter(file);
                writer.write("<?xml version=\"1.0\"?>\n" +
                        "<" + rootTag + ">\n" +
                        "  <Deployments dir=\"apps\" />\n" +
                        "</" + rootTag + ">\n");
                writer.close();
            }

            final File appsFolder = new File(catalinaBase, "apps");
            if (!appsFolder.exists() && !appsFolder.mkdirs()) {
                throw new RuntimeException("Failed to create: " + appsFolder);
            }
        }
    }

    private static void doWrite(final File file, final String content) throws IOException {
        final FileWriter writer = new FileWriter(file);
        try {
            writer.write(content);
        } finally {
            IO.close(writer);
        }
    }

    /**
     * Gets cmd.
     *
     * @return the cmd
     */
    public abstract String getCmd();

    /**
     * The interface Resolver.
     */
    public interface Resolver {
        /**
         * Resolve file.
         *
         * @param group      the group
         * @param artifact   the artifact
         * @param version    the version
         * @param classifier the classifier
         * @param type       the type
         * @return the file
         */
        File resolve(String group, String artifact, String version, String classifier, String type);

        /**
         * Resolve file.
         *
         * @param group    the group
         * @param artifact the artifact
         * @param version  the version
         * @param type     the type
         * @return the file
         */
        File resolve(String group, String artifact, String version, String type);

        /**
         * Resolve file.
         *
         * @param group    the group
         * @param artifact the artifact
         * @param version  the version
         * @return the file
         */
        File resolve(String group, String artifact, String version);
    }

    private static class FileWithMavenMeta {
        private final String group;
        private final String artifact;
        private final String version;
        private final String classifier;
        private final String type;
        private final File resolved;

        private FileWithMavenMeta(final String group, final String artifact, final String version,
                                 final String classifier, final String type, final File resolved) {
            this.group = group;
            this.artifact = artifact;
            this.version = version;
            this.classifier = classifier;
            this.type = type;
            this.resolved = resolved;
        }

        /**
         * Strip version string.
         *
         * @param keepExtension the keep extension
         * @return the string
         */
        String stripVersion(final boolean keepExtension) {
            return artifact + (classifier != null && !classifier.isEmpty() ? "-" + classifier : "") +  (keepExtension ? "." + type : "");
        }
    }
}
