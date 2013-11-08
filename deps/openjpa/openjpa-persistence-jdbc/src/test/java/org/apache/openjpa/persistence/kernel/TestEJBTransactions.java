/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.kernel;

import java.util.ArrayList;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestEJBTransactions extends AbstractTestCase {

    public TestEJBTransactions(String name) {
        super(name, "kernelcactusapp");
    }

    public void setUp() {
    }

    public void testTxCannotBeCommittedTwiceInDiffThreads() throws Throwable {
        final ArrayList list = new ArrayList();

        final EntityManager em = currentEntityManager();
        final EntityTransaction tx;

        tx = em.getTransaction();
        if (tx.isActive())
            tx.rollback();
        tx.begin();
        endTx(em);

        Thread thread = new Thread() {
            @SuppressWarnings("unchecked")
            public void run() {
                try {
                    endTx(em);
                    list.add(new Integer(0));
                }
                catch (Exception e) {
                    list.add(e);
                }
                catch (Throwable t) {
                    list.add(t);
                }
            }
        };

        thread.start();

        while (list.size() == 0) ;

        Object result = list.get(0);

        if (!(result instanceof Exception)) {
            if (result instanceof Throwable)
                throw (Throwable) result;
        }
        thread.join();
    }

    public void testTransactionsCannotBeCommittedTwice() {
        EntityManager pm = currentEntityManager();
        rollbackTx(pm);

        startTx(pm);
        endTx(pm);

        try {
            // second commit
            endTx(pm);
        }
        catch (Exception e) {
            // good: we should be throwing an exception here
            System.out.println("Exception should be thrown here..." +
                "Transactions cannot be committed twice...");
        }
    }

    public void testTransactionsCannotBeRolledBackTwice() {
        EntityManager pm = currentEntityManager();
        startTx(pm);

        EntityTransaction t;
        t = pm.getTransaction();

        if (t.isActive()) {
            t.rollback();
        } else {
            t.begin();
            t.rollback();
        }

        try {
            // second rollback
            t.rollback();
        }
        catch (Exception e) {
            // good: we should be throwing an exception here
            System.out.println("Exception should be thrown here..." +
                "Transactions cannot be rolled back twice...");
        }
    }
}
