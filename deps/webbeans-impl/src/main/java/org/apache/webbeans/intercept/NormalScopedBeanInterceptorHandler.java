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
import java.util.concurrent.CopyOnWriteArrayList;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.UnproxyableResolutionException;
import javax.enterprise.inject.spi.InterceptionType;

import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.context.AbstractContext;
import org.apache.webbeans.context.creational.CreationalContextFactory;
import org.apache.webbeans.context.creational.CreationalContextImpl;


/**
 * Normal scoped beans interceptor handler.
 * @version $Rev: 1384120 $ $Date: 2012-09-12 22:52:57 +0200 (mer., 12 sept. 2012) $
 *
 */
@SuppressWarnings("unchecked")
public class NormalScopedBeanInterceptorHandler extends InterceptorHandler 
{
    /**Serial id*/
    private static final long serialVersionUID = 1L;

    /** this stores the {@link java.lang.reflect.Method#hashCode()} of intercepted methods */
    private CopyOnWriteArrayList<Integer> cachedInterceptedMethods = new CopyOnWriteArrayList<Integer>();

    /**
     * Creates a new bean instance
     * @param bean bean 
     * @param creationalContext creational context
     */
    public NormalScopedBeanInterceptorHandler(OwbBean<?> bean, CreationalContext<?> creationalContext)
    {
        super(bean);    
        
        //Initiate bean for saving creational context instance
        initiateBeanBag((OwbBean<Object>)bean, (CreationalContext<Object>)creationalContext);
    }
    
    private void initiateBeanBag(OwbBean<Object> bean, CreationalContext<Object> creationalContext)
    {
        try
        {
            Context webbeansContext = getBeanManager().getContext(bean.getScope());
            if (webbeansContext instanceof AbstractContext)
            {
                AbstractContext owbContext = (AbstractContext)webbeansContext;
                owbContext.initContextualBag(bean, creationalContext);
            }            
        }
        catch(ContextNotActiveException e)
        {
            //Nothing
        }
    }

    @Override
    protected boolean isNotInterceptedOrDecoratedMethod(Method method)
    {
        int currentHash = method.hashCode();
        if (cachedInterceptedMethods.contains(currentHash))
        {
            return true;
        }
        return false;
    }

    @Override
    protected void setNotInterceptedOrDecoratedMethod(Method method)
    {
        Integer hashCode = method.hashCode();
        if (!cachedInterceptedMethods.contains(hashCode))
        {
            cachedInterceptedMethods.add(hashCode);
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
        //Get instance from context
        Object webbeansInstance = getContextualInstance();
        
        //Call super
        return super.invoke(webbeansInstance, method, arguments, (CreationalContextImpl<?>) getContextualCreationalContext());
    }
        
    /**
     * {@inheritDoc}
     */
    protected Object callAroundInvokes(Method proceed, Object[] arguments, List<InterceptorData> stack) throws Exception
    {
        InvocationContextImpl impl = new InvocationContextImpl(webBeansContext, bean, getContextualInstance(),
                                                               proceed, arguments, stack, InterceptionType.AROUND_INVOKE);
        impl.setCreationalContext(getContextualCreationalContext());

        return impl.proceed();

    }
    
    
    /**
     * Gets instance from context.
     * @return the underlying contextual instance, either cached or resolved from the context 
     */
    protected Object getContextualInstance()
    {
        Object webbeansInstance;

        //Context of the bean
        Context webbeansContext = getBeanManager().getContext(bean.getScope());
        
        //Already saved in context?
        webbeansInstance = webbeansContext.get(bean);
        if (webbeansInstance != null)
        {
            // voila, we are finished if we found an existing contextual instance
            return webbeansInstance;
        }

        // finally, we create a new contextual instance
        webbeansInstance = webbeansContext.get((Contextual<Object>) bean, getContextualCreationalContext());

        if (webbeansInstance == null)
        {
            throw new UnproxyableResolutionException("Cannot find a contextual instance of bean " + bean.toString());
        }
        return webbeansInstance;
    }
    
    protected CreationalContext<Object> getContextualCreationalContext()
    {
        CreationalContext<Object> creationalContext = null;
        
        OwbBean<Object> contextual = (OwbBean<Object>) bean;
        //Context of the bean
        Context webbeansContext = getBeanManager().getContext(bean.getScope());
        CreationalContextFactory contextFactory = bean.getWebBeansContext().getCreationalContextFactory();
        if (webbeansContext instanceof AbstractContext)
        {
            AbstractContext owbContext = (AbstractContext)webbeansContext;
            creationalContext = owbContext.getCreationalContext(contextual);

            //No creational context means that no BeanInstanceBag
            //Actually this can be occurs like scenarions
            //@SessionScoped bean injected into @ApplicationScopedBean
            //And session is destroyed and restarted but proxy still
            //contained in @ApplicationScopedBean
            if(creationalContext == null)
            {
                creationalContext = contextFactory.getCreationalContext(contextual);
                owbContext.initContextualBag((OwbBean<Object>) bean, creationalContext);
            }
        }

        // for 3rd party contexts (actually all contexts provided via portable extensions)
        // we don't have all the stuff of AbstractContext available
        // In this case we may safely simply create a fresh CreationalContext, because
        // if an 'old' contextual instance exists, it would have been found by the
        // preceding call to Context.get(Contextual) (without any CreationalContext)
        if(creationalContext == null)
        {
            creationalContext = contextFactory.getCreationalContext(contextual);
        }

        return creationalContext;
    }
}
