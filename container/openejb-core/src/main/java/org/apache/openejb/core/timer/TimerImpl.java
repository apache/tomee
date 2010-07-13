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

import javax.ejb.EJBContext;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.ScheduleExpression;
import javax.ejb.EJBException;

import java.io.Serializable;
import java.util.Date;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.BaseContext;
import org.apache.openejb.core.ThreadContext;

public class TimerImpl implements Timer {
    private final TimerData timerData;

    public TimerImpl(TimerData timerData) {
        this.timerData = timerData;
    }

    public void cancel() throws IllegalStateException, NoSuchObjectLocalException {
        checkState();
        timerData.cancel();
    }

    public long getTimeRemaining() throws IllegalStateException, NoSuchObjectLocalException {
        checkState();
        long now = System.currentTimeMillis();
        long then = timerData.getExpiration().getTime();
        return then - now;
    }

    public Date getNextTimeout() throws IllegalStateException, NoSuchObjectLocalException {
        checkState();
        return timerData.getExpiration();
    }

    public Serializable getInfo() throws IllegalStateException, NoSuchObjectLocalException {
        checkState();
        return (Serializable) timerData.getInfo();
    }

    public TimerHandle getHandle() throws IllegalStateException, NoSuchObjectLocalException {
        checkState();
        return new TimerHandleImpl(timerData.getId(), timerData.getDeploymentId());
    }

    public ScheduleExpression getSchedule() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isPersistent() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean isCalendarTimer() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Insure that timer methods can be invoked for the current operation on this Context.
     */
    private void checkState() throws IllegalStateException, NoSuchObjectLocalException {
        final DeploymentInfo deployment = timerData.timerService.deployment;
        final BaseContext context = (BaseContext) deployment.get(EJBContext.class);
        context.check(BaseContext.Call.timerMethod);

        if (timerData.isCancelled()) {
            throw new NoSuchObjectLocalException("Timer has been cancelled");
        }
    }
    
}
