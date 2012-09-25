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
package org.apache.webbeans.el10;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Set;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.el.ELContextStore;

public class EL10Resolver extends ELResolver
{
    private WebBeansContext webBeansContext;

    public EL10Resolver()
    {
        webBeansContext = WebBeansContext.getInstance();
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext arg0, Object arg1)
    {
        
        return null;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext arg0, Object arg1)
    {
        
        return null;
    }

    @Override
    public Class<?> getType(ELContext arg0, Object arg1, Object arg2) throws NullPointerException, PropertyNotFoundException, ELException
    {
        
        return null;
    }

    @Override
    @SuppressWarnings({"unchecked","deprecation"})
    public Object getValue(ELContext context, Object obj, Object property) throws NullPointerException, PropertyNotFoundException, ELException
    {
        // Check if the OWB actually got used in this application
        if (!webBeansContext.getBeanManagerImpl().isInUse())
        {
            return null;
        }
        
        //Bean instance
        Object contextualInstance = null;

        if (obj == null)
        {
            //Name of the bean
            String name = (String) property;
            //Local store, create if not exist
            ELContextStore elContextStore = ELContextStore.getInstance(true);

            contextualInstance = elContextStore.findBeanByName(name);

            if(contextualInstance != null)
            {
                context.setPropertyResolved(true);

                return contextualInstance;
            }

            //Manager instance
            BeanManagerImpl manager = elContextStore.getBeanManager();

            //Get beans
            Set<Bean<?>> beans = manager.getBeans(name);

            //Found?
            if(beans != null && !beans.isEmpty())
            {
                //Managed bean
                Bean<Object> bean = (Bean<Object>)beans.iterator().next();

                if(bean.getScope().equals(Dependent.class))
                {
                    contextualInstance = getDependentContextualInstance(manager, elContextStore, context, bean);
                }
                else
                {
                    // now we check for NormalScoped beans
                    contextualInstance = getNormalScopedContextualInstance(manager, elContextStore, context, bean, name);
                }
            }
        }

        return contextualInstance;
    }

    private Object getNormalScopedContextualInstance(BeanManagerImpl manager, ELContextStore store, ELContext context, Bean<Object> bean, String beanName)
    {
        CreationalContext<Object> creationalContext = manager.createCreationalContext(bean);
        Object contextualInstance = manager.getReference(bean, Object.class, creationalContext);

        if (contextualInstance != null)
        {
            context.setPropertyResolved(true);
            //Adding into store
            store.addNormalScoped(beanName, contextualInstance);
        }

        return contextualInstance;
    }


    private Object getDependentContextualInstance(BeanManagerImpl manager, ELContextStore store, ELContext context, Bean<Object> bean)
    {
        Object contextualInstance = store.getDependent(bean);
        if(contextualInstance != null)
        {
            //Object found on the store
            context.setPropertyResolved(true);
        }
        else
        {
            // If no contextualInstance found on the store
            CreationalContext<Object> creationalContext = manager.createCreationalContext(bean);
            contextualInstance = manager.getReference(bean, Object.class, creationalContext);
            if (contextualInstance != null)
            {
                context.setPropertyResolved(true);
                //Adding into store
                store.addDependent(bean, contextualInstance, creationalContext);
            }
        }
        return contextualInstance;
    }

    @Override
    public boolean isReadOnly(ELContext arg0, Object arg1, Object arg2) throws NullPointerException, PropertyNotFoundException, ELException
    {
        return false;
    }

    @Override
    public void setValue(ELContext arg0, Object arg1, Object arg2, Object arg3) throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException
    {
    }

}
