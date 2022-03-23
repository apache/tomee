/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.junit;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.ModuleContext;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.core.transaction.JtaTransactionPolicyFactory;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.loader.SystemInstance;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import jakarta.interceptor.Interceptors;
import jakarta.transaction.TransactionManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
public class LocalClientRunner extends BlockJUnit4ClassRunner {

    private final BeanContext deployment;
    private final Class<?> clazz;

    public LocalClientRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);
        deployment = createDeployment(clazz);
        this.clazz = clazz;
    }

    @Override
    protected Statement methodBlock(final FrameworkMethod method) {
        final Object instance = newTestInstance();

        final Test test = new Test(clazz, method.getMethod(), instance, deployment);

        Statement statement = methodInvoker(method, instance);

        statement = wrap(test, statement, RunAs.class, jakarta.annotation.security.RunAs.class);
        statement = wrap(test, statement, RunTestAs.class, org.apache.openejb.junit.RunTestAs.class);
        statement = wrap(test, statement, Transaction.class, org.apache.openejb.junit.Transaction.class);
        statement = wrap(test, statement, TransactionAttribute.class, jakarta.ejb.TransactionAttribute.class);

        statement = possiblyExpectingExceptions(method, instance, statement);
        statement = withPotentialTimeout(method, instance, statement);
        statement = withBefores(method, instance, statement);
        statement = withAfters(method, instance, statement);
        return statement;
    }

    private Statement wrap(final Test test, final Statement statement, final Class<? extends AnnotationStatement> clazz, final Class<? extends Annotation> annotation) {

        if (test.has(annotation)) {
            try {
                final Class[] types = {annotation, Statement.class, Test.class};
                final Object[] args = {test.get(annotation), statement, test};
                return clazz.getConstructor(types).newInstance(args);
            } catch (final Exception e) {
                throw new IllegalStateException("Cannot construct " + clazz, e);
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
        } catch (final Throwable e) {
            return new Fail(e);
        }
    }

    private BeanContext createDeployment(final Class<?> testClass) {
        try {
            final AppContext appContext = new AppContext("", SystemInstance.get(), testClass.getClassLoader(), new IvmContext(), new IvmContext(), false);
            final ModuleContext moduleContext = new ModuleContext("", null, "", appContext, new IvmContext(), null);
            return new BeanContext(null, new IvmContext(), moduleContext, testClass, null, null, null, null, null, null, null, null, null, BeanType.MANAGED, false, false);
        } catch (final SystemException e) {
            throw new IllegalStateException(e);
        }
    }

    public abstract static class AnnotationStatement<A extends Annotation> extends Statement {

        protected final A annotation;
        protected final Statement next;
        protected final Test test;
        protected final BeanContext info;

        protected AnnotationStatement(final A annotation, final Statement next, final Test test) {
            this.annotation = annotation;
            this.next = next;
            this.test = test;
            this.info = test.info;
        }
    }

    private static final class Test {

        public final Class clazz;
        public final Method method;
        public final Object instance;
        public final BeanContext info;

        private Test(final Class clazz, final Method method, final Object instance, final BeanContext info) {
            this.clazz = clazz;
            this.method = method;
            this.instance = instance;
            this.info = info;
        }

        private <A extends Annotation> boolean has(final Class<A> a) {
            return method.isAnnotationPresent(a) || clazz.isAnnotationPresent(a);
        }

        private <A extends Annotation> A get(final Class<A> annotationClass) {
            A annotation = method.getAnnotation(annotationClass);

            if (annotation == null) {
                annotation = (A) clazz.getAnnotation(annotationClass);
            }

            return annotation;
        }
    }

    public static class RunAs extends AnnotationStatement<jakarta.annotation.security.RunAs> {

        public RunAs(final jakarta.annotation.security.RunAs annotation, final Statement next, final Test test) {
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

        public RunTestAs(final org.apache.openejb.junit.RunTestAs annotation, final Statement next, final Test test) {
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

    public static class TransactionAttribute extends AnnotationStatement<jakarta.ejb.TransactionAttribute> {

        public TransactionAttribute(final jakarta.ejb.TransactionAttribute annotation, final Statement next, final Test test) {
            super(annotation, next, test);
        }

        public void evaluate() throws Throwable {
            final TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
            final JtaTransactionPolicyFactory factory = new JtaTransactionPolicyFactory(transactionManager);
            final TransactionType transactionType = TransactionType.get(annotation.value());
            // This creates *and* begins the transaction
            final TransactionPolicy policy = factory.createTransactionPolicy(transactionType);
            try {
                next.evaluate();
            } catch (final Throwable t) {
                if (!isApplicationException(t)) {
                    policy.setRollbackOnly();
                }
            } finally {
                policy.commit();
            }
        }

        private boolean isApplicationException(final Throwable t) {
            if (t.getClass().isAnnotationPresent(jakarta.ejb.ApplicationException.class)) {
                return true;
            }
            if (t instanceof Error) {
                return false;
            }
            if (t instanceof RuntimeException) {
                return false;
            }
            return true;
        }
    }

    public static class Transaction extends AnnotationStatement<org.apache.openejb.junit.Transaction> {

        public Transaction(final org.apache.openejb.junit.Transaction annotation, final Statement next, final Test test) {
            super(annotation, next, test);
        }

        public void evaluate() throws Throwable {
            final TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
            final JtaTransactionPolicyFactory factory = new JtaTransactionPolicyFactory(transactionManager);

            // This creates *and* begins the transaction
            final TransactionPolicy policy = factory.createTransactionPolicy(TransactionType.RequiresNew);
            try {
                next.evaluate();
            } finally {

                if (annotation.rollback()) {
                    policy.setRollbackOnly();
                }

                policy.commit();
            }
        }
    }

    public static class Interceptorss extends AnnotationStatement<Interceptors> {
        public Interceptorss(final Interceptors annotation, final Statement next, final Test test) {
            super(annotation, next, test);
        }

        public void evaluate() throws Throwable {
        }
    }
}
