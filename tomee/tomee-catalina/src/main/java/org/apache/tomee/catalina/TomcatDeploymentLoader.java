/**
 *
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
package org.apache.tomee.catalina;

import org.apache.catalina.core.StandardContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.DeploymentModule;

import java.net.URL;
import java.util.Set;

/**
 * @version $Id$
 */
public class TomcatDeploymentLoader extends DeploymentLoader {
    private StandardContext standardContext = null;
    private String moduleId = null;

    public TomcatDeploymentLoader(StandardContext standardContext, String moduleId) {
        this.standardContext = standardContext;
        this.moduleId = moduleId;
    }

    @Override
    protected String getContextRoot() {
        return standardContext.getPath();
    }

    @Override
    protected String getModuleName() {
        return moduleId;
    }

}
