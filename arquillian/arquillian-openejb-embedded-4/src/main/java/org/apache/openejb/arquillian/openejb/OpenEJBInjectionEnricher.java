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

import java.lang.reflect.Method;
import java.util.Set;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InjectionProcessor;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.webbeans.inject.OWBInjector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;

public class OpenEJBInjectionEnricher implements TestEnricher {
    @Inject
    private Instance<AppContext> appContext;

    @Inject
    private Instance<ContainerSystem> containerSystem;

    @Override
    public void enrich(final Object testInstance) {
        final AppContext ctx = appContext.get();
        final BeanManager bm = ctx.getWebBeansContext().getBeanManagerImpl();
        try {
            final Set<Bean<?>> beans = bm.getBeans(testInstance.getClass());
            final Bean<?> bean = bm.resolve(beans);
            final OWBInjector beanInjector = new OWBInjector(ctx.getWebBeansContext());
            beanInjector.inject(testInstance, bm.createCreationalContext(bean));
        } catch (Throwable t) {
            // ignored
        }

        final BeanContext context = containerSystem.get().getBeanContext(testInstance.getClass().getName());
        if (context != null) {
            ThreadContext callContext = new ThreadContext(context, null, Operation.INJECTION);
            ThreadContext oldContext = ThreadContext.enter(callContext);
            try {
                final InjectionProcessor processor = new InjectionProcessor<Object>(testInstance, context.getInjections(), context.getJndiContext());
                processor.createInstance();
            } catch (OpenEJBException e) {
                // ignored
            } finally {
                ThreadContext.exit(oldContext);
            }
        }
    }

    @Override
    public Object[] resolve(final Method method) {
        return new Object[method.getParameterTypes().length];
    }
}
