/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.stateless;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.api.Monitor;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.OpenEJBInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * INVOCATIONS MBEAN
 *
 * All beans have this set of attributes and operations by default:
 *
 * javax.management.MBeanAttributeInfo[description=, name=InvocationCount, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=InvocationTime, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=MonitoredMethods, type=long, read-only, descriptor={}]
 * javax.management.MBeanOperationInfo[description=, name=FilterAttributes, returnType=void, signature=[javax.management.MBeanParameterInfo[description="", name=excludeRegex, type=java
 * .lang.String, descriptor={}], javax.management.MBeanParameterInfo[description="", name=includeRegex, type=java.lang.String, descriptor={}]], impact=unknown, descriptor={}]
 *
 * Then for every method there will be these attributes and operations:
 *
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
 * javax.management.MBeanOperationInfo[description=, name=someMethod().setSampleSize, returnType=void, signature=[javax.management.MBeanParameterInfo[description=, name=p1, type=int,
 * descriptor={}]], impact=unknown, descriptor={}]
 * javax.management.MBeanOperationInfo[description=, name=someMethod().sortedValues, returnType=[D, signature=[], impact=unknown, descriptor={}]
 * javax.management.MBeanOperationInfo[description=, name=someMethod().values, returnType=[D, signature=[], impact=unknown, descriptor={}]
 *
 * Attribute values that should be tested:
 * javax.management.MBeanAttributeInfo[description=, name=InvocationCount, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=InvocationTime, type=long, read-only, descriptor={}]
 *
 * Atribute values that should be tested per method:
 *
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Count, type=long, read-only, descriptor={}]
 * javax.management.MBeanOperationInfo[description=, name=someMethod().values, returnType=[D, signature=[], impact=unknown, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=someMethod().Max, type=double, read-only, descriptor={}]
 *
 * We should test that all expected attributes are there, but when testing each method's values
 * are updated correctly we only need to check that count is correct, values.length == count, max is as expected.
 * To determine that max is as expected one technique would be to make each method sleep for a different amount of time
 * and test that the max is at least that length.
 *
 * @version $Rev$ $Date$
 */
public class StatelessInvocationStatsTest {

    @Before
    public void setUp() {
        System.setProperty(LocalMBeanServer.OPENEJB_JMX_ACTIVE, Boolean.TRUE.toString());
    }

    @After
    public void tearDown() {
        System.clearProperty(LocalMBeanServer.OPENEJB_JMX_ACTIVE);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    /**
     * This whole method is a template, feel free to split it anyway you like
     * Fine to have one big
     *
     * @throws Exception On error
     */
    @Test
    public void testBasic() throws Exception {
        // some pre-load to avoid to load the class lazily with the first invocation
        new CounterBean().red();
        new CounterBean().blue();
        new CounterBean().green();
        // end preload

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, OpenEJBInitialContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        final StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainerInfo.properties.setProperty("AccessTimeout", "100");
        statelessContainerInfo.properties.setProperty("MaxSize", "15");
        statelessContainerInfo.properties.setProperty("MinSize", "3");
        statelessContainerInfo.properties.setProperty("StrictPooling", "true");
        assembler.createContainer(statelessContainerInfo);

        // Setup the descriptor information
        CounterBean.instances.set(0);

        final EjbJar ejbJar = new EjbJar("StatsModule");
        ejbJar.addEnterpriseBean(new StatelessBean(CounterBean.class));

        assembler.createApplication(config.configureApplication(ejbJar));

        final javax.naming.Context context = new InitialContext();
        final CounterBean bean = (CounterBean) context.lookup("CounterBeanLocalBean");

        // Invoke each method once
        bean.red();
        bean.green();
        bean.blue();

        final MBeanServer server = LocalMBeanServer.get();
        final ObjectName invocationsName = new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=<empty>,EJBModule=StatsModule,StatelessSessionBean=CounterBean," +
                "j2eeType=Invocations,name=CounterBean");

        // Grab the mbeanInfo and check the expected attributes exist and have the correct return types and parameters

        /*
         * Invocation MBeanInfo
         *
         */
        final List<MBeanAttributeInfo> expectedAttributes = new ArrayList<>();
        expectedAttributes.add(new MBeanAttributeInfo("InvocationCount", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("InvocationTime", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("MonitoredMethods", "long", "", true, false, false));

        final Map<String, Object> expectedValues = new TreeMap<>();
        expectedValues.put("InvocationCount", (long) 6);
        expectedValues.put("InvocationTime", (long) 0);
        expectedValues.put("MonitoredMethods", (long) 4);


        final String[] methods = {"PostConstruct()", "blue()", "green()", "red()"};
        for (final String s : methods) {
            expectedAttributes.add(new MBeanAttributeInfo(s + ".Count", "long", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".GeometricMean", "double", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".Kurtosis", "double", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".Max", "double", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".Mean", "double", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".Min", "double", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".Percentile01", "double", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".Percentile10", "double", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".Percentile25", "double", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".Percentile50", "double", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".Percentile75", "double", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".Percentile90", "double", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".Percentile99", "double", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".SampleSize", "int", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".Skewness", "double", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".StandardDeviation", "double", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".Sum", "double", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".Sumsq", "double", "", true, false, false));
            expectedAttributes.add(new MBeanAttributeInfo(s + ".Variance", "double", "", true, false, false));
            if (s.equals("PostConstruct()")) {
                expectedValues.put(s + ".Count", (long) 3);
            } else {
                expectedValues.put(s + ".Count", (long) 1);
            }
            expectedValues.put(s + ".GeometricMean", 0.0);
            expectedValues.put(s + ".Kurtosis", Double.NaN);
            expectedValues.put(s + ".Max", 0.0);
            expectedValues.put(s + ".Mean", 0.0);
            expectedValues.put(s + ".Min", 0.0);
            expectedValues.put(s + ".Percentile01", 0.0);
            expectedValues.put(s + ".Percentile10", 0.0);
            expectedValues.put(s + ".Percentile25", 0.0);
            expectedValues.put(s + ".Percentile50", 0.0);
            expectedValues.put(s + ".Percentile75", 0.0);
            expectedValues.put(s + ".Percentile90", 0.0);
            expectedValues.put(s + ".Percentile99", 0.0);
            expectedValues.put(s + ".SampleSize", 2000);
            expectedValues.put(s + ".Skewness", Double.NaN);
            expectedValues.put(s + ".StandardDeviation", 0.0);
            expectedValues.put(s + ".Sum", 0.0);
            expectedValues.put(s + ".Sumsq", 0.0);
            expectedValues.put(s + ".Variance", 0.0);
        }

        final List<MBeanAttributeInfo> actualAttributes = new ArrayList<>();
        final Map<String, Object> actualValues = new TreeMap<>();
        final MBeanInfo beanInfo = server.getMBeanInfo(invocationsName);
        for (final MBeanAttributeInfo info : beanInfo.getAttributes()) {
            actualAttributes.add(info);
            actualValues.put(info.getName(), server.getAttribute(invocationsName, info.getName()));
        }

        //Verify invocation attributes and values
        Assert.assertEquals(expectedAttributes, actualAttributes);
        boolean ok = true;
        for (final Map.Entry<String, Object> entry : actualValues.entrySet()) {
            final Number value = (Number) expectedValues.get(entry.getKey());
            final Number real = (Number) actualValues.get(entry.getKey());

            if (!value.equals(real)) { // tolerating a 1 wide range
                Logger.getLogger(StatelessInvocationStatsTest.class.getName()).log(Level.WARNING, "Test tolerance: " + entry.getKey() + " => " + entry.getValue() + "/" + expectedValues
                        .get(entry.getKey()));
                final Double abs = Math.abs(real.doubleValue() - value.doubleValue());
                if (abs.intValue() > 1) {
                    ok = false;
                }
            }
        }

        Assert.assertTrue("Expected status to be true, but was: " + ok, ok);

        // Grab invocation mbean operations
        final MBeanParameterInfo[] invocationParameters1 = {
                new MBeanParameterInfo("excludeRegex", "java.lang.String", "\"\""),
                new MBeanParameterInfo("includeRegex", "java.lang.String", "\"\"")};
        final MBeanParameterInfo[] invocationParameters2 = {
                new MBeanParameterInfo("p1", "int", "")};

        final List<MBeanOperationInfo> expectedOperations = new ArrayList<>();
        expectedOperations.add(new MBeanOperationInfo(
                "FilterAttributes",
                "Filters the attributes that show up in the MBeanInfo.  The exclude is applied first, then any attributes that match the include are re-added.  It may be required to " +
                        "disconnect and reconnect the JMX console to force a refresh of the MBeanInfo",
                invocationParameters1, "void", MBeanOperationInfo.UNKNOWN));

        for (final String s : methods) {
            expectedOperations.add(new MBeanOperationInfo(s + ".setSampleSize", "", invocationParameters2, "void", MBeanOperationInfo.UNKNOWN));
            expectedOperations.add(new MBeanOperationInfo(s + ".sortedValues", "", new MBeanParameterInfo[0], "[D", MBeanOperationInfo.UNKNOWN));
            expectedOperations.add(new MBeanOperationInfo(s + ".values", "", new MBeanParameterInfo[0], "[D", MBeanOperationInfo.UNKNOWN));
        }

        final List<MBeanOperationInfo> actualOperations1 = new ArrayList<>(Arrays.asList(beanInfo.getOperations()));

        //Verify invocation operation information and remove bean.
        Assert.assertEquals(expectedOperations, actualOperations1);
    }

    /**
     * @throws Exception On error
     */
    @Test
    public void testInvocation() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, OpenEJBInitialContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        final StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainerInfo.properties.setProperty("AccessTimeout", "0");
        statelessContainerInfo.properties.setProperty("MaxSize", "2");
        statelessContainerInfo.properties.setProperty("MinSize", "0");
        statelessContainerInfo.properties.setProperty("StrictPooling", "true");
        statelessContainerInfo.properties.setProperty("IdleTimeout", "0");

        assembler.createContainer(statelessContainerInfo);

        // Setup the descriptor information
        CounterBean.instances.set(0);

        final EjbJar ejbJar = new EjbJar("StatsInvocModule");
        ejbJar.addEnterpriseBean(new StatelessBean(CounterBean.class));


        assembler.createApplication(config.configureApplication(ejbJar));

        final javax.naming.Context context = new InitialContext();
        final CounterBean bean = (CounterBean) context.lookup("CounterBeanLocalBean");

        //Invoke  
        bean.waitSecs();

        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        final ObjectName invocationsName = new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=<empty>,EJBModule=StatsInvocModule,StatelessSessionBean=CounterBean," +
                "j2eeType=Invocations,name=CounterBean");

        // Grab the mbeanInfo and check the expected attributes exist and have the correct return types and parameters        
        final MBeanInfo invocationsMBeanInfo = server.getMBeanInfo(invocationsName);
        for (final MBeanAttributeInfo info : invocationsMBeanInfo.getAttributes()) {
            //            System.out.println("//" + info.getName() + " " + server.getAttribute(invocationsName, info.getName()));
            if (info.getName().equals("waitSecs().GeometricMean")
                    || info.getName().equals("waitSecs().Max")
                    || info.getName().equals("waitSecs().Mean")
                    || info.getName().equals("waitSecs().Min")
                    || info.getName().equals("waitSecs().Percentile01")
                    || info.getName().equals("waitSecs().Percentile10")
                    || info.getName().equals("waitSecs().Percentile25")
                    || info.getName().equals("waitSecs().Percentile50")
                    || info.getName().equals("waitSecs().Percentile75")
                    || info.getName().equals("waitSecs().Percentile90")
                    || info.getName().equals("waitSecs().Percentile99")
                    || info.getName().equals("waitSecs().Sum")) {
                final Double actual = (Double) (server.getAttribute(invocationsName, info.getName()));
                Assert.assertTrue("Expected: " + actual + " >= 999", actual >= 999);
            }
        }
        ejbJar.removeEnterpriseBean("StatsInvocModule");
    }

    /**
     * convenience method for checking out a specific number of instances.
     * Can be used like so:
     *
     * // checkout some instances from the pool
     * CountDownLatch startingPistol = checkout(bean, 7);
     *
     * // Look at pool stats
     * ...
     *
     * // Release them all back into the pool
     * startingPistol.countDown();
     *
     * @param bean CounterBean
     * @param count int
     * @return CountDownLatch
     * @throws InterruptedException On error
     */
    private CountDownLatch checkout(final CounterBean bean, final int count) throws InterruptedException {
        final CountDownLatch startingLine = new CountDownLatch(count);
        final CountDownLatch startingPistol = new CountDownLatch(1);

        for (int i = 0; i < count; i++) {
            final Thread thread = new Thread(new Runnable() {
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

        private final int count;

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

        public void waitSecs() throws InterruptedException {
            // Sleep is not guaranteed to be super accurate
            // On windows it sleeps for a bit under the specified time
            Thread.sleep((long) (1100));
        }

        public void checkout(final CountDownLatch startingLine, final CountDownLatch startPistol) {
            try {
                startingLine.countDown();
                startPistol.await(60, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                Thread.interrupted();
                throw new RuntimeException(e);
            }
        }
    }

}
