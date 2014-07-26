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
import org.apache.openejb.AppContext;
import org.apache.openejb.assembler.classic.event.AssemblerAfterApplicationCreated;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.observer.event.ObserverAdded;
import org.apache.openejb.util.AppFinder;

import java.util.Properties;

public class BatchEEServiceManager implements ServicesManagerLocator {
    public void initEnvironment(final @Observes ObserverAdded event) {
        if (event.getObserver() == this) {
            ServicesManager.setServicesManagerLocator(this);
        }
    }

    public void storeClassLoader(final @Observes AssemblerAfterApplicationCreated init) {
        final Properties properties = new Properties(SystemInstance.get().getProperties());
        properties.putAll(init.getApp().properties);

        final Thread thread = Thread.currentThread();
        final ClassLoader current = thread.getContextClassLoader();
        thread.setContextClassLoader(init.getContext().getClassLoader());
        final ServicesManager servicesManager = new ServicesManager();
        try {
            servicesManager.init(properties); // will look for batchee.properties so need the right classloader
        } finally {
            thread.setContextClassLoader(current);
        }

        init.getContext().set(ServicesManager.class, servicesManager);
    }

    @Override
    public ServicesManager find() {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final AppContext context = AppFinder.findAppContextOrWeb(contextClassLoader, AppFinder.AppContextTransformer.INSTANCE);
        if (context != null) {
            return context.get(ServicesManager.class);
        }
        throw new IllegalStateException("Can't find ServiceManager for " + contextClassLoader);
    }
}
