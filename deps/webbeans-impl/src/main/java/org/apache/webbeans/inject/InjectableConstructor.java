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
package org.apache.webbeans.inject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.exception.WebBeansException;

/**
 * Injects the parameters of the {@link org.apache.webbeans.component.ManagedBean} constructor and returns
 * the created instance.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @since 1.0
 * @see AbstractInjectable
 */
public class InjectableConstructor<T> extends AbstractInjectable
{
    /** Injectable constructor instance */
    protected Constructor<T> con;

    /**
     * Sets the constructor.
     * 
     * @param cons injectable constructor
     */
    public InjectableConstructor(Constructor<T> cons, AbstractOwbBean<?> owner,CreationalContext<?> creationalContext)
    {
        super(owner,creationalContext);
        con = cons;
        injectionMember = con;
    }

    /**
     * Creates the instance from the constructor. Each constructor parameter
     * instance is resolved using the resolution algorithm.
     */
    public T doInjection()
    {
        T instance = null;
        
        List<InjectionPoint> injectedPoints = getInjectedPoints(con);
        List<Object> list = new ArrayList<Object>();
                
        
        for(int i=0;i<injectedPoints.size();i++)
        {
            for(InjectionPoint point : injectedPoints)
            {
                AnnotatedParameter<?> parameter = (AnnotatedParameter<?>)point.getAnnotated();
                if(parameter.getPosition() == i)
                {
                    list.add(inject(point));
                    break;
                }
            }
        }

        try
        {
            if(!con.isAccessible())
            {
                injectionOwnerBean.getWebBeansContext().getSecurityService().doPrivilegedSetAccessible(con, true);
            }
            
            instance = con.newInstance(list.toArray());

        }
        catch (Exception e)
        {
            throw new WebBeansException(e);
        }

        return instance;
    }
}
