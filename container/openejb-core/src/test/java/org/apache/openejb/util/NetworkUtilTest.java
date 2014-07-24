/**
 * Tomitribe Confidential
 * <p/>
 * Copyright(c) Tomitribe Corporation. 2014
 * <p/>
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 * <p/>
 */
package org.apache.openejb.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NetworkUtilTest {

    @Test
    public void test() throws Exception {

        final int count = 20;
        final CountDownLatch latch = new CountDownLatch(count);
        final long start = System.currentTimeMillis();
        final CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<Integer>();

        for (int i = 0; i < count; i++) {
            final Thread thread = new Thread(new Runnable() {
                public void run() {
                    final int nextAvailablePort = NetworkUtil.getNextAvailablePort();
                    if (list.contains(nextAvailablePort)) {
                        if ((System.currentTimeMillis() - start) > 10000)
                            Assert.fail("Got a duplicate port with ten seconds");
                    } else {
                        list.add(nextAvailablePort);
                    }

                    latch.countDown();
                }
            }, "test-thread-" + count);
            thread.setDaemon(false);
            thread.start();
        }

        final boolean success = latch.await(15, TimeUnit.SECONDS);
        Assert.assertTrue(success);

        System.out.println("Thread safe port list = " + list);
    }
}
