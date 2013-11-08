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
package org.apache.openjpa.slice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.persistence.EntityManagerFactoryImpl;
import org.apache.openjpa.slice.jdbc.DistributedJDBCBrokerFactory;
import org.apache.openjpa.slice.jdbc.DistributedJDBCConfiguration;

/**
 * Tests user-level configuration is set on per-slice basis.
 * 
 * @author Pinaki Poddar
 * 
 */
public class TestConfiguration extends SliceTestCase {
    protected String getPersistenceUnitName() {
        return "per-slice";
    }

    public void testConfig() {
        assertTrue(emf.getConfiguration() instanceof DistributedConfiguration);
        DistributedJDBCConfiguration conf =
                (DistributedJDBCConfiguration) emf.getConfiguration();
        List<String> slices = conf.getAvailableSliceNames();
        assertTrue(slices.size() > 1);
        assertTrue(slices.contains("One"));
        assertTrue(slices.contains("Two"));
        assertTrue(slices.contains("Three"));
        BrokerFactory bf = ((EntityManagerFactoryImpl) emf).getBrokerFactory();
        Broker broker = bf.newBroker();
        assertEquals(DistributedJDBCBrokerFactory.class, bf.getClass());
        assertEquals(DistributedBrokerImpl.class, broker.getClass());
        assertNotNull(conf.getDistributionPolicyInstance());

        emf.createEntityManager();

        slices = conf.getActiveSliceNames();
        assertTrue(slices.size() > 1);
        assertTrue(slices.contains("One"));
        assertTrue(slices.contains("Two"));
        assertFalse(slices.contains("Three"));
    }
    
    public void testDynamicConfiguration() {
        DistributedJDBCConfiguration conf = (DistributedJDBCConfiguration) emf.getConfiguration();
        List<String> slices = conf.getAvailableSliceNames();
        assertTrue(slices.contains("One"));
        assertTrue(slices.contains("Two"));
        assertTrue(slices.contains("Three"));
        DistributedBrokerFactory bf = (DistributedBrokerFactory)((EntityManagerFactoryImpl) emf).getBrokerFactory();
        Map newProps = new HashMap();
        newProps.put("openjpa.slice.newslice.ConnectionURL",
                "jdbc:derby:target/database/newslice;create=true");
        newProps.put("openjpa.slice.newslice.ConnectionDriverName",
                "org.apache.derby.jdbc.EmbeddedDriver");
        bf.addSlice("newslice", newProps);
        
        assertTrue(conf.getActiveSliceNames().contains("newslice"));
        
        
    }
}
