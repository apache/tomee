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

package org.apache.openejb.config;

import org.apache.openejb.util.URLs;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import java.net.URI;

/**
 * @version $Rev$ $Date$
 */
public class VmDeploymentFactory implements DeploymentFactory {
    public static final String URI_SCHEME = "openejb";

    public String getDisplayName() {
        return "OpenEJB - VM";
    }

    public String getProductVersion() {
        return "3.1.1";
    }

    public boolean handlesURI(final String uri) {
        final URI fullUri = URLs.uri(uri);
        return URI_SCHEME.equals(fullUri.getScheme());
    }

    public DeploymentManager getDisconnectedDeploymentManager(final String uri) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException("Invalid URI: " + uri);
        }

        final VmDeploymentManager deploymentManager = new VmDeploymentManager();
        deploymentManager.release();
        return deploymentManager;
    }

    public DeploymentManager getDeploymentManager(final String uri, final String username, final String password) throws DeploymentManagerCreationException {
        return new VmDeploymentManager();
    }
}
