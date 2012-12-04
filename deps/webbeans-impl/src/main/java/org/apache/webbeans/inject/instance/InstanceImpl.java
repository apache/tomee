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
package org.apache.webbeans.inject.instance;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.TypeLiteral;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.container.InjectionResolver;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.util.ClassUtil;
import org.apache.webbeans.util.OwbCustomObjectInputStream;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * Implements the {@link Instance} interface.
 * 
 * @param <T> specific instance type
 */
class InstanceImpl<T> implements Instance<T>, Serializable
{
    private static final long serialVersionUID = -8401944412490389024L;

    /** Injected class type */
    private Type injectionClazz;

    /**
     * injection point class used to determine the BDA it was loaded from or null.
     */
    private Class<?> injectionPointClazz;

    /** Qualifier annotations appeared on the injection point */
    private Set<Annotation> qualifierAnnotations = new HashSet<Annotation>();

    private WebBeansContext webBeansContext;

    private CreationalContext<?> parentCreationalContext;

    private Object ownerInstance;

    /**
     * Creates new instance.
     * 
     * @param injectionClazz injection class type
     * @param injectionPointClazz null or class of injection point
     * @param webBeansContext
     * @param creationalContext will get used for creating &#064;Dependent beans
     * @param ownerInstance the object the current Instance got injected into
     * @param annotations qualifier annotations
     */
    InstanceImpl(Type injectionClazz, Class<?> injectionPointClazz, WebBeansContext webBeansContext,
                 CreationalContext<?> creationalContext, Object ownerInstance, Annotation... annotations)
    {
        this.injectionClazz = injectionClazz;
        this.injectionPointClazz=injectionPointClazz;
        this.parentCreationalContext = creationalContext;
        this.ownerInstance = ownerInstance;

        for (Annotation ann : annotations)
        {
            qualifierAnnotations.add(ann);
        }
        this.webBeansContext = webBeansContext;
    }

    /**
     * Returns the bean instance with given qualifier annotations.
     * 
     * @return bean instance
     */
    @SuppressWarnings("unchecked")
    public T get()
    {
        T instance;

        Annotation[] anns = new Annotation[qualifierAnnotations.size()];
        anns = qualifierAnnotations.toArray(anns);
        
        Set<Bean<?>> beans = resolveBeans();

        webBeansContext.getResolutionUtil().checkResolvedBeans(beans, ClassUtil.getClazz(injectionClazz), anns, null);
        BeanManagerImpl beanManager = webBeansContext.getBeanManagerImpl();

        Bean<?> bean = beanManager.resolve(beans);

        // since Instance<T> is Dependent, we we gonna use the parent CreationalContext by default
        CreationalContext<?> creationalContext = parentCreationalContext;

        boolean isDependentBean = WebBeansUtil.isDependent(bean);

        if (!isDependentBean)
        {
            // but for all NormalScoped beans we will need to create a fresh CreationalContext
            creationalContext = beanManager.createCreationalContext(bean);
        }

        instance = (T) beanManager.getReference(bean, null, creationalContext);

        if (isDependentBean && ownerInstance != null && creationalContext instanceof CreationalContextImpl)
        {
            ((CreationalContextImpl<?>) creationalContext).addDependent(ownerInstance, bean, instance);
        }

        return instance;
    }

    /**
     * Returns set of resolved beans.
     * 
     * @return set of resolved beans
     */
    private Set<Bean<?>> resolveBeans()
    {
        Annotation[] anns = new Annotation[qualifierAnnotations.size()];
        anns = qualifierAnnotations.toArray(anns);

        InjectionResolver injectionResolver = webBeansContext.getBeanManagerImpl().getInjectionResolver();

        InjectionResolver resolver = injectionResolver;
        Set<Bean<?>> beans = resolver.implResolveByType(injectionClazz, injectionPointClazz, anns);
        return beans;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isAmbiguous()
    {
        Set<Bean<?>> beans = resolveBeans();
        
        return beans.size() > 1 ? true : false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUnsatisfied()
    {
        Set<Bean<?>> beans = resolveBeans();
        
        return beans.size() == 0 ? true : false;
    }

    /**
     * {@inheritDoc}
     */
    public Instance<T> select(Annotation... qualifiers)
    {
        Annotation[] newQualifiersArray = getAdditionalQualifiers(qualifiers);
        InstanceImpl<T> newInstance = new InstanceImpl<T>(injectionClazz, injectionPointClazz,
                                                          webBeansContext, parentCreationalContext,
                                                          ownerInstance, newQualifiersArray);

        return newInstance;
    }

    /**
     * Returns total qualifiers array
     * 
     * @param qualifiers additional qualifiers
     * @return total qualifiers array
     */
    private Annotation[] getAdditionalQualifiers(Annotation[] qualifiers)
    {
        webBeansContext.getAnnotationManager().checkQualifierConditions(qualifiers);
        Set<Annotation> newQualifiers = new HashSet<Annotation>(qualifierAnnotations);

        if (qualifiers != null && qualifiers.length > 0)
        {
            for (Annotation annot : qualifiers)
            {
                if (newQualifiers.contains(annot))
                {
                    throw new IllegalArgumentException("Duplicate Qualifier Exception, " + toString());
                }

                newQualifiers.add(annot);
            }
        }

        Annotation[] newQualifiersArray = new Annotation[newQualifiers.size()];
        newQualifiersArray = newQualifiers.toArray(newQualifiersArray);
        
        return newQualifiersArray;
    }
    
    /**
     * {@inheritDoc}
     */
    public <U extends T> Instance<U> select(Class<U> subtype, Annotation... qualifiers)
    {
        webBeansContext.getAnnotationManager().checkQualifierConditions(qualifiers);

        Type sub = subtype;
        
        if(sub == null)
        {
            sub = injectionClazz;
        }
        
        Annotation[] newQualifiers = getAdditionalQualifiers(qualifiers);
        
        InstanceImpl<U> newInstance = new InstanceImpl(sub, injectionPointClazz, webBeansContext,
                                                       parentCreationalContext, ownerInstance,
                                                       newQualifiers);
                    
        return newInstance;
    }

    /**
     * {@inheritDoc}
     */
    public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers)
    {        
        return select(subtype.getRawType(), qualifiers);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator()
    {
        Set<Bean<?>> beans = resolveBeans();
        Set<T> instances = new HashSet<T>();
        for(Bean<?> bean : beans)
        {
            T instance = (T) webBeansContext.getBeanManagerImpl().getReference(bean,null, parentCreationalContext);
            instances.add(instance);
        }
        
        return instances.iterator();
    }
    
    private void writeObject(java.io.ObjectOutputStream op) throws IOException
    {
        ObjectOutputStream oos = new ObjectOutputStream(op);
        oos.writeObject(injectionClazz);
        oos.writeObject(qualifierAnnotations);
        oos.writeObject(injectionPointClazz);
        
        oos.flush();
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        webBeansContext = WebBeansContext.currentInstance();
        final ObjectInputStream inputStream = new OwbCustomObjectInputStream(in, WebBeansUtil.getCurrentClassLoader());
        injectionClazz = (Type)inputStream.readObject();
        qualifierAnnotations = (Set<Annotation>)inputStream.readObject();
        injectionPointClazz = (Class<?>) inputStream.readObject();
    }
    

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Instance<");
        builder.append(ClassUtil.getClazz(injectionClazz).getName());
        builder.append("> injectionPointClazz=").append(injectionPointClazz);
        
        builder.append(",with qualifier annotations {");
        int i = 0;
        for (Annotation qualifier : qualifierAnnotations)
        {
            if (i != 0)
            {
                builder.append(",");
            }

            builder.append(qualifier.toString());
        }

        builder.append("}");

        return builder.toString();
    }
    
}
