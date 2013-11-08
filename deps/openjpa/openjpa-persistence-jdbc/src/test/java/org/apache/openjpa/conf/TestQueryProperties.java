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
package org.apache.openjpa.conf;

import java.util.HashMap;
import java.util.Map;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.query.common.apps.QTimeout;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests the following JPA 2.0 Persistence Unit Properties scenarios:
 *   1) persistence.xml provided properties 
 *      1a) No PU properties provided is same as no timeout (JDBC defined)
 *      1b) PU provided properties translated into config
 *   2) Map of properties provided to createEMF()
 *      2a) EMF props can be set when no PU props provided
 *      2b) EMF props override PU set properties in config
 *   3) QueryHints override default values from PU or EMF (2b)
 *   4) Query.setHint() 
 *      4a) can override default values from PU or EMF (2a)
 *      4b) can override QueryHints (3)
 *
 * @version $Rev$ $Date$
 */
public class TestQueryProperties extends SingleEMFTestCase {
    
    private Map<String,String> props = null;

    @Override
    public void setUp() throws Exception {
        // setup using a simple entity
        super.setUp(QTimeout.class, CLEAR_TABLES);
        // create the Map to test overrides
        props = new HashMap<String,String>();
        props.put("javax.persistence.lock.timeout", "12000");
        props.put("javax.persistence.query.timeout", "7000");
    }
    
    public void testNoProperties() {
        getLog().trace("testNoProperties() - no properties in persistence.xml");
        OpenJPAEntityManagerFactory emf1 = null, emf2 = null;
        OpenJPAEntityManager em1 = null, em2 = null;
        
        try {
            OpenJPAQuery q;
            Map<String, Object> hints;
            Integer timeout;
            Integer lTime = new Integer(0);
            Integer qTime = new Integer(0);
            
            // create our PU without properties
            emf1 = OpenJPAPersistence.createEntityManagerFactory(
                "qtimeout-no-properties", "persistence3.xml");
            assertNotNull(emf1);
            emf2 = OpenJPAPersistence.createEntityManagerFactory(
                "qtimeout-no-properties", "persistence3.xml", props);
            assertNotNull(emf2);
            
            //=============
            // Test for 1a)
            //=============
            // verify no config properties from persistence.xml
            OpenJPAConfiguration conf1 = emf1.getConfiguration();
            assertNotNull(conf1);
            assertEquals("Expected no default lock timeout", lTime.intValue(),
                conf1.getLockTimeout());
            assertEquals("Expected no default query timeout", qTime.intValue(),
                conf1.getQueryTimeout());
            // verify Query receives no properties
            em1 = emf1.createEntityManager();
            assertNotNull(em1);
            q = em1.createNamedQuery("NoHintList");
            // verify no Query hints
            hints = q.getHints();
            assertFalse(hints.containsKey("javax.persistence.lock.timeout"));
            assertFalse(hints.containsKey("javax.persistence.query.timeout"));
            // verify default config values of no timeouts
            timeout = q.getFetchPlan().getLockTimeout();
            assertEquals("Expected no default lock timeout", lTime.intValue(),
                timeout.intValue());
            timeout = q.getFetchPlan().getQueryTimeout();
            assertEquals("Expected no default query timeout", qTime.intValue(),
                timeout.intValue());

            //=============
            // Test for 2a)
            //=============
            // verify properties in Map override persistence.xml
            OpenJPAConfiguration conf2 = emf2.getConfiguration();
            assertNotNull(conf2);
            lTime = 12000;
            qTime = 7000;
            assertEquals("Expected Map updated lock timeout", lTime.intValue(),
                conf2.getLockTimeout());
            assertEquals("Expected Map updated query timeout", qTime.intValue(),
                conf2.getQueryTimeout());
            // Verify Query receives the properties
            em2 = emf2.createEntityManager();
            assertNotNull(em2);
            q = em2.createNamedQuery("NoHintList");
            // Cannot verify properties are passed through as Query hints
            /*
             * Following test would fail, as the code currently does not pass
             * the properties down as hints, but only as config settings.
             * 
             * The spec says that PU or Map provided properties to the EMF
             * will be used as defaults and that Query.setHint() can be used
             * to override, but there is no requirement for getHints() to 
             * return these default values.
             *  
            hints = q.getHints();
            assertTrue(hints.containsKey("javax.persistence.lock.timeout"));
            assertTrue(hints.containsKey("javax.persistence.query.timeout"));
            timeout = new Integer((String) hints.get(
                "javax.persistence.lock.timeout"));
            assertEquals("Expected Map updated lockTimeout",
                lTime.intValue(), timeout.intValue());
            timeout = new Integer((String) hints.get(
                "javax.persistence.query.timeout"));
            assertEquals("Expected Map updated queryTimeout",
                qTime.intValue(), timeout.intValue());
            */
            // verify internal config values were updated
            timeout = q.getFetchPlan().getLockTimeout();
            assertEquals("Expected Map updated lock timeout", lTime.intValue(),
                timeout.intValue());
            timeout = q.getFetchPlan().getQueryTimeout();
            assertEquals("Expected Map updated query timeout", qTime.intValue(),
                timeout.intValue());
            
            //=============
            // Test for 4a)
            //=============
            // verify setHint overrides Map provided properties
            lTime = 15000;
            qTime = 10000;
            q.setHint("javax.persistence.lock.timeout", lTime);
            q.setHint("javax.persistence.query.timeout", qTime);
            hints = q.getHints();
            // verify getHints values were updated
            timeout = (Integer) hints.get("javax.persistence.lock.timeout");
            assertEquals(
                "Expected setHint updated javax.persistence.lock.timeout",
                lTime.intValue(), timeout.intValue());
            timeout = (Integer) hints.get("javax.persistence.query.timeout");
            assertEquals(
                "Expected setHint updated javax.persistence.query.timeout",
                qTime.intValue(), timeout.intValue());
            // verify internal config values were updated
            timeout = q.getFetchPlan().getLockTimeout();
            assertEquals("Expected setHint updated lockTimeout",
                lTime.intValue(), timeout.intValue());
            timeout = q.getFetchPlan().getQueryTimeout();
            assertEquals("Expected setHint updated queryTimeout",
                qTime.intValue(), timeout.intValue());
        } finally {
            // cleanup
            closeEMF(emf1);
            closeEMF(emf2);
        }
    }

    public void testWithProperties() {
        getLog().trace("testWithProperties() - properties in persistence.xml");
        OpenJPAEntityManagerFactory emf1 = null, emf2 = null;
        OpenJPAEntityManager em1 = null, em2 = null;
        
        try {
            OpenJPAQuery q;
            Map<String, Object> hints;
            Integer timeout;
            Integer lTime = new Integer(10000);
            Integer qTime = new Integer(5000);
            
            // create our PU with properties
            emf1 = OpenJPAPersistence.createEntityManagerFactory(
                "qtimeout-with-properties", "persistence3.xml");
            assertNotNull(emf1);
            emf2 = OpenJPAPersistence.createEntityManagerFactory(
                "qtimeout-with-properties", "persistence3.xml", props);
            assertNotNull(emf2);
            
            //=============
            // Test for 1b)
            //=============
            // verify properties in persistence.xml
            OpenJPAConfiguration conf1 = emf1.getConfiguration();
            assertNotNull(conf1);
            assertEquals("Default PU lock timeout", lTime.intValue(),
                conf1.getLockTimeout());
            assertEquals("Default PU query timeout.", qTime.intValue(),
                conf1.getQueryTimeout());
            // verify Query receives the properties
            em1 = emf1.createEntityManager();
            assertNotNull(em1);
            q = em1.createNamedQuery("NoHintList");    
            // Cannot verify properties are passed through as Query hints
            //     See explanation and commented out test in testNoProperties()
            // verify timeout properties supplied in persistence.xml
            timeout = q.getFetchPlan().getLockTimeout();
            assertEquals("Expected default PU lock timeout", lTime.intValue(),
                timeout.intValue());
            timeout = q.getFetchPlan().getQueryTimeout();
            assertEquals("Expected default PU query timeout", qTime.intValue(),
                timeout.intValue());

            //=============
            // Test for 2b)
            //=============
            // verify properties in Map override persistence.xml
            OpenJPAConfiguration conf2 = emf2.getConfiguration();
            assertNotNull(conf2);
            lTime = 12000;
            qTime = 7000;
            assertEquals("Expected Map updated lock timeout", lTime.intValue(),
                conf2.getLockTimeout());
            assertEquals("Expected Map updated query timeout", qTime.intValue(),
                conf2.getQueryTimeout());
            // Verify Query receives the properties
            em2 = emf2.createEntityManager();
            assertNotNull(em2);
            q = em2.createNamedQuery("NoHintList");
            // Cannot verify properties are passed through as Query hints
            //     See explanation and commented out test in testNoProperties()
            // verify internal config values were updated
            timeout = q.getFetchPlan().getLockTimeout();
            assertEquals("Expected Map updated lockTimeout", lTime.intValue(),
                timeout.intValue());
            timeout = q.getFetchPlan().getQueryTimeout();
            assertEquals("Expected Map updated queryTimeout", qTime.intValue(),
                timeout.intValue());
            
            //=============
            // Test for 3)
            //=============
            // verify QueryHints override Map provided properties
            q = em2.createNamedQuery("Hint1000msec");
            qTime = 1000;
            // verify getHints values were updated
            hints = q.getHints();
            timeout = new Integer((String)hints.get(
                    "javax.persistence.query.timeout"));
            assertEquals("Expected QueryHints updated query timeout",
                qTime.intValue(), timeout.intValue());
            // verify internal config value was updated
            timeout = q.getFetchPlan().getQueryTimeout();
            assertEquals("Expected QueryHints updated queryTimeout",
                qTime.intValue(), timeout.intValue());

            //=============
            // Test for 4b)
            //=============
            // verify setHint overrides QueryHint provided properties
            lTime = 15000;
            qTime = 10000;
            q.setHint("javax.persistence.lock.timeout", lTime);
            q.setHint("javax.persistence.query.timeout", qTime);
            // verify getHints values were updated
            hints = q.getHints();
            timeout = (Integer) hints.get("javax.persistence.lock.timeout");
            assertEquals("Expected setHint updated lock timeout",
                lTime.intValue(), timeout.intValue());
            timeout = (Integer) hints.get("javax.persistence.query.timeout");
            assertEquals("Expected setHint updated query timeout",
                qTime.intValue(), timeout.intValue());
            // verify internal config values were updated
            timeout = q.getFetchPlan().getLockTimeout();
            assertEquals("Expected setHint updated lockTimeout",
                lTime.intValue(), timeout.intValue());
            timeout = q.getFetchPlan().getQueryTimeout();
            assertEquals("Expected setHint updated queryTimeout",
                qTime.intValue(), timeout.intValue());
        } finally {
            // cleanup
            closeEMF(emf1);
            closeEMF(emf2);
        }
    }
    
}
