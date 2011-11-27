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

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.loader.SystemInstance;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class Deployer implements BundleListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Deployer.class);

    public void bundleChanged(BundleEvent event) {
        switch (event.getType()) {
            case BundleEvent.STARTED:
                deploy(event.getBundle());
                break;
            case BundleEvent.STOPPED:
                undeploy(event.getBundle());
                break;
        }
    }

    private void deploy(Bundle bundle) {
        final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(new OSGIClassLoader(bundle));

        LOGGER.info(String.format("[Deployer] Bundle %s has been started", bundle.getSymbolicName()));

        LOGGER.info(String.format("[Deployer] Checking whether it's an EJB module"));
        try {
            Enumeration<URL> e = bundle.findEntries("META-INF", "ejb-jar.xml", false);
            if (e != null && e.hasMoreElements()) {
                URL ejbJarUrl = e.nextElement();

                LOGGER.info("[Deployer] It's an EJB module: " + ejbJarUrl);

                LOGGER.info("[Deployer] Deploying onto OpenEJB");

                String location = bundle.getLocation();
                LOGGER.info("[Deployer] bundle location: " + location);
                try {
                    File file = new File(new URL(location).getFile());
                    try {
                        DeploymentLoader deploymentLoader = new DeploymentLoader();
                        AppModule appModule = deploymentLoader.load(file);

                        ConfigurationFactory configurationFactory = new ConfigurationFactory();
                        AppInfo appInfo = configurationFactory.configureApplication(appModule);

                        Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
                        LOGGER.debug("Assembler : " + assembler);
                        LOGGER.debug("AppInfo id: " + appInfo.appId);
                        assembler.createApplication(appInfo);

                        LOGGER.info("[Deployer] Application deployed: " + appInfo.path);

                        registerService(bundle, appInfo);

                    } catch (Exception ex) {
                        LOGGER.error("can't deploy " + ejbJarUrl.toExternalForm(), ex);
                    }
                } catch (Exception ex1) {
                    LOGGER.error("can't deploy " + ejbJarUrl.toExternalForm(), ex1);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    private void undeploy(Bundle bundle) {
        LOGGER.info(String.format("[Deployer] Bundle %s has been stopped", bundle.getSymbolicName()));

        // Let's others finish what needs to be here
        // It should leave openejb in the state as if the ejb had not been deployed at all

        // Step 1. Check whether it's an ejb (cf. deploy method above)

        // Step 2. Unregister a service (cf. deploy method above)
    }

    /**
     * Register OSGi Service for EJB so calling the service will actually call the EJB
     *
     * @param bundle
     * @param appInfo
     */
    private void registerService(Bundle bundle, AppInfo appInfo) {
        LOGGER.info("[Deployer] Registering a service for the EJB");
        BundleContext context = bundle.getBundleContext();
        for (EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
            for (EnterpriseBeanInfo ejbInfo : ejbJarInfo.enterpriseBeans) {
                try {
                    context.registerService(ejbInfo.businessRemote.toArray(new String[ejbInfo.businessRemote.size()]), bundle.loadClass(
                        ejbInfo.ejbClass).newInstance(), new Properties());
                    LOGGER.info(String.format(
                        "[Deployer] Service object %s registered under the class names: %s", ejbInfo.ejbClass,
                        ejbInfo.businessRemote));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class OSGIClassLoader extends ClassLoader {
        private final Bundle backingBundle;

        public OSGIClassLoader(Bundle bundle) {
            super(null);
            backingBundle = bundle;
        }

        protected Class findClass(String name) throws ClassNotFoundException {
            try {
                return this.backingBundle.loadClass(name);
            } catch (ClassNotFoundException cnfe) {
                throw new ClassNotFoundException(name + " not found from bundle [" + backingBundle.getSymbolicName() + "]", cnfe);
            } catch (NoClassDefFoundError ncdfe) {
                NoClassDefFoundError e = new NoClassDefFoundError(name + " not found from bundle [" + backingBundle + "]");
                e.initCause(ncdfe);
                throw e;
            }
        }

        protected URL findResource(String name) {
            return backingBundle.getResource(name);
        }

        protected Enumeration findResources(String name) throws IOException {
            return backingBundle.getResources(name);
        }

        public URL getResource(String name) {
            return findResource(name);
        }

        protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class clazz = findClass(name);
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }

        public String toString() {
            return "OSGIClassLoader for [" + backingBundle + "]";
        }
    }
}
