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
package org.apache.openjpa.persistence.jdbc.query;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DerbyDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.jdbc.query.domain.Applicant;
import org.apache.openjpa.persistence.jdbc.query.domain.Game;
import org.apache.openjpa.persistence.jdbc.query.procedure.DerbyProcedureList;
import org.apache.openjpa.persistence.jdbc.query.procedure.AbstractProcedureList;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests that Native queries use only 1-based positional parameters and 
 * disallows named parameters.
 * 
 * Originally reported in 
 * <A HRE="http://issues.apache.org/jira/browse/OPENJPA-918>OPENJPA-918</A>
 *  
 */
public class TestNativeQueryProcedures extends SingleEMFTestCase {
    AbstractProcedureList procedureList = null;
    
    @Override
    public void setUp() throws Exception {
        super.setUp(Applicant.class, Game.class, CLEAR_TABLES);

        // Figure out which DB we have and get the proper DB Procedure List
        OpenJPAEntityManagerFactorySPI ojpaEmf =
            (OpenJPAEntityManagerFactorySPI) emf;
        JDBCConfiguration conf = (JDBCConfiguration) ojpaEmf.getConfiguration();
        if (conf.getDBDictionaryInstance() instanceof DerbyDictionary) {
            procedureList = new DerbyProcedureList();
        }
        
        if (procedureList != null) {
            EntityManager em = emf.createEntityManager();
            List<String> createList = procedureList.getCreateProcedureList();
            try {
                for (String createProcedure : createList) {
                    em.getTransaction().begin();
                    Query query = em.createNativeQuery(createProcedure);
                    query.executeUpdate();
                    em.getTransaction().commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
                em.getTransaction().commit();
            }
            em.clear();
            em.close();
        }
    }

    public void tearDown() throws Exception {
        if (procedureList != null) {
            EntityManager em = emf.createEntityManager();
            List<String> dropList = procedureList.getDropProcedureList();
            try {
                for (String dropProcedure : dropList) {
                    em.getTransaction().begin();
                    Query query = em.createNativeQuery(dropProcedure);
                    query.executeUpdate();
                    em.getTransaction().commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
                em.getTransaction().commit();
            }
            em.clear();
            em.close();
        }
        super.tearDown();
    }

    public void testNoReturnNoParamProcedure() {
        if (procedureList != null) {
            EntityManager em = emf.createEntityManager();

            Applicant applicant1 = new Applicant();
            applicant1.setName("Charlie");
            Applicant applicant2 = new Applicant();
            applicant2.setName("Snoopy");

            em.getTransaction().begin();
            em.persist(applicant1);
            em.persist(applicant2);
            em.getTransaction().commit();

            String sql = procedureList.callAddXToCharlie();

            // query.getSingleResult() and query.getResultList() both throw an
            // exception:
            // Statement.executeQuery() cannot be called with a statement that
            // returns a row count
            try {
                em.getTransaction().begin();
                Query query = em.createNativeQuery(sql);
                query.getSingleResult();
                em.getTransaction().commit();
                fail("Expected exception. getSingleResult() with no returns " +
                		"should fail.");
            } catch (Exception e) {
                //Expected exception
                em.getTransaction().rollback();
            }
            try {
                em.getTransaction().begin();
                Query query = em.createNativeQuery(sql);
                query.getResultList();
                em.getTransaction().commit();
                fail("Expected exception. getResultList() with no returns " +
                		"should fail.");
            } catch (Exception e) {
                //Expected exception
                em.getTransaction().rollback();
            }

            // This one should work properly
            try {
                em.getTransaction().begin();
                Query query = em.createNativeQuery(sql);
                query.executeUpdate();
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                fail("Caught unexpected exception executing stored procedure: "
                    + e.getMessage());
            }

            // refresh the data
            em.clear();
            em.close();
            em = emf.createEntityManager();
            applicant1 = em.find(Applicant.class, applicant1.getId());
            applicant2 = em.find(Applicant.class, applicant2.getId());

            // verify one changed and one didn't
            assertEquals("Charliex", applicant1.getName());
            assertEquals("Snoopy", applicant2.getName());
        
            em.clear();
            em.close();
        }
    }

    @AllowFailure(value=true, message="Waiting for resolution for JIRA DERBY-4558")
    public void testNoReturnMultiParamProcedure() {
        if (procedureList != null) {
            EntityManager em = emf.createEntityManager();

            Applicant applicant1 = new Applicant();
            applicant1.setName("Charlie");
            Applicant applicant2 = new Applicant();
            applicant2.setName("Snoopy");

            em.getTransaction().begin();
            em.persist(applicant1);
            em.persist(applicant2);
            em.getTransaction().commit();

            String sql = procedureList.callAddSuffixToName();

            // query.getSingleResult() and query.getResultList() both throw an
            // exception:
            // Statement.executeQuery() cannot be called with a statement that
            // returns a row count
            try {
                em.getTransaction().begin();
                Query query = em.createNativeQuery(sql);
                query.setParameter(1, "Charlie");
                query.setParameter(2, "x");
                query.getSingleResult();
                em.getTransaction().commit();
                fail("Expected exception. getSingleResult() with no returns " +
                		"should fail.");
            } catch (Exception e) {
                //Expected exception
                em.getTransaction().rollback();
            }
            try {
                em.getTransaction().begin();
                Query query = em.createNativeQuery(sql);
                query.setParameter(1, "Charlie");
                query.setParameter(2, "x");
                query.getResultList();
                em.getTransaction().commit();
                fail("Expected exception. getResultList() with no returns " +
                		"should fail.");
            } catch (Exception e) {
                //Expected exception
                em.getTransaction().rollback();
            }

            // This one should work properly
            try {
                em.getTransaction().begin();
                Query query = em.createNativeQuery(sql);
                query.setParameter(1, "Charlie");
                query.setParameter(2, "x");
                query.executeUpdate();
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                fail("Caught unexpected exception executing stored procedure: "
                    + e.getMessage());
            }

            // refresh the data
            em.clear();
            em.close();
            em = emf.createEntityManager();
            applicant1 = em.find(Applicant.class, applicant1.getId());
            applicant2 = em.find(Applicant.class, applicant2.getId());

            // verify one changed and one didn't
            assertEquals("Charliex", applicant1.getName());
            assertEquals("Snoopy", applicant2.getName());
        
            em.clear();
            em.close();
        }
    }

    
    public void testOneReturnNoParamProcedure() {
        if (procedureList != null) {
            EntityManager em = emf.createEntityManager();

            Applicant applicant1 = new Applicant();
            applicant1.setName("Charlie");
            Applicant applicant2 = new Applicant();
            applicant2.setName("Snoopy");

            em.getTransaction().begin();
            em.persist(applicant1);
            em.getTransaction().commit();

            String sql = procedureList.callGetAllApplicants();

            try {
                em.getTransaction().begin();
                Query query = em.createNativeQuery(sql, Applicant.class);
                Applicant app = (Applicant)query.getSingleResult();
                em.getTransaction().commit();
                assertEquals("Charlie", app.getName());
            } catch (Exception e) {
                em.getTransaction().rollback();
                fail("Caught unexpected exception executing stored procedure: "
                    + e.getMessage());
            }

            em.getTransaction().begin();
            em.persist(applicant2);
            em.getTransaction().commit();
            
            try {
                em.getTransaction().begin();
                Query query = em.createNativeQuery(sql, Applicant.class);
                List<Applicant> appList = query.getResultList();
                em.getTransaction().commit();
                assertEquals(2, appList.size());
                for (Applicant a : appList) {
                    if (!a.getName().equals("Charlie")
                        && !a.getName().equals("Snoopy"))
                        fail("found invalid applicant " + a.getName());
                }
            } catch (Exception e) {
                em.getTransaction().rollback();
                fail("Caught unexpected exception executing stored procedure: "
                    + e.getMessage());
            }

            // This one should fail since we are doing select in stead of update
            try {
                em.getTransaction().begin();
                Query query = em.createNativeQuery(sql, Applicant.class);
                query.executeUpdate();
                em.getTransaction().commit();
                fail("Expected exception. executeUpdate() with query procedure "
                    + "should fail.");
            } catch (Exception e) {
                // Expected exception
                em.getTransaction().rollback();
            }

            // refresh the data
            em.clear();
            em.close();
            em = emf.createEntityManager();
        }
    }

    public void testOneReturnMultiParamProcedure() {
        if (procedureList != null) {
            EntityManager em = emf.createEntityManager();

            Applicant applicant1 = new Applicant();
            applicant1.setName("Charlie");
            Applicant applicant2 = new Applicant();
            applicant2.setName("Snoopy");
            Applicant applicant3 = new Applicant();
            applicant3.setName("Linus");
            Applicant applicant4 = new Applicant();
            applicant4.setName("Lucy");

            em.getTransaction().begin();
            em.persist(applicant1);
            em.persist(applicant3);
            em.persist(applicant4);
            em.getTransaction().commit();

            String sql = procedureList.callGetTwoApplicants();

            try {
                em.getTransaction().begin();
                Query query = em.createNativeQuery(sql, Applicant.class);
                query.setParameter(1, "Charlie");
                query.setParameter(2, "Snoopy");
                Applicant app = (Applicant)query.getSingleResult();
                em.getTransaction().commit();
                assertEquals("Charlie", app.getName());
            } catch (Exception e) {
                em.getTransaction().rollback();
                fail("Caught unexpected exception executing stored procedure: "
                    + e.getMessage());
            }

            em.getTransaction().begin();
            em.persist(applicant2);
            em.getTransaction().commit();
            
            try {
                em.getTransaction().begin();
                Query query = em.createNativeQuery(sql, Applicant.class);
                query.setParameter(1, "Charlie");
                query.setParameter(2, "Snoopy");
                List<Applicant> appList = query.getResultList();
                em.getTransaction().commit();
                assertEquals(2, appList.size());
                for (Applicant a : appList) {
                    if (!a.getName().equals("Charlie")
                        && !a.getName().equals("Snoopy"))
                        fail("found invalid applicant " + a.getName());
                }
            } catch (Exception e) {
                em.getTransaction().rollback();
                fail("Caught unexpected exception executing stored procedure: "
                    + e.getMessage());
            }

            // This one should fail since we are doing select in stead of update
            try {
                em.getTransaction().begin();
                Query query = em.createNativeQuery(sql, Applicant.class);
                query.setParameter(1, "Charlie");
                query.setParameter(2, "Snoopy");
                query.executeUpdate();
                em.getTransaction().commit();
                fail("Expected exception. executeUpdate() with query procedure "
                    + "should fail.");
            } catch (Exception e) {
                // Expected exception
                em.getTransaction().rollback();
            }

            // refresh the data
            em.clear();
            em.close();
            em = emf.createEntityManager();
        }
    }
}
