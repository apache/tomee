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
package org.apache.openejb.core.osgi.impl;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.OpenEJBInstance;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.ServiceManagerProxy;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class Activator implements BundleActivator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);
    private static final String SERVICE_MANAGER_NAME = "org.apache.openejb.server.ServiceManager";

    private OpenEJBInstance openejb;

    public void start(BundleContext context) throws Exception {
        LOGGER.info("Starting OpenEJB");

        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        openejb = new OpenEJBInstance();

        Properties env = new Properties();
        // env.setProperty("openejb.embedded", "true");
        // default, but to remember that the setting exists
        env.setProperty("openejb.loader", "context");
        // NPE
        env.setProperty("openejb.deployments.classpath", "false");
        env.setProperty("openejb.log.factory", "org.apache.openejb.util.JuliLogStreamFactory");

        SystemInstance.init(env);
        // OptionsLog.install();

        // OpenEJB.init(env);
        try {
            openejb.init(env);
        } catch (Exception e) {
            LOGGER.error("can't start the bundle", e);
            throw e;
        }

        // should be registered through openejb-server
        try {
            ServiceReference serviceManager = context.getServiceReference(SERVICE_MANAGER_NAME);
            invoke(serviceManager, "init");
            invoke(serviceManager, "start");
        } catch (Exception e) {
            LOGGER.error("can't start OpenEJB services", e);
        }

        LOGGER.info("Registering OSGified OpenEJB Deployer");
        context.addBundleListener(new Deployer());
    }

    private static void invoke(ServiceReference serviceManager, String name) throws OpenEJBException, InvocationTargetException, IllegalAccessException {
        if (serviceManager == null) {
            LOGGER.warn("can't invoke method {} since the service manager is null", name);
        }

        Class<?> current = serviceManager.getClass();
        Method mtd = null;
        while (mtd == null || !current.equals(Object.class)) {
            try {
                mtd = current.getDeclaredMethod(name);
            } catch (NoSuchMethodException e) {
                // ignored
            }
        }

        if (mtd == null) {
            throw new OpenEJBException("can't find method " + name + " on service " + serviceManager);
        }
        mtd.invoke(serviceManager);
    }

    public void stop(BundleContext context) throws Exception {
        LOGGER.info("Stopping OpenEJB; openejb.isInitialized(): " + openejb.isInitialized());

        // should be registered through openejb-server
        try {
            ServiceReference serviceManager = context.getServiceReference(SERVICE_MANAGER_NAME);
            invoke(serviceManager, "stop");
        } catch (Exception e) {
            LOGGER.error("can't stop OpenEJB services", e);
        }

        openejb = null;
        OpenEJB.destroy(); // todo: should it be static?
    }
}
