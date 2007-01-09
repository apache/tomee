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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.telnet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

public class Ls extends Command {

    public static void register() {
        Ls cmd = new Ls();
        Command.register("system", cmd);

    }

    public void exec(Arguments args, DataInputStream in, PrintStream out) throws IOException {

        ContainerSystem containerSystem1 = SystemInstance.get().getComponent(ContainerSystem.class);
        Container[] c = containerSystem1.containers();
        out.println("Containers:");

        for (int i = 0; i < c.length; i++) {
            out.print(" " + c[i].getContainerID());
            out.println("");
        }
        out.println("");

        out.println("Deployments:");

        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        DeploymentInfo[] d = containerSystem.deployments();
        for (int i = 0; i < d.length; i++) {
            out.print(" " + d[i].getDeploymentID());
            out.println("");
        }
    }

}

