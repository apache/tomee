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
package org.apache.openejb.assembler.classic;

import jakarta.validation.ValidatorFactory;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

// TODO: make it generic (LazyDelegate + Factory + refactor LazyValidator)
public class LazyValidatorFactory implements InvocationHandler, Serializable {
    private final transient ReentrantLock lock = new ReentrantLock();
    private final transient ClassLoader loader;
    private final ValidationInfo info;
    private volatile ValidatorFactory factory;

    public LazyValidatorFactory(final ClassLoader classLoader, final ValidationInfo validationInfo) {
        this.loader = classLoader;
        this.info = validationInfo;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        ensureDelegate();
        try {
            return method.invoke(factory, args);
        } catch (final InvocationTargetException ite) {
            throw ite.getCause();
        }
    }

    private void ensureDelegate() {
        if (factory == null) {
            final ReentrantLock l = lock;
            l.lock();
            try {
                if (factory == null) {
                    factory = ValidatorBuilder.buildFactory(
                            loader == null ? Thread.currentThread().getContextClassLoader() : loader,
                            info == null ? new ValidationInfo() : info);
                }
            } finally {
                l.unlock();
            }
        }
    }

    public ValidatorFactory getFactory() {
        ensureDelegate();
        return factory;
    }
}
