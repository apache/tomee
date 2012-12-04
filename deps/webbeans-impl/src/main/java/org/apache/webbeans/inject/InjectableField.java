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
package org.apache.webbeans.inject;

import java.lang.reflect.Field;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.exception.WebBeansException;

/**
 * Field type injection.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @since 1.0
 */
public class InjectableField extends AbstractInjectable
{
    protected Field field;
    protected Object instance;

    public InjectableField(Field field, Object instance, AbstractOwbBean<?> owner,CreationalContext<?> creationalContext)
    {
        super(owner,creationalContext);
        this.field = field;
        this.instance = instance;
        injectionMember = field;
    }

    public Object doInjection()
    {
        try
        {
            InjectionPoint injectedField = getInjectedPoints(field).get(0);
            
            if (!field.isAccessible())
            {
                injectionOwnerBean.getWebBeansContext().getSecurityService().doPrivilegedSetAccessible(field, true);
            }

            Object object = inject(injectedField);
            
            field.set(instance, object);

        }
        catch (IllegalAccessException e)
        {
            throw new WebBeansException(e);
        }

        return null;
    }
}
