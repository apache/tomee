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
package org.apache.openjpa.persistence.meta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestQueryTypeAliasRegistration extends SingleEMFTestCase {
    private final int threads = 100;

    @Override
    public void setUp() throws Exception {
        super.setUp(MdrTestEntity.class);
        assertNotNull(emf);
        // Create the Entity which we're going to try to get MetaData for using
        // an alias. Do this to ensure that it has been registered to the PCRegistry.
        new MdrTestEntity();
    }

    /**
     * This method tests a timing window where more than one thread requests MetaData using an alias
     * at the same time. All threads should get data back and no threads should receive an
     * exception.
     */
    public void testMultiThreadGetMetaDataAlias() throws Exception {
        try {
            List<Worker> workers = new ArrayList<Worker>();
            Set<Exception> exceptions = new HashSet<Exception>();
            for (int i = 0; i < threads; i++) {
                Worker w = new Worker(emf);
                workers.add(w);
            }
            for (Worker w : workers) {
                w.start();
            }
            for (Worker w : workers) {
                w.join();
                Exception e = w.getException();
                if (e != null) {
                    exceptions.add(w.getException());
                }
            }
            assertTrue("Caught "  + exceptions.toString(), exceptions.size() == 0);
        } finally {
            if (emf != null) {
                emf.close();
            }
        }
    }

    class Worker extends Thread {
        OpenJPAEntityManagerFactorySPI emf;
        OpenJPAEntityManagerSPI em;
        MetaDataRepository repo;
        Exception ex;

        Worker(EntityManagerFactory e) {
            emf = (OpenJPAEntityManagerFactorySPI) e;
            // Create an EM here because that triggers registering the MDR as a class registration
            // listener with the PCRegistry.
            em = emf.createEntityManager();
            repo = em.getConfiguration().getMetaDataRepositoryInstance();
        }

        Exception getException() {
            return ex;
        }

        @Override
        public void run() {
            try {
                repo.getMetaData("MdrTestEntity", Thread.currentThread().getContextClassLoader(), true);
            } catch (Exception e) {
                ex = e;
                e.printStackTrace();
            } finally {
                em.close();
            }
        }
    }
}
