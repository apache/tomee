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

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Producer;

/**
 * Reponsible for producing, injection etc.
 * of beans. Delegate operations to the wrapped
 * instance.
 * <p>
 * Container uses final InjectionTarget or Producer instance
 * to use while doing operations on beans.
 * </p> 
 * @version $Rev$ $Date$
 *
 * @param <T>
 */
public class InjectionTargetWrapper<T> implements InjectionTarget<T>
{
    /**Wrapped injection target*/
    //This defaults to InjectionTargetProducer
    private InjectionTarget<T> wrapped = null;
    
    /**Wrapped producer*/
    //This default to ProducerBeansProducer
    private Producer<T> wrappedProducer = null;
    
    /**
     * New instance.
     * @param wrapped wrapped injection target
     */
    public InjectionTargetWrapper(InjectionTarget<T> wrapped)
    {
        this.wrapped = wrapped;
    }

    
    /**
     * New instance.
     * @param wrappedProducer wrapped producer.
     */
    public InjectionTargetWrapper(Producer<T> wrappedProducer)
    {
        this.wrappedProducer = wrappedProducer;
    }

    /**
     * {@inheritDoc}
     */
    public void inject(T instance, CreationalContext<T> ctx)
    {
        if(wrapped != null)
        {
            wrapped.inject(instance, ctx);   
        }
    }

    /**
     * {@inheritDoc}
     */
    public void postConstruct(T instance)
    {
        if(wrapped != null)
        {
            wrapped.postConstruct(instance);   
        }
    }

    /**
     * {@inheritDoc}
     */    
    public void preDestroy(T instance)
    {
        if(wrapped != null)
        {
            wrapped.preDestroy(instance);   
        }
    }

    /**
     * {@inheritDoc}
     */    
    public void dispose(T instance)
    {
        if(wrappedProducer != null)
        {
            wrappedProducer.dispose(instance);   
        }
    }

    /**
     * {@inheritDoc}
     */    
    public Set<InjectionPoint> getInjectionPoints()
    {
        if(wrappedProducer != null)
        {
            return wrappedProducer.getInjectionPoints();
        }
        
        return wrapped.getInjectionPoints();
    }

    /**
     * {@inheritDoc}
     */    
    public T produce(CreationalContext<T> creationalContext)
    {
        if(wrappedProducer != null)
        {
            return wrappedProducer.produce(creationalContext);
        }
        
        return wrapped.produce(creationalContext);
    }
}
