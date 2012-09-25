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
package org.apache.webbeans.jsf;

import org.apache.webbeans.config.WebBeansContext;

import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;

public class OwbApplicationFactory extends ApplicationFactory
{
    private ApplicationFactory wrapped;
    
    private volatile Application wrappedApplication;

    private WebBeansContext webBeansContext;
    
    public OwbApplicationFactory(ApplicationFactory applicationFactory)
    {
        wrapped = applicationFactory;
        webBeansContext = WebBeansContext.currentInstance();
    }

    @Override
    public Application getApplication()
    {
        if(!webBeansContext.getBeanManagerImpl().isInUse())
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
    public void setApplication(Application application)
    {
        if(!webBeansContext.getBeanManagerImpl().isInUse())
        {
            wrappedApplication = new OwbApplication(application);
        }

        wrapped.setApplication(application);
    }

    /* (non-Javadoc)
     * @see javax.faces.application.ApplicationFactory#getWrapped()
     */
    @Override
    public ApplicationFactory getWrapped()
    {
        return wrapped;
    }
    
    

}
