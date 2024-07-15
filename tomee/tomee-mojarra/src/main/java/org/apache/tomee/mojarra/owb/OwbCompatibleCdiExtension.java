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

import com.sun.faces.cdi.CdiExtension;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import org.apache.webbeans.config.WebBeansContext;

public class OwbCompatibleCdiExtension extends CdiExtension {
    private final WebBeansContext webBeansContext;

    // no-arg constructor is omitted on purpose, WebBeansContext is injected by OptimizedLoaderService
    // when replacing the Mojarra Extension with this one
    public OwbCompatibleCdiExtension(WebBeansContext webBeansContext) {
        this.webBeansContext = webBeansContext;
    }

    @Override
    public void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        super.afterBeanDiscovery(new MojarraAfterBeanDiscoveryDecorator(afterBeanDiscovery, webBeansContext), beanManager);
    }
}
