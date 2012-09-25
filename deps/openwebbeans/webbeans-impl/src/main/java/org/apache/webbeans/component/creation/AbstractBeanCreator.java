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
package org.apache.webbeans.component.creation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.config.DefinitionUtil;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * Abstract implementation.
 * 
 * @version $Rev$ $Date$
 *
 * @param <T> bean class info
 */
public class AbstractBeanCreator<T> implements BeanCreator<T>
{
    /**Bean instance*/
    private final AbstractOwbBean<T> bean;    
    
    /**Default metadata provider*/
    private MetaDataProvider metadataProvider = MetaDataProvider.DEFAULT;
    
    /**Bean annotations*/
    private final Annotation[] beanAnnotations;
    
    /**
     * If annotated type is set by ProcessAnnotatedType event, used this annotated type
     * to define bean instance instead of using class artifacts.
     */
    private AnnotatedType<T> annotatedType;

    private final DefinitionUtil definitionUtil;
    
    /**
     * Creates a bean instance.
     * 
     * @param bean bean instance
     * @param beanAnnotations annotations
     */
    public AbstractBeanCreator(AbstractOwbBean<T> bean, Annotation[] beanAnnotations)
    {
        this.bean = bean;
        this.beanAnnotations = beanAnnotations;
        definitionUtil = bean.getWebBeansContext().getDefinitionUtil();
    }

    /**
     * {@inheritDoc}
     */
    public void checkCreateConditions()
    {
        //Sub-class can override this
    }

    /**
     * {@inheritDoc}
     */
    public void defineApiType()
    {
        if(isDefaultMetaDataProvider())
        {
            DefinitionUtil.defineApiTypes(bean, bean.getReturnType());
        }
        else
        {
            Set<Type> types = annotatedType.getTypeClosure();
            bean.getTypes().addAll(types);
        }
        Set<String> ignored = bean.getWebBeansContext().getOpenWebBeansConfiguration().getIgnoredInterfaces();
        for (Iterator<Type> i = bean.getTypes().iterator(); i.hasNext();)
        {
            Type t = i.next();
            if (t instanceof Class && ignored.contains(((Class<?>)t).getName()))
            {
                i.remove();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void defineQualifier()
    {
        if(isDefaultMetaDataProvider())
        {
            definitionUtil.defineQualifiers(bean, beanAnnotations);
        }
        else
        {
            definitionUtil.defineQualifiers(bean, AnnotationUtil.getAnnotationsFromSet(annotatedType.getAnnotations()));
        }
        
    }
    
    /**
     * {@inheritDoc}
     */
    public void defineName(String defaultName)
    {
        if(isDefaultMetaDataProvider())
        {
            definitionUtil.defineName(bean, beanAnnotations, defaultName);
        }
        else
        {
            definitionUtil.defineName(bean, AnnotationUtil.getAnnotationsFromSet(annotatedType.getAnnotations()),
                    WebBeansUtil.getManagedBeanDefaultName(annotatedType.getJavaClass().getSimpleName()));
        }
        
    }

    /**
     * {@inheritDoc}
     */
    public void defineScopeType(String errorMessage, boolean allowLazyInit)
    {
        if(isDefaultMetaDataProvider())
        {
            definitionUtil.defineScopeType(bean, beanAnnotations, errorMessage, allowLazyInit);
        }
        else
        {
            definitionUtil.defineScopeType(bean, AnnotationUtil.getAnnotationsFromSet(annotatedType.getAnnotations()), errorMessage, false);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void defineSerializable()
    {
        definitionUtil.defineSerializable(bean);
    }

    /**
     * {@inheritDoc}
     */
    public void defineStereoTypes()
    {
        if(isDefaultMetaDataProvider())
        {
            definitionUtil.defineStereoTypes(bean, beanAnnotations);
        }
        else
        {
            definitionUtil.defineStereoTypes(bean, AnnotationUtil.getAnnotationsFromSet(annotatedType.getAnnotations()));
        }
        
    }
    
    /**
     * {@inheritDoc}
     */
    public MetaDataProvider getMetaDataProvider()
    {
        return metadataProvider;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setMetaDataProvider(MetaDataProvider metadataProvider)
    {
        this.metadataProvider = metadataProvider;
    }
    
    /**
     * Returns true if metadata provider is default,
     * false otherwise
     * 
     * @return true if metadata provider is default
     */
    protected boolean isDefaultMetaDataProvider()
    {
        return metadataProvider.equals(MetaDataProvider.DEFAULT);
    }

    /**
     * {@inheritDoc}
     */
    public AbstractOwbBean<T> getBean()
    {
        return bean;
    }

   protected AnnotatedType<T> getAnnotatedType()
    {
        return annotatedType;
    }
    
    public void setAnnotatedType(AnnotatedType<T> annotatedType)
    {
        this.annotatedType = annotatedType;
    }
}
