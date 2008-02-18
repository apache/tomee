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

import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.xbean.finder.ResourceFinder;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.apache.xbean.recipe.MissingFactoryMethodException;

import javax.naming.NamingException;
import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.resource.spi.ResourceAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 * @org.apache.xbean.XBean element="serviceManager"
 */
public class ServiceManager {

    static Messages messages = new Messages("org.apache.openejb.server.util.resources");
    static Logger logger = Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE, "org.apache.openejb.server.util.resources");

    private static ServiceManager manager;

    private static ServerService[] daemons;

    private boolean stop = false;

    public ServiceManager() {
    }

    public static ServiceManager getManager() {
        if (manager == null) {
            manager = new ServiceManager();
        }

        return manager;
    }

    // Have properties files (like xinet.d) that specifies what daemons to
    // Look into the xinet.d file structure again
    // conf/server.d/
    //    admin.properties
    //    ejbd.properties
    //    webadmin.properties
    //    telnet.properties
    //    corba.properties
    //    soap.properties
    //    xmlrpc.properties
    //    httpejb.properties
    //    webejb.properties
    //    xmlejb.properties
    // Each contains the class name of the daemon implamentation
    // The port to use
    // whether it's turned on

    // May be reusable elsewhere, move if another use occurs
    public static class ServiceFinder {
        private final ResourceFinder resourceFinder;
        private ClassLoader classLoader;

        public ServiceFinder(String basePath) {
            this(basePath, Thread.currentThread().getContextClassLoader());
        }

        public ServiceFinder(String basePath, ClassLoader classLoader) {
            this.resourceFinder = new ResourceFinder(basePath, classLoader);
            this.classLoader = classLoader;
        }

        public Map mapAvailableServices(Class interfase) throws IOException, ClassNotFoundException {
            Map services = resourceFinder.mapAvailableProperties(ServerService.class.getName());

            for (Iterator iterator = services.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String name = (String) entry.getKey();
                Properties properties = (Properties) entry.getValue();

                String className = properties.getProperty("className");
                if (className == null) {
                    className = properties.getProperty("classname");
                    if (className == null) {
                        className = properties.getProperty("server");
                    }
                }

                Class impl = classLoader.loadClass(className);

                properties.put(interfase, impl);
                String rawProperties = resourceFinder.findString(interfase.getName() + "/" + name);
                properties.put(Properties.class, rawProperties);

            }
            return services;
        }
    }

    public void init() throws Exception {
        try {
            org.apache.log4j.MDC.put("SERVER", "main");
            InetAddress localhost = InetAddress.getLocalHost();
            org.apache.log4j.MDC.put("HOST", localhost.getHostName());
        } catch (Throwable e) {
        }

        ServiceFinder serviceFinder = new ServiceFinder("META-INF/");

        Map availableServices = serviceFinder.mapAvailableServices(ServerService.class);
        List enabledServers = new ArrayList();

        OpenEjbConfiguration conf = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
        for (Iterator iterator = availableServices.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String serviceName = (String) entry.getKey();
            Properties serviceProperties = (Properties) entry.getValue();

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

                logger.debug("Creating ServerService(id=" + serviceName + ")");

                try {
                    // Create Service
                    ServerService service = null;


                    ObjectRecipe recipe = new ObjectRecipe(serviceClass);
                    try {
                        if (recipe.findFactoryMethod(serviceClass, "createServerService") != null){
                            recipe = new ObjectRecipe(serviceClass, "createServerService");
                        }
                    } catch (MissingFactoryMethodException e) {
                    }

                    recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
                    recipe.allow(Option.IGNORE_MISSING_PROPERTIES);

                    service = (ServerService) recipe.create(serviceClass.getClassLoader());

                    if (!(service instanceof SelfManaging)) {
                        service = new ServiceLogger(service);
                        service = new ServiceAccessController(service);
                        service = new ServiceDaemon(service);
                    }

                    service.init(serviceProperties);
                    enabledServers.add(service);
                } catch (Throwable t) {
                    logger.error("service.instantiation.err", t, serviceClass.getName(), t.getClass().getName(), t.getMessage());
                }
            }

        }

        daemons = (ServerService[]) enabledServers.toArray(new ServerService[]{});
    }

    private void overrideProperties(String serviceName, Properties serviceProperties) throws IOException {
        FileUtils base = SystemInstance.get().getBase();

        // Override with file from conf dir
        File conf = base.getDirectory("conf");
        if (conf.exists()) {
            File serviceConfig = new File(conf, serviceName + ".properties");
            if (serviceConfig.exists()) {
                FileInputStream in = new FileInputStream(serviceConfig);
                try {
                    serviceProperties.load(in);
                } finally {
                    in.close();
                }
            } else {
                FileOutputStream out = new FileOutputStream(serviceConfig);
                try {
                    String rawPropsContent = (String) serviceProperties.get(Properties.class);
                    out.write(rawPropsContent.getBytes());
                } finally {
                    out.close();
                }
            }
        }

        // Override with system properties
        String prefix = serviceName + ".";
        Properties sysProps = new Properties(System.getProperties());
        sysProps.putAll(SystemInstance.get().getProperties());
        for (Iterator iterator1 = sysProps.entrySet().iterator(); iterator1.hasNext();) {
            Map.Entry entry1 = (Map.Entry) iterator1.next();
            String key = (String) entry1.getKey();
            Object value = entry1.getValue();
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

    public synchronized void start() throws ServiceException {
        start(true);
    }

    public synchronized void start(boolean block) throws ServiceException {
        boolean display = System.getProperty("openejb.nobanner") == null;

        if (display) {
            System.out.println("  ** Starting Services **");
            printRow("NAME", "IP", "PORT");
        }

        for (int i = 0; i < daemons.length; i++) {
            ServerService d = daemons[i];
            try {
                d.start();
                if (display) {
                    printRow(d.getName(), d.getIP(), d.getPort() + "");
                }
            } catch (Exception e) {
                logger.fatal("Service Start Failed: "+d.getName() + " " + d.getIP() + " " + d.getPort() + ": " + e.getMessage());
                if (display) {
                    printRow(d.getName(), "----", "FAILED");
                }
            }
        }
        if (display) {
            System.out.println("-------");
            System.out.println("Ready!");
        }
        if (!block) return;

        /*
        * This will cause the user thread (the thread that keeps the
        *  vm alive) to go into a state of constant waiting.
        *  Each time the thread is woken up, it checks to see if
        *  it should continue waiting.
        *
        *  To stop the thread (and the VM), just call the stop method
        *  which will set 'stop' to true and notify the user thread.
        */
        try {
            while (!stop) {

                this.wait(Long.MAX_VALUE);
            }
        } catch (Throwable t) {
            logger.fatal("Unable to keep the server thread alive. Received exception: " + t.getClass().getName() + " : " + t.getMessage());
        }
        logger.info("Stopping Remote Server");
    }

    public synchronized void stop() throws ServiceException {
        logger.info("Received stop signal");
        stop = true;

        try {
            ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            NamingEnumeration<Binding> namingEnumeration = null;
            try {
                namingEnumeration = containerSystem.getJNDIContext().listBindings("java:openejb/resourceAdapter");
            } catch (NamingException ignored) {
                // no resource adapters were created
            }
            while (namingEnumeration != null && namingEnumeration.hasMoreElements()) {
                Binding binding = namingEnumeration.nextElement();
                Object object = binding.getObject();
                ResourceAdapter resourceAdapter = (ResourceAdapter) object;
                try {
                    resourceAdapter.stop();
                } catch (Exception e) {
                    logger.fatal("ResourceAdapter Shutdown Failed: "+binding.getName(), e);
                }
            }
        } catch (Throwable e) {
            logger.fatal("Unable to get ResourceAdapters from JNDI.  Stop must be called on them for proper vm shutdown.", e);
        }

        for (int i = 0; i < daemons.length; i++) {
            try {
                daemons[i].stop();
            } catch (ServiceException e) {
                logger.fatal("Service Shutdown Failed: "+daemons[i].getName()+".  Exception: "+e.getMessage(), e);
            }
        }
        notifyAll();
    }

    private void printRow(String col1, String col2, String col3) {

        col1 += "                    ";
        col1 = col1.substring(0, 20);

        col2 += "                    ";
        col2 = col2.substring(0, 15);

        col3 += "                    ";
        col3 = col3.substring(0, 6);

        StringBuffer sb = new StringBuffer(50);
        sb.append("  ").append(col1);
        sb.append(" ").append(col2);
        sb.append(" ").append(col3);

        System.out.println(sb.toString());
    }
}
