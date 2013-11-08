/*
 * TestSubclassJoinGetObjectById.java
 *
 * Created on October 5, 2006, 3:55 PM
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

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.Join;
import org.apache.openjpa.util.Id;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestSubclassJoinGetObjectById
    extends org.apache.openjpa.persistence.jdbc.kernel.TestSQLListenerTestCase {
    

    private String _outer = "OUTER";
    
    /** Creates a new instance of TestSubclassJoinGetObjectById */
    public TestSubclassJoinGetObjectById(String name) 
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
    }
    
    public void testBase()
    throws Exception {
        Base pc = new Base();
        pc.setBaseField(1);
        Object oid = persist(pc);
        sql.clear();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        
        pc = (Base) pm.getObjectId(oid);
        assertEquals(1, pc.getBaseField());
        assertEquals(Base.class, pc.getClass());
        pm.close();
        
        assertEquals(1, sql.size());
        assertNotSQL(_outer);
        sql.clear();
        
        // should outer join with non-exact oid
        oid = new Id(Base.class, ((Id) oid).getId());
        pm = (OpenJPAEntityManager)currentEntityManager();
        pc = (Base) pm.getObjectId(oid);
        assertEquals(1, pc.getBaseField());
        assertEquals(Base.class, pc.getClass());
        pm.close();
        
        assertEquals(1, sql.size());
        assertSQL(_outer);
    }
    
    public void testBadId()
    throws Exception {
        Base pc = new Base();
        pc.setBaseField(1);
        Object oid = persist(pc);
        sql.clear();
        
        Id id = new Id(Base.class, -1);
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        try {
            pm.getObjectId(id);
            fail("No exception on bad id.");
        } catch (Exception jonfe) {
        }
        pm.close();
    }
    
    public void testLeaf()
    throws Exception {
        BaseSub1Sub2 pc = new BaseSub1Sub2();
        pc.setBaseField(4);
        pc.setBaseSub1Field(5);
        pc.setBaseSub1Sub2Field(6);
        Object oid = persist(pc);
        sql.clear();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pc = (BaseSub1Sub2) pm.getObjectId(oid);
        assertEquals(4, pc.getBaseField());
        assertEquals(5, pc.getBaseSub1Field());
        assertEquals(6, pc.getBaseSub1Sub2Field());
        pm.close();
        
        assertEquals(1, sql.size());
        assertNotSQL(_outer);
        sql.clear();
        
        // should outer join with inexact oid
        oid = new Id(Base.class, ((Id) oid).getId());
        pm = (OpenJPAEntityManager)currentEntityManager();
        pc = (BaseSub1Sub2) pm.getObjectId(oid);
        assertEquals(4, pc.getBaseField());
        assertEquals(5, pc.getBaseSub1Field());
        assertEquals(6, pc.getBaseSub1Sub2Field());
        pm.close();
        
        assertEquals(1, sql.size());
        assertSQL(_outer);
    }
    
    public void testLeaf2()
    throws Exception {
        BaseSub2 pc = new BaseSub2();
        pc.setBaseField(3);
        pc.setBaseSub2Field(4);
        Object oid = persist(pc);
        sql.clear();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pc = (BaseSub2) pm.getObjectId(oid);
        assertEquals(3, pc.getBaseField());
        assertEquals(4, pc.getBaseSub2Field());
        assertEquals(BaseSub2.class, pc.getClass());
        pm.close();
        
        assertEquals(1, sql.size());
        assertNotSQL(_outer);
        sql.clear();
        
        // should outer join with inexact oid
        oid = new Id(Base.class, ((Id) oid).getId());
        pm = (OpenJPAEntityManager)currentEntityManager();
        pc = (BaseSub2) pm.getObjectId(oid);
        assertEquals(3, pc.getBaseField());
        assertEquals(4, pc.getBaseSub2Field());
        assertEquals(BaseSub2.class, pc.getClass());
        pm.close();
        
        assertEquals(1, sql.size());
        assertSQL(_outer);
    }
    
    public void testMid()
    throws Exception {
        BaseSub1 pc = new BaseSub1();
        pc.setBaseField(2);
        pc.setBaseSub1Field(3);
        Object oid = persist(pc);
        sql.clear();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pc = (BaseSub1) pm.getObjectId(oid);
        assertEquals(2, pc.getBaseField());
        assertEquals(3, pc.getBaseSub1Field());
        assertEquals(BaseSub1.class, pc.getClass());
        pm.close();
        
        assertEquals(1, sql.size());
        assertNotSQL(_outer);
        sql.clear();
        
        // should outer join with inexact oid
        oid = new Id(Base.class, ((Id) oid).getId());
        pm = (OpenJPAEntityManager)currentEntityManager();
        pc = (BaseSub1) pm.getObjectId(oid);
        assertEquals(2, pc.getBaseField());
        assertEquals(3, pc.getBaseSub1Field());
        assertEquals(BaseSub1.class, pc.getClass());
        pm.close();
        
        assertEquals(1, sql.size());
        assertSQL(_outer);
    }
    
    
    
}
