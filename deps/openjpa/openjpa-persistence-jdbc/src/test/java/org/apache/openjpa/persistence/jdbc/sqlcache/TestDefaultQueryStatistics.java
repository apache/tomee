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
package org.apache.openjpa.persistence.jdbc.sqlcache;

import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.kernel.PreparedQueryCache;
import org.apache.openjpa.kernel.QueryStatistics;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests that query statistics are collected in a thread-safe manner.
 *  
 * @author Rick Curtis
 * @author Pinaki Poddar
 *
 */
public class TestDefaultQueryStatistics extends SingleEMFTestCase {
    QueryStatistics<String> statistics;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        statistics = new QueryStatistics.Default<String>();
    }
    
    public void testThreadSafety() throws Exception{
        final QueryStatistics<String> finalStats = statistics;
        
        Runnable runner = new Runnable() {
            public void run() {
                for (int i = 0; i < 10000; i++) {
                    finalStats.recordExecution("query " + Thread.currentThread().getId() + " " + i);
                }
            }
        };
        
        List<Thread> threads = new ArrayList<Thread>();
        for(int i = 0;i<10;i++){
            threads.add(new Thread(runner));
        }
        for(Thread t : threads){
            t.start();
        }
        for(Thread t : threads){
            t.join();
        }
        assertEquals(1000, finalStats.keys().size());
    }
    
    public void testStatsSize() throws Exception{
        for (int i = 0; i < 10000; i++) {
            statistics.recordExecution("query " + Thread.currentThread().getId() + " " + i);
        }
        assertEquals(1000, statistics.keys().size());
    }
    
    public void testQueryStatisticsIsDisabledByDefault() {
        PreparedQueryCache cache = emf.getConfiguration().getQuerySQLCacheInstance();
        assertNotNull(cache);
        QueryStatistics<String> stats = cache.getStatistics();
        assertNotNull(stats);
        assertTrue(stats.keys().isEmpty());
        assertEquals(QueryStatistics.None.class, stats.getClass());
    }
}
