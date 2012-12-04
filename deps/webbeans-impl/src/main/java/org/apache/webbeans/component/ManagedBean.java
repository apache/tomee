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
package org.apache.webbeans.component;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.annotation.Annotation;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Decorator;

import org.apache.webbeans.component.creation.ManagedBeanCreatorImpl;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.decorator.AbstractDecoratorMethodHandler;
import org.apache.webbeans.inject.InjectableConstructor;
import org.apache.webbeans.intercept.InterceptorData;

/**
 * Managed bean implementation of the {@link javax.enterprise.inject.spi.Bean}.
 * 
 * @version $Rev: 1385030 $ $Date: 2012-09-15 10:41:29 +0200 (sam., 15 sept. 2012) $
 */
public class ManagedBean<T> extends AbstractInjectionTargetBean<T> implements InterceptedMarker
{
    /** Constructor of the web bean component */
    private Constructor<T> constructor;
    
    protected boolean isAbstractDecorator;

    /**
     * Whether the bean is fully initialized or not yet.
     * Only beans scanned from the classpath can be lazily initialized,
     * and only if they do _NOT_ contain any javax.inject or javax.enterprise
     * annotation! In other words: we can skip eager initialisation for
     * beans which only could picked up as auto-&#0064;Dependent beans which
     * do not register/ any other beans (e.g. via &#0064;Produces)
     */
    private volatile boolean fullInit = true;


    public ManagedBean(Class<T> returnType, WebBeansContext webBeansContext)
    {
        this(returnType, WebBeansType.MANAGED, webBeansContext);
    }

    /**
     * Creates a new instance.
     * 
     * @param returnType bean class
     * @param type webbeans type
     * @param webBeansContext
     */
    public ManagedBean(Class<T> returnType, WebBeansType type, WebBeansContext webBeansContext)
    {
        super(type, returnType, webBeansContext);
        
        //Setting inherited meta data instance
        setInheritedMetaData();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected T createComponentInstance(CreationalContext<T> creationalContext)
    {
        if (!fullInit)
        {
            lazyInit();
        }


        Constructor<T> con = getConstructor();
        InjectableConstructor<T> ic = new InjectableConstructor<T>(con, this,creationalContext);

        T instance = ic.doInjection();
        
        //If this is an abstract Decorator, we need to set the handler on the Proxy instance
        if(isAbstractDecorator)
        {
            webBeansContext.getProxyFactory().setHandler(instance, new AbstractDecoratorMethodHandler());
        }
        
        return instance;
    }

    private synchronized void lazyInit()
    {
        if (!fullInit)
        {
            fullInit = true;
            ManagedBeanCreatorImpl<T> managedBeanCreator = new ManagedBeanCreatorImpl<T>(this);
            managedBeanCreator.setAnnotatedType(getAnnotatedType());

            getWebBeansContext().getWebBeansUtil().lazyInitializeManagedBean(getBeanClass(), this, managedBeanCreator);
        }
    }

    public boolean isFullInit()
    {
        return fullInit;
    }

    public void setFullInit(boolean fullInit)
    {
        this.fullInit = fullInit;
    }

    @Override
    public void addQualifier(Annotation qualifier)
    {
        if (!(qualifier instanceof Default || qualifier instanceof Any))
        {
            // if a bean defines other qualifiers than Default or Any, we need to fully initialize it
            fullInit = true;
        }
        super.addQualifier(qualifier);
    }

    /**
     * Get constructor.
     * 
     * @return constructor
     */
    public Constructor<T> getConstructor()
    {
        return constructor;
    }

    /**
     * Set constructor.
     * 
     * @param constructor constructor instance
     */
    public void setConstructor(Constructor<T> constructor)
    {
        this.constructor = constructor;
    }
    
    public boolean isPassivationCapable()
    {
        if (isPassivationCapable != null)
        {
            return isPassivationCapable.booleanValue();
        }
        if(Serializable.class.isAssignableFrom(returnType))
        {
            for(Decorator<?> dec : decorators)
            {
                if(dec.getBeanClass() != null && !Serializable.class.isAssignableFrom(dec.getBeanClass()))
                {
                    isPassivationCapable = Boolean.FALSE;
                    return false;
                }
            }

            for(InterceptorData interceptorData : interceptorStack)
            {
                if(interceptorData.isDefinedInInterceptorClass())
                {
                    Class<?> interceptor = interceptorData.getInterceptorClass();
                    if(!Serializable.class.isAssignableFrom(interceptor))
                    {
                        isPassivationCapable = Boolean.FALSE;
                        return false;
                    }
                }
            }

            isPassivationCapable = Boolean.TRUE;
            return true;
        }

        isPassivationCapable = Boolean.FALSE;
        return false;
    }

    /** cache previously calculated result */
    private Boolean isPassivationCapable = null;
    
    public void setIsAbstractDecorator(boolean flag)
    {
        isAbstractDecorator = flag;
    }
}
