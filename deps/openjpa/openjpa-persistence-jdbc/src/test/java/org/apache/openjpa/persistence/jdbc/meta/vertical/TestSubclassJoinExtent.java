/*
 * TestSubclassJoinExtent.java
 *
 * Created on October 5, 2006, 3:41 PM
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
package org.apache.openjpa.persistence.jdbc.meta.vertical;

import java.util.*;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.Join;
import org.apache.openjpa.persistence.Extent;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestSubclassJoinExtent
    extends org.apache.openjpa.persistence.jdbc.kernel.TestSQLListenerTestCase {
   
    private String _outer = "OUTER";
    
    /** Creates a new instance of TestSubclassJoinExtent */
    public TestSubclassJoinExtent(String name) 
    {
    	super(name);
    }
    
    public boolean skipTest() {
        //FIXME
        /*
        return super.skipTest()
            || ((JDBCConfiguration) getConfiguration()).
            getDBDictionaryInstance().joinSyntax == Join.SYNTAX_TRADITIONAL;
         */
        return false;
    }
    
    public void setUpTestCase() {
        // make sure all classes are registered
        Class[] reg = new Class[]{
            Base.class, BaseSub1.class, BaseSub2.class,
            BaseSub1Sub1.class, BaseSub1Sub2.class,
        };
        
        if (((JDBCConfiguration) getConfiguration()).getDBDictionaryInstance().
                joinSyntax == Join.SYNTAX_DATABASE)
            _outer = "(+)";
        
       deleteAll(Base.class);
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);;
        Base base = new Base();
        base.setBaseField(1);
        pm.persist(base);
        
        BaseSub1 sub1 = new BaseSub1();
        sub1.setBaseField(2);
        sub1.setBaseSub1Field(3);
        pm.persist(sub1);
        
        BaseSub2 sub2 = new BaseSub2();
        sub2.setBaseField(3);
        sub2.setBaseSub2Field(4);
        pm.persist(sub2);
        
        BaseSub1Sub2 sub1sub2 = new BaseSub1Sub2();
        sub1sub2.setBaseField(4);
        sub1sub2.setBaseSub1Field(5);
        sub1sub2.setBaseSub1Sub2Field(6);
        pm.persist(sub1sub2);
        
        endTx(pm);;
        pm.close();
    }
    
    public void testBaseNoSubs()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Extent extent = pm.createExtent(Base.class, false);
        Iterator itr = extent.iterator();
        assertTrue(itr.hasNext());
        Base pc = (Base) itr.next();
        assertEquals(1, pc.getBaseField());
        assertEquals(Base.class, pc.getClass());
        assertTrue(!itr.hasNext());
        extent.closeAll();
        pm.close();
        
        assertEquals(1, sql.size());
        assertSQL(_outer);
    }
    
    public void testBaseWithSubs()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Extent extent = pm.createExtent(Base.class, true);
        Collection pcs = new TreeSet(((Extent) extent).list());
        assertEquals(4, pcs.size());
        Iterator itr = pcs.iterator();
        for (int i = 0; i < 4; i++) {
            Base pc = (Base) itr.next();
            assertEquals(i + 1, pc.getBaseField());
            switch (i) {
                case 0:
                    assertEquals(Base.class, pc.getClass());
                    break;
                case 1:
                    assertEquals(BaseSub1.class, pc.getClass());
                    break;
                case 2:
                    assertEquals(BaseSub2.class, pc.getClass());
                    break;
                case 3:
                    assertEquals(BaseSub1Sub2.class, pc.getClass());
                    break;
            }
        }
        pm.close();
        
        assertEquals(1, sql.size());
        assertSQL(_outer);
    }
    
    public void testEmptyNoSubs()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Extent extent = pm.createExtent(BaseSub1Sub1.class, false);
        Iterator itr = extent.iterator();
        assertTrue(!itr.hasNext());
        extent.closeAll();
        pm.close();
        assertNotSQL(_outer);
    }
    
    public void testEmptyWithSubs()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Extent extent = pm.createExtent(BaseSub1Sub1.class, true);
        Iterator itr = extent.iterator();
        assertTrue(!itr.hasNext());
        extent.closeAll();
        pm.close();
        assertNotSQL(_outer);
    }
    
    public void testLeafNoSubs()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Extent extent = pm.createExtent(BaseSub1Sub2.class, false);
        Iterator itr = extent.iterator();
        assertTrue(itr.hasNext());
        BaseSub1Sub2 pc = (BaseSub1Sub2) itr.next();
        assertEquals(4, pc.getBaseField());
        assertEquals(5, pc.getBaseSub1Field());
        assertEquals(6, pc.getBaseSub1Sub2Field());
        assertTrue(!itr.hasNext());
        extent.closeAll();
        pm.close();
        
        assertEquals(1, sql.size());
        assertNotSQL(_outer);
    }
    
    public void testLeafWithSubs()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Extent extent = pm.createExtent(BaseSub1Sub2.class, true);
        Iterator itr = extent.iterator();
        assertTrue(itr.hasNext());
        BaseSub1Sub2 pc = (BaseSub1Sub2) itr.next();
        assertEquals(4, pc.getBaseField());
        assertEquals(5, pc.getBaseSub1Field());
        assertEquals(6, pc.getBaseSub1Sub2Field());
        assertTrue(!itr.hasNext());
        extent.closeAll();
        pm.close();
        
        assertEquals(1, sql.size());
        assertNotSQL(_outer);
    }
    
    public void testLeafNoSubs2()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Extent extent = pm.createExtent(BaseSub2.class, false);
        Iterator itr = extent.iterator();
        assertTrue(itr.hasNext());
        BaseSub2 pc = (BaseSub2) itr.next();
        assertEquals(3, pc.getBaseField());
        assertEquals(4, pc.getBaseSub2Field());
        assertTrue(!itr.hasNext());
        extent.closeAll();
        pm.close();
        
        assertEquals(1, sql.size());
        assertNotSQL(_outer);
    }
    
    public void testLeafWithSubs2()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Extent extent = pm.createExtent(BaseSub2.class, true);
        Iterator itr = extent.iterator();
        assertTrue(itr.hasNext());
        BaseSub2 pc = (BaseSub2) itr.next();
        assertEquals(3, pc.getBaseField());
        assertEquals(4, pc.getBaseSub2Field());
        assertTrue(!itr.hasNext());
        extent.closeAll();
        pm.close();
        
        assertEquals(1, sql.size());
        assertNotSQL(_outer);
    }
    
    public void testMidNoSubs()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Extent extent = pm.createExtent(BaseSub1.class, false);
        Iterator itr = extent.iterator();
        assertTrue(itr.hasNext());
        BaseSub1 pc = (BaseSub1) itr.next();
        assertEquals(2, pc.getBaseField());
        assertEquals(3, pc.getBaseSub1Field());
        assertEquals(BaseSub1.class, pc.getClass());
        assertTrue(!itr.hasNext());
        extent.closeAll();
        pm.close();
        
        assertEquals(1, sql.size());
        assertSQL(_outer);
    }
    
    public void testMidWithSubs()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Extent extent = pm.createExtent(BaseSub1.class, true);
        Collection pcs = new TreeSet(((Extent) extent).list());
        assertEquals(2, pcs.size());
        Iterator itr = pcs.iterator();
        BaseSub1 pc = (BaseSub1) itr.next();
        assertEquals(2, pc.getBaseField());
        assertEquals(3, pc.getBaseSub1Field());
        assertEquals(BaseSub1.class, pc.getClass());
        pc = (BaseSub1) itr.next();
        assertEquals(4, pc.getBaseField());
        assertEquals(5, pc.getBaseSub1Field());
        assertEquals(BaseSub1Sub2.class, pc.getClass());
        assertEquals(6, ((BaseSub1Sub2) pc).getBaseSub1Sub2Field());
        pm.close();
        
        assertEquals(1, sql.size());
        assertSQL(_outer);
    }
    
}
