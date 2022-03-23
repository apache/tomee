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

import org.apache.openejb.BeanContext;
import org.apache.openejb.assembler.classic.ProxyInterfaceResolver;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.ejb.common.component.EjbBeanBuilder;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanAttributes;
import java.util.List;

public class OpenEJBBeanBuilder<A> extends EjbBeanBuilder<A, CdiEjbBean<A>> {
    private final BeanContext beanContext;

    public OpenEJBBeanBuilder(final BeanContext bc, final WebBeansContext webBeansContext, final AnnotatedType<A> annotatedType,
                              final BeanAttributes<A> attributes) {
        super(webBeansContext, annotatedType, attributes);
        this.beanContext = bc;
    }

    @Override
    protected CdiEjbBean<A> createBean(final Class<A> beanClass, final boolean beanEnabled) {
        return new CdiEjbBean<>(beanContext, webBeansContext, annotatedType, beanAttributes);
    }

    @Override
    protected A getInstance(final CreationalContext<A> creationalContext) {
        final List<Class> classes = beanContext.getBusinessLocalInterfaces();
        final CurrentCreationalContext currentCreationalContext = beanContext.get(CurrentCreationalContext.class);
        final CreationalContext existing = currentCreationalContext.get();
        currentCreationalContext.set(creationalContext);
        try {
            if (classes.size() == 0 && beanContext.isLocalbean()) {
                final BeanContext.BusinessLocalBeanHome home = beanContext.getBusinessLocalBeanHome();
                return (A) home.create();
            } else {
                final Class<?> mainInterface = classes.get(0);
                final List<Class> interfaces = ProxyInterfaceResolver.getInterfaces(beanContext.getBeanClass(), mainInterface, classes);
                final BeanContext.BusinessLocalHome home = beanContext.getBusinessLocalHome(interfaces, mainInterface);
                return (A) home.create();
            }
        } finally {
            currentCreationalContext.set(existing);
        }
    }
}
