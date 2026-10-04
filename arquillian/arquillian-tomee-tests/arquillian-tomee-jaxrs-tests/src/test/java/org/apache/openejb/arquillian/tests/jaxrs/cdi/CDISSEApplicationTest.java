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
package org.apache.openejb.arquillian.tests.jaxrs.cdi;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@RunWith(Arquillian.class)
public class CDISSEApplicationTest {
    @ArquillianResource
    private URL base;

    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(CDISSEApplicationTest.class)
            .addAsWebInfResource(new StringAsset("<beans xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" bean-discovery-mode=\"all\" version=\"4.0\"/>"), "beans.xml")
            .setWebXML(new StringAsset("<web-app xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" version=\"6.0\">" +
                "<servlet>" +
                "<servlet-name>REST Application</servlet-name>" +
                "<servlet-class>" + Application.class.getName() + "</servlet-class>" +
                "<init-param>" +
                "<param-name>jakarta.ws.rs.Application</param-name>" +
                "<param-value>" + MyCdiRESTApplication.class.getName() + "</param-value>" +
                "</init-param>" +
                "</servlet>" +
                "</web-app>"));
    }

    @Inject
    private Event<Message> messageEvent;

    @Test
    public void testSse() throws Exception {
        final List<Message> messages = new ArrayList<>();

        final Runnable r = () -> {
            final Client client = ClientBuilder.newClient();
            final WebTarget target = client.target(base.toExternalForm() + "sse");

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
