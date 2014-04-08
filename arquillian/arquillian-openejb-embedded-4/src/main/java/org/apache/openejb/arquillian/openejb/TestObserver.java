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

import org.apache.openejb.BeanContext;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.jboss.arquillian.container.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.ClassEvent;

import javax.enterprise.context.spi.CreationalContext;

public class TestObserver {
    @Inject
    private Instance<ClassLoader> classLoader;

    @Inject
    private Instance<TestClass> testClass;

    public void observes(@Observes final EventContext<ClassEvent> event) {
        final BeanContext context = beanContext();
        ThreadContext oldCtx = null;
        ClassLoader oldCl = null;

        if (context != null) {
            oldCtx = ThreadContext.enter(new ThreadContext(context, null));
        } else {
            oldCl = Thread.currentThread().getContextClassLoader();
            if (classLoader.get() != null) {
                setTCCL(classLoader.get());
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
        return SystemInstance.get().getComponent(ContainerSystem.class)
                .getBeanContext(testClass.get().getName());
    }
}
