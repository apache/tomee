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
package org.apache.openejb.util;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author Romain Manni-Bucau
 */
public final class NetworkUtil {
    private NetworkUtil() {
        // no-op
    }

    public static int getNextAvailablePort() {
        return getNextAvailablePort(new int[]{0});
    }

    public static int getNextAvailablePort(int[] portList) {
        int port;
        ServerSocket s = null;
        try {
            s = create(portList);
            port = s.getLocalPort();
            s.close();
        } catch (IOException ioe) {
            port = -1;
        }
        return port;
    }

    private static ServerSocket create(int[] ports) throws IOException {
        for (int port : ports) {
            try {
                return new ServerSocket(port);
            } catch (IOException ex) {
                continue; // try next port
            }
        }

        // if the program gets here, no port in the range was found
        throw new IOException("no free port found");
    }

    public static String getLocalAddress(String start, String end) {
        return start + "localhost:" + getNextAvailablePort() + end;
    }
}
