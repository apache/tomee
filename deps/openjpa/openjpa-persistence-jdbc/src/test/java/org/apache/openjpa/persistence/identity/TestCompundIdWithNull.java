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
package org.apache.openjpa.persistence.identity;

import java.sql.Connection;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.DatabasePlatform;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test that compound identity can consists of null component column value.
 * 
 * This test uses pre-defined database tables created by DDL explicitly.
 * The tables have <em>logical</em> compound primary keys in the sense non-null
 * constraint is <em>not</em> set on the primary columns. The tables are populated 
 * with SQL to contain null values in these columns. 
 * The test verifies that results are returned as par expectation.
 * For more details, refer
 * <A href="https://issues.apache.org/jira/browse/OPENJPA-1397">JIRA-1397</A>
 * 
 * @author Pinaki Poddar
 * @author Michael Vorburger
 */

@DatabasePlatform("org.apache.derby.jdbc.EmbeddedDriver")
public class TestCompundIdWithNull extends SingleEMFTestCase {
    private static boolean tablesCreated = false;

    public void setUp() throws Exception {
        // Only run on Derby because we use DDL specific to Derby
        setSupportedDatabases(
            org.apache.openjpa.jdbc.sql.DerbyDictionary.class);
        if (isTestsDisabled()) {
            return;
        }

        // do not use CLEAR_TABLES or DROP_TABLES
        super.setUp(SimpleCompoundIdTestEntity.class, ComplexCompoundIdTestEntity.class, TypeEntity.class);
        if (!tablesCreated) {
            createTables(emf.createEntityManager());
            tablesCreated = true;
        }
    }
	
    public void testSimpleCompoundIdTestEntity() throws Exception  {
			EntityManager em = emf.createEntityManager();
			String jpql = "SELECT o FROM SimpleCompoundIdTestEntity o ORDER BY o.secondId";
			List<SimpleCompoundIdTestEntity> list = em.createQuery(jpql,SimpleCompoundIdTestEntity.class)
			    .getResultList();
			assertEquals(2, list.size());
			assertEquals(Long.valueOf(123), list.get(0).getSecondId());
			
			SimpleCompoundIdTestEntity secondResult = list.get(1);
			assertNotNull("BUG (JIRA-1397)! Result list contains null in second element", secondResult);
			assertNull(secondResult.getSecondId());
			em.close();
	}

	
	public void testComplexCompoundIdTestEntity() throws Exception  {
			EntityManager em = emf.createEntityManager();
			String jpql = "SELECT o FROM ComplexCompoundIdTestEntity o ORDER BY o.type";
			List<ComplexCompoundIdTestEntity> list = em.createQuery(jpql,ComplexCompoundIdTestEntity.class)
			    .getResultList();
			assertEquals(2, list.size());
			ComplexCompoundIdTestEntity secondResult = list.get(1);
			assertNotNull("Result list contains null in second element", secondResult);
			assertNull("Result list's second record secondId field was not null", secondResult.getType());
			em.close();
	}
	
	/**
	 * Create tables with logical compound keys without non-null constraint.
	 * Populate them with null values in some of the columns.
	 */
    private void createTables(EntityManager em) throws Exception {
        em.getTransaction().begin();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast(em);

        Connection conn = (Connection) kem.getConnection();
        // NOTE that 'logically' test_simple has a ", CONSTRAINT test_simple_pk PRIMARY KEY (firstId, secondId)",
        // but at least Derby doesn't permit NULL then.. in our real-world underlying schema that leads
        // to this there are *NO* PRIMARY KEY on any tables, but there is a logical model expressed
        // elsewhere stating that those two columns uniquely identify a row.
        try {
            conn.createStatement().execute("DROP TABLE test_type");
            conn.createStatement().execute("DROP TABLE test_simple");
            conn.createStatement().execute("DROP TABLE test_complex");
        } catch (Exception e) {
        }
        
        conn.createStatement().execute("CREATE TABLE test_simple(firstId NUMERIC, secondId NUMERIC)");
        conn.createStatement().execute("INSERT INTO test_simple(firstId, secondId) VALUES (1, 123)");
        conn.createStatement().execute("INSERT INTO test_simple(firstId, secondId) VALUES (1, NULL)");

        conn.createStatement().execute("CREATE TABLE test_type(id NUMERIC CONSTRAINT test_type_pk PRIMARY KEY, " +
                "code VARCHAR(16))");
        conn.createStatement().execute("INSERT INTO test_type(id, code) VALUES (987, 'ABC')");

        conn.createStatement().execute("CREATE TABLE test_complex(id NUMERIC, type_id NUMERIC)");
        conn.createStatement().execute("INSERT INTO test_complex(id, type_id) VALUES (1, 987)");
        conn.createStatement().execute("INSERT INTO test_complex(id, type_id) VALUES (1, NULL)");

        conn.close();
        
        em.flush();
        em.getTransaction().commit();
        em.close();
    }
	    
}
