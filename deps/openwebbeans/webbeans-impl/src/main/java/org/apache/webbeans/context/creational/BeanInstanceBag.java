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
package org.apache.webbeans.context.creational;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BeanInstanceBag<T> implements Serializable
{
    private static final long serialVersionUID = 1656996021599122499L;
    private final CreationalContext<T> beanCreationalContext;
    
    private T beanInstance;
    
    private final Lock lock = new ReentrantLock();
    
    public BeanInstanceBag(CreationalContext<T> beanCreationalContext)
    {
        this.beanCreationalContext = beanCreationalContext;
    }

    /**
     * @return the beanCreationalContext
     */
    public CreationalContext<T> getBeanCreationalContext()
    {
        return beanCreationalContext;
    }
    
    

    /**
     * @param beanInstance the beanInstance to set
     */
    public void setBeanInstance(T beanInstance)
    {
        this.beanInstance = beanInstance;
    }

    /**
     * @return the beanInstance
     */
    public T getBeanInstance()
    {
        return beanInstance;
    }

    /**
     * Create the contextual instance in a thread safe fashion
     * @param contextual
     * @return the single contextual instance for the context
     */
    public T create(Contextual<T> contextual)
    {
        try
        {
            lock.lock();
            
            // we need to check again, maybe we got blocked by a previous invocation
            if (beanInstance == null)
            {
                beanInstance = contextual.create(beanCreationalContext);
            }
            
        }
        finally
        {
            lock.unlock();
        }
        
        return beanInstance; 
    }

    @Override
    public String toString()
    {
        return "Bag:" + beanCreationalContext + ", Instance: " + beanInstance;
    }
}
