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
package org.apache.tomee.mojarra;

import com.sun.faces.spi.DiscoverableInjectionProvider;
import com.sun.faces.spi.InjectionProviderException;
import org.apache.catalina.core.StandardContext;
import org.apache.tomee.catalina.JavaeeInstanceManager;
import org.apache.tomee.catalina.TomEEContainerListener;

public class TomEEInjectionProvider extends DiscoverableInjectionProvider {
    private JavaeeInstanceManager instanceManager;

    public TomEEInjectionProvider() {
        final StandardContext context = TomEEContainerListener.get();
        if (context == null) {
            throw new IllegalArgumentException("standard context cannot be found");
        }
        instanceManager = (JavaeeInstanceManager) context.getInstanceManager();
    }

    @Override
    public void inject(final Object managedBean) throws InjectionProviderException {
        try {
            instanceManager.inject(managedBean);
        } catch (final Exception e) {
            throw new InjectionProviderException(e);
        }
    }

    @Override
    public void invokePreDestroy(final Object managedBean) throws InjectionProviderException {
        try {
            instanceManager.destroyInstance(managedBean);
        } catch (final Exception e) {
            throw new InjectionProviderException(e);
        }
    }

    @Override
    public void invokePostConstruct(final Object managedBean) throws InjectionProviderException {
        try {
            instanceManager.postConstruct(managedBean, managedBean.getClass());
        } catch (final Exception e) {
            throw new InjectionProviderException(e);
        }
    }
}
