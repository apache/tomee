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
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.JndiReference;
import org.apache.openejb.jee.ResourceRef;

import javax.ejb.EJBContext;
import javax.ejb.EntityContext;
import javax.ejb.MessageDrivenContext;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.enterprise.inject.spi.BeanManager;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.xml.ws.WebServiceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class LinkBuiltInTypes implements DynamicDeployer {

    @Override
    public AppModule deploy(AppModule appModule) throws OpenEJBException {

        for (ClientModule module : appModule.getClientModules()) {
            final JndiConsumer consumer = module.getApplicationClient();
            if (consumer == null) continue;

            link(consumer);
        }

        for (WebModule module : appModule.getWebModules()) {
            final JndiConsumer consumer = module.getWebApp();
            if (consumer == null) continue;

            link(consumer);
        }

        for (EjbModule module : appModule.getEjbModules()) {
            final EjbJar ejbJar = module.getEjbJar();
            if (ejbJar == null) continue;

            for (EnterpriseBean consumer : ejbJar.getEnterpriseBeans()) {
                link(consumer);
            }
        }

        return appModule;

    }

    private void link(JndiConsumer consumer) {


        Map<String, String> links = new HashMap<String, String>();

        add(links, BeanManager.class);
        add(links, Validator.class);
        add(links, ValidatorFactory.class);
        add(links, EJBContext.class, EntityContext.class, SessionContext.class, MessageDrivenContext.class);
        add(links, UserTransaction.class);
        add(links, TransactionManager.class);
        add(links, TransactionSynchronizationRegistry.class);
        add(links, TimerService.class);
        add(links, WebServiceContext.class);

        List<JndiReference> refs = new ArrayList<JndiReference>();
        refs.addAll(consumer.getResourceRef());
        refs.addAll(consumer.getResourceEnvRef());

        for (JndiReference ref : refs) {
            final String link = links.get(ref.getType());

            if (link == null) continue;

            if (ref.getName().equals(link)) {
                // make sure the user hasn't linked it to itself or anything else
                ref.setLookupName(null);
                continue;
            }

            ref.setLookupName(link);
        }
    }

    private void add(Map<String, String> links, Class<?> type, Class... aliases) {
        links.put(type.getName(), "java:comp/"+type.getSimpleName());

        for (Class clazz : aliases) {
            links.put(clazz.getName(), "java:comp/"+type.getSimpleName());
        }
    }
}
