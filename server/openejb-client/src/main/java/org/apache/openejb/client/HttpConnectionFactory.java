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

import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @version $Revision$ $Date$
 */
public class HttpConnectionFactory implements ConnectionFactory {

    public void init(Properties props) {
    }

    public Connection getConnection(ServerMetaData server) throws IOException {
        return new HttpConnection(server);
    }

    public static class HttpConnection implements Connection {

        private final ServerMetaData server;
        private HttpURLConnection httpURLConnection;

        public HttpConnection(ServerMetaData server) throws IOException {
            this.server = server;
            String host = "localhost";
//            String host = server.getLocation().getHost();
            // TODO: Use the URI for making the URL
            URL url = server.getLocation().toURL();
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.connect();
        }

        public void close() throws IOException {
            httpURLConnection.disconnect();
        }

        public InputStream getInputStream() throws IOException {
            return httpURLConnection.getInputStream();
        }

        public OutputStream getOuputStream() throws IOException {
            return httpURLConnection.getOutputStream();
        }
    }
}
