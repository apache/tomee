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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.mojarra.owb;

import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.ObserverMethod;
import jakarta.enterprise.inject.spi.configurator.BeanConfigurator;
import jakarta.enterprise.inject.spi.configurator.ObserverMethodConfigurator;
import org.apache.webbeans.config.WebBeansContext;

public class MojarraAfterBeanDiscoveryDecorator implements AfterBeanDiscovery {
    private final AfterBeanDiscovery delegate;
    private final WebBeansContext webBeansContext;

    public MojarraAfterBeanDiscoveryDecorator(final AfterBeanDiscovery delegate) {
        this.delegate = delegate;
        this.webBeansContext = WebBeansContext.currentInstance();
    }

    @Override
    public void addDefinitionError(Throwable t) {
        delegate.addDefinitionError(t);
    }

    @Override
    public void addBean(Bean<?> bean) {
        delegate.addBean(new MojarraPassivationCapableThirdPartyBeanImpl<>(webBeansContext, bean));
    }

    @Override
    public <T> BeanConfigurator<T> addBean() {
        return delegate.addBean();
    }

    @Override
    public void addObserverMethod(ObserverMethod<?> observerMethod) {
        delegate.addObserverMethod(observerMethod);
    }

    @Override
    public <T> ObserverMethodConfigurator<T> addObserverMethod() {
        return delegate.addObserverMethod();
    }

    @Override
    public void addContext(Context context) {
        delegate.addContext(context);
    }

    @Override
    public <T> AnnotatedType<T> getAnnotatedType(Class<T> type, String id) {
        return delegate.getAnnotatedType(type, id);
    }

    @Override
    public <T> Iterable<AnnotatedType<T>> getAnnotatedTypes(Class<T> type) {
        return delegate.getAnnotatedTypes(type);
    }
}
