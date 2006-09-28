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
package org.apache.openejb.sfsb;

import javax.ejb.SessionBean;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openejb.AbstractInstanceContext;
import org.apache.openejb.EJBContextImpl;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.StatefulEjbContainer;
import org.apache.openejb.StatefulEjbDeployment;
import org.apache.openejb.cache.InstanceCache;
import org.apache.openejb.proxy.EJBProxyFactory;
import org.apache.openejb.util.TransactionUtils;

/**
 * @version $Revision$ $Date$
 */
public class StatefulInstanceContext extends AbstractInstanceContext {
    private static final Log log = LogFactory.getLog(StatefulInstanceContext.class);
    private final StatefulEjbContainer statefulEjbContainer;
    private final Object id;
    private final StatefulSessionContext statefulContext;
    private Transaction preexistingTransaction;
    private EJBOperation operation;
    private InstanceCache cache;

    public StatefulInstanceContext(StatefulEjbDeployment statefulEjbDeployment,
            StatefulEjbContainer statefulEjbContainer,
            SessionBean instance,
            Object id,
            EJBProxyFactory proxyFactory) {
        super(statefulEjbDeployment, instance, proxyFactory);

        this.statefulEjbContainer = statefulEjbContainer;
        this.id = id;

        TransactionManager transactionManager = statefulEjbContainer.getTransactionManager();

        UserTransaction userTransaction;
        if (statefulEjbDeployment.isBeanManagedTransactions()) {
            userTransaction = statefulEjbContainer.getUserTransaction();
        } else {
            userTransaction = null;
        }

        statefulContext = new StatefulSessionContext(this, transactionManager, userTransaction);
    }

    public EJBOperation getOperation() {
        return operation;
    }

    public void setOperation(EJBOperation operation) {
        statefulContext.setState(operation);
        this.operation = operation;
    }

    public boolean setTimerState(EJBOperation operation) {
        return statefulContext.setTimerState(operation);
    }

    public EJBContextImpl getEJBContextImpl() {
        return statefulContext;
    }

    public Object getId() {
        return id;
    }

    public Transaction getPreexistingTransaction() {
        return preexistingTransaction;
    }

    public void setPreexistingTransaction(Transaction preexistingTransaction) {
        this.preexistingTransaction = preexistingTransaction;
    }

    public InstanceCache getCache() {
        return cache;
    }

    public void setCache(InstanceCache cache) {
        this.cache = cache;
    }

    public void die() {
        if (preexistingTransaction != null) {
            if (TransactionUtils.isActive(preexistingTransaction)) {
                try {
                    preexistingTransaction.rollback();
                } catch (Exception e) {
                    log.warn("Unable to roll back", e);
                }
            }
            preexistingTransaction = null;
        }
        if (cache != null) {
            cache.remove(id);
            cache = null;
        }
        super.die();
    }

    public void setContext() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        statefulEjbContainer.setContext(this, statefulContext);
    }

    public void associate() throws Throwable {
        super.associate();
        statefulEjbContainer.afterBegin(this);
    }

    public void beforeCommit() throws Throwable {
        super.beforeCommit();
        statefulEjbContainer.beforeCommit(this);
    }

    public void afterCommit(boolean committed) throws Throwable {
        super.afterCommit(committed);
        statefulEjbContainer.afterCommit(this, committed);
    }

    public void unassociate() throws Throwable {
        super.unassociate();
        if (!isDead() && cache != null) {
            cache.putInactive(id, this);
        }
    }
}
