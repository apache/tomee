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
package org.apache.openejb.junit;

import org.apache.openejb.BeanType;
import org.apache.openejb.SystemException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.AppContext;
import org.apache.openejb.core.ModuleContext;
import org.apache.openejb.core.transaction.JtaTransactionPolicyFactory;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import javax.transaction.TransactionManager;
import javax.interceptor.Interceptors;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
public class LocalClientRunner extends BlockJUnit4ClassRunner {

    private final CoreDeploymentInfo deployment;
    private final Class<?> clazz;

    public LocalClientRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        deployment = createDeployment(clazz);
        this.clazz = clazz;
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        Object instance = newTestInstance();

        Test test = new Test(clazz, method.getMethod(), instance, deployment);

        Statement statement = methodInvoker(method, instance);

        statement = wrap(test, statement, RunAs.class, javax.annotation.security.RunAs.class);
        statement = wrap(test, statement, RunTestAs.class, org.apache.openejb.junit.RunTestAs.class);
        statement = wrap(test, statement, Transaction.class, org.apache.openejb.junit.Transaction.class);
        statement = wrap(test, statement, TransactionAttribute.class, javax.ejb.TransactionAttribute.class);

        statement = possiblyExpectingExceptions(method, instance, statement);
        statement = withPotentialTimeout(method, instance, statement);
        statement = withBefores(method, instance, statement);
        statement = withAfters(method, instance, statement);
        return statement;
    }

    private Statement wrap(Test test, Statement statement, Class<? extends AnnotationStatement> clazz, Class<? extends Annotation> annotation) {

        if (test.has(annotation)) {
            try {
                Class[]  types = {annotation, Statement.class, Test.class};
                Object[] args = {test.get(annotation), statement, test};
                return clazz.getConstructor(types).newInstance(args);
            } catch (Exception e) {
                throw new IllegalStateException("Cannot construct "+ clazz, e);
            }
        }
        return statement;
    }

    /**
     * Creates a new test instance
     *
     * @return new instance
     */
    private Object newTestInstance() {
        try {
            return new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return createTest();
                }
            }.run();
        } catch (Throwable e) {
            return new Fail(e);
        }
    }

    private CoreDeploymentInfo createDeployment(Class<?> testClass) {
        try {
            return new CoreDeploymentInfo(null, new IvmContext(), new ModuleContext("", new AppContext("", SystemInstance.get(), testClass.getClassLoader())), testClass, null, null, null, null, null, null, null, null, BeanType.MANAGED);
        } catch (SystemException e) {
            throw new IllegalStateException(e);
        }
    }

    public static abstract class AnnotationStatement<A extends Annotation> extends Statement {

        protected final A annotation;
        protected final Statement next;
        protected final Test test;
        protected final CoreDeploymentInfo info;

        protected AnnotationStatement(A annotation, Statement next, Test test) {
            this.annotation = annotation;
            this.next = next;
            this.test = test;
            this.info = test.info;
        }
    }

    private static class Test {

        public final Class clazz;
        public final Method method;
        public final Object instance;
        public final CoreDeploymentInfo info;

        private Test(Class clazz, Method method, Object instance, CoreDeploymentInfo info) {
            this.clazz = clazz;
            this.method = method;
            this.instance = instance;
            this.info = info;
        }

        private <A extends Annotation> boolean has(Class<A> a) {
            return method.isAnnotationPresent(a) || clazz.isAnnotationPresent(a);
        }

        private <A extends Annotation> A get(Class<A> annotationClass) {
            A annotation = method.getAnnotation(annotationClass);

            if (annotation == null) {
                annotation = (A) clazz.getAnnotation(annotationClass);
                ;
            }

            return annotation;
        }
    }

    public static class RunAs extends AnnotationStatement<javax.annotation.security.RunAs> {

        public RunAs(javax.annotation.security.RunAs annotation, Statement next, Test test) {
            super(annotation, next, test);
        }

        public void evaluate() throws Throwable {
            info.setRunAs(annotation.value());
            final ThreadContext context = new ThreadContext(info, null);
            final ThreadContext old = ThreadContext.enter(context);
            try {
                next.evaluate();
            } finally {
                ThreadContext.exit(old);
            }
        }
    }

    public static class RunTestAs extends AnnotationStatement<org.apache.openejb.junit.RunTestAs> {

        public RunTestAs(org.apache.openejb.junit.RunTestAs annotation, Statement next, Test test) {
            super(annotation, next, test);
        }

        public void evaluate() throws Throwable {
            info.setRunAs(annotation.value());
            final ThreadContext context = new ThreadContext(info, null);
            final ThreadContext old = ThreadContext.enter(context);
            try {
                next.evaluate();
            } finally {
                ThreadContext.exit(old);
            }
        }
    }

    public static class TransactionAttribute extends AnnotationStatement<javax.ejb.TransactionAttribute> {

        public TransactionAttribute(javax.ejb.TransactionAttribute annotation, Statement next, Test test) {
            super(annotation, next, test);
        }

        public void evaluate() throws Throwable {
            TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
            JtaTransactionPolicyFactory factory = new JtaTransactionPolicyFactory(transactionManager);
            TransactionType transactionType = TransactionType.get(annotation.value());
            // This creates *and* begins the transaction
            TransactionPolicy policy = factory.createTransactionPolicy(transactionType);
            try {
                next.evaluate();
            } catch (Throwable t) {
                if (!isApplicationException(t)) policy.setRollbackOnly();
            } finally {
                policy.commit();
            }
        }

        private boolean isApplicationException(Throwable t) {
            if (t.getClass().isAnnotationPresent(javax.ejb.ApplicationException.class)) return true;
            if (t instanceof Error) return false;
            if (t instanceof RuntimeException) return false;
            return true;
        }
    }

    public static class Transaction extends AnnotationStatement<org.apache.openejb.junit.Transaction> {

        public Transaction(org.apache.openejb.junit.Transaction annotation, Statement next, Test test) {
            super(annotation, next, test);
        }

        public void evaluate() throws Throwable {
            TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
            JtaTransactionPolicyFactory factory = new JtaTransactionPolicyFactory(transactionManager);

            // This creates *and* begins the transaction
            TransactionPolicy policy = factory.createTransactionPolicy(TransactionType.RequiresNew);
            try {
                next.evaluate();
            } finally {

                if (annotation.rollback()) policy.setRollbackOnly();

                policy.commit();
            }
        }
    }

    public static class Interceptorss extends AnnotationStatement<Interceptors> {
        public Interceptorss(Interceptors annotation, Statement next, Test test) {
            super(annotation, next, test);
        }

        public void evaluate() throws Throwable {
        }
    }
}
