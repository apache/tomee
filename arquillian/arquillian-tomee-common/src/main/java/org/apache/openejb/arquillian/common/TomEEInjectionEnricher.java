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
import org.apache.openejb.arquillian.common.enrichment.OpenEJBEnricher;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.TestEnricher;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TomEEInjectionEnricher implements TestEnricher {
    @Inject
    private Instance<TestClass> testClass;

    @Inject
    private Instance<Deployment> deployment;

    @Override
    public void enrich(final Object o) {
        if (!SystemInstance.isInitialized()) {
            return;
        }
        final Class<?> oClass = o.getClass();
        if (oClass.getName().startsWith("org.junit.rules.")) { // no need of enrichments
            return;
        }
        OpenEJBEnricher.enrich(o, getAppContext(oClass));
    }

    private AppContext getAppContext(final Class<?> clazz) {
        final String clazzName = clazz.getName();

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        if (deployment != null && deployment.get() != null) {
            final BeanContext context = containerSystem.getBeanContext(deployment.get().getDescription().getName() + "_" + clazzName);
            if (context != null) {
                return context.getModuleContext().getAppContext();
            }
        }

        final List<AppContext> appContexts = containerSystem.getAppContexts();
        final ClassLoader loader = clazz.getClassLoader();

        for (final AppContext app : appContexts) {
            final BeanContext context = containerSystem.getBeanContext(app.getId() + "_" + clazzName);
            if (context != null) {
                // in embedded mode we have deployment so we dont go here were AppLoader would just be everywhere
                if (context.getBeanClass().getClassLoader() == loader) {
                    return app;
                }
            }
        }

        if (deployment != null && deployment.get() != null && deployment.get().getDescription().testable()
                && !isJunitComponent(clazz) /*app context will be found by classloader, no need to log anything there*/) {
            Logger.getLogger(TomEEInjectionEnricher.class.getName()).log(Level.WARNING, "Failed to find AppContext for: " + clazzName);
        }

        return null;
    }

    private boolean isJunitComponent(final Class<?> clazz) {
        final ClassLoader classLoader = clazz.getClassLoader();
        try {
            return classLoader.loadClass("org.junit.rules.TestRule").isAssignableFrom(clazz)
                    || classLoader.loadClass("org.junit.runners.model.Statement").isAssignableFrom(clazz);
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public Object[] resolve(final Method method) {
        return OpenEJBEnricher.resolve(getAppContext(method.getDeclaringClass()), testClass.get(), method);
    }
}
