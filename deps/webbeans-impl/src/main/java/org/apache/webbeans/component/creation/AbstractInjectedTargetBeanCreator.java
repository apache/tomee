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

import java.util.Set;

import javax.enterprise.inject.spi.ObserverMethod;

import org.apache.webbeans.component.AbstractInjectionTargetBean;
import org.apache.webbeans.component.ProducerFieldBean;
import org.apache.webbeans.component.ProducerMethodBean;
import org.apache.webbeans.util.WebBeansAnnotatedTypeUtil;

/**
 * Abstract implementation of {@link InjectedTargetBeanCreator}.
 * 
 * @version $Rev: 1182847 $ $Date: 2011-10-13 15:31:37 +0200 (jeu., 13 oct. 2011) $
 *
 * @param <T> bean class type
 */
public abstract class AbstractInjectedTargetBeanCreator<T> extends AbstractBeanCreator<T> implements InjectedTargetBeanCreator<T>
{    
    /**
     * Creates a new instance.
     * 
     * @param bean bean instance
     */
    public AbstractInjectedTargetBeanCreator(AbstractInjectionTargetBean<T> bean)
    {
        super(bean, bean.getReturnType().getDeclaredAnnotations());
    }
    
 
    /**
     * {@inheritDoc}
     */
    public void defineDisposalMethods()
    {
        AbstractInjectionTargetBean bean = getBean();
        if(isDefaultMetaDataProvider())
        {

            bean.getWebBeansContext().getDefinitionUtil().defineDisposalMethods(getBean());
        }
        else
        {
            bean.getWebBeansContext().getAnnotatedTypeUtil().defineDisposalMethods(getBean(), getAnnotatedType());
        }
        
    }

    /**
     * {@inheritDoc}
     */
    public void defineInjectedFields()
    {
        AbstractInjectionTargetBean bean = getBean();
        if(isDefaultMetaDataProvider())
        {
            bean.getWebBeansContext().getDefinitionUtil().defineInjectedFields(bean);
        }
        else
        {
            WebBeansAnnotatedTypeUtil.defineInjectedFields(bean, getAnnotatedType());
        }
        
    }

    /**
     * {@inheritDoc}
     */
    public void defineInjectedMethods()
    {
        AbstractInjectionTargetBean bean = getBean();
        if(isDefaultMetaDataProvider())
        {
            bean.getWebBeansContext().getDefinitionUtil().defineInjectedMethods(bean);
        }
        else
        {
            bean.getWebBeansContext().getAnnotatedTypeUtil().defineInjectedMethods(bean, getAnnotatedType());
        }
        
    }

    /**
     * {@inheritDoc}
     */
    public Set<ObserverMethod<?>> defineObserverMethods()
    {   
        AbstractInjectionTargetBean bean = getBean();
        if(isDefaultMetaDataProvider())
        {
            return bean.getWebBeansContext().getDefinitionUtil().defineObserverMethods(bean, bean.getReturnType());
        }
        else
        {
            return bean.getWebBeansContext().getAnnotatedTypeUtil().defineObserverMethods(bean, getAnnotatedType());
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<ProducerFieldBean<?>> defineProducerFields()
    {
        AbstractInjectionTargetBean bean = getBean();
        if(isDefaultMetaDataProvider())
        {
            return bean.getWebBeansContext().getDefinitionUtil().defineProducerFields(bean);
        }
        else
        {
            return bean.getWebBeansContext().getAnnotatedTypeUtil().defineProducerFields(bean, getAnnotatedType());
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<ProducerMethodBean<?>> defineProducerMethods()
    {
        AbstractInjectionTargetBean bean = getBean();
        if(isDefaultMetaDataProvider())
        {
            return bean.getWebBeansContext().getDefinitionUtil().defineProducerMethods(bean);
        }
        else
        {
            return bean.getWebBeansContext().getAnnotatedTypeUtil().defineProducerMethods(bean, getAnnotatedType());
        }
    }
    
    /**
     * Return type-safe bean instance.
     */
    public AbstractInjectionTargetBean<T> getBean()
    {
        return (AbstractInjectionTargetBean<T>)super.getBean();
    }
}