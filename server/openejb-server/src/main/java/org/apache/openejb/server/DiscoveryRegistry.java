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

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

/**
 * @version $Rev$ $Date$
 */
public class DiscoveryRegistry implements DiscoveryListener, DiscoveryAgent {

    private final List<DiscoveryAgent> agents = new CopyOnWriteArrayList<>();
    private final List<DiscoveryListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, URI> services = new ConcurrentHashMap<>();
    private final Map<String, URI> registered = new ConcurrentHashMap<>();

    @Managed
    private final Monitor monitor = new Monitor();

    private final ThreadPoolExecutor executor;

    public DiscoveryRegistry() {
        this(null);
    }

    public DiscoveryRegistry(final DiscoveryAgent agent) {

        executor = new ThreadPoolExecutor(1, 10, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1), new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable runable) {
                final Thread t = new Thread(runable, DiscoveryRegistry.class.getSimpleName());
                t.setDaemon(true);
                return t;
            }
        });

        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(final Runnable r, final ThreadPoolExecutor tpe) {
                if (null == r || null == tpe || tpe.isShutdown() || tpe.isTerminated() || tpe.isTerminating()) {
                    return;
                }

                try {
                    tpe.getQueue().offer(r, 20, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    //Ignore
                }
            }
        });

        SystemInstance.get().setComponent(DiscoveryRegistry.class, this);
        SystemInstance.get().setComponent(DiscoveryAgent.class, this);

        if (null != agent) {
            addDiscoveryAgent(agent);
        }
    }

    public void addDiscoveryAgent(final DiscoveryAgent agent) {
        agents.add(agent);
        agent.setDiscoveryListener(this);
        for (final URI uri : registered.values()) {
            try {
                agent.registerService(uri);
            } catch (Exception e) {
                //Ignore
            }
        }
    }

    public Set<URI> getServices() {
        return new HashSet<>(services.values());
    }

    @Override
    public void registerService(final URI serviceUri) throws IOException {
        registered.put(serviceUri.toString(), serviceUri);
        for (final DiscoveryAgent agent : agents) {
            agent.registerService(serviceUri);
        }
    }

    @Override
    public void reportFailed(final URI serviceUri) throws IOException {
        registered.remove(serviceUri.toString());
        for (final DiscoveryAgent agent : agents) {
            agent.reportFailed(serviceUri);
        }
    }

    @Override
    public void unregisterService(final URI serviceUri) throws IOException {
        registered.remove(serviceUri.toString());
        for (final DiscoveryAgent agent : agents) {
            agent.unregisterService(serviceUri);
        }
    }

    @Override
    public void setDiscoveryListener(final DiscoveryListener listener) {
        addDiscoveryListener(listener);
    }

    public void addDiscoveryListener(final DiscoveryListener listener) {
        // get the listener caught up
        for (final URI service : services.values()) {
            executor.execute(new ServiceAddedTask(listener, service));
        }

        listeners.add(listener);
    }

    public void removeDiscoveryListener(final DiscoveryListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void serviceAdded(final URI service) {
        services.put(service.toString(), service);
        for (final DiscoveryListener discoveryListener : getListeners()) {
            executor.execute(new ServiceAddedTask(discoveryListener, service));
        }
    }

    @Override
    public void serviceRemoved(final URI service) {
        services.remove(service.toString());
        for (final DiscoveryListener discoveryListener : getListeners()) {
            executor.execute(new ServiceRemovedTask(discoveryListener, service));
        }
    }

    List<DiscoveryListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    private abstract static class Task implements Runnable {

        protected final DiscoveryListener discoveryListener;
        protected final URI service;

        protected Task(final DiscoveryListener discoveryListener, final URI service) {
            this.discoveryListener = discoveryListener;
            this.service = service;
        }
    }

    private static class ServiceRemovedTask extends Task {

        public ServiceRemovedTask(final DiscoveryListener discoveryListener, final URI service) {
            super(discoveryListener, service);
        }

        @Override
        public void run() {
            if (discoveryListener != null) {
                discoveryListener.serviceRemoved(service);
            }
        }
    }

    private static class ServiceAddedTask extends Task {

        public ServiceAddedTask(final DiscoveryListener discoveryListener, final URI service) {
            super(discoveryListener, service);
        }

        @Override
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
            final List<String> list = new ArrayList<>();
            for (final DiscoveryAgent agent : DiscoveryRegistry.this.agents) {
                list.add(agent.getClass().getName());
            }
            return list.toArray(new String[list.size()]);
        }

    }
}
