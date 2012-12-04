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
package org.apache.webbeans.portable.events.generics;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ProcessSessionBean;
import javax.enterprise.inject.spi.SessionBeanType;

import org.apache.webbeans.portable.events.ProcessSessionBeanImpl;

@SuppressWarnings("unchecked")
public class GProcessSessionBean extends ProcessSessionBeanImpl implements GenericBeanEvent
{
    public GProcessSessionBean(Bean<Object> bean, AnnotatedType<?> annotatedType, String name, SessionBeanType type)
    {
        super(bean, annotatedType, name, type);
    }

    /**
     * This is an exceptional case due to the definition
     * ProcessSessionBean<X>  extends ProcessManagedBean<Object>
     *
     * If we are thinking of this event as a ProcessSessionBean then the bean class is X
     * but if we are thinking of it as a ProcessManagedBean or superclass then the bean class
     * is Object.  See https://issues.jboss.org/browse/CDITCK-215
     *
     * @param eventClass the class of event we are treating this event as
     * @return X.class or Object.class
     */
    public Class<?> getBeanClassFor(Class<?> eventClass)
    {
        if (ProcessSessionBean.class.isAssignableFrom(eventClass))
        {
            return getBean().getBeanClass();
        }
        else
        {
            return Object.class;
        }
    }
}
