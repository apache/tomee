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
package org.apache.webbeans.component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

import javax.enterprise.context.spi.CreationalContext;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.proxy.ResourceProxyHandler;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.api.ResourceReference;

public class ResourceBean<X, T extends Annotation> extends ProducerFieldBean<X>
{
    private ResourceReference<X,T> resourceReference = null;
    
    public ResourceBean(Class<X> returnType, InjectionTargetBean<?> ownerBean,
                        ResourceReference<X, T> resourceReference)
    {
        super(ownerBean, returnType);
        this.resourceReference = resourceReference;
    }

     
    
    @Override
    @SuppressWarnings("unchecked")
    protected X createInstance(CreationalContext<X> creationalContext)
    {
        try
        {
            ResourceInjectionService resourceService = getWebBeansContext().getService(ResourceInjectionService.class);
            X instance = resourceService.getResourceReference(resourceReference);

            if (instance == null || Modifier.isFinal(instance.getClass().getModifiers()))
            {
                return instance;
            }

            X proxyInstance = (X) getWebBeansContext().getProxyFactory().getResourceBeanProxyClass(this).newInstance();
            webBeansContext.getProxyFactory().setHandler(proxyInstance, new ResourceProxyHandler(this,instance));
            return proxyInstance;
        }
        catch (Exception e)
        {
            throw new WebBeansException(e);
        }

    }

    /**
     * Called after deserialization to get a new instance for some type of resource bean instance that are
     * not serializable.
     * 
     * @return a new instance of this resource bean.
     */
    public X getActualInstance() 
    {
        ResourceInjectionService resourceService = getWebBeansContext().getService(ResourceInjectionService.class);
        X instance = resourceService.getResourceReference(resourceReference);
        return instance;
    }
    
    public boolean isPassivationCapable()
    {
        return true;
    }
}
