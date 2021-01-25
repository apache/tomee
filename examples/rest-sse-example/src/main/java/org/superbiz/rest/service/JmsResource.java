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

import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import javax.jms.Message;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

@Path("jms")
@Singleton
public class JmsResource {

    private SseBroadcaster broadcaster;
    private OutboundSseEvent.Builder builder;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void stats(final @Context SseEventSink sink) {
        broadcaster.register(sink);
    }

    @Context
    public void setSse(final Sse sse) {
        this.broadcaster = sse.newBroadcaster();
        this.builder = sse.newEventBuilder();
    }

    public void onMessage(final @Observes Message message) {
        if (broadcaster == null) {
            return;
        }

        broadcaster.broadcast(builder.data(Message.class, message).mediaType(MediaType.APPLICATION_JSON_TYPE).build());
    }
}
