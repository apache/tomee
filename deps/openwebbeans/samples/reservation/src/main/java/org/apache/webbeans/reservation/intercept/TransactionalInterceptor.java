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
package org.apache.webbeans.reservation.intercept;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.logging.Log;
import org.apache.webbeans.reservation.bindings.ApplicationLog;
import org.apache.webbeans.reservation.bindings.EntityManagerQualifier;
import org.apache.webbeans.reservation.bindings.intercep.Transactional;

@Interceptor
@Transactional
public class TransactionalInterceptor
{
    private @Inject @EntityManagerQualifier EntityManager entityManager;
    
    private @Inject @ApplicationLog Log logger; 
    
    @AroundInvoke
    public Object invoke(InvocationContext context) throws Exception
    {
        EntityTransaction transaction = entityManager.getTransaction();
        
        try
        {
            if(!transaction.isActive())
            {
                transaction.begin();    
            }
                        
            return context.proceed();
            
        }
        catch(Exception e)
        {
            logger.error("Exception in transactional method call", e);
            
            if(transaction != null)
            {
                transaction.rollback();
            }
            
            throw e;
            
        }
        finally
        {
            if(transaction != null && transaction.isActive())
            {
                transaction.commit();
            }
        }        
        
    }

}
