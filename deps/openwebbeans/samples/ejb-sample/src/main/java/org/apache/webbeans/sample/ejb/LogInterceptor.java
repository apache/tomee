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
package org.apache.webbeans.sample.ejb;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor @LogInterceptorBinding
public class LogInterceptor
{

    @AroundInvoke
    public Object log(InvocationContext context) throws Exception
    {
        System.out.println("Calling method : " + context.getMethod().getName() + " at time " + new Date());
        return context.proceed();
    }
    
    @PostConstruct
    public void postConstruct(InvocationContext context)
    {
        System.out.println("Post Construct with OWB interceptor");
    }
    
    @PreDestroy
    public void preDestroy(InvocationContext context)
    {
        System.out.println("Pre Destroy with OWB interceptor");
    }
    
}
