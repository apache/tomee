/*
 * TestJoinSubclasses.java
 *
 * Created on October 5, 2006, 2:17 PM
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
import org.apache.openjpa.util.Id;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestJoinSubclasses
    extends org.apache.openjpa.persistence.jdbc.kernel.TestSQLListenerTestCase {
     
    /** Creates a new instance of TestJoinSubclasses */
    public TestJoinSubclasses(String name) 
    {
    	super(name);
    }
    
    
    private String _outer = "OUTER";
    private Object _base = null;
    private Object _baseSubFlat = null;
    
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
        if (((JDBCConfiguration) getConfiguration()).getDBDictionaryInstance().
                joinSyntax == Join.SYNTAX_DATABASE)
            _outer = "(+)";
        
       deleteAll(JoinSubclassBase.class);
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);;
        JoinSubclassBase base = new JoinSubclassBase();
        base.setBaseField(1);
        pm.persist(base);
        
        JoinSubclassBaseSubVert sub1 = new JoinSubclassBaseSubVert();
        sub1.setBaseField(2);
        sub1.setBaseSubVertField(3);
        pm.persist(sub1);
        
        JoinSubclassBaseSubFlat sub2 = new JoinSubclassBaseSubFlat();
        sub2.setBaseField(3);
        sub2.setBaseSubVertField(4);
        sub2.setBaseSubFlatField(5);
        pm.persist(sub2);
        
        endTx(pm);;
        _base = pm.getObjectId(base);
        _baseSubFlat = pm.getObjectId(sub2);
        pm.close();
    }
    
    public void testBaseExtentNoSubs()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Extent extent = pm.createExtent(JoinSubclassBase.class, false);
        Iterator itr = extent.iterator();
        assertTrue(itr.hasNext());
        JoinSubclassBase pc = (JoinSubclassBase) itr.next();
        assertEquals(1, pc.getBaseField());
        assertEquals(JoinSubclassBase.class, pc.getClass());
        assertTrue(!itr.hasNext());
        extent.closeAll();
        pm.close();
        
        assertEquals(sql.toString(), 1, sql.size());
        assertNotSQL(_outer);
    }
    
    public void testBaseExtentWithSubs()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Extent extent = pm.createExtent(JoinSubclassBase.class, true);
        Collection pcs = new TreeSet(((Extent) extent).list());
        assertEquals(3, pcs.size());
        Iterator itr = pcs.iterator();
        for (int i = 0; i < 2; i++) {
            JoinSubclassBase pc = (JoinSubclassBase) itr.next();
            assertEquals(i + 1, pc.getBaseField());
            switch (i) {
                case 0:
                    assertEquals(JoinSubclassBase.class, pc.getClass());
                    break;
                case 1:
                    assertEquals(JoinSubclassBaseSubVert.class, pc.getClass());
                    assertEquals(i + 2, ((JoinSubclassBaseSubVert) pc).
                            getBaseSubVertField());
                    break;
                case 2:
                    assertEquals(JoinSubclassBaseSubFlat.class, pc.getClass());
                    assertEquals(i + 2, ((JoinSubclassBaseSubFlat) pc).
                            getBaseSubVertField());
                    assertEquals(i + 3, ((JoinSubclassBaseSubFlat) pc).
                            getBaseSubFlatField());
                    break;
            }
        }
        pm.close();
        
        assertEquals(sql.toString(), 1, sql.size());
        assertSQL(_outer);
    }
    
    public void testLeafExtent()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Extent extent = pm.createExtent(JoinSubclassBaseSubFlat.class, false);
        Iterator itr = extent.iterator();
        assertTrue(itr.hasNext());
        JoinSubclassBaseSubFlat pc = (JoinSubclassBaseSubFlat) itr.next();
        assertEquals(3, pc.getBaseField());
        assertEquals(4, pc.getBaseSubVertField());
        assertEquals(5, pc.getBaseSubFlatField());
        assertTrue(!itr.hasNext());
        extent.closeAll();
        pm.close();
        
        assertEquals(sql.toString(), 1, sql.size());
        assertNotSQL(_outer);
    }
    
    public void testBaseGetById()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        JoinSubclassBase pc = (JoinSubclassBase) pm.getObjectId(_base);
        assertEquals(1, pc.getBaseField());
        assertEquals(JoinSubclassBase.class, pc.getClass());
        pm.close();
        
        assertEquals(sql.toString(), 1, sql.size());
        assertNotSQL(_outer);
        sql.clear();
        
        // should outer join with non-exact id
        Id oid = new Id(JoinSubclassBase.class, ((Id) _base).getId());
        pm = (OpenJPAEntityManager)currentEntityManager();
        pc = (JoinSubclassBase) pm.getObjectId(oid);
        assertEquals(1, pc.getBaseField());
        pm.close();
        
        assertEquals(sql.toString(), 1, sql.size());
        assertSQL(_outer);
    }
    
    public void testLeafGetById()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        JoinSubclassBaseSubFlat pc = (JoinSubclassBaseSubFlat)
        pm.getObjectId(_baseSubFlat);
        assertEquals(3, pc.getBaseField());
        assertEquals(4, pc.getBaseSubVertField());
        assertEquals(5, pc.getBaseSubFlatField());
        pm.close();
        
        assertEquals(sql.toString(), 1, sql.size());
        assertNotSQL(_outer);
        sql.clear();
        
        // should outer join with non-exact id
        Id oid = new Id(JoinSubclassBase.class, ((Id) _baseSubFlat).getId());
        pm = (OpenJPAEntityManager)currentEntityManager();
        pc = (JoinSubclassBaseSubFlat) pm.getObjectId(oid);
        assertEquals(3, pc.getBaseField());
        assertEquals(4, pc.getBaseSubVertField());
        assertEquals(5, pc.getBaseSubFlatField());
        pm.close();
        
        assertEquals(sql.toString(), 1, sql.size());
        assertSQL(_outer);
    }
    
}
