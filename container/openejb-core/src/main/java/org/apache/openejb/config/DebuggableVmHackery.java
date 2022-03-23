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
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.Method;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.ResourceLink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
class DebuggableVmHackery implements DynamicDeployer {

    public AppModule deploy(final AppModule appModule) throws OpenEJBException {

        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            final EjbJar ejbJar = ejbModule.getEjbJar();
            final OpenejbJar openejbJar = ejbModule.getOpenejbJar();
            final Map<String, EjbDeployment> deployments = openejbJar.getDeploymentsByEjbName();

            ejbJar.setRelationships(null);

            final List<String> removed = new ArrayList<>();

            for (final EnterpriseBean bean : ejbJar.getEnterpriseBeans()) {

                final String ejbName = bean.getEjbName();
                final EjbDeployment ejbDeployment = deployments.get(ejbName);


                pruneRefs(bean, ejbDeployment);


//                if (bean.getEjbName().equals("BasicSingletonBean")) {
//                    continue;
//                }
                if (!(bean instanceof MessageDrivenBean) && !(bean instanceof EntityBean)) {
                    continue;
                }

                ejbJar.removeEnterpriseBean(ejbName);
                openejbJar.removeEjbDeployment(ejbDeployment);
                removed.add(ejbName);

                final AssemblyDescriptor assemblyDescriptor = ejbJar.getAssemblyDescriptor();
                if (assemblyDescriptor != null) {
                    for (final MethodPermission permission : copy(assemblyDescriptor.getMethodPermission())) {
                        for (final Method method : copy(permission.getMethod())) {
                            if (method.getEjbName().equals(ejbName)) {
                                permission.getMethod().remove(method);
                            }
                        }
                        if (permission.getMethod().size() == 0) {
                            assemblyDescriptor.getMethodPermission().remove(permission);
                        }
                    }

                    for (final ContainerTransaction transaction : copy(assemblyDescriptor.getContainerTransaction())) {
                        for (final Method method : copy(transaction.getMethod())) {
                            if (method.getEjbName().equals(ejbName)) {
                                transaction.getMethod().remove(method);
                            }
                        }
                        if (transaction.getMethod().size() == 0) {
                            assemblyDescriptor.getContainerTransaction().remove(transaction);
                        }
                    }

                    for (final InterceptorBinding binding : copy(assemblyDescriptor.getInterceptorBinding())) {
                        if (binding.getEjbName().equals(ejbName)) {
                            assemblyDescriptor.getInterceptorBinding().remove(binding);
                        }
                    }
                }
            }

            // Drop any ejb ref to with an ejb-link to a removed ejb
            for (final EnterpriseBean bean : ejbJar.getEnterpriseBeans()) {
                bean.getEjbLocalRefMap().keySet().removeAll(removed);
                bean.getEjbRefMap().keySet().removeAll(removed);
            }

            for (final Interceptor interceptor : ejbJar.getInterceptors()) {
                pruneRefs(interceptor, new EjbDeployment());
            }

        }
        return appModule;
    }

    private void pruneRefs(final JndiConsumer bean, final EjbDeployment ejbDeployment) {
        for (final ResourceRef ref : copy(bean.getResourceRef())) {
            if (ref.getResType().startsWith("jakarta.jms.")) {
                final ResourceLink resourceLink = ejbDeployment.getResourceLink(ref.getName());
                ejbDeployment.getResourceLink().remove(resourceLink);
                bean.getResourceRef().remove(ref);
            }
        }

        for (final ResourceEnvRef ref : bean.getResourceEnvRef()) {
            final ResourceLink resourceLink = ejbDeployment.getResourceLink(ref.getName());
            ejbDeployment.getResourceLink().remove(resourceLink);
        }
        bean.getResourceEnvRef().clear();

        for (final MessageDestinationRef ref : bean.getMessageDestinationRef()) {
            final ResourceLink resourceLink = ejbDeployment.getResourceLink(ref.getName());
            ejbDeployment.getResourceLink().remove(resourceLink);
        }
        bean.getMessageDestinationRef().clear();

        bean.getPersistenceContextRef().clear();
        bean.getPersistenceUnitRef().clear();
    }

    public <T> List<T> copy(final Collection<T> list) {
        return new ArrayList<>(list);
    }
}
