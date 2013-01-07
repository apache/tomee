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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.io.IOException;

import static org.apache.openejb.maven.plugin.util.Zips.zip;

/**
 * Create but not run a TomEE.
 */
@Mojo(name = "build", requiresDependencyResolution = ResolutionScope.RUNTIME_PLUS_SYSTEM)
public class BuildTomEEMojo extends AbstractTomEEMojo {
    @Parameter(property = "tomee-plugin.zip", defaultValue = "true")
    protected boolean zip;

    @Parameter(property = "tomee-plugin.attach", defaultValue = "true")
    protected boolean attach;

    @Parameter(property = "tomee-plugin.zip-file", defaultValue = "${project.build.directory}/${project.build.finalName}.zip")
    private File zipFile;

    @Component
    private MavenProjectHelper projectHelper;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(property = "tomee-plugin.classifier")
    protected String classifier = null;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        if (zip) {
            getLog().info("Zipping Custom TomEE Distribution");
            try {
                zip(catalinaBase, zipFile);
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }

            if (attach) {
                getLog().info("Attaching Custom TomEE Distribution");
                if (classifier != null) {
                    projectHelper.attachArtifact(project, "zip", classifier, zipFile);
                } else {
                    projectHelper.attachArtifact(project, "zip", zipFile);
                }
            }
        }
    }

    @Override
    protected void run() {
        // don't start
    }

    @Override
    public String getCmd() {
        return null; // no need
    }

    @Override
    protected boolean getWaitTomEE() {
        return false;
    }
}
