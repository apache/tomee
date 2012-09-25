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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Decorator;

import org.apache.webbeans.config.inheritance.IBeanInheritedMetaData;
import org.apache.webbeans.intercept.InterceptorData;

/**
 * Defines contract for injection target beans.
 * 
 * @version $Rev$ $Date$
 *
 * @param <T> bean type
 */
public interface InjectionTargetBean<T> extends OwbBean<T>
{
    /**
     * Returns set of observable methods.
     * 
     * @return set of observable methods
     */
    public Set<Method> getObservableMethods();

    /**
     * Adds new observer method.
     * 
     * @param observerMethod observer method
     */
    public void addObservableMethod(Method observerMethod);
    
    /**
     * Inject JavaEE resources.
     * 
     * @param instance bean instance
     * @param creationalContext creational context
     */
    public void injectResources(T instance, CreationalContext<T> creationalContext);
    
    /**
     * Inject fields of the bean instance.
     * 
     * @param instance bean instance
     * @param creationalContext creational context
     */
    public void injectFields(T instance, CreationalContext<T> creationalContext);
    
    /**
     * Inject initializer methods of the bean instance.
     * 
     * @param instance bean instance
     * @param creationalContext creational context
     */
    public void injectMethods(T instance, CreationalContext<T> creationalContext);
    
    /**
     * Inject fields of the bean instance.
     * 
     * @param instance bean instance
     * @param creationalContext creational context
     */
    public void injectSuperFields(T instance, CreationalContext<T> creationalContext);
    
    /**
     * Inject initializer methods of the bean instance.
     * 
     * @param instance bean instance
     * @param creationalContext creational context
     */
    public void injectSuperMethods(T instance, CreationalContext<T> creationalContext);
        
    /**
     * Gets all injected fields of bean.
     * @return all injected fields
     */
    public Set<Field> getInjectedFields();

    /**
     * Adds new injected field.
     * @param field new injected field
     */
    public void addInjectedField(Field field);
    
    /**
     * Gets injected fields from super class.
     * @return injected fields from super class 
     */
    public Set<Field> getInjectedFromSuperFields();

    /**
     * Adds new super injected field.
     * @param field add to super
     */
    public void addInjectedFieldToSuper(Field field);    
    
    /**
     * Gets injected methods.
     * @return injected(initializer) methods
     */
    public Set<Method> getInjectedMethods();

    /**
     * Adds new injected method.
     * @param method new injected method
     */
    public void addInjectedMethod(Method method);

    /**
     * Gets injected methods from super class.
     * @return injected method from super class
     */
    public Set<Method> getInjectedFromSuperMethods();

    /**
     * Add injected method to super list.
     * @param method injected method
     */
    public void addInjectedMethodToSuper(Method method);
    
    /**
     * Gets inherited meta data.
     * @return inherited meta data
     */
    public IBeanInheritedMetaData getInheritedMetaData();
    
    /**
     * Gets interceptor stack of bean instance.
     * @return interceptor stack
     */
    public List<InterceptorData> getInterceptorStack();    
    
    /**
     * Gets decorator stack of bean instance.
     * @return decorator stack
     */
    public List<Decorator<?>> getDecoratorStack();

    /**
     * Calls post constrcut method.
     * 
     * @param instance bean instance
     */
    public void postConstruct(T instance, CreationalContext<T> creationalContext);
    
    /**
     * Calls predestroy method.
     * 
     * @param instance bean instance
     */
    public void preDestroy(T instance, CreationalContext<T> creationalContext);    
    
    /**
     * Sets annotated type.
     * @param annotatedType annotated type
     */
    public void setAnnotatedType(AnnotatedType<T> annotatedType);    
    
    /**
     * Gets annotated type.
     * @return annotated type
     */
    public AnnotatedType<T> getAnnotatedType();        
}
