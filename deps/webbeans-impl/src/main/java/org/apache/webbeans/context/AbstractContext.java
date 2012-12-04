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
package org.apache.webbeans.context;

import java.io.*;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.apache.webbeans.container.SerializableBean;
import org.apache.webbeans.container.SerializableBeanVault;
import org.apache.webbeans.context.creational.BeanInstanceBag;

/**
 * Abstract implementation of the {@link WebBeansContext} interfaces.
 * 
 * @see javax.enterprise.context.spi.Context
 * @see RequestContext
 * @see DependentContext
 * @see SessionContext
 * @see ApplicationContext
 * @see ConversationContext
 */
public abstract class AbstractContext implements WebBeansContext, Serializable
{
    private static final long serialVersionUID = 2357678967444477818L;
    /**Context status, active or not*/
    protected volatile boolean active;


    /**Context contextual instances*/
    protected Map<Contextual<?>, BeanInstanceBag<?>> componentInstanceMap = null;

    /**Contextual Scope Type*/
    protected Class<? extends Annotation> scopeType;
    
    /**
     * Creates a new context instance
     */
    protected AbstractContext()
    {

    }
    

    public <T> void initContextualBag(Contextual<T> contextual, CreationalContext<T> creationalContext)
    {
        createContextualBag(contextual, creationalContext);
    }

    @SuppressWarnings("unchecked")
    private <T> void createContextualBag(Contextual<T> contextual, CreationalContext<T> creationalContext)
    {
        BeanInstanceBag<T> bag = new BeanInstanceBag<T>(creationalContext);
        
        if(componentInstanceMap instanceof ConcurrentMap)
        {
            T exist = (T) ((ConcurrentMap) componentInstanceMap).putIfAbsent(contextual, bag);
            //no instance
            if(exist == null)
            {
                componentInstanceMap.put(contextual, bag);
            }
        }
        else
        {
            componentInstanceMap.put(contextual, bag);
        }                
    }
    
    /**
     * Creates a new context with given scope type.
     * 
     * @param scopeType context scope type
     */
    protected AbstractContext(Class<? extends Annotation> scopeType)
    {
        this.scopeType = scopeType;
        setComponentInstanceMap();

    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Contextual<T> component)
    {
        checkActive();

        BeanInstanceBag bag = componentInstanceMap.get(component);
        
        if(bag != null)
        {
            return (T) bag.getBeanInstance();
        }
        
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext)
    {
        checkActive();
        
        return getInstance(contextual, creationalContext);
    }

    /**
     * {@inheritDoc} 
     */
    @SuppressWarnings("unchecked")
    protected <T> T getInstance(Contextual<T> contextual, CreationalContext<T> creationalContext)
    {
        T instance;
        
        //Look for bag
        BeanInstanceBag<T> bag = (BeanInstanceBag<T>)componentInstanceMap.get(contextual);        
        if(bag == null)
        {
            createContextualBag(contextual, creationalContext);
            bag = (BeanInstanceBag<T>)componentInstanceMap.get(contextual);
        }

        //Look for instance
        instance = bag.getBeanInstance();
        if (instance != null)
        {
            return instance;
        }

        else
        {
            if(creationalContext == null)
            {
                return null;
            }
            
            else
            {                
                instance = bag.create(contextual);    
            }
        }

        return  instance;
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> CreationalContext<T> getCreationalContext(Contextual<T> contextual)
    {
        BeanInstanceBag<?> bag = componentInstanceMap.get(contextual);
        if (bag != null)
        {
            return (CreationalContext<T>) bag.getBeanCreationalContext();
        }
        
        return null;
    }

    /**
     * Destroy the given web beans component instance.
     * 
     * @param <T>
     * @param component web beans component
     * @param instance component instance
     */
    private <T> void destroyInstance(Contextual<T> component, T instance,CreationalContext<T> creationalContext)
    {
        //Destroy component
        component.destroy(instance,creationalContext);
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void destroy()
    {
        Set<Entry<Contextual<?>, BeanInstanceBag<?>>> entrySet = componentInstanceMap.entrySet();
        Iterator<Entry<Contextual<?>, BeanInstanceBag<?>>> it = entrySet.iterator();

        Contextual<?> contextual;
        while (it.hasNext())
        {
            contextual = it.next().getKey();
            
            BeanInstanceBag<?> instance = componentInstanceMap.get(contextual);
            //Get creational context
            CreationalContext<Object> cc = (CreationalContext<Object>)instance.getBeanCreationalContext();

            //Destroy instance
            destroyInstance((Contextual<Object>) contextual, instance.getBeanInstance(), cc);
        }
        
        //Clear context map
        componentInstanceMap.clear();
        setActive(false);
    }

    /**
     * Gets context active flag.
     * 
     * @return active flag
     */
    public boolean isActive()
    {
        return active;
    }

    /**
     * Set component active flag.
     * 
     * @param active active flag
     */
    public void setActive(boolean active)
    {
        this.active = active;
    }

    /**
     * {@inheritDoc}
     */
    public Class<? extends Annotation> getScope()
    {

        return scopeType;
    }

    /**
     * {@inheritDoc}
     */
    protected abstract void setComponentInstanceMap();
    
    /**
     * Check that context is active or throws exception.
     */
    protected void checkActive()
    {
        if (!active)
        {
            throw new ContextNotActiveException("WebBeans context with scope annotation @" + getScope().getName() + " is not active with respect to the current thread");
        }        
    }

    /**
     * Write Object.
     */
    private void writeObject(ObjectOutputStream s)
    throws IOException
    {
        s.writeObject(scopeType);

        // we need to repack the Contextual<T> from the componentInstanceMap into Serializable ones
        if (componentInstanceMap != null)
        {
            SerializableBeanVault sbv = org.apache.webbeans.config.WebBeansContext.getInstance().getSerializableBeanVault();

            Map<Contextual<?>, BeanInstanceBag<?>> serializableInstanceMap =
                    new HashMap<Contextual<?>, BeanInstanceBag<?>>();

            for (Map.Entry<Contextual<?>, BeanInstanceBag<?>> componentInstanceMapEntry : componentInstanceMap.entrySet())
            {
                serializableInstanceMap.put(sbv.getSerializableBean(componentInstanceMapEntry.getKey()),
                                            componentInstanceMapEntry.getValue());
            }
            
            s.writeObject(serializableInstanceMap);
        }
        else
        {
            s.writeObject(null);
        }

    }

    /**
     * Read object.
     */
    private void readObject(ObjectInputStream s)
    throws IOException, ClassNotFoundException
    {
        scopeType = (Class<? extends Annotation>) s.readObject();

        HashMap<Contextual<?>, BeanInstanceBag<?>> serializableInstanceMap =
                (HashMap<Contextual<?>, BeanInstanceBag<?>>) s.readObject();

        if (serializableInstanceMap != null)
        {
            setComponentInstanceMap();
            if (componentInstanceMap == null)
            {
                throw new NotSerializableException("componentInstanceMap not initialized!");
            }

            for (Map.Entry<Contextual<?>, BeanInstanceBag<?>> serializableInstanceMapEntry : serializableInstanceMap.entrySet())
            {
                Contextual<?> bean = serializableInstanceMapEntry.getKey();
                if (bean instanceof SerializableBean)
                {
                    componentInstanceMap.put(((SerializableBean<?>)bean).getBean(), serializableInstanceMapEntry.getValue());
                }
                else
                {
                    componentInstanceMap.put(bean, serializableInstanceMapEntry.getValue());
                }
            }
        }
    }
}
