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
package org.apache.openejb.slsb;

import javax.ejb.SessionBean;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;

import org.apache.openejb.AbstractInstanceContext;
import org.apache.openejb.EJBContextImpl;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.StatelessEjbContainer;
import org.apache.openejb.StatelessEjbDeployment;
import org.apache.openejb.cache.InstancePool;
import org.apache.openejb.proxy.EJBProxyFactory;

/**
 * Wrapper for a Stateless SessionBean.
 *
 * @version $Revision$ $Date$
 */
public final class StatelessInstanceContext extends AbstractInstanceContext {
    private final StatelessEjbContainer statelessEjbContainer;
    private final StatelessSessionContext sessionContext;

    private InstancePool pool;
    private MessageContext messageContext;

    public StatelessInstanceContext(StatelessEjbDeployment statelessEjbDeployment,
            StatelessEjbContainer statelessEjbContainer,
            SessionBean instance,
            EJBProxyFactory proxyFactory) {
        super(statelessEjbDeployment, instance, proxyFactory);

        this.statelessEjbContainer = statelessEjbContainer;

        TransactionManager transactionManager = statelessEjbContainer.getTransactionManager();

        UserTransaction userTransaction;
        if (statelessEjbDeployment.isBeanManagedTransactions()) {
            userTransaction = statelessEjbContainer.getUserTransaction();
        } else {
            userTransaction = null;
        }

        this.sessionContext = new StatelessSessionContext(this, transactionManager, userTransaction);
    }

    public void setId(Object id) {
        throw new AssertionError("Cannot set identity for a Stateless Context");
    }

    public InstancePool getPool() {
        return pool;
    }

    public void setPool(InstancePool pool) {
        this.pool = pool;
    }

    public void die() {
        if (pool != null) {
            pool.remove(this);
            pool = null;
        }
        super.die();
    }

    public void exit() {
        if (pool != null) {
            pool.release(this);
        }
        super.exit();
    }

    public MessageContext getMessageContext() {
        return messageContext;
    }

    public void setMessageContext(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    public void flush() {
        throw new AssertionError("Cannot flush Stateless Context");
    }

    public void setOperation(EJBOperation operation) {
        sessionContext.setState(operation);
    }

    public boolean setTimerState(EJBOperation operation) {
        return sessionContext.setTimerState(operation);
    }

    public EJBContextImpl getEJBContextImpl() {
        return sessionContext;
    }

    public void setContext() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        statelessEjbContainer.setContext(this, -1, sessionContext);
    }

    public void ejbCreate() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        assert(getInstance() != null);
        statelessEjbContainer.ejbCreate(this, -1);
    }

    public void ejbRemove() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        assert(getInstance() != null);
        statelessEjbContainer.ejbRemove(this, -1);
    }
}
