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
package org.apache.openejb.arquillian.openejb;

import org.apache.openejb.AppContext;
import org.apache.openejb.arquillian.common.enrichment.OpenEJBEnricher;
import org.apache.openejb.arquillian.common.mockito.MockitoEnricher;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.webbeans.inject.OWBInjector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.TestEnricher;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import java.lang.reflect.Method;
import java.util.List;

public class OpenEJBInjectionEnricher implements TestEnricher {
    @Inject
    private Instance<AppContext> appContext;

    @Inject
    private Instance<TestClass> testClass;

    @Override
    public void enrich(final Object testInstance) {
        if (!SystemInstance.isInitialized()) {
            return;
        }

        new MockitoEnricher().enrich(testInstance);

        final AppContext context = appContext.get();
        if (context != null) {
            OpenEJBEnricher.enrich(testInstance, context);
        } else { // try to enrich with all for deployment at startup feature - only once context can be used in a class
            final List<AppContext> appContexts = SystemInstance.get().getComponent(ContainerSystem.class).getAppContexts();
            final Class<?> clazz = testInstance.getClass();
            for (final AppContext appContext : appContexts) {
                if (appContext.getWebBeansContext() == null) {
                    continue;
                }
                try {
                    final BeanManager bm = appContext.getWebBeansContext().getBeanManagerImpl();
                    final AnnotatedType<?> at = bm.createAnnotatedType(clazz);
                    bm.createInjectionTarget(at);
                    final CreationalContext<Object> cc = bm.createCreationalContext(null);
                    OWBInjector.inject(bm, testInstance, cc);
                    cc.release();
                } catch (final Exception e) {
                    // no-op
                }
            }
        }
    }

    @Override
    public Object[] resolve(final Method method) {
        return OpenEJBEnricher.resolve(appContext.get(), testClass.get(), method);
    }
}
