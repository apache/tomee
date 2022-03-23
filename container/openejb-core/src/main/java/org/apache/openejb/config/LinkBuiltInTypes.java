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
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.JndiReference;

import jakarta.ejb.EJBContext;
import jakarta.ejb.EntityContext;
import jakarta.ejb.MessageDrivenContext;
import jakarta.ejb.SessionContext;
import jakarta.ejb.TimerService;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.UserTransaction;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.xml.ws.WebServiceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class LinkBuiltInTypes implements DynamicDeployer {

    @Override
    public AppModule deploy(final AppModule appModule) throws OpenEJBException {

        for (final ClientModule module : appModule.getClientModules()) {
            final JndiConsumer consumer = module.getApplicationClient();
            if (consumer == null) {
                continue;
            }

            link(consumer);
        }

        for (final WebModule module : appModule.getWebModules()) {
            final JndiConsumer consumer = module.getWebApp();
            if (consumer == null) {
                continue;
            }

            link(consumer);
        }

        for (final EjbModule module : appModule.getEjbModules()) {
            final EjbJar ejbJar = module.getEjbJar();
            if (ejbJar == null) {
                continue;
            }

            for (final EnterpriseBean consumer : ejbJar.getEnterpriseBeans()) {
                link(consumer);
            }
        }

        return appModule;

    }

    private void link(final JndiConsumer consumer) {


        final Map<String, String> links = new HashMap<>();

        add(links, BeanManager.class);
        add(links, Validator.class);
        add(links, ValidatorFactory.class);
        add(links, EJBContext.class, EntityContext.class, SessionContext.class, MessageDrivenContext.class);
        add(links, UserTransaction.class);
        add(links, TransactionManager.class);
        add(links, TransactionSynchronizationRegistry.class);
        add(links, TimerService.class);
        add(links, WebServiceContext.class);

        final List<JndiReference> refs = new ArrayList<>();
        refs.addAll(consumer.getResourceRef());
        refs.addAll(consumer.getResourceEnvRef());

        for (final JndiReference ref : refs) {
            final String link = links.get(ref.getType());

            if (link == null) {
                continue;
            }

            if (ref.getName().equals(link)) {
                // make sure the user hasn't linked it to itself or anything else
                ref.setLookupName(null);
                continue;
            }

            ref.setLookupName(link);
        }
    }

    private void add(final Map<String, String> links, final Class<?> type, final Class... aliases) {
        links.put(type.getName(), "java:comp/" + type.getSimpleName());

        for (final Class clazz : aliases) {
            links.put(clazz.getName(), "java:comp/" + type.getSimpleName());
        }
    }
}
