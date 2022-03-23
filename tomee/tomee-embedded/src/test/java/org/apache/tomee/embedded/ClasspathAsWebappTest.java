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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.embedded;

import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.JarLocation;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Rule;
import org.junit.Test;

import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.Principal;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static jakarta.xml.bind.DatatypeConverter.printBase64Binary;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class ClasspathAsWebappTest {
    @Inject
    private MyBean bean;

    @EJB
    private Task2 anEjb;

    @Rule
    public final ThreadStackRule debugWatcher = new ThreadStackRule();

    @Test
    public void run() throws MalformedURLException {
        MyInitializer.found = null;
        MyBean.VALUE = null;
        try (final Container container = new Container(
                    new Configuration()
                            .http(NetworkUtil.getNextAvailablePort())
                            .property("openejb.container.additional.exclude", "org.apache.tomee.embedded.")
                            .property("openejb.additional.include", "tomee-")
                            .user("tomee", "tomeepwd")
                            .loginConfig(new LoginConfigBuilder().basic())
                            .securityConstaint(new SecurityConstaintBuilder().addAuthRole("**").authConstraint(true).addCollection("api", "/api/resource2/")))
                .deployPathsAsWebapp(JarLocation.jarLocation(MyInitializer.class))
                .inject(this)) {

            // Servlet (initializer, servlet)
            assertNotNull(MyInitializer.found);
            final Iterator<Class<?>> it = MyInitializer.found.iterator();
            while (it.hasNext()) { // ThreadStackRule defines one for instance
                final Class<?> next = it.next();
                if (next.getEnclosingClass() != null && !Modifier.isStatic(next.getModifiers())) {
                    it.remove();
                }
            }
            assertEquals(1, MyInitializer.found.size());
            assertEquals(Task1.class, MyInitializer.found.iterator().next());
            try {
                assertEquals("Servlet!", IO.slurp(new URL("http://localhost:" + container.getConfiguration().getHttpPort() + "/s")));
            } catch (final IOException e) {
                fail(e.getMessage());
            }
            try {
                assertEquals("WebServlet", IO.slurp(new URL("http://localhost:" + container.getConfiguration().getHttpPort() + "/w")));
            } catch (final IOException e) {
                fail(e.getMessage());
            }

            // JSP
            try {
                assertEquals("JSP", IO.slurp(new URL("http://localhost:" + container.getConfiguration().getHttpPort() + "/?test=JSP")).trim());
            } catch (final IOException e) {
                fail(e.getMessage());
            }

            // CDI
            assertNotNull(bean);
            assertNull(bean.value());
            MyBean.VALUE = "cdi";
            assertEquals("cdi", bean.value());

            // EJB
            MyBean.VALUE = "ejb";
            assertEquals("ejb", anEjb.run());

            // JAXRS
            try {
                assertEquals("jaxrs", IO.slurp(new URL("http://localhost:" + container.getConfiguration().getHttpPort() + "/api/resource")));
            } catch (final IOException e) {
                fail(e.getMessage());
            }

            // JAXRS + servlet security
            try {
                final URL url = new URL("http://localhost:" + container.getConfiguration().getHttpPort() + "/api/resource2/");
                final HttpURLConnection c = HttpURLConnection.class.cast(url.openConnection());
                c.setRequestProperty("Authorization", "Basic " + printBase64Binary("tomee:tomeepwd".getBytes()));
                assertEquals("tomee", IO.slurp(c.getInputStream()));
                c.disconnect();
            } catch (final IOException e) {
                fail(e.getMessage());
            }
            try {
                assertEquals("tomee", IO.slurp(new URL("http://tomee:tomeepwd@localhost:" + container.getConfiguration().getHttpPort() + "/api/resource2/")));
                fail("should have been not authorized");
            } catch (final IOException e) {
                // ok
            }

            // WebSocket
            final WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
            try {
                WebSocketClient.message = null;
                final WebSocketClient webSocketClient = new WebSocketClient();
                final Session session = webSocketContainer.connectToServer(webSocketClient, new URI("ws://localhost:" + container.getConfiguration().getHttpPort() + "/ws"));
                webSocketClient.latch.await(20, TimeUnit.SECONDS);
                session.close();
                assertEquals("websocket", WebSocketClient.message);
            } catch (final Exception e) {
                fail(e.getMessage());
            }
        }
    }

    @ClientEndpoint
    public static class WebSocketClient {
        private static String message;
        private final CountDownLatch latch = new CountDownLatch(1);

        @OnMessage
        public void onMessage(final String msg) {
            message = msg;
            latch.countDown();
        }
    }

    @ServerEndpoint("/ws")
    public static class WebSocketEndpoint {
        @OnOpen
        public void open(final Session session) {
            try {
                session.getBasicRemote().sendText("websocket");
            } catch (IOException e) {
                try {
                    session.close(new CloseReason(CloseReason.CloseCodes.PROTOCOL_ERROR, "Should be able to send a message"));
                } catch (final IOException e1) {
                    throw new IllegalStateException(e1);
                }
            }
        }
    }

    @ApplicationPath("api")
    public static class App extends Application {
    }

    @Path("resource")
    public static class Resource {
        @GET
        public String t() {
            return "jaxrs";
        }
    }

    @Path("resource2")
    public static class Secured {
        @Context
        private SecurityContext sc;

        @GET
        public String t() {
            final Principal principal = sc.getUserPrincipal();
            return principal.getName();
        }
    }

    @WebServlet(urlPatterns = "/w")
    public static class MyWebServlet extends HttpServlet {
        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().write("WebServlet");
        }
    }

    @HandlesTypes(Runnable.class)
    public static class MyInitializer implements ServletContainerInitializer {
        private static Set<Class<?>> found;

        @Override
        public void onStartup(final Set<Class<?>> c, final ServletContext ctx) throws ServletException {
            found = c;
            ctx.addServlet("s", new HttpServlet() {
                @Override
                protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
                    resp.getWriter().write("Servlet!");
                }
            }).addMapping("/s");
        }
    }

    public static class Task1 implements Runnable {
        @Override
        public void run() {
            // no-op
        }
    }

    @Singleton
    public static class Task2 {
        public String run() {
            return MyBean.VALUE;
        }
    }

    public static class MyBean {
        private static volatile String VALUE;

        public String value() {
            return VALUE;
        }
    }
}
