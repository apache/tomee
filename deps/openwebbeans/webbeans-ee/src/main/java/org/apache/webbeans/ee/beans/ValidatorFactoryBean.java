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
package org.apache.webbeans.ee.beans;

import javax.enterprise.context.spi.CreationalContext;
import javax.validation.ValidatorFactory;

import org.apache.webbeans.annotation.DefaultLiteral;
import org.apache.webbeans.annotation.DependentScopeLiteral;
import org.apache.webbeans.component.BuildInOwbBean;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.spi.ValidatorService;

public class ValidatorFactoryBean extends BuildInOwbBean<ValidatorFactory>
{

    public ValidatorFactoryBean()
    {
        super(WebBeansType.VALIDATIONFACT, ValidatorFactory.class);
        addApiType(Object.class);
        addApiType(ValidatorFactory.class);
        addQualifier(new DefaultLiteral());
        setImplScopeType(new DependentScopeLiteral());
    }

    @Override
    protected ValidatorFactory createInstance(CreationalContext<ValidatorFactory> creationalContext)
    {
        ValidatorService validatorService = getWebBeansContext().getService(ValidatorService.class);
        if(validatorService != null)
        {
            ValidatorFactory t = validatorService.getDefaultValidatorFactory();
            return createProxyWrapper(t, creationalContext);
        }
        
        return null;
    }

    @Override
    protected ValidatorFactory createActualInstance(CreationalContext<ValidatorFactory> creationalContext)
    {
        ValidatorService validatorService = getWebBeansContext().getService(ValidatorService.class);
        if(validatorService != null)
        {
            return validatorService.getDefaultValidatorFactory();
        }        
        return null;
    }
    
    @Override
    public boolean isPassivationCapable()
    {
        return true;
    }
    
}
