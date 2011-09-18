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
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.JndiReference;
import org.apache.openejb.jee.ResourceEnvRef;

import javax.enterprise.inject.spi.BeanManager;
import java.util.Map;

/**
 */
public class BuiltInEnvironmentEntries implements DynamicDeployer {

    public AppModule deploy(AppModule appModule) throws OpenEJBException {

        for (ClientModule module : appModule.getClientModules()) {
            final JndiConsumer consumer = module.getApplicationClient();
            if (consumer == null) continue;

            add(consumer, module, appModule);
        }

        for (WebModule module : appModule.getWebModules()) {
            final JndiConsumer consumer = module.getWebApp();
            if (consumer == null) continue;

            add(consumer, module, appModule);
        }

        for (EjbModule module : appModule.getEjbModules()) {
            final EjbJar ejbJar = module.getEjbJar();
            if (ejbJar == null) continue;

            for (EnterpriseBean consumer : ejbJar.getEnterpriseBeans()) {
                add(consumer, module, appModule);
            }
        }

        return appModule;
    }

    private void add(JndiConsumer jndi, DeploymentModule module, DeploymentModule app) {

        // Standard names
        add(jndi.getEnvEntryMap(), new EnvEntry().name("java:module/ModuleName").value(module.getModuleId()).type(String.class));
        add(jndi.getEnvEntryMap(), new EnvEntry().name("java:app/AppName").value(app.getModuleId()).type(String.class));

        // Standard References to built-in objects
        add(jndi.getResourceEnvRefMap(), new ResourceEnvRef().name("java:comp/BeanManager").type(BeanManager.class));
        add(jndi.getResourceEnvRefMap(), new ResourceEnvRef().name("java:comp/Validator").type(javax.validation.Validator.class));
        add(jndi.getResourceEnvRefMap(), new ResourceEnvRef().name("java:comp/ValidatorFactory").type(javax.validation.ValidatorFactory.class));
        add(jndi.getResourceEnvRefMap(), new ResourceEnvRef().name("java:comp/TransactionManager").type(javax.transaction.TransactionManager.class));
        add(jndi.getResourceEnvRefMap(), new ResourceEnvRef().name("java:comp/TransactionSynchronizationRegistry").type(javax.transaction.TransactionSynchronizationRegistry.class));


        // OpenEJB specific feature
        add(jndi.getEnvEntryMap(), new EnvEntry().name("java:comp/ComponentName").value(jndi.getJndiConsumerName()).type(String.class));

    }

    private <E extends JndiReference> void add(Map<String, E> map, E entry) {
        final E existing = map.get(entry.getKey());

        map.put(entry.getKey(), entry);

        if (existing != null) {
            entry.getInjectionTarget().addAll(existing.getInjectionTarget());
        }
    }
}
