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

import jakarta.annotation.Resource;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Reception;
import jakarta.inject.Singleton;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Message;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
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

    @Resource(name="EVENT")
    private Topic topic;

    @Resource
    private ConnectionFactory cf;

    private SseBroadcaster broadcaster;
    private OutboundSseEvent.Builder builder;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void stats(final @Context SseEventSink sink) {
        broadcaster.register(sink);
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void sendMessage(final String message) {
        try {
            final Connection connection = cf.createConnection();
            final Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            final TextMessage textMessage = session.createTextMessage(message);
            final MessageProducer producer = session.createProducer(topic);
            producer.send(textMessage);
            producer.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Context
    public void setSse(final Sse sse) {
        this.broadcaster = sse.newBroadcaster();
        this.builder = sse.newEventBuilder();
    }

    public void onMessage(final @Observes(notifyObserver = Reception.IF_EXISTS) Message message) {
        if (broadcaster == null) {
            return;
        }

        broadcaster.broadcast(builder.data(Message.class, message).mediaType(MediaType.APPLICATION_JSON_TYPE).build());
    }
}
