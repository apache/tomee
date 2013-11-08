/*
 * TestInitialValueFetching.java
 *
 * Created on October 12, 2006, 11:58 AM
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest3;
import org.apache.openjpa.persistence.test.AllowFailure;

import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.RestoreStateType;

public class TestInitialValueFetching extends BaseKernelTest {

    /**
     * Creates a new instance of TestInitialValueFetching
     */
    public TestInitialValueFetching(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp(RuntimeTest1.class, RuntimeTest2.class, RuntimeTest3.class);
        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        RuntimeTest1 rt1 = new RuntimeTest1("TestInitialValueFetching", 10);
        pm.persist(rt1);

        rt1.setDateField(new Date());
        endTx(pm);
        endEm(pm);
    }

    public void testInitialValueString() {
        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        RuntimeTest1 rt1 = getObject(pm);
        OpenJPAStateManager sm = getStateManager(rt1, pm);
        FieldMetaData fmd = sm.getMetaData().getField("stringField");
        assertEquals("TestInitialValueFetching",
            sm.fetchInitialField(fmd.getIndex()));
        rt1.setStringField("TestInitialValueFetching-2");
        assertEquals("TestInitialValueFetching",
            sm.fetchInitialField(fmd.getIndex()));
        endTx(pm);
        assertEquals("TestInitialValueFetching-2",
            sm.fetchInitialField(fmd.getIndex()));
        endEm(pm);
    }

    public void testInitialValueInt() {
        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        RuntimeTest1 rt1 = getObject(pm);
        OpenJPAStateManager sm = getStateManager(rt1, pm);
        FieldMetaData fmd = sm.getMetaData().getField("intField1");
        assertEquals(10,
            ((Integer) sm.fetchInitialField(fmd.getIndex())).intValue());
        rt1.setIntField1(11);
        assertEquals(10,
            ((Integer) sm.fetchInitialField(fmd.getIndex())).intValue());
        endTx(pm);
        assertEquals(11,
            ((Integer) sm.fetchInitialField(fmd.getIndex())).intValue());
        endEm(pm);
    }

    @AllowFailure
    public void testInitialValueMutableValueFailures() {
        Map props = new HashMap();
        props.put("openjpa.RestoreMutableValues", "false");
        OpenJPAEntityManagerFactory pmf = getEmf(props);

        OpenJPAEntityManager pm = pmf.createEntityManager();
        RuntimeTest1 rt1 = getObject(pm);

        rt1.getDateField();
        OpenJPAStateManager sm = getStateManager(rt1, pm);
        FieldMetaData fmd = sm.getMetaData().getField("dateField");
        try {
            sm.fetchInitialField(fmd.getIndex());
            fail("should get an exception if RestoreMutableValues is false");
        } catch (org.apache.openjpa.util.UserException e) {
            // expected
        }
        endEm(pm);
        pmf.close();
    }

    @AllowFailure
    public void testInitialValueDate() {
        Map props = new HashMap();
        props.put("openjpa.RestoreState", "all");
        OpenJPAEntityManagerFactory pmf = getEmf(props);
        OpenJPAEntityManager pm = pmf.createEntityManager();
        startTx(pm);
        RuntimeTest1 rt1 = getObject(pm);

        Date d = rt1.getDateField();

        OpenJPAStateManager sm = getStateManager(rt1, pm);
        FieldMetaData fmd = sm.getMetaData().getField("dateField");
        assertEquals(d, sm.fetchInitialField(fmd.getIndex()));

        // == should pass here since we haven't made any modifications.
        assertTrue("mutable object fails == test; should not",
            d == sm.fetchInitialField(fmd.getIndex()));

        Date d2 = new Date();
        rt1.setDateField(d2);
        assertEquals(d, sm.fetchInitialField(fmd.getIndex()));
        endTx(pm);
        assertEquals(d2, sm.fetchInitialField(fmd.getIndex()));
        assertTrue("mutable object passes == test; should not",
            d2 != sm.fetchInitialField(fmd.getIndex()));
        endEm(pm);
    }

    public void testInitialValueExceptions() {
        OpenJPAEntityManager pm = getPM();
        pm.setRestoreState(RestoreStateType.NONE);
        startTx(pm);
        RuntimeTest1 rt1 = getObject(pm);
        OpenJPAStateManager sm = getStateManager(rt1, pm);
        FieldMetaData fmd = sm.getMetaData().getField("stringField");
        try {
            sm.fetchInitialField(fmd.getIndex());
            fail("exception should be thrown by KodoSM.fetchInitialField");
        } catch (org.apache.openjpa.util.UserException e) {
            // expected case
        }
        endTx(pm);
        endEm(pm);
    }

    private RuntimeTest1 getObject(OpenJPAEntityManager pm) {
//        return (RuntimeTest1) ((Collection) 
//        		pm.createNativeQuery( "stringField == "
//              +"\"TestInitialValueFetching\"",RuntimeTest1.class)
//              .getResultList()).iterator().next();

        OpenJPAQuery q = pm.createQuery("SELECT o FROM RuntimeTest1 o "
                + "WHERE o.stringField = \'TestInitialValueFetching\'");
        List l = q.getResultList();

        return (RuntimeTest1) l.iterator().next();
    }
}
