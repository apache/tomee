/*
 * TestInheritance.java
 *
 * Created on October 12, 2006, 11:46 AM
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

import java.util.Collection;



import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest3;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;

public class TestInheritance extends BaseKernelTest {

    /**
     * Creates a new instance of TestInheritance
     */
    public TestInheritance() {
    }

    public TestInheritance(String name) {
        super(name);
    }

    public void setUp()
        throws Exception {
        super.setUp(RuntimeTest1.class, RuntimeTest2.class, RuntimeTest3.class);

        // create some instances to query on
        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        pm.persist(new RuntimeTest1("RuntimeTest1-instance", 2));
        pm.persist(new RuntimeTest2("RuntimeTest2-instance", 3));
        pm.persist(new RuntimeTest3("RuntimeTest3-instance", 4));
        endTx(pm);
        endEm(pm);
    }

    /**
     * Tests that we can get all three objects via an extent.
     */
    public void testGetAllRuntimeTest1s() {
        OpenJPAEntityManager pm = getPM();
        OpenJPAQuery q = pm.createQuery("SELECT o FROM RuntimeTest1 o");
        Collection c = q.getResultList();
        assertEquals(3, c.size());
        endEm(pm);
    }

    /**
     * Tests that we can get all RuntimeTest2 objects via an extent.
     */
    public void testGetAllRuntimeTest2s() {
        OpenJPAEntityManager pm = getPM();
        OpenJPAQuery q = pm.createQuery("SELECT o FROM RuntimeTest2 o");
        Collection c = q.getResultList();
        assertEquals(2, c.size());
        endEm(pm);
    }

    /**
     * Tests that we can get all RuntimeTest3 objects via an extent.
     */
    public void testGetAllRuntimeTest3s() {
        OpenJPAEntityManager pm = getPM();
        OpenJPAQuery q = pm.createQuery("SELECT o FROM RuntimeTest3 o");
        Collection c = q.getResultList();
        assertEquals(1, c.size());
        endEm(pm);
    }

    /**
     * Tests that we can get just RuntimeTest1s via an extent.
     */
    public void testGetJustRuntimeTest1s() {
        OpenJPAEntityManager pm = getPM();
        OpenJPAQuery q = pm.createQuery("SELECT o FROM RuntimeTest1 o");
        q.setSubclasses(false);

//        OpenJPAQuery q = pm.createNativeQuery("",RuntimeTest1.class);
//        FIXME jthomas
//        q.setCandidates(pm.getExtent(RuntimeTest1.class, false));
        Collection c = q.getResultList();
        assertEquals(1, c.size());
        endEm(pm);
    }

    /**
     * Tests that we can get just RuntimeTest2s via an extent.
     */
    public void testGetJustRuntimeTest2s() {
        OpenJPAEntityManager pm = getPM();
        OpenJPAQuery q = pm.createQuery("SELECT o FROM RuntimeTest2 o");
        q.setSubclasses(false);

//        FIXME jthomas        
//        OpenJPAQuery q = pm.createNativeQuery("",RuntimeTest2.class);
//        q.setCandidates(pm.createExtent(RuntimeTest2.class, false));
        Collection c = q.getResultList();
        assertEquals(1, c.size());
        endEm(pm);
    }

    /**
     * Tests that we can get just RuntimeTest3s via an extent.
     */
    public void testGetJustRuntimeTest3() {
        OpenJPAEntityManager pm = getPM();
        OpenJPAQuery q = pm.createQuery("SELECT o FROM RuntimeTest3 o");
        q.setSubclasses(false);

//      FIXME jthomas
//        OpenJPAQuery q = pm.createNativeQuery("",RuntimeTest3.class);
//        q.setCandidates(pm.getExtent(RuntimeTest3.class, false));
        Collection c = q.getResultList();
        assertEquals(1, c.size());
        endEm(pm);
    }
}
