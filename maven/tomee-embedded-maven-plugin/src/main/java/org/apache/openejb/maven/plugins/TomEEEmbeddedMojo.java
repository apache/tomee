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
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.core.ProvidedClassLoaderFinder;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.maven.util.MavenLogStreamFactory;
import org.apache.openejb.maven.util.XmlFormatter;
import org.apache.openejb.util.JuliLogStreamFactory;
import org.apache.tomee.catalina.TomEERuntimeException;
import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;
import org.apache.tomee.livereload.LiveReloadInstaller;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.FileUtils;

import javax.naming.NamingException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.SimpleFormatter;

/**
 * Run an Embedded TomEE.
 */
@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.RUNTIME_PLUS_SYSTEM)
public class TomEEEmbeddedMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.packaging}")
    protected String packaging;

    /**
     * When not in classpath mode which war to deploy.
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}")
    protected File warFile;

    /**
     * HTTP port.
     */
    @Parameter(property = "tomee-embedded-plugin.http", defaultValue = "8080")
    protected int httpPort;

    /**
     * HTTPS port if relevant.
     */
    @Parameter(property = "tomee-embedded-plugin.httpsPort", defaultValue = "8443")
    protected int httpsPort;

    /**
     * Shutdown port.
     */
    @Parameter(property = "tomee-embedded-plugin.stop", defaultValue = "8005")
    protected int stopPort;

    /**
     * Server host.
     */
    @Parameter(property = "tomee-embedded-plugin.host", defaultValue = "localhost")
    protected String host;

    /**
     * Temporary working directory.
     */
    @Parameter(property = "tomee-embedded-plugin.dir", defaultValue = "${project.build.directory}/apache-tomee-embedded")
    protected String dir;

    /**
     * For https connector the keystore location.
     */
    @Parameter(property = "tomee-embedded-plugin.keystoreFile")
    protected String keystoreFile;

    /**
     * For https connector the keystore password.
     */
    @Parameter(property = "tomee-embedded-plugin.keystorePass")
    protected String keystorePass;

    /**
     * For https connector the keystore type.
     */
    @Parameter(property = "tomee-embedded-plugin.keystoreType", defaultValue = "JKS")
    protected String keystoreType;

    /**
     * For https connector if client auth is activated.
     */
    @Parameter(property = "tomee-embedded-plugin.clientAuth")
    protected String clientAuth;

    /**
     * For https connector the keystore alias to use.
     */
    @Parameter(property = "tomee-embedded-plugin.keyAlias")
    protected String keyAlias;

    /**
     * For https connector the SSL protocol.
     */
    @Parameter(property = "tomee-embedded-plugin.sslProtocol")
    protected String sslProtocol;

    /**
     * Where is the server.xml to use if provided.
     */
    @Parameter
    protected File serverXml;

    /**
     * Is https activated.
     */
    @Parameter(property = "tomee-embedded-plugin.ssl", defaultValue = "false")
    protected boolean ssl;

    /**
     * Is EJBd activated.
     */
    @Parameter(property = "tomee-embedded-plugin.withEjbRemote", defaultValue = "false")
    protected boolean withEjbRemote;

    /**
     * Should we use a fast but unsecured session id generation implementation.
     */
    @Parameter(property = "tomee-embedded-plugin.quickSession", defaultValue = "true")
    protected boolean quickSession;

    /**
     * Should we skip http connector (and rely only on other connectors if setup).
     */
    @Parameter(property = "tomee-embedded-plugin.skipHttp", defaultValue = "false")
    protected boolean skipHttp;

    /**
     * Deploy the classpath as a webapp (instead of deploying a war).
     */
    @Parameter(property = "tomee-embedded-plugin.classpathAsWar", defaultValue = "false")
    protected boolean classpathAsWar;

    /**
     * Use pom dependencies when classpathAsWar=true.
     */
    @Parameter(property = "tomee-embedded-plugin.useProjectClasspath", defaultValue = "true")
    protected boolean useProjectClasspath;

    /**
     * Used to deactivate tomcat web resources caching (useful to get F5 working).
     */
    @Parameter(property = "tomee-embedded-plugin.webResourceCached", defaultValue = "true")
    protected boolean webResourceCached;

    /**
     * Avoid to create multiple classloaders and use root one for the application.
     */
    @Parameter(property = "tomee-embedded-plugin.singleClassLoader", defaultValue = "false" /* for compat */)
    protected boolean singleClassLoader;

    /**
     * Support for reload command (ie redeploy the webapp by undeploying/deploying).
     */
    @Parameter(property = "tomee-embedded-plugin.forceReloadable", defaultValue = "true")
    protected boolean forceReloadable;

    /**
     * Additional modules.
     */
    @Parameter(property = "tomee-embedded-plugin.modules", defaultValue = "${project.build.outputDirectory}")
    protected List<File> modules;

    /**
     * Additional web resources (directories).
     */
    @Parameter(property = "tomee-embedded-plugin.web-resources")
    protected List<File> webResources;

    /**
     * Where is docBase/web resources.
     */
    @Parameter(property = "tomee-embedded-plugin.docBase", defaultValue = "${project.basedir}/src/main/webapp")
    protected File docBase;

    /**
     * Context name.
     */
    @Parameter(property = "tomee-embedded-plugin.context")
    protected String context;

    /**
     * Conf classpath folder.
     */
    @Parameter(property = "tomee-embedded-plugin.conf")
    protected String conf;

    /**
     * TomEE properties.
     */
    @Parameter // don't call it properties to avoid to break getConfig()
    protected Map<String, String> containerProperties;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Should TomEE use maven logging system instead of default one.
     */
    @Parameter(property = "tomee-embedded-plugin.mavenLog", defaultValue = "true")
    private boolean mavenLog;

    /**
     * Don't try to update port/host in server.xml.
     */
    @Parameter(property = "tomee-embedded-plugin.keepServerXmlAsThis", defaultValue = "false")
    private boolean keepServerXmlAsThis;

    /**
     * User/Password map.
     */
    @Parameter
    private Map<String, String> users;

    /**
     * Role/users map.
     */
    @Parameter
    private Map<String, String> roles;

    /**
     * force webapp to be support JSP reloading.
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

    /**
     * Additional applications to deploy.
     */
    @Parameter
    private List<String> applications;

    /**
     * Scopes to take into account when deploying the project classpath.
     */
    @Parameter
    private List<String> applicationScopes;

    @Parameter(property = "tomee-plugin.skip-current-project", defaultValue = "false")
    private boolean skipCurrentProject;

    @Parameter(property = "tomee-plugin.application-copy", defaultValue = "${project.build.directory}/tomee-embedded/applications")
    private File applicationCopyFolder;

    @Parameter(property = "tomee-plugin.work", defaultValue = "${project.build.directory}/tomee-embedded-work")
    private File workDir;

    /**
     * serverl.xml content directly in the pom.xml.
     */
    @Parameter
    protected PlexusConfiguration inlinedServerXml;

    /**
     * tomee.xml directly in the pom.xml.
     */
    @Parameter
    protected PlexusConfiguration inlinedTomEEXml;

    /**
     * Advanced configuration for live reload (to change port, context...).
     */
    @Parameter //advanced config but a simple boolean will be used for defaults (withLiveReload)
    private LiveReload liveReload;

    /**
     * Use livereload.
     */
    @Parameter(property = "tomee-plugin.liveReload", defaultValue = "false")
    private boolean withLiveReload;

    /**
     * A list of js scripts executed before the container starts.
     */
    @Parameter
    protected List<String> jsCustomizers;

    /**
     * A list of groovy scripts executed before the container starts. Needs to add groovy as dependency.
     */
    @Parameter
    protected List<String> groovyCustomizers;

    private Map<String, Command> commands;
    private String deployedName;

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

        if (inlinedServerXml != null && inlinedServerXml.getChildCount() > 0) {
            if (serverXml != null && serverXml.exists()) {
                throw new MojoFailureException("you can't define a server.xml and an inlinedServerXml");
            }
            try {
                FileUtils.forceMkdir(workDir);
                serverXml = new File(workDir, "server.xml_dump");
                FileUtils.fileWrite(serverXml, XmlFormatter.format(inlinedServerXml.getChild(0).toString()));
            } catch (final Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

        final Container container = new Container() {
            @Override
            public void setup(final Configuration configuration) {
                super.setup(configuration);
                if (inlinedTomEEXml != null && inlinedTomEEXml.getChildCount() > 0) {
                    try {
                        final File conf = new File(dir, "conf");
                        FileUtils.forceMkdir(conf);
                        FileUtils.fileWrite(new File(conf, "tomee.xml"), XmlFormatter.format(inlinedTomEEXml.getChild(0).toString()));
                    } catch (final Exception e) {
                        throw new TomEERuntimeException(e);
                    }
                }

                final String base = getBase().getAbsolutePath();
                scriptCustomization(jsCustomizers, "js", base);
                scriptCustomization(groovyCustomizers, "groovy", base);
            }
        };
        final Configuration config = getConfig();
        container.setup(config);

        final Thread hook = new Thread() {
            @Override
            public void run() {
                if (container.getTomcat() != null && container.getTomcat().getServer().getState() != LifecycleState.DESTROYED) {
                    final Thread thread = Thread.currentThread();
                    final ClassLoader old = thread.getContextClassLoader();
                    thread.setContextClassLoader(ParentClassLoaderFinder.Helper.get());
                    try {
                        if (!classpathAsWar) {
                            container.undeploy(warFile.getAbsolutePath());
                        }
                        container.stop();
                    } catch (final NoClassDefFoundError noClassDefFoundError) {
                        // debug cause it is too late to shutdown properly so don't pollute logs
                        getLog().debug("can't stop TomEE", noClassDefFoundError);
                    } catch (final Exception e) {
                        getLog().error("can't stop TomEE", e);
                    } finally {
                        thread.setContextClassLoader(old);
                    }
                }
            }
        };
        hook.setName("TomEE-Embedded-ShutdownHook");

        try {
            container.start();
            SystemInstance.get().setComponent(ParentClassLoaderFinder.class, new ProvidedClassLoaderFinder(loader));

            Runtime.getRuntime().addShutdownHook(hook);

            deployedName = doDeploy(thread, loader, container, useProjectClasspath);

            if (applications != null && !applications.isEmpty()) {
                Files.mkdirs(applicationCopyFolder);

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

        installLiveReloadEndpointIfNeeded();

        try {
            String line;
            final Scanner scanner = newScanner();
            while ((line = scanner.nextLine()) != null) {
                switch (line.trim()) {
                    case "exit":
                    case "quit":
                        Runtime.getRuntime().removeShutdownHook(hook);
                        container.close();
                        return;
                    case "reload":
                        reload(thread, loader, container);
                        break;
                    default:
                        onMissingCommand(line);
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

    private void scriptCustomization(final List<String> customizers, final String ext, final String base) {
        if (customizers == null || customizers.isEmpty()) {
            return;
        }
        final ScriptEngine engine = new ScriptEngineManager().getEngineByExtension(ext);
        if (engine == null) {
            throw new IllegalStateException("No engine for " + ext + ". Maybe add the JSR223 implementation as plugin dependency.");
        }
        for (final String js : customizers) {
            try {
                final SimpleBindings bindings = new SimpleBindings();
                bindings.put("catalinaBase", base);
                engine.eval(new StringReader(js), bindings);
            } catch (final ScriptException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    protected Scanner newScanner() {
        return new Scanner(System.in);
    }

    private String doDeploy(final Thread thread, final ClassLoader loader, final Container container, final boolean useProjectClasspath) throws OpenEJBException, IOException, NamingException {
        if (!skipCurrentProject) {
            if (!classpathAsWar) {
                final String name = '/' + (context == null ? warFile.getName() : context);
                container.deploy(name, warFile, true);
                return name;
            } else {
                if (useProjectClasspath) {
                    thread.setContextClassLoader(createClassLoader(loader));
                }
                container.deployClasspathAsWebApp(context, docBase, singleClassLoader);
            }
        }
        return context;
    }

    protected void onMissingCommand(final String line) {
        if (line == null) {
            return;
        }
        if (commands == null) { // lazy loading
            commands = new HashMap<>();
            for (final Command c : ServiceLoader.load(Command.class)) {
                commands.put(c.name(), c);
            }
        }
        { // direct command
            final Command c = commands.get(line.trim());
            if (c != null) {
                c.invoke(line);
                return;
            }
        }
        // else match by "startsWith" all possible commands
        for (final Map.Entry<String, Command> c : commands.entrySet()) {
            if (line.startsWith(c.getKey())) {
                c.getValue().invoke(line);
            }
        }
    }

    protected synchronized void reload(final Thread thread, final ClassLoader loader, final Container container) throws OpenEJBException, NamingException, IOException {
        getLog().info("Redeploying " + (deployedName == null ? '/' : deployedName));
        try {
            final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
            if (classpathAsWar) { // this doesn't track module names so no need to go through container.undeploy()
                assembler.destroyApplication(assembler.getDeployedApplications().iterator().next().path);
            } else {
                container.undeploy(deployedName);
            }
        } catch (final UndeployException e) {
            throw new IllegalStateException(e);
        }
        doDeploy(thread, loader, container, false/*already done*/);
        getLog().info("Redeployed " + (deployedName == null ? '/' : deployedName));
    }

    private void installLiveReloadEndpointIfNeeded() {
        if (withLiveReload && liveReload == null) {
            liveReload = new LiveReload();
        }
        if (liveReload != null) {
            LiveReloadInstaller.install(
                liveReload.getPath(), liveReload.getPort(),
                liveReload.getWatchedFolder() == null ? docBase.getAbsolutePath() : liveReload.getWatchedFolder());
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
            final String scope = artifact.getScope();
            if ((applicationScopes == null && !(Artifact.SCOPE_COMPILE.equals(scope) || Artifact.SCOPE_RUNTIME.equals(scope)))
                || (applicationScopes != null && !applicationScopes.contains(scope))) {
                continue;
            }
            try {
                urls.add(artifact.getFile().toURI().toURL());
            } catch (final MalformedURLException e) {
                getLog().warn("can't use artifact " + artifact.toString());
            }
        }
        if (modules != null) {
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
        }
        return urls.isEmpty() ? parent : new URLClassLoader(urls.toArray(new URL[urls.size()]), parent) {
            @Override
            public boolean equals(final Object obj) {
                return super.equals(obj) || parent.equals(obj); // fake container loader since we deploy the classpath normally (see tomee webapp loader)
            }
        };
    }

    private Configuration getConfig() { // lazy way but it works fine
        final Configuration config = new Configuration();
        for (final Field field : TomEEEmbeddedMojo.class.getDeclaredFields()) {
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
            for(Map.Entry<String, String> e : containerProperties.entrySet()) {
                if(e.getValue() == null) {
                    getLog().warn("Value for container property '" + e.getKey() + "' is NULL. Skipping.'");
                } else {
                    props.put(e.getKey(), e.getValue());
                }
            }
            config.setProperties(props);
        }
        if (forceJspDevelopment) {
            if (config.getProperties() == null) {
                config.setProperties(new Properties());
            }
            config.getProperties().put("tomee.jsp-development", "true");
        }
        if (forceReloadable) {
            if (config.getProperties() == null) {
                config.setProperties(new Properties());
            }
            config.getProperties().setProperty("tomee.force-reloadable", "true");
        }
        if (webResources != null && !webResources.isEmpty()) {
            for (final File f : webResources) {
                config.addCustomWebResources(f.getAbsolutePath());
            }
        }
        return config;
    }

    /**
     * A potential command identified by a name.
     *
     * Note that reload and quit/exit are built in commands.
     *
     * It is recommanded to prefix the command by something specific to your set of commands.
     */
    public interface Command {
        /**
         * @return the string to invoke this comamnd.
         */
        String name();

        /**
         * Executes this command.
         *
         * @param line the raw line entered by the user.
         */
        void invoke(String line);
    }
}
