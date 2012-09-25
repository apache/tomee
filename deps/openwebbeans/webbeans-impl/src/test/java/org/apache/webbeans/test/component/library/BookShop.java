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
package org.apache.webbeans.test.component.library;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.apache.webbeans.test.event.LoggedInEvent;

@RequestScoped
public class BookShop extends Business implements Shop<Book>
{

    public String shop()
    {
        return "shop";
    }

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception
    {
        return context.proceed();
    }
    
    public void observeSomething(@Observes LoggedInEvent lie)
    {
        // this is purely for checking if the Extension mechanism works
    }

}
