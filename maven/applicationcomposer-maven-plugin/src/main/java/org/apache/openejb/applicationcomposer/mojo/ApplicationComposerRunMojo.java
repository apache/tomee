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
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.testing.ApplicationComposers;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ApplicationComposerRunMojo extends ApplicationComposerMojo {
    @Parameter
    private String[] args;

    @Parameter
    private String[] acceptScopes;

    @Parameter
    private String[] excludedArtifacts;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (application == null) {
            getLog().error("You should specify <application>org.superbiz.MyApp</application>");
            return;
        }

        setupLogs();

        final Thread thread = Thread.currentThread();
        final ClassLoader old = thread.getContextClassLoader();
        final Collection<URL> depUrls = findDeps();
        if (!depUrls.isEmpty()) {
            try {
                final ClassLoader loader = new URLClassLoader(new URL[]{binaries.toURI().toURL()}, old);
                LazyClassLoaderFinder.loader = loader;
                thread.setContextClassLoader(loader);
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            LazyClassLoaderFinder.loader = old;
        }

        // run do a reset of SystemInstance so we can't set it directly
        System.setProperty(ParentClassLoaderFinder.class.getName(), LazyClassLoaderFinder.class.getName());

        try {
            ApplicationComposers.run(thread.getContextClassLoader().loadClass(application), args);
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        } finally {
            thread.setContextClassLoader(old);
        }
    }

    private Collection<URL> findDeps() {
        final List<URL> urls = new ArrayList<>();
        final Collection<String> passingScoped = acceptScopes == null ? asList("compile", "runtime") : asList(acceptScopes);
        final Collection<String> excludedAnyway = excludedArtifacts == null ? Collections.<String>emptyList() : asList(excludedArtifacts);
        for (final Artifact artifact : (Set<Artifact>) project.getArtifacts()) {
            if (!passingScoped.contains(artifact.getScope())) {
                continue;
            }
            if (excludedAnyway.contains(artifact.getGroupId() + ":" + artifact.getArtifactId())) {
                continue;
            }
            try {
                urls.add(artifact.getFile().toURI().toURL());
            } catch (final MalformedURLException e) {
                getLog().warn("can't use artifact " + artifact.toString());
            }
        }
        if (binaries.exists()) {
            try {
                urls.add(binaries.toURI().toURL());
            } catch (final MalformedURLException e) {
                getLog().warn("can't use artifact " + binaries.getAbsolutePath());
            }
        }
        return urls;
    }
}
