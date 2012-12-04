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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.enterprise.context.spi.Contextual;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * This class contains creation information about &#0064;Dependent scoped 
 * contextual instances.
 */
public class DependentCreationalContext<S> implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Contextual<S> contextual;
    
    private DependentType dependentType;
    
    private Object instance;
    
    /**
     * @return the instance
     */
    public Object getInstance()
    {
        return instance;
    }


    /**
     * @param instance the instance to set
     */
    public void setInstance(Object instance)
    {
        this.instance = instance;
    }


    public enum DependentType
    {
        DECORATOR,
        INTERCEPTOR,
        BEAN
    }
    
    public DependentCreationalContext(Contextual<S> contextual)
    {
        this.contextual = contextual;
    }
    
    
    /**
     * @return the dependentType
     */
    public DependentType getDependentType()
    {
        return dependentType;
    }



    /**
     * @param dependentType the dependentType to set
     */
    public void setDependentType(DependentType dependentType)
    {
        this.dependentType = dependentType;
    }

    /**
     * @return the contextual
     */
    public Contextual<S> getContextual()
    {
        return contextual;
    }

    private void writeObject(ObjectOutputStream s)
    throws IOException
    {
        s.writeObject(dependentType);
        s.writeObject(instance);

        //Write for contextual
        if (contextual != null)
        {
            String id = WebBeansUtil.isPassivationCapable(contextual);
            if (id != null)
            {
                s.writeObject(id);
            }
            else
            {
                throw new NotSerializableException("cannot serialize " + contextual.toString());
            }
            
        }
        else
        {
            s.writeObject(null);
        }
    }


    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream s)
    throws IOException, ClassNotFoundException
    {
        dependentType = (DependentType) s.readObject();
        instance = s.readObject();

        //Read for contextual
        String id = (String) s.readObject();
        if (id != null)
        {
            WebBeansContext webBeansContext = WebBeansContext.currentInstance();
            contextual = (Contextual<S>) webBeansContext.getBeanManagerImpl().getPassivationCapableBean(id);
        }
    }

}
