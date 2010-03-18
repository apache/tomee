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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.concurrent.TimeoutException;
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
        Runnable r = new Runnable() {
            public void run() {
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

    /**
     * Tests the idle timeout as well as the Thread pool
     * used to invoke the discard/create jobs.
     *
     * 
     * @throws Exception exception
     */
    public void testIdleTimeout() throws Exception {

        final int min = 4;
        final int max = 9;
        final int idleTimeout = 500;

        final CountDownLatch discarded = new CountDownLatch(max - min);
        final CountDownLatch hold = new CountDownLatch(1);

        final Pool.Builder builder = new Pool.Builder();
        builder.setPoolMin(min);
        builder.setPoolMax(max);
        builder.setIdleTimeout(new Duration(idleTimeout, TimeUnit.MILLISECONDS));
        builder.setPollInterval(new Duration(idleTimeout / 2, TimeUnit.MILLISECONDS));
        builder.setSupplier(new Pool.Supplier() {
            public void discard(Object o) {
                discarded.countDown();
                try {
                    // Executor should have enough threads
                    // to execute removes on all the
                    // timed out objects.
                    hold.await();
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }

            public Object create() {
                // Should never be called
                return new CounterBean();
            }
        });


        final Pool pool = builder.build();

        // Fill pool to max
        CounterBean.instances.set(0);
        for (int i = 0; i < max; i++) {
            assertTrue(pool.add(new CounterBean()));
        }


        { // Should have a full, non-null pool
            final List entries = drain(pool, 100);
            checkMin(min, entries);
            checkEntries(max, entries);
            push(pool, entries);
        }


        discarded.await();
//        assertTrue(discarded.await(idleTimeout * 2, TimeUnit.MILLISECONDS));

        { // Pool should only have min number of non-null entries
            final List entries = drain(pool, 100);
            checkMin(min, entries);
            checkEntries(min, entries);
            push(pool, entries);
        }

        //  -- DONE --

        assertEquals(max, CounterBean.instances.get());
    }

    public void testFlush() throws Exception {

        final int min = 4;
        final int max = 9;
        final int poll = 200;

        final CountDownLatch discarded = new CountDownLatch(max);
        final CountDownLatch created = new CountDownLatch(min);
        final CountDownLatch createInstances = new CountDownLatch(1);

        final Pool.Builder builder = new Pool.Builder();
        builder.setPoolMin(min);
        builder.setPoolMax(max);
        builder.setPollInterval(new Duration(poll, TimeUnit.MILLISECONDS));
        builder.setSupplier(new Pool.Supplier() {
            public void discard(Object o) {
                discarded.countDown();
            }

            public Object create() {
                try {
                    createInstances.await();
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
                try {
                    return new CounterBean();
                } finally {
                    created.countDown();
                }
            }
        });


        final Pool pool = builder.build();

        // Fill pool to max
        CounterBean.instances.set(0);
        for (int i = 0; i < max; i++) {
            assertTrue(pool.add(new CounterBean()));
        }


        { // Should have a full, non-null pool
            final List entries = drain(pool, 100);
            checkMin(min, entries);
            checkEntries(max, entries);
            push(pool, entries);
        }

        pool.flush();

        // Wait for the Evictor to come around and sweep out the pool
        assertTrue(discarded.await(poll * 10, TimeUnit.MILLISECONDS));

        // Minimum instances are still being created
        // The rest of the pool should be empty (null)
        {
            final List entries = drain(pool, 100);
            // Should have "non-min" number of entries
            checkMax(max - min, entries);

            // Nothing should be a "min" item as those
            // are still being created
            checkMin(0, entries);

            // And as the pool was just drained all the
            // entries we get should be null
            checkNull(entries);

            push(pool, entries);
        }

        CounterBean.instances.set(0);

        // Try and trick the pool into adding more "min" items
        // Fill the pool as much as we can -- should only let us
        // fill to the max factoring in the "min" instances that
        // are currently being created
        {
            final List entries = drain(pool, 100);

            // Should reject the instance we try to add

            assertFalse(pool.add(new CounterBean()));


            // Empty the pool
            discard(pool, entries);


            // Now count how many instances it lets us add
            CounterBean.instances.set(0);
            while (pool.add(new CounterBean())) ;

            // As the "min" instances are still being created
            // it should only let us fill max - min, then + 1
            // to account for the instance that gets rejected
            // and terminates the while loop
            final int expected = max - min + 1;
            
            assertEquals(expected, CounterBean.instances.getAndSet(0));
        }

        // Ok, let the "min" instance creation threads continue
        createInstances.countDown();

        // Wait for the "min" instance creation to complete
        assertTrue(created.await(poll * 10, TimeUnit.MILLISECONDS));

        { // Pool should be full again
            final List entries = drain(pool, 100);
            checkMin(min, entries);
            checkEntries(max, entries);
            push(pool, entries);
        }

        //  -- DONE --
    }

    public void testMaxAge() throws Exception {
        System.out.println("PoolTest.testMaxAge");
        final int min = 4;
        final int max = 9;
        final int maxAge = 500;
        final int poll = maxAge / 2;

        final CountDownLatch discarded = new CountDownLatch(max);
        final CountDownLatch created = new CountDownLatch(min);
        final CountDownLatch createInstances = new CountDownLatch(1);

        final Pool.Builder builder = new Pool.Builder();
        builder.setPoolMin(min);
        builder.setPoolMax(max);
        builder.setMaxAge(new Duration(maxAge, MILLISECONDS));
        builder.setPollInterval(new Duration(poll, MILLISECONDS));
        builder.setSupplier(new Pool.Supplier<CounterBean>() {
            public void discard(CounterBean o) {
                countDown(discarded, o);
            }

            public CounterBean create() {
                try {
                    createInstances.await();
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
                try {
                    return new CounterBean();
                } finally {
                    created.countDown();
                }
            }
        });


        final Pool pool = builder.build();

        // Fill pool to max
        CounterBean.instances.set(0);
        for (int i = 0; i < max; i++) {
            assertTrue(pool.add(new CounterBean()));
        }


        { // Should have a full, non-null pool
            final List entries = drain(pool, 100);
            checkMin(min, entries);
            checkEntries(max, entries);
            push(pool, entries);
        }

        // Now wait for the instances in the pool to expire
        assertTrue(discarded.await(maxAge * 4, TimeUnit.MILLISECONDS));

        // Minimum instances are still being created
        // The rest of the pool should be empty (null)
        {
            final List entries = drain(pool, 100);

            // Nothing should be a "min" item as those
            // are still being created
            checkMin(0, entries);

            // And as the pool was just drained all the
            // entries we get should be null
            checkNull(entries);

            // Should have "non-min" number of entries
            checkMax(max - min, entries);

            push(pool, entries);
        }

        CounterBean.instances.set(0);

        // Try and trick the pool into adding more "min" items
        // Fill the pool as much as we can -- should only let us
        // fill to the max factoring in the "min" instances that
        // are currently being created
        {
            final List entries = drain(pool, 100);

            // Should reject the instance we try to add

            assertFalse(pool.add(new CounterBean()));


            // Empty the pool
            discard(pool, entries);


            // Now count how many instances it lets us add
            CounterBean.instances.set(0);
            while (pool.add(new CounterBean())) ;

            // As the "min" instances are still being created
            // it should only let us fill max - min, then + 1
            // to account for the instance that gets rejected
            // and terminates the while loop
            final int expected = max - min + 1;

            assertEquals(expected, CounterBean.instances.getAndSet(0));
        }

        // Ok, let the "min" instance creation threads continue
        createInstances.countDown();

        // Wait for the "min" instance creation to complete
        assertTrue(created.await(poll * 10, TimeUnit.MILLISECONDS));

        { // Pool should be full again
            final List entries = drain(pool, 100);
            checkMin(min, entries);
            checkEntries(max, entries);
            push(pool, entries);
        }

        //  -- DONE --
    }

    private void countDown(CountDownLatch discarded, CounterBean o) {
        discarded.countDown();
        System.out.format("%1$tH:%1$tM:%1$tS.%1$tL  %2$s\n", System.currentTimeMillis(), o.count());
//        try {
//            Thread.sleep(50);
//        } catch (InterruptedException e) {
//            Thread.interrupted();
//        }
    }

    /**
     * What happens if we fail to create a "min" instance after a flush?
     *
     * @throws Exception exception
     */
    public void testFlushFailedCreation() throws Exception {

        final int min = 4;
        final int max = 9;
        final int poll = 200;

        final CountDownLatch discarded = new CountDownLatch(max);
        final CountDownLatch created = new CountDownLatch(min);
        final CountDownLatch createInstances = new CountDownLatch(1);

        final Pool.Builder builder = new Pool.Builder();
        builder.setPoolMin(min);
        builder.setPoolMax(max);
        builder.setPollInterval(new Duration(poll, TimeUnit.MILLISECONDS));
        builder.setSupplier(new Pool.Supplier() {
            public void discard(Object o) {
                discarded.countDown();
            }

            public Object create() {
                try {
                    createInstances.await();
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
                try {
                    throw new RuntimeException();
                } finally {
                    created.countDown();
                }
            }
        });


        final Pool pool = builder.build();

        // Fill pool to max
        CounterBean.instances.set(0);
        for (int i = 0; i < max; i++) {
            assertTrue(pool.add(new CounterBean()));
        }


        { // Should have a full, non-null pool
            final List entries = drain(pool, 100);
            checkMin(min, entries);
            checkEntries(max, entries);
            push(pool, entries);
        }

        pool.flush();

        // Wait for the Evictor to come around and sweep out the pool
        assertTrue(discarded.await(poll * 10, TimeUnit.MILLISECONDS));

        // Minimum instances are still being created
        // The rest of the pool should be empty (null)
        {
            final List entries = drain(pool, 100);
            // Should have "non-min" number of entries
            checkMax(max - min, entries);

            // Nothing should be a "min" item as those
            // are still being created
            checkMin(0, entries);

            // And as the pool was just drained all the
            // entries we get should be null
            checkNull(entries);

            push(pool, entries);
        }

        CounterBean.instances.set(0);

        // Try and trick the pool into adding more "min" items
        // Fill the pool as much as we can -- should only let us
        // fill to the max factoring in the "min" instances that
        // are currently being created
        {
            final List entries = drain(pool, 100);

            // Should reject the instance we try to add

            assertFalse(pool.add(new CounterBean()));


            // Empty the pool
            discard(pool, entries);


            // Now count how many instances it lets us add
            CounterBean.instances.set(0);
            while (pool.add(new CounterBean())) ;

            // As the "min" instances are still being created
            // it should only let us fill max - min, then + 1
            // to account for the instance that gets rejected
            // and terminates the while loop
            final int expected = max - min + 1;

            assertEquals(expected, CounterBean.instances.getAndSet(0));
        }

        // Ok, let the "min" instance creation threads continue
        createInstances.countDown();

        // Wait for the "min" instance creation to complete
        assertTrue(created.await(poll * 10, TimeUnit.MILLISECONDS));

        { // Pool should be full but...
            final List entries = drain(pool, 100);
            // we failed to create the min instances
            checkMin(0, entries);

            // the "min" entries should have been freed up
            // and we should have all the possible entires
            checkMax(max, entries);

            // though there should be "min" quantities of nulls
            checkEntries(max-min, entries);

            // Now when we push these back in, the right number
            // of entries should be converted to "min" entries
            push(pool, entries);

        }

        { // Pool should be full but...
            final List entries = drain(pool, 100);

            // now we should have the right number of mins
            checkMin(min, entries);

            // should still have a full pool
            checkMax(max, entries);

            // though there should still be "min" quantities of nulls
            // as we still haven't created any more instances, we just
            // converted some of our instances into "min" entries
            checkEntries(max-min, entries);

        }

        //  -- DONE --
    }

    /**
     * What happens if we fail to create a "min" instance after a maxAge expiration?
     *
     * @throws Exception exception
     */
    public void testMaxAgeFailedCreation() throws Exception {
        System.out.println("PoolTest.testMaxAgeFailedCreation");
        final int min = 4;
        final int max = 9;
        final int maxAge = 1000;
        final int poll = 100;

        final CountDownLatch discarded = new CountDownLatch(max);
        final CountDownLatch created = new CountDownLatch(min);
        final CountDownLatch createInstances = new CountDownLatch(1);

        final Pool.Builder builder = new Pool.Builder();
        builder.setPoolMin(min);
        builder.setPoolMax(max);
        builder.setMaxAge(new Duration(maxAge, MILLISECONDS));
        builder.setPollInterval(new Duration(poll, MILLISECONDS));
        builder.setSupplier(new Pool.Supplier<CounterBean>() {
            public void discard(CounterBean o) {
                countDown(discarded, o);
            }

            public CounterBean create() {
                try {
                    createInstances.await();
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
                try {
                    throw new RuntimeException();
                } finally {
                    created.countDown();
                }
            }
        });


        final Pool pool = builder.build();

        // Fill pool to max
        CounterBean.instances.set(0);
        for (int i = 0; i < max; i++) {
            assertTrue(pool.add(new CounterBean()));
        }


        { // Should have a full, non-null pool
            final List entries = drain(pool, 100);
            checkMin(min, entries);
            checkEntries(max, entries);
            push(pool, entries);
        }

        // Now wait for the instances in the pool to expire
        assertTrue(discarded.await(maxAge * 4, TimeUnit.MILLISECONDS));

        // Minimum instances are still being created
        // The rest of the pool should be empty (null)
        {
            final List entries = drain(pool, 100);
            // Should have "non-min" number of entries
            checkMax(max - min, entries);

            // Nothing should be a "min" item as those
            // are still being created
            checkMin(0, entries);

            // And as the pool was just drained all the
            // entries we get should be null
            checkNull(entries);

            push(pool, entries);
        }

        CounterBean.instances.set(0);

        // Try and trick the pool into adding more "min" items
        // Fill the pool as much as we can -- should only let us
        // fill to the max factoring in the "min" instances that
        // are currently being created
        {
            final List entries = drain(pool, 100);

            // Should reject the instance we try to add

            assertFalse(pool.add(new CounterBean()));


            // Empty the pool
            discard(pool, entries);


            // Now count how many instances it lets us add
            CounterBean.instances.set(0);
            while (pool.add(new CounterBean())) ;

            // As the "min" instances are still being created
            // it should only let us fill max - min, then + 1
            // to account for the instance that gets rejected
            // and terminates the while loop
            final int expected = max - min + 1;

            assertEquals(expected, CounterBean.instances.getAndSet(0));
        }

        // Ok, let the "min" instance creation threads continue
        createInstances.countDown();

        // Wait for the "min" instance creation to complete
        assertTrue(created.await(poll * 10, TimeUnit.MILLISECONDS));

        { // Pool should be full but...
            final List entries = drain(pool, 100);
            // we failed to create the min instances
            checkMin(0, entries);

            // the "min" entries should have been freed up
            // and we should have all the possible entires
            checkMax(max, entries);

            // though there should be "min" quantities of nulls
            checkEntries(max-min, entries);

            // Now when we push these back in, the right number
            // of entries should be converted to "min" entries
            push(pool, entries);

        }

        { // Pool should be full but...
            final List entries = drain(pool, 100);

            // now we should have the right number of mins
            checkMin(min, entries);

            // should still have a full pool
            checkMax(max, entries);

            // though there should still be "min" quantities of nulls
            // as we still haven't created any more instances, we just
            // converted some of our instances into "min" entries
            checkEntries(max-min, entries);

        }

        //  -- DONE --
    }


    private <T> void checkMax(int max, List<Pool.Entry<T>> entries) {
        assertEquals(max, entries.size());
    }

    private <T> void checkMin(int min, List<Pool.Entry<T>> entries) {
        assertEquals(min, getMin(entries).size());
    }

    private <T> void checkNull(List<Pool.Entry<T>> entries) {
        for (Pool.Entry<T> entry : entries) {
            assertNull(entry);
        }
    }

    private <T> List<Pool.Entry<T>> getMin(List<Pool.Entry<T>> entries) {
        List<Pool.Entry<T>> list = new ArrayList<Pool.Entry<T>>();

        for (Pool.Entry<T> entry : entries) {
            if (entry != null && entry.hasHardReference()) list.add(entry);
        }
        return list;
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
        return drain(pool, 0);
    }

    private <T> List<Pool.Entry<T>> drain(Pool<T> pool, int timeout) throws InterruptedException {
        List<Pool.Entry<T>> entries = new ArrayList<Pool.Entry<T>>();
        try {
            while (true) {
                entries.add(pool.pop(timeout, MILLISECONDS));
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
            return count;
        }

    }

}
