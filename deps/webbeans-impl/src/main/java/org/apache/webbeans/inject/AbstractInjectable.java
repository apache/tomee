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

import java.io.Serializable;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.enterprise.inject.IllegalProductException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Provider;

import org.apache.webbeans.component.AbstractProducerBean;
import org.apache.webbeans.component.EventBean;
import org.apache.webbeans.component.InjectionPointBean;
import org.apache.webbeans.component.InstanceBean;
import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.container.InjectionResolver;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.context.creational.DependentCreationalContext;
import org.apache.webbeans.util.ClassUtil;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * Abstract implementation of the {@link Injectable} contract.
 * 
 * <p>
 * Do actual injection via {@link AbstractInjectable#inject(InjectionPoint)}
 * </p>
 * 
 * @see InjectableField
 * @see InjectableConstructor
 * @see InjectableMethods
 */
public abstract class AbstractInjectable implements Injectable
{
    /** Owner bean of the injection point*/
    protected OwbBean<?> injectionOwnerBean;
    
    /**Creational context instance that is passed to bean's create*/
    protected CreationalContext<?> injectionOwnerCreationalContext;
    
    /**Field, method or constructor injection*/
    protected Member injectionMember;
    
    //X TODO refactor. public static variables are utterly ugly
    public static ThreadLocal<Object> instanceUnderInjection = new ThreadLocal<Object>();

    //X TODO this MUST NOT be public! 
    public static ThreadLocal<List<DependentCreationalContext<Object>>> dependentInstanceOfProducerMethods = 
        new ThreadLocal<List<DependentCreationalContext<Object>>>();

    /**
     * Creates a new injectable.
     * 
     * @param injectionOwnerBean owner bean
     * @param injectionOwnerCreationalContext creational context instance
     */
    protected AbstractInjectable(OwbBean<?> injectionOwnerBean, CreationalContext<?> injectionOwnerCreationalContext)
    {
        this.injectionOwnerBean = injectionOwnerBean;
        this.injectionOwnerCreationalContext = injectionOwnerCreationalContext;
    }

    /**
     * Gets the injected bean instance in its scoped context. 
     * @param injectionPoint injection point definition  
     * @return current bean instance in the resolved bean scope
     */
    public <T> Object inject(InjectionPoint injectionPoint)
    {
        Object injected;
        BeanManagerImpl beanManager = injectionOwnerBean.getWebBeansContext().getBeanManagerImpl();

        //Injected contextual beam
        InjectionResolver instance = beanManager.getInjectionResolver();

        Bean<?> injectedBean = instance.getInjectionPointBean(injectionPoint);
        if(isInstanceProviderInjection(injectionPoint))
        {
            InstanceBean.local.set(injectionPoint);
        }
        
        else if(isEventProviderInjection(injectionPoint))
        {
            EventBean.local.set(injectionPoint);
        }        
        
        boolean injectionPointBeanLocalSetOnStack = false;
        try 
        {
            //Injection for dependent instance InjectionPoint fields
            boolean dependentProducer = false;
            if(WebBeansUtil.isDependent(injectedBean))
            {
                if(!InjectionPoint.class.isAssignableFrom(ClassUtil.getClass(injectionPoint.getType())))
                {
                    injectionPointBeanLocalSetOnStack = InjectionPointBean.setThreadLocal(injectionPoint);
                }

                if(!injectionPoint.isTransient())
                {
                    if(injectedBean instanceof AbstractProducerBean)
                    {
                        if(injectionOwnerBean.isPassivationCapable())
                        {
                            dependentProducer = true;   
                        }
                    }
                }
            }        

            //Gets injectable reference for injected bean
            injected = beanManager.getInjectableReference(injectionPoint, injectionOwnerCreationalContext);

            /*X TODO see spec issue CDI-140 */
            if(dependentProducer)
            {
                if(injected != null && !Serializable.class.isAssignableFrom(injected.getClass()))
                {
                    throw new IllegalProductException("If a producer method or field of scope @Dependent returns an serializable object for injection " +
                                                    "into an injection point "+ injectionPoint +" that requires a passivation capable dependency");
                }
            }

            // add this dependent into bean dependent list
            if (!WebBeansUtil.isStaticInjection(injectionPoint) && WebBeansUtil.isDependent(injectedBean))
            {
                if(instanceUnderInjection.get() != null)
                {
                    ((CreationalContextImpl<?>) injectionOwnerCreationalContext).addDependent(instanceUnderInjection.get(),injectedBean, injected);
                }
            }
        } 
        finally
        {
            if (injectionPointBeanLocalSetOnStack)
            {
                InjectionPointBean.unsetThreadLocal();
            }
        }
        

        return injected;
    }
    
        
    /**
     * Returns injection points related with given member type of the bean.
     * @param member java member
     * @return injection points related with given member type
     */
    protected List<InjectionPoint> getInjectedPoints(Member member)
    {
        List<InjectionPoint> injectedFields = injectionOwnerBean.getInjectionPoint(member);
        
        return injectedFields;

    }

    private boolean isInstanceProviderInjection(InjectionPoint injectionPoint)
    {
        Type type = injectionPoint.getType();
        
        if (type instanceof ParameterizedType)
        {
            ParameterizedType pt = (ParameterizedType) type;            
            Class<?> clazz = (Class<?>) pt.getRawType();
            
            if(Provider.class.isAssignableFrom(clazz))
            {
                return true;
            }
        }
        
        return false;
    }
    
    
    private boolean isEventProviderInjection(InjectionPoint injectionPoint)
    {
        Type type = injectionPoint.getType();
        
        if (type instanceof ParameterizedType)
        {
            ParameterizedType pt = (ParameterizedType) type;            
            Class<?> clazz = (Class<?>) pt.getRawType();
            
            if(clazz.isAssignableFrom(Event.class))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets the component.
     * 
     * @return the component
     */
    public OwbBean<?> getInjectionOwnerComponent()
    {
        return injectionOwnerBean;
    }

}
