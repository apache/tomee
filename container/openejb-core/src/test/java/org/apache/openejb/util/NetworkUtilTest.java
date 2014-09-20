/**
 *
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

import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;
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
                        if ((System.currentTimeMillis() - start) > 10000) {
                            Assert.fail("Got a duplicate port with ten seconds");
                        }
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

    @Test
    public void testLocal() throws Exception {
        Assert.assertTrue(NetworkUtil.isLocalAddress(InetAddress.getLocalHost()));
    }

    @Test
    public void testLocalhost() throws Exception {
        Assert.assertTrue(NetworkUtil.isLocalAddress("localhost"));
    }
}
