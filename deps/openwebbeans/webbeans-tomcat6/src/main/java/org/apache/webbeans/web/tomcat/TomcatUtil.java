/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.web.tomcat;

import org.apache.webbeans.component.InjectionTargetWrapper;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.inject.OWBInjector;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;

public class TomcatUtil
{
    public static Object inject(Object object, ClassLoader loader) throws Exception
    {
        final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        CreationalContext<?> context = null;
        try
        {
            final BeanManager beanManager = WebBeansContext.currentInstance().getBeanManagerImpl();
            context = beanManager.createCreationalContext(null);
            OWBInjector.inject(beanManager, object, context);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
        return new Instance(object, context);
    }

    public static void destroy(Object injectorInstance, ClassLoader loader) throws Exception
    {
        final Instance instance = (TomcatUtil.Instance) injectorInstance;
        final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try
        {
            final BeanManagerImpl beanManager = WebBeansContext.currentInstance().getBeanManagerImpl();
            final InjectionTargetWrapper wrapper = beanManager.getInjectionTargetWrapper(instance.object.getClass());
            if (wrapper != null)
            {
                wrapper.dispose(instance.object);
            }
            else if (instance.context != null)
            {
                instance.context.release();
            }
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }

    private static class Instance
    {
        private Object object;
        private CreationalContext<?> context;

        private Instance(Object object, CreationalContext<?> context)
        {
            this.object = object;
            this.context = context;
        }
    }
}
