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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import java.io.Serializable;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
@Ignore
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
            .addInitParam("REST Application", "javax.ws.rs.Application", MyCdiRESTApplication.class.getName());
    }

    @Test
    public void isCdi() {
        assertEquals("[{\"id\":1}]", ClientBuilder.newClient().target("http://localhost:" + port).path("/foo/sse").request().get(String.class));
    }

    public static class MyCdiRESTApplication extends Application {

    }

    @Path("sse")
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

        @POST
        @Consumes(MediaType.TEXT_PLAIN)
        public void send(final String message) {
            broadcaster.broadcast(createEvent(builder, eventId.incrementAndGet(), message));
        }

        @GET
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void stats(final @Context SseEventSink sink) {
            broadcaster.register(sink);
        }

        private static OutboundSseEvent createEvent(final OutboundSseEvent.Builder builder, final long eventId, final String text) {
            return builder
                    .id("" + eventId)
                    .data(Message.class, new Message(new Date().getTime(), text))
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
}
