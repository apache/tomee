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
package org.apache.webbeans.deployment;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.NormalScope;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Scope;

import org.apache.webbeans.annotation.AnnotationManager;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.deployment.stereotype.IStereoTypeModel;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.util.AnnotationUtil;

/**
 * Default implementation of the {@link IStereoTypeModel} contract.
 * 
 * @version $Rev$ $Date$
 *
 */
public class StereoTypeModel implements IStereoTypeModel
{
    /**Name of the stereotype model. It is usd for registering model with StereoTypeManager*/
    private String name;

    /**Default deployment type*/
    private Annotation defaultDeploymentType;

    /**Default scope type*/
    private Annotation defaultScopeType;

    /**Interceptor Bindings*/
    private Set<Annotation> interceptorBindingTypes = new HashSet<Annotation>();

    /**Inherit StereoType annotations*/
    private Set<Annotation> inherits = new HashSet<Annotation>();
    
    private static final Logger logger = WebBeansLoggerFacade.getLogger(StereoTypeModel.class);

    /**
     * Creates a new instance of the stereotype model for
     * given class.
     * @param webBeansContext
     * @param clazz stereotype type
     */
    public StereoTypeModel(WebBeansContext webBeansContext, Class<? extends Annotation> clazz)
    {
        this(webBeansContext, clazz, clazz.getDeclaredAnnotations());
    }
    
    public StereoTypeModel(WebBeansContext webBeansContext, Class<? extends Annotation> clazz, Annotation[] annotationDefs)
    {
        name = clazz.getName();

        configAnnotations(clazz, webBeansContext, annotationDefs);
        
    }
    
    private void configAnnotations(Class<? extends Annotation> clazz, WebBeansContext webBeansContext, Annotation... annotations)
    {
        if(clazz.getAnnotation(Typed.class) != null)
        {
            if(logger.isLoggable(Level.WARNING))
            {
                logger.log(Level.WARNING, OWBLogConst.WARN_0016, clazz.getName());
            }            
        }

        final AnnotationManager annotationManager = webBeansContext.getAnnotationManager();
        Annotation[] qualifiers = annotationManager.getQualifierAnnotations(annotations);
        
        if(qualifiers != null)
        {
            for(Annotation qualifier : qualifiers)
            {
                if(qualifier.annotationType() == Default.class)
                {
                    return;
                }
                
                if(qualifier.annotationType() != Named.class)
                {
                    if(logger.isLoggable(Level.WARNING))
                    {
                        logger.log(Level.WARNING, OWBLogConst.WARN_0017, WebBeansLoggerFacade.args(clazz.getName(),qualifier.annotationType().getName()));
                    }
                }
            }            
        }
        
        if (AnnotationUtil.hasMetaAnnotation(annotations, NormalScope.class))
        {
            defaultScopeType = AnnotationUtil.getMetaAnnotations(annotations, NormalScope.class)[0];
        }

        if (AnnotationUtil.hasMetaAnnotation(annotations, Scope.class))
        {
            defaultScopeType = AnnotationUtil.getMetaAnnotations(annotations, Scope.class)[0];
        }

        if (annotationManager.hasInterceptorBindingMetaAnnotation(annotations))
        {
            Annotation[] ibs =
                annotationManager.getInterceptorBindingMetaAnnotations(annotations);
            for (Annotation ann : ibs)
            {
                interceptorBindingTypes.add(ann);
            }
        }

        if (annotationManager.hasStereoTypeMetaAnnotation(annotations))
        {
            Annotation[] isy =
                annotationManager.getStereotypeMetaAnnotations(annotations);

            Target outerStereo = clazz.getAnnotation(Target.class);
            for (Annotation is : isy)
            {
                Target innerStereo = is.annotationType().getAnnotation(Target.class);

                ElementType[] innerValues = innerStereo.value();
                ElementType[] outerValues = outerStereo.value();

                for (ElementType innerValue : innerValues)
                {
                    if (innerValue.equals(ElementType.METHOD) || innerValue.equals(ElementType.FIELD))
                    {
                        for (ElementType outerValue : outerValues)
                        {
                            if (outerValue.equals(ElementType.TYPE) && outerValues.length == 1)
                            {
                                throw new WebBeansConfigurationException("Inherited StereoType with class name : " + clazz.getName()
                                                                         + " must have compatible @Target annotation with Stereotype class name : " + clazz.getName());
                            }
                        }
                    }
                    else if (innerValue.equals(ElementType.TYPE) && innerValues.length == 1)
                    {
                        for (ElementType outerValue : outerValues)
                        {
                            if (outerValue.equals(ElementType.METHOD) || outerValue.equals(ElementType.FIELD))
                            {
                                throw new WebBeansConfigurationException("Inherited StereoType with class name : " + clazz.getName()
                                                                         + " must have compatible @Target annotation with Stereotype class name : " + clazz.getName());
                            }
                        }
                    }
                }

                inherits.add(is);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    public Annotation getDefaultDeploymentType()
    {
        return defaultDeploymentType;
    }

    /**
     * {@inheritDoc}
     */
    public Annotation getDefaultScopeType()
    {
        return defaultScopeType;
    }

    /**
     * {@inheritDoc}
     */    
    public Set<Annotation> getInterceptorBindingTypes()
    {
        return interceptorBindingTypes;
    }

    /**
     * {@inheritDoc}
     */    
    public Set<Annotation> getInheritedStereoTypes()
    {
        return inherits;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (!(obj instanceof StereoTypeModel))
        {
            return false;   
        }

        if(obj == null)
        {
            return false;
        }
        
        StereoTypeModel model = (StereoTypeModel) obj;

        return model.name.equals(name);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
