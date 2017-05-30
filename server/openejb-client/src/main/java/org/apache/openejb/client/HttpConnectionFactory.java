/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.client;

import javax.naming.AuthenticationException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

/**
 * @version $Revision$ $Date$
 */
public class HttpConnectionFactory implements ConnectionFactory {
    // this map only ensures JVM keep alive socket caching works properly
    private final ConcurrentMap<URI, SSLSocketFactory> socketFactoryMap = new ConcurrentHashMap<>();
    private final Queue<byte[]> drainBuffers = new ConcurrentLinkedQueue<>();

    @Override
    public Connection getConnection(final URI uri) throws IOException {
        byte[] buffer = drainBuffers.poll();
        if (buffer == null) {
            buffer = new byte[Integer.getInteger("openejb.client.http.drain-buffer.size", 64)];
        }
        try {
            return new HttpConnection(uri, socketFactoryMap, buffer);
        } finally { // auto adjusting buffer caching, queue avoids leaks (!= ThreadLocal)
            drainBuffers.add(buffer);
        }
    }

    public static class HttpConnection implements Connection {
        private final byte[] buffer;
        private HttpURLConnection httpURLConnection;
        private InputStream inputStream;
        private OutputStream outputStream;
        private final URI uri;

        public HttpConnection(final URI uri, final ConcurrentMap<URI, SSLSocketFactory> socketFactoryMap,
                              final byte[] buffer) throws IOException {
            this.uri = uri;
            this.buffer = buffer;
            final URL url = uri.toURL();

            final Map<String, String> params;
            try {
                params = MulticastConnectionFactory.URIs.parseParamters(uri);
            } catch (final URISyntaxException e) {
                throw new IllegalArgumentException("Invalid uri " + uri.toString(), e);
            }

            final String basicUsername = params.get("basic.username");
            final String basicPassword = params.get("basic.password");
            final String authorizationHeader = params.get("authorizationHeader");
            String authorization = params.get("authorization");
            if (authorization != null && basicUsername != null) {
                throw new IllegalArgumentException("You can't set basic.* properties AND authorization on the provider url");
            }
            if (authorization == null && basicUsername != null) {
                authorization = "Basic " + printBase64Binary((basicUsername + (basicPassword != null ? ":" + basicPassword : "")).getBytes(StandardCharsets.UTF_8));
            }

            final String newUrl =
                    stripQuery(
                        stripQuery(
                            stripQuery(
                                stripQuery(url.toExternalForm(), "authorization"),
                        "basic.username"),
                            "basic.password"),
                "authorizationHeader");
            httpURLConnection = (HttpURLConnection) (authorization == null ? url : new URL(newUrl)).openConnection();
            httpURLConnection.setDoOutput(true);

            final int timeout;
            if (params.containsKey("connectTimeout")) {
                timeout = Integer.parseInt(params.get("connectTimeout"));
            } else {
                timeout = 10000;
            }

            httpURLConnection.setConnectTimeout(timeout);

            if (params.containsKey("readTimeout")) {
                httpURLConnection.setReadTimeout(Integer.parseInt(params.get("readTimeout")));
            }
            if (authorization != null) {
                httpURLConnection.setRequestProperty(authorizationHeader == null ? "Authorization" : authorizationHeader, authorization);
            }

            if (params.containsKey("sslKeyStore") || params.containsKey("sslTrustStore")) {
                try {
                    SSLSocketFactory sslSocketFactory = socketFactoryMap.get(uri);
                    if (sslSocketFactory == null) {
                        sslSocketFactory = new SSLContextBuilder(params).build().getSocketFactory();
                        final SSLSocketFactory existing = socketFactoryMap.putIfAbsent(uri, sslSocketFactory);
                        if (existing != null) {
                            sslSocketFactory = existing;
                        }
                    }

                    ((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(sslSocketFactory);
                } catch (final NoSuchAlgorithmException | KeyManagementException e) {
                    throw new ClientRuntimeException(e.getMessage(), e);
                }
            }

            try {
                httpURLConnection.connect();
            } catch (final IOException e) {
                httpURLConnection.connect();
            }
        }

        private String stripQuery(final String url, final String param) {
            String result = url;
            do {
                final int h = result.indexOf(param + '=');
                int end = result.indexOf('&', h);
                if (end < 0) {
                    end = result.length();
                }
                if (h <= 0) {
                    return result.endsWith("?") ? result.substring(0, result.length() - 1) : result;
                }
                result = result.substring(0, h) +
                        (end < 0 || end == result.length() ? "" : result.substring(end + 1, result.length()));
            } while (true);
        }

        @Override
        public void discard() {
            try {
                close();
            } catch (final Exception e) {
                //Ignore
            }
        }

        @Override
        public URI getURI() {
            return uri;
        }

        @Override
        public void close() throws IOException {
            IOException exception = null;
            if (inputStream != null) {
                // consume anything left in the buffer
                try {// use a buffer cause it is faster, check HttpInputStreamImpl
                    while (inputStream.read(buffer) > -1) {
                        // no-op
                    }
                } catch (final Throwable e) {
                    // ignore
                }
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    exception = e;
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (final IOException e) {
                    if (exception == null) {
                        exception = e;
                    }
                }
            }

            inputStream = null;
            outputStream = null;
            httpURLConnection = null;

            if (exception != null) {
                throw exception;
            }
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            if (outputStream == null) {
                outputStream = httpURLConnection.getOutputStream();
            }
            return outputStream;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (inputStream == null) {
                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new IOException(new AuthenticationException());
                }
                inputStream = httpURLConnection.getInputStream();
            }
            return inputStream;
        }
    }

}
