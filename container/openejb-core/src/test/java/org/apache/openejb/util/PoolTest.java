/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.util;

import junit.framework.TestCase;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version $Rev$ $Date$
 */
public class PoolTest extends TestCase {

    public void testStrictBasics() throws Exception {
        exerciseStrictPool(1, 0);
        exerciseStrictPool(3, 0);
        exerciseStrictPool(4, 2);
        exerciseStrictPool(5, 5);
    }

    public void testEmptyPool() throws Exception {
        final int max = 4;
        final int min = 2;
        final Pool<String> pool = new Pool<String>(max, min, true);

        final List<Pool.Entry<String>> entries = drain(pool);

        // Should have received "max" number of nulls
        checkMax(max, entries);

        // All entries should be null
        for (Pool.Entry<String> entry : entries) {
            assertNull(entry);
        }

        // Pool is drained and no permits are available
        // Test that an add does not work
        assertFalse(pool.add("extra"));

        // Fill the pool via push
        for (int i = 0; i < max; i++) {
            pool.push("a" + System.currentTimeMillis());
        }

        // Drain, check, then discard all
        drainCheckPush(max, min, pool);
        drainCheckPush(max, min, pool);
        drainCheckPush(max, min, pool);

        discard(pool, drain(pool));


        // Fill the pool one item at a time, check it's integrity
        for (int i = 1; i <= max; i++) {
            assertTrue("i=" + i + ", max=" + max, pool.add("a" + i));

            List<Pool.Entry<String>> list = drain(pool);
            checkMax(max, list);
            checkMin(Math.min(i, min), list);
            checkEntries(i, list);
            push(pool, list);
        }

    }

    private void drainCheckPush(int max, int min, Pool<String> pool) throws InterruptedException {
        final List<Pool.Entry<String>> list = drain(pool);
        checkMax(max, list);
        checkMin(min, list);
        push(pool, list);
    }

    private void discard(Pool<String> pool, List<Pool.Entry<String>> list) {
        for (Pool.Entry<String> entry : list) {
            pool.discard(entry);
        }
    }

    private void push(Pool<String> pool, List<Pool.Entry<String>> list) {
        for (Pool.Entry<String> entry : list) {
            pool.push(entry);
        }
    }

    private void exerciseStrictPool(int max, int min) throws InterruptedException {
        Pool<String> pool = new Pool<String>(max, min, true);

        // Fill the pool
        for (int i = 0; i < max; i++) {
            assertTrue(pool.add("a" + i));
        }

        // Add one past the max
        assertFalse(pool.add("extra"));

        // Check the contents of the pool
        final List<Pool.Entry<String>> entries = drain(pool);

        checkMax(max, entries);
        checkMin(min, entries);

        // Push one back and check pool
        pool.push(entries.remove(0));
        final List<Pool.Entry<String>> entries2 = drain(pool);
        assertEquals(max, entries2.size() + entries.size());


        // discard all instances and add new ones
        entries.addAll(entries2);
        entries2.clear();
        final Iterator<Pool.Entry<String>> iterator = entries.iterator();
        while (iterator.hasNext()) {

            // Attempt two discards, followed by two adds

            pool.discard(iterator.next());
            if (iterator.hasNext()) {
                pool.discard(iterator.next());
                pool.add("s" + System.currentTimeMillis());
            }
            pool.add("s" + System.currentTimeMillis());
        }

        // Once again check min and max
        final List<Pool.Entry<String>> list = drain(pool);
        checkMax(max, list);
        checkMin(min, list);
    }

    public void testStrictMultiThreaded() throws Exception {

        final int threadCount = 200;

        final Pool pool = new Pool(10, 5, true);
        final CountDownLatch startPistol = new CountDownLatch(1);
        final CountDownLatch startingLine = new CountDownLatch(10);
        final CountDownLatch finishingLine = new CountDownLatch(threadCount);

        // Do a business method...
        Runnable r = new Runnable(){
        	public void run(){
                startingLine.countDown();
                try {
                    startPistol.await();

                    Pool.Entry entry = pool.pop(1000, MILLISECONDS);
                    Thread.sleep(50);
                    if (entry == null) {
                        pool.push(new CounterBean());
                    } else {
                        pool.push(entry);
                    }
                } catch (TimeoutException e) {
                    // Simple timeout while waiting on pop()
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
                finishingLine.countDown();
            }
        };

        //  -- READY --

        // How much ever the no of client invocations the count should be 10 as only 10 instances will be created.
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(r);
            t.start();
        }

        // Wait for the beans to reach the finish line
        startingLine.await(1000, TimeUnit.MILLISECONDS);

        //  -- SET --

        assertEquals(0, CounterBean.instances.get());

        //  -- GO --

        startPistol.countDown(); // go

        assertTrue(finishingLine.await(5000, TimeUnit.MILLISECONDS));

        //  -- DONE --

        assertEquals(10, CounterBean.instances.get());


    }

    private void checkMax(int max, List<Pool.Entry<String>> entries) {
        assertEquals(max, entries.size());
    }

    private void checkMin(int min, List<Pool.Entry<String>> entries) {
        int actualMin = 0;
        for (Pool.Entry<String> entry : entries) {
            if (entry != null && entry.hasHardReference()) actualMin++;
        }

        assertEquals(min, actualMin);
    }

    private void checkEntries(int expected, List<Pool.Entry<String>> entries) {
        int found = 0;
        for (Pool.Entry<String> entry : entries) {
            if (entry == null) continue;
            found++;
            assertNotNull(entry.get());
        }

        assertEquals(expected, found);
    }

    private <T> List<Pool.Entry<T>> drain(Pool<T> pool) throws InterruptedException {
        List<Pool.Entry<T>> entries = new ArrayList<Pool.Entry<T>>();
        try {
            while (true) {
                entries.add(pool.pop(0, MILLISECONDS));
            }
        } catch (TimeoutException e) {
            // pool drained
        }
        return entries;
    }

    public static class CounterBean {

        public static AtomicInteger instances = new AtomicInteger();

        private int count;

        public CounterBean() {
            count = instances.incrementAndGet();
        }

        public int count() {
            return instances.get();
        }

    }

}
