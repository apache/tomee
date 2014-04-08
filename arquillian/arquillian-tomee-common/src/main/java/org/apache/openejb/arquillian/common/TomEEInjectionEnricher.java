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
package org.apache.openejb.arquillian.common;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.arquillian.common.enrichment.OpenEJBEnricher;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.jboss.arquillian.test.spi.TestEnricher;

import java.lang.reflect.Method;

public class TomEEInjectionEnricher implements TestEnricher {
    @Override
    public void enrich(final Object o) {
        OpenEJBEnricher.enrich(o, getAppContext(o.getClass().getName()));
    }

    private AppContext getAppContext(final String className) {
        final BeanContext context = SystemInstance.get().getComponent(ContainerSystem.class).getBeanContext(className);
        if (context == null) {
            return null;
        }
        return context.getModuleContext().getAppContext();
    }

    @Override
    public Object[] resolve(final Method method) {
        return new Object[method.getParameterTypes().length];
    }
}
