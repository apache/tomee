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
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.Method;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
class RemoveWebServices implements DynamicDeployer {

    public AppModule deploy(AppModule appModule) throws OpenEJBException {

        for (EjbModule ejbModule : appModule.getEjbModules()) {
            EjbJar ejbJar = ejbModule.getEjbJar();
            OpenejbJar openejbJar = ejbModule.getOpenejbJar();
            Map<String, EjbDeployment> deployments = openejbJar.getDeploymentsByEjbName();

            for (EnterpriseBean bean : ejbJar.getEnterpriseBeans()) {

                String ejbName = bean.getEjbName();
                EjbDeployment ejbDeployment = deployments.get(ejbName);

                // Clear any <service-ref> references from ejbs
                bean.getServiceRef().clear();

                if (!(bean instanceof SessionBean)) {
                    continue;
                }

                SessionBean sessionBean = (SessionBean) bean;

                if (sessionBean.getServiceEndpoint() == null) continue;

                sessionBean.setServiceEndpoint(null);

                // Now check if the bean has no other interfaces
                // if not, then we should just delete it
                if (sessionBean.getHome() != null) continue;
                if (sessionBean.getLocalHome() != null) continue;
                if (sessionBean.getBusinessLocal().size() > 0) continue;
                if (sessionBean.getBusinessRemote().size() > 0) continue;

                // Ok, delete away...
                ejbJar.removeEnterpriseBean(ejbName);
                openejbJar.removeEjbDeployment(ejbDeployment);

                // As well, let's get rid of any transaction or security attributes
                // associated with the bean we just deleted.
                AssemblyDescriptor assemblyDescriptor = ejbJar.getAssemblyDescriptor();
                if (assemblyDescriptor != null) {
                    for (MethodPermission permission : copy(assemblyDescriptor.getMethodPermission())) {
                        for (Method method : copy(permission.getMethod())) {
                            if (method.getEjbName().equals(ejbName)) {
                                permission.getMethod().remove(method);
                            }
                        }
                        if (permission.getMethod().size() == 0) {
                            assemblyDescriptor.getMethodPermission().remove(permission);
                        }
                    }

                    for (ContainerTransaction transaction : copy(assemblyDescriptor.getContainerTransaction())) {
                        for (Method method : copy(transaction.getMethod())) {
                            if (method.getEjbName().equals(ejbName)) {
                                transaction.getMethod().remove(method);
                            }
                        }
                        if (transaction.getMethod().size() == 0) {
                            assemblyDescriptor.getContainerTransaction().remove(transaction);
                        }
                    }

                    for (InterceptorBinding binding : copy(assemblyDescriptor.getInterceptorBinding())) {
                        if (binding.getEjbName().equals(ejbName)) {
                            assemblyDescriptor.getInterceptorBinding().remove(binding);
                        }
                    }
                }
            }

            // Clear any <service-ref> references from interceptors
            for (Interceptor interceptor : ejbJar.getInterceptors()) {
                interceptor.getServiceRef().clear();
            }

        }
        return appModule;
    }

    public <T> List<T> copy(Collection<T> list) {
        return new ArrayList<T>(list);
    }
}
