/*
 * TestClassDBSequenceFactory.java
 *
 * Created on October 6, 2006, 11:29 AM
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
package org.apache.openjpa.persistence.jdbc.schema;

import java.util.*;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.kernel.ClassTableJDBCSeq;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.schema.Sequence;
import org.apache.openjpa.kernel.Broker;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestClassDBSequenceFactory
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
    
    
    /** Creates a new instance of TestClassDBSequenceFactory */
    public TestClassDBSequenceFactory() {
    }
    
    public TestClassDBSequenceFactory(String test) {
        super(test);
    }
    
    public void setUp() {
       deleteAll(SeqE.class);
    }
    
    boolean supportsPessimisticLocking() {
        OpenJPAConfiguration conf = getConfiguration();
        return conf instanceof JDBCConfiguration
                && ((JDBCConfiguration) conf).getDBDictionaryInstance().
                supportsSelectForUpdate;
    }
    
    public void testVirtualSuperclass() {
        
        OpenJPAEntityManagerFactory pmf =(OpenJPAEntityManagerFactory)
                getEmf(getProps());
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) pmf.createEntityManager();
        //FIXME jthomas
        //Sequence gen = pm.getIdentitySequence(SeqF.class);
        Sequence gen=null;
        long next = ((Number) gen.getIncrement()).longValue();
        //FIXME jthomas
        //gen = pm.getIdentitySequence(SeqG.class);
        assertEquals(next + 1, ((Number) gen.getIncrement()).longValue());
        pm.close();
        pmf.close();
    }
    
    public void testIgnoreVirtualSuperclass() {
        
        Map props=new HashMap();
        props.put("TableName", "JDO_CLASS_SEQUENCE");
        props.put("IgnoreVirtual", "true");
        OpenJPAEntityManagerFactory pmf =(OpenJPAEntityManagerFactory)
                getEmf(props);
        OpenJPAEntityManager pm = pmf.createEntityManager();
        //FIXME jthomas
        //Sequence gen = pm.getIdentitySequence(SeqF.class);
        Sequence gen =null;
        long next = ((Number) gen.getIncrement()).longValue();
        //FIXME jthomas
        //Sequence gen2 = pm.getIdentitySequence(SeqG.class);
        Sequence gen2 =null;
        long next2 = ((Number) gen2.getIncrement()).longValue();
        if (next2 != next + 1)
            return; // valid.
        assertTrue(((Number) gen.getIncrement()).longValue() != next2 + 1);
        
        pm.close();
        pmf.close();
    }
    
    /**
     * Based on reported bug case.
     */
    public void testNoSequenceHolesOnAttach() {
        PerClassTestObject3 pc = new PerClassTestObject3();
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        startTx(pm);;
        pm.persist(pc);
        endTx(pm);;
        long id1 = pc.getId();
        
        pc = new PerClassTestObject3();
        startTx(pm);;
        pm.persist(pc);
        long id2 = pc.getId();
        endTx(pm);;
        pm.close();
        
        pc = new PerClassTestObject3();
        pm = (OpenJPAEntityManager)currentEntityManager();
        startTx(pm);;
        pm.persist(pc);
        endTx(pm);;
        long id3 = pc.getId();
        pm.close();
        
        assertEquals(id1 + 1, id2);
        assertEquals(id2 + 1, id3);
    }
    
    /**
     * Tests that all sequence numbers are unique and in order.
     * Will fail for dbs without pessimistic locking.
     */
    public void testSequence()
    throws Exception {
        if (!(supportsPessimisticLocking()))
            return;
        
        Set set = new HashSet();
        JDBCConfiguration conf = (JDBCConfiguration) getConfiguration();
        Broker broker = getBrokerFactory().newBroker();
        
        
        UpdateThread t1 = new UpdateThread(set, broker);
        UpdateThread t2 = new UpdateThread(set, broker);
        
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        
        if (t1.error != null)
            throw t1.error;
        if (t2.error != null)
            throw t2.error;
        
        assertEquals(102, set.size());
    }
    
    public void testSequenceGenerator() {
        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        
        // make sure the sequence generator creates separate
        // instances.
        for (int i = 0; i < 100; i++) {
            //FIXME jthomas
            /*assertEquals(((Number) pm.getIdentitySequence(SeqD.class).
                    next()).longValue() + 1,
                    ((Number) pm.getIdentitySequence(SeqD.class).
                    getIncrement()).longValue());
             
             */
        }
        // make sure the sequence generate is not the same as is
        // used elsewhere
        
        for (int j = 0; j < 100; j++) {
            //FIXME
            /*
            assertNotEquals(new Long(((Number) pm.getIdentitySequence
                    (SeqA.class).getIncrement()).longValue() + 1),
                    pm.getIdentitySequence(SeqD.class).next());
             */
        }
    }
    
    public static void main(String[] args) {
        //main();
    }
    private Map getProps() {
        Map props=new HashMap();
        props.put("openjpa.Sequence", "db-class(TableName=JDO_CLASS_SEQUENCE");
        return props;
    }
    
    
    
    private static class UpdateThread
            extends Thread {
        
        private Set _set = null;
        private Broker _broker = null;
        public Exception error = null;
        
        public UpdateThread(Set set, Broker broker) {
            _set = set;
            _broker = broker;
        }
        
        public void run() {
            try {
                ClassMapping mapping =
                        ((JDBCConfiguration) _broker.getConfiguration()).
                        getMappingRepositoryInstance().
                        getMapping(SeqD.class, null, true);
                ClassTableJDBCSeq seq = (ClassTableJDBCSeq)
                _broker.getIdentitySequence(mapping);
                for (int i = 0; i < 51; i++)
                    _set.add(seq.next(_broker, mapping));
            } catch (Exception e) {
                error = e;
            }
        }
    }
}
