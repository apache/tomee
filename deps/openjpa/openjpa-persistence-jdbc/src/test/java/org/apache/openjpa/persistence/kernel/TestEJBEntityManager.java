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
import java.util.List;
import javax.persistence.EntityManager;


import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest3;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestEJBEntityManager extends AbstractTestCase {

    private Object _id = null;

    public TestEJBEntityManager(String name) {
        super(name, "kernelcactusapp");
    }

    public void setUp() throws Exception {
        super.setUp(RuntimeTest1.class, RuntimeTest2.class, RuntimeTest3.class);

        EntityManager em = currentEntityManager();
        startTx(em);

        RuntimeTest1 a = new RuntimeTest1("STRING", 10);
        RuntimeTest2 b = new RuntimeTest2("STRING2", 11);
        em.persist(a);
        em.persist(b);
        _id = a.getIntField();
        b.getIntField();

        endTx(em);
        endEm(em);
    }

    /**
     * Tests that the PM throws Exceptions on usage attempts after it has been
     * closed.
     */
    /* Fix Me - aokeke - takes a lot of time to run */
    // public void testClosed ()
    // {
    // EntityManager em = currentEntityManager();
    // startTx(em);
    // endEm(em);
    //
    // try
    // {
    // // this is the only method that should succeed
    // if(em.isOpen ())
    // fail("Supposed to be closed...but cannot be closed inside
    // container..closed at the end of funct");
    // }
    // catch (RuntimeException re)
    // {
    // fail ("isClosed");
    // }
    // try
    // {
    // em.find(RuntimeTest1.class, _id);
    // fail ("find");
    // }
    // catch (RuntimeException re)
    // {
    // }
    // }
    public void testMultipleCloseThreaded() throws Throwable {
        final EntityManager em = currentEntityManager();
        final List result = new ArrayList();
        // EntityTransaction t = em.getTransaction();
        // t.begin ();

        em.close();

        new Thread() {
            @SuppressWarnings("unchecked")
            public void run() {
                try {
                    em.close();
                    result.add(new Integer(0));
                }
                catch (Exception jdoe) {
                    result.add(jdoe);
                }
                catch (Throwable t) {
                    result.add(t);
                }
            }
        }.start();

        while (result.size() == 0)
            Thread.yield(); // wait for results
        Object ret = result.get(0);

        if (ret instanceof Exception)
            return; // good

        if (ret instanceof Throwable)
            throw (Throwable) ret;
    }

    /**
     * This method tries to perform operations that should lead to illegal
     * states, such as persisting instances outside of transactions, etc.
     */
    public void testIllegalState() {
        EntityManager em = currentEntityManager();

        RuntimeTest1 a = new RuntimeTest1("foo", 10);
        em.find(RuntimeTest1.class, _id);

        try {
            em.persist(a);
            fail("persist...");
        }
        catch (Exception ise) {
        }

        endEm(em);
    }
}
