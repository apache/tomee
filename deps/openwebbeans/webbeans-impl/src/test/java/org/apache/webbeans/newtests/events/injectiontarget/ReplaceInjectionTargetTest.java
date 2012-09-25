/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.newtests.events.injectiontarget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.inject.Inject;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ReplaceInjectionTargetTest extends AbstractUnitTest
{
    public static class IJBean {
        @Inject
        private InjectedBean injection;
    }

    public static class InjectedBean {

    }

    public static class MyInjectionTarget implements InjectionTarget<IJBean> {
        private static boolean injected = false;

        private final InjectionTarget<IJBean> injectionTarget;

        public MyInjectionTarget(InjectionTarget<IJBean> injectionTarget) {
            this.injectionTarget = injectionTarget;
        }

        public void inject(IJBean instance, CreationalContext<IJBean> ctx) {
            injected = true;
            injectionTarget.inject(instance, ctx);
        }

        public void postConstruct(IJBean instance) {
            injectionTarget.postConstruct(instance);
        }

        public void preDestroy(IJBean instance) {
            injectionTarget.preDestroy(instance);
        }

        public IJBean produce(CreationalContext<IJBean> ijBeanCreationalContext) {
            return injectionTarget.produce(ijBeanCreationalContext);
        }

        public void dispose(IJBean instance) {
            injectionTarget.dispose(instance);
        }

        public Set<InjectionPoint> getInjectionPoints() {
            return injectionTarget.getInjectionPoints();
        }
    }

    public static class InjectionTargetReplacer implements Extension {
        public void replaceInjectionTarget(@Observes ProcessInjectionTarget<IJBean> event) {
            event.setInjectionTarget(new MyInjectionTarget(event.getInjectionTarget()));
        }
    }

    @Test
    public void checkCustomWrapperIsUsed() {
        addExtension(new InjectionTargetReplacer());

        final Collection<String> beanXmls = new ArrayList<String>();

        final Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(IJBean.class);
        beanClasses.add(InjectedBean.class);

        startContainer(beanClasses, beanXmls);

        final Set<Bean<?>> beans = getBeanManager().getBeans(IJBean.class);
        assertNotNull(beans);
        assertFalse(beans.isEmpty());
        assertNotNull(getBeanManager().getReference(beans.iterator().next(), IJBean.class, null));
        assertTrue(MyInjectionTarget.injected);

        shutDownContainer();
    }
}
