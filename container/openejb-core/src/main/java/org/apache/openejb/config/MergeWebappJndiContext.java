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
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceContextType;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.TransactionType;

import jakarta.ejb.EJBContext;
import jakarta.ejb.EntityContext;
import jakarta.ejb.MessageDrivenContext;
import jakarta.ejb.SessionContext;
import jakarta.transaction.UserTransaction;
import java.util.Map;

/**
 * In a webapp all ejbs will share the JNDI namespace of the servlets
 * This means no private namespace for each EJB.
 *
 * To make this happen we merge the JNDI entries of each ejb into
 *
 * @version $Rev$ $Date$
 */
public class MergeWebappJndiContext implements DynamicDeployer {

    @Override
    public AppModule deploy(final AppModule appModule) throws OpenEJBException {

        for (final WebModule webModule : appModule.getWebModules()) {
            if (webModule.getFinder() == null) {
                continue;
            }

            for (final EjbModule ejbModule : appModule.getEjbModules()) {
                // If they are the same module, they'll have the same finder
                if (ejbModule.getFinder() != webModule.getFinder()) {
                    final String forceMerge = ejbModule.getProperties().getProperty("openejb.ejbmodule.MergeWebappJndiContext"); // always true is not null
                    if (forceMerge != null) { // default resource propagation
                        for (final EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                            copy(webModule.getWebApp().getResourceEnvRefMap(), bean.getResourceEnvRefMap());
                        }
                    }
                    continue;
                }

                merge(ejbModule, webModule);
            }
        }

        return appModule;
    }

    private void merge(final EjbModule ejbModule, final WebModule webModule) {
        final JndiConsumer webApp = webModule.getWebApp();

        final EjbJar ejbJar = ejbModule.getEjbJar();

        for (final EnterpriseBean bean : ejbJar.getEnterpriseBeans()) {
            merge(bean.getEnvEntryMap(), webApp.getEnvEntryMap());
            merge(bean.getEjbRefMap(), webApp.getEjbRefMap());
            merge(bean.getEjbLocalRefMap(), webApp.getEjbLocalRefMap());
            merge(bean.getServiceRefMap(), webApp.getServiceRefMap());
            merge(bean.getResourceRefMap(), webApp.getResourceRefMap());
            merge(bean.getResourceEnvRefMap(), webApp.getResourceEnvRefMap());
            merge(bean.getMessageDestinationRefMap(), webApp.getMessageDestinationRefMap());
            merge(bean.getPersistenceContextRefMap(), webApp.getPersistenceContextRefMap());
            merge(bean.getPersistenceUnitRefMap(), webApp.getPersistenceUnitRefMap());

            mergeUserTransaction(bean.getResourceRefMap(), webApp.getResourceRefMap(), webApp);
            mergeUserTransaction(bean.getResourceEnvRefMap(), webApp.getResourceEnvRefMap(), webApp);
            mergeUserTransaction(webApp.getResourceRefMap(), bean.getResourceRefMap(), bean);
            mergeUserTransaction(webApp.getResourceEnvRefMap(), bean.getResourceEnvRefMap(), bean);
        }

        final SessionBean aggregator = new SessionBean(); // easy way to get a JndiConsumer

        for (final EnterpriseBean a : ejbJar.getEnterpriseBeans()) {
            aggregator.getEnvEntryMap().putAll(a.getEnvEntryMap());
            aggregator.getEjbRefMap().putAll(a.getEjbRefMap());
            aggregator.getEjbLocalRefMap().putAll(a.getEjbLocalRefMap());
            aggregator.getServiceRefMap().putAll(a.getServiceRefMap());
            aggregator.getResourceRefMap().putAll(a.getResourceRefMap());
            aggregator.getResourceEnvRefMap().putAll(a.getResourceEnvRefMap());
            aggregator.getMessageDestinationRefMap().putAll(a.getMessageDestinationRefMap());
            aggregator.getPersistenceContextRefMap().putAll(a.getPersistenceContextRefMap());
            aggregator.getPersistenceUnitRefMap().putAll(a.getPersistenceUnitRefMap());
        }

        for (final EnterpriseBean a : ejbJar.getEnterpriseBeans()) {
            copy(aggregator.getEnvEntryMap(), a.getEnvEntryMap());
            copy(aggregator.getEjbRefMap(), a.getEjbRefMap());
            copy(aggregator.getEjbLocalRefMap(), a.getEjbLocalRefMap());
            copy(aggregator.getServiceRefMap(), a.getServiceRefMap());
            copy(aggregator.getResourceRefMap(), a.getResourceRefMap());
            copy(aggregator.getResourceEnvRefMap(), a.getResourceEnvRefMap());
            copy(aggregator.getMessageDestinationRefMap(), a.getMessageDestinationRefMap());
            copy(aggregator.getPersistenceContextRefMap(), a.getPersistenceContextRefMap());
            copy(aggregator.getPersistenceUnitRefMap(), a.getPersistenceUnitRefMap());

            mergeUserTransaction(aggregator.getResourceRefMap(), a.getResourceRefMap(), a);
            mergeUserTransaction(aggregator.getResourceEnvRefMap(), a.getResourceEnvRefMap(), a);
        }
    }

    /**
     * Bidirectional a-b merge
     */
    private <R extends JndiReference> void merge(final Map<String, R> a, final Map<String, R> b) {
        copy(a, b);
        copy(b, a);
    }

    private <R extends JndiReference> void copy(final Map<String, R> from, final Map<String, R> to) {
        for (final R a : from.values()) {

            if (isPrivateReference(a)) {
                continue;
            }

            final R b = to.get(a.getKey());

            // New entry
            if (b == null) {
                if (!isExtendedPersistenceContext(a)) {
                    to.put(a.getKey(), a);
                }
                continue;
            }

            // Update existing entry

            // merge injection points
            b.getInjectionTarget().addAll(a.getInjectionTarget());

            // merge env-entry values
            if (b instanceof EnvEntry && a instanceof EnvEntry) {
                final EnvEntry eb = (EnvEntry) b;
                final EnvEntry ea = (EnvEntry) a;

                if (eb.getEnvEntryValue() == null) {
                    eb.setEnvEntryValue(ea.getEnvEntryValue());
                }

                if (eb.getEnvEntryType() == null) {
                    eb.setEnvEntryType(ea.getEnvEntryType());
                }
            }
        }
    }

    private static <R extends JndiReference> boolean isExtendedPersistenceContext(final R b) {
        return b instanceof PersistenceContextRef
            && PersistenceContextType.EXTENDED.equals(((PersistenceContextRef) b).getPersistenceContextType());
    }

    private <R extends JndiReference> boolean isPrivateReference(final R a) {
        if (!isResourceRef(a)) {
            return false;
        }

        final Class[] types = {EJBContext.class, EntityContext.class, SessionContext.class, MessageDrivenContext.class, UserTransaction.class};

        for (final Class type : types) {
            if (type.getName().equals(a.getType())) {
                return true;
            }
        }

        return false;
    }

    private <R extends JndiReference> boolean isResourceRef(final R a) {
        return a instanceof ResourceRef || a instanceof ResourceEnvRef;
    }

    private <R extends JndiReference> void mergeUserTransaction(final Map<String, R> from, final Map<String, R> to, final JndiConsumer consumer) {
        if (consumer instanceof EnterpriseBean) {
            final EnterpriseBean enterpriseBean = (EnterpriseBean) consumer;
            if (enterpriseBean.getTransactionType() != TransactionType.BEAN) {
                return;
            }
        }

        for (final R a : from.values()) {

            if (!UserTransaction.class.getName().equals(a.getType())) {
                continue;
            }

            final R b = to.get(a.getKey());

            // New entry
            if (b == null) {
                to.put(a.getKey(), a);
                continue;
            }

            // Update existing entry
            // merge injection points
            b.getInjectionTarget().addAll(a.getInjectionTarget());
        }
    }


}
