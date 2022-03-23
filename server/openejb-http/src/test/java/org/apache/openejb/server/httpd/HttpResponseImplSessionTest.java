/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.httpd;

import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.ivm.naming.IvmJndiFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.httpd.session.SessionManager;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import jakarta.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HttpResponseImplSessionTest {
    private OpenEJBHttpEjbServer server;

    private static int numberOfSessions() throws IllegalAccessException {
        return SystemInstance.get().getComponent(SessionManager.class).size();
    }

    @Before
    public void start() throws Exception {
        SystemInstance.get().setComponent(ContainerSystem.class, new CoreContainerSystem(new IvmJndiFactory()));

        server = new OpenEJBHttpEjbServer();
        server.init(new Properties());
        server.start();
    }

    private String request(final InOutSocket socket) throws ServiceException, IOException {
        server.service(socket);
        return socket.out();
    }

    @After
    public void stop() throws ServiceException {
        server.stop();
        SystemInstance.reset();
    }

    @Test
    public void noSession() throws Exception {
        final int numberOfSessionsBefore = numberOfSessions();
        assertFalse(request(new InOutSocket().reset("GET /foo")).contains("Set-Cookie"));
        assertEquals(numberOfSessionsBefore, numberOfSessions());
    }

    @Test
    public void session() throws Exception {
        final AtomicReference<Boolean> clearSession = new AtomicReference<Boolean>(false);

        SystemInstance.get().getComponent(HttpListenerRegistry.class).addHttpListener(new HttpListener() {
            @Override
            public void onMessage(final HttpRequest request, final HttpResponse response) throws Exception {
                final HttpSession session = request.getSession(true);
                if (clearSession.get()) {
                    session.invalidate();
                }
            }
        }, "/session");

        try {
            final int numberOfSessions = numberOfSessions() + 1; // first request will create one
            final InOutSocket socket = new InOutSocket();
            String session = null;
            for (int i = 0; i < 3; i++) {
                String request = "GET /session";
                if (session != null) {
                    request += "\nCookie: EJBSESSIONID=" + session + ";\n";
                }
                final String response = request(socket.reset(request));
                assertTrue(response.contains("Set-Cookie"));

                if (session == null) {
                    final int beginIndex = response.indexOf("EJBSESSIONID=") + "EJBSESSIONID=".length();
                    session = response.substring(beginIndex, response.indexOf(";", beginIndex));
                }

                assertEquals(numberOfSessions, numberOfSessions());
            }

            clearSession.set(true);
            request(socket.reset("GET /session\nCookie: EJBSESSIONID=" + session + ";\n"));
            assertEquals(numberOfSessions - 1, numberOfSessions());
        } finally {
            SystemInstance.get().getComponent(HttpListenerRegistry.class).removeHttpListener("/session");
        }
    }

    private static class InOutSocket extends Socket {
        private ByteArrayInputStream inputStream;
        private ByteArrayOutputStream outputStream;

        @Override
        public InputStream getInputStream() throws IOException {
            return inputStream;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return outputStream;
        }

        public InOutSocket reset(final String input) {
            inputStream = new ByteArrayInputStream(input.getBytes());
            outputStream = new ByteArrayOutputStream();
            return this;
        }

        public String out() {
            return new String(outputStream.toByteArray());
        }
    }
}
