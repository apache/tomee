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
package org.apache.openejb.server.hessian;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.valves.ValveBase;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LimitedBasicValveTest {
    @Test
    public void rootContext() throws Exception {
        assertTrue(authenticates("", "/hessian/MyBean"));
        assertFalse(authenticates("", "/other"));
    }

    @Test
    public void nonRootContext() throws Exception {
        assertTrue(authenticates("/myapp", "/myapp/hessian/MyBean"));
        assertFalse(authenticates("/myapp", "/myapp/other"));
    }

    private static boolean authenticates(final String contextPath, final String uri) throws IOException, ServletException {
        final AtomicBoolean authenticated = new AtomicBoolean();
        final AtomicBoolean invokedNext = new AtomicBoolean();

        final TomcatHessianRegistry.LimitedBasicValve valve = new TomcatHessianRegistry.LimitedBasicValve() {
            @Override
            public boolean authenticate(final Request request, final HttpServletResponse response) {
                authenticated.set(true);
                return true;
            }
        };
        valve.setNext(new ValveBase() {
            @Override
            public void invoke(final Request request, final Response response) {
                invokedNext.set(true);
            }
        });

        final StandardEngine engine = new StandardEngine();
        engine.setService(new StandardService());
        final StandardHost host = new StandardHost();
        host.setParent(engine);
        final StandardContext context = new StandardContext();
        context.setParent(host);
        context.setPath(contextPath);

        final Connector connector = new Connector();
        final Request request = new Request(connector, new org.apache.coyote.Request());
        request.getCoyoteRequest().requestURI().setString(uri);
        request.getCoyoteRequest().decodedURI().setString(uri);
        request.getMappingData().context = context;
        request.getMappingData().contextSlashCount = contextPath.isEmpty() ? 0 : 1;

        valve.invoke(request, new Response(new org.apache.coyote.Response()));

        assertTrue(invokedNext.get());
        return authenticated.get();
    }
}
