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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.threads.impl;

import org.junit.Test;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Verifies the ownership contract for
 * {@link ManagedScheduledExecutorServiceImpl}'s secondary scheduled-async pool:
 * when the MSES owns its secondary pool, {@code destroyResource()} must shut it down;
 * when it merely borrows one from another MSES, {@code destroyResource()} must NOT
 * touch the borrowed pool.
 */
public class ManagedScheduledExecutorSecondaryPoolLifecycleTest {

    @Test
    public void ownedSecondaryIsShutDownOnDestroy() {
        final ScheduledExecutorService primary = new ScheduledThreadPoolExecutor(1);
        final ScheduledExecutorService secondary = new ScheduledThreadPoolExecutor(1);
        final ContextServiceImpl ctx = ContextServiceImplFactory.getOrCreateDefaultSingleton();

        final ManagedScheduledExecutorServiceImpl mses =
                new ManagedScheduledExecutorServiceImpl(primary, ctx, secondary, true);

        assertSame("secondary exposed via getScheduledAsyncDelegate()", secondary, mses.getScheduledAsyncDelegate());

        mses.destroyResource();

        assertTrue("owned secondary must be shut down", secondary.isShutdown());
        assertTrue("primary must be shut down", primary.isShutdown());
    }

    @Test
    public void borrowedSecondaryIsNotShutDownOnDestroy() {
        final ScheduledExecutorService primary = new ScheduledThreadPoolExecutor(1);
        final ScheduledExecutorService borrowedSecondary = new ScheduledThreadPoolExecutor(1);
        final ContextServiceImpl ctx = ContextServiceImplFactory.getOrCreateDefaultSingleton();

        final ManagedScheduledExecutorServiceImpl mses =
                new ManagedScheduledExecutorServiceImpl(primary, ctx, borrowedSecondary, false);

        mses.destroyResource();

        assertTrue("primary must be shut down", primary.isShutdown());
        assertFalse("borrowed secondary must NOT be shut down — the lending MSES owns it",
                borrowedSecondary.isShutdown());

        borrowedSecondary.shutdownNow();
    }

    @Test
    public void legacyConstructorAliasesDelegateAsSecondary() {
        final ScheduledExecutorService primary = new ScheduledThreadPoolExecutor(1);
        final ContextServiceImpl ctx = ContextServiceImplFactory.getOrCreateDefaultSingleton();

        final ManagedScheduledExecutorServiceImpl mses =
                new ManagedScheduledExecutorServiceImpl(primary, ctx);

        assertSame("legacy 2-arg ctor must alias secondary to primary for back-compat",
                primary, mses.getScheduledAsyncDelegate());

        mses.destroyResource();
        assertTrue("primary shut down by super.destroyResource()", primary.isShutdown());
    }
}
