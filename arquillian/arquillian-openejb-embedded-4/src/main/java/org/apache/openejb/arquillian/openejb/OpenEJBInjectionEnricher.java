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
import org.apache.webbeans.inject.AbstractInjectable;
import org.apache.webbeans.inject.OWBInjector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import java.lang.reflect.Method;
import java.util.List;

public class OpenEJBInjectionEnricher implements TestEnricher {
    @Inject
    private Instance<AppContext> appContext;

    @Override
    public void enrich(final Object testInstance) {
        final AppContext context = appContext.get();
        if (context != null) {
            OpenEJBEnricher.enrich(testInstance, context);
        } else { // try to enrich with all for deployment at startup feature - only once context can be used in a class
            new MockitoEnricher().enrich(testInstance);

            final List<AppContext> appContexts = SystemInstance.get().getComponent(ContainerSystem.class).getAppContexts();
            final Class<?> clazz = testInstance.getClass();
            for (final AppContext appContext : appContexts) {
                try {
                    final BeanManager bm = appContext.getWebBeansContext().getBeanManagerImpl();
                    final AnnotatedType<?> at = bm.createAnnotatedType(clazz);
                    final InjectionTarget<Object> it = (InjectionTarget<Object>) bm.createInjectionTarget(at);
                    final CreationalContext<Object> cc = bm.createCreationalContext(null);
                    AbstractInjectable.instanceUnderInjection.set(testInstance);
                    try {
                        OWBInjector.inject(bm, testInstance, cc);
                    } finally {
                        AbstractInjectable.instanceUnderInjection.remove();
                    }
                    cc.release();
                } catch (Exception e) {
                    // no-op
                }
            }
        }
    }

    @Override
    public Object[] resolve(final Method method) {
        return new Object[method.getParameterTypes().length];
    }
}
