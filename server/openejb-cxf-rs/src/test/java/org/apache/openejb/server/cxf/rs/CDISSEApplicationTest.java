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

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class CDISSEApplicationTest {

    private static int port = -1;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder().p("httpejbd.port", Integer.toString(port)).build();
    }

    @Module
    @Classes(cdi = true, value = {MyCdiRESTApplication.class, Resource.class})
    public WebApp war() {
        return new WebApp()
            .contextRoot("foo")
            .addServlet("REST Application", Application.class.getName())
            .addInitParam("REST Application", "jakarta.ws.rs.Application", MyCdiRESTApplication.class.getName());
    }

    @Inject
    private Event<Message> messageEvent;

    @Test
    public void testSse() throws Exception {
        final List<Message> messages = new ArrayList<>();

        final Runnable r = () -> {
            final Client client = ClientBuilder.newClient();
            final WebTarget target = client.target("http://localhost:" + port + "/foo/sse");

            final SseEventSource source = SseEventSource
                    .target(target)
                    .reconnectingEvery(500, TimeUnit.MILLISECONDS)
                    .build();

            source.register((inboundSseEvent) -> {
                final Message message = inboundSseEvent.readData(Message.class);
                messages.add(message);
            });

            source.open();
        };

        new Thread(r).start();

        Control.getInstance().waitUntilListening();
        messageEvent.fire(new Message(new Date().getTime(), "Hello"));
    }

    public static class MyCdiRESTApplication extends Application {

    }

    @Path("sse")
    @Singleton
    public static class Resource {

        private SseBroadcaster broadcaster;
        private OutboundSseEvent.Builder builder;
        private AtomicLong eventId = new AtomicLong();
        private HttpServletRequest request;

        public Resource() {
            System.out.println("Resource created");
        }

        @Context
        public void setHttpRequest(final HttpServletRequest request) {
            this.request = request;
        }

        @Context
        public void setSse(final Sse sse) {
            this.broadcaster = sse.newBroadcaster();
            this.builder = sse.newEventBuilder();
        }

        public void send(@Observes final Message message) {
            broadcaster.broadcast(createEvent(builder, eventId.incrementAndGet(), message));
        }

        @GET
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void stats(final @Context SseEventSink sink) {
            broadcaster.register(sink);
            Control.getInstance().listening();
        }

        private static OutboundSseEvent createEvent(final OutboundSseEvent.Builder builder, final long eventId, final Message message) {
            return builder
                    .id("" + eventId)
                    .data(Message.class, message)
                    .mediaType(MediaType.APPLICATION_JSON_TYPE)
                    .build();
        }
    }

    public static class Message implements Serializable {
        private static final long serialVersionUID = -6705829915457870975L;

        private long timestamp;
        private String text;

        public Message() {
        }

        public Message(final long timestamp, final String text) {
            this.timestamp = timestamp;
            this.text = text;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(final long timestamp) {
            this.timestamp = timestamp;
        }

        public String getText() {
            return text;
        }

        public void setText(final String text) {
            this.text = text;
        }
    }

    public static class Control {
        private static final Control INSTANCE = new Control();
        private final CountDownLatch listen = new CountDownLatch(1);

        private Control() {
        }

        public static Control getInstance() {
            return INSTANCE;
        }

        public void listening() {
            listen.countDown();
        }

        public void waitUntilListening() throws InterruptedException {
            listen.await(1, TimeUnit.MINUTES);
        }
    }
}
