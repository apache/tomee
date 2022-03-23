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

package org.apache.openejb.core.interceptor;

import org.apache.openejb.core.Operation;
import org.apache.openejb.util.Classes;

import jakarta.interceptor.InvocationContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @version $Rev$ $Date$
 */
public class ReflectionInvocationContext implements InvocationContext {
    private final Iterator<Interceptor> interceptors;
    private final Object target;
    private final Method method;
    private final Object[] parameters;
    private final Map<String, Object> contextData = new TreeMap<>();
    private final Class<?>[] parameterTypes;

    private final Operation operation;

    public ReflectionInvocationContext(final Operation operation, final List<Interceptor> interceptors, final Object target, final Method method, final Object... parameters) {
        if (operation == null) {
            throw new NullPointerException("operation is null");
        }
        if (interceptors == null) {
            throw new NullPointerException("interceptors is null");
        }
        if (target == null) {
            throw new NullPointerException("target is null");
        }

        this.operation = operation;
        this.interceptors = interceptors.iterator();
        this.target = target;
        this.method = method;
        this.parameters = parameters;

        if (method == null) {
            parameterTypes = new Class[0];
        } else {
            parameterTypes = method.getParameterTypes();
        }
    }

    @Override
    public Object getTimer() {
        if (operation.equals(Operation.TIMEOUT)) {
            return parameters[0];
        }
        return null;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Constructor<?> getConstructor() {
        throw new IllegalStateException(); // TODO
    }

    @Override
    public Object[] getParameters() {
        //TODO Need to figure out what is going on with afterCompletion call back here ?
        if (Operation.POST_CONSTRUCT.equals(operation) || Operation.PRE_DESTROY.equals(operation)) {
            //if (operation.isCallback() && !operation.equals(Operation.AFTER_COMPLETION) && !operation.equals(Operation.TIMEOUT)) {
            throw new IllegalStateException(getIllegalParameterAccessMessage());
        }
        return this.parameters;
    }

    private String getIllegalParameterAccessMessage() {
        String m = "Callback methods cannot access parameters.";
        m += "  Callback Type: " + operation;
        if (method != null) {
            m += ", Target Method: " + method.getName();
        }
        if (target != null) {
            m += ", Target Bean: " + target.getClass().getName();
        }
        return m;
    }

    @Override
    public void setParameters(final Object[] parameters) {
        if (operation.isCallback() && !operation.equals(Operation.TIMEOUT)) {
            throw new IllegalStateException(getIllegalParameterAccessMessage());
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters is null");
        }
        if (parameters.length != this.parameters.length) {
            throw new IllegalArgumentException("Expected " + this.parameters.length + " parameters, but only got " + parameters.length + " parameters");
        }
        for (int i = 0; i < parameters.length; i++) {
            final Object parameter = parameters[i];
            final Class<?> parameterType = parameterTypes[i];

            if (parameter == null) {
                if (parameterType.isPrimitive()) {
                    throw new IllegalArgumentException("Expected parameter " + i + " to be primitive type " + parameterType.getName() +
                        ", but got a parameter that is null");
                }
            } else {
                //check that types are applicable
                final Class<?> actual = Classes.deprimitivize(parameterType);
                final Class<?> given = Classes.deprimitivize(parameter.getClass());

                if (!actual.isAssignableFrom(given)) {
                    throw new IllegalArgumentException("Expected parameter " + i + " to be of type " + parameterType.getName() +
                        ", but got a parameter of type " + parameter.getClass().getName());
                }
            }
        }
        System.arraycopy(parameters, 0, this.parameters, 0, parameters.length);
    }

    @Override
    public Map<String, Object> getContextData() {
        return contextData;
    }

    private Invocation next() {
        if (interceptors.hasNext()) {
            final Interceptor interceptor = interceptors.next();
            final Object nextInstance = interceptor.getInstance();
            final Method nextMethod = interceptor.getMethod();

            if (nextMethod.getParameterTypes().length == 1 && nextMethod.getParameterTypes()[0] == InvocationContext.class) {
                return new InterceptorInvocation(nextInstance, nextMethod, this);
            } else {
                return new LifecycleInvocation(nextInstance, nextMethod, this, parameters);
            }
        } else if (method != null) {
            //EJB 3.1, it is allowed that timeout method does not have parameter Timer.class,
            //However, while invoking the timeout method, the timer value is passed, as it is also required by InnvocationContext.getTimer() method
            final Object[] methodParameters;
            if (operation.equals(Operation.TIMEOUT) && method.getParameterTypes().length == 0) {
                methodParameters = new Object[0];
            } else {
                methodParameters = parameters;
            }
            return new BeanInvocation(target, method, methodParameters);
        } else {
            return new NoOpInvocation();
        }
    }

    @Override
    public Object proceed() throws Exception {
        // The bulk of the logic of this method has intentionally been moved
        // out so stepping through a large stack in a debugger can be done quickly.
        // Simply put one break point on 'next.invoke()' or one inside that method.
        try {
            final Invocation next = next();
            return next.invoke();
        } catch (final InvocationTargetException e) {
            throw unwrapInvocationTargetException(e);
        }
    }

    private abstract static class Invocation {
        private final Method method;
        private final Object[] args;
        private final Object target;

        public Invocation(final Object target, final Method method, final Object[] args) {
            this.target = target;
            this.method = method;
            this.args = args;
        }

        public Object invoke() throws Exception {

            final Object value = method.invoke(target, args);
            return value;
        }


        public String toString() {
            return method.getDeclaringClass().getName() + "." + method.getName();
        }
    }

    private static class BeanInvocation extends Invocation {
        public BeanInvocation(final Object target, final Method method, final Object[] args) {
            super(target, method, args);
        }
    }

    private static class InterceptorInvocation extends Invocation {
        public InterceptorInvocation(final Object target, final Method method, final InvocationContext invocationContext) {
            super(target, method, new Object[]{invocationContext});
        }
    }

    private static class LifecycleInvocation extends Invocation {
        private final InvocationContext invocationContext;

        public LifecycleInvocation(final Object target, final Method method, final InvocationContext invocationContext, final Object[] args) {
            super(target, method, args);
            this.invocationContext = invocationContext;
        }

        public Object invoke() throws Exception {
            // invoke the callback
            super.invoke();

            // we need to call proceed so callbacks in subclasses get invoked
            final Object value = invocationContext.proceed();
            return value;
        }
    }

    private static class NoOpInvocation extends Invocation {
        public NoOpInvocation() {
            super(null, null, null);
        }

        public Object invoke() throws IllegalAccessException, InvocationTargetException {
            return null;
        }
    }

    // todo verify excpetion types

    /**
     * Business method interceptors can only throw exception allowed by the target business method.
     * Lifecycle interceptors can only throw RuntimeException.
     *
     * @param e the invocation target exception of a reflection method invoke
     * @return the cause of the exception
     * @throws AssertionError if the cause is not an Exception or Error.
     */
    private Exception unwrapInvocationTargetException(final InvocationTargetException e) {
        final Throwable cause = e.getCause();
        if (cause == null) {
            return e;
        } else if (cause instanceof Exception) {
            return (Exception) cause;
        } else if (cause instanceof Error) {
            throw (Error) cause;
        } else {
            throw new AssertionError(cause);
        }
    }

    public String toString() {
        final String methodName = method != null ? method.getName() : null;

        return "InvocationContext(operation=" + operation + ", target=" + target.getClass().getName() + ", method=" + methodName + ")";
    }
}
