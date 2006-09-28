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
package org.apache.openejb;

import javax.ejb.EnterpriseBean;
import javax.ejb.TimerService;

import org.apache.openejb.proxy.EJBProxyFactory;
import org.apache.openejb.timer.BasicTimerService;

/**
 * @version $Revision$ $Date$
 */
public interface EJBInstanceContext {
    Object getId();

    Object getContainerId();

    // entity/stateful
    void associate() throws Throwable;

    // entity/stateful
    void unassociate() throws Throwable;

    // stateful
    void beforeCommit() throws Throwable;

    // stateful
    void afterCommit(boolean status) throws Throwable;

    // entity
    void flush() throws Throwable;

    // ALL beginInvocation
    boolean isInCall();

    // ALL beginInvocation
    void enter();

    // ALL endInvocation
    void exit();

    // ALL beginInvocation
    boolean isDead();

    // ALL beginInvocation
    void die();

    // ALL
    EnterpriseBean getInstance();

    // ALL
    void setOperation(EJBOperation operation);

    // replace with getDeployment().getProxyFactory()
    EJBProxyFactory getProxyFactory();

    // All
    TimerService getTimerService();

    // remove only used by internal TimerService
    BasicTimerService getBasicTimerService();

    // both of these should be handled by setOperation above
    void setTimerServiceAvailable(boolean available);
    boolean setTimerState(EJBOperation operation);

    EJBContextImpl getEJBContextImpl();

    ExtendedEjbDeployment getDeployment();

    Object getConnectorInstanceData();

    void setConnectorInstanceData(Object connectorInstanceData);
}
