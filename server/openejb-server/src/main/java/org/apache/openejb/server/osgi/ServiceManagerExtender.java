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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.osgi;

import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.DiscoveryRegistry;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ServiceManager;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class ServiceManagerExtender extends ServiceManager {

    static Logger logger = Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE, "org.apache.openejb.server.util.resources");

    private final BundleContext bundleContext;
    private BundleTracker tracker;
    private final Map<Bundle, List<Service>> serverMap = new HashMap<>();
    private Boolean started;
    private volatile boolean stopped = false;

    public ServiceManagerExtender(final BundleContext bundleContext) {
        setServiceManager(this);

        this.bundleContext = bundleContext;
    }

    @Override
    public void init() throws Exception {
        if (started != null && started.equals(Boolean.TRUE)) {
            throw new IllegalStateException("ServiceManager is already initialized");
        }
        final DiscoveryRegistry registry = new DiscoveryRegistry();
        SystemInstance.get().setComponent(DiscoveryRegistry.class, registry);

        started = Boolean.FALSE;
        stopped = false;
        final ServerServiceTracker t = new ServerServiceTracker();
        tracker = new BundleTracker(bundleContext, Bundle.ACTIVE | Bundle.STOPPING, t);
        tracker.open();
    }

    @Override
    public synchronized void start(final boolean block) throws ServiceException {

        //This implementaion ignores block

        if (started == null) {
            throw new ServiceException("ServiceManager not initialized");
        }
        if (stopped) {
            throw new ServiceException("ServiceManager has already been stopped");
        }

        started = Boolean.TRUE;
        for (final Map.Entry<Bundle, List<Service>> entry : serverMap.entrySet()) {
            for (final Service service : entry.getValue()) {
                service.start();
            }
        }
    }

    private synchronized void startServers(final Bundle bundle, final List<Service> services) {
        serverMap.put(bundle, services);
        if (started == Boolean.TRUE) {
            for (final Service service : services) {
                service.start();
            }
        }
    }

    protected void addedServers(final Bundle bundle, final Map<String, Properties> resources) {
        final List<Service> services = new ArrayList<>();
        for (final Map.Entry<String, Properties> entry : resources.entrySet()) {
            services.add(new Service(bundle, entry.getKey(), entry.getValue()));
        }
        startServers(bundle, services);
    }

    @Override
    public synchronized void stop() {
        if (started == Boolean.TRUE) {
            started = Boolean.FALSE;
            for (final Map.Entry<Bundle, List<Service>> entry : serverMap.entrySet()) {
                for (final Service service : entry.getValue()) {
                    service.stop();
                }
            }
        }
    }

    protected synchronized void removedServers(final Bundle bundle) {
        final List<Service> services = serverMap.remove(bundle);
        if (services != null) {
            for (final Service service : services) {
                service.stop();
            }
        }
    }

    protected void shutdown() {
        if (tracker != null) {
            tracker.close();
        }
        stop();
    }

    private class Service {

        private final String name;
        private final Properties description;
        private final Bundle bundle;

        private ServiceRegistration registration;
        private ServerService server;

        public Service(final Bundle bundle, final String name, final Properties description) {
            this.bundle = bundle;
            this.name = name;
            this.description = description;
        }

        public void start() {
            try {
                server = initServer(name, description);
            } catch (IOException e) {
                logger.error("Error initializing " + name + " service.", e);
            }

            if (server != null) {
                try {
                    server.start();
                } catch (Exception e) {
                    logger.error("Service Start Failed: " + name + " " + server.getIP() + " " + server.getPort() + ". Exception: " + e.getMessage(), e);
                }

                final BundleContext context = bundle.getBundleContext();
                registration = context.registerService(ServerService.class.getName(),
                    server,
                    getServiceProperties());
            }
        }

        @SuppressWarnings({"UseOfObsoleteCollectionType", "unchecked"})
        private Hashtable getServiceProperties() {
            final Hashtable props = new Hashtable();
            for (final Map.Entry<Object, Object> entry : description.entrySet()) {
                if (entry.getKey() instanceof String) {
                    props.put(entry.getKey(), entry.getValue());
                }
            }
            return props;
        }

        public void stop() {
            if (server != null) {
                try {
                    server.stop();
                } catch (Exception e) {
                    logger.warning("Service Shutdown Failed: " + name + ". Exception: " + e.getMessage(), e);
                }
            }
            if (registration != null) {
                try {
                    registration.unregister();
                } catch (IllegalStateException ignore) {
                }
            }
        }
    }

    private class ServerServiceTracker implements BundleTrackerCustomizer {

        @Override
        public Object addingBundle(final Bundle bundle, final BundleEvent event) {
            return scan(bundle);
        }

        @Override
        public void modifiedBundle(final Bundle bundle, final BundleEvent event, final Object arg2) {
        }

        @Override
        public void removedBundle(final Bundle bundle, final BundleEvent event, final Object arg2) {
            removedServers(bundle);
        }

        @SuppressWarnings("unchecked")
        private Object scan(final Bundle bundle) {
            final String basePath = "/META-INF/" + ServerService.class.getName() + "/";
            final Enumeration<URL> entries = bundle.findEntries(basePath, "*", false);
            if (entries != null) {
                final Map<String, Properties> resources = new HashMap<>();
                while (entries.hasMoreElements()) {
                    final URL entry = entries.nextElement();
                    final String name = entry.getPath().substring(basePath.length());
                    try {
                        final Properties props = loadProperties(entry);
                        setClass(props, bundle, ServerService.class);
                        setRawProperties(props, entry);
                        resources.put(name, props);
                    } catch (Exception e) {
                        logger.error("Error loading " + name + " properties", e);
                    }
                }
                addedServers(bundle, resources);
                return bundle;
            }
            return null;
        }

        private void setClass(final Properties properties, final Bundle bundle, final Class interfase) throws ClassNotFoundException {
            String className = properties.getProperty("className");
            if (className == null) {
                className = properties.getProperty("classname");
                if (className == null) {
                    className = properties.getProperty("server");
                }
            }

            final Class impl = bundle.loadClass(className);
            properties.put(interfase, impl);
        }

        private void setRawProperties(final Properties properties, final URL entry) throws IOException {
            final String rawProperties = readContents(entry);
            properties.put(Properties.class, rawProperties);
        }

        private Properties loadProperties(final URL resource) throws IOException {
            return IO.readProperties(resource);
        }

        private String readContents(final URL resource) throws IOException {
            return IO.slurp(resource);
        }

    }
}
