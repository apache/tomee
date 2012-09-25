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

import java.lang.annotation.Annotation;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.container.ExternalScope;
import org.apache.webbeans.deployment.StereoTypeModel;

/**
 * Events that is fired before container starts to discover beans.
 * 
 * @version $Rev$ $Date$
 *
 */
public class BeforeBeanDiscoveryImpl implements BeforeBeanDiscovery
{
    
    private BeanManagerImpl beanManager = null;
    private final WebBeansContext webBeansContext;

    public BeforeBeanDiscoveryImpl(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
        beanManager = this.webBeansContext.getBeanManagerImpl();
    }
    
    /**
     * {@inheritDoc}
     */
    public void addAnnotatedType(AnnotatedType<?> type)
    {
        beanManager.addAdditionalAnnotatedType(type);
    }

    /**
     * {@inheritDoc}
     */
    public void addQualifier(Class<? extends Annotation> qualifier)
    {
        beanManager.addAdditionalQualifier(qualifier);
        
    }

    /**
     * {@inheritDoc}
     */
    public void addInterceptorBinding(Class<? extends Annotation> binding, Annotation... bindingDef)
    {
        webBeansContext.getBeanManagerImpl().addInterceptorBindingType(binding, bindingDef);
    }

    /**
     * {@inheritDoc}
     */
    public void addScope(Class<? extends Annotation> scope, boolean normal, boolean passivating)
    {
        ExternalScope additionalScope = new ExternalScope(scope, normal, passivating); 
        beanManager.addAdditionalScope(additionalScope);
    }

    /**
     * {@inheritDoc}
     */
    public void addStereotype(Class<? extends Annotation> stereotype, Annotation... stereotypeDef)
    {
        webBeansContext.getAnnotationManager().checkStereoTypeClass(stereotype, stereotypeDef);
        StereoTypeModel model = new StereoTypeModel(webBeansContext, stereotype, stereotypeDef);
        webBeansContext.getStereoTypeManager().addStereoTypeModel(model);
    }

}