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
package org.apache.openjpa.persistence.kernel;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


import org.apache.openjpa.persistence.kernel.common.apps.ProxiesPC;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.util.Proxy;

public class TestProxies2 extends BaseKernelTest {

    private int _oid = 0;
    private Date _date = null;
    private java.sql.Date _sqlDate = null;
    private java.sql.Timestamp _timestamp = null;

    public TestProxies2(String casename) {
        super(casename);
    }

    public void setUp() throws Exception {
        super.setUp(ProxiesPC.class);

        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);

        long now = System.currentTimeMillis();
        _date = new Date(now);
        _sqlDate = new java.sql.Date(now);
        _timestamp = new java.sql.Timestamp(now);

        ProxiesPC pc = new ProxiesPC("main");
        pc.setDate(_date);
        pc.setSQLDate(_sqlDate);
        pc.setTimestamp(_timestamp);

        pc.getStringSet().add("val1");
        pc.getStringSet().add("val2");
        pc.getStringSet().add("val3");
        pc.getStringSet().add(null);

        pc.getProxySet().add(new ProxiesPC("set1"));
        pc.getProxySet().add(new ProxiesPC("set2"));
        pc.getProxySet().add(new ProxiesPC("set3"));

        pc.getStringMap().put("key1", "1");
        pc.getStringMap().put("key2", "2");
        pc.getStringMap().put("key3", "3");
        pc.getStringMap().put(null, "null");
        pc.getStringMap().put("null", null);

        pc.getProxyMap().put("key1", new ProxiesPC("map1"));
        pc.getProxyMap().put("key2", new ProxiesPC("map2"));
        pc.getProxyMap().put("key3", new ProxiesPC("map3"));

        pc.getList().add("val1");
        pc.getList().add("val1");
        pc.getList().add("val2");
        pc.getList().add("val3");
        pc.getList().add("val3");

        pm.persist(pc);

        _oid = pc.getId();

        endTx(pm);
        endEm(pm);
    }

    public void testStringSet() {
        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);
        ProxiesPC pc = pm.find(ProxiesPC.class, _oid);

        // check that orig values are correct
        Set set = pc.getStringSet();
        assertEquals(4, set.size());
        assertTrue(set.contains("val1"));
        assertTrue(set.contains("val2"));
        assertTrue(set.contains("val3"));
        assertTrue(set.contains(null));

        // do some mods to try to confuse the proxy
        set.remove("val1");
        set.remove("val1");
        set.add("val4");
        set.remove("val4");
        set.add("val5");
        set.add("val5");
        endTx(pm);
        endEm(pm);

        // re-retrieve and check set
        pm = getPM(false, false);
        pc = pm.find(ProxiesPC.class, _oid);

        set = pc.getStringSet();
        assertEquals(4, set.size());
        assertTrue(!set.contains("val1"));
        assertTrue(set.contains("val2"));
        assertTrue(set.contains("val3"));
        assertTrue(!set.contains("val4"));
        assertTrue(set.contains("val5"));
        assertTrue(set.contains(null));
        endEm(pm);
    }

    public void testStringMap() {
        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);
        ProxiesPC pc = pm.find(ProxiesPC.class, _oid);

        // check that orig values are correct
        Map map = pc.getStringMap();
        assertEquals(5, map.size());
        assertEquals("1", map.get("key1"));
        assertEquals("2", map.get("key2"));
        assertEquals("3", map.get("key3"));
        assertNull(map.get("null"));
        assertEquals("null", map.get(null));

        // do some mods to try to confuse the proxy
        map.put("key1", "1a");
        map.put("key1", "1b");
        map.put("key4", "4");
        map.remove("key4");
        map.remove("foo");
        map.put("key5", "5");
        endTx(pm);
        endEm(pm);

        // re-retrieve and check map
        pm = getPM(false, false);
        pc = pm.find(ProxiesPC.class, _oid);

        map = pc.getStringMap();
        assertEquals(6, map.size());
        assertEquals("1b", map.get("key1"));
        assertEquals("5", map.get("key5"));
        endEm(pm);
    }

    public void testProxySet() {
        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);
        ProxiesPC pc = pm.find(ProxiesPC.class, _oid);

        // check that orig values are correct
        Set set = pc.getProxySet();
        assertEquals(3, set.size());
        Iterator itr = set.iterator();
        ProxiesPC set1 = (ProxiesPC) itr.next();
        ProxiesPC set2 = (ProxiesPC) itr.next();
        ProxiesPC set3 = (ProxiesPC) itr.next();
        assertEquals("set1", set1.getName());
        assertEquals("set2", set2.getName());
        assertEquals("set3", set3.getName());

        // do some mods to try to confuse the proxy
        set.remove(set1);
        set.remove(set1);
        ProxiesPC set4 = new ProxiesPC("set4");
        set.add(set4);
        set.remove(set4);
        ProxiesPC set5 = new ProxiesPC("set5");
        set.add(set5);
        set.add(set5);
        endTx(pm);
        endEm(pm);

        // re-retrieve and check set
        pm = getPM(true, false);
        startTx(pm);
        pc = pm.find(ProxiesPC.class, _oid);
        pm.refresh(pc);

        set = pc.getProxySet();
        assertEquals(3, set.size());
        itr = set.iterator();
        set1 = (ProxiesPC) itr.next();
        set2 = (ProxiesPC) itr.next();
        set3 = (ProxiesPC) itr.next();
        assertEquals("set2", set1.getName());
        assertEquals("set3", set2.getName());
        assertEquals("set5", set3.getName());
        endTx(pm);
        endEm(pm);
    }

    public void testProxyMap() {
        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);
        ProxiesPC pc = pm.find(ProxiesPC.class, _oid);

        // check that orig values are correct
        Map map = pc.getProxyMap();
        assertEquals("original map size is correct: 3", 3, map.size());
        ProxiesPC map1 = (ProxiesPC) map.get("key1");
        ProxiesPC map2 = (ProxiesPC) map.get("key2");
        ProxiesPC map3 = (ProxiesPC) map.get("key3");
        assertEquals("map1", map1.getName());
        assertEquals("map2", map2.getName());
        assertEquals("map3", map3.getName());

        // do some mods to try to confuse the proxy
        ProxiesPC map1a = new ProxiesPC("map1a");
        map.put("key1", map1a);
        ProxiesPC map1b = new ProxiesPC("map1b");
        map.put("key1", map1b);
        map.put("key4", new ProxiesPC("map4"));
        map.remove("key4");
        map.remove("foo");
        map.put("key5", new ProxiesPC("map5"));
        endTx(pm);
        endEm(pm);

        // re-retrieve and check map
        pm = getPM(false, false);
        pc = pm.find(ProxiesPC.class, _oid);
        startTx(pm);
        pm.refresh(pc);

        map = pc.getProxyMap();

        assertEquals(4, map.size());
        assertEquals("map1b", ((ProxiesPC) map.get("key1")).getName());
        assertEquals("map5", ((ProxiesPC) map.get("key5")).getName());

        endTx(pm);
        endEm(pm);
    }

    public void testReplace() {
        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);
        ProxiesPC pc = pm.find(ProxiesPC.class, _oid);

        // totally replace set
        Set set = new HashSet();
        set.add("new");
        pc.setStringSet(set);

        endTx(pm);
        endEm(pm);

        // re-retrieve and check set
        pm = getPM(false, false);
        pc = pm.find(ProxiesPC.class, _oid);

        set = pc.getStringSet();
        assertEquals(1, set.size());
        assertTrue(set.contains("new"));
        endEm(pm);
    }

    public void testComparators() {
        // make sure the system uses the initial field value to find
        // comparators
        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);
        ProxiesPC pc = pm.find(ProxiesPC.class, _oid);
        assertNotNull("pc is null", pc);
        assertTrue("pc.getComp() is not instanceof Proxy",
            pc.getComp() instanceof Proxy);
        assertTrue(
  "(TreeSet) is not pc.getComp()).comparator() instanceof ComparableComparator",
            ((TreeSet) pc.getComp())
                .comparator() instanceof ComparableComparator);
        pm.evict(pc);
        endTx(pm);

        // see if it still saves comparator after transition to hollow
        // and back
        assertTrue("pc.getComp() is not instanceof ProxyTreeSet",
            pc.getComp() instanceof Proxy);
        Comparator compart = ((TreeSet) pc.getComp()).comparator();
        assertNotNull("compart is null", compart);
        assertTrue(
  "((TreeSet) is not pc.getComp()).comparator()instanceof ComparableComparator",
            ((TreeSet) pc.getComp())
                .comparator() instanceof ComparableComparator);

        endEm(pm);
    }

    //    FIX ME: Moving fix to essex
    /*public void testList() {
        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);
        ProxiesPC pc = pm.find(ProxiesPC.class, _oid);

        // check that orig values are correct
        List list = pc.getList();
        assertEquals(5, list.size());
        assertEquals("val1", list.get(0));
        assertEquals("val1", list.get(1));
        assertEquals("val2", list.get(2));
        assertEquals("val3", list.get(3));
        assertEquals("val3", list.get(4));

        // do some mods to try to confuse the proxy
        list.remove("val2");
        list.add("val4");
        list.remove("val4");
        list.add("val5");
        list.add("val6");
        list.add("val6");
        endTx(pm);
        endEm(pm);

        // re-retrieve and modify again to check holes in ordering
        pm = getPM(false, false);
        startTx(pm);
        pc = (ProxiesPC) pm.find(ProxiesPC.class, _oid);

        list = pc.getList();
        assertEquals(7, list.size());
        assertEquals("val1", list.get(0));
        assertEquals("val1", list.get(1));
        assertEquals("val3", list.get(2));
        assertEquals("val3", list.get(3));
        assertEquals("val5", list.get(4));
        assertEquals("val6", list.get(5));
        assertEquals("val6", list.get(6));

        list.remove("val5");
        list.add("val7");
        endTx(pm);
        endEm(pm);

        // re-retrieve and check final contents
        pm = getPM(false, false);
        pc = pm.find(ProxiesPC.class, _oid);

        list = pc.getList();
        assertEquals(7, list.size());
        assertEquals("val1", list.get(0));
        assertEquals("val1", list.get(1));
        assertEquals("val3", list.get(2));
        assertEquals("val3", list.get(3));
        assertEquals("val6", list.get(4));
        assertEquals("val6", list.get(5));
        assertEquals("val7", list.get(6));
        endEm(pm);
    }

    public void testListDisablesChangeTracking() {
        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);
        ProxiesPC pc = pm.find(ProxiesPC.class, _oid);

        // check that orig values are correct
        List list = pc.getList();
        assertEquals(5, list.size());
        assertEquals("val1", list.get(0));
        assertEquals("val1", list.get(1));
        assertEquals("val2", list.get(2));
        assertEquals("val3", list.get(3));
        assertEquals("val3", list.get(4));

        // removing a copy of val3 should disable tracking
        list.remove("val2");
        list.remove("val3");
        list.add("val5");
        list.add("val5");
        endTx(pm);
        endEm(pm);

        // re-retrieve and change again to check ordering
        pm = getPM(false, false);
        startTx(pm);
        pc = pm.find(ProxiesPC.class, _oid);

        list = pc.getList();
        assertEquals(5, list.size());
        assertEquals("val1", list.get(0));
        assertEquals("val1", list.get(1));
        assertEquals("val3", list.get(2));
        assertEquals("val5", list.get(3));
        assertEquals("val5", list.get(4));

        list.remove("val3");
        list.add("val6");
        endTx(pm);
        endEm(pm);

        // check final contents
        pm = getPM(false, false);
        pc = pm.find(ProxiesPC.class, _oid);

        list = pc.getList();
        assertEquals(5, list.size());
        assertEquals("val1", list.get(0));
        assertEquals("val1", list.get(1));
        assertEquals("val6", list.get(2));
        assertEquals("val5", list.get(3));
        assertEquals("val5", list.get(4));
        endEm(pm);
    }
*/
    public void testChangeListOrder() {
        OpenJPAEntityManager pm = getPM(false, false);
        startTx(pm);
        ProxiesPC pc = pm.find(ProxiesPC.class, _oid);

        // check that orig values are correct
        List list = pc.getList();
        assertEquals(5, list.size());
        assertEquals("val1", list.get(0));
        assertEquals("val1", list.get(1));
        assertEquals("val2", list.get(2));
        assertEquals("val3", list.get(3));
        assertEquals("val3", list.get(4));

        // reorder val2
        list.remove("val2");
        list.add("val2");
        endTx(pm);
        endEm(pm);

        // re-retrieve to check ordering
        pm = getPM(false, false);
        pc = pm.find(ProxiesPC.class, _oid);

        list = pc.getList();
        assertEquals(5, list.size());
        assertEquals("val1", list.get(0));
        assertEquals("val1", list.get(1));
        assertEquals("val2", list.get(2));
        assertEquals("val3", list.get(3));
        assertEquals("val3", list.get(4));
        endEm(pm);
    }

    public void testDate() {
        OpenJPAEntityManager pm = getPM(true, true);
        startTx(pm);
        ProxiesPC pc = pm.find(ProxiesPC.class, _oid);
        Date date = pc.getDate();
        assertNotNull(date);

        // dates can lose precision, but make sure same day
        assertEquals(_date.getYear(), date.getYear());
        assertEquals(_date.getMonth(), date.getMonth());
        assertEquals(_date.getDate(), date.getDate());

        // make sure proxied
        assertTrue(!pm.isDirty(pc));
        date.setTime(System.currentTimeMillis() + 1000 * 60 * 60 * 24);
        assertTrue(pm.isDirty(pc));

        endTx(pm);
        assertEquals(date, pc.getDate());
        endEm(pm);
    }

    public void testSQLDate() {
        OpenJPAEntityManager pm = getPM(true, true);
        startTx(pm);
        ProxiesPC pc = pm.find(ProxiesPC.class, _oid);
        java.sql.Date date = pc.getSQLDate();
        assertNotNull(date);

        // dates can lose precision, but make sure same day
        assertEquals(_sqlDate.getYear(), date.getYear());
        assertEquals(_sqlDate.getMonth(), date.getMonth());
        assertEquals(_sqlDate.getDate(), date.getDate());

        // make sure proxied
        assertTrue(!pm.isDirty(pc));
        date.setTime(System.currentTimeMillis() + 1000 * 60 * 60 * 24);
        assertTrue(pm.isDirty(pc));

        endTx(pm);
        assertEquals(date, pc.getSQLDate());
        endEm(pm);
    }

    public void testTimestamp() {
        OpenJPAEntityManager pm = getPM(true, true);
        startTx(pm);
        ProxiesPC pc = pm.find(ProxiesPC.class, _oid);
        java.sql.Timestamp tstamp = pc.getTimestamp();
        assertNotNull(tstamp);

        // dates can lose precision, but make sure same day
        assertEquals(_timestamp.getYear(), tstamp.getYear());
        assertEquals(_timestamp.getMonth(), tstamp.getMonth());
        assertEquals(_timestamp.getDate(), tstamp.getDate());

        // make sure proxied
        assertTrue(!pm.isDirty(pc));
        tstamp.setNanos(100);
        assertTrue(pm.isDirty(pc));

        endTx(pm);
        assertEquals(tstamp, pc.getTimestamp());
        endEm(pm);
    }
}
