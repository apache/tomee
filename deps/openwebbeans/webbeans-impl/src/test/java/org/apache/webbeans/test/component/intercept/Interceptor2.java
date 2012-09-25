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
package org.apache.webbeans.test.component.intercept;

import javax.annotation.PostConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.apache.webbeans.test.component.PostConstructDoubleInterceptorComponent;

public class Interceptor2
{
    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception
    {
        System.out.println("Interceptor class : " + Interceptor2.class.getSimpleName());

        context.getContextData().put("key2", "value2");

        return context.proceed();
    }

    @PostConstruct
    public void construct(InvocationContext ctx) throws RuntimeException
    {
        try
        {
            String value = ctx.getContextData().get("key1").toString();
            PostConstructDoubleInterceptorComponent.setValue( value);

            ctx.proceed();

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
