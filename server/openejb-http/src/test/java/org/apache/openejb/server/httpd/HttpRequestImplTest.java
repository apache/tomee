/**
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
package org.apache.openejb.server.httpd;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.session.SessionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HttpRequestImplTest {
    @Before
    public void init() {
        SystemInstance.get().setComponent(SessionManager.class, new SessionManager());
    }

    @After
    public void reset() {
        SystemInstance.reset();
    }

    @Test
    public void run() throws URISyntaxException {
        final HttpRequest req = new HttpRequestImpl(new URI("http://localhost:1234/foo"));
        final jakarta.servlet.http.HttpSession session = req.getSession();
        assertNotNull(session);
        session.invalidate();
        assertNull(req.getSession(false));
    }

    @Test
    public void changeSessionIdRekeysSession() throws URISyntaxException {
        final HttpRequestImpl req = new HttpRequestImpl(new URI("http://localhost:1234/foo"));
        final jakarta.servlet.http.HttpSession session = req.getSession();
        final String oldId = session.getId();

        final String newId = req.changeSessionId();

        assertNotEquals(oldId, newId);
        final SessionManager sessionManager = SystemInstance.get().getComponent(SessionManager.class);
        assertNull(sessionManager.findSession(oldId));
        assertNotNull(sessionManager.findSession(newId));
        assertSame(session, sessionManager.findSession(newId).session);
    }

    @Test
    public void initContext() throws URISyntaxException {
        final HttpRequestImpl req = new HttpRequestImpl(new URI("http://localhost:1234/api/foo/bar"));
        req.setUri(req.getSocketURI());

        req.initPathFromContext("/");
        assertEquals("/api/foo/bar", req.getServletPath());

        req.initPathFromContext("/api"); // reinit, happens with cxf + embedded http
        assertEquals("/foo/bar", req.getServletPath());

        req.initPathFromContext("/api/bar"); // that's too late we tolerate a wrong context only if its value is "/"
        assertEquals("/foo/bar", req.getServletPath());
    }

    @Test
    public void oversizedContentLengthRejected() throws Exception {
        final HttpRequestImpl req = new HttpRequestImpl(new URI("http://localhost:1234/foo"));
        final String message = "POST /foo HTTP/1.1\r\nContent-Length: 2147483647\r\n\r\n";
        try {
            req.readMessage(new ByteArrayInputStream(message.getBytes(StandardCharsets.ISO_8859_1)));
            fail("Should have rejected the oversized Content-Length");
        } catch (final IOException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("maximum allowed request body size"));
        }
    }

    @Test
    public void oversizedChunkRejected() throws Exception {
        final HttpRequestImpl req = new HttpRequestImpl(new URI("http://localhost:1234/foo"));
        final String message = "POST /foo HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n7fffffff\r\n";
        try {
            req.readMessage(new ByteArrayInputStream(message.getBytes(StandardCharsets.ISO_8859_1)));
            fail("Should have rejected the oversized chunk");
        } catch (final IOException expected) {
            assertTrue(expected.getMessage(), expected.getCause() instanceof IOException
                && expected.getCause().getMessage().contains("maximum allowed request body size"));
        }
    }

    @Test
    public void smallBodyStillRead() throws Exception {
        final HttpRequestImpl req = new HttpRequestImpl(new URI("http://localhost:1234/foo"));
        final String message = "POST /foo HTTP/1.1\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: 7\r\n\r\na=1&b=2";
        assertTrue(req.readMessage(new ByteArrayInputStream(message.getBytes(StandardCharsets.ISO_8859_1))));
        assertEquals("1", req.getParameter("a"));
        assertEquals("2", req.getParameter("b"));
    }
}
