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

import javax.enterprise.context.RequestScoped;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;


@RequestScoped
@Interceptors(value = { Interceptor1.class })
public class MethodInterceptedWithoutClassInterceptorsComponent
{

    public Object hello(Integer ss)
    {
        return ss;
    }

    @Interceptors(value = { Interceptor2.class })
    @ExcludeClassInterceptors
    public Object intercept(InvocationContext context) throws Exception
    {
        context.setParameters(new Object[] { new Integer(5) });

        return null;
    }

}
