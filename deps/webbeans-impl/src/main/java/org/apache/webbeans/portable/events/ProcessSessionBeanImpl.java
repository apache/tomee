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

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ProcessSessionBean;
import javax.enterprise.inject.spi.SessionBeanType;

/**
 * Implementation of {@link ProcessSessionBean}.
 * 
 * @version $Rev: 1182847 $ $Date: 2011-10-13 15:31:37 +0200 (jeu., 13 oct. 2011) $
 *
 * @param <X> ejb class info
 */
public class ProcessSessionBeanImpl<X> extends ProcessBeanImpl<Object> implements ProcessSessionBean<X>
{
    /**Session bean annotated type*/
    private final AnnotatedType<Object> annotatedBeanClass;
    
    /**Ejb name*/
    private final String ejbName;
    
    /**Session bean type*/
    private final SessionBeanType type;

    public ProcessSessionBeanImpl(Bean<Object> bean, AnnotatedType<Object> annotatedType, String name, SessionBeanType type)
    {
        super(bean, annotatedType);
        annotatedBeanClass = annotatedType;
        ejbName = name;
        this.type = type;
    }
    

    /**
     * {@inheritDoc}
     */
    public String getEjbName()
    {
        return ejbName;
    }
    
    /**
     * {@inheritDoc}
     */
    public SessionBeanType getSessionBeanType()
    {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    public AnnotatedType<Object> getAnnotatedBeanClass()
    {
        return annotatedBeanClass;
    }

}
