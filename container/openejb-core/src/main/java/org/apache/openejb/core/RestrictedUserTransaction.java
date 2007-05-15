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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core;

import javax.transaction.UserTransaction;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;

/**
 * @version $Rev$ $Date$
 */
public class RestrictedUserTransaction implements UserTransaction {
    private final UserTransaction userTransaction;

    public RestrictedUserTransaction(UserTransaction userTransaction) {
        this.userTransaction = userTransaction;
    }

    public void begin() throws NotSupportedException, SystemException {
        checkAccess("begin");
        userTransaction.begin();
    }

    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        checkAccess("commit");
        userTransaction.commit();
    }

    public int getStatus() throws SystemException {
        checkAccess("getStatus");
        return userTransaction.getStatus();
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        checkAccess("rollback");
        userTransaction.rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        checkAccess("setRollbackOnly");
        userTransaction.setRollbackOnly();
    }

    public void setTransactionTimeout(int i) throws SystemException {
        checkAccess("setTransactionTimeout");
        userTransaction.setTransactionTimeout(i);
    }

    private void checkAccess(String methodName) {
        Operation operation = ThreadContext.getThreadContext().getCurrentOperation();
        if (operation == Operation.POST_CONSTRUCT) {
            throw new IllegalStateException("userTransaction."+methodName +"() not allowed in PostConstruct");
        }
    }
}
