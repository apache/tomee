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

    private final boolean lifecycleInvocation;
    private boolean parametersSet;

    public ReflectionInvocationContext(List<Interceptor> interceptors, Object target, Method method, boolean lifecycleInvocation) {
        if (interceptors == null) throw new NullPointerException("interceptors is null");
        if (target == null) throw new NullPointerException("target is null");

        this.interceptors = interceptors.iterator();
        this.target = target;
        this.method = method;
        parameterTypes = method.getParameterTypes();
        parameters = new Object[parameterTypes.length];

        this.lifecycleInvocation = lifecycleInvocation;
        parametersSet = parameters.length == 0;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        if (lifecycleInvocation) return null;
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

            if (parameter == null) {
                if (parameterType.isPrimitive()) {
                    throw new IllegalArgumentException("Parameter " + i + " to be primitive type " + parameterType.getName() +
                        ", but got a parameter is null");
                }
            } else if (!parameterType.isInstance(parameter)) {
                throw new IllegalArgumentException("Expect parameter " + i + " to be type " + parameterType.getName() +
                    ", but got a parameter of type " + parameter.getClass().getName());
            }
        }
        System.arraycopy(parameters, 0, this.parameters, 0, parameters.length);
    }

    public Map<String, Object> getContextData() {
        return contextData;
    }

    public Object proceed() throws Exception {
        if (!parametersSet) throw new IllegalStateException("Parameters have not been set");

        if (interceptors.hasNext()) {
            Interceptor interceptor = interceptors.next();
            Object nextInstance = interceptor.getInstance();
            Method nextMethod = interceptor.getMethod();
            try {
                Object value = nextMethod.invoke(nextInstance, this);
                return value;
            } catch (InvocationTargetException e) {
                throw unwrapInvocationTargetException(e);
            }
        } else if (method != null) {
            try {
                Object value = method.invoke(target, parameters);
                return value;
            } catch (InvocationTargetException e) {
                throw unwrapInvocationTargetException(e);
            }
        }
        return null;
    }

    // todo verify excpetion types

    /**
     * Business method interceptors can only throw exception allowed by the target business method.
     * Lifecycle interceptors can only throw RuntimException.
     * @param e the invocation target excption of a reflection method invoke
     * @return the cause of the exception
     * @throws Error if the cause is not an Exception
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
}
