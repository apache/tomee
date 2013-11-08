/*
 * TestRestoreValues.java
 *
 * Created on October 13, 2006, 4:48 PM
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

import java.math.BigInteger;



import org.apache.openjpa.persistence.kernel.common.apps.FetchGroupTestObject;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.RestoreStateType;

public class TestRestoreValues extends BaseKernelTest {

    private Object _oid = null;

    /**
     * Creates a new instance of TestRestoreValues
     */
    public TestRestoreValues(String name) {
        super(name);
    }

    public void setUp() {
        deleteAll(FetchGroupTestObject.class);

        FetchGroupTestObject pc = new FetchGroupTestObject();
        pc.setA(1);
        pc.setB("2");
        pc.setC(new BigInteger("100"));

        OpenJPAEntityManager pm = getPM();
        //pm.getTransaction().begin();
        startTx(pm);
        pm.persist(pc);
        //pm.getTransaction().commit();
        endTx(pm);
        _oid = pm.getObjectId(pc);
        endEm(pm);
    }

    public OpenJPAEntityManager getPM() {
        OpenJPAEntityManager pm = super.getPM();
        pm.setOptimistic(true);
        //pm.setRestoreValues(true);
        //pm.setRetainValues(true);
        pm.setRestoreState(RestoreStateType.ALL);
        pm.setRetainState(true);

        return pm;
    }

    public void testUnloadedFieldDirtiedBeforeLoadedField() {
        OpenJPAEntityManager pm = getPM();
        FetchGroupTestObject pc = (FetchGroupTestObject) pm.find
            (FetchGroupTestObject.class, _oid);

        assertNotNull("fetch object is null", pc);

        startTx(pm);
        pc.setB("3");
        pc.setA(2);
        rollbackTx(pm);

        assertEquals(1, pc.getA());
        assertEquals("2", pc.getB());
        assertEquals(new BigInteger("100"), pc.getC());
        endEm(pm);
    }

    public void testUnloadedFieldDirtiedAfterLoadedField() {
        OpenJPAEntityManager pm = getPM();
        FetchGroupTestObject pc = (FetchGroupTestObject) pm.find
            (FetchGroupTestObject.class, _oid);

        startTx(pm);
        pc.setA(2);
        pc.setB("3");
        rollbackTx(pm);

        assertEquals(1, pc.getA());
        assertEquals("2", pc.getB());
        assertEquals(new BigInteger("100"), pc.getC());
        endEm(pm);
    }

    public void testLoadedFieldDirtiedAfterLoadedField() {
        OpenJPAEntityManager pm = getPM();
        FetchGroupTestObject pc = (FetchGroupTestObject) pm.find
            (FetchGroupTestObject.class, _oid);

        startTx(pm);
        pc.setA(2);
        pc.getB();
        pc.setB("3");
        rollbackTx(pm);

        assertEquals(1, pc.getA());
        assertEquals("2", pc.getB());
        assertEquals(new BigInteger("100"), pc.getC());
        endEm(pm);
    }

    public void testNewInstanceUnmodifiedInTransaction() {
        FetchGroupTestObject pc = new FetchGroupTestObject();
        pc.setA(2);
        pc.setB("3");

        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        pm.persist(pc);
        rollbackTx(pm);
        assertEquals(2, pc.getA());
        assertEquals("3", pc.getB());
        endEm(pm);
    }
}
