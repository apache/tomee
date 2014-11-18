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
package org.apache.openejb.maven.plugins;

import org.apache.catalina.LifecycleState;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.core.ProvidedClassLoaderFinder;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.maven.util.MavenLogStreamFactory;
import org.apache.openejb.util.JuliLogStreamFactory;
import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.SimpleFormatter;

/**
 * Run an Embedded TomEE.
 */
@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.RUNTIME_PLUS_SYSTEM)
public class TomEEEmbeddedMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.packaging}")
    protected String packaging;

    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}")
    protected File warFile;

    @Parameter(property = "tomee-embedded-plugin.http", defaultValue = "8080")
    protected int httpPort;

    @Parameter(property = "tomee-embedded-plugin.httpsPort", defaultValue = "8443")
    protected int httpsPort;

    @Parameter(property = "tomee-embedded-plugin.ajp", defaultValue = "8009")
    protected int ajpPort = 8009;

    @Parameter(property = "tomee-embedded-plugin.stop", defaultValue = "8005")
    protected int stopPort;

    @Parameter(property = "tomee-embedded-plugin.host", defaultValue = "localhost")
    protected String host;

    @Parameter(property = "tomee-embedded-plugin.lib", defaultValue = "${project.build.directory}/apache-tomee-embedded")
    protected String dir;

    @Parameter(property = "tomee-embedded-plugin.keystoreFile")
    protected String keystoreFile;

    @Parameter(property = "tomee-embedded-plugin.keystorePass")
    protected String keystorePass;

    @Parameter(property = "tomee-embedded-plugin.keystoreType", defaultValue = "JKS")
    protected String keystoreType;

    @Parameter(property = "tomee-embedded-plugin.clientAuth")
    protected String clientAuth;

    @Parameter(property = "tomee-embedded-plugin.keyAlias")
    protected String keyAlias;

    @Parameter(property = "tomee-embedded-plugin.sslProtocol")
    protected String sslProtocol;

    @Parameter
    protected File serverXml;

    @Parameter(property = "tomee-embedded-plugin.ssl", defaultValue = "false")
    protected boolean ssl;

    @Parameter(property = "tomee-embedded-plugin.quickSession", defaultValue = "true")
    protected boolean quickSession;

    @Parameter(property = "tomee-embedded-plugin.skipHttp", defaultValue = "false")
    protected boolean skipHttp;

    @Parameter(property = "tomee-embedded-plugin.classpathAsWar", defaultValue = "false")
    protected boolean classpathAsWar;

    @Parameter(property = "tomee-embedded-plugin.useProjectClasspath", defaultValue = "true")
    protected boolean useProjectClasspath;

    @Parameter(property = "tomee-embedded-plugin.modules", defaultValue = "${project.build.outputDirectory}")
    protected List<File> modules;

    @Parameter(property = "tomee-embedded-plugin.docBase", defaultValue = "${project.basedir}/src/main/webapp")
    protected File docBase;

    @Parameter(property = "tomee-embedded-plugin.context")
    protected String context;

    @Parameter // don't call it properties to avoid to break getConfig()
    protected Map<String, String> containerProperties;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "tomee-embedded-plugin.mavenLog", defaultValue = "true")
    private boolean mavenLog;

    @Parameter(property = "tomee-embedded-plugin.keepServerXmlAsThis", defaultValue = "false")
    private boolean keepServerXmlAsThis;

    @Parameter
    private Map<String, String> users;

    @Parameter
    private Map<String, String> roles;

    /**
     * force webapp to be reloadable
     */
    @Parameter(property = "tomee-plugin.jsp-development", defaultValue = "true")
    private boolean forceJspDevelopment;

    @Component
    private ArtifactFactory factory;

    @Component
    private ArtifactResolver resolver;

    @Parameter(defaultValue = "${localRepository}", readonly = true)
    private ArtifactRepository local;

    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true)
    private List<ArtifactRepository> remoteRepos;

    @Parameter
    private List<String> applications;

    @Parameter(property = "tomee-plugin.skip-current-project", defaultValue = "false")
    private boolean skipCurrentProject;

    @Parameter(property = "tomee-plugin.application-copy", defaultValue = "${project.build.directory}/tomee-embedded/applications")
    private File applicationCopyFolder;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!classpathAsWar && "pom".equals(packaging)) {
            getLog().warn("this project is a pom, it is not deployable");
            return;
        }

        final Properties originalSystProp = new Properties();
        originalSystProp.putAll(System.getProperties());

        // we use MavenLogStreamFactory but if user set some JUL config in properties we want to respect them
        configureJULIfNeeded();

        final Thread thread = Thread.currentThread();
        final ClassLoader loader = thread.getContextClassLoader();

        final String logFactory = System.getProperty("openejb.log.factory");
        MavenLogStreamFactory.setLogger(getLog());
        if (mavenLog) {
            System.setProperty("openejb.log.factory", MavenLogStreamFactory.class.getName()); // this line also preload the class (<cinit>)
            System.setProperty("openejb.jul.forceReload", "true");
        }

        final Container container = new Container();
        final Configuration config = getConfig();
        container.setup(config);

        final Thread hook = new Thread() {
            @Override
            public void run() {
                if (container.getTomcat() != null && container.getTomcat().getServer().getState() != LifecycleState.DESTROYED) {
                    try {
                        if (!classpathAsWar) {
                            container.undeploy(warFile.getAbsolutePath());
                        }
                        container.stop();
                    } catch (final Exception e) {
                        getLog().error("can't stop TomEE", e);
                    }
                }
            }
        };

        try {
            container.start();
            SystemInstance.get().setComponent(ParentClassLoaderFinder.class, new ProvidedClassLoaderFinder(loader));

            Runtime.getRuntime().addShutdownHook(hook);

            if (!skipCurrentProject) {
                if (!classpathAsWar) {
                    container.deploy('/' + (context == null ? warFile.getName() : context), warFile, true);
                } else {
                    if (useProjectClasspath) {
                        thread.setContextClassLoader(createClassLoader(loader));
                    }
                    container.deployClasspathAsWebApp(context, docBase); // null is handled properly so no issue here
                }
            }

            if (applications != null) {
                for (final String app : applications) {
                    final String renameStr = "?name=";
                    final int nameIndex = app.lastIndexOf(renameStr);
                    final String coordinates = nameIndex > 0 ? app.substring(0, nameIndex) : app;
                    File file = mvnToFile(coordinates);
                    final String name = nameIndex > 0 ? app.substring(nameIndex + renameStr.length() + 1) : file.getName();
                    if (applicationCopyFolder != null) {
                        final File copy = new File(applicationCopyFolder, name);
                        IO.copy(file, copy);
                        file = copy;
                    }
                    container.deploy(name, file);
                }
            }

            getLog().info("TomEE embedded started on " + config.getHost() + ":" + config.getHttpPort());
        } catch (final Exception e) {
            getLog().error("can't start TomEE", e);
        }

        try {
            String line;
            final Scanner scanner = new Scanner(System.in);
            while ((line = scanner.nextLine()) != null) {
                switch (line.trim()) {
                    case "exit":
                    case "quit":
                        Runtime.getRuntime().removeShutdownHook(hook);
                        container.close();
                        return;
                }
            }
        } catch (final Exception e) {
            Thread.interrupted();
        } finally {
            if (logFactory == null) {
                System.clearProperty("openejb.log.factory");
            } else {
                System.setProperty("openejb.log.factory", logFactory);
            }
            thread.setContextClassLoader(loader);
            System.setProperties(originalSystProp);
        }
    }

    private File mvnToFile(final String lib) throws Exception {
        final String[] infos = lib.split(":");
        final String classifier;
        final String type;
        if (infos.length < 3) {
            throw new MojoExecutionException("format for librairies should be <groupId>:<artifactId>:<version>[:<type>[:<classifier>]]");
        }
        if (infos.length >= 4) {
            type = infos[3];
        } else {
            type = "war";
        }
        if (infos.length == 5) {
            classifier = infos[4];
        } else {
            classifier = null;
        }

        final Artifact artifact = factory.createDependencyArtifact(infos[0], infos[1], VersionRange.createFromVersion(infos[2]), type, classifier, "compile");
        resolver.resolve(artifact, remoteRepos, local);
        return artifact.getFile();
    }


    private void configureJULIfNeeded() {
        if (containerProperties != null && "true".equalsIgnoreCase(containerProperties.get("openejb.jul.forceReload"))) {
            System.getProperties().putAll(containerProperties);
            new JuliLogStreamFactory(); // easiest way to support forceReload, note this doesn't do that much ATM
            final String simpleFormat = containerProperties.get("java.util.logging.SimpleFormatter.format");
            if (simpleFormat != null) {
                try {
                    final Field field = SimpleFormatter.class.getDeclaredField("format");
                    field.setAccessible(true);
                    final int modifiers = field.getModifiers();
                    if (Modifier.isFinal(modifiers)) {
                        final Field modifiersField = Field.class.getDeclaredField("modifiers");
                        modifiersField.setAccessible(true);
                        modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
                    }
                    field.set(null, simpleFormat);
                } catch (final Throwable ignored) {
                    // no-op: don't block for it
                }
            }
        }
    }

    private ClassLoader createClassLoader(final ClassLoader parent) {
        final List<URL> urls = new ArrayList<>();
        for (final Artifact artifact : (Set<Artifact>) project.getArtifacts()) {
            try {
                urls.add(artifact.getFile().toURI().toURL());
            } catch (final MalformedURLException e) {
                getLog().warn("can't use artifact " + artifact.toString());
            }
        }
        for (final File file : modules) {
            if (file.exists()) {
                try {
                    urls.add(file.toURI().toURL());
                } catch (final MalformedURLException e) {
                    getLog().warn("can't use path " + file.getAbsolutePath());
                }
            } else {
                getLog().warn("can't find " + file.getAbsolutePath());
            }
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
    }

    private Configuration getConfig() { // lazy way but it works fine
        final Configuration config = new Configuration();
        for (final Field field : getClass().getDeclaredFields()) {
            try {
                final Field configField = Configuration.class.getDeclaredField(field.getName());
                field.setAccessible(true);
                configField.setAccessible(true);

                final Object value = field.get(this);
                if (value != null) {
                    configField.set(config, value);
                    getLog().debug("using " + field.getName() + " = " + value);
                }
            } catch (final NoSuchFieldException nsfe) {
                // ignored
            } catch (final Exception e) {
                getLog().warn("can't initialize attribute " + field.getName());
            }

        }
        if (containerProperties != null) {
            final Properties props = new Properties();
            props.putAll(containerProperties);
            config.setProperties(props);
        }
        if (forceJspDevelopment) {
            if (config.getProperties() == null) {
                config.setProperties(new Properties());
            }
            config.getProperties().put("tomee.jsp-development", "true");
        }
        return config;
    }
}
