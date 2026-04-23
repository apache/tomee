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
package org.apache.openejb.cdi.concurrency;

import jakarta.annotation.Priority;
import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InterceptorBinding;
import jakarta.interceptor.InvocationContext;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class AsynchronousScheduledTest {

    @Inject
    private ScheduledBean scheduledBean;

    @Module
    public EnterpriseBean ejb() {
        // Dummy EJB to trigger full resource deployment including default concurrency resources
        return new SingletonBean(DummyEjb.class).localBean();
    }

    @Module
    public Class<?>[] beans() {
        return new Class<?>[]{ScheduledBean.class, CountingInterceptor.class,
                TracingOuterInterceptor.class, TracingInnerInterceptor.class};
    }

    @Test
    public void scheduledVoidMethodExecutesRepeatedly() throws Exception {
        // Call the method once — the interceptor sets up the recurring schedule
        scheduledBean.everySecondVoid();

        // Wait for at least 3 invocations
        final boolean reached = ScheduledBean.VOID_LATCH.await(10, TimeUnit.SECONDS);
        assertTrue("Scheduled void method should have been invoked at least 3 times, count: "
                + ScheduledBean.VOID_COUNTER.get(), reached);
    }

    @Test
    public void scheduledReturningMethodExecutes() throws Exception {
        // Call the method once — the interceptor sets up the recurring schedule
        final CompletableFuture<String> future = scheduledBean.everySecondReturning();

        // Wait for at least 1 invocation
        final boolean reached = ScheduledBean.RETURNING_LATCH.await(10, TimeUnit.SECONDS);
        assertTrue("Scheduled returning method should have been invoked, count: "
                + ScheduledBean.RETURNING_COUNTER.get(), reached);
    }

    @Test
    public void scheduledMethodExecutesThroughCdiInterceptor() throws Exception {
        CountingInterceptor.INVOCATIONS.set(0);
        assertEquals("Control invocation should go through the CDI interceptor", "ok", scheduledBean.directInterceptedCall());
        assertEquals("Control invocation should increment the CDI interceptor", 1, CountingInterceptor.INVOCATIONS.get());

        ScheduledBean.INTERCEPTED_COUNTER.set(0);
        CountingInterceptor.INVOCATIONS.set(0);

        final CompletableFuture<Integer> future = scheduledBean.everySecondIntercepted(2);
        final Integer result = future.get(15, TimeUnit.SECONDS);

        assertEquals("Scheduled method should complete after 2 runs", Integer.valueOf(2), result);
        assertEquals("Business method should have been invoked twice", 2, ScheduledBean.INTERCEPTED_COUNTER.get());
        assertEquals("CDI interceptor should run for each scheduled firing", 2, CountingInterceptor.INVOCATIONS.get());
    }

    @Test
    public void scheduledMethodPreservesInterceptorOrderingOnEveryFiring() throws Exception {
        ScheduledBean.TRACE.clear();
        ScheduledBean.TRACED_COUNTER.set(0);

        final CompletableFuture<Integer> future = scheduledBean.tracedSchedule(2);
        final Integer result = future.get(15, TimeUnit.SECONDS);

        assertEquals("Scheduled method should complete after 2 runs", Integer.valueOf(2), result);

        // Two firings, each must walk outer -> inner -> body in priority order.
        final List<String> expected = Arrays.asList(
                "outer", "inner", "body",
                "outer", "inner", "body");
        assertEquals("Interceptor chain must run in priority order on every firing",
                expected, ScheduledBean.TRACE);
    }

    @ApplicationScoped
    public static class ScheduledBean {
        static final AtomicInteger VOID_COUNTER = new AtomicInteger();
        static final CountDownLatch VOID_LATCH = new CountDownLatch(3);

        static final AtomicInteger RETURNING_COUNTER = new AtomicInteger();
        static final CountDownLatch RETURNING_LATCH = new CountDownLatch(1);
        static final AtomicInteger INTERCEPTED_COUNTER = new AtomicInteger();
        static final AtomicInteger TRACED_COUNTER = new AtomicInteger();
        static final List<String> TRACE = new CopyOnWriteArrayList<>();

        @Asynchronous(runAt = @Schedule(cron = "* * * * * *"))
        public void everySecondVoid() {
            VOID_COUNTER.incrementAndGet();
            VOID_LATCH.countDown();
        }

        @Asynchronous(runAt = @Schedule(cron = "* * * * * *"))
        public CompletableFuture<String> everySecondReturning() {
            RETURNING_COUNTER.incrementAndGet();
            RETURNING_LATCH.countDown();
            return Asynchronous.Result.complete("done");
        }

        @Counted
        public String directInterceptedCall() {
            return "ok";
        }

        @Counted
        @Asynchronous(runAt = @Schedule(cron = "* * * * * *"))
        public CompletableFuture<Integer> everySecondIntercepted(final int runs) {
            final int count = INTERCEPTED_COUNTER.incrementAndGet();
            if (count < runs) {
                return null;
            }

            final CompletableFuture<Integer> future = Asynchronous.Result.getFuture();
            future.complete(count);
            return future;
        }

        @Traced
        @Asynchronous(runAt = @Schedule(cron = "* * * * * *"))
        public CompletableFuture<Integer> tracedSchedule(final int runs) {
            TRACE.add("body");
            final int count = TRACED_COUNTER.incrementAndGet();
            if (count < runs) {
                return null;
            }
            final CompletableFuture<Integer> future = Asynchronous.Result.getFuture();
            future.complete(count);
            return future;
        }
    }

    @InterceptorBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Counted {
    }

    @Interceptor
    @Counted
    @Priority(Interceptor.Priority.APPLICATION)
    public static class CountingInterceptor {
        static final AtomicInteger INVOCATIONS = new AtomicInteger();

        @AroundInvoke
        public Object aroundInvoke(final InvocationContext context) throws Exception {
            INVOCATIONS.incrementAndGet();
            return context.proceed();
        }
    }

    @InterceptorBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Traced {
    }

    @Interceptor
    @Traced
    @Priority(Interceptor.Priority.LIBRARY_BEFORE)
    public static class TracingOuterInterceptor {
        static final List<String> TRACE = new CopyOnWriteArrayList<>();

        @AroundInvoke
        public Object aroundInvoke(final InvocationContext context) throws Exception {
            TRACE.add("outer");
            ScheduledBean.TRACE.add("outer");
            return context.proceed();
        }
    }

    @Interceptor
    @Traced
    @Priority(Interceptor.Priority.APPLICATION)
    public static class TracingInnerInterceptor {
        static final List<String> TRACE = new CopyOnWriteArrayList<>();

        @AroundInvoke
        public Object aroundInvoke(final InvocationContext context) throws Exception {
            TRACE.add("inner");
            ScheduledBean.TRACE.add("inner");
            return context.proceed();
        }
    }

    @jakarta.ejb.Singleton
    public static class DummyEjb {
    }
}
