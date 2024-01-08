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
package org.superbiz.rest.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.enterprise.event.Observes;
import jakarta.jms.Message;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;

@Path("jms")
@Singleton
public class JmsResource {

    private SseBroadcaster sseBroadcaster;
    private OutboundSseEvent.Builder builder;

    @EJB
    private Broadcaster broadcaster;

    @EJB
    private Producer producer;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void stats(final @Context SseEventSink sink) {
        sseBroadcaster.register(sink);
    }

    @POST
    @Path("send/{brokerid}")
    public void send(@PathParam("brokerid") final String brokerId, final String message) {
        producer.sendMessage(message, brokerId);
    }

    @POST
    @Path("broadcast")
    public void setBroadcast(final String message) {
        broadcaster.broadcastMessage(message);
    }

    @Context
    public void setSse(final Sse sse) {
        this.sseBroadcaster = sse.newBroadcaster();
        this.builder = sse.newEventBuilder();
    }

    public void onMessage(final @Observes Message message) {
        if (sseBroadcaster == null) {
            return;
        }

        sseBroadcaster.broadcast(builder.data(Message.class, message).mediaType(MediaType.APPLICATION_JSON_TYPE).build());
    }
}
