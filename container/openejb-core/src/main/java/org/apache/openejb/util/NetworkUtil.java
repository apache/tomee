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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public final class NetworkUtil {

    private static final ReentrantLock lock = new ReentrantLock();
    private static final AtomicReference<LastPort> lastPort = new AtomicReference<LastPort>();

    private NetworkUtil() {
        // no-op
    }

    public static int getNextAvailablePort() {
        final ReentrantLock l = lock;
        l.lock();
        try {
            return getNextAvailablePort(new int[]{0});
        } finally {
            l.unlock();
        }
    }

    public static synchronized int getNextAvailablePort(final int[] portList) {

        final ReentrantLock l = lock;
        l.lock();

        try {
            int port;
            ServerSocket s = null;
            try {
                s = create(portList);
                port = s.getLocalPort();
            } catch (final IOException ioe) {
                port = -1;
            } finally {
                if (s != null) {
                    try {
                        s.close();
                    } catch (final Throwable e) {
                        //Ignore
                    }
                }
            }

            lastPort.set(new LastPort(port, System.currentTimeMillis()));
            return port;
        } finally {
            l.unlock();
        }
    }

    public static synchronized int getNextAvailablePort(final int min, final int max, final Collection<Integer> excluded) {

        final ReentrantLock l = lock;
        l.lock();

        try {
            int port = -1;
            ServerSocket s = null;
            for (int i = min; i <= max; i++) {
                try {
                    s = create(new int[]{i});
                    port = s.getLocalPort();

                    if (excluded == null || !excluded.contains(port)) {
                        break;
                    }
                } catch (final IOException ioe) {
                    port = -1;
                } finally {
                    if (s != null) {
                        try {
                            s.close();
                        } catch (final Throwable e) {
                            //Ignore
                        }
                    }
                }
            }

            lastPort.set(new LastPort(port, System.currentTimeMillis()));
            return port;
        } finally {
            l.unlock();
        }
    }

    private static ServerSocket create(final int[] ports) throws IOException {
        for (final int port : ports) {
            try {

                final LastPort last = lastPort.get();
                if (null != last && port == last.port) {
                    if ((System.currentTimeMillis() - last.time) < 10000) {
                        continue;
                    }
                }

                return new ServerSocket(port);
            } catch (final IOException ex) {
                // try next port
            }
        }

        // if the program gets here, no port in the range was found
        throw new IOException("No free port found");
    }

    public static String getLocalAddress(final String start, final String end) {
        return start + "localhost:" + getNextAvailablePort() + end;
    }

    public static boolean isLocalAddress(final String addr) {
        try {
            return isLocalAddress(InetAddress.getByName(addr));
        } catch (final Exception e) {
            return false;
        }
    }

    public static boolean isLocalAddress(final InetAddress addr) {

        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
            return true;
        }

        // Check if the address is defined on any interface
        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (final SocketException e) {
            return false;
        }
    }

    private static final class LastPort {
        private final int port;
        private final long time;

        private LastPort(final int port, final long time) {
            this.port = port;
            this.time = time;
        }
    }
}
