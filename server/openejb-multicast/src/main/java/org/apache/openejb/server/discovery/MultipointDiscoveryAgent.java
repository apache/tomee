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

import org.apache.openejb.monitoring.Event;
import org.apache.openejb.monitoring.Managed;
import org.apache.openejb.server.SelfManaging;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.DiscoveryAgent;
import org.apache.openejb.server.DiscoveryListener;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.loader.Options;
import org.apache.openejb.util.OptionsLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @version $Rev$ $Date$
 */
public class MultipointDiscoveryAgent implements DiscoveryAgent, ServerService, SelfManaging {

    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB_SERVER.createChild("discovery").createChild("multipoint"), MultipointDiscoveryAgent.class);

    private AtomicBoolean running = new AtomicBoolean(false);

    @Managed
    private String host = "127.0.0.1";

    @Managed
    private int port = 4212;

    private String initialServers = "";

    private long heartRate = 500;

    @Managed(append = false)
    private Tracker tracker;

    private MultipointServer multipointServer;

    private boolean debug = true;
    private String name;
    private String discoveryHost;
    private Set<URI> roots;
    private Duration reconnectDelay;

    @Managed
    private final Event restarts = new Event();

    public MultipointDiscoveryAgent() {
    }

    public MultipointDiscoveryAgent(boolean debug, String name) {
        this.debug = debug;
        this.name = name;
    }

    public void init(Properties props) {

        final Options options = new Options(props);
        options.setLogger(new OptionsLog(log));

        host = props.getProperty("bind", host);
        port = options.get("port", port);
        initialServers = options.get("initialServers", initialServers);
        heartRate = options.get("heart_rate", heartRate);
        discoveryHost = options.get("discoveryHost", host);
        name = name != null ? name : options.get("discoveryName", MultipointServer.randomColor());
        reconnectDelay = options.get("reconnectDelay", new Duration("30 seconds"));

        final Set<URI> uris = new LinkedHashSet<URI>();

        // Connect the initial set of peer servers
        StringTokenizer st = new StringTokenizer(initialServers, ",");
        while (st.hasMoreTokens()) {
            final String string = st.nextToken().trim();
            if (string.startsWith("conn://")) {
                final URI uri = URI.create(string);
                uris.add(uri);
            } else {
                final URI uri = URI.create("conn://" + string);
                uris.add(uri);
            }
        }

        roots = uris;

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

    @Override
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
    @Managed
    public void start() throws ServiceException {
        try {
            if (running.compareAndSet(false, true)) {
                log.info("MultipointDiscoveryAgent Starting");
                multipointServer = new MultipointServer(host, discoveryHost, port, tracker, name, debug, roots, reconnectDelay).start();
                log.info("MultipointDiscoveryAgent Started");

                this.port = multipointServer.getPort();

            }
        } catch (Exception e) {
            throw new ServiceException(port+"", e);
        }
    }

    @Managed
    public void restart() throws ServiceException {
        stop();
        start();
        restarts.record();
    }

    /**
     * stop the channel
     *
     * @throws Exception
     */
    @Managed
    public void stop() throws ServiceException {
        if (running.compareAndSet(true, false)) {
            log.info("MultipointDiscoveryAgent Stopping");
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

    @Managed
    public URI getURI() {
        return multipointServer.getMe();
    }

    @Managed
    public Set<URI> getRoots() {
        return multipointServer.getRoots();
    }

    @Managed
    public long getRuns() {
        return multipointServer.getRuns().get();
    }

    @Managed
    public String getRunsLatest() {
        return multipointServer.getRuns().getLatest();
    }

    @Managed
    public long getRunsLatestTime() {
        return multipointServer.getRuns().getLatestTime();
    }

    @Managed
    public long getHeartbeats() {
        return multipointServer.getHeartbeats().get();
    }

    @Managed
    public String getHeartbeatsLatest() {
        return multipointServer.getHeartbeats().getLatest();
    }

    @Managed
    public long getHeartbeatsLatestTime() {
        return multipointServer.getHeartbeats().getLatestTime();
    }

    @Managed
    public long getSessionsCreated() {
        return multipointServer.getSessionsCreated().get();
    }

    @Managed
    public String getSessionsCreatedLatest() {
        return multipointServer.getSessionsCreated().getLatest();
    }

    @Managed
    public long getSessionsCreatedLatestTime() {
        return multipointServer.getSessionsCreated().getLatestTime();
    }

    @Managed
    public long getReconnects() {
        return multipointServer.getReconnects().get();
    }

    @Managed
    public String getReconnectsLatest() {
        return multipointServer.getReconnects().getLatest();
    }

    @Managed
    public long getReconnectsLatestTime() {
        return multipointServer.getReconnects().getLatestTime();
    }

    @Managed
    public long getJoined() {
        return multipointServer.getJoined();
    }

    @Managed
    public List<URI> getSessions() {
        return multipointServer.getSessions();
    }

    @Managed
    public List<URI> getConnectionsQueued() {
        return multipointServer.getConnectionsQueued();
    }

    @Managed
    public long getReconnectDelay() {
        return multipointServer.getReconnectDelay();
    }

}