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
package org.apache.tomee.gradle.embedded;

import org.apache.tomee.gradle.embedded.classloader.FilterGradleClassLoader;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.UnknownConfigurationException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.GFileUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class TomEEEmbeddedTask extends DefaultTask {
    @Optional
    @Input
    private int httpPort = 8080;

    @Optional
    @Input
    private int httpsPort = 8443;

    @Optional
    @Input
    private int ajpPort = 8009;

    @Optional
    @Input
    private int stopPort = 8005;

    @Optional
    @Input
    private String host = "localhost";

    @Optional
    @Input
    private String keystoreFile;

    @Optional
    @Input
    private String keystorePass;

    @Optional
    @Input
    private String keystoreType = "JKS";

    @Optional
    @Input
    private String clientAuth;

    @Optional
    @Input
    private String keyAlias;

    @Optional
    @Input
    private String sslProtocol;

    @Optional
    @Input
    private File serverXml;

    @Optional
    @Input
    private boolean singleClassloader = false;

    @Optional
    @Input
    private boolean ssl = false;

    @Optional
    @Input
    private boolean withEjbRemote = false;

    @Optional
    @Input
    private boolean quickSession = true;

    @Optional
    @Input
    private boolean skipHttp = false;

    @Optional
    @Input
    private Collection<String> applicationScopes = new HashSet<>(asList("compile", "runtime"));

    @Optional
    @Input
    private Collection<String> classloaderFilteredPackages;

    @Optional
    @Input
    private Collection<String> customWebResources;

    @Optional
    @Input
    private boolean webResourceCached = true;

    @Optional
    @Input
    private String context = null;

    @Optional
    @Input
    private Map<String, String> containerProperties;

    @Optional
    @Input
    private boolean keepServerXmlAsThis = false;

    @Optional
    @Input
    private Map<String, String> users;

    @Optional
    @Input
    private Map<String, String> roles;

    @Optional
    @Input
    private boolean forceJspDevelopment = true;

    @Optional
    @Input
    private String inlinedServerXml;

    @Optional
    @Input
    private String inlinedTomEEXml;

    @Optional
    @Input
    private File workDir;

    @Optional
    @Input
    private List<File> modules;

    @Optional
    @Input
    private File docBase;

    @Optional
    @Input
    private String dir;

    @Optional
    @Input
    private String conf;

    /* TODO if needed
    @Parameter //a dvanced config but a simple boolean will be used for defaults (withLiveReload)
    private LiveReload liveReload;

    @Parameter(property = "tomee-plugin.liveReload", defaultValue = "false")
    private boolean withLiveReload;
     */

    private Configuration classpath;

    @TaskAction
    public void runTomEEEmbedded() {
        fixConfig();

        final Thread thread = Thread.currentThread();
        final ClassLoader tccl = thread.getContextClassLoader();
        thread.setContextClassLoader(createLoader(tccl));
        try {
            doRun();
        } finally {
            thread.setContextClassLoader(tccl);
        }
    }

    private void fixConfig() {
        final Project project = getProject();

        // defaults
        if (classpath == null) {
            try {
                classpath.add(project.getConfigurations().getByName(TomEEEmbeddedExtension.ALIAS).fileCollection());
            } catch (final UnknownConfigurationException uce) {
                classpath = project.getConfigurations().getByName(TomEEEmbeddedExtension.NAME);
            }
        }

        if (docBase == null) {
            docBase = new File(project.getProjectDir(), "src/main/webapp");
        }
        if (workDir == null) {
            workDir = new File(project.getBuildDir(), "tomee-embedded/work");
        }
        if (dir == null) {
            dir = new File(project.getBuildDir(), "tomee-embedded/run").getAbsolutePath();
        }
        if (modules == null || modules.isEmpty()) {
            final File main = new File(project.getBuildDir(), "classes/main");
            if (main.isDirectory()) {
                modules = new ArrayList<>(singletonList(main));
            }
        }

        // extension override
        for (final String name : asList(TomEEEmbeddedExtension.NAME, TomEEEmbeddedExtension.ALIAS)) {
            final TomEEEmbeddedExtension extension = TomEEEmbeddedExtension.class.cast(project.getExtensions().findByName(name));
            if (extension != null) {
                for (final Field f : TomEEEmbeddedTask.class.getDeclaredFields()) {
                    if (f.isAnnotationPresent(Input.class)) {
                        try {
                            final Field extField = TomEEEmbeddedExtension.class.getDeclaredField(f.getName());
                            if (!extField.isAccessible()) {
                                extField.setAccessible(true);
                            }
                            final Object val = extField.get(extension);
                            if (val != null) {
                                if (!f.isAccessible()) {
                                    f.setAccessible(true);
                                }
                                f.set(this, val);
                            }
                        } catch (final IllegalAccessException | NoSuchFieldException e) {
                            getLogger().warn("No field " + f.getName() + " in " + extension, e);
                        }
                    }
                }
            }
        }
    }

    private void doRun() {
        final Properties originalSystProp = new Properties();
        originalSystProp.putAll(System.getProperties());

        final Thread thread = Thread.currentThread();
        final ClassLoader loader = thread.getContextClassLoader();

        if (inlinedServerXml != null && !inlinedServerXml.trim().isEmpty()) {
            if (serverXml != null && serverXml.exists()) {
                throw new GradleException("you can't define a server.xml and an inlinedServerXml");
            }
            try {
                GFileUtils.mkdirs(workDir);
                serverXml = new File(workDir, "server.xml_dump");
                GFileUtils.writeFile(inlinedServerXml, serverXml);
            } catch (final Exception e) {
                throw new GradleException(e.getMessage(), e);
            }
        }

        final AtomicBoolean running = new AtomicBoolean();
        AutoCloseable container;
        Thread hook;
        try {
            final Class<?> containerClass = loader.loadClass("org.apache.tomee.embedded.Container");
            final Class<?> configClass = loader.loadClass("org.apache.tomee.embedded.Configuration");
            final Class<?> parentLoaderFinderClass = loader.loadClass("org.apache.openejb.core.ParentClassLoaderFinder");
            final Class<?> loaderFinderClass = loader.loadClass("org.apache.openejb.core.ProvidedClassLoaderFinder");
            final Class<?> systemInstanceClass = loader.loadClass("org.apache.openejb.loader.SystemInstance");

            container = AutoCloseable.class.cast(containerClass.newInstance());
            final Object config = getConfig(configClass);

            containerClass.getMethod("setup", configClass).invoke(container, config);

            if (inlinedTomEEXml != null && inlinedTomEEXml.trim().isEmpty()) {
                try {
                    final File conf = new File(dir, "conf");
                    GFileUtils.mkdirs(conf);
                    GFileUtils.writeFile(inlinedTomEEXml, new File(conf, "tomee.xml"));
                } catch (final Exception e) {
                    throw new GradleException(e.getMessage(), e);
                }
            }

            final AutoCloseable finalContainer = container;
            hook = new Thread() {
                @Override
                public void run() {
                    if (running.compareAndSet(true, false)) {
                        final Thread thread = Thread.currentThread();
                        final ClassLoader old = thread.getContextClassLoader();
                        thread.setContextClassLoader(loader);
                        try {
                            finalContainer.close();
                        } catch (final NoClassDefFoundError noClassDefFoundError) {
                            // debug cause it is too late to shutdown properly so don't pollute logs
                            getLogger().debug("can't stop TomEE", noClassDefFoundError);
                        } catch (final Exception e) {
                            getLogger().error("can't stop TomEE", e);
                        } finally {
                            thread.setContextClassLoader(old);
                        }
                    }
                }
            };
            hook.setName("TomEE-Embedded-ShutdownHook");

            running.set(true); // yes should be done after but we can't help much if we don't do it there for auto shutdown
            containerClass.getMethod("start").invoke(container);

            // SystemInstance.get().setComponent(ParentClassLoaderFinder.class, new ProvidedClassLoaderFinder(loader));
            final Object providedLoaderFinder = loaderFinderClass.getConstructor(ClassLoader.class).newInstance(loader);
            final Object systemInstance = systemInstanceClass.getMethod("get").invoke(null);
            systemInstanceClass.getMethod("setComponent", Class.class, Object.class)
                    .invoke(systemInstance, parentLoaderFinderClass, providedLoaderFinder);

            Runtime.getRuntime().addShutdownHook(hook);

            containerClass.getMethod("deployClasspathAsWebApp", String.class, File.class, boolean.class).invoke(container, context, docBase, singleClassloader);

            getLogger().info("TomEE embedded started on " + configClass.getMethod("getHost").invoke(config) + ":" + configClass.getMethod("getHttpPort").invoke(config));
        } catch (final Exception e) {
            throw new GradleException(e.getMessage(), e);
        }


        // installLiveReloadEndpointIfNeeded();

        try {
            String line;
            final Scanner scanner = new Scanner(System.in);
            while ((line = scanner.nextLine()) != null) {
                final String cmd = line.trim().toLowerCase(Locale.ENGLISH);
                switch (cmd) {
                    case "exit":
                    case "quit":
                        running.set(false);
                        Runtime.getRuntime().removeShutdownHook(hook);
                        container.close();
                        return;
                    default:
                        getLogger().warn("Unknown: '" + cmd + "', use 'exit' or 'quit'");
                }
            }
        } catch (final Exception e) {
            Thread.interrupted();
        } finally {
            thread.setContextClassLoader(loader);
            System.setProperties(originalSystProp);
        }
    }

    private Object getConfig(final Class<?> configClass) throws Exception {
        final Object config = configClass.newInstance();
        for (final Field field : TomEEEmbeddedTask.class.getDeclaredFields()) {
            try {
                final Field configField = configClass.getDeclaredField(field.getName());
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                final Object value = field.get(this);
                if (value != null) {
                    if (!configField.isAccessible()) {
                        configField.setAccessible(true);
                    }
                    configField.set(config, value);
                    getLogger().debug("using " + field.getName() + " = " + value);
                }
            } catch (final NoSuchFieldException nsfe) {
                // ignored
            } catch (final Exception e) {
                getLogger().warn("can't initialize attribute " + field.getName());
            }
        }
        if (containerProperties == null) {
            containerProperties = new HashMap<>();
        }
        if (forceJspDevelopment) {
            containerProperties.put("tomee.jsp-development", "true");
        }

        containerProperties.put("openejb.log.factory", "slf4j"); // like gradle

        { // ensure we don't scan gradle
            final String original = containerProperties.get("openejb.additional.exclude");
            final String additional =
                    "gradle,ant,jna,native-platform,reflectasm,bsh,jetty,rhino," +
                            "aws,core-3,bcpg,jsch,pmaven,sonar,bndlib,jatl,simple-,snakeyaml,jcl-over-slf4j,ivy," +
                            "jarjar,jul-to-slf4j,jaxen,minlog,jcip-annotations,kryo,objenesis";
            if (original == null) {
                containerProperties.put("openejb.additional.exclude", additional);
            } else {
                containerProperties.put("openejb.additional.exclude", original + ',' + additional);
            }
        }
        if (containerProperties != null) {
            final Properties props = new Properties();
            props.putAll(containerProperties);
            configClass.getMethod("setProperties", Properties.class)
                    .invoke(config, props);
        }
        return config;
    }

    private ClassLoader createLoader(final ClassLoader parent) {
        getLogger().info("Resolving tomee-embedded classpath...");

        final Collection<URL> urls = new LinkedHashSet<>(64);

        addFiles(modules, urls);

        for (final Configuration cc : getProject().getConfigurations()) {
            if (applicationScopes.contains(cc.getName())) {
                addFiles(cc.getFiles(), urls);
            }
        }

        addFiles(classpath.getFiles(), urls);

        // use JVM loader to avoid the noise of gradle and its plugins
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), new FilterGradleClassLoader(parent, classloaderFilteredPackages));
    }

    private void addFiles(final Collection<File> files, final Collection<URL> urls) {
        if (files == null || files.isEmpty()) {
            return;
        }
        for (final File f : files) {
            final String name = f.getName();
            if (name.startsWith("slf4j-api") || name.startsWith("slf4j-jdk14")) {
                continue; // use gradle
            }
            try {
                urls.add(f.toURI().toURL());
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(final int httpPort) {
        this.httpPort = httpPort;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(final int httpsPort) {
        this.httpsPort = httpsPort;
    }

    public int getAjpPort() {
        return ajpPort;
    }

    public void setAjpPort(final int ajpPort) {
        this.ajpPort = ajpPort;
    }

    public int getStopPort() {
        return stopPort;
    }

    public void setStopPort(final int stopPort) {
        this.stopPort = stopPort;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getKeystoreFile() {
        return keystoreFile;
    }

    public void setKeystoreFile(final String keystoreFile) {
        this.keystoreFile = keystoreFile;
    }

    public String getKeystorePass() {
        return keystorePass;
    }

    public void setKeystorePass(final String keystorePass) {
        this.keystorePass = keystorePass;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(final String keystoreType) {
        this.keystoreType = keystoreType;
    }

    public String getClientAuth() {
        return clientAuth;
    }

    public void setClientAuth(final String clientAuth) {
        this.clientAuth = clientAuth;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(final String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getSslProtocol() {
        return sslProtocol;
    }

    public void setSslProtocol(final String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    public File getServerXml() {
        return serverXml;
    }

    public void setServerXml(final File serverXml) {
        this.serverXml = serverXml;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(final boolean ssl) {
        this.ssl = ssl;
    }

    public boolean isWithEjbRemote() {
        return withEjbRemote;
    }

    public void setWithEjbRemote(final boolean withEjbRemote) {
        this.withEjbRemote = withEjbRemote;
    }

    public boolean isQuickSession() {
        return quickSession;
    }

    public void setQuickSession(final boolean quickSession) {
        this.quickSession = quickSession;
    }

    public boolean isSkipHttp() {
        return skipHttp;
    }

    public void setSkipHttp(final boolean skipHttp) {
        this.skipHttp = skipHttp;
    }

    public Collection<String> getApplicationScopes() {
        return applicationScopes;
    }

    public void setApplicationScopes(final Collection<String> applicationScopes) {
        this.applicationScopes = applicationScopes;
    }

    public boolean isWebResourceCached() {
        return webResourceCached;
    }

    public void setWebResourceCached(final boolean webResourceCached) {
        this.webResourceCached = webResourceCached;
    }

    public String getContext() {
        return context;
    }

    public void setContext(final String context) {
        this.context = context;
    }

    public Map<String, String> getContainerProperties() {
        return containerProperties;
    }

    public void setContainerProperties(final Map<String, String> containerProperties) {
        this.containerProperties = containerProperties;
    }

    public boolean isKeepServerXmlAsThis() {
        return keepServerXmlAsThis;
    }

    public void setKeepServerXmlAsThis(final boolean keepServerXmlAsThis) {
        this.keepServerXmlAsThis = keepServerXmlAsThis;
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public void setUsers(final Map<String, String> users) {
        this.users = users;
    }

    public Map<String, String> getRoles() {
        return roles;
    }

    public void setRoles(final Map<String, String> roles) {
        this.roles = roles;
    }

    public boolean isForceJspDevelopment() {
        return forceJspDevelopment;
    }

    public void setForceJspDevelopment(final boolean forceJspDevelopment) {
        this.forceJspDevelopment = forceJspDevelopment;
    }

    public String getInlinedServerXml() {
        return inlinedServerXml;
    }

    public void setInlinedServerXml(final String inlinedServerXml) {
        this.inlinedServerXml = inlinedServerXml;
    }

    public String getInlinedTomEEXml() {
        return inlinedTomEEXml;
    }

    public void setInlinedTomEEXml(final String inlinedTomEEXml) {
        this.inlinedTomEEXml = inlinedTomEEXml;
    }

    public File getWorkDir() {
        return workDir;
    }

    public void setWorkDir(final File workDir) {
        this.workDir = workDir;
    }

    public List<File> getModules() {
        return modules;
    }

    public void setModules(final List<File> modules) {
        this.modules = modules;
    }

    public File getDocBase() {
        return docBase;
    }

    public void setDocBase(final File docBase) {
        this.docBase = docBase;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(final String dir) {
        this.dir = dir;
    }

    public Configuration getClasspath() {
        return classpath;
    }

    public void setClasspath(final Configuration classpath) {
        this.classpath = classpath;
    }

    public void setSingleClassloader(final boolean singleClassloader) {
        this.singleClassloader = singleClassloader;
    }

    public Collection<String> getCustomWebResources() {
        return customWebResources;
    }

    public void setCustomWebResources(final Collection<String> customWebResources) {
        this.customWebResources = customWebResources;
    }
}
