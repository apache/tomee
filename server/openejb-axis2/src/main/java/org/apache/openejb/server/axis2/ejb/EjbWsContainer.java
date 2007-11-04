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

package org.apache.openejb.server.axis2.ejb;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.axis2.Axis2WsContainer;
import org.apache.openejb.server.axis2.AxisServiceGenerator;

import javax.xml.ws.WebServiceException;

public class EjbWsContainer extends Axis2WsContainer {
    private DeploymentInfo deploymnetInfo;

    public EjbWsContainer(PortData portData, DeploymentInfo deploymentInfo) {
        super(portData, deploymentInfo.getBeanClass(), deploymentInfo.getJndiEnc());
        this.deploymnetInfo = deploymentInfo;
    }

    public void start() throws Exception {
        super.start();

        String rootContext = null;
        String servicePath = null;
        String location = trimContext(this.port.getLocation());
        int pos = location.indexOf('/');
        if (pos > 0) {
            rootContext = location.substring(0, pos);
            servicePath = location.substring(pos + 1);
        } else {
            rootContext = "/";
            servicePath = location;
        }

        this.configurationContext.setServicePath(servicePath);
        //need to setContextRoot after servicePath as cachedServicePath is only built 
        //when setContextRoot is called.
        this.configurationContext.setContextRoot(rootContext);

        // configure handlers
        try {
            configureHandlers();
        } catch (Exception e) {
            throw new WebServiceException("Error configuring handlers", e);
        }
    }

    protected AxisServiceGenerator createServiceGenerator() {
        AxisServiceGenerator serviceGenerator = super.createServiceGenerator();
        EjbMessageReceiver messageReceiver = new EjbMessageReceiver(this, endpointClass, deploymnetInfo);
        serviceGenerator.setMessageReceiver(messageReceiver);
        return serviceGenerator;
    }

    public void destroy() {
        // call handler preDestroy
        destroyHandlers();
        super.destroy();
    }
}
