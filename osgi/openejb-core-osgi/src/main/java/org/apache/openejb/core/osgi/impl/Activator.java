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
import org.apache.openejb.cdi.CdiScanner;
import org.apache.openejb.cdi.OptimizedLoaderService;
import org.apache.openejb.loader.OpenEJBInstance;
import org.apache.openejb.loader.SystemInstance;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.TransactionManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class Activator implements BundleActivator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);
    private static final String SERVICE_MANAGER_NAME = "org.apache.openejb.server.ServiceManager";
    private static final long TRACKER_TIMEOUT = SystemInstance.get().getOptions().get("openejb.osgi.tracker.timeout", 30);

    public static final String OPENEJB_OSGI_NATIVE_TRANSACTION_MANAGER = "openejb.osgi.native-transaction-manager";

    private OpenEJBInstance openejb;
    private Object serviceManager;

    @Override
    public void start(final BundleContext context) throws Exception {
        LOGGER.info("Starting OpenEJB for bundle #{}", context.getBundle().getBundleId());

        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        openejb = new OpenEJBInstance();
        OpenEJBBundleContextHolder.set(context);

        final Properties env = new Properties();
        // env.setProperty("openejb.embedded", "true");
        // default, but to remember that the setting exists
        env.setProperty("openejb.loader", "context");
        env.setProperty("openejb.deployments.classpath", "false");
        env.setProperty("openejb.nobanner", "true");
        env.setProperty("openejb.classloader.forced-skip", "org.slf4j"); // otherwise we'll get a ClassNotFound on org.slf4j.impl.StaticLoggerBinder
        env.setProperty("openejb.modulename.useHash", "true");
        env.setProperty(CdiScanner.OPENEJB_CDI_FILTER_CLASSLOADER, "false"); // TODO: improve it reactivating it

        SystemInstance.init(env);
        // OptionsLog.install();
        if (!SystemInstance.get().getOptions().get(OPENEJB_OSGI_NATIVE_TRANSACTION_MANAGER, false)) {
            final ServiceReference sr = context.getServiceReference(TransactionManager.class.getName());
            if (sr != null) {
                final TransactionManager txMgr = (TransactionManager) context.getService(sr);
                if (txMgr != null) {
                    SystemInstance.get().setComponent(TransactionManager.class, txMgr);
                } else {
                    LOGGER.info("Using standard OpenEJB Transaction Manager (but found a service reference on an OSGi one)");
                }
            } else {
                LOGGER.info("Using standard OpenEJB Transaction Manager");
            }
        }

        try {
            openejb.init(env);
        } catch (Exception e) {
            LOGGER.error("can't start the bundle", e);
            throw e;
        }

        // should be registered through openejb-server
        checkServiceManager(context);

        context.addBundleListener(new Deployer(this));
    }

    public synchronized void checkServiceManager(final BundleContext context) {
        if (serviceManager != null) { // already started
            return;
        }

        ServiceTracker serviceManagerTracker = null;
        try {
            serviceManagerTracker = getServiceManager(context);
            serviceManager = serviceManagerTracker.getService();
            if (serviceManager == null) {
                LOGGER.warn("can't find service manager");
                return;
            }

            invoke(serviceManager, "init");
            invoke(serviceManager, "start");
        } catch (InterruptedException ie) {
            LOGGER.warn("can't find service manager");
        } catch (Exception e) {
            LOGGER.error("can't start OpenEJB services");
        } finally {
            if (serviceManagerTracker != null) {
                serviceManagerTracker.close();
            }
        }
    }


    private static ServiceTracker getServiceManager(final BundleContext context) throws InterruptedException {
        final ServiceTracker serviceManagerTracker = new ServiceTracker(context, SERVICE_MANAGER_NAME, null);
        serviceManagerTracker.open();
        serviceManagerTracker.waitForService(TRACKER_TIMEOUT);
        return serviceManagerTracker;
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        LOGGER.info("Stopping OpenEJB");

        try {
            invoke(serviceManager, "stop");
        } catch (Exception e) {
            LOGGER.error("can't stop OpenEJB services");
        }

        openejb = null;
        SystemInstance.reset();
        OpenEJB.destroy();
    }

    private static void invoke(final Object serviceManager, final String name) throws OpenEJBException, InvocationTargetException, IllegalAccessException {
        if (serviceManager == null) {
            LOGGER.warn("can't invoke method {} since the service manager is null", name);
            return;
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
