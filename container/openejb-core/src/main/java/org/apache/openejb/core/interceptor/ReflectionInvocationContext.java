/**
 *
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
package org.apache.openejb.core.interceptor;

import javax.interceptor.InvocationContext;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.TreeMap;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * @version $Rev$ $Date$
 */
public class ReflectionInvocationContext implements InvocationContext {
    private final Iterator<Interceptor> interceptors;
    private final Object target;
    private final Method method;
    private final Object[] parameters;
    private final Map<String, Object> contextData = new TreeMap<String, Object>();
    private final Class<?>[] parameterTypes;

    private boolean parametersSet;

    public ReflectionInvocationContext(List<Interceptor> interceptors, Object target, Method method) {
        if (interceptors == null) throw new NullPointerException("interceptors is null");
        if (target == null) throw new NullPointerException("target is null");

        this.interceptors = interceptors.iterator();
        this.target = target;
        this.method = method;
        parameterTypes = (method == null)? new Class[]{}: method.getParameterTypes();
        parameters = new Object[parameterTypes.length];

        parametersSet = parameters.length == 0;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getParameters() {
        return parameters.clone();
    }

    public void setParameters(Object[] parameters) {
        if (parameters == null) throw new NullPointerException("parameters is null");
        if (parameters.length != this.parameters.length) {
            throw new IllegalArgumentException("Expected " + this.parameters.length + " parameters, but only got " + parameters.length + " parameters");
        }
        for (int i = 0; i < parameters.length; i++) {
            Object parameter = parameters[i];
            Class<?> parameterType = parameterTypes[i];

//            if (parameter == null) {
//                if (parameterType.isPrimitive()) {
//                    throw new IllegalArgumentException("Expected parameter " + i + " to be primitive type " + parameterType.getName() +
//                        ", but got a parameter that is null");
//                }
//            } else if (!parameterType.isInstance(parameter)) {
//                throw new IllegalArgumentException("Expected parameter " + i + " to be of type " + parameterType.getName() +
//                    ", but got a parameter of type " + parameter.getClass().getName());
//            }
        }
        System.arraycopy(parameters, 0, this.parameters, 0, parameters.length);
        parametersSet = true;
    }

    public Map<String, Object> getContextData() {
        return contextData;
    }

    private Invocation next() {
        if (interceptors.hasNext()) {
            Interceptor interceptor = interceptors.next();
            Object nextInstance = interceptor.getInstance();
            Method nextMethod = interceptor.getMethod();

            if (nextMethod.getParameterTypes().length == 0){
                return new Invocation(nextInstance, nextMethod, new Object[]{});
            } else {
                return new Invocation(nextInstance, nextMethod, new Object[]{this});
            }
        } else if (method != null) {
            return new Invocation(target, method, parameters);
        } else {
            return new NoOpInvocation();
        }
    }

    public Object proceed() throws Exception {
        if (!parametersSet) throw new IllegalStateException("Parameters have not been set");
        // The bulk of the logic of this method has intentionally been moved
        // out so stepping through a large stack in a debugger can be done quickly.
        // Simply put one break point on 'next.invoke()' or one inside that method.
        try {
            Invocation next = next();
            return next.invoke();
        } catch (InvocationTargetException e) {
            throw unwrapInvocationTargetException(e);
        }
    }

    private static class Invocation {
        private final Method method;
        private final Object[] args;
        private final Object target;

        public Invocation(Object target, Method method, Object[] args) {
            this.target = target;
            this.method = method;
            this.args = args;
        }
        public Object invoke() throws IllegalAccessException, InvocationTargetException {
            Object value = method.invoke(target, args);
            return value;
        }

        public String toString() {
            return method.getDeclaringClass().getName() + "." + method.getName();
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
     * @param e the invocation target exception of a reflection method invoke
     * @return the cause of the exception
     * @throws AssertionError if the cause is not an Exception or Error.
     */
    private Exception unwrapInvocationTargetException(InvocationTargetException e) {
        Throwable cause = e.getCause();
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
        String methodName = (method != null)? method.getName(): null;

        return "InvocationContext(target="+target.getClass().getName()+", method="+methodName+")";
    }
}
