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
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.JndiReference;
import org.apache.openejb.jee.ResourceEnvRef;

import javax.ejb.EJBContext;
import javax.ejb.MessageDrivenContext;
import javax.ejb.SessionContext;
import java.util.Iterator;
import java.util.Map;

/**
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

        removePrivateReferences(webApp);

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
        }

        removePrivateReferences(webApp);
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
            }
        }
    }

    private void removePrivateReferences(JndiConsumer webApp) {
        Class[] types = {EJBContext.class, SessionContext.class, MessageDrivenContext.class};
        final Iterator<ResourceEnvRef> refs = webApp.getResourceEnvRef().iterator();
        while (refs.hasNext()) {
            final ResourceEnvRef ref = refs.next();
            if (isInvalid(types, ref)) refs.remove();
        }
    }

    private boolean isInvalid(Class[] types, ResourceEnvRef ref) {
        boolean invalid = false;
        for (Class type : types) {
            if (type.getName().equals(ref.getType())) {
                invalid= true;
                break;
            }
        }
        return invalid;
    }

    private void yank(ResourceEnvRef ref, Class<EJBContext> ejbContextClass, Class<SessionContext> sessionContextClass, Class<MessageDrivenContext> messageDrivenContextClass) {
    }

}
