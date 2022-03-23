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
 *
 */
package org.superbiz.websockets;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@ServerEndpoint("/chat")
@ApplicationScoped
public class WebSocketResource {

    @Inject
    private MessageSender messageSender;

    private static final Logger LOG = Logger.getLogger(WebSocketResource.class.getName());
    private static final Set<Session> SESSIONS = Collections.synchronizedSet(new HashSet<>());


    @OnOpen
    public void onOpen(final Session session) throws Exception {
        SESSIONS.add(session);
    }

    @OnClose
    public void onClose(final Session session) throws IOException {
        SESSIONS.remove(session);
    }

    @OnMessage
    public void onMessage(final String message, final Session session) {
        messageSender.send(message);
    }

    @OnError
    public void onError(final Session session, final Throwable throwable) {
        LOG.warning("websocket error: " + throwable.getMessage());
    }

    public void broadcast(final @Observes MessageReceivedEvent event) {
        SESSIONS.forEach(s -> {
            try {
                s.getBasicRemote().sendText(event.getMessage());
            } catch (IOException e) {
                LOG.warning("Error sending websocket message: " + e.getMessage());
            }
        });
    }
}
