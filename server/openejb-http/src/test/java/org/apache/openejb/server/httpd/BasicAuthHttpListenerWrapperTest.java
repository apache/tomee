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

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BasicAuthHttpListenerWrapperTest {
    @Test
    public void getWithoutCredentialsIsChallenged() throws Exception {
        final AtomicBoolean dispatched = new AtomicBoolean(false);
        final BasicAuthHttpListenerWrapper wrapper = new BasicAuthHttpListenerWrapper(listener(dispatched), "TestRealm");
        final Map<String, Object> written = new HashMap<>();

        wrapper.onMessage(get(), response(written));

        assertFalse(dispatched.get());
        assertEquals(401, written.get("status"));
        assertEquals("Basic realm=\"TestRealm\"", written.get("WWW-Authenticate"));
    }

    @Test
    public void anonymousGetIsDispatchedWhenEnabled() throws Exception {
        final AtomicBoolean dispatched = new AtomicBoolean(false);
        final BasicAuthHttpListenerWrapper wrapper = new BasicAuthHttpListenerWrapper(listener(dispatched), "TestRealm", true);
        final Map<String, Object> written = new HashMap<>();

        wrapper.onMessage(get(), response(written));

        assertTrue(dispatched.get());
        assertTrue(written.isEmpty());
    }

    private static HttpListener listener(final AtomicBoolean dispatched) {
        return new HttpListener() {
            @Override
            public void onMessage(final HttpRequest request, final HttpResponse response) {
                dispatched.set(true);
            }
        };
    }

    private static HttpRequest get() {
        return (HttpRequest) Proxy.newProxyInstance(HttpRequest.class.getClassLoader(), new Class<?>[]{HttpRequest.class},
                (proxy, method, args) -> "getMethod".equals(method.getName()) ? "GET" : null);
    }

    // records setStatus as "status" and setHeader as name -> value
    private static HttpResponse response(final Map<String, Object> written) {
        return (HttpResponse) Proxy.newProxyInstance(HttpResponse.class.getClassLoader(), new Class<?>[]{HttpResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName())) {
                        written.put("status", args[0]);
                    } else if ("setHeader".equals(method.getName())) {
                        written.put((String) args[0], args[1]);
                    }
                    return null;
                });
    }
}
