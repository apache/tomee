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
package org.apache.webbeans.config.inheritance;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.Set;

import javax.enterprise.context.NormalScope;
import javax.enterprise.inject.Stereotype;
import javax.inject.Qualifier;
import javax.inject.Scope;
import javax.interceptor.InterceptorBinding;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.util.AnnotationUtil;

public class BeanInheritedMetaData<T> extends AbstractBeanInheritedMetaData<T>
{
    public BeanInheritedMetaData(AbstractOwbBean<T> component)
    {
        super(component, component.getReturnType().getSuperclass());
    }

    
    protected void setInheritedQualifiers()
    {
        if(inheritedClazz != null && inheritedClazz != Object.class)
        {
            setInheritedTypes(getInheritedQualifiers(), inheritedClazz, Qualifier.class);
        }        
    }
    
    protected void setInheritedInterceptorBindings()
    {
        if(inheritedClazz != null && inheritedClazz != Object.class)
        {
            setInheritedTypes(getInheritedInterceptorBindings(), inheritedClazz, InterceptorBinding.class);
        }        
        
    }

    
    protected void setInheritedScopeType()
    {
        if(inheritedClazz != null && inheritedClazz != Object.class)
        {
            setInheritedType(inheritedClazz, NormalScope.class);
            setInheritedType(inheritedClazz, Scope.class);
            
        }
    }

    
    protected void setInheritedStereoTypes()
    {
        if(inheritedClazz != null && inheritedClazz != Object.class)
        {
            setInheritedTypes(getInheritedStereoTypes(), inheritedClazz, Stereotype.class);
        }        
        
    }
    
    private void setInheritedType(Class<?> inheritedClass, Class<? extends Annotation> annotationType)
    {
        Annotation[] inheritedAnnotations = null;
        
        if(inheritedClass != null)
        {
           inheritedAnnotations =  AnnotationUtil.getMetaAnnotations(inheritedClass.getDeclaredAnnotations(), annotationType);
        }
        
        if(inheritedAnnotations != null && inheritedAnnotations.length > 0)
        {
            if(inheritedAnnotations[0].annotationType().isAnnotationPresent(Inherited.class))
            {
                Annotation annotation = inheritedAnnotations[0];
                
                if(annotationType.equals(NormalScope.class) || annotationType.equals(Scope.class))
                {
                    inheritedScopeType = annotation;
                }
            }
        }
        else
        {
            if(hasSuperType(inheritedClass))
            {
                setInheritedType(inheritedClass.getSuperclass(), annotationType);
            }
        }
        
    }
    
    private void setInheritedTypes(Set<Annotation> types, Class<?> inheritedClass, Class<? extends Annotation> annotationType)
    {
        Annotation[] annotations = null;
        
        if(inheritedClass != null)
        {
            annotations = AnnotationUtil.getMetaAnnotations(inheritedClass.getDeclaredAnnotations(), annotationType);
        }
        
        if(annotations != null)
        {
            for(Annotation annotation : annotations)
            {
                if(!types.contains(annotation))
                {
                    if(AnnotationUtil.hasClassAnnotation(annotation.annotationType(), Inherited.class))
                    {
                        types.add(annotation);   
                    }
                }
            }            
        }
        
        if(hasSuperType(inheritedClass))
        {
            setInheritedTypes(types, inheritedClass.getSuperclass(), annotationType);    
        }        
    }
    

    private boolean hasSuperType(Class<?> clazz)
    {
        return (clazz.getSuperclass() != Object.class) ? true : false;
    }
}
