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

/**
 * @version $Rev$ $Date$
 */
public class Interceptor {
    private final Object instance;
    private final Method method;

    public Interceptor(Object instance, String methodName) {
        if (instance == null) throw new NullPointerException("instance is null");
        if (methodName == null) throw new NullPointerException("methodName is null");
        this.instance = instance;
        Class instanceClass = instance.getClass();
        try {
            this.method = instanceClass.getMethod(methodName, InvocationContext.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Interceptor method " + methodName + " not found on intercepted class " + instanceClass.getName());
        }
    }

    public Interceptor(Object instance, Method method) {
        if (instance == null) throw new NullPointerException("instance is null");
        if (method == null) throw new NullPointerException("method is null");
        this.instance = instance;
        this.method = method;
    }

    public Object getInstance() {
        return instance;
    }

    public Method getMethod() {
        return method;
    }
}
