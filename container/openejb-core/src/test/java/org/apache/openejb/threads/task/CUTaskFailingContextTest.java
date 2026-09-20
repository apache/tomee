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
package org.apache.openejb.threads.task;

import org.apache.openejb.threads.impl.ContextServiceImpl;
import org.junit.Test;

import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.enterprise.concurrent.ManagedTaskListener;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * TOMEE-4704: when establishing the context fails, the task listener is still notified and the
 * thread is left as it was found.
 */
public class CUTaskFailingContextTest {
    @Test
    public void aFailureEstablishingTheContextIsReportedAndCleanedUp() {
        final IllegalStateException failure = new IllegalStateException("begin() blew up");
        final RecordingListener listener = new RecordingListener();

        final CUCallable<String> task = new CUCallable<>(
            new ListenedCallable(listener, () -> "never reached"),
            new ContextServiceImpl(singletonList(new FailingProvider(failure)), emptyList(), emptyList()));

        try {
            task.call();
            fail("the failure establishing the context must be propagated");
        } catch (final Exception e) {
            assertSame(failure, rootCause(e));
        }

        assertSame("the listener is notified that the task was aborted", failure, rootCause(listener.aborted));
        assertSame("and that it is done", failure, rootCause(listener.done));
        assertFalse("the task never ran", listener.started);

        // the thread must be usable for the next task. A context left behind here is what turned a
        // single failure into a pool thread that failed every task after it.
        assertNull(CUTask.Context.CURRENT.get());
    }

    @Test
    public void aTaskThatRunsNormallyStillReportsInOrder() throws Exception {
        final RecordingListener listener = new RecordingListener();

        final CUCallable<String> task = new CUCallable<>(
            new ListenedCallable(listener, () -> "done"),
            new ContextServiceImpl(emptyList(), emptyList(), emptyList()));

        assertEquals("done", task.call());
        assertNull(listener.aborted);
        assertNull(listener.done);
        assertNull(CUTask.Context.CURRENT.get());
    }

    private static Throwable rootCause(final Throwable t) {
        Throwable current = t;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private static final class RecordingListener implements ManagedTaskListener {
        private boolean started;
        private Throwable aborted;
        private Throwable done;

        @Override
        public void taskSubmitted(final Future<?> future, final ManagedExecutorService executor, final Object task) {
        }

        @Override
        public void taskStarting(final Future<?> future, final ManagedExecutorService executor, final Object task) {
            started = true;
        }

        @Override
        public void taskAborted(final Future<?> future, final ManagedExecutorService executor, final Object task,
                                final Throwable exception) {
            aborted = exception;
        }

        @Override
        public void taskDone(final Future<?> future, final ManagedExecutorService executor, final Object task,
                             final Throwable exception) {
            done = exception;
        }
    }

    private static final class ListenedCallable implements Callable<String>, ManagedTask {
        private final ManagedTaskListener listener;
        private final Callable<String> delegate;

        private ListenedCallable(final ManagedTaskListener listener, final Callable<String> delegate) {
            this.listener = listener;
            this.delegate = delegate;
        }

        @Override
        public ManagedTaskListener getManagedTaskListener() {
            return listener;
        }

        @Override
        public Map<String, String> getExecutionProperties() {
            return null;
        }

        @Override
        public String call() throws Exception {
            return delegate.call();
        }
    }

    private static final class FailingProvider implements ThreadContextProvider {
        private final RuntimeException failure;

        private FailingProvider(final RuntimeException failure) {
            this.failure = failure;
        }

        @Override
        public ThreadContextSnapshot currentContext(final Map<String, String> props) {
            return () -> {
                throw failure;
            };
        }

        @Override
        public ThreadContextSnapshot clearedContext(final Map<String, String> props) {
            return () -> (ThreadContextRestorer) () -> {
            };
        }

        @Override
        public String getThreadContextType() {
            return "failing-for-test";
        }
    }

}
