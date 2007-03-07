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
package org.apache.openejb.core.timer;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.DeploymentInfo;

import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import java.io.Serializable;

public class TimerHandleImpl implements TimerHandle, Serializable {
    private static final long serialVersionUID = 9198316188404706377L;
    private final long id;
    private final String deploymentId;

    public TimerHandleImpl(long id, String deploymentId) {
        this.id = id;
        this.deploymentId = deploymentId;
    }

    public Timer getTimer() {
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        if (containerSystem == null) {
            throw new NoSuchObjectLocalException("OpenEJb container system is not running");
        }
        DeploymentInfo deploymentInfo = containerSystem.getDeploymentInfo(deploymentId);
        if (deploymentInfo == null) {
            throw new NoSuchObjectLocalException("Deployment info not found " + deploymentId);
        }
        EjbTimerService timerService = deploymentInfo.getEjbTimerService();
        if (timerService == null) {
            throw new NoSuchObjectLocalException("Deployment no longer supports ejbTimout " + deploymentId + ". Has this ejb been redeployed?");
        }
        Timer timer = timerService.getTimer(id);
        if (timer == null) {
            throw new NoSuchObjectLocalException("Timer not found for ejb " + deploymentId);
        }
        return timer;
    }
}
