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
import javax.transaction.UserTransaction;

import org.apache.webbeans.annotation.DefaultLiteral;
import org.apache.webbeans.annotation.DependentScopeLiteral;
import org.apache.webbeans.component.BuildInOwbBean;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.spi.TransactionService;

public class UserTransactionBean extends BuildInOwbBean<UserTransaction>
{

    public UserTransactionBean()
    {
        super(WebBeansType.USERTRANSACTION, UserTransaction.class);
        addApiType(Object.class);
        addApiType(UserTransaction.class);
        addQualifier(new DefaultLiteral());
        setImplScopeType(new DependentScopeLiteral());
    }

    @Override
    protected UserTransaction createActualInstance(CreationalContext<UserTransaction> creationalContext)
    {
        TransactionService transactionService = getWebBeansContext().getService(TransactionService.class);
        if(transactionService != null)
        {
            return transactionService.getUserTransaction();
        }
        
        return null;
    }

    @Override
    protected UserTransaction createInstance(CreationalContext<UserTransaction> creationalContext)
    {
        TransactionService transactionService = getWebBeansContext().getService(TransactionService.class);
        if(transactionService != null)
        {
            UserTransaction t = transactionService.getUserTransaction();
            return createProxyWrapper(t, creationalContext);
        }
        
        return null;
    }

    @Override
    public boolean isPassivationCapable()
    {
        return true;
    }
    
}
