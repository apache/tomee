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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

public class AsynchronousRunnerTest extends TestCase {

    /**
     * A delayed get, sleeping until the result is complete
     */
    public void testDelayedGet() throws Exception {
        AsynchronousRunner asyncRunner = instantiate(2000);
        Future<Object> future = asyncRunner.runAsync(object(), method(), arguments());
        assertFalse(future.isDone());
        Thread.sleep(2500);
        assertTrue(future.isDone());
        assertEquals(expected(), future.get());
    }
    
    /**
     * A regular get, waiting for result to be complete
     */
    public void testGet() throws Exception {
        AsynchronousRunner asyncRunner = instantiate();
        Future<Object> future = asyncRunner.runAsync(object(), method(), arguments());
        assertEquals(expected(), future.get());
    }
    
    /**
     * A get with max timeout
     */
    public void testTimedGet() throws Exception {
        AsynchronousRunner asyncRunner = instantiate(2000);
        Future<Object> future = asyncRunner.runAsync(object(), method(), arguments());
        try {
            future.get(1, TimeUnit.SECONDS);
            fail();
        } catch (TimeoutException e) {
            //Ok
        }
        Thread.sleep(1500);
        assertTrue(future.isDone());
        assertEquals(expected(), future.get());
    }
    
    /**
     * Tests the cancel method
     */
    public void testCancel() throws Exception {
        AsynchronousRunner asyncRunner = instantiate(2000);
        Future<Object> future = asyncRunner.runAsync(object(), method(), arguments());
        future.cancel(true);
        assertTrue(future.isCancelled());
        try {
            future.get();
        } catch (CancellationException e) {
            //Ok
        }
    }
    
    private Object[] arguments() {
        return new Object[] {BigDecimal.ONE};
    }
    
    private BigDecimal expected() {
        return new BigDecimal(11);
    }
    
    private AsynchronousRunner instantiate() {
        return instantiate(0);
    }

    private AsynchronousRunner instantiate(long timeout) {
        System.setProperty("openejb.asynchronousRunnerSleep", "" + timeout);
        return new AsynchronousRunner(Executors.newCachedThreadPool());
    }
    
    private Method method() {
        try {
            return BigDecimal.class.getMethod("add", BigDecimal.class);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    private Object object() {
        return BigDecimal.TEN;
    }

}

