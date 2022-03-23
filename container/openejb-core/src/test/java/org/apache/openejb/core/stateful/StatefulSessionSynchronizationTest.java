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

package org.apache.openejb.core.stateful;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.StatefulBean;
import org.junit.AfterClass;

import jakarta.ejb.AfterBegin;
import jakarta.ejb.AfterCompletion;
import jakarta.ejb.BeforeCompletion;
import jakarta.ejb.EJBException;
import jakarta.ejb.Local;
import jakarta.ejb.LocalBean;
import jakarta.ejb.SessionSynchronization;
import jakarta.ejb.Stateful;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.InvocationContext;
import javax.naming.InitialContext;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class StatefulSessionSynchronizationTest extends TestCase {

    private static final List<Call> result = new ArrayList<Call>();

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        final AssemblyDescriptor assemblyDescriptor = ejbJar.getAssemblyDescriptor();

        final Interceptor interceptor = new Interceptor(SimpleInterceptor.class);
        ejbJar.addInterceptor(interceptor);

        //Test SessionSynchronization interface
        final StatefulBean subBeanA = new StatefulBean(SubBeanA.class);
        ejbJar.addEnterpriseBean(subBeanA);
        assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(subBeanA, interceptor));

        //Test configure session synchronization callback methods in deployment plan
        final StatefulBean subBeanB = new StatefulBean(SubBeanB.class);
        subBeanB.setAfterBeginMethod(new NamedMethod(SubBeanB.class.getDeclaredMethod("afterBegin")));
        subBeanB.setBeforeCompletionMethod(new NamedMethod(SubBeanB.class.getDeclaredMethod("beforeCompletion")));
        subBeanB.setAfterCompletionMethod(new NamedMethod(SubBeanB.class.getDeclaredMethod("afterCompletion", boolean.class)));
        ejbJar.addEnterpriseBean(subBeanB);
        assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(subBeanB, interceptor));

        //Test session synchronization methods via annotations
        final StatefulBean subBeanC = new StatefulBean(SubBeanC.class);
        ejbJar.addEnterpriseBean(subBeanC);
        assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(subBeanC, interceptor));

        //Test override the annotations by deployment plan
        final StatefulBean subBeanD = new StatefulBean(SubBeanD.class);
        subBeanD.setAfterBeginMethod(new NamedMethod(SubBeanD.class.getDeclaredMethod("afterBeginNew")));
        subBeanD.setBeforeCompletionMethod(new NamedMethod(SubBeanD.class.getDeclaredMethod("beforeCompletionNew")));
        subBeanD.setAfterCompletionMethod(new NamedMethod(SubBeanD.class.getDeclaredMethod("afterCompletionNew", boolean.class)));
        ejbJar.addEnterpriseBean(subBeanD);
        assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(subBeanD, interceptor));

        //Test only one session synchronization method @AfterBegin
        final StatefulBean subBeanE = new StatefulBean(SubBeanE.class);
        ejbJar.addEnterpriseBean(subBeanE);
        assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(subBeanE, interceptor));

        //Test only one session synchronization method @AfterCompletion
        final StatefulBean subBeanF = new StatefulBean(SubBeanF.class);
        ejbJar.addEnterpriseBean(subBeanF);
        assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(subBeanF, interceptor));

        //Test only one session synchronization method @BeforeCompletion
        final StatefulBean subBeanG = new StatefulBean(SubBeanG.class);
        ejbJar.addEnterpriseBean(subBeanG);
        assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(subBeanG, interceptor));

        //Test SessionSynchronization interface but methods are in the parent class
        //Interceptor is declared on the bean method
        final StatefulBean subBeanH = new StatefulBean(SubBeanH.class);
        ejbJar.addEnterpriseBean(subBeanH);

        //Test SessionSynchronization interface but methods are in the parent class
        //using @LocalBean
        final StatefulBean subBeanI = new StatefulBean(SubBeanI.class);
        ejbJar.addEnterpriseBean(subBeanI);

        final EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);
        assembler.createApplication(ejbJarInfo);
        final InitialContext context = new InitialContext();

        final List<Call> expectedResult = Arrays.asList(Call.INTERCEPTOR_AFTER_BEGIN, Call.BEAN_AFTER_BEGIN, Call.INTERCEPTOR_AROUND_INVOKE_BEGIN, Call.BEAN_AROUND_INVOKE_BEGIN, Call.BEAN_METHOD,
            Call.BEAN_AROUND_INVOKE_AFTER, Call.INTERCEPTOR_AROUND_INVOKE_AFTER, Call.INTERCEPTOR_BEFORE_COMPLETION, Call.BEAN_BEFORE_COMPLETION, Call.INTERCEPTOR_AFTER_COMPLETION,
            Call.BEAN_AFTER_COMPLETION);

        {
            final BeanInterface beanA = (BeanInterface) context.lookup("SubBeanALocal");
            beanA.simpleMethod();
            assertEquals(expectedResult, result);
            result.clear();
        }

        {
            final BeanInterface beanB = (BeanInterface) context.lookup("SubBeanBLocal");
            beanB.simpleMethod();
            assertEquals(expectedResult, result);
            result.clear();
        }

        {
            final BeanInterface beanC = (BeanInterface) context.lookup("SubBeanCLocal");
            beanC.simpleMethod();
            assertEquals(expectedResult, result);
            result.clear();
        }

        {
            final BeanInterface beanD = (BeanInterface) context.lookup("SubBeanDLocal");
            beanD.simpleMethod();
            assertEquals(expectedResult, result);
            result.clear();
        }

        {
            final BeanInterface beanE = (BeanInterface) context.lookup("SubBeanELocal");
            beanE.simpleMethod();
            assertEquals(Arrays.asList(Call.INTERCEPTOR_AFTER_BEGIN, Call.BEAN_AFTER_BEGIN, Call.INTERCEPTOR_AROUND_INVOKE_BEGIN, Call.BEAN_AROUND_INVOKE_BEGIN, Call.BEAN_METHOD,
                Call.BEAN_AROUND_INVOKE_AFTER, Call.INTERCEPTOR_AROUND_INVOKE_AFTER, Call.INTERCEPTOR_BEFORE_COMPLETION, Call.INTERCEPTOR_AFTER_COMPLETION), result);
            result.clear();
        }

        {
            final BeanInterface beanF = (BeanInterface) context.lookup("SubBeanFLocal");
            beanF.simpleMethod();
            assertEquals(Arrays.asList(Call.INTERCEPTOR_AFTER_BEGIN, Call.INTERCEPTOR_AROUND_INVOKE_BEGIN, Call.BEAN_AROUND_INVOKE_BEGIN, Call.BEAN_METHOD, Call.BEAN_AROUND_INVOKE_AFTER,
                Call.INTERCEPTOR_AROUND_INVOKE_AFTER, Call.INTERCEPTOR_BEFORE_COMPLETION, Call.INTERCEPTOR_AFTER_COMPLETION, Call.BEAN_AFTER_COMPLETION), result);
            result.clear();
        }

        {
            final BeanInterface beanG = (BeanInterface) context.lookup("SubBeanGLocal");
            beanG.simpleMethod();
            assertEquals(Arrays.asList(Call.INTERCEPTOR_AFTER_BEGIN, Call.INTERCEPTOR_AROUND_INVOKE_BEGIN, Call.BEAN_AROUND_INVOKE_BEGIN, Call.BEAN_METHOD, Call.BEAN_AROUND_INVOKE_AFTER,
                Call.INTERCEPTOR_AROUND_INVOKE_AFTER, Call.INTERCEPTOR_BEFORE_COMPLETION, Call.BEAN_BEFORE_COMPLETION, Call.INTERCEPTOR_AFTER_COMPLETION), result);
            result.clear();
        }

        final List<Call> synchAndArroundInvokeResult = Arrays.asList(
            Call.BEAN_AFTER_BEGIN, Call.INTERCEPTOR_AROUND_INVOKE_BEGIN,
            Call.BEAN_AROUND_INVOKE_BEGIN, Call.BEAN_METHOD,
            Call.BEAN_AROUND_INVOKE_AFTER,
            Call.INTERCEPTOR_AROUND_INVOKE_AFTER, Call.BEAN_BEFORE_COMPLETION,
            Call.BEAN_AFTER_COMPLETION);
        {
            final BeanInterface beanH = (BeanInterface) context.lookup("SubBeanHLocal");
            beanH.simpleMethod();
            assertEquals(synchAndArroundInvokeResult, result);
            result.clear();
        }

        {
            final BeanInterface beanI = (BeanInterface) context.lookup("SubBeanILocalBean");
            beanI.simpleMethod();
            assertEquals(synchAndArroundInvokeResult, result);
            result.clear();
        }
    }

    public static interface BeanInterface {

        public void simpleMethod();
    }

    public static class BaseBean implements BeanInterface {

        @TransactionAttribute(TransactionAttributeType.REQUIRED)
        public void simpleMethod() {
            result.add(Call.BEAN_METHOD);
        }

        @AroundInvoke
        public Object invoke(final InvocationContext context) throws Exception {
            result.add(Call.BEAN_AROUND_INVOKE_BEGIN);
            final Object o = context.proceed();
            result.add(Call.BEAN_AROUND_INVOKE_AFTER);
            return o;
        }

    }

    @Stateful
    @Local(BeanInterface.class)
    public static class SubBeanA extends BaseBean implements SessionSynchronization {

        @Override
        public void afterBegin() throws EJBException, RemoteException {
            result.add(Call.BEAN_AFTER_BEGIN);
        }

        @Override
        public void afterCompletion(final boolean arg0) throws EJBException, RemoteException {
            result.add(Call.BEAN_AFTER_COMPLETION);
        }

        @Override
        public void beforeCompletion() throws EJBException, RemoteException {
            result.add(Call.BEAN_BEFORE_COMPLETION);
        }
    }

    @Stateful
    @Local(BeanInterface.class)
    public static class SubBeanB extends BaseBean {

        public void afterBegin() throws EJBException, RemoteException {
            result.add(Call.BEAN_AFTER_BEGIN);
        }

        public void afterCompletion(final boolean arg0) throws EJBException, RemoteException {
            result.add(Call.BEAN_AFTER_COMPLETION);
        }

        public void beforeCompletion() throws EJBException, RemoteException {
            result.add(Call.BEAN_BEFORE_COMPLETION);
        }

    }

    @Stateful
    @Local(BeanInterface.class)
    public static class SubBeanC extends BaseBean {

        @AfterBegin
        private void afterBegin() throws EJBException, RemoteException {
            result.add(Call.BEAN_AFTER_BEGIN);
        }

        @AfterCompletion
        protected void afterCompletion(final boolean arg0) throws EJBException, RemoteException {
            result.add(Call.BEAN_AFTER_COMPLETION);
        }

        @BeforeCompletion
        public void beforeCompletion() throws EJBException, RemoteException {
            result.add(Call.BEAN_BEFORE_COMPLETION);
        }

    }

    @Stateful
    @Local(BeanInterface.class)
    public static class SubBeanD extends BaseBean {

        @AfterBegin
        public void afterBegin() throws EJBException, RemoteException {
            result.add(Call.BAD_VALUE);
        }

        @AfterCompletion
        public void afterCompletion(final boolean arg0) throws EJBException, RemoteException {
            result.add(Call.BAD_VALUE);
        }

        @BeforeCompletion
        public void beforeCompletion() throws EJBException, RemoteException {
            result.add(Call.BAD_VALUE);
        }

        private void afterBeginNew() throws EJBException, RemoteException {
            result.add(Call.BEAN_AFTER_BEGIN);
        }

        protected void afterCompletionNew(final boolean arg0) throws EJBException, RemoteException {
            result.add(Call.BEAN_AFTER_COMPLETION);
        }

        public void beforeCompletionNew() throws EJBException, RemoteException {
            result.add(Call.BEAN_BEFORE_COMPLETION);
        }
    }

    @Stateful
    @Local(BeanInterface.class)
    public static class SubBeanE extends BaseBean {

        @AfterBegin
        public void afterBegin() throws EJBException, RemoteException {
            result.add(Call.BEAN_AFTER_BEGIN);
        }
    }

    @Stateful
    @Local(BeanInterface.class)
    public static class SubBeanF extends BaseBean {

        @AfterCompletion
        public void afterCompletion(final boolean arg0) throws EJBException, RemoteException {
            result.add(Call.BEAN_AFTER_COMPLETION);
        }
    }

    @Stateful
    @Local(BeanInterface.class)
    public static class SubBeanG extends BaseBean {

        @BeforeCompletion
        public void beforeCompletion() throws EJBException, RemoteException {
            result.add(Call.BEAN_BEFORE_COMPLETION);
        }
    }

    public static class SimpleInterceptor {

        @AfterBegin
        public void afterBegin(final InvocationContext invocationContext) throws Exception {
            result.add(Call.INTERCEPTOR_AFTER_BEGIN);
            invocationContext.proceed();
        }

        @BeforeCompletion
        public void beforeComplete(final InvocationContext invocationContext) throws Exception {
            result.add(Call.INTERCEPTOR_BEFORE_COMPLETION);
            invocationContext.proceed();
        }

        @AfterCompletion
        public void afterComplete(final InvocationContext invocationContext) throws Exception {
            result.add(Call.INTERCEPTOR_AFTER_COMPLETION);
            invocationContext.proceed();
        }

        @AroundInvoke
        public Object invoke(final InvocationContext context) throws Exception {
            result.add(Call.INTERCEPTOR_AROUND_INVOKE_BEGIN);
            final Object o = context.proceed();
            result.add(Call.INTERCEPTOR_AROUND_INVOKE_AFTER);
            return o;
        }
    }

    public static class BaseBeanB implements BeanInterface {

        @TransactionAttribute(TransactionAttributeType.REQUIRED)
        @Interceptors(SimpleInterceptor.class)
        public void simpleMethod() {
            result.add(Call.BEAN_METHOD);
        }

        public void afterBegin() {
            result.add(Call.BEAN_AFTER_BEGIN);
        }

        public void afterCompletion(final boolean arg0) {
            result.add(Call.BEAN_AFTER_COMPLETION);
        }

        public void beforeCompletion() {
            result.add(Call.BEAN_BEFORE_COMPLETION);
        }

        @AroundInvoke
        public Object aroundInvoke(final InvocationContext context) throws Exception {
            result.add(Call.BEAN_AROUND_INVOKE_BEGIN);
            final Object o = context.proceed();
            result.add(Call.BEAN_AROUND_INVOKE_AFTER);
            return o;
        }

    }

    @Stateful
    @Local(BeanInterface.class)
    public static class SubBeanH extends BaseBeanB implements SessionSynchronization {
    }

    @Stateful
    @LocalBean
    public static class SubBeanI extends BaseBeanB implements SessionSynchronization {
    }

    public static enum Call {
        BEAN_METHOD, BEAN_AROUND_INVOKE_BEGIN, BEAN_AROUND_INVOKE_AFTER, INTERCEPTOR_AROUND_INVOKE_BEGIN, INTERCEPTOR_AROUND_INVOKE_AFTER, BEAN_AFTER_BEGIN, BEAN_BEFORE_COMPLETION, BEAN_AFTER_COMPLETION, BAD_VALUE, INTERCEPTOR_AFTER_BEGIN, INTERCEPTOR_BEFORE_COMPLETION, INTERCEPTOR_AFTER_COMPLETION
    }
}
