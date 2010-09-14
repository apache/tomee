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

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.PostConstruct;
import javax.ejb.SessionContext;
import javax.ejb.ConcurrentAccessTimeoutException;
import javax.ejb.ConcurrentAccessException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.InitialContext;
import java.io.Flushable;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

public class StatelessPoolStatsTest extends TestCase {
    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    private ObjectName objectName;

    /**
     * @throws Exception
     */
    public void testBasic() throws Exception {

        Properties properties = new Properties();
        properties.setProperty("AccessTimeout", "100");
        properties.setProperty("MaxSize", "15");
        properties.setProperty("MinSize", "3");
        properties.setProperty("StrictPooling", "true");

        deploy("testBasic", properties);

        // Grab the mbeanInfo and check the expected attributes exist and have the correct return types and parameters
        MBeanInfo poolMBeanInfo = server.getMBeanInfo(objectName);

        /*
        * Pool MBeanInfo
        *
        */
        List<MBeanAttributeInfo> expectedAttributes = new ArrayList<MBeanAttributeInfo>();
        expectedAttributes.add(new MBeanAttributeInfo("AccessTimeouts", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("AccessTimeouts.Latest", "java.lang.String", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Aged", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Aged.Latest", "java.lang.String", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("AvailablePermits", "int", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Flushed", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Flushed.Latest", "java.lang.String", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Flushes", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Flushes.Latest", "java.lang.String", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("GarbageCollected", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("GarbageCollected.Latest", "java.lang.String", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("GarbageCollection", "boolean", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("IdleTimeout", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("IdleTimeouts", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("IdleTimeouts.Latest", "java.lang.String", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("InstancesActive", "int", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("InstancesIdle", "int", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("InstancesInitializing", "int", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("InstancesPooled", "int", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("MaxAge", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("MaxAgeOffset", "double", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("MaxSize", "int", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("MinSize", "int", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("MinimumInstances", "int", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Overdrafts", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Overdrafts.Latest", "java.lang.String", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("PoolVersion", "int", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("ReplaceAged", "boolean", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("ReplaceFlushed", "boolean", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Replaced", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Replaced.Latest", "java.lang.String", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("StrictPooling", "boolean", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("SweepInterval", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Sweeps", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Sweeps.Latest", "java.lang.String", "", true, false, false));


        // The hardest part, check the values of each, PoolVersion is AtomicaInteger, *.Latest are time-sensitive, so not verified.
        Map<String, Object> expectedAttributesValue = new HashMap<String, Object>();
        expectedAttributesValue.put("AccessTimeouts", (long) 0);
        expectedAttributesValue.put("Aged", (long) 0);
        expectedAttributesValue.put("AvailablePermits", (int) 15);
        expectedAttributesValue.put("Flushed", (long) 0);
        expectedAttributesValue.put("Flushes", (long) 0);
        expectedAttributesValue.put("GarbageCollected", (long) 0);
        expectedAttributesValue.put("GarbageCollection", true);
        expectedAttributesValue.put("IdleTimeout", (long) 0);
        expectedAttributesValue.put("IdleTimeouts", (long) 0);
        expectedAttributesValue.put("InstancesPooled", (int) 3);
        expectedAttributesValue.put("InstancesActive", (int) 0);
        expectedAttributesValue.put("InstancesIdle", (int) 3);
        expectedAttributesValue.put("InstancesInitializing", (int) 0);
        expectedAttributesValue.put("MaxAge", (long) 0);
        expectedAttributesValue.put("MaxAgeOffset", (double) (-1.0));
        expectedAttributesValue.put("MaxSize", (int) 15);
        expectedAttributesValue.put("MinSize", (int) 3);
        expectedAttributesValue.put("MinimumInstances", (int) 3);
        expectedAttributesValue.put("Overdrafts", (long) 0);
        expectedAttributesValue.put("PoolVersion", (int) 0);
        expectedAttributesValue.put("ReplaceAged", true);
        expectedAttributesValue.put("ReplaceFlushed", false);
        expectedAttributesValue.put("Replaced", (long) 0);
        expectedAttributesValue.put("SweepInterval", (long) 300000);
        expectedAttributesValue.put("Sweeps", (long) 1);
        expectedAttributesValue.put("StrictPooling", true);

        List<MBeanAttributeInfo> actualAttributes = new ArrayList<MBeanAttributeInfo>();
        Map<String, Object> actualAttributesValue = new HashMap<String, Object>();
        for (MBeanAttributeInfo info : poolMBeanInfo.getAttributes()) {
            actualAttributes.add(info);
            if (!info.getName().endsWith(".Latest")) {
                actualAttributesValue.put(info.getName(), server.getAttribute(objectName, info.getName()));
            }
        }

        assertEquals(expectedAttributes, actualAttributes);
        assertEquals(expectedAttributesValue, actualAttributesValue);

        // Grab pool mbean operations
        MBeanParameterInfo[] operations = {
                new MBeanParameterInfo("excludeRegex", "java.lang.String", "\"\""),
                new MBeanParameterInfo("includeRegex", "java.lang.String", "\"\"")};
        List<MBeanOperationInfo> expectedOperations = new ArrayList<MBeanOperationInfo>();
        expectedOperations.add(new MBeanOperationInfo(
                "FilterAttributes",
                "Filters the attributes that show up in the MBeanInfo.  The exclude is applied first, then any attributes that match the include are re-added.  It may be required to disconnect and reconnect the JMX console to force a refresh of the MBeanInfo",
                operations, "void", MBeanOperationInfo.UNKNOWN));

        List<MBeanOperationInfo> actualOperations = new ArrayList<MBeanOperationInfo>();
        actualOperations.addAll(Arrays.asList(poolMBeanInfo.getOperations()));
        assertEquals(expectedOperations, actualOperations);

    }

    /**
     * Attribute values that only need to be checked once (configuration constants):
     * javax.management.MBeanAttributeInfo[description=, name=MaxAge, type=long, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=MaxAgeOffset, type=double, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=MaxSize, type=int, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=MinSize, type=int, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=SweepInterval, type=long, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=ReplaceAged, type=boolean, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=ReplaceFlushed, type=boolean, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=GarbageCollection, type=boolean, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=IdleTimeout, type=long, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testConfigOptions() throws Exception {
        final Properties properties = new Properties();
        properties.put("GarbageCollection", "false");
        properties.put("IdleTimeout", "23 milliseconds");
        properties.put("MaxAge", "12456789 milliseconds");
        properties.put("MaxAgeOffset", "2");
        properties.put("MaxSize", "55");
        properties.put("MinSize", "22");
        properties.put("ReplaceAged", "false");
        properties.put("ReplaceFlushed", "true");
        properties.put("SweepInterval", "999999 milliseconds");

        deploy("testConfigOptions", properties);
        
        assertAttribute("GarbageCollection", false);
        assertAttribute("IdleTimeout", 23L);
        assertAttribute("MaxAge", 12456789L);
        assertAttribute("MaxAgeOffset", 2.0);
        assertAttribute("MaxSize", 55);
        assertAttribute("MinSize", 22);
        assertAttribute("ReplaceAged", false);
        assertAttribute("ReplaceFlushed", true);
        assertAttribute("SweepInterval", 999999L);
    }

    /**
     * Flusing related.
     * javax.management.MBeanAttributeInfo[description=, name=Flushed, type=long, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=Flushes, type=long, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=PoolVersion, type=int, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=Replaced, type=long, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testFlushing() throws Exception {
        final Properties properties = new Properties();
        properties.put("MaxSize", "10");
        properties.put("MinSize", "3");
        properties.put("ReplaceFlushed", "false");
        final CounterBean bean = deploy("testFlushing", properties);

        checkout(bean, 8).release();
        
        CounterBean.constructed = new CountDownLatch(3);

        final Checkout checkout = checkout(bean, 7);

        assertAttribute("PoolVersion", 0);
        assertAttribute("Flushed", 0L);
        assertAttribute("Flushes", 0L);

        bean.flush();

        checkout.release();

        assertAttribute("PoolVersion", 1);
        assertAttribute("Flushed", 8L);
        assertAttribute("Flushes", 1L);

        CounterBean.constructed.await(10, TimeUnit.SECONDS);
        Thread.sleep(1);

        assertAttribute("Replaced", 3L);
    }

    /**
     * Flusing related.
     * javax.management.MBeanAttributeInfo[description=, name=Flushed, type=long, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=Flushes, type=long, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=PoolVersion, type=int, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=Replaced, type=long, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testFlushingWithReplacement() throws Exception {
        final Properties properties = new Properties();
        properties.put("MaxSize", "10");
        properties.put("MinSize", "3");
        properties.put("ReplaceFlushed", "true");
        final CounterBean bean = deploy("testFlushingWithReplacement", properties);

        checkout(bean, 8).release();

        CounterBean.constructed = new CountDownLatch(8);

        final Checkout checkout = checkout(bean, 7);

        assertAttribute("PoolVersion", 0);
        assertAttribute("Flushed", 0L);
        assertAttribute("Flushes", 0L);

        bean.flush();

        checkout.release();

        assertAttribute("PoolVersion", 1);
        assertAttribute("Flushed", 8L);
        assertAttribute("Flushes", 1L);

        CounterBean.constructed.await(10, TimeUnit.SECONDS);
        Thread.sleep(1);

        assertAttribute("Replaced", 8L);
    }

    /**
     * Attribute values that should be checked with a new pool and again on a maxed pool:
     * javax.management.MBeanAttributeInfo[description=, name=AvailablePermits, type=int, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=MinimumInstances, type=int, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=InstancesPooled, type=int, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=InstancesIdle, type=int, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=InstancesActive, type=int, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testInstances() throws Exception {
        final Properties properties = new Properties();
        properties.put("MaxSize", "10");
        properties.put("MinSize", "4");
        final CounterBean bean = deploy("testInstances", properties);

        assertAttribute("AvailablePermits", 10);
        assertAttribute("MinimumInstances", 4);
        assertAttribute("InstancesPooled", 4);
        assertAttribute("InstancesIdle", 4);
        assertAttribute("InstancesActive", 0);

        final Checkout checkout1 = checkout(bean, 3);

        assertAttribute("AvailablePermits", 7);
        assertAttribute("MinimumInstances", 4);
        assertAttribute("InstancesPooled", 4);
        assertAttribute("InstancesIdle", 1);
        assertAttribute("InstancesActive", 3);

        checkout1.release();

        checkout(bean, 6).release();

        final Checkout checkout2 = checkout(bean, 6);

        assertAttribute("AvailablePermits", 4);
        assertAttribute("MinimumInstances", 4);
        assertAttribute("InstancesPooled", 6);
        assertAttribute("InstancesIdle", 0);
        assertAttribute("InstancesActive", 6);

        checkout2.release();
    }

    /**
     * javax.management.MBeanAttributeInfo[description=, name=Aged, type=long, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=Replaced, type=long, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testAging() throws Exception {
        final Properties properties = new Properties();
        properties.put("MaxSize", "10");
        properties.put("MinSize", "3");
        properties.put("MaxAge", "100 ms");
        properties.put("ReplaceAged", "false");
        properties.put("MaxAgeOffset", "0");

        final CounterBean bean = deploy("testAging", properties);

        assertAttribute("MaxAge", 100L);

        checkout(bean, 5).release();
        
        final Checkout checkout = checkout(bean, 5);

        CounterBean.constructed = new CountDownLatch(3);

        Thread.sleep(101);

        checkout.release();

        assertAttribute("Aged", 5L);

        CounterBean.constructed.await(10, TimeUnit.SECONDS);
        Thread.sleep(1);

        assertAttribute("Replaced", 3L);
    }

    /**
     * javax.management.MBeanAttributeInfo[description=, name=Aged, type=long, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=Replaced, type=long, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testAgingWithReplacement() throws Exception {
        final Properties properties = new Properties();
        properties.put("MaxSize", "10");
        properties.put("MinSize", "3");
        properties.put("MaxAge", "100 ms");
        properties.put("ReplaceAged", "true");
        properties.put("MaxAgeOffset", "0");

        final CounterBean bean = deploy("testAgingWithReplacement", properties);

        assertAttribute("MaxAge", 100L);

        checkout(bean, 5).release();

        final Checkout checkout = checkout(bean, 5);

        CounterBean.constructed = new CountDownLatch(5);

        Thread.sleep(101);

        checkout.release();

        assertAttribute("Aged", 5L);

        CounterBean.constructed.await(10, TimeUnit.SECONDS);
        Thread.sleep(1);

        assertAttribute("Replaced", 5L);
    }

    /**
     * Requires an invocation against a maxed pool with all instances checked out, must be a strict pool
     * javax.management.MBeanAttributeInfo[description=, name=AccessTimeouts, type=long, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testAccessTimeouts() throws Exception {
        final Properties properties = new Properties();
        properties.put("MaxSize", "10");
        properties.put("AccessTimeout", "0");
        properties.put("StrictPooling", "true");

        final CounterBean bean = deploy("testAccessTimeouts", properties);

        assertAttribute("AccessTimeouts", 0l);

        final Checkout checkout = checkout(bean, 10);

        for (int i = 0; i < 7; i++) {
            try {
                bean.doSomething();
                fail("ConcurrentAccessException should have been thrown");
            } catch (ConcurrentAccessException expected) {
            }
        }

        checkout.release();
        
        assertAttribute("AccessTimeouts", 7l);
    }

    /**
     * Should be greater than 1 and 'Latest' should be no less than System.currentTimeMillis() - SweepInterval
     * javax.management.MBeanAttributeInfo[description=, name=Sweeps, type=long, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=Sweeps.Latest, type=java.lang.String, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testSweeps() throws Exception {
    	Properties properties = new Properties();
    	properties.setProperty("SweepInterval", "1");

        deploy("testSweeps", properties);

        assertTrue((Long)(server.getAttribute(objectName, "Sweeps")) >= 1L);

        //Get current time
        DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance();

        Date expected = new Date(System.currentTimeMillis() - 1 * 60 * 1000L);

        final String latest = (String) (server.getAttribute(objectName, "Sweeps.Latest"));
        assertTrue(dateFormatter.parse(latest).after(expected));
    }

    /**
     * Fill a pool to the max, let the non-min instances timeout, check the IdleTimeouts
     * javax.management.MBeanAttributeInfo[description=, name=IdleTimeouts, type=long, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testIdleTimeouts() throws Exception {
        final Properties properties = new Properties();
        properties.put("MaxSize", "10");
        properties.put("MinSize", "3");
        properties.put("IdleTimeout", "100 ms");
        properties.put("SweepInterval", "50 ms");

        final CounterBean bean = deploy("testIdleTimeouts", properties);

        assertAttribute("SweepInterval", 50L);
        assertAttribute("IdleTimeout", 100L);

        CounterBean.discarded = new CountDownLatch(2);
        
        checkout(bean, 5).release();

        assertAttribute("InstancesPooled", 5);
        assertAttribute("IdleTimeouts", 0L);

        CounterBean.discarded.await(10, TimeUnit.SECONDS);
        Thread.sleep(1);

        assertAttribute("InstancesPooled", 3);
        assertAttribute("IdleTimeouts", 2L);
    }

    /**
     * Drain more than the max from a non-strict pool and test that Overdrafts
     * reflects the number of instances beyond the max that were created
     * javax.management.MBeanAttributeInfo[description=, name=Overdrafts, type=long, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testOverdrafts() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("MaxSize", "2");
        properties.setProperty("MinSize", "0");
        properties.setProperty("StrictPooling", "false");

        final CounterBean bean = deploy("Overdrafts", properties);

        assertAttribute("MaxSize", 2);
        assertAttribute("MinSize", 0);
        assertAttribute("StrictPooling", false);
        assertAttribute("Overdrafts", 0L);
        assertAttribute("AvailablePermits", 2);

        final Checkout checkout = checkout(bean, 7);

        assertAttribute("Overdrafts", 0L);
        assertAttribute("AvailablePermits", -5);
        
        checkout.release();

        assertAttribute("AvailablePermits", 2);
        assertAttribute("Overdrafts", 5L);
    }

    private void assertAttribute(String name, Object value) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
        assertEquals(name, value, server.getAttribute(objectName, name));
    }

    private CounterBean deploy(String moduleId, Properties properties) throws Exception {
        objectName = new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=null,EJBModule=" + moduleId + ",StatelessSessionBean=CounterBean,j2eeType=Pool,name=CounterBean");

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainerInfo.properties.putAll(properties);

        assembler.createContainer(statelessContainerInfo);

        EjbJar ejbJar = new EjbJar(moduleId);
        ejbJar.addEnterpriseBean(new StatelessBean(CounterBean.class));
        assembler.createApplication(config.configureApplication(ejbJar));

        javax.naming.Context context = new InitialContext();
        return (CounterBean) context.lookup("CounterBeanLocalBean");
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
    private Checkout checkout(final CounterBean bean, int count) throws InterruptedException {
        final CountDownLatch startingLine = new CountDownLatch(count);

        final Checkout checkout = new Checkout(count);

        for (int i = 0; i < count; i++) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    bean.checkout(startingLine, checkout.startingPistol);
                    checkout.finishLine.countDown();
                }
            });
            thread.setDaemon(true);
            thread.start();
        }

        startingLine.await(60, TimeUnit.SECONDS);

        return checkout;
    }

    public static class Checkout {
        final CountDownLatch startingPistol = new CountDownLatch(1);
        final CountDownLatch finishLine;

        public Checkout(int count) {
            finishLine = new CountDownLatch(count);
        }

        public boolean release() {
            try {
                startingPistol.countDown();
                return finishLine.await(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.interrupted();
                return false;
            }
        }
    }

    @Monitor
    public static class CounterBean {

        public static CountDownLatch discarded = new CountDownLatch(0);
        public static CountDownLatch constructed = new CountDownLatch(0);

        @Resource
        private SessionContext sessionContext;

        @PostConstruct
        private void construct() {
            constructed.countDown();
        }

        @PreDestroy
        private void destroy() {
            discarded.countDown();
        }

        public void doSomething(){}
        
        public void checkout(CountDownLatch startingLine, CountDownLatch startPistol) {
            try {
                startingLine.countDown();
                startPistol.await(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.interrupted();
                throw new RuntimeException(e);
            }
        }

        public void flush() throws IOException {
            ((Flushable) sessionContext).flush();
        }
    }


}