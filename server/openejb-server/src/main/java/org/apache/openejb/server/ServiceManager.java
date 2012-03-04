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
package org.apache.openejb.server;

import org.apache.openejb.EnvProps;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 * @org.apache.xbean.XBean element="serviceManager"
 */
public abstract class ServiceManager {

    static Logger logger = Logger.getInstance(LogCategory.OPENEJB_SERVER, "org.apache.openejb.server.util.resources");

    private static ServiceManager manager;

    public ServiceManager() {
    }

    public static ServiceManager getManager() {
        if (manager == null) {
            manager = new SimpleServiceManager();
        }

        return manager;
    }

    public static ServiceManager get() {
        return manager;
    }

    protected static void setServiceManager(ServiceManager newManager) {
        manager = newManager;
    }

    protected boolean accept(final String serviceName) {
        return true;
    }

    protected List<ServerService> initServers(Map<String, Properties> availableServices)
        throws IOException {
        List<ServerService> enabledServers = new ArrayList<ServerService>();

        for (Iterator iterator = availableServices.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String serviceName = (String) entry.getKey();
            Properties serviceProperties = (Properties) entry.getValue();
            
            ServerService service = initServer(serviceName, serviceProperties);
            if (service != null && accept(service.getName())) {
                enabledServers.add(service);
            }
        }
        
        return enabledServers;
    }
    
    protected ServerService initServer(String serviceName, Properties serviceProperties) 
        throws IOException {

        DiscoveryRegistry registry = SystemInstance.get().getComponent(DiscoveryRegistry.class);
        
        OpenEjbConfiguration conf = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
        
        logger.debug("Processing ServerService(id="+serviceName+")");

        overrideProperties(serviceName, serviceProperties);
        serviceProperties.setProperty("name", serviceName);

        if (conf != null && conf.facilities != null){
            ServiceInfo info = new ServiceInfo();
            info.className = ((Class) serviceProperties.get(ServerService.class)).getName();
            info.service = "ServerService";
            info.id = serviceName;
            info.properties = serviceProperties;
            conf.facilities.services.add(info);
        }

        boolean enabled = isEnabled(serviceProperties);

        logger.debug("Found ServerService(id=" + serviceName + ", disabled=" + (!enabled) + ")");

        if (enabled) {

            Class serviceClass = (Class) serviceProperties.get(ServerService.class);

            logger.info("Creating ServerService(id=" + serviceName + ")");

            // log all properties on debug
            if (logger.isDebugEnabled()){
                for (Map.Entry<Object, Object> entry : serviceProperties.entrySet()) {
                    logger.debug(entry.getKey() +" = "+ entry.getValue());
                }
            }

            try {
                // Create Service
                ServerService service;

                ObjectRecipe recipe = new ObjectRecipe(serviceClass);
                try {
                    // Do not import.  This class is not available in xbean-reflect-3.3
                    if (org.apache.xbean.recipe.ReflectionUtil.findStaticFactory(serviceClass, "createServerService", null, null) != null){
                        recipe = new ObjectRecipe(serviceClass, "createServerService");
                    }
                } catch (Throwable e) {
                }

                recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
                recipe.allow(Option.IGNORE_MISSING_PROPERTIES);

                service = (ServerService) recipe.create(serviceClass.getClassLoader());

                if (!(service instanceof SelfManaging)) {
                    service = new ServicePool(service, serviceName, serviceProperties);
                    service = new ServiceLogger(service);
                    service = new ServiceAccessController(service);
                    service = new ServiceDaemon(service);
                }

                service.init(serviceProperties);
                    
                if (service instanceof DiscoveryAgent){
                    DiscoveryAgent agent = (DiscoveryAgent) service;
                    registry.addDiscoveryAgent(agent);
                }

                return service;
            } catch (Throwable t) {
                logger.error("service.instantiation.err", t, serviceClass.getName(), t.getClass().getName(), t.getMessage());
            }
        }
       
        return null;
    }

    private void overrideProperties(String serviceName, Properties serviceProperties) throws IOException {
        final FileUtils base = SystemInstance.get().getBase();

        // Override with file from conf dir
        final File conf = base.getDirectory("conf");
        if (conf.exists()) {
            File serviceConfig = new File(conf, serviceName + ".properties");
            if (!serviceConfig.exists()) {
                serviceConfig = new File(conf, "conf.d/" + serviceConfig.getName()); // name was already built so use it
            }
            if (serviceConfig.exists()) {
                IO.readProperties(serviceConfig, serviceProperties);
            } else {
                final File confD = serviceConfig.getParentFile();
                if (!confD.exists() && !confD.mkdirs()) {
                    logger.warning("can't create " + serviceConfig.getPath());
                }

                if (confD.exists()) {
                    if (EnvProps.extractConfigurationFiles()) {

                        final String rawPropsContent = (String) serviceProperties.get(Properties.class);
                        IO.copy(IO.read(rawPropsContent), serviceConfig);

                    } else {
                        serviceProperties.put("disabled", "true");
                    }
                }
            }
        }

        // Override with system properties
        final String prefix = serviceName + ".";
        final Properties sysProps = new Properties(System.getProperties());
        sysProps.putAll(SystemInstance.get().getProperties());
        for (Iterator iterator1 = sysProps.entrySet().iterator(); iterator1.hasNext();) {
            final Map.Entry entry1 = (Map.Entry) iterator1.next();
            final Object value = entry1.getValue();
            String key = (String) entry1.getKey();
            if (value instanceof String && key.startsWith(prefix)) {
                key = key.replaceFirst(prefix, "");
                serviceProperties.setProperty(key, (String) value);
            }
        }

    }

    private boolean isEnabled(Properties props) {
        // if it should be started, continue
        String disabled = props.getProperty("disabled", "");

        if (disabled.equalsIgnoreCase("yes") || disabled.equalsIgnoreCase("true")) {
            return false;
        } else {
            return true;
        }
    }

    abstract public void init() throws Exception;
    
    public void start() throws ServiceException {
        start(true);
    }

    abstract public void start(boolean block) throws ServiceException;

    abstract public void stop() throws ServiceException;
    
}