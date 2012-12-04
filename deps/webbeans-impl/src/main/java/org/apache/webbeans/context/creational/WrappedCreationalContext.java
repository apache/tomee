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
package org.apache.webbeans.context.creational;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.apache.webbeans.config.WebBeansContext;

class WrappedCreationalContext<T> extends CreationalContextImpl<T> implements CreationalContext<T>
{
    private static final long serialVersionUID = 3580925478881669439L;
    
    private CreationalContext<T> wrapped = null;    

    WrappedCreationalContext(Contextual<T> contextual, CreationalContext<T> creationalContext,
                             WebBeansContext webBeansContext)
    {
        super(contextual, webBeansContext);
        wrapped = creationalContext;
    }

    @Override
    public void push(T instance)
    {
        super.push(instance);
        wrapped.push(instance);
    }

    @Override
    public void release()
    {
        super.release();
        wrapped.release();
    }
    
}
