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

import org.apache.openejb.assembler.DeployerEjb;
import org.apache.openejb.assembler.classic.cmd.ConfigurationInfoEjb;
import org.apache.openejb.assembler.monitoring.JMXDeployer;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.mgmt.MEJBBean;
import org.apache.openejb.security.internal.InternalSecurityInterceptor;

/**
 * Avoids the needs to scan the classpath to load system applications that are used
 * for deploy tools and other command line tooling.
 */
// see org.apache.openejb.config.SystemAppInfo
public class SystemApps {

    public static EjbModule getSystemModule() {
        final EjbModule module = new EjbModule(new EjbJar("openejb"), new OpenejbJar());

        final OpenejbJar openejbJar = module.getOpenejbJar();
        final EjbJar ejbJar = module.getEjbJar();

        //
        //
        // DONT MODIFY IT WITHOUT VALIDATING org.apache.openejb.config.SystemAppInfo.preComputedInfo()
        //
        //
        ejbJar.addEnterpriseBean(singleton(DeployerEjb.class));
        ejbJar.addEnterpriseBean(singleton(ConfigurationInfoEjb.class));
        ejbJar.addEnterpriseBean(singleton(MEJBBean.class));
        ejbJar.addInterceptor(new Interceptor(InternalSecurityInterceptor.class));
        ejbJar.getAssemblyDescriptor().addInterceptorBinding(new InterceptorBinding("*", InternalSecurityInterceptor.class.getName()));
        module.getMbeans().add(JMXDeployer.class.getName());

        final SingletonBean bean = ejbJar.addEnterpriseBean(new SingletonBean("openejb/WebappDeployer", "org.apache.tomee.catalina.deployer.WebappDeployer"));
        final EjbDeployment deployment = openejbJar.addEjbDeployment(bean);
        deployment.getProperties().put("openejb.jndiname.format", "{deploymentId}{interfaceType.annotationName}");

        final SingletonBean exceptionManager = ejbJar.addEnterpriseBean(new SingletonBean("openejb/ExceptionManagerFacade", "org.apache.tomee.catalina.facade.ExceptionManagerFacadeBean"));
        final EjbDeployment exceptionMgr = openejbJar.addEjbDeployment(exceptionManager);
        exceptionMgr.getProperties().put("openejb.jndiname.format", "{deploymentId}{interfaceType.annotationName}");

        openejbJar.getProperties().put("openejb.deploymentId.format", "{ejbName}");
        openejbJar.getProperties().put("openejb.jndiname.format", "{deploymentId}{interfaceType.openejbLegacyName}");

        return module;
    }

    public static boolean isExtended() {
        return exists("org.apache.tomee.catalina.deployer.WebappDeployer");
    }

    private static SingletonBean singleton(final Class<?> ejbClass) {
        return new SingletonBean(null, ejbClass);
    }

    private static boolean exists(final String className) {
        try {
            SystemApps.class.getClassLoader().loadClass(className);
            return true;
        } catch (final Throwable t) {
            return false;
        }
    }
}
