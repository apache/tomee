/*
 * TestDBSequenceFactory.java
 *
 * Created on October 6, 2006, 1:24 PM
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
import javax.sql.DataSource;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.kernel.TableJDBCSeq;
import org.apache.openjpa.kernel.Broker;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


public class TestDBSequenceFactory
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
        
    private Broker _broker;
    
    /** Creates a new instance of TestDBSequenceFactory */
    public TestDBSequenceFactory() {
    }

    public TestDBSequenceFactory(String test) {
        super(test);
    }

    /**
     * Tests that all sequence numbers are unique and in order.
     * Will fail for dbs without pessimistic locking.
     */
    public void testSequence()
        throws Exception {
        _broker = getBrokerFactory().newBroker();

        Set set = Collections.synchronizedSet(new HashSet());
        Thread t1 = new UpdateThread(set);
        Thread t2 = new UpdateThread(set);

        t1.start();
        t2.start();
        t1.join(5 * 60 * 1000);
        t2.join(5 * 60 * 1000);

        assertFalse(t1.isAlive());
        assertFalse(t2.isAlive());

        assertEquals(102, set.size());
    }

    public static void main(String[] args) {
        //main();
    }

    private class UpdateThread
        extends Thread {

        private Set _set = null;

        public UpdateThread(Set set) {
            _set = set;
        }

        public void run() {
            try {
                JDBCConfiguration conf = new JDBCConfigurationImpl();
                TableJDBCSeq seq = new TableJDBCSeq();
                seq.setConfiguration(conf);
                seq.startConfiguration();
                seq.endConfiguration();
                DataSource ds = conf.getDataSource2(null);
                for (int i = 0; i < 51; i++)
                    _set.add(seq.next(_broker, 
                        conf.getMetaDataRepositoryInstance().
                        getMetaData(RuntimeTest1.class, null, true)));
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }
    
}
