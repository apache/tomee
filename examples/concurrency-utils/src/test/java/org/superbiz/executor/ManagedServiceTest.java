package org.superbiz.executor;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


@RunWith(Arquillian.class)
public class ManagedServiceTest {

    private static final Logger LOGGER = Logger.getLogger(ManagedServiceTest.class.getName());

    @Inject
    private ManagedService managedService;

    @Deployment()
    public static final WebArchive app() {
        return ShrinkWrap.create(WebArchive.class, "example.war")
                .addClasses(ManagedService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    /**
     * Happy path. Normal invocation.
     */
    @Test
    public void managedInvocationTest() {
        final CompletableFuture<Integer> future = managedService.asyncTask(1);
        LOGGER.info("You can do something else in the meantime and later get the future value");
        try {
            // To prevent hanged tasks, you should obtain the value of a future with a timeout.
            assertEquals(3, future.get(200, TimeUnit.MILLISECONDS).intValue());
        } catch (Exception e) {
            fail("Unexpected exception" + e);
        }
    }

    /**
     * Request timeout. The result will take at least 100ms and we want it after 10ms.
     *
     * @throws InterruptedException we don't expect it
     * @throws ExecutionException   we don't expect it
     * @throws TimeoutException     Expected exception
     */
    @Test(expected = TimeoutException.class)
    public void managedInvocationTestWithTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Integer> future = managedService.asyncTask(1);
        future.get(10, TimeUnit.MILLISECONDS);
    }

    /**
     * The execution ended with an exception.
     * Handle the exception appropriately.
     *
     * @throws InterruptedException we don't expect it
     * @throws ExecutionException   Expected exception
     * @throws TimeoutException     we don't expect it
     */
    @Test
    public void managedInvocationTestWithException() {
        final CompletableFuture<Integer> future = managedService.asyncTaskWithException(1);

        try {
            future.get(200, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            // the thrown RuntimeException will be wrapped around an ExecutionException
            assertEquals("Planned exception", e.getCause().getMessage());
        } catch (Exception e) {
            fail("Unexpected exception" + e);
        }

    }

}