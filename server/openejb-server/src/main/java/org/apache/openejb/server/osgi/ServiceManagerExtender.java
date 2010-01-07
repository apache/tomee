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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.DiscoveryRegistry;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceManager;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

/**
 * @version $Rev$ $Date$
 */
public class ServiceManagerExtender extends ServiceManager {

    static Logger logger = Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE, "org.apache.openejb.server.util.resources");

    private BundleContext bundleContext;
    private BundleTracker tracker;    
    private Map<Bundle, List<Service>> serverMap = new HashMap<Bundle, List<Service>>();
    private Boolean started;
    
    public ServiceManagerExtender(BundleContext bundleContext) {
        setServiceManager(this);
        
        this.bundleContext = bundleContext;
    }
        
    public void init() throws Exception {
        if (started != null) {
            throw new IllegalStateException("ServiceManager is already initialized");
        }
        DiscoveryRegistry registry = new DiscoveryRegistry();
        SystemInstance.get().setComponent(DiscoveryRegistry.class, registry);
        
        started = Boolean.FALSE;
        ServerServiceTracker t = new ServerServiceTracker();
        tracker = new BundleTracker(bundleContext, Bundle.ACTIVE | Bundle.STOPPING, t);
        tracker.open();       
    }
    
    public synchronized void start(boolean block) {
        if (started == null) {
            throw new IllegalStateException("ServiceManager not initialized");
        }
        started = Boolean.TRUE;
        for (Map.Entry<Bundle, List<Service>> entry : serverMap.entrySet()) {
            for (Service service : entry.getValue()) {
                service.start();
            }
        }
    }
    
    private synchronized void startServers(Bundle bundle, List<Service> services) {
        serverMap.put(bundle, services);
        if (started == Boolean.TRUE) { 
            for (Service service : services) {
                service.start();
            }
        }
    }
    
    protected void addedServers(Bundle bundle, Map<String, Properties> resources) {
        List<Service> services = new ArrayList<Service>();
        for (Map.Entry<String, Properties> entry : resources.entrySet()) {
            services.add(new Service(bundle, entry.getKey(), entry.getValue()));
        }
        startServers(bundle, services);
    }
        
    public synchronized void stop() {
        if (started == Boolean.TRUE) {
            started = Boolean.FALSE;
            for (Map.Entry<Bundle, List<Service>> entry : serverMap.entrySet()) {
                for (Service service : entry.getValue()) {
                    service.stop();
                }
            }
        }
    }
        
    protected synchronized void removedServers(Bundle bundle) {
        List<Service> services = serverMap.remove(bundle);
        if (services != null) {
            for (Service service : services) {
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
        
        private String name;
        private Properties description;
        private Bundle bundle;
        
        private ServiceRegistration registration;
        private ServerService server;
        
        public Service(Bundle bundle, String name, Properties description) {
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
            
                BundleContext context = bundle.getBundleContext();
                registration = context.registerService(ServerService.class.getName(), 
                                                       server, 
                                                       getServiceProperties());
            }
        }
        
        private Hashtable getServiceProperties() {
            Hashtable props = new Hashtable();
            for (Map.Entry<Object, Object> entry : description.entrySet()) {
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
                try { registration.unregister(); } catch (IllegalStateException ignore) {}
            }
        }
    }
    
    private class ServerServiceTracker implements BundleTrackerCustomizer {

        public Object addingBundle(Bundle bundle, BundleEvent event) {
            return scan(bundle);
        }

        public void modifiedBundle(Bundle bundle, BundleEvent event, Object arg2) {
        }

        public void removedBundle(Bundle bundle, BundleEvent event, Object arg2) {
            removedServers(bundle);
        }
        
        private Object scan(Bundle bundle) {
            String basePath = "/META-INF/" + ServerService.class.getName() + "/";
            Enumeration<URL> entries = bundle.findEntries(basePath, "*", false);
            if (entries != null) {             
                Map<String, Properties> resources = new HashMap<String, Properties>();
                while (entries.hasMoreElements()) {
                    URL entry = entries.nextElement();
                    String name = entry.getPath().substring(basePath.length());
                    try {
                        Properties props = loadProperties(entry);
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
        
        private void setClass(Properties properties, Bundle bundle, Class interfase) throws ClassNotFoundException {
            String className = properties.getProperty("className");
            if (className == null) {
                className = properties.getProperty("classname");
                if (className == null) {
                    className = properties.getProperty("server");
                }
            }

            Class impl = bundle.loadClass(className);
            properties.put(interfase, impl);
        }

        private void setRawProperties(Properties properties, URL entry) throws IOException {
            String rawProperties = readContents(entry);
            properties.put(Properties.class, rawProperties);
        }

        private Properties loadProperties(URL resource) throws IOException {
            InputStream in = resource.openStream();

            BufferedInputStream reader = null;
            try {
                reader = new BufferedInputStream(in);
                Properties properties = new Properties();
                properties.load(reader);

                return properties;
            } finally {
                try {
                    in.close();
                    reader.close();
                } catch (Exception e) {
                }
            }
        }
        
        private String readContents(URL resource) throws IOException {
            InputStream in = resource.openStream();
            BufferedInputStream reader = null;
            StringBuffer sb = new StringBuffer();

            try {
                reader = new BufferedInputStream(in);

                int b = reader.read();
                while (b != -1) {
                    sb.append((char) b);
                    b = reader.read();
                }

                return sb.toString().trim();
            } finally {
                try {
                    in.close();
                    reader.close();
                } catch (Exception e) {
                }
            }
        }

    }
}
