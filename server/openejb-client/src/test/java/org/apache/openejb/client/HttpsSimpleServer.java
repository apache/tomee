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
package org.apache.openejb.client;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class HttpsSimpleServer implements AutoCloseable {

    private final HttpsServer server;

    public HttpsSimpleServer(int serverPort, final String storePath, final String storePassword) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        final Map<String, String> params = new HashMap<String, String>() {
            {
                put("sslKeyStore", storePath);
                put("sslKeyStorePassword", storePassword);
                put("sslTrustStore", storePath);
                put("sslTrustStorePassword", storePassword);
            }
        };

        server = HttpsServer.create(new InetSocketAddress(serverPort), 5);
        SSLContext sslContext = new SSLContextBuilder(params).build();

        final SSLEngine m_engine = sslContext.createSSLEngine();
        server.setHttpsConfigurator(new HttpsConfigurator(new SSLContextBuilder(params).build()) {
            public void configure(HttpsParameters params) {

                params.setCipherSuites(m_engine.getEnabledCipherSuites());
                params.setProtocols(m_engine.getEnabledProtocols());
            }
        });

        server.createContext("/secure", new MyHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    @Override
    public void close() {
        server.stop(0);
    }


    class MyHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();
            responseBody.write("secure page".getBytes());
            responseBody.close();
        }
    }
}
