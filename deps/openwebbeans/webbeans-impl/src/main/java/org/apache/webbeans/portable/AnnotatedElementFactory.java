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
package org.apache.webbeans.portable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.util.Asserts;

/**
 * Factory for {@link javax.enterprise.inject.spi.Annotated} elements.
 * 
 * @version $Rev$ $Date$
 */
public final class AnnotatedElementFactory
{

    // Logger instance
    private final static Logger logger = WebBeansLoggerFacade.getLogger(AnnotatedElementFactory.class);

    //Cache of the AnnotatedType
    private ConcurrentMap<Class<?>, AnnotatedType<?>> annotatedTypeCache =
        new ConcurrentHashMap<Class<?>, AnnotatedType<?>>();
    
    //Cache of AnnotatedConstructor
    private ConcurrentMap<Constructor<?>, AnnotatedConstructor<?>> annotatedConstructorCache =
        new ConcurrentHashMap<Constructor<?>, AnnotatedConstructor<?>>();
    
    //Cache of AnnotatedMethod
    private ConcurrentMap<Method, AnnotatedMethod<?>> annotatedMethodCache =
        new ConcurrentHashMap<Method, AnnotatedMethod<?>>();
    
    //Cache of AnnotatedField
    private ConcurrentMap<Field, AnnotatedField<?>> annotatedFieldCache =
        new ConcurrentHashMap<Field, AnnotatedField<?>>();

    private WebBeansContext webBeansContext;
    
    /**
     * No instantiate.
     */
    public AnnotatedElementFactory(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
    }

    /**
     * Get an already registered AnnotatedType. This will NOT create a new one!
     * @param annotatedClass
     * @param <X>
     * @return AnnotatedType
     */
    public <X> AnnotatedType<X> getAnnotatedType(Class<X> annotatedClass)
    {
        return (AnnotatedType<X>) annotatedTypeCache.get(annotatedClass);
    }

    /**
     * This method will get used to manually add AnnoatedTypes to our storage.
     * Those AnnotatedTypes are coming from Extensions and get registered e.g. via
     * {@link javax.enterprise.inject.spi.BeforeBeanDiscovery#addAnnotatedType(AnnotatedType)}
     *
     * Sets the annotatedType and replace the given one.
     * @param annotatedType
     * @param <X>
     * @return the previously registered AnnotatedType or null if not previously defined.
     */
    public <X> AnnotatedType<X> setAnnotatedType(AnnotatedType<X> annotatedType)
    {
        return (AnnotatedType<X>) annotatedTypeCache.put(annotatedType.getJavaClass(), annotatedType);
    }


    /**
     * Creates and configures new annotated type.
     * 
     * @param <X> class info
     * @param annotatedClass annotated class
     * @return new annotated type
     */
    @SuppressWarnings("unchecked")
    public <X> AnnotatedType<X> newAnnotatedType(Class<X> annotatedClass)
    {
        Asserts.assertNotNull(annotatedClass, "annotatedClass is null");
        AnnotatedType<X> annotatedType = (AnnotatedType<X>)annotatedTypeCache.get(annotatedClass);
        if(annotatedType == null)
        {
            try
            {
                annotatedType = new AnnotatedTypeImpl<X>(webBeansContext, annotatedClass);

                AnnotatedType<X> oldType = (AnnotatedType<X>)annotatedTypeCache.putIfAbsent(annotatedClass, annotatedType);
                if(oldType != null)
                {
                    annotatedType = oldType;
                }
            }
            catch (Exception e)
            {
                if (e instanceof ClassNotFoundException || e instanceof ArrayStoreException)
                {
                    if (logger.isLoggable(Level.SEVERE))
                    {
                        logger.log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0027, annotatedClass.getName(), e.getCause()), e);
                    }

                    annotatedType = null;
                } 
                else
                {
                    throw new RuntimeException(e);
                }
            } 
            catch (NoClassDefFoundError ncdfe)
            {
                if (logger.isLoggable(Level.SEVERE))
                {
                    logger.log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0027, annotatedClass.getName(), ncdfe.getCause()), ncdfe);
                }

                annotatedType = null;
            }
        }
                
        return annotatedType;
    }

    /**
     * Creates and configures new annotated constructor.
     * 
     * @param <X> declaring class
     * @param constructor constructor
     * @return new annotated constructor
     */
    @SuppressWarnings("unchecked")
    public <X> AnnotatedConstructor<X> newAnnotatedConstructor(Constructor<X> constructor, AnnotatedType<X> declaringClass)
    {
        Asserts.assertNotNull(constructor, "constructor is null");
        Asserts.assertNotNull(declaringClass, "declaringClass is null");
        
        AnnotatedConstructorImpl<X> annConstructor;
        if(annotatedConstructorCache.containsKey(constructor))
        {
            annConstructor = (AnnotatedConstructorImpl<X>)annotatedConstructorCache.get(constructor);
        }
        else
        {
            annConstructor = new AnnotatedConstructorImpl<X>(webBeansContext, constructor, declaringClass);
            AnnotatedConstructorImpl<X> old = (AnnotatedConstructorImpl<X>)annotatedConstructorCache.putIfAbsent(constructor, annConstructor);
            if(old != null)
            {
                annConstructor = old;
            }
        }
        
        return annConstructor;
    }

    /**
     * Creates and configures new annotated field.
     * 
     * @param <X> declaring class
     * @param field field instance
     * @param declaringClass declaring class
     * @return new annotated field
     */
    @SuppressWarnings("unchecked")
    public <X> AnnotatedField<X> newAnnotatedField(Field field, AnnotatedType<X> declaringClass)
    {
        Asserts.assertNotNull(field, "field is null");
        Asserts.assertNotNull(declaringClass, "declaringClass is null");
        
        AnnotatedFieldImpl<X> annotField;
        if(annotatedFieldCache.containsKey(field))
        {
            annotField = (AnnotatedFieldImpl<X>)annotatedFieldCache.get(field);
        }
        else
        {
            annotField = new AnnotatedFieldImpl<X>(webBeansContext, field, declaringClass);
            AnnotatedFieldImpl<X> old = (AnnotatedFieldImpl<X>) annotatedFieldCache.putIfAbsent(field, annotField);
            if(old != null)
            {
                annotField = old;
            }
        }
        
        return annotField; 
    }

    /**
     * Creates and configures new annotated method.
     * 
     * @param <X> declaring class
     * @param method annotated method
     * @param declaringType declaring class info
     * @return new annotated method
     */
    @SuppressWarnings("unchecked")
    public <X> AnnotatedMethod<X> newAnnotatedMethod(Method method, AnnotatedType<X> declaringType)
    {
        Asserts.assertNotNull(method, "method is null");
        Asserts.assertNotNull(declaringType, "declaringType is null");
        
        AnnotatedMethodImpl<X> annotMethod;
        if(annotatedMethodCache.containsKey(method))
        {
            annotMethod = (AnnotatedMethodImpl<X>)annotatedMethodCache.get(method);
        }
        else
        {
            annotMethod = new AnnotatedMethodImpl<X>(webBeansContext, method, declaringType);
            AnnotatedMethodImpl<X> old = (AnnotatedMethodImpl<X>) annotatedMethodCache.putIfAbsent(method, annotMethod);
            if(old != null)
            {
                annotMethod = old;
            }
        }
        
        return annotMethod;          
    }
    
    /**
     * Clear caches.
     */
    public void clear()
    {
        annotatedTypeCache.clear();
        annotatedConstructorCache.clear();
        annotatedFieldCache.clear();
        annotatedMethodCache.clear();
    }
}
