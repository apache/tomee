/*
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
package org.apache.openejb;

import org.apache.openejb.config.ServiceUtils;
import org.apache.openejb.util.Messages;

import java.util.concurrent.Semaphore;

/**
* @version $Rev$ $Date$
*/
public class Core {
    static {
        final String[] classes = {
                "org.slf4j.LoggerFactory",
                "org.slf4j.impl.StaticLoggerBinder",

                "org.apache.openejb.config.sys.JaxbJavaee",
                "org.apache.bval.jsr303.ApacheValidationProvider",
                "org.apache.bval.jsr303.ApacheValidatorFactory",
                "org.apache.bval.jsr303.ConstraintAnnotationAttributes",
                "org.apache.bval.jsr303.ConstraintDefaults",
                "org.apache.bval.jsr303.groups.GroupsComputer",
                "org.apache.bval.jsr303.xml.ValidationMappingParser",
                "org.apache.bval.util.PrivilegedActions",
                "org.apache.geronimo.transaction.manager.TransactionManagerImpl",
                "org.apache.openejb.InterfaceType",
                "org.apache.openejb.assembler.classic.Assembler",
                "org.apache.openejb.assembler.classic.AssemblerTool",
                "org.apache.openejb.cdi.CdiBuilder",
                "org.apache.openejb.cdi.ThreadSingletonServiceImpl",
                "org.apache.openejb.config.AppValidator",
                "org.apache.openejb.config.AnnotationDeployer",
                "org.apache.openejb.config.AutoConfig",
                "org.apache.openejb.config.ConfigurationFactory",
                "org.apache.openejb.config.MBeanDeployer",
                "org.apache.openejb.config.PersistenceContextAnnFactory",
                "org.apache.openejb.core.ServerFederation",
                "org.apache.openejb.core.ivm.EjbHomeProxyHandler$1",
                "org.apache.openejb.core.ivm.EjbHomeProxyHandler$MethodType",
                "org.apache.openejb.core.managed.ManagedContainer$MethodType",
                "org.apache.openejb.loader.FileUtils",
                "org.apache.openejb.loader.IO",
                "org.apache.openejb.loader.SystemInstance",
                "org.apache.openejb.monitoring.StatsInterceptor",
                "org.apache.openejb.persistence.JtaEntityManagerRegistry",
                "org.apache.openejb.util.Join",
                "org.apache.openejb.util.JuliLogStreamFactory",
                "org.apache.openejb.util.LogCategory",
                "org.apache.openejb.util.Messages",
                "org.apache.openejb.util.SafeToolkit",
                "org.apache.openejb.util.StringTemplate",
                "org.apache.openejb.util.proxy.ProxyManager",
                "org.apache.openjpa.enhance.PCRegistry",
                "org.apache.openjpa.lib.util.Localizer",
                "org.apache.webbeans.logger.WebBeansLoggerFacade",
                "org.apache.xbean.naming.reference.SimpleReference",
                "org.apache.xbean.propertyeditor.PropertyEditors",
                "org.apache.xbean.propertyeditor.ReferenceIdentityMap",
                "org.apache.xbean.recipe.ReflectionUtil"
        };

        final Thread preloadMessages = new Thread() {
            @Override
            public void run() {
                new Messages("org.apache.openejb.util.resources");
                new Messages("org.apache.openejb.config");
                new Messages("org.apache.openejb.config.resources");
            }
        };
        preloadMessages.start();

        final Thread preloadServiceProviders = new Thread() {
            @Override
            public void run() {
                try {
                    ServiceUtils.getServiceProviders();
                } catch (OpenEJBException e) {
                    // no-op
                }
            }
        };
        preloadServiceProviders.start();

        final int permits = 2 * Runtime.getRuntime().availableProcessors() + 1;
        final Semaphore semaphore = new Semaphore(0);
        final ClassLoader loader = OpenEjbContainer.class.getClassLoader();

        try { // logging classes should be loaded before any other classes so do it here synchronously
            Class.forName("org.apache.openejb.util.Logger", true, loader);
            Class.forName("org.apache.openejb.util.JuliLogStreamFactory", true, loader);
        } catch (Throwable e) {
            // no-op
        }

        final int part = (int) Math.round(classes.length * 1. / permits);
        for (int i = 0; i < permits; i++) {
            final int offset = i * part;
            final Thread thread = new Thread() {
                @Override
                public void run() {
                    int max = offset + part;
                    if (offset / part == permits - 1) { // last one
                        max = classes.length;
                    }

                    for (int c = offset; c < max; c++) {
                        try {
                            Class.forName(classes[c], true, loader);
                        } catch (Throwable e) {
                            // no-op
                        }
                    }
                    semaphore.release();
                }
            };
            thread.setDaemon(true);
            thread.start();
        }
        try {
            preloadServiceProviders.join();
            preloadMessages.join();
            semaphore.acquire(permits);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    public static void warmup() {}
}
