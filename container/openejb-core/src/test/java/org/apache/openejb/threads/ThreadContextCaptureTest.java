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

import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.apache.openejb.threads.impl.ContextServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.interceptor.InvocationContext;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * TOMEE-4699: the submitting thread's ThreadContext is captured by value, on that thread, rather than
 * read later from the thread running the task.
 */
@RunWith(ApplicationComposer.class)
public class ThreadContextCaptureTest {
    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(CaptureFacade.class).localBean();
    }

    @EJB
    private CaptureFacade facade;

    @Test
    public void contextIsCapturedWhenTheContextualProxyIsCreated() throws Exception {
        facade.checkCaptureTime();
    }

    @Test
    public void submittingWhileMutatingTheCallerContextDoesNotFail() throws Exception {
        facade.submitWhileMutating();
    }

    @Test
    public void inlineExecutionLeavesTheCallersClassLoaderInPlace() throws Exception {
        facade.checkInlineClassLoader();
    }

    @Test
    public void theCallersInvocationContextIsNotPropagated() throws Exception {
        facade.checkInvocationContextNotPropagated();
    }

    @Test
    public void currentContextExecutorCapturesWhereItWasCreated() throws Exception {
        facade.checkCurrentContextExecutorCaptureTime();
    }

    @Test
    public void aContextualProxyCanRunOnSeveralThreadsAtOnce() throws Exception {
        facade.checkContextualProxyIsReusable();
    }

    public static class BeforeCapture {
    }

    public static class AfterCapture {
    }

    public static class Churn {
    }

    @Singleton
    public static class CaptureFacade {
        @Resource
        private ContextService contextService;

        @Resource
        private ManagedExecutorService executorService;

        public void checkCaptureTime() throws Exception {
            final ThreadContext caller = ThreadContext.getThreadContext();
            assertNotNull(caller);

            caller.set(BeforeCapture.class, new BeforeCapture());

            final Callable<Object[]> contextual = contextService.contextualCallable(() -> {
                final ThreadContext taskContext = ThreadContext.getThreadContext();
                assertNotNull(taskContext);
                return new Object[]{taskContext.get(BeforeCapture.class), taskContext.get(AfterCapture.class)};
            });

            // the caller keeps updating its own thread confined ThreadContext after the capture
            caller.set(AfterCapture.class, new AfterCapture());

            final ExecutorService plain = Executors.newSingleThreadExecutor();
            try {
                final Object[] seen = plain.submit(contextual).get(1, TimeUnit.MINUTES);
                assertNotNull("state present at capture time must be propagated", seen[0]);
                assertNull("state added after the capture must not leak into the task", seen[1]);
            } finally {
                plain.shutdownNow();
                caller.remove(BeforeCapture.class);
                caller.remove(AfterCapture.class);
            }
        }

        public void checkInlineClassLoader() {
            final Thread thread = Thread.currentThread();
            final ClassLoader original = thread.getContextClassLoader();

            // take the snapshot under the real thread context class loader, since the application is
            // resolved from it and any other loader would produce a cleared snapshot
            final ContextServiceImpl impl = ContextServiceImpl.class.cast(contextService);
            final ContextServiceImpl.Snapshot snapshot = impl.snapshot(null);

            // a loader that is neither the application's nor the bean's, so that a restore to either
            // of those is visible here
            final ClassLoader marker = new URLClassLoader(new URL[0], original);
            thread.setContextClassLoader(marker);
            try {
                impl.exit(impl.enter(snapshot));

                assertSame("applying and restoring a context leaves the thread's loader unchanged",
                    marker, thread.getContextClassLoader());
            } finally {
                thread.setContextClassLoader(original);
            }
        }

        public void checkInvocationContextNotPropagated() throws Exception {
            final ThreadContext caller = ThreadContext.getThreadContext();
            assertNotNull(caller);
            // set by the interceptor stack on the way into this method
            assertNotNull("precondition: the caller is inside an invocation",
                caller.get(InvocationContext.class));

            final Future<InvocationContext> seen = executorService.submit(
                () -> ThreadContext.getThreadContext().get(InvocationContext.class));

            assertNull("the caller's InvocationContext must not be propagated to the task",
                seen.get(1, TimeUnit.MINUTES));
        }

        public void checkCurrentContextExecutorCaptureTime() throws Exception {
            final ThreadContext caller = ThreadContext.getThreadContext();
            assertNotNull(caller);

            caller.set(BeforeCapture.class, new BeforeCapture());
            final Executor executor = contextService.currentContextExecutor();
            caller.set(AfterCapture.class, new AfterCapture());

            try {
                final Object[] seen = new Object[2];
                executor.execute(() -> {
                    final ThreadContext taskContext = ThreadContext.getThreadContext();
                    seen[0] = taskContext.get(BeforeCapture.class);
                    seen[1] = taskContext.get(AfterCapture.class);
                });

                assertNotNull("state present when the executor was created must be propagated", seen[0]);
                assertNull("state added afterwards must not be, since the capture happens in"
                    + " currentContextExecutor() and not in execute()", seen[1]);
            } finally {
                caller.remove(BeforeCapture.class);
                caller.remove(AfterCapture.class);
            }
        }

        public void checkContextualProxyIsReusable() throws Exception {
            final int threads = 2;
            final int rounds = 200;
            final Callable<Boolean> contextual = contextService.contextualCallable(
                () -> ThreadContext.getThreadContext() != null);

            // one proxy, called by two threads at the same time. The callers are managed tasks, so
            // each already has a CUTask context of its own, which is required to observe a task
            // scoped CUTask.Context. The SPI requires a snapshot to be applicable "to any number of
            // threads, including concurrently".
            final CyclicBarrier barrier = new CyclicBarrier(threads);
            final List<Future<Boolean>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(executorService.submit(() -> {
                    for (int round = 0; round < rounds; round++) {
                        barrier.await(1, TimeUnit.MINUTES);
                        if (!contextual.call()) {
                            return false;
                        }
                    }
                    return true;
                }));
            }

            for (final Future<Boolean> future : futures) {
                assertTrue(future.get(1, TimeUnit.MINUTES));
            }

            // and once more afterwards, sequentially
            assertTrue(contextual.call());
        }

        public void submitWhileMutating() throws Exception {
            final ThreadContext caller = ThreadContext.getThreadContext();
            assertNotNull(caller);

            final List<Future<Boolean>> futures = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                futures.add(executorService.submit(() -> ThreadContext.getThreadContext() != null));

                // the interceptor stack updates the caller's context like this while the tasks start
                for (int j = 0; j < 200; j++) {
                    caller.set(Churn.class, new Churn());
                    caller.remove(Churn.class);
                }
            }

            for (final Future<Boolean> future : futures) {
                assertTrue(future.get(1, TimeUnit.MINUTES));
            }
        }
    }
}
