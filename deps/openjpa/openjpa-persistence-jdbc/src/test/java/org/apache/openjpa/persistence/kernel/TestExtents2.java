/*
 * TestExtents2.java
 *
 * Created on October 10, 2006, 5:30 PM
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

import java.util.Iterator;



import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest3;

import org.apache.openjpa.persistence.Extent;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestExtents2 extends BaseKernelTest {

    private Object _oid1 = null;
    private Object _oid2 = null;
    private Object _oid3 = null;

    public TestExtents2(String name) {
        super(name);
    }

    /**
     * Creates a new instance of TestExtents2
     */
    public TestExtents2() {
    }

    public void setUp() throws Exception {
        super.setUp(RuntimeTest1.class, RuntimeTest2.class, RuntimeTest3.class);

        RuntimeTest1 test1 = new RuntimeTest1();
        test1.setIntField(1);
        RuntimeTest2 test2 = new RuntimeTest2();
        test2.setIntField(2);
        test2.setIntField2(2);
        RuntimeTest3 test3 = new RuntimeTest3();
        test3.setIntField(3);
        test3.setIntField3(3);

        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        pm.persist(test1);
        pm.persist(test2);
        pm.persist(test3);
        _oid1 = pm.getObjectId(test1);
        _oid2 = pm.getObjectId(test2);
        _oid3 = pm.getObjectId(test3);
        endTx(pm);
        endEm(pm);
    }

    public void testProperties() {
        OpenJPAEntityManager pm = getPM();
        Extent ext = pm.createExtent(RuntimeTest2.class, false);
        assertEquals(pm, ext.getEntityManager());
        assertEquals(RuntimeTest2.class, ext.getElementClass());
        assertTrue(!ext.hasSubclasses());

        ext = pm.createExtent(RuntimeTest1.class, true);
        assertEquals(pm, ext.getEntityManager());
        assertEquals(RuntimeTest1.class, ext.getElementClass());
        assertTrue(ext.hasSubclasses());
    }

    public void testNoSubclasses() {
        OpenJPAEntityManager pm = getPM();
        Extent ext = pm.createExtent(RuntimeTest1.class, false);
        Iterator itr = ext.iterator();
        assertTrue(itr.hasNext());
        assertEquals(_oid1, pm.getObjectId(itr.next()));
        assertTrue(!itr.hasNext());
        ext.closeAll();
    }

    public void testSubclasses() {
        OpenJPAEntityManager pm = getPM();
        Extent ext = pm.createExtent(RuntimeTest1.class, true);

        int test1Count = 0;
        int test2Count = 0;
        int test3Count = 0;
        Object next;
        for (Iterator itr = ext.iterator(); itr.hasNext();) {
            next = pm.getObjectId(itr.next());
            if (_oid1.equals(next))
                test1Count++;
            else if (_oid2.equals(next))
                test2Count++;
            else if (_oid3.equals(next))
                test3Count++;
        }
        ext.closeAll();
        assertEquals(1, test1Count);
        assertEquals(1, test2Count);
        assertEquals(1, test3Count);
    }

    public void testContainsNewlyPersisted() {
        RuntimeTest1 test1 = new RuntimeTest1();
        RuntimeTest1 test2 = new RuntimeTest2();

        OpenJPAEntityManager pm = getPM();

        // pcl: 14 Oct 2003: default in 3.0 is now true, but this test
        // assumes false somewhere.

        pm.setIgnoreChanges(false);
        startTx(pm);
        try {
            pm.persist(test1);
            pm.persist(test2);
            Object newOid = pm.getObjectId(test1);

            Extent ext = pm.createExtent(RuntimeTest1.class, false);
            boolean foundOid1 = false;
            boolean foundNew = false;
            Object next;
            for (Iterator itr = ext.iterator(); itr.hasNext();) {
                next = pm.getObjectId(itr.next());
                if (_oid1.equals(next))
                    foundOid1 = true;
                else if (newOid.equals(next))
                    foundNew = true;
                else
                    fail("Bad object in extent.");
            }
            ext.closeAll();
            assertTrue(foundOid1);
            assertTrue(foundNew);
        } finally {
            rollbackTx(pm);
        }
    }

    public void testNotContainsNewlyDeleted() {
        OpenJPAEntityManager pm = getPM();

        // pcl: 14 Oct 2003: default in 3.0 is now true, but this test
        // assumes false somewhere.
        pm.setIgnoreChanges(false);
        startTx(pm);
        try {
            RuntimeTest2 test2 =
                pm.find(RuntimeTest2.class, _oid2);
            pm.remove(test2);
            RuntimeTest1 test1 = new RuntimeTest1();
            pm.persist(test1);
            pm.remove(test1);

            Extent ext = pm.createExtent(RuntimeTest1.class, true);
            boolean foundOid1 = false;
            boolean foundOid3 = false;
            Object next;
            for (Iterator itr = ext.iterator(); itr.hasNext();) {
                next = pm.getObjectId(itr.next());
                if (_oid1.equals(next))
                    foundOid1 = true;
                else if (_oid3.equals(next))
                    foundOid3 = true;
                else
                    fail("Bad object in extent.");
            }
            ext.closeAll();
            assertTrue(foundOid1);
            assertTrue(foundOid3);
        } finally {
            rollbackTx(pm);
        }
    }
}
