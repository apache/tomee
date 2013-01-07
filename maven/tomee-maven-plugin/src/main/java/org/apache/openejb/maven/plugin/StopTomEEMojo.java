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
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.openejb.config.RemoteServer;

import java.util.List;

/**
 * Stop a TomEE started with start command.
 */
@Mojo(name = "stop", requiresDependencyResolution = ResolutionScope.RUNTIME_PLUS_SYSTEM)
public class StopTomEEMojo extends AbstractTomEEMojo {
    @Override
    public String getCmd() {
        return "stop";
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        run(); // skip other processings which are useless for a stop
    }

    @Override
    protected void serverCmd(final RemoteServer server, final List<String> strings) {
        try {
            server.forceStop();
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
        }
    }

    @Override
    protected boolean getWaitTomEE() {
        return false;
    }
}
