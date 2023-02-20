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

import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.JndiReference;
import org.apache.openejb.jee.ResourceEnvRef;

import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Map;

public class BuiltInEnvironmentEntries implements DynamicDeployer {
    private final boolean addDefaults;

    public BuiltInEnvironmentEntries(final boolean addDefaults) {
        this.addDefaults = addDefaults;
    }

    public AppModule deploy(final AppModule appModule) throws OpenEJBException {

        for (final ClientModule module : appModule.getClientModules()) {
            final JndiConsumer consumer = module.getApplicationClient();
            if (consumer == null) {
                continue;
            }

            add(consumer, module, appModule, false);
        }

        for (final WebModule module : appModule.getWebModules()) {
            final JndiConsumer consumer = module.getWebApp();
            if (consumer == null) {
                continue;
            }

            add(consumer, module, appModule, addDefaults);
        }

        for (final EjbModule module : appModule.getEjbModules()) {
            final EjbJar ejbJar = module.getEjbJar();
            if (ejbJar == null) {
                continue;
            }

            for (final EnterpriseBean consumer : ejbJar.getEnterpriseBeans()) {
                add(consumer, module, appModule, addDefaults && BeanContext.Comp.class.getName().equals(consumer.getEjbClass()));
            }
        }

        return appModule;
    }

    private void add(final JndiConsumer jndi, final DeploymentModule module, final DeploymentModule app, final boolean defaults) {

        // Standard names
        add(jndi.getEnvEntryMap(), new EnvEntry().name("java:module/ModuleName").value(module.getModuleId()).type(String.class));
        add(jndi.getEnvEntryMap(), new EnvEntry().name("java:app/AppName").value(app.getModuleId()).type(String.class));

        // Standard References to built-in objects
        add(jndi.getResourceEnvRefMap(), new ResourceEnvRef().name("java:comp/BeanManager").type(BeanManager.class));
        add(jndi.getResourceEnvRefMap(), new ResourceEnvRef().name("java:comp/Validator").type(Validator.class));
        add(jndi.getResourceEnvRefMap(), new ResourceEnvRef().name("java:comp/ValidatorFactory").type(ValidatorFactory.class));
        add(jndi.getResourceEnvRefMap(), new ResourceEnvRef().name("java:comp/TransactionManager").type(TransactionManager.class));
        add(jndi.getResourceEnvRefMap(), new ResourceEnvRef().name("java:comp/TransactionSynchronizationRegistry").type(TransactionSynchronizationRegistry.class));

        // From: https://jakarta.ee/specifications/concurrency/3.0/jakarta-concurrency-spec-3.0.pdf
        // Jakarta Concurrency §3.1.4.3
        add(jndi.getResourceEnvRefMap(), new ResourceEnvRef().name("java:comp/DefaultManagedExecutorService").type(ManagedExecutorService.class));

        // Jakarta Concurrency §3.2.4.3
        add(jndi.getResourceEnvRefMap(), new ResourceEnvRef().name("java:comp/DefaultManagedScheduledExecutorService").type(ManagedScheduledExecutorService.class));

        // Jakarta Concurrency §3.4.4.3
        add(jndi.getResourceEnvRefMap(), new ResourceEnvRef().name("java:comp/DefaultManagedThreadFactory").type(ManagedThreadFactory.class));

        // Jakarta Concurrency §3.3.4.3
        add(jndi.getResourceEnvRefMap(), new ResourceEnvRef().name("java:comp/DefaultContextService").type(ContextService.class));

        if (defaults) {
            try {
                final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                contextClassLoader.loadClass("org.apache.activemq.ActiveMQSslConnectionFactory");
                final ResourceEnvRef ref = new ResourceEnvRef().name("java:comp/DefaultJMSConnectionFactory")
                    .type(contextClassLoader.loadClass("jakarta.jms.ConnectionFactory"));
                add(jndi.getResourceEnvRefMap(), ref);
            } catch (final ClassNotFoundException | NoClassDefFoundError notThere) {
                // no-op
            }
        }

        // OpenEJB specific feature
        add(jndi.getEnvEntryMap(), new EnvEntry().name("java:comp/ComponentName").value(jndi.getJndiConsumerName()).type(String.class));
    }

    private <E extends JndiReference> void add(final Map<String, E> map, final E entry) {
        final E existing = map.get(entry.getKey());

        map.put(entry.getKey(), entry);

        if (existing != null) {
            entry.getInjectionTarget().addAll(existing.getInjectionTarget());
        }
    }
}
