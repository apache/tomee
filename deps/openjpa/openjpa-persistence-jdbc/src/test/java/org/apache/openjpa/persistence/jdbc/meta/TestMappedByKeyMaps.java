/*
 * TestMappedByKeyMaps.java
 *
 * Created on October 4, 2006, 9:26 AM
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
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.strats.RelationMapInverseKeyFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.RelationMapTableFieldStrategy;
import org.apache.openjpa.util.AbstractLRSProxyMap;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;


public class TestMappedByKeyMaps
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
    
    /** Creates a new instance of TestMappedByKeyMaps */
    public TestMappedByKeyMaps(String name) 
    {
    	super(name);
    }
    
    public void testInverseKeyMapMapping() {
        JDBCConfiguration conf = (JDBCConfiguration) getConfiguration();
        ClassMapping pc = conf.getMappingRepositoryInstance().getMapping
                (InverseKeyMapPC.class, null, true);
        FieldMapping map = pc.getFieldMapping("helpers");
        
        ClassMapping helper = conf.getMappingRepositoryInstance().getMapping
                (HelperPC.class, null, true);
        FieldMapping str = helper.getFieldMapping("stringField");
        
        assertEquals("stringField", map.getKey().getValueMappedBy());
        assertEquals(str, map.getKey().getValueMappedByMetaData());
        assertTrue(map.getStrategy() instanceof
                RelationMapInverseKeyFieldStrategy);
        assertEquals(1, map.getKeyMapping().getColumns().length);
        assertEquals(map.getKeyMapping().getColumns()[0],
                str.getColumns()[0]);
    }
    
    public void testJoinTableMapMapping() {
        JDBCConfiguration conf = (JDBCConfiguration) getConfiguration();
        ClassMapping pc = conf.getMappingRepositoryInstance().getMapping
                (JoinTableMapPC.class, null, true);
        FieldMapping map = pc.getFieldMapping("helpers");
        
        ClassMapping helper = conf.getMappingRepositoryInstance().getMapping
                (HelperPC.class, null, true);
        FieldMapping str = helper.getFieldMapping("stringField");
        
        assertEquals("stringField", map.getKey().getValueMappedBy());
        assertEquals(str, map.getKey().getValueMappedByMetaData());
        assertTrue(map.getStrategy() instanceof RelationMapTableFieldStrategy);
        assertEquals(1, map.getKeyMapping().getColumns().length);
        assertEquals(map.getKeyMapping().getColumns()[0],
                str.getColumns()[0]);
    }
    
    public void testInverseKeyMap() {
        mappedByMap(new InverseKeyMapPC(), false);
        queryMap(new InverseKeyMapPC());
    }
    
    public void testInverseKeyLRSMap() {
        mappedByMap(new InverseKeyMapPC(), true);
    }
    
    public void testJoinTableMap() {
        mappedByMap(new JoinTableMapPC(), false);
        queryMap(new JoinTableMapPC());
    }
    
    public void testJoinTableLRSMap() {
        mappedByMap(new JoinTableMapPC(), true);
    }
    
    private void mappedByMap(MappedByMapPC pc, boolean lrs) {
       deleteAll(HelperPC.class);
       deleteAll(pc.getClass());
        
        HelperPC h1 = new HelperPC();
        h1.setStringField("h1");
        pc.getHelpers().put(h1.getStringField(), h1);
        HelperPC h2 = new HelperPC();
        h2.setStringField("h2");
        pc.getHelpers().put(h2.getStringField(), h2);
        HelperPC h3 = new HelperPC();
        h3.setStringField("h3");
        pc.getHelpers().put(h3.getStringField(), h3);
        
        setLRS(pc.getClass(), lrs);
        try {
            OpenJPAEntityManager pm =
                (OpenJPAEntityManager)currentEntityManager();
            startTx(pm);;
            pm.persist(pc);
            endTx(pm);;
            Object oid = pm.getObjectId(pc);
            
            assertFalse(pc.getHelpers().containsKey("foo"));
            assertNull(pc.getHelpers().get("foo"));
            assertEquals(3, pc.getHelpers().size());
            assertEquals(h1, pc.getHelpers().get("h1"));
            assertEquals(h2, pc.getHelpers().get("h2"));
            pm.close();
            
            pm = (OpenJPAEntityManager)currentEntityManager();;
            pc = (MappedByMapPC) pm.getObjectId(oid);
            if (lrs)
                assertTrue(pc.getHelpers() instanceof AbstractLRSProxyMap);
            assertEquals(3, pc.getHelpers().size());
            assertFalse(pc.getHelpers().containsKey("foo"));
            assertNull(pc.getHelpers().get("foo"));
            assertEquals("h1", ((HelperPC) pc.getHelpers().get("h1")).
                    getStringField());
            assertEquals("h2", ((HelperPC) pc.getHelpers().get("h2")).
                    getStringField());
            
            pm.begin();
            pc.getHelpers().remove("h1");
            assertEquals(2, pc.getHelpers().size());
            assertFalse(pc.getHelpers().containsKey("h1"));
            assertNull(pc.getHelpers().get("h1"));
            HelperPC h4 = new HelperPC();
            h4.setStringField("h4");
            pc.getHelpers().put("h4", h4);
            assertTrue(pc.getHelpers().containsKey("h4"));
            assertEquals(h4, pc.getHelpers().get("h4"));
            assertEquals(3, pc.getHelpers().size());
            pm.commit();
            assertEquals(3, pc.getHelpers().size());
            assertFalse(pc.getHelpers().containsKey("h1"));
            assertNull(pc.getHelpers().get("h1"));
            assertEquals("h2", ((HelperPC) pc.getHelpers().get("h2")).
                    getStringField());
            assertEquals("h4", ((HelperPC) pc.getHelpers().get("h4")).
                    getStringField());
            pm.close();
            
            pm = (OpenJPAEntityManager)currentEntityManager();;
            pc = (MappedByMapPC) pm.getObjectId(oid);
            assertEquals(3, pc.getHelpers().size());
            assertFalse(pc.getHelpers().containsKey("h1"));
            assertNull(pc.getHelpers().get("h1"));
            assertEquals("h2", ((HelperPC) pc.getHelpers().get("h2")).
                    getStringField());
            assertEquals("h4", ((HelperPC) pc.getHelpers().get("h4")).
                    getStringField());
            
            // to test lrs functions
            assertTrue(pc.getHelpers().containsValue
                    (pc.getHelpers().get("h2")));
            
            Set keySet = pc.getHelpers().keySet();
            Set ordered = new TreeSet();
            assertEquals(3, keySet.size());
            Iterator itr = keySet.iterator();
            while (itr.hasNext())
                ordered.add(itr.next());
            //FIXME jthomas
            //KodoJDOHelper.close(itr);
            assertEquals(3, ordered.size());
            assertTrue(ordered.contains("h2"));
            assertTrue(ordered.contains("h3"));
            assertTrue(ordered.contains("h4"));
            ordered.clear();
            
            Collection values = pc.getHelpers().values();
            assertEquals(3, values.size());
            itr = values.iterator();
            while (itr.hasNext()) {
                Object next = itr.next();
                assertTrue(next instanceof HelperPC);
                ordered.add(((HelperPC) next).getStringField());
            }
            //FIXME jthomas
            //KodoJDOHelper.close(itr);
            assertEquals(3, ordered.size());
            assertTrue(ordered.contains("h2"));
            assertTrue(ordered.contains("h3"));
            assertTrue(ordered.contains("h4"));
            pm.close();
        } finally {
            unsetLRS(pc.getClass());
        }
    }
    
    private void queryMap(MappedByMapPC pc) {
        HelperPC h5 = new HelperPC();
        h5.setStringField("h5");
        pc.getHelpers().put("h5", h5);
        OpenJPAEntityManager pm = (OpenJPAEntityManager)currentEntityManager();
        pm.begin();
        pm.persist(pc);
        pm.commit();
        pm.close();
        
        pm = (OpenJPAEntityManager)currentEntityManager();;
        OpenJPAQuery q = pm.createNativeQuery("stringField == 'h2'",
                HelperPC.class);
        //FIXME jthomas
        //q.setUnique(true);
        HelperPC h2 = (HelperPC) q.getSingleResult();
        
        q = pm.createNativeQuery("helpers.containsKey ('h2')",pc.getClass());
        //FIXME jthomas
        //q.setUnique(true);
        pc = (MappedByMapPC) q.getSingleResult();
        assertEquals(3, pc.getHelpers().size());
        assertEquals(h2, pc.getHelpers().get("h2"));
        
        q = pm.createNativeQuery("helpers.containsValue (:h2)",pc.getClass());
        //FIXME  jthomas
        //q.setUnique(true);
        pc = (MappedByMapPC) q.getSingleResult();
        assertEquals(3, pc.getHelpers().size());
        assertEquals(h2, pc.getHelpers().get("h2"));
        pm.close();
    }
    
    private void setLRS(Class cls, boolean lrs) {
        ClassMapping cm = ((JDBCConfiguration) getConfiguration()).
                getMappingRepositoryInstance().getMapping(cls, null, true);
        cm.getFieldMapping("helpers").setLRS(lrs);
    }
    
    private void unsetLRS(Class cls) {
        ClassMapping cm = ((JDBCConfiguration) getConfiguration()).
                getMappingRepositoryInstance().getMapping(cls, null, true);
        cm.getFieldMapping("helpers").setLRS(false);
    }
    
}
