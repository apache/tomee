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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.testing.TestInstance;
import org.apache.webbeans.annotation.AnyLiteral;
import org.apache.webbeans.annotation.DefaultLiteral;
import org.apache.webbeans.annotation.NamedLiteral;
import org.apache.webbeans.util.GenericsUtil;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * This class is responsible to initialize mocks declared in the test class and register them as CDI beans.
 */
public class MockitoExtension implements Extension {

	private static class MockBean implements Bean<Object> {

		private final Class<?> beanClass;
		private final Object instance;
		private final Set<Annotation> qualifiers;
		private final Set<Type> types;

		public MockBean(Class<?> beanClass, Object instance, Set<Type> types, Set<Annotation> qualifiers) {
			this.beanClass = beanClass;
			this.instance = instance;
			this.types = types;
			this.qualifiers = qualifiers;
		}

		public Object create(CreationalContext<Object> context) {
			return instance;
		}

		public void destroy(Object instance, CreationalContext<Object> context) {
		}

		public Class<?> getBeanClass() {
			return beanClass;
		}

		public Set<InjectionPoint> getInjectionPoints() {
			return Collections.emptySet();
		}

		public String getName() {
			return null;
		}

		public Set<Annotation> getQualifiers() {
			return qualifiers;
		}

		public Class<? extends Annotation> getScope() {
			return Dependent.class; // otherwise will be proxied
		}

		public Set<Class<? extends Annotation>> getStereotypes() {
			return Collections.emptySet();
		}

		public Set<Type> getTypes() {
			return types;
		}

		public boolean isAlternative() {
			return true;
		}

		public boolean isNullable() {
			return false;
		}
	}
	
	public void addMocks(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
		TestInstance instance = SystemInstance.get().getComponent(TestInstance.class);
		if (instance != null) {
			MockitoAnnotations.initMocks(instance.getInstance());
			for (Class<?> c = instance.getTestClass(); !c.equals(Object.class); c = c.getSuperclass()) {
				for (Field f : c.getDeclaredFields()) {
					if (f.isAnnotationPresent(Mock.class)) {
						boolean a = f.isAccessible();
						try {
							f.setAccessible(true);
							Object value = f.get(instance.getInstance());
							afterBeanDiscovery.addBean(createBean(f, value, beanManager));
						} catch (IllegalAccessException iae) {
							return; // forget it
						} finally {
							f.setAccessible(a);
						}
					}
				}
			}
		}
	}

	private void collectQualifiers(AnnotatedElement annotated, Collection<Annotation> qualifiers, BeanManager beanManager) {
		for (Annotation annotation : annotated.getAnnotations()) {
			if (beanManager.isQualifier(annotation.annotationType())) {
				qualifiers.add(annotation);
			} else if (beanManager.isStereotype(annotation.annotationType())) {
				collectQualifiers(annotation.annotationType(), qualifiers, beanManager);
			}
		}
	}

	private Bean<?> createBean(Field field, Object instance, BeanManager beanManager) {
		
		Set<Type> types = new HashSet<>();
		if (field.isAnnotationPresent(Typed.class)) {
			types.addAll(Arrays.asList(field.getAnnotation(Typed.class).value()));
		} else {
			types.addAll(GenericsUtil.getTypeClosure(field.getGenericType()));
		}
		
		Set<Annotation> qualifiers = new HashSet<>();
		collectQualifiers(field, qualifiers, beanManager);
		
		Mock mock = field.getAnnotation(Mock.class);
		if (!"".equals(mock.name())) {
			qualifiers.add(new NamedLiteral(mock.name()));
		}

		if (qualifiers.isEmpty()) {
			qualifiers.add(DefaultLiteral.INSTANCE);
		}
		qualifiers.add(AnyLiteral.INSTANCE);

		return new MockBean(field.getType(), instance, types, qualifiers);
	}
}
