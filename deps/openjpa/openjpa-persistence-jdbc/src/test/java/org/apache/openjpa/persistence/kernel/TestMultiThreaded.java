/*
 * TestMultiThreaded.java
 *
 * Created on October 12, 2006, 2:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAQuery;

public class TestMultiThreaded extends BaseKernelTest {

    static int serial = 5;
    static int threads = 5;
    static int iterations = 5;

    private OpenJPAEntityManager pm;
    private Object id;
    private String name;

    /**
     * Creates a new instance of TestMultiThreaded
     */
    public TestMultiThreaded() {
    }

    public TestMultiThreaded(String name) {
        super(name);
    }

    public void setUp() {
        deleteAll(RuntimeTest1.class);

        OpenJPAEntityManager pm2 = getPM();
        startTx(pm2);
        name = "testMultiThreaded" + Math.random();
        RuntimeTest1 a = new RuntimeTest1(name,
            (int) (Math.random() * Integer.MAX_VALUE));
        pm2.persist(a);
        id = pm2.getObjectId(a);
        endTx(pm2);

        Map props = new HashMap();
        props.put("openjpa.Multithreaded", "true");
        OpenJPAEntityManagerFactory pmf =
            (OpenJPAEntityManagerFactory) getEmf(props);
        pm = pmf.createEntityManager();
        startTx(pm);
    }

    public void tearDown()
        throws Exception {
        try {
            rollbackTx(pm);
            endEm(pm);
        } catch (Exception e) {
            // this is not what we are testing
        }
        super.tearDown();
    }

    public void testgetTransaction() {
        mttest(serial, threads, iterations);
        pm.getTransaction();
    }

    public void testGetObjectById() {
        mttest(serial, threads, iterations);
        assertNotNull(pm.find(RuntimeTest1.class, id));
    }

    public void testQueryExecution() {
        mttest(serial, threads, iterations);
        OpenJPAQuery q = pm.createQuery("select o from RuntimeTest1 o "
            + "where o.stringField = '" + name + "'");
        assertEquals(1, q.getResultList().size());
    }

    public void testDeletePersistent() {
        mttest(serial, threads, iterations);
        pm.removeAll(pm.createQuery("select o from RuntimeTest1 o "
            + "where o.stringField = '" + name + "'").getResultList());
    }

    public void testRefreshAll() {
        mttest(serial, threads, iterations);
        pm.refreshAll();
    }

    public void testEvictAll() {
        mttest(serial, threads, iterations);
        pm.evictAll();
    }

    public void testIterateExtent()
        throws Throwable {
        if (timeout(120 * 1000)) return;

        mttest(serial, threads, iterations);

        assertTrue("Transaction should have been active",
            pm.getTransaction().isActive());

        for (int i = 0; i < 3; i++) {
            pm.persist(new RuntimeTest1("testIterateExtent" + Math.random(),
                (int) (Math.random() * Integer.MAX_VALUE)));
        }

        assertTrue("Transaction should have been active",
            pm.getTransaction().isActive());

        for (Iterator i = pm.createExtent(RuntimeTest1.class, true).iterator();
            i.hasNext(); i.next())
            ;

        assertTrue("Transaction should have been active",
            pm.getTransaction().isActive());
    }
}
