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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NetworkUtilTest {

    private static File f = null;

    @BeforeClass
    public static void beforeClass() throws Exception {
        f = File.createTempFile("tomee", "lock");
    }

    @AfterClass
    public static void afterClass() {
        if (null != f && !f.delete()) {
            f.deleteOnExit();
        }
    }


    @Test
    public void testNext() throws Exception {

        NetworkUtil.clearLockFile();

        final int count = 20;
        final CountDownLatch latch = new CountDownLatch(count);
        final long start = System.currentTimeMillis();
        final CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<Integer>();

        final List<NetworkUtil.LastPort> lastPorts = new ArrayList<NetworkUtil.LastPort>();
        for (int i = 0; i < count; i++) {
            final Thread thread = new Thread(new Runnable() {
                public void run() {
                    final int nextAvailablePort = NetworkUtil.getNextAvailablePort();
                    if (list.contains(nextAvailablePort)) {
                        if ((System.currentTimeMillis() - start) < 10000) {
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

        System.out.println("VM safe port list = " + list);
    }

    @Test
    public void testNextLock() throws Exception {

        System.setProperty(NetworkUtil.TOMEE_LOCK_FILE, f.getAbsolutePath());

        final int count = 20;
        final CountDownLatch latch = new CountDownLatch(count);
        final long start = System.currentTimeMillis();
        final CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<Integer>();

        final List<NetworkUtil.LastPort> lastPorts = new ArrayList<NetworkUtil.LastPort>();
        for (int i = 0; i < count; i++) {
            final Thread thread = new Thread(new Runnable() {
                public void run() {
                    final int nextAvailablePort = NetworkUtil.getNextAvailablePort(NetworkUtil.PORT_MIN, NetworkUtil.PORT_MAX, Collections.<Integer>emptyList(), lastPorts);
                    if (list.contains(nextAvailablePort)) {
                        if ((System.currentTimeMillis() - start) < 10000) {
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

        System.out.println("Machine safe port list = " + list);
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
