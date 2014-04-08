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
package org.apache.openejb.client;

import java.io.IOException;
import java.net.URI;
import java.util.Stack;

public class HeartbeatMonitor {

    public static void main(final String[] args) throws Exception {
        final Stack<String> stack = new Stack<String>();
        for (final String s : args) {
            stack.push(s);
        }

        main(stack);
    }

    private static void main(final Stack<String> args) throws IOException {
        String host = "239.255.2.3";
        int port = 6142;

        for (final String arg : args) {
            if (arg.equals("--host") || arg.equals("-h")) {
                host = args.pop();
            } else if (arg.equals("--port") || arg.equals("-p")) {
                port = Integer.parseInt(args.pop());
            } else {
                throw new IllegalArgumentException(arg);
            }
        }

        final MulticastSearch search = new MulticastSearch(host, port);
        try {
            search.search(new MulticastSearch.Filter() {
                @Override
                @SuppressWarnings("UseOfSystemOutOrSystemErr")
                public boolean accept(final URI service) {
                    System.out.println(service);
                    return false;
                }
            });
        } finally {
            search.close();
        }

    }

}
