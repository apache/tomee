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
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.ClassLifecycleEvent;
import org.jboss.arquillian.test.spi.event.suite.Test;
import org.jboss.arquillian.test.spi.event.suite.TestLifecycleEvent;

public class TestObserver {
    @Inject
    @SuiteScoped
    private Instance<ClassLoader> classLoader;

    private void setClassLoader(final Class<?> clazz, final Runnable run) {
        final BeanContext context = SystemInstance.get().getComponent(ContainerSystem.class)
                .getBeanContext(clazz.getName());
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
            run.run();
        } finally {
            if (context != null) {
                ThreadContext.exit(oldCtx);
            } else {
                setTCCL(oldCl);
            }
        }
    }

    public void observe(@Observes final EventContext<Test> event) {
        setClassLoader(event.getEvent().getTestClass().getJavaClass(), new Runnable() {
            @Override
            public void run() {
                event.proceed();
            }
        });
    }

    public void on(@Observes(precedence = 200) final BeforeClass event) throws Throwable {
        setClassLoader(event.getTestClass().getJavaClass(), new CLassEventRunnable(event));
    }

    public void on(@Observes(precedence = 200) AfterClass event) throws Throwable {
        setClassLoader(event.getTestClass().getJavaClass(), new CLassEventRunnable(event));
    }

    public void on(@Observes(precedence = 200) Before event) throws Throwable {
        setClassLoader(event.getTestClass().getJavaClass(), new LifeCycleEventRunnable(event));
    }

    public void on(@Observes(precedence = 200) After event) throws Throwable {
        setClassLoader(event.getTestClass().getJavaClass(), new LifeCycleEventRunnable(event));
    }

    private void setTCCL(final ClassLoader cl) {
        Thread.currentThread().setContextClassLoader(cl);
    }

    private static class LifeCycleEventRunnable implements Runnable {
        private final TestLifecycleEvent event;

        public LifeCycleEventRunnable(final TestLifecycleEvent e) {
            event = e;
        }

        @Override
        public void run() {
            try {
                event.getExecutor().invoke();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
    }

    private static class CLassEventRunnable implements Runnable {
        private final ClassLifecycleEvent event;

        public CLassEventRunnable(final ClassLifecycleEvent e) {
            event = e;
        }

        @Override
        public void run() {
            try {
                event.getExecutor().invoke();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
    }
}
