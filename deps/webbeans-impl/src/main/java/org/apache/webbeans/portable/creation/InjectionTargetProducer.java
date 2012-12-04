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
package org.apache.webbeans.portable.creation;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;

import org.apache.webbeans.component.EnterpriseBeanMarker;
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.inject.AbstractInjectable;
import org.apache.webbeans.proxy.ProxyFactory;

/**
 * InjectionTargetProducer implementation.
 * 
 * @version $Rev: 1385030 $ $Date: 2012-09-15 10:41:29 +0200 (sam., 15 sept. 2012) $
 *
 * @param <T> bean type info
 */
@SuppressWarnings("unchecked")
public class InjectionTargetProducer<T> extends AbstractProducer<T> implements InjectionTarget<T>
{
    /**
     * Creates a new injection target producer.
     * @param bean injection target bean
     */
    public InjectionTargetProducer(InjectionTargetBean<T> bean)
    {
        super(bean);
    }
        
    /**
     * {@inheritDoc}
     */
    public void inject(T instance, CreationalContext<T> ctx)
    {
        if(!(ctx instanceof CreationalContextImpl))
        {
            ctx = bean.getWebBeansContext().getCreationalContextFactory().wrappedCreationalContext(ctx, bean);
        }
        
        Object oldInstanceUnderInjection = AbstractInjectable.instanceUnderInjection.get();
        boolean isInjectionToAnotherBean = false;
        try
        {
            Contextual<?> contextual = null;
            if(ctx instanceof CreationalContextImpl)
            {
                contextual = ((CreationalContextImpl)ctx).getBean();
                isInjectionToAnotherBean = contextual == getBean(InjectionTargetBean.class) ? false : true;
            }
            
            if(!isInjectionToAnotherBean)
            {
                AbstractInjectable.instanceUnderInjection.set(instance);   
            }
                        
            InjectionTargetBean<T> bean = getBean(InjectionTargetBean.class);
            
            if(!(bean instanceof EnterpriseBeanMarker))
            {
                //GE: Currently we have a proxy for DependentScoped beans
                //that has an interceptor or decroator. This means that
                //injection will be occured on Proxy instances that are 
                //not correct. Injection must be on actual dependent
                //instance,so not necessary to inject on proxy
                final ProxyFactory proxyFactory = this.bean.getWebBeansContext().getProxyFactory();
                if(bean.getScope() == Dependent.class && proxyFactory.isProxyInstance(instance))
                {
                    return;
                }
                
                bean.injectResources(instance, ctx);
                bean.injectSuperFields(instance, ctx);
                bean.injectSuperMethods(instance, ctx);
                bean.injectFields(instance, ctx);
                bean.injectMethods(instance, ctx);            
            }                    
        }
        finally
        {
            if(oldInstanceUnderInjection != null)
            {
                AbstractInjectable.instanceUnderInjection.set(oldInstanceUnderInjection);   
            }
            else
            {
                AbstractInjectable.instanceUnderInjection.set(null);
                AbstractInjectable.instanceUnderInjection.remove();
            }
        }
        
    }
    
    /**
     * {@inheritDoc}
     */
    public void postConstruct(T instance)
    {
        InjectionTargetBean<T> bean = getBean(InjectionTargetBean.class);    
        if(!(bean instanceof EnterpriseBeanMarker))
        {
            bean.postConstruct(instance, creationalContext);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void preDestroy(T instance)
    {
        InjectionTargetBean<T> bean = getBean(InjectionTargetBean.class);
        bean.destroyCreatedInstance(instance, creationalContext);
    }

}
