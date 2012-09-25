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
package org.apache.webbeans.web.intercept;

import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.intercept.NormalScopedBeanInterceptorHandler;

import javax.enterprise.context.spi.CreationalContext;
import java.util.HashMap;


/**
 * <p>This is a {@link org.apache.webbeans.proxy.MethodHandler} especially
 * made for &#064;RequestScoped beans used in web applications.</p>
 * 
 * <p>Since there is only one single contextual instance of an &#064;RequestScoped bean per thread,
 * we can simply cache this instance inside our bean. We only need to reload this instance
 * if it is null or if the thread ends.</p>
 */
public class RequestScopedBeanInterceptorHandler extends NormalScopedBeanInterceptorHandler
{
    /**default serial id*/
    private static final long serialVersionUID = 1L;

    /**
     * Cached bean instance for each thread
     */
    private static ThreadLocal<HashMap<OwbBean<?>, CacheEntry>> cachedInstances = new ThreadLocal<HashMap<OwbBean<?>, CacheEntry>>();


    public static void removeThreadLocals()
    {
        cachedInstances.set(null);
        cachedInstances.remove();
    }

    /**
     * Creates a new handler.
     * @param bean bean
     * @param creationalContext creaitonal context
     */
    public RequestScopedBeanInterceptorHandler(OwbBean<?> bean, CreationalContext<?> creationalContext)
    {
        super(bean, creationalContext);
    }
    
    /**
     * {@inheritDoc}
     */
    protected Object getContextualInstance()
    {
        HashMap<OwbBean<?>, CacheEntry> beanMap = cachedInstances.get();
        if (beanMap == null)
        {
            beanMap = new HashMap<OwbBean<?>, CacheEntry>();
            cachedInstances.set(beanMap);
        }

        CacheEntry cachedEntry = beanMap.get(bean);
        if (cachedEntry == null)
        {
            cachedEntry = new CacheEntry();
            cachedEntry.creationalContext = super.getContextualCreationalContext();
            cachedEntry.instance = super.getContextualInstance();
            beanMap.put(bean, cachedEntry);
        }

        return cachedEntry.instance;
    }

    protected CreationalContext<Object> getContextualCreationalContext()
    {
        HashMap<OwbBean<?>, CacheEntry> beanMap = cachedInstances.get();
        if (beanMap != null)
        {
            CacheEntry cachedEntry = beanMap.get(bean);
            if (cachedEntry != null)
            {
                return cachedEntry.creationalContext;
            }
        }

        return super.getContextualCreationalContext();
    }

    /**
     * This will store the cached contextual instance and it's CreationalContext
     */
    private static final class CacheEntry
    {
        CreationalContext<Object> creationalContext;
        Object instance;
    }
}
