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
package org.apache.openjpa.integration.slf4j;

import javax.persistence.EntityManager;

import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.log.SLF4JLogFactory;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Simple test case to verify SLF4JLogFactory is being loaded
 */
public class TestLogFactory extends SingleEMFTestCase {
    String systemPropLog = null;
    
    public void setUp() throws Exception {
        // Maven users may disable logging via a global system prop - which breaks this testcase. 
        systemPropLog = System.getProperty("openjpa.Log");
        System.getProperties().remove("openjpa.Log");
        super.setUp();
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
        if(systemPropLog != null) { 
            System.setProperty("openjpa.Log", systemPropLog);
        }
    }
    
    public void testSLF4J() {
        try {
            EntityManager em = emf.createEntityManager();
            // do some logging
            Log log = getLog(); 
            String logFactory = log.getClass().getName();
            log.info("Log class=" + logFactory);
            assertEquals(SLF4JLogFactory.LogAdapter.class.getName(), logFactory);
            // next one should not be logged if using slf4j-simple binding - only INFO, WARN and ERROR
            log.trace("TRACE level logging");
            
            em.close();
        } finally {
            closeEMF(emf);
        }
    }
    
    public String getPersistenceUnitName() { 
        // override puName instead of creating our own EMF. 
        // getLog uses the SingleEMFTestCase's emf. 
        return "openjpa-integration-slf4j";
    }
}

