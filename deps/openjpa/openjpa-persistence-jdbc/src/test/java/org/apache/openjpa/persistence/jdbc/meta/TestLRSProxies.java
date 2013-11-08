/*
 * TestLRSProxies.java
 *
 * Created on October 3, 2006, 5:01 PM
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

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestLRSProxies
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
   
    private Object _oid = null;
    private Object _coid = null;
    
    public TestLRSProxies(String casename) {
        super(casename);
    }
    
    
    /** Creates a new instance of TestLRSProxies */
    public TestLRSProxies() {
    }
    public void setUp() {
       deleteAll(LRSPC.class);
       deleteAll(LRSCompoundPC.class);
        
        LRSPC pc = new LRSPC("main");
        
        pc.getStringSet().add("val1");
        pc.getStringSet().add("val2");
        pc.getStringSet().add("val3");
        
        pc.getRelSet().add(new LRSPC("set1"));
        pc.getRelSet().add(new LRSPC("set2"));
        pc.getRelSet().add(new LRSPC("set3"));
        
        pc.getStringCollection().add("val1");
        pc.getStringCollection().add("val2");
        pc.getStringCollection().add("val3");
        
        pc.getRelCollection().add(new LRSPC("set1"));
        pc.getRelCollection().add(new LRSPC("set2"));
        pc.getRelCollection().add(new LRSPC("set3"));
        
        pc.getStringMap().put("key1", "1");
        pc.getStringMap().put("key2", "2");
        pc.getStringMap().put("key3", "3");
        
        pc.getRelMap().put("key1", new LRSPC("map1"));
        pc.getRelMap().put("key2", new LRSPC("map2"));
        pc.getRelMap().put("key3", new LRSPC("map3"));
        
        LRSCompoundPC cpc = new LRSCompoundPC("main");
        
        cpc.getStringSet().add("val1");
        cpc.getStringSet().add("val2");
        cpc.getStringSet().add("val3");
        
        cpc.getRelSet().add(new LRSCompoundPC("set1"));
        cpc.getRelSet().add(new LRSCompoundPC("set2"));
        cpc.getRelSet().add(new LRSCompoundPC("set3"));
        
        cpc.getStringCollection().add("val1");
        cpc.getStringCollection().add("val2");
        cpc.getStringCollection().add("val3");
        
        cpc.getRelCollection().add(new LRSCompoundPC("set1"));
        cpc.getRelCollection().add(new LRSCompoundPC("set2"));
        cpc.getRelCollection().add(new LRSCompoundPC("set3"));
        
        cpc.getStringMap().put("key1", "1");
        cpc.getStringMap().put("key2", "2");
        cpc.getStringMap().put("key3", "3");
        
        cpc.getRelMap().put("key1", new LRSCompoundPC("map1"));
        cpc.getRelMap().put("key2", new LRSCompoundPC("map2"));
        cpc.getRelMap().put("key3", new LRSCompoundPC("map3"));
        
        
        
        OpenJPAEntityManager pm = getEm(false, false);
        
        startTx(pm);
        pm.persist(pc);
        pm.persist(cpc);
        endTx(pm);
        _oid = pm.getObjectId(pc);
        _coid = pm.getObjectId(cpc);
        pm.close();
    }
    
    public void testStringSet() {
        stringCollectionTest(_oid, true, true);
        stringCollectionTest(_coid, true, true);
    }
    
    public void testStringSetRetain() {
        stringCollectionTest(_oid, false, true);
        stringCollectionTest(_coid, false, true);
    }
    
    public void testStringCollection() {
        stringCollectionTest(_oid, true, false);
        stringCollectionTest(_coid, true, false);
    }
    
    public void testStringCollectionRetain() {
        stringCollectionTest(_oid, false, false);
        stringCollectionTest(_coid, false, false);
    }
    
    private void stringCollectionTest(Object oid, boolean close,
            boolean isSet) {
        //FIXME jthomas
        //PersistenceManager pm = getPM(!close, !close);
        OpenJPAEntityManager pm =null;
        startTx(pm);
        
        LRSPCIntf pc = (LRSPCIntf) pm.getObjectId(oid);
        
        // check that orig values are correct
        Collection set = isSet ? pc.getStringSet() : pc.getStringCollection();
        assertEquals(3, set.size());
        assertTrue(set.contains("val1"));
        assertTrue(set.contains("val2"));
        assertTrue(set.contains("val3"));
        if (!isSet) {
            Iterator itr = set.iterator();
            assertEquals("val1", itr.next());
            assertEquals("val2", itr.next());
            assertEquals("val3", itr.next());
            //FIXME jthomas
            //KodoJDOHelper.close(itr);
        }
        
        // do some mods to try to confuse the proxy
        set.remove("val1");
        set.remove("val1");
        set.add("val4");
        set.remove("val4");
        set.add("val5");
        set.add("val6");
        assertStringCollectionChanged(set, isSet);
        endTx(pm);
        if (close) {
            pm.close();
            pm = getEm(false, false);
        }
        
        // re-retrieve and check set
        pc = (LRSPCIntf) pm.getObjectId(oid);
        set = isSet ? pc.getStringSet() : pc.getStringCollection();
        assertStringCollectionChanged(set, isSet);
        pm.close();
    }
    
    private void assertStringCollectionChanged(Collection set, boolean isSet) {
        assertEquals(4, set.size());
        assertTrue(!set.contains("val1"));
        assertTrue(set.contains("val2"));
        assertTrue(set.contains("val3"));
        assertTrue(!set.contains("val4"));
        assertTrue(set.contains("val5"));
        assertTrue(set.contains("val6"));
        if (!isSet) {
            Iterator itr = set.iterator();
            assertEquals("val2", itr.next());
            assertEquals("val3", itr.next());
            assertEquals("val5", itr.next());
            assertEquals("val6", itr.next());
            assertTrue(!itr.hasNext());
            //FIXME jthomas
            //KodoJDOHelper.close(itr);
        }
    }
    
    public void testStringMap() {
        stringMapTest(_oid, true);
        stringMapTest(_coid, true);
    }
    
    public void testStringMapRetain() {
        stringMapTest(_oid, false);
        stringMapTest(_coid, false);
    }
    
    private void stringMapTest(Object oid, boolean close) {
        OpenJPAEntityManager pm = getEm(!close, !close);
        startTx(pm);
        LRSPCIntf pc = (LRSPCIntf) pm.getObjectId(oid);
        
        // check that orig values are correct
        Map map = pc.getStringMap();
        assertEquals(3, map.size());
        assertEquals("1", map.get("key1"));
        assertEquals("2", map.get("key2"));
        assertEquals("3", map.get("key3"));
        
        // do some mods to try to confuse the proxy
        map.put("key1", "1a");
        map.put("key1", "1b");
        map.put("key2", "4");
        map.remove("key2");
        map.remove("foo");
        map.put("key5", "5");
        assertStringMapChanged(map);
        endTx(pm);
        
        if (close) {
            pm.close();
            pm = getEm(false, false);
        }
        
        // re-retrieve and check map
        pc = (LRSPCIntf) pm.getObjectId(oid);
        map = pc.getStringMap();
        assertStringMapChanged(map);
        pm.close();
    }
    
    private void assertStringMapChanged(Map map) {
        assertEquals(3, map.size());
        assertEquals("1b", map.get("key1"));
        assertEquals("3", map.get("key3"));
        assertEquals("5", map.get("key5"));
        assertNull(map.get("key2"));
        assertTrue(map.containsKey("key1"));
        assertFalse(map.containsKey("key2"));
        assertTrue(map.containsValue("5"));
        assertFalse(map.containsValue("1"));
        
        Iterator itr = map.entrySet().iterator();
        Map.Entry entry;
        int count = 0;
        for (; itr.hasNext(); count++) {
            entry = (Map.Entry) itr.next();
            if (entry.getKey().equals("key1"))
                assertEquals("1b", entry.getValue());
            else if (entry.getKey().equals("key3"))
                assertEquals("3", entry.getValue());
            else if (entry.getKey().equals("key5"))
                assertEquals("5", entry.getValue());
            else
                fail("Bad key: " + entry.getKey());
        }
        assertEquals(3, count);
        //FIXME
        //KodoJDOHelper.close(itr);
    }
    
    public void testRelSet() {
        relCollectionTest(_oid, true, true);
        relCollectionTest(_coid, true, true);
    }
    
    public void testRelSetRetain() {
        relCollectionTest(_oid, false, true);
        relCollectionTest(_coid, false, true);
    }
    
    public void testRelCollection() {
        relCollectionTest(_oid, true, false);
        relCollectionTest(_coid, true, false);
    }
    
    public void testRelCollectionRetain() {
        relCollectionTest(_oid, false, false);
        relCollectionTest(_coid, false, false);
    }
    
    private void relCollectionTest(Object oid, boolean close, boolean isSet) {
        OpenJPAEntityManager pm = getEm(!close, !close);
        startTx(pm);
        LRSPCIntf pc = (LRSPCIntf) pm.getObjectId(oid);
        
        // check that orig values are correct
        Collection set = isSet ? pc.getRelSet() : pc.getRelCollection();
        assertEquals(3, set.size());
        Collection ordered = new TreeSet();
        Iterator itr = set.iterator();
        ordered.add(itr.next());
        ordered.add(itr.next());
        ordered.add(itr.next());
        assertTrue(!itr.hasNext());
        //FIXME
        //KodoJDOHelper.close(itr);
        itr = ordered.iterator();
        LRSPCIntf set1 = (LRSPCIntf) itr.next();
        if (!isSet) {
            LRSPCIntf set2 = (LRSPCIntf) itr.next();
            LRSPCIntf set3 = (LRSPCIntf) itr.next();
            assertEquals("set1", set1.getStringField());
            assertEquals("set2", set2.getStringField());
            assertEquals("set3", set3.getStringField());
        }
        assertTrue(set.contains(set1));
        assertFalse(set.contains(pc));
        
        // do some mods to try to confuse the proxy
        set.remove(set1);
        set.remove(set1);
        LRSPCIntf set4 = pc.newInstance("set4");
        set.add(set4);
        set.remove(set4);
        LRSPCIntf set5 = pc.newInstance("set5");
        set.add(set5);
        assertRelCollectionChanged(pc, isSet);
        endTx(pm);
        
        if (close) {
            pm.close();
            pm = getEm(false, false);
        }
        
        // re-retrieve and check set
        pc = (LRSPCIntf) pm.getObjectId(oid);
        assertRelCollectionChanged(pc, isSet);
        pm.close();
    }
    
    private void assertRelCollectionChanged(LRSPCIntf pc, boolean isSet) {
        Collection set = isSet ? pc.getRelSet() : pc.getRelCollection();
        assertEquals(3, set.size());
        Collection ordered = new TreeSet();
        Iterator itr = set.iterator();
        ordered.add(itr.next());
        ordered.add(itr.next());
        ordered.add(itr.next());
        assertTrue(!itr.hasNext());
        //FIXME
        //KodoJDOHelper.close(itr);
        itr = ordered.iterator();
        LRSPCIntf set2 = (LRSPCIntf) itr.next();
        if (!isSet) {
            LRSPCIntf set3 = (LRSPCIntf) itr.next();
            LRSPCIntf set5 = (LRSPCIntf) itr.next();
            assertEquals("set2", set2.getStringField());
            assertEquals("set3", set3.getStringField());
            assertEquals("set5", set5.getStringField());
        }
        assertTrue(set.contains(set2));
        assertFalse(set.contains(pc));
    }
    
    public void testRelMap() {
        relMapTest(_oid, true);
        relMapTest(_coid, true);
    }
    
    public void testRelMapRetain() {
        relMapTest(_oid, false);
        relMapTest(_coid, false);
    }
    
    private void relMapTest(Object oid, boolean close) {
        OpenJPAEntityManager pm = getEm(!close, !close);
        startTx(pm);
        LRSPCIntf pc = (LRSPCIntf) pm.getObjectId(oid);
        
        // check that orig values are correct
        Map map = pc.getRelMap();
        assertEquals(3, map.size());
        LRSPCIntf map1 = (LRSPCIntf) map.get("key1");
        LRSPCIntf map2 = (LRSPCIntf) map.get("key2");
        LRSPCIntf map3 = (LRSPCIntf) map.get("key3");
        assertEquals("map1", map1.getStringField());
        assertEquals("map2", map2.getStringField());
        assertEquals("map3", map3.getStringField());
        assertTrue(map.containsKey("key1"));
        assertFalse(map.containsKey("key4"));
        assertTrue(map.containsValue(map1));
        assertFalse(map.containsValue(pc));
        
        // do some mods to try to confuse the proxy
        LRSPCIntf map1a = pc.newInstance("map1a");
        map.put("key1", map1a);
        LRSPCIntf map1b = pc.newInstance("map1b");
        map.put("key1", map1b);
        map.remove("key2");
        map.put("key4", pc.newInstance("map4"));
        map.remove("key4");
        map.remove("foo");
        map.put("key5", pc.newInstance("map5"));
        assertRelMapChanged(pc);
        endTx(pm);
        
        if (close) {
            pm.close();
            pm = getEm(false, false);
        }
        
        // re-retrieve and check map
        pc = (LRSPCIntf) pm.getObjectId(oid);
        assertRelMapChanged(pc);
        pm.close();
    }
    
    private void assertRelMapChanged(LRSPCIntf pc) {
        Map map = pc.getRelMap();
        assertEquals(3, map.size());
        LRSPCIntf map1b = (LRSPCIntf) map.get("key1");
        LRSPCIntf map3 = (LRSPCIntf) map.get("key3");
        LRSPCIntf map5 = (LRSPCIntf) map.get("key5");
        assertEquals("map1b", map1b.getStringField());
        assertEquals("map3", map3.getStringField());
        assertEquals("map5", map5.getStringField());
        assertTrue(map.containsKey("key1"));
        assertFalse(map.containsKey("key2"));
        assertTrue(map.containsValue(map1b));
        assertFalse(map.containsValue(pc));
        
        Iterator itr = map.entrySet().iterator();
        Map.Entry entry;
        int count = 0;
        for (; itr.hasNext(); count++) {
            entry = (Map.Entry) itr.next();
            if (entry.getKey().equals("key1"))
                assertEquals(map1b, entry.getValue());
            else if (entry.getKey().equals("key3"))
                assertEquals(map3, entry.getValue());
            else if (entry.getKey().equals("key5"))
                assertEquals(map5, entry.getValue());
            else
                fail("Bad key: " + entry.getKey());
        }
        assertEquals(3, count);
        //FIXME
        //KodoJDOHelper.close(itr);
    }
    
    public void testTransfer() {
        // cannot transfer an lrs from one field to another
        
        OpenJPAEntityManager pm = getEm(true, true);
        LRSPC pc = (LRSPC) pm.getObjectId(_oid);
        LRSPC map1 = (LRSPC) pc.getRelMap().get("key1");
        assertNotNull(map1);
        
        startTx(pm);
        Map map = pc.getRelMap();
        pc.setRelMap(null);
        map1.setRelMap(map);
        
        try {
            endTx(pm);
            fail("Allowed transfer of lrs field");
        } catch (Exception jue) {
        }
        if (pm.getTransaction().isActive())
            pm.getTransaction().rollback();
        pm.close();
    }
    
    public void testShare() {
        OpenJPAEntityManager pm = getEm(true, true);
        LRSPC pc = (LRSPC) pm.getObjectId(_oid);
        LRSPC map1 = (LRSPC) pc.getRelMap().get("key1");
        assertNotNull(map1);
        
        startTx(pm);
        Map map = pc.getRelMap();
        map1.setRelMap(map);
        endTx(pm);
        assertTrue(pc.getRelMap() != map1.getRelMap());
        assertEquals(3, map1.getRelMap().size());
        assertTrue(map1.getRelMap().containsValue(map1));
        pm.close();
        
        // make sure it sticks
        pm = getEm(true, true);
        pc = (LRSPC) pm.getObjectId(_oid);
        map1 = (LRSPC) pc.getRelMap().get("key1");
        assertEquals(map1, map1.getRelMap().get("key1"));
        pm.close();
    }
    
    public void testRollback() {
        //FIXME
        //PersistenceManagerFactory factory = getPMFactory(new String[]{
        //    "openjpa.RestoreMutableValues", "true",
        //});
        OpenJPAEntityManagerFactory factory =null;
        OpenJPAEntityManager pm = factory.createEntityManager();
        LRSPC pc = (LRSPC) pm.getObjectId(_oid);
        startTx(pm);
        pc.getStringCollection().remove("val2");
        pc.getStringCollection().add("val4");
        rollbackTx(pm);
        assertTrue(pc.getStringCollection().contains("val2"));
        assertFalse(pc.getStringCollection().contains("val4"));
        pm.close();
        factory.close();
    }
    
    public void testReplace() {
        OpenJPAEntityManager pm = getEm(false, false);
        startTx(pm);
        LRSPC pc = (LRSPC) pm.getObjectId(_oid);
        
        // totally replace set
        Collection set = new HashSet();
        set.add("new");
        pc.setStringCollection(set);
        
        endTx(pm);
        pm.close();
        
        // re-retrieve and check set
        pm = getEm(false, false);
        pc = (LRSPC) pm.getObjectId(_oid);
        
        set = pc.getStringCollection();
        assertEquals(1, set.size());
        assertTrue(set.contains("new"));
        pm.close();
    }
    
    public void testAdd()
    throws Exception {
/*
        //FIXME
        //KodoPersistenceManagerFactory pmf = getPMFactory(new String []{
        //    "openjpa.jdbc.JDBCListeners", Listener.class.getName(),
        //});
        OpenJPAEntityManagerFactory pmf =null;
        JDBCConfiguration conf = (JDBCConfiguration) pmf.getConfiguration();
        //FIXME need to fix inner class
        //Listener l = (Listener) conf.getJDBCListenerInstances()[0];
        OpenJPAEntityManager pm = pmf.createEntityManager();
        try {
            startTx(pm);
            LRSPC pc = (LRSPC) pm.getObjectId(_oid);
            l.count = 0;
            pc.getStringCollection().add("testAddStringValue");
            endTx(pm);
            assertEquals(3, l.count);
        } catch (Exception e) {
            if (pm.getTransaction().isActive())
                pm.getTransaction().rollback();
            throw e;
        } finally {
            pm.close();
        }
 */
    }
    
    private OpenJPAEntityManager getEm(boolean optimistic,
            boolean retainValues) {
        OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
        em.setNontransactionalRead(true);
        em.setRetainState(retainValues);
        em.setOptimistic(optimistic);
        return em;
    }
    
    //FIXME - could not find AbstractJDBCListener because of package imports in
    //source file
/*
    public static class Listener extends AbstractJDBCListener {
 
        public int count = 0;
 
        public void afterExecuteStatement(JDBCEvent ev) {
            count++;
        }
    }
 */
}
