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

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class ManagedScheduledServiceTest {

    @Inject
    private ManagedScheduledService scheduledService;

    @Deployment()
    public static final WebArchive app() {
        return ShrinkWrap.create(WebArchive.class, "example.war")
                .addClasses(ManagedScheduledService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }


    @Test
    public void singleFixedDelayTask() throws InterruptedException, ExecutionException, TimeoutException {
        final Future<Integer> futureA = scheduledService.singleFixedDelayTask(1, null);
        final Future<Integer> futureB = scheduledService.singleFixedDelayTask(50, null);
        System.out.println("Do some other work while we wait for the tasks");
        assertEquals(2, futureA.get(200, TimeUnit.MILLISECONDS).intValue());
        assertEquals(51, futureB.get(200, TimeUnit.MILLISECONDS).intValue());

    }

    @Test
    public void periodicFixedDelayTask() throws InterruptedException, TimeoutException, ExecutionException {
        final ScheduledFuture<?> scheduledFuture = scheduledService.periodicFixedDelayTask(1, null);
        TimeUnit.MILLISECONDS.sleep(500);
        if (!scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
            System.out.println("task stopped");
        }
    }


    @Test
    public void singleFixedDelayTaskWithException() throws InterruptedException, ExecutionException, TimeoutException {
        final Future<Integer> future = scheduledService.singleFixedDelayTask(1, "Planned exception");
        try {
            future.get(200, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            // the thrown RuntimeException will be wrapped around an ExecutionException
            assertEquals("Planned exception", e.getCause().getMessage());
        } catch (Exception e) {
            fail("Unexpected exception" + e);
        }
    }

    @Test
    public void periodicFixedDelayTaskWithException() throws InterruptedException {
        final ScheduledFuture<?> scheduledFuture = scheduledService.periodicFixedDelayTask(1, "Planned exception");
        TimeUnit.MILLISECONDS.sleep(500);

        try {
            scheduledFuture.get(200, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            // the thrown RuntimeException will be wrapped around an ExecutionException
            assertEquals("Planned exception", e.getCause().getMessage());
        } catch (Exception e) {
            fail("Unexpected exception" + e);
        }

        if (!scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
            System.out.println("task stopped");
        }
    }

}