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
package org.apache.openjpa.persistence.batch.exception;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.OracleDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;
import org.apache.openjpa.util.ExceptionInfo;

//This test was created for OPENJPA-1550.  In this issue the user was
//not able to get the 'failed object' (the object causing the failure) when
//batch limit was -1 or a value greater than 1.  Also, they found that the
//'params' listed in the prepared statement were missing.  This test will set
//various batch limits and verify that with the fix to 1550, the correct
//'failed object' and prepared statement is returned. 
public class TestBatchLimitException extends AbstractPersistenceTestCase {

    static Ent1 expectedFailedObject;
    static Ent1 expectedFailedObjectOracle;
    static boolean isOracle = false;
    final String expectedFailureMsg =
        "INSERT INTO Ent1 (pk, name) VALUES (?, ?) [params=(int) 200, (String) twohundred]";
    final String expectedFailureMsg18 =
        "INSERT INTO Ent1 (pk, name) VALUES (?, ?) [params=(int) 18, (String) name18]";
    String expectedFailureMsgOracle = expectedFailureMsg18;

    public EntityManagerFactory newEmf(String batchLimit) {
        OpenJPAEntityManagerFactorySPI emf =
            createEMF(Ent1.class, 
                "openjpa.jdbc.SynchronizeMappings", 
                "buildSchema(ForeignKeys=true)",
                "openjpa.jdbc.DBDictionary", batchLimit, 
                "openjpa.ConnectionFactoryProperties", "PrintParameters=true",
                CLEAR_TABLES);

        assertNotNull("Unable to create EntityManagerFactory", emf);
        JDBCConfiguration conf = (JDBCConfiguration) emf.getConfiguration();
        DBDictionary dict = conf.getDBDictionaryInstance();
        isOracle = dict instanceof OracleDictionary;
        return emf;
    }

    public void setUp() {
        expectedFailedObject = null;
        expectedFailedObjectOracle = null;
    }

    // Test that we get the correct 'failed object' when we have a batchLimt
    // of X and Y rows, where X>Y. A duplicate row will be inserted
    // sometime within the Y rows. This will verify that we get the right
    // 'failed object' and message.
    public void testExceptionInFirstBatch() throws Throwable {
        EntityManagerFactory emf = newEmf("batchLimit=-1");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.persist(new Ent1(1, "one"));
        expectedFailedObject = new Ent1(200, "twohundred");
        em.persist(expectedFailedObject);
        em.persist(new Ent1(5, "five"));
        em.getTransaction().commit();
        em.close();

        EntityManager em2 = emf.createEntityManager();

        em2.getTransaction().begin();
        // special case, due to how Oracle returns all statements in the batch
        expectedFailedObjectOracle = new Ent1(18, "name18");
        expectedFailureMsgOracle = expectedFailureMsg18;
        em2.persist(expectedFailedObjectOracle);
        em2.persist(new Ent1(2, "two"));
        em2.persist(new Ent1(200, "twohundred"));
        em2.persist(new Ent1(3, "three"));
        em2.persist(new Ent1(1, "one"));
        em2.persist(new Ent1(5, "five"));
        
        try {
            em2.getTransaction().commit();
        } catch (Throwable excp) {
            verifyExDetails(excp);
        }
        finally {
            if (em2.getTransaction().isActive()) {
                em2.getTransaction().rollback();
            }
            em2.close();
            closeEMF(emf);
        }
    }

    // Test that we get the correct 'failed object' when there is only one
    // row in the batch. The 'batching' logic executes a different
    // statement when only one row is to be updated/inserted.
    public void testExceptionSingleBatchedRow() throws Throwable {
        EntityManagerFactory emf = newEmf("batchLimit=-1");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        expectedFailedObject = new Ent1(200, "twohundred");
        expectedFailedObjectOracle = expectedFailedObject;
        expectedFailureMsgOracle = expectedFailureMsg;
        em.persist(expectedFailedObject);
        em.getTransaction().commit();
        em.close();

        EntityManager em2 = emf.createEntityManager();

        em2.getTransaction().begin();
        em2.persist(new Ent1(200, "twohundred"));
        
        try {
            em2.getTransaction().commit();
        } catch (Throwable excp) {
            verifyExDetails(excp);
        }
        finally {
            if (em2.getTransaction().isActive()) {
                em2.getTransaction().rollback();
            }
            em2.close();
            closeEMF(emf);
        }
    }

    // Test that we get the correct 'failed object' and message when we
    // have a batchLimt of X and Y rows, where Y>X. In this case, the
    // batch is executed every time the batchLimt is hit. A duplicate
    // row will be inserted sometime after X (X+1, i.e right at the
    // boundary of the batch) to verify that we get the right
    // 'failed object' and msg no matter which batch a duplicate is
    // contained in. This test is important because as part of the
    // fix to OPENJPA-1510 we had to add extra logic to keep track
    // of which batch the 'failed object' was in, along with the
    // index into that batch.
    public void testExceptionInSecondBatch() throws Throwable {
        EntityManagerFactory emf = newEmf("batchLimit=9");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        expectedFailedObject = new Ent1(200, "twohundred");
        expectedFailedObjectOracle = expectedFailedObject;
        expectedFailureMsgOracle = expectedFailureMsg;
        em.persist(expectedFailedObject);
        em.getTransaction().commit();
        em.close();

        EntityManager em2 = emf.createEntityManager();

        em2.getTransaction().begin();

        // Put 9 objects/rows into the batch
        for (int i = 0; i < 9; i++) {
            em2.persist(new Ent1(i, "name" + i));
        }

        // Put the duplicate object/row as the first element in the second batch.
        em2.persist(new Ent1(200, "twohundred"));
        
        try {
            em2.getTransaction().commit();
        } catch (Throwable excp) {
            verifyExDetails(excp);
        }
        finally {
            if (em2.getTransaction().isActive()) {
                em2.getTransaction().rollback();
            }
            em2.close();
            closeEMF(emf);
        }
    }

    // Same as testRowsGreaterThanBatchLimit_boundaryCase, but the object to cause the failure
    // is in the middle of the second batch. testExceptioninSecondBatch puts
    // the failing object as the first element in the second batch, this test puts
    // it somewhere in the middle of the third batch. Again, we want to make sure our
    // indexing into the batch containing the 'failed object' is correct.
    public void testExceptionInThirdBatch() throws Throwable {
        final int batchLimit=9;
        EntityManagerFactory emf = newEmf("batchLimit="+batchLimit);
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        expectedFailedObject = new Ent1(200, "twohundred");
        em.persist(expectedFailedObject);
        em.getTransaction().commit();
        em.close();

        EntityManager em2 = emf.createEntityManager();

        em2.getTransaction().begin();

        // Persist 21 objects/rows....as such we will have two 'full'
        // batches (9*2=18) and 3 (21-18=3) objects/rows in the 3rd batch.
        int i=0;
        for (; i < 2*batchLimit; i++) {
            em2.persist(new Ent1(i, "name" + i));
        }

        // manually create third batch, due to how Oracle returns all statements in the batch
        expectedFailedObjectOracle = new Ent1(i, "name" + i++);
        expectedFailureMsgOracle = expectedFailureMsg18;
        em2.persist(expectedFailedObjectOracle);    // 18
        em2.persist(new Ent1(i, "name" + i++));     // 19
        em2.persist(new Ent1(i, "name" + i++));     // 20
        em2.persist(new Ent1(i, "name" + i++));     // 21        
        // Put the duplicate row in the 3rd batch.
        em2.persist(new Ent1(200, "twohundred"));
        // Put a few more objects into the batch.
        for (i = 22; i < 4*batchLimit; i++) {
            em2.persist(new Ent1(i, "name" + i));
        }

        try {
            em2.getTransaction().commit();
        } catch (Throwable excp) {
            verifyExDetails(excp);
        }
        finally {
            if (em2.getTransaction().isActive()) {
                em2.getTransaction().rollback();
            }
            em2.close();
            closeEMF(emf);
        }
    }

    // Similar to the previous two tests, but lets run the test with a large
    // batch with a failure, and then commit, then run large batches
    // again with failures again.....just want to make sure things are not in
    // some way 're-used' between the two commits as far as the indexes go.
    public void testSecondExceptionHasRightIndex() throws Throwable {
        final int batchLimit=9;

        testExceptionInThirdBatch();

        EntityManagerFactory emf = newEmf("batchLimit=9");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        for (int i = 4*batchLimit; i < 5*batchLimit; i++) {
            em.persist(new Ent1(i, "name" + i));
        }

        // manually capture start of batch, due to how Oracle returns all statements in the batch
        expectedFailedObjectOracle = new Ent1(200, "twohundred");
        expectedFailureMsgOracle = expectedFailureMsg;
        em.persist(expectedFailedObjectOracle);

        for (int i = 5*batchLimit; i < 7*batchLimit; i++) {
            em.persist(new Ent1(i, "name" + i));
        }

        try {
            em.getTransaction().commit();
        } catch (Throwable excp) {
            verifyExDetails(excp);
        }
        finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
            closeEMF(emf);
        }
    }

    public void testExceptionWithMultipleCommits() throws Throwable {
        EntityManagerFactory emf = newEmf("batchLimit=-1");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.persist(new Ent1(1, "one"));
        expectedFailedObject = new Ent1(200, "twohundred");
        em.persist(expectedFailedObject);
        em.persist(new Ent1(5, "five"));
        em.getTransaction().commit();
        em.close();

        EntityManager em2 = emf.createEntityManager();
        em2.getTransaction().begin();
        em2.persist(new Ent1(4, "four"));
        em2.persist(new Ent1(0, "zero"));
        em2.persist(new Ent1(2, "two"));
        em2.persist(new Ent1(3, "three"));
        em2.getTransaction().commit();

        em2.getTransaction().begin();
        // special case, due to how Oracle returns all statements in the batch
        expectedFailedObjectOracle = new Ent1(18, "name18");
        expectedFailureMsgOracle = expectedFailureMsg18;
        em2.persist(expectedFailedObjectOracle);
        em2.persist(new Ent1(6, "six"));
        em2.persist(new Ent1(200, "twohundred"));
        em2.persist(new Ent1(7, "seven"));
        
        try {
            em2.getTransaction().commit();
        } catch (Throwable excp) {
            verifyExDetails(excp);
        }
        finally {
            if (em2.getTransaction().isActive()) {
                em2.getTransaction().rollback();
            }
            em2.close();
            closeEMF(emf);
        }
    }

    // Verify that the resultant exception contains the correct 'failed object'
    // and exception message.
    public void verifyExDetails(Throwable excp) throws Throwable {
        // The exception should contain the 'failed object'
        verifyFailedObject(excp);
        // The second cause should contain the message which shows the failing prepared statement.
        Throwable cause = excp.getCause().getCause();
        verifyExMsg(cause.getMessage());
    }

    public void verifyFailedObject(Throwable excp) throws Throwable {
        if (excp instanceof ExceptionInfo) {
            ExceptionInfo e = (ExceptionInfo) excp;

            Ent1 failedObject = (Ent1) e.getFailedObject();

            assertNotNull("Failed object was null.", failedObject);
            if (!isOracle) {
                assertEquals(expectedFailedObject, failedObject);
            } else {
                // special case, as Oracle returns all statements in the batch
                assertEquals(expectedFailedObjectOracle, failedObject);                
            }
        }
        else {
            throw excp;
        }
    }

    public void verifyExMsg(String msg) {
        assertNotNull("Exception message was null.", msg);
        if (!isOracle) {
            assertTrue("Did not see expected text in message. Expected <" + expectedFailureMsg + "> but was " +
                msg, msg.contains(expectedFailureMsg));
        } else {
            // special case, as Oracle returns all statements in the batch
            assertTrue("Did not see expected text in message. Expected <" + expectedFailureMsgOracle + "> but was " +
                msg, msg.contains(expectedFailureMsgOracle));
        }
    }
}

