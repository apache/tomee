/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.dynamic;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.Repository;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

public class SpringDataProxy implements InvocationHandler {

    @PersistenceContext
    private EntityManager em;

    @Resource(name = "implementingInterfaceClass")
    private Class<Repository<?, ?>> implementingInterfaceClass; // implicitly for this kind of proxy

    private final AtomicReference<Repository<?, ?>> repository = new AtomicReference<Repository<?, ?>>();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (repository.get() == null) {
            synchronized (this) {
                if (repository.get() == null) {
                    repository.set(new JpaRepositoryFactory(em).getRepository(implementingInterfaceClass));
                }
            }
        }
        return method.invoke(repository.get(), args);
    }
}
