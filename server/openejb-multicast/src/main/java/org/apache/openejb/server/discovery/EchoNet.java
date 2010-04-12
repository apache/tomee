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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.discovery;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @version $Rev$ $Date$
 */
public class EchoNet {

    public static void main(String[] args) throws Exception {

        final int INITIAL_PORT = 3000;
        int maxServers = 3;

        if (args.length > 0)
            maxServers = Integer.parseInt(args[0]);

        if (maxServers < 1) {
            System.out.println("number of servers must be greater than zero");
            return;
        }

        MultipointServer lastServer = new MultipointServer(INITIAL_PORT, new Tracker.Builder().build()).start();
        for (int i=1; i<maxServers; i++) {
            MultipointServer newServer = new MultipointServer(INITIAL_PORT+i, new Tracker.Builder().build()).start();
            
            if (lastServer != null) 
                newServer.connect(lastServer);

            lastServer = newServer;
        }

        new CountDownLatch(1).await();
    }

}

