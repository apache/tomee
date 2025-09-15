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
package org.apache.openejb.threads;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.enterprise.concurrent.ManageableThread;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.threads.impl.ContextServiceImpl;
import org.apache.openejb.threads.impl.ContextServiceImplFactory;
import org.apache.openejb.threads.impl.ManagedThreadFactoryImpl;
import org.apache.openejb.threads.task.CURunnable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testng.Assert;

import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(ApplicationComposer.class)
public class ManagedExecutorServiceTCCLTest {
    @Configuration
    public Properties configuration() {
        return new PropertiesBuilder()
                // don't set concurrent/es.context, it needs to resolve to the default ContextService in this case
                .property("concurrent/es1", "new://Resource?type=ManagedExecutorService")
                .property("concurrent/es1.core", "2")
                .property("concurrent/es1.max", "10")
                .property("concurrent/es1.keepAlive", "5 s")
                .property("concurrent/es1.queue", "3")
                .property("concurrent/es1.lazy", "True")
                .property("concurrent/es1.threadFactory", "concurrent/tf")
                .property("concurrent/es2", "new://Resource?type=ManagedExecutorService")
                .property("concurrent/es2.core", "2")
                .property("concurrent/es2.max", "10")
                .property("concurrent/es2.keepAlive", "5 s")
                .property("concurrent/es2.queue", "3")
                .property("concurrent/es2.lazy", "True")
                .property("concurrent/es2.threadFactory", "concurrent/tf")
                .property("concurrent/tf", "new://Resource?type=ManagedThreadFactory&class-name=org.apache.openejb.threads.ManagedExecutorServiceTCCLTest$MyManagedThreadFactoryImplFactory&factory-name=create")
                .property("concurrent/tf.prefix", "mes-test-thread-")
                .property("concurrent/tf.priority", "5")
                .property("concurrent/tf.context", "[automatic]")
                .build();
    }

    @EJB
    private ExecutorBean execBean;

    @Module
    public EjbJar bean() {
        return new EjbJar()
                .enterpriseBean(new SingletonBean(ExecutorBean.class).localBean())
                .enterpriseBean(new SingletonBean(SecondExecutorBean.class).localBean())
                .enterpriseBean(new SingletonBean(EchoBean.class).localBean());
    }

    @Test
    public void test() throws Exception {
        final String output = execBean.execute();
        Assert.assertEquals("Hello TomEE!", output);
    }

    @Singleton
    @Lock(LockType.READ)
    public static class ExecutorBean {

        @Resource(name = "concurrent/es1")
        private ManagedExecutorService es1;

        @EJB
        private SecondExecutorBean secondBean;

        public String execute() {
            try {
                final Future<String> future = es1.submit(() -> secondBean.run("TomEE!"));
                return future.get(1, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Singleton
    @Lock(LockType.READ)
    public static class SecondExecutorBean {

        @Resource(name = "concurrent/es2")
        private ManagedExecutorService es2;

        @EJB
        private EchoBean echoBean;

        public String run(final String name) {
            try {
                final Future<String> future = es2.submit(() -> "Hello " + echoBean.echo(name));
                return future.get(1, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Singleton
    @Lock(LockType.READ)
    public static class EchoBean {
        public String echo(final String input) {
            return input;
        }
    }

    public static class MyManagedThreadFactoryImplFactory {
        private String prefix = "openejb-managed-thread-";
        private Integer priority;
        private String context;

        public ManagedThreadFactory create() {
            return new MyManagedThreadFactoryImpl(prefix, priority, ContextServiceImplFactory.lookupOrDefault(context));
        }

        public void setPrefix(final String prefix) {
            this.prefix = prefix;
        }

        public void setPriority(final int priority) {
            this.priority = priority;
        }

        public void setContext(final String context) {
            this.context = context;
        }
    }

    /*
     * This is a copy of ManagedThreadFactoryImpl, but with an override on the thread that prevents the
     * context class loader being set to null - if a caller attempts to do this, the code
     * throws an exception, which should cause the test to fail
     */
    public static class MyManagedThreadFactoryImpl implements ManagedThreadFactory  {
        public static final String DEFAULT_PREFIX = "managed-thread-";
        private static final AtomicInteger ID = new AtomicInteger();

        private final ContextServiceImpl contextService;
        private final String prefix;
        private final Integer priority;

        // Invoked by ThreadFactories.findThreadFactory via reflection
        @SuppressWarnings("unused")
        public MyManagedThreadFactoryImpl() {
            this(DEFAULT_PREFIX, Thread.NORM_PRIORITY, ContextServiceImplFactory.getOrCreateDefaultSingleton());
        }

        public MyManagedThreadFactoryImpl(final String prefix, final Integer priority, final ContextServiceImpl contextService) {
            this.prefix = prefix;
            this.priority = priority;
            this.contextService = contextService;
        }

        @Override
        public Thread newThread(final Runnable r) {
            final CURunnable wrapper = new CURunnable(r, contextService);
            final Thread thread = new ManagedThread(wrapper);
            thread.setDaemon(true);
            thread.setName(prefix + ID.incrementAndGet());
            thread.setContextClassLoader(ManagedThreadFactoryImpl.class.getClassLoader()); // ensure we use container loader as main context classloader to avoid leaks
            if (priority != null) {
                thread.setPriority(priority);
            }
            return thread;
        }

        @Override
        public ForkJoinWorkerThread newThread(final ForkJoinPool pool) {
            return new ManagedForkJoinWorkerThread(pool, priority, contextService);
        }

        public static class ManagedThread extends Thread implements ManageableThread {
            public ManagedThread(final Runnable r) {
                super(r);
            }

            @Override
            public boolean isShutdown() {
                return getState() == State.TERMINATED;
            }

            @Override
            public void setContextClassLoader(ClassLoader cl) {
                if (cl == null) {
                    throw new IllegalArgumentException("The thread context class loader should not be set to null");
                }
                super.setContextClassLoader(cl);
            }
        }

        public static class ManagedForkJoinWorkerThread extends ForkJoinWorkerThread {
            private final ContextServiceImpl contextService;
            private final Integer priority;

            private final ContextServiceImpl.Snapshot snapshot;

            private ContextServiceImpl.State state;
            private Integer initialPriority;

            protected ManagedForkJoinWorkerThread(final ForkJoinPool pool, final Integer priority, final ContextServiceImpl contextService) {
                super(pool);
                this.priority = priority;
                this.contextService = contextService;

                this.snapshot = contextService.snapshot(null);
            }

            @Override
            protected void onStart() {
                super.onStart();
                initialPriority = getPriority();
                if (priority != null) {
                    setPriority(priority);
                }

                contextService.enter(snapshot);
            }

            @Override
            protected void onTermination(Throwable exception) {
                setPriority(initialPriority);
                contextService.exit(state);

                super.onTermination(exception);
            }
        }
    }
}
