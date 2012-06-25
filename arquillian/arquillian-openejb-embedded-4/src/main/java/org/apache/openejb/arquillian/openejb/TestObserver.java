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

import java.lang.reflect.Field;
import org.apache.openejb.BeanContext;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.core.spi.event.Event;
import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.ClassEvent;
import org.jboss.arquillian.test.spi.event.suite.LifecycleEvent;
import org.jboss.arquillian.test.spi.event.suite.Test;
import org.jboss.arquillian.test.spi.event.suite.TestEvent;

public class TestObserver {
    @Inject
    @SuiteScoped
    private Instance<ClassLoader> classLoader;

    @Inject
    @ClassScoped
    private Instance<TestClass> testClass;

    private void setClassLoader(final EventContext<ClassEvent> event) {
        final BeanContext context = SystemInstance.get().getComponent(ContainerSystem.class)
                .getBeanContext(testClass.get().getName());
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

    public void observes(@Observes final EventContext<ClassEvent> event) {
        setClassLoader(event);
    }

    private void setTCCL(final ClassLoader cl) {
        Thread.currentThread().setContextClassLoader(cl);
    }
}
