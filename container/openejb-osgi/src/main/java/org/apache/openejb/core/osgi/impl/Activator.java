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

import java.util.Properties;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.loader.OpenEJBInstance;
import org.apache.openejb.loader.SystemInstance;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
public class Activator implements BundleActivator {

    private OpenEJBInstance openejb;

    public void start(BundleContext context) throws Exception {

        System.out.println("Starting OpenEJB...");

        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        openejb = new OpenEJBInstance();

        Properties env = new Properties();
        // env.setProperty("openejb.embedded", "true");
        // default, but to remember that the setting exists
        env.setProperty("openejb.loader", "context");
        // NPE
        env.setProperty("openejb.deployments.classpath", "false");
        // Else it doesn't work - ClassNotFoundException: org.apache.log4j.Layout
        env.setProperty("openejb.log.factory", "org.apache.openejb.util.JuliLogStreamFactory");

        SystemInstance.init(env);
        // OptionsLog.install();

        // OpenEJB.init(env);
        openejb.init(env);

        System.out.println("Registering OSGified OpenEJB Deployer...");
        context.addBundleListener(new Deployer());
    }

    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping OpenEJB; openejb.isInitialized(): " + openejb.isInitialized());
        openejb = null;
        OpenEJB.destroy();
    }
}
