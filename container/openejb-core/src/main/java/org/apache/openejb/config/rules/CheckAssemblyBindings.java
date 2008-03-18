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
package org.apache.openejb.config.rules;

import static org.apache.openejb.util.Join.join;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.Method;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.util.Join;

import java.util.Map;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class CheckAssemblyBindings extends ValidationBase {
    public void validate(EjbModule ejbModule) {

        Map<String, EnterpriseBean> ejbsByName = ejbModule.getEjbJar().getEnterpriseBeansByEjbName();

        AssemblyDescriptor assembly = ejbModule.getEjbJar().getAssemblyDescriptor();

        if (assembly == null) return;

        for (InterceptorBinding binding : assembly.getInterceptorBinding()) {
            List<String> interceptorClasses = binding.getInterceptorClass();
            if (binding.getInterceptorOrder() != null){
                interceptorClasses.addAll(binding.getInterceptorOrder().getInterceptorClass());
            }

            if (binding.getEjbName() != null && !binding.getEjbName().equals("*") && !ejbsByName.containsKey(binding.getEjbName())) {
                fail("InterceptorBinding", "interceptorBinding.noSuchEjbName", binding.getEjbName(), join(",", interceptorClasses));
            }

            if (binding.getMethod() != null) {
                if (binding.getEjbName() == null) {
                    fail("InterceptorBinding", "interceptorBinding.ejbNameRequiredWithMethod", binding.getMethod().getMethodName(), join(",", interceptorClasses));
                }
            }
        }

        for (MethodPermission permission : assembly.getMethodPermission()) {
            for (Method method : permission.getMethod()) {
                if (method.getEjbName() == null) {
                    fail("MethodPermission", "methodPermission.ejbNameRequired", method.getMethodName(), join(",", permission.getRoleName()));
                } else if (method.getEjbName().equals("*")){
                } else if (!ejbsByName.containsKey(method.getEjbName())){
                    fail("MethodPermission", "methodPermission.noSuchEjbName", method.getEjbName(), method.getMethodName(), join(",", permission.getRoleName()));
                }
            }
        }

        for (ContainerTransaction transaction : assembly.getContainerTransaction()) {
            for (Method method : transaction.getMethod()) {
                if (method.getEjbName() == null) {
                    fail("ContainerTransaction", "containerTransaction.ejbNameRequired", method.getMethodName(), transaction.getTransAttribute());
                } else if (method.getEjbName().equals("*")){
                } else if (!ejbsByName.containsKey(method.getEjbName())){
                    fail("ContainerTransaction", "containerTransaction.noSuchEjbName", method.getEjbName(), method.getMethodName(), transaction.getTransAttribute());
                }
            }
        }
    }

}
