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
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.util.HashMap;

/**
 * Custom dependencies can be added using the scope "tomee-embedded".
 */
public class TomEEEmbeddedPlugin implements Plugin<Project> {
    @Override
    public void apply(final Project project) {
        project.getExtensions().create(TomEEEmbeddedExtension.NAME, TomEEEmbeddedExtension.class);

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(final Project actionProject) {
                final TomEEEmbeddedExtension extension = actionProject.getExtensions().findByType(TomEEEmbeddedExtension.class);
                if (!extension.isSkipDefaultRepository()) {
                    actionProject.getRepositories().mavenCentral();
                }
            }
        });

        final Configuration configuration = project.getConfigurations().maybeCreate(TomEEEmbeddedExtension.NAME);
        configuration.getIncoming().beforeResolve(new Action<ResolvableDependencies>() {
            @Override
            public void execute(final ResolvableDependencies resolvableDependencies) {
                final DependencyHandler dependencyHandler = project.getDependencies();
                final DependencySet dependencies = configuration.getDependencies();
                dependencies.add(dependencyHandler.create("org.apache.tomee:tomee-embedded:" +
                        project.getExtensions().findByType(TomEEEmbeddedExtension.class).getTomeeVersion()));
            }
        });

        project.task(new HashMap<String, Object>() {{
            put("type", TomEEEmbeddedTask.class);
            put("group", "Embedded Application Server");
            put("description", "Start an embedded Apache TomEE server deploying application classpath");
        }}, TomEEEmbeddedExtension.NAME);
        TomEEEmbeddedTask.class.cast(project.getTasks().findByName(TomEEEmbeddedExtension.NAME)).setClasspath(configuration);
    }
}
