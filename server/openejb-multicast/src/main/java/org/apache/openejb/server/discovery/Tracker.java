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

import org.apache.openejb.server.DiscoveryListener;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
*/
public class Tracker {
    
    private final Logger log;
    
    private final String group;
    private final String groupPrefix;
    private final long heartRate;
    private final int maxMissedHeartbeats;
    private final long reconnectDelay;
    private final long maxReconnectDelay;
    private final int maxReconnectAttempts;
    private final long exponentialBackoff;
    private final boolean useExponentialBackOff;
    private final boolean debug;

    public Tracker(String group, long heartRate, int maxMissedHeartbeats, long reconnectDelay, long maxReconnectDelay, int maxReconnectAttempts, long exponentialBackoff, final Logger log, boolean debug) {
        this.group = group;
        this.groupPrefix = group + ":";

        this.heartRate = heartRate;
        this.maxMissedHeartbeats = maxMissedHeartbeats;
        this.reconnectDelay = reconnectDelay;
        this.maxReconnectDelay = maxReconnectDelay;
        this.maxReconnectAttempts = maxReconnectAttempts;
        this.exponentialBackoff = exponentialBackoff;
        this.useExponentialBackOff = exponentialBackoff > 1;
        this.log = log;
        this.debug = debug;
        this.log.info("Created " + this);
    }

    private Map<String, Service> registeredServices = new ConcurrentHashMap<String, Service>();

    private Map<String, ServiceVitals> discoveredServices = new ConcurrentHashMap<String, ServiceVitals>();
    private DiscoveryListener discoveryListener;

    public long getHeartRate() {
        return heartRate;
    }

    public int getMaxMissedHeartbeats() {
        return maxMissedHeartbeats;
    }

    public void setDiscoveryListener(DiscoveryListener discoveryListener) {
        this.discoveryListener = discoveryListener;
    }

    public Set<String> getRegisteredServices() {
        return registeredServices.keySet();
    }

    public void registerService(URI serviceUri) throws IOException {
        Service service = new Service(serviceUri);
        this.registeredServices.put(service.broadcastString, service);
        fireServiceAddedEvent(serviceUri);
    }

    public void unregisterService(URI serviceUri) throws IOException {
        Service service = new Service(serviceUri);
        this.registeredServices.remove(service.broadcastString);
        fireServiceRemovedEvent(serviceUri);
    }

    private boolean isSelf(Service service) {
        return isSelf(service.broadcastString);
    }

    private boolean isSelf(String service) {
        return registeredServices.keySet().contains(service);
    }

    public void processData(String uriString) {
        if (discoveryListener == null) {
            return;
        }

        if (!uriString.startsWith(groupPrefix)){
            return;
        }

        if (isSelf(uriString)) {
            return;
        }

        ServiceVitals vitals = discoveredServices.get(uriString);

        if (vitals == null) {
            try {
                vitals = new ServiceVitals(new Service(uriString));

                discoveredServices.put(uriString, vitals);

                fireServiceAddedEvent(vitals.service.uri);
            } catch (URISyntaxException e) {
                // don't continuously log this
            }

        } else {
            vitals.heartbeat();

            if (vitals.doRecovery()) {
                fireServiceAddedEvent(vitals.service.uri);
            }
        }
    }

    public void checkServices() {
        final long threshold = heartRate * maxMissedHeartbeats;

        final long now = System.currentTimeMillis();

        final long expireTime = now - threshold;

        for (ServiceVitals serviceVitals : discoveredServices.values()) {
            if (serviceVitals.getLastHeartbeat() < expireTime && !isSelf(serviceVitals.service)) {

                if (debug()) {
                    log.debug("Expired " + serviceVitals.service + String.format(" Timeout{lastSeen=%s, threshold=%s}", serviceVitals.getLastHeartbeat() - now, threshold ));
                }

                ServiceVitals vitals = discoveredServices.remove(serviceVitals.service.broadcastString);
                if (vitals != null && !vitals.isDead()) {
                    fireServiceRemovedEvent(vitals.service.uri);
                }
            }
        }
    }

    private boolean debug() {
        return debug && log.isDebugEnabled();
    }

    private final Executor executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
        public Thread newThread(Runnable runable) {
            Thread t = new Thread(runable, "Discovery Agent Notifier");
            t.setDaemon(true);
            return t;
        }
    });

    private void fireServiceRemovedEvent(final URI uri) {
        if (debug()) {
            log.debug(String.format("Removed Service{uri=%s}", uri));
        }

        if (discoveryListener != null) {
            final DiscoveryListener discoveryListener = this.discoveryListener;

            // Have the listener process the event async so that
            // he does not block this thread since we are doing time sensitive
            // processing of events.
            executor.execute(new Runnable() {
                public void run() {
                    if (discoveryListener != null) {
                        discoveryListener.serviceRemoved(uri);
                    }
                }
            });
        }
    }

    private void fireServiceAddedEvent(final URI uri) {
        if (debug()) {
            log.debug(String.format("Added Service{uri=%s}", uri));
        }

        if (discoveryListener != null) {
            final DiscoveryListener discoveryListener = this.discoveryListener;

            // Have the listener process the event async so that
            // he does not block this thread since we are doing time sensitive
            // processing of events.
            executor.execute(new Runnable() {
                public void run() {
                    if (discoveryListener != null) {
                        discoveryListener.serviceAdded(uri);
                    }
                }
            });
        }
    }

    public void reportFailed(URI serviceUri) {
        final Service service = new Service(serviceUri);
        ServiceVitals serviceVitals = discoveredServices.get(service.broadcastString);
        if (serviceVitals != null && serviceVitals.pronounceDead()) {
            fireServiceRemovedEvent(service.uri);
        }
    }

    public class Service {
        private final URI uri;
        private final String broadcastString;

        public Service(URI uri) {
            this.uri = uri;
            this.broadcastString = groupPrefix + uri.toString();
        }

        public Service(String uriString) throws URISyntaxException {
            URI uri = new URI(uriString);
            uri = new URI(uri.getSchemeSpecificPart());
            this.uri = uri;
            this.broadcastString = uriString;
        }

        @Override
        public String toString() {
            return "Service{" +
                    "uri=" + uri +
                    ", broadcastString='" + broadcastString + '\'' +
                    '}';
        }
    }

    private class ServiceVitals {

        private final Service service;

        private long lastHeartBeat;
        private long recoveryTime;
        private int failureCount;
        private boolean dead;

        public ServiceVitals(Service service) {
            this.service = service;
            this.lastHeartBeat = System.currentTimeMillis();
        }

        public synchronized void heartbeat() {
            lastHeartBeat = System.currentTimeMillis();

            // Consider that the service recovery has succeeded if it has not
            // failed in 60 seconds.
            if (!dead && failureCount > 0 && (lastHeartBeat - recoveryTime) > 1000 * 60) {
                if (debug()) {
                    log.debug("I now think that the " + service + " service has recovered.");
                }
                failureCount = 0;
                recoveryTime = 0;
            }
        }

        public synchronized long getLastHeartbeat() {
            return lastHeartBeat;
        }

        public synchronized boolean pronounceDead() {
            if (!dead) {
                dead = true;
                failureCount++;

                long delay;
                if (useExponentialBackOff) {
                    delay = (long) Math.pow(exponentialBackoff, failureCount);
                    if (delay > maxReconnectDelay) {
                        delay = maxReconnectDelay;
                    }
                } else {
                    delay = reconnectDelay;
                }

                if (debug()) {
                    log.debug("Remote failure of " + service + " while still receiving multicast advertisements.  " +
                            "Advertising events will be suppressed for " + delay
                            + " ms, the current failure count is: " + failureCount);
                }

                recoveryTime = System.currentTimeMillis() + delay;
                return true;
            }
            return false;
        }

        /**
         * @return true if this broker is marked failed and it is now the right
         *         time to start recovery.
         */
        public synchronized boolean doRecovery() {
            if (!dead) {
                return false;
            }

            // Are we done trying to recover this guy?
            if (maxReconnectAttempts > 0 && failureCount > maxReconnectAttempts) {
                if (debug()) {
                    log.debug("Max reconnect attempts of the " + service + " service has been reached.");
                }
                return false;
            }

            // Is it not yet time?
            if (System.currentTimeMillis() < recoveryTime) {
                return false;
            }

            if (debug()) {
                log.debug("Resuming event advertisement of the " + service + " service.");
            }
            dead = false;
            return true;
        }

        public boolean isDead() {
            return dead;
        }

        @Override
        public String toString() {
            return service + "Vitals{" +
                    ", lastHeartBeat=" + lastHeartBeat +
                    ", recoveryTime=" + recoveryTime +
                    ", failureCount=" + failureCount +
                    ", dead=" + dead +
                    '}';
        }
    }


    public static class Builder {
        private String group = "default";
        private int maxMissedHeartbeats = 10;
        private long heartRate = 500;
        // ---------------------------------
        // Listenting specific settings
        private long reconnectDelay = 1000 * 5;
        private long maxReconnectDelay = 1000 * 30;
        private long exponentialBackoff = 0;
        private int maxReconnectAttempts = 10; // todo: check this out
        private Logger logger;
        private boolean debug;
        // ---------------------------------


        public long getExponentialBackoff() {
            return exponentialBackoff;
        }

        public void setExponentialBackoff(long exponentialBackoff) {
            this.exponentialBackoff = exponentialBackoff;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public long getHeartRate() {
            return heartRate;
        }

        public void setHeartRate(long heartRate) {
            this.heartRate = heartRate;
        }

        public long getReconnectDelay() {
            return reconnectDelay;
        }

        public void setReconnectDelay(long reconnectDelay) {
            this.reconnectDelay = reconnectDelay;
        }

        public int getMaxMissedHeartbeats() {
            return maxMissedHeartbeats;
        }

        public void setMaxMissedHeartbeats(int maxMissedHeartbeats) {
            this.maxMissedHeartbeats = maxMissedHeartbeats;
        }

        public int getMaxReconnectAttempts() {
            return maxReconnectAttempts;
        }

        public void setMaxReconnectAttempts(int maxReconnectAttempts) {
            this.maxReconnectAttempts = maxReconnectAttempts;
        }

        public long getMaxReconnectDelay() {
            return maxReconnectDelay;
        }

        public void setMaxReconnectDelay(long maxReconnectDelay) {
            this.maxReconnectDelay = maxReconnectDelay;
        }

        public Logger getLogger() {
            return logger;
        }

        public void setLogger(Logger logger) {
            this.logger = logger;
        }

        public boolean isDebug() {
            return debug;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }

        public Tracker build() {
            logger = Logger.getInstance(LogCategory.OPENEJB_SERVER.createChild("discovery"), Tracker.class);
            return new Tracker(group, heartRate, maxMissedHeartbeats, reconnectDelay, maxReconnectDelay, maxReconnectAttempts, exponentialBackoff, logger, debug);
        }
    }

    @Override
    public String toString() {
        return "Tracker{" +
                "group='" + group + '\'' +
                ", groupPrefix='" + groupPrefix + '\'' +
                ", heartRate=" + heartRate +
                ", maxMissedHeartbeats=" + maxMissedHeartbeats +
                ", reconnectDelay=" + reconnectDelay +
                ", maxReconnectDelay=" + maxReconnectDelay +
                ", maxReconnectAttempts=" + maxReconnectAttempts +
                ", exponentialBackoff=" + exponentialBackoff +
                ", useExponentialBackOff=" + useExponentialBackOff +
                ", registeredServices=" + registeredServices.size() +
                ", discoveredServices=" + discoveredServices.size() +
                '}';
    }
}
