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

import org.apache.openejb.junit.ApplicationComposerRule;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Rule;
import org.junit.Test;

import jakarta.inject.Inject;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@EnableServices("http")
@Classes(cdi = true, innerClassesAsBean = true)
@ContainerProperties(@ContainerProperties.Property(name = "httpejbd.useJetty", value = "fase"))
public class AsyncHttpTest {
    @Rule
    public final ApplicationComposerRule container = new ApplicationComposerRule(this);

    @RandomPort("http")
    private URL context;

    @Test
    public void async() throws IOException {
        SimpleAsyncListener.started = false;
        assertEquals("OK", IO.slurp(new URL(context.toExternalForm() + "openejb/AsyncServlet")));
        assertTrue(SimpleAsyncListener.started);
    }

    @Test
    public void asyncDispatch() throws IOException {
        assertEquals("OK2", IO.slurp(new URL(context.toExternalForm() + "openejb/DispatchAsyncServlet")));
    }

    @WebServlet(name = "AsyncServlet", urlPatterns = "/AsyncServlet", asyncSupported = true)
    public static class AsyncServlet extends HttpServlet {
        private ExecutorService executorService;

        @Override
        public void init() throws ServletException {
            executorService = Executors.newSingleThreadExecutor();
        }

        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            final AsyncContext actx = req.startAsync();
            actx.addListener(actx.createListener(SimpleAsyncListener.class));
            resp.setContentType("text/plain");
            executorService.execute(new AsyncHandler(actx));
        }

        @Override
        public void destroy() {
            executorService.shutdownNow();
        }
    }

    @WebServlet(name = "DispatchAsyncServlet", urlPatterns = "/DispatchAsyncServlet", asyncSupported = true)
    public static class DispatchAsyncServlet extends HttpServlet {
        private ExecutorService executorService;

        @Override
        public void init() throws ServletException {
            executorService = Executors.newSingleThreadExecutor();
        }

        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            final AsyncContext actx = req.startAsync();
            resp.setContentType("text/plain");
            actx.dispatch("/ok");
        }

        @Override
        public void destroy() {
            executorService.shutdownNow();
        }
    }

    public static class ABean {}

    public static class SimpleAsyncListener implements AsyncListener {
        private static boolean started;

        @Inject
        private ABean bean;

        @Override
        public void onComplete(final AsyncEvent event) throws IOException {
            // no-op
        }

        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
            // no-op
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {
            // no-op
        }

        @Override
        public void onStartAsync(final AsyncEvent event) throws IOException {
            started = bean != null;
        }
    }

    @WebServlet("/ok")
    public static class SimpleServlet extends HttpServlet {
        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().write("OK2");
        }
    }

    private static class AsyncHandler implements Runnable {
        private final AsyncContext actx;

        public AsyncHandler(final AsyncContext ctx) {
            this.actx = ctx;
        }

        @Override
        public void run() {
            try {
                actx.getResponse().getWriter().write("OK");
            } catch (final IOException e) {
                try {
                    HttpServletResponse.class.cast(actx.getResponse()).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } catch (final IOException e1) {
                    // no-op
                }
                actx.complete();
            }
            actx.complete();
        }
    }
}
