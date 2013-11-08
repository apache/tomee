/*
 * TestEagerOuterToManyJoins.java
 *
 * Created on October 3, 2006, 10:53 AM
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
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.persistence.FetchPlan;
import org.apache.openjpa.persistence.OpenJPAQuery;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestEagerOuterToManyJoins
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {

    /** Creates a new instance of TestEagerOuterToManyJoins */
    public TestEagerOuterToManyJoins(String name) {
    	super(name);
    }
    
    public boolean skipTest() {
        DBDictionary dict = ((JDBCConfiguration) getConfiguration()).
                getDBDictionaryInstance();
        return !dict.supportsSubselect;
    }
    
    public void setUp() {
        
       deleteAll(HelperPC.class);
       deleteAll(EagerOuterJoinPC2.class);
       deleteAll(EagerOuterJoinPC.class);
    }
    
    
    public void testStringCollectionById() {
        stringCollectionByIdTest(false);
    }
    
    public void testEmptyStringCollectionById() {
        stringCollectionByIdTest(true);
    }
    
    private void stringCollectionByIdTest(boolean empty) {
        Object oid = insertStringCollection((empty) ? 1 : 0);
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "stringCollection");
        EagerOuterJoinPC pc = (EagerOuterJoinPC) pm.getObjectId(oid);
        assertEquals("1", pc.getName());
        if (empty)
            assertEquals(pc.getStringCollection().toString(),
                    0, pc.getStringCollection().size());
        else {
            assertEquals(pc.getStringCollection().toString(),
                    2, pc.getStringCollection().size());
            assertTrue(pc.getStringCollection().contains("1.1"));
            assertTrue(pc.getStringCollection().contains("1.2"));
        }
        pm.close();
    }
    
    public void testStringCollectionByQuery() {
        stringCollectionByQueryTest(0);
    }
    
    public void testEmptyStringCollectionByQuery1() {
        stringCollectionByQueryTest(1);
    }
    
    public void testEmptyStringCollectionByQuery2() {
        stringCollectionByQueryTest(2);
    }
    
    public void testEmptyStringCollectionByQuery3() {
        stringCollectionByQueryTest(3);
    }
    
    private void stringCollectionByQueryTest(int empty) {
        insertStringCollection(empty);
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "stringCollection");
        fetch.setFetchBatchSize(-1);
        OpenJPAQuery q = pm.createNativeQuery("",EagerOuterJoinPC.class);
        
        //FIXME jthomas
        //q.setOrdering("name ascending");
        Collection results = (Collection) q.getResultList();
        
        assertEquals(2, results.size());
        Iterator itr = results.iterator();
        EagerOuterJoinPC pc = (EagerOuterJoinPC) itr.next();
        assertEquals("1", pc.getName());
        if ((empty & 1) > 0)
            assertEquals(pc.getStringCollection().toString(),
                    0, pc.getStringCollection().size());
        else {
            assertEquals(pc.getStringCollection().toString(),
                    2, pc.getStringCollection().size());
            assertTrue(pc.getStringCollection().contains("1.1"));
            assertTrue(pc.getStringCollection().contains("1.2"));
        }
        
        pc = (EagerOuterJoinPC) itr.next();
        assertEquals("2", pc.getName());
        if ((empty & 2) > 0)
            assertEquals(pc.getStringCollection().toString(),
                    0, pc.getStringCollection().size());
        else {
            assertEquals(pc.getStringCollection().toString(),
                    2, pc.getStringCollection().size());
            assertTrue(pc.getStringCollection().contains("2.1"));
            assertTrue(pc.getStringCollection().contains("2.2"));
        }
        assertTrue(!itr.hasNext());
        pm.close();
    }
    
    private Object insertStringCollection(int empty) {
        EagerOuterJoinPC pc1 = new EagerOuterJoinPC();
        pc1.setName("1");
        if ((empty & 1) == 0) {
            pc1.getStringCollection().add("1.1");
            pc1.getStringCollection().add("1.2");
        }
        
        EagerOuterJoinPC pc2 = new EagerOuterJoinPC();
        pc2.setName("2");
        if ((empty & 2) == 0) {
            pc2.getStringCollection().add("2.1");
            pc2.getStringCollection().add("2.2");
        }
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        
        startTx(pm);;
        pm.persist(pc1);
        pm.persist(pc2);
        endTx(pm);;
        Object oid = pm.getObjectId(pc1);
        pm.close();
        return oid;
    }
    
    public void testStringListById() {
        Object oid = insertStringList();
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        
        
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "stringList");
        EagerOuterJoinPC pc = (EagerOuterJoinPC) pm.getObjectId(oid);
        assertEquals("1", pc.getName());
        assertEquals(2, pc.getStringList().size());
        assertEquals("1.1", pc.getStringList().get(0));
        assertEquals("1.2", pc.getStringList().get(1));
        pm.close();
    }
    
    public void testStringListByQuery() {
        insertStringList();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "stringList");
        fetch.setFetchBatchSize(-1);
        OpenJPAQuery q = pm.createNativeQuery("",EagerOuterJoinPC.class);
        //FIXME jthomas
        //q.setOrdering("name ascending");
        Collection results = (Collection) q.getResultList();
        
        assertEquals(2, results.size());
        Iterator itr = results.iterator();
        EagerOuterJoinPC pc = (EagerOuterJoinPC) itr.next();
        assertEquals("1", pc.getName());
        assertEquals(2, pc.getStringList().size());
        assertEquals("1.1", pc.getStringList().get(0));
        assertEquals("1.2", pc.getStringList().get(1));
        pc = (EagerOuterJoinPC) itr.next();
        assertEquals("2", pc.getName());
        assertEquals(2, pc.getStringList().size());
        assertEquals("2.1", pc.getStringList().get(0));
        assertEquals("2.2", pc.getStringList().get(1));
        assertTrue(!itr.hasNext());
        pm.close();
    }
    
    private Object insertStringList() {
        EagerOuterJoinPC pc1 = new EagerOuterJoinPC();
        pc1.setName("1");
        pc1.getStringList().add("1.1");
        pc1.getStringList().add("1.2");
        
        EagerOuterJoinPC pc2 = new EagerOuterJoinPC();
        pc2.setName("2");
        pc2.getStringList().add("2.1");
        pc2.getStringList().add("2.2");
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);
        pm.persist(pc1);
        pm.persist(pc2);
        endTx(pm);;
        Object oid = pm.getObjectId(pc1);
        pm.close();
        return oid;
    }
    
    public void testOneManyCollectionById() {
        oneManyCollectionByIdTest(false);
    }
    
    public void testEmptyOneManyCollectionById() {
        oneManyCollectionByIdTest(true);
    }
    
    private void oneManyCollectionByIdTest(boolean empty) {
        Object oid = insertOneManyCollection((empty) ? 1 : 0);
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "oneManyCollection");
        EagerOuterJoinPC pc = (EagerOuterJoinPC) pm.getObjectId(oid);
        assertEquals("1", pc.getName());
        if (empty)
            assertEquals(0, pc.getOneManyCollection().size());
        else
            assertEquals(2, pc.getOneManyCollection().size());
        pm.close();
    }
    
    public void testOneManyCollectionByQuery() {
        oneManyCollectionByQueryTest(0);
    }
    
    public void testEmptyOneManyCollectionByQuery1() {
        oneManyCollectionByQueryTest(1);
    }
    
    public void testEmptyOneManyCollectionByQuery2() {
        oneManyCollectionByQueryTest(2);
    }
    
    public void testEmptyOneManyCollectionByQuery3() {
        oneManyCollectionByQueryTest(3);
    }
    
    private void oneManyCollectionByQueryTest(int empty) {
        insertOneManyCollection(empty);
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "oneManyCollection");
        fetch.setFetchBatchSize(-1);
        OpenJPAQuery q = pm.createNativeQuery("",EagerOuterJoinPC.class);
        //FIXME jthomas
        //q.setOrdering("name ascending");
        Collection results = (Collection) q.getResultList();
        
        assertEquals(2, results.size());
        Iterator itr = results.iterator();
        EagerOuterJoinPC pc = (EagerOuterJoinPC) itr.next();
        assertEquals("1", pc.getName());
        if ((empty & 1) > 0)
            assertEquals(0, pc.getOneManyCollection().size());
        else
            assertEquals(2, pc.getOneManyCollection().size());
        
        pc = (EagerOuterJoinPC) itr.next();
        assertEquals("2", pc.getName());
        if ((empty & 2) > 0)
            assertEquals(0, pc.getOneManyCollection().size());
        else
            assertEquals(2, pc.getOneManyCollection().size());
        
        assertTrue(!itr.hasNext());
        pm.close();
    }
    
    private Object insertOneManyCollection(int empty) {
        EagerOuterJoinPC pc1 = new EagerOuterJoinPC();
        pc1.setName("1");
        EagerOuterJoinPC2 hpc;
        if ((empty & 1) == 0) {
            hpc = new EagerOuterJoinPC2();
            hpc.setName("1.1");
            hpc.setRef(pc1);
            pc1.getOneManyCollection().add(hpc);
            hpc = new EagerOuterJoinPC2();
            hpc.setName("1.2");
            hpc.setRef(pc1);
            pc1.getOneManyCollection().add(hpc);
        }
        
        EagerOuterJoinPC pc2 = new EagerOuterJoinPC();
        pc2.setName("2");
        if ((empty & 2) == 0) {
            hpc = new EagerOuterJoinPC2();
            hpc.setName("2.1");
            hpc.setRef(pc2);
            pc2.getOneManyCollection().add(hpc);
            hpc = new EagerOuterJoinPC2();
            hpc.setName("2.2");
            hpc.setRef(pc2);
            pc2.getOneManyCollection().add(hpc);
        }
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);
        pm.persist(pc1);
        pm.persist(pc2);
        endTx(pm);
        Object oid = pm.getObjectId(pc1);
        pm.close();
        return oid;
    }
    
    public void testManyManyCollectionById() {
        Object oid = insertManyManyCollection();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "manyManyCollection");
        EagerOuterJoinPC pc = (EagerOuterJoinPC) pm.getObjectId(oid);
        assertEquals("1", pc.getName());
        assertEquals(2, pc.getManyManyCollection().size());
        pm.close();
    }
    
    public void testManyManyCollectionByQuery() {
        insertManyManyCollection();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "manyManyCollection");
        fetch.setFetchBatchSize(-1);
        OpenJPAQuery q = pm.createNativeQuery("",EagerOuterJoinPC.class);
        //FIXME jthomas
        //q.setOrdering("name ascending");
        Collection results = (Collection) q.getResultList();
        
        assertEquals(2, results.size());
        Iterator itr = results.iterator();
        EagerOuterJoinPC pc = (EagerOuterJoinPC) itr.next();
        assertEquals("1", pc.getName());
        assertEquals(2, pc.getManyManyCollection().size());
        pc = (EagerOuterJoinPC) itr.next();
        assertEquals("2", pc.getName());
        assertEquals(2, pc.getManyManyCollection().size());
        assertTrue(!itr.hasNext());
        pm.close();
    }
    
    private Object insertManyManyCollection() {
        EagerOuterJoinPC pc1 = new EagerOuterJoinPC();
        pc1.setName("1");
        EagerOuterJoinPC2 hpc = new EagerOuterJoinPC2();
        hpc.setName("1.1");
        pc1.getManyManyCollection().add(hpc);
        hpc = new EagerOuterJoinPC2();
        hpc.setName("1.2");
        pc1.getManyManyCollection().add(hpc);
        
        EagerOuterJoinPC pc2 = new EagerOuterJoinPC();
        pc2.setName("2");
        hpc = new EagerOuterJoinPC2();
        hpc.setName("2.1");
        pc2.getManyManyCollection().add(hpc);
        hpc = new EagerOuterJoinPC2();
        hpc.setName("2.2");
        pc2.getManyManyCollection().add(hpc);
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);
        pm.persist(pc1);
        pm.persist(pc2);
        endTx(pm);;
        Object oid = pm.getObjectId(pc1);
        pm.close();
        return oid;
    }
    
    public void testManyManyListById() {
        manyManyListByIdTest(false);
    }
    
    public void testEmptyManyManyListById() {
        manyManyListByIdTest(true);
    }
    
    private void manyManyListByIdTest(boolean empty) {
        Object oid = insertManyManyList((empty) ? 1 : 0);
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "manyManyList");
        EagerOuterJoinPC pc = (EagerOuterJoinPC) pm.getObjectId(oid);
        assertEquals("1", pc.getName());
        if (empty)
            assertEquals(0, pc.getManyManyList().size());
        else {
            assertEquals(2, pc.getManyManyList().size());
            EagerOuterJoinPC2 hpc = (EagerOuterJoinPC2)
            pc.getManyManyList().get(0);
            assertEquals("1.1", hpc.getName());
            hpc = (EagerOuterJoinPC2) pc.getManyManyList().get(1);
            assertEquals("1.2", hpc.getName());
        }
        pm.close();
    }
    
    public void testManyManyListByQuery() {
        manyManyListByQueryTest(0);
    }
    
    public void testEmptyManyManyListByQuery1() {
        manyManyListByQueryTest(1);
    }
    
    public void testEmptyManyManyListByQuery2() {
        manyManyListByQueryTest(2);
    }
    
    public void testEmptyManyManyListByQuery3() {
        manyManyListByQueryTest(3);
    }
    
    private void manyManyListByQueryTest(int empty) {
        insertManyManyList(empty);
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "manyManyList");
        fetch.setFetchBatchSize(-1);
        OpenJPAQuery q = pm.createNativeQuery("",EagerOuterJoinPC.class);
        //FIXME jthomas
        //q.setOrdering("name ascending");
        Collection results = (Collection) q.getResultList();
        
        assertEquals(2, results.size());
        Iterator itr = results.iterator();
        EagerOuterJoinPC pc = (EagerOuterJoinPC) itr.next();
        assertEquals("1", pc.getName());
        EagerOuterJoinPC2 hpc;
        if ((empty & 1) > 0)
            assertEquals(0, pc.getManyManyList().size());
        else {
            assertEquals(2, pc.getManyManyList().size());
            hpc = (EagerOuterJoinPC2) pc.getManyManyList().get(0);
            assertEquals("1.1", hpc.getName());
            hpc = (EagerOuterJoinPC2) pc.getManyManyList().get(1);
            assertEquals("1.2", hpc.getName());
        }
        
        pc = (EagerOuterJoinPC) itr.next();
        assertEquals("2", pc.getName());
        if ((empty & 2) > 0)
            assertEquals(0, pc.getManyManyList().size());
        else {
            assertEquals(2, pc.getManyManyList().size());
            hpc = (EagerOuterJoinPC2) pc.getManyManyList().get(0);
            assertEquals("2.1", hpc.getName());
            hpc = (EagerOuterJoinPC2) pc.getManyManyList().get(1);
            assertEquals("2.2", hpc.getName());
        }
        
        assertTrue(!itr.hasNext());
        pm.close();
    }
    
    private Object insertManyManyList(int empty) {
        EagerOuterJoinPC pc1 = new EagerOuterJoinPC();
        pc1.setName("1");
        EagerOuterJoinPC2 hpc;
        if ((empty & 1) == 0) {
            hpc = new EagerOuterJoinPC2();
            hpc.setName("1.1");
            pc1.getManyManyList().add(hpc);
            hpc = new EagerOuterJoinPC2();
            hpc.setName("1.2");
            pc1.getManyManyList().add(hpc);
        }
        
        EagerOuterJoinPC pc2 = new EagerOuterJoinPC();
        pc2.setName("2");
        if ((empty & 2) == 0) {
            hpc = new EagerOuterJoinPC2();
            hpc.setName("2.1");
            pc2.getManyManyList().add(hpc);
            hpc = new EagerOuterJoinPC2();
            hpc.setName("2.2");
            pc2.getManyManyList().add(hpc);
        }
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);;
        pm.persist(pc1);
        pm.persist(pc2);
        endTx(pm);;
        Object oid = pm.getObjectId(pc1);
        pm.close();
        return oid;
    }
    
    public void testTwoCollectionsInFetchGroupsById() {
        Object oid = insertTwoCollections();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "stringCollection");
        fetch.addField(EagerOuterJoinPC.class, "manyManyList");
        EagerOuterJoinPC pc = (EagerOuterJoinPC) pm.getObjectId(oid);
        assertEquals("1", pc.getName());
        assertEquals(2, pc.getStringCollection().size());
        assertTrue(pc.getStringCollection().contains("1.1"));
        assertTrue(pc.getStringCollection().contains("1.2"));
        assertEquals(2, pc.getManyManyList().size());
        EagerOuterJoinPC2 hpc = (EagerOuterJoinPC2)
        pc.getManyManyList().get(0);
        assertEquals("1.1", hpc.getName());
        hpc = (EagerOuterJoinPC2) pc.getManyManyList().get(1);
        assertEquals("1.2", hpc.getName());
        pm.close();
    }
    
    public void testTwoCollectionsInFetchGroupsByQuery() {
        insertTwoCollections();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "stringCollection");
        fetch.addField(EagerOuterJoinPC.class, "manyManyList");
        fetch.setFetchBatchSize(-1);
        
        OpenJPAQuery q = pm.createNativeQuery("",EagerOuterJoinPC.class);
        //FIXME jthomas
        //q.setOrdering("name ascending");
        Collection results = (Collection) q.getResultList();
        
        assertEquals(2, results.size());
        Iterator itr = results.iterator();
        EagerOuterJoinPC pc = (EagerOuterJoinPC) itr.next();
        assertEquals("1", pc.getName());
        assertEquals(2, pc.getStringCollection().size());
        assertTrue(pc.getStringCollection().contains("1.1"));
        assertTrue(pc.getStringCollection().contains("1.2"));
        assertEquals(2, pc.getManyManyList().size());
        EagerOuterJoinPC2 hpc = (EagerOuterJoinPC2)
        pc.getManyManyList().get(0);
        assertEquals("1.1", hpc.getName());
        hpc = (EagerOuterJoinPC2) pc.getManyManyList().get(1);
        assertEquals("1.2", hpc.getName());
        
        pc = (EagerOuterJoinPC) itr.next();
        assertEquals("2", pc.getName());
        assertEquals(2, pc.getStringCollection().size());
        assertTrue(pc.getStringCollection().contains("2.1"));
        assertTrue(pc.getStringCollection().contains("2.2"));
        assertEquals(2, pc.getManyManyList().size());
        hpc = (EagerOuterJoinPC2) pc.getManyManyList().get(0);
        assertEquals("2.1", hpc.getName());
        hpc = (EagerOuterJoinPC2) pc.getManyManyList().get(1);
        assertEquals("2.2", hpc.getName());
        
        assertTrue(!itr.hasNext());
        pm.close();
    }
    
    private Object insertTwoCollections() {
        EagerOuterJoinPC pc1 = new EagerOuterJoinPC();
        pc1.setName("1");
        pc1.getStringCollection().add("1.1");
        pc1.getStringCollection().add("1.2");
        EagerOuterJoinPC2 hpc = new EagerOuterJoinPC2();
        hpc.setName("1.1");
        pc1.getManyManyList().add(hpc);
        hpc = new EagerOuterJoinPC2();
        hpc.setName("1.2");
        pc1.getManyManyList().add(hpc);
        
        EagerOuterJoinPC pc2 = new EagerOuterJoinPC();
        pc2.setName("2");
        pc2.getStringCollection().add("2.1");
        pc2.getStringCollection().add("2.2");
        hpc = new EagerOuterJoinPC2();
        hpc.setName("2.1");
        pc2.getManyManyList().add(hpc);
        hpc = new EagerOuterJoinPC2();
        hpc.setName("2.2");
        pc2.getManyManyList().add(hpc);
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);;
        pm.persist(pc1);
        pm.persist(pc2);
        endTx(pm);;
        Object oid = pm.getObjectId(pc1);
        pm.close();
        return oid;
    }
    
    public void testQueryRandomAccess() {
        insertManyStringList();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "stringList");
        fetch.setFetchBatchSize(3);
        OpenJPAQuery q = pm.createNativeQuery("",EagerOuterJoinPC.class);
        //FIXME jthomas
        //q.setOrdering("name ascending");
        List results = (List) q.getResultList();
        assertEquals(10, results.size());
        
        for (int i = 5; i < results.size(); i++) {
            EagerOuterJoinPC pc = (EagerOuterJoinPC) results.get(i);
            assertEquals(String.valueOf(i), pc.getName());
            assertEquals(2, pc.getStringList().size());
            assertEquals(i + ".1", pc.getStringList().get(0));
            assertEquals(i + ".2", pc.getStringList().get(1));
        }
        q.closeAll();
        pm.close();
    }
    
    public void testQueryRange() {
        insertManyStringList();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "stringList");
        fetch.setFetchBatchSize(3);
        OpenJPAQuery q = pm.createNativeQuery("",EagerOuterJoinPC.class);
        //FIXME jthomas
        //q.setOrdering("name ascending");
        //q.setRange(5, 20);
        
        List results = (List) q.getResultList();
        assertEquals(5, results.size());
        
        for (int i = 0; i < results.size(); i++) {
            EagerOuterJoinPC pc = (EagerOuterJoinPC) results.get(i);
            assertEquals(String.valueOf(i + 5), pc.getName());
            assertEquals(2, pc.getStringList().size());
            assertEquals((i + 5) + ".1", pc.getStringList().get(0));
            assertEquals((i + 5) + ".2", pc.getStringList().get(1));
        }
        q.closeAll();
        pm.close();
    }
    
    private void insertManyStringList() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);;
        for (int i = 0; i < 10; i++) {
            EagerOuterJoinPC pc = new EagerOuterJoinPC();
            pc.setName(String.valueOf(i));
            pc.getStringList().add(i + ".1");
            pc.getStringList().add(i + ".2");
            pm.persist(pc);
        }
        endTx(pm);;
        pm.close();
    }
    
    public void testEagerToOneThenEagerToMany() {
        insertEagers();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC2.class, "ref");
        fetch.addField(EagerOuterJoinPC.class, "stringCollection");
        OpenJPAQuery q = pm.createNativeQuery("",EagerOuterJoinPC2.class);
        //FIXME jthomas
        //q.setOrdering("name ascending");
        Collection results = (Collection) q.getResultList();
        assertEquals(new ArrayList(results).toString(), 2, results.size());
        
        Iterator itr = results.iterator();
        EagerOuterJoinPC2 ref = (EagerOuterJoinPC2) itr.next();
        assertEquals("r1", ref.getName());
        EagerOuterJoinPC pc = ref.getRef();
        assertEquals("1", pc.getName());
        assertEquals(2, pc.getStringCollection().size());
        assertTrue(pc.getStringCollection().contains("1.1"));
        assertTrue(pc.getStringCollection().contains("1.2"));
        
        ref = (EagerOuterJoinPC2) itr.next();
        assertEquals("r2", ref.getName());
        assertTrue(pc == ref.getRef());
        
        assertTrue(!itr.hasNext());
        pm.close();
    }
    
    public void testEagerToManyThenEagerToOne() {
        insertEagers();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "manyManyList");
        fetch.addField(EagerOuterJoinPC2.class, "helper");
        OpenJPAQuery q = pm.createNativeQuery("",EagerOuterJoinPC.class);
        //FIXME jthomas
        //q.setOrdering("name ascending");
        Collection results = (Collection) q.getResultList();
        assertEquals(1, results.size());
        
        EagerOuterJoinPC pc = (EagerOuterJoinPC) results.iterator().next();
        assertEquals("1", pc.getName());
        assertEquals(2, pc.getManyManyList().size());
        EagerOuterJoinPC2 ref = (EagerOuterJoinPC2)
        pc.getManyManyList().get(0);
        assertEquals("r1", ref.getName());
        assertEquals("h1", ref.getHelper().getStringField());
        ref = (EagerOuterJoinPC2) pc.getManyManyList().get(1);
        assertEquals("r2", ref.getName());
        assertEquals("h2", ref.getHelper().getStringField());
        
        pm.close();
    }
    
    public void testEagerToManyThenEagerToMany() {
        insertEagers();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "manyManyList");
        fetch.addField(EagerOuterJoinPC2.class, "stringCollection");
        OpenJPAQuery q = pm.createNativeQuery("",EagerOuterJoinPC.class);
        //FIXME jthomas
        //q.setOrdering("name ascending");
        Collection results = (Collection) q.getResultList();
        assertEquals(1, results.size());
        
        EagerOuterJoinPC pc = (EagerOuterJoinPC) results.iterator().next();
        assertEquals("1", pc.getName());
        assertEquals(2, pc.getManyManyList().size());
        EagerOuterJoinPC2 ref = (EagerOuterJoinPC2)
        pc.getManyManyList().get(0);
        assertEquals("r1", ref.getName());
        assertEquals(2, ref.getStringCollection().size());
        assertTrue(ref.getStringCollection().contains("r1.1"));
        assertTrue(ref.getStringCollection().contains("r1.2"));
        
        ref = (EagerOuterJoinPC2) pc.getManyManyList().get(1);
        assertEquals("r2", ref.getName());
        assertEquals(2, ref.getStringCollection().size());
        assertTrue(ref.getStringCollection().contains("r2.1"));
        assertTrue(ref.getStringCollection().contains("r2.2"));
        
        pm.close();
    }
    
    public void testEagerToOneAndToManyThenEagerToOne() {
        Object oid = insertEagers();
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) pm.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "oneManyCollection");
        fetch.addField(EagerOuterJoinPC.class, "helper");
        fetch.addField(EagerOuterJoinPC2.class, "helper");
        OpenJPAQuery q = pm.createNativeQuery("",EagerOuterJoinPC.class);
        //FIXME jthomas
        //q.setOrdering("name ascending");
        Collection results = (Collection) q.getResultList();
        assertEquals(1, results.size());
        
        EagerOuterJoinPC pc = (EagerOuterJoinPC) results.iterator().next();
        assertEquals("1", pc.getName());
        assertEquals("h3", pc.getHelper().getStringField());
        assertEquals(2, pc.getOneManyCollection().size());
        Iterator itr = pc.getOneManyCollection().iterator();
        EagerOuterJoinPC2 ref = (EagerOuterJoinPC2) itr.next();
        if ("r1".equals(ref.getName())) {
            assertEquals("h1", ref.getHelper().getStringField());
            ref = (EagerOuterJoinPC2) itr.next();
            assertEquals("r2", ref.getName());
            assertEquals("h2", ref.getHelper().getStringField());
        } else {
            assertEquals("r2", ref.getName());
            assertEquals("h2", ref.getHelper().getStringField());
            ref = (EagerOuterJoinPC2) itr.next();
            assertEquals("r1", ref.getName());
            assertEquals("h1", ref.getHelper().getStringField());
        }
        assertTrue(!itr.hasNext());
        pm.close();
    }
    
    private Object insertEagers() {
        EagerOuterJoinPC pc1 = new EagerOuterJoinPC();
        pc1.setName("1");
        pc1.getStringCollection().add("1.1");
        pc1.getStringCollection().add("1.2");
        
        EagerOuterJoinPC2 ref1 = new EagerOuterJoinPC2();
        ref1.setName("r1");
        ref1.getStringCollection().add("r1.1");
        ref1.getStringCollection().add("r1.2");
        
        EagerOuterJoinPC2 ref2 = new EagerOuterJoinPC2();
        ref2.setName("r2");
        ref2.getStringCollection().add("r2.1");
        ref2.getStringCollection().add("r2.2");
        
        HelperPC hpc1 = new HelperPC();
        hpc1.setStringField("h1");
        
        HelperPC hpc2 = new HelperPC();
        hpc2.setStringField("h2");
        
        HelperPC hpc3 = new HelperPC();
        hpc3.setStringField("h3");
        
        pc1.getManyManyList().add(ref1);
        pc1.getOneManyCollection().add(ref1);
        ref1.setRef(pc1);
        pc1.getManyManyList().add(ref2);
        pc1.getOneManyCollection().add(ref2);
        ref2.setRef(pc1);
        
        ref1.setHelper(hpc1);
        ref2.setHelper(hpc2);
        pc1.setHelper(hpc3);
        
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);;
        pm.persist(pc1);
        endTx(pm);;
        Object oid = pm.getObjectId(pc1);
        pm.close();
        return oid;
    }
    
    
}
