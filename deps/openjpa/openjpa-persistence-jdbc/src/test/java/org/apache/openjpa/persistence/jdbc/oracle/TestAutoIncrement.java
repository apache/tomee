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
package org.apache.openjpa.persistence.jdbc.oracle;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.DatabasePlatform;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests identity value assignment with IDENTITY strategy specifically for
 * Oracle database. IDENTITY strategy for most database platforms is supported
 * with auto-increment capabilities. As Oracle does not natively support
 * auto-increment, the same effect is achieved by a combination of a database
 * sequence and a pre-insert database trigger [1].
 * 
 * This test verifies that a persistent entity using IDENTITY generation type
 * is allocating identities in monotonic sequence on Oracle platform.
 * 
 * [1] http://jen.fluxcapacitor.net/geek/autoincr.html
 * 
 * @author Pinaki Poddar
 * 
 */

@DatabasePlatform("oracle.jdbc.driver.OracleDriver")
public class TestAutoIncrement extends SingleEMFTestCase {

    public void setUp() throws Exception {
        // Only run on Oracle
        setSupportedDatabases(
            org.apache.openjpa.jdbc.sql.OracleDictionary.class);
        if (isTestsDisabled()) {
            return;
        }

        if ("testAutoIncrementIdentityWithNamedSequence".equals(getName())) {
            String sequence = "autoIncrementSequence";
            createSequence(sequence);
			super.setUp(CLEAR_TABLES, PObject.class,
			    "openjpa.jdbc.DBDictionary",
			    "oracle(UseTriggersForAutoAssign=true,autoAssignSequenceName=" + sequence + ")");
		} else {
			super.setUp(CLEAR_TABLES, PObjectNative.class,
					"openjpa.jdbc.DBDictionary",
                    "oracle(UseTriggersForAutoAssign=true)");
		}
    }

    public void testAutoIncrementIdentityWithNamedSequence() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		PObject pc1 = new PObject();
		PObject pc2 = new PObject();
		em.persist(pc1);
		em.persist(pc2);
		em.getTransaction().commit();

		assertEquals(1, Math.abs(pc1.getId() - pc2.getId()));
		em.close();
	}

	public void testAutoIncrementIdentityWithNativeSequence() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		PObjectNative pc1 = new PObjectNative();
		PObjectNative pc2 = new PObjectNative();
		em.persist(pc1);
		em.persist(pc2);
		em.getTransaction().commit();

		assertEquals(1, Math.abs(pc1.getId() - pc2.getId()));
		em.close();
	}

    /**
     * Create sequence so that the test does not require manual intervention in database.
     */
    private void createSequence(String sequence) {
        OpenJPAEntityManagerFactorySPI factorySPI = createEMF();
        OpenJPAEntityManagerSPI em = factorySPI.createEntityManager();

        try {
            em.getTransaction().begin();
            Query q = em.createNativeQuery("CREATE SEQUENCE " + sequence + " START WITH 1");
            q.executeUpdate();
            em.getTransaction().commit();
        } catch (PersistenceException e) {          
            // Sequence probably exists.
            em.getTransaction().rollback();
        }
        closeEM(em);
        closeEMF(factorySPI);
    }
}
