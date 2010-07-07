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
    public void testBasic() throws Exception {

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

        // Invoke each method once
        bean.red();
        bean.green();
        bean.blue();

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName invocationsName = new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=null,EJBModule=StatsModule,StatelessSessionBean=CounterBean,j2eeType=Invocations,name=CounterBean");
        ObjectName poolName = new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=null,EJBModule=StatsModule,StatelessSessionBean=CounterBean,j2eeType=Pool,name=CounterBean");

        // Grab the mbeanInfo and check the expected attributes exist and have the correct return types and parameters        
        MBeanInfo invocationsMBeanInfo = server.getMBeanInfo(invocationsName);
        MBeanInfo poolMBeanInfo = server.getMBeanInfo(poolName);


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
        expectedAttributes.add(new MBeanAttributeInfo("PoolVersion", "java.util.concurrent.atomic.AtomicInteger", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("ReplaceAged", "boolean", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Replaced", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Replaced.Latest", "java.lang.String", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Sweeps", "long", "", true, false, false));
        expectedAttributes.add(new MBeanAttributeInfo("Sweeps.Latest", "java.lang.String", "", true, false, false));


        // The hardest part, check the values of each, PoolVersion is AtomicaInteger, *.Latest are time-sensitive, so not verified.
        Map<String, Object> expectedAttributesValue = new HashMap<String, Object>();
        expectedAttributesValue.put("Aged", (long) 0);
        expectedAttributesValue.put("Available", (long) 15);
        expectedAttributesValue.put("Flushed", (long) 0);
        expectedAttributesValue.put("Flushes", (long) 0);
        expectedAttributesValue.put("GarbageCollected", (long) 0);
        expectedAttributesValue.put("IdleTimeout", (long) 0);
        expectedAttributesValue.put("Instances", (int) 3);
        expectedAttributesValue.put("Interval", (long) 300000);
        expectedAttributesValue.put("MaxAge", (long) 0);
        expectedAttributesValue.put("MaxAgeOffset", (double) (-1.0));
        expectedAttributesValue.put("MaxSize", (int) 15);
        expectedAttributesValue.put("MinSize", (int) 3);
        expectedAttributesValue.put("MinimumInstances", (int) 3);
        expectedAttributesValue.put("Overdrafts", (long) 0);
        //expectedAttributesValue.put("PoolVersion", new AtomicInteger(0));
        expectedAttributesValue.put("ReplaceAged", false);
        expectedAttributesValue.put("Replaced", (long) 0);
        expectedAttributesValue.put("Sweeps", (long) 1);

        List<MBeanAttributeInfo> actualAttributes = new ArrayList<MBeanAttributeInfo>();
        Map<String, Object> actualAttributesValue = new HashMap<String, Object>();
        for (MBeanAttributeInfo info : poolMBeanInfo.getAttributes()) {
            actualAttributes.add(info);
            if (info.getName().equals("Aged")
                    || info.getName().equals("Available")
                    || info.getName().equals("MinimumInstances")
                    || info.getName().equals("MaxSize")
                    || info.getName().equals("MinSize")
                    || info.getName().equals("IdleTimeout")
                    || info.getName().equals("Instances")
                    || info.getName().equals("Interval")
                    || info.getName().equals("Flushed")
                    || info.getName().equals("Flushes")
                    || info.getName().equals("GarbageCollected")
                    || info.getName().equals("Overdrafts")
                    || info.getName().equals("ReplaceAged")
                    || info.getName().equals("Replaced")
                    || info.getName().equals("Sweeps")
                    || info.getName().equals("MaxAge")
                    || info.getName().equals("MaxAgeOffset")) {
                actualAttributesValue.put(info.getName(), server.getAttribute(
                        poolName, info.getName()));
            }
            //System.out.println(info.getName() + " " + server.getAttribute(poolName, info.getName()));            
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
        for (MBeanOperationInfo info : poolMBeanInfo.getOperations()) {
            //System.out.println("// " + info);
            actualOperations.add(info);
        }
        assertEquals(expectedOperations, actualOperations);


        /*
        * Invocation MBeanInfo
        *
        */
        List<MBeanAttributeInfo> expectedInvocationAttributes = new ArrayList<MBeanAttributeInfo>();
        expectedInvocationAttributes.add(new MBeanAttributeInfo("InvocationCount", "long", "", true, false, false));
        expectedInvocationAttributes.add(new MBeanAttributeInfo("InvocationTime", "long", "", true, false, false));
        expectedInvocationAttributes.add(new MBeanAttributeInfo("MonitoredMethods", "long", "", true, false, false));

        Map<String, Object> expectedInvocationAttributesValue = new HashMap<String, Object>();
        expectedInvocationAttributesValue.put("InvocationCount", (long) 6);
        expectedInvocationAttributesValue.put("InvocationTime", (long) 0);
        expectedInvocationAttributesValue.put("MonitoredMethods", (long) 4);


        String[] methods = {"PostConstruct()", "blue()", "green()", "red()"};
        for (String s : methods) {
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".Count", "long", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".GeometricMean", "double", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".Kurtosis", "double", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".Max", "double", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".Mean", "double", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".Min", "double", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".Percentile01", "double", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".Percentile10", "double", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".Percentile25", "double", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".Percentile50", "double", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".Percentile75", "double", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".Percentile90", "double", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".Percentile99", "double", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".SampleSize", "int", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".Skewness", "double", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".StandardDeviation", "double", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".Sum", "double", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".Sumsq", "double", "", true, false, false));
            expectedInvocationAttributes.add(new MBeanAttributeInfo(s + ".Variance", "double", "", true, false, false));
            if (s.equals("PostConstruct()")) {
                expectedInvocationAttributesValue.put(s + ".Count", (long) 3);
            } else {
                expectedInvocationAttributesValue.put(s + ".Count", (long) 1);
            }
            expectedInvocationAttributesValue.put(s + ".GeometricMean", (double) 0.0);
            expectedInvocationAttributesValue.put(s + ".Kurtosis", Double.NaN);
            expectedInvocationAttributesValue.put(s + ".Max", (double) 0.0);
            expectedInvocationAttributesValue.put(s + ".Mean", (double) 0.0);
            expectedInvocationAttributesValue.put(s + ".Min", (double) 0.0);
            expectedInvocationAttributesValue.put(s + ".Percentile01", (double) 0.0);
            expectedInvocationAttributesValue.put(s + ".Percentile10", (double) 0.0);
            expectedInvocationAttributesValue.put(s + ".Percentile25", (double) 0.0);
            expectedInvocationAttributesValue.put(s + ".Percentile50", (double) 0.0);
            expectedInvocationAttributesValue.put(s + ".Percentile75", (double) 0.0);
            expectedInvocationAttributesValue.put(s + ".Percentile90", (double) 0.0);
            expectedInvocationAttributesValue.put(s + ".Percentile99", (double) 0.0);
            expectedInvocationAttributesValue.put(s + ".SampleSize", (int) 2000);
            expectedInvocationAttributesValue.put(s + ".Skewness", Double.NaN);
            expectedInvocationAttributesValue.put(s + ".StandardDeviation", (double) 0.0);
            expectedInvocationAttributesValue.put(s + ".Sum", (double) 0.0);
            expectedInvocationAttributesValue.put(s + ".Sumsq", (double) 0.0);
            expectedInvocationAttributesValue.put(s + ".Variance", (double) 0.0);
        }

        List<MBeanAttributeInfo> actualInvocationAttributes = new ArrayList<MBeanAttributeInfo>();
        Map<String, Object> actualInvocationAttributesValue = new HashMap<String, Object>();
        for (MBeanAttributeInfo info : invocationsMBeanInfo.getAttributes()) {
            //System.out.println(info.getName() + " " + server.getAttribute(invocationsName, info.getName()));
            actualInvocationAttributes.add(info);
            actualInvocationAttributesValue.put(info.getName(), server.getAttribute(invocationsName, info.getName()));
        }
        //Verify invocation attributes and values
        assertEquals(expectedInvocationAttributes, actualInvocationAttributes);
        assertEquals(expectedInvocationAttributesValue, actualInvocationAttributesValue);

        // Grab invocation mbean operations
        MBeanParameterInfo[] invocationParameters1 = {
                new MBeanParameterInfo("excludeRegex", "java.lang.String", "\"\""),
                new MBeanParameterInfo("includeRegex", "java.lang.String", "\"\"")};
        MBeanParameterInfo[] invocationParameters2 = {
                new MBeanParameterInfo("p1", "int", "")};

        List<MBeanOperationInfo> expectedInvocationOperations = new ArrayList<MBeanOperationInfo>();
        expectedInvocationOperations.add(new MBeanOperationInfo(
                "FilterAttributes",
                "Filters the attributes that show up in the MBeanInfo.  The exclude is applied first, then any attributes that match the include are re-added.  It may be required to disconnect and reconnect the JMX console to force a refresh of the MBeanInfo",
                invocationParameters1, "void", MBeanOperationInfo.UNKNOWN));

        for (String s : methods) {
            expectedInvocationOperations.add(new MBeanOperationInfo(s + ".setSampleSize", "", invocationParameters2, "void", MBeanOperationInfo.UNKNOWN));
            expectedInvocationOperations.add(new MBeanOperationInfo(s + ".sortedValues", "", new MBeanParameterInfo[0], "[D", MBeanOperationInfo.UNKNOWN));
            expectedInvocationOperations.add(new MBeanOperationInfo(s + ".values", "", new MBeanParameterInfo[0], "[D", MBeanOperationInfo.UNKNOWN));
        }

        List<MBeanOperationInfo> actualInvocationOperations = new ArrayList<MBeanOperationInfo>();
        for (MBeanOperationInfo info : invocationsMBeanInfo.getOperations()) {
            //System.out.println("// " + info);
            actualInvocationOperations.add(info);
        }

        //Verify invocation operation information and remove bean.
        assertEquals(expectedInvocationOperations, actualInvocationOperations);
        ejbJar.removeEnterpriseBean("StatsModule");
    }

    /**
     * @throws Exception
     */
    public void testAdvanced() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainerInfo.properties.setProperty("AccessTimeout", "0");
        statelessContainerInfo.properties.setProperty("PoolSize", "2");
        statelessContainerInfo.properties.setProperty("PoolMin", "0");
        statelessContainerInfo.properties.setProperty("StrictPooling", "false");
        statelessContainerInfo.properties.setProperty("PollInterval", "1");
        statelessContainerInfo.properties.setProperty("IdleTimeout", "30000");
        //statelessContainerInfo.properties.setProperty("PoolVersion", "1.0");

        assembler.createContainer(statelessContainerInfo);

        // Setup the descriptor information
        CounterBean.instances.set(0);

        EjbJar ejbJar = new EjbJar("StatsAdvancedModule");
        ejbJar.addEnterpriseBean(new StatelessBean(CounterBean.class));


        assembler.createApplication(config.configureApplication(ejbJar));

        javax.naming.Context context = new InitialContext();
        CounterBean bean = (CounterBean) context.lookup("CounterBeanLocalBean");

        // Invoke each method a different number of times
        for (int i = 0; i < 15; i++) {
            bean.red();
        }
        for (int i = 0; i < 10; i++) {
            bean.green();
        }
        for (int i = 0; i < 5; i++) {
            bean.blue();
        }

        CountDownLatch startingPistol = checkout(bean, 7);
        startingPistol.countDown();

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName invocationsName = new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=null,EJBModule=StatsAdvancedModule,StatelessSessionBean=CounterBean,j2eeType=Invocations,name=CounterBean");
        ObjectName poolName = new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=null,EJBModule=StatsAdvancedModule,StatelessSessionBean=CounterBean,j2eeType=Pool,name=CounterBean");

        // Grab the mbeanInfo and check the expected attributes exist and have the correct return types and parameters        
        MBeanInfo invocationsMBeanInfo = server.getMBeanInfo(invocationsName);
        MBeanInfo poolMBeanInfo = server.getMBeanInfo(poolName);
        for (MBeanAttributeInfo info : poolMBeanInfo.getAttributes()) {
            //System.out.println("//" + info.getName() + " " + server.getAttribute(poolName, info.getName()) );
            if (info.getName().equals("Available")) {
                assertEquals((long) 2147483647, server.getAttribute(poolName, info.getName()));
            }
            if (info.getName().equals("IdleTimeout")) {
                assertEquals((long) 30000 * 60 * 1000, server.getAttribute(poolName, info.getName()));
            }
        }

        for (MBeanAttributeInfo info : invocationsMBeanInfo.getAttributes()) {
            //System.out.println("//" + info.getName() + " " + server.getAttribute(invocationsName, info.getName()));
            if (info.getName().equals("MonitoredMethods")) {
                assertEquals((long) 6, server.getAttribute(invocationsName, info.getName()));
            } else if (info.getName().equals("InvocationCount")) {
                assertTrue((Long) (server.getAttribute(invocationsName, info.getName())) >= (7 * 2 + 30));
            } else if (info.getName().equals("PostConstruct().Count")) {
                assertEquals((long) 7, server.getAttribute(invocationsName, info.getName()));
            } else if (info.getName().equals("red().Count")) {
                assertEquals((long) 15, server.getAttribute(invocationsName, info.getName()));
            } else if (info.getName().equals("green().Count")) {
                assertEquals((long) 10, server.getAttribute(invocationsName, info.getName()));
            } else if (info.getName().equals("blue().Count")) {
                assertEquals((long) 5, server.getAttribute(invocationsName, info.getName()));
            } else if (info.getName().equals("checkout(CountDownLatch,CountDownLatch).Count")) {
                assertEquals((long) 7, server.getAttribute(invocationsName, info.getName()));
            } else if (info.getName().equals("Overdrafts")) {
                assertEquals((long) 5, server.getAttribute(invocationsName, info.getName()));
            }
        }
        ejbJar.removeEnterpriseBean("StatsAdvancedModule");
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
        statelessContainerInfo.properties.setProperty("PoolSize", "2");
        statelessContainerInfo.properties.setProperty("PoolMin", "0");
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
