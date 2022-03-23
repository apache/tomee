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

import org.apache.webbeans.container.InjectionResolver;

import jakarta.enterprise.inject.spi.Bean;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

public class WebAppInjectionResolver extends InjectionResolver {
    private final WebappWebBeansContext context;

    public WebAppInjectionResolver(final WebappWebBeansContext ctx) {
        super(ctx);
        context = ctx;
    }

    @Override
    public Set<Bean<?>> implResolveByType(final boolean delegate, final Type injectionPointType, final Class<?> injectinPointClass, final Annotation... qualifiers) {
        final Set<Bean<?>> set = super.implResolveByType(delegate, injectionPointType, injectinPointClass, qualifiers);
        if (set.isEmpty() && context.getParent() != null) {
            return context.getParent().getBeanManagerImpl().getInjectionResolver().implResolveByType(delegate, injectionPointType, injectinPointClass, qualifiers);
        }
        return set;
    }
}
