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
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;
import javax.naming.InitialContext;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
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
 * Not testable:
 * javax.management.MBeanAttributeInfo[description=, name=GarbageCollected, type=long, read-only, descriptor={}]
 * javax.management.MBeanAttributeInfo[description=, name=GarbageCollected.Latest, type=java.lang.String, read-only, descriptor={}]
 *
 * @version $Rev$ $Date$
 */
public class StatelessPoolStatsTest extends TestCase {
    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    private ObjectName objectName;

    /**
     * @throws Exception
     */
    public void testBasic() throws Exception {

        Properties properties = new Properties();
        properties.setProperty("AccessTimeout", "100");
        properties.setProperty("PoolSize", "15");
        properties.setProperty("PoolMin", "3");
        properties.setProperty("StrictPooling", "true");

        deploy("StatsModule", properties);

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
        expectedAttributes.add(new MBeanAttributeInfo("Available", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Flushed", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Flushed.Latest", "java.lang.String", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Flushes", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Flushes.Latest", "java.lang.String", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("GarbageCollected", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("GarbageCollected.Latest", "java.lang.String", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("IdleTimeout", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("IdleTimeouts", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("IdleTimeouts.Latest", "java.lang.String", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Instances", "int", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Interval", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("MaxAge", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("MaxAgeOffset", "double", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("MaxSize", "int", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("MinSize", "int", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("MinimumInstances", "int", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Overdrafts", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Overdrafts.Latest", "java.lang.String", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("PoolVersion", "int", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("ReplaceAged", "boolean", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Replaced", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Replaced.Latest", "java.lang.String", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("StrictPooling", "boolean", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Sweeps", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Sweeps.Latest", "java.lang.String", "", true, false, false));


        // The hardest part, check the values of each, PoolVersion is AtomicaInteger, *.Latest are time-sensitive, so not verified.
        Map<String, Object> expectedAttributesValue = new HashMap<String, Object>();
        expectedAttributesValue.put("AccessTimeouts", (long) 0);
        expectedAttributesValue.put("Aged", (long) 0);
        expectedAttributesValue.put("Available", (long) 15);
        expectedAttributesValue.put("Flushed", (long) 0);
        expectedAttributesValue.put("Flushes", (long) 0);
        expectedAttributesValue.put("GarbageCollected", (long) 0);
        expectedAttributesValue.put("IdleTimeout", (long) 0);
        expectedAttributesValue.put("IdleTimeouts", (long) 0);
        expectedAttributesValue.put("Instances", (int) 3);
        expectedAttributesValue.put("Interval", (long) 300000);
        expectedAttributesValue.put("MaxAge", (long) 0);
        expectedAttributesValue.put("MaxAgeOffset", (double) (-1.0));
        expectedAttributesValue.put("MaxSize", (int) 15);
        expectedAttributesValue.put("MinSize", (int) 3);
        expectedAttributesValue.put("MinimumInstances", (int) 3);
        expectedAttributesValue.put("Overdrafts", (long) 0);
        expectedAttributesValue.put("PoolVersion", (int) 0);
        expectedAttributesValue.put("ReplaceAged", false);
        expectedAttributesValue.put("Replaced", (long) 0);
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
     * javax.management.MBeanAttributeInfo[description=, name=Interval, type=long, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=ReplaceAged, type=boolean, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=IdleTimeout, type=long, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testConfigOptions() throws Exception {

    }

    /**
     * Flusing related.
     * javax.management.MBeanAttributeInfo[description=, name=Flushed, type=long, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=Flushes, type=long, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=PoolVersion, type=java.util.concurrent.atomic.AtomicInteger, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testFlushing() throws Exception {

    }

    /**
     * Attribute values that should be checked with a new pool and again on a maxed pool:
     * javax.management.MBeanAttributeInfo[description=, name=Available, type=long, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=MinimumInstances, type=int, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=Instances, type=int, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testInstances() throws Exception {

    }

    /**
     * javax.management.MBeanAttributeInfo[description=, name=Aged, type=long, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=Replaced, type=long, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testAging() throws Exception {

    }

    /**
     * Requires an invocation against a maxed pool with all instances checked out, must be a strict pool
     * javax.management.MBeanAttributeInfo[description=, name=AccessTimeouts, type=long, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testAccessTimeouts() throws Exception {

    }

    /**
     * Should be greater than 1 and 'Latest' should be no less than System.currentTimeMillis() - Interval
     * javax.management.MBeanAttributeInfo[description=, name=Sweeps, type=long, read-only, descriptor={}]
     * javax.management.MBeanAttributeInfo[description=, name=Sweeps.Latest, type=java.lang.String, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testSweeps() throws Exception {

    }

    /**
     * Fill a pool to the max, let the non-min instances timeout, check the IdleTimeouts
     * javax.management.MBeanAttributeInfo[description=, name=IdleTimeouts, type=long, read-only, descriptor={}]
     *
     * @throws Exception
     */
    public void testIdleTimeouts() throws Exception {

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
        properties.setProperty("PoolSize", "2");
        properties.setProperty("PoolMin", "0");
        properties.setProperty("StrictPooling", "false");

        CounterBean bean = deploy("StatsAdvancedModule", properties);

        CounterBean.discarded = new CountDownLatch(5);

        checkout(bean, 7).countDown();

        assertTrue(CounterBean.discarded.await(10, TimeUnit.SECONDS));

        assertAttribute("MaxSize", 2);
        assertAttribute("MinSize", 0);
        assertAttribute("Available", 2147483647L);
        assertAttribute("Overdrafts", 5L);
        assertAttribute("StrictPooling", false);
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

        public static CountDownLatch discarded = new CountDownLatch(0);

        @PreDestroy
        private void destroy() {
            discarded.countDown();
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