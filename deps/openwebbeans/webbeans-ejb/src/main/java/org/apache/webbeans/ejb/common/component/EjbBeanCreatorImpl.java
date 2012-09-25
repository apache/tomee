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
package org.apache.webbeans.ejb.common.component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.ObserverMethod;

import org.apache.webbeans.component.creation.AbstractInjectedTargetBeanCreator;
import org.apache.webbeans.ejb.common.util.EjbValidator;

/**
 * EjbBeanCreatorImpl.
 * 
 * @version $Rev$ $Date$
 *
 * @param <T> ejb class type
 */
public class EjbBeanCreatorImpl<T> extends AbstractInjectedTargetBeanCreator<T> implements EjbBeanCreator<T>
{
    public EjbBeanCreatorImpl(BaseEjbBean<T> ejbBean)
    {
        super(ejbBean);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkCreateConditions()
    {        
        EjbValidator.validateDecoratorOrInterceptor(getBean().getReturnType());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void defineScopeType(String errorMessage, boolean allowLazyInit)
    {
        super.defineScopeType(errorMessage, allowLazyInit);

        EjbValidator.validateEjbScopeType(getBean());
        EjbValidator.validateGenericBeanType(getBean().getReturnType(), getBean().getScope());
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void defineApiType()
    {
        if(isDefaultMetaDataProvider())
        {
            List<Class<?>> businessLocals = getBean().getBusinessLocalInterfaces();
            for(Class clazz : businessLocals)
            {
                getBean().addApiType(clazz);
            }
            
            getBean().addApiType(Object.class);
            
            if (getBean().needsBeanLocalViewAddedToTypes())
            {
                for(Class<?> clazz = getBean().getBeanClass(); !clazz.equals(Object.class); clazz = clazz.getSuperclass()) 
                { 
                    getBean().addApiType(clazz);    
                }   
            }
        }
        else
        {  
            Set<Type> types = getAnnotatedType().getTypeClosure();
            getBean().getTypes().addAll(types);
        }
    }
    
    
    
    
    /* (non-Javadoc)
     * @see org.apache.webbeans.component.creation.AbstractInjectedTargetBeanCreator#defineObserverMethods()
     */
    @Override
    public Set<ObserverMethod<?>> defineObserverMethods()
    {
        Set<ObserverMethod<?>> observerMethods = super.defineObserverMethods();
        EjbValidator.validateObserverMethods(getBean(), observerMethods);
        
        return observerMethods;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public BaseEjbBean<T> getBean()
    {
        return BaseEjbBean.class.cast(super.getBean());
    }    
}
