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
package org.apache.webbeans.event;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.Asserts;
import org.apache.webbeans.util.ClassUtil;

public final class EventUtil
{
    private EventUtil()
    {

    }

    public static void checkEventType(Class<?> eventType)
    {
        Asserts.assertNotNull(eventType, "eventType parameter can not be null");

        if (ClassUtil.isDefinitionContainsTypeVariables(eventType))
        {
            throw new IllegalArgumentException("Event type : " + eventType.getName() + " can not be generic");
        }
    }

    public static void checkEventBindings(WebBeansContext webBeansContext, Annotation... annotations)
    {
        for(Annotation ann : annotations)
        {
            //This is added, because TCK Event tests for this.
            Retention retention = ann.annotationType().getAnnotation(Retention.class);
            RetentionPolicy policy = retention.value();
            if(!policy.equals(RetentionPolicy.RUNTIME))
            {
                throw new IllegalArgumentException("Event qualifiere RetentionPolicy must be RUNTIME for qualifier : " + ann);
            }
            ///////////////////////////////////////////////////////

        }
        webBeansContext.getAnnotationManager().checkQualifierConditions(annotations);
    }

    public static TransactionPhase getObserverMethodTransactionType(Method observerMethod)
    {
        Observes observes = AnnotationUtil.getMethodFirstParameterAnnotation(observerMethod, Observes.class);
        if (observes != null)
        {
            return observes.during();
        }
        
        return null;
    }

    public static void checkObserverMethodConditions(Method candidateObserverMethod, Class<?> clazz)
    {
        Asserts.assertNotNull(candidateObserverMethod, "candidateObserverMethod parameter can not be null");
        Asserts.nullCheckForClass(clazz);

        if (AnnotationUtil.hasMethodMultipleParameterAnnotation(candidateObserverMethod, Observes.class))
        {
            throw new WebBeansConfigurationException("Observer method : " + candidateObserverMethod.getName() + " in class : " + clazz.getName()
                                                     + " can not define two parameters with annotated @Observes");
        }

        if (AnnotationUtil.hasMethodAnnotation(candidateObserverMethod, Produces.class) || AnnotationUtil.hasMethodAnnotation(candidateObserverMethod, Inject.class))
        {
            throw new WebBeansConfigurationException("Observer method : " + candidateObserverMethod.getName() + " in class : "
                                                     + clazz.getName() + " can not annotated with annotation in the list {@Produces, @Initializer, @Destructor}");

        }

        if (AnnotationUtil.hasMethodParameterAnnotation(candidateObserverMethod, Disposes.class))
        {
            throw new WebBeansConfigurationException("Observer method : " + candidateObserverMethod.getName() + " in class : "
                                                     + clazz.getName() + " can not annotated with annotation @Disposes");
        }                
    }
    
    public static boolean isReceptionIfExist(Method observerMethod)
    {
        Observes observes = AnnotationUtil.getMethodFirstParameterAnnotation(observerMethod, Observes.class);
        Reception reception = observes.notifyObserver();
        if(reception.equals(Reception.IF_EXISTS))
        {
            return true;
        }

        return false;
    }


    public static boolean checkObservableInjectionPointConditions(InjectionPoint injectionPoint)
    {
        Type type = injectionPoint.getType();
        
        Class<?> candidateClazz = null;
        if(type instanceof Class)
        {
            candidateClazz = (Class<?>)type;
        }
        else if(type instanceof ParameterizedType)
        {
            ParameterizedType pt = (ParameterizedType)type;
            candidateClazz = (Class<?>)pt.getRawType();
        }
        
        if(!candidateClazz.equals(Event.class))
        {
            return false;
        }                

        if (!ClassUtil.isParametrizedType(injectionPoint.getType()))
        {
            throw new WebBeansConfigurationException("@Observable field injection " + injectionPoint
                                                     + " must be ParametrizedType with actual type argument");
        }
        else
        {                        
            if(ClassUtil.isParametrizedType(injectionPoint.getType()))
            {
                ParameterizedType pt = (ParameterizedType)injectionPoint.getType();
                
                Class<?> rawType = (Class<?>) pt.getRawType();
                
                Type[] typeArgs = pt.getActualTypeArguments();
                
                if(!(rawType.equals(Event.class)))
                {
                    return false;
                }                
                else
                {                                        
                    if(typeArgs.length != 1)
                    {
                        throw new IllegalArgumentException("@Observable field injection " + injectionPoint.toString()
                                                           + " must not have more than one actual type argument");
                    }
                }                                
            }
            else
            {
                throw new IllegalArgumentException("@Observable field injection " + injectionPoint.toString()
                                                   + " must be defined as ParameterizedType with one actual type argument");
            }        
        }
        
        return true;

    }

}
