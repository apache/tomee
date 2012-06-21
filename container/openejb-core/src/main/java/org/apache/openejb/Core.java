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

import java.util.concurrent.Semaphore;
import org.apache.openejb.util.JuliLogStreamFactory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

/**
* @version $Rev$ $Date$
*/
public class Core {
    static {
        final String[] classes = {
                "org.slf4j.LoggerFactory",
                "org.slf4j.impl.StaticLoggerBinder",

                "org.apache.openejb.config.sys.JaxbJavaee",
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
                "org.apache.openejb.util.Duration",
                "org.apache.openejb.util.Join",
                "org.apache.openejb.util.JuliLogStreamFactory",
                "org.apache.openejb.util.LogCategory",
                "org.apache.openejb.util.Logger",
                "org.apache.openejb.util.Messages",
                "org.apache.openejb.util.SafeToolkit",
                "org.apache.openejb.util.StringTemplate",
                "org.apache.openejb.util.proxy.ProxyManager",
                "org.apache.openjpa.enhance.PCRegistry",
                "org.apache.openjpa.lib.util.Localizer",
                "org.apache.xbean.naming.reference.SimpleReference",
                "org.apache.xbean.propertyeditor.PropertyEditors",
                "org.apache.xbean.propertyeditor.ReferenceIdentityMap",
                "org.apache.xbean.recipe.ReflectionUtil",
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

        final int permits = Runtime.getRuntime().availableProcessors() + 1;
        final Semaphore semaphore = new Semaphore(permits);
        final ClassLoader loader = OpenEjbContainer.class.getClassLoader();

        try { // do it before all other to force juli config
            Class.forName("org.apache.openejb.util.JuliLogStreamFactory", true, loader);
        } catch (Throwable e) {
            // no-op
        } finally {
            semaphore.release();
        }

        for (final String className : classes) {
            try {
                semaphore.acquire();
                final Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Class.forName(className, true, loader);
                        } catch (Throwable e) {
                            // no-op
                        } finally {
                            semaphore.release();
                        }
                    }
                });
                thread.setDaemon(true);
                thread.start();
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        try {
            semaphore.acquire(permits);
            preloadMessages.join();
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    public static void warmup() {}
}
