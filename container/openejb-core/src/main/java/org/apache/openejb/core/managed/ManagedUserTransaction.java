/*
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

package org.apache.openejb.core.managed;

import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

public class ManagedUserTransaction implements UserTransaction {
    private final UserTransaction userTransaction;
    private final JtaEntityManagerRegistry jtaEntityManagerRegistry;

    public ManagedUserTransaction(final UserTransaction userTransaction, final JtaEntityManagerRegistry jtaEntityManagerRegistry) {
        this.userTransaction = userTransaction;
        this.jtaEntityManagerRegistry = jtaEntityManagerRegistry;
    }

    public void begin() throws NotSupportedException, SystemException {
        userTransaction.begin();

        // get the callContext
        final ThreadContext callContext = ThreadContext.getThreadContext();
        if (callContext == null) {
            // someone is using the user transaction out side of the component
            return;
        }

        // get the deployment info
        final BeanContext beanContext = callContext.getBeanContext();
        if (beanContext.getComponentType() != BeanType.MANAGED) {
            // some other non-stateful ejb is using our user transaction
            return;
        }

        // get the primary key
        final Object primaryKey = callContext.getPrimaryKey();
        if (primaryKey == null) {
            // is is not a bean method
            return;
        }
        jtaEntityManagerRegistry.transactionStarted((String) beanContext.getDeploymentID(), primaryKey);
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        userTransaction.commit();
    }

    public int getStatus() throws SystemException {
        return userTransaction.getStatus();
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        userTransaction.rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        userTransaction.setRollbackOnly();
    }

    public void setTransactionTimeout(final int i) throws SystemException {
        userTransaction.setTransactionTimeout(i);
    }
}
