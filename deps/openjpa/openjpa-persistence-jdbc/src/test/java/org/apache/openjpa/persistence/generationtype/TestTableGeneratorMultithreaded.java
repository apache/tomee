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
package org.apache.openjpa.persistence.generationtype;

import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.jdbc.kernel.TableJDBCSeq;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.SequenceMetaData;
import org.apache.openjpa.persistence.EntityManagerImpl;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestTableGeneratorMultithreaded extends SingleEMFTestCase {
    public final int ALLOC_SIZE = 100000;
    public final int INITIAL = 1;
    public final int LOOPS = 100000;
    public final int THREADS = 5;

    public void setUp() {
        setUp(Dog.class, CLEAR_TABLES
        // , "openjpa.Log", "SQL=trace", "openjpa.ConnectionFactoryProperties","PrintParameters=true"
        );
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        MetaDataRepository repo = emf.getConfiguration().getMetaDataRepositoryInstance();
        // Initialize MetaData
        repo.getMetaData(Dog.class, loader, true);
        repo.getSequenceMetaData("Dog_Gen", loader, true);

    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAllIdsAreUsed() throws Exception {
        List<WorkerThread> threads = new ArrayList<WorkerThread>();
        for (int i = 0; i < THREADS; i++) {
            threads.add(new WorkerThread(emf));
        }
        for (Thread t : threads) {
            t.start();
        }
        long max = 0;
        for (WorkerThread w : threads) {
            w.join();
            max = Math.max(max, (Long) w.getLast());
        }
        assertEquals((LOOPS * (THREADS)), max);
    }

    class WorkerThread extends Thread {
        Object _first = null, _last = null;
        int _count = 0;
        StoreContext _ctx;
        TableJDBCSeq _seq;
        ClassMetaData _cmd;
        EntityManagerImpl _em;
        MetaDataRepository _repo;

        public WorkerThread(OpenJPAEntityManagerFactorySPI emf) {
            _repo = emf.getConfiguration().getMetaDataRepositoryInstance();
        }

        public void run() {
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            SequenceMetaData meta = _repo.getSequenceMetaData("Dog_Gen", contextLoader, true);
            meta.setInitialValue(1);
            meta.setIncrement(10000);
            _em = (EntityManagerImpl) emf.createEntityManager();
            _ctx = (StoreContext) _em.getBroker();
            _cmd = _repo.getMetaData(Dog.class, contextLoader, true);

            _seq = (TableJDBCSeq) meta.getInstance(contextLoader);
            // Change defaults so this test doesn't take so long to run.
            _seq.setAllocate(ALLOC_SIZE);
            _seq.setInitialValue(1);

            Object obj = _seq.next(_ctx, _cmd);
            _first = obj;
            // start at 1 because we already got our first result.
            for (int i = 1; i < LOOPS; i++) {
                obj = _seq.next(_ctx, _cmd);
            }
            _last = obj;
            _em.close();
        }

        public Object getLast() {
            return _last;
        }

        public Object getFirst() {
            return _first;
        }

        public int getCount() {
            return _count;
        }
    }// end WorkerThread
}
