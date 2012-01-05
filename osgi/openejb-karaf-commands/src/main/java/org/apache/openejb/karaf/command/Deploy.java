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

package org.apache.openejb.karaf.command;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.openejb.assembler.Deployer;

import java.io.File;

@Command(scope = "openejb", name = "deploy", description = "deploy a JEE file/directory")
public class Deploy extends DeploymentCommand {
    @Argument(index = 0, name = "path", description = "location of the archive file/directory", required = true, multiValued = false)
    private String path;

    @Override
    protected Object doExecute() throws Exception {
        if (!new File(path).exists()) {
            System.out.println(path + " doesn't exist");
            return null;
        }

        lookup(Deployer.class, "openejb/DeployerBusinessRemote").deploy(path, properties(path));

        return null;
    }
}
