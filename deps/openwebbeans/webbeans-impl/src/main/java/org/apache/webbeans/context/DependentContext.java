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

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

/**
 * Defines the component {@link javax.enterprise.context.Dependent} context.
 * <p>
 * Each web beans component has a dependent context, that saves its dependent
 * objects. Dependent context is destroyed at the end of the component
 * destruction or its dependent objects are destroyed by the container at any
 * time that the dependent object is no longer alive.
 * </p>
 * 
 * <p>
 * Dependent context is always active.
 * </p>
 * 
 * @version $Rev$ $Date$
 */
public class DependentContext extends AbstractContext
{
    private static final long serialVersionUID = 8225241216057316441L;

    /**
     * Creats a new instance of dependent context.
     */
    public DependentContext()
    {
        super(Dependent.class);
        active = true;
    }
    
    

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    protected <T> T getInstance(Contextual<T> component,CreationalContext<T> creationalContext)
    {
        T object = null;
        
        if(creationalContext == null)
        {
            return null;
        }
        else
        {
            object = component.create(creationalContext);   
        }
        

        return object;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setComponentInstanceMap()
    {

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(Contextual<T> component)
    {
        return null;
    }

}
