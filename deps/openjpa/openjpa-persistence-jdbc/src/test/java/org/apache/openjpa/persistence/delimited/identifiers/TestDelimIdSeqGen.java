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
package org.apache.openjpa.persistence.delimited.identifiers;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestDelimIdSeqGen extends SQLListenerTestCase {
    OpenJPAEntityManager em;
    JDBCConfiguration conf;
    DBDictionary dict;
    boolean supportsNativeSequence = false;
    
    EntityB entityB;
    
    @Override
    public void setUp() throws Exception {
        setSupportedDatabases(
            org.apache.openjpa.jdbc.sql.DerbyDictionary.class,
            org.apache.openjpa.jdbc.sql.DB2Dictionary.class);
        if (isTestsDisabled()) {
            return;
        }
        
        super.setUp(EntityB.class, DROP_TABLES);
        assertNotNull(emf);
        
        conf = (JDBCConfiguration) emf.getConfiguration();
        dict = conf.getDBDictionaryInstance();
        supportsNativeSequence = dict.nextSequenceQuery != null;
        
        if (supportsNativeSequence) {
            em = emf.createEntityManager();
            assertNotNull(em);
        }
    }
    
    @Override
    public void tearDown() throws Exception {
        if (em != null && em.isOpen()) {
            em.close();
            em = null;
        }
        super.tearDown();
    }

    @Override
    protected OpenJPAEntityManagerFactorySPI createEMF(final Object... props) {
        return createNamedEMF("delimited-identifiers", props);
    }
    
    public void createEntityB() {
        entityB = new EntityB("b name");
    }
    
    public void testSeqGen() {
        if (!supportsNativeSequence) {
            return;
        }
        createEntityB();
        
        em.getTransaction().begin();
        em.persist(entityB);
        em.getTransaction().commit();
        
        int genId = entityB.getId();
        em.clear();
        em.getTransaction().begin();
        EntityB bA = em.find(EntityB.class, genId);
        assertEquals("b name", bA.getName());
        em.getTransaction().commit();
        em.close();
    }
}
