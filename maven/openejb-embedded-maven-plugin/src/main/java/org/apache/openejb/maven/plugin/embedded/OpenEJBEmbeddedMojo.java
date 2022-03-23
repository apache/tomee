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
package org.apache.openejb.maven.plugin.embedded;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.openejb.maven.util.MavenLogStreamFactory;

import jakarta.ejb.embeddable.EJBContainer;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Run an EJBContainer.
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.COMPILE)
public class OpenEJBEmbeddedMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.artifactId}")
    private String id;

    @Parameter(property = "embedded.provider", defaultValue = "org.apache.openejb.OpenEjbContainer")
    private String provider;

    @Parameter(property = "embedded.modules", defaultValue = "${project.build.outputDirectory}")
    private String modules;

    @Parameter(property = "embedded.await", defaultValue = "true")
    private boolean await;

    @Parameter
    private Map<String, String> properties;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        MavenLogStreamFactory.setLogger(getLog());
        final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(createClassLoader(oldCl));

        EJBContainer container = null;
        try {
            container = EJBContainer.createEJBContainer(map());
            if (await) {
                final CountDownLatch latch = new CountDownLatch(1);
                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        latch.countDown();
                    }
                }));
                try {
                    latch.await();
                } catch (final InterruptedException e) {
                    // ignored
                }
            }
        } finally {
            if (container != null) {
                container.close();
            }
            Thread.currentThread().setContextClassLoader(oldCl);
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
        for (final String str : modules.split(",")) {
            final File file = new File(str);
            if (file.exists()) {
                try {
                    urls.add(file.toURI().toURL());
                } catch (final MalformedURLException e) {
                    getLog().warn("can't use path " + str);
                }
            } else {
                getLog().warn("can't find " + str);
            }
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
    }

    private Map<?, ?> map() {
        final Map<String, Object> map = new HashMap<>();
        map.put(EJBContainer.APP_NAME, id);
        map.put(EJBContainer.PROVIDER, provider);
        map.put(EJBContainer.MODULES, modules.split(","));
        map.put("openejb.log.factory", "org.apache.openejb.maven.util.MavenLogStreamFactory");
        if (properties != null) {
            map.putAll(properties);
        }
        return map;
    }
}
