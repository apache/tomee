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

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.embeddable.EJBContainer;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
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
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(EJBContainer.MODULES, new SingletonBean(Orange.class));
        EJBContainer.createEJBContainer(map).getContext().bind("inject", this);

        userTransaction.begin();

        orange.exceptionRollback();

        try {
            userTransaction.commit();
            fail("transaction should have been rolled back");
        } catch (RollbackException e) {
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
            } catch (Exception e) {
            }
        }

        public void throwException() {
            throw new UserException("rollback");
        }
    }

    public static class UserException extends RuntimeException {
        public UserException(String message) {
            super(message);
        }
    }
}
