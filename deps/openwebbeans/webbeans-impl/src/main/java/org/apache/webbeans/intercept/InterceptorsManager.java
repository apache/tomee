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
package org.apache.webbeans.intercept;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.interceptor.Interceptor;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.util.Asserts;

public class InterceptorsManager
{
    private List<Class<?>> enabledInterceptors = new CopyOnWriteArrayList<Class<?>>();
    private final WebBeansContext webBeansContext;

    private final BeanManagerImpl manager;

    public InterceptorsManager(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
        manager = webBeansContext.getBeanManagerImpl();
    }

    public void addNewInterceptor(Class<?> interceptorClazz)
    {
        Asserts.nullCheckForClass(interceptorClazz, "interceptorClazz can not be null");

        if (!enabledInterceptors.contains(interceptorClazz))
        {
            enabledInterceptors.add(interceptorClazz);
        }
    }

    public int compare(Class<?> src, Class<?> target)
    {
        Asserts.assertNotNull(src, "src parameter can not be  null");
        Asserts.assertNotNull(target, "target parameter can not be null");

        int srcIndex = enabledInterceptors.indexOf(src);
        int targetIndex = enabledInterceptors.indexOf(target);

        if (srcIndex == -1 || targetIndex == -1)
        {
            throw new IllegalArgumentException("One of the compare class of the list : [" + src.getName() + "," + target.getName() + "]"
                                               + " is not contained in the enabled interceptors list!");
        }

        if (srcIndex == targetIndex)
        {
            return 0;
        }
        else if (srcIndex < targetIndex)
        {
            return -1;
        }
        else
        {
            return 1;
        }
    }

    public boolean isInterceptorEnabled(Class<?> interceptorClazz)
    {
        Asserts.nullCheckForClass(interceptorClazz, "interceptorClazz can not be null");

        return enabledInterceptors.contains(interceptorClazz);
    }
    
    public void validateInterceptorClasses()
    {
        for(Class<?> interceptorClass : enabledInterceptors)
        {
            AnnotatedType<?> annotatedType = webBeansContext.getAnnotatedElementFactory().getAnnotatedType(interceptorClass);

            //Validate decorator classes
            if(!annotatedType.isAnnotationPresent(Interceptor.class) && !manager.containsCustomInterceptorClass(interceptorClass))
            {
                throw new WebBeansConfigurationException("Given class : " + interceptorClass + " is not a interceptor class");
            }   
        }                
    }    
}
