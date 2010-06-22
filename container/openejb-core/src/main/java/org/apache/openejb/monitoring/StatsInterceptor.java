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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.monitoring;

import org.apache.commons.math.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.apache.xbean.finder.ClassFinder;
import org.apache.openejb.api.Monitor;
import org.apache.openejb.core.interceptor.InterceptorData;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.AfterCompletion;
import javax.ejb.BeforeCompletion;
import javax.ejb.AfterBegin;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.interceptor.AroundTimeout;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @version $Rev$ $Date$
 */
public class StatsInterceptor {

    public static final InterceptorData metadata = InterceptorData.scan(StatsInterceptor.class);

    private final Map<Method, Stats> map = new ConcurrentHashMap<Method, Stats>();
    private final AtomicLong invocations = new AtomicLong();
    private final AtomicLong invocationTime = new AtomicLong();

    private Monitor monitor;
    private final boolean enabled;

    public StatsInterceptor(Class<?> componentClass) {

        monitor = componentClass.getAnnotation(Monitor.class);
        ClassFinder finder = new ClassFinder(componentClass);
        for (Method method : finder.findAnnotatedMethods(Monitor.class)) {
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
    public Object invoke(InvocationContext invocationContext) throws Exception {
        return record(invocationContext, null);
    }

    public Method PostConstruct() throws NoSuchMethodException {
        return this.getClass().getMethod("PostConstruct");
    }

    @PostConstruct
    public void PostConstruct(InvocationContext invocationContext) throws Exception {
        record(invocationContext, PostConstruct());
    }

    public Method PreDestroy() throws NoSuchMethodException {
        return this.getClass().getMethod("PreDestroy");
    }

    @PreDestroy
    public void PreDestroy(InvocationContext invocationContext) throws Exception {
        record(invocationContext, PreDestroy());
    }

    public Method PostActivate() throws NoSuchMethodException {
        return this.getClass().getMethod("PostActivate");
    }

    @PostActivate
    public void PostActivate(InvocationContext invocationContext) throws Exception {
        record(invocationContext, PostActivate());
    }

    public Method PrePassivate() throws NoSuchMethodException {
        return this.getClass().getMethod("PrePassivate");
    }

    @PrePassivate
    public void PrePassivate(InvocationContext invocationContext) throws Exception {
        record(invocationContext, PrePassivate());
    }

    public Method AroundTimeout() throws NoSuchMethodException {
        return this.getClass().getMethod("AroundTimeout");
    }

    @AroundTimeout
    public void AroundTimeout(InvocationContext invocationContext) throws Exception {
        record(invocationContext, AroundTimeout());
    }

    public Method AfterBegin() throws NoSuchMethodException {
        return this.getClass().getMethod("AfterBegin");
    }

    @AfterBegin
    public void AfterBegin(InvocationContext invocationContext) throws Exception {
        record(invocationContext, AfterBegin());
    }

    public Method BeforeCompletion() throws NoSuchMethodException {
        return this.getClass().getMethod("BeforeCompletion");
    }

    @BeforeCompletion
    public void BeforeCompletion(InvocationContext invocationContext) throws Exception {
        record(invocationContext, BeforeCompletion());
    }

    public Method AfterCompletion() throws NoSuchMethodException {
        return this.getClass().getMethod("AfterCompletion");
    }

    @AfterCompletion
    public void AfterCompletion(InvocationContext invocationContext) throws Exception {
        record(invocationContext, AfterCompletion());
    }

    private Object record(InvocationContext invocationContext, Method callback) throws Exception {
        invocations.incrementAndGet();

        Stats stats = enabled ? stats(invocationContext, callback): null;
        long start = System.nanoTime();
        try{
            return invocationContext.proceed();
        } finally {
            long time = millis(System.nanoTime() - start);
            if (stats != null) stats.record(time);
            invocationTime.addAndGet(time);
        }
    }

    private long millis(long nanos) {
        return TimeUnit.MILLISECONDS.convert(nanos, TimeUnit.NANOSECONDS);
    }

    private Stats stats(InvocationContext invocationContext, Method callback) {
        Method method = callback == null? invocationContext.getMethod(): callback;

        Stats stats = map.get(method);
        if (stats == null) {
            stats = new Stats(method, monitor);
            map.put(method, stats);
        }
        return stats;
    }

    public class Stats {
        private final AtomicLong count = new AtomicLong();
        private final SynchronizedDescriptiveStatistics samples;

        // Used as the prefix for the MBeanAttributeInfo
        private final String method;

        public Stats(Method method, Monitor classAnnotation) {
            Monitor methodAnnotation = method.getAnnotation(Monitor.class);

            int window = (methodAnnotation != null) ? methodAnnotation.sample() : (classAnnotation != null) ? classAnnotation.sample() : 2000;

            this.samples = new SynchronizedDescriptiveStatistics(window);
            String s = ",";

            StringBuilder sb = new StringBuilder(method.getName());
            sb.append("(");
            Class<?>[] params = method.getParameterTypes();
            for (Class<?> clazz : params) {
                sb.append(clazz.getSimpleName());
                sb.append(s);
            }
            if (params.length > 0) sb.delete(sb.length() - s.length(), sb.length());
            sb.append(")");

            this.method = sb.toString();
        }

        @Managed
        public void setSampleSize(int i) {
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

        public void record(long time) {
            count.incrementAndGet();
            samples.addValue(time);
        }

    }
}
