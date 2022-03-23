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

package org.apache.openejb.cdi;

import org.apache.webbeans.annotation.NewLiteral;
import org.apache.webbeans.component.BeanAttributesImpl;
import org.apache.webbeans.component.NewBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.InjectionTargetFactoryImpl;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionTarget;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class NewCdiEjbBean<T> extends CdiEjbBean<T> implements NewBean<T> {
    private static final Set<Annotation> QUALIFIERS = Collections.singleton(Annotation.class.cast(new NewLiteral()));

    private final String id;

    public NewCdiEjbBean(final CdiEjbBean<T> that) {
        super(that.getBeanContext(), that.getWebBeansContext(), that.getBeanContext().getManagedClass(), that.getAnnotatedType(),
                new NewEjbInjectionTargetFactory<T>(that.getAnnotatedType(), that.getWebBeansContext(), that.getInjectionTarget()),
                new BeanAttributesImpl<T>(that));
        this.id = that.getId() + "NewBean";
        initInternals();
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return QUALIFIERS;
    }

    private static final class NewEjbInjectionTargetFactory<T> extends InjectionTargetFactoryImpl<T> {
        private final InjectionTarget<T> injectionTarget;

        public NewEjbInjectionTargetFactory(final AnnotatedType<T> annotatedType, final WebBeansContext webBeansContext, final InjectionTarget<T> it) {
            super(annotatedType, webBeansContext);
            this.injectionTarget = it;
        }

        @Override
        public InjectionTarget<T> createInjectionTarget(final Bean<T> bean) { // avoid to refire it
            return injectionTarget;
        }
    }
}
