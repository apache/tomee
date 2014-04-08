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
import java.util.HashSet;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class EchoNet {

    public static void _main(String[] args) throws Exception {
        MultipointServer a = new MultipointServer(1111, new Tracker.Builder().build()).start();
        MultipointServer b = new MultipointServer(3333, new Tracker.Builder().build()).start();
        a.connect(b);
        b.connect(a);
        a.connect(b);
        b.connect(a);
        a.connect(b);
        b.connect(a);
        a.connect(b);
        b.connect(a);
    }

    public static void main(String[] args) throws Exception {

        final int multiple = 1;
        final int base = 2000;
//        final int multiple = 1111;
//        final int base = 1;

        int servers = 50;

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


    public static class Calc {
        public static void main(String[] args) {
            Set<Item> set = new HashSet<Item>();

            int x = 150;

            for (int i = 1; i <= x; i++) {
                for (int j = 1; j <= x; j++) {
                    if (i==j) continue;

                    Item item = new Item(i, j);
                    boolean b = set.add(item);
//                    if (b) System.out.println("item = " + item);
                }
            }

            // 100 4950
            System.out.println(x + " ? " + 2 + " = " + set.size());
        }


        static class Item {
            int a;
            int b;

            Item(int a, int b) {
                this.a = a;
                this.b = b;
            }

            @Override
            public boolean equals(Object o) {
                if (o == null || getClass() != o.getClass()) return false;

                Item set = (Item) o;

                if (a == set.a && b == set.b) return true;
                if (a == set.b && b == set.a) return true;

                return false;
            }

            @Override
            public int hashCode() {
                return 1;
            }

            @Override
            public String toString() {
                return a + " " + b;
            }
        }
    }
}

