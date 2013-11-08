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
package org.apache.openjpa.persistence.sequence;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests the drop then add schema action for SynchronizeMappings when a
 * native sequence suffixed with "_seq" is in use.  This test only runs when 
 * the configured database supports native sequences.
 */
public class TestDropAddSequence extends SingleEMFTestCase {
    
    @Override
    public void setUp() throws Exception {

        // Create a basic emf to determine whether sequences are supported.
        // If so, run the normal setup with the test PU.
        OpenJPAEntityManagerFactorySPI tempEMF = createNamedEMF("test");
        try {
            if (!supportsSequences(tempEMF)) {
                return;
            }
        } finally {
            if (tempEMF != null) {
                tempEMF.close();
            }
        }
        super.setUp();
        // Force creation of the base schema artifacts including the base
        // sequences (add, no drop)
        emf.createEntityManager().close();
    }

    @Override
    protected String getPersistenceUnitName() {
        return "TestDropAddSequence";
    }

    
    /**
     * Verifies a new EMF can be created when the runtime forward mapping tool 
     * is enabled with drop then add schema action when an entity contains a
     * named native sequence suffixed with "_seq".
     */
    public void testDropAddSequence() {
        
        if (!supportsSequences(emf)) {
            return;
        }
        
        Object[] props = new Object[] { "openjpa.jdbc.SynchronizeMappings",
            "buildSchema(SchemaAction='drop,add')" };
        OpenJPAEntityManagerFactorySPI oemf = createNamedEMF("TestDropAddSequence",props);
        
        OpenJPAEntityManager em = oemf.createEntityManager();
        
        em.close();
        oemf.close();
    }
    
    private boolean supportsSequences(OpenJPAEntityManagerFactorySPI oemf) {
        if (oemf == null) {
            return false;
        }
        DBDictionary dict = ((JDBCConfiguration)oemf.getConfiguration()).getDBDictionaryInstance();
        if (dict != null) {
            return dict.nextSequenceQuery != null;
        }
        return false;
    }
}
