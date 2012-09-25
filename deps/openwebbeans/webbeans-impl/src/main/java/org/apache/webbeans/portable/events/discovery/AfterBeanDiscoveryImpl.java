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
package org.apache.webbeans.portable.events.discovery;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessObserverMethod;

import org.apache.webbeans.component.ManagedBean;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.decorator.WebBeansDecorator;
import org.apache.webbeans.intercept.custom.CustomInterceptor;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.portable.events.generics.GProcessBean;
import org.apache.webbeans.portable.events.generics.GProcessObservableMethod;
import org.apache.webbeans.util.AnnotationUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Event that is fired by the container after it discovers beans.
 * 
 * @version $Rev$ $Date$
 *
 */
public class AfterBeanDiscoveryImpl implements AfterBeanDiscovery
{
    private BeanManagerImpl beanManager = null;
    
    private static final Logger logger = WebBeansLoggerFacade.getLogger(AfterBeanDiscoveryImpl.class);
    private final WebBeansContext webBeansContext;

    public AfterBeanDiscoveryImpl(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
        beanManager = this.webBeansContext.getBeanManagerImpl();
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void addBean(Bean<?> bean)
    {
        AnnotatedType<?> annotatedType = webBeansContext.getAnnotatedElementFactory().newAnnotatedType(bean.getBeanClass());
        
        //Fire Event
        ProcessBean<?> processBeanEvent = new GProcessBean(bean,annotatedType);
        beanManager.fireEvent(processBeanEvent, AnnotationUtil.EMPTY_ANNOTATION_ARRAY);
        
        if(bean instanceof Interceptor)
        {
            //Required for custom interceptors
            ManagedBean managedBean =
                webBeansContext.getWebBeansUtil().defineManagedBeanWithoutFireEvents(
                    (AnnotatedType<?>) annotatedType);
            
            CustomInterceptor<?> interceptor = new CustomInterceptor(managedBean, (Interceptor<?>)bean);
            if(interceptor.getScope() != Dependent.class)
            {
                if(logger.isLoggable(Level.WARNING))
                {
                    logger.log(Level.WARNING, OWBLogConst.WARN_0005_1, interceptor.getBeanClass().getName());
                }
            }
            
            if(interceptor.getName() != null)
            {
                if(logger.isLoggable(Level.WARNING))
                {
                    logger.log(Level.WARNING, OWBLogConst.WARN_0005_2, interceptor.getBeanClass().getName());
                }
            }
            
            if(interceptor.isAlternative())
            {
                if(logger.isLoggable(Level.WARNING))
                {
                    logger.log(Level.WARNING, OWBLogConst.WARN_0005_3, interceptor.getBeanClass().getName());
                }                
            }

            beanManager.addInterceptor(interceptor);
            webBeansContext.getBeanManagerImpl().addCustomInterceptorClass(bean.getBeanClass());
        }
        
        else if(bean instanceof Decorator)
        {
            //Required for custom decorators
            ManagedBean managedBean =
                webBeansContext.getWebBeansUtil().defineManagedBeanWithoutFireEvents(
                    (AnnotatedType<?>) annotatedType);
            if(managedBean.getScope() != Dependent.class)
            {
                if(logger.isLoggable(Level.WARNING))
                {
                    logger.log(Level.WARNING, OWBLogConst.WARN_0005_1, managedBean.getBeanClass().getName());
                }
            }
            
            if(managedBean.getName() != null)
            {
                if(logger.isLoggable(Level.WARNING))
                {
                    logger.log(Level.WARNING, OWBLogConst.WARN_0005_2, managedBean.getBeanClass().getName());
                }
            }
            
            if(managedBean.isAlternative())
            {
                if(logger.isLoggable(Level.WARNING))
                {
                    logger.log(Level.WARNING, OWBLogConst.WARN_0005_3, managedBean.getBeanClass().getName());
                }                
            }


            beanManager.addDecorator(new WebBeansDecorator(managedBean, (Decorator) bean));
            webBeansContext.getBeanManagerImpl().addCustomDecoratorClass(bean.getBeanClass());
        }
        else
        {
            beanManager.addBean(bean);
        }                
    }

    /**
     * {@inheritDoc}
     */
    public void addContext(Context context)
    {
        beanManager.addContext(context);

    }

    /**
     * {@inheritDoc}
     */
    public void addDefinitionError(Throwable t)
    {
        beanManager.getErrorStack().pushError(t);
    }

    /**
     * {@inheritDoc}
     */
    public void addObserverMethod(ObserverMethod<?> observerMethod)
    {
        ProcessObserverMethod<?, ?> event = new GProcessObservableMethod(null,observerMethod);
        beanManager.fireEvent(event, AnnotationUtil.EMPTY_ANNOTATION_ARRAY);
        beanManager.getNotificationManager().addObserver(observerMethod, observerMethod.getObservedType());
    }

}
