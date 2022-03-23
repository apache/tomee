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
package org.apache.openejb.server.httpd;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.session.SessionManager;
import org.apache.openejb.util.reflection.Reflections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import static org.junit.Assert.assertEquals;

public class HttpSessionImplTest {
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
        Reflections.set(session, "listeners", Collections.<Object>singletonList(new HttpSessionListener() {
            private int count = 0;

            @Override
            public void sessionCreated(final HttpSessionEvent se) {
                // no-op
            }

            @Override
            public void sessionDestroyed(final HttpSessionEvent se) {
                se.getSession().setAttribute("seen", ++count);
            }
        }));
        session.invalidate();
        final long c1 = Integer.class.cast(session.getAttribute("seen"));
        session.invalidate();
        final long c2 = Integer.class.cast(session.getAttribute("seen"));
        assertEquals(c1, c2);
    }
}
