/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @version $Rev: 437623 $ $Date: 2006-08-28 02:48:23 -0700 (Mon, 28 Aug 2006) $
 */
public class VmDeploymentFactory implements DeploymentFactory {
    public static final String URI_SCHEME = "openejb";

    public String getDisplayName() {
        return "OpenEJB - VM";
    }

    public String getProductVersion() {
        return "3.0";
    }

    public boolean handlesURI(String uri) {
        try {
            URI fullUri = new URI(uri);
            return URI_SCHEME.equals(fullUri.getScheme());
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException("Invalid URI: " + uri);
        }

        VmDeploymentManager deploymentManager = new VmDeploymentManager();
        deploymentManager.release();
        return deploymentManager;
    }

    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
        return new VmDeploymentManager();
    }
}
