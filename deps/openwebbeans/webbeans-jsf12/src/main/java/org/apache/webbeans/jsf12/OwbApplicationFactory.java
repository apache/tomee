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
package org.apache.webbeans.jsf12;

import org.apache.webbeans.config.WebBeansContext;

import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;

public class OwbApplicationFactory extends ApplicationFactory
{
    private ApplicationFactory wrapped;
    
    private Application wrappedApplication;
    
    public OwbApplicationFactory(ApplicationFactory applicationFactory)
    {
        this.wrapped = applicationFactory;        
    }

    @Override
    public Application getApplication()
    {
        if(WebBeansContext.getInstance().getBeanManagerImpl().isInUse())
        {
            return wrapped.getApplication();
        }
        
        if(wrappedApplication == null)
        {
            wrappedApplication = new OwbApplication(wrapped.getApplication());
        }
        
        return wrappedApplication;
    }

    @Override
    public void setApplication(Application arg0)
    {
        this.wrapped.setApplication(arg0);
    }

}
