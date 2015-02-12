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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.maven.util.MavenLogStreamFactory;

import java.io.File;

public abstract class ApplicationComposerMojo extends AbstractMojo {
    @Parameter
    protected String application;

    @Parameter(defaultValue = "${project.build.outputDirectory}")
    protected File binaries;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "true")
    protected boolean mavenLog;

    protected void setupLogs() {
        if (mavenLog) {
            MavenLogStreamFactory.setLogger(getLog());
            System.setProperty("openejb.log.factory", MavenLogStreamFactory.class.getName());
            System.setProperty("openejb.jul.forceReload", "true");
        }
    }

    public static class LazyClassLoaderFinder implements ParentClassLoaderFinder {
        protected static volatile ClassLoader loader;

        @Override
        public ClassLoader getParentClassLoader(ClassLoader fallback) {
            return loader;
        }
    }
}
