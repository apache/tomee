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
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
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
 *
 * @version $Rev$ $Date$
 */
public class StatelessInvocationStatsTest extends TestCase {

    /**
     * This whole method is a template, feel free to split it anyway you like
     * Fine to have one big
     *
     * @throws Exception
     */
    public void testBasic() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainerInfo.properties.setProperty("AccessTimeout", "100");
        statelessContainerInfo.properties.setProperty("MaxSize", "15");
        statelessContainerInfo.properties.setProperty("MinSize", "3");
        statelessContainerInfo.properties.setProperty("StrictPooling", "true");
        assembler.createContainer(statelessContainerInfo);

        // Setup the descriptor information
        CounterBean.instances.set(0);

        EjbJar ejbJar = new EjbJar("StatsModule");
        ejbJar.addEnterpriseBean(new StatelessBean(CounterBean.class));

        assembler.createApplication(config.configureApplication(ejbJar));

        javax.naming.Context context = new InitialContext();
        CounterBean bean = (CounterBean) context.lookup("CounterBeanLocalBean");

        // Invoke each method once
        bean.red();
        bean.green();
        bean.blue();

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName invocationsName = new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=null,EJBModule=StatsModule,StatelessSessionBean=CounterBean,j2eeType=Invocations,name=CounterBean");

        // Grab the mbeanInfo and check the expected attributes exist and have the correct return types and parameters

        /*
        * Invocation MBeanInfo
        *
        */
        List<MBeanAttributeInfo> expectedAttributes = new ArrayList<MBeanAttributeInfo>();
        expectedAttributes.add(new MBeanAttributeInfo("InvocationCount", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("InvocationTime", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("MonitoredMethods", "long", "", true, false, false));

        Map<String, Object> expectedValues = new HashMap<String, Object>();
        expectedValues.put("InvocationCount", (long) 6);
        expectedValues.put("InvocationTime", (long) 0);
        expectedValues.put("MonitoredMethods", (long) 4);


        String[] methods = {"PostConstruct()", "blue()", "green()", "red()"};
        for (String s : methods) {
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
            expectedValues.put(s + ".GeometricMean", (double) 0.0);
            expectedValues.put(s + ".Kurtosis", Double.NaN);
            expectedValues.put(s + ".Max", (double) 0.0);
            expectedValues.put(s + ".Mean", (double) 0.0);
            expectedValues.put(s + ".Min", (double) 0.0);
            expectedValues.put(s + ".Percentile01", (double) 0.0);
            expectedValues.put(s + ".Percentile10", (double) 0.0);
            expectedValues.put(s + ".Percentile25", (double) 0.0);
            expectedValues.put(s + ".Percentile50", (double) 0.0);
            expectedValues.put(s + ".Percentile75", (double) 0.0);
            expectedValues.put(s + ".Percentile90", (double) 0.0);
            expectedValues.put(s + ".Percentile99", (double) 0.0);
            expectedValues.put(s + ".SampleSize", (int) 2000);
            expectedValues.put(s + ".Skewness", Double.NaN);
            expectedValues.put(s + ".StandardDeviation", (double) 0.0);
            expectedValues.put(s + ".Sum", (double) 0.0);
            expectedValues.put(s + ".Sumsq", (double) 0.0);
            expectedValues.put(s + ".Variance", (double) 0.0);
        }

        List<MBeanAttributeInfo> actualAttributes = new ArrayList<MBeanAttributeInfo>();
        Map<String, Object> actualValues = new HashMap<String, Object>();
        MBeanInfo beanInfo = server.getMBeanInfo(invocationsName);
        for (MBeanAttributeInfo info : beanInfo.getAttributes()) {
            actualAttributes.add(info);
            actualValues.put(info.getName(), server.getAttribute(invocationsName, info.getName()));
        }

        //Verify invocation attributes and values
        assertEquals(expectedAttributes, actualAttributes);
        assertEquals(expectedValues, actualValues);

        // Grab invocation mbean operations
        MBeanParameterInfo[] invocationParameters1 = {
                new MBeanParameterInfo("excludeRegex", "java.lang.String", "\"\""),
                new MBeanParameterInfo("includeRegex", "java.lang.String", "\"\"")};
        MBeanParameterInfo[] invocationParameters2 = {
                new MBeanParameterInfo("p1", "int", "")};

        List<MBeanOperationInfo> expectedOperations = new ArrayList<MBeanOperationInfo>();
        expectedOperations.add(new MBeanOperationInfo(
                "FilterAttributes",
                "Filters the attributes that show up in the MBeanInfo.  The exclude is applied first, then any attributes that match the include are re-added.  It may be required to disconnect and reconnect the JMX console to force a refresh of the MBeanInfo",
                invocationParameters1, "void", MBeanOperationInfo.UNKNOWN));

        for (String s : methods) {
            expectedOperations.add(new MBeanOperationInfo(s + ".setSampleSize", "", invocationParameters2, "void", MBeanOperationInfo.UNKNOWN));
            expectedOperations.add(new MBeanOperationInfo(s + ".sortedValues", "", new MBeanParameterInfo[0], "[D", MBeanOperationInfo.UNKNOWN));
            expectedOperations.add(new MBeanOperationInfo(s + ".values", "", new MBeanParameterInfo[0], "[D", MBeanOperationInfo.UNKNOWN));
        }

        List<MBeanOperationInfo> actualOperations1 = new ArrayList<MBeanOperationInfo>();
        actualOperations1.addAll(Arrays.asList(beanInfo.getOperations()));

        //Verify invocation operation information and remove bean.
        assertEquals(expectedOperations, actualOperations1);
    }

    /**
     * @throws Exception
     */
    public void testInvocation() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainerInfo.properties.setProperty("AccessTimeout", "0");
        statelessContainerInfo.properties.setProperty("MaxSize", "2");
        statelessContainerInfo.properties.setProperty("MinSize", "0");
        statelessContainerInfo.properties.setProperty("StrictPooling", "true");
        statelessContainerInfo.properties.setProperty("PollInterval", "1");
        statelessContainerInfo.properties.setProperty("IdleTimeout", "0");

        assembler.createContainer(statelessContainerInfo);

        // Setup the descriptor information
        CounterBean.instances.set(0);

        EjbJar ejbJar = new EjbJar("StatsInvocModule");
        ejbJar.addEnterpriseBean(new StatelessBean(CounterBean.class));


        assembler.createApplication(config.configureApplication(ejbJar));

        javax.naming.Context context = new InitialContext();
        CounterBean bean = (CounterBean) context.lookup("CounterBeanLocalBean");

        //Invoke  
        bean.waitSecs();

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName invocationsName = new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=null,EJBModule=StatsInvocModule,StatelessSessionBean=CounterBean,j2eeType=Invocations,name=CounterBean");

        // Grab the mbeanInfo and check the expected attributes exist and have the correct return types and parameters        
        MBeanInfo invocationsMBeanInfo = server.getMBeanInfo(invocationsName);
        for (MBeanAttributeInfo info : invocationsMBeanInfo.getAttributes()) {
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
                assertTrue(((Double) (server.getAttribute(invocationsName, info.getName()))) >= 999);
            }
        }
        ejbJar.removeEnterpriseBean("StatsInvocModule");
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
     * <p/>
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

        public void waitSecs() throws InterruptedException {
            Thread.sleep((long) (1000));
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
