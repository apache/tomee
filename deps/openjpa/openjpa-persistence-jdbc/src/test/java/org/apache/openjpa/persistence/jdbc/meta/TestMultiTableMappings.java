/*
 * TestMultiTableMappings.java
 *
 * Created on October 4, 2006, 4:09 PM
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
import javax.persistence.EntityTransaction;

import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.jdbc.JDBCFetchPlan;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestMultiTableMappings
    extends org.apache.openjpa.persistence.jdbc.kernel.TestSQLListenerTestCase {
    

    private Object aoid;
    private Object boid;
    private Object coid;
    private Object doid;
    
    public TestMultiTableMappings(String name) {
        super(name);
    }
    
    /** Creates a new instance of TestMultiTableMappings */
    public TestMultiTableMappings() {
    }
    
    public void setUpTestCase() {
       deleteAll(MultiA.class);
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        Collection os = new ArrayList();
        MultiA a = new MultiA();
        MultiB b = new MultiB();
        MultiC c = new MultiC();
        MultiD d = new MultiD();
        os.add(a);
        os.add(b);
        os.add(c);
        os.add(d);
        d.setA(new MultiA());
        os.add(new MultiD());
        d.setDString1("d string 1");
        
        EntityTransaction t = pm.getTransaction();
        t.begin();
        pm.persistAll(os);
        t.commit();
        aoid = pm.getObjectId(a);
        boid = pm.getObjectId(b);
        coid = pm.getObjectId(c);
        doid = pm.getObjectId(d);
        pm.close();
    }
    
    public void XXXtestUpdates() {
        //TEST DISABLED ... not converted
        /*
        PersistenceManager pm = getPM();
        Transaction t = pm.currentTransaction();
        try {
            for (int i = 0; i < 2; i++) {
                t.begin();
                changeA((MultiA) pm.getObjectById(aoid, i == 0));
                changeB((MultiB) pm.getObjectById(boid, i == 0));
                changeC((MultiC) pm.getObjectById(coid, i == 0));
                changeD((MultiD) pm.getObjectById(doid, i == 0));
                t.commit();
            }
        } finally {
            if (t.isActive()) {
                t.rollback();
            }
            pm.close();
        }
         */
    }
    
    public void XXXtestInserts() {
        //TEST DISABLED ... not converted
        /*
        PersistenceManager pm = getPM();
        assertEquals(2,
                ((Collection) pm.newQuery
                (pm.getExtent(MultiA.class, false)).execute()).size());
         
        assertEquals(4,
                ((Collection) pm.newQuery
                (pm.getExtent(MultiB.class, true)).execute()).size());
         
        assertEquals(1,
                ((Collection) pm.newQuery
                (pm.getExtent(MultiB.class, false)).execute()).size());
         
        assertEquals(1,
                ((Collection) pm.newQuery
                (pm.getExtent(MultiC.class, false)).execute()).size());
         
        assertEquals(2,
                ((Collection) pm.newQuery
                (pm.getExtent(MultiD.class, false)).execute()).size());
         
        assertEquals(6,
                ((Collection) pm.newQuery
                (pm.getExtent(MultiA.class, true)).execute()).size());
         
        pm.close();
         */
    }
    
    public void XXXtestOneToOne() {
        //TEST DISABLED ... not converted
        /*
        PersistenceManager pm = getPM();
        MultiD d = (MultiD) pm.getObjectById(doid, true);
        assertNotNull(d.getA());
        pm.close();
         */
    }
    
    public void XXXtestQueries() {
        //TEST DISABLED ... not converted
        /*
        PersistenceManager pm = getPM();
        Query q = pm.newQuery(pm.getExtent(MultiD.class, false),
                "dString1 == \"d string 1\"");
        //### this behaves like '""': "d string 1");
        Collection c = (Collection) q.execute();
        assertEquals(1, c.size());
        pm.close();
         */
    }
    
    // ### more tests:
    // ### - horizontal with extent with subclass=false
    // ### - aggregates with horizontal, interface, this stuff,
    // ### - base A, vertical B extends A, virtual C extends B,
    // ###   vertical D extends C, vertical E extends C, flat F extends C
    
    public void testVerticalQueryModeQueries() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        OpenJPAQuery q = pm.createNativeQuery("",MultiA.class);
        ((JDBCFetchPlan) q.getFetchPlan()).
                setSubclassFetchMode(JDBCFetchPlan.EAGER_PARALLEL);
        // we need ordering; otherwise kodo is smart enough to only run first
        // select until its results are exhausted
        
        //FIXME jthomas
        //q.setOrdering("string0 ascending");
        sql.clear();
        assertEquals(sql.toString(), 0, sql.size());
        
        Collection c = (Collection) q.getCandidateCollection();
        
        // account for the select distinct
        if (((String) sql.get(0)).startsWith("SELECT DISTINCT"))
            assertEquals(sql.toString(), 5, sql.size());
        else
            assertEquals(sql.toString(), 4, sql.size());
        sql.clear();
        
        assertEquals(6, c.size());
        
        // only check for counts sql if any was executed (some databases
        // might eagerly instantiate all the rows, such as pointbase)
        if (sql.size() != 0)
            assertEquals(sql.toString(), 4, sql.size()); // select counts
        sql.clear();
        
        // walk through the results. This will cause individual
        // objects to be loaded, and therefore the dfgs to be loaded,
        // and therefore any additional SQL to be executed.
        for (Iterator iter = c.iterator(); iter.hasNext();)
            iter.next();
        
        assertEquals(sql.toString(), 0, sql.size());
        
        pm.close();
    }
    
    private void changeA(MultiA a) {
        a.setString0(a.getString0() + " changed");
        a.setInt0(a.getInt0() + 1);
    }
    
    private void changeB(MultiB b) {
        changeA(b);
        b.setBString(b.getBString() + " changed");
    }
    
    private void changeC(MultiC c) {
        changeB(c);
        c.setCString0(c.getCString0() + " changed");
    }
    
    private void changeD(MultiD d) {
        changeB(d);
        d.setDString0(d.getDString0() + " changed");
    }
    
    
}
