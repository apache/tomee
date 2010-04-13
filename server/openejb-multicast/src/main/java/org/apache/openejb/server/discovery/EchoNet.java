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

import java.util.concurrent.CountDownLatch;

/**
 * @version $Rev$ $Date$
 */
public class EchoNet {

    public static void main(String[] args) throws Exception {

        final int multiple = 1111;
        final int base = 3;

        int servers = 3;

        if (args.length > 0)
            servers = Integer.parseInt(args[0]);

        if (servers < 1) {
            System.out.println("number of servers must be greater than zero");
            return;
        }

        // get out of the 1000 port range
        servers += base;
        
        MultipointServer lastServer = null;
        for (int i = base; i < servers; i++) {
            MultipointServer newServer = new MultipointServer(multiple * i, new Tracker.Builder().build()).start();

            if (lastServer != null)
                newServer.connect(lastServer);

            lastServer = newServer;
        }

        new CountDownLatch(1).await();
    }

}

