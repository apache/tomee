/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ejb.Local;
import javax.ejb.Schedule;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.ejb.TimedObject;
import javax.ejb.Timeout;
import javax.interceptor.AroundTimeout;
import javax.interceptor.InvocationContext;

import junit.framework.TestCase;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.Timer;
import org.apache.openejb.jee.TimerSchedule;
import org.junit.Assert;

/**
 * @version $Rev$ $Date$
 */
public class ScheduleTest extends TestCase {

    private static final List<Call> result = new ArrayList<Call>();
    private static final CountDownLatch countDownLatch = new CountDownLatch(3);

    public void testSchedule() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        Assembler assembler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        EjbJar ejbJar = new EjbJar();

        //Configure schedule by deployment plan
        StatelessBean subBeanA = new StatelessBean(SubBeanA.class);
        Timer subBeanATimer = new Timer();
        subBeanATimer.setTimeoutMethod(new NamedMethod("subBeanA", "javax.ejb.Timer"));
        TimerSchedule timerScheduleA = new TimerSchedule();
        timerScheduleA.setSecond("2");
        timerScheduleA.setMinute("*");
        timerScheduleA.setHour("*");
        subBeanATimer.setSchedule(timerScheduleA);
        subBeanATimer.setInfo("SubBeanAInfo");
        subBeanA.getTimer().add(subBeanATimer);
        ejbJar.addEnterpriseBean(subBeanA);

        //Configure schedule by annotation
        StatelessBean subBeanB = new StatelessBean(SubBeanB.class);
        ejbJar.addEnterpriseBean(subBeanB);

        //Override aroundTimeout annotation by deployment plan
        StatelessBean subBeanC = new StatelessBean(SubBeanC.class);
        Timer subBeanCTimer = new Timer();
        subBeanCTimer.setTimeoutMethod(new NamedMethod("subBeanC", "javax.ejb.Timer"));
        TimerSchedule timerScheduleC = new TimerSchedule();
        timerScheduleC.setSecond("2");
        timerScheduleC.setMinute("*");
        timerScheduleC.setHour("*");
        subBeanCTimer.setSchedule(timerScheduleC);
        subBeanCTimer.setInfo("SubBeanCInfo");
        subBeanC.getTimer().add(subBeanCTimer);
        ejbJar.addEnterpriseBean(subBeanC);

        StatefulBean subBeanM = new StatefulBean(SubBeanM.class);
        ejbJar.addEnterpriseBean(subBeanM);
        EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);
        assembler.createApplication(ejbJarInfo);

        countDownLatch.await(1L, TimeUnit.MINUTES);

        //A better way for validation ?
        int beforeAroundInvocationCount = 0;
        int afterAroundInvocationCount = 0;
        int timeoutInvocationCount = 0;
        int size = 0;

        synchronized (result) {

            size = result.size();

            for (Call call : result) {
                switch (call) {
                    case BEAN_BEFORE_AROUNDTIMEOUT:
                        beforeAroundInvocationCount++;
                        break;
                    case BEAN_AFTER_AROUNDTIMEOUT:
                        afterAroundInvocationCount++;
                        break;
                    case TIMEOUT:
                        timeoutInvocationCount++;
                        break;
                }
            }
        }

        assertEquals(3, beforeAroundInvocationCount);
        assertEquals(3, afterAroundInvocationCount);
        assertEquals(3, timeoutInvocationCount);
        assertEquals(9, size);
    }

    public static interface BeanInterface {

        public void simpleMethod();
    }

    public static class BaseBean implements BeanInterface {

        public void simpleMethod() {
        }

        @AroundTimeout
        public Object beanTimeoutAround(InvocationContext context) throws Exception {
            synchronized (result) {
                assertNotNull(context.getTimer());
                result.add(Call.BEAN_BEFORE_AROUNDTIMEOUT);

                Object ret = null;
                try {
                    ret = context.proceed();
                } catch (Throwable t) {
                    throw new Exception(t);
                } finally {
                    result.add(Call.BEAN_AFTER_AROUNDTIMEOUT);
                    countDownLatch.countDown();
                }

                return ret;
            }
        }
    }

    @Stateless
    @Local(BeanInterface.class)
    public static class SubBeanA extends BaseBean implements TimedObject {

        public void subBeanA(javax.ejb.Timer timer) {
            synchronized (result) {
                assertEquals("SubBeanAInfo", timer.getInfo());
                result.add(Call.TIMEOUT);
            }
        }

        @Override
        public void ejbTimeout(javax.ejb.Timer arg0) {
            Assert.fail("This method should not be invoked, we might confuse the auto-created timers and timeout timer");
        }
    }

    @Stateful
    @Local(BeanInterface.class)
    public static class SubBeanM extends BaseBean implements TimedObject {

        @Schedule(second = "2", minute = "*", hour = "*", info = "SubBeanBInfo")
        public void subBeanA(javax.ejb.Timer timer) {
            synchronized (result) {
                assertEquals("SubBeanAInfo", timer.getInfo());
                result.add(Call.TIMEOUT);
            }
        }

        @Override
        public void ejbTimeout(javax.ejb.Timer arg0) {
            fail("This method should not be invoked, we might confuse the auto-created timers and timeout timer");
        }
    }

    @Stateless
    @Local(BeanInterface.class)
    public static class SubBeanB extends BaseBean {

        @Schedule(second = "2", minute = "*", hour = "*", info = "SubBeanBInfo")
        public void subBeanB(javax.ejb.Timer timer) {
            synchronized (result) {
                assertEquals("SubBeanBInfo", timer.getInfo());
                result.add(Call.TIMEOUT);
            }
        }

        @Timeout
        public void ejbT(javax.ejb.Timer timer) {
            fail("This method should not be invoked, we might confuse the auto-created timers and timeout timer");
        }
    }

    @Stateless
    @Local(BeanInterface.class)
    public static class SubBeanC extends BaseBean {

        @Schedule(info = "badValue")
        public void subBeanC(javax.ejb.Timer timer) {
            synchronized (result) {
                assertEquals("SubBeanCInfo", timer.getInfo());
                result.add(Call.TIMEOUT);
            }
        }

        @Timeout
        public void ejbT() {
            fail("This method should not be invoked, we might confuse the auto-created timers and timeout timer");
        }
    }

    public static enum Call {

        BEAN_TIMEOUT, BEAN_BEFORE_AROUNDTIMEOUT, BEAN_AFTER_AROUNDTIMEOUT, BAD_VALUE, TIMEOUT
    }
}
