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
package org.apache.webbeans.component;

import javax.enterprise.context.spi.CreationalContext;

import org.apache.webbeans.annotation.ApplicationScopeLiteral;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.portable.events.ExtensionLoader;

/**
 * Extension service bean definition.
 * 
 * @version $Rev: 1061122 $ $Date: 2011-01-20 05:46:59 +0100 (jeu., 20 janv. 2011) $
 *
 * @param <T> type info
 */
public class ExtensionBean<T> extends AbstractInjectionTargetBean<T>
{
    /**
     * Creates a new extesion bean.
     * 
     * @param returnType return type
     * @param webBeansContext
     */
    public ExtensionBean(Class<T> returnType, WebBeansContext webBeansContext)
    {
        super(WebBeansType.EXTENSION,returnType, webBeansContext);
        setImplScopeType(new ApplicationScopeLiteral());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected T createComponentInstance(CreationalContext<T> creationalContext)
    {
        ExtensionLoader loader = getWebBeansContext().getExtensionLoader();
        
        return loader.getBeanInstance(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void destroyComponentInstance(T instance, CreationalContext<T> creationalContext)
    {
        
    }
    
}