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
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.CallbackInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.InterceptorBindingInfo;
import org.apache.openejb.assembler.classic.InterceptorInfo;
import org.apache.openejb.assembler.classic.MethodConcurrencyInfo;
import org.apache.openejb.assembler.classic.MethodInfo;
import org.apache.openejb.assembler.classic.SingletonBeanInfo;
import org.apache.openejb.assembler.classic.SingletonSessionContainerInfo;

import java.net.URI;

public final class SystemAppInfo {
    //
    //
    // DONT MODIFY IT WITHOUT UPDATING org.apache.openejb.config.SystemApps
    //
    //
    public static AppInfo preComputedInfo(final ConfigurationFactory factory) {
        final String singletonContainerId;
        try {
            singletonContainerId = findSingletonContainer(factory);
        } catch (final OpenEJBException e) {
            throw new IllegalStateException(e);
        }

        final EjbJarInfo ejbJarInfo = new EjbJarInfo();
        ejbJarInfo.moduleId = "openejb";
        ejbJarInfo.moduleName = ejbJarInfo.moduleId;
        ejbJarInfo.moduleUri = URI.create(ejbJarInfo.moduleId);
        ejbJarInfo.properties.setProperty("openejb.deploymentId.format", "{ejbName}");
        ejbJarInfo.properties.setProperty("openejb.jndiname.format", "{deploymentId}{interfaceType.openejbLegacyName}");

        final SingletonBeanInfo deployer = new SingletonBeanInfo();
        deployer.ejbDeploymentId = "openejb/Deployer";
        deployer.ejbName = deployer.ejbDeploymentId;
        deployer.ejbClass = "org.apache.openejb.assembler.DeployerEjb";
        deployer.businessRemote.add("org.apache.openejb.assembler.Deployer");
        deployer.parents.add(deployer.ejbClass);
        deployer.transactionType = "BEAN";
        deployer.concurrencyType = "CONTAINER";
        deployer.containerId = singletonContainerId;
        ejbJarInfo.enterpriseBeans.add(deployer);

        final SingletonBeanInfo configuration = new SingletonBeanInfo();
        configuration.ejbDeploymentId = "openejb/ConfigurationInfo";
        configuration.ejbName = deployer.ejbDeploymentId;
        configuration.ejbClass = "org.apache.openejb.assembler.classic.cmd.ConfigurationInfoEjb";
        configuration.businessRemote.add("org.apache.openejb.assembler.classic.cmd.ConfigurationInfo");
        configuration.parents.add(deployer.ejbClass);
        configuration.transactionType = "CONTAINER";
        configuration.concurrencyType = "CONTAINER";
        configuration.containerId = singletonContainerId;
        ejbJarInfo.enterpriseBeans.add(configuration);

        final SingletonBeanInfo mejb = new SingletonBeanInfo();
        mejb.ejbDeploymentId = "MEJB";
        mejb.ejbName = deployer.ejbDeploymentId;
        mejb.ejbClass = "org.apache.openejb.mgmt.MEJBBean";
        mejb.home = "javax.management.j2ee.ManagementHome";
        mejb.remote = "javax.management.j2ee.Management";
        mejb.parents.add(deployer.ejbClass);
        mejb.transactionType = "CONTAINER";
        mejb.concurrencyType = "CONTAINER";
        mejb.containerId = singletonContainerId;
        ejbJarInfo.enterpriseBeans.add(mejb);

        for (final EnterpriseBeanInfo ebi : ejbJarInfo.enterpriseBeans) {
            final MethodInfo methodInfo = new MethodInfo();
            methodInfo.ejbDeploymentId = ebi.ejbDeploymentId;
            methodInfo.ejbName = ebi.ejbName;
            methodInfo.methodName = "*";
            methodInfo.className = ebi.ejbClass;

            final MethodConcurrencyInfo methodConcurrencyInfo = new MethodConcurrencyInfo();
            methodConcurrencyInfo.concurrencyAttribute = "READ";
            methodConcurrencyInfo.methods.add(methodInfo);
            ejbJarInfo.methodConcurrency.add(methodConcurrencyInfo);
        }

        final CallbackInfo callbackInfo = new CallbackInfo();
        callbackInfo.className = "org.apache.openejb.security.internal.InternalSecurityInterceptor";
        callbackInfo.method = "invoke";

        final InterceptorInfo interceptorInfo = new InterceptorInfo();
        interceptorInfo.clazz = "org.apache.openejb.security.internal.InternalSecurityInterceptor";
        interceptorInfo.aroundInvoke.add(callbackInfo);
        ejbJarInfo.interceptors.add(interceptorInfo);

        final InterceptorBindingInfo interceptorBindingInfo = new InterceptorBindingInfo();
        interceptorBindingInfo.ejbName = "*";
        interceptorBindingInfo.interceptors.add("org.apache.openejb.security.internal.InternalSecurityInterceptor");
        ejbJarInfo.interceptorBindings.add(interceptorBindingInfo);

        ejbJarInfo.mbeans.add("org.apache.openejb.assembler.monitoring.JMXDeployer");
        ejbJarInfo.uniqueId = "0"; // we start at 1 so no conflict using 0

        final AppInfo appInfo = new AppInfo();
        appInfo.appId = ejbJarInfo.moduleId;
        appInfo.path = appInfo.appId;
        appInfo.ejbJars.add(ejbJarInfo);
        return appInfo;
    }

    // simplified logic compared to AutoConfig
    private static String findSingletonContainer(final ConfigurationFactory configFactory) throws OpenEJBException {
        for (final ContainerInfo containerInfo : configFactory.getContainerInfos()) {
            if (SingletonSessionContainerInfo.class.isInstance(containerInfo)) {
                return containerInfo.id;
            }
        }
        if (configFactory.isOffline()) {
            throw new IllegalStateException("system application (openejb) needs a singleton container. " +
                    "Noone is defined and container is in offline mode. " +
                    "Please define one in tomee.xml.");
        }
        final ContainerInfo containerInfo = configFactory.configureService(SingletonSessionContainerInfo.class);
        configFactory.install(containerInfo);
        return containerInfo.id;
    }

    private SystemAppInfo() {
        // no-op
    }
}
