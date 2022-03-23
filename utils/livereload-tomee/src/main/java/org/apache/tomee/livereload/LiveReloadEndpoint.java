/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.tomee.livereload;

import org.apache.johnzon.mapper.Mapper;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.OpenEjbVersion;

import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;

import static java.util.Arrays.asList;

@ServerEndpoint("/livereload")
public class LiveReloadEndpoint {
    private static final Command HELLO = new Command();
    static {
        HELLO.setCommand("hello");
        HELLO.setProtocols(asList("http://livereload.com/protocols/official-7", "http://livereload.com/protocols/connection-check-1"));
        HELLO.setServerName("Apache TomEE " + OpenEjbVersion.get().getVersion());
    }

    private final FileWatcher watcher;
    private final Mapper mapper;
    private final Logger logger;
    private volatile boolean loggedInfo = false;

    public LiveReloadEndpoint() {
        this.watcher = Instances.get().getWatcher();
        this.mapper = Instances.get().getMapper();
        this.logger = Logger.getInstance(Instances.get().getLogCategory(), LiveReloadEndpoint.class);
    }

    @OnMessage
    public void onMessage(final String msg, final Session session) {
        final Command command = mapper.readObject(msg, Command.class);
        if (command.isHello()) {
            try {
                session.getBasicRemote().sendText(mapper.writeObjectAsString(HELLO));
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
            this.watcher.addSession(session);
            logger.info("Registered livereload session #" + session.getId());
        } else if (command.isClientUpdate()) {
            logger.info("Ignoring livereload client update message: " + msg);
        } else if (command.isInfo()) {
            if (!loggedInfo) {
                logger.info("Livereload registration:\n  - url:" + command.getUrl() + "\n  - plugins: " + command.getPlugins());
                loggedInfo = true;
            }
        } else {
            logger.info("Unknown livereload message: " + msg);
        }
    }

    @OnClose
    public void onClose(final CloseReason reason, final Session session) {
        this.watcher.removeSession(session);
    }
}
