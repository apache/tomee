/**
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
package org.apache.openejb.mockito;

import org.apache.openejb.injection.FallbackPropertyInjector;
import org.apache.openejb.loader.SystemInstance;
import org.apache.webbeans.annotation.AnyLiteral;
import org.apache.webbeans.annotation.DefaultLiteral;
import org.apache.webbeans.annotation.NamedLiteral;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Prioritized;
import jakarta.interceptor.Interceptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is responsible to add mocks as CDI beans.
 */
public class MockitoExtension implements Extension {
    private static final Annotation DEFAULT_ANNOTATION = new DefaultLiteral();
    private static final Annotation ANY_ANNOTATION = new AnyLiteral();

    public void addMocks(@Observes final BeforeBeanDiscovery bbd) {
        // ensure it is init
        SystemInstance.get().getComponent(FallbackPropertyInjector.class);
    }

    public void addMocks(@Observes final AfterBeanDiscovery abd) {
        for (Map.Entry<Class<?>, Object> instance : MockRegistry.mocksByType().entrySet()) {
            abd.addBean(new MockBean(instance.getKey(), instance.getValue()));
        }
        for (Map.Entry<String, Object> instance : MockRegistry.mocksByName().entrySet()) {
            abd.addBean(new NamedMockBean(instance.getKey(), instance.getValue()));
        }
    }

    private static class MockBean<T> implements Bean<T>, Prioritized {
        protected static final Set<Annotation> QUALIFIERS = new HashSet<Annotation>(2) {{
            add(DEFAULT_ANNOTATION);
            add(ANY_ANNOTATION);
        }};

        protected final Class<T> clazz;
        protected final Object instance;
        protected final HashSet<Type> types;

        public MockBean(final Class<T> key, final Object value) {
            clazz = key;
            instance = value;

            types = new HashSet<Type>();
            Class<?> current = clazz;
            if (clazz != null) {
                if (!Proxy.isProxyClass(current)) {
                    while (!Object.class.equals(current) && current != null) {
                        types.add(current);
                        current = current.getSuperclass();
                    }
                }
                for (Class<?> itf : clazz.getInterfaces()) {
                    if (itf.getName().startsWith("org.mockito")) {
                        continue;
                    }

                    types.add(itf);
                }
            }
        }

        public Set<Type> getTypes() {
            return types;
        }

        public Set<Annotation> getQualifiers() {
            return QUALIFIERS;
        }

        public Class<? extends Annotation> getScope() {
            return Dependent.class;
        }

        public String getName() {
            return null;
        }

        public boolean isNullable() {
            return false;
        }

        public Set<InjectionPoint> getInjectionPoints() {
            return Collections.emptySet();
        }

        public Class<?> getBeanClass() {
            return clazz;
        }

        public Set<Class<? extends Annotation>> getStereotypes() {
            return Collections.emptySet();
        }

        public boolean isAlternative() {
            return false;
        }

        public T create(final CreationalContext<T> context) {
            return clazz.cast(instance);
        }

        public void destroy(final T instance, final CreationalContext<T> context) {
            // no-op
        }

        @Override
        public int getPriority() {
            return Interceptor.Priority.PLATFORM_AFTER+1000;
        }
    }

    private static class NamedMockBean<T> extends MockBean<T> {
        private final String name;
        private final Set<Annotation> qualifiers;

        public NamedMockBean(final String named, final Object value) {
            super((Class<T>) value.getClass(), value);

            name = named;

            // we need to pass value.getClass() to get interfaces
            // but we don't want the proxy to be injectable
            if (!clazz.isInterface()) {
                types.remove(clazz);
            }

            qualifiers = new HashSet<Annotation>(2);
            qualifiers.add(ANY_ANNOTATION);
            qualifiers.add(new NamedLiteral(name));
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return qualifiers;
        }

        @Override
        public T create(final CreationalContext<T> context) {
            return clazz.cast(instance);
        }
    }
}
