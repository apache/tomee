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
import java.net.URI;
import java.util.Properties;

import junit.framework.TestCase;


public class ConnectionManagerTest extends TestCase{

    public void testRegisterFactory() throws Exception {
        MockConnectionFactory connectionFactory = new MockConnectionFactory();
        ConnectionManager.registerFactory("mock", connectionFactory);

        Connection connection = ConnectionManager.getConnection(new URI("mock://foo"));

        assertTrue(connection instanceof MockConnection);
    }
    
    public static class MockConnectionFactory implements ConnectionFactory {

        public Connection getConnection(URI uri) throws IOException {
            return new MockConnection();
        }

    }

    private static class MockConnection implements Connection {
        public void close() throws IOException {
            throw new UnsupportedOperationException();
        }

        public InputStream getInputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        public OutputStream getOuputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        public void discard() {
            throw new UnsupportedOperationException();
        }

        public URI getURI() {
            throw new UnsupportedOperationException();
        }
    }
}
