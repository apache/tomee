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
package org.apache.webbeans.portable.creation;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;

import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.context.creational.CreationalContextImpl;

/**
 * Abstract implementation of {@link Producer} contract.
 * 
 * @version $Rev$ $Date$
 *
 * @param <T> bean type info
 */
public abstract class AbstractProducer<T> implements Producer<T> 
{
    /**Bean instance*/
    protected OwbBean<T> bean;

    /**Passing creational context*/
    protected CreationalContext<T> creationalContext = null;
    
    /**
     * Create a new producer with given bean.
     * 
     * @param bean bean instance
     */
    protected AbstractProducer(OwbBean<T> bean)
    {
        this.bean = bean;
    }

    /**
     * {@inheritDoc}
     */
    public Set<InjectionPoint> getInjectionPoints()
    {
        return bean.getInjectionPoints();
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public T produce(CreationalContext<T> creationalContext)
    {
        T instance;
        if(!(creationalContext instanceof CreationalContextImpl))
        {
            creationalContext = bean.getWebBeansContext().getCreationalContextFactory().wrappedCreationalContext(creationalContext, bean);
        }
        
        //Save it
        this.creationalContext = creationalContext;
        
        //Create an instance of the bean
        instance = bean.createNewInstance(this.creationalContext);
                
        return instance; 
    }
    
    /**
     * {@inheritDoc}
     */
    public void dispose(T instance)
    {
        bean.destroyCreatedInstance(instance, creationalContext);
    }

    /**
     * Returns actual bean instance.
     * 
     * @param <X> bean type info
     * @param clazz bean type class
     * @return actual bean
     */
    protected <X> X getBean(Class<X> clazz)
    {
        return clazz.cast(bean);
    }
}
