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

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.UnknownConfigurationException;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;

/**
 * Custom dependencies can be added using the scope "tomee-embedded" or "tomeeembedded".
 */
public class TomEEEmbeddedPlugin implements Plugin<Project> {
    @Override
    public void apply(final Project project) {
        final List<String> extensions = asList(TomEEEmbeddedExtension.NAME, TomEEEmbeddedExtension.ALIAS);
        for (final String name : extensions) {
            project.getExtensions().create(name, TomEEEmbeddedExtension.class);
        }

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(final Project actionProject) {
                for (final String name : extensions) {
                    final TomEEEmbeddedExtension extension = TomEEEmbeddedExtension.class.cast(actionProject.getExtensions().findByName(name));
                    if (extension == null) {
                        return;
                    }
                    if (extension.isSkipDefaultRepository() != null && !extension.isSkipDefaultRepository()) {
                        actionProject.getRepositories().mavenCentral();
                        return;
                    }
                }
                actionProject.getRepositories().mavenCentral();
            }
        });

        String configName = TomEEEmbeddedExtension.ALIAS;
        try {
            project.getConfigurations().getByName(configName);
        } catch (final UnknownConfigurationException uce) {
            configName = TomEEEmbeddedExtension.NAME;
        }

        final Configuration configuration = project.getConfigurations().maybeCreate(configName);
        configuration.getIncoming().beforeResolve(new Action<ResolvableDependencies>() {
            @Override
            public void execute(final ResolvableDependencies resolvableDependencies) {
                String tomeeVersion = null;
                for (final String name : extensions) {
                    final TomEEEmbeddedExtension extension = TomEEEmbeddedExtension.class.cast(project.getExtensions().findByName(name));
                    if (extension == null) {
                        return;
                    }
                    tomeeVersion = extension.getTomeeVersion();
                    if (tomeeVersion != null) {
                        break;
                    }
                }
                if (tomeeVersion == null) {
                    try {
                        try (final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/maven/org.apache.tomee.gradle/tomee-embedded/pom.properties")) {
                            final Properties p = new Properties();
                            p.load(is);
                            tomeeVersion = p.getProperty("version");
                        }
                    } catch (final IOException e) {
                        tomeeVersion = "7.0.2"; // we should never be there
                    }
                }

                final DependencyHandler dependencyHandler = project.getDependencies();
                final DependencySet dependencies = configuration.getDependencies();
                dependencies.add(dependencyHandler.create("org.apache.tomee:tomee-embedded:" + tomeeVersion));
            }
        });

        project.task(new HashMap<String, Object>() {{
            put("type", TomEEEmbeddedTask.class);
            put("group", "Embedded Application Server");
            put("description", "Start an embedded Apache TomEE server deploying application classpath");
        }}, TomEEEmbeddedExtension.NAME);
    }
}
