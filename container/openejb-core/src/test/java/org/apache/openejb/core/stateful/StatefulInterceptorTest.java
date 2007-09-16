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
package org.apache.openejb.core.stateful;

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ConnectionManagerInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.StatefulBean;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class StatefulInterceptorTest extends TestCase {

    public void test() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        assembler.createConnectionManager(config.configureService(ConnectionManagerInfo.class));

        // containers
        assembler.createContainer(config.configureService(StatefulSessionContainerInfo.class));

        EjbJarInfo ejbJar = config.configureApplication(buildTestApp());
        assertNotNull(ejbJar);
        assertEquals(1, ejbJar.enterpriseBeans.size());
        assertEquals(1, ejbJar.enterpriseBeans.get(0).aroundInvoke.size());
        assertEquals(1, ejbJar.enterpriseBeans.get(0).postConstruct.size());

        assertEquals(3, ejbJar.interceptors.size());
        assertEquals(1, ejbJar.interceptors.get(0).aroundInvoke.size());
        assertEquals(1, ejbJar.interceptors.get(0).postConstruct.size());

        assertEquals(3, ejbJar.interceptorBindings.size());

        assembler.createApplication(ejbJar);

        InitialContext ctx = new InitialContext();
        Target target = (Target) ctx.lookup("TargetBeanLocal");
        target.echo(new ArrayList());

        assertCalls(Call.values());

        calls.clear();

        int i = target.echo(123);
        assertEquals(123, i);

        try {
            target.throwAppException();
            fail("Should have thrown app exception");
        } catch (AppException e) {
            // pass
        }

        try {
            target.throwSysException();
            fail("Should have thrown a sys exception");
        } catch (EJBException e) {
            // so far so good
            Throwable cause = e.getCause();
            if (!(cause instanceof SysException)) {
                fail("Inner Exception should be a SysException");
            }
        }
    }

    private void assertCalls(Call... expectedCalls) {
        List expected = Arrays.asList(expectedCalls);
        assertEquals(join("\n", expected), join("\n", calls));
    }

    public static enum Call {
        Default_PostConstruct_BEFORE,
        Class_PostConstruct_BEFORE,
        Bean_PostConstruct,
        Class_PostConstruct_AFTER,
        Default_PostConstruct_AFTER,

        Default_Invoke_BEFORE,
        Class_Invoke_BEFORE,
        Method_Invoke_BEFORE,
        Bean_Invoke_BEFORE,
        Bean_Invoke,
        Bean_Invoke_AFTER,
        Method_Invoke_AFTER,
        Class_Invoke_AFTER,
        Default_Invoke_AFTER,

    }

    public EjbModule buildTestApp() {
        EjbJar ejbJar = new EjbJar();
        AssemblyDescriptor ad = ejbJar.getAssemblyDescriptor();

        EnterpriseBean bean = ejbJar.addEnterpriseBean(new StatefulBean(TargetBean.class));

        Interceptor interceptor;

        interceptor = ejbJar.addInterceptor(new Interceptor(ClassInterceptor.class));
        ad.addInterceptorBinding(new InterceptorBinding(bean, interceptor));

        interceptor = ejbJar.addInterceptor(new Interceptor(DefaultInterceptor.class));
        ad.addInterceptorBinding(new InterceptorBinding("*", interceptor));

        interceptor = ejbJar.addInterceptor(new Interceptor(EchoMethodInterceptor.class));
        InterceptorBinding binding = ad.addInterceptorBinding(new InterceptorBinding(bean, interceptor));
        binding.setMethod(new NamedMethod("echo"));

        return new EjbModule(this.getClass().getClassLoader(), this.getClass().getSimpleName(), "test", ejbJar, null);
    }

    public static List<Call> calls = new ArrayList<Call>();

    public static class TargetBean implements Target {

        @PostConstruct
        public void construct() {
            calls.add(Call.Bean_PostConstruct);
        }

        @AroundInvoke
        public Object invoke(InvocationContext context) throws Exception {
            calls.add(Call.Bean_Invoke_BEFORE);
            Object o = context.proceed();
            calls.add(Call.Bean_Invoke_AFTER);
            return o;
        }

        public List echo(List data) {
            calls.add(Call.Bean_Invoke);
            return data;
        }

        public void throwAppException() throws AppException {
            throw new AppException();
        }

        public void throwSysException() {
            throw new SysException();
        }

        public int echo(int i) {
            return i;
        }
    }

    public static interface Target {
        List echo(List data);

        void throwAppException() throws AppException;

        void throwSysException();

        int echo(int i);
    }

    public static class AppException extends Exception {
        public AppException() {
        }
    }

    public static class SysException extends RuntimeException {
        public SysException() {
        }
    }

    public static class EchoMethodInterceptor {

        @AroundInvoke
        public Object invoke(InvocationContext context) throws Exception {
            calls.add(Call.Method_Invoke_BEFORE);
            Object o = context.proceed();
            calls.add(Call.Method_Invoke_AFTER);
            return o;
        }
    }

    public static class ClassInterceptor {

        @PostConstruct
        public void construct(InvocationContext context) throws Exception {
            calls.add(Call.Class_PostConstruct_BEFORE);
            context.proceed();
            calls.add(Call.Class_PostConstruct_AFTER);
        }

        @AroundInvoke
        public Object invoke(InvocationContext context) throws Exception {
            calls.add(Call.Class_Invoke_BEFORE);
            Object o = context.proceed();
            calls.add(Call.Class_Invoke_AFTER);
            return o;
        }
    }

    public static class DefaultInterceptor {

        @PostConstruct
        public void construct(InvocationContext context) throws Exception {
            calls.add(Call.Default_PostConstruct_BEFORE);
            context.proceed();
            calls.add(Call.Default_PostConstruct_AFTER);
        }

        @AroundInvoke
        public Object invoke(InvocationContext context) throws Exception {
            calls.add(Call.Default_Invoke_BEFORE);
            Object o = context.proceed();
            calls.add(Call.Default_Invoke_AFTER);
            return o;
        }
    }


    private static String join(String delimeter, List items) {
        StringBuffer sb = new StringBuffer();
        for (Object item : items) {
            sb.append(item.toString()).append(delimeter);
        }
        return sb.toString();
    }
}
