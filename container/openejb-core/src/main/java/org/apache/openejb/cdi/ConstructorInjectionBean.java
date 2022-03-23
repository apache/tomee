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

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.component.creation.BeanAttributesBuilder;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.portable.InjectionTargetImpl;

import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.InjectionPoint;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;

/**
 * NOTE: think to cache this object to avoid concurrent issues.
 *
 * @version $Rev$ $Date$
 */
public class ConstructorInjectionBean<T> extends InjectionTargetBean<T> { // TODO: see InjectableConstructor
    private static final Field INJECTION_TARGET_FIELD;

    static {
        try {
            INJECTION_TARGET_FIELD = InjectionTargetBean.class.getDeclaredField("injectionTarget");
        } catch (final NoSuchFieldException e) {
            throw new OpenEJBRuntimeException(e);
        }
        INJECTION_TARGET_FIELD.setAccessible(true);
    }

    private final boolean passivationCapable;

    public ConstructorInjectionBean(final WebBeansContext webBeansContext, final Class<T> returnType, final AnnotatedType<T> at) {
        this(webBeansContext, returnType, at, null);
    }

    public ConstructorInjectionBean(final WebBeansContext webBeansContext, final Class<T> returnType, final AnnotatedType<T> at, final Boolean passivationCapable) {
        super(webBeansContext, WebBeansType.DEPENDENT, at, BeanAttributesBuilder.forContext(webBeansContext).newBeanAttibutes(at).build(), returnType);
        try {
            INJECTION_TARGET_FIELD.set(this, new ConstructorInjectionTarget<>(getAnnotatedType(), getInjectionPoints(), getWebBeansContext()));
        } catch (final Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
        if (passivationCapable != null) {
            this.passivationCapable = passivationCapable;
        } else {
            this.passivationCapable = super.isPassivationCapable();
        }
    }

    @Override
    public boolean isPassivationCapable() {
        return passivationCapable;
    }

    private static final class ConstructorInjectionTarget<T> extends InjectionTargetImpl<T> {
        public ConstructorInjectionTarget(final AnnotatedType<T> annotatedType, final Set<InjectionPoint> points, final WebBeansContext webBeansContext) {
            super(annotatedType, points, webBeansContext, Collections.<AnnotatedMethod<?>>emptyList(), Collections.<AnnotatedMethod<?>>emptyList());
        }
    }
}
