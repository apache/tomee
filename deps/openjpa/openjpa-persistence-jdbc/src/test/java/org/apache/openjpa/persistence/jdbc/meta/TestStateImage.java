/*
 * TestStateImage.java
 *
 * Created on October 4, 2006, 3:09 PM
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
package org.apache.openjpa.persistence.jdbc.meta;

import java.util.*;

import org.apache.openjpa.persistence.OpenJPAQuery;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestStateImage
    extends org.apache.openjpa.persistence.jdbc.kernel.TestSQLListenerTestCase {
    private Object _oid = null;
    
    /** Creates a new instance of TestStateImage */
    public TestStateImage() {
    }
    public TestStateImage(String test) {
        super(test);
    }
    
    
    public void setUpTestCase() {
       deleteAll(StateImagePC2.class);
       deleteAll(StateImagePC3.class);
        
        StateImagePC2 pc = new StateImagePC2();
        pc.setStringField("string1");
        pc.setIntField(1);
        StateImagePC2 pc2 = new StateImagePC2();
        pc2.setStringField("string2");
        pc2.setIntField(2);
        pc.setStateImage(pc2);
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pm.getTransaction().begin();
        pm.persist(pc);
        _oid = pm.getObjectId(pc);
        pm.getTransaction().commit();
        pm.close();
    }
    
    public void testOptLock() {
        OpenJPAEntityManager pm1 = getEm(true, true);
        OpenJPAEntityManager pm2 = getEm(true, true);
        
        pm1.getTransaction().begin();
        pm2.getTransaction().begin();
        StateImagePC2 pc1 = (StateImagePC2) pm1.getObjectId(_oid);
        StateImagePC2 pc2 = (StateImagePC2) pm2.getObjectId(_oid);
        
        pc1.setIntField(3);
        pc1.setStateImage(null);
        
        pc2.setIntField(4);
        
        pm1.getTransaction().commit();
        try {
            pm2.getTransaction().commit();
            fail("Should have caused OL exception.");
        } catch (Exception jfe) {
            pm2.getTransaction().begin();
            pm2.refresh(pc2);
            pc2.setIntField(4);
            pm2.getTransaction().commit();
        }
        
        // make sure the next transaction works too
        pm2.getTransaction().begin();
        pc2.setIntField(5);
        pm2.getTransaction().commit();
        
        pm1.getTransaction().begin();
        pm1.refresh(pc1);
        pc1.setIntField(6);
        
        pm2.getTransaction().begin();
        pc2.setIntField(7);
        
        pm1.getTransaction().commit();
        try {
            pm2.getTransaction().commit();
            fail("Should have caused OL exception.");
        } catch (Exception jfe) {
            pm2.getTransaction().begin();
            pm2.refresh(pc2);
            pc2.setIntField(7);
            pm2.getTransaction().commit();
        }
        pm1.close();
        pm2.close();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        StateImagePC2 pc = (StateImagePC2) pm.getObjectId(_oid);
        assertNull(pc.getStateImage());
        assertEquals(7, pc.getIntField());
        pm.close();
    }
    
    /**
     * This currently isn't working: state-image locking will not
     * detect when someone else updated the row before deleting.
     */
    public void NOTWORKINGtestOptLockWithDelete() {
        OpenJPAEntityManager pm1 = getEm(true, true);
        StateImagePC2 pc1 = (StateImagePC2) pm1.getObjectId(_oid);
        
        OpenJPAEntityManager pm2 = getEm(true, true);
        StateImagePC2 pc2 = (StateImagePC2) pm2.getObjectId(_oid);
        
        pm1.getTransaction().begin();
        pc1.setIntField(3);
        
        pm2.getTransaction().begin();
        pm2.remove(pc2);
        
        pm1.getTransaction().commit();
        try {
            pm2.getTransaction().commit();
            fail("Should have caused OL exception.");
        } catch (Exception jfe) {
            pm2.getTransaction().begin();
            pm2.refresh(pc2);
            pm2.remove(pc2);
            pm2.getTransaction().commit();
        }
    }
    
    public void testOptLockOnVerticalClass() {
        OpenJPAEntityManager pm1 = getEm(true, true);
        OpenJPAEntityManager pm2 = getEm(true, true);
        
        // have to load via query or extent where we're selecting the vertical
        // field in the initial SELECT
        OpenJPAQuery q1 = pm1.createNativeQuery("",StateImagePC2.class);
        //FIXME  jthomas
        //q1.setOrdering("intField ascending");
        StateImagePC2 pc1 =
            (StateImagePC2) ((Collection) q1.getCandidateCollection()).
            iterator().next();
        q1.closeAll();
        
        OpenJPAQuery q2 = pm2.createNativeQuery("",StateImagePC2.class);
        //FIXME jthomas
        //q2.setOrdering("intField ascending");
        StateImagePC2 pc2 =
            (StateImagePC2) ((Collection) q2.getCandidateCollection()).
            iterator().next();
        q2.closeAll();
        
        pm1.getTransaction().begin();
        pc1.setStringField("changed1");
        pc1.setStateImage(null);
        
        pm2.getTransaction().begin();
        pc2.setStringField("changed2");
        
        pm1.getTransaction().commit();
        
        try {
            pm2.getTransaction().commit();
            fail("Should have caused OL exception.");
        } catch (Exception jfe) {
            pm2.getTransaction().begin();
            pm2.refresh(pc2);
            pc2.setStringField("changed2");
            pm2.getTransaction().commit();
        }
        pm1.close();
        pm2.close();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        StateImagePC2 pc = (StateImagePC2) pm.getObjectId(_oid);
        assertNull(pc.getStateImage());
        assertEquals("changed2", pc.getStringField());
        pm.close();
    }
    
    public void testLockGroup()
    throws Exception {
        OpenJPAEntityManager pm = getEm(true, true);
        pm.getTransaction().begin();
        StateImagePC3 pc = new StateImagePC3();
        pc.setLockField(4);
        pc.setNoLockField(6);
        pm.persist(pc);
        pm.getTransaction().commit();
        
        pm.getTransaction().begin();
        pc.setLockField(6);
        sql.clear();
        pm.getTransaction().commit();
        assertNotSQL("* WHERE * NOLOCK*");
        
        pm.close();
    }
    
    public static void main(String[] args) {
        
        //FIXME
        //main(TestStateImage.class);
    }
    private OpenJPAEntityManager getEm(boolean optimistic,boolean retainValues){
        OpenJPAEntityManager em = currentEntityManager();
        em.setNontransactionalRead(true);
        em.setRetainState(retainValues);
        em.setOptimistic(optimistic);
        return em;
    }
}
