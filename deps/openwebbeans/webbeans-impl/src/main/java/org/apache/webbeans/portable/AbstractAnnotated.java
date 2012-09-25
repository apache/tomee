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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Annotated;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.ClassUtil;

/**
 * Abstract implementation of the {@link Annotated} contract.
 * 
 * @version $Rev$ $Date$
 */
abstract class AbstractAnnotated implements Annotated
{
    /**Base type of an annotated element*/
    private final Type baseType;
    
    /**Type closures*/
    private Set<Type> typeClosures = null;
    
    /**Set of annotations*/
    private Set<Annotation> annotations = new HashSet<Annotation>();

    private final WebBeansContext webBeansContext;
    
    /**
     * Createa a new annotated element.
     *
     * @param webBeansContext our WebBeansContext
     * @param baseType annotated element type
     */
    protected AbstractAnnotated(WebBeansContext webBeansContext, Type baseType)
    {
        if (webBeansContext == null)
        {
            throw new NullPointerException("no WebBeansContext");
        }
        if (baseType == null)
        {
            throw new NullPointerException("no base type");
        }
        this.baseType = baseType;
        this.webBeansContext = webBeansContext;
    }

    /**
     * Adds new annotation to set.
     * 
     * @param annotation new annotation
     */
    protected void addAnnotation(Annotation annotation)
    {
        annotations.add(annotation);
    }

    protected WebBeansContext getWebBeansContext()
    {
        return webBeansContext;
    }

    /**
     * Adds new annotation to set.
     * 
     * @param annotations new annotations
     */
    protected void setAnnotations(Annotation[] annotations)
    {        
        this.annotations.clear();
        Collections.addAll(this.annotations, annotations);
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getAnnotation(Class<T> annotationType)
    {
        for(Annotation ann : annotations)
        {
            if(ann.annotationType().equals(annotationType))
            {
                return (T)ann;
            }
        }
        
        return null;

    }

    /**
     * {@inheritDoc}
     */
    public Set<Annotation> getAnnotations()
    {
        return annotations;
    }

    /**
     * {@inheritDoc}
     */
    public Type getBaseType()
    {
        return baseType;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Type> getTypeClosure()
    {
        if (typeClosures == null)
        {
            initTypeClosures();
        }
        return typeClosures;
    }

    private synchronized void initTypeClosures()
    {
        if (typeClosures == null)
        {
            typeClosures = new HashSet<Type>();
            typeClosures.add(Object.class);
            ClassUtil.setTypeHierarchy(typeClosures, baseType);
            Set<String> ignoredInterfaces = webBeansContext.getOpenWebBeansConfiguration().getIgnoredInterfaces();
            for (Iterator<Type> i = typeClosures.iterator(); i.hasNext(); )
            {
                Type t = i.next();
                if (t instanceof Class && ignoredInterfaces.contains(((Class<?>)t).getName()))
                {
                    i.remove();
                }
            }

            Annotation[] anns = annotations.toArray(new Annotation[annotations.size()]);
            if(AnnotationUtil.hasAnnotation(anns, Typed.class))
            {
                Typed beanTypes = AnnotationUtil.getAnnotation(anns, Typed.class);
                Class<?>[] types = beanTypes.value();

                //New api types
                Set<Type> newTypes = new HashSet<Type>();
                for(Class<?> type : types)
                {
                    Type foundType = null;

                    for(Type apiType : typeClosures)
                    {
                        if(ClassUtil.getClazz(apiType) == type)
                        {
                            foundType = apiType;
                            break;
                        }
                    }

                    if(foundType == null)
                    {
                        throw new WebBeansConfigurationException("@Type values must be in bean api types of class: " + baseType);
                    }

                    newTypes.add(foundType);
                }

                typeClosures.clear();
                typeClosures.addAll(newTypes);

                typeClosures.add(Object.class);
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
    {
        for(Annotation ann : annotations)
        {
            if(ann.annotationType().equals(annotationType))
            {
                return true;
            }
        }        
        
        return false;
    }
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Base Type : " + baseType.toString() + ",");
        builder.append("Type Closures : " + typeClosures + ",");
        builder.append("Annotations : " + annotations.toString());
        
        return builder.toString();
    }

}
