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

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
}
