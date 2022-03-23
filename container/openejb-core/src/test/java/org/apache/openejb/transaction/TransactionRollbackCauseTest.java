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
package org.apache.openejb.transaction;

import junit.framework.TestCase;
import org.apache.openejb.jee.SingletonBean;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Singleton;
import jakarta.ejb.embeddable.EJBContainer;
import jakarta.transaction.RollbackException;
import jakarta.transaction.UserTransaction;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class TransactionRollbackCauseTest extends TestCase {

    @EJB
    private Orange orange;

    @Resource
    private UserTransaction userTransaction;

    public void test() throws Exception {
        final Map<String, Object> map = new HashMap<>();
        map.put(EJBContainer.MODULES, new SingletonBean(Orange.class));
        EJBContainer.createEJBContainer(map).getContext().bind("inject", this);

        userTransaction.begin();

        orange.exceptionRollback();

        try {
            userTransaction.commit();
            fail("transaction should have been rolled back");
        } catch (final RollbackException e) {
            final Throwable throwable = e.getCause();
            assertTrue(throwable instanceof UserException);
        }
    }


    @Singleton
    @LocalBean
    @Lock(LockType.READ)
    public static class Orange {

        @EJB
        private Orange orange;

        @Resource
        private SessionContext sessionContext;

        public void exceptionRollback() {
            try {
                orange.throwException();
            } catch (final Exception e) {
            }
        }

        public void throwException() {
            throw new UserException("rollback");
        }
    }

    public static class UserException extends RuntimeException {
        public UserException(final String message) {
            super(message);
        }
    }
}
