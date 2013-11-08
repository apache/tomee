/*
 * TestClassSequenceFactory.java
 *
 * Created on October 6, 2006, 12:21 PM
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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.kernel.ClassTableJDBCSeq;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.Seq;
import org.apache.openjpa.persistence.jdbc.common.apps.SeqA;
import org.apache.openjpa.persistence.jdbc.common.apps.SeqB;
import org.apache.openjpa.persistence.jdbc.common.apps.SeqC;
import org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest;
import org.apache.openjpa.persistence.test.AllowFailure;

@AllowFailure(message="This test only applies to run with Oracle. This test also is not functional; disable until fix")
public class TestClassSequenceFactory extends BaseJDBCTest{
    
    private static Map _sysprops = new HashMap();
    
    Broker _broker;
    
    /** Creates a new instance of TestClassSequenceFactory */
    public TestClassSequenceFactory() {
    }
    public TestClassSequenceFactory(String test) {
        super(test);
    }
    
    public void setUp()
    throws Exception {
        JDBCConfiguration conf = new JDBCConfigurationImpl();
        conf.fromProperties(getProperties());
        if (!adjustConfiguration(conf))
            return;
        
        String driver = conf.getConnectionDriverName().toLowerCase();
        String [] sql = null;
        
        if (driver.indexOf("oracle") >= 0) {
            sql = new String []{
                "create sequence seqa_seq",
                "create sequence seqb_seq"
            };
        }
        
        if (sql == null)
            return;
        
        DataSource ds = conf.getDataSource2(null);
        Connection c = ds.getConnection();
        Statement s = null;
        try {
            s = c.createStatement();
            for (int i = 0; i < sql.length; i++) {
                try {
                    s.execute(sql[i]);
                } catch (SQLException sqe) {
                }
            }
        } finally {
            if (s != null)
                try {
                    s.close();
                } catch (Exception e) {
                }
        }
        
        _broker = getBrokerFactory().newBroker();
    }
    
    /**
     * Tests that all sequence numbers are unique and in order.
     */
    public void testSequence()
    throws Exception {
        Set set = new HashSet();
        
        JDBCConfiguration conf = new JDBCConfigurationImpl();
        conf.fromProperties(getProperties());
        if (!adjustConfiguration(conf))
            return;
        Thread t1 = new UpdateThread(set, conf);
        Thread t2 = new UpdateThread(set, conf);
        
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        
        assertEquals(102, set.size());
    }
    
    public void testExtensions()
    throws Exception {
        JDBCConfiguration conf = new JDBCConfigurationImpl();
        conf.fromProperties(getProperties());
        if (!adjustConfiguration(conf))
            return;
        
        ClassMapping aMapping = conf.getMappingRepositoryInstance().
                getMapping(SeqA.class, null, true);
        ClassMapping bMapping = conf.getMappingRepositoryInstance().
                getMapping(SeqB.class, null, true);
        ClassMapping cMapping = conf.getMappingRepositoryInstance().
                getMapping(SeqC.class, null, true);
        DataSource ds = conf.getDataSource2(null);
        
        // hold a and c and start b
        
        Seq seq = conf.getSequenceInstance();
        long aid = ((Long) seq.next(_broker, aMapping)).longValue();
        for (int i = 0; i < 5; i++)
            seq.next(_broker, bMapping);
        
        assertEquals(new Long(aid + 1), seq.next(_broker, aMapping));
        assertEquals(new Long(aid + 2), seq.next(_broker, cMapping));
    }
    
    /**
     * Pass in a mutable configuration
     * <p/>
     * return true if useable.
     */
    private boolean adjustConfiguration(JDBCConfiguration conf) {
        String driver = conf.getConnectionDriverName();
        if (driver == null)
            return false;
        driver = driver.toLowerCase();
        if (driver.indexOf("oracle") >= 0) {
            conf.setSequence(ClassTableJDBCSeq.class.getName());
            return true;
        }
        
        return false;
    }
    
    public static void main(String[] args) {
//        main();
    }
    
    
    private class UpdateThread
            extends Thread {
        
        private JDBCConfiguration _conf;
        private Set _set = null;
        
        public UpdateThread(Set set, JDBCConfiguration conf) {
            _set = set;
            _conf = conf;
        }
        
        public void run() {
            DataSource ds = _conf.getDataSource2(null);
            try {
                Seq seq = _conf.getSequenceInstance();
                ClassMapping mapping = _conf.getMappingRepositoryInstance().
                        getMapping(SeqA.class, null, true);
                for (int i = 0; i < 51; i++)
                    _set.add(seq.next(_broker, mapping));
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }
    
}
