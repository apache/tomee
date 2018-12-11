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

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.config.RemoteServer;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.JarLocation;
import org.apache.openejb.loader.LoaderRuntimeException;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.Zips;
import org.apache.openejb.maven.plugin.runner.ExecRunner;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.Pipe;
import org.apache.tomee.util.QuickServerXmlParser;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.apache.openejb.loader.Files.mkdirs;


/**
 * The type ExecMojo.
 * Creates an executable jar of the application.
 */
@Mojo(name = "exec", requiresDependencyResolution = ResolutionScope.RUNTIME_PLUS_SYSTEM)
public class ExecMojo extends BuildTomEEMojo {
    private static final String DEFAULT_SCRIPT = "bin/catalina[.sh|.bat]";

    /**
     * The Exec file.
     */
    @Parameter(property = "tomee-plugin.exec-file", defaultValue = "${project.build.directory}/${project.build.finalName}-exec.jar")
    protected File execFile;

    @Parameter(property = "tomee-plugin.runner-class", defaultValue = "org.apache.openejb.maven.plugin.runner.ExecRunner")
    private String runnerClass;

    @Parameter(property = "tomee-plugin.distribution-name", defaultValue = "tomee.zip")
    private String distributionName;

    @Parameter(property = "tomee-plugin.runtime-working-dir", defaultValue = ".distribution")
    private String runtimeWorkingDir;

    @Parameter(property = "tomee-plugin.script", defaultValue = DEFAULT_SCRIPT)
    private String script;

    @Parameter(property = "tomee-plugin.waitFor", defaultValue = "true")
    private boolean waitFor;

    @Parameter
    private List<String> additionalClasses;

    @Parameter
    private List<String> preTasks;

    @Parameter
    private List<String> postTasks;

    /**
     * Perform the Execution
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final boolean realAttach = attach;

        attach = false;
        zip = true;
        super.execute();

        try {
            createExecutableJar();
        } catch (final Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        if (realAttach) {
            getLog().info("Attaching Exec TomEE binary");
            if (classifier != null) {
                projectHelper.attachArtifact(project, "jar", classifier, execFile);
            } else {
                projectHelper.attachArtifact(project, "jar", execFile);
            }
        }
    }

    private void createExecutableJar() throws Exception {
        mkdirs(execFile.getParentFile());

        final Properties config = new Properties();
        config.put("distribution", distributionName);
        config.put("workingDir", runtimeWorkingDir);
        config.put("command", DEFAULT_SCRIPT.equals(script) ? (skipArchiveRootFolder ? "" : catalinaBase.getName() + "/") + DEFAULT_SCRIPT : script);
        final List<String> jvmArgs = generateJVMArgs();

        final String catalinaOpts = toString(jvmArgs, " ");
        config.put("catalinaOpts", catalinaOpts);
        config.put("timestamp", Long.toString(System.currentTimeMillis()));
        // java only
        final String cp = getAdditionalClasspath();
        if (cp != null) {
            config.put("additionalClasspath", cp);
        }
        config.put("shutdownCommand", tomeeShutdownCommand);
        int i = 0;
        boolean encodingSet = catalinaOpts.contains("-Dfile.encoding");
        for (final String jvmArg : jvmArgs) {
            config.put("jvmArg." + i++, jvmArg);
            encodingSet = encodingSet || jvmArg.contains("-Dfile.encoding");
        }
        if (!encodingSet) { // forcing encoding for launched process to be able to read conf files
            config.put("jvmArg." + i, "-Dfile.encoding=UTF-8");
        }

        if (preTasks != null) {
            config.put("preTasks", toString(preTasks, ","));
        }
        if (postTasks != null) {
            config.put("postTasks", toString(postTasks, ","));
        }
        config.put("waitFor", Boolean.toString(waitFor));

        // create an executable jar with main runner and zipFile
        final FileOutputStream fileOutputStream = new FileOutputStream(execFile);
        final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.JAR, fileOutputStream);

        { // distrib
            os.putArchiveEntry(new JarArchiveEntry(distributionName));
            final FileInputStream in = new FileInputStream(zipFile);
            try {
                IOUtils.copy(in, os);
                os.closeArchiveEntry();
            } finally {
                IOUtil.close(in);
            }
        }

        { // config
            os.putArchiveEntry(new JarArchiveEntry("configuration.properties"));
            final StringWriter writer = new StringWriter();
            config.store(writer, "");
            IOUtils.copy(new ByteArrayInputStream(writer.toString().getBytes("UTF-8")), os);
            os.closeArchiveEntry();
        }

        { // Manifest
            final Manifest manifest = new Manifest();

            final Manifest.Attribute mainClassAtt = new Manifest.Attribute();
            mainClassAtt.setName("Main-Class");
            mainClassAtt.setValue(runnerClass);
            manifest.addConfiguredAttribute(mainClassAtt);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
            manifest.write(baos);

            os.putArchiveEntry(new JarArchiveEntry("META-INF/MANIFEST.MF"));
            IOUtils.copy(new ByteArrayInputStream(baos.toByteArray()), os);
            os.closeArchiveEntry();
        }

        { // Main + utility
            for (final Class<?> clazz : asList(
                    ExecRunner.class,
                    Files.class, Files.PatternFileFilter.class, Files.DeleteThread.class,
                    Files.FileRuntimeException.class, Files.FileDoesNotExistException.class, Files.NoopOutputStream.class,
                    LoaderRuntimeException.class,
                    Pipe.class, IO.class, Zips.class, JarLocation.class,
                    RemoteServer.class, RemoteServer.CleanUpThread.class,
                    OpenEJBRuntimeException.class, Join.class, QuickServerXmlParser.class,
                    Options.class, Options.NullLog.class, Options.TomEEPropertyAdapter.class, Options.NullOptions.class,
                    Options.Log.class, JavaSecurityManagers.class)) {
                addToJar(os, clazz);
            }
        }
        addClasses(additionalClasses, os);
        addClasses(preTasks, os);
        addClasses(postTasks, os);

        IOUtil.close(os);
        IOUtil.close(fileOutputStream);
    }

    private void addClasses(final List<String> classes, final ArchiveOutputStream os) throws IOException {
        if (classes != null) { // user classes
            for (final String className : classes) {
                addToJar(os, load(className));
            }
        }
    }

    private void addToJar(final ArchiveOutputStream os, final Class<?> clazz) throws IOException {
        final String name = clazz.getName().replace('.', '/') + ".class";
        os.putArchiveEntry(new JarArchiveEntry(name));
        IOUtils.copy(getClass().getResourceAsStream('/' + name), os);
        os.closeArchiveEntry();
    }

    private Class<?> load(final String className) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String toString(final List<String> strings, final String sep) {
        final StringBuilder builder = new StringBuilder();
        for (final String s : strings) {
            builder.append(s).append(sep);
        }
        return builder.toString();
    }
}
