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

import org.apache.openejb.core.Operation;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class InterceptorStack {
    private final Object beanInstance;
    private final List<Interceptor> interceptors;
    private final Method targetMethod;
    private final Operation operation;

    public InterceptorStack(Object beanInstance, Method targetMethod, Operation operation, List<InterceptorData> interceptorDatas, Map<String, Object> interceptorInstances) {
        if (interceptorDatas == null) throw new NullPointerException("interceptorDatas is null");
        if (interceptorInstances == null) throw new NullPointerException("interceptorInstances is null");
        this.beanInstance = beanInstance;
        this.targetMethod = targetMethod;
        this.operation = operation;

        interceptors = new ArrayList<Interceptor>(interceptorDatas.size());
        for (InterceptorData interceptorData : interceptorDatas) {
            Class interceptorClass = interceptorData.getInterceptorClass();
            Object interceptorInstance = interceptorInstances.get(interceptorClass.getName());
            if (interceptorInstance == null) {
                throw new IllegalArgumentException("No interceptor of type " + interceptorClass.getName());
            }

            List<Method> methods = interceptorData.getMethods(operation);
            for (Method method : methods) {
                Interceptor interceptor = new Interceptor(interceptorInstance, method);
                interceptors.add(interceptor);
            }
        }
    }

    public InvocationContext createInvocationContext(Object... parameters) {
        InvocationContext invocationContext = new ReflectionInvocationContext(operation, interceptors, beanInstance, targetMethod, parameters);
        return invocationContext;
    }

    public Object invoke(Object... parameters) throws Exception {
        InvocationContext invocationContext = createInvocationContext(parameters);
        Object value = invocationContext.proceed();
        return value;
    }

    public Object invoke(javax.xml.ws.handler.MessageContext messageContext) throws Exception {
        InvocationContext invocationContext = new JaxWsInvocationContext(operation, interceptors, beanInstance, targetMethod, messageContext);
        Object value = invocationContext.proceed();
        return value;
    }

    public Object invoke(javax.xml.rpc.handler.MessageContext messageContext, Object... parameters) throws Exception {
        InvocationContext invocationContext = new JaxRpcInvocationContext(operation, interceptors, beanInstance, targetMethod, messageContext, parameters);
        Object value = invocationContext.proceed();
        return value;
    }
}
