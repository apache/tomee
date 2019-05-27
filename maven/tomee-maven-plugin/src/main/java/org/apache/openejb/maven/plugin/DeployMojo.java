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
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.DeployerEjb;
import org.apache.openejb.loader.IO;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * The type DeployMojo.
 * Simply deploy an application in a running TomEE
 */
@Mojo(name = "deploy")
public class DeployMojo extends AbstractDeployMojo {
    /**
     * The System variables.
     */
    @Parameter
    protected Map<String, String> systemVariables = new HashMap<>();

    @Parameter(property = "tomee-plugin.binary", defaultValue = "false")
    private boolean useBinaries;

    /**
     * Perform the deploy
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Deployer deployer = (Deployer) lookup("openejb/DeployerBusinessRemote");
        if ((!"localhost".equals(tomeeHost) && !tomeeHost.startsWith("127.")) || useBinaries) {

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final byte[] archive;
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(path);
                IO.copy(fis, baos);
                archive = baos.toByteArray();
            } catch (final Exception e) {
                throw new TomEEException(e.getMessage(), e);
            } finally {
                IO.close(fis);
                IO.close(baos);
            }

            try {
                final Properties prop = new Properties();
                prop.putAll(systemVariables);
                prop.put(DeployerEjb.OPENEJB_USE_BINARIES, "true");
                prop.put(DeployerEjb.OPENEJB_PATH_BINARIES, new File(path).getName());
                prop.put(DeployerEjb.OPENEJB_VALUE_BINARIES, archive);
                deployer.deploy(path, prop);
            } catch (final OpenEJBException e) {
                throw new TomEEException(e.getMessage(), e);
            }
        } else {
            try {
                if (systemVariables.isEmpty()) {
                    deployer.deploy(path);
                } else {
                    final Properties prop = new Properties();
                    prop.putAll(systemVariables);
                    deployer.deploy(path, prop);
                }
            } catch (final OpenEJBException e) {
                throw new TomEEException(e.getMessage(), e);
            }
        }
    }
}
