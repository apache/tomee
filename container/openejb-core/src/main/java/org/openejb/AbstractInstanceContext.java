/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openejb;

import javax.ejb.EnterpriseBean;
import javax.ejb.TimerService;

import org.openejb.proxy.EJBProxyFactory;
import org.openejb.timer.BasicTimerService;
import org.openejb.timer.TimerServiceImpl;
import org.openejb.timer.UnavailableTimerService;


/**
 * Simple implementation of ComponentContext satisfying invariant.
 *
 * @version $Revision$ $Date$
 *
 * */
public abstract class AbstractInstanceContext implements EJBInstanceContext {
    private final ExtendedEjbDeployment deployment;
    private final Object containerId;
    private final EnterpriseBean instance;
    private final EJBProxyFactory proxyFactory;
    private final BasicTimerService activeTimer;
    private final TimerService timerService;

    private BasicTimerService timerState = UnavailableTimerService.INSTANCE;
    private boolean dead = false;
    private int callDepth;
    private Object connectorInstanceData;

    public AbstractInstanceContext(ExtendedEjbDeployment deployment,
            EnterpriseBean instance,
            EJBProxyFactory proxyFactory) {
        this.deployment = deployment;
        this.containerId = deployment.getContainerId();
        this.instance = instance;
        this.proxyFactory = proxyFactory;
        this.activeTimer = deployment.getTimerService();
        if (activeTimer == null) {
            this.timerService = null;
        } else {
            this.timerService = new TimerServiceImpl(this);
        }    }

    public ExtendedEjbDeployment getDeployment() {
        return deployment;
    }

    public Object getId() {
        return null;
    }

    public Object getContainerId() {
        return containerId;
    }

    public void associate() throws Throwable {
        if (dead) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
    }

    public void flush() throws Throwable {
        if (dead) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
    }

    public void beforeCommit() throws Throwable {
        if (dead) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
    }

    public void afterCommit(boolean status) throws Throwable {
        if (dead) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
    }

    public void unassociate() throws Throwable {
    }

    public Object getConnectorInstanceData() {
        return connectorInstanceData;
    }

    public void setConnectorInstanceData(Object connectorInstanceData) {
        this.connectorInstanceData = connectorInstanceData;
    }

    public EnterpriseBean getInstance() {
        return instance;
    }

    public EJBProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public TimerService getTimerService() {
        return timerService;
    }

    public BasicTimerService getBasicTimerService() {
        return timerState;
    }

    public void setTimerServiceAvailable(boolean available) {
        if (available) {
            timerState = activeTimer;
        } else {
            timerState = UnavailableTimerService.INSTANCE;
        }
    }

    public void die() {
        this.dead = true;
    }

    public final boolean isDead() {
        return dead;
    }

    public boolean isInCall() {
        return callDepth > 0;
    }

    public void enter() {
        callDepth++;
    }

    public void exit() {
        if (!isInCall()){
            throw new IllegalArgumentException("EJB instance is not in a call");
        }
        callDepth--;
    }

    public String toString() {
        return "[InstanceContext: container=" + getContainerId() + ", id=" + getId() + "]";
    }

}
