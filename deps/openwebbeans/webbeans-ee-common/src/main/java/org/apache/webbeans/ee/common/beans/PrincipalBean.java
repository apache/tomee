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
package org.apache.webbeans.ee.common.beans;

import java.security.Principal;

import javax.enterprise.context.spi.CreationalContext;

import org.apache.webbeans.annotation.DefaultLiteral;
import org.apache.webbeans.annotation.DependentScopeLiteral;
import org.apache.webbeans.component.BuildInOwbBean;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.spi.SecurityService;

public class PrincipalBean extends BuildInOwbBean<Principal>
{

    public PrincipalBean()
    {
        super(WebBeansType.PRINCIPAL, Principal.class);
        addApiType(Object.class);
        addApiType(Principal.class);
        addQualifier(new DefaultLiteral());
        setImplScopeType(new DependentScopeLiteral());
    }

    @Override
    protected Principal createInstance(CreationalContext<Principal> creationalContext)
    {
        SecurityService securityService = getWebBeansContext().getService(SecurityService.class);
        if(securityService != null)
        {
            Principal t = securityService.getCurrentPrincipal();
            return createProxyWrapper(t, creationalContext);
        }
        
        return null;
    }

    @Override
    protected Principal createActualInstance(CreationalContext<Principal> creationalContext)
    {
        SecurityService securityService = getWebBeansContext().getService(SecurityService.class);
        if(securityService != null)
        {
            return securityService.getCurrentPrincipal();
        }
        
        return null;
    }

    @Override
    public boolean isPassivationCapable()
    {
        return true;
    }
    
}
