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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.openejb.server.ServiceException;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;

/**
 * Jetty based http server implementation
 */
public class JettyHttpServer implements HttpServer {
    private final HttpListener listener;
    private Server server;
    private int port;

    public JettyHttpServer() {
        this(null);
    }

    public JettyHttpServer(HttpListener listener) {
        this.listener = listener;
    }

    public HttpListener getListener() {
        return listener;
    }

    public void service(Socket socket) throws ServiceException, IOException {
        throw new UnsupportedOperationException();
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return "jetty";
    }

    public int getPort() {
        return port;
    }

    public String getIP() {
        return "0.0.0.0";
    }

    public void init(Properties props) throws Exception {
        port = Integer.parseInt(props.getProperty("port", "8080"));
        
        // Create all the Jetty objects but dont' start them
        server = new Server();
        Connector connector = new SelectChannelConnector();
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});

        ContextHandler context = new ContextHandler();
        context.setContextPath("/");
        final ServletContext servletContext = context.getServletContext();
        server.setHandler(context);

        Handler handler = new AbstractHandler() {
            public void handle(String target, HttpServletRequest req, HttpServletResponse res, int dispatch) throws IOException, ServletException {
                try {
                    ((Request) req).setHandled(true);
                    HttpRequest httpRequest = new ServletRequestAdapter(req, res, servletContext);
                    HttpResponse httpResponse = new ServletResponseAdapter(res);
                    JettyHttpServer.this.listener.onMessage(httpRequest, httpResponse);
                } catch (IOException e) {
                    throw e;
                } catch (ServletException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServletException(e);
                }
            }
        };

        context.setHandler(handler);
    }

    public void start() throws ServiceException {
        try {
            server.start();
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public void stop() throws ServiceException {
        try {
            server.stop();
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }
}