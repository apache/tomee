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
package org.apache.openejb.testing;

import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.FileArchive;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.openejb.loader.JarLocation.jarLocation;

// goal is to share the same container for all embedded tests and hold the config there
// only works if all tests use the same config
public class SingleApplicationComposerRunner extends BlockJUnit4ClassRunner {
    private static volatile boolean started = false;
    private static final AtomicReference<Object> APP = new AtomicReference<>();
    private static final AtomicReference<Thread> HOOK = new AtomicReference<>();

    // use when you use another runner like Parameterized of JUnit
    public static class Rule implements TestRule {
        private final Object test;

        public Rule(final Object test) {
            this.test = test;
        }

        @Override
        public Statement apply(final Statement base, final Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    start(test.getClass());
                    composerInject(test);
                    base.evaluate();
                }
            };
        }
    }

    public static class Start extends RunListener {
        @Override
        public void testStarted(final Description description) throws Exception {
            start(null);
        }
    }

    public static void setApp(final Object o) {
        APP.set(o);
    }

    public static void close() {
        final Thread hook = HOOK.get();
        if (hook != null) {
            hook.run();
            Runtime.getRuntime().removeShutdownHook(hook);
            HOOK.compareAndSet(hook, null);
            APP.set(null);
        }
    }

    public SingleApplicationComposerRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<MethodRule> rules(final Object test) {
        final List<MethodRule> rules = super.rules(test);
        rules.add(new MethodRule() {
            @Override
            public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
                return new Statement() {
                    @Override
                    public void evaluate() throws Throwable {
                        start(getTestClass().getJavaClass());
                        composerInject(target);
                        base.evaluate();
                    }
                };
            }
        });
        return rules;
    }

    private static void start(final Class<?> marker) throws Exception {
        if (APP.get() == null) {
            final Class<?> type;
            final String typeStr = JavaSecurityManagers.getSystemProperty("tomee.application-composer.application");
            if (typeStr != null) {
                try {
                    type = Thread.currentThread().getContextClassLoader().loadClass(typeStr);
                } catch (final ClassNotFoundException e) {
                    throw new IllegalArgumentException(e);
                }
            } else if (marker == null) {
                throw new IllegalArgumentException("set tomee.application-composer.application system property or add a marker to the rule or runner");
            } else {
                final Iterator<Class<?>> descriptors =
                    new AnnotationFinder(new FileArchive(Thread.currentThread().getContextClassLoader(), jarLocation(marker)), false)
                        .findAnnotatedClasses(Application.class).iterator();
                if (!descriptors.hasNext()) {
                    throw new IllegalArgumentException("No descriptor class using @Application");
                }
                type = descriptors.next();
                if (descriptors.hasNext()) {
                    throw new IllegalArgumentException("Ambiguous @Application: " + type + ", " + descriptors.next());
                }
            }
            try {
                APP.compareAndSet(null, type.newInstance());
            } catch (final InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        if (!started) {
            final Object app = APP.get();
            final ApplicationComposers composers = new ApplicationComposers(app.getClass()) {
                @Override
                public void deployApp(final Object inputTestInstance) throws Exception {
                    super.deployApp(inputTestInstance);
                    if (!started) {
                        final ThreadContext previous = ThreadContext.getThreadContext(); // done here for logging
                        final ApplicationComposers comp = this;
                        final Thread hook = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    comp.after();
                                } catch (final Exception e) {
                                    ThreadContext.exit(previous);
                                    throw new IllegalStateException(e);
                                }
                            }
                        };
                        HOOK.set(hook);
                        Runtime.getRuntime().addShutdownHook(hook);
                        started = true;
                    }
                }
            };
            composers.before(app);
            composers.handleLifecycle(app.getClass(), app);
        }
    }

    private static void composerInject(final Object target) throws IllegalAccessException {
        WebBeansContext wbc = null;
        try {
            wbc = WebBeansContext.currentInstance();
        } catch (final IllegalStateException ise) {
            // no-op
        }
        if (wbc != null) {
            OWBInjector.inject(wbc.getBeanManagerImpl(), target, null);
        }

        final Object app = APP.get();
        final Class<?> aClass = target.getClass();
        for (final Field f : aClass.getDeclaredFields()) {
            if (f.isAnnotationPresent(RandomPort.class)) {
                for (final Field field : app.getClass().getDeclaredFields()) {
                    if (field.getType() ==  f.getType()) {
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        if (!f.isAccessible()) {
                            f.setAccessible(true);
                        }

                        final Object value = field.get(app);
                        f.set(target, value);
                        break;
                    }
                }
            } else if (f.isAnnotationPresent(Application.class)) {
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                f.set(target, app);
            }
        }
        final Class<?> superclass = aClass.getSuperclass();
        if (superclass != Object.class) {
            composerInject(superclass);
        }
    }
}
