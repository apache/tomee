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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.util.Properties;

public class SocketConnectionFactory implements ConnectionFactory {

    public void init(Properties props) {
    }

    public Connection getConnection(ServerMetaData server) throws java.io.IOException {
        SocketConnection conn = new SocketConnection();
        conn.open(server);
        return conn;
    }

    class SocketConnection implements Connection {

        Socket socket = null;
        OutputStream socketOut = null;
        InputStream socketIn = null;

        protected void open(ServerMetaData server) throws IOException {
            /*-----------------------*/
            /* Open socket to server */
            /*-----------------------*/
            try {
                socket = new Socket(server.getHost(), server.getPort());
                socket.setTcpNoDelay(true);
            } catch (IOException e) {
                throw new IOException("Cannot access server: " + server.getHost() + ":" + server.getPort() + " Exception: " + e.getClass().getName() + " : " + e.getMessage());

            } catch (SecurityException e) {
                throw new IOException("Cannot access server: " + server.getHost() + ":" + server.getPort() + " due to security restrictions in the current VM: " + e.getClass().getName() + " : " + e.getMessage());

            } catch (Throwable e) {
                throw new IOException("Cannot access server: " + server.getHost() + ":" + server.getPort() + " due to an unkown exception in the OpenEJB client: " + e.getClass().getName() + " : " + e.getMessage());
            }

        }

        public void close() throws IOException {
            try {
                if (socketOut != null) socketOut.close();
                if (socketIn != null) socketIn.close();
                if (socket != null) socket.close();
            } catch (Throwable t) {
                throw new IOException("Error closing connection with server: " + t.getMessage());
            }
        }

        public InputStream getInputStream() throws IOException {
            /*----------------------------------*/
            /* Open input streams               */
            /*----------------------------------*/
            try {
                socketIn = socket.getInputStream();
            } catch (StreamCorruptedException e) {
                throw new IOException("Cannot open input stream to server, the stream has been corrupted: " + e.getClass().getName() + " : " + e.getMessage());

            } catch (IOException e) {
                throw new IOException("Cannot open input stream to server: " + e.getClass().getName() + " : " + e.getMessage());

            } catch (Throwable e) {
                throw new IOException("Cannot open output stream to server: " + e.getClass().getName() + " : " + e.getMessage());
            }
            return socketIn;
        }

        public OutputStream getOuputStream() throws IOException {
            /*----------------------------------*/
            /* Openning output streams          */
            /*----------------------------------*/
            try {
                socketOut = socket.getOutputStream();
            } catch (IOException e) {
                throw new IOException("Cannot open output stream to server: " + e.getClass().getName() + " : " + e.getMessage());

            } catch (Throwable e) {
                throw new IOException("Cannot open output stream to server: " + e.getClass().getName() + " : " + e.getMessage());
            }
            return socketOut;
        }

    }
}

