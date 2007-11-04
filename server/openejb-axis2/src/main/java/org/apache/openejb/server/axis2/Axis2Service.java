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
package org.apache.openejb.server.axis2;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.axis2.ejb.EjbWsContainer;
import org.apache.openejb.server.axis2.pojo.PojoWsContainer;
import org.apache.openejb.server.webservices.WsService;

import javax.naming.Context;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

public class Axis2Service extends WsService {
    private final Map<String, Axis2WsContainer> wsContainers = new TreeMap<String, Axis2WsContainer>();

    public String getName() {
        return "axis2";
    }

    protected HttpListener createEjbWsContainer(URL moduleBaseUrl, PortData port, DeploymentInfo deploymentInfo) throws Exception {
        EjbWsContainer container = new EjbWsContainer(port, deploymentInfo);
        container.start();
        wsContainers.put(deploymentInfo.getDeploymentID().toString(), container);
        return container;
    }

    protected void destroyEjbWsContainer(String deploymentId) {
        Axis2WsContainer container = wsContainers.remove(deploymentId);
        if (container != null) {
            container.destroy();
        }
    }

    protected HttpListener createPojoWsContainer(URL moduleBaseUrl, PortData port, String serviceId, Class target, Context context, String contextRoot) throws Exception {
        PojoWsContainer container = new PojoWsContainer(port, target, context, contextRoot);
        container.start();
        wsContainers.put(serviceId, container);
        return container;
    }

    protected void destroyPojoWsContainer(String serviceId) {
        Axis2WsContainer container = wsContainers.remove(serviceId);
        if (container != null) {
            container.destroy();
        }
    }
}
