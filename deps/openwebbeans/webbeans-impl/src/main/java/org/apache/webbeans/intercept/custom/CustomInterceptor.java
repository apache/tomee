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
package org.apache.webbeans.intercept.custom;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.interceptor.InvocationContext;

import org.apache.webbeans.component.AbstractInjectionTargetBean;
import org.apache.webbeans.intercept.webbeans.WebBeansInterceptor;

public class CustomInterceptor<T> extends WebBeansInterceptor<T> implements Interceptor<T>
{
    private Interceptor<T> actualInterceptor;

    public CustomInterceptor(AbstractInjectionTargetBean<T> delegateBean, Interceptor<T> actualInterceptor)
    {
        super(delegateBean);
        this.actualInterceptor = actualInterceptor;
    }

    @Override
    public Set<Annotation> getInterceptorBindings()
    {        
        return actualInterceptor.getInterceptorBindings();
    }

    @Override
    public Object intercept(InterceptionType type, T instance, InvocationContext ctx)
    {
        
        return actualInterceptor.intercept(type, instance, ctx);
    }

    @Override
    public boolean intercepts(InterceptionType type)
    {
        
        return actualInterceptor.intercepts(type);
    }

    @Override
    public Class<?> getBeanClass()
    {
        
        return actualInterceptor.getBeanClass();
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints()
    {
        
        return actualInterceptor.getInjectionPoints();
    }

    @Override
    public String getName()
    {
        
        return actualInterceptor.getName();
    }

    @Override
    public Set<Annotation> getQualifiers()
    {
        
        return actualInterceptor.getQualifiers();
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        
        return actualInterceptor.getScope();
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes()
    {
        
        return actualInterceptor.getStereotypes();
    }

    @Override
    public Set<Type> getTypes()
    {
        
        return actualInterceptor.getTypes();
    }

    @Override
    public boolean isAlternative()
    {
        
        return actualInterceptor.isAlternative();
    }

    @Override
    public boolean isNullable()
    {
        
        return actualInterceptor.isNullable();
    }

    @Override
    public T create(CreationalContext<T> context)
    {
        
        return actualInterceptor.create(context);
    }

    @Override
    public void destroy(T instance, CreationalContext<T> context)
    {
        actualInterceptor.destroy(instance, context);
        
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((actualInterceptor == null) ? 0 : actualInterceptor.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        CustomInterceptor other = (CustomInterceptor) obj;
        if (actualInterceptor == null)
        {
            if (other.actualInterceptor != null)
            {
                return false;
            }
        }
        else if (!actualInterceptor.equals(other.actualInterceptor))
        {
            return false;
        }
        return true;
    }
}
