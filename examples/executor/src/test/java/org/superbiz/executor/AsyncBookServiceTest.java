package org.superbiz.executor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

@RunWith(Arquillian.class)
public class AsyncBookServiceTest {

    @Inject
    private AsyncBookService service;

    @Deployment()
    public static final WebArchive app() {
        return ShrinkWrap.create(WebArchive.class, "example.war")
                .addClasses(AsyncBookService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testServiceA() {
        final Future<String> future = service.serviceA();
        try {
            future.get(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException e) {
            fail("Unexpected exception" + e);
        } catch (ExecutionException ioe) {
            assertEquals("Simulated error", ioe.getCause().getMessage());
        }
    }

    @Test
    public void testServiceB() {
        final CompletableFuture<Integer> future = service.serviceB();
        try {
            assertEquals(2, future.get(200, TimeUnit.MILLISECONDS).intValue());
        } catch (Exception e) {
            fail("Unexpected exception" + e);
        }
    }

}