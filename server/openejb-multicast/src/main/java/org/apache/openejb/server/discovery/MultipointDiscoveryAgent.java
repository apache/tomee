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
import java.net.Socket;
import java.net.URI;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @version $Rev$ $Date$
 */
public class MultipointDiscoveryAgent implements DiscoveryAgent, ServerService, SelfManaging {

    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB_SERVER.createChild("discovery").createChild("multipoint"), MultipointDiscoveryAgent.class);

    private AtomicBoolean running = new AtomicBoolean(false);

    private String host = "127.0.0.1";
    private int port = 4212;

    private String initialServers = "";

    private long heartRate = 500;

    private Tracker tracker;
    private MultipointServer multipointServer;
    private boolean debug;
    private String name;

    public MultipointDiscoveryAgent() {
    }

    public MultipointDiscoveryAgent(boolean debug, String name) {
        this.debug = debug;
        this.name = name;
    }

    public void init(Properties props) {

        Options options = new Options(props);
        options.setLogger(new OptionsLog(log));

        host = props.getProperty("bind", host);
        port = options.get("port", port);
        initialServers = options.get("initialServers", initialServers);
        heartRate = options.get("heart_rate", heartRate);


        Tracker.Builder builder = new Tracker.Builder();
        builder.setHeartRate(heartRate);
        builder.setGroup(props.getProperty("group", builder.getGroup()));
        builder.setMaxMissedHeartbeats(options.get("max_missed_heartbeats", builder.getMaxMissedHeartbeats()));
        builder.setMaxReconnectDelay(options.get("max_reconnect_delay", builder.getMaxReconnectDelay()));
        builder.setReconnectDelay(options.get("reconnect_delay", builder.getReconnectDelay()));
        builder.setExponentialBackoff(options.get("exponential_backoff", builder.getExponentialBackoff()));
        builder.setMaxReconnectAttempts(options.get("max_reconnect_attempts", builder.getMaxReconnectAttempts()));
        builder.setDebug(debug);

        tracker = builder.build();
    }

    public String getIP() {
        return host;
    }

    public String getName() {
        return "multipoint";
    }

    public int getPort() {
        return port;
    }

    public String getInitialServers() {
        return initialServers;
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

                multipointServer = new MultipointServer(host, port, tracker, name, debug).start();

                this.port = multipointServer.getPort();
                
                // Connect the initial set of peer servers
                StringTokenizer st = new StringTokenizer(initialServers, ",");
                while (st.hasMoreTokens()) {
                    multipointServer.connect(URI.create("conn://"+st.nextToken().trim()));
                }

            }
        } catch (Exception e) {
            throw new ServiceException(port+"", e);
        }
    }

    /**
     * stop the channel
     *
     * @throws Exception
     */
    public void stop() throws ServiceException {
        if (running.compareAndSet(true, false)) {
            multipointServer.stop();
        }
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
    }

    public void service(Socket socket) throws ServiceException, IOException {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}