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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ivm.naming.Reference;
import org.apache.openejb.core.ivm.naming.ObjectReference;

import javax.naming.Context;

/**
 * @version $Rev$ $Date$
 */
public class JndiBuilder {
    private final Context context;

    public JndiBuilder(Context context) {
        this.context = context;
    }

    public void bind(DeploymentInfo deploymentInfo) {
        CoreDeploymentInfo deployment = (CoreDeploymentInfo) deploymentInfo;
        if (deployment.getHomeInterface() != null) {
            bindProxy(deployment, deployment.getEJBHome(), false);
        }
        if (deployment.getLocalHomeInterface() != null) {
            bindProxy(deployment, deployment.getEJBLocalHome(), true);
        }
    }

    private void bindProxy(CoreDeploymentInfo deployment, Object proxy, boolean isLocal) {
        Reference ref = new ObjectReference(proxy);

        if (deployment.getComponentType() == DeploymentInfo.STATEFUL) {
            ref = new org.apache.openejb.core.stateful.EncReference(ref);
        } else if (deployment.getComponentType() == DeploymentInfo.STATELESS) {
            ref = new org.apache.openejb.core.stateless.EncReference(ref);
        } else {
            ref = new org.apache.openejb.core.entity.EncReference(ref);
        }

        try {

            String bindName = deployment.getDeploymentID().toString();

            if (bindName.charAt(0) == '/') {
                bindName = bindName.substring(1);
            }

            bindName = "openejb/ejb/" + bindName;
            if (isLocal) {
                bindName += "Local";
            }
            context.bind(bindName, ref);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
