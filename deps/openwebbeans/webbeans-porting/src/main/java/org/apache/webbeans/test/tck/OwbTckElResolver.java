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
package org.apache.webbeans.test.tck;

import java.util.Set;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.PropertyNotFoundException;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.Bean;

import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.el.ELContextStore;
import org.apache.webbeans.el.WebBeansELResolver;

/**
 * Exist for TCK standalone EL resolver.
 * <p>
 * In standalone case, there is no JSF 
 * related artifacts, therefore {@link WebBeansELResolver}
 * does not work.
 * </p>
 */
public class OwbTckElResolver extends WebBeansELResolver
{
    /**
     * {@inheritDoc}
     */    
    @Override
    @SuppressWarnings({"unchecked"})
    public Object getValue(ELContext context, Object obj, Object property) throws NullPointerException, PropertyNotFoundException, ELException
    {
        //Bean instance
        Object contextualInstance = null;
        ELContextStore elContextStore = null;
        if (obj == null)
        {
            //Name of the bean
            String name = (String) property;
            //Local store, create if not exist
            elContextStore = ELContextStore.getInstance(true);

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

}
