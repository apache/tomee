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
package org.apache.tomee.embedded.junit;

import org.apache.openejb.testing.Application;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.RandomPort;
import org.apache.openejb.testing.WebResource;
import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.apache.xbean.finder.archive.FileArchive;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import javax.enterprise.inject.Vetoed;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.openejb.loader.JarLocation.jarLocation;
import static org.apache.openejb.util.Classes.ancestors;

/**
 * see org.apache.tomee.embedded.SingleInstanceRunnerTest for a sample.
 * idea is to reuse some part of ApplicationComposer API to get a single container for all tests in embedded mode.
 *
 * Base is to declare an @Application class which holds the model and some injections.
 * Note: this can be replaced setting tomee.application-composer.application property to the fully qualified name of the app.
 * Note: @Application classes are only searched in the same jar as the test.
 *
 * Model:
 * - @Configuration: programmatic properties - note injections don't work there.
 * - @Classes: only context value is used.
 * - @ContainerProperties: to configure the container
 * - @WebResource: first value can be used to set the docBase (other values are ignored)
 * - @TomEEEmbeddedSingleRunner.LifecycleTasks: allow to add some lifecycle tasks (like starting a ftp/sft/elasticsearch... server)
 *
 * Injections:
 * - CDI
 * - @RandomPort: with the value http or https. Supported types are URL (context base) and int (the port).
 */
@Vetoed
public class TomEEEmbeddedSingleRunner extends BlockJUnit4ClassRunner {
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

    public TomEEEmbeddedSingleRunner(final Class<?> klass) throws InitializationError {
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
            final String typeStr = System.getProperty("tomee.application-composer.application");
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
            synchronized (TomEEEmbeddedSingleRunner.class) {
                started = true;

                final Class<?> appClass = APP.get().getClass();
                final AnnotationFinder finder = new AnnotationFinder(new ClassesArchive(ancestors(appClass)));

                // setup the container config reading class annotation, using a randome http port and deploying the classpath
                final Configuration configuration = new Configuration();
                final ContainerProperties props = appClass.getAnnotation(ContainerProperties.class);
                if (props != null) {
                    for (final ContainerProperties.Property p : props.value()) {
                        configuration.property(p.name(), p.value());
                    }
                }

                final List<Method> annotatedMethods = finder.findAnnotatedMethods(org.apache.openejb.testing.Configuration.class);
                if (annotatedMethods.size() > 1) {
                    throw new IllegalArgumentException("Only one @Configuration is supported: " + annotatedMethods);
                }
                for (final Method m : annotatedMethods) {
                    final Object o = m.invoke(APP.get());
                    if (Properties.class.isInstance(o)) {
                        final Properties properties = Properties.class.cast(o);
                        if (configuration.getProperties() == null) {
                            configuration.setProperties(new Properties());
                        }
                        configuration.getProperties().putAll(properties);
                    } else {
                        throw new IllegalArgumentException("Unsupported " + o + " for @Configuration");
                    }
                }

                final Collection<Closeable> postTasks = new ArrayList<>();
                final LifecycleTasks tasks = appClass.getAnnotation(LifecycleTasks.class);
                if (tasks != null) {
                    for (final Class<? extends org.apache.tomee.embedded.LifecycleTask> type : tasks.value()) {
                        postTasks.add(type.newInstance().beforeContainerStartup());
                    }
                }

                final Map<String, Field> ports = new HashMap<>();
                {
                    Class<?> type = appClass;
                    while (type != null && type != Object.class) {
                        for (final Field f : type.getDeclaredFields()) {
                            final RandomPort annotation = f.getAnnotation(RandomPort.class);
                            final String value = annotation == null ? null : annotation.value();
                            if (value != null && value.startsWith("http")) {
                                f.setAccessible(true);
                                ports.put(value, f);
                            }
                        }
                        type = type.getSuperclass();
                    }
                }

                if (ports.containsKey("http")) {
                    configuration.randomHttpPort();
                }

                // at least after LifecycleTasks to inherit from potential states (system properties to get a port etc...)
                final Configurers configurers = appClass.getAnnotation(Configurers.class);
                if (tasks != null) {
                    for (final Class<? extends Configurer> type : configurers.value()) {
                        type.newInstance().configure(configuration);
                    }
                }

                final Classes classes = appClass.getAnnotation(Classes.class);
                String context = classes != null ? classes.context() : "";
                context = !context.isEmpty() && context.startsWith("/") ? context.substring(1) : context;
                // TODO: potentially respect classes() giving to deployClasspath a built Finder

                final WebResource resources = appClass.getAnnotation(WebResource.class);
                if (resources != null && resources.value().length > 1) {
                    throw new IllegalArgumentException("Only one docBase is supported for now using @WebResource");
                }

                String webResource = null;
                if (resources != null && resources.value().length > 0) {
                    webResource = resources.value()[0];
                } else {
                    final File webapp = new File("src/main/webapp");
                    if (webapp.isDirectory()) {
                        webResource = "src/main/webapp";
                    }
                }

                final Container container = new Container(configuration)
                        .deployClasspathAsWebApp(context, webResource != null ? new File(webResource) : null);

                for (final Map.Entry<String, Field> f : ports.entrySet()) {
                    switch (f.getKey()) {
                        case "http":
                            setPortField(f.getKey(), f.getValue(), configuration, context, APP.get());
                            break;
                        case "https":
                            break;
                        default:
                            throw new IllegalArgumentException("port " + f.getKey() + " not yet supported");
                    }
                }

                composerInject(APP.get());

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() { // ensure to log errors but not fail there
                        try {
                            if (container != null) {
                                container.close();
                            }
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                        for (final Closeable c : postTasks) {
                            try {
                                c.close();
                            } catch (final IOException e) {
                                e.printStackTrace();
                            }
                        }
                        postTasks.clear();
                        APP.set(null);
                        try {
                            Runtime.getRuntime().removeShutdownHook(this);
                        } catch (final Exception e) {
                            // no-op: that's ok at that moment if not called manually
                        }
                    }
                });
            }
        }
    }

    private static void setPortField(final String key, final Field value, final Configuration configuration, final String ctx,
                                     final Object instance) {
        final int port = "http".equals(key) ? configuration.getHttpPort() : configuration.getHttpsPort();
        if (value.getType() == URL.class) {
            try {
                value.set(instance, new URL(key + "://localhost:" + port + "/" + ctx));
            } catch (final Exception e) {
                throw new IllegalArgumentException(e);
            }
        } else if (value.getType() == int.class) {
            try {
                value.set(instance, port);
            } catch (final Exception e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            throw new IllegalArgumentException("Unsupported " + key);
        }
    }

    private static void composerInject(final Object target) throws IllegalAccessException {
        WebBeansContext webBeansContext = null;
        try {
            webBeansContext = WebBeansContext.currentInstance();
        } catch (final IllegalStateException ise) {
            // no-op
        }
        if (webBeansContext != null) {
            OWBInjector.inject(webBeansContext.getBeanManagerImpl(), target, null);
        }

        final Object app = APP.get();
        final Class<?> aClass = target.getClass();
        for (final Field f : aClass.getDeclaredFields()) {
            final RandomPort randomPort = f.getAnnotation(RandomPort.class);
            if (randomPort != null) {
                for (final Field field : app.getClass().getDeclaredFields()) {
                    final RandomPort appPort = field.getAnnotation(RandomPort.class);
                    if (field.getType() == f.getType() && appPort != null && appPort.value().equals(randomPort.value())) {
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

    public interface LifecycleTask extends org.apache.tomee.embedded.LifecycleTask {
    }

    @Retention(RUNTIME)
    @Target(TYPE)
    public @interface LifecycleTasks {
        Class<? extends org.apache.tomee.embedded.LifecycleTask>[] value();
    }

    @Retention(RUNTIME)
    @Target(TYPE)
    public @interface Configurers {
        Class<? extends Configurer>[] value();
    }

    public interface Configurer {
        void configure(Configuration configuration);
    }
}
