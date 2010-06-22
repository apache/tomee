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
package org.apache.openejb.core.stateless;

import junit.framework.TestCase;
import org.apache.openejb.api.Monitor;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import java.lang.management.ManagementFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * INVOCATIONS MBEAN
 * <p/>
 * All beans have this set of attributes and operations by default:
 * <p/>
 * javax.management.MBeanAttributeInfo[description=, name=InvocationCount, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=InvocationTime, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=MonitoredMethods, type=long, read-only, descriptor={}]
 * javax.management.MBeanOperationInfo[description=, name=FilterAttributes, returnType=void, signature=[javax.management.MBeanParameterInfo[description="", name=excludeRegex, type=java.lang.String, descriptor={}], javax.management.MBeanParameterInfo[description="", name=includeRegex, type=java.lang.String, descriptor={}]], impact=unknown, descriptor={}]
 * <p/>
 * Then for every method there will be these attributes and operations:
 * <p/>
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Count, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().GeometricMean, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Kurtosis, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Max, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Mean, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Min, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Percentile01, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Percentile10, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Percentile25, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Percentile50, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Percentile75, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Percentile90, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Percentile99, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().SampleSize, type=int, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Skewness, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().StandardDeviation, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Sum, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Sumsq, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Variance, type=double, read-only, descriptor={}]
 * javax.management.MBeanOperationInfo[description=, name=someMethod().setSampleSize, returnType=void, signature=[javax.management.MBeanParameterInfo[description=, name=p1, type=int, descriptor={}]], impact=unknown, descriptor={}]
 * javax.management.MBeanOperationInfo[description=, name=someMethod().sortedValues, returnType=[D, signature=[], impact=unknown, descriptor={}]
 * javax.management.MBeanOperationInfo[description=, name=someMethod().values, returnType=[D, signature=[], impact=unknown, descriptor={}]
 * <p/>
 * Attribute values that should be tested:
 * javax.management.MBeanAttributeInfo[description=, name=InvocationCount, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=InvocationTime, type=long, read-only, descriptor={}]
 * <p/>
 * Atribute values that should be tested per method:
 * <p/>
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Count, type=long, read-only, descriptor={}]
 * javax.management.MBeanOperationInfo[description=, name=someMethod().values, returnType=[D, signature=[], impact=unknown, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Max, type=double, read-only, descriptor={}]
 * <p/>
 * We should test that all expected attributes are there, but when testing each method's values
 * are updated correctly we only need to check that count is correct, values.length == count, max is as expected.
 * To determine that max is as expected one technique would be to make each method sleep for a different amount of time
 * and test that the max is at least that length.
 * <p/>
 * POOL MBEAN
 * <p/>
 * All expected attributes and operations
 * javax.management.MBeanAttributeInfo[description=, name=AccessTimeouts, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=AccessTimeouts.Latest, type=java.lang.String, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Aged, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Aged.Latest, type=java.lang.String, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Available, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Flushed, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Flushed.Latest, type=java.lang.String, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Flushes, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Flushes.Latest, type=java.lang.String, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=GarbageCollected, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=GarbageCollected.Latest, type=java.lang.String, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=IdleTimeout, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=IdleTimeouts, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=IdleTimeouts.Latest, type=java.lang.String, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Instances, type=int, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Interval, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=MaxAge, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=MaxAgeOffset, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=MaxSize, type=int, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=MinSize, type=int, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=MinimumInstances, type=int, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Overdrafts, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Overdrafts.Latest, type=java.lang.String, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=PoolVersion, type=java.util.concurrent.atomic.AtomicInteger, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=ReplaceAged, type=boolean, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Replaced, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Replaced.Latest, type=java.lang.String, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Sweeps, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Sweeps.Latest, type=java.lang.String, read-only, descriptor={}]
 * javax.management.MBeanOperationInfo[description=, name=FilterAttributes, returnType=void, signature=[javax.management.MBeanParameterInfo[description="", name=excludeRegex, type=java.lang.String, descriptor={}], javax.management.MBeanParameterInfo[description="", name=includeRegex, type=java.lang.String, descriptor={}]], impact=unknown, descriptor={}]
 * <p/>
 * Attribute values that only need to be checked once (configuration constants):
 * javax.management.MBeanAttributeInfo[description=, name=MaxAge, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=MaxAgeOffset, type=double, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=MaxSize, type=int, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=MinSize, type=int, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Interval, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=ReplaceAged, type=boolean, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=IdleTimeout, type=long, read-only, descriptor={}]
 * <p/>
 * Attribute values that should be checked with a new pool and again on a maxed pool:
 * javax.management.MBeanAttributeInfo[description=, name=Available, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=MinimumInstances, type=int, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Instances, type=int, read-only, descriptor={}]
 * <p/>
 * Flusing related.  Check before/after
 * javax.management.MBeanAttributeInfo[description=, name=Flushed, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Flushes, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=PoolVersion, type=java.util.concurrent.atomic.AtomicInteger, read-only, descriptor={}]
 * <p/>
 * Requires an invocation against a maxed pool with all instances checked out, must be a strict pool
 * javax.management.MBeanAttributeInfo[description=, name=AccessTimeouts, type=long, read-only, descriptor={}]
 * <p/>
 * See PoolTest on how to potentially test for these
 * javax.management.MBeanAttributeInfo[description=, name=Aged, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=IdleTimeouts, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=ReplaceAged, type=boolean, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Replaced, type=long, read-only, descriptor={}]
 * <p/>
 * Should be greater than 1 and 'Latest' should be no less than System.currentTimeMillis() - Interval
 * javax.management.MBeanAttributeInfo[description=, name=Sweeps, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=Sweeps.Latest, type=java.lang.String, read-only, descriptor={}]
 * <p/>
 * Only testable with a non-strict pool, specified how many instances beyond max have been created
 * javax.management.MBeanAttributeInfo[description=, name=Overdrafts, type=long, read-only, descriptor={}]
 * <p/>
 * Not testable:
 * javax.management.MBeanAttributeInfo[description=, name=GarbageCollected, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=GarbageCollected.Latest, type=java.lang.String, read-only, descriptor={}]
 *
 * @version $Rev$ $Date$
 */
public class StatelessStatisticsTest extends TestCase {

    /**
     * This whole method is a template, feel free to split it anyway you like
     * Fine to have one big
     *
     * @throws Exception
     */
    public void test() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainerInfo.properties.setProperty("AccessTimeout", "100");
        statelessContainerInfo.properties.setProperty("PoolSize", "15");
        statelessContainerInfo.properties.setProperty("PoolMin", "3");
        statelessContainerInfo.properties.setProperty("StrictPooling", "true");
        assembler.createContainer(statelessContainerInfo);

        // Setup the descriptor information

        CounterBean.instances.set(0);

        EjbJar ejbJar = new EjbJar("StatsModule");
        ejbJar.addEnterpriseBean(new StatelessBean(CounterBean.class));

        assembler.createApplication(config.configureApplication(ejbJar));

        javax.naming.Context context = new InitialContext();
        CounterBean bean = (CounterBean) context.lookup("CounterBeanLocalBean");

        // TODO: invoke each method a different number of times
        bean.red();
        bean.green();
        bean.blue();


        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName invocationsName = new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=null,EJBModule=StatsModule,StatelessSessionBean=CounterBean,j2eeType=Invocations,name=CounterBean");
        ObjectName poolName = new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=null,EJBModule=StatsModule,StatelessSessionBean=CounterBean,j2eeType=Pool,name=CounterBean");

        // TODO grab the mbeanInfo and check the expected attributes exist and have the correct return types and parameters
        MBeanInfo invocationsMBeanInfo = server.getMBeanInfo(invocationsName);
        MBeanInfo poolMBeanInfo = server.getMBeanInfo(poolName);

        // TODO The hardest part, check the values of each

        System.out.println("// Attributes:");
        for (MBeanAttributeInfo info : poolMBeanInfo.getAttributes()) {
            System.out.println("// " + info);
        }

        System.out.println("// Operations:");
        for (MBeanOperationInfo info : poolMBeanInfo.getOperations()) {
            System.out.println("// " + info);
        }
    }

    /**
     * convenience method for checking out a specific number of instances.
     * Can be used like so:
     * <p/>
     * // checkout some instances from the pool
     * CountDownLatch startingPistol = checkout(bean, 7);
     * <p/>
     * // Look at pool stats
     * ...
     * 
     * // Release them all back into the pool
     * startingPistol.countDown();
     *
     * @param bean
     * @param count
     * @return
     * @throws InterruptedException
     */
    private CountDownLatch checkout(final CounterBean bean, int count) throws InterruptedException {
        final CountDownLatch startingLine = new CountDownLatch(count);
        final CountDownLatch startingPistol = new CountDownLatch(1);

        for (int i = 0; i < count; i++) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    bean.checkout(startingLine, startingPistol);
                }
            });
            thread.setDaemon(true);
            thread.start();
        }

        startingLine.await(60, TimeUnit.SECONDS);

        return startingPistol;
    }


    @Monitor
    public static class CounterBean {

        public static AtomicInteger instances = new AtomicInteger();
        public static AtomicInteger discardedInstances = new AtomicInteger();

        private int count;

        public CounterBean() {
            count = instances.incrementAndGet();
        }

        @PostConstruct
        private void construct() {
        }

        @PreDestroy
        private void destroy() {
        }

        public void red() {
        }

        public void green() {
        }

        public void blue() {
        }

        public void checkout(CountDownLatch startingLine, CountDownLatch startPistol) {
            try {
                startingLine.countDown();
                startPistol.await(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.interrupted();
                throw new RuntimeException(e);
            }
        }
    }

}
