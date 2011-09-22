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
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.JndiReference;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.TransactionType;

import javax.ejb.EJBContext;
import javax.ejb.EntityContext;
import javax.ejb.MessageDrivenContext;
import javax.ejb.SessionContext;
import javax.transaction.UserTransaction;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
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
    public AppModule deploy(AppModule appModule) throws OpenEJBException {

        for (WebModule webModule : appModule.getWebModules()) {
            if (webModule.getFinder() == null) continue;

            for (EjbModule ejbModule : appModule.getEjbModules()) {
                // If they are the same module, they'll have the same finder
                if (ejbModule.getFinder() != webModule.getFinder()) continue;

                merge(ejbModule, webModule);
            }
        }

        return appModule;
    }

    private void merge(EjbModule ejbModule, WebModule webModule) {
        final JndiConsumer webApp = webModule.getWebApp();

        final EjbJar ejbJar = ejbModule.getEjbJar();

        for (EnterpriseBean bean : ejbJar.getEnterpriseBeans()) {
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

        for (EnterpriseBean a : ejbJar.getEnterpriseBeans()) {

            // Merge the bean namespaces together too
            for (EnterpriseBean b : ejbJar.getEnterpriseBeans()) {
                if (a == b) continue;

                merge(a.getEnvEntryMap(), b.getEnvEntryMap());
                merge(a.getEjbRefMap(), b.getEjbRefMap());
                merge(a.getEjbLocalRefMap(), b.getEjbLocalRefMap());
                merge(a.getServiceRefMap(), b.getServiceRefMap());
                merge(a.getResourceRefMap(), b.getResourceRefMap());
                merge(a.getResourceEnvRefMap(), b.getResourceEnvRefMap());
                merge(a.getMessageDestinationRefMap(), b.getMessageDestinationRefMap());
                merge(a.getPersistenceContextRefMap(), b.getPersistenceContextRefMap());
                merge(a.getPersistenceUnitRefMap(), b.getPersistenceUnitRefMap());

                mergeUserTransaction(a.getResourceRefMap(), b.getResourceRefMap(), b);
                mergeUserTransaction(a.getResourceEnvRefMap(), b.getResourceEnvRefMap(), b);
            }
        }
    }

    /**
     * Bidirectional a-b merge
     *
     * @param a
     * @param b
     * @param <R>
     */
    private <R extends JndiReference> void merge(Map<String, R> a, Map<String, R> b) {
        copy(a, b);
        copy(b, a);
    }

    private <R extends JndiReference> void copy(Map<String, R> from, Map<String, R> to) {
        for (R a : from.values()) {

            if (isPrivateReference(a)) continue;

            final R b = to.get(a.getKey());

            // New entry
            if (b == null) {
                to.put(a.getKey(), a);
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

    private <R extends JndiReference> boolean isPrivateReference(R a) {
        if (!isResourceRef(a)) return false;

        Class[] types = {EJBContext.class, EntityContext.class, SessionContext.class, MessageDrivenContext.class, UserTransaction.class};

        for (Class type : types) {
            if (type.getName().equals(a.getType())) return true;
        }

        return false;
    }

    private <R extends JndiReference> boolean isResourceRef(R a) {
        return a instanceof ResourceRef || a instanceof ResourceEnvRef;
    }

    private <R extends JndiReference> void mergeUserTransaction(Map<String, R> from, Map<String, R> to, JndiConsumer consumer) {
        if (consumer instanceof EnterpriseBean) {
            final EnterpriseBean enterpriseBean = (EnterpriseBean) consumer;
            if (enterpriseBean.getTransactionType() != TransactionType.BEAN) return;
        }

        for (R a : from.values()) {

            if (!UserTransaction.class.getName().equals(a.getType())) continue;

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
