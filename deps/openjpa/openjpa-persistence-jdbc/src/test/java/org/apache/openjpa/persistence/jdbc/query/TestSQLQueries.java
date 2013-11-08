/*
 * TestSQLQueries.java
 *
 * Created on October 5, 2006, 4:59 PM
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
package org.apache.openjpa.persistence.jdbc.query;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.MappingRepository;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.persistence.OpenJPAQuery;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;


public class TestSQLQueries
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
     
    
    /** Creates a new instance of TestSQLQueries */
    public TestSQLQueries(String name) 
    {
    	super(name);
    }
    
    public TestSQLQueries() 
    {}
    
    private String _tableName = null;
    private String _fullTableName = null;
    private String _pkColName = null;
    private String _intColName = null;
    private String _stringColName = null;
    private String _relColName = null;
    
    public void setUp() {
       deleteAll(RuntimeTest1.class);
        
        RuntimeTest1 pc1 = new RuntimeTest1("1", 1);
        RuntimeTest1 pc2 = new RuntimeTest1("2", 2);
        pc1.setSelfOneOne(pc2);
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);;
        pm.persist(pc1);
        endTx(pm);;
        
        JDBCConfiguration conf =
            (JDBCConfiguration) ((OpenJPAEntityManagerFactorySPI) pm)
            .getConfiguration();
        DBDictionary dict = conf.getDBDictionaryInstance();
        MappingRepository repos = conf.getMappingRepositoryInstance();
        ClassMapping mapping = repos.getMapping(RuntimeTest1.class,
                pm.getClassLoader(), true);
        
        _tableName = mapping.getTable().getName();
        _fullTableName = dict.getFullName(mapping.getTable(), false);
        _pkColName = mapping.getTable().getPrimaryKey().
                getColumns()[0].getName();
        _intColName = mapping.getFieldMapping("intField").
                getColumns()[0].getName();
        _stringColName = mapping.getFieldMapping("stringField").
                getColumns()[0].getName();
        _relColName = mapping.getFieldMapping("selfOneOne").
                getColumns()[0].getName();
        
        pm.close();
    }
    
    public void testStarQuery() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        OpenJPAQuery q = pm.createQuery("javax.jdo.query.SQL",
                "select * from " + _fullTableName + " order by " + _intColName);
        q.setResultClass(RuntimeTest1.class);
        Iterator itr = ((Collection) q.getCandidateCollection()).iterator();
        assertTrue(itr.hasNext());
        assertEquals("1", ((RuntimeTest1) itr.next()).getStringField());
        assertTrue(itr.hasNext());
        assertEquals("2", ((RuntimeTest1) itr.next()).getStringField());
        assertFalse(itr.hasNext());
        q.closeAll();
        pm.close();
    }
    
    public void testCompiledQuery()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        OpenJPAQuery q = pm.createQuery("javax.jdo.query.SQL",
                "select * from " + _fullTableName + " order by " + _intColName);
        q.setResultClass(RuntimeTest1.class);
        
        //FIXME jthomas
        //q = pm.createQuery(roundtrips(q, false));
        Iterator itr = ((Collection) q.getCandidateCollection()).iterator();
        assertTrue(itr.hasNext());
        assertEquals("1", ((RuntimeTest1) itr.next()).getStringField());
        assertTrue(itr.hasNext());
        assertEquals("2", ((RuntimeTest1) itr.next()).getStringField());
        assertFalse(itr.hasNext());
        q.closeAll();
        pm.close();
    }
    
    public void testCompiledLanguageQuery()
    throws Exception {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        OpenJPAQuery q = pm.createQuery("javax.jdo.query.SQL",
                "select * from " + _fullTableName + " order by " + _intColName);
        q.setResultClass(RuntimeTest1.class);
        
        //FIXME jthomas
        //q = pm.createQuery("javax.jdo.query.SQL", roundtrips(q, false));
        Iterator itr = ((Collection) q.getCandidateCollection()).iterator();
        assertTrue(itr.hasNext());
        assertEquals("1", ((RuntimeTest1) itr.next()).getStringField());
        assertTrue(itr.hasNext());
        assertEquals("2", ((RuntimeTest1) itr.next()).getStringField());
        assertFalse(itr.hasNext());
        q.closeAll();
        pm.close();
    }
    
    public void testTableStarQuery() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        OpenJPAQuery q = pm.createQuery("javax.jdo.query.SQL",
                "select " + _tableName + ".* from " + _fullTableName
                + " order by " + _intColName);
        q.setResultClass(RuntimeTest1.class);
        Iterator itr = ((Collection) q.getCandidateCollection()).iterator();
        assertTrue(itr.hasNext());
        assertEquals("1", ((RuntimeTest1) itr.next()).getStringField());
        assertTrue(itr.hasNext());
        assertEquals("2", ((RuntimeTest1) itr.next()).getStringField());
        assertFalse(itr.hasNext());
        q.closeAll();
        pm.close();
    }
    
    public void testColumnQuery() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        OpenJPAQuery q = pm.createQuery("javax.jdo.query.SQL",
                "select " + _pkColName + ", " + _intColName + ", "
                + _stringColName + " from " + _fullTableName + " order by "
                + _intColName);
        q.setResultClass(RuntimeTest1.class);
        Iterator itr = ((Collection) q.getCandidateCollection()).iterator();
        assertTrue(itr.hasNext());
        assertEquals("1", ((RuntimeTest1) itr.next()).getStringField());
        assertTrue(itr.hasNext());
        assertEquals("2", ((RuntimeTest1) itr.next()).getStringField());
        assertFalse(itr.hasNext());
        q.closeAll();
        pm.close();
    }
    
    public void testJoinQuery() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        OpenJPAQuery q = pm.createQuery("javax.jdo.query.SQL",
                "select t0.* from " + _fullTableName + " t0, "
                + _fullTableName + " t1 where t0." + _relColName + " = t1."
                + _pkColName + " and t1." + _intColName + " = 2");
        q.setResultClass(RuntimeTest1.class);
        Iterator itr = ((Collection) q.getCandidateCollection()).iterator();
        assertTrue(itr.hasNext());
        assertEquals("1", ((RuntimeTest1) itr.next()).getStringField());
        assertFalse(itr.hasNext());
        q.closeAll();
        pm.close();
    }
    
    public void testParameters() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        OpenJPAQuery q = pm.createQuery("javax.jdo.query.SQL",
                "select * from " + _fullTableName + " where 'foo' = ? and "
                + _intColName + " = ?");
        q.setResultClass(RuntimeTest1.class);
        //FIXME jthomas
        /*Iterator itr = ((Collection) q.execute("foo", new Integer(2))).
                iterator();
        assertTrue(itr.hasNext());
        assertEquals("2", ((RuntimeTest1) itr.next()).getStringField());
        assertFalse(itr.hasNext());
         */
        q.closeAll();
        
        Map params = new HashMap();
        params.put(new Integer(1), "foo");
        params.put(new Integer(2), new Integer(2));
        //FIXME jthomas
        /*itr = ((Collection) q.executeWithMap(params)).iterator();
        assertTrue(itr.hasNext());
        assertEquals("2", ((RuntimeTest1) itr.next()).getStringField());
        assertFalse(itr.hasNext());
         */
        q.closeAll();
        pm.close();
    }
    
    public void testOnlySelectedFieldsLoaded() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        OpenJPAQuery q = pm.createQuery("javax.jdo.query.SQL",
                "select " + _pkColName + ", " + _intColName + ", "
                + _stringColName + " from " + _fullTableName + " order by "
                + _intColName);
        q.setResultClass(RuntimeTest1.class);
        Iterator itr = ((Collection) q.getCandidateCollection()).iterator();
        assertTrue(itr.hasNext());
        RuntimeTest1 pc = (RuntimeTest1) itr.next();
        OpenJPAStateManager sm = getStateManager(pc, pm);
        assertTrue(sm.getLoaded().get(sm.getMetaData().
                getField("intField").getIndex()));
        assertTrue(sm.getLoaded().get(sm.getMetaData().
                getField("stringField").getIndex()));
        assertFalse(sm.getLoaded().get(sm.getMetaData().
                getField("longField").getIndex()));
        assertEquals("1", pc.getStringField());
        assertFalse(sm.getLoaded().get(sm.getMetaData().
                getField("longField").getIndex()));
        q.closeAll();
        pm.close();
    }
    
    public void testSingleColumnClasslessQuery() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        OpenJPAQuery q = pm.createQuery("javax.jdo.query.SQL",
                "select " + _stringColName + " from " + _fullTableName
                + " order by " + _stringColName);
        Iterator itr = ((Collection) q.getCandidateCollection()).iterator();
        assertTrue(itr.hasNext());
        assertEquals("1", itr.next());
        assertTrue(itr.hasNext());
        assertEquals("2", itr.next());
        assertFalse(itr.hasNext());
        q.closeAll();
        
        q.setResultClass(Object[].class);
        itr = ((Collection) q.getCandidateCollection()).iterator();
        assertTrue(itr.hasNext());
        Object[] vals = (Object[]) itr.next();
        assertEquals(1, vals.length);
        assertEquals("1", vals[0]);
        assertTrue(itr.hasNext());
        vals = (Object[]) itr.next();
        assertEquals(1, vals.length);
        assertEquals("2", vals[0]);
        assertFalse(itr.hasNext());
        q.closeAll();
        
        pm.close();
    }
    
    public void testMultiColumnClasslessQuery() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        OpenJPAQuery q = pm.createQuery("javax.jdo.query.SQL",
                "select " + _intColName + ", " + _stringColName + " from "
                + _fullTableName + " order by " + _stringColName);
        Iterator itr = ((Collection) q.getCandidateCollection()).iterator();
        assertTrue(itr.hasNext());
        Object[] vals = (Object[]) itr.next();
        assertEquals(2, vals.length);
        assertEquals(1, ((Number) vals[0]).intValue());
        assertEquals("1", vals[1]);
        assertTrue(itr.hasNext());
        vals = (Object[]) itr.next();
        assertEquals(2, vals.length);
        assertEquals(2, ((Number) vals[0]).intValue());
        assertEquals("2", vals[1]);
        assertFalse(itr.hasNext());
        q.closeAll();
        pm.close();
    }
    
    public void testResultClass() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        OpenJPAQuery q = pm.createQuery("javax.jdo.query.SQL",
                "select * from " + _fullTableName + " order by " + _intColName);
        q.setResultClass(RuntimeTest1.class);
        q.setResultClass(Holder.class);
        Iterator itr = ((Collection) q.getCandidateCollection()).iterator();
        assertTrue(itr.hasNext());
        Holder holder = (Holder) itr.next();
        assertEquals(0, holder.I);
        assertNull(holder.S);
        assertNotNull(holder.pc);
        assertEquals("1", holder.pc.getStringField());
        assertTrue(itr.hasNext());
        holder = (Holder) itr.next();
        assertEquals(0, holder.I);
        assertNull(holder.S);
        assertNotNull(holder.pc);
        assertEquals("2", holder.pc.getStringField());
        assertFalse(itr.hasNext());
        q.closeAll();
        pm.close();
    }
    
    public void testClasslessProjection() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        OpenJPAQuery q = pm.createQuery("javax.jdo.query.SQL",
                "select " + _intColName + " as I, " + _stringColName
                + " as S from " + _fullTableName + " order by " + _intColName);
        q.setResultClass(Holder.class);
        Iterator itr = ((Collection) q.getCandidateCollection()).iterator();
        assertTrue(itr.hasNext());
        Holder holder = (Holder) itr.next();
        assertNull(holder.pc);
        assertEquals(1, holder.I);
        assertEquals("1", holder.S);
        assertTrue(itr.hasNext());
        holder = (Holder) itr.next();
        assertNull(holder.pc);
        assertEquals(2, holder.I);
        assertEquals("2", holder.S);
        assertFalse(itr.hasNext());
        q.closeAll();
        pm.close();
    }
    
    /**
     * Manual test to see if a relation will be eagerly loaded when SQL
     * containing enough information is run. This is not run as part of
     * the unit tests since we don't know if this behavior should be
     * really expected to work or not.
     */
    public void relationLoadedTest() {
       deleteAll(AttachD.class);
       deleteAll(AttachA.class);
        
        AttachD d = new AttachD();
        AttachA a = new AttachA();
        d.setA(a);
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        pm.getTransaction().begin();
        pm.persist(d);
        pm.getTransaction().commit();
        pm.close();
        
        JDBCConfiguration conf =
            (JDBCConfiguration) ((OpenJPAEntityManagerFactorySPI) pm)
            .getConfiguration();
        DBDictionary dict = conf.getDBDictionaryInstance();
        MappingRepository repos = conf.getMappingRepositoryInstance();
        
        ClassMapping mappingA = repos.getMapping(AttachD.class,
                pm.getClassLoader(), true);
        String tableNameA = mappingA.getTable().getName();
        String fullTableNameA = dict.getFullName(mappingA.getTable(), false);
        String relColNameA = mappingA.getFieldMapping("a").
                getColumns()[0].getName();
        
        ClassMapping mappingD = repos.getMapping(AttachA.class,
                pm.getClassLoader(), true);
        String tableNameD = mappingD.getTable().getName();
        String fullTableNameD = dict.getFullName(mappingD.getTable(), false);
        String pkColNameD = mappingD.getTable().getPrimaryKey().
                getColumns()[0].getName();
        
        pm = (OpenJPAEntityManager)currentEntityManager();
        OpenJPAQuery q = pm.createQuery("javax.jdo.query.SQL",
                "select t0.*, t1.* from "
                + fullTableNameA + " t0, "
                + fullTableNameD + " t1 "
                + "where t0." + relColNameA + " = t1." + pkColNameD);
        
        // even the exact same SQL that Kodo generates will not
        // eagerly load the relation
        /*
          q = pm.newQuery ("javax.jdo.query.SQL",
              "SELECT t1.ID, t1.TYP, t1.VERSN, t1.ADBL, t1.AINT, "
              + "t1.ASTR, t1.BDBL, t1.BINT, t1.BSTR, t1.CDBL, t1.CINT, "
              + "t1.CSTR, t0.DDBL, t0.DINT, t0.DSTR "
              + "FROM ATTACHD t0 LEFT OUTER JOIN ATTACHA t1 ON t0.A = t1.ID");
         */
        
        q.setResultClass(AttachD.class);
        Iterator itr = ((Collection) q.getCandidateCollection()).iterator();
        assertTrue(itr.hasNext());
        
        d = (AttachD) itr.next();
        // d.getDstr ();
        
        OpenJPAStateManager sm = getStateManager(d, pm);
        assertTrue(sm.getLoaded().
                get(sm.getMetaData().getField("a").getIndex()));
        assertNotNull(d.getA());
        assertFalse(itr.hasNext());
        
        q.closeAll();
        pm.close();
    }
    
    public static class Holder {
        
        public RuntimeTest1 pc;
        public int I;
        public String S;
        
        public void setRuntimeTest1(RuntimeTest1 pc) {
            this.pc = pc;
        }
    }
    
    public static void main(String[] args)
    throws Exception {
        // main ();
        
        new TestSQLQueries().relationLoadedTest();
    }
    
    private static Object roundtrips(Object orig, boolean validateEquality)
    throws IOException, ClassNotFoundException {
        assertNotNull(orig);
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(orig);
        ByteArrayInputStream bin = new ByteArrayInputStream(
                bout.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bin);
        Object result = in.readObject();
        
        if (validateEquality) {
            assertEquals(orig.hashCode(), result.hashCode());
            assertEquals(orig, result);
        }
        
        return result;
    }
    
}
