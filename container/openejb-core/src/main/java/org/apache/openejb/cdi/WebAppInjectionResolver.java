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

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.InjectionResolver;

import jakarta.enterprise.inject.spi.Bean;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * A WAR and its parent EAR live in separate CDI bean archives. OWB's default
 * {@link InjectionResolver} does not cross BDA boundaries, so an injection
 * point in a WAR cannot see beans from a library jar bundled in the parent
 * EAR. When the WAR's own resolution returns an empty set, fall back to the
 * parent application's resolver so EAR-scoped beans stay visible to the WAR.
 */
public class WebAppInjectionResolver extends InjectionResolver {
    private final WebBeansContext context;

    public WebAppInjectionResolver(final WebBeansContext ctx) {
        super(ctx);
        this.context = ctx;
    }

    @Override
    public Set<Bean<?>> implResolveByType(final boolean delegate, final Type injectionPointType,
                                          final Class<?> injectionPointClass, final Annotation... qualifiers) {
        final Set<Bean<?>> set = super.implResolveByType(delegate, injectionPointType, injectionPointClass, qualifiers);
        if (set.isEmpty() && context instanceof WebappWebBeansContext wwbc && wwbc.getParent() != null) {
            return wwbc.getParent().getBeanManagerImpl().getInjectionResolver()
                    .implResolveByType(delegate, injectionPointType, injectionPointClass, qualifiers);
        }
        return set;
    }
}
