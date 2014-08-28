/**
 *
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
package org.apache.openejb.client;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class HttpConnectionFactory implements ConnectionFactory {

    @Override
    public Connection getConnection(final URI uri) throws IOException {
        return new HttpConnection(uri);
    }

    public static class HttpConnection implements Connection {

        private HttpURLConnection httpURLConnection;
        private InputStream inputStream;
        private OutputStream outputStream;
        private final URI uri;

        public HttpConnection(final URI uri) throws IOException {
            this.uri = uri;
            final URL url = uri.toURL();

            final Map<String, String> params;
            try {
                params = MulticastConnectionFactory.URIs.parseParamters(uri);
            } catch (final URISyntaxException e) {
                throw new IllegalArgumentException("Invalid uri " + uri.toString(), e);
            }

            httpURLConnection = (HttpURLConnection) url.openConnection();
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

            if (params.containsKey("sslKeyStore") || params.containsKey("sslTrustStore")) {
                try {
                    ((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(new SSLContextBuilder(params).build().getSocketFactory());
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
                inputStream = httpURLConnection.getInputStream();
            }
            return inputStream;
        }
    }

}
