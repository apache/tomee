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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

@Mojo(name = "generate", threadSafe = true,
        requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.COMPILE)
public class JarsTxtMojo extends AbstractMojo {
    @Component
    protected MavenProject project;

    @Parameter(property = "outputFile", defaultValue = "${project.build.directory}/${project.build.finalName}/WEB-INF/jars.txt" )
    protected File outputFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!outputFile.getParentFile().exists()) {
            FileUtils.mkdir(outputFile.getParentFile().getAbsolutePath());
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(outputFile);

            for (final Artifact a : (Set<Artifact>) project.getArtifacts()) {
                if (!acceptScope(a.getScope()) || !acceptType(a.getType())) {
                    continue;
                }

                writer.write("mvn:" + a.getGroupId() + "/" + a.getArtifactId() + "/" + a.getVersion());
                writer.write("\n");
            }

            writer.flush();
        } catch (IOException e) {
            getLog().error(e.getMessage(), e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // no-op
                }
            }
        }
    }

    private boolean acceptType(final String type) {
        return "jar".equals(type) || "zip".equals(type);
    }

    private boolean acceptScope(final String scope) {
        return Artifact.SCOPE_COMPILE.equals(scope) || Artifact.SCOPE_RUNTIME.equals(scope);
    }
}
