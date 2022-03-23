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
package org.apache.openejb.arquillian.common;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.TestEvent;

import jakarta.enterprise.context.spi.CreationalContext;
import java.util.HashMap;
import java.util.Map;

public class TestObserver {
    @Inject
    private Instance<ClassLoaders> classLoader;

    @Inject
    private Instance<TestClass> testClass;

    @Inject
    @DeploymentScoped
    private InstanceProducer<DeploymentContext> contextProducer;

    @Inject
    private Instance<DeploymentContext> context;

    public void observesDeploy(@Observes final AfterDeploy afterDeployment) {
        contextProducer.set(new DeploymentContext(Thread.currentThread().getContextClassLoader()/*use as a fallback, multiple deployment can have webapp loader*/));
        final ClassLoaders loader = classLoader.get();
        final String name = afterDeployment.getDeployment().getArchive() != null ? afterDeployment.getDeployment().getArchive().getName() : null;
        if (loader != null && loader.classloaders.containsKey(name)) {
            setTCCL(loader.classloaders.get(name));
        }
    }

    public void observesUndeploy(@Observes final BeforeUnDeploy beforeUnDeploy) {
        final ClassLoaders loader = classLoader.get();
        if (loader != null) { // with multiple deployments order is not guaranteed so we reset it the hard way
            setTCCL(loader.original);
        } else {
            final DeploymentContext deploymentContext = context.get();
            if (deploymentContext != null) {
                setTCCL(deploymentContext.loader);
            }
        }
    }

    public void observesTest(@Observes final EventContext<TestEvent> event) {
        switchLoader(event);
    }

    private void switchLoader(final EventContext<?> event) {
        if (!SystemInstance.isInitialized()) {
            event.proceed();
            return;
        }
        final BeanContext context = beanContext();
        ThreadContext oldCtx = null;
        ClassLoader oldCl = null;
        if (context != null) {
            oldCtx = ThreadContext.enter(new ThreadContext(context, null));
        } else {
            oldCl = Thread.currentThread().getContextClassLoader();
            final ClassLoaders classLoaders = classLoader.get();
            if (classLoaders != null) {
                final ClassLoader loader = classLoaders.classloaders.size() == 1 /*assume it is the one we want*/ ?
                        classLoaders.classloaders.values().iterator().next() : oldCl /* we don't know the deployment so just passthrough */;
                setTCCL(loader);
            }
        }
        try {
            event.proceed();
        } finally {
            if (context != null) {
                ThreadContext.exit(oldCtx);
            } else {
                setTCCL(oldCl);
            }
        }
    }

    private void setTCCL(final ClassLoader cl) {
        Thread.currentThread().setContextClassLoader(cl);
    }

    public void release(@Observes final EventContext<BeforeUnDeploy> event) {
        if (!SystemInstance.isInitialized()) {
            event.proceed();
            return;
        }

        try {
            event.proceed();
        } finally {
            final BeanContext bc = beanContext();
            if (bc != null) { // can be null if deployment exception
                final CreationalContext<?> cc = bc.get(CreationalContext.class);
                if (cc != null) {
                    cc.release();
                }
            }
        }
    }

    private BeanContext beanContext() {
        final TestClass tc = testClass.get();
        if (tc == null) {
            return null;
        }

        final String className = tc.getName();
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        for (final AppContext app : containerSystem.getAppContexts()) {
            final BeanContext context = containerSystem.getBeanContext(app.getId() + "_" + className);
            if (context != null) {
                return context;
            }
        }
        return null;
    }

    public static class DeploymentContext {
        private final ClassLoader loader;

        public DeploymentContext(final ClassLoader loader) {
            this.loader = loader;
        }
    }

    public static class ClassLoaders { // to support multiple deployments otherwise we can lead a broken classloader
        private final ClassLoader original = Thread.currentThread().getContextClassLoader(); // created once for multiple deployment so likely container one
        private final Map<String, ClassLoader> classloaders = new HashMap<>();

        public void register(final String name, final ClassLoader loader) {
            classloaders.put(name, loader);
        }

        public void unregister(final String name) {
            classloaders.remove(name);
        }
    }
}
