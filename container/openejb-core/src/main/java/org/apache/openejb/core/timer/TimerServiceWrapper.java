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

import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.ThreadContext;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

public class TimerServiceWrapper implements TimerService {

    public TimerServiceWrapper() {
    }

    public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createTimer(initialExpiration, intervalDuration, info);
    }

    public Timer createTimer(Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createTimer(expiration, info);
    }

    public Timer createTimer(long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createTimer(initialDuration, intervalDuration, info);
    }

    public Timer createTimer(long duration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        return getTimerService().createTimer(duration, info);
    }

    public Collection getTimers() throws IllegalStateException, EJBException {
        return getTimerService().getTimers();
    }
    
    private TimerService getTimerService() throws IllegalStateException {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        DeploymentInfo deploymentInfo = threadContext.getDeploymentInfo();
        EjbTimerService timerService = deploymentInfo.getEjbTimerService();
        if (timerService == null) {
            throw new IllegalStateException("This ejb does not support timers " + deploymentInfo.getDeploymentID());
        }
        return new TimerServiceImpl(timerService, threadContext.getPrimaryKey());
    }
}
