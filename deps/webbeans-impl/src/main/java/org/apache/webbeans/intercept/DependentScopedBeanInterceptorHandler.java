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
package org.apache.webbeans.intercept;

import java.lang.reflect.Method;
import java.util.List;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InterceptionType;

import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.context.creational.CreationalContextImpl;

/**
 * Dependent scoped beans interceptor handler.
 * @version $Rev$ $Date$
 *
 */
public class DependentScopedBeanInterceptorHandler extends InterceptorHandler
{
    /**default servial id*/
    private static final long serialVersionUID = 1L;
    
    /**Bean instance*/
    private Object actualInstance;
    
    /**Creaitonal context*/
    private CreationalContext<?> creationalContext;
    
    /**
     * Creates a new instance of handler.
     * @param bean dependent bean 
     * @param instance bean instance
     * @param creationalContext creational context
     */
    public DependentScopedBeanInterceptorHandler(OwbBean<?> bean, Object instance, CreationalContext<?> creationalContext)
    {
        super(bean);
        actualInstance = instance;
        this.creationalContext = creationalContext;
        
        if(creationalContext instanceof CreationalContextImpl)
        {
            //If this creational context is owned by this DependentBean, add it
            CreationalContextImpl<?> ccImpl = (CreationalContextImpl<?>)creationalContext;
            if(ccImpl.getBean() != null && ccImpl.getBean().equals(bean))
            {
                //Owner of the dependent is itself
                ccImpl.addDependent(instance, bean, instance);
            }            
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object invoke(Object instance, Method method, Method proceed, Object[] arguments) throws Throwable
    {
        return invoke(instance, method, arguments);
    }

    public Object invoke(Object instance, Method method, Object[] arguments) throws Throwable
    {
        return super.invoke(actualInstance, method, arguments, (CreationalContextImpl<?>)creationalContext);
    }
    
    /**
     * {@inheritDoc}
     */
    protected Object callAroundInvokes(Method proceed, Object[] arguments, List<InterceptorData> stack) throws Exception
    {
        InvocationContextImpl impl = new InvocationContextImpl(webBeansContext, bean, actualInstance,proceed, arguments, stack, InterceptionType.AROUND_INVOKE);
        impl.setCreationalContext(creationalContext);
        
        return impl.proceed();
    }
}
