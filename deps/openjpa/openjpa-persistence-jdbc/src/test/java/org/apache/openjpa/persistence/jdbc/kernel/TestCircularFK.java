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
package org.apache.openjpa.persistence.jdbc.kernel;


import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestCircularFK extends BaseJDBCTest{
    
    private Object _oid1 = null;
    private Object _oid2 = null;
    
    public TestCircularFK(String name) {
        super(name);
    }
    
    /** Creates a new instance of TestCircularFK */
    public TestCircularFK() {
    }
    
    public void setUp() {
        OpenJPAEntityManager pm = getPM();
        
        startTx(pm);
       deleteAll(CircularFKPC.class,pm);
       deleteAll(CircularFKPC2.class,pm);
        endTx(pm);
        endEm(pm);
        
        
        CircularFKPC pc1 = new CircularFKPC();
        pc1.setStringField("pc1");
        CircularFKPC2 pc2 = new CircularFKPC2();
        pc2.setStringField("pc2");
        pc1.setFKField(pc2);
        pc2.setFKField(pc1);
        
        pm = getPM();
        startTx(pm);
        pm.persist(pc1);
        endTx(pm);
        _oid1 = pm.getObjectId(pc1);
        _oid2 = pm.getObjectId(pc2);
        endEm(pm);
    }
    
    public void testInsert() {
        // inserts are tested in setup; just make sure the inserts
        // actually worked
        OpenJPAEntityManager pm = getPM();
        CircularFKPC pc1 = (CircularFKPC) pm.find(CircularFKPC.class,_oid1);
        CircularFKPC2 pc2 = (CircularFKPC2) pm.find(CircularFKPC2.class,_oid2);
        assertNotNull(pc1);
        assertNotNull(pc2);
        assertEquals("pc1", pc1.getStringField());
        assertEquals("pc2", pc2.getStringField());
        assertEquals(pc2, pc1.getFKField());
        assertEquals(pc1, pc2.getFKField());
        pm.close();
    }
    
    public void testDelete() {
        OpenJPAEntityManager pm = getPM();
        CircularFKPC pc1 = (CircularFKPC) pm.find(CircularFKPC.class,_oid1);
        CircularFKPC2 pc2 = (CircularFKPC2) pm.find(CircularFKPC2.class,_oid2);
        startTx(pm);
        pm.remove(pc1);
        pm.remove(pc2);
        endTx(pm);
        endEm(pm);
        
        pm = getPM();
        try {
            pm.find(CircularFKPC.class,_oid1);
            fail("PC1 still exists!");
        } catch (Exception jdse) {
        }
        try {
            pm.find(CircularFKPC2.class,_oid2);
            fail("PC2 still exists!");
        } catch (Exception jdse) {
        }
        endEm(pm);
    }
    
    public void testUpdate() {
        OpenJPAEntityManager pm = getPM();
        CircularFKPC pc1 = (CircularFKPC) pm.find(CircularFKPC.class,_oid1);
        CircularFKPC2 pc2 = (CircularFKPC2) pm.find(CircularFKPC2.class,_oid2);
        
        startTx(pm);;
        CircularFKPC2 pc3 = new CircularFKPC2();
        pc3.setStringField("pc3");
        pc3.setFKField(pc1);
        pc1.setFKField(pc3);
        pc2.setFKField(null);
        endTx(pm);;
        pm.close();
        
        pm = getPM();
        pc1 = (CircularFKPC) pm.find(CircularFKPC.class,_oid1);
        pc2 = (CircularFKPC2) pm.find(CircularFKPC2.class,_oid2);
        assertNotNull(pc1);
        assertNotNull(pc2);
        assertEquals("pc1", pc1.getStringField());
        assertEquals("pc2", pc2.getStringField());
        assertNotNull(pc1.getFKField());
        assertEquals("pc3", pc1.getFKField().getStringField());
        assertEquals(pc1, pc1.getFKField().getFKField());
        assertNull(pc2.getFKField());
    }
}
