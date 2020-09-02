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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.bootstrap;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Will allocate a block of ports as an iterable.  The ports are held open
 * and reserved until the time the user calls `next()` on the iterable.
 *
 * After the user calls `next()` the port stays in a reserved status for
 * one minute and will not be issued again by this class within that time.
 *
 * Ports returned are guaranteed to be unique with no duplicates.
 *
 * It is important to note once the user calls `next()` the port is released
 * and nothing stops an external process from grabbing the port.  The one minute
 * reserved status is only effective for code in this VM using this utility.
 */
public class Ports {

    private static final List<Port> allocated = new CopyOnWriteArrayList<>();

    private Ports() {
    }

    /**
     * Allocates a single port which is immediately released
     * and available.
     */
    public static int allocate() {
        return allocate(1).iterator().next();
    }

    /**
     * Allocates N ports all of which are open and being held
     * and not available until the iterable is consumed.
     */
    public static Iterable<Integer> allocate(final int count) {
        // Trim any old ports from the list
        final ListIterator<Port> iterator = allocated.listIterator();
        while (iterator.hasNext()) {
            final Port port = iterator.next();
            if (port.isOld()) iterator.remove();
        }

        // Allocate new ports
        final List<Port> ports = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final Port port = new Port();
            if (isNotReserved(port)) {
                ports.add(port);
            }
        }

        // Add them to the allocated list so we don't issue them
        // again for at least a minute
        allocated.addAll(ports);

        return new Iterable<Integer>() {
            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {
                    @Override
                    public boolean hasNext() {
                        return ports.size() > 0;
                    }

                    @Override
                    public Integer next() {
                        final Port port = ports.remove(0);
                        // A port's "age" starts right now
                        //
                        // The person who called this method
                        // now has one minute to consume the port
                        return port.release();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("remove");
                    }
                };
            }
        };
    }

    /**
     * If this port is in the allocated list, we must immediately
     * release the port and not include it in the new list
     */
    private static boolean isNotReserved(Port port) {
        if (allocated.contains(port)) {
            port.release();
            return false;
        }
        return true;
    }

    static class Port {

        private final ServerSocket serverSocket;
        private final int port;
        private volatile long closed;
        private long tolerance;

        Port() {
            this(TimeUnit.MINUTES.toNanos(1));
        }

        Port(final long nanoseconds) {
            this.tolerance = nanoseconds;
            try {
                // When the system is out of ports the following exception will be thrown and caught here
                // java.net.SocketException: Too many open files in system
                this.serverSocket = new ServerSocket(0);
                this.port = this.serverSocket.getLocalPort();
                this.closed = 0;
            } catch (final IOException e) {
                throw new IllegalStateException("Unable to create a server socket with random port", e);
            }
        }

        /**
         * If this port has been released more than a minute ago, it is old
         * and should be removed from the list
         */
        public boolean isOld() {
            return closed != 0 && closed < System.nanoTime();
        }

        public int get() {
            return port;
        }

        public int release() {
            if (serverSocket.isClosed()) throw new IllegalStateException("Port has already been consumed");

            final int port = serverSocket.getLocalPort();
            try {
                serverSocket.close();
                this.closed = System.nanoTime() + tolerance;
                return port;
            } catch (IOException e) {
                throw new IllegalStateException("Unable to close server socket and free port " + port, e);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Port port1 = (Port) o;

            if (port != port1.port) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return port;
        }
    }
}
