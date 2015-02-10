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
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

@EnableServices("jaxrs")
@Classes(cdi = true, innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class SuspendedTest {
    @Path("touch")
    @ApplicationScoped
    public static class Endpoint {
        private static final CountDownLatch LATCH = new CountDownLatch(1);
        private volatile AsyncResponse current;

        @GET
        public String async(@Suspended final AsyncResponse response) {
            if (current == null) {
                current = response;
                LATCH.countDown();
                return "ignored";
            } else {
                throw new IllegalStateException("we shouldnt go here back since");
            }
        }

        @POST
        @Path("answer")
        public void async(final String response) {
            current.resume(response);
        }        
    }

    @RandomPort("http")
    private URL url;

    @Test
    public void run() throws InterruptedException {
        final AtomicReference<Response> response = new AtomicReference<>();
        final CountDownLatch end = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                response.set(WebClient.create(url.toExternalForm() + "openejb/touch").get());
                end.countDown();
            }
        }.start();
        Endpoint.LATCH.await();
        WebClient.create(url.toExternalForm() + "openejb/touch").path("answer").post("hello");
        end.await();
        assertEquals("hello", response.get().readEntity(String.class));
    }
}
