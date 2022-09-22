/*
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

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class CachedSupplierTest {

    /**
     * Supplier returns a valid result immediately and there
     * are no delays on the first get.
     *
     * We also assert that calling get multiple times on the
     * CachedSupplier return the cached result and do not get
     * updated results before the refresh occurs.
     */
    @Test
    public void happyPath() {
    }

    /**
     * Supplier does not immediately return an initial instance, so we
     * block till one is available. We assert that we blocked until get
     * a valid result and no timeout or null is returned.
     */
    @Test
    public void delayedInitialization() {
    }

    /**
     * Supplier does not immediately return an initial instance
     * and the timeout is reached.  We assert a TimeoutException
     * is thrown.  We also assert that when the Supplier eventually
     * does return a valid result we no longer get a TimeoutException
     * or any blocking.
     */
    @Test
    public void delayedInitializationTimeout() {
    }

    /**
     * Supplier returns null on the first three calls to get.  On the
     * fourth retry a valid result is returned from get.  We assert
     * the number of times the supplier get is called as well as the
     * time between each call to ensure exponential backoff is working
     */
    @Test
    public void initializationRetry() {
    }

    /**
     * Supplier returns null repeatedly on all initialization attempts.
     * We assert that when the max retry time is reached all subsequent
     * retries are at that same time interval and do not continue increasing
     * exponentially.
     */
    @Test
    public void initializationRetryTillMax() {
    }

    /**
     * Suppler returns a valid result on initialization.  We expect that even
     * when we are not actively calling get() the value will be refreshed
     * according to the refreshInterval.  We wait for at least 3 refreshes
     * to occur and assert the value we get is the most recent value returned
     * from the supplier.  We intentionally check for this expected value
     * while the refresh is currently executing for the fourth time.  We do
     * that to ensure that there is no time values are null, even when we're
     * concurrently trying to refresh it in the background.
     */
    @Test
    public void refreshReliablyCalled() {
    }


    /**
     * On the first refresh the Supplier returns null indicating there is
     * no valid replacement.  We assert that the previous valid value is
     * still in use.
     */
    @Test
    public void refreshFailedWithNull() {
    }

    /**
     * On the first refresh the Supplier throws an exception, therefore there is
     * no valid replacement.  We assert that the previous valid value is
     * still in use.
     */
    @Test
    public void refreshFailedWithException() {
    }

}
