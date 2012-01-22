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

package org.apache.openejb.server.cli.command;

import org.apache.openejb.assembler.Deployer;

@Command(name = "undeploy", usage = "undeploy <location>", description = "undeploy an application. Note the location should be the same than for deploy")
public class Undeploy extends AbstractCommand {
    @Override
    public void execute(String cmd) {
        try {
            lookup(Deployer.class, "openejb/DeployerBusinessRemote").undeploy(cmd.substring(8).trim());
        } catch (Exception e) {
            streamManager.writeErr(e);
        }
    }
}
