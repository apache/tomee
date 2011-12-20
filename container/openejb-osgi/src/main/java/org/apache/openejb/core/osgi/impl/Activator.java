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
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
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
    private static final long TRACKER_TIMEOUT = 60000;

    private OpenEJBInstance openejb;
    private Object serviceManager;

    public void start(BundleContext context) throws Exception {
        LOGGER.info("Starting OpenEJB for bundle #{}", context.getBundle().getBundleId());

        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        openejb = new OpenEJBInstance();

        Properties env = new Properties();
        // env.setProperty("openejb.embedded", "true");
        // default, but to remember that the setting exists
        env.setProperty("openejb.loader", "context");
        env.setProperty("openejb.deployments.classpath", "false");

        SystemInstance.init(env);
        // OptionsLog.install();

        try {
            openejb.init(env);
        } catch (Exception e) {
            LOGGER.error("can't start the bundle", e);
            throw e;
        }

        // should be registered through openejb-server
        ServiceTracker serviceManagerTracker = null;
        try {
            serviceManagerTracker = getServiceManger(context);
            serviceManager = serviceManagerTracker.getService();
            if (serviceManager == null) {
                LOGGER.warn("can't find service manager");
            }

            invoke(serviceManager, "init");
            invoke(serviceManager, "start");
        } catch (InterruptedException ie) {
            LOGGER.warn("can't find service manager");
        } catch (Exception e) {
            LOGGER.error("can't start OpenEJB services", e);
        } finally {
            if (serviceManagerTracker != null) {
                serviceManagerTracker.close();
            }
        }

        LOGGER.info("Registering OSGified OpenEJB Deployer");
        context.addBundleListener(new Deployer());
    }

    private static ServiceTracker getServiceManger(BundleContext context) throws InterruptedException {
        ServiceTracker serviceManagerTracker = new ServiceTracker(context, SERVICE_MANAGER_NAME, null);
        serviceManagerTracker.open();
        serviceManagerTracker.waitForService(TRACKER_TIMEOUT);
        return serviceManagerTracker;
    }

    public void stop(BundleContext context) throws Exception {
        LOGGER.info("Stopping OpenEJB; openejb.isInitialized(): " + openejb.isInitialized());

        try {
            invoke(serviceManager, "stop");
        } catch (Exception e) {
            LOGGER.error("can't stop OpenEJB services", e);
        }

        openejb = null;
        OpenEJB.destroy(); // todo: should it be static?
    }

    private static void invoke(Object serviceManager, String name) throws OpenEJBException, InvocationTargetException, IllegalAccessException {
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
            current = current.getSuperclass();
        }

        if (mtd == null) {
            throw new OpenEJBException("can't find method " + name + " on service " + serviceManager);
        }
        mtd.invoke(serviceManager);
    }
}
