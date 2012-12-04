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
package org.apache.webbeans.context;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.SerializableBeanVault;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

/**
 *
 */
public class CustomPassivatingContextImpl extends CustomContextImpl
{
    private SerializableBeanVault sbv = null;

    CustomPassivatingContextImpl(Context context)
    {
        super(context);
    }

    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext)
    {
        return super.get(getSerializableBeanVault().getSerializableBean(contextual), creationalContext);
    }

    public <T> T get(Contextual<T> contextual)
    {
        return super.get(getSerializableBeanVault().getSerializableBean(contextual));
    }

    private SerializableBeanVault getSerializableBeanVault()
    {
        if (sbv == null)
        {
            sbv = WebBeansContext.getInstance().getSerializableBeanVault();
        }

        return sbv;
    }
}
