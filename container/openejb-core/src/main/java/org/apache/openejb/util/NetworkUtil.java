/*
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
package org.apache.openejb.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;

public final class NetworkUtil {

    private NetworkUtil() {
        // no-op
    }

    public static int getNextAvailablePort() {
        return getNextAvailablePort(new int[]{0});
    }

    public static int getNextAvailablePort(final int[] portList) {
        int port;
        ServerSocket s = null;
        try {
            s = create(portList);
            port = s.getLocalPort();
        } catch (IOException ioe) {
            port = -1;
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }
        }
        return port;
    }

    public static int getNextAvailablePort(final int min, final int max, final Collection<Integer> excepted) {
        int port = -1;
        ServerSocket s = null;
        for (int i = min; i <= max; i++) {
            try {
                s = create(new int[]{i});
                port = s.getLocalPort();

                if (excepted == null || !excepted.contains(port)) {
                    break;
                }
            } catch (IOException ioe) {
                port = -1;
            } finally {
                if (s != null) {
                    try {
                        s.close();
                    } catch (Throwable e) {
                        //Ignore
                    }
                }
            }
        }
        return port;
    }

    private static ServerSocket create(final int[] ports) throws IOException {
        for (final int port : ports) {
            try {
                return new ServerSocket(port);
            } catch (IOException ex) {
                // try next port
            }
        }

        // if the program gets here, no port in the range was found
        throw new IOException("No free port found");
    }

    public static String getLocalAddress(final String start, final String end) {
        return start + "localhost:" + getNextAvailablePort() + end;
    }
}
