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
package org.apache.openejb.data.extension;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.PassivationCapable;
import org.apache.openejb.data.handler.RepositoryInvocationHandler;
import org.apache.openejb.data.meta.RepositoryMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RepositoryBean<T> implements Bean<T>, PassivationCapable {

    private final Class<T> repositoryInterface;
    private final RepositoryMetadata metadata;
    private final Set<Type> types;
    private final Set<Annotation> qualifiers;

    public RepositoryBean(final Class<T> repositoryInterface) {
        this.repositoryInterface = repositoryInterface;
        this.metadata = new RepositoryMetadata(repositoryInterface);

        final Set<Type> t = new HashSet<>();
        t.add(repositoryInterface);
        t.add(Object.class);
        this.types = Collections.unmodifiableSet(t);

        final Set<Annotation> q = new HashSet<>();
        q.add(Default.Literal.INSTANCE);
        q.add(Any.Literal.INSTANCE);
        this.qualifiers = Collections.unmodifiableSet(q);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T create(final CreationalContext<T> creationalContext) {
        final RepositoryInvocationHandler handler = new RepositoryInvocationHandler(metadata);
        return (T) Proxy.newProxyInstance(
            repositoryInterface.getClassLoader(),
            new Class<?>[]{repositoryInterface},
            handler
        );
    }

    @Override
    public void destroy(final T instance, final CreationalContext<T> creationalContext) {
        creationalContext.release();
    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ApplicationScoped.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public Class<?> getBeanClass() {
        return repositoryInterface;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public String getId() {
        return "openejb-jakarta-data-" + repositoryInterface.getName();
    }
}
