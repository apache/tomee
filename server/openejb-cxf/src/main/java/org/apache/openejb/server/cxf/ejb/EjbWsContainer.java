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
package org.apache.openejb.server.cxf.ejb;

import org.apache.cxf.Bus;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.cxf.CxfWsContainer;

public class EjbWsContainer extends CxfWsContainer {
    private final DeploymentInfo deploymentInfo;

    public EjbWsContainer(Bus bus, PortData port, DeploymentInfo deploymentInfo) {
        super(bus, port);
        if (deploymentInfo == null) throw new NullPointerException("deploymentInfo is null");
        this.deploymentInfo = deploymentInfo;
    }

    protected EjbEndpoint createEndpoint() {
        EjbEndpoint ep = new EjbEndpoint(bus, port, deploymentInfo);
        return ep;
    }
}
