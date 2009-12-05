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

import java.net.URI;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
public class MulticastSearch {

    private static final int BUFF_SIZE = 8192;

    private final MulticastSocket multicast;

    public MulticastSearch() throws IOException {
        this("239.255.3.2", 6142);
    }

    public MulticastSearch(String host, int port) throws IOException {
        InetAddress inetAddress = InetAddress.getByName(host);

        multicast = new MulticastSocket(port);
        multicast.joinGroup(inetAddress);
        multicast.setSoTimeout(500);
    }

    public URI search(int timeout, TimeUnit milliseconds) throws IOException {
        return search(new DefaultFilter(), timeout, milliseconds);
    }

    public URI search() throws IOException {
        return search(new DefaultFilter(), 0, TimeUnit.MILLISECONDS);
    }

    public URI search(Filter filter) throws IOException {
        return search(filter, 0, TimeUnit.MILLISECONDS);
    }

    public URI search(Filter filter, long timeout, TimeUnit unit) throws IOException {
        timeout = TimeUnit.MILLISECONDS.convert(timeout, unit);
        long waited = 0;

        byte[] buf = new byte[BUFF_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, 0, buf.length);

        while (timeout == 0 || waited < timeout){
            long start = System.currentTimeMillis();
            try {
                multicast.receive(packet);
                if (packet.getLength() > 0) {
                    String str = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    URI service = URI.create(str);
                    if (service != null && filter.accept(service)) {
                        return service;
                    }
                }
            } finally {
                long stop = System.currentTimeMillis();
                waited += stop - start;
            }
        }

        return null;
    }

    public interface Filter {
        boolean accept(URI service);
    }

    public static class DefaultFilter implements Filter {
        public boolean accept(URI service) {
            return true;
        }
    }
}
