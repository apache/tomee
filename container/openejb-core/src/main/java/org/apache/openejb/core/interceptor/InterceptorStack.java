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
import java.lang.reflect.Method;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * @version $Rev$ $Date$
 */
public class InterceptorStack {
    private final Object beanInstance;
    private final List<Interceptor> interceptors;
    private final Method method;
    private boolean lifecycleInvocation;

    public InterceptorStack(Object beanInstance, Method method, boolean lifecycleInvocation, Collection<Object> interceptorInstances, List<InterceptorData> interceptorDatas) {
        this.beanInstance = beanInstance;
        this.method = method;
        this.lifecycleInvocation = lifecycleInvocation;

        Map<String, Object> interceptorsByClass = new HashMap<String, Object>();
        for (Object interceptor : interceptorInstances) {
            interceptorsByClass.put(interceptor.getClass().getName(), interceptor);
        }

        interceptors = new ArrayList<Interceptor>(interceptorDatas.size());
        for (InterceptorData interceptorData : interceptorDatas) {
            String interceptorClass = interceptorData.getInterceptorClass();
            Object interceptorInstance = interceptorsByClass.get(interceptorClass);
            if (interceptorInstance == null) {
                throw new IllegalArgumentException("No interceptor of type " + interceptorClass);
            }
            Interceptor interceptor = new Interceptor(interceptorInstance, interceptorData.getInterceptorMethod());
            interceptors.add(interceptor);
        }
    }

    public InterceptorStack(Object beanInstance, Method method, boolean lifecycleInvocation, List<Interceptor> interceptors) {
        this.beanInstance = beanInstance;
        this.method = method;
        this.lifecycleInvocation = lifecycleInvocation;
        this.interceptors = interceptors;
    }

    public InvocationContext createInvocationContext() {
        InvocationContext invocationContext = new ReflectionInvocationContext(interceptors, beanInstance, method, lifecycleInvocation);
        return invocationContext;
    }

    public Object invoke(Object... parameters) throws Exception {
        InvocationContext invocationContext = createInvocationContext();
        invocationContext.setParameters(parameters);
        Object value = invocationContext.proceed();
        return value;
    }
}
