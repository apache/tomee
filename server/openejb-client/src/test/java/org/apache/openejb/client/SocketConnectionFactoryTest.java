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

import org.junit.Test;

import javax.net.ssl.SSLSocket;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;

import static org.junit.Assert.assertEquals;

public class SocketConnectionFactoryTest {

    @Test
    public void ejbdsSocketVerifiesThePeerHostname() throws Exception {
        // a plain server socket is enough: the TLS handshake only starts on first read/write
        final ServerSocket server = new ServerSocket(0, 1, InetAddress.getByName("localhost"));
        System.setProperty(SocketConnectionFactory.ENABLED_CIPHER_SUITES, "TLS_AES_128_GCM_SHA256");
        try {
            final SocketConnectionFactory factory = new SocketConnectionFactory();
            final URI uri = new URI("ejbds://localhost:" + server.getLocalPort());
            final SocketConnectionFactory.SocketConnection connection = factory.new SocketConnection(uri, null);
            connection.open(uri);

            final Field socketField = SocketConnectionFactory.SocketConnection.class.getDeclaredField("socket");
            socketField.setAccessible(true);
            final Socket socket = (Socket) socketField.get(connection);
            try {
                assertEquals("HTTPS", ((SSLSocket) socket).getSSLParameters().getEndpointIdentificationAlgorithm());
            } finally {
                socket.close();
            }
        } finally {
            System.clearProperty(SocketConnectionFactory.ENABLED_CIPHER_SUITES);
            server.close();
        }
    }
}
