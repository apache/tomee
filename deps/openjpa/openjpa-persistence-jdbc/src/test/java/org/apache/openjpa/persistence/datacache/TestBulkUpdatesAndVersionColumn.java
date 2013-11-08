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

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import javax.persistence.RollbackException;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;

public class TestBulkUpdatesAndVersionColumn
    extends SingleEMFTestCase {

    public void setUp() throws Exception {
        setUp("openjpa.DataCache", "true",
            "openjpa.RemoteCommitProvider", "sjvm",
            OptimisticLockInstance.class, CLEAR_TABLES);

        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        OptimisticLockInstance pc = new OptimisticLockInstance("foo");
        em.persist(pc);
        em.getTransaction().commit();
        em.close();
    }

    public void testSelectOnOplockField() {
        EntityManager em = emf.createEntityManager();
        em.createQuery("select o from OptimisticLockInstance o "
            + "where o.oplock = 0").getResultList();
        em.close();
    }

    public void testOplockFieldMapping() {
        ClassMapping cm = (ClassMapping) JPAFacadeHelper.getMetaData(
            emf, OptimisticLockInstance.class);
        FieldMapping fm = cm.getFieldMapping("oplock");
        assertEquals(1, fm.getColumns().length);
    }

    public void testBulkUpdateWithManualVersionIncrement() {
        bulkUpdateHelper(true);
    }

    public void testBulkUpdateWithoutManualVersionIncrement() {
        bulkUpdateHelper(false);
    }

    private void bulkUpdateHelper(boolean incrementVersionField) {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        OptimisticLockInstance oli = (OptimisticLockInstance) em.createQuery(
            "SELECT o FROM OptimisticLockInstance o WHERE o.str = 'foo'")
            .getSingleResult();
        assertNotNull(oli);
        em.lock(oli, LockModeType.READ);

        EntityManager em2 = emf.createEntityManager();
        em2.getTransaction().begin();
        em2.createQuery("UPDATE OptimisticLockInstance o SET o.str = 'foo', "
            + "o.intField = o.intField + 1"
            + (incrementVersionField ? ", o.oplock = o.oplock + 1 " : "")
            + "WHERE o.str = 'foo'")
            .executeUpdate();
        em2.getTransaction().commit();
        em2.close();

        try {
            em.getTransaction().commit();
            fail("transaction should have failed");
        } catch (RollbackException re) {
            assertTrue("nested exception must be an oplock exception",
                re.getCause() instanceof OptimisticLockException);
        } finally {
            em.close();
        }
    }
}
