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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BasicAuthHttpListenerWrapperTest {
    @Test
    public void getWithoutCredentialsIsChallenged() throws Exception {
        final AtomicBoolean dispatched = new AtomicBoolean(false);
        final BasicAuthHttpListenerWrapper wrapper = new BasicAuthHttpListenerWrapper(listener(dispatched), "TestRealm");
        final HttpResponseImpl response = new HttpResponseImpl();

        wrapper.onMessage(get(), response);

        assertFalse(dispatched.get());
        assertEquals(401, response.getStatus());
        assertEquals("Basic realm=\"TestRealm\"", response.getHeader("WWW-Authenticate"));
    }

    @Test
    public void anonymousGetIsDispatchedWhenEnabled() throws Exception {
        final AtomicBoolean dispatched = new AtomicBoolean(false);
        final BasicAuthHttpListenerWrapper wrapper = new BasicAuthHttpListenerWrapper(listener(dispatched), "TestRealm", true);
        final HttpResponseImpl response = new HttpResponseImpl();

        wrapper.onMessage(get(), response);

        assertTrue(dispatched.get());
        assertEquals(200, response.getStatus());
    }

    private static HttpListener listener(final AtomicBoolean dispatched) {
        return new HttpListener() {
            @Override
            public void onMessage(final HttpRequest request, final HttpResponse response) {
                dispatched.set(true);
            }
        };
    }

    private static HttpRequestImpl get() throws Exception {
        final HttpRequestImpl request = new HttpRequestImpl(new URI("http://localhost:4204"));
        assertTrue(request.readMessage(new ByteArrayInputStream(
                "GET /app/api/customers HTTP/1.1\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1))));
        return request;
    }
}
