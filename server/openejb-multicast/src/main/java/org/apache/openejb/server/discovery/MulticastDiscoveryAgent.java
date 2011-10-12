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

import org.apache.openejb.server.SelfManaging;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.DiscoveryAgent;
import org.apache.openejb.server.DiscoveryListener;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.loader.Options;
import org.apache.openejb.util.OptionsLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @version $Rev$ $Date$
 */
public class MulticastDiscoveryAgent implements DiscoveryAgent, ServerService, SelfManaging {

    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB_SERVER.createChild("discovery").createChild("multicast"), MulticastDiscoveryAgent.class);

    private AtomicBoolean running = new AtomicBoolean(false);

    private String host = "239.255.3.2";
    private int port = 6142;

    private int timeToLive = 1;
    private boolean loopbackMode = false;
    private InetSocketAddress address;
    private long heartRate = 500;

    private Tracker tracker;
    private Multicast multicast;

    public void init(Properties props) {

        Options options = new Options(props);
        options.setLogger(new OptionsLog(log));

        host = props.getProperty("bind", host);
        loopbackMode = options.get("loopback_mode", loopbackMode);
        port = options.get("port", port);
        heartRate = options.get("heart_rate", heartRate);


        Tracker.Builder builder = new Tracker.Builder();
        builder.setGroup(props.getProperty("group", builder.getGroup()));
        builder.setHeartRate(heartRate);
        builder.setMaxMissedHeartbeats(options.get("max_missed_heartbeats", builder.getMaxMissedHeartbeats()));
        builder.setMaxReconnectDelay(options.get("max_reconnect_delay", builder.getMaxReconnectDelay()));
        builder.setReconnectDelay(options.get("reconnect_delay", builder.getReconnectDelay()));
        builder.setExponentialBackoff(options.get("exponential_backoff", builder.getExponentialBackoff()));
        builder.setMaxReconnectAttempts(options.get("max_reconnect_attempts", builder.getMaxReconnectAttempts()));

        tracker = builder.build();
    }

    public String getIP() {
        return host;
    }

    public String getName() {
        return "multicast";
    }

    public int getPort() {
        return port;
    }

    public void setDiscoveryListener(DiscoveryListener listener) {
        this.tracker.setDiscoveryListener(listener);
    }

    public void registerService(URI serviceUri) throws IOException {
        tracker.registerService(serviceUri);
    }

    public void unregisterService(URI serviceUri) throws IOException {
        tracker.unregisterService(serviceUri);
    }

    public void reportFailed(URI serviceUri) {
        tracker.reportFailed(serviceUri);
    }

    public static void main(String[] args) throws Exception {
    }

    /**
     * start the discovery agent
     *
     * @throws Exception
     */
    public void start() throws ServiceException {
        try {
            if (running.compareAndSet(false, true)) {

                InetAddress inetAddress = InetAddress.getByName(host);

                this.address = new InetSocketAddress(inetAddress, port);
                multicast = new Multicast(tracker);
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * stop the channel
     *
     * @throws Exception
     */
    public void stop() throws ServiceException {
        if (running.compareAndSet(true, false)) {
            multicast.close();
        }
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
    }

    public void service(Socket socket) throws ServiceException, IOException {
    }

    class Multicast {

        private static final int BUFF_SIZE = 8192;
        
        private final Tracker tracker;
        private final MulticastSocket multicast;
        private Timer timer;
        private Thread listenerThread;

        Multicast(Tracker tracker) throws IOException {
            this.tracker = tracker;

            multicast = new MulticastSocket(port);
            multicast.setLoopbackMode(loopbackMode);
            multicast.setTimeToLive(timeToLive);
            multicast.joinGroup(address.getAddress());
            multicast.setSoTimeout((int) heartRate);

            listenerThread = new Thread(new Listener());
            listenerThread.setName("MulticastDiscovery: Listener");
            listenerThread.setDaemon(true);
            listenerThread.start();

            Broadcaster broadcaster = new Broadcaster();

            timer = new Timer("MulticastDiscovery: Broadcaster", true);
            timer.scheduleAtFixedRate(broadcaster, 0, heartRate);

        }

        public void close() {
            timer.cancel();
        }

        class Listener implements Runnable {
            public void run() {
                byte[] buf = new byte[BUFF_SIZE];
                DatagramPacket packet = new DatagramPacket(buf, 0, buf.length);
                while (running.get()) {
                    tracker.checkServices();
                    try {
                        multicast.receive(packet);
                        if (packet.getLength() > 0) {
                            String str = new String(packet.getData(), packet.getOffset(), packet.getLength());
//                        System.out.println("read = " + str);
                            tracker.processData(str);
                        }
                    } catch (SocketTimeoutException se) {
                        // ignore
                    } catch (IOException e) {
                        if (running.get()) {
                            log.error("failed to process packet: " + e);
                        }
                    }
                }
            }
            
        }

        class Broadcaster extends TimerTask {
            private IOException failed;

            public void run() {
                if (running.get()) {
                    heartbeat();
                }
            }

            private void heartbeat() {
                for (String uri : tracker.getRegisteredServices()) {
                    try {
                        byte[] data = uri.getBytes();
                        DatagramPacket packet = new DatagramPacket(data, 0, data.length, address);
//                    System.out.println("ann = " + uri);
                        multicast.send(packet);
                    } catch (IOException e) {
                        // If a send fails, chances are all subsequent sends will fail
                        // too.. No need to keep reporting the
                        // same error over and over.
                        if (failed == null) {
                            failed = e;

                            log.error("Failed to advertise our service: " + uri, e);
                            if ("Operation not permitted".equals(e.getMessage())) {
                                log.error("The 'Operation not permitted' error has been know to be caused by improper firewall/network setup.  "
                                        + "Please make sure that the OS is properly configured to allow multicast traffic over: " + multicast.getLocalAddress());
                            }
                        }
                    }
                }
            }
        }
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isLoopbackMode() {
        return loopbackMode;
    }

    public void setLoopbackMode(boolean loopbackMode) {
        this.loopbackMode = loopbackMode;
    }
    public int getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

}
