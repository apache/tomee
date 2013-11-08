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
package org.apache.openjpa.audit;

import org.apache.openjpa.ee.ManagedRuntime;
import org.apache.openjpa.kernel.Audited;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Collection;

public class TestBeginEventOnTransactionListener extends SingleEMFTestCase {

    public void setUp() {
        setUp(X.class, AuditedEntry.class, CLEAR_TABLES);
    }

    @Override
    protected String getPersistenceUnitName() {
        return "auditjta";
    }


    public void test() throws Exception {
        doTest(emf);
        assertTrue(MockAuditor.called);
    }

    private void doTest(final EntityManagerFactory emf) throws Exception {
        final ManagedRuntime runtime = OpenJPAEntityManagerFactorySPI.class.cast(emf)
                .getConfiguration().getManagedRuntimeInstance();

        runtime.getTransactionManager().begin();
        try {
            final EntityManager em = emf.createEntityManager();
            em.joinTransaction();

            final X x = new X();
            em.persist(x);
            runtime.getTransactionManager().commit();
        }
        finally {
            emf.close();
        }
    }


    public static class MockAuditor implements Auditor
    {
        public static boolean called = false;

        @Override
        public void audit(Broker broker, Collection<Audited> newObjects,
                          Collection<Audited> updates, Collection<Audited> deletes) {
            called = true;
        }

        @Override
        public boolean isRollbackOnError() {
            return false;
        }

        @Override
        public void close() throws Exception {

        }

        @Override
        public void setConfiguration(Configuration conf) {

        }

        @Override
        public void startConfiguration() {

        }

        @Override
        public void endConfiguration() {

        }
    }

}
