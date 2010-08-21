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

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

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

/**
 * @version $Rev$ $Date$
 */
public class Deployer implements BundleListener {

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
        System.out.println(String.format("[Deployer] Bundle %s has been started", bundle.getSymbolicName()));

        System.out.println(String.format("[Deployer] Checking whether it's an EJB module"));
        Enumeration<?> e = bundle.findEntries("META-INF", "ejb-jar.xml", false);
        if (e.hasMoreElements()) {
            URL ejbJarUrl = (URL) e.nextElement();

            System.out.println("[Deployer] It's an EJB module: " + ejbJarUrl);

            System.out.println("[Deployer] Deploying onto OpenEJB");

            String location = bundle.getLocation();
            System.out.println("[Deployer] bundle location: " + location);
            try {
                File file = new File(new URL(location).getFile());
                try {
                    DeploymentLoader deploymentLoader = new DeploymentLoader();
                    AppModule appModule = deploymentLoader.load(file);

                    ConfigurationFactory configurationFactory = new ConfigurationFactory();
                    AppInfo appInfo = configurationFactory.configureApplication(appModule);

                    Assembler assembler = (Assembler) SystemInstance.get().getComponent(Assembler.class);
                    System.out.println(assembler);
                    System.out.println(appInfo);
                    assembler.createApplication(appInfo);

                    System.out.println("[Deployer] Application deployed: " + appInfo.path);

                    registerService(bundle, appInfo);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }

        }
    }

    private void undeploy(Bundle bundle) {
        System.out.println(String.format("[Deployer] Bundle %s has been stopped", bundle.getSymbolicName()));

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
        System.out.println("[Deployer] Registering a service for the EJB");
        BundleContext context = bundle.getBundleContext();
        for (EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
            for (EnterpriseBeanInfo ejbInfo : ejbJarInfo.enterpriseBeans) {
                try {
                    context.registerService(ejbInfo.businessRemote.toArray(new String[0]), bundle.loadClass(
                            ejbInfo.ejbClass).newInstance(), new Properties());
                    System.out.println(String.format(
                            "[Deployer] Service object %s registered under the class names: %s", ejbInfo.ejbClass,
                            ejbInfo.businessRemote));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
