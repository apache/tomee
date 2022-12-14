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

package org.apache.openejb.monitoring;

import org.apache.openejb.api.Monitor;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.math.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.finder.ClassFinder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.AfterBegin;
import javax.ejb.AfterCompletion;
import javax.ejb.BeforeCompletion;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @version $Rev$ $Date$
 */
public class StatsInterceptor {
    static {
        InterceptorData.cacheScan(StatsInterceptor.class);
    }

    private static final String DISABLE_STAT_INTERCEPTOR_PROPERTY = "openejb.stats.interceptor.disable";

    public static final InterceptorData metadata = InterceptorData.scan(StatsInterceptor.class);

    private final ConcurrentMap<Method, Stats> map = new ConcurrentHashMap<>();
    private final AtomicLong invocations = new AtomicLong();
    private final AtomicLong invocationTime = new AtomicLong();

    private final Monitor monitor;
    private final boolean enabled;

    public StatsInterceptor(final Class<?> componentClass) {

        monitor = componentClass.getAnnotation(Monitor.class);
        final ClassFinder finder = new ClassFinder(componentClass);
        for (final Method method : finder.findAnnotatedMethods(Monitor.class)) {
            map.put(method, new Stats(method, monitor));
        }
        enabled = monitor != null || map.size() > 0;
    }

    public boolean isMonitoringEnabled() {
        return enabled;
    }

    @Managed
    public long getInvocationCount() {
        return invocations.get();
    }

    @Managed
    public long getInvocationTime() {
        return invocationTime.get();
    }

    @Managed
    public long getMonitoredMethods() {
        return map.size();
    }

    @ManagedCollection(type = Stats.class, key = "method")
    public Collection<Stats> stats() {
        return map.values();
    }

//    private Method $n() throws NoSuchMethodException { return this.getClass().getMethod(\"$n\"); } @$n public void $n(InvocationContext invocationContext) throws Exception { record(invocationContext, $n()); }

    @AroundInvoke
    public Object invoke(final InvocationContext invocationContext) throws Exception {
        return record(invocationContext, null);
    }

    public Method PostConstruct() throws NoSuchMethodException {
        return this.getClass().getMethod("PostConstruct");
    }

    @PostConstruct
    public void PostConstruct(final InvocationContext invocationContext) throws Exception {
        record(invocationContext, PostConstruct());
    }

    public Method PreDestroy() throws NoSuchMethodException {
        return this.getClass().getMethod("PreDestroy");
    }

    @PreDestroy
    public void PreDestroy(final InvocationContext invocationContext) throws Exception {
        record(invocationContext, PreDestroy());
    }

    public Method PostActivate() throws NoSuchMethodException {
        return this.getClass().getMethod("PostActivate");
    }

    @PostActivate
    public void PostActivate(final InvocationContext invocationContext) throws Exception {
        record(invocationContext, PostActivate());
    }

    public Method PrePassivate() throws NoSuchMethodException {
        return this.getClass().getMethod("PrePassivate");
    }

    @PrePassivate
    public void PrePassivate(final InvocationContext invocationContext) throws Exception {
        record(invocationContext, PrePassivate());
    }

    public Method AroundTimeout() throws NoSuchMethodException {
        return this.getClass().getMethod("AroundTimeout");
    }

    @AroundTimeout
    public void AroundTimeout(final InvocationContext invocationContext) throws Exception {
        record(invocationContext, AroundTimeout());
    }

    public Method AfterBegin() throws NoSuchMethodException {
        return this.getClass().getMethod("AfterBegin");
    }

    @AfterBegin
    public void AfterBegin(final InvocationContext invocationContext) throws Exception {
        record(invocationContext, AfterBegin());
    }

    public Method BeforeCompletion() throws NoSuchMethodException {
        return this.getClass().getMethod("BeforeCompletion");
    }

    @BeforeCompletion
    public void BeforeCompletion(final InvocationContext invocationContext) throws Exception {
        record(invocationContext, BeforeCompletion());
    }

    public Method AfterCompletion() throws NoSuchMethodException {
        return this.getClass().getMethod("AfterCompletion");
    }

    @AfterCompletion
    public void AfterCompletion(final InvocationContext invocationContext) throws Exception {
        record(invocationContext, AfterCompletion());
    }

    private Object record(final InvocationContext invocationContext, final Method callback) throws Exception {
        invocations.incrementAndGet();

        final Stats stats = enabled ? stats(invocationContext, callback) : null;
        final long start = System.nanoTime();
        try {
            return invocationContext.proceed();
        } finally {
            long time = System.nanoTime() - start;
            time = millis(time); // do it in 2 steps since otherwise the measure is false (more false)
            if (stats != null) {
                stats.record(time);
            }
            invocationTime.addAndGet(time);
        }
    }

    private long millis(final long nanos) {
        return TimeUnit.MILLISECONDS.convert(nanos, TimeUnit.NANOSECONDS);
    }

    private Stats stats(final InvocationContext invocationContext, final Method callback) {
        final Method method = callback == null ? invocationContext.getMethod() : callback;

        final Stats stats = map.computeIfAbsent(method, m -> new Stats(m, monitor));
        return stats;
    }

    public class Stats {
        private final AtomicLong count = new AtomicLong();
        private final SynchronizedDescriptiveStatistics samples;

        // Used as the prefix for the MBeanAttributeInfo
        private final String method;

        public Stats(final Method method, final Monitor classAnnotation) {
            final Monitor methodAnnotation = method.getAnnotation(Monitor.class);

            final int window = methodAnnotation != null ? methodAnnotation.sample() : classAnnotation != null ? classAnnotation.sample() : 2000;

            this.samples = new SynchronizedDescriptiveStatistics(window);
            final String s = ",";

            final StringBuilder sb = new StringBuilder(method.getName());
            sb.append("(");
            final Class<?>[] params = method.getParameterTypes();
            for (final Class<?> clazz : params) {
                sb.append(clazz.getSimpleName());
                sb.append(s);
            }
            if (params.length > 0) {
                sb.delete(sb.length() - s.length(), sb.length());
            }
            sb.append(")");

            this.method = sb.toString();
        }

        @Managed
        public void setSampleSize(final int i) {
            samples.setWindowSize(i);
        }

        @Managed
        public int getSampleSize() {
            return samples.getWindowSize();
        }

        @Managed
        public long getCount() {
            return count.get();
        }

        @Managed
        public double getPercentile99() {
            return samples.getPercentile(99.0);
        }

        @Managed
        public double getPercentile90() {
            return samples.getPercentile(90.0);
        }

        @Managed
        public double getPercentile75() {
            return samples.getPercentile(75.0);
        }

        @Managed
        public double getPercentile50() {
            return samples.getPercentile(50.0);
        }

        @Managed
        public double getPercentile25() {
            return samples.getPercentile(25.0);
        }

        @Managed
        public double getPercentile10() {
            return samples.getPercentile(10.0);
        }

        @Managed
        public double getPercentile01() {
            return samples.getPercentile(1.0);
        }

        @Managed
        public double getStandardDeviation() {
            return samples.getStandardDeviation();
        }

        @Managed
        public double getMean() {
            return samples.getMean();
        }

        @Managed
        public double getVariance() {
            return samples.getVariance();
        }

        @Managed
        public double getGeometricMean() {
            return samples.getGeometricMean();
        }

        @Managed
        public double getSkewness() {
            return samples.getSkewness();
        }

        @Managed
        public double getKurtosis() {
            return samples.getKurtosis();
        }

        @Managed
        public double getMax() {
            return samples.getMax();
        }

        @Managed
        public double getMin() {
            return samples.getMin();
        }

        @Managed
        public double getSum() {
            return samples.getSum();
        }

        @Managed
        public double getSumsq() {
            return samples.getSumsq();
        }

        @Managed
        public double[] sortedValues() {
            return samples.getSortedValues();
        }

        @Managed
        public double[] values() {
            return samples.getValues();
        }

        public void record(final long time) {
            count.incrementAndGet();
            samples.addValue(time);
        }

    }

    public static boolean isStatsActivated() {
        return SystemInstance.get().getOptions().get(DISABLE_STAT_INTERCEPTOR_PROPERTY, true);
    }
}
