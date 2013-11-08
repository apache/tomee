/*
 * TestIntermediate.java
 *
 * Created on October 3, 2006, 12:01 PM
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

import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.persistence.JPAFacadeHelper;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestIntermediate
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
    
    private Object _pc;
    private Object _rel;
    
    public TestIntermediate(String test) {
        super(test);
    }
    
    /** Creates a new instance of TestIntermediate */
    public TestIntermediate() {
    }
    
    public void setUp() {
        RuntimeTest1 pc = new RuntimeTest1("pc", 1);
        RuntimeTest1 rel = new RuntimeTest1("rel", 2);
        pc.setSelfOneOne(rel);
        
        OpenJPAEntityManager em =(OpenJPAEntityManager)currentEntityManager();
        Broker broker = JPAFacadeHelper.toBroker(em);
        broker.begin();
        broker.persist(pc, null);
        broker.commit();
        _pc = broker.getObjectId(pc);
        _rel = broker.getObjectId(rel);
        broker.close();
        em.close();
    }
    
    public void testOneOneSetsIntermediate() {
        OpenJPAEntityManager em =(OpenJPAEntityManager)currentEntityManager();
        Broker broker = JPAFacadeHelper.toBroker(em);
        try {
            oneOneIntermediateTest(broker);
        } finally {
            broker.close();
            em.close();
        }
    }
    
    public void testDataCacheOneOneSetsIntermediate() {
        BrokerFactory factory = getBrokerFactory(new String[]{
            "openjpa.DataCache", "true",
            "openjpa.RemoteCommitProvider", "sjvm",
        });
        
        // get obj into cache
        Broker broker = factory.newBroker();
        try {
            broker.find(_pc, true, null);
        } finally {
            broker.close();
        }
        
        // test from cache
        broker = factory.newBroker();
        try {
            oneOneIntermediateTest(broker);
        } finally {
            broker.close();
            try {
                factory.close();
            } catch (Exception e) {
            }
        }
    }
    
    /**
     * Helper method to see that the one to one uses an intermediate values.
     */
    private void oneOneIntermediateTest(Broker broker) {
        RuntimeTest1 pc = (RuntimeTest1) broker.find(_pc, true, null);
        OpenJPAStateManager sm = broker.getStateManager(pc);
        assertNotNull(sm);
        FieldMetaData fmd = sm.getMetaData().getField("selfOneOne");
        assertNotNull(fmd);
        assertEquals(_rel, sm.getIntermediate(fmd.getIndex()));
        assertEquals(_rel, broker.getObjectId(pc.getSelfOneOne()));
        assertNull(sm.getIntermediate(fmd.getIndex()));
    }
    
    
}
