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
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.io.IOException;

import static org.apache.openejb.maven.plugin.util.Zips.zip;

/**
 * @goal build
 * @requiresDependencyResolution runtime
 */
public class BuildTomEEMojo extends AbstractTomEEMojo {
    /**
     * @parameter expression="${tomee-plugin.zip}" default-value="true"
     */
    protected boolean zip;

    /**
     * @parameter expression="${tomee-plugin.attach}" default-value="true"
     */
    protected boolean attach;

    /**
     * @parameter expression="${tomee-plugin.zip-file}" default-value="${project.build.directory}/${project.build.finalName}.zip""
     * @required
     * @readOnly
     */
    private File zipFile;

    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter expression="${tomee-plugin.classifier}"
     */
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
}
