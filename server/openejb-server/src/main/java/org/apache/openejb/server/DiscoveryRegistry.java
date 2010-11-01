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
package org.apache.openejb.server;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.Managed;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
public class DiscoveryRegistry implements DiscoveryListener, DiscoveryAgent {

    private final List<DiscoveryAgent> agents = new CopyOnWriteArrayList<DiscoveryAgent>();
    private final List<DiscoveryListener> listeners = new CopyOnWriteArrayList<DiscoveryListener>();
    private final Map<String, URI> services = new ConcurrentHashMap<String, URI>();
    private final Map<String, URI> registered = new ConcurrentHashMap<String, URI>();

    @Managed
    private final Monitor monitor = new Monitor();

    private final Executor executor = new ThreadPoolExecutor(1, 10, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
        public Thread newThread(Runnable runable) {
            Thread t = new Thread(runable, DiscoveryRegistry.class.getSimpleName());
            t.setDaemon(true);
            return t;
        }
    });

    public DiscoveryRegistry() {
        SystemInstance.get().setComponent(DiscoveryRegistry.class, this);
        SystemInstance.get().setComponent(DiscoveryAgent.class, this);
    }

    public DiscoveryRegistry(DiscoveryAgent agent) {
        SystemInstance.get().setComponent(DiscoveryRegistry.class, this);
        SystemInstance.get().setComponent(DiscoveryAgent.class, this);
        addDiscoveryAgent(agent);
    }

    public void addDiscoveryAgent(DiscoveryAgent agent) {
        agents.add(agent);
        agent.setDiscoveryListener(this);
        for (URI uri : registered.values()) {
            try {
                agent.registerService(uri);
            } catch (IOException e) {
            }
        }
    }

    public Set<URI> getServices() {
        return new HashSet<URI>(services.values());
    }

    public void registerService(URI serviceUri) throws IOException {
        registered.put(serviceUri.toString(), serviceUri);
        for (DiscoveryAgent agent : agents) {
            agent.registerService(serviceUri);
        }
    }

    public void reportFailed(URI serviceUri) throws IOException {
        registered.remove(serviceUri.toString());
        for (DiscoveryAgent agent : agents) {
            agent.reportFailed(serviceUri);
        }
    }

    public void unregisterService(URI serviceUri) throws IOException {
        registered.remove(serviceUri.toString());
        for (DiscoveryAgent agent : agents) {
            agent.unregisterService(serviceUri);
        }
    }

    public void setDiscoveryListener(DiscoveryListener listener) {
        addDiscoveryListener(listener);
    }

    public void addDiscoveryListener(DiscoveryListener listener){
        // get the listener caught up
        for (URI service : services.values()) {
            executor.execute(new ServiceAddedTask(listener, service));
        }

        listeners.add(listener);
    }

    public void removeDiscoveryListener(DiscoveryListener listener){
        listeners.remove(listener);
    }


    public void serviceAdded(URI service) {
        services.put(service.toString(), service);
        for (final DiscoveryListener discoveryListener : getListeners()) {
            executor.execute(new ServiceAddedTask(discoveryListener, service));
        }
    }

    public void serviceRemoved(URI service) {
        services.remove(service.toString());
        for (final DiscoveryListener discoveryListener : getListeners()) {
            executor.execute(new ServiceRemovedTask(discoveryListener, service));
        }
    }

    List<DiscoveryListener> getListeners(){
        return Collections.unmodifiableList(listeners);
    }

    private abstract static class Task implements Runnable {
        protected final DiscoveryListener discoveryListener;
        protected final URI service;

        protected Task(DiscoveryListener discoveryListener, URI service) {
            this.discoveryListener = discoveryListener;
            this.service = service;
        }
    }

    private static class ServiceRemovedTask extends Task {
        public ServiceRemovedTask(DiscoveryListener discoveryListener, URI service) {
            super(discoveryListener, service);
        }

        public void run() {
            if (discoveryListener != null) {
                discoveryListener.serviceRemoved(service);
            }
        }
    }

    private static class ServiceAddedTask extends Task {
        public ServiceAddedTask(DiscoveryListener discoveryListener, URI service) {
            super(discoveryListener, service);
        }

        public void run() {
            if (discoveryListener != null) {
                discoveryListener.serviceAdded(service);
            }
        }
    }

    @Managed
    private class Monitor {

        @Managed
        public String[] getDiscovered() {
            final Set<String> set = DiscoveryRegistry.this.services.keySet();
            return set.toArray(new String[set.size()]);
        }

        @Managed
        public String[] getRegistered() {
            final Set<String> set = DiscoveryRegistry.this.registered.keySet();
            return set.toArray(new String[set.size()]);
        }

        @Managed
        public String[] getAgents() {
            List<String> list = new ArrayList<String>();
            for (DiscoveryAgent agent : DiscoveryRegistry.this.agents) {
                list.add(agent.getClass().getName());
            }
            return list.toArray(new String[list.size()]);
        }

    }
}
