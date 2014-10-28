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
package org.apache.openejb.maven.jarstxt;

import edu.emory.mathcs.backport.java.util.Collections;
import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.ProvisioningUtil;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Mojo(name = "generate", threadSafe = true,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.COMPILE)
public class JarsTxtMojo extends AbstractMojo {
    public static final String JAR = "jar";
    @Component
    protected MavenProject project;

    @Parameter(property = "outputFile", defaultValue = "${project.build.directory}/${project.build.finalName}/WEB-INF/jars.txt" )
    protected File outputFile;

    @Parameter(property = "hash")
    protected String hashAlgo;

    @Parameter(property = "useTimeStamp", defaultValue = "false")
    protected boolean useTimeStamp;

    @Component
    protected ArtifactFactory factory;

    @Component
    protected ArtifactResolver resolver;

    @Parameter(defaultValue = "${localRepository}", readonly = true)
    protected ArtifactRepository local;

    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true)
    protected List<ArtifactRepository> remoteRepos;

    @Parameter
    protected List<String> additionals;

    @Parameter
    protected Map<String, String> placeHolders;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!outputFile.getParentFile().exists()) {
            FileUtils.mkdir(outputFile.getParentFile().getAbsolutePath());
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(outputFile);

            final TreeSet<String> set = new TreeSet<>();

            for (final Artifact a : (Set<Artifact>) project.getArtifacts()) {
                if (!acceptScope(a.getScope()) || !acceptType(a.getType())) {
                    continue;
                }

                a.setScope(Artifact.SCOPE_PROVIDED);

                final StringBuilder line = new StringBuilder("mvn:")
                        .append(a.getGroupId()).append("/")
                        .append(a.getArtifactId()).append("/")
                        .append(version(a));

                final boolean isJar = JAR.equals(a.getType());
                if (!isJar) {
                    line.append("/").append(a.getType());
                }

                if (a.getClassifier() != null) {
                    if (isJar) {
                        line.append("/").append(JAR);
                    }
                    line.append("/").append(a.getClassifier());
                }

                if (hashAlgo != null) {
                    final Artifact artifact = factory.createDependencyArtifact(a.getGroupId(), a.getArtifactId(), VersionRange.createFromVersion(a.getVersion()), a.getType(), a.getClassifier(), a.getScope());
                    try {
                        resolver.resolve(artifact, remoteRepos, local);
                    } catch (final ArtifactResolutionException | ArtifactNotFoundException e) {
                        throw new MojoExecutionException(e.getMessage(), e);
                    }
                    final File file = artifact.getFile();
                    line.append("|").append(Files.hash((Set<URL>) Collections.singleton(file.toURI().toURL()), hashAlgo))
                        .append("|").append(hashAlgo);
                }

                set.add(line.toString());
            }

            if (additionals != null) {
                if (placeHolders == null) {
                    placeHolders = new HashMap<>();
                }

                final StrSubstitutor lookup = new StrSubstitutor(StrLookup.mapLookup(placeHolders));

                for (final String line : additionals) {
                    final StringBuilder builder = new StringBuilder(line);
                    if (hashAlgo != null) {
                        builder.append("|").append(Files.hash(urls(line, lookup), hashAlgo))
                                .append("|").append(hashAlgo);
                    }
                    set.add(builder.toString());
                }
            }

            // written after to be sorted, more readable
            for (final String line : set) {
                writer.write(line);
                writer.write("\n");
            }

            writer.flush();
        } catch (final IOException e) {
            getLog().error(e.getMessage(), e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (final IOException e) {
                    // no-op
                }
            }
        }
    }

    private Set<URL> urls(final String line, final StrSubstitutor lookup) {
        final Set<URL> urls = new HashSet<>();
        for (final String location : ProvisioningUtil.realLocation(lookup.replace(line))) { // should have 1 item
            try {
                urls.add(new File(location).toURI().toURL());
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return urls;
    }

    private String version(final Artifact a) {
        if (!useTimeStamp && a.getBaseVersion().endsWith("SNAPSHOT")) {
            return a.getBaseVersion();
        }
        return a.getVersion();
    }

    private boolean acceptType(final String type) {
        return "jar".equals(type) || "zip".equals(type);
    }

    private boolean acceptScope(final String scope) {
        return Artifact.SCOPE_COMPILE.equals(scope) || Artifact.SCOPE_RUNTIME.equals(scope);
    }
}
