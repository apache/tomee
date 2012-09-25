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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.TypeLiteral;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.inject.AbstractInjectable;
import org.apache.webbeans.inject.instance.InstanceFactory;

public class InstanceBean<T> extends AbstractOwbBean<Instance<T>>
{
    // TODO refactor. public static variables are uterly ugly
    public static ThreadLocal<InjectionPoint> local = new ThreadLocal<InjectionPoint>();
    
    @SuppressWarnings("serial")
    public InstanceBean(WebBeansContext webBeansContext)
    {
        super(WebBeansType.INSTANCE, new TypeLiteral<Instance<T>>(){}.getRawType(), webBeansContext);
    }
    
         
    @Override
    protected Instance<T> createInstance(CreationalContext<Instance<T>> creationalContext)
    {
        try
        {
            InjectionPoint injectionPoint = local.get();
            Set<Annotation> qualifiers;
            Type type;
            Class injectionPointClass = null;

            if (injectionPoint != null)
            {
                ParameterizedType injectedType = (ParameterizedType)injectionPoint.getType();
                qualifiers = injectionPoint.getQualifiers();
                type = injectedType.getActualTypeArguments()[0];
                if (injectionPoint.getBean() != null)
                {
                    injectionPointClass = injectionPoint.getBean().getBeanClass();
                }
            }
            else
            {
                qualifiers = getQualifiers();
                type = getReturnType();
            }

            Object ownerInstance = AbstractInjectable.instanceUnderInjection.get();

            Instance<T> instance = InstanceFactory.getInstance(type, injectionPointClass, getWebBeansContext(),
                                                               creationalContext, ownerInstance,
                                                               qualifiers.toArray(new Annotation[qualifiers.size()]));
            
            return instance;
        }
        finally
        {
            local.set(null);
            local.remove();
        }
    }

    /* (non-Javadoc)
    * @see org.apache.webbeans.component.AbstractOwbBean#isPassivationCapable()
    */
    @Override
    public boolean isPassivationCapable()
    {
        return true;
    }

}
