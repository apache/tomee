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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO: this class is no more an utility class (static)
 * and should be rewritten to get a config + state
 * -> listOfPorts with release(port) method (otherwise caching makes half of usages broken)
 * -> lockFile
 * -> minPort/maxPort
 * -> ...
 */
public final class NetworkUtil {

    /**
     * Lock file property name
     */
    public static final String TOMEE_LOCK_FILE = "TOMEE_LOCK_FILE";
    public static final int[] RANDOM = new int[]{0};

    private static final ReentrantLock lock = new ReentrantLock();
    private static final ByteBuffer buf = ByteBuffer.allocate(512);
    public static final int PORT_MIN = 1025;
    public static final int PORT_MAX = 65535;
    public static final int EVICTION_TIMEOUT = Integer.getInteger("openejb.network.random-port.cache-timeout", 10000);
    private static File lockFile;

    private NetworkUtil() {
        // no-op
    }

    public static synchronized void clearLockFile() {
        System.clearProperty(NetworkUtil.TOMEE_LOCK_FILE);
        lockFile = null;
    }

    public static synchronized int getNextAvailablePortInDefaultRange() {
        final ReentrantLock l = lock;
        l.lock();
        try {
            return getNextAvailablePort(PORT_MIN, PORT_MAX, null);
        } finally {
            l.unlock();
        }
    }

    // fully random by default to avoid to get PORT_MIN, PORT_MIN, PORT_MIN locally/on a dev machine
    public static synchronized int getNextAvailablePort() {
        final ReentrantLock l = lock;
        l.lock();
        try {
            return getNextAvailablePort(RANDOM);
        } finally {
            l.unlock();
        }
    }

    public static synchronized int getNextAvailablePort(final int[] portList) {

        final ReentrantLock l = lock;
        l.lock();

        final int originalRetryCount = Integer.getInteger("openejb.network.random-port.retries", 10);
        int retry = originalRetryCount;
        ServerSocket s = null;
        try {
            do {
                try {
                    s = create(portList, null);
                    return s.getLocalPort();
                } catch (final IOException ioe) {
                    // particular case where iteration is not really the meaning of the config
                    final boolean isRandom = portList == RANDOM || (portList.length == 1 && portList[0] == 0);
                    if (isRandom) {
                        retry--;
                    } else { // otherwise infinite loop
                        retry = 0;
                    }
                    if (retry <= 0) { // 0 retry -> -1
                        throw new IllegalStateException("Failed to find a port matching list " + Arrays.toString(portList) + (isRandom ? " with " + originalRetryCount + " retries" : ""));
                    }
                } finally {
                    if (s != null) {
                        try {
                            s.close();
                        } catch (final Throwable e) {
                            //Ignore
                        }
                    }
                }
            } while (true);
        } finally {
            l.unlock();
        }
    }

    public static synchronized int getNextAvailablePort(final int min, final int max, final Collection<Integer> excluded, final Collection<LastPort> lastPorts) {
        final ReentrantLock l = lock;
        l.lock();

        try {
            purgeLast(lastPorts);
            int port = -1;
            ServerSocket s = null;
            for (int i = min; i <= max; i++) {

                if (excluded != null && excluded.contains(i) || i > PORT_MAX || i < PORT_MIN) {
                    continue;
                }

                try {
                    s = create(new int[]{i}, lastPorts);
                    port = s.getLocalPort();
                    break;

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

            return port;
        } finally {
            l.unlock();
        }
    }

    public static synchronized int getNextAvailablePort(final int min, final int max, final Collection<Integer> excluded) {
        return getNextAvailablePort(min, max, excluded, null);
    }

    private static void purgeLast(final Collection<LastPort> lastPort) {
        if (lastPort == null) {
            return;
        }
        final Iterator<LastPort> it = lastPort.iterator();
        while (it.hasNext()) {
            final LastPort last = it.next();
            if ((System.currentTimeMillis() - last.time) >= EVICTION_TIMEOUT) {
                it.remove();
            }
        }
    }

    private static ServerSocket create(final int[] ports, final Collection<LastPort> lastPort) throws IOException {

        for (int port : ports) {
            try {

                final ServerSocket ss = new ServerSocket(port);
                port = ss.getLocalPort();

                final LastPort lp = new LastPort(port, System.currentTimeMillis());

                if (lastPort != null) {
                    if (lastPort.contains(lp)) {
                        try {
                            ss.close();
                        } catch (final Exception e) {
                            //Ignore
                        }
                        continue;
                    }
                }

                if (!checkLockFile(port)) {
                    try {
                        ss.close();
                    } catch (final Exception e) {
                        //Ignore
                    }
                    continue;
                }

                if (lastPort != null) {
                    lastPort.add(lp);
                }

                return ss;

            } catch (final IOException ex) {
                // try next port
            }
        }

        // If the program gets here, no port in the range was found
        throw new IOException("No free port found");
    }

    private static File getLockFile() {

        if (null == lockFile) {
            String lf = System.getenv("TOMEE_LOCK_FILE");
            lf = (null != lf ? lf : System.getProperty("TOMEE_LOCK_FILE"));

            if (null != lf) {
                final File f = new File(lf);
                try {
                    lockFile = (!f.exists() && !f.createNewFile() ? null : (f.isFile() ? f : null));
                } catch (final IOException e) {
                    //Ignore
                }
            }
        }

        return lockFile;
    }

    /**
     * If a lockfile exists then see if we can really reserve this port
     *
     * @param port int
     * @return true if we can reserve else false
     */
    private static boolean checkLockFile(final int port) {

        boolean result = true;

        final File lf = getLockFile();
        if (null != lf) {

            final Properties p = new Properties();
            RandomAccessFile raf = null;
            ByteArrayOutputStream baos = null;
            ByteArrayInputStream bais = null;
            FileLock lock = null;

            try {

                raf = new RandomAccessFile(lf, "rw");
                final FileChannel fileChannel = raf.getChannel();

                int i = 0;
                while ((lock = fileChannel.tryLock()) == null) {
                    Thread.sleep(10);
                    i++;

                    if (i > 200) {
                        return false;
                    }
                }

                baos = new ByteArrayOutputStream();

                while (fileChannel.read(buf) > 0) {
                    baos.write((byte[]) buf.flip().array());
                    buf.clear();
                }

                bais = new ByteArrayInputStream(baos.toByteArray());
                p.load(bais);

                final boolean purged = purgeOld(p);

                if (null != p.getProperty(String.valueOf(port))) {
                    result = false;
                    //System.out.println("Locked " + port);
                } else {
                    p.setProperty(String.valueOf(port), String.valueOf(System.currentTimeMillis()));
                    //System.out.println("Reserved " + port);
                }

                if (result || purged) {
                    baos.reset();
                    p.store(baos, "TomEE port locks");
                    fileChannel.truncate(0);
                    fileChannel.write(ByteBuffer.wrap(baos.toByteArray()));
                }

            } catch (final Exception e) {
                result = false;
            } finally {
                if (null != lock) {
                    try {
                        lock.release();
                    } catch (final Exception e) {
                        //Ignore
                    }
                }
                if (null != baos) {
                    try {
                        baos.close();
                    } catch (final Exception e) {
                        //Ignore
                    }
                }
                if (null != bais) {
                    try {
                        bais.close();
                    } catch (final Exception e) {
                        //Ignore
                    }
                }
                if (null != raf) {
                    try {
                        raf.close();
                    } catch (final Exception e) {
                        //Ignore
                    }
                }
            }
        }

        return result;
    }

    /**
     * Purge keys (ports) older than 30 seconds
     *
     * @param p Properties
     */
    private static boolean purgeOld(final Properties p) {

        boolean purged = false;
        final long now = System.currentTimeMillis();
        final Set<String> names = p.stringPropertyNames();

        for (final String key : names) {
            final String value = p.getProperty(key);

            if (isOld(now, value)) {
                purged = true;
                p.remove(key);
            }
        }

        return purged;
    }

    private static boolean isOld(final long now, final String value) {
        try {
            return now - Long.parseLong(value) > 30000;
        } catch (final Exception e) {
            return true;
        }
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

    public static final class LastPort {
        private final int port;
        private final long time;

        private LastPort(final int port, final long time) {
            this.port = port;
            this.time = time;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final LastPort lastPort = (LastPort) o;

            return port == lastPort.port;

        }

        @Override
        public int hashCode() {
            return port;
        }
    }
}
