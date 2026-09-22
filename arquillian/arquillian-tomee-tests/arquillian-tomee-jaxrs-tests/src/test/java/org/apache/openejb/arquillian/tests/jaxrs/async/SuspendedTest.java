/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jaxrs.async;

import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class SuspendedTest {
    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(SuspendedTest.class)
            .addAsWebInfResource(new StringAsset("<beans xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" bean-discovery-mode=\"all\" version=\"4.0\"/>"), "beans.xml");
    }

    @Path("touch")
    @ApplicationScoped
    public static class Endpoint {
        private static final CountDownLatch LATCH = new CountDownLatch(1);
        private volatile AsyncResponse current;
        private static volatile Future<String> asyncPath;

        @Context
        private UriInfo info;

        @Resource
        private ManagedExecutorService es;

        @GET
        public void async(@Suspended final AsyncResponse response) {
            if (current == null) {
                try {
                    asyncPath = es.submit(new Callable<String>() {
                        @Override
                        public String call() throws Exception {
                            final String path = info.getPath();
                            assertNotNull(path);
                            return path;
                        }
                    });
                    current = response;
                } finally {
                    LATCH.countDown();
                }
            } else {
                throw new IllegalStateException("we shouldnt go here back since");
            }
        }

        @GET
        @Path("path")
        public String path() {
            try {
                return asyncPath.get();
            } catch (final InterruptedException | ExecutionException e) {
                e.printStackTrace();
                fail();
                return null;
            }
        }

        @POST
        @Path("answer")
        public void async(final String response) {
            current.resume(response); // spec doesnt mandate a new thread here but tomcat does
        }
    }

    @ArquillianResource
    private URL url;

    @Test
    public void run() throws InterruptedException {
        final AtomicReference<Response> response = new AtomicReference<>();
        final CountDownLatch end = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                try {
                    response.set(WebClient.create(url.toExternalForm() + "touch").get());
                } finally {
                    end.countDown();
                }
            }
        }.start();
        assertTrue(Endpoint.LATCH.await(1, MINUTES));
        WebClient.create(url.toExternalForm() + "touch").path("answer").post("hello");
        end.await();
        assertEquals("hello", response.get().readEntity(String.class));
        assertEquals("touch", WebClient.create(url.toExternalForm() + "touch").path("path").get(String.class));
    }
}
