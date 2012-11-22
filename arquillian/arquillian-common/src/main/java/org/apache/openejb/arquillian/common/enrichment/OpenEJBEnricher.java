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
package org.apache.openejb.arquillian.common.enrichment;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InjectionProcessor;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.arquillian.common.mockito.MockitoEnricher;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.inject.AbstractInjectable;
import org.apache.webbeans.inject.OWBInjector;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import java.util.Set;

public final class OpenEJBEnricher {
    private OpenEJBEnricher() {
        // no-op
    }

    public static void enrich(final Object testInstance, final AppContext ctx) {
        if (ctx == null) { // deployment exception
            return;
        }

        // don't rely on arquillian since this enrichment should absolutely be done before the following ones
        new MockitoEnricher().enrich(testInstance);

        final BeanContext context = SystemInstance.get().getComponent(ContainerSystem.class)
                .getBeanContext(testInstance.getClass().getName());

        final BeanManagerImpl bm = ctx.getWebBeansContext().getBeanManagerImpl();
        if (bm.isInUse()) {
            try {
                final Set<Bean<?>> beans = bm.getBeans(testInstance.getClass());
                final Bean<?> bean = bm.resolve(beans);
                final CreationalContext<?> cc = bm.createCreationalContext(bean);
                if (context != null) {
                    context.set(CreationalContext.class, cc);
                }
                AbstractInjectable.instanceUnderInjection.set(testInstance);
                try {
                    OWBInjector.inject(bm, testInstance, cc);
                } finally {
                    AbstractInjectable.instanceUnderInjection.remove();
                }
            } catch (Throwable t) {
                Logger.getInstance(LogCategory.OPENEJB, OpenEJBEnricher.class).error("Can't inject in " + testInstance.getClass(), t);
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                }
                if (t instanceof Exception) {
                    throw new OpenEJBRuntimeException((Exception) t);
                }
                // ignoring other cases for the moment, let manage some OWB API change without making all tests failing
            }
        }

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
}
