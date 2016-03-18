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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.applicationcomposer.mojo;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.URLs;
import org.apache.xbean.finder.ClassLoaders;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static java.util.Arrays.asList;

@Mojo(name = "zip", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ApplicationComposerZipMojo extends ApplicationComposerMojo {
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}-applicationcomposer")
    protected File workDir;

    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}-applicationcomposer.zip")
    protected File zip;

    @Component(role = Archiver.class, hint = "zip")
    private ZipArchiver archiver;

    @Component
    protected MavenProjectHelper projectHelper;

    @Parameter(defaultValue = "true")
    protected boolean attach;

    @Parameter
    protected String classifier;

    @Parameter
    private String[] excludedArtifacts;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (application == null) {
            getLog().error("You should specify <application>org.superbiz.MyApp</application>");
            return;
        }

        Files.mkdirs(workDir);

        final File lib = Files.mkdirs(new File(workDir, "lib/"));
        try { // container
            for (final URL u : ClassLoaders.findUrls(ApplicationComposerZipMojo.class.getClassLoader())) {
                final File file = URLs.toFile(u);
                final String name = file.getName();
                if (name.startsWith("doxia") || name.startsWith("maven") || name.startsWith("plexus")
                        || name.startsWith("jcl-over-slf4j")) {
                    continue;
                }
                try {
                    final File to = new File(lib, file.getName());
                    IO.copy(file, to);
                } catch (final IOException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
        } catch (final IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        // app deps
        final Collection<String> excludedAnyway = excludedArtifacts == null ? Collections.<String>emptyList() : asList(excludedArtifacts);
        for (final Artifact artifact : (Set<Artifact>) project.getArtifacts()) {
            if (excludedAnyway.contains(artifact.getGroupId() + ":" + artifact.getArtifactId())) {
                continue;
            }
            final File file = artifact.getFile();
            try {
                final File to = new File(lib, file.getName());
                IO.copy(file, to);
            } catch (final IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

        // apps bin
        final File classes = Files.mkdirs(new File(workDir, "classes/"));
        if (binaries.exists()) {
            try {
                IO.copy(binaries, classes);
            } catch (final IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

        // scripts
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final File bin = Files.mkdirs(new File(workDir, "bin/"));
        // TODO: .bat for windows
        try {
            final File to = new File(bin, "applicationcomposer");
            IO.copy(loader.getResourceAsStream("bin/applicationcomposer"), to);
            to.setExecutable(true);
        } catch (final IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        {
            try {
                final File environment = new File(bin, "environment");
                final FileWriter writer = new FileWriter(environment);
                writer.write("#! /bin/bash\n\nexport APPCOMPOSER_MAIN=" + application + "\n");
                writer.close();
                environment.setExecutable(true);
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

        getLog().info("Created distribution in " + zip);

        if (zip != null) {
            getLog().info("Zipping distribution " + zip);
            archiver.setDestFile(zip);
            archiver.setIgnorePermissions(false);
            archiver.addDirectory(workDir, zip.getName().replace(".zip", "") + '/');
            try {
                archiver.createArchive();
            } catch (final IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
            if (attach) {
                getLog().info("Attaching distribution " + zip);
                if (classifier != null) {
                    projectHelper.attachArtifact(project, "zip", classifier, zip);
                } else {
                    projectHelper.attachArtifact(project, "zip", zip);
                }
            }
        }
    }

    private void addFile(final JarOutputStream os, final File source, final String key) throws IOException {
        if (source.isDirectory()) {
            os.putNextEntry(new JarEntry(key));
            os.closeEntry();
        } else {
            os.putNextEntry(new JarEntry(key));
            final FileInputStream input = new FileInputStream(source);
            os.write(IOUtil.toByteArray(input));
            input.close();
            os.closeEntry();
        }
    }
}
