/*
 * TestPersistentMaps.java
 *
 * Created on October 13, 2006, 1:54 PM
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
package org.apache.openjpa.persistence.kernel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



import org.apache.openjpa.persistence.kernel.common.apps.MapElementPC;
import org.apache.openjpa.persistence.kernel.common.apps.MapElementPCChild;
import org.apache.openjpa.persistence.kernel.common.apps.PersistentMapHolder;

import org.apache.openjpa.persistence.Extent;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;

public class TestPersistentMaps extends BaseKernelTest {

    private static final String JDOQL = "javax.jdo.query.JDOQL";

    /**
     * Creates a new instance of TestPersistentMaps
     */
    public TestPersistentMaps() {
    }

    public TestPersistentMaps(String name) {
        super(name);
    }

    public void setUp() {
        deleteAll(PersistentMapHolder.class);
        OpenJPAEntityManager pm = getPM();
        startTx(pm);

        pm.persist(new PersistentMapHolder());

        endTx(pm);
        endEm(pm);
    }

    private PersistentMapHolder getHolder(OpenJPAEntityManager pm) {
        Extent e = pm.createExtent(PersistentMapHolder.class, true);
        assertSize(1, ((Collection) e.list()));
        return (PersistentMapHolder) e.iterator().next();
    }

    private Object keyInstance(Class type) throws Exception {
        if (type.getName().equals(String.class.getName()))
            return randomString();
        else
            return type.newInstance();
    }

    private void testMap(int num, String name, Class keyClass, Class valueClass)
        throws Exception {
        OpenJPAEntityManager srcpm = getPM(true, true);
        startTx(srcpm);

        PersistentMapHolder holder = getHolder(srcpm);
        Map map = holder.getNamedMap(name);
        Map internalMap = new HashMap();

        for (int i = 0; i < num; i++) {
            Object key = keyInstance(keyClass);
            Object value = keyInstance(valueClass);
            map.put(key, value);
            internalMap.put(key, value);

            assertEquals(map.get(key), internalMap.get(key));
        }
        endTx(srcpm);

        OpenJPAEntityManager pm = getPM();
        startTx(pm);
//        holder = getHolder(pm);
        map = holder.getNamedMap(name);
        for (Iterator i = internalMap.keySet().iterator(); i.hasNext();) {
            Object k = i.next();
            assertEquals(map.get(k), internalMap.get(k));
        }
        endTx(pm);
        endEm(pm);

        String selectWhere =
            "select from " + PersistentMapHolder.class.getName() + " where ";

        pm = getPM();
        startTx(pm);
        for (Iterator i = internalMap.keySet().iterator(); i.hasNext();) {

            Object param = i.next();
            if (pm.isPersistent(param))
                param = pm.find(param.getClass(), pm.getObjectId(param));

            Object val = internalMap.get(param);
            if (pm.isPersistent(val))
                val = pm.find(val.getClass(), pm.getObjectId(val));

            OpenJPAQuery q;

            q = pm.createQuery(JDOQL,
                selectWhere + name + ".containsKey(:param)");
            q.setParameter("param", param);
            assertSize(1, q.getResultList());

            q = pm.createQuery(JDOQL,
                selectWhere + name + ".containsValue(:value)");
            q.setParameter("value", val);
            assertSize(1, q.getResultList());

            q = pm.createQuery(JDOQL, selectWhere + name + ".containsValue(" +
                name + ".get(:param))");
            q.setParameter("param", param);
            assertSize(1, q.getResultList());

            q = pm.createQuery(JDOQL,
                selectWhere + name + ".get(:param) != null");
            q.setParameter("param", param);
            assertSize(1, q.getResultList());

            q = pm.createQuery(JDOQL,
                selectWhere + name + ".get(:param) == :value");
            q.setParameter("param", param);
            q.setParameter("value", val);
            assertSize(1, q.getResultList());

            q = pm.createQuery(JDOQL,
                selectWhere + name + ".get(:param) != :value");
            q.setParameter("param", param);
            q.setParameter("value", val);
            assertSize(0, q.getResultList());
        }
        endTx(pm);
        endEm(pm);

        pm = getPM();
        startTx(pm);
        for (Iterator i = internalMap.keySet().iterator(); i.hasNext();) {
            Object param = i.next();
            if (pm.isPersistent(param))
                param = pm.find(param.getClass(), pm.getObjectId(param));

            List getQueries = new ArrayList(Arrays.asList(new String[]{
                selectWhere + name + ".get(:param) != null",
                selectWhere + name + ".get(:param) == " + name + ".get(:param)",
                selectWhere + "!(" + name + ".get(:param) == null)",
                selectWhere + "!(" + name + ".get(:param) != " + name +
                    ".get(:param))",
            }));

            for (Iterator qi = getQueries.iterator(); qi.hasNext();) {
                String query = (String) qi.next();
                if (valueClass == String.class)
                    query += " order by " + name + ".get(:param) desc";
                OpenJPAQuery q = pm.createQuery(JDOQL, query);
                q.setParameter("param", param);
                assertSize(1, q.getResultList());
            }
        }
        endTx(pm);
        endEm(pm);

        endEm(srcpm);
    }

    public void testPCKeyStringValue()
        throws Exception {
        testMap(5, "testPCKeyStringValue",
            MapElementPC.class, String.class);
    }

    public void testStringKeyPCValue()
        throws Exception {
        testMap(6, "testStringKeyPCValue",
            String.class, MapElementPC.class);
    }

    public void testPCKeyPCValue()
        throws Exception {
        testMap(7, "testPCKeyPCValue",
            MapElementPC.class, MapElementPC.class);
    }

    public void testPCSubKeyStringValue()
        throws Exception {
        testMap(8, "testPCSubKeyStringValue",
            MapElementPCChild.class, String.class);
    }

    public void testStringKeyPCSubValue()
        throws Exception {
        testMap(9, "testStringKeyPCSubValue",
            String.class, MapElementPCChild.class);
    }

    public void testPCSubKeyPCValue()
        throws Exception {
        testMap(10, "testPCSubKeyPCValue",
            MapElementPCChild.class, MapElementPC.class);
    }

    public void testPCSubKeyPCSubValue()
        throws Exception {
        testMap(11, "testPCSubKeyPCSubValue",
            MapElementPCChild.class, MapElementPCChild.class);
    }

    public void testPCKeyPCSubValue()
        throws Exception {
        testMap(12, "testPCKeyPCSubValue",
            MapElementPC.class, MapElementPCChild.class);
    }

    public void testPCIntfKeyStringValue()
        throws Exception {
        testMap(13, "testPCIntfKeyStringValue",
            MapElementPC.class, String.class);
    }

    public void testStringKeyPCIntfValue()
        throws Exception {
        testMap(14, "testStringKeyPCIntfValue",
            String.class, MapElementPC.class);
    }

    public void testPCIntfKeyPCValue()
        throws Exception {
        testMap(15, "testPCIntfKeyPCValue",
            MapElementPC.class, MapElementPC.class);
    }

    /**
     * Test querying maps when there are multiple holder instances that
     * have maps with the same key.
     */
    public void testQueryMultipleMaps() throws Exception {
        deleteAll(PersistentMapHolder.class);

        String mapName = "testStringKeyPCValue";

        String[] mapNames = new String[]{
            "testPCKeyStringValue",
            // "testStringKeyPCValue",
            "testPCKeyPCValue",
            "testPCSubKeyStringValue",
            "testStringKeyPCSubValue",
            "testPCSubKeyPCValue",
            "testPCSubKeyPCSubValue",
            "testPCKeyPCSubValue",
            "testPCIntfKeyStringValue",
            "testStringKeyPCIntfValue",
            "testPCIntfKeyPCValue",
        };

        OpenJPAEntityManager pm;

        pm = getPM();
        startTx(pm);

        MapElementPC pc = new MapElementPC();
        pc.setElementData("foo");

        int max = 5;

        for (int i = 0; i < max; i++) {
            PersistentMapHolder holder = new PersistentMapHolder();
            for (int j = 0; j < i; j++) {
                holder.getNamedMap(mapName).put("key" + j, pc);
            }
            pm.persist(holder);
        }

        endTx(pm);
        endEm(pm);

        pm = getPM();
        pc = (MapElementPC) pm.find(MapElementPC.class, pm.getObjectId(pc));
        for (int i = 0; i < max; i++) {
            OpenJPAQuery q;

            String key = "key" + i;
            String selectWhere = "select from " +
                PersistentMapHolder.class.getName() + " where ";
            q = pm.createQuery(JDOQL,
                selectWhere + mapName + ".containsKey(:key)");
            q.setParameter("key", key);
            assertSize(max - i - 1, q.getResultList());

            q = pm.createQuery(JDOQL,
                selectWhere + mapName + ".get(:key) == :val");
            q.setParameter("key", key);
            q.setParameter("val", pc);
            assertSize(max - i - 1, q.getResultList());

            q = pm.createQuery(JDOQL,
                selectWhere + "testPCKeyStringValue.isEmpty() && "
                    + mapName + ".get(:key) == :val");
            q.setParameter("key", key);
            q.setParameter("val", pc);
            assertSize(max - i - 1, q.getResultList());

            // now try to execute queries against multiple other
            // map instances, so we can make sure the joins are robust
            for (int j = 0; j < mapNames.length; j++) {
                StringBuffer query = new StringBuffer(selectWhere);

                for (int k = 0; k < j; k++) {
                    query.append(mapNames[k] + ".isEmpty() && ");
                }

                q = pm.createQuery(JDOQL,
                    query + mapName + ".get(:key) == :val");
                q.setParameter("key", key);
                q.setParameter("val", pc);
                assertSize(max - i - 1, q.getResultList());
            }
        }
        endEm(pm);
    }
}
