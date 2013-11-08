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
package org.apache.openjpa.persistence.datacache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.persistence.LockModeType;
import javax.sql.DataSource;

import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.event.RemoteCommitListener;
import org.apache.openjpa.event.RemoteCommitEvent;

public class TestDataCacheOptimisticLockRecovery
    extends SingleEMFTestCase {

    private int pk;
    private int remoteCommitEventStaleCount = 0;
    private Object staleOid;

    public void setUp() {
        setUp("openjpa.DataCache", "true",
            "openjpa.RemoteCommitProvider", "sjvm",
            OptimisticLockInstance.class);

        emf.getConfiguration().getRemoteCommitEventManager().addListener(
            new RemoteCommitListener() {
                public void afterCommit(RemoteCommitEvent e) {
                    if (e.getPayloadType() ==
                        RemoteCommitEvent.PAYLOAD_LOCAL_STALE_DETECTION) {
                        remoteCommitEventStaleCount++;
                        staleOid = e.getUpdatedObjectIds().iterator().next();
                    }
                }

                public void close() {
                }
            }
        );

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        OptimisticLockInstance oli = new OptimisticLockInstance("foo");
        em.persist(oli);
        em.getTransaction().commit();
        pk = oli.getPK();
        em.close();
    }

    public void testOptimisticLockRecovery() 
        throws SQLException {

        EntityManager em;
        
        // 1. get the oplock value for the instance after commit and
        // get a read lock to ensure that we check for the optimistic
        // lock column at tx commit.
        em = emf.createEntityManager();
        em.getTransaction().begin();
        OptimisticLockInstance oli = em.find(OptimisticLockInstance.class, pk);
        Object oid = JPAFacadeHelper.toOpenJPAObjectId(
            JPAFacadeHelper.getMetaData(oli),
            OpenJPAPersistence.cast(em).getObjectId(oli));
        int firstOpLockValue = oli.getOpLock();
        em.lock(oli, LockModeType.READ);

        // 2. make a change to the instance's optimistic lock column
        // via direct SQL in a separate transaction
        int secondOpLockValue = firstOpLockValue + 1;

        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.cast(em).getEntityManagerFactory();
        DataSource ds = (DataSource) emf.getConfiguration()
            .getConnectionFactory();
        Connection c = ds.getConnection();
        c.setAutoCommit(false);
        PreparedStatement ps = c.prepareStatement(
            "UPDATE OPTIMISTIC_LOCK_INSTANCE SET OPLOCK = ? WHERE PK = ?");
        ps.setInt(1, secondOpLockValue);
        ps.setInt(2, pk);
        assertEquals(1, ps.executeUpdate());
        c.commit();
        
        // 3. commit the transaction, catching the expected oplock
        // exception
        try {
            em.getTransaction().commit();
            fail("tx should have failed due to out-of-band oplock change");
        } catch (RollbackException re) {
            // expected
        } finally {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        }

        // 4. check that the corresponding remote commit event was fired
        assertEquals(1, remoteCommitEventStaleCount);
        assertEquals(oid, staleOid);

        // 5. obtain the object in a new persistence context and
        // assert that the oplock column is set to the one that
        // happened in the out-of-band transaction
        em.close();
        em = this.emf.createEntityManager();
        oli = em.find(OptimisticLockInstance.class, pk);

        // If this fails, then the data cache has the wrong value.
        // This is what this test case is designed to exercise.
        assertEquals("data cache is not being cleared when oplock "
            + "violations occur", secondOpLockValue, oli.getOpLock());

        // 6. get a read lock on the instance and commit the tx; this
        // time it should go through
        em.getTransaction().begin();
        em.lock(oli, LockModeType.READ);
        try {
            em.getTransaction().commit();
        } catch (RollbackException e) {
            throw e;
        } finally {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        }
        em.close();
    }
    
    public void testExpectedOptimisticLockException() {
        EntityManager em;
        
        // 1. start a new tx
        em = emf.createEntityManager();
        em.getTransaction().begin();
        em.lock(em.find(OptimisticLockInstance.class, pk), LockModeType.READ);
        
        // 2. start another tx, and cause a version increment
        EntityManager em2 = emf.createEntityManager();
        em2.getTransaction().begin();
        em2.lock(em2.find(OptimisticLockInstance.class, pk), 
            LockModeType.WRITE);
        em2.getTransaction().commit();
        em2.close();
        
        // 3. try to commit. this should fail, as this is a regular optimistic
        // lock failure situation.
        try {
            em.getTransaction().commit();
            fail("write lock in em2 should trigger an optimistic lock failure");
        } catch (RollbackException pe) {
            // expected
        }
        em.close();
    }
}
