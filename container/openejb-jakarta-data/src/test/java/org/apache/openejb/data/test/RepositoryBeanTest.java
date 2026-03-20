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
package org.apache.openejb.data.test;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import org.apache.openejb.data.extension.RepositoryBean;
import org.apache.openejb.data.test.entity.Person;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryBeanTest {

    @Repository
    interface TestRepo extends CrudRepository<Person, Long> {
    }

    @Test
    void beanClassIsRepositoryInterface() {
        final RepositoryBean<TestRepo> bean = new RepositoryBean<>(TestRepo.class);
        assertEquals(TestRepo.class, bean.getBeanClass());
    }

    @Test
    void scopeIsApplicationScoped() {
        final RepositoryBean<TestRepo> bean = new RepositoryBean<>(TestRepo.class);
        assertEquals(ApplicationScoped.class, bean.getScope());
    }

    @Test
    void typesContainInterfaceAndObject() {
        final RepositoryBean<TestRepo> bean = new RepositoryBean<>(TestRepo.class);
        final Set<Type> types = bean.getTypes();
        assertEquals(2, types.size());
        assertTrue(types.contains(TestRepo.class));
        assertTrue(types.contains(Object.class));
    }

    @Test
    void qualifiersContainDefaultAndAny() {
        final RepositoryBean<TestRepo> bean = new RepositoryBean<>(TestRepo.class);
        final Set<Annotation> qualifiers = bean.getQualifiers();
        assertEquals(2, qualifiers.size());
        assertTrue(qualifiers.stream().anyMatch(a -> a.annotationType().equals(Default.class)));
        assertTrue(qualifiers.stream().anyMatch(a -> a.annotationType().equals(Any.class)));
    }

    @Test
    void nameIsNull() {
        final RepositoryBean<TestRepo> bean = new RepositoryBean<>(TestRepo.class);
        assertNull(bean.getName());
    }

    @Test
    void isNotAlternative() {
        final RepositoryBean<TestRepo> bean = new RepositoryBean<>(TestRepo.class);
        assertFalse(bean.isAlternative());
    }

    @Test
    void stereotypesAreEmpty() {
        final RepositoryBean<TestRepo> bean = new RepositoryBean<>(TestRepo.class);
        assertTrue(bean.getStereotypes().isEmpty());
    }

    @Test
    void injectionPointsAreEmpty() {
        final RepositoryBean<TestRepo> bean = new RepositoryBean<>(TestRepo.class);
        assertTrue(bean.getInjectionPoints().isEmpty());
    }

    @Test
    void idContainsInterfaceName() {
        final RepositoryBean<TestRepo> bean = new RepositoryBean<>(TestRepo.class);
        final String id = bean.getId();
        assertNotNull(id);
        assertTrue(id.contains(TestRepo.class.getName()));
        assertTrue(id.startsWith("openejb-jakarta-data-"));
    }

    @Test
    void createProducesProxyInstance() {
        final RepositoryBean<TestRepo> bean = new RepositoryBean<>(TestRepo.class);
        final TestRepo proxy = bean.create(null);
        assertNotNull(proxy);
        assertTrue(Proxy.isProxyClass(proxy.getClass()));
        assertTrue(proxy instanceof TestRepo);
    }

    @Test
    void createdProxyToStringContainsInterfaceName() {
        final RepositoryBean<TestRepo> bean = new RepositoryBean<>(TestRepo.class);
        final TestRepo proxy = bean.create(null);
        final String str = proxy.toString();
        assertTrue(str.contains("TestRepo"));
        assertTrue(str.contains("Jakarta Data Repository Proxy"));
    }
}
