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
package org.apache.openejb.persistence.java;

import org.apache.openejb.persistence.JNDIContext;

import javax.naming.spi.ObjectFactory;
import javax.naming.Name;
import javax.naming.Context;
import javax.transaction.TransactionManager;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Transaction;
import javax.transaction.InvalidTransactionException;
import java.util.Hashtable;

public class javaURLContextFactory implements ObjectFactory {
    /**
     * Return a Context that is able to resolve names in the java: namespace.
     * The root context, "java:" is always returned. This is a specific
     * implementation of a URLContextFactory and not a general ObjectFactory.
     * @param obj must be null
     * @param name ignored
     * @param nameCtx ignored
     * @param environment ignored
     * @return the Context for "java:"
     * @throws javax.naming.OperationNotSupportedException if obj is not null
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception {
        JNDIContext jndiContext = new JNDIContext();
        jndiContext.bind("java:TransactionManager", new MockTransactionManager());
        return jndiContext;
    }


    public static class MockTransactionManager implements TransactionManager {
        public void begin() throws NotSupportedException, SystemException {
        }

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        }

        public int getStatus() throws SystemException {
            return 0;
        }

        public Transaction getTransaction() throws SystemException {
            return null;
        }

        public void resume(Transaction transaction) throws IllegalStateException, InvalidTransactionException, SystemException {
        }

        public void rollback() throws IllegalStateException, SecurityException, SystemException {
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
        }

        public void setTransactionTimeout(int i) throws SystemException {
        }

        public Transaction suspend() throws SystemException {
            return null;
        }
    }
}
