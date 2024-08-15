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
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.KeyedCollection;
import org.apache.openejb.jee.ManagedExecutor;
import org.apache.openejb.util.PropertyPlaceHolderHelper;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConvertExecutorServiceDefinitions extends BaseConvertDefinitions {
    @Override
    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        final List<JndiConsumer> jndiConsumers = collectConsumers(appModule);

        final KeyedCollection<String, ManagedExecutor> managedExecutors = new KeyedCollection<>();
        final KeyedCollection<String, ManagedExecutor> managedExecutorServicesFromCompManagedBeans = new KeyedCollection<>();

        for (final JndiConsumer consumer : jndiConsumers) {
            if (consumer == null) {
                continue;
            }

            if (consumer instanceof CompManagedBean) {
                /*
                 * TOMEE-2053: It may contain invalid context service definitions
                 * because it is never updated with content from the ejb-jar.xml
                 * Wait until all other consumers have been processed, to safely
                 * decide which context services to transfer;
                 */

                managedExecutorServicesFromCompManagedBeans.addAll(consumer.getManagedExecutorServiceMap().values());
                continue;
            }
            managedExecutors.addAll(consumer.getManagedExecutorServiceMap().values());
        }

        final Map<String, ManagedExecutor> managedExecutorsMap = managedExecutors.toMap();
        for (ManagedExecutor ManagedExecutor : managedExecutorServicesFromCompManagedBeans) {
            //Interested only in ManagedExecutorServices that come from non-JndiConsumers
            if (!managedExecutorsMap.containsKey(ManagedExecutor.getName().getvalue())) {
                managedExecutors.add(ManagedExecutor);
            }
        }

        for (final ManagedExecutor dataSource : managedExecutors) {
            appModule.getResources().add(toResource(dataSource));
        }

        return appModule;
    }

    private Resource toResource(final ManagedExecutor executorService) {
        final String name = cleanUpName(executorService.getName().getvalue());

        final Resource def = new Resource(name, jakarta.enterprise.concurrent.ManagedExecutorService.class.getName());

        def.setJndi(executorService.getName().getvalue().replaceFirst("java:", ""));

        final Properties p = def.getProperties();
        put(p, "ContextService", executorService.getContextService().getvalue());
        put(p, "LongHungTaskThreshold", executorService.getLongHungTaskThreshold());
        put(p, "MaxAsync", executorService.getMaxAsync());

        // to force it to be bound in JndiEncBuilder
        put(p, "JndiName", def.getJndi());

        return def;
    }

    private static void put(final Properties properties, final String key, final Object value) {
        if (key == null) {
            return;
        }
        if (value == null) {
            return;
        }

        properties.put(key, PropertyPlaceHolderHelper.value(String.valueOf(value)));
    }
}
