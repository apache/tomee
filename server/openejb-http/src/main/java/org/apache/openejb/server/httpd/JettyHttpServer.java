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

import org.apache.openejb.loader.Options;
import org.apache.openejb.server.ServiceException;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.SessionManager;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.HashSessionIdManager;
import org.mortbay.jetty.servlet.HashSessionManager;
import org.mortbay.jetty.servlet.SessionHandler;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

/**
 * Jetty based http server implementation
 */
public class JettyHttpServer implements HttpServer {

    private final HttpListener listener;
    private Server server;
    private int port;

    public JettyHttpServer() {
        this(OpenEJBHttpServer.getHttpListenerRegistry());
    }

    public JettyHttpServer(final HttpListener listener) {
        this.listener = listener;
    }

    @Override
    public HttpListener getListener() {
        return listener;
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return "jetty";
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getIP() {
        return "0.0.0.0";
    }

    @Override
    public void init(final Properties props) throws Exception {
        final Options options = new Options(props);

        port = options.get("port", 8080);

        // Create all the Jetty objects but dont' start them
        server = new Server();
        final Connector connector = new SelectChannelConnector();
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});

        final ContextHandler context = new ContextHandler();
        context.setContextPath("/");
        final ServletContext servletContext = context.getServletContext();
        server.setHandler(context);

        final Handler handler = new AbstractHandler() {
            @Override
            public void handle(final String target, final HttpServletRequest req, final HttpServletResponse res, final int dispatch) throws IOException, ServletException {
                try {
                    ((Request) req).setHandled(true);
                    final HttpRequest httpRequest = new ServletRequestAdapter(req, res, servletContext);
                    final HttpResponse httpResponse = new ServletResponseAdapter(res);
                    JettyHttpServer.this.listener.onMessage(httpRequest, httpResponse);
                } catch (IOException | ServletException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServletException(e);
                }
            }
        };

        final SessionHandler sessionHandler = new SessionHandler();
        final SessionManager sessionManager = new HashSessionManager();
        sessionManager.setIdManager(new HashSessionIdManager());
        sessionHandler.setSessionManager(sessionManager);
        sessionHandler.setHandler(handler);

        context.setHandler(sessionHandler);
    }

    @Override
    public void start() throws ServiceException {
        try {
            server.start();
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public void stop() throws ServiceException {
        try {
            server.stop();
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }
}