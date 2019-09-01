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
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The type UnDeployMojo.
 * Simply undeploy an application in a running TomEE
 */
@Mojo(name = "undeploy")
public class UnDeployMojo extends AbstractDeployMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Deployer deployer = (Deployer) lookup("openejb/DeployerBusinessRemote");
        try {
            final Collection<AppInfo> apps = deployer.getDeployedApps();
            final Collection<String> paths = new ArrayList<>(apps.size());
            for (final AppInfo info : apps) {
                paths.add(info.path);
            }

            if (paths.contains(path)) { // exact matching
                deployer.undeploy(path);
            } else {
                for (final String proposed : paths) { // exact matching + extension
                    if (path.equals(proposed + ".war") || path.equals(proposed + ".ear") || path.equals(proposed + ".jar")) {
                        deployer.undeploy(proposed);
                        return;
                    }
                }
                for (final String proposed : paths) { // just the app/folder name
                    if (proposed.endsWith("/" + path) || proposed.endsWith("\\" + path)) {
                        deployer.undeploy(proposed);
                        return;
                    }
                }
            }
        } catch (final OpenEJBException e) {
            throw new TomEEException(e.getMessage(), e);
        }
    }
}
