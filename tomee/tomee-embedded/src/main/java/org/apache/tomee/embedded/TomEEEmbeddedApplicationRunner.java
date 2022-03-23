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
package org.apache.tomee.embedded;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.openejb.config.DeploymentsResolver;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.testing.Application;
import org.apache.openejb.testing.ApplicationComposers;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.Jars;
import org.apache.openejb.testing.RandomPort;
import org.apache.openejb.testing.WebResource;
import org.apache.tomee.embedded.component.TomEEEmbeddedArgs;
import org.apache.tomee.embedded.event.TomEEEmbeddedApplicationRunnerInjection;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.apache.xbean.finder.archive.FileArchive;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.inject.Vetoed;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.logging.Level.SEVERE;
import static org.apache.openejb.loader.JarLocation.jarLocation;
import static org.apache.openejb.util.Classes.ancestors;

@Vetoed
public class TomEEEmbeddedApplicationRunner implements AutoCloseable {
    private static final ConcurrentMap<Runnable, Runnable> SHUTDOWN_TASKS = new ConcurrentHashMap<>();

    static { // to ensure we have an ordering for shutdown tasks, we typically want to avoid Files.delete() before stop()
        Runtime.getRuntime().addShutdownHook(new Thread("TomEEEmbeddedApplicationRunner-shutdown") {
            @Override
            public void run() {
                for (final Runnable task : SHUTDOWN_TASKS.keySet()) {
                    try {
                        task.run();
                    } catch (final Exception e) {
                        Logger.getLogger(TomEEEmbeddedApplicationRunner.class.getName()).log(SEVERE, e.getMessage(), e);
                    }
                }
                SHUTDOWN_TASKS.clear();
            }
        });
    }

    private volatile boolean started = false;
    private volatile Object app;
    private volatile Thread hook;

    public static void run(final Object app, final String... args) {
        final TomEEEmbeddedApplicationRunner runner = new TomEEEmbeddedApplicationRunner();
        runner.start(app, args);
        try {
            new CountDownLatch(1).await();
        } catch (final InterruptedException e) {
            Thread.interrupted();
            runner.close();
        }
    }

    public AutoCloseable start(final Object app, final String... args) {
        setApp(app);
        final Properties overrides = args == null || args.length == 0 ? null : new Properties();
        if (overrides != null) {
            for (final String prop : args) {
                final String[] seg = prop.split("=");
                if (seg[0].startsWith("--")) {
                    seg[0] = seg[0].substring("--".length());
                }
                overrides.put(seg[0], seg[1]);
            }
        }
        try {
            start(app.getClass(), overrides, args);
            return this;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void setApp(final Object app) {
        this.app = app;
    }

    public Object getApp() {
        return app;
    }

    public synchronized void start(final Class<?> marker, final Properties config, final String... args) throws Exception {
        if (started) {
            return;
        }

        ensureAppInit(marker);
        started = true;

        final Class<?> appClass = app.getClass();
        final AnnotationFinder finder = new AnnotationFinder(new ClassesArchive(ancestors(appClass)));

        // setup the container config reading class annotation, using a randome http port and deploying the classpath
        final Configuration configuration = new Configuration();
        final ContainerProperties props = appClass.getAnnotation(ContainerProperties.class);
        if (props != null) {
            final Properties runnerProperties = new Properties();
            for (final ContainerProperties.Property p : props.value()) {
                final String name = p.name();
                if (name.startsWith("tomee.embedded.application.runner.")) { // allow to tune the Configuration
                    // no need to filter there since it is done in loadFromProperties()
                    runnerProperties.setProperty(name.substring("tomee.embedded.application.runner.".length()), p.value());
                } else {
                    configuration.property(name, StrSubstitutor.replaceSystemProperties(p.value()));
                }
            }
            if (!runnerProperties.isEmpty()) {
                configuration.loadFromProperties(runnerProperties);
            }
        }
        configuration.loadFromProperties(System.getProperties()); // overrides, note that some config are additive by design

        final List<Method> annotatedMethods = finder.findAnnotatedMethods(org.apache.openejb.testing.Configuration.class);
        if (annotatedMethods.size() > 1) {
            throw new IllegalArgumentException("Only one @Configuration is supported: " + annotatedMethods);
        }
        for (final Method m : annotatedMethods) {
            final Object o = m.invoke(app);
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

        final Collection<org.apache.tomee.embedded.LifecycleTask> lifecycleTasks = new ArrayList<>();
        final Collection<Closeable> postTasks = new ArrayList<>();
        final LifecycleTasks tasks = appClass.getAnnotation(LifecycleTasks.class);
        if (tasks != null) {
            for (final Class<? extends org.apache.tomee.embedded.LifecycleTask> type : tasks.value()) {
                final org.apache.tomee.embedded.LifecycleTask lifecycleTask = type.newInstance();
                lifecycleTasks.add(lifecycleTask);
                postTasks.add(lifecycleTask.beforeContainerStartup());
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
        if (configurers != null) {
            for (final Class<? extends Configurer> type : configurers.value()) {
                type.newInstance().configure(configuration);
            }
        }

        final Classes classes = appClass.getAnnotation(Classes.class);
        String context = classes != null ? classes.context() : "";
        context = !context.isEmpty() && context.startsWith("/") ? context.substring(1) : context;

        Archive archive = null;
        if (classes != null && classes.value().length > 0) {
            archive = new ClassesArchive(classes.value());
        }

        final Jars jars = appClass.getAnnotation(Jars.class);
        final List<URL> urls;
        if (jars != null) {
            final Collection<File> files = ApplicationComposers.findFiles(jars);
            urls = new ArrayList<>(files.size());
            for (final File f : files) {
                urls.add(f.toURI().toURL());
            }
        } else {
            urls = null;
        }

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

        if (config != null) { // override other config from annotations
            configuration.loadFromProperties(config);
        }

        final Container container = new Container(configuration);
        SystemInstance.get().setComponent(TomEEEmbeddedArgs.class, new TomEEEmbeddedArgs(args, null));
        SystemInstance.get().setComponent(LifecycleTaskAccessor.class, new LifecycleTaskAccessor(lifecycleTasks));
        container.deploy(new Container.DeploymentRequest(
                context,
                // call ClasspathSearcher that lazily since container needs to be started to not preload logging
                urls == null ? new DeploymentsResolver.ClasspathSearcher().loadUrls(Thread.currentThread().getContextClassLoader()).getUrls() : urls,
                webResource != null ? new File(webResource) : null,
                true,
                null,
                archive));

        for (final Map.Entry<String, Field> f : ports.entrySet()) {
            switch (f.getKey()) {
                case "http":
                    setPortField(f.getKey(), f.getValue(), configuration, context, app);
                    break;
                case "https":
                    break;
                default:
                    throw new IllegalArgumentException("port " + f.getKey() + " not yet supported");
            }
        }

        SystemInstance.get().addObserver(app);
        composerInject(app);

        final AnnotationFinder appFinder = new AnnotationFinder(new ClassesArchive(appClass));
        for (final Method mtd : appFinder.findAnnotatedMethods(PostConstruct.class)) {
            if (mtd.getParameterTypes().length == 0) {
                if (!mtd.isAccessible()) {
                    mtd.setAccessible(true);
                }
                mtd.invoke(app);
            }
        }

        hook = new Thread() {
            @Override
            public void run() { // ensure to log errors but not fail there
                for (final Method mtd : appFinder.findAnnotatedMethods(PreDestroy.class)) {
                    if (mtd.getParameterTypes().length == 0) {
                        if (!mtd.isAccessible()) {
                            mtd.setAccessible(true);
                        }
                        try {
                            mtd.invoke(app);
                        } catch (final IllegalAccessException e) {
                            throw new IllegalStateException(e);
                        } catch (final InvocationTargetException e) {
                            throw new IllegalStateException(e.getCause());
                        }
                    }
                }

                try {
                    container.close();
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
                app = null;
                try {
                    SHUTDOWN_TASKS.remove(this);
                } catch (final Exception e) {
                    // no-op: that's ok at that moment if not called manually
                }
            }
        };
        SHUTDOWN_TASKS.put(hook, hook);
    }

    // if app is not set then we'll check if -Dtomee.application-composer.application is set otherwise
    // we'll try to find a single @Application class in the jar containing marker (case for tests).
    private void ensureAppInit(final Class<?> marker) {
        if (app != null) {
            return;
        }

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
            app = type.newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public synchronized void close() {
        if (hook != null) {
            hook.run();
            SHUTDOWN_TASKS.remove(hook);
            hook = null;
            app = null;
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

    public void composerInject(final Object target) throws IllegalAccessException {
        WebBeansContext webBeansContext = null;
        try {
            webBeansContext = WebBeansContext.currentInstance();
        } catch (final IllegalStateException ise) {
            // no-op
        }
        if (webBeansContext != null) {
            OWBInjector.inject(webBeansContext.getBeanManagerImpl(), target, null);
        }

        Class<?> aClass = target.getClass();
        while (aClass != null && aClass != Object.class) {
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
                } else if (f.isAnnotationPresent(LifecycleTask.class)) {
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    final LifecycleTaskAccessor accessor = SystemInstance.get().getComponent(LifecycleTaskAccessor.class);
                    final Class type = f.getType();
                    final Object taskByType = accessor.getTaskByType(type);
                    f.set(target, taskByType);
                } else if (f.isAnnotationPresent(Args.class)) {
                    if (String[].class != f.getType()) {
                        throw new IllegalArgumentException("@Args can only be used for String[] field, not on " + f.getType());
                    }
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    final TomEEEmbeddedArgs args = SystemInstance.get().getComponent(TomEEEmbeddedArgs.class);
                    f.set(target, args == null ? new String[0] : args.getArgs());
                }
            }
            aClass = aClass.getSuperclass();
        }

        SystemInstance.get().fireEvent(new TomEEEmbeddedApplicationRunnerInjection(target));
    }

    @Retention(RUNTIME)
    @Target(TYPE)
    public @interface LifecycleTasks {
        Class<? extends org.apache.tomee.embedded.LifecycleTask>[] value();
    }

    @Retention(RUNTIME)
    @Target(FIELD)
    public @interface LifecycleTask {
    }

    @Retention(RUNTIME)
    @Target(FIELD)
    public @interface Args {
    }

    @Retention(RUNTIME)
    @Target(TYPE)
    public @interface Configurers {
        Class<? extends Configurer>[] value();
    }

    public interface Configurer {
        void configure(Configuration configuration);
    }

    public static final class LifecycleTaskAccessor {
        private final Collection<org.apache.tomee.embedded.LifecycleTask> tasks;

        private LifecycleTaskAccessor(final Collection<org.apache.tomee.embedded.LifecycleTask> lifecycleTasks) {
            this.tasks = lifecycleTasks;
        }

        public Collection<org.apache.tomee.embedded.LifecycleTask> getTasks() {
            return tasks;
        }

        public <T> T getTaskByType(final Class<T> type) {
            for (final org.apache.tomee.embedded.LifecycleTask task : tasks) {
                if (type == task.getClass()) {
                    return (T) task;
                }
            }
            if (Collection.class.isAssignableFrom(type)) {
                return (T) tasks;
            }
            return null;
        }
    }
}
