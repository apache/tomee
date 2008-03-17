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

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.NamedMethod;
import org.junit.Test;
import org.junit.Before;

import javax.annotation.PostConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.ejb.EJBException;
import javax.ejb.Local;
import java.util.*;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class StatelessInterceptorTest extends TestCase {

    InitialContext ctx;

    @Before
    public void setUp() throws Exception {
        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        assembler.createContainer(config.configureService(StatelessSessionContainerInfo.class));

        EjbJarInfo ejbJar = config.configureApplication(buildTestApp());
        assertNotNull(ejbJar);

        assembler.createApplication(ejbJar);

        Properties properties = new Properties(System.getProperties());
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());
        ctx = new InitialContext(properties);
    }

    public void test() throws Exception {
        Target target = (Target) ctx.lookup("TargetBeanLocal");
        target.echo(new ArrayList());

        assertCalls(Call.values());

        calls.clear();

        int i = target.echo(123);
        assertEquals(123, i);

        assertCalls(
                Call.Default_Invoke_BEFORE,
                Call.Method_ann_Invoke_BEFORE,
                Call.Method_dd_Invoke_BEFORE,
                Call.Bean_Invoke_BEFORE,
                Call.Bean_Invoke,
                Call.Bean_Invoke_AFTER,
                Call.Method_dd_Invoke_AFTER,
                Call.Method_ann_Invoke_AFTER,
                Call.Default_Invoke_AFTER);
        calls.clear();

        boolean b = target.echo(true);
        assertTrue(b);

        assertCalls(
                Call.Method_ann_Invoke_BEFORE,
                Call.Method_dd_Invoke_BEFORE,
                Call.Bean_Invoke_BEFORE,
                Call.Bean_Invoke,
                Call.Bean_Invoke_AFTER,
                Call.Method_dd_Invoke_AFTER,
                Call.Method_ann_Invoke_AFTER);
        calls.clear();

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

        calls.clear();

        Target target2 = (Target) ctx.lookup("Target2BeanLocal");

        i = target2.echo(123);
        assertEquals(123, i);

        calls.clear();

        i = target2.echo(123);
        assertEquals(123, i);

        assertCalls(
                Call.Method_ann_Invoke_BEFORE,
                Call.Bean_Invoke_BEFORE,
                Call.Bean_Invoke,
                Call.Bean_Invoke_AFTER,
                Call.Method_ann_Invoke_AFTER);
        calls.clear();

        b = target2.echo(true);
        assertTrue(b);

        assertCalls(
                Call.Method_ann_Invoke_BEFORE,
                Call.Bean_Invoke_BEFORE,
                Call.Bean_Invoke,
                Call.Bean_Invoke_AFTER,
                Call.Method_ann_Invoke_AFTER);
        calls.clear();
    }

    @Test
    public void testExcludeClassAndDefaultInterceptors() throws Exception {

        // 1. Look up the bean it's to be tested against
        Target target3 = (Target) ctx.lookup("Target3BeanLocal");

        // 2. Execute intercepted method
        target3.echo(Collections.EMPTY_LIST);

        // 3. Assert that appropriate interceptors were executed
        assertCalls(
                Call.Bean_PostConstruct,
                Call.Method_ann_Invoke_BEFORE,
                Call.Bean_Invoke_BEFORE,
                Call.Bean_Invoke,
                Call.Bean_Invoke_AFTER,
                Call.Method_ann_Invoke_AFTER);

        // 4. Clean up after yourself
        calls.clear();
    }

    private void assertCalls(Call... expectedCalls) {
        List expected = Arrays.asList(expectedCalls);
        assertEquals(join("\n", expected) , join("\n", calls));
    }

    public static enum Call {
        Default_PostConstruct_BEFORE,
        SuperClass_PostConstruct_BEFORE,
        Class_PostConstruct_BEFORE,
        Bean_PostConstruct,
        Class_PostConstruct_AFTER,
        SuperClass_PostConstruct_AFTER,
        Default_PostConstruct_AFTER,

        Default_Invoke_BEFORE,
        SuperClass_Invoke_BEFORE,
        Class_Invoke_BEFORE,
        Method_ann_Invoke_BEFORE,
        Method_dd_Invoke_BEFORE,
        Bean_Invoke_BEFORE,
        Bean_Invoke,
        Bean_Invoke_AFTER,
        Method_dd_Invoke_AFTER,
        Method_ann_Invoke_AFTER,
        Class_Invoke_AFTER,
        SuperClass_Invoke_AFTER,
        Default_Invoke_AFTER,

    }

    public EjbModule buildTestApp() throws Exception {
        EjbJar ejbJar = new EjbJar();
        ejbJar.setId(this.getClass().getName());
        
        AssemblyDescriptor ad = ejbJar.getAssemblyDescriptor();

        ejbJar.addEnterpriseBean(new StatelessBean(Target2Bean.class));

        EnterpriseBean bean = ejbJar.addEnterpriseBean(new StatelessBean(TargetBean.class));

        Interceptor interceptor;

        interceptor = ejbJar.addInterceptor(new Interceptor(DefaultInterceptor.class));
        ad.addInterceptorBinding(new InterceptorBinding("*", interceptor));

        {
            interceptor = ejbJar.addInterceptor(new Interceptor(EchoMethodInterceptorViaDD.class));
            InterceptorBinding binding = ad.addInterceptorBinding(new InterceptorBinding(bean, interceptor));
            binding.setMethod(new NamedMethod(TargetBean.class.getMethod("echo", List.class)));
        }

        {
            interceptor = ejbJar.addInterceptor(new Interceptor(EchoMethodInterceptorViaDD.class));
            InterceptorBinding binding = ad.addInterceptorBinding(new InterceptorBinding(bean, interceptor));
            binding.setMethod(new NamedMethod(TargetBean.class.getMethod("echo", int.class)));
        }

        {
            interceptor = ejbJar.addInterceptor(new Interceptor(EchoMethodInterceptorViaDD.class));
            InterceptorBinding binding = ad.addInterceptorBinding(new InterceptorBinding(bean, interceptor));
            binding.setMethod(new NamedMethod(TargetBean.class.getMethod("echo", boolean.class)));
        }

        EnterpriseBean bean3 = ejbJar.addEnterpriseBean(new StatelessBean(Target3Bean.class));
        InterceptorBinding binding = ad.addInterceptorBinding(new InterceptorBinding(bean3));
        binding.setExcludeDefaultInterceptors(true);
        binding.setExcludeClassInterceptors(true);

        return new EjbModule(ejbJar);
    }

    public static List<Call> calls = new ArrayList<Call>();

    @Interceptors({ClassInterceptor.class})
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

        @Interceptors({EchoMethodInterceptorViaAnn.class})
        public List echo(List data){
            calls.add(Call.Bean_Invoke);
            return data;
        }

        public void throwAppException() throws AppException {
            throw new AppException();
        }

        public void throwSysException() {
            throw new SysException();
        }

        @Interceptors({EchoMethodInterceptorViaAnn.class})
        @ExcludeClassInterceptors
        public int echo(int i) {
            calls.add(Call.Bean_Invoke);
            return i;
        }

        @Interceptors({EchoMethodInterceptorViaAnn.class})
        @ExcludeClassInterceptors
        @ExcludeDefaultInterceptors
        public boolean echo(boolean i) {
            calls.add(Call.Bean_Invoke);
            return i;
        }
    }

    @ExcludeDefaultInterceptors
    @Interceptors({ClassInterceptor.class})
    public static class Target2Bean implements Target {

        @AroundInvoke
        public Object invoke(InvocationContext context) throws Exception {
            calls.add(Call.Bean_Invoke_BEFORE);
            Object o = context.proceed();
            calls.add(Call.Bean_Invoke_AFTER);
            return o;
        }

        @Interceptors({EchoMethodInterceptorViaAnn.class})
        public List echo(List data){
            calls.add(Call.Bean_Invoke);
            return data;
        }

        public void throwAppException() throws AppException {
            throw new AppException();
        }

        public void throwSysException() {
            throw new SysException();
        }

        @Interceptors({EchoMethodInterceptorViaAnn.class})
        @ExcludeClassInterceptors
        public int echo(int i) {
            calls.add(Call.Bean_Invoke);
            return i;
        }

        @Interceptors({EchoMethodInterceptorViaAnn.class})
        @ExcludeClassInterceptors
        @ExcludeDefaultInterceptors
        public boolean echo(boolean i) {
            calls.add(Call.Bean_Invoke);
            return i;
        }
    }

    @Local({Target.class})
    public static class Target3Bean extends TargetBean {
    }

    public static interface Target {
        List echo(List data);
        void throwAppException() throws AppException;
        void throwSysException();
        int echo(int i);
        boolean echo(boolean b);
    }

    public static class AppException extends Exception {
        public AppException() {
        }
    }

    public static class SysException extends RuntimeException {
        public SysException() {
        }
    }

    public static class EchoMethodInterceptorViaAnn {

        @AroundInvoke
        public Object invoke(InvocationContext context) throws Exception {
            calls.add(Call.Method_ann_Invoke_BEFORE);
            Object o = context.proceed();
            calls.add(Call.Method_ann_Invoke_AFTER);
            return o;
        }
    }

    public static class EchoMethodInterceptorViaDD {

        @AroundInvoke
        public Object invoke(InvocationContext context) throws Exception {
            calls.add(Call.Method_dd_Invoke_BEFORE);
            Object o = context.proceed();
            calls.add(Call.Method_dd_Invoke_AFTER);
            return o;
        }
    }

    public static class ClassInterceptor extends SuperClassInterceptor {

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

    public static class SuperClassInterceptor {

        @PostConstruct
        public void superConstruct(InvocationContext context) throws Exception {
            calls.add(Call.SuperClass_PostConstruct_BEFORE);
            context.proceed();
            calls.add(Call.SuperClass_PostConstruct_AFTER);
        }

        @AroundInvoke
        public Object superInvoke(InvocationContext context) throws Exception {
            calls.add(Call.SuperClass_Invoke_BEFORE);
            Object o = context.proceed();
            calls.add(Call.SuperClass_Invoke_AFTER);
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
