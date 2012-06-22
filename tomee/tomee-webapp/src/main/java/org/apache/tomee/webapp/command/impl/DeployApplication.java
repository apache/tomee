/*
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

package org.apache.tomee.webapp.command.impl;

import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.DeployerEjb;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.tomee.webapp.command.Command;
import org.apache.tomee.webapp.command.Params;

import java.util.HashMap;
import java.util.Map;

public class DeployApplication implements Command {
    private Deployer deployer = new DeployerEjb();

    @Override
    public Object execute(Params params) throws Exception {
        final AppInfo info = deployer.deploy(params.getString("path"));

        // the path is translated from the parameter to a file path
        // the input can be "mvn:org.superbiz/rest-example.1.0/war" for instance or an http url
        final Map<String, Object> json = new HashMap<String, Object>();
        json.put("path", info.path);
        json.put("appId", info.appId);
        return json;
    }
}
