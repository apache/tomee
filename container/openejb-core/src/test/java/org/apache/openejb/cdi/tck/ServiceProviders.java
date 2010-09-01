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
package org.apache.openejb.cdi.tck;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.core.AppContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.webbeans.container.BeanManagerImpl;
import org.jboss.testharness.api.DeploymentException;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.jee.EjbJar;
import org.apache.xbean.finder.ClassFinder;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Interceptor;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.lang.reflect.Type;
import java.lang.annotation.Annotation;

/**
 * @version $Rev$ $Date$
 */
public class ServiceProviders {


    private static AppContext appContext;

    public static class ManagersProvider implements org.jboss.jsr299.tck.spi.Managers {
        public BeanManager getManager() {
            System.out.println("ServiceProviders$ManagersProvider.getManager");
            return appContext.getBeanManager();
        }

    }

    public static class BeansProvider implements org.jboss.jsr299.tck.spi.Beans {
        public boolean isProxy(Object instance) {
            System.out.println("ServiceProviders$BeansProvider.isProxy");
            return false;
        }
    }

    public static class ContextsProvider implements org.jboss.jsr299.tck.spi.Contexts {
        public void setActive(Context context) {
            System.out.println("ServiceProviders$ContextsProvider.setActive");
        }

        public void setInactive(Context context) {
            System.out.println("ServiceProviders$ContextsProvider.setInactive");
        }

        public Context getRequestContext() {
            System.out.println("ServiceProviders$ContextsProvider.getRequestContext");
            return null;
        }

        public Context getDependentContext() {
            System.out.println("ServiceProviders$ContextsProvider.getDependentContext");
            return null;
        }

        public void destroyContext(Context context) {
            System.out.println("ServiceProviders$ContextsProvider.destroyContext");
        }
    }

    public static class StandaloneContainersProvider implements org.jboss.testharness.spi.StandaloneContainers {

        private DeploymentException deploymentException;

        public void deploy(Collection<Class<?>> classes) throws DeploymentException {
            System.out.println("ServiceProviders$StandaloneContainersProvider.deploy");
            System.out.println("StandaloneContainersImpl.deploy(classes)");
            for (Class<?> clazz : classes) {
                System.out.println("clazz = " + clazz);
            }
        }

        public boolean deploy(Collection<Class<?>> classes, Collection<URL> urls) {
            System.out.println("ServiceProviders$StandaloneContainersProvider.deploy");
            List<String> classNames = new ArrayList<String>();

            for (Class<?> clazz : classes) classNames.add(clazz.getName());
            Collections.sort(classNames);

            for (String clazz : classNames) {
                System.out.println("clazz = " + clazz);
            }

            for (URL url : urls) {
                System.out.println("url = " + url);
            }
            try {
                EjbModule ejbModule = new EjbModule(new EjbJar("beans"));
                ejbModule.setFinder(new ClassFinder(new ArrayList(classes)));

                Map<String,Object> dds = ejbModule.getAltDDs();

                for (URL url : urls) {
                    final File file = new File(url.getFile());
                    dds.put(file.getName(), url);
                }

                Assembler assembler = new Assembler();
                ConfigurationFactory config = new ConfigurationFactory();
                assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
                assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));

                final EjbJarInfo ejbJar = config.configureApplication(ejbModule);
                ejbJar.beans.managedClasses.addAll(classNames);


                assembler.createApplication(ejbJar);

                final ContainerSystem component = SystemInstance.get().getComponent(ContainerSystem.class);

                final CoreDeploymentInfo deploymentInfo = (CoreDeploymentInfo) component.deployments()[0];
                appContext = deploymentInfo.getModuleContext().getAppContext();

                // This must be set or the OWB static lookup code won't work and everything will fall apart
                Thread.currentThread().setContextClassLoader(appContext.getClassLoader());

            } catch (Exception e) {
                e.printStackTrace();
                deploymentException = new DeploymentException("Deploy failed", e);
                return false;
            }
//            System.out.println("StandaloneContainersImpl.deploy(classes, urls)");
//            for (Class<?> clazz : classes) {
//                System.out.println("clazz = " + clazz);
//            }
//            for (URL url : urls) {
//                System.out.println("url = " + url);
//            }
            return true;
        }

        public DeploymentException getDeploymentException() {
            System.out.println("ServiceProviders$StandaloneContainersProvider.getDeploymentException");
            return deploymentException;
        }

        public void undeploy() {
            System.out.println("ServiceProviders$StandaloneContainersProvider.undeploy");
        }

        public void setup() {
            System.out.println("ServiceProviders$StandaloneContainersProvider.setup");
        }

        public void cleanup() {
            System.out.println("ServiceProviders$StandaloneContainersProvider.cleanup");
        }
    }

    public static class ELProvider implements org.jboss.jsr299.tck.spi.EL {
        public <T> T evaluateValueExpression(String expression, Class<T> expectedType) {
            System.out.println("ServiceProviders$ELProvider.evaluateValueExpression");
            return null;
        }

        public <T> T evaluateMethodExpression(String expression, Class<T> expectedType, Class<?>[] expectedParamTypes, Object[] expectedParams) {
            System.out.println("ServiceProviders$ELProvider.evaluateMethodExpression");
            return null;
        }

        public ELContext createELContext() {
            System.out.println("ServiceProviders$ELProvider.createELContext");
            return null;
        }
    }

}
