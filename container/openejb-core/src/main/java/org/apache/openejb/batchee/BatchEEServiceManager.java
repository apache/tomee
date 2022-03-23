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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.batchee;

import org.apache.batchee.container.services.ServicesManager;
import org.apache.batchee.container.services.ServicesManagerLocator;
import org.apache.batchee.container.services.executor.DefaultThreadPoolService;
import org.apache.batchee.container.services.factory.CDIBatchArtifactFactory;
import org.apache.batchee.spi.BatchArtifactFactory;
import org.apache.batchee.spi.BatchThreadPoolService;
import org.apache.openejb.AppContext;
import org.apache.openejb.assembler.classic.event.AssemblerAfterApplicationCreated;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.observer.event.ObserverAdded;
import org.apache.openejb.util.AppFinder;
import org.apache.openejb.util.classloader.Unwrappable;
import org.apache.webbeans.config.WebBeansContext;

import jakarta.enterprise.inject.spi.BeanManager;
import java.util.Properties;

public class BatchEEServiceManager implements ServicesManagerLocator {
    public void initEnvironment(@Observes final ObserverAdded event) {
        if (event.getObserver() == this && !Boolean.getBoolean("openejb.batchee.integration.skip")) {
            ServicesManager.setServicesManagerLocator(this);
        }
    }

    public void storeClassLoader(@Observes final AssemblerAfterApplicationCreated init) {
        doInit(init.getContext());
    }

    private void doInit(final AppContext context) {
        if (context.get(ServicesManager.class) != null) {
            return;
        }

        final Thread thread = Thread.currentThread();
        final ClassLoader current = thread.getContextClassLoader();
        thread.setContextClassLoader(context.getClassLoader());
        final ServicesManager servicesManager = new ServicesManager();
        final Properties properties = new Properties(SystemInstance.get().getProperties());
        properties.putAll(context.getProperties());
        try {
            if (properties.getProperty(BatchArtifactFactory.class.getName()) == null) {
                properties.setProperty(BatchThreadPoolService.class.getName(), TomEEThreadPoolService.class.getName());
            }
            if (properties.getProperty(BatchArtifactFactory.class.getName()) == null) {
                properties.setProperty(BatchArtifactFactory.class.getName(), TomEEArtifactFactory.class.getName());
            }
            servicesManager.init(properties); // will look for batchee.properties so need the right classloader
        } finally {
            thread.setContextClassLoader(current);
        }

        context.set(ServicesManager.class, servicesManager);
    }

    @Override
    public ServicesManager find() {
        final ClassLoader contextClassLoader = unwrap(Thread.currentThread().getContextClassLoader());
        final AppContext context = AppFinder.findAppContextOrWeb(contextClassLoader, AppFinder.AppContextTransformer.INSTANCE);
        if (context != null) {
            doInit(context);
            return context.get(ServicesManager.class);
        }
        throw new IllegalStateException("Can't find ServiceManager for " + contextClassLoader);
    }

    private static ClassLoader unwrap(final ClassLoader tccl) {
        if (Unwrappable.class.isInstance(tccl)) {
            final ClassLoader unwrapped = Unwrappable.class.cast(tccl).unwrap();
            if (unwrapped != null) {
                return unwrapped;
            }
        }
        return tccl;
    }

    public static class TomEEArtifactFactory extends CDIBatchArtifactFactory {
        @Override
        protected BeanManager getBeanManager() {
            return WebBeansContext.currentInstance().getBeanManagerImpl();
        }
    }

    public static class TomEEThreadPoolService extends DefaultThreadPoolService {
        @Override
        public void executeTask(final Runnable work, final Object config) {
            final Thread thread = Thread.currentThread();
            final ClassLoader tccl = thread.getContextClassLoader();
            thread.setContextClassLoader(unwrap(tccl));
            try {
                super.executeTask(work, config);
            } finally {
                thread.setContextClassLoader(tccl);
            }
        }
    }
}
