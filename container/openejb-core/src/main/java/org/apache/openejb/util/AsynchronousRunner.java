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
package org.apache.openejb.util;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Utility class used to invoke methods asynchronously, using a given {@link Executor}.
 */
public class AsynchronousRunner {

    private final Executor executor;

    public AsynchronousRunner(Executor executor) {
        this.executor = executor;
    }

    /**
     * Performs the given method invocation asynchronously
     *
     * @param object    The object which will have the method invoked
     * @param method    The method to be invoked
     * @param arguments The invocation arguments
     * @return A {@link Future} containing the method return value
     */
    public Future<Object> runAsync(Object object, Method method, Object... arguments) {
        Callable<Object> callable = new MethodInvoker(object, method, arguments);
        FutureTask<Object> futureTask = new FutureTask<Object>(callable);
        executor.execute(futureTask);
        return futureTask;
    }

    /**
     * A {@link Callable} implementation which just delegates the execution using
     * {@link Method#invoke(Object, Object[])}
     *
     * @author luis
     */
    private class MethodInvoker implements Callable<Object> {
        private Object object;
        private Method method;
        private Object[] arguments;

        public MethodInvoker(Object object, Method method, Object[] arguments) {
            this.object = object;
            this.method = method;
            this.arguments = arguments;
        }

        public Object call() throws Exception {
            return method.invoke(object, arguments);
        }
    }
}
