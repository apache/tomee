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

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.apache.openejb.core.osgi.impl.Deployer;
import org.apache.openejb.table.Line;
import org.apache.openejb.table.Lines;
import org.osgi.framework.Bundle;

@Command(scope = "openejb", name = "bundles", description = "Lists all deployed bundles")
public class DeployedBundles extends OsgiCommandSupport {
    @Override
    protected Object doExecute() throws Exception {
        Lines lines = new Lines();
        lines.add(new Line("Id", "Symbolic name", "Version"));
        for (Bundle bundle : Deployer.instance().deployedBundles()) {
            lines.add(new Line(Long.toString(bundle.getBundleId()), bundle.getSymbolicName(), bundle.getVersion().toString()));
        }

        lines.print(System.out);
        return null;
    }
}
