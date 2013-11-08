/*
 * TestEagerDistinct.java
 *
 * Created on October 4, 2006, 4:27 PM
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
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.Join;
import org.apache.openjpa.persistence.OpenJPAQuery;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestEagerDistinct
    extends org.apache.openjpa.persistence.jdbc.kernel.TestSQLListenerTestCase {
        
    /** Creates a new instance of TestEagerDistinct */
    public TestEagerDistinct(String name) 
    {
    	super(name);
    }
    
    public boolean skipTest() {
        return ((JDBCConfiguration) getConfiguration()).
                getDBDictionaryInstance().joinSyntax == Join.SYNTAX_TRADITIONAL;
    }
    
    public void setUpTestCase() {
       deleteAll(EagerPC.class);
       deleteAll(HelperPC.class);
       deleteAll(HelperPC2.class);
        
        HelperPC shared = new HelperPC();
        shared.setStringField("shared");
        
        HelperPC2 pc1 = new HelperPC2();
        pc1.setStringField("pc1");
        pc1.getHelperCollection().add(shared);
        pc1.getHelperCollection().add(new HelperPC());
        
        HelperPC2 pc2 = new HelperPC2();
        pc2.setStringField("pc2");
        pc2.getHelperCollection().add(shared);
        pc2.getHelperCollection().add(new HelperPC());
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);
        pm.persist(pc1);
        pm.persist(pc2);
        endTx(pm);
        
        // to make sure subclasses are selected, etc
        //FIXME jthomas
        //pm.createNativeQuery("",HelperPC.class).execute();
        //pm.newQuery(HelperPC2.class).execute();
        pm.close();
    }
    
    public OpenJPAEntityManager getPM() {
        OpenJPAEntityManager pm = (OpenJPAEntityManager)currentEntityManager();
        pm.getFetchPlan().setMaxFetchDepth(-1);
        return pm;
    }
    
    public void testEagerParallelWithNonDistinctQuery()
    throws Exception {
        eagerParallelWithNonDistinctQuery(-1);
    }
    
    public void testPagingEagerParallelWithNonDistinctQuery()
    throws Exception {
        eagerParallelWithNonDistinctQuery(0);
    }
    
    private void eagerParallelWithNonDistinctQuery(int fetchSize)
    throws Exception {
        OpenJPAEntityManager pm = getPM();
        OpenJPAQuery q = pm.createNativeQuery(
                "stringField.startsWith ('pc')", HelperPC2.class);
        //FIXME jthomas
        //q.setOrdering("stringField ascending");
        q.getFetchPlan().setFetchBatchSize(fetchSize);
        List res = (List) q.getResultList();
        
        if (fetchSize == -1)
            assertEquals(2, sql.size());
        
        assertEquals(2, res.size());
        assertHelperPC2("pc1", (HelperPC2) res.get(0));
        assertHelperPC2("pc2", (HelperPC2) res.get(1));
        
        assertNotSQL("DISTINCT");
        pm.close();
    }
    
    private void assertHelperPC2(String stringField, HelperPC2 pc) {
        assertEquals(stringField, pc.getStringField());
        assertEquals(2, pc.getHelperCollection().size());
        assertEquals("shared", ((HelperPC) pc.getHelperCollection().get(0)).
                getStringField());
        assertNull(((HelperPC) pc.getHelperCollection().get(1)).
                getStringField());
    }
    
    public void testEagerParallelWithDistinctQuery()
    throws Exception {
        eagerParallelWithDistinctQuery(-1);
    }
    
    public void testPagingEagerParallelWithDistinctQuery()
    throws Exception {
        eagerParallelWithDistinctQuery(0);
    }
    
    private void eagerParallelWithDistinctQuery(int fetchSize)
    throws Exception {
        OpenJPAEntityManager pm = getPM();
        OpenJPAQuery q = pm.createNativeQuery(
                "helperCollection.contains (h) && h.stringField == 'shared'",
                HelperPC2.class);
        //FIXME  jthomas
        //q.setOrdering("stringField ascending");
        q.getFetchPlan().setFetchBatchSize(fetchSize);
        List res = (List) q.getResultList();
        
        if (fetchSize == -1) {
            sql.remove(0);    // orig sel
            assertSQL("DISTINCT");
        }
        
        assertEquals(2, res.size());
        assertHelperPC2("pc1", (HelperPC2) res.get(0));
        assertHelperPC2("pc2", (HelperPC2) res.get(1));
        pm.close();
    }
    
    public void testNestedEagerParallel()
    throws Exception {
        nestedEagerParallel(-1);
    }
    
    public void testPagingNestedEagerParallel()
    throws Exception {
        nestedEagerParallel(0);
    }
    
    private void nestedEagerParallel(int fetchSize)
    throws Exception {
        OpenJPAEntityManager pm = getPM();
        OpenJPAQuery q = pm.createNativeQuery("",HelperPC2.class);
        //FIXME jthomas
        //q.setOrdering("stringField ascending");
        List helpers = (List) q.getResultList();
        
        EagerPC eager1 = new EagerPC();
        eager1.setStringField("eager1");
        eager1.getRecurseCollection().addAll(helpers);
        
        EagerPC eager2 = new EagerPC();
        eager2.setStringField("eager2");
        eager2.getRecurseCollection().addAll(helpers);
        HelperPC2 pc3 = new HelperPC2();
        pc3.setStringField("pc3");
        pc3.getHelperCollection().add(new HelperPC());
        pc3.getHelperCollection().add(new HelperPC());
        pc3.getHelperCollection().add(new HelperPC());
        eager2.getRecurseCollection().add(pc3);
        
        startTx(pm);;
        pm.persist(eager1);
        pm.persist(eager2);
        endTx(pm);;
        // make sure subclasses selected, etc
        //FIXME jthomas
        pm.createNativeQuery("",EagerPC.class).getResultList();
        pm.close();
        sql.clear();
        
        pm = getPM();
        q = pm.createNativeQuery("stringField.startsWith ('eager')",
                EagerPC.class);
        //FIXME jthomas
        //q.setOrdering("stringField ascending");
        q.getFetchPlan().setFetchBatchSize(fetchSize);
        List res = (List) q.getResultList();
        
        if (fetchSize == -1) {
            sql.remove(0); // orig sel
            assertSQL("DISTINCT");
        }
        
        assertEquals(2, res.size());
        eager1 = (EagerPC) res.get(0);
        assertEquals("eager1", eager1.getStringField());
        assertEquals(2, eager1.getRecurseCollection().size());
        assertHelperPC2("pc1", (HelperPC2) eager1.getRecurseCollection().
                get(0));
        assertHelperPC2("pc2", (HelperPC2) eager1.getRecurseCollection().
                get(1));
        
        eager2 = (EagerPC) res.get(1);
        assertEquals("eager2", eager2.getStringField());
        assertEquals(3, eager2.getRecurseCollection().size());
        assertHelperPC2("pc1", (HelperPC2) eager2.getRecurseCollection().
                get(0));
        assertHelperPC2("pc2", (HelperPC2) eager2.getRecurseCollection().
                get(1));
        pc3 = (HelperPC2) eager2.getRecurseCollection().get(2);
        assertEquals("pc3", pc3.getStringField());
        assertEquals(3, pc3.getHelperCollection().size());
        pm.close();
    }
    
}
