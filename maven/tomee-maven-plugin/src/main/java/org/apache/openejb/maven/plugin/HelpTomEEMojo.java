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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal help
 */
public class HelpTomEEMojo extends AbstractMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Available commands:");
        getLog().info("\t- tomee:run: run and wait the server");
        getLog().info("\t- tomee:start: run the server");
        getLog().info("\t- tomee:stop: stop the server (to use with start)");
        getLog().info("\t- tomee:configtest: run configtest tomcat command");
        getLog().info("\t- tomee:deploy <path>: deploy path archive");
        getLog().info("\t- tomee:undeploy <path>: undeploy path archive. Note it should be the same path than the one used in deploy command");
        getLog().info("\t- tomee:list: list ejbs deployed");
        getLog().info("\t- tomee:help: this");
    }
}
