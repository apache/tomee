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
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ManagedMBean;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.apache.openejb.util.PropertyPlaceHolderHelper.holdsWithUpdate;

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

        for (Map.Entry<String, Properties> serviceInfo : availableServices.entrySet()) {
            final String serviceName = serviceInfo.getKey();
            if (!accept(serviceName)) {
                continue;
            }

            ServerService service = initServer(serviceName, serviceInfo.getValue());
            if (service != null) {
                enabledServers.add(service);
            }
        }

        return enabledServers;
    }

    protected ServerService initServer(String serviceName, Properties serviceProperties)
            throws IOException {

        DiscoveryRegistry registry = SystemInstance.get().getComponent(DiscoveryRegistry.class);

        OpenEjbConfiguration conf = SystemInstance.get().getComponent(OpenEjbConfiguration.class);

        logger.debug("Processing ServerService(id=" + serviceName + ")");

        overrideProperties(serviceName, serviceProperties);
        serviceProperties.setProperty("name", serviceName);

        if (conf != null && conf.facilities != null) {
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
            if (logger.isDebugEnabled()) {
                for (Map.Entry<Object, Object> entry : serviceProperties.entrySet()) {
                    logger.debug(entry.getKey() + " = " + entry.getValue());
                }
            }

            try {
                // Create Service
                ServerService service;

                ObjectRecipe recipe = new ObjectRecipe(serviceClass);
                try {
                    // Do not import.  This class is not available in xbean-reflect-3.3
                    if (org.apache.xbean.recipe.ReflectionUtil.findStaticFactory(serviceClass, "createServerService", null, null) != null) {
                        recipe = new ObjectRecipe(serviceClass, "createServerService");
                    }
                } catch (Throwable e) {
                    //Ignore
                }

                recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
                recipe.allow(Option.IGNORE_MISSING_PROPERTIES);

                service = (ServerService) recipe.create(serviceClass.getClassLoader());

                if (!(service instanceof SelfManaging)) {
                    service = manage(serviceName, serviceProperties, service);
                }

                service.init(serviceProperties);

                if (service instanceof DiscoveryAgent) {
                    DiscoveryAgent agent = (DiscoveryAgent) service;
                    registry.addDiscoveryAgent(agent);
                }

                if (LocalMBeanServer.isJMXActive()) {
                    MBeanServer server = LocalMBeanServer.get();

                    register(serviceName, service, server);
                }

                return service;
            } catch (Throwable t) {
                t.printStackTrace();
                logger.error("service.instantiation.err", t, serviceClass.getName(), t.getClass().getName(), t.getMessage());
            }
        }

        return null;
    }

    protected static ObjectName getObjectName(final String serviceName) {
        final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb");
        jmxName.set("type", "ServerService");
        jmxName.set("name", serviceName);
        return jmxName.build();
    }

    public static void register(String serviceName, ServerService service, MBeanServer server) {
        try {
            final ObjectName on = getObjectName(serviceName);
            if (server.isRegistered(on)) {
                server.unregisterMBean(on);
            }
            server.registerMBean(new ManagedMBean(service), on);
        } catch (Exception e) {
            logger.error("Unable to register MBean ", e);
        }
    }

    public static ServerService manage(String serviceName, Properties serviceProperties, ServerService service) {
        service = new NamedService(service, serviceName);
        service = new ServiceStats(service);
        service = new ServiceLogger(service);
        service = new ServicePool(service, serviceProperties);
        service = new ServiceAccessController(service);
        service = new ServiceDaemon(service);
        return service;
    }

    private void overrideProperties(String serviceName, Properties serviceProperties) throws IOException {
        final SystemInstance systemInstance = SystemInstance.get();
        final FileUtils base = systemInstance.getBase();

        // Override with file from conf dir
        final File conf = base.getDirectory("conf");
        if (conf.exists()) {

            final String legacy = System.getProperty("openejb.conf.schema.legacy");
            boolean legacySchema = Boolean.parseBoolean((null != legacy ? legacy : "false"));

            if (null == legacy) {
                //Legacy is not configured either way, so make an educated guess.
                //If we find at least 2 known service.properties files then assume legacy
                final File[] files = conf.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(final File dir, String name) {
                        name = name.toLowerCase();
                        return name.equals("ejbd.properties")
                                || name.equals("ejbds.properties")
                                || name.equals("admin.properties")
                                || name.equals("httpejbd.properties");
                    }
                });

                if (null != files && files.length > 1) {
                    legacySchema = true;
                }
            }

            addProperties(conf, legacySchema, new File(conf, serviceName + ".properties"), serviceProperties, true);
            addProperties(conf, legacySchema, new File(conf, SystemInstance.get().currentProfile() + "." + serviceName + ".properties"), serviceProperties, false);
        }

        holdsWithUpdate(serviceProperties);

        // Override with system properties
        final String prefix = serviceName + ".";
        final Properties sysProps = new Properties(System.getProperties());
        sysProps.putAll(systemInstance.getProperties());
        for (final Map.Entry<Object, Object> entry : sysProps.entrySet()) {
            final Map.Entry entry1 = (Map.Entry) entry;
            final Object value = entry1.getValue();
            String key = (String) entry1.getKey();
            if (value instanceof String && key.startsWith(prefix)) {
                key = key.replaceFirst(prefix, "");
                serviceProperties.setProperty(key, (String) value);
            }
        }

    }

    private void addProperties(final File conf, final boolean legacySchema, final File path, final Properties fullProps, final boolean tryToDump) throws IOException {
        File serviceConfig = path;
        if (!serviceConfig.exists()) {
            serviceConfig = new File(conf, (legacySchema ? "" : "conf.d/") + serviceConfig.getName());

            if (legacySchema) {
                logger.info("Using legacy configuration path for new service: " + serviceConfig);
            }
        }

        final Properties props = new Properties();

        if (serviceConfig.exists()) {
            IO.readProperties(serviceConfig, props);
        } else if (tryToDump) {

            final File confD = serviceConfig.getParentFile();

            if (!confD.exists() && !confD.mkdirs()) {
                logger.warning("Failed to create " + serviceConfig.getPath());
            }

            if (confD.exists()) {
                if (EnvProps.extractConfigurationFiles()) {

                    final String rawPropsContent = (String) fullProps.get(Properties.class);
                    if (rawPropsContent != null) {
                        IO.copy(IO.read(rawPropsContent), serviceConfig);
                    }

                } else {
                    props.put("disabled", "true");
                }
            }
        }

        fullProps.putAll(props);
    }

    public static boolean isEnabled(Properties props) {
        // if it should be started, continue
        String disabled = props.getProperty("disabled", "");

        return !(disabled.equalsIgnoreCase("yes") || disabled.equalsIgnoreCase("true"));
    }

    abstract public void init() throws Exception;

    public void start() throws ServiceException {
        start(true);
    }

    abstract public void start(boolean block) throws ServiceException;

    abstract public void stop() throws ServiceException;

}
