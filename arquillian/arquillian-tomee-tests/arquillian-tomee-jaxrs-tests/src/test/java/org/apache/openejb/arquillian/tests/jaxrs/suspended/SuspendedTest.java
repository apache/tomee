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
package org.apache.openejb.arquillian.tests.jaxrs.suspended;

import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import jakarta.ws.rs.core.Response;

import static java.lang.Thread.sleep;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class SuspendedTest {
    @Deployment(testable = false)
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, "suspended.war")
                .addClasses(Endpoint.class, Endpoint.RunThread.class);
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
                response.set(WebClient.create(url.toExternalForm() + "touch").accept(TEXT_PLAIN_TYPE).get());
                end.countDown();
            }
        }.start();
        final WebClient client = WebClient.create(url.toExternalForm() + "touch");
        for (int i = 0; i < 120 && !client.reset().path("check").accept(TEXT_PLAIN_TYPE).get(Boolean.class); i++) {
            sleep(1000);
        }
        client.reset().path("answer").post("hello");
        end.await();
        assertEquals("hello", response.get().readEntity(String.class));
    }
}
