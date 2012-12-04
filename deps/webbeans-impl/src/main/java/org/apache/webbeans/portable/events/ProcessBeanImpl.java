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
package org.apache.webbeans.portable.events;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ProcessBean;

import org.apache.webbeans.config.WebBeansContext;

/**
 * Implementation of the {@link ProcessBean}.
 * 
 * @version $Rev: 1182847 $ $Date: 2011-10-13 15:31:37 +0200 (jeu., 13 oct. 2011) $
 *
 * @param <X> bean class info
 */
public  class ProcessBeanImpl<X> implements ProcessBean<X>
{
    /**Annotated instance. Can be AnnotatedType, AnnotatedMethod or AnnotatedField*/
    private final Annotated annotated;
    
    /**ManagedBean, SessionBean, ProducerMethodBean, ProducerFieldBean*/
    private final Bean<X> bean;
    
    protected ProcessBeanImpl(Bean<X> bean, Annotated annotated)
    {
        this.bean = bean;
        this.annotated = annotated;
    }
    
    /**
     * {@inheritDoc}
     */
    public void addDefinitionError(Throwable t)
    {
        WebBeansContext.getInstance().getBeanManagerImpl().getErrorStack().pushError(t);
    }

    /**
     * {@inheritDoc}
     */
    public Annotated getAnnotated()
    {
        return annotated;
    }

    /**
     * {@inheritDoc}
     */
    public Bean<X> getBean()
    {
        return bean;
    }

}
