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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;


public class TestSequenceGenerationOnMT extends SingleEMFTestCase {
    public void setUp() {
        super.setUp(CLEAR_TABLES, GeneratedIdObject.class, 
          "openjpa.Multithreaded", "true");
    }
    
    public void testIdGenerationInMultithreadedEnvironment() {
        int nThreads = 5;
        ExecutorService threads = Executors.newFixedThreadPool(nThreads);
        List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();
        for (int i = 0; i < nThreads; i++) {
            Loader loader = new Loader(emf.createEntityManager());
            tasks.add(loader);
        }
        List<Future<Boolean>> results;
        try {
            results = threads.invokeAll(tasks);
            for (Future<Boolean> result : results) {
                assertTrue(result.get());
            }
        } catch (ExecutionException ee) {
            ee.getCause().printStackTrace();
            fail("Failed " + ee.getCause());
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Failed " + e);
        }
        
    }
    
    
    public static class Loader implements Callable<Boolean> {
        EntityManager em;
        Loader(EntityManager em) {
            this.em = em;
        }
        public Boolean call() throws Exception {
            GeneratedIdObject pc = new GeneratedIdObject();
            em.getTransaction().begin();
            em.persist(pc);
            em.getTransaction().commit();
            return true;
        }
    }
}
