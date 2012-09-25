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
package org.apache.webbeans.ejb.common.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.decorator.Decorator;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.SessionBeanType;
import javax.interceptor.Interceptor;

import org.apache.webbeans.ejb.common.component.BaseEjbBean;
import org.apache.webbeans.event.ObserverMethodImpl;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.Asserts;
import org.apache.webbeans.util.ClassUtil;

/**
 * Validates session beans.
 * 
 * @version $Rev: 915746 $ $Date: 2010-02-24 12:43:43 +0200 (Wed, 24 Feb 2010) $
 */
public final class EjbValidator
{
    // No-instaniate
    private EjbValidator()
    {
        // Empty
    }

    /**
     * Validates session bean's scope.
     * 
     * @param ejbBean ejb bean
     */
    public static void validateEjbScopeType(BaseEjbBean<?> ejbBean)
    {
        Asserts.assertNotNull(ejbBean, "Session Bean can not be null");

        if (ejbBean.getScope() == null)
        {
            throw new NullPointerException("Session Bean scope can not be null");
        }

        if (ejbBean.getEjbType() == null)
        {
            throw new NullPointerException("Session Bean type can not be null. It must be one of @Stateless, @Stateful, @Singleton");
        }

        if (ejbBean.getEjbType().equals(SessionBeanType.STATELESS))
        {
            if (!ejbBean.getScope().equals(Dependent.class))
            {
                throw new WebBeansConfigurationException("Stateless Session Bean class : " + ejbBean.getReturnType() + " " + "can not define scope other than @Dependent");
            }
        }
        else if (ejbBean.getEjbType().equals(SessionBeanType.SINGLETON))
        {
            if (!ejbBean.getScope().equals(Dependent.class) && !ejbBean.getScope().equals(ApplicationScoped.class))
            {
                throw new WebBeansConfigurationException("Singleton Session Bean class : " + ejbBean.getReturnType() + " "
                                                         + "can not define scope other than @Dependent or @ApplicationScoped");
            }
        }
    }

    /**
     * Validates session bean decorator/interceptor conditions.
     * 
     * @param ejbClass ejb bean class
     */
    public static void validateDecoratorOrInterceptor(Class<?> ejbClass)
    {
        Asserts.assertNotNull(ejbClass, "ejbClass parameter can not be null");

        if (AnnotationUtil.hasClassAnnotation(ejbClass, Decorator.class))
        {
            throw new WebBeansConfigurationException(EjbConstants.EJB_WEBBEANS_ERROR_CLASS_PREFIX + ejbClass.getName() + " can not annotated with @Decorator");

        }

        if (AnnotationUtil.hasClassAnnotation(ejbClass, Interceptor.class))
        {
            throw new WebBeansConfigurationException(EjbConstants.EJB_WEBBEANS_ERROR_CLASS_PREFIX + ejbClass.getName() + " can not annotated with @Interceptor");
        }
    }
    
    /**
     * Check generic type conditions.
     * @param ejbClass ebj class
     * @param scopeType scope type
     */
    public static void validateGenericBeanType(Class<?> ejbClass, Class<? extends Annotation> scopeType)
    {
        Asserts.assertNotNull(ejbClass, "ejbClass parameter can not be null");
        Asserts.assertNotNull(ejbClass, "scopeType parameter can not be null");
        
        if(ClassUtil.isDefinitionContainsTypeVariables(ejbClass))
        {
            if(!scopeType.equals(Dependent.class))
            {
                throw new WebBeansConfigurationException("Ejb generic bean class : " + ejbClass.getName() + "scope must be @Dependent");
            }
        }
    }
    
    public static void validateObserverMethods(BaseEjbBean<?> bean, Set<ObserverMethod<?>> observers)
    {
        for(ObserverMethod<?> observer : observers)
        {
            ObserverMethodImpl<?> obs = (ObserverMethodImpl<?>)observer;
            Method method = obs.getObserverMethod();
            List<?> locals =  bean.getBusinessLocalInterfaces();
            if(locals != null)
            {
                Iterator<?> it = locals.iterator();
                boolean found = false;
                while(it.hasNext())
                {
                    Class<?> clazz = (Class<?>)it.next();
                    List<Method> methods = ClassUtil.getClassMethodsWithTypes(clazz, method.getName(), Arrays.asList(method.getParameterTypes()));
                    if(methods.isEmpty())
                    {
                        continue;
                    }
                    else
                    {
                        //Should only be a single method that matches the names & params
                        obs.setObserverMethod(methods.get(0));
                        found = true;
                        break;
                    }
                }
                
                if(!found)
                {
                    if(!Modifier.isStatic(method.getModifiers()))
                    {
                        throw new WebBeansConfigurationException("Observer method : " + method.getName() + " in session bean class : " + 
                                bean.getBeanClass() + " must be business method");                                            
                    }
                }
            }
        }
    }

}