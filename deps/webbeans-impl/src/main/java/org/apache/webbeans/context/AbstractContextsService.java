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

import java.lang.annotation.Annotation;

import javax.enterprise.context.ContextException;
import javax.enterprise.context.spi.Context;

import org.apache.webbeans.spi.ContextsService;

public abstract class AbstractContextsService implements ContextsService
{

    public void destroy(Object destroyObject)
    {
        //Default no-op
    }

    public void endContext(Class<? extends Annotation> scopeType, Object endParameters)
    {
        //Default no-op
    }

    public Context getCurrentContext(Class<? extends Annotation> scopeType)
    {
        
        return null;
    }

    public void init(Object initializeObject)
    {
        //Default no-op        
    }

    public void startContext(Class<? extends Annotation> scopeType, Object startParameter) throws ContextException
    {
        //Default no-op        
    }

    public boolean supportsContext(Class<? extends Annotation> scopeType)
    {        
        return false;
    }
    
    public void activateContext(Class<? extends Annotation> scopeType)
    {
        if(supportsContext(scopeType))
        {
            Context context = getCurrentContext(scopeType);
            if(context instanceof AbstractContext)
            {
                ((AbstractContext)context).setActive(true);
            }
        }
    }
    
    public void deActivateContext(Class<? extends Annotation> scopeType)
    {
        if(supportsContext(scopeType))
        {
            Context context = getCurrentContext(scopeType);
            if(context instanceof AbstractContext)
            {
                ((AbstractContext)context).setActive(false);
            }
        }        
    }
    
}
