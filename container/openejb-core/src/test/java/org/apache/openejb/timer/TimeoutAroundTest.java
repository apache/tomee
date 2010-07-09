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
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.interceptor.AroundTimeout;
import javax.interceptor.InvocationContext;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.client.LocalInitialContextFactory;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.StatelessBean;

/**
 * @version $Rev$ $Date$
 */
public class TimeoutAroundTest extends TestCase {

    private static final List<Call> result = new ArrayList<Call>();

    public void testTimeoutAround() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        Assembler assembler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        EjbJar ejbJar = new EjbJar();
        AssemblyDescriptor assemblyDescriptor = ejbJar.getAssemblyDescriptor();

        //Configure AroundTimeout by deployment plan
        Interceptor interceptorA = new Interceptor(SimpleInterceptorA.class);
        interceptorA.getAroundTimeout().add(new org.apache.openejb.jee.AroundTimeout(SimpleInterceptorA.class.getName(), "interceptorTimeoutAround"));
        ejbJar.addInterceptor(interceptorA);

        //Configure AroundTimeout by annotation
        Interceptor interceptorB = new Interceptor(SimpleInterceptorB.class);
        ejbJar.addInterceptor(interceptorB);

        //Override AroundTimeout annotation by deployment plan
        Interceptor interceptorC = new Interceptor(SimpleInterceptorC.class);
        interceptorC.getAroundTimeout().add(new org.apache.openejb.jee.AroundTimeout(SimpleInterceptorC.class.getName(), "interceptorTimeoutAround"));
        ejbJar.addInterceptor(interceptorC);

        //Configure aroundTimeout by deployment plan
        StatelessBean subBeanA = new StatelessBean(SubBeanA.class);
        subBeanA.addAroundTimeout("beanTimeoutAround");
        ejbJar.addEnterpriseBean(subBeanA);
        assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(subBeanA, interceptorA));
        assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(subBeanA, interceptorB));
        assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(subBeanA, interceptorC));

        //Configure aroundTimeout by annotation
        StatelessBean subBeanB = new StatelessBean(SubBeanB.class);
        ejbJar.addEnterpriseBean(subBeanB);
        assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(subBeanB, interceptorA));
        assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(subBeanB, interceptorB));
        assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(subBeanB, interceptorC));

        //Override aroundTimeout annotation by deployment plan
        StatelessBean subBeanC = new StatelessBean(SubBeanC.class);
        subBeanC.addAroundTimeout("beanTimeoutAround");
        ejbJar.addEnterpriseBean(subBeanC);
        assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(subBeanC, interceptorA));
        assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(subBeanC, interceptorB));
        assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(subBeanC, interceptorC));

        EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);
        assembler.createApplication(ejbJarInfo);
        InitialContext context = new InitialContext();

        List<Call> expectedResult = Arrays.asList(Call.INTERCEPTOR_BEFORE_AROUNDTIMEOUT, Call.INTERCEPTOR_BEFORE_AROUNDTIMEOUT, Call.INTERCEPTOR_BEFORE_AROUNDTIMEOUT, Call.BEAN_BEFORE_AROUNDTIMEOUT,
                Call.BEAN_TIMEOUT, Call.BEAN_AFTER_AROUNDTIMEOUT, Call.INTERCEPTOR_AFTER_AROUNDTIMEOUT, Call.INTERCEPTOR_AFTER_AROUNDTIMEOUT, Call.INTERCEPTOR_AFTER_AROUNDTIMEOUT);

        {
            BeanInterface beanA = (BeanInterface) context.lookup("SubBeanALocal");
            beanA.simpleMethod();
            Thread.sleep(5000L);
            assertEquals(expectedResult, result);
            result.clear();
        }

        {
            BeanInterface beanB = (BeanInterface) context.lookup("SubBeanBLocal");
            beanB.simpleMethod();
            Thread.sleep(5000L);
            assertEquals(expectedResult, result);
            result.clear();
        }

        {
            BeanInterface beanC = (BeanInterface) context.lookup("SubBeanCLocal");
            beanC.simpleMethod();
            Thread.sleep(5000L);
            assertEquals(expectedResult, result);
            result.clear();
        }

    }

    public static interface BeanInterface {

        public void simpleMethod();
    }

    public static class BaseBean implements BeanInterface {

        @Resource
        TimerService timerService;

        public void simpleMethod() {
            timerService.createTimer(3000L, null);
        }
    }

    @Stateless
    @Local(BeanInterface.class)
    public static class SubBeanA extends BaseBean implements TimedObject {

        @Override
        public void ejbTimeout(Timer arg0) {
            result.add(Call.BEAN_TIMEOUT);
        }

        public Object beanTimeoutAround(InvocationContext context) throws Exception {
            assertNotNull(context.getTimer());
            result.add(Call.BEAN_BEFORE_AROUNDTIMEOUT);
            Object ret = context.proceed();
            result.add(Call.BEAN_AFTER_AROUNDTIMEOUT);
            return ret;
        }

    }

    @Stateless
    @Local(BeanInterface.class)
    public static class SubBeanB extends BaseBean implements TimedObject {

        @Override
        public void ejbTimeout(Timer arg0) {
            result.add(Call.BEAN_TIMEOUT);
        }

        @AroundTimeout
        public Object beanTimeoutAround(InvocationContext context) throws Exception {
            assertNotNull(context.getTimer());
            result.add(Call.BEAN_BEFORE_AROUNDTIMEOUT);
            Object ret = context.proceed();
            result.add(Call.BEAN_AFTER_AROUNDTIMEOUT);
            return ret;
        }

    }

    @Stateless
    @Local(BeanInterface.class)
    public static class SubBeanC extends BaseBean implements TimedObject {

        @Override
        public void ejbTimeout(Timer arg0) {
            result.add(Call.BEAN_TIMEOUT);
        }

        @AroundTimeout
        public Object timeoutAroundBAD(InvocationContext context) throws Exception {
            result.add(Call.BAD_VALUE);
            return context.proceed();
        }

        public Object beanTimeoutAround(InvocationContext context) throws Exception {
            assertNotNull(context.getTimer());
            result.add(Call.BEAN_BEFORE_AROUNDTIMEOUT);
            Object ret = context.proceed();
            result.add(Call.BEAN_AFTER_AROUNDTIMEOUT);
            return ret;
        }
    }

    public static class SimpleInterceptorA {

        public Object interceptorTimeoutAround(InvocationContext context) throws Exception {
            assertNotNull(context.getTimer());
            result.add(Call.INTERCEPTOR_BEFORE_AROUNDTIMEOUT);
            Object ret = context.proceed();
            result.add(Call.INTERCEPTOR_AFTER_AROUNDTIMEOUT);
            return ret;
        }
    }

    public static class SimpleInterceptorB {

        @AroundTimeout
        public Object interceptorTimeoutAround(InvocationContext context) throws Exception {
            assertNotNull(context.getTimer());
            result.add(Call.INTERCEPTOR_BEFORE_AROUNDTIMEOUT);
            Object ret = context.proceed();
            result.add(Call.INTERCEPTOR_AFTER_AROUNDTIMEOUT);
            return ret;
        }

    }

    public static class SimpleInterceptorC {

        @AroundTimeout
        public Object invokeBAD(InvocationContext context) throws Exception {
            assertNotNull(context.getTimer());
            result.add(Call.BAD_VALUE);
            Object ret = context.proceed();
            return ret;
        }

        public Object interceptorTimeoutAround(InvocationContext context) throws Exception {
            assertNotNull(context.getTimer());
            result.add(Call.INTERCEPTOR_BEFORE_AROUNDTIMEOUT);
            Object ret = context.proceed();
            result.add(Call.INTERCEPTOR_AFTER_AROUNDTIMEOUT);
            return ret;
        }

    }

    public static enum Call {
        BEAN_TIMEOUT, BEAN_BEFORE_AROUNDTIMEOUT, BEAN_AFTER_AROUNDTIMEOUT, BAD_VALUE, INTERCEPTOR_BEFORE_AROUNDTIMEOUT, INTERCEPTOR_AFTER_AROUNDTIMEOUT
    }

}
