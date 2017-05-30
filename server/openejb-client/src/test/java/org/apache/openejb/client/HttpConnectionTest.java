/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.client;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.fail;

public class HttpConnectionTest {
    private HttpServer server;

    @Before
    public void init() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 5);
        server.createContext("/e", new HttpHandler() {
            @Override
            public void handle(final HttpExchange exchange) throws IOException {
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, 0);

                final OutputStream responseBody = exchange.getResponseBody();
                responseBody.write("secure page".getBytes());
                final String query = exchange.getRequestURI().getQuery();
                if (query != null) {
                    responseBody.write(query.getBytes());
                }
                final String authorization = exchange.getRequestHeaders().getFirst("Authorization");
                if (authorization != null) {
                    responseBody.write(authorization.getBytes("UTF-8"));
                }
                final String authorization2 = exchange.getRequestHeaders().getFirst("AltAuthorization");
                if (authorization2 != null) {
                    responseBody.write(("alt" + authorization2).getBytes("UTF-8"));
                }
                responseBody.close();
            }
        });
        server.start();
    }

    @After
    public void close() {
        server.stop(0);
    }

    @Test
    public void http() throws URISyntaxException, IOException {
        final HttpConnectionFactory factory = new HttpConnectionFactory();
        final String url = "http://localhost:" + server.getAddress().getPort() + "/e";
        for (int i = 0; i < 3; i++) {
            final Connection connection = factory.getConnection(new URI(url));

            BufferedReader br = null;
            final StringBuilder sb = new StringBuilder();
            String line;
            try {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                connection.close();
            }

            Assert.assertTrue("should contain", sb.toString().contains("secure"));
        }
    }

    @Test
    public void httpBasic() throws URISyntaxException, IOException {
        final HttpConnectionFactory factory = new HttpConnectionFactory();
        final String url = "http://localhost:" + server.getAddress().getPort() + "/e?authorization=Basic%20token";
        for (int i = 0; i < 3; i++) {
            final Connection connection = factory.getConnection(new URI(url));

            BufferedReader br = null;
            final StringBuilder sb = new StringBuilder();
            String line;
            try {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                connection.close();
            }

            Assert.assertTrue("should contain", sb.toString().contains("secure pageBasic token"));
        }
    }

    @Test
    public void httpBasicSpecificConfig() throws URISyntaxException, IOException {
        final HttpConnectionFactory factory = new HttpConnectionFactory();
        final String url = "http://localhost:" + server.getAddress().getPort() + "/e?basic.password=pwd&basic.username=test&authorizationHeader=AltAuthorization";
        for (int i = 0; i < 3; i++) {
            final Connection connection = factory.getConnection(new URI(url));

            BufferedReader br = null;
            final StringBuilder sb = new StringBuilder();
            String line;
            try {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
                connection.close();
            }

            Assert.assertTrue("should contain", sb.toString().contains("secure pagealtBasic dGVzdDpwd2Q="));
        }
    }

    @Test
    public void complexURIAuthorization() throws IOException, URISyntaxException {
        final String baseHttp = "http://localhost:" + server.getAddress().getPort() + "/e?authorization=";
        final String uri = "failover:sticky+random:" + baseHttp + "Basic%20ABCD&" + baseHttp + "Basic%20EFG";
        final Connection connection = ConnectionManager.getConnection(new URI(uri));
        BufferedReader br = null;
        final StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (final IOException e) {
            fail(e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            connection.close();
        }
        final String out = sb.toString();
        Assert.assertTrue(out, out.contains("secure pagehttp://localhost:" + server.getAddress().getPort() + "/eBasic ABCD"));
    }
}
